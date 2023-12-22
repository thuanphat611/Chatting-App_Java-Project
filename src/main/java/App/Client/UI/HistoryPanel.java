package App.Client.UI;

import App.Client.Controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class HistoryPanel extends JPanel {
    private JFrame parent;
    private ChatPanel prev;
    private Controller controller;
    private JPanel content;
    private JScrollPane contentSP;
    private String receiverName;
    private String type;
    private String username;
    private ArrayList<String[]> messages;
    public HistoryPanel(JFrame parentFrame, ChatPanel prevPanel, Controller control, String username, String receiver, ArrayList<String[]> messageList, String typeOfChat) {
        this.parent = parentFrame;
        this.prev = prevPanel;
        this.controller = control;
        this.receiverName = receiver;
        this.messages = messageList;
        this.username = username;
        this.type = typeOfChat;

        content = new JPanel();
        content.setLayout(new BorderLayout());

        setPreferredSize(new Dimension(600, 700));
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        JPanel verticalMargin = new JPanel();
        verticalMargin.setLayout(new BoxLayout(verticalMargin, BoxLayout.PAGE_AXIS));
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(verticalMargin);
        add(Box.createRigidArea(new Dimension(10, 0)));

        JPanel header = new JPanel();
        header.setLayout(new BorderLayout());
        JButton backBtn = new JButton("Back");
        JButton clearBtn = new JButton("Delete all");
        JLabel panelLabel = new JLabel("Messages you have sent");

        JPanel labelWrap = new JPanel();
        labelWrap.setLayout(new FlowLayout(FlowLayout.CENTER));
        labelWrap.add(panelLabel);
        header.add(backBtn, BorderLayout.LINE_START);
        header.add(labelWrap, BorderLayout.CENTER);
        header.add(clearBtn, BorderLayout.LINE_END);
        JScrollPane headerSP = new JScrollPane(header);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(headerSP, BorderLayout.PAGE_START);

        contentSP = new JScrollPane(content);
        wrapper.add(contentSP, BorderLayout.CENTER);

        verticalMargin.add(Box.createRigidArea(new Dimension(0, 10)));
        verticalMargin.add(wrapper);
        verticalMargin.add(Box.createRigidArea(new Dimension(0, 10)));

        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.setCurrentPanel("chat");
                controller.updateChatPanel(username, receiverName);
                parent.setContentPane(prev);
                parent.pack();
                parent.validate();
            }
        });

        clearBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prev.clearHistory();
                messages.clear();
                refreshMsg();
                contentSP.revalidate();
                contentSP.repaint();
                controller.clearAllMessages(username, receiverName);
            }
        });

        refreshMsg();
    }

    public void setChatList(ArrayList<String[]> list) {
        this.messages = list;
    }


    public void refreshMsg() {
        content.removeAll();
        if (messages == null)
            return;
        if (messages.isEmpty()) {
            JPanel centerPnl = new JPanel();
            centerPnl.setLayout(new FlowLayout(FlowLayout.CENTER));
            JLabel noMsg = new JLabel("You haven't sent any messages to this user/group");
            centerPnl.add(noMsg);
            content.add(centerPnl);
            return;
        }
        JPanel contentWrapper = new JPanel();
        contentWrapper.setLayout(new BoxLayout(contentWrapper, BoxLayout.PAGE_AXIS));


        for (int i = 0; i < messages.size(); i++) {
            int indexInMessages = i;
            String[] message = messages.get(i);
            if (!message[0].equals(this.username))
                continue;

            JPanel messageBody = new JPanel();
            messageBody.setLayout(new BoxLayout(messageBody, BoxLayout.PAGE_AXIS));

            JPanel msgContentWrap = new JPanel();
            msgContentWrap.setLayout(new BoxLayout(msgContentWrap, BoxLayout.PAGE_AXIS));

            if (message[2].equals("file")) {
                String sender = message[1].split(" : ")[0];
                String fileName = message[1].split(" : ")[1];
                String fileIndex = message[1].split(" : ")[2];

                JLabel fileNameLbl = new JLabel("Send file: " + fileName);
                msgContentWrap.add(fileNameLbl);
            } else {
                int lineSize = 90;
                int numberOfLine = message[1].length() / lineSize;
                if (message[1].length() % lineSize != 0)
                    numberOfLine = (message[1].length() / lineSize) + 1;
                for (int j = 0; j < numberOfLine; j++) {
                    JLabel line = new JLabel();
                    int end = j * lineSize + lineSize;
                    if (end > message[1].length())
                        end = message[1].length();
                    line.setText(message[1].substring(j * lineSize, end));
                    msgContentWrap.add(line);
                }
            }

            JPanel msgContentWrapLeft = new JPanel();
            msgContentWrapLeft.setLayout(new FlowLayout(FlowLayout.LEFT));
            msgContentWrapLeft.add(msgContentWrap);
            messageBody.add(msgContentWrapLeft);

            JPanel btnPanel = new JPanel();
            btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            JButton deleteBtn = new JButton("Delete");
            btnPanel.add(deleteBtn);
            messageBody.add(btnPanel);

            deleteBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    controller.deleteOneMsg(username, receiverName, message[1], message[3], message[2]);
                    messages.remove(indexInMessages);
                    refreshMsg();
                    contentSP.revalidate();
                    contentSP.repaint();
                }
            });

            JPanel paddingY = new JPanel();
            paddingY.setLayout(new BoxLayout(paddingY, BoxLayout.PAGE_AXIS));
            paddingY.add(Box.createRigidArea(new Dimension(0, 5)));
            paddingY.add(messageBody);
            paddingY.add(Box.createRigidArea(new Dimension(0, 5)));

            JPanel paddingX = new JPanel();
            paddingX.setLayout(new BoxLayout(paddingX, BoxLayout.LINE_AXIS));
            paddingX.add(Box.createRigidArea(new Dimension(5, 0)));
            paddingX.add(paddingY);
            paddingX.add(Box.createRigidArea(new Dimension(5, 0)));

            JScrollPane chatWrap = new JScrollPane(paddingX);
            contentWrapper.add(chatWrap);
            content.add(contentWrapper, BorderLayout.PAGE_START);
        }
        contentSP.repaint();
        contentSP.revalidate();
    }
}
