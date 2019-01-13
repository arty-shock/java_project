package ru.chat.client;

import ru.network.TCPConnection;
import ru.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {

    private static final String IP_ADDR = "localhost";
    private static final int PORT = 8189;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    public static void main(String[] args){
       SwingUtilities.invokeLater(new Runnable() {
           @Override
           public void run() {
               new ClientWindow();
           }
       });
    }
    private final JTextArea log = new JTextArea();
    private final JTextArea members = new JTextArea();
    private final JScrollPane scrollLog = new JScrollPane(log);
    private final JScrollPane scrollMem = new JScrollPane(members);
    private final JFileChooser file= new JFileChooser();
    private final JTextField fieldNickname = new JTextField();
    private final JTextField fieldInput = new JTextField();
    private final JButton fileButton = new JButton("Add file");


   private TCPConnection connection;

    private ClientWindow(){

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH,HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

       /* log.setEditable(false); //запретить редактирование
        log.setLineWrap(true);
        add(log, BorderLayout.CENTER);*/

        log.setEditable(false); //запретить редактирование
        log.setLineWrap(true);
        add(scrollLog, BorderLayout.CENTER);

        members.setEditable(false); //запретить редактирование
        members.setLineWrap(true);
       add(scrollMem, BorderLayout.WEST);


        fieldInput.addActionListener(this);
        add(fieldInput, BorderLayout.SOUTH);
        add(fieldNickname, BorderLayout.NORTH);

        add(fileButton,BorderLayout.EAST);
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    JFileChooser fileopen = new JFileChooser();
                    int ret = fileopen.showDialog(null, "Открыть файл");
                    if (ret == JFileChooser.APPROVE_OPTION) {
                        File file = fileopen.getSelectedFile();
                        connection.sendFile(file.getAbsolutePath());
                        System.out.println(file.getAbsolutePath());
                    }
                }
        });

        setVisible(true);
        try {
            connection = new TCPConnection(this, IP_ADDR, PORT);
//            connection.sendFile("C:\\Users\\Артём\\Downloads\\JavaMessenger\\testfile.txt");
            String connectionNum=connection.toString();
            System.out.println(connectionNum);
            connectionNum=connectionNum.substring(connectionNum.length()-4);
            fieldNickname.setText("Guest"+connectionNum);
        } catch (IOException e) {
            printMSG("Connection exception: " + e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();
        if (msg.equals("")) return;
        fieldInput.setText(null);
        connection.sendString(fieldNickname.getText() + ": " + msg);
    }


    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMSG("Connection ready...");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMSG(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMSG("Connection closed...");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMSG("Connection exception: " + e);
    }

    @Override
    public void onReceiveFile(TCPConnection tcpConnection, String fileName) {
        tcpConnection.getFile("client/", fileName);
    }


    private synchronized void printMSG(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                members.setText("");
                if (msg.contains("/1a2b3c")) {
                    String nmsg = msg.substring(7);
                    System.out.println(nmsg);
                    members.append(nmsg);
                    members.setCaretPosition(members.getDocument().getLength());
                } else {
                    log.append(msg + "\n");
                    log.setCaretPosition(log.getDocument().getLength());
                }

            }
        });
    }

    @Override
    public synchronized  void onRequestFile(TCPConnection tcpConnection, String fileName) { }
}
