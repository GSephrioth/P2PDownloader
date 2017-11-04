package Peer;

import Interfaces.ServerInterface;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Peer part implements services of peer client and peer server
 * Created by xuzhuchen on 9/20/17.
 */
public class Client extends UnicastRemoteObject implements Runnable {

    PeerInfo info;
    private HashMap<String, List<String>> regisDic;
    private Thread t;
    private String threadName;

    Client(PeerInfo info, HashMap<String, List<String>> regisDic) throws RemoteException{
        this.info = info;
        this.regisDic = regisDic;
        threadName = info.getSharedDir()+"-ClientThread";
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
        ServerInterface servingPeer;

        // byte array that will contain file contents
        byte[] temp = null;

        // attempt to lookup clientserver
        try {
            Registry r = LocateRegistry.getRegistry(peerServerIP, peerServerPort);
            servingPeer = (ServerInterface) r.lookup("server");
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
     * x = 1 for main menu
     * x = 2 for download menu
     */
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



    @Override
    public void run() {

        try {
            // menu
            printMenu(1);
            Scanner userInput = new Scanner(System.in);
            outer:
            while (userInput.hasNext()) {
                String temp = userInput.next();
                switch (temp) {
                    case "1": {
                        System.out.println("Registered files :");
                        regisDic.keySet().forEach(System.out::println);
                        printMenu(1);
                        break;
                    }
                    case "2": {
                        printMenu(2);
                        while (userInput.hasNext()) {
                            // get file name and call for remote search
                            String filename = userInput.next();
                            List<String> peerServerList = regisDic.get(filename);

                            // if search result is not null, print
                            if (peerServerList != null) {

                                System.out.println("\nPeer Server List:");
                                for (int i = 0; i < peerServerList.size(); i++) {
                                    System.out.println((i + 1) + ".  " + peerServerList.get(i));
                                }

                                // Ask user to select a server for download
                                System.out.println("\nPlease input number of the server");
                                System.out.println("type 0 to go back to main menu");
                                while (userInput.hasNextInt()) {
                                    int x = userInput.nextInt();
                                    if (x == 0) {
                                        printMenu(1);
                                        continue outer;
                                    } else if (x <= peerServerList.size() && x > 0) {
                                        // download the file
                                        getFile(filename, peerServerList.get(x - 1), info.getSharedDir());
                                        System.out.println("Downloaded file: "+ filename);
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
            // exit
            System.exit(0);

        } catch (RemoteException e) {
            System.out.println("Remote Exception!");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start () {
        System.out.println("Starting " +  threadName );
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}
