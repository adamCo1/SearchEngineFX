package Indexer;

/**
 * buffer for info on doc's
 * this buffer does'nt need to change because the documents don't change .
 * has only getters and setters
 */

public class BufferDataDoc {

    private int ID;
    private byte[] maxTF , uniqueNumber;
    private String city , author , lang , type , name;

    public BufferDataDoc(int ID, byte[] maxTF , byte[] uniqueNumber,String city,String author,String lang,String type , String name){
        this.ID = ID;
        this.maxTF = maxTF;
        this.uniqueNumber = uniqueNumber;
        this.city = city;
        this.author = author;
        this.lang = lang;
        this.type = type;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getLang() {
        return lang;
    }

    public String getAuthor() {
        return author;
    }

    public String getCity() {
        return city;
    }

    public int getID() {
        return ID;
    }

    public int getSize() {
        try {
            return 4 + this.maxTF.length + this.uniqueNumber.length + 12 + city.length() + lang.length() + author.length() + type.length() + name.length();

        }catch (Exception e) {
            return 24;
        }
    }

    public byte[] getMaxTF(){
        return this.maxTF;
    }

    public byte[] getUniqueNumber(){
        return this.uniqueNumber;
    }
}
