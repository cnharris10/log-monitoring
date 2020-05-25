package Pipelines;

import Models.LogRecord;
import Models.StatsPipelineRecord;

import java.util.concurrent.ArrayBlockingQueue;

public class StatsPipeline extends Pipeline {

    public StatsPipeline(ArrayBlockingQueue<StatsPipelineRecord> queue) {
        super(queue);
    }

    @Override public void ingest(LogRecord line) {
        this.queue.offer(line.statsData());
    }

}