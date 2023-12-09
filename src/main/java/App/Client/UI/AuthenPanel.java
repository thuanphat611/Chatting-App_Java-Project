package App.Client.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        content.add(form, BorderLayout.CENTER);

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
        if (type == "Login") {
            label.setText("Login");Font labelFont = label.getFont();
            label.setFont(new Font(labelFont.getName(), Font.PLAIN, 30));
        }
    }
}
