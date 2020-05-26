package Views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatsView extends View {

    final private Long totalRequests;
    final private Integer intervalStart;
    final private Integer intervalEnd;
    private List<Map.Entry<String, Long>> topSections;
    private Long successTotal;
    private Long failuresTotal;
    private Double successesPercentage;
    private Double failuresPercentage;
    private Long bytesTotal;

    public static StatsView build(Long totalRequests, Integer intervalStart, Integer intervalEnd) {
        return new StatsView(totalRequests, intervalStart, intervalEnd);
    }

    private StatsView(Long totalRequests, Integer intervalStart, Integer intervalEnd) {
        this.totalRequests = totalRequests;
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
    }

    public void addTopSections(List<Map.Entry<String, Long>> topSections) {
        this.topSections = topSections;
    }

    public void addSuccessTotals(Long successes) {
        this.successTotal = successes;
        this.successesPercentage = (double)successes / totalRequests;
    }

    public void addFailureTotals(Long failures) {
        this.failuresTotal = failures;
        this.failuresPercentage = 1.0 - successesPercentage;
    }

    public void addByteTotals(Long bytes) {
        this.bytesTotal = bytes;
    }

    public <T> void present(T data) {
        log.info(data);
    }

    // Present top sections and debug status in console
    // Example:
    //    ------------------------------------------------------
    //    Top sections for interval: 1549574210 - 1549574220
    //    Section: /api - Count: 242
    //    Section: /report - Count: 30
    //    Successes: 226 (0.8308823529411765%)
    //    Failures: 46 (0.16911764705882348%)
    //    Total Requests: 272
    //    Total Bytes Processed: 334298
    //    ------------------------------------------------------
    public void render() {
        List<String> output = new ArrayList<>();
        output.add("\n------------------------------------------------------");
        output.add("Top sections for interval: "+intervalStart+" - "+intervalEnd);
        topSections.forEach(section -> output.add("Section: "+section.getKey()+" - " +section.getValue()));
        output.add("Successes: "+successTotal+" ("+successesPercentage+"%)");
        output.add("Failures: "+failuresTotal+" ("+failuresPercentage+"%)");
        output.add("Total Requests: "+totalRequests);
        output.add("Total Bytes Processed: "+bytesTotal);
        output.add("------------------------------------------------------");
        present(String.join("\n", output));
    }

}
