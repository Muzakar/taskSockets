package com.mk.task.transport;

import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

/**
 * Responsibilities:
 * 1. Write the messages into the output write
 * 2. Read messages from input scanner.
 */
public class MessageHandler {

    private final Writer writer;
    private final Scanner readScanner;

    public MessageHandler(final Writer writer, final Scanner readScanner) {
        this.writer = writer;
        this.readScanner = readScanner;
    }

    public void write(final String message) throws IOException {
        writer.write(message + "\n");
        writer.flush();
    }

    public String read(){
        if(readScanner.hasNextLine()) {
            return readScanner.nextLine();
        }
        return "Unknown";
    }

}
