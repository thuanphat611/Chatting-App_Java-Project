package App.Client.Controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

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