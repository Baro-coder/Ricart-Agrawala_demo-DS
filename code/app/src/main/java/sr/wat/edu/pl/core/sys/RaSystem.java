package sr.wat.edu.pl.core.sys;

import java.util.ArrayList;
import java.util.List;

import sr.wat.edu.pl.controllers.PrimaryController;
import sr.wat.edu.pl.controllers.StatusPanelController;
import sr.wat.edu.pl.core.Logger;
import sr.wat.edu.pl.core.sys.Node.NodeState;
import sr.wat.edu.pl.core.sys.com.Message;
import sr.wat.edu.pl.core.sys.com.UDPMulticastClient;
import sr.wat.edu.pl.core.sys.com.UDPMulticastServer;
import sr.wat.edu.pl.core.sys.com.Message.MessageType;

public class RaSystem {
    private static RaSystem instance;

    public static RaSystem getInstance() {
        if (instance == null) {
            instance = new RaSystem();
        }
        return instance;
    }

    private ArrayList<Node> nodes;
    private Node localNode;

    private String interfaceName;
    private String multicastAddress;
    private int multicastPort;

    private int healthcheckPeriod;
    private boolean autoHealthcheck;

    private UDPMulticastServer udpMulticastServer;
    private UDPMulticastClient udpMulticastClient;

    private List<Message> requests;

    public RaSystem() {
        nodes = new ArrayList<>();
        localNode = new Node(-1);
        StatusPanelController.getInstance().setState(localNode.getState());

        interfaceName = null;
        multicastAddress = null;
        multicastPort = -1;

        healthcheckPeriod = -1;
        autoHealthcheck = false;

        udpMulticastServer = new UDPMulticastServer();
        udpMulticastClient = new UDPMulticastClient();
    }


    private void sendMessage(MessageType messageType) {
        Message message = new Message(messageType, localNode.getId());
        Logger.log_info(this.getClass().getSimpleName(), String.format("Sending message %s", message.getType()));

        try {
            udpMulticastClient.sendUDPMessage(message.toString());
        } catch (Exception e) {
            Logger.log_error(this.getClass().getSimpleName(), e.getMessage());
            Logger.log_error(this.getClass().getSimpleName(), interfaceName);
        }
    }

    private void performDiscover() {
        ArrayList<Integer> nodeIds = new ArrayList<>();

        Message msg = new Message(MessageType.DISCOVER, localNode.getId());

        try {
            nodeIds = udpMulticastClient.sendUDPMessageAndReceiveResponsedNodeIds(msg.toString(), MessageType.HELLO);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!nodeIds.isEmpty()) {
            for (Integer id : nodeIds) {
                addNode(new Node(id));
            }
        }
    }


    public void joinSystem() {
        Logger.log_info(this.getClass().getSimpleName(), "Joining system...");

        performDiscover();

        if (nodes.isEmpty()) {
            localNode = new Node(1);
        } else {
            int maxId = 0;
            for (Node node : nodes) {
                if (node.getId() > maxId) {
                    maxId = node.getId();
                }
            }
            localNode = new Node(maxId + 1);
        }
        localNode.setState(NodeState.IDLE);
        StatusPanelController.getInstance().setState(localNode.getState());

        udpMulticastServer.start();

        Logger.log_info(this.getClass().getSimpleName(), "Joined.");
    }

    public void leaveSystem() {
        Logger.log_info(this.getClass().getSimpleName(), "Leaving system...");

        // BYE send
        Message msg = new Message(MessageType.BYE, localNode.getId());

        try {
            udpMulticastClient.sendUDPMessage(msg.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Node list clear
        nodes.clear();

        // Local node reset
        localNode = new Node(0, NodeState.READY);
        StatusPanelController.getInstance().setState(localNode.getState());

        udpMulticastServer.stop();

        Logger.log_info(this.getClass().getSimpleName(), "System left.");
    }

    public void sendRequest() {
        sendMessage(MessageType.REQUEST);
        localNode.setState(NodeState.WAITING);
        StatusPanelController.getInstance().setState(localNode.getState());
    }

    public void sendResponse() {
        sendMessage(MessageType.RESPONSE);
        localNode.setState(NodeState.IDLE);
        StatusPanelController.getInstance().setState(localNode.getState());
    }

    public void sendHealthcheck() {
        sendMessage(MessageType.HEALTHCHECK_REQUEST);
    }

    private String showRequests() {
        StringBuilder builder = new StringBuilder();
        for(Message request : requests) {
            builder.append(String.format("(%d :: %d) ", request.getTimestamp(), request.getNodeId()));
        }
        return builder.toString();
    }

    private boolean isLocalNodeInQueue() {
        for(Message request : requests) {
            if (request.getNodeId() == localNode.getId()) {
                return true;
            }
        }
        return false;
    }

    public int handleRequest(Message request) {
        boolean inserted = false;

        if (requests.isEmpty()) {
            requests.add(request);
            inserted = true;
        } else {
            for (Message req : requests) {
                if (req.getTimestamp() > request.getTimestamp()) {
                    requests.add(requests.indexOf(req), request);
                    inserted = true;
                }
            }
        }
        if (!inserted) {
            requests.add(requests.size(), request);
        }

        PrimaryController.getInstance().addRequestsToList(requests.indexOf(request), String.format("T%d :: Node[%d]", request.getTimestamp(), request.getNodeId()));

        Logger.log_debug(this.getClass().getSimpleName(), "Requests: " + showRequests());

        if (isLocalNodeInQueue()) {
            return 1;
        }
        return 0;
    }
    /*
     * 
     * Getters and Setters
     * 
     */
    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void addNode(Node node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
            Logger.log_info(this.getClass().getSimpleName(), "Added node : " + node);
        }
    }

    public void removeNodeById(int nodeId) {
        for (Node node : nodes) {
            if (node.getId() == nodeId) {
                nodes.remove(node);
                Logger.log_info(this.getClass().getSimpleName(), "Removed node : " + node);
            }
        }
    }

    public Node getNodeById(int nodeId) {
        for (Node node : nodes) {
            if (node.getId() == nodeId) {
                return node;
            }
        }
        return null;
    }

    public Node getLocalNode() {
        return localNode;
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

    public void setLocalNode(Node node) {
        if (localNode.getId() != node.getId()) {
            localNode = node;
            Logger.log_info(this.getClass().getSimpleName(), "Local node -> " + localNode.getId());
        }
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
        Logger.log_info(this.getClass().getSimpleName(), "Active interface -> " + name);
    }

    public void setHealthcheckPeriod(int value) {
        healthcheckPeriod = value;
    }

    public void setAutoHealthcheck(boolean value) {
        autoHealthcheck = value;
    }
}
