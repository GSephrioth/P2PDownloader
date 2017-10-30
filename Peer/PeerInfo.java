package Peer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Defines all the infomation of a peer
 *
 * Created by xuzhuchen on 9/20/17.
 */


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PeerInfo")
@XmlType(propOrder={"SharedDir","ServerIP","ServerPort","ClientIP","ClientPort","RemoteServers"})
public class PeerInfo implements Serializable {

    // Shared file path of the peer
    @XmlElement(name = "SharedDir")
    private String SharedDir;

    // ip and id of the peer server
    @XmlElement(name = "ClientIP")
    private String ClientIP;
    @XmlElement(name = "ClientPort")
    private int ClientPort;

    // ip and id of the indexing server
    @XmlElement(name = "ServerIP")
    private String ServerIP;
    @XmlElement(name = "ServerPort")
    private int ServerPort;

    // remote server list
    @XmlElementWrapper(name = "RemoteServers")
    @XmlElement(name = "Server")
    private List<RemoteServerInfo> RemoteServers;

    public PeerInfo (){
        this.SharedDir = "Peer1";
        this.ClientIP = "127.0.0.1";
        this.ClientPort = 10001;
        this.ServerIP = "127.0.0.1";
        this.ServerPort = 10000;
        RemoteServers = new LinkedList<>();
    }

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

    List<RemoteServerInfo> getRemoteServers(){return RemoteServers;}

    @Override
    public String toString() {
        return "Peer [Shared Directory=" + SharedDir + ", Client=" + ClientIP+":"+ClientPort
                + ", LocalServer=" +ServerIP+":"+ServerPort+ "]\n"
                + "Remote Servers ["+RemoteServers+"]";
    }

    /**
     * Convert JavaBean into xml encoded with UTF-8
     * @param obj
     * @return
     */
    public static String convertToXml(Object obj) {
        return convertToXml(obj, "UTF-8");
    }

    /**
     * Convert JavaBean into xml
     * @param obj
     * @param encoding
     * @return
     */
    public static String convertToXml(Object obj, String encoding) {
        String result = null;
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);
            StringWriter writer = new StringWriter();
            marshaller.marshal(obj, writer);
            result = writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Convert xml into JavaBean
     * @param xml
     * @param c
     * @return
     * @throws JAXBException
     */
    @SuppressWarnings("unchecked")
    public static <T> T converyToJavaBean(String xml, Class<T> c)
            throws JAXBException {
        T t = null;
        JAXBContext context = JAXBContext.newInstance(c);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        t = (T) unmarshaller.unmarshal(new StringReader(xml));
        return t;
    }

    public static PeerInfo readConfig(String configFile){
        File config = new File(configFile);
        String xml = null;
        PeerInfo info = null;
        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(config));
            StringBuilder sb = new StringBuilder();
            String temp;
            while((temp = br.readLine()) != null){
                sb.append(temp);
            }
            xml = sb.toString();
//            System.out.println(info);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            info = PeerInfo.converyToJavaBean(xml,PeerInfo.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return info;
    }

    public static void writeConfig(String configFile,PeerInfo info){
        File config = new File(configFile);
        String xml;
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(config));
            xml = convertToXml(info);
            br.write(xml);
            br.flush();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
