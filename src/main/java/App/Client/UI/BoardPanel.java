package App.Client.UI;

import App.Client.Controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class BoardPanel extends JPanel {
    JFrame parent;
    Controller controller;
    JPanel content;
    String username;
    ArrayList<String[]> chatList;
    public BoardPanel(JFrame parent, Controller controller, String username) {
        this.parent = parent;
        this.controller = controller;
        this.username = username;

        //TODO remove this chatList
        this.chatList = new ArrayList<String[]>();
        for (int i = 0; i < 10; i++) {
            String name = "User" + i;
            String type = "user";

            if (i == 3) {
                name = "Group" + i;
                type = "group";
            }
            String[] temp = new String[2];
            temp[0] = name;
            temp[1] = type;
            chatList.add(temp);
        }

        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));

        setPreferredSize(new Dimension(600, 700));
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        JPanel verticalMargin = new JPanel();
        verticalMargin.setLayout(new BoxLayout(verticalMargin, BoxLayout.PAGE_AXIS));
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(verticalMargin);
        add(Box.createRigidArea(new Dimension(10, 0)));

        JPanel search = new JPanel();
        search.setLayout(new FlowLayout(FlowLayout.CENTER));
        JTextField searchbar = new JTextField();
        JButton searchBtn = new JButton("Search");
        searchbar.setPreferredSize(new Dimension(400, 25));
        //TODO make searchbar lose focus
        //searchbar placeholder
        searchbar.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchbar.getText().equals("Search for user")) {
                    searchbar.setText("");
                    searchbar.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchbar.getText().isEmpty()) {
                    searchbar.setForeground(Color.GRAY);
                    searchbar.setText("Search for user");
                }
            }
        });

        search.add(searchbar);
        search.add(Box.createRigidArea(new Dimension(10, 0)));
        search.add(searchBtn);
        JScrollPane searchWrap = new JScrollPane(search);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(searchWrap, BorderLayout.PAGE_START);

        JScrollPane contentSP = new JScrollPane(content);
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
        JButton quit = new JButton("Quit");
        JButton logout = new JButton("Logout");
        JButton createGroup = new JButton("Create group");
        btnGroup.add(createGroup);
        btnGroup.add(Box.createRigidArea(new Dimension(10, 0)));
        btnGroup.add(logout);
        btnGroup.add(Box.createRigidArea(new Dimension(10, 0)));
        btnGroup.add(quit);

        footer.add(btnGroupWrap, BorderLayout.CENTER);
        wrapper.add(footer, BorderLayout.PAGE_END);
        //TODO implement buttons functionality

        verticalMargin.add(Box.createRigidArea(new Dimension(0, 10)));
        verticalMargin.add(wrapper);
        verticalMargin.add(Box.createRigidArea(new Dimension(0, 10)));

        refresh();
    }

    public void refresh() {
        content.removeAll();
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
                JButton outGroup = new JButton("Leave group");
                btnGroup.add(Box.createRigidArea(new Dimension(0, 5)));
                btnGroup.add(outGroup);
            }
            JPanel btnWrap = new JPanel();
            btnWrap.setLayout(new BorderLayout());
            btnWrap.add(btnGroup, BorderLayout.CENTER);
            //TODO implement chat buttons
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
            content.add(chatWrap);
        }
    }

}
