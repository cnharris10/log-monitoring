package Pipelines;

import Models.LogRecord;

import java.util.concurrent.ArrayBlockingQueue;

public class RatePipeline extends Pipeline {

    public RatePipeline(ArrayBlockingQueue<Integer> queue) {
        super(queue);
    }

    public void ingest(LogRecord line) {
        this.queue.offer(line.getDate());
    }

}
