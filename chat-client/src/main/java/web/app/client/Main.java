package web.app.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.print("Введите ваше имя: ");
        Scanner scanner = new Scanner(System.in);
        String name = scanner.nextLine();
        Socket socket = new Socket(InetAddress.getLocalHost(), 1024);

        Client client = new Client(socket, name);

        //информация о подключении (поменяется порт на назначенный сервером)
        System.out.println("InetAddress: " + socket.getInetAddress());
        System.out.println("RemoteIP: " + socket.getLocalAddress());
        System.out.println("LocalPort: " + socket.getLocalPort());

        client.listenForMessage(); //создается второй поток
        client.sendMessage();
    }
}
