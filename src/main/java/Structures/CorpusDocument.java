package Structures;

public class CorpusDocument implements IData{

    private double rank ;
    private int docID , length , maxTF , uniqueNumberOfTerms;
    private String name , author , city , lang , type;

    public CorpusDocument(int docID , int length , int maxTF , int uniqueNumberOfTerms, String name,
                          String author,String city,String lang, String type){
        this.docID = docID;
        this.length = length;
        this.maxTF = maxTF;
        this.uniqueNumberOfTerms = uniqueNumberOfTerms;
        this.name = name;
        this.author = author;
        this.city = city;
        this.lang = lang;
        this.type = type;
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

    public String toString(){
        return this.name;
    }
}
