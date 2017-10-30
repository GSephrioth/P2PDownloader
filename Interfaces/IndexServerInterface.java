package Interfaces;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface for Indexing Server
 *
 * Created by xuzhuchen on 9/20/17.
 */
public interface IndexServerInterface extends java.rmi.Remote{

    /*
    Search function called by peer client
    return a list of URL of peer Server
    return null if file is not found
    **/
    List<String> search(String filename) throws RemoteException;

    // list all the files registered in the indexing server
    List<String> listAll() throws RemoteException;

    // Register function called by peer server
    void register(String peerServer, List<String> fileNameList) throws RemoteException;
    void register(String peerServer, String fileName) throws RemoteException;

    // Unregister function called by peer server when it is about to terminate
    void unregister(String peerServer, List<String> fileNameList) throws RemoteException;

    //method to return file from client
    byte[] obtain(String fileName) throws RemoteException;

}
