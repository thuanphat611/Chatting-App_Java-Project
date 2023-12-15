package App.Client.Controller;

import App.Client.UI.HomePanel;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class Controller implements Runnable {
    private HomePanel home;
    private String username;
    private Socket socket;
    private Sender sendThread;
    private Receiver receiveThread;
    private JFrame parent;

    public Controller(JFrame parent) {
        this.parent = parent;
        username = "";
    }

    @Override
    public void run() {
        try {
            socket = new Socket("localhost", 9999);

            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            OutputStream os = socket.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

            sendThread = new Sender(bw, this);
            receiveThread = new Receiver(br, parent, this);
            Thread th1 = new Thread(sendThread);
            Thread th2 = new Thread(receiveThread);

            th1.start();
            th2.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        parent.setContentPane(home);
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
}
