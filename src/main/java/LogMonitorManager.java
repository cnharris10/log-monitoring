import Collectors.Collector;
import Monitoring.RateMonitor;
import Monitoring.StatsMonitor;
import Pipelines.RatePipeline;
import Pipelines.StatsPipeline;
import Processors.RateProcessor;
import Processors.StatsProcessor;
import Shared.SharedResources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LogMonitorManager {

    public static String filepath;
    public static Integer messagesPerSecond;
    public static Integer averageMessageRate;
    public static Integer statsMonitoringDelay = 30;
    public static Integer intervalLength = 10;
    public static Integer processingWindow = 60;
    public static Integer numberOfSecondsInTwoMinutes = 120;

    public static void main(String[] args) {
        buildOptions(args);
        try {
            List<Thread> components = buildComponents();
            execute(components);
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
        List<Thread> threads = new ArrayList<>();

        // Collector and include 2 streams adapters
        Collector collector = new Collector(filepath);

        collector.addStream(new StatsPipeline(instance.statsProcessingQueue));
        collector.addStream(new RatePipeline(instance.rateProcessingQueue));
        threads.add(new Thread(collector));

        // Processors
        threads.add(new Thread(new StatsProcessor(instance.statsProcessingQueue, instance.statsMonitoringQueue, intervalLength, processingWindow)
        ));
        threads.add(new Thread(new RateProcessor(instance.rateProcessingQueue)));

        // Stats Monitoring system
        threads.add(new Thread(new StatsMonitor(instance.statsMonitoringQueue, statsMonitoringDelay, intervalLength)));
        threads.add(new Thread(new RateMonitor(averageMessageRate, numberOfSecondsInTwoMinutes)));

        return threads;
    }

    public static void execute(List<Thread> threads) {
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
