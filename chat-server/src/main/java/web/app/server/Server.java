package web.app.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Метод запуска сервера с ожиданием подключения клиентов
     */
    public void runServer(){
        try{
            //пока сокет(связь) открыта
            while (!serverSocket.isClosed()){
                //здесь приложение блокируется, ожидая подключение клиента
                Socket socket = serverSocket.accept();

                ClientManager client = new ClientManager(socket);
                //запускаем клиент-менеджер в отдельном потоке для того,
                // чтобы можно было работать с несколькими клиентами
                Thread thread = new Thread(client);
                thread.start();
            }
        } catch (IOException e){
            closeSocket();
        }
    }

    /**
     * Метод для закрытия соединения
     */
    public void closeSocket(){
        try{
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
