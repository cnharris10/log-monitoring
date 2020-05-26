package Views;

public interface Viewable {

    void render();
    <T> void present(T output);

}
