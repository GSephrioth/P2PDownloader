package Peer;

import Interfaces.ServerInterface;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Indexing Server part implements register service for peer server and search service for peer client.
 * Created by xuzhuchen on 9/20/17.
 */
public class Server extends UnicastRemoteObject implements ServerInterface, Runnable {

    private static final long serialVersionUID = -8306441060593704819L;
    PeerInfo info;
    private String threadName;
    String serverName;
    private HashMap<String, List<String>> regisDic;
    HashMap<String,ServerInterface> servers;

    Server(PeerInfo info, HashMap<String, List<String>> regisDic) throws RemoteException {
        super();
        this.info  = info;
        this.regisDic = regisDic;
        threadName = info.getSharedDir()+"ServerThread";
        serverName = "rmi://" + info.getServerIP() + ":" + info.getServerPort() + "/server";
    }

    /*
     * try to find all servers listed in the CONFIG.xml
     * connect to found servers
     */
    private void findALLServers(){
        for (RemoteServerInfo server: info.getRemoteServers()){
            String ServerURL = server.getIP() + ":" + server.getPORT();

            ServerInterface rmiService = null;
            try {
                rmiService = (ServerInterface) LocateRegistry.getRegistry(server.getIP(), server.getPORT()).lookup("server");
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
            }
            servers.put(ServerURL,rmiService);
        }
    }

    @Override
    public byte[] obtain(String fileName) throws RemoteException {

        // create reader in order to read local file into byte array
        String pathfile = info.getSharedDir() + "/" + fileName;

        // test if file exists
        File readfile = new File(pathfile);
        if (!readfile.exists()) {
            return null;
        }

        // File length
        long size = readfile.length();
        if (size > Integer.MAX_VALUE) {
            System.out.println("File is to large");
        }

        byte[] bytes = new byte[(int)size];
        DataInputStream dis = null;

        try {
            dis = new DataInputStream(new FileInputStream(readfile));
        } catch (FileNotFoundException e) {
            return null;
        }

        // Read file & count length of read data
        int read = 0;
        int numRead = 0;
        try {
            while (read < bytes.length
                    && (numRead = dis.read(bytes, read, bytes.length - read)) >= 0) {
                read = read + numRead;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Ensure all the bytes have been read in
        if (read < bytes.length) {
            System.out.println("Unable to read: " + readfile.getName());
        }
        return bytes;

    }

    @Override
    public List<String> search(String fileURI) throws RemoteException {
        return regisDic.get(fileURI);
    }

    @Override
    public List<String> listAll() throws RemoteException {
        return new LinkedList<>(regisDic.keySet());
    }

    @Override
    public void register(String peerServer, List<String> fileNameList) throws RemoteException{
        fileNameList.forEach(filename -> {
            try {
                register(peerServer, filename);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    public void register(String peerServer, String fileName) throws RemoteException {
        // when file is already in the dictionary
        if (regisDic.containsKey(fileName)) {
            List<String> tmp = regisDic.get(fileName);
            // if the peer and file mapping does not exist, add the peer
            if (!tmp.contains(peerServer)) {
                tmp.add(peerServer);
                System.out.println("peer and file added: " + peerServer + "\t" + fileName);
            }
        } else {
            // if file is not in the dictionary, add file and peer
            List<String> list = new LinkedList<>();
            list.add(peerServer);
            regisDic.put(fileName, list);
            System.out.println("peer and file added: " + peerServer + "\t" + fileName);
        }

    }

    @Override
    public void unregister(String peerServer, List<String> fileNameList) throws RemoteException{
        for (String fileName : fileNameList) {
            // if file is in the dictionary, do nothing
            if (regisDic.containsKey(fileName)) {
                List<String> tmp = regisDic.get(fileName);
                // if the peer and file mapping exist, delete the peer
                if (tmp.contains(peerServer)) {
                    tmp.remove(peerServer);
                    System.out.println("peer and file deleted: " + peerServer + "\t" + fileName);
                }
                // if the file name does not register to any file, delete the pair
                if (tmp.isEmpty()) regisDic.remove(fileName);
            }
        }
    }

    @Override
    public void run() {
        try {
            // Get instance of Local Registry with a specific port '10001'
            LocateRegistry.createRegistry(info.getServerPort());
            // Run an RMI server: rmi://host:port/url
            Naming.bind(serverName, this);
            System.out.println(">>>>>INFO: RMI Service bind with :" + serverName);

            // synchronize with other servers every second
            while (true){
                findALLServers();

                Thread.sleep(1000);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start () {
        Thread t ;
        System.out.println("Starting " +  threadName );
        t = new Thread (this, threadName);
        t.setDaemon(true);
        t.start ();

    }
}
