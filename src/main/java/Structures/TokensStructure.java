package Structures;

import java.util.ArrayList;

public class TokensStructure {

    private ArrayList<String> tokenList;
    private String fromCity , docName ,docAuthor , docLang, docType;

    public TokensStructure(ArrayList<String> tokenList,String fromCity,String docName,String docLang,String docType,
                           String docAuthor){
        this.tokenList = tokenList;
        this.fromCity = fromCity;
        this.docName = docName;
        this.docAuthor = docAuthor;
        this.docLang = docLang;
        this.docType = docType;
    }

    public String getDocName() {
        return docName;
    }

    public String getFromCity() {
        return fromCity;
    }

    public ArrayList<String> getTokenList() {
        return tokenList;
    }

    public String getDocType() {
        return docType;
    }

    public String getDocLang() {
        return docLang;
    }

    public String getDocAuthor() {
        return docAuthor;
    }
}
