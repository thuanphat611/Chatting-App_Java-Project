package App.Client.Controller;

import java.io.*;
import java.net.Socket;

public class Controller {
    private Socket socket;
    private Sender sendThread;
    private Receiver receiveThread;
    public Controller() {
        try {
            socket = new Socket("localhost", 9999);
            sendThread = new Sender(socket.getOutputStream());
            receiveThread = new Receiver(socket.getInputStream());
            Thread th1 = new Thread(sendThread);
            Thread th2 = new Thread(receiveThread);
            th1.start();
            th2.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            sendThread.close();
            receiveThread.close();
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean login(String username, String password) {
        if (username.isEmpty() || password.isEmpty())
            return false;
        return sendThread.sendLogin(username, password);
    }
}
