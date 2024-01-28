package sr.wat.edu.pl.core.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class UDPMulticastClient {
    private NetInterface netInterface;
    private String groupAddress;
    private int port;


    public UDPMulticastClient() {
        netInterface = null;
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
        if (netInterface == null) {
            throw new NullPointerException("netInterface is unset!");
        }
        NetworkInterface networkInterface = NetworkInterface.getByName(netInterface.getName());
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
    
    public void setNetInterface(NetInterface netInterface) {
        this.netInterface = netInterface;
    }

    public void setGroupAddress(String groupAddress) {
        this.groupAddress = groupAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
