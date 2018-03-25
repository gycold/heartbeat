package com.easyandroid.heartbeat.client;

/**
 * package: com.easyandroid.heartbeat.client.Client
 * author: gyc
 * description:客户端
 * time: create at 2018/3/26 0:20
 */

public class Client extends Thread {
    @Override
    public void run() {
        try{
            while(true){
                ClientSender.getInstance().send();
                synchronized(Client.class){
//                    this.wait(5000);
                    Thread.sleep(2000);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 程序的入口main方法
     * @param args
     */
    public static void main(String[] args){
        Client client = new Client();
        client.start();
    }
}
