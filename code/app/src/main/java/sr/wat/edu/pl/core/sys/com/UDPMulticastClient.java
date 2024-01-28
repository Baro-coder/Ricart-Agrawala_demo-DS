package sr.wat.edu.pl.core.sys.com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


public class UDPMulticastClient {
    private String interfaceName;
    private String groupAddress;
    private int port;


    public UDPMulticastClient() {
        interfaceName = null;
        groupAddress = null;
        port = -1;
    }

    private static InetAddress getInetAddressForNetworkInterface(NetworkInterface networkInterface) {
        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        InetAddress inetAddress = null;
        while(inetAddresses.hasMoreElements()) {
            inetAddress = inetAddresses.nextElement();
        }
        return inetAddress;
    }

    public void sendUDPMessage(String message) throws NullPointerException, IOException {
        if (interfaceName == null) {
            throw new NullPointerException("netInterface is unset!");
        }
        NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
        InetAddress interfaceAddress = getInetAddressForNetworkInterface(networkInterface);

        if (groupAddress == null || port == -1) {
            throw new NullPointerException("groupAddress or port is unset!");
        }
        DatagramSocket socket = new DatagramSocket(new java.net.InetSocketAddress(interfaceAddress, 0));
        InetAddress group = InetAddress.getByName(groupAddress);
        byte[] msg = message.getBytes();

        DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);
        socket.send(packet);
    }
    
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public void setGroupAddress(String groupAddress) {
        this.groupAddress = groupAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
