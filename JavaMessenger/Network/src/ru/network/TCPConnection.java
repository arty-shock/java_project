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

/**
 * TCPConnection class.
 */
public class TCPConnection {
    /**
     * Socket.
     */
    private final Socket socket;
    /**
     * Thread.
     */
    private final Thread rxThread;
    /**
     * Event listener.
     */
    private final TCPConnectionListener eventListener;
    /**
     * Input stream.
     */
    private final DataInputStream in;
    /**
     * Output stream.
     */
    private final DataOutputStream out;
    /**
     * Message key for file downloading.
     */
    private static final  String DOWNLOADKEY = "/download";
    /**
     * Buffer size for file loading.
     */
    private static final  int BUFFERSIZE = 4096;
    /**
     * Maximum file size = 100mb.
     */
    private static final  int MAXFILESIZE = 10 * 1024 * 1024;

    /**
     * TCPConnection constructor.
     * @param eListener
     * Event listener.
     * @param ipAddr
     * IP Address.
     * @param port
     * Port.
     * @throws IOException
     * Exception.
     */
    public TCPConnection(final TCPConnectionListener eListener, final String ipAddr, final int port) throws IOException {
        this(eListener, new Socket(ipAddr, port));
    }

    /**
     * TCPConnection constructor.
     * @param eListener
     * Event listener.
     * @param sct
     * Socket.
     * @throws IOException
     * Exception.
     */
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

    /**
     * Function for message sending from client to server.
     * @param value
     * Text message.
     */
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
    /**
     *  Function for file sending from client to server.
     * @param filepath
     * Shared file path.
     * @throws IOException
     * Exception.
     */
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

    /**
     *  Function for file sending from server to client.
     * @param filepath
     * Shared file path.
     * @param fileName
     * Shared file name.
     */
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

    /**
     * Function for writing from input stream to output stream.
     * @param fin
     * Input stream.
     * @param fout
     * Output stream.
     * @param remaining
     * File ending.
     * @throws IOException
     * Exception.
     */
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

    /**
     * Funciton for connection closing.
     */
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
