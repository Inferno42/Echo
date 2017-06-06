import socket
import threading

class SocketListener:
    clients = []

    def init(self, port):
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        host = socket.gethostname()

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
        print("[INFO] Listening on Socket.")
        sock.listen(10)
        while True:
            client, addr = sock.accept()
            print("[INFO] Established connection from", addr)
            print("[INFO] Creating listen thread for", addr)
            threading.Thread(target = self.listenClient, args = (client, addr)).start()
            
    def listenClient(self, client, addr):
        client.send(b"[IDENTIFY]\n")
        try:
            data = client.recv(1024)
            if data:
                identity = data.decode('utf-8').rstrip("\n")
                print("[INFO] Client registered as ", identity)
            else:
                raise error("")
        except:
            print("[INFO] Client ", addr, " disconnected without authentication.")
            client.close()
            return False
            
        self.clients.append(client)    
        
        while True:
            try:
                data = client.recv(1024)
                if data:
                    print(identity , data.decode('utf-8').rstrip("\n"))
                    for item in self.clients[:]:
                        if item != client:
                            message = ''.join(identity + ' ' + data.decode('utf-8') + '\n')
                            item.send(message.encode('utf-8'))
                else:
                    raise error("")
            except:
                print("[INFO] Client ", identity, " disconnected.")
                for item in self.clients[:]:
                    if item == client:
                        self.clients.remove(client)
                        
                client.close()
                return False
            
if __name__ == "__main__":
    print("[ERROR] Please launch from Echo_Server.py")