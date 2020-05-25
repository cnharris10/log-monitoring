package Shared;

import Models.StatsPipelineGroupedRecord;
import Models.StatsPipelineRecord;

import java.util.concurrent.ArrayBlockingQueue;

public class SharedResources {

    private static SharedResources instance = null;
    final public ArrayBlockingQueue<StatsPipelineRecord> statsProcessingQueue;
    final public ArrayBlockingQueue<StatsPipelineGroupedRecord> statsMonitoringQueue;
    final public ArrayBlockingQueue<Integer> rateProcessingQueue;
    final public ConcurrentSlidingWindow rateWindow;
    final public Integer threadSleepCount = 10;

    private Integer clock;
    private Boolean collectorIdle;
    private Integer capacity = 1000;

    private SharedResources(){
        this.statsProcessingQueue = new ArrayBlockingQueue<>(capacity);
        this.rateProcessingQueue = new ArrayBlockingQueue<>(capacity);
        this.statsMonitoringQueue = new ArrayBlockingQueue<>(capacity);
        this.rateWindow = new ConcurrentSlidingWindow();
    }

    public static SharedResources instance() {
        if(instance == null) {
            instance = new SharedResources();
        }
        return instance;
    }

    public synchronized void setProcessingTime(Integer clock) {
        this.clock = clock;
    }

    public synchronized Integer getClockTime() {
        return clock;
    }

    public synchronized Integer getRateWindowCount() { return this.rateWindow.size(); }

    public void setIdle(Boolean status) {
        collectorIdle = status;
    }

    public Boolean isCollectorIdle() {
        return collectorIdle;
    }

}
