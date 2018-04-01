package com.mk.task.player;

import com.mk.task.Application;
import com.mk.task.transport.MessageHandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsibilities:
 * 1. Reads the messages from the socket, adds the message count.
 * 2. Then, sends it back to the socket for {@link Initiator}
 * 3. The process will stop if it receives {@link Application#POISON_PILL}.
 * 4. The process will also stop if there is any exception while writing data into socket.
 *
 */
public class Receiver implements IPlayer {

    private final static Logger logger = Logger.getLogger(Receiver.class.getName());

    private final MessageHandler messageHandler;

    public Receiver(final MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void start() {
        int count = 1;
        while (true) {
            String message = messageHandler.read();
            logger.info("Received message: [" + message + "]");
            if (Application.POISON_PILL.equals(message)) {
                logger.info("Received poison pill. So, exiting.");
                break;
            }
            try {
                String sendingMessage = message + " " + count;
                messageHandler.write(sendingMessage);
                logger.info("Sent message: [" + sendingMessage + "]");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception while sending message: count: [" + count + "]", e);
                break;
            }
            count++;
        }
    }

}
