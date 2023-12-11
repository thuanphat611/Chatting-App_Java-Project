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

    class Sender implements Runnable {
        private BufferedWriter bw;
        private boolean isDone;
        public Sender(OutputStream out) {
            isDone = false;
            bw = new BufferedWriter(new OutputStreamWriter(out));
        }
        @Override
        public void run() {
            System.out.println("Send thread started");
            while (!isDone) {
                if (Thread.currentThread().isInterrupted())
                    close();
            }
        }

        public void close() {
            try {
                System.out.println("Send thread closed");
                isDone = true;
                bw.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    class Receiver implements Runnable {
        private BufferedReader br;
        private boolean isDone;
        public Receiver(InputStream in) {
            isDone = false;
            br = new BufferedReader(new InputStreamReader(in));
        }
        @Override
        public void run() {
            System.out.println("Receive thread started");
            while (!isDone) {
                if (Thread.currentThread().isInterrupted())
                    close();
            }
        }

        public void close() {
            try {
                System.out.println("Receive thread closed");
                isDone = true;
                br.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
