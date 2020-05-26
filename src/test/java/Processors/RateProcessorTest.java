package Processors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class RateProcessorTest {

    Integer capacity = 1;
    ArrayBlockingQueue<Integer> queue;
    RateProcessor processor;

    @BeforeEach
    private void init() {
        queue = new ArrayBlockingQueue<>(capacity);
        processor = new RateProcessor(queue);
    }

    @Test
    void testConstructor() {
        assertEquals(this.queue.getClass(), queue.getClass());
    }

    @Test
    void testReceive() {
        this.queue.offer(1);
        assertEquals(this.queue.poll(), 1);
    }

    @Test
    void testExecute() {
        RateProcessor processorSpy = Mockito.spy(processor);
        this.queue.offer(1);
        processorSpy.execute();
        verify(processorSpy, times(1)).send(1);
    }

}
