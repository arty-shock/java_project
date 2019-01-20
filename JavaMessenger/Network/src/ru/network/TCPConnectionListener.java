package ru.network;

public interface TCPConnectionListener {

    void onConnectionReady(TCPConnection tcpConnection);

    void onReceiveString(String value);

    void onDisconnect(TCPConnection tcpConnection);

    void onException(Exception e);

    void onReceiveFile(TCPConnection tcpConnection, String fileName);

    void onRequestFile(TCPConnection tcpConnection, String fileName);
}
