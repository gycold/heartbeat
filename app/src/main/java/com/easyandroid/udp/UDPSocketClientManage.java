package com.easyandroid.udp;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * package: com.easyandroid.udp.UDPSocketClientManage
 * author: gyc
 * description:
 * time: create at 2018/7/17 9:30
 */
public class UDPSocketClientManage {
    // 服务器IP
    private static String SERVER_IP = "192.168.1.100";
    // 服务器端口
    private static int LOCAL_PORT_AUDIO = 5052;
    // 构造数据报包，用来将长度为 length 偏移量为 offset 的包发送到指定主机上的指定端口号。
    private DatagramPacket packetReceive;
    // 构造数据报套接字并将其绑定到本地主机上任何可用的端口。
    private DatagramSocket mSocketClient;

    NetworkState mLastNetworkState = NetworkState.NETWORK_STATE_NULL;
    SocketConnectListener mConnectListener = null;

    // 设置网络连接参数
    public void setNetworkParameter(String strIP, int nPort) {
        SERVER_IP = strIP;
        LOCAL_PORT_AUDIO = nPort;
    }

    // 注册接收连接状态和数据的回调函数
    public void registerSocketConnectListener(SocketConnectListener listener) {
        mConnectListener = listener;
    }

    /**
     * 启动连接服务器
     */
    public void connect() {
        // 正在开始连接
        mLastNetworkState = NetworkState.NETWORK_STATE_CONNECT_ING;

        try {
            // 端口
            mSocketClient = new DatagramSocket(LOCAL_PORT_AUDIO);
            // 接收数据缓存
            byte[] bufferReceive = new byte[1024];
            // 接收包
            packetReceive = new DatagramPacket(bufferReceive, 1024);

            mLastNetworkState = NetworkState.NETWORK_STATE_CONNECT_SUCCEED;

        } catch (IOException e) {
            mLastNetworkState = NetworkState.NETWORK_STATE_CONNECT_FAILLD;
            Log.e("Show", e.toString());
            e.printStackTrace();
        } catch (Exception e) {
            mLastNetworkState = NetworkState.NETWORK_STATE_CONNECT_FAILLD;
            Log.e("Show", e.toString());
            e.printStackTrace();
        }

        // 向回调发数据
        if (null != mConnectListener) {
            mConnectListener.onConnectStatusCallBack(mLastNetworkState);
        }

        if (mSocketClient != null) {
            new Thread(receiveRunnable).start();
        }
    }

    Runnable receiveRunnable = new Runnable() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            while (true) {
                try {
                    // 接收数据
                    if (packetReceive != null) {
                        mSocketClient.receive(packetReceive);

                        // 判断数据是否合法
                        InetSocketAddress address = (InetSocketAddress) packetReceive.getSocketAddress();
                        // 判断是否是调度服务器的ip
                        if (!address.getHostString().equals(SERVER_IP)) {
                            continue;
                        }
                        // 判断是否是调度服务器的端口
                        if (address.getPort() != LOCAL_PORT_AUDIO) {
                            continue;
                        }

                        int length = packetReceive.getLength();
                        if (length > 0)
                            mConnectListener.onReceiverCallBack(length, packetReceive.getData());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Show", e.toString());
                }
            }
        }
    };

    /**
     * 断开连接
     */
    public void close() {
        if (mSocketClient != null) {
            mSocketClient.close();
            mSocketClient = null;
            mLastNetworkState = NetworkState.NETWORK_STATE_DISCONNECT_SUCCEED;
            mConnectListener.onConnectStatusCallBack(mLastNetworkState);
        }
    }

    /**
     * @param data :需要发送的数据
     * @param len  :数据字节数据
     * @brief 发送数据
     */
    public void send(byte[] data, int len) {
        ThreadSend threadSend = new ThreadSend(data, len);
        new Thread(threadSend).start();
    }

    /**
     * @brief 发送线程
     */
    private class ThreadSend implements Runnable {
        // 发送数据缓存
        private byte[] bufferSend = new byte[1024];
        // 发送数据包
        private DatagramPacket packetSend;

        /**
         * @param data :需要发送的数据
         * @param len  :数据字节数据
         * @brief 构造函数
         */
        public ThreadSend(byte[] data, int len) {
            // 发送包
            packetSend = new DatagramPacket(bufferSend, 1024);
            packetSend.setData(data);
            packetSend.setLength(len);
        }

        @Override
        public void run() {
            try {
                packetSend.setPort(LOCAL_PORT_AUDIO);
                packetSend.setAddress(InetAddress.getByName(SERVER_IP));
                if (mSocketClient != null) {
                    mSocketClient.send(packetSend);
                    mLastNetworkState = NetworkState.NETWORK_STATE_TXD;
                    mConnectListener.onConnectStatusCallBack(mLastNetworkState);
                } else {
                    mLastNetworkState = NetworkState.NETWORK_STATE_NULL;
                    mConnectListener.onConnectStatusCallBack(mLastNetworkState);
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
                mLastNetworkState = NetworkState.NETWORK_STATE_NULL;
                mConnectListener.onConnectStatusCallBack(mLastNetworkState);
            } catch (IOException e) {
                e.printStackTrace();
                mLastNetworkState = NetworkState.NETWORK_STATE_NULL;
                mConnectListener.onConnectStatusCallBack(mLastNetworkState);
            }
        }
    }

    // 获取最后的网络状态
    public NetworkState getLastNetworkState() {
        return mLastNetworkState;
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("WifiPreferenceIpAddress", ex.toString());
        }
        return null;
    }
}
