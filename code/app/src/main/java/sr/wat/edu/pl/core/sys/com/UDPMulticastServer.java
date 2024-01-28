package sr.wat.edu.pl.core.sys.com;

import sr.wat.edu.pl.core.Logger;


public class UDPMulticastServer {
    private String interfaceName;
    private String address;
    private int port;


    public UDPMulticastServer() {
        interfaceName = null;
        address = null;
        port = -1;
    }


    public void run() {
        Logger.log_info(this.getClass().getSimpleName(), String.format("Listening at [%s] %s:%d ...", interfaceName, address, port));
    }

    public void stop() {
        Logger.log_info(this.getClass().getSimpleName(), "Listening stoped.");
    }

    public void setInterfaceName(String name) {
        interfaceName = name;
    }

    public void setAddress(String ipAddress) {
        address = ipAddress;
    }

    public void setPort(int portNum) {
        port = portNum;
    }
}
