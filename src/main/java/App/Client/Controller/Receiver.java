package App.Client.Controller;

import java.io.BufferedReader;
import java.io.IOException;

public class Receiver implements Runnable {
    private BufferedReader br;
    private volatile boolean isDone;
    public Receiver(BufferedReader in) {
        isDone = false;
        br = in;
    }
    @Override
    public void run() {
        System.out.println("Receive thread started");
        try {
            while (!isDone) {
                String msg = br.readLine();
                System.out.println("Receiver: " + msg);
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Receive thread closed"); //TODO remove here 2
    }

    public void close() {
        try {
            isDone = true;
            br.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
