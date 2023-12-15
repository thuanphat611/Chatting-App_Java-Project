package App.Client.Controller;

import java.io.BufferedWriter;
import java.io.IOException;

public class Sender implements Runnable {
    private Controller controller;
    private BufferedWriter bw;
    private volatile boolean isDone;
    public Sender(BufferedWriter out, Controller controller) {
        isDone = false;
        bw = out;
        this.controller = controller;
    }
    @Override
    public void run() {
        System.out.println("Send thread started");
        while (!isDone) {
            //wait until controller call its methods
        }
        System.out.println("Send thread closed");
    }

    public void close() {
        try {
            isDone = true;
            bw.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean sendMessage(String message) {
        try {
            bw.write(message);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return  false;
        }
        return true;
    }
}