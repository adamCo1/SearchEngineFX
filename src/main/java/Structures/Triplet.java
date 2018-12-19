package Structures;

public class Triplet<V,K,T> {

    private V first ;
    private K second ;
    private T third;

    public Triplet(V first ,K second , T third){
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public V getFirst() {
        return first;
    }

    public T getThird() {
        return third;
    }

    public K getSecond() {
        return second;
    }
}
