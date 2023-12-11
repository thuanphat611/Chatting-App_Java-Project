package App.Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Server implements Runnable {
    private ServerSocket server;
    private ArrayList<ConnectionHandler> clientList;
    private HashMap<String, Integer> clientHashMap;
    private boolean finish;

    public Server() {
        clientList = new ArrayList<>();
        clientHashMap = new HashMap<>();
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
    }

    //announce to all current clients that user abc is online
    void broadcast(String username) {
        for (ConnectionHandler client : clientList) {
            if (!username.equals(client.getUsername())) {
                client.send("/online " + username); //TODO : implement user online (Client.java)
            }
        }
    }

    void forwardMessage(String from, String to, String message) {
        if (clientHashMap.get(from) == null || clientHashMap.get(to) == null)
            return;
        int fromIndex = clientHashMap.get(from);
        int toIndex = clientHashMap.get(to);
        String sender = clientList.get(fromIndex).getUsername();

        clientList.get(toIndex).send("/receiveMessage " + sender + " " + message);
    }

    void shutdown() throws IOException {
        finish = true;
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
        private int index; //index in clientList of server

        public ConnectionHandler(Socket client, int index) {
            this.socket = client;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                this.sender = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                System.out.println("Get connection from " + socket.getPort() + "(local)");

                //Login/signup
                while (true) {
                    String loginMsg = receiver.readLine();
                    String[] loginInfo = loginMsg.split(" ");

                    if (loginInfo.length != 3) {
                        send("/fail login info is missing");
                    }

                    if (loginMsg.startsWith("/login")) {
                        // TODO: implement login

                        this.username = loginInfo[1];
                        setOnline(username, index);
                        send("/success");
                        System.out.println(username + " Logged in, port: " + socket.getPort());
                        break;
                    }
                    else if (loginMsg.startsWith("/signup")) {
                        // TODO: implement sign up

                        this.username = loginInfo[1];
                        setOnline(username,index);
                        send("/success");
                        System.out.println(username + " successfully registered, port: " + socket.getPort());
                        break;
                    }
                    send("/fail error");
                }

                String receivedMessage;
                do {
                    receivedMessage = receiver.readLine();
                    if (receivedMessage.startsWith("/quit")) {
                        // TODO: implement client quit
                        send("/quit");
                        quit();
                        break;
                    }
                    else if (receivedMessage.startsWith("/sendMessage")) {
                        String[] splitMessage =  receivedMessage.split(" ");

                        if (splitMessage.length != 4) {
                            continue;
                        }

                        forwardMessage(splitMessage[1], splitMessage[2], splitMessage[3]);
                    }
                    else if (receivedMessage.startsWith("/SendGroupMessage")) {
                        //TODO: implement group chatting
                    }
                }
                while (true);
            }
           catch (Exception e) {
                System.out.println("Client " + socket.getPort() + " has disconnected");
           }
        }

        public void send(String message) {
            try {
                sender.write(message);
                sender.flush();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        public String getUsername() {
            return username;
        }

        void quit() throws IOException {
            if (!socket.isClosed())
                socket.close();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
