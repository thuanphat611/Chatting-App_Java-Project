package App.Client.Controller;

import java.io.*;

public class Sender implements Runnable {
    private Controller controller;
    private BufferedWriter bw;
    private DataOutputStream fileSender;
    private volatile boolean isDone;
    public Sender(BufferedWriter out, DataOutputStream fs, Controller controller) {
        isDone = false;
        bw = out;
        fileSender = fs;
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
            fileSender.close();
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

    public void sendFile(String path) {
        try {
            int bytes = 0;

            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);

            fileSender.writeLong(file.length());
            byte[] buffer = new byte[1024];
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                fileSender.write(buffer, 0, bytes);
                fileSender.flush();
            }
            fileInputStream.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}