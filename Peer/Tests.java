package Peer;
import Peer.Client;
import Peer.Main;
import Peer.PeerInfo;
import Peer.Server;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Test register, synchronization and obtain
 *
 * Created by xuzhuchen on 11/5/17.
 */
public class Tests {
    Server s;
    PeerInfo pi;

    // start peer1 with ./Peer1/CONGIF.xml. default server will bind to "rmi://127.0.0.1:10001/server"
    @BeforeClass
    public void startPeer(){
        String peer1cfg[] = {"Peer1/CONFIG.xml"};
        Main.main(peer1cfg);
        s = Main.server;
        pi = Main.info;
    }

    @Test
    public void registerTest() throws RemoteException {
        for (int i = 0; i < 10000; i++) {
            s.register(pi.getServerIP()+":"+pi.getServerPort(), "registerTest.txt");
        }
    }

    @Test
    public void syncTest() throws RemoteException {
        for (int i = 0; i < 10000; i++) {
            s.synchronize();
        }
    }

    @Test
    public void obtainTest() throws IOException {
        String peer2cfg[] = {"Peer2/CONFIG.xml"};
        Main.main(peer2cfg);
        Client c = Main.client;
        System.out.println(c.info.getClientPort());
        System.out.println(s.getServerURL());
        for (int i = 0; i < 10000; i++) {
            c.getFile("test1.txt",s.getServerURL(), pi.getSharedDir());
        }

    }
}
