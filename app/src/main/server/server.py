import time
import threading
import socketserver
import json
import queue
import random
import select
from collections import deque
import colorama


class RequestHandler(socketserver.BaseRequestHandler):

    def handle(self):

        ipAddress, port = self.client_address

        print(colorama.Fore.GREEN + "new connection to {} at port {}".format(ipAddress, port))

        try:
            # Read the message
            length = int(self.request.recv(4).decode())
        except ValueError:
            message = encodeErrorMessage(requestType="",
                                         errorMessage="Wrong format (no requestType)")
            self.request.sendall(message.encode())
            return

        print(colorama.Style.DIM + "length {}".format(length))

        receivedData = self.request.recv(length)

        print(colorama.Style.DIM + "received {}".format(receivedData.decode()))

        # Turn into a JSON (a dict)
        d = json.loads(receivedData.decode())

        # Get requestType (None if non-existent)
        requestType = d.get("requestType")

        # Case 0: No requestType specified --> return error message
        if requestType is None:
            message = encodeErrorMessage(requestType="",
                                         errorMessage="Wrong format (no requestType)")
            self.request.sendall(message.encode())
            return

        # Case 1: New game requested
        elif requestType == "newGame":
            print(colorama.Style.DIM + "received requestType {}".format(requestType))

            # Create a "random" game id
            gameId = random.randrange(0, 9999)
            while gameId in games.keys():
                gameId = random.randrange(0, 9999)

            print(colorama.Fore.GREEN + "created new gameId {}".format(gameId))

            # Create a new empty queue for the new gameId
            games[gameId] = dict()
            gameQueues[gameId] = queue.Queue()

            # Read out all values from the request
            try:
                body = d["body"]
                rounds = body["rounds"]
                teamName1 = body["teamName1"]
                teamName2 = body["teamName2"]
                timePerRound = body["timePerRound"]
                wordsPerPerson = body["wordsPerPerson"]
                username = body["username"]

            # In case any of the lookups would fail --> return error message
            except KeyError:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="Wrong format in the body",
                                             gameId=gameId)
                self.request.sendall(message.encode())
                return

            # Creating a new game starts a new gameThread
            newGameThread = threading.Thread(target=gameThread,
                                             args=(gameId,
                                                   rounds,
                                                   teamName1,
                                                   teamName2,
                                                   timePerRound,
                                                   wordsPerPerson))

            # Put team names into the dict
            games[gameId]["teamName1"] = teamName1
            games[gameId]["teamName2"] = teamName2

            # Start new thread
            newGameThread.start()

            print(colorama.Style.DIM + "started gameThread")

            # The host has clientId 0
            clientId = 0

            # Setup communication queue
            games[gameId][0] = queue.Queue()

            # Send back the gameId (host has clientId 0)
            message = encodeJoinMessage(gameId, clientId, teamName1, teamName2, 1, 2, port, requestType)
            self.request.sendall(message.encode())

            # Initialize game thread communication queue
            games[gameId][0] = queue.Queue()

            # Send username and clientId to gameThread
            gameQueues[gameId].put(("newClient", username, clientId))

            # Do client logic
            client(self.request, gameId, clientId)

        # Case 2: New client connection to game
        elif requestType == "join":
            print(colorama.Style.DIM + "received requestType {}".format(requestType))

            # Test if gameId is valid
            gameId = d.get("gameId")
            if gameId is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="no gameId specified")
                self.request.sendall(message.encode())
                return
            if gameId not in games.keys():
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="gameId not known",
                                             gameId=gameId)
                self.request.sendall(message.encode())
                return

            # Get the next clientId
            clientId = games[gameId].get("userCount")
            if clientId is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="userCount not known",
                                             gameId=gameId)
                self.request.sendall(message.encode())
                return
            games[gameId]["userCount"] += 1

            # Get the teamNames
            teamName1 = games[gameId].get("teamName1")
            teamName2 = games[gameId].get("teamName2")
            if teamName1 is None or teamName2 is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="teamNames not set",
                                             gameId=gameId,
                                             clientId=clientId)
                self.request.sendall(message.encode())
                return

            # Get username
            body = d.get("body")
            if body is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="no body sent",
                                             gameId=gameId,
                                             clientId=clientId)
                self.request.sendall(message.encode())
                return
            username = body.get("username")
            if username is None:
                message = encodeErrorMessage(requestType=requestType,
                                             errorMessage="no username specified",
                                             gameId=gameId,
                                             clientId=clientId)
                self.request.sendall(message.encode())
                return

            # Setup communication queue
            games[gameId][clientId] = queue.Queue()

            # Send ACK back the client
            message = encodeJoinMessage(gameId, clientId, teamName1, teamName2, 1, 2, port, requestType)
            self.request.sendall(message.encode())

            # Send username and clientId to gameThread
            gameQueues[gameId].put(("newClient", username, clientId))

            # Do client logic
            client(self.request, gameId, clientId)

        # Case 3: A message for an existing game was received.
        else:
            message = encodeErrorMessage(requestType=requestType,
                                         errorMessage="unknown requestType")
            self.request.sendall(message.encode())
            return


