package App.Client.Controller;

import App.Client.Client;
import App.Client.UI.BoardPanel;
import App.Client.UI.ChatPanel;
import App.Client.UI.HomePanel;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Controller implements Runnable {
    private HomePanel home;
    private String username;
    private Socket socket;
    private Sender sendThread;
    private Receiver receiveThread;
    private Client parent;
    private ChatPanel chatPnl;
    private BoardPanel board;
    private String currentPanel;
    private ArrayList<String[]> historyBuffer;
    private volatile boolean getHistoryDone;
    private String downloadLocation;

    public Controller(Client parent) {
        this.parent = parent;
        username = "";
        board = null;
        historyBuffer = new ArrayList<>();
        getHistoryDone = false;
        currentPanel = "";
        downloadLocation = "Client_storage";

        File storage_dir = new File(downloadLocation);
        if (!storage_dir.exists()) {
            if (!storage_dir.mkdir()) {
                System.out.println("Error in creating storage directory for file sending functionality(client)");
            }
        }
    }



    @Override
    public void run() {
        try {
            socket = new Socket("localhost", 9999);

            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            OutputStream os = socket.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            DataOutputStream fileSender = new DataOutputStream(socket.getOutputStream());
            DataInputStream fileReceiver = new DataInputStream(socket.getInputStream());

            sendThread = new Sender(bw, fileSender, this);
            receiveThread = new Receiver(br, fileReceiver, parent, this);
            Thread th1 = new Thread(sendThread);
            Thread th2 = new Thread(receiveThread);

            th1.start();
            th2.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(parent, "Some error happened, please check if server is running", "Error", JOptionPane.INFORMATION_MESSAGE);
            parent.closeFrame();
        }
    }

    public void close() {
        sendThread.sendMessage("/quit");
        try {
            sendThread.close();
            receiveThread.close();
            if (!socket.isClosed())
                socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setHomePanel(HomePanel home) {
        this.home = home;
    }

    public void toHome() {
        currentPanel = "home";
        parent.setContentPane(home);
        parent.pack();
        parent.validate();
    }

    public void createBoardPanel(ArrayList<String[]> chatList) {
        board = new BoardPanel(parent, this, username, chatList);
        currentPanel = "board";
        parent.setContentPane(board);
        parent.pack();
        parent.validate();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean login(String username, String password) {
        if (username.isEmpty() || password.isEmpty())
            return false;
        return sendThread.sendMessage("/login|" + username + "|" + password);
    }

    public boolean register(String username, String password) {
        if (username.isEmpty() || password.isEmpty())
            return false;
        return sendThread.sendMessage("/register|" + username + "|" + password);
    }

    public void logout() {
        if (this.username.isEmpty())
            return;
        sendThread.sendMessage("/logout");
        this.username = "";
        HomePanel home = new HomePanel(parent, this);
        currentPanel = "home";
        parent.setContentPane(home);
        parent.pack();
        parent.validate();
    }

    public void refreshRequest() {
        sendThread.sendMessage("/refresh");
    }

    public String getCurrentPanel() {
        return currentPanel;
    }

    public void clearAllMessages(String sender, String receiver) {
        sendThread.sendMessage("/clearAllMessages|" + sender + "|" + receiver);
    }

    public void updateBoard(ArrayList<String[]> chatList) {
        if (board == null)
            return;
        board.setChatList(chatList);
        parent.pack();
        parent.validate();
    }

    public String getDownloadLocation() {
        return downloadLocation;
    }

    public void getChatHistory(String name1, String name2, int amount) {
        getHistoryDone = false;
        if (!name2.contains(" ")) { // normal user cannot contain any spaces in his name
            if (amount == -1)
                sendThread.sendMessage("/requestChatHistory|" + name1 + "|" + name2);
            else
                sendThread.sendMessage("/requestChatHistory|" + name1 + "|" + name2 + "|" + amount);
        }
        else { //group has a space to separate owner and group name
            String[] splitGroup = name2.split(" ");
            String owner = splitGroup[0];
            StringBuilder groupName = new StringBuilder(splitGroup[1]);
            for (int i = 2; i < splitGroup.length; i++)
                groupName.append(" ").append(splitGroup[i]);
            if (amount == -1)
                sendThread.sendMessage("/requestGroupChatHistory|" + groupName.toString().trim() + "|" + owner);
            else
                sendThread.sendMessage("/requestGroupChatHistory|" + groupName.toString().trim() + "|" + owner + "|" + amount);
        }

        while (!getHistoryDone) {
            Thread.onSpinWait();
        }
    }

    public void setHistoryBuffer(ArrayList<String[]> buffer) {
        historyBuffer = buffer;
    }

    public void setGetHistoryDone(boolean state) {
        getHistoryDone = state;
    }

    public void sendMessage(String sender, String receiver, String content, String type) {
        if (type.equals("user"))
            sendThread.sendMessage("/sendMessage|" + sender + "|" + receiver + "|" + content);
        else if (type.equals("group"))
            sendThread.sendMessage("/sendGroupMessage|" + sender + "|" + receiver + "|" + content);
    }

    public void toChatPanel( JPanel prev, String username, String receiverName, String type) {
        getChatHistory(username, receiverName, 100);
        chatPnl = new ChatPanel(parent, prev, this, username, receiverName, historyBuffer, type);
        currentPanel = "chat";
        parent.setContentPane(chatPnl);
        parent.pack();
        parent.validate();
    }

    public void updateChatPanel(String username, String receiverName) {
        if (!currentPanel.equals("chat"))
            return;
        getChatHistory(username, receiverName, 100);
        refreshChatPanel();
    }

    public void addMessageToPanel(String sender, String content, String type) {
        String[] temp = new String[3];
        temp[0] = sender;
        temp[1] = content;
        temp[2] = type;
        historyBuffer.add(0, temp);
    }

    public void refreshChatPanel() {
        chatPnl.setChatList(historyBuffer);
        chatPnl.refreshMsg();
        parent.pack();
        parent.validate();
    }

    public String getChatWith() {
        if (chatPnl == null)
            return "";
        return chatPnl.getReceiverName();
    }

    public void createGroup(String username, String groupName) {
        sendThread.sendMessage("/createGroup|" + username + "|" + groupName);
    }

    public void leaveGroup(String groupName, String owner, String memberName) {
        sendThread.sendMessage("/leaveGroup|" + groupName + "|" + owner + "|" + memberName);
    }

    public void addMemberRequest(String groupName, String owner, String memberName) {
        sendThread.sendMessage("/addMember|" + groupName + "|" + owner + "|" + memberName);
    }

    public void sendFile(String username, String receiverName, String filePath) {
        String[] splitPath = filePath.split("\\\\");
        String fileName = splitPath[splitPath.length - 1];
        if (!receiverName.contains(" "))
            sendThread.sendMessage("/sendFile|" + username + "|" + receiverName+ "|" + fileName);
        else
            sendThread.sendMessage("/sendFile|" + username + "|" + receiverName+ "|" + fileName + "|group");
        sendThread.sendFile(filePath);
    }

    public void downloadFile(String sender, String receiver, String fileIndex, String fileName) {
        sendThread.sendMessage("/getFile|" + sender + "|" + receiver + "|" + fileIndex + "|"  + fileName);
    }
}
