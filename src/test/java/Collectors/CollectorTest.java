package Collectors;

import Models.LogRecord;
import Pipelines.RatePipeline;
import Pipelines.StatsPipeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
class CollectorTest {

    private Collector collector;

    @Mock
    Scanner scannerMock;

    String defaultFilename = "/tmp/foo.txt";

    @BeforeEach
    void init() {
        try {
            new File(defaultFilename).createNewFile();
            collector = new Collector(defaultFilename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testConstructor() {
        Collector collectorSpy = Mockito.spy(collector);
        try { when(collectorSpy.buildScanner()).thenReturn(scannerMock); } catch (Exception e) { }
        assertEquals(collectorSpy.filepath, defaultFilename);
        assertEquals(collectorSpy.pipelines.getClass(), ArrayList.class);
        assertNotNull(collectorSpy.scanner);
    }

    @Test
    void testReadFileWithHeaderOnly() {
        try {
            String filename = "/tmp/header_only.txt";
            FileWriter myWriter = new FileWriter(filename);
            myWriter.write("remotehost");
            myWriter.close();
            Collector collectorHeaderOnly = new Collector(filename);
            Collector collectorSpy = Mockito.spy(collectorHeaderOnly);
            try { collectorSpy.read(); } catch (Exception ex) { }
            verify(collectorSpy, times(1)).setIdle(false);
            verify(collectorSpy, times(1)).setIdle(true);
            verify(collectorSpy, never()).send(anyString());
        } catch(Exception ex) { }
    }

    @Test
    void testReadFileWithValidData() {
        try {
            String filename = "/tmp/contents.txt";
            String line = "10.0.0.1,-,apache,1549574334,GET /api/user HTTP/1.0,200,1194";
            FileWriter myWriter = new FileWriter(filename);
            myWriter.write(line);
            myWriter.close();
            Collector collectorHeaderOnly = new Collector(filename);
            Collector collectorSpy = Mockito.spy(collectorHeaderOnly);
            try { collectorSpy.read(); } catch (Exception ex) { }
            verify(collectorSpy, times(1)).setIdle(false);
            verify(collectorSpy, times(1)).send(line);
            verify(collectorSpy, times(1)).setIdle(true);
        } catch(Exception ex) { }
    }

    @Test
    void testIngest() {
        try {
            String filename = "/tmp/contents.txt";
            String line = "10.0.0.1,-,apache,1549574334,GET /api/user HTTP/1.0,200,1194";
            FileWriter myWriter = new FileWriter(filename);
            myWriter.write(line);
            myWriter.close();
            Collector collectorHeaderOnly = new Collector(filename);
            Collector collectorSpy = Mockito.spy(collectorHeaderOnly);
            StatsPipeline statsStream  = new StatsPipeline(new ArrayBlockingQueue<>(1));
            RatePipeline rateStream = new RatePipeline(new ArrayBlockingQueue<>(1));
            StatsPipeline statsStreamSpy = Mockito.spy(statsStream);
            RatePipeline rateStreamSpy = Mockito.spy(rateStream);
            collectorSpy.addStream(statsStreamSpy);
            collectorSpy.addStream(rateStreamSpy);
            collectorSpy.send(line);
            verify(statsStreamSpy, times(1)).ingest(isA(LogRecord.class));
            verify(rateStreamSpy, times(1)).ingest(isA(LogRecord.class));

        } catch(Exception ex) { }
    }
}