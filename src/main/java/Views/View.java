package Views;

import Monitoring.StreamingMonitor;
import org.apache.log4j.Logger;

public class View implements Viewable {

    static Logger log = Logger.getLogger(StreamingMonitor.class);

    @Override
    public void render() { }

    @Override
    public <T> void present(T output) { }
}
