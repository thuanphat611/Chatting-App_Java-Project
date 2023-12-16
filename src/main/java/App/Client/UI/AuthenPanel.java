package App.Client.UI;

import App.Client.Controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class AuthenPanel extends JPanel {
    JFrame parent;
    JPanel prev;
    Controller controller;

    JLabel label;
    JButton back;
    JPanel form;

    public AuthenPanel(JFrame parent, JPanel prev, Controller controller) {
        this.parent = parent;
        this.prev = prev;
        this.controller = controller;
        this.label = new JLabel("");
        this.form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.PAGE_AXIS));

        setPreferredSize(new Dimension(500, 400));
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(Box.createRigidArea(new Dimension(10, 0)));
        JPanel contentWrap = new JPanel();
        contentWrap.setLayout(new BoxLayout(contentWrap, BoxLayout.PAGE_AXIS));
        add(contentWrap);
        add(Box.createRigidArea(new Dimension(10, 0)));

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        contentWrap.add(Box.createRigidArea(new Dimension(0, 10)));
        contentWrap.add(content);

        JPanel header = new JPanel();
        header.setLayout(new BorderLayout());
        JPanel backWrap = new JPanel();
        backWrap.setLayout(new BorderLayout());
        back =  new JButton("Back");
        backWrap.add(back, BorderLayout.PAGE_START);
        back.setPreferredSize(new Dimension(80, 30));
        header.add(backWrap, BorderLayout.LINE_START);
        JPanel labelWrap = new JPanel();
        labelWrap.setLayout(new FlowLayout(FlowLayout.CENTER));
        labelWrap.add(label);
        header.add(labelWrap, BorderLayout.CENTER);
        header.add(Box.createRigidArea(new Dimension(80, 30)), BorderLayout.LINE_END);


        content.add(header, BorderLayout.PAGE_START);

        JPanel formWrap = new JPanel();
        formWrap.setLayout(new BorderLayout());
        JPanel formCentering = new JPanel();
        formCentering.setLayout(new FlowLayout(FlowLayout.CENTER));
        formCentering.add(form);
        formWrap.add(formCentering, BorderLayout.PAGE_START);
        content.add(formWrap, BorderLayout.CENTER);

        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.setContentPane(prev);
                parent.pack();
                parent.validate();
            }
        });
    }

    public void setType(String type) {
        if (Objects.equals(type, "Login")) {
            label.setText("Login");Font labelFont = label.getFont();
            label.setFont(new Font(labelFont.getName(), Font.PLAIN, 30));

            form.removeAll();
            form.setPreferredSize(new Dimension(160, 160));

            JPanel usernameWrap = new JPanel();
            usernameWrap.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel usernameLbl = new JLabel("Username");
            usernameWrap.add(usernameLbl);
            JTextField usernameIn = new JTextField();
            JPanel passwordWrap = new JPanel();
            passwordWrap.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel passwordLbl = new JLabel("Password");
            passwordWrap.add(passwordLbl);
            JPasswordField passwordIn = new JPasswordField();
            passwordIn.setEchoChar('*');

            JPanel submitWrap = new JPanel();
            submitWrap.setLayout(new FlowLayout(FlowLayout.CENTER));
            JButton submit = new JButton("Login");
            submitWrap.add(submit);

            form.add(usernameWrap);
            form.add(usernameIn);
            form.add(Box.createRigidArea(new Dimension(0, 10)));
            form.add(passwordWrap);
            form.add(passwordIn);
            form.add(Box.createRigidArea(new Dimension(0, 10)));
            form.add(submitWrap);

            submit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    back.setEnabled(false);
                    String username = usernameIn.getText();
                    String password = new String(passwordIn.getPassword());
                    if (username.isEmpty() || password.isEmpty()) {
                        JOptionPane.showMessageDialog(parent, "Username/password can not be empty", "Error", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else {
                        boolean result =  controller.login(username, password);
                        if (!result) {
                            JOptionPane.showMessageDialog(parent, "Error sending message to server", "Error", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    back.setEnabled(true);
                }
            });
        }
        else {
            label.setText("Register");
            Font labelFont = label.getFont();
            label.setFont(new Font(labelFont.getName(), Font.PLAIN, 30));

            form.removeAll();
            form.setPreferredSize(new Dimension(160, 220));

            JPanel usernameWrap = new JPanel();
            usernameWrap.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel usernameLbl = new JLabel("Username");
            usernameWrap.add(usernameLbl);
            JTextField usernameIn = new JTextField();
            JPanel passwordWrap = new JPanel();
            passwordWrap.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel passwordLbl = new JLabel("Password");
            passwordWrap.add(passwordLbl);
            JPasswordField passwordIn = new JPasswordField();
            passwordIn.setEchoChar('*');
            JPanel confirmPasswordWrap = new JPanel();
            confirmPasswordWrap.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel confirmPasswordLbl = new JLabel("Confirm password");
            confirmPasswordWrap.add(confirmPasswordLbl);
            JPasswordField confirmPasswordIn = new JPasswordField();
            confirmPasswordIn.setEchoChar('*');

            JPanel submitWrap = new JPanel();
            submitWrap.setLayout(new FlowLayout(FlowLayout.CENTER));
            JButton submit = new JButton("Register");
            submitWrap.add(submit);

            form.add(usernameWrap);
            form.add(usernameIn);
            form.add(Box.createRigidArea(new Dimension(0, 10)));
            form.add(passwordWrap);
            form.add(passwordIn);
            form.add(Box.createRigidArea(new Dimension(0, 10)));
            form.add(confirmPasswordWrap);
            form.add(confirmPasswordIn);
            form.add(Box.createRigidArea(new Dimension(0, 10)));
            form.add(submitWrap);

            submit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    back.setEnabled(false);
                    String username = usernameIn.getText();
                    String password = new String(passwordIn.getPassword());
                    String confirmPassword = new String(confirmPasswordIn.getPassword());

                    if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                        JOptionPane.showMessageDialog(parent, "Some fields is missing", "Error", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else if (username.contains(" ") || password.contains(" ")) {
                        JOptionPane.showMessageDialog(parent, "Username and password can not contain space", "Error", JOptionPane.INFORMATION_MESSAGE);
                        usernameIn.setText("");
                        passwordIn.setText("");
                        confirmPasswordIn.setText("");
                    }
                    else if (!password.equals(confirmPassword)) {
                        JOptionPane.showMessageDialog(parent, "Confirm password is different from password", "Error", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else if (username.length() < 4 || username.length() > 30) {
                        JOptionPane.showMessageDialog(parent, "Username must be at least 4 characters and can not exceed 30 characters", "Error", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else if (password.length() < 8 || password.length() > 30) {
                        JOptionPane.showMessageDialog(parent, "Password must be at least 8 characters and can not exceed 30 characters", "Error", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        boolean result =  controller.register(username, password);
                        if (!result) {
                            JOptionPane.showMessageDialog(parent, "Error sending message to server", "Error", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    back.setEnabled(true);
                }
            });
        }
    }
}
