package Pipelines;

import Models.LogRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class RatePipelineTest {

    Integer capacity = 1;
    ArrayBlockingQueue<Integer> queue;
    RatePipeline pipeline;

    @BeforeEach
    private void init() {
        queue = new ArrayBlockingQueue<>(capacity);
        pipeline = new RatePipeline(queue);
    }

    @Test
    void testConstructor() {
        assertEquals(this.queue.getClass(), queue.getClass() );
    }

    @Test
    void testOffer() {
        String line = "10.0.0.1,-,apache,1549574334,GET /api/user HTTP/1.0,200,1194";
        LogRecord record = LogRecord.parse(line);
        pipeline.ingest(record);
        assertEquals(this.queue.poll(),1549574334);
    }

}