package Monitoring;

import Shared.ConcurrentSlidingWindow;
import Shared.SharedResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
class RateMonitorTest {

    private RateMonitor monitor;
    private SharedResources sharedResources;
    private ConcurrentSlidingWindow window;

    @BeforeEach
    void init() {
        monitor = new RateMonitor(10, 120);
        sharedResources = SharedResources.instance();
        window = SharedResources.instance().rateWindow;
    }

    @Test
    void testConstructor() {
        assertEquals(monitor.averageMessageRate, Integer.valueOf(10));
        assertEquals(monitor.windowLengthInSeconds, Integer.valueOf(120));
        assertEquals(monitor.sharedResources.getClass(), SharedResources.class);
    }

    @Test
    void testAnalyzeRateWithNullProcessingTime() {
        RateMonitor monitorSpy = Mockito.spy(monitor);
        SharedResources sharedResourcesSpy = Mockito.spy(sharedResources);
        when(sharedResourcesSpy.getClockTime()).thenReturn(null);
        verify(sharedResourcesSpy, never()).getRateWindowCount();
        monitorSpy.analyzeRate();
    }

    @Test
    void testAnalyzeRateWithValidProcessingTime() {
        Integer clock = 123456789;
        Integer rateWindowCount = 200;
        RateMonitor monitorSpy = Mockito.spy(monitor);
        when(monitorSpy.getCurrentClockTime()).thenReturn(clock);
        when(monitorSpy.getCurrentRateWindowCount()).thenReturn(rateWindowCount);
        monitorSpy.analyzeRate();
        verify(monitorSpy, times(1)).processRate(123456789, 200);
        verify(monitorSpy, times(1)).compress(123456669);
    }

    @Test
    void testProcessRateWithGeneratedAlert() {
        RateMonitor monitorSpy = Mockito.spy(monitor);
        monitorSpy.processRate(123456789, 1000);
        verify(monitorSpy, times(1)).present("High traffic generated an alert - hits = 1000 at: 123456789");
        verify(monitorSpy, times(1)).toggleFlag();
        assertEquals(monitorSpy.flag, true);
    }

    @Test
    void testProcessRateWithRecoveredAlert() {
        RateMonitor monitorSpy = Mockito.spy(monitor);
        monitorSpy.flag = true;
        monitorSpy.processRate(123456789, 1);
        verify(monitorSpy, times(1)).present("Traffic recovered from alert - hits 1 at: 123456789");
        verify(monitorSpy, times(1)).toggleFlag();
        assertEquals(monitorSpy.flag, false);
    }

}