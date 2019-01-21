package ru.network;

/**
 * Interface for TCPConnectionListener.
 */
public interface TCPConnectionListener {
    /**
     * Algorithm for modules right after connection.
     * @param tcpConnection
     * New connection.
     */
    void onConnectionReady(TCPConnection tcpConnection);

    /**
     * Algorithm for modules right after receiving text message.
     * @param value
     * Text message.
     */
    void onReceiveString(String value);

    /**
     * Algorithm for modules right after disconnect.
     * @param tcpConnection
     * Disconnected connection.
     */
    void onDisconnect(TCPConnection tcpConnection);

    /**
     * Algorithm for modules when exception catched.
     * @param e
     * Exception.
     */
    void onException(Exception e);

    /**
     * Algorithm for modules right after receiving file on server.
     * @param tcpConnection
     * Connection of file owner.
     * @param fileName
     * Sent file name.
     */
    void onReceiveFile(TCPConnection tcpConnection, String fileName);

    /**
     * Algorithm for modules right after requesting file on client.
     * @param tcpConnection
     * Connection of file recipient.
     * @param fileName
     * Sent file name.
     */
    void onRequestFile(TCPConnection tcpConnection, String fileName);
}
