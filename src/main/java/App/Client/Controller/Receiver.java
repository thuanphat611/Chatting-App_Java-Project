package App.Client.Controller;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

public class Receiver implements Runnable {
    private JFrame parent;
    private BufferedReader br;
    private DataInputStream fileReceiver;
    private volatile boolean isDone;
    private Controller controller;

    public Receiver(BufferedReader in, DataInputStream fr, JFrame parent, Controller controller) {
        isDone = false;
        br = in;
        fileReceiver = fr;
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
                            String[] chatItem = new String[3];
                            chatItem[0] = splitList[1];
                            chatItem[1] = "group";
                            chatItem[2] = splitList[2];
                            chatList.add(chatItem);
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
                    JOptionPane.showMessageDialog(parent, "Register successfully, you can login with your new account now", "Register successfully", JOptionPane.INFORMATION_MESSAGE);
                    controller.toHome();
                }
                else if (header.equals("/fail"))
                    JOptionPane.showMessageDialog(parent, splitMsg[1], "Failed", JOptionPane.PLAIN_MESSAGE);
                else if (header.equals("/info")) {
                    JOptionPane.showMessageDialog(parent, splitMsg[1], "Info", JOptionPane.PLAIN_MESSAGE);
                } else if (header.equals("/refresh")) {
                    ArrayList<String[]> chatList = new ArrayList<>();
                    while (true) {
                        String listMsg = br.readLine();
                        System.out.println("Receiver: " + listMsg);
                        String[] splitList = listMsg.split("\\|");
                        String listHeader = splitList[0];

                        if (listHeader.equals("/end"))
                            break;
                        else if (listHeader.equals("/groupList")) {
                            String[] chatItem = new String[3];
                            chatItem[0] = splitList[1];
                            chatItem[1] = "group";
                            chatItem[2] = splitList[2];
                            chatList.add(chatItem);
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
                            String[] chat = new String[3];
                            chat[0] = splitList[1];
                            chat[1] = splitList[2];
                            chat[2] = splitList[3];
                            response.add(chat);
                        }
                    }
                    controller.setHistoryBuffer(response);
                    controller.setGetHistoryDone(true);
                }
                else if (header.equals("/receiveMessage")) {
                    if (!controller.getCurrentPanel().equals("chat"))
                        continue;
                    if (controller.getChatWith().equals(splitMsg[1])) {
                        controller.addMessageToPanel(splitMsg[1], splitMsg[2], splitMsg[3]);
                        controller.refreshChatPanel();
                    }
                }
                else if (header.equals("/receiveGroupMessage")) {
                    if (!controller.getCurrentPanel().equals("chat"))
                        continue;
                    if (controller.getChatWith().equals(splitMsg[1])) {
                        controller.addMessageToPanel(splitMsg[2], splitMsg[3], splitMsg[4]);
                        controller.refreshChatPanel();
                    }
                }
                else if (header.equals("/downloadFile")) {
                    String fileName = splitMsg[1];
                    File file = new File(controller.getDownloadLocation() + "\\" + fileName);
                    if (file.exists()) {
                        int fileIndex = 1;
                        file = new File(controller.getDownloadLocation() + "\\" + "(" + fileIndex + ") " + fileName);
                        while(file.exists()) {
                            fileIndex++;
                            file = new File(controller.getDownloadLocation() + "\\" + "(" + fileIndex + ") " + fileName);
                        }
                        if (!file.createNewFile()) {
                            JOptionPane.showMessageDialog(parent, "Some errors happened", "Error", JOptionPane.PLAIN_MESSAGE);
                            continue;
                        }
                        receiveFile(controller.getDownloadLocation() + "\\" + "(" + fileIndex + ") " + fileName);
                    }
                    else {
                        if (!file.createNewFile()) {
                            JOptionPane.showMessageDialog(parent, "Some errors happened", "Error", JOptionPane.PLAIN_MESSAGE);
                            continue;
                        }
                        receiveFile(controller.getDownloadLocation() + "\\" + fileName);
                    }
                    JOptionPane.showMessageDialog(parent, "File downloaded", "Info", JOptionPane.PLAIN_MESSAGE);
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Receive thread closed");
    }

    private void receiveFile(String fileName) {
        try {
            int bytes = 0;
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            long size = fileReceiver.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytes = fileReceiver.read(buffer, 0,(int) Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer, 0, bytes);
                size -= bytes;
            }
            fileOutputStream.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void close() {
        try {
            isDone = true;
            br.close();
            fileReceiver.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
