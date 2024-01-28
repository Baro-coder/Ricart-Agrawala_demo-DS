package sr.wat.edu.pl.core.net;

public class NetInterface {
    private String name;
    private boolean supportsMulticast;

    // Contructor
    public NetInterface(String name, boolean isMulticast) {
        this.name = name;
        this.supportsMulticast = isMulticast;
    }

    // Short interface info
    // @Override
    // public String toString() {
    //     return name + " : " + address + " /" + String.valueOf(maskLength);
    // }
    
    // ----------------------------
    // ----
    // - Getters
    // ----
    public String getName() {
        return name;
    }

    public boolean supportsMulticast() {
        return supportsMulticast;
    }
}
