package com.easyandroid.heartbeat.server;

import com.easyandroid.heartbeat.Entity;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * package: com.easyandroid.heartbeat.server.Server
 * author: gyc
 * description:服务器端
 * time: create at 2018/3/25 23:58
 */

public class Server extends Thread{
    private ServerSocket server = null;
    Object obj = new Object();

    @Override
    public void run() {
        try{
            while(true){
                server = new ServerSocket(25535);
                Socket client = server.accept();
                synchronized(obj){
                    new Thread(new Client(client)).start();
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 接收客户端数据的线程
     * @author USER
     *
     */
    class Client implements Runnable{
        Socket client;
        public Client(Socket client){
            this.client = client;
        }
        @Override
        public void run() {
            try{
                while(true){
                    ObjectInput in = new ObjectInputStream(client.getInputStream());
                    Entity entity = (Entity)in.readObject();
                    System.out.println(entity.getName());
                    System.out.println(entity.getSex());
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    /**
     *程序的入口main方法
     * @param args
     */
    public static void main(String[] args){
        new Server().start();
    }
}
