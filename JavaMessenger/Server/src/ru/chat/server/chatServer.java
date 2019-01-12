package ru.chat.server;

import ru.network.TCPConnection;
import ru.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;


public class chatServer implements TCPConnectionListener {
    public static void main(String[] args){

        new chatServer();
    }

    private static int Client_count = 0;//количество клиентов в чате

    private  final ArrayList<TCPConnection> connections = new ArrayList<>();//список из TCP соединений

    private  chatServer(){
        System.out.println("Server running...");
        try(ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true){
                try{
                    new TCPConnection(this, serverSocket.accept());
                }catch (IOException e){
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);

//        Client_count++;//считаем количество подключенных
//        sendToAllConnections("Количесво участников " + Client_count ); //выводим количество подключенных

        sendToAllConnections("Client connected " + tcpConnection );
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
         sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);

//        Client_count--;//считаем количество подключенных
//        sendToAllConnections("Количесво участников: " + Client_count ); //выводим количество подключенных

        sendToAllConnections("Client disconnected " + tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection: " + e);
    }

    private void sendToAllConnections(String value){
        System.out.println(value);
        final int cnt = connections.size();
        for (int i = 0; i < cnt; i++){
            connections.get(i).sendString(value);
        }
    }
}

