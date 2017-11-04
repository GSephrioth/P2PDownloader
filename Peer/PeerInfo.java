package Peer;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Defines all the infomation of a peer
 * <p>
 * Created by xuzhuchen on 9/20/17.
 */


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PeerInfo")
public class PeerInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    // Shared file path of the peer
    @XmlElement(name = "SharedDir")
    private String sharedDir;

    // ip and id of the indexing server
    @XmlElement(name = "ServerIP")
    private String serverIP;
    @XmlElement(name = "ServerPort")
    private int serverPort;

    // ip and id of the peer server
    @XmlElement(name = "ClientIP")
    private String clientIP;
    @XmlElement(name = "ClientPort")
    private int clientPort;

    // remote server list
    @XmlElementWrapper(name = "RemoteServers")
    @XmlElement(name = "Server")
    private List<RemoteServerInfo> remoteServers;

    // Dictionary that maps URI of files to the information identifies a specific peer


    int getClientPort() {
        return clientPort;
    }

    String getClientIP() {
        return clientIP;
    }

    String getServerIP() {
        return serverIP;
    }

    int getServerPort() {
        return serverPort;
    }

    String getSharedDir() {
        return sharedDir;
    }

    List<RemoteServerInfo> getRemoteServers() {
        return remoteServers;
    }

    public void setSharedDir(String sharedDir) {
        this.sharedDir = sharedDir;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public void setClientPort(Integer clientPort) {
        this.clientPort = clientPort;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }



    @Override
    public String toString() {
        return "Peer [Shared Directory=" + sharedDir + ", Client=" + clientIP + ":" + clientPort
                + ", LocalServer=" + serverIP + ":" + serverPort + "]\n"
                + "Remote Servers [" + remoteServers + "]";
    }


    // read the config file and convert into a JavaBean
    static PeerInfo readConfig(String configFile) {
        File config = new File(configFile);
        PeerInfo info = null;
        try {
            JAXBContext context = JAXBContext.newInstance(PeerInfo.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            info = (PeerInfo) unmarshaller.unmarshal(config);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return info;
    }


    //write the information to the config file
    static void writeConfig(String configFile, PeerInfo info) {
        File config = new File(configFile);
        try {
            JAXBContext context = JAXBContext.newInstance(info.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);
            marshaller.marshal(info, config);
        } catch (PropertyException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    /*
     * Method to read all the file in the shared directory,
     * get file name of all the files, put in a list and return
     */
    List<String> getLocalFileList() {
        List<String> fileURIList = new LinkedList<>();
        if (sharedDir == null || sharedDir.isEmpty()) return fileURIList;

        try {
            File f = new File(sharedDir);
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
}
