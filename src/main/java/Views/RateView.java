package Views;

import java.util.ArrayList;
import java.util.List;

public class RateView extends View {

    final private Integer windowCount;
    final private Integer clock;
    private String message;

    public static RateView build(Integer windowCount, Integer clock) {
        return new RateView(windowCount, clock);
    }

    public static void alert(Integer windowCount, Integer clock) {
        build(windowCount, clock).alert().render();
    }

    public static void recover(Integer windowCount, Integer clock) {
        build(windowCount, clock).recover().render();
    }

    private RateView(Integer windowCount, Integer clock) {
        this.windowCount = windowCount;
        this.clock = clock;
    }

    public void render() {
        List<String> output = new ArrayList<>();
        output.add("\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        output.add(message);
        output.add("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        present(String.join("\n", output));
    }

    public <T> void present(T output) {
        log.info(output);
    }

    private RateView alert() {
        this.message = "High traffic generated an alert - hits = "+windowCount+" at: "+clock;
        return this;
    }

    private RateView recover() {
        this.message = "Traffic recovered from alert - hits "+windowCount+" at: "+clock;
        return this;
    }

}
