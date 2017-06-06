import socket
import threading

#SocketListener acts as a manager for all inbound and outbound connections to the Echo Server.
class SocketListener:
    #Maintain an array of clients for broadcasting messages.
    clients = []

    def init(self, port):
        #Create socket on current host and specified port.
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        host = socket.gethostname()
        
        #Bind to port and attempt to listen for connections.
        try:
            sock.bind((host, port))
        except socket.error as e:
            if e.errno == 98:
                print("[ERROR] Address", port, "already in use.")
                exit()
            else:
                print("[ERROR] Unknown error. Error code:", e.errno, ". Please search \"Errno number\" for more information.")
                exit()
                
        print("[INFO] Socket opened successfully on ", port)
        self.listenConnections(sock)
    
    def listenConnections(self, sock):
        #Listen for incoming connections with a maximum of 10.
        #Plan to make this editable in next push with a config.
        print("[INFO] Listening on Socket.")
        sock.listen(10)
        while True:
            #If a client connects, give them their own thread as to not tie up the main thread for additional connections.
            client, addr = sock.accept()
            print("[INFO] Established connection from", addr)
            print("[INFO] Creating listen thread for", addr)
            threading.Thread(target = self.listenClient, args = (client, addr)).start()
            
    def listenClient(self, client, addr):
        #Request identification
        client.send(b"[IDENTIFY]\n")
        try:
            data = client.recv(1024)
            if data:
                #For compability with Java's sockets, all messages must end in a newline.
                #This newline is stripped before the message is processed.
                identity = data.decode('utf-8').rstrip("\n")
                print("[INFO] Client registered as ", identity)
            else:
                raise error("")
        except:
            print("[INFO] Client ", addr, " disconnected without authentication.")
            client.close()
            return False
            
        #Client successfully identified themselves, append them to the array.
        self.clients.append(client)    
        
        #Enter a read/broadcast loop.
        while True:
            try:
                data = client.recv(1024)
                if data:
                    #Strip newlines again
                    print(identity , data.decode('utf-8').rstrip("\n"))
                    #For every client that is not the owner of this thread, send the latest message to them.
                    for item in self.clients[:]:
                        if item != client:
                            message = ''.join(identity + ' ' + data.decode('utf-8') + '\n')
                            item.send(message.encode('utf-8'))
                else:
                    raise error("")
            except:
                print("[INFO] Client ", identity, " disconnected.")
                #Remove owner from array and close out thread.
                for item in self.clients[:]:
                    if item == client:
                        self.clients.remove(client)
                        
                client.close()
                return False
            
if __name__ == "__main__":
    print("[ERROR] Please launch from Echo_Server.py")