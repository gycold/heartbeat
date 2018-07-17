package com.easyandroid.udp;

/**
 * package: com.easyandroid.udp.SocketConnectListener
 * author: gyc
 * description:
 * time: create at 2018/7/17 9:29
 */
public interface SocketConnectListener {
    // 网络状态回调
    void onConnectStatusCallBack(NetworkState networkState);

    // 接收数据回调
    void onReceiverCallBack(int nLength, byte[] data);
}
