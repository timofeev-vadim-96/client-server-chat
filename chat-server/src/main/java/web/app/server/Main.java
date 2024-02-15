package web.app.server;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1024);
        Server server = new Server(serverSocket);
        server.runServer();
    }
}