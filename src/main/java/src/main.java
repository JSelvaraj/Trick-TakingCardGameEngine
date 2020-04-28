/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package src;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import src.ai.CardPOMDP;
import src.exceptions.InvalidGameDescriptionException;
import src.gameEngine.HostRunner;
import src.gameEngine.PlayerRunner;
import src.player.LocalPlayer;
import src.player.POMDPPlayer;
import src.player.RandomPlayer;

import java.util.concurrent.TimeUnit;

public class main {
    @Parameters(commandNames = "host", commandDescription = "Host a game")
    private static class CommandHost {
        @Parameter(names = {"-b", "-broadcast"}, description = "Whether or not to broadcast this game on the network.")
        private boolean broadcast = false;
        @Parameter(names = {"-p", "-port"}, description = "Port to host the game from", required = false)
        private int port = 1;
        @Parameter(names = {"-g", "-game"}, description = "Path to the game to host", required = true)
        private String game;
        @Parameter(names = {"-l", "-local"}, description = "Number of additional local players to include", required = false)
        private int localPlayers = 0;
        @Parameter(names = {"-a", "-ai"}, description = "Number of AI players to include", required = false)
        private int aiPlayers = 0;
        @Parameter(names = {"-r", "-rdm"}, description = "Whether to enable random events", required = false)
        private boolean enableRandomEvents = false;
    }

    @Parameters(commandNames = "join", commandDescription = "Join a game")
    private static class CommandJoin {
        @Parameter(names = {"-s", "-search"}, description = "Whether or not to search for games")
        private boolean search = false;
        @Parameter(names = {"-l", "-localPort"}, description = "Local port to use")
        private int localPort = 0;
        @Parameter(names = {"-a", "-address"}, description = "Address of the host")
        private String address;
        @Parameter(names = {"-p", "-port"}, description = "Port of the host")
        private int port;
    }

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack" , "false");
        CommandHost host = new CommandHost();
        CommandJoin join = new CommandJoin();

        JCommander jc = JCommander.newBuilder()
                .addCommand(host)
                .addCommand("join", join)
                .build();
        jc.setProgramName(main.class.getName());
        //print the usage if no arguments supplied.
        if (args.length == 0) {
            jc.usage();
            System.exit(0);
        }
        jc.parse(args);
        //See which command was input, and run what is required.
        String command = jc.getParsedCommand();
        switch (command) {
            case "host":
                if (host.broadcast) {
                    //TODO implement starting to broadcast.
                } else {
                    Thread thread = new Thread(new HostRunner(new POMDPPlayer(), host.port, host.game, host.enableRandomEvents));
                    thread.start();
                    System.out.println("Host Player1 started");

                    for (int i = 0; i < host.localPlayers; i++) {
                        System.out.println("Local player" + (i+1) + " started");
                        PlayerRunner runner = new PlayerRunner(new LocalPlayer(), "localhost", host.port,
                                true, true, host.enableRandomEvents);
                        Thread localThread = new Thread(runner);
                        localThread.start();
                    }

                    for (int i = 0; i < host.aiPlayers; i++) {
                        System.out.println("AI started");
                        PlayerRunner runner = new PlayerRunner(new POMDPPlayer(), "localhost", host.port,
                                true, false, host.enableRandomEvents);
                        Thread aiThread = new Thread(runner);
                        aiThread.start();
                    }
                }
                break;
            case "join":
                if (join.search) {

                } else {
                    Thread thread = new Thread(new PlayerRunner(new LocalPlayer(), join.address, join.port, join.localPort, false, true, false));
                    thread.start();
                }
                break;
        }
    }
}
