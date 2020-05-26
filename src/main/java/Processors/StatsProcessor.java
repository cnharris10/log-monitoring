package Processors;

import Models.StatsPipelineGroupedRecord;
import Models.StatsPipelineRecord;
import Shared.SharedResources;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class StatsProcessor extends Processor {

    protected ArrayBlockingQueue<StatsPipelineGroupedRecord> monitoringQueue;
    protected TreeSet<Integer> sentGroupDates;
    protected final Integer intervalLength;
    protected final Integer processingWindow;
    protected HashMap<Integer, List<StatsPipelineRecord>> map;

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

    // For each received StatsPipelineRecord, build interval groups and send to stats monitor
    public void execute() {
        StatsPipelineRecord line = receive();
        Integer clock = SharedResources.instance().getClockTime();
        if(line != null) {
            this.bucketLogLineIntervalByFlooredStart(line);
        }
        this.sendGroupings(clock);
    }

    public StatsPipelineRecord receive() {
        return (StatsPipelineRecord)this.processingQueue.poll();
    }

    public void send(StatsPipelineGroupedRecord group) {
        this.monitoringQueue.offer(group);
    }

    // Bucket every line item by floored (date % 10) date
    // Set clock time to log line that was just processed
    public void bucketLogLineIntervalByFlooredStart(StatsPipelineRecord line) {
        Integer date = line.getDate();
        Integer key = line.getIntervalDate(intervalLength);
        map.putIfAbsent(key, new ArrayList<>());
        map.get(key).add(line);
        SharedResources.instance().setClockTime(date);
    }

    // At most-once send, interval groups to stats monitor
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
