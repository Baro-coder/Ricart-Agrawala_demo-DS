package sr.wat.edu.pl.core.sys.com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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

    public ArrayList<Integer> sendUDPMessageAndReceiveResponsedNodeIds(String message, MessageType expectedResponseType) throws NullPointerException, IOException {
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

        CompletableFuture<ArrayList<Integer>> future = new CompletableFuture<>();

        Thread receiveThread = new Thread(() -> {
            try {
                ArrayList<Integer> receivedNodeIds = receiveUDPResponse(socket, 5, expectedResponseType);
                future.complete(receivedNodeIds);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
        receiveThread.start();

        try {
            CompletableFuture.runAsync(() -> {
                try {
                    receiveThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        socket.close();

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private static ArrayList<Integer> receiveUDPResponse(DatagramSocket socket, int timeout, MessageType expectedResponseType) throws IOException {
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
                    Logger.log_debug(UDPMulticastClient.class.getSimpleName(), "Response received: " + raMsg);
                    nodeIds.add(raMsg.getNodeId());
                }
            }
        } catch (java.net.SocketTimeoutException ignored) {} 

        return nodeIds;
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
