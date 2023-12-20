package App.Client.UI;

import App.Client.Controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class BoardPanel extends JPanel {
    private JFrame parent;
    private Controller controller;
    private JPanel content;
    private JScrollPane contentSP;
    private String username;
    private ArrayList<String[]> chatList;
    public BoardPanel(JFrame parent, Controller controller, String username, ArrayList<String[]> chatList) {
        this.parent = parent;
        this.controller = controller;
        this.username = username;
        this.chatList = chatList;

        content = new JPanel();
        content.setLayout(new BorderLayout());

        setPreferredSize(new Dimension(600, 700));
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        JPanel verticalMargin = new JPanel();
        verticalMargin.setLayout(new BoxLayout(verticalMargin, BoxLayout.PAGE_AXIS));
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(verticalMargin);
        add(Box.createRigidArea(new Dimension(10, 0)));

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());

        contentSP = new JScrollPane(content);
        wrapper.add(contentSP, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setLayout(new BorderLayout());
        JLabel usernameLbl = new JLabel(this.username);
        JPanel usernameWrap = new JPanel();
        usernameWrap.setLayout(new FlowLayout(FlowLayout.CENTER));
        usernameWrap.add(usernameLbl);
        JScrollPane userSP = new JScrollPane(usernameWrap);
        userSP.setPreferredSize(new Dimension(150, 0));
        footer.add(userSP, BorderLayout.LINE_START);

        JPanel btnGroupWrap = new JPanel();
        btnGroupWrap.setLayout(new FlowLayout(FlowLayout.CENTER));
        JPanel btnGroup = new JPanel();
        btnGroup.setLayout(new BoxLayout(btnGroup, BoxLayout.LINE_AXIS));
        btnGroupWrap.add(btnGroup);
        JButton refresh = new JButton("Refresh");
        JButton logout = new JButton("Logout");
        JButton createGroup = new JButton("Create group");
        btnGroup.add(createGroup);
        btnGroup.add(Box.createRigidArea(new Dimension(10, 0)));
        btnGroup.add(refresh);
        btnGroup.add(Box.createRigidArea(new Dimension(10, 0)));
        btnGroup.add(logout);

        footer.add(btnGroupWrap, BorderLayout.CENTER);
        wrapper.add(footer, BorderLayout.PAGE_START);
        //TODO implement buttons functionality

        verticalMargin.add(Box.createRigidArea(new Dimension(0, 10)));
        verticalMargin.add(wrapper);
        verticalMargin.add(Box.createRigidArea(new Dimension(0, 10)));
        if (this.chatList != null)
            refresh();

        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.logout();
            }
        });
        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.refreshRequest();
            }
        });
        createGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String groupName = JOptionPane.showInputDialog(parent, "Input group name", "Create new group", JOptionPane.PLAIN_MESSAGE);
                if (groupName == null)
                    return;
                if (groupName.isEmpty()) {
                    JOptionPane.showMessageDialog(parent, "Group name cannot be empty", "Create new group", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                groupName = groupName.trim();
                if (groupName.length() > 50) {
                    JOptionPane.showMessageDialog(parent, "Maximum length for group name is 50 characters", "Create new group", JOptionPane.PLAIN_MESSAGE);
                }
                controller.createGroup(username, groupName);
                controller.refreshRequest();
            }
        });
    }

    public void refresh() {
        content.removeAll();
        JPanel contentWrapper = new JPanel();
        contentWrapper.setLayout(new BoxLayout(contentWrapper, BoxLayout.PAGE_AXIS));
        if (chatList.size() == 0) {
            JLabel noUser = new JLabel("There are no users online at the moment");
            JPanel noUserPnl = new JPanel();
            noUserPnl.setLayout(new FlowLayout(FlowLayout.CENTER));
            noUserPnl.add(noUser);
            content.add(noUserPnl);
        }
        for (int i = 0; i < chatList.size(); i++) {
            String[] chat = chatList.get(i);
            JPanel chatPnl = new JPanel();
            chatPnl.setLayout(new BorderLayout());
            JLabel chatName = new JLabel(chat[0]);
            JButton chatBtn = new JButton("Start chatting");
            JPanel btnGroup = new JPanel();
            btnGroup.setLayout(new BoxLayout(btnGroup, BoxLayout.PAGE_AXIS));
            btnGroup.add(chatBtn);
            if (chat[1].equals("group")) {
                chatName.setText("Group: " + chat[0]);
                JButton outGroup = new JButton("Leave group");
                btnGroup.add(Box.createRigidArea(new Dimension(0, 5)));
                btnGroup.add(outGroup);
                JButton addMember = new JButton("Add Member");
                btnGroup.add(Box.createRigidArea(new Dimension(0, 5)));
                btnGroup.add(addMember);

                outGroup.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        chatBtn.setEnabled(false);
                        addMember.setEnabled(false);
                        outGroup.setEnabled(false);
                        controller.leaveGroup(chat[0], chat[2], username);
                        controller.refreshRequest();
                    }
                });
            }
            JPanel btnWrap = new JPanel();
            btnWrap.setLayout(new BorderLayout());
            btnWrap.add(btnGroup, BorderLayout.CENTER);

            chatPnl.add(chatName, BorderLayout.LINE_START);
            JPanel center = new JPanel();
            center.setPreferredSize(new Dimension(0, 60));
            chatPnl.add(center, BorderLayout.CENTER);
            chatPnl.add(btnWrap, BorderLayout.LINE_END);

            JPanel paddingY = new JPanel();
            paddingY.setLayout(new BoxLayout(paddingY, BoxLayout.PAGE_AXIS));
            paddingY.add(Box.createRigidArea(new Dimension(0, 5)));
            paddingY.add(chatPnl);
            paddingY.add(Box.createRigidArea(new Dimension(0, 5)));

            JPanel paddingX = new JPanel();
            paddingX.setLayout(new BoxLayout(paddingX, BoxLayout.LINE_AXIS));
            paddingX.add(Box.createRigidArea(new Dimension(5, 0)));
            paddingX.add(paddingY);
            paddingX.add(Box.createRigidArea(new Dimension(5, 0)));

            JScrollPane chatWrap = new JScrollPane(paddingX);
            contentWrapper.add(chatWrap);
            content.add(contentWrapper, BorderLayout.PAGE_START);

            JPanel prev = this;
            chatBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    controller.toChatPanel(prev, username, chat[0], chat[1]);
                    parent.pack();
                    parent.validate();
                }
            });
        }
        contentSP.revalidate();
        contentSP.repaint();
    }

    public void setChatList(ArrayList<String[]> list) {
        this.chatList = list;
        refresh();
        parent.pack();
        parent.validate();
    }
}
