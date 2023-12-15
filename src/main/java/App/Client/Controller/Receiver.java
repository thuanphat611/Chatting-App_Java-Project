package App.Client.Controller;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;

public class Receiver implements Runnable {
    private JFrame parent;
    private BufferedReader br;
    private volatile boolean isDone;
    private Controller controller;

    public Receiver(BufferedReader in, JFrame parent, Controller controller) {
        isDone = false;
        br = in;
        this.parent = parent;
        this.controller = controller;
    }
    @Override
    public void run() {
        System.out.println("Receive thread started");
        try {
            while (!isDone) {
                String msg = br.readLine();
                System.out.println("Receiver: " + msg);
                String[] splitMsg = msg.split("\\|");
                String header = splitMsg[0];

                if (header.equals("/loginSuccess")) {
                    controller.setUsername(splitMsg[1]);
                }
                else if (header.equals("/registerSuccess")) {
                    JOptionPane.showMessageDialog(parent, "Register successfull, you can login with your new account now", "Register successfully", JOptionPane.INFORMATION_MESSAGE);
                    controller.toHome();
                }
                else if (header.equals("/fail"))
                    JOptionPane.showMessageDialog(parent, splitMsg[1], "Failed", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Receive thread closed");
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
