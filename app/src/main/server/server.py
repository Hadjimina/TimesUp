import socket
import threading
import socketserver
import _thread
import json
import queue
import random


class RequestHandler(socketserver.BaseRequestHandler):
    def handle(self):
        # Read the size of the message
        size = int(self.request.recv(4))

        # Read the message
        receivedData = self.request.recv(size).strip()

        # Turn into a JSON (a dict)
        d = json.load(receivedData)

        # Get requestType (None if non-existent)
        requestType = d.get("requestType")

        # Case distinction on the type of request

        # Case 0: No requestType specified --> return error message
        if requestType is None:
            message = encodeErrorMessage(requestType="",
                                         errorMessage="Wrong format (no requestType)")
            self.request.sendall(message.encode())
            return

        # Case 1: New game requested
        elif requestType == "newGame":

            # Create a "random" game id
            gameId = random.randrange(0, 9999)
            while gameId not in games.keys():
                gameId = random.randrange(0, 9999)

            # Create a new empty queue for the new gameId
            games[gameId] = queue.Queue()
            b = d["body"]

            # Creating a new game starts a new gameThread
            try:
                newGameThread = threading.Thread(target=gameThread,
                                                 args=(gameId,
                                                       self.request,
                                                       b["rounds"],
                                                       b["teamName1"],
                                                       b["teamName2"],
                                                       b["timePerRound"],
                                                       b["username"],
                                                       b["wordsPerPerson"]))

            # In case any of the lookups would fail --> return error message
            except KeyError:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="Wrong format in the body")
                self.request.sendall(message.encode())
                return

            # Start new thread and let it run after handler has returned
            newGameThread.daemon = True
            newGameThread.start()

        # Case 2: A message for an existing game was received.
        else:
            # Test if no gameId was specified --> return error message
            gameId = d.get("gameId")
            if gameId is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="gameId not found")
                self.request.sendall(message.encode())
                return

            # We already have a game with that gameId --> put data into gameQueue
            else:
                games[gameId].put((d, self.client_address, self.request))


def periodicLog(clientQueues=None):
    print("currently connected are:")
    for k in clientQueues.keys():
        print(k)


def clientThread(queue, request):

    # Initialize fields
    team = 0

    while True:
        (messageType, content) = queue.get()
        if messageType == "teamJoin":
            team = content;


