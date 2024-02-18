package web.app.client.view;

import web.app.client.model.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;

/**
 * Класс для создания пользовательского окна чата
 */
public class ClientUI extends JFrame {
    private static final int WIDTH = 450;
    private static final int HEIGHT = 300;
    private JTextArea chat;
    private Client client;
    private Component connectionComponent;
    private final String IP_REGEX = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)(\\.(?!$)|$)){4}$";
    private final String NUMBER_REGEX = "(\\d*\\.)?\\d+";

    /**
     * Конструктор пользовательского окна чата
     */
    public ClientUI() {
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
        JPanel component = new JPanel(new GridLayout(2, 2));

        JTextField ipAddressTextField = new JTextField("127.0.0.1");
        ipAddressTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ipAddressTextField.setText("");
            }
        });

        JTextField portTextField = new JTextField("1024");
        portTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                portTextField.setText("");
            }
        });

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
                    connect(nameField, ipAddressTextField, portTextField, component);
                }
            }
        });

        JButton loginButton = new JButton("Подключиться");
        loginButton.addActionListener(event -> {
            connect(nameField, ipAddressTextField, portTextField, component);
        });

        component.add(ipAddressTextField);
        component.add(portTextField);
        component.add(nameField);
        component.add(loginButton);
        return component;
    }

    /**
     * Метод попытки подключения к серверу
     * @param nameField текстовое поле для ввода имени
     * @param ipField текстовое поле для ввода ip-адреса
     * @param portField текстовое поле для ввода номера порта
     * @param component панель, включающая все компоненты для подключения к серверу
     */
    private void connect(JTextField nameField, JTextField ipField, JTextField portField, Component component) {
        String name = nameField.getText();
        String ip = ipField.getText();
        String port = portField.getText();
        if (validateSocketParameters(ip, port)) {
            JOptionPane.showMessageDialog(connectionComponent, "IP-адрес или port указаны не верно.");
        } else {
            int portNumb = Integer.parseInt(port);
            component.setVisible(false);
            createClient(name, ip, portNumb);
            setTitle(String.format("Мессенджер (%s)", name));
        }
    }

    /**
     * Метод для валидации ip и порта
     * @param ip ip-адрес
     * @param port номер порта
     * @return true, если параметры корректны
     */
    private boolean validateSocketParameters(String ip, String port) {
        return !ip.matches(IP_REGEX) || !(port.matches(NUMBER_REGEX) && port.length() >= 4 && port.length() <= 5);
    }

    /**
     * Метод создания клиента
     * @param name имя
     * @param ip ip-адрес
     * @param port номер порта
     */
    private void createClient(String name, String ip, int port) {
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
            setTitle("Мессенджер");
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
