package Shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
class ConcurrentSlidingWindowTest {

    private ConcurrentSlidingWindow window;

    @BeforeEach
    void init() {
        window = new ConcurrentSlidingWindow();
    }

    @Test
    void testExpand() {
        window.expand(1);
        assertEquals(window.size(), 1);
    }

    @Test
    void testCompressNoOp() {
        window.expand(300);
        window.compress(200);
        assertEquals(window.size(), 1);
    }

    @Test
    void testCompress() {
        window.expand(1);
        window.compress(10);
        assertEquals(window.size(), 0);
    }

    @Test
    void testSize() {
        window.expand(1);
        assertEquals(window.size(), 1);
    }
}