def client(request, gameId, clientId):
    socket_active = True
    while True:

        if socket_active:

            # See for changes for TIMEOUT long
            r, w, x = select.select([request], [], [], TIMEOUT)
            if r:
                # If something changed, read
                try:
                    length = int(request.recv(4).decode())
                except ValueError:
                    gameQueues[gameId].put(("clientLost", None, clientId))
                    return

                print(colorama.Style.DIM + "length {}".format(length))

                message = request.recv(length)

                print(colorama.Style.DIM + "received {}".format(message.decode()))

                # If the message is None, the socket is broken
                if not message:
                    socket_active = False
                else:

                    # Function that handles the message
                    handleClientMessage(request, message.decode(), gameId, clientId)
        try:
            # Get an item from the queue
            item = games[gameId][clientId].get_nowait()

            # Ignore empty items
            if item is None:
                pass
            else:

                # Function that handles the item
                handleQueueItem(request, item, gameId, clientId)

        # If the queue was empty, do nothing
        except queue.Empty:
            pass

        # If socket is broken, stop
        if not socket_active:

            # Tell the game that the connection to the client has been closed
            gameQueues[gameId].put(("clientLost", None, clientId))
            break


def handleClientMessage(request, rawMessage, gameId, clientId):
    message = json.loads(rawMessage)

    messageType = None
    # Get fields from message
    try:
        requestType = message["requestType"]
        if gameId == message["gameId"] and clientId == message["clientId"]:
            body = message["body"]
        else:
            message = encodeErrorMessage(requestType=requestType,
                                         errorMessage="gameId or clientId do not match",
                                         gameId=gameId,
                                         clientId=clientId)
            request.sendall(message.encode())
            return False
    except KeyError:
        if messageType is None:
            message = encodeErrorMessage(requestType="",
                                         errorMessage="failed to parse message",
                                         gameId=gameId,
                                         clientId=clientId)
            request.sendall(message.encode())
            return False
        else:
            message = encodeErrorMessage(requestType=messageType,
                                         errorMessage="failed to parse message",
                                         gameId=gameId,
                                         clientId=clientId)
            request.sendall(message.encode())
            return False

    if requestType == "joinTeam":

        # Find out which team to join
        teamToJoin = body.get("teamToJoin")
        if teamToJoin is None:
            message = encodeErrorMessage(requestType=requestType,
                                         errorMessage="teamToJoin unspecified",
                                         gameId=gameId,
                                         clientId=clientId)
            request.sendall(message.encode())
            return False

        if teamToJoin == 1 or teamToJoin == 2:

            # Tell the gameThread what team the player wants to join
            print(colorama.Fore.GREEN + "player {} wants to join team {}".format(clientId, teamToJoin))
            gameQueues[gameId].put(("teamToJoin", teamToJoin, clientId))
        else:
            message = encodeErrorMessage(requestType=requestType,
                                         errorMessage="teamToJoin must be 1 or 2",
                                         gameId=gameId,
                                         clientId=clientId)
            request.sendall(message.encode())
            return False

    elif requestType == "ready":

        # Make sure wordList is valid
        wordList = body.get("wordList")
        if wordList is None:
            message = encodeErrorMessage(requestType=requestType,
                                         errorMessage="wordList missing",
                                         gameId=gameId,
                                         clientId=clientId)
            request.sendall(message.encode())
            return False
        if not isinstance(wordList, list):
            message = encodeErrorMessage(requestType=requestType,
                                         errorMessage="wordList is not a list",
                                         gameId=gameId,
                                         clientId=clientId)
            request.sendall(message.encode())
            return False

        # Forward to gameThread
        gameQueues[gameId].put((requestType, wordList, clientId))

    elif requestType == "ack":

        # Forward to gameThread
        gameQueues[gameId].put((requestType, None, clientId))

    elif requestType == "nextRound":
        print("put nextRound into Queue")

        # Forward to gameThread
        gameQueues[gameId].put((requestType, None, clientId))

    elif requestType == "roundFinished":

        # Check message validity
        phaseNumber = body.get("phaseNumber")
        if phaseNumber is None:
            message = encodeErrorMessage(requestType=requestType,
                                         errorMessage="phaseNumber is missing",
                                         gameId=gameId,
                                         clientId=clientId)
            request.sendall(message.encode())
            return False

        wordIndex = body.get("wordIndex")
        if wordIndex is None:
            message = encodeErrorMessage(requestType=requestType,
                                         errorMessage="wordIndex is missing",
                                         gameId=gameId,
                                         clientId=clientId)
            request.sendall(message.encode())
            return False

        # Forward to gameThread
        gameQueues[gameId].put((requestType, (phaseNumber, wordIndex), clientId))
    else:
        message = encodeErrorMessage(requestType=requestType,
                                     errorMessage="unknown requestType",
                                     gameId=gameId,
                                     clientId=clientId)
        request.sendall(message.encode())
        return False


