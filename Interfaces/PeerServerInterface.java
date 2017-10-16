package Interfaces;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;

/**
 * interface for Clientserver
 *
 * Created by xuzhuchen on 9/20/17.
 */
public interface PeerServerInterface extends java.rmi.Remote {

    //method to return file from client
    byte[] obtain(String fileName) throws RemoteException;
}
