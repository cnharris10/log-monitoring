package Processors;

import Models.StatsPipelineGroupedRecord;
import Models.StatsPipelineRecord;
import Shared.SharedResources;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class StatsProcessor extends Processor {

    private ArrayBlockingQueue<StatsPipelineGroupedRecord> monitoringQueue;
    private TreeSet<Integer> sentGroupDates;
    private final Integer intervalLength;
    private final Integer processingWindow;
    private HashMap<Integer, List<StatsPipelineRecord>> map;

    public StatsProcessor(ArrayBlockingQueue<StatsPipelineRecord> processingQueue,
                          ArrayBlockingQueue<StatsPipelineGroupedRecord> monitoringQueue,
                          Integer intervalLength,
                          Integer processingWindow) {
        super(processingQueue);
        this.monitoringQueue = monitoringQueue;
        this.sentGroupDates = new TreeSet<>();
        this.intervalLength = intervalLength;
        this.processingWindow = processingWindow;
        this.map = new HashMap<>();
    }

    public void execute() {
        StatsPipelineRecord line = receive();
        Integer clock = SharedResources.instance().getClockTime();
        if(line != null) {
            this.bucketLogLineIntervalByFlooredStart(line);
        }
        this.sendGroupings(clock);
    }

    public void run() {
        try {
            Thread.sleep(SharedResources.instance().threadSleepCount);
            while (true) {
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }
                this.execute();
            }
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    // Bucket every line item by floored (date % 10) date
    // Synchronize map access to be thread-safe
    public void bucketLogLineIntervalByFlooredStart(StatsPipelineRecord line) {
        Integer date = line.getDate();
        Integer key = date - (date % intervalLength);
        map.putIfAbsent(key, new ArrayList<>());
        map.get(key).add(line);
        SharedResources.instance().setProcessingTime(date);
    }

    public void sendGroupings(Integer clock) {
        for (Map.Entry<Integer, List<StatsPipelineRecord>> integerListEntry : map.entrySet()) {
            Integer key = integerListEntry.getKey();
            if(shouldSendGroupings(clock, key)) {
                StatsPipelineGroupedRecord group = StatsPipelineGroupedRecord.build(key, integerListEntry.getValue());
                send(group);
                this.sentGroupDates.add(key);
            }
        }
    }

    public StatsPipelineRecord receive() {
        return (StatsPipelineRecord)this.processingQueue.poll();
    }

    public void send(StatsPipelineGroupedRecord group) {
        this.monitoringQueue.offer(group);
    }

    // Send group interval to StatsMonitor at most once.
    //  - clock is not null (Must have processed something upstream)
    //  - (clock - group time) > processingWindow
    //    - Examples:
    //      - True: 1549574437 (processing time) - 1549574300 (group time) > 60 (processingWindow)
    //      - False: 1549574337 (processing time) - 1549574300 (group time) > 60 (processingWindow)
    //  - Collector has declared itself idle (not sending more logs)
    public Boolean shouldSendGroupings(Integer clock, Integer key) {
        if(clock == null) {
            return false;
        }
        return ((clock - key > processingWindow) ||
                    SharedResources.instance().isCollectorIdle())
                    && !sentGroupDates.contains(key);
    }
}
