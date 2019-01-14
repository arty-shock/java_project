package ru.chat.server;

import ru.network.TCPConnection;
import ru.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class chatServer implements TCPConnectionListener {

    private static final int port = 8189;
    private static final String serverPath = "server/";

    public static void main(String[] args){

        new chatServer();
    }

    private  final ArrayList<TCPConnection> connections = new ArrayList<>();//список из TCP соединений
    private  final ArrayList<String> fileList = new ArrayList<>();//список файлов
    private final ArrayList<String> messages=new ArrayList<>();//сообщения

    private  chatServer(){
        System.out.println("Server running...");
        try(ServerSocket serverSocket = new ServerSocket(port)) {
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
        if(messages.size()!=0) {
            for(int i=0; i<messages.size();i++) {
                tcpConnection.sendString(messages.get(i));
                System.out.println(messages.get(i));
            }
        }
        sendToAllConnections("Client connected " + tcpConnection );
        updateActiveConnections(tcpConnection);
        if(fileList.size()!=0) {
            String files = "/4d5e6f";
            for (int j = 0; j < fileList.size(); j++) {
                files += (fileList.get(j) + "#");
            }
            tcpConnection.sendString(files);
        }
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
         sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnected " + tcpConnection);
        updateActiveConnections(tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection: " + e);
    }

    @Override
    public synchronized void onReceiveFile(TCPConnection tcpConnection, String fileName) {
        tcpConnection.getFile(serverPath, fileName);
        fileList.add(fileName);
        updateFileList();
    }

    @Override
    public synchronized  void onRequestFile(TCPConnection tcpConnection, String fileName) {
        Path path = Paths.get(serverPath + fileName);
        if (Files.exists(path)) {
            tcpConnection.sendFile(serverPath + fileName);
        }
    }

    private void sendToAllConnections(String value){
        messages.add(value);
        if(messages.size()>100){
            if(messages.get(0).contains("/7g8h9i")){
                String[] nvalue=messages.get(0).split("#");
                fileList.remove(nvalue[1]);
                updateFileList();
            }
            messages.remove(0);
        }
        System.out.println(value);
        int cnt = connections.size();
        for (int i = 0; i < cnt; i++){
            connections.get(i).sendString(value);

//            String members="/1a2b3c";
//            for(int j=0;j<cnt;j++){
//                members+=(connections.get(j));
//            }
//            System.out.println(members);
//            connections.get(i).sendString(members);
        }
    }
    private void updateFileList(){
        final int cnt = connections.size();
        for (int i = 0; i < cnt; i++){
            if(fileList.size()!=0) {
                String files = "/4d5e6f";
                for (int j = 0; j < fileList.size(); j++) {
                    files += (fileList.get(j) + "#");
                }
                System.out.println(files);
                connections.get(i).sendString(files);
            }
        }

    }
    private void updateActiveConnections(TCPConnection tcpConnection) {
        int cnt = connections.size();
        for (int i = 0; i < cnt; i++){
            String members="/1a2b3c";
            for(int j=0;j<cnt;j++){
                //if (connections.get(j) != tcpConnection)
                    members+=(connections.get(j)+"#");
            }
            System.out.println(members);
            connections.get(i).sendString(members);
        }
    }
}

