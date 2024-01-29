package sr.wat.edu.pl.core.sys;

import java.util.ArrayList;

import javafx.application.Platform;
import sr.wat.edu.pl.controllers.PrimaryController;
import sr.wat.edu.pl.controllers.StatusPanelController;
import sr.wat.edu.pl.core.Logger;
import sr.wat.edu.pl.core.sys.Node.NodeState;
import sr.wat.edu.pl.core.sys.com.HealthcheckDaemon;
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
    private HealthcheckDaemon healthcheckDaemon;

    private ArrayList<Message> requests;


    public RaSystem() {
        nodes = new ArrayList<>();
        localNode = new Node(-1);
        StatusPanelController.getInstance().setState(localNode.getState());
        StatusPanelController.getInstance().setNodeId(localNode.getId());

        interfaceName = null;
        multicastAddress = null;
        multicastPort = -1;

        healthcheckPeriod = -1;
        autoHealthcheck = false;

        udpMulticastServer = new UDPMulticastServer();
        udpMulticastClient = new UDPMulticastClient();
        healthcheckDaemon = new HealthcheckDaemon(healthcheckPeriod);

        requests = new ArrayList<Message>() {};
    }


    private void sendMessage(MessageType messageType) {
        Message message = new Message(messageType, localNode.getId());
        Logger.log_info(this.getClass().getSimpleName(), String.format("Sending message %s", message.getType()));

        try {
            udpMulticastClient.sendUDPMessage(message.toString());
        } catch (Exception e) {
            Logger.log_error(this.getClass().getSimpleName(), e.getMessage());
        }
    }

    private void performDiscover() {
        ArrayList<Integer> nodeIds = new ArrayList<>();

        Message msg = new Message(MessageType.DISCOVER, localNode.getId());

        try {
            nodeIds = udpMulticastClient.sendUDPMessageAndReceiveNodeIds(msg.toString(), MessageType.HELLO);
        } catch (Exception e) {
            Logger.log_error(this.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
        }

        if (!nodeIds.isEmpty()) {
            for (Integer id : nodeIds) {
                addNode(new Node(id));
            }
        }
    }

    private void performListing() {
        ArrayList<Message> requests = new ArrayList<>();

        Message msg = new Message(MessageType.LIST_REQUEST, localNode.getId());

        try {
            requests = udpMulticastClient.sendUDPMessageAndReceiveRequestsList(msg.toString(), MessageType.LIST_REPLY);
        } catch (Exception e) {
            Logger.log_error(this.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
        }

        if (!requests.isEmpty()) {
            this.requests = requests;
            ArrayList<String> records = new ArrayList<>();

            for (Message req : requests) {
                records.add(String.format("T%d :: Node[%d]", req.getTimestamp(), req.getNodeId()));
            }

            PrimaryController.getInstance().addAllRequests(records);
        }
    }

    private void performHealthcheck() {
        ArrayList<Integer> nodeIds = new ArrayList<>();

        Message msg = new Message(MessageType.HEALTHCHECK_REQUEST, localNode.getId());

        try {
            for(int i = 0; i < 4; i++) {
                ArrayList<Integer> tmpNodeIds = new ArrayList<>();
            
                tmpNodeIds = udpMulticastClient.sendUDPMessageAndReceiveNodeIds(msg.toString(), MessageType.HEALTHCHECK_REPLY);
            
                if (nodeIds == null || tmpNodeIds.size() > nodeIds.size()) {
                    nodeIds = tmpNodeIds;
                    tmpNodeIds = null;
                }

                Thread.sleep(100);
            }
        } catch (Exception e) {
            Logger.log_error(this.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
        }

        System.err.println(" [+!+] HEALTHCHECK REPLIES: " + String.valueOf(nodeIds.size()) + " / " + String.valueOf(nodes.size()));

        if (nodes.size() != nodeIds.size()) {
            for (Node node : nodes) {
                if (!nodeIds.contains(node.getId())) {
                    removeNodeById(node.getId());
                }
            }
        }
    }

    public void joinSystem() {
        Logger.log_info(this.getClass().getSimpleName(), "Joining system...");

        udpMulticastServer = new UDPMulticastServer(interfaceName, multicastAddress, multicastPort);
        udpMulticastClient = new UDPMulticastClient(interfaceName, multicastAddress, multicastPort);
        healthcheckDaemon = new HealthcheckDaemon(healthcheckPeriod);

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
        addNode(localNode);

        localNode.setState(NodeState.IDLE);
        StatusPanelController.getInstance().setState(localNode.getState());
        StatusPanelController.getInstance().setNodeId(localNode.getId());

        performListing();

        udpMulticastServer.start();

        if (autoHealthcheck) {
            healthcheckDaemon.start();
        }

        Logger.log_info(this.getClass().getSimpleName(), "Joined.");
    }

    public void leaveSystem() {
        if (localNode.getState() == NodeState.READY || localNode.getState() == NodeState.NOT_READY) {
            return;
        }

        Logger.log_info(this.getClass().getSimpleName(), "Leaving system...");

        // Stop Multicast server
        udpMulticastServer.stop();

        // Stop healthcheck daemon
        healthcheckDaemon.stop();

        // BYE send
        Message msg = new Message(MessageType.BYE, localNode.getId());

        try {
            udpMulticastClient.sendUDPMessage(msg.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Node list clear
        nodes.clear();
        PrimaryController.getInstance().clearNodes();

        // Requests clear
        requests.clear();
        PrimaryController.getInstance().clearRequestsList();

        // Local node reset
        localNode = new Node(0, NodeState.READY);
        StatusPanelController.getInstance().setState(localNode.getState());
        StatusPanelController.getInstance().setNodeId(localNode.getId());

        Logger.log_info(this.getClass().getSimpleName(), "System left.");
    }


    public void sendRequest() {
        sendMessage(MessageType.REQUEST);
        localNode.setState(NodeState.WAITING);
        StatusPanelController.getInstance().setState(localNode.getState());
    }

    public void sendResponse() {
        sendMessage(MessageType.RESPONSE);

        for (Message req : requests) {
            if (req.getNodeId() == localNode.getId()) {
                PrimaryController.getInstance().removeRequestByIndex(requests.indexOf(req));
                requests.remove(req);
                break;
            }
        }

        if (!isLocalNodeInQueue()) {
            localNode.setState(NodeState.IDLE);
            StatusPanelController.getInstance().setState(localNode.getState());
        } else if (hasAccesss()) {
            localNode.setState(NodeState.WORKING);
            StatusPanelController.getInstance().setState(localNode.getState());
        } else {
            localNode.setState(NodeState.WAITING);
            StatusPanelController.getInstance().setState(localNode.getState());
        }
    }

    public void sendHealthcheck() {
        performHealthcheck();
    }


    private String showRequests() {
        StringBuilder builder = new StringBuilder();
        for(Message request : requests) {
            builder.append(String.format("(%d :: %d) ", request.getTimestamp(), request.getNodeId()));
        }
        return builder.toString();
    }

    public ArrayList<Message> getRequests() {
        return requests;
    }

    private boolean isLocalNodeInQueue() {
        if (!requests.isEmpty()) {
            for(Message request : requests) {
                if (request.getNodeId() == localNode.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void removeNode(Node node) {
        Platform.runLater(() -> {
            PrimaryController ctl = PrimaryController.getInstance();
            ctl.removeNode(node);
        });
        nodes.remove(node);
    }

    private void removeNodeRequests(Node node) {
        ArrayList<Message> newRequests = new ArrayList<>();
        
        for (Message req : requests) {
            if (req.getNodeId() != node.getId()) {
                newRequests.add(req);
            }
        }

        ArrayList<String> records = new ArrayList<>();

        requests.clear();
        requests = newRequests;

        for (Message req : requests) {
            records.add(String.format("T%d :: Node[%d]", req.getTimestamp(), req.getNodeId()));
        }

        Platform.runLater(() -> {
            PrimaryController.getInstance().clearRequestsList();

            PrimaryController.getInstance().addAllRequests(records);
        });

        
    }


    public boolean hasAccesss() {
        if (!requests.isEmpty()) {
            if (requests.get(0).getNodeId() == localNode.getId()) {
                return true;
            }
        }
        return false;
    }

    public void handleRequest(Message request) {
        Platform.runLater(() -> {
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

            PrimaryController.getInstance().addRequestToList(requests.indexOf(request), String.format("T%d :: Node[%d]", request.getTimestamp(), request.getNodeId()));

            Logger.log_debug(this.getClass().getSimpleName(), "Requests: " + showRequests());

            if (!isLocalNodeInQueue()) {
                sendResponse();
            } else if (hasAccesss()) {
                localNode.setState(NodeState.WORKING);
                StatusPanelController.getInstance().setState(localNode.getState());
            }
        });
    }

    public void handleResponse(Message response) {
        Platform.runLater(() -> {
            for (Message req : requests) {
                if (req.getNodeId() == response.getNodeId()) {
                    PrimaryController.getInstance().removeRequestByIndex(requests.indexOf(req));
                    requests.remove(req);
                    break;
                }
            }

            if (hasAccesss()) {
                localNode.setState(NodeState.WORKING);
                StatusPanelController.getInstance().setState(localNode.getState());
            }
        });
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void addNode(Node node) {
        if (!nodes.contains(node)) {
            Platform.runLater(() -> {
                PrimaryController.getInstance().addNodeToList(node);
            });
            nodes.add(node);
            Logger.log_info(this.getClass().getSimpleName(), "Added node : " + node);
        }
    }

    public void removeNodeById(int nodeId) {
        for (Node node : nodes) {
            if (node.getId() == nodeId) {

                removeNodeRequests(node);
                
                removeNode(node);

                Logger.log_info(this.getClass().getSimpleName(), "Removed node : " + node);

                Platform.runLater(() -> {
                    if (!isLocalNodeInQueue()) {
                        localNode.setState(NodeState.IDLE);
                        StatusPanelController.getInstance().setState(localNode.getState());
                    } else if (hasAccesss()) {
                        localNode.setState(NodeState.WORKING);
                        StatusPanelController.getInstance().setState(localNode.getState());
                    } else {
                        localNode.setState(NodeState.WAITING);
                        StatusPanelController.getInstance().setState(localNode.getState());
                    }
                });
                
                break;
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
        healthcheckDaemon.setPeriod(value);
    }

    public void setAutoHealthcheck(boolean value) {
        autoHealthcheck = value;
        
        if (value == true && localNode.getState() != NodeState.NOT_READY && localNode.getState() != NodeState.READY) {
            // Run background task
            healthcheckDaemon.start();
        } else {
            // Interrupt background task
            healthcheckDaemon.stop();
        }
    }

    public UDPMulticastServer getUdpMulticastServer() {
        return udpMulticastServer;
    }
}
