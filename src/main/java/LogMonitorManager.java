import Collectors.Collector;
import Monitoring.RateMonitor;
import Monitoring.StatsMonitor;
import Monitoring.StreamingMonitor;
import Pipelines.RatePipeline;
import Pipelines.StatsPipeline;
import Processors.RateProcessor;
import Processors.StatsProcessor;
import Shared.SharedResources;
import org.apache.log4j.Logger;
import sun.jvm.hotspot.runtime.Threads;
import sun.rmi.runtime.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LogMonitorManager {

    public static Logger log = Logger.getLogger(LogMonitorManager.class);
    public static String filepath;
    public static Integer messagesPerSecond;
    public static Integer averageMessageRate;
    public static Integer intervalLength = 10;
    public static Integer processingWindow = 60;
    public static Integer numberOfSecondsInTwoMinutes = 120;
    public static List<Thread> threads;

    public static void main(String[] args) {
        buildOptions(args);
        try {
            List<Thread> components = buildComponents();
            start(components);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void buildOptions(String[] args) {
        Integer argsLength = args.length;
        if(argsLength < 2) {
            throw new IllegalArgumentException("Must include 2 args: <filepath> <messageRate>.  Ex: /tmp/sample_csv.txt 10");
        }

        File file = new File(args[0]);
        if(file.exists()) {
            filepath = args[0];
        } else {
            throw new IllegalArgumentException("File "+args[0]+" does not exist");
        }

        messagesPerSecond = Integer.parseInt(args[1]);
        if(messagesPerSecond <= 0) {
            throw new IllegalArgumentException("Invalid messagesPerSecond argument. Must be > 0");
        }

        averageMessageRate = messagesPerSecond * numberOfSecondsInTwoMinutes;
    }

    public static List<Thread> buildComponents() throws Exception {
        SharedResources instance = SharedResources.instance();
        threads = new ArrayList<>();

        log.info("Starting Collector...");

        // Collector and include 2 streams adapters
        Collector collector = new Collector(filepath);

        log.info("Adding 2 pipelines: Stats, Rate");
        collector.addPipeline(new StatsPipeline(instance.statsProcessingQueue));
        collector.addPipeline(new RatePipeline(instance.rateProcessingQueue));
        threads.add(new Thread(collector));

        log.info("Loading Processors...");
        // Processors
        threads.add(new Thread(new StatsProcessor(instance.statsProcessingQueue, instance.statsMonitoringQueue, intervalLength, processingWindow)
        ));
        threads.add(new Thread(new RateProcessor(instance.rateProcessingQueue)));

        log.info("Loading Monitors...");
        // Stats Monitoring system
        threads.add(new Thread(new StatsMonitor(instance.statsMonitoringQueue, intervalLength)));
        threads.add(new Thread(new RateMonitor(averageMessageRate, numberOfSecondsInTwoMinutes)));

        return threads;
    }

    public static void start(List<Thread> threads) {
        log.info("Starting application...");
        threads.forEach(thread -> thread.start());
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

}
