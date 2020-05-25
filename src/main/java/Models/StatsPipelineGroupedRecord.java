package Models;

import java.util.List;

public class StatsPipelineGroupedRecord {

    final protected Integer date;
    final protected List<StatsPipelineRecord> records;

    protected StatsPipelineGroupedRecord(Integer date, List<StatsPipelineRecord> records) {
        this.date = date;
        this.records = records;
    }

    public static StatsPipelineGroupedRecord build(Integer key, List<StatsPipelineRecord> records) {
        return new StatsPipelineGroupedRecord(key, records);
    }

    public Integer getDate() {
        return date;
    }

    public List<StatsPipelineRecord> getRecords() {
        return records;
    }

}
