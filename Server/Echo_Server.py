from includes.SocketListener import SocketListener

class Echo_Server:
   
    def init(self, port):
        print("[INFO] Starting Echo Server")
        print("[INFO] Attempting to start Socket Listener on port", port)
        sock = SocketListener()
        sock.init(port)
        

Server = Echo_Server()
Server.init(26000)