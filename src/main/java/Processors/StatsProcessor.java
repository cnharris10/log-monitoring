package Processors;

import Models.StatsPipelineGroupedRecord;
import Models.StatsPipelineRecord;
import Shared.SharedResources;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class StatsProcessor extends Processor {

    protected ArrayBlockingQueue<StatsPipelineGroupedRecord> monitoringQueue;
    protected LinkedHashMap<Integer, List<StatsPipelineRecord>> map;
    protected final Integer intervalLength;
    protected final Integer processingWindowInSeconds;
    protected final Integer processingTimeoutInSeconds;
    protected Long nullStartTime;
    protected Boolean idle = false;

    public StatsProcessor(ArrayBlockingQueue<StatsPipelineRecord> processingQueue,
                          ArrayBlockingQueue<StatsPipelineGroupedRecord> monitoringQueue,
                          LinkedHashMap<Integer, List<StatsPipelineRecord>> map,
                          Integer intervalLength,
                          Integer processingWindowInSeconds,
                          Integer processingTimeoutInSeconds) {
        super(processingQueue);
        this.monitoringQueue = monitoringQueue;
        this.map = map;
        this.intervalLength = intervalLength;
        this.processingWindowInSeconds = processingWindowInSeconds;
        this.processingTimeoutInSeconds = processingTimeoutInSeconds;
    }

    // For each received StatsPipelineRecord, build interval groups and send to stats monitor
    // Log how long the processingQueue does not poll and item and if the processingQueue does
    // not poll an item after processingTimeoutInSeconds times, declare the processor as "idle"
    public void execute() {
        StatsPipelineRecord line = receive();
        Integer clock = SharedResources.instance().getClockTime();
        if(line != null) {
            this.bucketLogLineIntervalByFlooredStart(line);
            nullStartTime = null;
            idle = false;
        } else if(nullStartTime == null) {
            nullStartTime = Instant.now().getEpochSecond();
        }

        setProcessingIdleAfterProcessingTimeoutInSeconds();
        this.sendGroupings(clock);
    }

    public StatsPipelineRecord receive() {
        return (StatsPipelineRecord)this.processingQueue.poll();
    }

    public void send(StatsPipelineGroupedRecord group) {
        this.monitoringQueue.offer(group);
    }

    // If nullStartTime is processingTimeoutInSeconds from now, declare idle
    public void setProcessingIdleAfterProcessingTimeoutInSeconds() {
        if(nullStartTime == null || idle == true) {
            return;
        }
        long now = Instant.now().getEpochSecond();
        if ((now - nullStartTime) > processingTimeoutInSeconds){
            SharedResources.instance().logger.log("StatsProcessor is idle after "+processingTimeoutInSeconds+" seconds");
            idle = true;
        }
    }

    // Bucket every line item by floored (date % 10) date
    // Set clock time to log line that was just processed
    public void bucketLogLineIntervalByFlooredStart(StatsPipelineRecord line) {
        Integer date = line.getDate();
        Integer key = line.getIntervalDate(intervalLength);
        synchronized (map) {
            map.putIfAbsent(key, new ArrayList<>());
            map.get(key).add(line);
        }
        SharedResources.instance().setClockTime(date);
    }

    // At most-once send, interval groups to stats monitor
    public void sendGroupings(Integer clock) {
        synchronized (map) {
            Iterator itr = map.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<Integer, List<StatsPipelineRecord>> entry = (Map.Entry<Integer, List<StatsPipelineRecord>>) itr.next();
                Integer key = entry.getKey();
                if (shouldSendGroupings(clock, key)) {
                    StatsPipelineGroupedRecord group = StatsPipelineGroupedRecord.build(key, entry.getValue());
                    send(group);
                    itr.remove();
                }
            }
        }
    }

    // Send group interval to StatsMonitor at most once.
    //  - clock is not null (Must have processed something upstream)
    //  - (clock - group time) > processingWindow
    //    - Examples:
    //      - True: 1549574437 (processing time) - 1549574300 (group time) > 30 (processingWindow)
    //      - False: 1549574337 (processing time) - 1549574300 (group time) > 30 (processingWindow)
    //  - Processor has declared itself idle (not sending more logs)
    public Boolean shouldSendGroupings(Integer clock, Integer key) {
        if(clock == null) {
            return false;
        }
        return ((clock - key) > processingWindowInSeconds) || idle;
    }
}
