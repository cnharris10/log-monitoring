package Monitoring;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import Models.StatsPipelineGroupedRecord;
import Models.StatsPipelineRecord;

import Shared.SharedResources;
import Views.StatsView;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import scala.Tuple2;

public class StatsMonitor extends StreamingMonitor {

    protected final ArrayBlockingQueue<StatsPipelineGroupedRecord> monitoringQueue;
    protected final Integer intervalLength;

    public StatsMonitor(ArrayBlockingQueue<StatsPipelineGroupedRecord> monitoringQueue, Integer intervalLength) {
        this.monitoringQueue = monitoringQueue;
        this.intervalLength = intervalLength;
    }

    // Poll monitoring queue for StatsPipelineGroupedRecord objects and build stats
    // Example:
    //   - 123456789 buckets to 12345678 bucket
    public void analyze() {
        StatsPipelineGroupedRecord group = this.monitoringQueue.poll();
        if(group != null) {
            this.presentView(group.getDate(), new ArrayList<>(group.getRecords()));
        }
    }

    // Build StatsView and present statistics for interval
    protected void presentView(Integer key, List<StatsPipelineRecord> data) {
        long requestsTotal = data.size();
        long successes = totalSuccesses(data);
        StatsView view = StatsView.build(requestsTotal, key, key + (intervalLength - 1));
        view.addTopSections(topSections(key, data));
        view.addSuccessTotals(successes);
        view.addFailureTotals(requestsTotal - successes);
        view.addByteTotals(totalBytes(data));
        view.render();
    }

    // Calculate top N sections by section name and hit count for given interval
    protected List topSections(Integer key, List<StatsPipelineRecord> data) {
        JavaRDD<StatsPipelineRecord> rdd = SharedResources.instance().sc.parallelize(data);
        JavaPairRDD<String, Integer> reduced = rdd.mapToPair(obj -> new Tuple2<>(obj.getPathSection(), 1)).reduceByKey((a, b) -> a + b);
        JavaPairRDD<Integer, String> reducedSwapped = reduced.mapToPair(obj -> obj.swap()).sortByKey(false);
        return reducedSwapped.collect();
    }

    // Count all requests with status 2XX
    protected long totalSuccesses(List<StatsPipelineRecord> data) {
        return data.stream().filter(line -> line.getStatus() >= 200 && line.getStatus() < 300).count();
    }

    // Count all bytes
    protected long totalBytes(List<StatsPipelineRecord> data) {
        return data.stream().map(x -> x.getBytes()).mapToLong(x -> x.longValue()).sum();
    }
}