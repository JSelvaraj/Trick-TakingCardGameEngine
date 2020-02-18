package src.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class BroadcastGames implements Runnable {

    private static final String MULTICAST_IP = "239.40.40.6";
    private static final int MULTICAST_PORT = 1903;
    private static final long BEACON_DELAY = 1000; //7 seconds

    private String gameName;
    private int portNumber;
    private int totalNumberOfPlayers;


    public BroadcastGames(String name, int port, int totalNumberOfPlayers) {
        this.gameName = name;
        this.portNumber = port;
        this.totalNumberOfPlayers = totalNumberOfPlayers;
    }

    @Override
    public void run() {
        try {
            System.out.println("Beginning Broadcast....");
            InetAddress group = InetAddress.getByName(MULTICAST_IP);
            MulticastSocket multicastGroup = new MulticastSocket(MULTICAST_PORT);
            multicastGroup.joinGroup(group);
            while (Networking.getCurrentNumberOfPlayers() < totalNumberOfPlayers) {
                String beacon = gameName + ":" + Networking.getCurrentNumberOfPlayers() + ":" + totalNumberOfPlayers + ":" + InetAddress.getLocalHost().getHostAddress() + ":" + portNumber;
                DatagramPacket beaconPacket = new DatagramPacket(beacon.getBytes(), beacon.getBytes().length, group, MULTICAST_PORT);
                multicastGroup.send(beaconPacket);
                System.out.println("Packet sent....");
                System.out.println(beacon);
                Thread.sleep(BEACON_DELAY);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
