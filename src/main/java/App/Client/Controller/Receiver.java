package App.Client.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class Receiver implements Runnable {
    private BufferedReader br;
    private volatile boolean isDone;
    public Receiver(InputStream in) {
        isDone = false;
        br = new BufferedReader(new InputStreamReader(in));
    }
    @Override
    public void run() {
        System.out.println("Receive thread started");
        while (!isDone) {
            Thread.onSpinWait();
        }
        System.out.println("here 2");
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
