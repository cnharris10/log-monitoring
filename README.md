# Log Monitoring

Given a provided log file (see Input section) at a desired message rate, this application outputs aggregate interval statistics for every 10 seconds and alerts in real-time when request spike occurs or subsides.

## Architecture

![Log Monitor Architecture](https://imgur.com/XckeNZ1.png)

(circular arrows == continuous thread)

This application consists of 3 generalized pipeline steps:
- Collection
- Processing
- Monitoring

with data between steps stored within shared Queues.

### Collection
- Read input file and enqueue a subset of data to all registered pipelines**
- Registers 2 Pipelines (StatsPipeline, RatePipeline)
    - Stats pipeline contains a subset of each log line's data (StatsPipelineRecord) that is enqueued to the StatsProcessor.
    - Rate pipeline contains each log line's date that is enqueued to the RateProcessor.

### Processing
- Processes incoming data based on each processor's corresponding function and sends processed data to paired monitor
- StatsProcessor
    - Groups all StatPipelineRecords into 10 second intervals (StatsPipelineGroupedRecord) and sends each interval grouping to corresponding StatsMonitor.
- RateProcessor
    - Acts as a pass-through and sends raw data to its corresponding RateMonitor.

### Monitoring
- Outputs data to STDOUT based on function
- StatsMonitor
    - For each StatsPipelineGroupedRecord interval, aggregate top sections in descending order, number of requests, successful requests, failed requests, and total bytes and output to STDOUT.
- RateMonitor
    - Using a sliding window that enqueues each incoming logLine's date and compresses for the last ~2 minutes, output an alert to STDOUT for a request-rate spike or recovery.

## Installation

### Requirements
- Java 1.8.0 (jdk1.8.0_191.jdk)

### Fetching application
- Download repository: `git clone https://github.com/cnharris10/log-monitoring.git`
- cd log-monitoring/

## Required Application Inputs

### Log file
**Recommendation: Use the provided log file for running application: sample_csv.txt**

If you want to create a custom log file, see the **Custom Log File** section below.

Example: **/path/to/log-monitoring/sample_csv.txt**

### Message rate
An positive integer value that specifies an upper bound for how many requests per **second** are allowed before an alert occurs

Example: **10**

### Running
`java -jar httplogstreaming.jar <log file> <message rate>`

Example:`java -jar httplogstreaming.jar /tmp/log.txt 10`

## Sample output
See top-level file: sample_log.txt for output of a previous application run.

## Testing

- :white_check_mark: Tests passed: 36 of 36 tests - 629ms
- 95% classes, 77% lines covered
    - **IMPORTANT: Stopped adding unit tests solely due to time**
    - For a production feature, full test coverage required.

## General Areas for Improvement
- Implement thread pool instead of 1 continuous thread per pipeline step.
- Support multiple output/input types/connectors
    - Process data from/to files, database, streams, object stores, etc.
- Stricter log line interpolation (if possible)
    - Current app simply skips any problematic log line.
- Convert queues to be elastic that adjust for high/low traffic
- Allow the user to configure all LogMonitorManager variables
    - Provide a fully-detailed `man` explanatation of all arguments for user.
    - Use a traditional option parser for parsing arguments
- CI/CD
    - All future changes rebuilt and tested before releasing subseqent JAR
- Collector
    - Add durable persistence/checkpointing for each queue in case of system failure
    - Shared queues are synonymous with topics in Kafka.  A production system will utilize a more battle-tested queuing system.
- Processors
    - Processors are synonymous with Spark Streaming/K-Streams/Flink for deriving aggregations/transforms from streaming data
    - A production system will utilize a more battle-tested streaming system.

## Custom Log File

### Header (optional)
If provided, the header must be the following comma-delimited string:
```
"remotehost","rfc931","authuser","date","request","status","bytes"
```

### Log File Requirements
Log lines must be N comma-delimted set of strings/integers with a carriage return ending each line.
Strict Example: 
```
"10.0.0.1","-","apache",1549574328,"GET /report HTTP/1.0",200,1136
"10.0.0.2","-","apache",1549574329,"GET /api HTTP/1.0",200,1136
"10.0.0.3","-","apache",15495743230,"GET /report HTTP/1.0",404,1136
"10.0.0.4","-","apache",1549574331,"GET /api HTTP/1.0",500,1136
```

Each column must be of type:
String: ["remotehost","rfc931","authuser","request"]
Integer: ["date", "status", "bytes"]

## License
[MIT](https://choosealicense.com/licenses/mit/)
