package ru.chat.server;
import ru.network.TCPConnection;
import ru.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Chat server class.
 */
 public final class  ChatServer implements TCPConnectionListener {
    /**
     * Server port.
     */
    private static final int PORT = 8189;
    /**
     *  Path for shared files.
     */
    private static final String SERVERPATH = "server/";

    /**
     * Main function.
     * @param args
     * Param.
     */
    public static void main(final String[] args) {
        new ChatServer();
    }

    /**
     * Current connections.
     */
    private final ArrayList<TCPConnection> connections = new ArrayList<>(); //список из TCP соединений
    /**
     * List of shared files.
     */
    private final ArrayList<String> fileList = new ArrayList<>(); //список файлов
    /**
     * Message history.
     */
    private final ArrayList<String> messages = new ArrayList<>(); //сообщения
    /**
     * Message key for connected members.
     */
    private static final String MEMBERSKEY = "/1a2b3c";
    /**
     * Message key for file-list.
     */
    private static final String R_FILESKEY = "/4d5e6f";
    /**
     * Message key for sent file.
     */
    private static final String S_FILESKEY = "/7g8h9i";
    /**
     * Limit in message history.
     */
    private static final int MESSAGELIMIT = 100;

    /**
     * Chat server constructor.
     */
    private ChatServer() {
        System.out.println("Server running...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
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
    public synchronized void onConnectionReady(final TCPConnection tcpConnection) {
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
    public synchronized void onReceiveString(final String value) {
        sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnect(final TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnected " + tcpConnection);
        System.out.println("Client disconnected " + tcpConnection);
        updateActiveConnections();
    }

    @Override
    public synchronized void onException(final Exception e) {
        System.out.println("TCPConnection: " + e);
    }

    @Override
    public synchronized void onReceiveFile(final TCPConnection tcpConnection, final String fileName) {
        tcpConnection.getFile(SERVERPATH, fileName);
        fileList.add(fileName);
        updateFileList();
    }

    @Override
    public synchronized void onRequestFile(final TCPConnection tcpConnection, final String fileName) {
        Path path = Paths.get(SERVERPATH + fileName);
        if (Files.exists(path)) {
            try {
                tcpConnection.sendFile(SERVERPATH + fileName);
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }
    }

    /**
     * Function for message sending to all active connections.
     * @param value
     * Text message.
     */
    private void sendToAllConnections(final String value) {
        messages.add(value);
        if (messages.size() > MESSAGELIMIT) {
            if (messages.get(0).contains(S_FILESKEY)) {
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

    /**
     * Function for shared files list update.
     */
    private void updateFileList() {
        for (TCPConnection connection : connections) {
            if (fileList.size() != 0) {
                StringBuilder files = new StringBuilder(R_FILESKEY);
                for (String s : fileList) {
                    files.append(s).append("#");
                }
                connection.sendString(files.toString());
            }
        }

    }

    /**
     * Function for active connections list update.
     */
    private void updateActiveConnections() {
        int cnt = connections.size();
        for (int i = 0; i < cnt; i++) {
            StringBuilder members = new StringBuilder(MEMBERSKEY);
            for (TCPConnection connection : connections) {
                members.append(connection).append("#");
            }
            connections.get(i).sendString(members.toString());
        }
    }
}
