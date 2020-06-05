package Shared;

import Models.StatsPipelineGroupedRecord;
import Models.StatsPipelineRecord;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import scala.App;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class SharedResources {

    private static SharedResources instance = null;
    final public ArrayBlockingQueue<StatsPipelineRecord> statsProcessingQueue;
    final public ArrayBlockingQueue<StatsPipelineGroupedRecord> statsMonitoringQueue;
    final public ArrayBlockingQueue<Integer> rateProcessingQueue;
    final public ConcurrentSlidingWindow rateWindow;
    final public LinkedHashMap<Integer, List<StatsPipelineRecord>> map;
    final public Integer threadSleepCount = 10;
    final public ApplicationLogger logger = new ApplicationLogger();
    public JavaSparkContext sc;
    private Integer clock;
    private Integer capacity = 1000;

    public class ApplicationLogger {

        final private Level applicationLevel = Level.forName("NOTICE", 250);
        final private Logger logger = LogManager.getLogger();

        public <T> void log(T msg) {
            logger.log(applicationLevel, msg);
        }

    }

    private SharedResources(){
        this.statsProcessingQueue = new ArrayBlockingQueue<>(capacity);
        this.rateProcessingQueue = new ArrayBlockingQueue<>(capacity);
        this.statsMonitoringQueue = new ArrayBlockingQueue<>(capacity);
        this.map = new LinkedHashMap<>();
        this.rateWindow = new ConcurrentSlidingWindow();
        this.buildSparkContext();
    }

    public static SharedResources instance() {
        if(instance == null) {
            instance = new SharedResources();
        }
        return instance;
    }

    public synchronized void setClockTime(Integer clock) {
        this.clock = clock;
    }

    public synchronized Integer getClockTime() {
        return clock;
    }

    public synchronized Integer getRateWindowCount() { return this.rateWindow.size(); }

    private void buildSparkContext() {
        System.setProperty("hadoop.home.dir", "/");
        SparkConf sparkConf = new SparkConf().setAppName("topSections").setMaster("local[*]");
        this.sc = new JavaSparkContext(sparkConf);
        this.sc.setLogLevel("ERROR");
    }

}
