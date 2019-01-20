package ru.chat.server;

import ru.network.TCPConnection;
import ru.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class chatServer implements TCPConnectionListener {

    private static final int port = 8189;
    private static final String serverPath = "server/";

    public static void main(String[] args) {

        new chatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();//список из TCP соединений
    private final ArrayList<String> fileList = new ArrayList<>();//список файлов
    private final ArrayList<String> messages = new ArrayList<>();//сообщения

    private static final String membersKey = "/1a2b3c";
    private static final String rfilesKey = "/4d5e6f";
    private static final String sfilesKey = "/7g8h9i";
    private chatServer() {
        System.out.println("Server running...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        if (messages.size() != 0) {
            for (String message : messages) {
                tcpConnection.sendString(message);
            }
        }
        sendToAllConnections("Client connected " + tcpConnection);
        System.out.println("Client connected " + tcpConnection);
        updateActiveConnections();
        updateFileList();
    }

    @Override
    public synchronized void onReceiveString(String value) {
        sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnected " + tcpConnection);
        System.out.println("Client disconnected " + tcpConnection);
        updateActiveConnections();
    }

    @Override
    public synchronized void onException(Exception e) {
        System.out.println("TCPConnection: " + e);
    }

    @Override
    public synchronized void onReceiveFile(TCPConnection tcpConnection, String fileName) {
        tcpConnection.getFile(serverPath, fileName);
        fileList.add(fileName);
        updateFileList();
    }

    @Override
    public synchronized void onRequestFile(TCPConnection tcpConnection, String fileName) {
        Path path = Paths.get(serverPath + fileName);
        if (Files.exists(path)) {
            try {
                tcpConnection.sendFile(serverPath + fileName);
            }
            catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }
    }

    private void sendToAllConnections(String value) {
        messages.add(value);
        if (messages.size() > 100) {
            if (messages.get(0).contains(sfilesKey)){
                String[] nvalue = messages.get(0).split("#");
                fileList.remove(nvalue[1]);
                updateFileList();
            }
            messages.remove(0);
        }
        for (TCPConnection connection : connections) {
            connection.sendString(value);
        }
    }

    private void updateFileList() {
        for (TCPConnection connection : connections) {
            if (fileList.size() != 0) {
                StringBuilder files = new StringBuilder(rfilesKey);
                for (String s : fileList) {
                    files.append(s).append("#");
                }
                connection.sendString(files.toString());
            }
        }

    }

    private void updateActiveConnections() {
        int cnt = connections.size();
        for (int i = 0; i < cnt; i++) {
            StringBuilder members = new StringBuilder(membersKey);
            for (TCPConnection connection : connections) {
                members.append(connection).append("#");
            }
            connections.get(i).sendString(members.toString());
        }
    }
}