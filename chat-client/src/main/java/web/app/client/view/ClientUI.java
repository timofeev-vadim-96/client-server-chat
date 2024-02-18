package web.app.client.view;

import web.app.client.model.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Класс для создания пользовательского окна чата
 */
public class ClientUI extends JFrame {
    private static final int WIDTH = 450;
    private static final int HEIGHT = 300;
    private JTextArea chat;
    private Client client;
    private InetAddress ip;
    private int port;
    private Component connectionComponent;

    /**
     * Конструктор пользовательского окна чата
     */
    public ClientUI(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setTitle("Мессенджер");

        connectionComponent = createNorthComponent();
        add(connectionComponent, BorderLayout.NORTH);
        add(createSouthComponent(), BorderLayout.SOUTH);
        add(createCentralComponent());

        setResizable(false);
        setVisible(true);
    }

    /**
     * Метод создания центрального графического компонента окна - чата
     *
     * @return чат
     */
    private JScrollPane createCentralComponent() {
        chat = new JTextArea();
        chat.setEditable(false);
        return new JScrollPane(chat);
    }

    /**
     * Метод для создания второго верхнего блока полей для ввода логина, пароля и входа
     *
     * @return second north component
     */
    private Component createNorthComponent() {
        JPanel component = new JPanel(new GridLayout(1, 2));

        JTextField nameField = new JTextField("имя: ", 30);
        nameField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                nameField.setText("");
            }
        });

        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    String name = nameField.getText();
                    component.setVisible(false);
                    createClient(name);
                    setTitle(String.format("Мессенджер (%s)", name));
                }
            }
        });

        JButton loginButton = new JButton("Подключиться");
        loginButton.addActionListener(event -> {
            String name = nameField.getText();
            component.setVisible(false);
            createClient(name);
            setTitle(String.format("Мессенджер (%s)", name));
        });

        component.add(nameField);
        component.add(loginButton);
        return component;
    }

    private void createClient(String name) {
        try {
            Socket socket = new Socket(ip, port);
            client = new Client(socket, name, ClientUI.this);

            //информация о подключении (поменяется порт на назначенный сервером)
            handleMessage(String.format("InetAddress: %s", socket.getInetAddress()));
            handleMessage(String.format("RemoteIP: %s", socket.getLocalAddress()));
            handleMessage(String.format("LocalPort: %s", socket.getLocalPort()));

            client.listenForMessage(); //создается второй поток

            //при первом подключении (отправке первого сообщения) на сервер,
            //отправляем свое имя - и это КОНТРАКТ, т.к. на этой логике работает Клиент-менеджер
            client.sendMessage(client.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(connectionComponent, "Подключение к серверу не удалось.");
            connectionComponent.setVisible(true);
        }
    }

    /**
     * Создание компонента с блоком из кнопок по вводу текстового сообщения и его отправки
     *
     * @return south component
     */
    private Component createSouthComponent() {
        JPanel southComponent = new JPanel(new FlowLayout());
        JTextField textField = new JTextField("Введите Ваше сообщение:  ", 27);

        //если пользователь нажимает по текстовому полю
        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                textField.setText("");
            }
        });

        //если пользователь нажимает Enter
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    client.sendMessage(textField.getText());
                    textField.setText("");
                }
            }
        });

        JButton sendButton = new JButton("Отправить");
        //если пользователь кликает по кнопке
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.sendMessage(textField.getText());
                textField.setText("");
            }
        });
        southComponent.add(textField);
        southComponent.add(sendButton);
        return southComponent;
    }

    /**
     * Метод для добавления текста в пользовательский чат
     *
     * @param text сообщение
     */
    public void handleMessage(String text) {
        chat.append(text + "\n");
    }
    public Component getConnectionComponent() {
        return connectionComponent;
    }
}
