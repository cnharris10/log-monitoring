package Models;

public class StatsPipelineRecord extends Record {

    protected String pathSection;
    protected Integer bytes;

    // Arguments:
    //   - pathSection: First prefix of url path (i.e. path: /report/users --> /report)
    //   - date: Log line "date"
    //   - status: Log line "status" (i.e. 200, 404, 500, etc)
    //   - bytes: Log line "bytes"
    private StatsPipelineRecord(String pathSection, Integer date, Integer status, Integer bytes) {
        super(date, status);
        this.setPathSection(pathSection);
        this.setBytes(bytes);
    }

    // Given a LogRecord, create a StatsStream Record with:
    //    - section: "GET /api/users HTTP/1.0" --> "/api"
    //    - date
    //    - status
    public static StatsPipelineRecord parse(LogRecord record) {
        String section = record.getRequest().split(" ")[1].split("/")[1];
        return new StatsPipelineRecord("/"+section, record.getDate(), record.getStatus(), record.getBytes());
    }

    public void setPathSection(String pathSection) {
        this.pathSection = pathSection;
    }

    public String getPathSection() {
        return pathSection;
    }

    public Integer getDate() {
        return date;
    }

    public Integer getIntervalDate(Integer intervalLength) { return getDate() - (date % intervalLength); }

    public void setBytes(Integer bytes) { this.bytes = bytes; }

    public Integer getBytes() { return bytes; }

}
