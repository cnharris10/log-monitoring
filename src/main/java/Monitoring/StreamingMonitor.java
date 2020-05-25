package Monitoring;

import org.apache.log4j.Logger;

public class StreamingMonitor implements Monitorable, Runnable {

    static Logger log = Logger.getLogger(StreamingMonitor.class);

    public void present(String data){
        log.info(data);
    }

    public void run() { }

}
