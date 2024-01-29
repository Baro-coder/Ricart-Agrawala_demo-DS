package sr.wat.edu.pl.core.sys.com;

import java.time.Instant;

import sr.wat.edu.pl.core.Logger;

public class Message {
    public static enum MessageType {
        DISCOVER,
        HELLO,
        BYE,
        REQUEST,
        RESPONSE,
        HEALTHCHECK_REQUEST,
        HEALTHCHECK_REPLY
    }


    private MessageType type;
    private int nodeId;
    private int timestamp;

    public Message(MessageType type, int nodeId) {
        this.type = type;
        this.nodeId = nodeId;

        Instant instant = Instant.now();
        this.timestamp = (int) instant.toEpochMilli();
    }

    public Message(MessageType type, int nodeId, int timestamp) {
        this.type = type;
        this.nodeId = nodeId;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("RA<<[%s]:[Node:%d|%d>>RA", type.name(), nodeId, timestamp);
    }

    public MessageType getType() {
        return type;
    }

    public int getNodeId() {
        return nodeId;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public static Message decodeDatagramMessage(String message) {
        if (message.startsWith("RA<<") && message.endsWith(">>RA")) {
            String datagramMsg = message.substring(5);
            int _endIndex_nodeId = datagramMsg.lastIndexOf('[') - 2;
            int _endIndex_timestamp = datagramMsg.lastIndexOf('|');
            
            String datagramMsgType = datagramMsg.substring(0, _endIndex_nodeId);
            String datagramMsgNodeId = datagramMsg.substring(_endIndex_nodeId + 8, _endIndex_timestamp);
            String datagramMsgTimestamp = datagramMsg.substring(_endIndex_timestamp + 1, datagramMsg.length() - 5);

            Logger.log_debug(Message.class.getSimpleName(), " Received Datagram : " + message);
            Logger.log_debug(Message.class.getSimpleName(), "   - type : " + datagramMsgType);
            Logger.log_debug(Message.class.getSimpleName(), "   - node : " + datagramMsgNodeId);
            Logger.log_debug(Message.class.getSimpleName(), "   - time : " + datagramMsgTimestamp);

            try {
                MessageType msgType = MessageType.valueOf(datagramMsgType);
                int nodeId = Integer.parseInt(datagramMsgNodeId);
                int timestamp = Integer.parseInt(datagramMsgTimestamp);
                return new Message(msgType, nodeId, timestamp);
            } catch (Exception ignored) {}
        }
        return null;
    }
}