def handleQueueItem(request, item, gameId, clientId):
    (requestType, data) = item

    if requestType == "teamJoinAck":
        [hasStarted, startTime, timePerRound, wordsPerPerson] = data
        message = encodeTeamJoinAck(gameId=gameId,
                                    clientId=clientId,
                                    hasStarted=hasStarted,
                                    startTime=startTime,
                                    timePerRound=timePerRound,
                                    wordsPerPerson=wordsPerPerson)
        request.sendall(message.encode())

    elif requestType == "error":
        [errorMessage, requestTypeThatFailed] = data
        message = encodeErrorMessage(requestType=requestTypeThatFailed,
                                     errorMessage=errorMessage,
                                     gameId=gameId,
                                     clientId=clientId)
        request.sendall(message.encode())

    elif requestType == "ack":
        ackResponse = data
        message = encodeAckMessage(requestType=ackResponse,
                                   gameId=gameId,
                                   clientId=clientId)
        request.sendall(message.encode())

    elif requestType == "setup":
        wordList = data
        message = encodeSetupMessage(gameId=gameId,
                                     clientId=clientId,
                                     wordList=wordList)
        request.sendall(message.encode())

    elif requestType == "startRound":
        print("Get startRound from Queue")
        [startTime, activeTeam, activePlayerId, activePlayerName, phaseNumber, wordIndex] = data
        message = encodeStartRoundMessage(gameId=gameId,
                                          clientId=clientId,
                                          startTime=startTime,
                                          activeTeam=activeTeam,
                                          activePlayerId=activePlayerId,
                                          activePlayerName=activePlayerName,
                                          phaseNumber=phaseNumber,
                                          wordIndex=wordIndex)
        request.sendall(message.encode())

    elif requestType == "roundFinished":
        [scoreTeam1, scoreTeam2, nextPlayerId, nextPlayerName, nextPhase] = data
        message = encodeRoundFinishedMessage(gameId=gameId,
                                             clientId=clientId,
                                             scoreTeam1=scoreTeam1,
                                             scoreTeam2=scoreTeam2,
                                             nextPlayerId=nextPlayerId,
                                             nextPlayerName=nextPlayerName,
                                             nextPhase=nextPhase)
        request.sendall(message.encode())

        # Check if the phase was the last one
        if nextPhase == -1:
            return


