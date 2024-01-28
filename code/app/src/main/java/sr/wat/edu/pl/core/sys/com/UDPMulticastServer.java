package sr.wat.edu.pl.core.sys.com;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
        DatagramSocket socket = null;

        try {
            // Create socket
            InetAddress serverAddress = InetAddress.getByName(address);
            socket = new DatagramSocket(port, serverAddress);

            while (isRunning) {
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
                socket.close();
            }
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
