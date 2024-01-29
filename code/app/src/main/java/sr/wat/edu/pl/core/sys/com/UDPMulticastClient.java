package sr.wat.edu.pl.core.sys.com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import sr.wat.edu.pl.core.Logger;

import sr.wat.edu.pl.core.sys.com.Message.MessageType;


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

        Logger.log_debug(this.getClass().getSimpleName(), "Datagram send: " + message);

        socket.close();
    }

    public ArrayList<Integer> sendUDPMessageAndReceiveNodeIds(String message, MessageType expectedResponseType) throws NullPointerException, IOException {
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

        Logger.log_debug(this.getClass().getSimpleName(), "Datagram send: " + message);

        ArrayList<Integer> nodesIds = receiveHelloUDPResponse(socket, 1000, expectedResponseType);

        socket.close();

        return nodesIds;
    }

    private static ArrayList<Integer> receiveHelloUDPResponse(DatagramSocket socket, int timeout, MessageType expectedResponseType) throws IOException {
        ArrayList<Integer> nodeIds = new ArrayList<Integer>();
        byte[] buffer = new byte[1024];
        
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);

        socket.setSoTimeout(timeout);

        try {
            while (true) {
                socket.receive(responsePacket);
                String responseMsg = new String(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength());
                
                Message raMsg = Message.decodeDatagramMessage(responseMsg);
                if (raMsg != null && raMsg.getType() == expectedResponseType) {
                    nodeIds.add(raMsg.getNodeId());
                }
            }
        } catch (java.net.SocketTimeoutException ignored) {} 

        return nodeIds;
    }

    public ArrayList<Message> sendUDPMessageAndReceiveRequestsList(String message, MessageType expectedResponseType) throws NullPointerException, IOException {
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

        Logger.log_debug(this.getClass().getSimpleName(), "Datagram send: " + message);

        ArrayList<Message> requests = receiveListUDPResponse(socket, 1000, expectedResponseType);

        socket.close();

        return requests;
    }

    private static ArrayList<Message> receiveListUDPResponse(DatagramSocket socket, int timeout, MessageType expectedResponseType) throws IOException {
        ArrayList<Message> requests = new ArrayList<Message>();
        byte[] buffer = new byte[1024];
        
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);

        socket.setSoTimeout(timeout);

        try {
            while (true) {
                socket.receive(responsePacket);
                String responseMsg = new String(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength());
                
                ListMessage raMsg = ListMessage.decodeDatagramListMessage(responseMsg);
                if (raMsg != null && raMsg.getType() == expectedResponseType) {
                    requests = raMsg.getRequests();
                    break;
                }
            }
        } catch (java.net.SocketTimeoutException ignored) {} 

        return requests;
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
