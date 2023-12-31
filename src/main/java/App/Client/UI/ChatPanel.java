package App.Client.UI;

import App.Client.Controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ChatPanel extends JPanel {
    private JFrame parent;
    private JPanel prev;
    private Controller controller;
    private JPanel content;
    private JScrollPane contentSP;
    private String receiverName;
    private String type;
    private String username;
    private ArrayList<String[]> messages;
    public ChatPanel(JFrame parentFrame, JPanel prevPanel, Controller control, String username, String receiverName, ArrayList<String[]> messageList, String typeOfChat) {
        this.parent = parentFrame;
        this.prev = prevPanel;
        this.controller = control;
        this.receiverName = receiverName;
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
        JButton historyBtn = new JButton("History");
        JLabel receiverLbl = new JLabel();
        receiverLbl.setText(this.receiverName);
        if (type.equals("group")) {
            String[] splitReceiver = this.receiverName.split(" ");
            StringBuilder groupName = new StringBuilder(splitReceiver[1]);
            for (int i = 2; i < splitReceiver.length; i++)
                groupName.append(" ").append(splitReceiver[i]);
            receiverLbl.setText(groupName.toString().trim());
        }
        JPanel receiverWrap = new JPanel();
        receiverWrap.setLayout(new FlowLayout(FlowLayout.CENTER));
        receiverWrap.add(receiverLbl);
        header.add(backBtn, BorderLayout.LINE_START);
        header.add(receiverWrap, BorderLayout.CENTER);
        header.add(historyBtn, BorderLayout.LINE_END);
        JScrollPane headerSP = new JScrollPane(header);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(headerSP, BorderLayout.PAGE_START);

        JPanel inputSection = new JPanel();
        inputSection.setLayout(new FlowLayout(FlowLayout.CENTER));
        JTextField msgIn = new JTextField();
        JButton sendBtn = new JButton("Send");
        JButton fileBtn =  new JButton("Send file");
        msgIn.setPreferredSize(new Dimension(350, 25));
        inputSection.add(fileBtn);
        inputSection.add(Box.createRigidArea(new Dimension(10, 0)));
        inputSection.add(msgIn);
        inputSection.add(Box.createRigidArea(new Dimension(10, 0)));
        inputSection.add(sendBtn);
        JScrollPane inputSP = new JScrollPane(inputSection);
        wrapper.add(inputSP, BorderLayout.PAGE_END);

        contentSP = new JScrollPane(content);
        wrapper.add(contentSP, BorderLayout.CENTER);

        verticalMargin.add(Box.createRigidArea(new Dimension(0, 10)));
        verticalMargin.add(wrapper);
        verticalMargin.add(Box.createRigidArea(new Dimension(0, 10)));

        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.setCurrentPanel("board");
                controller.refreshRequest();
                parent.setContentPane(prev);
                parent.pack();
                parent.validate();
            }
        });

        ChatPanel thisPanel = this;
        historyBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.setCurrentPanel("history");
                controller.getChatHistory(username, receiverName, -1);
                HistoryPanel history = new HistoryPanel(parent, thisPanel, controller, username, receiverName, controller.getHistoryBuffer(), type);
                parent.setContentPane(history);
                parent.pack();
                parent.validate();
            }
        });

        sendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = msgIn.getText();
                if (message.isEmpty())
                    return;
                if (message.length() > 200) {
                    JOptionPane.showMessageDialog(parent, "Sorry, messages can only be up to 200 characters long", "Cannot send message", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                controller.sendMessage(username, receiverName, message, type);
                msgIn.setText("");
                controller.addMessageToPanel(username, message , type);
                controller.refreshChatPanel();
            }
        });

        fileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int result = fc.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    controller.sendFile(username, receiverName, fc.getSelectedFile().getPath());
                    controller.updateChatPanel(username, receiverName);
                }
            }
        });
        refreshMsg();
    }

    public void setChatList(ArrayList<String[]> list) {
        this.messages = list;
    }

    public void clearHistory() {
        this.messages.clear();
    }

    public void refreshMsg() {
        content.removeAll();
        if (messages == null || messages.isEmpty())
            return;
        JPanel contentWrapper = new JPanel();
        contentWrapper.setLayout(new BoxLayout(contentWrapper, BoxLayout.PAGE_AXIS));


        for (int i = 0; i < messages.size(); i++) {
            String[] message = messages.get(i);

            JPanel messageBody = new JPanel();
            messageBody.setLayout(new BoxLayout(messageBody, BoxLayout.PAGE_AXIS));
            JLabel msgSender = new JLabel(message[0]);
            JPanel msgSenderWrap = new JPanel();
            msgSenderWrap.setLayout(new FlowLayout(FlowLayout.LEFT));
            msgSenderWrap.add(msgSender);
            if (message[0].equals(this.username)) {
                msgSender.setText(this.username + "(you)");
                msgSender.setForeground(Color.RED);
            }
            JPanel msgContentWrap = new JPanel();
            msgContentWrap.setLayout(new BoxLayout(msgContentWrap, BoxLayout.PAGE_AXIS));

            if (message[2].equals("file")) {
                String sender = message[1].split(" : ")[0];
                String fileName = message[1].split(" : ")[1];
                String fileIndex = message[1].split(" : ")[2];
                JPanel fileMsg = new JPanel();
                fileMsg.setLayout(new BorderLayout());
                JLabel fileNameLbl = new JLabel(fileName);
                JButton download = new JButton("Download");
                JPanel space = new JPanel();
                fileMsg.add(fileNameLbl, BorderLayout.LINE_START);
                fileMsg.add(space, BorderLayout.CENTER);
                fileMsg.add(download, BorderLayout.LINE_END);
                msgContentWrap.add(fileMsg);

                download.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (type.equals("group"))
                            controller.downloadFile(sender, receiverName, fileIndex, fileName);
                        else{
                            if (username.equals(sender))
                                controller.downloadFile(sender, receiverName, fileIndex, fileName);
                            else
                                controller.downloadFile(sender, username, fileIndex, fileName);
                        }
                    }
                });
            }
            else {
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
            messageBody.add(msgSenderWrap);
            messageBody.add(msgContentWrapLeft);

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
        contentSP.revalidate();
        contentSP.repaint();
    }

    public String getReceiverName() {
        return receiverName;
    }
}