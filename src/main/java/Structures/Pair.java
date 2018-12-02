package Structures;

public class Pair<K,V> {

    private K firstValue;
    private V secondValue;

    public Pair(K firstValue , V secondValue){
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    public K getFirstValue() {
        return this.firstValue;
    }

    public V getSecondValue(){
        return this.secondValue;
    }

    public void setFirstValue(K firstValue){
        this.firstValue = firstValue;
    }

    public void setSecondValue(V secondValue){
        this.secondValue = secondValue;
    }

    public String toString(){
        return ""+firstValue+","+secondValue+" ";
    }
}
