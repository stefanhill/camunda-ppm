package uni_ko.JHPOFramework.Structures;

import java.io.Serializable;

public class Pair<T, U> implements Serializable{

    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() { return first; }
    public U getSecond() { return second; }
    
    public T getKey() { return first; }
    public U getValue() { return second; }

    
}