package App.Client.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class AuthenPanel extends JPanel {
    JFrame parent;
    JPanel prev;

    JLabel label;
    JPanel form;

    public AuthenPanel(JFrame parent, JPanel prev) {
        this.parent = parent;
        this.prev = prev;
        this.label = new JLabel("");
        this.form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.PAGE_AXIS));
        form.setPreferredSize(new Dimension(160, 160));

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
        JButton backBtn =  new JButton("Back");
        backWrap.add(backBtn, BorderLayout.PAGE_START);
        backBtn.setPreferredSize(new Dimension(80, 30));
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

        backBtn.addActionListener(new ActionListener() {
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

            JPanel usernameWrap = new JPanel();
            usernameWrap.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel usernameLbl = new JLabel("Username");
            usernameWrap.add(usernameLbl);
            JTextField usernameIn = new JTextField();
            JPanel passwordWrap = new JPanel();
            passwordWrap.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel passwordLbl = new JLabel("Password");
            passwordWrap.add(passwordLbl);
            JTextField passwordIn = new JTextField();

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
        }
    }
}
