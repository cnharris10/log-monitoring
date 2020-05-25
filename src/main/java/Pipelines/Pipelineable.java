package Pipelines;

import Models.LogRecord;

public interface Pipelineable {
    void ingest(LogRecord line);
}
