package ru.network;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("ALL")
public class TCPConnection {
    private final Socket socket;
    private final Thread rxThread;
    private final TCPConnectionListener eventListener;
    private final DataInputStream in;
    private final DataOutputStream out;
    private static final  String downloadKey="/download";

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
                            if (message.contains(downloadKey)) {
                                eventListener.onRequestFile(TCPConnection.this, message.substring(message.indexOf("/download ") + 10));
                            } else {
                                eventListener.onReceiveString(message);
                            }
                        } else if (dataType == 1) {
                            String title = in.readUTF();
                            eventListener.onReceiveFile(TCPConnection.this, title);
                        }
                    }

                } catch (IOException e) {
                    eventListener.onException(e);

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
            eventListener.onException(e);
            disconnect();
        }
    }

    public synchronized void sendFile(String filepath) throws IOException {
        Path path = Paths.get(filepath);
        long fileSize = Files.size(path);
        if (fileSize <= 100 * 1024 * 1024) {
            out.writeInt(1);
            out.writeUTF(path.getFileName().toString());
            out.writeLong(fileSize);
            FileInputStream fis = new FileInputStream(path.toFile());
            long remaining = Files.size(path);
            loadFile(fis,out,remaining);
            fis.close();
        } else {
            throw new IOException("File should be < 100 MB");
        }
    }
    public synchronized void getFile(String filepath, String fileName) {
        try {
            Files.createDirectories(Paths.get(filepath));
            FileOutputStream fos = new FileOutputStream(filepath + fileName);
            long remaining = in.readLong();
            loadFile(in,fos,remaining);
            fos.close();
        } catch (IOException e) {
            eventListener.onException(e);
            disconnect();
        }
    }
    public synchronized void loadFile(InputStream fin, OutputStream fout, long remaining) throws IOException{
        byte[] buffer = new byte[4096];
        int read;
        int totalRead = 0;
        while ((read = fin.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
            totalRead += read;
            remaining -= read;
            fout.write(buffer, 0, read);
        }
    }
    private synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(e);
        }
    }


    @Override
    public String toString() {

        return "TCPConnection" + socket.getInetAddress() + ": " + socket.getPort();
    }
}
