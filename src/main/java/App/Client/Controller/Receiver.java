package App.Client.Controller;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class Receiver implements Runnable {
    private JFrame parent;
    private BufferedReader br;
    private volatile boolean isDone;
    private Controller controller;

    public Receiver(BufferedReader in, JFrame parent, Controller controller) {
        isDone = false;
        br = in;
        this.parent = parent;
        this.controller = controller;
    }
    @Override
    public void run() {
        System.out.println("Receive thread started");
        try {
            while (!isDone) {
                String msg = br.readLine();
                System.out.println("Receiver: " + msg);
                String[] splitMsg = msg.split("\\|");
                String header = splitMsg[0];

                if (header.equals("/loginSuccess")) {
                    controller.setUsername(splitMsg[1]);
                    ArrayList<String[]> chatList = new ArrayList<>();
                    while (true) {
                        String listMsg = br.readLine();
                        System.out.println("Receiver: " + listMsg);
                        String[] splitList = listMsg.split("\\|");
                        String listHeader = splitList[0];

                        if (listHeader.equals("/end"))
                            break;
                        else if (listHeader.equals("/groupList")) {
                            for (int i = 1; i < splitList.length; i++) {
                                String[] chatItem = new String[2];
                                chatItem[0] = splitList[i];
                                chatItem[1] = "group";
                                chatList.add(chatItem);
                            }
                        } else if (listHeader.equals("/onlineList")) {
                            for (int i = 1; i < splitList.length; i++) {
                                String[] chatItem = new String[2];
                                chatItem[0] = splitList[i];
                                chatItem[1] = "user";
                                chatList.add(chatItem);
                            }
                        }
                    }
                    controller.createBoardPanel(chatList);
                }
                else if (header.equals("/registerSuccess")) {
                    JOptionPane.showMessageDialog(parent, "Register successfull, you can login with your new account now", "Register successfully", JOptionPane.INFORMATION_MESSAGE);
                    controller.toHome();
                }
                else if (header.equals("/fail"))
                    JOptionPane.showMessageDialog(parent, splitMsg[1], "Failed", JOptionPane.INFORMATION_MESSAGE);
                else if (header.equals("/refresh")) {
                    ArrayList<String[]> chatList = new ArrayList<>();
                    while (true) {
                        String listMsg = br.readLine();
                        System.out.println("Receiver: " + listMsg);
                        String[] splitList = listMsg.split("\\|");
                        String listHeader = splitList[0];

                        if (listHeader.equals("/end"))
                            break;
                        else if (listHeader.equals("/groupList")) {
                            for (int i = 1; i < splitList.length; i++) {
                                String[] chatItem = new String[2];
                                chatItem[0] = splitList[i];
                                chatItem[1] = "group";
                                chatList.add(chatItem);
                            }
                        }
                        else if (listHeader.equals("/onlineList")) {
                            for (int i = 1; i < splitList.length; i++) {
                                String[] chatItem = new String[2];
                                chatItem[0] = splitList[i];
                                chatItem[1] = "user";
                                chatList.add(chatItem);
                            }
                        }
                    }
                    controller.updateBoard(chatList);
                }
                else if (header.equals("/startHistory")) {
                    ArrayList<String[]> response = new ArrayList<>();
                    while (true) {
                        String listMsg = br.readLine();
                        System.out.println("Receiver: " + listMsg);
                        String[] splitList = listMsg.split("\\|");
                        String listHeader = splitList[0];

                        if (listHeader.equals("/endHistory"))
                            break;
                        else if (listHeader.equals("/chatHistory")) {
                            String[] chat = new String[2];
                            chat[0] = splitList[1];
                            chat[1] = splitList[2];
                            response.add(chat);
                        }
                    }
                    controller.setHistoryBuffer(response);
                    controller.setGetHistoryDone(true);
                }
                else if (header.equals("/receiveMessage")) {
                    if (!controller.getCurrentPanel().equals("chat"))
                        return;
                    if (controller.getChatWith().equals(splitMsg[1])) {
                        controller.addMessageToPanel(splitMsg[1], splitMsg[2]);
                        controller.refreshChatPanel();
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Receive thread closed");
    }

    public void close() {
        try {
            isDone = true;
            br.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