def gameThread(gameId, rounds, teamName1, teamName2, timePerRound, wordsPerPerson):
    users = list()  # This is the list of users in this game
    usernames = dict()  # This contains the mapping between the clientIds and the usernames
    team1 = deque()  # Players of team1 (clientId)
    team2 = deque()  # Players of team2 (clientId)
    submittedWords = dict()  # Players that have already submitted words
    globalWordList = list()  # List of all words
    readyCount = 0  # The number of players that are ready
    hasStarted = False  # Indicates if the game has already started
    startTime = -1  # Indicates the time the game has started (-1 if not yet)
    userCount = 0  # Indicated how many users are currently connected
    phaseNumber = -1  # Specified which phase the game is currently in
    wordIndex = -1  # Specified which word is currently the next
    scoreTeam1 = 0  # Current score for team 1
    scoreTeam2 = 0  # Current score for team 2
    activeTeam = -1  # Specifies which team is currently active
    activePlayerId = -1  # Specifies which player is currently active
    activePlayerName = ""  # Specifies which player (name) is currently active
    activePlayer = -1  # Specifies which player is active next
    nextPhase = 0  # Specified the next phase to be played
    phases = list()  # A list of all played phases in this game

    # Initialization
    print("rounds is {}".format(rounds))
    print("round[0] is {}".format(rounds[0]))
    print("round[1] is {}".format(rounds[1]))
    print("round[2] is {}".format(rounds[2]))
    print("round[3] is {}".format(rounds[3]))
    print("round[4] is {}".format(rounds[4]))

    # Find starting phase
    if rounds[0]:
        phases.append(1)
    if rounds[1]:
        phases.append(2)
    if rounds[2]:
        phases.append(3)
    if rounds[3]:
        phases.append(4)
    if rounds[4]:
        phases.append(5)

    print("phases is {}".format(phases))

    while True:

        # Wait for something to be written in the queue
        (messageType, data, clientId) = gameQueues[gameId].get()

        print(colorama.Fore.YELLOW + "getting {} from client {} with content {}".format(messageType, clientId, data))

        if messageType == "teamToJoin":

            # Make sure player is not already in a team
            if clientId in team1 or clientId in team2:
                games[gameId][clientId].put(("error", ["player is already in a team", messageType]))

            # Put player into the team (can only be 1 or 2)
            if data == 1:
                team1.append(clientId)
            elif data == 2:
                team2.append(clientId)
            # Acknowledge the team assignment
            games[gameId][clientId].put(("teamJoinAck", [hasStarted, startTime, timePerRound, wordsPerPerson]))

        elif messageType == "ready":
            print("team1 is {} and team2 is {}".format(team1, team2))

            # Check if the client has already submitted words
            if clientId in submittedWords.keys():
                games[gameId][clientId].put(("error", ["client has already submitted words", messageType]))
            elif clientId not in usernames.keys():
                games[gameId][clientId].put(("error", ["client has not submitted username", messageType]))
            elif (clientId not in team1) and (clientId not in team2):
                games[gameId][clientId].put(("error", ["client {} is not in a team".format(clientId), messageType]))
            else:

                # Add the words to the dict
                submittedWords[clientId] = data

                # One more player is ready
                readyCount += 1

                # Acknowledge wordList
                games[gameId][clientId].put(("ack", "ready"))

                # Test if everybody is ready
                if userCount == readyCount:

                    for key, words in submittedWords.items():
                        globalWordList.append(words)

                    # Get a permutation the word list and player lists
                    random.shuffle(globalWordList)
                    random.shuffle(team1)
                    random.shuffle(team2)

                    # Wait to ensure all clients are ready
                    time.sleep(0.5)

                    # Give a notification to all users
                    for user in users:
                        games[gameId][user].put(("setup", globalWordList))

                    # Non-deterministic choice which team starts
                    if bool(random.getrandbits(1)):
                        activeTeam = 1
                        prevPlayer = team2[0]
                    else:
                        activeTeam = 2
                        prevPlayer = team1[0]

                    # Get the first phase
                    phaseNumber = -1

                    # Start from the beginning
                    wordIndex = 0

                    # Put the fake "roundFinished" in the queue
                    gameQueues[gameId].put(("roundFinished", (phaseNumber, wordIndex), prevPlayer))

                    # And read it out of the queue
                    continue

        elif messageType == "ack":

            # Do we really need this?
            pass

        elif messageType == "nextRound":
            print("Get nextRound from Queue")

            # Get the current time
            startTime = int(round(time.time()*1000))

            # Send start signal to all users
            for user in users:
                print("Putting startRound in Queue for user {}".format(user))
                games[gameId][user].put(("startRound",
                                        [startTime, activeTeam, activePlayerId, activePlayerName, phaseNumber, wordIndex]))

        elif messageType == "roundFinished":
            (newPhaseNumber, newWordIndex) = data
            if newPhaseNumber != phaseNumber:
                games[gameId][clientId].put(("error", ["wrong phaseNumber", messageType]))
            elif newWordIndex < wordIndex or newWordIndex > len(globalWordList):
                games[gameId][clientId].put(("error", ["impossible wordIndex", messageType]))
            elif clientId not in team1 and clientId not in team2:
                games[gameId][clientId].put(("error", ["unknown team", messageType]))
            else:

                # Set the score and the new active team
                if clientId in team1:
                    scoreTeam1 += (newWordIndex - wordIndex)
                    activeTeam = 2
                elif clientId in team2:
                    scoreTeam2 += (newWordIndex - wordIndex)
                    activeTeam = 1

                # Check if round finished
                if newWordIndex == wordIndex:
                    print("new phase needs to be started")

                    # Check if all phases finished
                    print("phases is {}".format(phases))
                    if not phases:
                        print("no more additional phases")
                        nextPhase = -1
                    else:
                        print("phases contains {}".format(phases))
                        nextPhase = phases.pop()
                        print("nextPhase is {}".format(nextPhase))

                        # Get a new permutation the word list
                        random.shuffle(globalWordList)

                        # Give a notification to all users
                        for user in users:
                            games[gameId][user].put(("setup", globalWordList))

                    wordIndex = 0

                # Otherwise continue with the same phase but the new index
                else:
                    nextPhase = phaseNumber
                    wordIndex = newWordIndex

                # Get the next active player
                if activeTeam == 1:
                    activePlayer = team1.popleft()
                    team1.append(activePlayer)
                elif activeTeam == 2:
                    activePlayer = team2.popleft()
                    team2.append(activePlayer)

                # Find the name of the next player
                activePlayerName = usernames.get(activePlayer)
                if activePlayerName is None:
                    games[gameId][clientId].put(("error", ["client has no username", messageType]))

                # Broadcast next player and score to all players
                for user in users:
                    games[gameId][user].put(("roundFinished",
                                             [scoreTeam1, scoreTeam2, activePlayerId, activePlayerName, nextPhase]))

                # If game is completely done, stop the gameThread
                if nextPhase == -1:
                    print(colorama.Fore.GREEN + "GAME IS FINISHED")
                    return

                print("roundFinished handling done")

        elif messageType == "clientLost":
            print(colorama.Fore.RED + "Client with Id {} has been lost!".format(clientId))

            # Delete client communication queue
            del games[gameId][clientId]

            # Remove client from all lists
            if clientId in users:
                users.remove(clientId)
            if clientId in team1:
                team1.remove(clientId)
            if clientId in team2:
                team2.remove(clientId)

            # If active player was lost stop the round
            if clientId == activePlayerId:
                for user in users:
                    games[gameId][user].put(("roundFinished",
                                             [scoreTeam1, scoreTeam2, activePlayerId, activePlayerName, nextPhase]))

        elif messageType == "newClient":

            # Add user to the users list
            if clientId not in users:
                users.append(clientId)
            else:
                games[gameId][clientId].put(("error", ["clientId is already in users", messageType]))

            # Save new user into the usernames dictionary
            usernames[clientId] = data
            userCount += 1
            games[gameId]["userCount"] = userCount

        else:
            games[gameId][clientId].put(("error", ["unknown messageType", messageType]))

    # Loop all over again


