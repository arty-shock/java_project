package ru.network;

public interface TCPConnectionListener {

    void onConnectionReady(TCPConnection tcpConnection);

    void onReceiveString(TCPConnection tcpConnection, String value);

    void onDisconnect(TCPConnection tcpConnection);

    void onException(TCPConnection tcpConnection, Exception e);

    void onReceiveFile(TCPConnection tcpConnection, String fileName);

    void onRequestFile(TCPConnection tcpConnection, String fileName);
}
