package web.app.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String name;

    public Client(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
        try {
            //переход от массива байт в строковый буфер
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //если соединение потеряно
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    /**
     * Метод отправки сообщений от клиента
     */
    public void sendMessage() {
        try {
            //при первом подключении (отправке первого сообщения) на сервер,
            //отправляем свое имя - и это КОНТРАКТ, т.к. на этой логике работает Клиент-менеджер
            writer.write(name);
            writer.newLine();
            writer.flush(); //отправить имя клиента

            //отправка самого сообщения
            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                //здесь работа будет приостановлена пока в буфере что-то не появится
                // (ожидание сообщения от пользователя)
                String message = scanner.nextLine();
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
                        System.out.println(messageFromGroup);
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
}
