package Processors;

import Collectors.Sendable;
import Shared.SharedResources;

import java.util.concurrent.ArrayBlockingQueue;

public class Processor<T> implements Runnable, Processable, Sendable {

    final protected ArrayBlockingQueue<T> processingQueue;

    public Processor(ArrayBlockingQueue<T> queue) {
        this.processingQueue = queue;
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

    @Override
    public void execute() { }

    @Override
    public <U> void send(U data) { }

    @Override
    public <V> V receive() {
        V data = null;
        return (V)data;
    }

}
