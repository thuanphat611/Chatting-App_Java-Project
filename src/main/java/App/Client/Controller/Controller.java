package App.Client.Controller;

import java.io.*;
import java.net.Socket;

public class Controller implements Runnable {
    private Socket socket;
    private Sender sendThread;
    private Receiver receiveThread;

    @Override
    public void run() {
        try {
            socket = new Socket("localhost", 9999);

            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            OutputStream os = socket.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

            sendThread = new Sender(bw);
            receiveThread = new Receiver(br);
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
            if (!socket.isClosed())
                socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean login(String username, String password) {
        if (username.isEmpty() || password.isEmpty())
            return false;
        return sendThread.sendMessage("/login|" + username + "|" + password);
    }
}
