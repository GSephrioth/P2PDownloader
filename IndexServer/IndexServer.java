package IndexServer;

import Interfaces.IndexServerInterface;

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
import java.util.Scanner;

/**
 * Indexing Server part implements register service for peer server and search service for peer client.
 * Created by xuzhuchen on 9/20/17.
 */
public class IndexServer extends UnicastRemoteObject implements IndexServerInterface {

    private static final long serialVersionUID = -8306441060593704819L;

    // Dictionary that maps URI of files to the information identifies a specific peer
    private HashMap<String, List<String>> regisDic = new HashMap<>();

    IndexServer() throws RemoteException {
        super();
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

    public static void start(String ip, int port) {

        try {
            // Get instance of Local Registry with a specific port '10001'
            IndexServer service = new IndexServer();
            LocateRegistry.createRegistry(port);

            // Run an RMI server: rmi://host:port/url
            String ServerName = "rmi://" + ip + ":" + port + "/register";
            Naming.bind(ServerName, service);
            System.out.println(">>>>>INFO: RMI Service bind with :" + ServerName);

            // Terminate the server when admin enter 'exit'.
            String exit = "";
            System.out.println("Enter 'exit' to terminate the server! ");
            System.out.println("Enter 'ls' to list all the files registered! ");
            while (!exit.equals("exit")) {
                Scanner scan = new Scanner(System.in);
                exit = scan.nextLine();
                if (exit.equals("ls")) {
                    service.listAll().forEach(s -> System.out.println(s));
                }
            }
            //unbind and exit
            Naming.unbind(ServerName);
            System.exit(0);

        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 2)
            start("127.0.0.1", 10000);
        else
            start(args[0], Integer.valueOf(args[1]));
    }
}
