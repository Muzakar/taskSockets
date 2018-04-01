package com.mk.task.transport;

import org.junit.Test;

import java.io.*;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link MessageHandler}
 */
public class MessageHandlerTest {

    @Test
    public void WriteTest() throws IOException {
        StringWriter writer = new StringWriter();
        MessageHandler messageHandler = new MessageHandler(writer, null);
        messageHandler.write("Test message");
        assertEquals("Test message\n", writer.toString());
    }

    @Test(expected = IOException.class)
    public void ExceptionInWriterTest() throws IOException {
        Writer writer = mock(Writer.class);
        doThrow(new IOException()).when(writer).write(anyString());
        MessageHandler messageHandler = new MessageHandler(writer, null);
        messageHandler.write("Test message");
    }

    @Test
    public void ReadTest() {
        String input = "Test scanner message";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(in);
        MessageHandler messageHandler = new MessageHandler(null, scanner);
        assertEquals("Test scanner message", messageHandler.read());
    }

    @Test
    public void ReadWithNoMessageTest() {
        String input = "Test scanner message";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(in);
        scanner.nextLine();
        MessageHandler messageHandler = new MessageHandler(null, scanner);
        assertEquals("Unknown", messageHandler.read());
    }
}