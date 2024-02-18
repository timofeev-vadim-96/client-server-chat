package web.app.client;

import web.app.client.view.ClientUI;

import java.io.IOException;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        ClientUI view = new ClientUI();
        System.out.println(InetAddress.getLocalHost().getHostAddress());
    }
}
