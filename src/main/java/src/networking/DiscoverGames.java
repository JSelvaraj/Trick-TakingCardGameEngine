package src.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeSet;

public class DiscoverGames {
    private static final String MULTICASTIP = "239.40.40.6";
    private static final int MULTICASTPORT = 1903;
    private static final int LOOPTIME = 10000; // time spend looking for packets


    public static void find() {
        try {
            InetAddress group = InetAddress.getByName(MULTICASTIP);
            MulticastSocket multicastGroup = new MulticastSocket(MULTICASTPORT);
            multicastGroup.joinGroup(group);
            Scanner scanner = new Scanner(System.in);
            TreeSet<String> msgQueue = new TreeSet<>();
            String input;
            do {
                gatherBeacons(msgQueue, multicastGroup);
                while (msgQueue.size() != 0) System.out.println(msgQueue.pollFirst());
                System.out.println("Continue looking? (q to quit)");
                input = scanner.nextLine();
            } while (!input.equals("q"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TreeSet<String> search() {
        TreeSet<String> beacons = new TreeSet<>();
        try {
            InetAddress group = InetAddress.getByName(MULTICASTIP);
            MulticastSocket multicastGroup = new MulticastSocket(MULTICASTPORT);
            multicastGroup.joinGroup(group);
            gatherBeacons(beacons, multicastGroup);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return beacons;
    }

    private static void gatherBeacons(TreeSet<String> beacons, MulticastSocket multicastGroup) {
        long finishLoop = System.currentTimeMillis() + LOOPTIME;
        while (System.currentTimeMillis() < finishLoop) {
            try {
                multicastGroup.setSoTimeout(10);
                byte[] buffer = new byte[1024];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, 1024);
                System.out.println("Listening for packet...");
                multicastGroup.receive(datagramPacket);
                buffer = datagramPacket.getData();
                String message = new String(buffer, 0, datagramPacket.getLength());
                System.out.println("MESSAGE RECEIVED");
                System.out.println(message);
                beacons.add(message);
            } catch (SocketException e) {
            } catch (IOException e) {
                //                    e.printStackTrace();
            }
        }
    }
}
