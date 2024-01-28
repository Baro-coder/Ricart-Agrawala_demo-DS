package sr.wat.edu.pl.core.sys;

import java.util.ArrayList;

import sr.wat.edu.pl.core.sys.com.UDPMulticastClient;
import sr.wat.edu.pl.core.sys.com.UDPMulticastServer;

public class RaSystem {
    private static RaSystem instance;

    public static RaSystem getInstance() {
        if (instance == null) {
            instance = new RaSystem();
        }
        return instance;
    }

    private ArrayList<Node> nodes;
    private String interfaceName;
    private String multicastAddress;
    private int multicastPort;

    private UDPMulticastServer udpMulticastServer;
    private UDPMulticastClient udpMulticastClient;


    public RaSystem() {
        nodes = new ArrayList<>();
        interfaceName = null;
        multicastAddress = null;
        multicastPort = -1;

        udpMulticastServer = new UDPMulticastServer();
        udpMulticastClient = new UDPMulticastClient();
    }


    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void addNode(Node node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
        }
    }

    public Node getNodeById(String nodeId) {
        for (Node node : nodes) {
            if (node.getId().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }


    public String getMulticastAddress() {
        return multicastAddress;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setMulticastGroupAddress(String address) {
        multicastAddress = address;
        udpMulticastClient.setGroupAddress(address);
        udpMulticastServer.setAddress(address);
    }

    public void setMulticastGroupPort(int port) {
        multicastPort = port;
        udpMulticastClient.setPort(port);
        udpMulticastServer.setPort(port);
    }

    public void setInterfaceName(String name) {
        interfaceName = name;
        udpMulticastClient.setInterfaceName(name);
        udpMulticastServer.setInterfaceName(name);
    }
}
