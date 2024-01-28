package sr.wat.edu.pl.core.net;

public class UDPMulticastServer {
    private NetInterface netInterface;
    private String address;
    private int port;


    public UDPMulticastServer() {
        netInterface = null;
        address = null;
        port = -1;
    }

    public void setNetInterface(NetInterface netInterfaceObj) {
        netInterface = netInterfaceObj;
    }

    public void setAddress(String ipAddress) {
        address = ipAddress;
    }

    public void setPort(int portNum) {
        port = portNum;
    }
}
