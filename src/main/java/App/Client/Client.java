package App.Client;
import App.Client.UI.*;
import App.Client.Controller.*;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class Client extends JFrame {
    public void createAndShowGUI() {
        setTitle("Chatting App");
        setDefaultLookAndFeelDecorated(true);

        Controller controller = new Controller(this);
        Thread conThread = new Thread(controller);
        conThread.start();
        JPanel newContentPane = new HomePanel(this, controller);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.close();
                System.exit(0);
            }
        });

        newContentPane.setOpaque(true);
        this.setContentPane(newContentPane);
        this.pack();
        this.setVisible(true);
    }

    public void closeFrame() {
        System.exit(0);
    }

    public static void main(String[] args) {
        Client app = new Client();
        app.createAndShowGUI();
    }
}
