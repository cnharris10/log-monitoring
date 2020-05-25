# Log Monitoring

The application provided ingests a provided log file (see Input section) and does the following:
  - Outputs aggregate interval statistics for every 10 seconds
  - Alerts during a request-rate spike and when a spike subsides.

## Architecture

### Collection
**Goal: Enqueue a subset of data to all registered pipelines**

A common Collector registers 2 Pipelines (StatsPipeline, RatePipeline), iterates over the provided log file, and builds 2 parallel pipelines.  The Stats pipeline contains a subset of each log line's data (StatsStreamRecord) that is enqueued to the StatsProcessor.  The Rate pipeline contains each log line's date that is enqueued to the RateProcessor.

### Processing
**Goal: Process incoming data based on each processor's corresponding function, send processed data to paired monitor**

#### StatsProcessor
Groups all StatPipelineRecords into 10 second intervals (StatsPipelineGroupedRecord) and sends each interval grouping to corresponding StatsMonitor.

#### RateProcessor
Acts as a pass-through and sends raw data to its corresponding RateMonitor.

### Monitoring
**Goal: Output data based on function**

#### StatsMonitor
For each StatsPipelineGroupedRecord interval, aggregate top sections in descending order, number of requests, successful requests, failed requests, and total bytes and output to STDOUT.

#### RateMonitor
Using a sliding window that enqueues each incoming logLine's date and compresses for the last ~2 minutes, output an alert to STDOUT for a request-rate spike or recovery.

![Log Monitor Architecture](https://imgur.com/xaveg0z.png)

## Installation

### Requirements
- Java 13.0.2 (openjdk-13.0.2.jdk)

### Fetching application
- Download repository: `git clone https://github.com/cnharris10/log-monitoring.git`
- cd log-monitoring/out/artifacts/httplogstreaming_jar

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

## General Areas for Improvement
- Thread pool instead of 1 continuous thread per pipeline step
- Multiple output/input types/connectors (file, DB, streaming system)
- Elastic blocking queues that adjust for high/low traffic
- Fully configurable system with user-provided property files (not just 2 argmuments for file and messsageRate)
- CI/CD build for running tests/building JAR

- Collector
    - Add durable persistence/checkpointing for each queue in case of system failure
    - Shared queues are synonymous with topics in Kafka.  A production system will utilize a more battle-tested queuing system.
- Processors
    - Processors are synonymous with Spark Streaming/K-Streams/Flink for deriving aggregations/transforms from streaming data
    - A production system will utilize a more battle-tested streaming system.

- **Important: Full test coverage (only provided a sample of tests due to time)**


## Custom Log File

### Header (optional)
If provided, the header must be the following comma-delimited string:
`"remotehost","rfc931","authuser","date","request","status","bytes"`

### Log File Requirements
Log lines must be N comma-delimted set of strings/integers with a carriage return ending each line.
Strict Example: 
`"10.0.0.1","-","apache",1549574328,"GET /report HTTP/1.0",200,1136`
`"10.0.0.2","-","apache",1549574329,"GET /api HTTP/1.0",200,1136`
`"10.0.0.3","-","apache",15495743230,"GET /report HTTP/1.0",404,1136`
`"10.0.0.4","-","apache",1549574331,"GET /api HTTP/1.0",500,1136`

Each column must be of type:
String: ["remotehost","rfc931","authuser","request"]
Integer: ["date", "status", "bytes"]

## License
[MIT](https://choosealicense.com/licenses/mit/)
