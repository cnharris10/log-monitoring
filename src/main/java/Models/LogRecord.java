package Models;

import org.apache.log4j.Logger;

public class LogRecord extends Record {

    public static Logger log = Logger.getLogger(LogRecord.class);

    private static String delimiter = ",";
    private static String headerMatchingWord = "remotehost";
    private String request;
    private Integer bytes;

    public static LogRecord parse(String logLine) {
        try {
            String[] parts = logLine.split(delimiter);
            LogRecord record = new LogRecord();
            record.setDate(parts[3]);
            record.setRequest(parts[4]);
            record.setStatus(parts[5]);
            record.setBytes(parts[6]);
            return record;
        } catch(Exception ex) {
            log.error("Invalid log line, skipping: "+logLine);
            return null;
        }
    }

    public static Boolean isHeader(String rawLine) {
        return rawLine.contains(headerMatchingWord);
    }

    private LogRecord() { }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getRequest() {
        return request;
    }

    public void setBytes(String bytes) { this.bytes = Integer.parseInt(bytes); }

    public Integer getBytes() { return bytes; }

    public StatsPipelineRecord statsData() {
        return StatsPipelineRecord.parse(this);
    }

}
