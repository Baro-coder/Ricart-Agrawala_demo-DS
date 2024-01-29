package sr.wat.edu.pl.core.sys.com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import sr.wat.edu.pl.core.Logger;
import sr.wat.edu.pl.core.sys.RaSystem;
import sr.wat.edu.pl.core.sys.com.Message.MessageType;


public class UDPMulticastServer implements Runnable {
    private boolean isRunning;

    private String interfaceName;
    private String address;
    private int port;


    public UDPMulticastServer() {
        isRunning = false;

        interfaceName = null;
        address = null;
        port = -1;
    }

    @Override
    public void run() {
        isRunning = true;

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

            while (isRunning) {
                // Prepare packet to being received
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                // Listening
                socket.receive(receivePacket);

                // Gather datagram
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.err.println(" [!!!] UDP Server recv msg: " + receivedMessage);

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
                                // ignore;
                            break;
                        case HELLO:
                                // ignore;
                            break;
                        case HEALTHCHECK_REQUEST:
                                // resend HEALTHCHECK_REPLY;
                                responseType = MessageType.HEALTHCHECK_REPLY;
                            break;
                        case HEALTHCHECK_REPLY:
                                // ignore;
                            break;
                        case REQUEST:
                                // TODO: Check the list
                                // -- send RESPONSE or ignore;
                            break;
                        case RESPONSE:
                                // TODO: Check the list
                                // -- and ignore;
                            break;
                        default:
                            break;
                    }

                    if (responseType != null) {
                        // Build response message
                        String responseData = new Message(responseType, RaSystem.getInstance().getLocalNode().getId()).toString();
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
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();

        Logger.log_info(this.getClass().getSimpleName(), String.format("Listening at [%s] %s:%d ...", interfaceName, address, port));
    }

    public void stop() {
        isRunning = false;
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
