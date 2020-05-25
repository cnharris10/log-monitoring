package Collectors;

import Models.LogRecord;
import Shared.SharedResources;
import Pipelines.Pipeline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Collector implements Runnable, FileCollector {

    final protected Scanner scanner;
    final protected String filepath;
    final protected List<Pipeline> pipelines;
    private Boolean header = false;

    public Collector(String filepath) throws Exception {
        this.filepath = filepath;
        this.pipelines = new ArrayList<>();
        this.scanner = this.buildScanner();
    }

    public void addStream(Pipeline pipeline) {
        this.pipelines.add(pipeline);
    }

    protected Scanner buildScanner() throws Exception {
        return new Scanner(new File(this.filepath));
    }

    // Read input file one line at a time and queue massaged data into N pipelines
    // Examine first line (once) and skip if it is the known sample data header
    public void read() {
        setIdle(false);
        while (scanner.hasNextLine()) {
            String rawLine = scanner.nextLine();
            if(!header) {
                header = true;
                if(LogRecord.isHeader(rawLine)) {
                    continue;
                }
            }
            send(rawLine);
        }
        setIdle(true);
    }

    // For each registered pipeline, ingest LogRecord
    public void send(String rawLine) {
        LogRecord line = LogRecord.parse(rawLine);
        this.pipelines.forEach(pipeline -> pipeline.ingest(line));
    }

    // Track whether collector is running or idle
    protected void setIdle(Boolean flag) {
        SharedResources.instance().setIdle(flag);
    }

    // Read log file and post massaged log line contents to available pipelines
    public void run() {
        try {
            Thread.sleep(SharedResources.instance().threadSleepCount);
            while (true) {
                if(Thread.currentThread().isInterrupted()){
                    break;
                }
                this.read();
            }
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
