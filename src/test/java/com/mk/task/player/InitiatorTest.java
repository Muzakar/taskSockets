package com.mk.task.player;

import com.mk.task.Application;
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

public class InitiatorTest {

    private MessageHandler messageHandler;
    private List<String> logList;

    @Before
    public void setUp() throws Exception {
        messageHandler = mock(MessageHandler.class);
        logList = new ArrayList<>();
        Handler handler = new LogHandler(logList);
        Logger.getLogger(Initiator.class.getName()).addHandler(handler);
    }

    @Test
    public void startWith1MessageTest() throws IOException {
        doReturn(MESSAGE + "1").when(messageHandler).read();
        new Initiator(messageHandler, 1).start();
        verify(messageHandler, times(1)).write(POISON_PILL);
        verify(messageHandler, times(1)).read();
        verify(messageHandler, times(1)).write(MESSAGE);
        List<String> nonInfoLogs = logList.stream().filter(log -> !log.contains(Level.INFO.getName())).collect(Collectors.toList());
        assertEquals(0, nonInfoLogs.size());
    }

    @Test
    public void startWith3MessagesTest() throws IOException {
        doReturn(MESSAGE + "1", MESSAGE + "2", MESSAGE + "3").when(messageHandler).read();
        new Initiator(messageHandler, 3).start();
        verify(messageHandler, times(1)).write(POISON_PILL);
        verify(messageHandler, times(3)).read();
        verify(messageHandler, times(3)).write(MESSAGE);
        List<String> nonInfoLogs = logList.stream().filter(log -> !log.contains(Level.INFO.getName())).collect(Collectors.toList());
        assertEquals(0, nonInfoLogs.size());
    }

    @Test
    public void startWith1UnknownMessagesTest() throws IOException {
        doReturn(MESSAGE + "1", "Unknown", MESSAGE + "3").when(messageHandler).read();
        new Initiator(messageHandler, 3).start();
        verify(messageHandler, times(1)).write(POISON_PILL);
        verify(messageHandler, times(3)).read();
        verify(messageHandler, times(3)).write(MESSAGE);
        List<String> nonInfoLogs = logList.stream().filter(log -> !log.contains(Level.INFO.getName())).collect(Collectors.toList());
        assertEquals(1, nonInfoLogs.size());
        assertEquals("WARNING - Did not find the proper count in the message: [Unknown]", nonInfoLogs.get(0));
    }

    @Test
    public void exceptionWhileWritingMessage() throws IOException {
        doThrow(new IOException()).when(messageHandler).write(Application.MESSAGE);
        new Initiator(messageHandler, 10).start();
        verify(messageHandler, times(0)).read();
        List<String> nonInfoLogs = logList.stream().filter(log -> !log.contains(Level.INFO.getName())).collect(Collectors.toList());
        assertEquals(1, nonInfoLogs.size());
        assertEquals("SEVERE - Exception while writing the message", nonInfoLogs.get(0));
    }

    @Test
    public void exceptionWhileWritingPoisonPillTest() throws IOException {
        doReturn(MESSAGE + "1").when(messageHandler).read();
        doThrow(new IOException()).when(messageHandler).write(POISON_PILL);
        new Initiator(messageHandler, 1).start();
        verify(messageHandler, times(1)).write(POISON_PILL);
        verify(messageHandler, times(1)).read();
        verify(messageHandler, times(1)).write(MESSAGE);
        List<String> nonInfoLogs = logList.stream().filter(log -> !log.contains(Level.INFO.getName())).collect(Collectors.toList());
        assertEquals(1, nonInfoLogs.size());
        assertEquals("SEVERE - Exception while sending poison pill to stop receiver.", nonInfoLogs.get(0));
    }

}