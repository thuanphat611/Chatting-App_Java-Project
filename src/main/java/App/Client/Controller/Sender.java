package App.Client.Controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

class Sender implements Runnable {
    private BufferedWriter bw;
    private volatile boolean isDone;
    public Sender(OutputStream out) {
        isDone = false;
        bw = new BufferedWriter(new OutputStreamWriter(out));
    }
    @Override
    public void run() {
        System.out.println("Send thread started");
        while (!isDone) {
            Thread.onSpinWait();
        }
        System.out.println("Here 1");
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

    public boolean sendLogin(String username, String password) {
        try {
            bw.write("/login|" + username + "|" + password);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return  false;
        }
        return true;
    }
}