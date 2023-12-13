package App.Client.Controller;

import java.io.BufferedWriter;
import java.io.IOException;

public class Sender implements Runnable {
    private BufferedWriter bw;
    private volatile boolean isDone;
    public Sender(BufferedWriter out) {
        isDone = false;
        bw = out;
    }
    @Override
    public void run() {
        System.out.println("Send thread started");
        while (!isDone) {
//            Thread.onSpinWait();
        }
        System.out.println("Send thread closed");//TODO remove here 1
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