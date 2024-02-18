## Тимофеев Вадим

### Java-разработчик

### > `Десктопный пользовательский чат`

`Используемые технологии:`

📌 java.net
📌 Swing-framework
📌 multithreading

### `Реализация:`

Приложение реализовано в рамках двух модулей:
1) Клиентской части
2) Серверной части

✅ Поддерживается возможность подключения не ограниченного количества пользователей  
(в рамках максимального кол-ва свободных на сервере портов), за счет запуска в `отдельном потоке` клиентского менеджера  
✅ Добавлена возможность отправки `личных сообщений` пользователю по его имени, используя символ "@". 
Например: "**@Настя сообщение**"  
✅ Добавлен `пользовательский интерфейс` на базе `swing-framework`  
⚠️ Параметры сокета подключения к серверу (ip, port) по дефолту - локальная машина.
