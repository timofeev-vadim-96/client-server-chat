package web.app.client.model;

import web.app.client.view.ClientUI;

import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String name;
    private ClientUI view;

    public Client(Socket socket, String name, ClientUI view) {
        this.socket = socket;
        this.name = name;
        try {
            //переход от массива байт в строковый буфер
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //UI
            this.view = view;
            //если соединение потеряно
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    /**
     * Метод отправки сообщений от клиента
     */
    public void sendMessage(String message) {
        try {
            if (!socket.isClosed()) {
                writer.write(message);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    /**
     * Метод ожидания сообщений в чате (в отдельном потоке)
     */
    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromGroup;
                while (socket.isConnected()) {
                    try {
                        //здесь работа будет приостановлена пока в буфере что-то не появится
                        messageFromGroup = reader.readLine();
                        view.handleMessage(messageFromGroup);
                    } catch (IOException e) {
                        closeEverything(socket, reader, writer);
                    }
                }
            }
        }).start();
    }

    /**
     * Метод закрытия соединения и всех потоков ввода и вывода
     */
    private void closeEverything(Socket socket, BufferedReader reader, BufferedWriter writer) {
        try {
            if (socket != null) {
                socket.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }
}
