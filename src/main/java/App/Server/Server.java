package App.Server;

import App.Server.Database.Database;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Server implements Runnable {
    private String storage;
    private ServerSocket server;
    private ArrayList<ConnectionHandler> clientList;
    private HashMap<String, Integer> clientHashMap;
    private ArrayList<Integer> freeSpace;
    private boolean finish;
    private Database db;

    public Server() {
        clientList = new ArrayList<>();
        clientHashMap = new HashMap<>();
        freeSpace =  new ArrayList<>();
        db = new Database();
        finish = false;
        storage = "Server_storage";

        File storage_dir = new File(storage);
        if (!storage_dir.exists()) {
            if (!storage_dir.mkdir()) {
                System.out.println("Error in creating storage directory for file sending functionality(server)");
            }
        }
    }

    @Override
    public void run() {
        try {
            this.server = new ServerSocket(9999);
            System.out.println("Server started at port: " + server.getLocalPort());
            while (!finish)
            {
                Socket client = server.accept();
                if (freeSpace.isEmpty()) {
                    ConnectionHandler handler = new ConnectionHandler(client, clientList.size());
                    Thread child = new Thread(handler);

                    child.start();
                    clientList.add(handler);
                }
                else {
                    int handlerIndex = freeSpace.get(0);
                    freeSpace.remove(0);

                    ConnectionHandler handler = new ConnectionHandler(client, handlerIndex);
                    Thread child = new Thread(handler);

                    child.start();
                    clientList.set(handlerIndex, handler);
                }
            }
        } catch (Exception e) {
            shutdown();
            System.out.println(e.getMessage());
        }
    }

    void forwardMessage(String from, String to, String message, String type) {
        if (clientHashMap.get(from) == null || clientHashMap.get(to) == null)
            return;
        int fromIndex = clientHashMap.get(from);
        int toIndex = clientHashMap.get(to);
        String sender = clientList.get(fromIndex).getUsername();

        clientList.get(toIndex).send("/receiveMessage|" + sender + "|" + message + "|" + type);
    }

    private void forwardGroupMessage(String from, String owner, String groupName, String message, String type) {
        for (ConnectionHandler connectionHandler : clientList) {
            if (connectionHandler.username.isEmpty())
                continue;
            if (connectionHandler.username.equals(from))
                continue;
            if (db.groupMemberCheck(groupName, owner, connectionHandler.username))
                connectionHandler.send("/receiveGroupMessage|" + owner + " " + groupName + "|" + from + "|" + message + "|" + type);
        }
    }

    private void shutdown() {
        finish = true;
        db.close();
        for (ConnectionHandler client : clientList)
            if (client != null)
                client.quit();
        try {
            if (!server.isClosed())
                server.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    class ConnectionHandler implements Runnable {
        private String username;
        private Socket socket;
        private BufferedReader receiver;
        private BufferedWriter sender;
        private DataInputStream fileReceiver;
        private DataOutputStream fileSender;
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
                this.fileSender = new DataOutputStream(socket.getOutputStream());
                this.fileReceiver = new DataInputStream(socket.getInputStream());

                System.out.println("Get connection from " + socket.getPort() + "(local)");

                String receivedMessage;

                do {
                    receivedMessage = receiver.readLine();
                    String[] splitMsg = receivedMessage.split("\\|");
                    String header = splitMsg[0];

                    System.out.println(socket.getPort() + " client: "+ receivedMessage);
                    if (header.equals("/quit")) {
                        if (!this.username.isEmpty())
                            clientHashMap.remove(username);
                        freeSpace.add(index);
                        clientList.set(index, null);
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
                        if (clientHashMap.get(splitMsg[1]) != null) {
                            send("/fail|This account has been logged in on another device");
                            continue;
                        }
                        if (db.login(splitMsg[1].trim(), splitMsg[2].trim())) {
                            this.username = splitMsg[1];
                            clientHashMap.put(username, index);
                            send("/loginSuccess" + "|" + username);
                            System.out.println(username + " Logged in, port: " + socket.getPort());
                            sendGroupList(username);
                            sendOnlineList();
                            send("/end");
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
                    else if (header.equals("/logout")) {
                        if (this.username.isEmpty())
                            continue;

                        System.out.println(username + " logged out");
                        clientHashMap.remove(username);
                        this.username = "";
                    }
                    else if (header.equals("/refresh")) {
                        send("/refresh");
                        if (!this.username.isEmpty())
                            sendGroupList(username);
                        sendOnlineList();
                        send("/end");
                    }
                    else if (header.equals("/sendMessage")) {
                        if (splitMsg.length != 4) {
                            continue;
                        }
                        db.saveMsgHistory(splitMsg[1], splitMsg[2], splitMsg[3], "text");
                        forwardMessage(splitMsg[1], splitMsg[2], splitMsg[3], "text");
                    }
                    else if (header.equals("/sendGroupMessage")) {
                        String[] splitGroup = splitMsg[2].split(" ");
                        String owner = splitGroup[0];
                        StringBuilder groupName = new StringBuilder(splitGroup[1]);
                        for (int i = 2; i < splitGroup.length; i++)
                            groupName.append(" ").append(splitGroup[i]);
                        if (!db.groupMemberCheck(groupName.toString().trim(), owner, splitMsg[1])) {
                            send("/fail|You are not in that group to send message");
                            continue;
                        }
                        db.saveGroupMsgHistory(splitMsg[1], owner, groupName.toString().trim(), splitMsg[3], "text");
                        forwardGroupMessage(splitMsg[1], owner, groupName.toString().trim(), splitMsg[3], "text");
                    }
                    else if (header.equals("/requestChatHistory")) {
                        String name1 = splitMsg[1];
                        String name2 = splitMsg[2];
                        int amount = -1;
                        if (splitMsg.length == 4)
                            amount = Integer.parseInt(splitMsg[3]);
                        sendChatHistory(name1, name2, amount);
                    }
                    else if (header.equals("/requestGroupChatHistory")) {
                        String owner = splitMsg[2];
                        String groupName = splitMsg[1];
                        int amount = -1;
                        if (splitMsg.length == 4)
                            amount = Integer.parseInt(splitMsg[3]);
                        sendGroupChatHistory(owner, groupName, amount);
                    }
                    else if (header.equals("/createGroup")) {
                        if (!db.groupNameCheck(splitMsg[1], splitMsg[2])) {
                            send("/fail|You have already created a group with that name");
                            continue;
                        }
                        db.createGroup(splitMsg[1], splitMsg[2]);
                        send("/info|Create group successfully");
                    }
                    else if (header.equals("/leaveGroup")) {
                        if (!db.groupMemberCheck(splitMsg[1], splitMsg[2], splitMsg[3])) {
                            send("/fail|You have already left that group");
                            continue;
                        }
                        db.leaveGroup(splitMsg[1], splitMsg[2], splitMsg[3]);
                        send("/info|You left the group");
                    }
                    else if (header.equals("/addMember")) {
                        if (db.groupMemberCheck(splitMsg[1], splitMsg[2], splitMsg[3])) {
                            send("/fail|This user has already in the group");
                            continue;
                        }
                        if (db.usernameCheck(splitMsg[3])) {
                            send("/fail|This user is not exist");
                            continue;
                        }
                        db.addMemberToGroup(splitMsg[1], splitMsg[2], splitMsg[3]);
                        send("/info|Add this member to group successfully");
                    }
                    else if (header.equals("/sendFile")) {
                        String sender = splitMsg[1];
                        String receiver = splitMsg[2];
                        String fileName = splitMsg[3];
                        int fileIndex = 0;
                        while (checkFileExist(sender, receiver, fileIndex + "_" + fileName)) {
                            fileIndex++;
                        }
                        File createFile = new File(storage + "\\" + sender + "\\" + receiver + "\\" + fileIndex + "_" + fileName);
                        if (!createFile.createNewFile())
                            System.out.println("Error: cannot create file for storage");
                        db.saveMsgHistory(sender, receiver, sender + " : " + fileName + " : " + fileIndex, "file");
                        receiveFile(storage + "\\" + sender + "\\" + receiver + "\\" + fileIndex + "_" + fileName);
                        if (splitMsg.length < 5)
                            forwardMessage(sender, receiver, sender + " : " + fileName + " : " + fileIndex, "file");
                        else {
                            String[] splitGroup = splitMsg[2].split(" ");
                            String owner = splitGroup[0];
                            StringBuilder groupName = new StringBuilder(splitGroup[1]);
                            for (int i = 2; i < splitGroup.length; i++)
                                groupName.append(" ").append(splitGroup[i]);
                            forwardGroupMessage(sender, owner, groupName.toString().trim(), sender + " : " + fileName + " : " + fileIndex, "file");
                        }
                    }
                    else if (header.equals("/getFile")) {
                        String sender = splitMsg[1];
                        String receiver = splitMsg[2];
                        String fileIndex = splitMsg[3];
                        String fileName = splitMsg[4];

                        if (!checkFileExist(sender, receiver, fileIndex + "_" + fileName)) {
                            send("/info|File not exist in server");
                            continue;
                        }
                        send("/downloadFile|" + fileName);
                        sendFile(storage + "\\" + sender + "\\" + receiver + "\\" + fileIndex + "_" + fileName);
                    }
                    else if (header.equals("/clearAllMessages")) {
                        String sender = splitMsg[1];
                        String receiver = splitMsg[2];
                        String directoryPath = storage + "\\" + sender + "\\" + receiver;
                        File directory = new File(directoryPath);
                        deleteDirectory(directory);
                        db.clearAllMessages(sender, receiver);
                    }
                    else if (header.equals("/deleteOneMessage")) {
                        String sender = splitMsg[1];
                        String receiver = splitMsg[2];
                        String content = splitMsg[3];
                        String orderIndex = splitMsg[4];
                        String type = splitMsg[5];
                        db.deleteOneMessage(sender, receiver, orderIndex);
                        if (type.equals("file")) {
                            String[] splitContent = content.split(" : ");
                            String fileSender = splitContent[0];
                            String fileName = splitContent[1];
                            String fileIndex = splitContent[2];
                            deleteFile(fileSender, receiver, fileName, fileIndex);
                        }
                    }
                }
                while (true); //TODO implement change password if have enough time.p/s:i Think there is not enough time bro:<
                System.out.println("Client " + socket.getPort() + " has disconnected");
            }
           catch (Exception e) {
                System.out.println("Client " + socket.getPort() + " has disconnected due to error");
           }
        }

        private  void sendOnlineList() {//send list of users that currently online
            StringBuilder message = new StringBuilder("/onlineList");
            int count = 0;
            for (String username : clientHashMap.keySet()) {
                if (!Objects.equals(username, this.username)) {
                    message.append("|").append(username);
                    count++;
                }
                if (count == 200) { //send 200 username per message
                    send(message.toString());
                    count = 0;
                    message = new StringBuilder("/onlineList");
                }
            }
            //send the remaining username
            if (count != 0)
                send(message.toString());
        }

        private void sendGroupList(String username) {
            String[] groups = db.getGroups(username);
            if (groups == null)
                return;

            StringBuilder message = new StringBuilder("/groupList|");

            for (String group : groups) {
                message.append(group);
                send(message.toString());
                message = new StringBuilder("/groupList|");
            }
        }

        private void send(String message) {//send a message to user
            try {
                sender.write(message);
                sender.newLine();
                sender.flush();
            } catch (Exception e) {
                System.out.print("Send message to client " + socket.getPort() + " error:");
                System.out.println(e.getMessage());
            }
        }

        private void sendChatHistory(String name1, String name2, int amount) {
            ArrayList<String[]> messages = db.getAllMessages(name1, name2);
            int count = 0;
            send("/startHistory");
            if (messages.isEmpty()) {
                send("/endHistory");
                return;
            }
            for (String[] message : messages) {
                send("/chatHistory|" + message[0] + "|" + message[1] + "|" + message[2] + "|" + message[3]);
                count++;
                if (count == amount)
                    break;
            }
            send("/endHistory");
        }

        private void sendGroupChatHistory(String owner, String groupName, int amount) {
            ArrayList<String[]> messages = db.getAllGroupMessages(owner, groupName);
            int count = 0;
            send("/startHistory");
            if (messages.isEmpty()) {
                send("/endHistory");
                return;
            }
            for (String[] message : messages) {
                send("/chatHistory|" + message[0] + "|" + message[1] + "|" + message[2] + "|" + message[3]);
                count++;
                if (count == amount)
                    break;
            }
            send("/endHistory");
        }

        private String getUsername() {
            return username;
        }

        private void sendFile(String path) {
            try {
                int bytes = 0;

                File file = new File(path);
                FileInputStream fileInputStream = new FileInputStream(file);

                fileSender.writeLong(file.length());
                byte[] buffer = new byte[1024];
                while ((bytes = fileInputStream.read(buffer)) != -1) {
                    fileSender.write(buffer, 0, bytes);
                    fileSender.flush();
                }
                fileInputStream.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        private void receiveFile(String fileName) {
            try {
                int bytes = 0;
                FileOutputStream fileOutputStream = new FileOutputStream(fileName);

                long size = fileReceiver.readLong();
                byte[] buffer = new byte[1024];
                while (size > 0 && (bytes = fileReceiver.read(buffer, 0,(int) Math.min(buffer.length, size))) != -1) {
                    fileOutputStream.write(buffer, 0, bytes);
                    size -= bytes;
                }
                fileOutputStream.close();
            } catch (Exception e) {
               System.out.println(e.getMessage());
            }
        }

        private boolean checkFileExist(String sender, String receiver, String fileName) {
            File sender_dir = new File(storage + "\\" + sender);
            if (!sender_dir.exists()) {
                if (!sender_dir.mkdir()) {
                    System.out.println("Error in creating storage directory for sender");
                    return true;
                }
            }

            File receiver_dir = new File(storage + "\\" + sender + "\\" + receiver);
            if (!receiver_dir.exists()) {
                if (!receiver_dir.mkdir()) {
                    System.out.println("Error in creating storage directory for receiver");
                    return true;
                }
                return false;
            }

            File file = new File(storage + "\\" + sender + "\\" + receiver + "\\" + fileName);
            return file.exists();
        }

        private static void deleteDirectory(File directory) {
            if (directory.exists()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        deleteDirectory(file);
                    }
                }
            }
            directory.delete();
        }

        private void deleteFile(String sender, String receiver, String fileName, String fileIndex) {
            File file = new File(storage + "\\" + sender + "\\" + receiver + "\\" + fileIndex + "_" + fileName);
            if (!file.exists())
                return;
            file.delete();
        }

        void quit() {
            try {
                sender.close();
                receiver.close();
                fileReceiver.close();
                fileSender.close();
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