package Processors;

public interface Processable {
    void execute();
    <U> U receive();
    <V> void send(V data);
}
