package Processors;

import java.util.concurrent.ArrayBlockingQueue;

public class Processor<T> implements Runnable, Processable {

    final protected ArrayBlockingQueue<T> processingQueue;

    public Processor(ArrayBlockingQueue<T> queue) {
        this.processingQueue = queue;
    }

    @Override
    public void execute() { }

    @Override
    public void run() { }

    @Override
    public <U> void send(U data) { }

    @Override
    public <V> V receive() {
        V data = null;
        return (V)data;
    }

}
