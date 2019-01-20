package ru.network;

//import java.io.*;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
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
    private static final  String DOWNLOADKEY = "/download";
    private static final  int BUFFERSIZE = 4096;
    private static final  int MAXFILESIZE = 10 * 1024 * 1024;

    public TCPConnection(final TCPConnectionListener eListener, final String ipAddr, final int port) throws IOException {
        this(eListener, new Socket(ipAddr, port));
    }

    public TCPConnection(final TCPConnectionListener eListener, final Socket sct) throws IOException {
        this.eventListener = eListener;
        this.socket = sct;
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
                            if (message.contains(DOWNLOADKEY)) {
                                eventListener.onRequestFile(TCPConnection.this, message.substring(message.indexOf(DOWNLOADKEY) + DOWNLOADKEY.length() + 1));
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

    public final synchronized void sendString(final String value) {
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

    public final synchronized void sendFile(final String filepath) throws IOException {
        Path path = Paths.get(filepath);
        long fileSize = Files.size(path);
        if (fileSize <= MAXFILESIZE) {
            out.writeInt(1);
            out.writeUTF(path.getFileName().toString());
            out.writeLong(fileSize);
            FileInputStream fis = new FileInputStream(path.toFile());
            long remaining = Files.size(path);
            loadFile(fis, out, remaining);
            fis.close();
        } else {
            throw new IOException("File should be < 100 MB");
        }
    }
    public final synchronized void getFile(final String filepath, final String fileName) {
        try {
            Files.createDirectories(Paths.get(filepath));
            FileOutputStream fos = new FileOutputStream(filepath + fileName);
            long remaining = in.readLong();
            loadFile(in, fos, remaining);
            fos.close();
        } catch (IOException e) {
            eventListener.onException(e);
            disconnect();
        }
    }
    public final synchronized void loadFile(final InputStream fin, final OutputStream fout, final long remaining) throws IOException {
        byte[] buffer = new byte[BUFFERSIZE];
        int read;
        int totalRead = 0;
        long left = remaining;
        while ((read = fin.read(buffer, 0, (int) Math.min(buffer.length, left))) > 0) {
            totalRead += read;
            left -= read;
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
     public final String toString() {
        return "TCPConnection" + socket.getInetAddress() + ": " + socket.getPort();
    }
}
