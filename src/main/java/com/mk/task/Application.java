package com.mk.task;

import com.mk.task.player.IPlayer;
import com.mk.task.player.Initiator;
import com.mk.task.player.Receiver;
import com.mk.task.transport.MessageHandler;
import sun.rmi.runtime.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Main class which would start up the process.
 * Responsibilities:
 * 1. Validate the program arguments passed into the program.
 * 2. Creates the required objects and does the dependency injection.
 * 3. Initializes the socket to transport the messages.
 * 4. Starts the players. There are two types of players which can be executed.
 * For more details: {@link Initiator} & {@link Receiver}
 * <p>
 * NOTE:
 * Using frameworks like Spring, JCommander, Guice will drastically reduce the code in this class.
 * However, none of the above are used considering the requirements.
 * For logging, java util logging is used. However, utilities like log4j, logback e.t.c. would be more user friendly.
 */
public class Application {

    private final static Logger logger = Logger.getLogger(Application.class.getName());

    private int maxMessages;
    private String receiverHost;
    private int port;
    private String action;

    public final static String MESSAGE = "Message for this Task -- ";
    public final static String POISON_PILL = "poison-pill";

    private final String INITIATOR_IDENTIFIER = "initiator";
    private final String RECEIVER_IDENTIFIER = "receiver";

    private Socket socket;
    private Writer writer;
    private Scanner readScanner;

    /**
     * Main program to start the process.
     * For Initiator, 4 program arguments should be passed.
     * For Receiver, passing 2 arguments should be enough. Any additional arguments will be ignored.
     * For more details: {@link #validateArgs(String[])}
     *
     * @param args
     * @throws IOException -   If there are any issues with socket initiation.
     */
    public static void main(String[] args) throws IOException {
        Application application = new Application();
        logger.info("Application is now starting .. ");
        application.start(args);
    }

    /**
     * Tasks carried out are:
     * 1. Validates the program arguments. {@link #validateArgs(String[])}
     * 2. Adds the shut down hook. {@link #addShutDownHook()}
     * 3. Starts the player. {@link IPlayer#start()}
     *
     * @param args
     * @throws IOException
     */
    private void start(String[] args) throws IOException {
        validateArgs(args);
        IPlayer player;
        if (INITIATOR_IDENTIFIER.equals(action)) {
            socket = initializeSimpleSocket(receiverHost, port);
            player = new Initiator(createMessageHandler(socket), maxMessages);
        } else {
            socket = initializeServerSocket(port);
            player = new Receiver(createMessageHandler(socket));
        }
        addShutDownHook();
        player.start();
        logger.info("Player task is now complete. So, shutting down.");
    }

    /**
     * Validates the arguments passed to the program.
     * 1. Length of arguments passed should be 3 for receiver and 4 for initiator.
     * 2. Port (2nd Arg) and max number of messages (4th Arg) should be integers.
     * 3. If the above does not match, the process will exit with 0 code.
     *
     * @param args
     */
    private void validateArgs(String[] args) {
        if (args.length != 2 && args.length != 4) {
            logger.log(Level.SEVERE, "4 arguments for Initiator & 2 for receiver are required to start the program.");
            logger.log(Level.SEVERE, "1st Argument: Type of player (receiver / initiator).");
            logger.log(Level.SEVERE, "2nd Argument: Port at which receiver should be started.");
            logger.log(Level.SEVERE, "Next two parameters are for initiator. Not required for receiver and will not be used if provided for the same.");
            logger.log(Level.SEVERE, "3rd Argument: Host at which receiver started.");
            logger.log(Level.SEVERE, "4th Argument: Max number of messages to be sent and received.");
            System.exit(0);
        }
        action = args[0];
        if (!INITIATOR_IDENTIFIER.equals(action) && !RECEIVER_IDENTIFIER.equals(action)) {
            logger.log(Level.SEVERE, "Allowed 1st Argument: Type of player (initiator / receiver)");
            System.exit(0);
        }
        logger.info("Type of player to be started: [" + action + "]");
        try {
            port = Integer.valueOf(args[1]);
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Allowed 2nd Argument: Port at which receiver should be started. Should be an integer.");
            System.exit(0);
        }
        if (args.length > 2 && INITIATOR_IDENTIFIER.equals(action)) {
            receiverHost = args[2];
            try {
                maxMessages = Integer.valueOf(args[3]);
                logger.info("Maximum number of message to be sent: [" + maxMessages + "]");
            } catch (NumberFormatException e) {
                logger.log(Level.SEVERE, "Allowed 4th Argument: Max number of messages to be sent and received. Should be an integer");
                System.exit(0);
            }
        }
    }

    /**
     * Create an instance of {@link MessageHandler}
     *
     * @param socket
     * @return -   {@link MessageHandler} with input write and output scanner.
     * @throws IOException -   thrown if there is any exception while getting output or input stream.
     */
    private MessageHandler createMessageHandler(Socket socket) throws IOException {
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        readScanner = new Scanner(socket.getInputStream());
        return new MessageHandler(writer, readScanner);
    }

    /**
     * Closes the input scanner, output write and socket.
     */
    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                readScanner.close();
                writer.close();
                socket.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception while closing connections.", e);
            }
        }));
    }

    private Socket initializeSimpleSocket(final String receiverHost, final int port) throws IOException {
        logger.info("Simple socket initialized on [" + port + "] and connected to [" + receiverHost + "].");
        return new Socket(receiverHost, port);
    }

    private Socket initializeServerSocket(final int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        logger.info("Receiver socket start on [" + port + "]. Waiting for initiator to connect.");
        return serverSocket.accept();
    }

}
