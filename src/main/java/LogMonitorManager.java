import Collectors.Collector;
import Monitoring.RateMonitor;
import Monitoring.StatsMonitor;
import Pipelines.RatePipeline;
import Pipelines.StatsPipeline;
import Processors.RateProcessor;
import Processors.StatsProcessor;
import Shared.SharedResources;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LogMonitorManager {

    public static String filepath;
    public static Integer messagesPerSecond;
    public static Integer averageMessageRate;
    public static Integer intervalLength = 10;
    public static Integer processingWindow = 30;
    public static Integer numberOfSecondsInTwoMinutes = 120;
    public static Integer processingTimeoutInSeconds = 3;
    public static List<Thread> threads;
    public static SharedResources sharedResourcesInstance = SharedResources.instance();

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
        sharedResourcesInstance.logger.log("Initializing application...");
        threads = new ArrayList<>();

        sharedResourcesInstance.logger.log("Starting Collector...");

        // Collector and include 2 streams adapters
        Collector collector = new Collector(filepath);

        sharedResourcesInstance.logger.log("Adding 2 pipelines: Stats, Rate");
        collector.addPipeline(new StatsPipeline(sharedResourcesInstance.statsProcessingQueue));
        collector.addPipeline(new RatePipeline(sharedResourcesInstance.rateProcessingQueue));
        threads.add(new Thread(collector));

        sharedResourcesInstance.logger.log("Loading Processors...");
        // Processors
        threads.add(new Thread(new StatsProcessor(sharedResourcesInstance.statsProcessingQueue, sharedResourcesInstance.statsMonitoringQueue, sharedResourcesInstance.map, intervalLength, processingWindow, processingTimeoutInSeconds)
        ));
        threads.add(new Thread(new RateProcessor(sharedResourcesInstance.rateProcessingQueue)));

        sharedResourcesInstance.logger.log("Loading Monitors...");
        // Stats Monitoring system
        threads.add(new Thread(new StatsMonitor(sharedResourcesInstance.statsMonitoringQueue, intervalLength)));
        threads.add(new Thread(new RateMonitor(averageMessageRate, numberOfSecondsInTwoMinutes)));

        return threads;
    }

    public static void start(List<Thread> threads) {
        sharedResourcesInstance.logger.log("Starting application...");
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
