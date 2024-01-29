package sr.wat.edu.pl.core.sys.com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javafx.concurrent.Task;
import sr.wat.edu.pl.core.Logger;
import sr.wat.edu.pl.core.sys.Node;
import sr.wat.edu.pl.core.sys.RaSystem;
import sr.wat.edu.pl.core.sys.com.Message.MessageType;


public class UDPMulticastServer extends Task<Void> {
    private Thread thread;

    private String interfaceName;
    private String address;
    private int port;


    public UDPMulticastServer() {
        interfaceName = null;
        address = null;
        port = -1;
    }

    @Override
    protected Void call() throws Exception {
        InetAddress group = null;
        try {
            group = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        InetSocketAddress sockaddr = new InetSocketAddress(group, port);

        MulticastSocket socket = null;
        
        NetworkInterface networkInterface = null;
        try {
            networkInterface = NetworkInterface.getByName(interfaceName);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try {
            // Create socket
            socket = new MulticastSocket(port);
            socket.joinGroup(sockaddr, networkInterface);
            
            Logger.log_debug(this.getClass().getSimpleName(), String.format("Socket [%s:%d] opened.", address, port));

            while (!Thread.interrupted()) {
                // Prepare packet to being received
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                // Listening
                socket.receive(receivePacket);

                // Gather datagram
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // Decode message
                Message raMsg = Message.decodeDatagramMessage(receivedMessage);

                if (raMsg != null) {
                    // If message has valid protocol
                    //  * Get client port to response on
                    int clientPort = receivePacket.getPort();

                    // Prepare response
                    MessageType responseType = null;
                    switch (raMsg.getType()) {
                        case DISCOVER:
                                // resend HELLO
                                responseType = MessageType.HELLO;
                            break;
                        case BYE:
                                // remove node
                                RaSystem.getInstance().removeNodeById(raMsg.getNodeId());
                            break;
                        case LIST_REQUEST:
                                RaSystem.getInstance().addNode(new Node(raMsg.getNodeId()));
                                responseType = MessageType.LIST_REPLY;
                            break;
                        case HEALTHCHECK_REQUEST:
                                // resend HEALTHCHECK_REPLY;
                                responseType = MessageType.HEALTHCHECK_REPLY;
                            break;
                        case REQUEST:
                                RaSystem.getInstance().handleRequest(raMsg);
                            break;
                        case RESPONSE:
                                if (raMsg.getNodeId() != RaSystem.getInstance().getLocalNode().getId()) {
                                    RaSystem.getInstance().handleResponse(raMsg);
                                }
                            break;
                        default:
                            break;
                    }

                    if (responseType != null) {
                        String responseData = null;
                        if (responseType == MessageType.LIST_REPLY) {
                            ArrayList<Message> requests = RaSystem.getInstance().getRequests();
                            responseData = new ListMessage(responseType, RaSystem.getInstance().getLocalNode().getId(), requests).toString();
                        } else {
                            // Build standard response message
                            responseData = new Message(responseType, RaSystem.getInstance().getLocalNode().getId()).toString();
                        }

                        System.err.println(" [*] UDP Server : Response data : " + responseData);
                        
                        byte[] responseBuffer = responseData.getBytes();

                        // Create packet to response
                        DatagramPacket reponsePacket = new DatagramPacket(responseBuffer, responseBuffer.length, receivePacket.getAddress(), clientPort);

                        // Send reponse
                        socket.send(reponsePacket);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    // Opuść grupę multicastową przed zamknięciem socketu
                    socket.leaveGroup(sockaddr, networkInterface);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        
                socket.close();
            }
            Logger.log_info(this.getClass().getSimpleName(), String.format("Socket [%s:%d] closed.", address, port));
        }
        return null;
    }

    public void start() {
        thread = new Thread(this);
        thread.start();

        Logger.log_info(this.getClass().getSimpleName(), String.format("Listening at [%s] %s:%d ...", interfaceName, address, port));
    }

    public void stop() {
        thread.interrupt();
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
