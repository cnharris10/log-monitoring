package Pipelines;

import Models.LogRecord;

import java.util.concurrent.ArrayBlockingQueue;

public class Pipeline<T> implements Pipelineable {

    protected final ArrayBlockingQueue<T> queue;

    public Pipeline(ArrayBlockingQueue<T> queue) {
        this.queue = queue;
    }

    public void ingest(LogRecord line) { }

}
