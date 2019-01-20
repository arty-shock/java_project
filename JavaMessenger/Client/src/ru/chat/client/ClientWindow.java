package ru.chat.client;

import ru.network.TCPConnection;
import ru.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("ALL")
public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {

    private static final String IP_ADDR = "localhost";
    private static final int PORT = 8189;
    private static final String clientPath = "client/";
    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final String membersKey = "/1a2b3c";
    private static final String rfilesKey = "/4d5e6f";
    private static final String sfilesKey = "/7g8h9i";
    private static final String downloadKey="/download";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });
    }

    private final JTextArea log = new JTextArea();
    private final JTextArea members = new JTextArea();
// --Commented out by Inspection START (20.01.2019 21:12):
//    //private final JScrollPane scrollFiles = new JScrollPane(filesArea);
//    private final JFileChooser file = new JFileChooser();
// --Commented out by Inspection STOP (20.01.2019 21:12)
    private final JTextField fieldNickname = new JTextField();
    private final JTextField fieldInput = new JTextField();

    private final DefaultListModel<String> listModel = new DefaultListModel();
    @SuppressWarnings("unchecked")
    private final JList<String> filesArea = new JList(listModel);

    private TCPConnection connection;

    private ClientWindow() {

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);

        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog, BorderLayout.CENTER);

        members.setEditable(false);
        members.setLineWrap(true);
        JScrollPane scrollMem = new JScrollPane(members);
        add(scrollMem, BorderLayout.WEST);

        JPanel listPanel = new JPanel();
        listPanel.add(new JLabel("Sent files:"));
        listPanel.add(new JScrollPane(filesArea));
        filesArea.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        add(listPanel, BorderLayout.EAST);
        filesArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    connection.sendString(downloadKey +" "+filesArea.getSelectedValue());
                }
            }
        });


        JPanel south = new JPanel(new GridLayout(1, 2));
        south.add(fieldInput);
        JButton fileButton = new JButton("Add file");
        south.add(fileButton);
        JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flow.add(south);
        Container container = getContentPane();
        container.add(flow, BorderLayout.SOUTH);


        fieldInput.addActionListener(this);
        add(fieldNickname, BorderLayout.NORTH);

        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser();
                int ret = fileopen.showDialog(null, "Открыть файл");
                if (ret == JFileChooser.APPROVE_OPTION&&connection!=null) {
                    File file = fileopen.getSelectedFile();
                    try {
                        connection.sendFile(file.getAbsolutePath());
                        connection.sendString(fieldNickname.getText() + " sent file #" + file.getName() + "#/7g8h9i");
                    }
                    catch (IOException ioe) {
                        printMSG("Connection exception: " + ioe);
                    }
                }
            }
        });

        setVisible(true);
        try {
            connection = new TCPConnection(this, IP_ADDR, PORT);

            String connectionNum = connection.toString();
            connectionNum = connectionNum.substring(connectionNum.length() - 4);
            fieldNickname.setText("Guest" + connectionNum);
        } catch (IOException e) {
            printMSG("Connection exception: " + e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();
        if (msg.equals("")) return;
        fieldInput.setText(null);
        if(connection!=null){
            connection.sendString(fieldNickname.getText() + ": " + msg);
        }else{
            printMSG("You can't send messages because server is off");
        }
    }


    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMSG("Connection ready...");
    }

    @Override
    public void onReceiveString(String value) {
        printMSG(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMSG("Connection closed...");
    }

    @Override
    public void onException(Exception e) {
        printMSG("Connection exception: " + e);
    }

    @Override
    public void onReceiveFile(TCPConnection tcpConnection, String fileName) {
        tcpConnection.getFile(clientPath, fileName);
    }


    private synchronized void printMSG(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (msg.contains(membersKey)) {
                    members.setText("Online:\n");
                    String[] membersList = msg.substring(7).split("#");
                    for (int i = 0; i < membersList.length; i++) {
                        members.append(membersList[i] + "\n");
                    }
                    members.setCaretPosition(members.getDocument().getLength());
                } else if (msg.contains(rfilesKey)) {
                    String[] sentFiles = msg.substring(7).split("#");
                    listModel.clear();
                    for (int i = 0; i < sentFiles.length; i++) {
                        listModel.addElement(sentFiles[i]);
                    }
                } else if (msg.contains(sfilesKey)) {
                    log.append(msg.replace("#", "").substring(0, msg.indexOf("/7g8h9i") - 2) + "\n");
                    log.setCaretPosition(log.getDocument().getLength());
                }else{
                    log.append(msg + "\n");
                    log.setCaretPosition(log.getDocument().getLength());
                }



            }
        });
    }

    @Override
    public synchronized void onRequestFile(TCPConnection tcpConnection, String fileName) {
    }
}
