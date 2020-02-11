/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package src;

import src.exceptions.InvalidGameDescriptionException;
import src.networking.Networking;

import java.util.Scanner;

public class main {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) throws InvalidGameDescriptionException {


//        System.out.println(new main().getGreeting());

//        switch (args.length) {
//            case 1: // if hosting only argument should be game description file directory
//                Networking.hostGame(args[0]);
//                break;
//            case 2: // if connecting to another player
//                Networking.connectToGame(args[0], Integer.parseInt(args[1]));
//
//        }
        System.out.println("test");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Local Port to use?");
        int localPort = scanner.nextInt();
        System.out.println("Host or join?\nh(ost)\nj(oin)");
        String mode = scanner.next();
        switch (mode){
            case "h":
                Networking.hostGame(args[0], localPort);
                break;
            case "j":
                System.out.println("IP of Host?");
                String ip = scanner.next();
                System.out.println("Port?");
                int port = scanner.nextInt();
                Networking.connectToGame(localPort, ip, port);
                break;
            default:
                throw new IllegalArgumentException();
        }


//        Parser parser = new Parser();
//        JSONObject GameJSON = Parser.readJSONFile(args[0]);
//        GameDesc gameDesc = parser.parseGameDescription(GameJSON);
////        System.out.println(gameDesc);
//
//        Player[] playerArray = {new LocalPlayer(0), new LocalPlayer(1), new LocalPlayer(2), new LocalPlayer(3)};
//        GameEngine.main(gameDesc, 0, playerArray);
    }
}
