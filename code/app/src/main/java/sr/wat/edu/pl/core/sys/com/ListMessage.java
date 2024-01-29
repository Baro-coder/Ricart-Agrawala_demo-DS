package sr.wat.edu.pl.core.sys.com;

import java.util.ArrayList;

import sr.wat.edu.pl.core.Logger;

public class ListMessage extends Message {

    private ArrayList<Message> requests;

    public ListMessage(MessageType type, int nodeId, ArrayList<Message> requests) {
        super(type, nodeId);
        this.requests = requests;
    }

    public ListMessage(MessageType type, int nodeId, int timestamp, ArrayList<Message> requests) {
        super(type, nodeId, timestamp);
        this.requests = requests;
    }

    public ArrayList<Message> getRequests() {
        return requests;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format("RA<<[%s]:[Node:%d|%d{", type.name(), nodeId, timestamp));
        
        if (!requests.isEmpty()) {
            for (Message req : requests) {
                builder.append(String.format("%s;", req.shortInfo()));
            }
            builder.deleteCharAt(builder.length() - 1);
        }

        builder.append("}>>RA");

        return builder.toString();
    }

    public static ListMessage decodeDatagramListMessage(String message) {
        if (message.startsWith("RA<<") && message.endsWith(">>RA")) {
            String datagramMsg = message.substring(5);
            int _endIndex_nodeId = datagramMsg.lastIndexOf('[') - 2;
            int _endIndex_timestamp = datagramMsg.lastIndexOf('|');
            int _endIndex_list = datagramMsg.lastIndexOf('{');
            
            String datagramMsgType = datagramMsg.substring(0, _endIndex_nodeId);
            String datagramMsgNodeId = datagramMsg.substring(_endIndex_nodeId + 8, _endIndex_timestamp);
            String datagramMsgTimestamp = datagramMsg.substring(_endIndex_timestamp + 1, _endIndex_list);
            String datagramMsgList = datagramMsg.substring(_endIndex_list + 1, datagramMsg.length() - 5);

            Logger.log_debug(Message.class.getSimpleName(), " Received Datagram : " + message);

            try {
                MessageType msgType = MessageType.valueOf(datagramMsgType);
                int nodeId = Integer.parseInt(datagramMsgNodeId);
                int timestamp = Integer.parseInt(datagramMsgTimestamp);

                ArrayList<Message> requests = new ArrayList<>();

                String[] records = datagramMsgList.split(";");
                for (String r : records) {
                    String[] parts = r.split(",");
                    
                    int t = Integer.parseInt(parts[0]);
                    int n = Integer.parseInt(parts[1]);

                    requests.add(new Message(MessageType.REQUEST, n, t));
                }

                return new ListMessage(msgType, nodeId, timestamp, requests);

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return null;
    }
}
