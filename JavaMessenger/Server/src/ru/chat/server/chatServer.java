package ru.chat.server;

import ru.network.TCPConnection;
import ru.network.TCPConnectionListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;


public class chatServer implements TCPConnectionListener {

    private static final int port = 8100;

    public static void main(String[] args){

        new chatServer();
    }

    private static int Client_count = 0;//количество клиентов в чате
    private  final ArrayList<TCPConnection> connections = new ArrayList<>();//список из TCP соединений

//    private  chatServer(){
//        System.out.println("Server running...");
//        try(ServerSocketChannel server = ServerSocketChannel.open()) {
//            Selector selector = Selector.open();
//            server.configureBlocking(false);
//            server.socket().bind(new InetSocketAddress(port));
//            server.register(selector, SelectionKey.OP_ACCEPT);
//            while (true){
//                selector.select();
//                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
//                while (iterator.hasNext()) {
//                    SelectionKey key = iterator.next();
//                    iterator.remove();
//                    if (key.isAcceptable()) {
//                        SocketChannel client = server.accept();
//                        client.configureBlocking(false);
//                        client.register(selector, SelectionKey.OP_READ);
//                        try{
//                            new TCPConnection(this, client.socket());
//                        }catch (IOException e){
//                            System.out.println("TCPConnection exception: " + e);
//                        }
//                    }
//                }
//            }
//        } catch (IOException e){
//            throw new RuntimeException(e);
//        }
//    }

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

    @Override
    public synchronized void onGetOnlineUsers(TCPConnection tcpConnection) {
        for (int i = 0; i < connections.size(); i++) {
            tcpConnection.sendString("Online : " + connections.get(i));
        }
    }
}

