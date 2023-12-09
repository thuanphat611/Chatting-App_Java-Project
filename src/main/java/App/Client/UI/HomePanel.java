package App.Client.UI;

import javax.swing.*;
import java.awt.*;

public class HomePanel extends JPanel {
    JFrame parent;
    public HomePanel(JFrame parent) {
        this.parent = parent;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 300));

        JLabel label = new JLabel("Chatting App");
        Font labelFont = label.getFont();
        label.setFont(new Font(labelFont.getName(), Font.PLAIN, 30));
        JPanel labelPnl = new JPanel();
        labelPnl.setLayout(new FlowLayout(FlowLayout.CENTER));
        labelPnl.add(label);
        add(labelPnl, BorderLayout.PAGE_START);

        JPanel btnGroupWrap = new JPanel();
        btnGroupWrap.setLayout(new FlowLayout(FlowLayout.CENTER));
        JPanel btnGroup = new JPanel();
        btnGroup.setLayout(new BoxLayout(btnGroup, BoxLayout.PAGE_AXIS));
        JButton loginBtn = new JButton("Login");
        loginBtn.setPreferredSize(new Dimension(100, 35));
        JPanel loginWrap = new JPanel();
        loginWrap.setLayout(new FlowLayout(FlowLayout.CENTER));
        loginWrap.add(loginBtn);
        JButton signupBtn = new JButton("Register");
        signupBtn.setPreferredSize(new Dimension(100, 35));
        JPanel signupWrap = new JPanel();
        signupWrap.setLayout(new FlowLayout(FlowLayout.CENTER));
        signupWrap.add(signupBtn);
        btnGroupWrap.add(btnGroup);
        btnGroup.add(Box.createRigidArea(new Dimension(0, 20)));
        btnGroup.add(loginWrap);
        btnGroup.add(Box.createRigidArea(new Dimension(0, 20)));
        btnGroup.add(signupWrap);

        add(btnGroupWrap, BorderLayout.CENTER);
    }
}
