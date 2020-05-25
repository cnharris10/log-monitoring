package Monitoring;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import Models.StatsPipelineGroupedRecord;
import Models.StatsPipelineRecord;

import Shared.SharedResources;

public class StatsMonitor extends StreamingMonitor {

    protected final ArrayBlockingQueue<StatsPipelineGroupedRecord> monitoringQueue;
    protected final Integer intervalLength;

    public StatsMonitor(ArrayBlockingQueue<StatsPipelineGroupedRecord> monitoringQueue, Integer threshold, Integer intervalLength) {
        this.monitoringQueue = monitoringQueue;
        this.intervalLength = intervalLength;
    }

    public void run() {
        try {
            Thread.sleep(SharedResources.instance().threadSleepCount);
            while (true) {
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }
                this.processGroupedIntervals();
            }
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    // For all hash keys (floored interval timestamp) that are greater than
    // remove from hash and print to console.
    // Console output will chase processors and only disregard threshold when
    // collector is idle
    protected void processGroupedIntervals() {
        StatsPipelineGroupedRecord group = this.monitoringQueue.poll();
        if(group != null) {
            this.buildStats(group.getDate(), new ArrayList<>(group.getRecords()));
        }
    }

    // Present top sections and debug status in console
    // Example:
    //    ------------------------------------------------------
    //    Top sections for interval: 1549574210 - 1549574220
    //    Section: /api - Count: 242
    //    Section: /report - Count: 30
    //    Successes: 226 (0.8308823529411765%)
    //    Failures: 46 (0.16911764705882348%)
    //    Total Requests: 272
    //    Total Bytes Processed: 334298
    //    ------------------------------------------------------
    protected void buildStats(Integer key, List<StatsPipelineRecord> data) {
        List<String> stats = new ArrayList<>();
        stats.add("\n------------------------------------------------------");
        stats.add("Top sections for interval: "+key+" - "+(key + intervalLength));

        topSections(key, data).forEach(x -> stats.add("Section: "+x.getKey()+" - Count: "+x.getValue()));

        long successes = totalSuccesses(data);
        long requests = data.size();
        long failures = requests - successes;
        double successPercentage = (double)successes / requests;
        double failurePercentage = 1.0 - successPercentage;

        stats.add("Successes: "+successes+" ("+successPercentage+"%)");
        stats.add("Failures: "+failures+" ("+failurePercentage+"%)");
        stats.add("Total Requests: "+requests);
        stats.add("Total Bytes Processed: "+totalBytes(data));
        stats.add("------------------------------------------------------");
        present(String.join("\n", stats));
    }

    // Calculate top N sections by section name and hit count for given interval
    protected List<Map.Entry<String, Long>> topSections(Integer key, List<StatsPipelineRecord> data) {
        Map<String, Long> map = data.stream()
                .collect(Collectors.groupingBy(w -> w.getPathSection(), Collectors.counting()));
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    // Count all requests with status 2XX
    protected long totalSuccesses(List<StatsPipelineRecord> data) {
        return data.stream().filter(line -> line.getStatus() >= 200 && line.getStatus() < 300).count();
    }

    // Count all bytes
    protected long totalBytes(List<StatsPipelineRecord> data) {
        return data.stream().map(x -> x.getBytes()).mapToLong(x -> x.longValue()).sum();
    }
}