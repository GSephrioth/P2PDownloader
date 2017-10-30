package Peer;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * Defines all the infomation of a remote server
 * Created by xuzhuchen on 10/30/17.
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Server")
@XmlType(propOrder={"ID","IP","PORT"})
public class RemoteServerInfo implements Serializable {

    private static final long serialVersionUID = 2L;
    @XmlElement(name = "ID")
    private int ID;

    @XmlElement(name = "IP")
    private String IP;

    @XmlElement(name = "PORT")
    private int PORT;

    public RemoteServerInfo(){

    }

    public RemoteServerInfo(int id, String ip, int port){
        ID = id;
        IP = ip;
        PORT = port;
    }

    public int getID() {
        return ID;
    }

    public String getIP() {
        return IP;
    }

    public int getPORT() {
        return PORT;
    }

    @Override
    public String toString() {
        return "\n\t[Server:"+ID+" at "+IP+":"+PORT+"]";
    }

}
