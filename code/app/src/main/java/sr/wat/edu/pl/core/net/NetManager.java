package sr.wat.edu.pl.core.net;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import sr.wat.edu.pl.core.Logger;
import sr.wat.edu.pl.core.sys.RaSystem;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;

public class NetManager {
    // Singleton design pattern implementation
    private static NetManager instance;

    public static NetManager getInstance() {
        if (instance == null) {
            instance = new NetManager();
        }
        return instance;
    }

    // Attributes
    private static int PORT_MIN = 1024;
    private static int PORT_MAX = 60000;

    private ArrayList<NetInterface> interfaces;
    private NetInterface activeNetInterface;
    private String multicastGroupAddress;
    private int multicastGroupPort;
    private boolean autoHealthcheck;
    private int healthcheckPeriod;

    private RaSystem raSystem;


    // Constructor
    public NetManager() {
        interfaces = new ArrayList<>();
        activeNetInterface = null;
        multicastGroupAddress = null;
        multicastGroupPort = -1;
        autoHealthcheck = true;
        healthcheckPeriod = 30;

        raSystem = new RaSystem();
    }

    // Interfaces log print
    private String showInterfaces() {
        StringBuilder builder = new StringBuilder();
        for (NetInterface netInterface : interfaces) {
            if (netInterface.equals(activeNetInterface)) {
                builder.append("[" + netInterface.getName() + "]");
            } else {
                builder.append(netInterface.getName());
            }
            builder.append(", ");
        }

        return builder.toString();
    }

    public void updateInterfacesList() {
        Logger.log_info(this.getClass().getSimpleName(), "Updating interface list...");
        interfaces.clear();

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            ArrayList<NetInterface> interfacesList = new ArrayList<>();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                
                String name = networkInterface.getDisplayName();
                if (name != null && networkInterface.supportsMulticast()) {

                    List<InterfaceAddress> intfAddresses = networkInterface.getInterfaceAddresses();
                    InterfaceAddress intf = intfAddresses.get(intfAddresses.size() - 1);

                    String address = intf.getAddress().getHostAddress();
                    int maskLength = intf.getNetworkPrefixLength();

                    interfacesList.add(new NetInterface(name, address, maskLength, true));
                }
            }

            interfaces = interfacesList;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Logger.log_info(this.getClass().getSimpleName(), "Interface list: " + showInterfaces());
        }
    }

    public ArrayList<String> getInterfacesNames() {
        ArrayList<String> names = new ArrayList<>();
        
        for (NetInterface objInterface : interfaces) {
            names.add(objInterface.getName());
        }

        return (names.size() == 0) ? null : names;
    }

    public NetInterface getInterfaceByName(String name) {
        for (NetInterface objInterface : interfaces) {
            if (objInterface.getName().equals(name)) {
                return objInterface;
            }
        }
        return null;
    }

    public NetInterface getActiveInterface() {
        return activeNetInterface;
    }

    public void setActiveInterface(NetInterface obj) {
        if(obj != null){
            if ((activeNetInterface != null && !activeNetInterface.equals(obj)) || activeNetInterface == null) {
                activeNetInterface = obj;

                Logger.log_info(this.getClass().getSimpleName(), "Active interface -> " + activeNetInterface.getName());
                Logger.log_debug(this.getClass().getSimpleName(), "Interfaces: " + showInterfaces());
        
                raSystem.setInterfaceName(activeNetInterface.getName());
            }
        }
    }

    public void setMulticastGroupAddress(String aOctet, String bOctet, String cOctet, String dOctet) throws Exception{
        int aOctetInt = Integer.parseInt(aOctet);
        int bOctetInt = Integer.parseInt(bOctet);
        int cOctetInt = Integer.parseInt(cOctet);
        int dOctetInt = Integer.parseInt(dOctet);

        if (aOctetInt != 239) {
            Logger.log_error(this.getClass().getSimpleName(), "Invalid address! Available subnet: 239.0.0.0 /8");
            throw new Exception("Invalid address!\nAvailable subnet: 239.0.0.0 /8");
        } else if (bOctetInt < 0 || bOctetInt > 255) {
            Logger.log_error(this.getClass().getSimpleName(), "Invalid address!");
            throw new Exception("Invalid address!");
        } else if (cOctetInt < 0 || cOctetInt > 255) {
            Logger.log_error(this.getClass().getSimpleName(), "Invalid address!");
            throw new Exception("Invalid address!");
        } else if (dOctetInt < 0 || dOctetInt > 255) {
            Logger.log_error(this.getClass().getSimpleName(), "Invalid address!");
            throw new Exception("Invalid address!");
        }

        String groupAddress = String.format("%d.%d.%d.%d", aOctetInt, bOctetInt, cOctetInt, dOctetInt);
        
        
        if (multicastGroupAddress == null || !multicastGroupAddress.equals(groupAddress)) {
            multicastGroupAddress = groupAddress;
            Logger.log_info(this.getClass().getSimpleName(), "Multicast group -> " + multicastGroupAddress);
        }
    }

    public void setMulticastGroupPort(int port) throws Exception {
        if (port < NetManager.PORT_MIN || port > NetManager.PORT_MAX) {
            Logger.log_error(this.getClass().getSimpleName(), String.format("Invalid port! Available range: <%d, %d>", NetManager.PORT_MIN, NetManager.PORT_MAX));
            throw new Exception(String.format("Invalid port!\nAvailable range: <%d, %d>", NetManager.PORT_MIN, NetManager.PORT_MAX));
        }
        if (multicastGroupPort != port) {
            multicastGroupPort = port;
            Logger.log_info(this.getClass().getSimpleName(), "Multicast port ->  " + String.valueOf(multicastGroupPort));
        }
    }

    public void setHealthcheckPeriod(int period) {
        if (healthcheckPeriod != period) {
            healthcheckPeriod = period;
            Logger.log_info(this.getClass().getSimpleName(), "Healthcheck period ->  " + healthcheckPeriod);
        }
    }

    public void setAutoHealthcheck(boolean state) {
        if (autoHealthcheck != state) {
            autoHealthcheck = state;
            Logger.log_info(this.getClass().getSimpleName(), "Auto Healthcheck ->  " + state);
        }
    }
}
