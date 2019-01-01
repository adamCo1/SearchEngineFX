package Structures;

import java.io.Serializable;

public class Pair<K,V> implements Serializable ,Comparable {

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

    //this compareTo implementation is for handle semantic class
    @Override
    public int compareTo(Object o) {
        return (int)((Double)((Pair)o).firstValue-(Double)this.firstValue);
    }
}
