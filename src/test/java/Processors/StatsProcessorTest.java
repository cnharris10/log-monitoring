package Processors;

import Models.LogRecord;
import Models.StatsPipelineGroupedRecord;
import Models.StatsPipelineRecord;
import Shared.SharedResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class StatsProcessorTest {

    Integer capacity = 1;
    ArrayBlockingQueue<StatsPipelineRecord> processingQueue;
    ArrayBlockingQueue<StatsPipelineGroupedRecord> monitoringQueue;
    StatsProcessor processor;
//
//    @BeforeEach
//    private void init() {
//        processingQueue = new ArrayBlockingQueue<>(capacity);
//        monitoringQueue = new ArrayBlockingQueue<>(capacity);
//        processor = new StatsProcessor(processingQueue, monitoringQueue, 10, 60);
//    }
//
//    @Test
//    void testConstructor() {
//        assertEquals(processor.processingQueue, processingQueue);
//        assertEquals(processor.monitoringQueue, monitoringQueue);
//        assertEquals(processor.intervalLength, 10);
//        assertEquals(processor.processingWindow, 60);
//        assertEquals(processor.sentGroupDates.getClass(), new TreeSet<Integer>().getClass());
//        assertEquals(processor.map.getClass(), new HashMap<Integer, List<StatsPipelineRecord>>().getClass());
//    }
//
//    public StatsPipelineRecord receive() {
//        return (StatsPipelineRecord)this.processingQueue.poll();
//    }
//
//    public void send(StatsPipelineGroupedRecord group) {
//        this.monitoringQueue.offer(group);
//    }
//
//    @Test
//    void testReceive() {
//        String line = "10.0.0.1,-,apache,1549574334,GET /api/user HTTP/1.0,200,1194";
//        LogRecord record = LogRecord.parse(line);
//        this.processingQueue.offer(record.statsData());
//        processor.receive();
//        assertEquals(this.processingQueue.size(), 0);
//    }
//
//    @Test
//    void testSend() {
//        String line = "10.0.0.1,-,apache,1549574334,GET /api/user HTTP/1.0,200,1194";
//        LogRecord record = LogRecord.parse(line);
//        List<StatsPipelineRecord> list = new ArrayList<>();
//        list.add(record.statsData());
//        StatsPipelineGroupedRecord groupedRecord = StatsPipelineGroupedRecord.build(1549574334, list);
//        processor.send(groupedRecord);
//        assertEquals(this.monitoringQueue.size(), 1);
//    }
//
//    @Test
//    void testExecute() {
//        Integer clockTime = 123456789;
//        StatsProcessor processorSpy = Mockito.spy(processor);
//        String line = "10.0.0.1,-,apache,1549574334,GET /api/user HTTP/1.0,200,1194";
//        LogRecord record = LogRecord.parse(line);
//        SharedResources.instance().setClockTime(clockTime);
//        SharedResources.instance().setIdle(false);
//        this.processingQueue.offer(record.statsData());
//        processorSpy.execute();
//        verify(processorSpy, times(1)).bucketLogLineIntervalByFlooredStart(any(StatsPipelineRecord.class));
//        verify(processorSpy, times(1)).sendGroupings(clockTime);
//    }
//
//    @Test
//    void testBucketLogLineIntervalByFlooredStartWithABlankMap() {
//        String line = "10.0.0.1,-,apache,1549574334,GET /api/user HTTP/1.0,200,1194";
//        LogRecord record = LogRecord.parse(line);
//        processor.bucketLogLineIntervalByFlooredStart(record.statsData());
//        assertEquals(processor.map.get(1549574330).size(), 1);
//        assertNotNull(SharedResources.instance().getClockTime());
//    }
//
//    @Test
//    void testBucketLogLineIntervalByFlooredStartWithANonBlankMap() {
//        String line = "10.0.0.1,-,apache,1549574334,GET /api/user HTTP/1.0,200,1194";
//        String line2 = "10.0.0.1,-,apache,1549574335,GET /api/user HTTP/1.0,200,1194";
//        LogRecord record = LogRecord.parse(line);
//        LogRecord record2 = LogRecord.parse(line2);
//        processor.bucketLogLineIntervalByFlooredStart(record.statsData());
//        processor.bucketLogLineIntervalByFlooredStart(record2.statsData());
//        assertEquals(processor.map.get(1549574330).size(), 2);
//        assertNotNull(SharedResources.instance().getClockTime());
//    }
//
//    @Test
//    void testSendGroupingsWithAValidKey() {
//        Integer clockTime = 1234565789;
//        Integer key = 123456578;
//        String line = "10.0.0.1,-,apache,1549574334,GET /api/user HTTP/1.0,200,1194";
//        LogRecord record = LogRecord.parse(line);
//        StatsProcessor processorSpy = Mockito.spy(processor);
//        List<StatsPipelineRecord> list = new ArrayList<>();
//        list.add(record.statsData());
//        processorSpy.map.put(key, list);
//        processorSpy.sendGroupings(clockTime);
//        when(processorSpy.shouldSendGroupings(clockTime, key)).thenReturn(true);
//        verify(processorSpy, times(1)).send(any(StatsPipelineGroupedRecord.class));
//        assertEquals(processorSpy.sentGroupDates.first(), key);
//    }
//
//    @Test
//    void testSendGroupingsWithAnInValidKey() {
//        Integer key = 123456578;
//        String line = "10.0.0.1,-,apache,1549574334,GET /api/user HTTP/1.0,200,1194";
//        LogRecord record = LogRecord.parse(line);
//        StatsProcessor processorSpy = Mockito.spy(processor);
//        List<StatsPipelineRecord> list = new ArrayList<>();
//        list.add(record.statsData());
//        processorSpy.map.put(key, list);
//        processorSpy.sendGroupings(null);
//        verify(processorSpy, never()).send(any(StatsPipelineGroupedRecord.class));
//        assertEquals(processorSpy.sentGroupDates.size(), 0);
//    }
//
//    @Test
//    void shouldSentGroupsAsTrueWhenLargerThanProcessingWindow() {
//        SharedResources.instance().setIdle(false);
//        StatsProcessor processorSpy = Mockito.spy(processor);
//        assertEquals(processorSpy.shouldSendGroupings(999999999, 123456578), true);
//    }
//
//    @Test
//    void shouldSentGroupsAsTrueWhenCollectorIsIdle() {
//        SharedResources.instance().setIdle(true);
//        StatsProcessor processorSpy = Mockito.spy(processor);
//        assertEquals(processorSpy.shouldSendGroupings(999999999, 999999999), true);
//    }
//
//    @Test
//    void shouldSentGroupsAsFalseWhenClockIsNull() {
//        SharedResources.instance().setIdle(false);
//        StatsProcessor processorSpy = Mockito.spy(processor);
//        assertEquals(processorSpy.shouldSendGroupings(null, 999999999), false);
//    }
//
//    @Test
//    void shouldSentGroupsAsFalseWhenGroupingAlreadySent() {
//        SharedResources.instance().setIdle(false);
//        StatsProcessor processorSpy = Mockito.spy(processor);
//        processorSpy.sentGroupDates.add(123456789);
//        assertEquals(processorSpy.shouldSendGroupings(999999999, 123456789), false);
//    }

}
