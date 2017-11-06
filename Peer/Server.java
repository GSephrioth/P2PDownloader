package Peer;

import Interfaces.ServerInterface;

import javax.swing.text.html.parser.Entity;
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
 * This is Server part.
 * Methods:
 *     obtain: Providing file download service for remote clients;
 *     register: Providing register service for remote servers;
 *     synchronize: Synchronizing file list in local shared directory with remote servers;
 *     search: Providing search service for remote clients to search file directory.
 *
 * Created by xuzhuchen on 9/20/17.
 */
public class Server extends UnicastRemoteObject implements ServerInterface, Runnable {

    private static final long serialVersionUID = -8306441060593704819L;
    private PeerInfo info;
    private String threadName;
    private String serverURL;
    private HashMap<String, List<String>> regisDic;
    private List<String> localFileList;

    Server(PeerInfo info, HashMap<String, List<String>> regisDic) throws RemoteException {
        super();
        this.info  = info;
        this.regisDic = regisDic;
        threadName = info.getSharedDir()+"-ServerThread";
        serverURL = info.getServerIP() + ":" + info.getServerPort();
        localFileList = getLocalFileList();
    }

    String getServerURL() {
        return serverURL;
    }

    /*
         * try to find all servers listed in the CONFIG.xml
         * get current files in the local shared directory
         * synchronize with found servers
         */
    public void synchronize() throws RemoteException{
        // if files in local shared dir are same as last synchronization, do not need to synchronize
        if (localFileList.containsAll(getLocalFileList()))return;

        HashMap<String,ServerInterface> servers = new HashMap<>();
        for (RemoteServerInfo server: info.getRemoteServers()){
            String ServerURL = server.getIP() + ":" + server.getPORT();

            ServerInterface rmiService = null;
            try {
                rmiService = (ServerInterface) LocateRegistry.getRegistry(server.getIP(), server.getPORT()).lookup("server");
            } catch (RemoteException | NotBoundException e) {
                /*
                 It is fine if some servers are not available.
                 Thus, do nothing even if there is a exception
                  */
            }
            servers.put(ServerURL,rmiService);
        }
        for(ServerInterface server: servers.values()){
            if (server != null)
                server.register(serverURL,localFileList);
        }
    }

    /*
     * Method to read all the file in the shared directory,
     * get file name of all the files, put in a list and return
     */
    private List<String> getLocalFileList() {
        List<String> fileURIList = new LinkedList<>();
        String sharedDir = info.getSharedDir();
        if (sharedDir == null || sharedDir.isEmpty()) return fileURIList;

        try {
            File f = new File(sharedDir);
            File[] files = f.listFiles(); // get all the files under f

            for (File file : files) {
                fileURIList.add(file.getName());
            }
        } catch (NullPointerException e) {
            System.out.println("File Path not found!");
        }

        return fileURIList;
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
            if (!tmp.contains(peerServer))
                tmp.add(peerServer);
        } else {
            // if file is not in the dictionary, add file and peer
            List<String> list = new LinkedList<>();
            list.add(peerServer);
            regisDic.put(fileName, list);
        }

    }

    @Override
    public void run() {
        try {
            // register local files
            register(serverURL,getLocalFileList());
            // Get instance of Local Registry with a specific port '10001'
            LocateRegistry.createRegistry(info.getServerPort());
            // Run an RMI server: rmi://host:port/url
            Naming.bind("rmi://" + serverURL + "/server", this);

            //synchronize with other servers every second
            while (true){
                synchronize();
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

    void start () {
        Thread t ;
        System.out.println("Starting " +  threadName );
        t = new Thread (this, threadName);
        t.setDaemon(true);
        t.start ();

    }
}
