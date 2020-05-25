package Monitoring;

import Shared.SharedResources;

public class RateMonitor extends StreamingMonitor {

    protected Integer averageMessageRate;
    protected Boolean flag;
    protected Integer windowLengthInSeconds;
    final protected SharedResources sharedResources;

    public RateMonitor(Integer averageMessageRate, Integer windowLengthInSeconds) {
        this.averageMessageRate = averageMessageRate;
        this.windowLengthInSeconds = windowLengthInSeconds;
        this.flag = false;
        this.sharedResources = SharedResources.instance();
    }

    public void run() {
        try {
            Thread.sleep(SharedResources.instance().threadSleepCount);
            while (true) {
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }
                this.analyzeRate();
            }
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    protected Integer getCurrentRateWindowCount() {
        return SharedResources.instance().getRateWindowCount();
    }

    protected Integer getCurrentClockTime() {
        return SharedResources.instance().getClockTime();
    }

    protected void analyzeRate() {
        Integer clock = getCurrentClockTime();
        if(clock == null) {
            return;
        }

        processRate(clock, getCurrentRateWindowCount());
        compress(clock - windowLengthInSeconds);
    }

    protected void compress(Integer value) {
        sharedResources.rateWindow.compress(value);
    }

    public void processRate(Integer clock, Integer windowCount) {
        if(!this.flag && windowCount > averageMessageRate) {
            this.present("High traffic generated an alert - hits = "+windowCount+" at: "+clock);
            this.toggleFlag();
        } else if(this.flag && windowCount <= averageMessageRate) {
            this.present("Traffic recovered from alert - hits "+windowCount+" at: "+clock);
            this.toggleFlag();
        }
    }

    protected void toggleFlag() {
        this.flag = !this.flag;
    }

    public void present(String message) {
        log.info("\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n"+message+"\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
    }

}