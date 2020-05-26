package Processors;

import Shared.SharedResources;

import java.util.concurrent.ArrayBlockingQueue;


public class RateProcessor extends Processor {

    public RateProcessor(ArrayBlockingQueue<Integer> queue) {
        super(queue);
    }

    public void execute() {
        Integer date = receive();
        if(date != null) {
            send(date);
        }
    }

    public Integer receive() {
        return (Integer)this.processingQueue.poll();
    }

    public void send(Integer date) {
        SharedResources.instance().rateWindow.expand(date);
    }
}