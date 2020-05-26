package Monitoring;

import Models.LogRecord;
import Models.StatsPipelineGroupedRecord;
import Models.StatsPipelineRecord;
import Processors.StatsProcessor;
import Shared.ConcurrentSlidingWindow;
import Shared.SharedResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
class StatsMonitorTest {

    private StatsMonitor monitor;
    private SharedResources sharedResources;
    private ArrayBlockingQueue<StatsPipelineGroupedRecord> queue = new ArrayBlockingQueue<>(1);
    private Integer intervalLength = 10;

    @BeforeEach
    void init() {
        monitor = new StatsMonitor(queue, intervalLength);
        sharedResources = SharedResources.instance();
    }

    @Test
    void testConstructor() {
        assertEquals(monitor.monitoringQueue, queue);
        assertEquals(monitor.intervalLength, intervalLength);
    }

    @Test
    void testProcessGroupIntervals() {
        Integer key = 1549574330;
        String line = "10.0.0.1,-,apache,1549574334,GET /api/user HTTP/1.0,200,1194";
        LogRecord record = LogRecord.parse(line);
        StatsMonitor monitorSpy = Mockito.spy(monitor);
        List<StatsPipelineRecord> list = new ArrayList<>();
        list.add(record.statsData());
        StatsPipelineGroupedRecord groupedRecord = StatsPipelineGroupedRecord.build(key, list);
        this.queue.add(groupedRecord);
        monitorSpy.analyze();
        verify(monitorSpy, times(1)).presentView(key, list);
    }
}