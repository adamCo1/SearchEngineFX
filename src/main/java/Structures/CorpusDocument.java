package Structures;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedList;

public class CorpusDocument implements IData{

    private double rank ;
    private int docID , length , maxTF , uniqueNumberOfTerms;
    private String name , author , city , lang , type;
    private LinkedList<String> entities ;
    private HashSet<String> loopupEntities;

    public CorpusDocument(int docID , int length , int maxTF , int uniqueNumberOfTerms, String name,
                          String author, String city, String lang, String type, LinkedList<String> entities){
        this.docID = docID;
        this.length = length;
        this.maxTF = maxTF;
        this.uniqueNumberOfTerms = uniqueNumberOfTerms;
        this.name = name;
        this.author = author;
        this.city = city;
        this.lang = lang;
        this.type = type;
        this.entities = new LinkedList<>();
        this.loopupEntities = new HashSet<>();
        deepCopyEntities(entities);

    }

    private void deepCopyEntities(LinkedList<String> temp){
        for (String entity:
             temp) {
            this.entities.addLast(entity);
            this.loopupEntities.add(entity);
        }
    }

    public boolean isEntity(String term){
        return this.loopupEntities.contains(term);
    }

    public double getRank() {
        return rank;
    }

    public void addRank(double rank){
        this.rank += rank;
    }

    public int getDocID() {
        return docID;
    }

    public String getType() {
        return type;
    }

    public String getLang() {
        return lang;
    }

    public String getCity() {
        return city;
    }

    public String getAuthor() {
        return author;
    }

    public int getUniqueNumberOfTerms() {
        return uniqueNumberOfTerms;
    }

    public String getName() {
        return name;
    }

    public int getMaxTF() {
        return maxTF;
    }

    public int getLength() {
        return length;
    }

    public LinkedList<String> getEntities() {
        return entities;
    }



    public String toString(){
        DecimalFormat format = new DecimalFormat("#.####");
        return this.name + "   " + format.format(this.rank);
    }
}
