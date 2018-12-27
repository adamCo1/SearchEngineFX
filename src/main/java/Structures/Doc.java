package Structures;

import ReadFromWeb.City;

/**
 * this class represents the docs which will load to the doc buffer from ReadFile class
 */
public class Doc {

    private String docId;
    private String docTitle;
    private String docDate;
    private City docOriginCity;
    private String docText;
    private String docAuthor;
    private String docType;
    private String docSubject;
    private String docLang;
    private int engineID ;

    public Doc(String docId, String docDate, String docTitle, String docText,City originCity,String docAuthor,String docLang) {
        this.docId = docId;
        this.docTitle = docTitle;
        this.docDate = docDate;
        this.docText = docText;
        if(originCity != null)
            this.docOriginCity = originCity;
        this.docAuthor = docAuthor;
        this.docType = docType;
        this.docSubject = docSubject;
        this.docLang = docLang;
    }



    public String getDocId() {
        return docId;
    }

    public String getDocTitle() {
        if(this.docTitle == null)
            return "";

        return docTitle;
    }

    public String getDocDate() {
        return docDate;
    }

    public String getDocText() {
        return docText;
    }

    public City getDocOriginCity() {
        return docOriginCity;
    }

    public String getOriginCity(){
        if(this.docOriginCity == null)
            return "";

        return this.docOriginCity.getName();
    }

    public String getDocAuthor() {
        return docAuthor;
    }

    public String getDocType() {
        return docType;
    }

    public String getDocSubject() {
        return docSubject;
    }

    public String getDocLang() {
        return docLang;
    }

    public void setEngineID(int id){
        this.engineID = id;
    }

    public int getEngineID(){
        return this.engineID;
    }

    @Override
    public String toString() {
        return "Doc{" +
                "docId='" + docId + '\'' +
                ", docTitle='" + docTitle + '\'' +
                ", docDate='" + docDate + '\'' +
                ", docOriginCity='" + docOriginCity + '\'' +
                ", docText='" + docText + '\'' +
                ", docAuthor='" + docAuthor + '\'' +
                ", docType='" + docType + '\'' +
                ", docSubject='" + docSubject + '\'' +
                '}';
    }
}