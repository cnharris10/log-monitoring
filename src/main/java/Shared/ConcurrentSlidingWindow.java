package Shared;

import java.util.concurrent.ConcurrentLinkedDeque;

public class ConcurrentSlidingWindow {

    private final ConcurrentLinkedDeque<Integer> window;

    public ConcurrentSlidingWindow() {
        this.window = new ConcurrentLinkedDeque<>();
    }

    public void expand(Integer obj) {
        this.window.addLast(obj);
    }

    public void compress(Integer windowLength) {
        Integer first = this.window.peek();
        while (this.shouldCompressWindow(first, windowLength)) {
            this.window.removeFirst();
            first = this.window.peek();
        }
    }

    public Integer size() {
        return this.window.size();
    }

    private Boolean shouldCompressWindow(Integer metric, Integer threshold) {
        return metric != null && (metric < threshold);
    }
}
