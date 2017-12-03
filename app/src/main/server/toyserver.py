import socketserver


class ThreadedTCPRequestHandler(socketserver.BaseRequestHandler):

    def handle(self):
        ipAddress, port = self.client_address

        print("new connection to {} at port {}".format(ipAddress, port))

        self.request.sendall("you are connected to port {}".format(port).encode())


if __name__ == "__main__":
    HOST = ""
    PORT = 9999

    # Wait for incoming connections and start a new thread RequestHandler that
    # handles the request
    with socketserver.ThreadingTCPServer((HOST, PORT), ThreadedTCPRequestHandler) as server:
        server.serve_forever()