# JSON encoding functions

def encodeJoinMessage(gameId, clientId, teamName1, teamName2, teamId1, teamId2, port, requestType):
    message = dict()
    message["returnType"] = "ACK"
    message["requestType"] = requestType
    message["gameId"] = gameId
    message["clientId"] = clientId
    body = dict()
    body["teamName1"] = teamName1
    body["teamName2"] = teamName2
    body["teamId1"] = teamId1
    body["teamId2"] = teamId2
    body["port"] = port
    message["body"] = body
    return json.dumps(message) + "\q"


def encodeErrorMessage(requestType, errorMessage, gameId=-1, clientId=-1):
    print(colorama.Fore.RED + "Send new error message ({}) as a response to {} to clientId {}".format(errorMessage, requestType, clientId))
    message = dict()
    message["returnType"] = "error"
    message["requestType"] = requestType
    message["gameId"] = gameId
    message["clientId"] = clientId
    body = dict()
    body["errorType"] = errorMessage
    message["body"] = body
    return json.dumps(message) + "\q"


def encodeTeamJoinAck(gameId, clientId, hasStarted, startTime, timePerRound, wordsPerPerson):
    message = dict()
    message["returnType"] = "ACK"
    message["requestType"] = "joinTeam"
    message["gameId"] = gameId
    message["clientId"] = clientId
    body = dict()
    body["hasStarted"] = hasStarted
    body["startTime"] = startTime
    body["timePerRound"] = timePerRound
    body["wordsPerPerson"] = wordsPerPerson
    message["body"] = body
    print(colorama.Style.DIM + "Send new message: {}".format(message))
    return json.dumps(message) + "\q"


