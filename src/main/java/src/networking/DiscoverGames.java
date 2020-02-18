package src.networking;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.Scanner;

public class DiscoverGames{
    private static final String MULTICASTIP = "239.40.40.6";
    private static final int MULTICASTPORT = 1903;
    private static final int LOOPTIME = 10000; // time spend looking for packets


    public static void find() {
        try {
            InetAddress group = InetAddress.getByName(MULTICASTIP);
            MulticastSocket multicastGroup = new MulticastSocket(MULTICASTPORT);
            multicastGroup.joinGroup(group);
            Scanner scanner = new Scanner(System.in);
            LinkedList<String> msgQueue = new LinkedList<>();
            String input;
            do {
                long finishLoop = System.currentTimeMillis() + LOOPTIME;
                while (System.currentTimeMillis() < finishLoop) {
                    try {
                        if (msgQueue.peek() != null) System.out.println(msgQueue.poll());
                        multicastGroup.setSoTimeout(10);
                        byte[] buffer = new byte[1024];
                        DatagramPacket datagramPacket = new DatagramPacket(buffer, 1024);
                        System.out.println("Listening for packet...");
                        multicastGroup.receive(datagramPacket);
                        buffer = datagramPacket.getData();
                        String message = new String(buffer, 0, datagramPacket.getLength());
                        System.out.println("MESSAGE RECEIVED");
                        System.out.println(message);
                        msgQueue.add(message);
                    } catch (SocketException e) {
                    } catch (IOException e) {
    //                    e.printStackTrace();
                    }
                }
                System.out.println("Continue looking? (q to quit)");
                input = scanner.nextLine();
            } while (!input.equals("q"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
