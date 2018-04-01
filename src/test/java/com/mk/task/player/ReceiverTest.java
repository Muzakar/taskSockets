package com.mk.task.player;

import com.mk.task.transport.MessageHandler;
import com.mk.task.util.LogHandler;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.mk.task.Application.MESSAGE;
import static com.mk.task.Application.POISON_PILL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link Receiver}
 */
public class ReceiverTest {

    private Receiver receiver;
    private MessageHandler messageHandler;
    private List<String> logList;

    @Before
    public void setUp() throws Exception {
        messageHandler = mock(MessageHandler.class);
        receiver = new Receiver(messageHandler);
        logList = new ArrayList<>();
        Handler handler = new LogHandler(logList);
        Logger.getLogger(Receiver.class.getName()).addHandler(handler);
    }

    @Test
    public void startTest() throws IOException {
        doReturn(MESSAGE, MESSAGE, POISON_PILL).when(messageHandler).read();
        receiver.start();
        verify(messageHandler, times(3)).read();
        verify(messageHandler, times(2)).write(anyString());
        List<String> nonInfoLogs = logList.stream().filter(log -> !log.contains(Level.INFO.getName())).collect(Collectors.toList());
        assertEquals(0, nonInfoLogs.size());
    }

    @Test
    public void startWithExceptionWhileWritingTest() throws IOException {
        doThrow(new IOException()).when(messageHandler).write(anyString());
        receiver.start();
        verify(messageHandler, times(1)).read();
        verify(messageHandler, times(1)).write(anyString());
        List<String> nonInfoLogs = logList.stream().filter(log -> !log.contains(Level.INFO.getName())).collect(Collectors.toList());
        assertEquals(1, nonInfoLogs.size());
        assertEquals("SEVERE - Exception while sending message: count: [1]", nonInfoLogs.get(0));
    }
}