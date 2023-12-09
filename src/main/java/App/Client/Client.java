package App.Client;
import App.Client.UI.*;

import javax.swing.*;

public class Client extends JFrame {
    public void createAndShowGUI() {
        setTitle("Chatting App");
        setDefaultLookAndFeelDecorated(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel newContentPane = new HomePanel(this);

        newContentPane.setOpaque(true);
        this.setContentPane(newContentPane);
        this.pack();
        this.setVisible(true);
    }

    public static void main(String[] args) {
        Client app = new Client();
        app.createAndShowGUI();
    }
}
