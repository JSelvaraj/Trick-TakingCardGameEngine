/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package src;

import org.json.JSONObject;
import src.exceptions.InvalidGameDescriptionException;
import src.gameEngine.GameEngine;
import src.networking.BroadcastGames;
import src.networking.DiscoverGames;
import src.networking.Networking;
import src.parser.GameDesc;
import src.parser.Parser;
import src.player.LocalPlayer;
import src.player.Player;


import java.util.Scanner;

public class main {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) throws InvalidGameDescriptionException {


        System.out.println(new main().getGreeting());

        /*switch (args.length) {
            case 1: // if hosting only argument should be game description file directory
                Networking.hostGame(args[0]);
                break;
            case 2: // if connecting to another player
                Networking.connectToGame(args[0], Integer.parseInt(args[1]));

        }*/

        Scanner scanner = new Scanner(System.in);
        boolean localPlay = true;

        if (localPlay) {
            Parser parser = new Parser();
            JSONObject GameJSON = Parser.readJSONFile("Games/spades.json");
            GameDesc gameDesc = parser.parseGameDescription(GameJSON);
            Player[] playerArray = new Player[gameDesc.getNUMBEROFPLAYERS()];
            for (int i = 0; i< playerArray.length; i++) {
                playerArray[i] = new LocalPlayer(i);
            }

            GameEngine.main(gameDesc, 3, playerArray, 23);
        }
        else {
            System.out.println("Local Port to use?");
            int localPort = scanner.nextInt();
            do {
                System.out.println("Host or join?\nh(ost)\nj(oin)\nb(roadcast)\ns(earch)");
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
                    case "s":
                        DiscoverGames.find();
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            } while (true);
        }

    }
}
