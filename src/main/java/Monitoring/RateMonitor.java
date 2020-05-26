package Monitoring;

import Shared.SharedResources;
import Views.RateView;

public class RateMonitor extends StreamingMonitor {

    final protected Integer averageMessageRate;
    final protected Integer windowLengthInSeconds;
    final protected SharedResources sharedResources;
    protected Boolean flag;

    public RateMonitor(Integer averageMessageRate, Integer windowLengthInSeconds) {
        this.averageMessageRate = averageMessageRate;
        this.windowLengthInSeconds = windowLengthInSeconds;
        this.flag = false;
        this.sharedResources = SharedResources.instance();
    }

    public void analyze() {
        Integer clock = getCurrentClockTime();
        if(clock == null) {
            return;
        }

        processRate(clock, getCurrentRateWindowCount());
        compress(clock - windowLengthInSeconds);
    }

    public void processRate(Integer clock, Integer windowCount) {
        if(shouldAlert(windowCount)) {
            RateView.alert(windowCount, clock);
            this.toggleFlag();
        } else if(shouldRecover(windowCount)) {
            RateView.recover(windowCount, clock);
            this.toggleFlag();
        }
    }

    protected Integer getCurrentRateWindowCount() {
        return SharedResources.instance().getRateWindowCount();
    }

    protected Integer getCurrentClockTime() {
        return SharedResources.instance().getClockTime();
    }

    protected void compress(Integer value) {
        sharedResources.rateWindow.compress(value);
    }

    protected Boolean shouldAlert(Integer windowCount) {
        return !this.flag && windowCount > averageMessageRate;
    }

    protected Boolean shouldRecover(Integer windowCount) {
        return this.flag && windowCount <= averageMessageRate;
    }

    protected void toggleFlag() {
        this.flag = !this.flag;
    }

}