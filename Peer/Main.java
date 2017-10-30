package Peer;

import java.rmi.RemoteException;

/**
 * The main class to start a Peer
 * Including:
 * 1. Read the config.xml file for information of the Peer.
 * 2. Start a Server, register all the local files.
 * 3. Synchronize the Dictionary with other servers in the network.
 * 4. Start a Client.
 * Created by xuzhuchen on 10/23/17.
 */
public class Main {
    static PeerInfo info;
    static Server server;
    static Client client;



    public static void main(String args[]){
        info = PeerInfo.readConfig("CONFIG.xml");
        System.out.println(info);
        try {
            server = new Server(info);
            client = new Client(info);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return;
    }


}
