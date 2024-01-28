package sr.wat.edu.pl.core.net;

public class NetInterface {
    private String name;
    private String address;
    private int maskLength;
    private boolean supportsMulticast;

    // Contructor
    public NetInterface(String name, String address, int maskLength, boolean isMulticast) {
        this.name = name;
        this.address = address;
        this.maskLength = maskLength;
        this.supportsMulticast = isMulticast;
    }
    
    // ----------------------------
    // ----
    // - Getters
    // ----
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getMaskLength() {
        return maskLength;
    }

    public boolean supportsMulticast() {
        return supportsMulticast;
    }
}