def gameThread(gameId, hostRequest, rounds, teamName1, teamName2, timePerRound,
               hostUsername, wordsPerPerson):
    # Send back the gameId
    message = encodeJoinMessage(gameId, 0, teamName1, teamName2)
    hostRequest.sendall(message.encode())

    users = list()  # This is the list of users in this game
    team1 = list()  # Players of team1 (clientId)
    team2 = list()  # Players of team2 (clientId)
    hasSubmittedWords = dict()  # Players that have already submitted words
    globalWordList = list()  # List of all words
    readyCount = 0  # The number of players that are ready
    hasStarted = False  # Indicates if the game has already started
    startTime = -1  # Indicates the time the game has started (-1 if not yet)

    # Create a dict of pairs of queues, that handle the communication between the
    # game thread and the client threads.
    clientQueues = dict()

    # Host is the first user
    userCount = 0
    users.append((hostUsername, userCount))

    # Initialize empty queues for communication
    clientQueues[userCount] = queue.Queue()
    hostThread = threading.Thread(target=clientThread,
                                  args=(clientQueues[userCount], hostRequest))
    hostThread.start()

    # Increase Id counter
    userCount += 1

    while True:
        # Wait for something to be written in the queue
        (newMessage, clientAddress, clientRequest) = games[gameId].get()

        # Handle internal messages
        if clientRequest is None:
            continue

        # Handle the new message
        requestType = newMessage.get("requestType")

        if requestType is None:
            message = encodeErrorMessage(requestType="",
                                         errorMessage="Wrong format (no requestType)")
            clientRequest.sendall(message.encode())
            continue

        elif requestType == "join":
            print("new connection received to {}".format(clientAddress))

            username = newMessage.get("username")
            if username is None:
                message = encodeErrorMessage(requestType="",
                                             errorMessage="Wrong format (no username specified)")
                clientRequest.sendall(message.encode())
                continue

            users.append((username, userCount))

            # Initialize empty queues for communication
            clientQueues[userCount] = queue.Queue()

            # Send back the acknowledgement
            ackMessage = encodeJoinMessage(gameId, userCount, teamName1, teamName2)
            clientRequest.sendall(ackMessage.encode())

            # Create a new user thread and start
            newUserThread = threading.Thread(target=clientThread,
                                             args=(clientQueues[userCount], clientRequest))
            newUserThread.start()
            continue

        elif requestType == "joinTeam":

            # Make sure the clientId was sent
            clientId = newMessage.get("clientId")
            if clientId is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="Wrong format (no clientId)")
                clientRequest.sendall(message.encode())
                continue

            # Make sure the clientId is known
            receiveQueue = clientQueues.get(clientId)
            if receiveQueue is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="Wrong format (clientId not saved)")
                clientRequest.sendall(message.encode())
                continue

            # Make sure the message has a body
            body = newMessage.get("body")
            if body is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="Wrong format (no body)")
                clientRequest.sendall(message.encode())
                continue

            # Make sure the body has teamToJoin
            temp = body.get("teamToJoin")
            if temp is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="Wrong format (no teamToJoin)")
                clientRequest.sendall(message.encode())
                continue

            # Set the team
            elif temp == 1 and clientId not in team1:
                team1.append(clientId)
                receiveQueue.put(("teamJoin", 1))
            elif temp == 2 and clientId not in team2:
                team2.append(clientId)
                receiveQueue.put(("teamJoin", 2))
            else:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="Wrong format (unknown team)")
                clientRequest.sendall(message.encode())
                continue

        elif requestType == "ready":
            # Make sure the message has a clientId
            clientId = newMessage.get(clientId)
            if clientId is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="Wrong format (no clientId)")
                clientRequest.sendall(message.encode())
                continue

            # Make sure the message has a body
            body = newMessage.get("body")
            if body is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="Wrong format (no body)")
                clientRequest.sendall(message.encode())
                continue

            wordList = body.get("wordList")
            if wordList is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="Wrong format (no wordList)")
                clientRequest.sendall(message.encode())
                continue

            # Make sure client has not yet submitted words
            if clientId in hasSubmittedWords.keys():
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="Wrong format (already submitted words)")
                clientRequest.sendall(message.encode())
                continue

            # Make sure the clientId is known
            receiveQueue = clientQueues.get(clientId)
            if receiveQueue is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="Wrong format (clientId not saved)")
                clientRequest.sendall(message.encode())
                continue

            # Save words to the dict and wait
            hasSubmittedWords[clientId] = wordList
            receiveQueue.put(("ready", True))

            # One more player is ready
            readyCount += 1

            # Check if the game is already running
            if hasStarted:
                message = encodeTeamJoinAck(timePerRound=timePerRound,
                                            clientId=clientId,
                                            gameId=gameId,
                                            wordsPerPerson=wordsPerPerson,
                                            hasStarted=True,
                                            startTime=startTime)
                clientRequest.sendall(message.encode())
                continue
            else:
                message = encodeTeamJoinAck(timePerRound=timePerRound,
                                            clientId=clientId,
                                            gameId=gameId,
                                            wordsPerPerson=wordsPerPerson)
                clientRequest.sendall(message.encode())

            # Check if game can be started
            if readyCount == userCount:

                # Make the global list of words
                for words in hasSubmittedWords.items():
                    globalWordList.append(words)

                # Todo Start game

        elif requestType == "unready":
            # Make sure the message has a clientId
            clientId = newMessage.get(clientId)
            if clientId is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="Wrong format (no clientId)")
                clientRequest.sendall(message.encode())
                continue

            # Make sure player was ready
            if clientId not in hasSubmittedWords.keys():
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="Cannot unready before being ready")
                clientRequest.sendall(message.encode())
                continue

            # Delete items from that player
            del hasSubmittedWords[clientId]

            # One less player is ready
            readyCount -= 1

        elif requestType == "ack":
            pass
        elif requestType == "nextRound":
            pass
        elif requestType == "roundFinished":
            pass
        else:
            message = encodeErrorMessage(requestType=requestType,
                                         errorMessage="Unknown requestType")
            clientRequest.sendall(message.encode())

            # Loop all over again


# JSON encoding functions

def encodeJoinMessage(gameId, clientId, teamName1, teamName2, teamId1=1, teamId2=2):
    message = dict()
    message["returnType"] = "ACK"
    message["requestType"] = "newGame"
    message["gameId"] = gameId
    message["clientId"] = clientId
    body = dict()
    body["teamName1"] = teamName1
    body["teamName2"] = teamName2
    body["teamId1"] = teamId1
    body["teamId2"] = teamId2
    message["body"] = body
    return json.dumps(message)


def encodeErrorMessage(requestType, errorMessage, gameId=-1, clientId=-1):
    message = dict()
    message["returnType"] = "error"
    message["requestType"] = requestType
    message["gameId"] = gameId
    message["clientId"] = clientId
    body = dict()
    body["errorType"] = errorMessage
    message["body"] = body
    return json.dumps(message)


def encodeTeamJoinAck(timePerRound, gameId, clientId, wordsPerPerson, hasStarted=False, startTime=-1):
    message = dict()
    message["returnType"] = "ACK"
    message["requestType"] = "teamJoin"
    message["gameId"] = gameId
    message["clientId"] = clientId
    body = dict()
    body["hasStarted"] = hasStarted
    body["startTime"] = startTime
    body["timePerRound"] = timePerRound
    body["wordsPerPerson"] = wordsPerPerson
    message["body"] = body
    return json.dumps(message)


if __name__ == "__main__":
    # Dict that maps gameIDs to queues
    games = dict()

    # Default values
    HOST = ""
    PORT = 9999

    # Wait for incoming connections and start a new thread RequestHandler that
    # handles the request
    with socketserver.ThreadingTCPServer((HOST, PORT), RequestHandler) as server:
        server.serve_forever()
