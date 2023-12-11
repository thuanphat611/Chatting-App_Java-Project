package App.Client.Controller;

import java.io.*;
import java.net.Socket;

public class Controller {
    private Socket socket;
    private Thread sendThread;
    private Thread receiveThread;
    public Controller() {
        try {
            socket = new Socket("localhost", 9999);
            sendThread = new Thread(new Sender(socket.getOutputStream()));
            receiveThread = new Thread(new Receiver(socket.getInputStream()));
            sendThread.start();
            receiveThread.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            sendThread.interrupt();
            receiveThread.interrupt();
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
