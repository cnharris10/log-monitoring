package Collectors;

import Pipelines.Pipeline;

public interface Collectable {

    void read();
    void addPipeline(Pipeline pipeline);
    void setIdle(Boolean flag);

}
