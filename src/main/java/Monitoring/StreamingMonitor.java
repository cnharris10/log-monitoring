package Monitoring;


import Shared.SharedResources;

public class StreamingMonitor implements Monitorable, Runnable {

    public void run() {
        try {
            Thread.sleep(SharedResources.instance().threadSleepCount);
            while (true) {
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }
                this.analyze();
            }
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void analyze() { }
}
