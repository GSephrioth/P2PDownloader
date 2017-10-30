package Peer;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.io.Serializable;
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
    private String SharedDir;

    // ip and id of the indexing server
    @XmlElement(name = "ServerIP")
    private String ServerIP;
    @XmlElement(name = "ServerPort")
    private int ServerPort;

    // ip and id of the peer server
    @XmlElement(name = "ClientIP")
    private String ClientIP;
    @XmlElement(name = "ClientPort")
    private int ClientPort;

    // remote server list
    @XmlElementWrapper(name = "RemoteServers")
    @XmlElement(name = "Server")
    private List<RemoteServerInfo> RemoteServers;

    int getClientPort() {
        return ClientPort;
    }

    String getClientIP() {
        return ClientIP;
    }

    String getServerIP() {
        return ServerIP;
    }

    int getServerPort() {
        return ServerPort;
    }

    String getSharedDir() {
        return SharedDir;
    }

    public void setSharedDir(String sharedDir) {
        SharedDir = sharedDir;
    }

    public void setClientIP(String clientIP) {
        ClientIP = clientIP;
    }

    public void setClientPort(Integer clientPort) {
        ClientPort = clientPort;
    }

    public void setServerIP(String serverIP) {
        ServerIP = serverIP;
    }

    public void setServerPort(Integer serverPort) {
        ServerPort = serverPort;
    }

    List<RemoteServerInfo> getRemoteServers() {
        return RemoteServers;
    }

    @Override
    public String toString() {
        return "Peer [Shared Directory=" + SharedDir + ", Client=" + ClientIP + ":" + ClientPort
                + ", LocalServer=" + ServerIP + ":" + ServerPort + "]\n"
                + "Remote Servers [" + RemoteServers + "]";
    }

    /*
    read the config file and convert into a JavaBean
     */
    public static PeerInfo readConfig(String configFile) {
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

    /*
    write the information to the config file
     */
    public static void writeConfig(String configFile, PeerInfo info) {
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

}
