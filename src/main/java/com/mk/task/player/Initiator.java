package com.mk.task.player;

import com.mk.task.Application;
import com.mk.task.transport.MessageHandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mk.task.Application.MESSAGE;
import static com.mk.task.Application.POISON_PILL;

/**
 * {@link IPlayer} which sends messages to {@link Receiver}
 * Responsibilities:
 * 1. Write {@link Application#MESSAGE} to the socket.
 * 2. Receive the message sent by {@link Receiver}
 * 3. If the maximum number of messages are sent and received,
 * {@link Application#POISON_PILL} is sent to Receiver to stop the receiver process.
 * 4. The process will stop if there is any exception while writing messages into the socket.
 */
public class Initiator implements IPlayer {

    private final static Logger logger = Logger.getLogger(Initiator.class.getName());

    private final MessageHandler messageHandler;
    private final int maxMessages;

    public Initiator(final MessageHandler messageHandler, final int maxMessages) {
        this.messageHandler = messageHandler;
        this.maxMessages = maxMessages;
    }

    @Override
    public void start() {
        int count = 1;
        while (true) {
            try {
                messageHandler.write(MESSAGE);
                logger.info("Sent message: [" + MESSAGE + "].");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception while writing the message", e);
                break;
            }
            String receivedMessage = messageHandler.read();
            if (receivedMessage.contains(String.valueOf(count))) {
                logger.info("Received message: [" + receivedMessage + "]");
            } else {
                logger.log(Level.WARNING, "Did not find the proper count in the message: [" + receivedMessage + "]");
            }
            if (sendPoisonIfRequired(maxMessages, count)) break;
            count++;
        }
    }

    private boolean sendPoisonIfRequired(int maxMessages, int count) {
        if (count >= maxMessages) {
            logger.info("Received the message having count: [" + count + "].");
            try {
                messageHandler.write(POISON_PILL);
                logger.info("Position pill is now sent.");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception while sending poison pill to stop receiver.", e);
            }
            return true;
        }
        return false;
    }

}