def encodeAckMessage(requestType, gameId, clientId):
    message = dict()
    message["returnType"] = "ACK"
    message["requestType"] = requestType
    message["gameId"] = gameId
    message["clientId"] = clientId
    body = dict()
    message["body"] = body
    print(colorama.Style.DIM + "Send new message: {}".format(message))
    return json.dumps(message) + "\q"


def encodeSetupMessage(gameId, clientId, wordList):
    message = dict()
    message["returnType"] = "setup"
    message["requestType"] = ""
    message["gameId"] = gameId
    message["clientId"] = clientId
    body = dict()
    body["wordList"] = wordList
    message["body"] = body
    print(colorama.Style.DIM + "Send new message: {}".format(message))
    return json.dumps(message) + "\q"


def encodeStartRoundMessage(gameId, clientId, startTime, activeTeam, activePlayerId, activePlayerName, phaseNumber, wordIndex):
    message = dict()
    message["returnType"] = "startRound"
    message["requestType"] = ""
    message["gameId"] = gameId
    message["clientId"] = clientId
    body = dict()
    body["startTime"] = startTime
    body["activeTeam"] = activeTeam
    body["activePlayerId"] = activePlayerId
    body["activePlayerName"] = activePlayerName
    body["phaseNumber"] = phaseNumber
    body["wordIndex"] = wordIndex
    message["body"] = body
    print(colorama.Style.DIM + "Send new message: {}".format(message))
    return json.dumps(message) + "\q"


def encodeRoundFinishedMessage(gameId, clientId, scoreTeam1, scoreTeam2, nextPlayerId, nextPlayerName, nextPhase):
    message = dict()
    message["returnType"] = "roundFinished"
    message["requestType"] = "roundFinished"
    message["gameId"] = gameId
    message["clientId"] = clientId
    body = dict()
    body["scoreTeam1"] = scoreTeam1
    body["scoreTeam2"] = scoreTeam2
    body["nextPlayerId"] = nextPlayerId
    body["nextPlayerName"] = nextPlayerName
    body["nextPhase"] = nextPhase
    message["body"] = body
    print(colorama.Style.DIM + "Send new message: {}".format(message))
    return json.dumps(message) + "\q"


if __name__ == "__main__":
    # Dict that maps gameIDs to queues
    games = dict()
    gameQueues = dict()

    # Initialize colorama
    colorama.init(autoreset=True)

    # Print welcome message
    print(colorama.Style.BRIGHT + " _________  ___  _____ ______   _______   ________           ___  ___  ________    ")
    print(colorama.Style.BRIGHT + "|\___   ___\\\\  \|\   _ \  _   \|\  ___ \ |\   ____\         |\  \|\  \|\   __  \   ")
    print(colorama.Style.BRIGHT + "\|___ \  \_\ \  \ \  \\\\\\__\ \  \ \   __/|\ \  \___|_        \ \  \\\\\\  \ \  \|\  \  ")
    print(colorama.Style.BRIGHT + "     \ \  \ \ \  \ \  \\\\|__| \  \ \  \_|/_\ \_____  \        \ \  \\\\\\  \ \   ____\ ")
    print(colorama.Style.BRIGHT + "      \ \  \ \ \  \ \  \    \ \  \ \  \_|\ \|____|\  \        \ \  \\\\\\  \ \  \___| ")
    print(colorama.Style.BRIGHT + "       \ \__\ \ \__\ \__\    \ \__\ \_______\____\_\  \        \ \_______\ \__\    ")
    print(colorama.Style.BRIGHT + "        \|__|  \|__|\|__|     \|__|\|_______|\_________\        \|_______|\|__|    ")
    print(colorama.Style.BRIGHT + "                                            \|_________|                           ")
    print(colorama.Style.BRIGHT + "                                                                                   ")

    # Client Socket Waiting Time
    TIMEOUT = 0.1

    # Default values
    HOST = ""
    PORT = 9999

    # To avoid annoying already in use message
    socketserver.ThreadingTCPServer.allow_reuse_address = True

    # Wait for incoming connections and start a new thread RequestHandler that
    # handles the request
    with socketserver.ThreadingTCPServer((HOST, PORT), RequestHandler) as server:
        server.serve_forever()
