package Peer;

import Interfaces.IndexServerInterface;
import Interfaces.PeerServerInterface;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Peer part implements services of peer client and peer server
 * Created by xuzhuchen on 9/20/17.
 */
public class Peer extends UnicastRemoteObject implements PeerServerInterface {

    private PeerInfo info;

    Peer(String sharedDir, String ip, int port) throws RemoteException {
        info = new PeerInfo(sharedDir, ip, port);
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

    /*
     * Method to set up connection with peer,
     * call obtain() to get file,
     * write file to local shared directory
     */
    private static void getFile(String fileName, String peerServer, String saveDir) throws IOException {

        // Detach information of peer Server
        String[] a = peerServer.split(":");
        String peerServerIP = a[0];
        int peerServerPort = Integer.valueOf(a[1]);

        // Connect to peer and obtain file
        PeerServerInterface servingPeer;

        // byte array that will contain file contents
        byte[] temp = null;

        // attempt to lookup clientserver
        try {
            Registry r = LocateRegistry.getRegistry(peerServerIP, peerServerPort);
            servingPeer = (PeerServerInterface) r.lookup("download");
            temp = servingPeer.obtain(fileName);
        } catch (NotBoundException e) {
            System.out.println("\nError - Invalid peer entered.  Please enter valid peer");
            return;
        }

        // invalid file
        if (temp == null) {
            System.out.println("\nError - Invalid fileName for specified peer.  Please enter valid fileName");
            return;
        }

        // write file to shared directory
        String strFilePath = saveDir + "/" + fileName;

        try {
            // write file
            FileOutputStream fos = new FileOutputStream(strFilePath);
            fos.write(temp);
            fos.close();

        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException : " + ex);
        } catch (IOException ioe) {
            System.out.println("IOException : " + ioe);
        }
    }

    /*
     * Method to read all the file in the shared directory,
     * get file name of all the files, put in a list and return
     */
    private List<String> readAllFiles(String filePath) {
        List<String> fileURIList = new LinkedList<>();
        if (filePath == null || filePath.isEmpty()) return fileURIList;

        try {
            File f = new File(filePath);
            File[] files = f.listFiles(); // 得到f文件夹下面的所有文件。

            for (File file : files) {
                fileURIList.add(file.getName());
//                System.out.println(file.getName());
            }
        } catch (NullPointerException e) {
            System.out.println("File Path not found!");
        }

        return fileURIList;
    }

    private void printMenu(int x) {
        switch (x) {
            case 1: {
                System.out.println("\n1. List all files on 'Indexing Server'.");
                System.out.println("2. Search for a file.");
                System.out.println("3. Exit.");
                break;
            }
            case 2: {
                System.out.println("\nplease type name of the file you want: ");
                break;
            }
        }

    }

    void start() throws RemoteException {

        String peerServerURL = info.getPeerIP() + ":" + info.getPeerPort();
        try {
            /*
             * connect to the index server
             * read all file names and register to index server
             * */
            Registry r = LocateRegistry.getRegistry(info.getIndexServerIP(), info.getIndexServerPort());
            IndexServerInterface rmiService = (IndexServerInterface) r.lookup("register");
            List<String> fileURIList = readAllFiles(info.getSharedDir());
            rmiService.register(peerServerURL, fileURIList);

            /*
             * bind peer Server to a id
             * Get instance of Local Registry with a specific id '10001'
             * */
            LocateRegistry.createRegistry(info.getPeerPort());
            String ServerName = "rmi://" + peerServerURL + "/download";
            Naming.bind(ServerName, this);
            System.out.println("ClientServer running...on id:  " + info.getPeerPort());

            // menu
            printMenu(1);
            Scanner userInput = new Scanner(System.in);
            outer:
            while (userInput.hasNext()) {
                String temp = userInput.next();
                switch (temp) {
                    case "1": {
                        System.out.println("Registered files :");
                        rmiService.listAll().forEach(s -> System.out.println(s));
                        printMenu(1);
                        break;
                    }
                    case "2": {
                        printMenu(2);
                        while (userInput.hasNext()) {
                            // get file name and call for remote search
                            String filename = userInput.next();
                            List<String> peerServerList = rmiService.search(filename);

                            // if search result is not null, print
                            if (peerServerList != null) {

                                System.out.println("\nPeer Server List:");
                                for (int i = 0; i < peerServerList.size(); i++) {
                                    System.out.println((i + 1) + ".  " + peerServerList.get(i));
                                }
                                System.out.println("\nPlease input number of the server");
                                System.out.println("type 0 to go back to main menu");

                                // Download file from peer server
                                while (userInput.hasNextInt()) {
                                    int x = userInput.nextInt();
                                    if (x == 0) {
                                        printMenu(1);
                                        continue outer;
                                    } else if (x <= peerServerList.size() && x > 0) {
                                        getFile(filename, peerServerList.get(x - 1), info.getSharedDir());
                                        System.out.println("Downloaded file: "+ filename);
                                        // register file to index server
                                        rmiService.register(peerServerURL, filename);
                                        printMenu(1);
                                        continue outer;
                                    } else System.out.println("Please input the right number of the server");
                                }
                            }
                            // if search result is null, ask user for a validate file name
                            else {
                                System.out.println("File doesn`t exist!");
                                printMenu(2);
                            }

                        }
                        break;
                    }
                    case "3": {
                        break outer;
                    }
                    default:
                        printMenu(1);
                }
            }

            // unregister, unbind and exit
            rmiService.unregister(peerServerURL, fileURIList);
            Naming.unbind(ServerName);
            System.exit(0);

        } catch (NotBoundException e) {
            System.out.println("Not bound Exception!");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.out.println("Remote Exception!");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            System.out.println("\nError - id " + info.getPeerPort()
                    + " is already bound.  Please choose a different id\n");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // without correct terminal input, use default settings
            if (args.length == 3) {
                Peer p = new Peer(args[0], args[1], Integer.valueOf(args[2]));
                p.start();
                
            } else if(args.length == 2){
                Peer p = new Peer(args[0], "127.0.0.1", Integer.valueOf(args[1]));
                p.start();
            } else {
                Peer p = new Peer("Peer1", "127.0.0.1", 10001);
                p.start();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
