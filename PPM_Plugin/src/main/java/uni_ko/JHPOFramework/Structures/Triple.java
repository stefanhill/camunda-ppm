package uni_ko.JHPOFramework.Structures;

import java.io.Serializable;

public class Triple<T, U, V> implements Serializable{

    private final T first;
    private final U second;
    private final V third;

    public Triple(T first, U second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst() { return first; }
    public U getSecond() { return second; }
    public V getThird() { return third; }
    
    public T getLeft() { return first; }
    public U getMiddle() { return second; }
    public V getRight() { return third; }
}