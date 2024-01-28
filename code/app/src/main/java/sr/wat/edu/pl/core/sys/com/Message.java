package sr.wat.edu.pl.core.sys.com;

import sr.wat.edu.pl.core.Logger;

public class Message {
    public static enum MessageType {
        DISCOVER,
        HELLO,
        BYE,
        REQUEST,
        RESPONSE
    }


    MessageType type;
    String nodeId;

    public Message(MessageType type, String nodeId) {
        this.type = type;
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return String.format("RA<<[%s]:[%s]>>RA", type.name(), nodeId);
    }

    public static Message decodeDatagramMessage(String message) {
        if (message.startsWith("RA<<") && message.endsWith(">>RA")) {
            String datagramMsg = message.substring(5);
            int _endIndex = datagramMsg.lastIndexOf('[') - 2;
            
            String datagramMsgType = datagramMsg.substring(0, _endIndex);
            String datagramMsgNodeId = datagramMsg.substring(_endIndex, datagramMsg.length() - 5);

            Logger.log_debug(Message.class.getSimpleName(), " Datagram : " + message);
            Logger.log_debug(Message.class.getSimpleName(), "    - Type : " + datagramMsgType);
            Logger.log_debug(Message.class.getSimpleName(), "    - Node : " + datagramMsgNodeId);
        }
        return null;
    }
}
