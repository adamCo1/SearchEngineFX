package Indexer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class BufferDataString extends ABufferData {

    /**
     * key - docID
     * value - Arraylist , where each odd element is docID and each even element is the info on it
     */

    private LinkedList<byte[]> data ;
    private String fullName , currency, population , countryName;
    private int termID;

    public BufferDataString(int termID, byte[] docID , byte[] onTitle, byte[] info , String fullName
    , String countryName, String currency , String population){
        data = new LinkedList<>();
        data.addFirst(docID);
        data.addLast(onTitle);
        data.addLast(info);
        this.fullName = fullName;
        this.countryName = countryName;
        this.currency = currency;
        this.population = population;
        this.termID = termID;
    }

    public byte[] getInfo(){
        byte[] ans = this.data.getFirst();
        this.data.removeFirst();
        return ans;
    }

    public void addInfo(byte[] docid ,byte[] onTitle, byte[] info){
        this.data.addLast(docid);
        this.data.addLast(onTitle);
        this.data.add(info);
    }

    public int getDataSize(){
        Iterator iter = this.data.iterator();
        int ans = 0 ;

        while(iter.hasNext()){
            byte[] t = (byte[])iter.next();
            ans += t.length;
        }

        return ans;
    }

    public boolean hasMore(){
        return this.data.size() != 0;
    }

    public String toString(){
        String ans = "";

        for (byte[] b:
                this.data) {
            ans += (Arrays.toString(b));
        }

        return ans ;
    }

    public String getPopulation() {
        return population;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getFullName() {
        return fullName;
    }
}


