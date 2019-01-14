package ru.network;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TCPConnection {
    private final Socket socket;
    private final Thread rxThread;
    private final TCPConnectionListener eventListener;
    private final DataInputStream in;
    private final DataOutputStream out;

    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException {
        this(eventListener, new Socket(ipAddr, port));
    }

    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted()) {
                        int dataType = in.readInt();
                        if (dataType == -1) {
                            String message = in.readUTF();
                            if (message.contains("/download ")) {
                                eventListener.onRequestFile(TCPConnection.this, message.substring(message.indexOf("/download ") + 10));
                            } else {
                                eventListener.onReceiveString(TCPConnection.this, message);
                            }
                        } else if (dataType == 1) {
                            String title = in.readUTF();
                            eventListener.onReceiveFile(TCPConnection.this, title);
                        }
                    }

                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);

                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThread.start();
    }

    public synchronized void sendString(String value) {
        try {
            out.writeInt(-1);
            out.flush();
            out.writeUTF(value);
            out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void sendFile(String filepath) {
        try {
            Path path = Paths.get(filepath);
            out.writeInt(1);
            out.writeUTF(path.getFileName().toString());
            out.writeLong(Files.size(path));
            FileInputStream fis = new FileInputStream(path.toFile());
            byte[] buffer = new byte[4096];
            int write = 0;
            int totalWrite = 0;
            long remaining = Files.size(path);
            while ((write = fis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                totalWrite += write;
                remaining -= write;
                out.write(buffer, 0, write);
            }
            fis.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void getFile(String filepath, String fileName) {
        try {
            Files.createDirectories(Paths.get(filepath));
            FileOutputStream fos = new FileOutputStream(filepath + fileName);
            byte[] buffer = new byte[4096];
            int read = 0;
            int totalRead = 0;
            long remaining = in.readLong();
            while ((read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                totalRead += read;
                remaining -= read;
                fos.write(buffer, 0, read);
            }
            fos.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {

        return "TCPConnection" + socket.getInetAddress() + ": " + socket.getPort();
    }
}
