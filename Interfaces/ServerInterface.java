package Interfaces;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface for Indexing Server
 *
 * Created by xuzhuchen on 9/20/17.
 */
public interface ServerInterface extends java.rmi.Remote{

    /*
    Search function called by peer client
    return a list of URL of peer Server
    return null if file is not found
    **/
    List<String> search(String filename) throws RemoteException;

    // Register function called by peer server
    void register(String peerServer, List<String> fileNameList) throws RemoteException;
    void register(String peerServer, String fileName) throws RemoteException;

    //method to return file from client
    byte[] obtain(String fileName) throws RemoteException;
}
