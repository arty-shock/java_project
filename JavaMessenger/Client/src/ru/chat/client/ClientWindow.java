package ru.chat.client;

import ru.network.TCPConnection;
import ru.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private final JTextField fieldNickname = new JTextField();
    private final JTextField fieldInput = new JTextField();

   private TCPConnection connection;

    private ClientWindow(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH,HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        log.setEditable(false); //запретить редактирование
        log.setLineWrap(true);
        add(log, BorderLayout.CENTER);

        fieldInput.addActionListener(this);
        add(fieldInput, BorderLayout.SOUTH);
        add(fieldNickname, BorderLayout.NORTH);

        setVisible(true);
        try {
            connection = new TCPConnection(this, IP_ADDR, PORT);
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

    private synchronized void printMSG(String msg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }
}
