//http://www.kodejava.org/examples/250.html
package spdv;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MacAddress {

    public static String get() {
        try {
            //InetAddress.getByName("192.168.46.53");
            final NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            if (null != ni) {
                final byte[] mac = ni.getHardwareAddress();
                if (null != mac) {
                    final StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append (String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    return sb.toString();
                } else {
                    System.out.println("Address doesn't exist or is not accessible.");
                }
            } else {
                System.out.println("Network Interface for the specified address is not found.");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "??-??-??-??-??-??";
    }
}
