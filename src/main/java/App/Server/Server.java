package App.Server;

import App.Server.Database.Database;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Server implements Runnable {
    private ServerSocket server;
    private ArrayList<ConnectionHandler> clientList;
    private HashMap<String, Integer> clientHashMap;
    private boolean finish;
    private Database db;

    public Server() {
        clientList = new ArrayList<>();
        clientHashMap = new HashMap<>();
        db = new Database();
        finish = false;
    }

    @Override
    public void run() {
        try {
            this.server = new ServerSocket(9999);
            System.out.println("Server started at port: " + server.getLocalPort());
            while (!finish)
            {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client, clientList.size());
                Thread child = new Thread(handler);
                child.start();
                clientList.add(handler);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    void setOnline(String username, int index) {
        clientHashMap.put(username, index);
        broadcast("/online", username);
    }

    void setOffline(String username, int index) {
        clientHashMap.remove(username);
        broadcast("/offline", username);
    }

    //announce to all current clients that user abc is online
    void broadcast(String announcement, String username) {
        for (ConnectionHandler client : clientList) {
            if (!username.equals(client.getUsername())) {
                client.send(announcement + "|" + username); //TODO : implement user online (Client.java)
            }
        }
    }

    void forwardMessage(String from, String to, String message) {
        if (clientHashMap.get(from) == null || clientHashMap.get(to) == null)
            return;
        int fromIndex = clientHashMap.get(from);
        int toIndex = clientHashMap.get(to);
        String sender = clientList.get(fromIndex).getUsername();

        clientList.get(toIndex).send("/receiveMessage|" + sender + "|" + message);
    }

    void shutdown() throws IOException {
        finish = true;
        db.close();
        for (ConnectionHandler client : clientList)
            client.quit();
        if (!server.isClosed())
            server.close();
    }

    class ConnectionHandler implements Runnable {
        private String username;
        private Socket socket;
        private BufferedReader receiver;
        private BufferedWriter sender;
        private final int index; //index in clientList of server

        public ConnectionHandler(Socket client, int index) {
            this.socket = client;
            this.index = index;
            username = "";
        }

        @Override
        public void run() {
            try {
                this.sender = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                System.out.println("Get connection from " + socket.getPort() + "(local)");

                String receivedMessage;

                do {
                    receivedMessage = receiver.readLine();
                    String[] splitMsg = receivedMessage.split("\\|");
                    String header = splitMsg[0];

                    System.out.println(socket.getPort() + " client: "+ receivedMessage);
                    if (header.equals("/quit")) {
                        quit();
                        break;
                    }
                    else if (header.equals("/login")) {
                        if (splitMsg.length != 3) {
                            send("/fail|Login information is missing");
                            continue;
                        }
                        if (!username.isEmpty())
                            continue;
                        if (db.login(splitMsg[1].trim(), splitMsg[2].trim())) {
                            this.username = splitMsg[1];
                            setOnline(username, index);
                            send("/loginSuccess" + "|" + username);
                            System.out.println(username + " Logged in, port: " + socket.getPort());
                        }
                        else {
                            send("/fail|Username or password is incorrect");
                        }
                    }
                    else if (header.equals("/register")) {
                        System.out.println("register called");
                        if (splitMsg.length != 3) {
                            send("/fail|Register information is missing");
                            continue;
                        }
                        if (!db.usernameCheck(splitMsg[1])) {
                            System.out.println("duplicate username");
                            send("/fail|Username is already taken");
                            continue;
                        }
                        if (db.register(splitMsg[1], splitMsg[2])) {
                            System.out.println("register success");
                            send("/registerSuccess");
                            System.out.println("Account " + splitMsg[1] + " successfully registered, port: " + socket.getPort());
                        }
                        else {
                            System.out.println("register fail");
                            send("/fail|Some errors happened");
                        }
                    }
                    else if (header.equals("/sendMessage")) {

                        if (splitMsg.length != 4) {
                            continue;
                        }

                        forwardMessage(splitMsg[1], splitMsg[2], splitMsg[3]);
                    }
                    else if (header.equals("/SendGroupMessage")) {
                        //TODO: implement group chatting
                    }
                }
                while (true);
                System.out.println("Client " + socket.getPort() + " has disconnected");
            }
           catch (Exception e) {
                System.out.println("Client " + socket.getPort() + " has disconnected");
           }
        }

        public void send(String message) {
            try {
                sender.write(message);
                sender.newLine();
                sender.flush();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        public String getUsername() {
            return username;
        }

        void quit() {
            try {
                sender.close();
                receiver.close();
                if (!socket.isClosed())
                    socket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
