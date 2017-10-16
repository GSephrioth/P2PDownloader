package Peer;

import java.io.Serializable;

/**
 * Defines all the infomation of a peer
 *
 * Created by xuzhuchen on 9/20/17.
 */
class PeerInfo implements Serializable {

    // Shared file path of the peer
    private String sharedDir;

    // ip and id of the peer server
    private String peerIP;
    private int peerPort;

    // ip and id of the indexing server
    private String indexServerIP = "127.0.0.1";
    private int indexServerPort = 10000;

    PeerInfo (String SharedDir,String ip, int port){
        this.sharedDir = SharedDir;
        this.peerIP = ip;
        this.peerPort = port;
    }

    int getPeerPort() {
        return peerPort;
    }

    String getPeerIP() {
        return peerIP;
    }

    String getIndexServerIP() {
        return indexServerIP;
    }

    int getIndexServerPort() {
        return indexServerPort;
    }

    String getSharedDir() {
        return sharedDir;
    }

}
