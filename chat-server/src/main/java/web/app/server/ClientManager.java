package web.app.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс-обертка над клиентским сокетом
 */
public class ClientManager implements Runnable {
    //сокет, который назначен клиенту после подключения к серверу
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String name;
    public static List<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        try {
            this.socket = socket;
            //для общения с клиентом
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //читаем имя клиента - при подключении первая строка его сообщения - его имя. это КОНТРАКТ
            name = reader.readLine();
            //добавляем в список клиентов текущий экземпляр класса
            clients.add(this);
            System.out.println(name + " подключился к чату."); //чат СЕРВЕРА
            broadcastMessage("Server: " + name + " подключился к чату."); //в чат КЛИЕНТОВ
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (!socket.isClosed()) {
            try {
                //читаем пришедшую строку и пересылаем остальным
                messageFromClient = reader.readLine();
                if (isPersonalMessage(messageFromClient)){
                    sendPersonalMessage(messageFromClient);
                }
                else {
                    broadcastMessage((String.format("%s: %s", this.name, messageFromClient)));
                }
            } catch (IOException e) {
                closeEverything(socket, reader, writer);
            }
        }
    }

    /**
     * Метод отправки персональных сообщений
     */
    private void sendPersonalMessage(String messageFromClient) {
        String recipient = parseRecipientName(messageFromClient);
        boolean clientFound = false;
        for (ClientManager client: clients){
            if (client.name.equals(recipient) && !client.equals(this)){
                clientFound = true;
                String messageBody = getPersonalMessageBody(messageFromClient, recipient);
                sendMessage(client, String.format("%s: %s", this.name, messageBody));
                break;
            }
        }
        if (!clientFound){
            sendMessage(this, String.format("Пользователь с именем \"%s\" не найден.", recipient));
        }
    }

    /**
     * Метод перенаправления сообщения другим КЛИЕНТАМ
     *
     * @param messageToSent
     */
    private void broadcastMessage(String messageToSent) {
        for (ClientManager client : clients) {
            //рассылка остальным клиентам сообщений
            if (!client.equals(this) && messageToSent != null) {
                sendMessage(client, messageToSent);
            }
        }
    }

    /**
     * Метод идентификации личных сообщений
     */
    private boolean isPersonalMessage(String message) {
        return message.startsWith("@");
    }

    /**
     * Метод для парсинга именем получателя сообщения, пропуская [0] символ, обозначающий личное сообщение
     * @param message
     * @return
     */
    private String parseRecipientName(String message){
        StringBuilder recipientName = new StringBuilder();
        for (int i = 1; i < message.length(); i++) {
            //парсим первое слово, являющееся именем другого клиента
            if (message.charAt(i) == ' ') return recipientName.toString();
            recipientName.append(message.charAt(i));
        }
        return recipientName.toString();
    }

    /**
     * Метод для парсинга тела личного сообщения (исключая имя получателя)
     */
    private String getPersonalMessageBody(String message, String clientName){
        return message.replaceFirst("@" + clientName, "").replaceFirst(" ", "");
    }

    /**
     * Метод отправки сообщения
     */
    private void sendMessage(ClientManager client, String message) {
        try {
            client.writer.write(message);
            client.writer.newLine();
            client.writer.flush();
        } catch (IOException e) {
            closeEverything(socket, reader, writer);
        }
    }

    /**
     * Метод закрытия соединения и всех потоков ввода и вывода
     */
    private void closeEverything(Socket socket, BufferedReader reader, BufferedWriter writer) {
        //Удаление клиента из коллекции
        removeClient();
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

    /**
     * Метод удаления клиента из статичного списка подключенных к серверу клиентов
     */
    private void removeClient() {
        clients.remove(this);
        System.out.printf("%s вышел из чата.", name);
        broadcastMessage("Server: " + name + " вышел из чата.");
    }
}
