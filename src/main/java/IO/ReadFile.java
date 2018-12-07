package IO;

import Engine.DocController;
import Engine.IBufferController;
import ReadFromWeb.City;
import ReadFromWeb.ReadFromWeb;
import Structures.Doc;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static ReadFromWeb.ReadFromWeb.allCities;

/**
 * this class has 3 roles:
 * 1.read doc files
 * 2.Process the documents content
 * 3.load proccessed documents on a buffer so the parser can do its job
 */


public class ReadFile implements IReader {
    public boolean done = false;

    private IBufferController docConroller;

    public ReadFile(IBufferController docController) {
        this.docConroller = docController;
    }

    public ReadFile() {
    }

    public boolean getDone() {
        return this.done;
    }

    /**
     * read all files from the corpus dir. parse the documents inside each file by their tags.
     * create a doc object and load it to the doc buffer.
     * @param sourcePath the corpus dir
     */
    public void read(String sourcePath) {
        if(allCities.size() == 0)
            ReadFromWeb.getCities();
        int count = 0;
        File corpusDir = new File(sourcePath);

        //create a subdir filter
        FileFilter subDirsFiller = file -> file.isDirectory();

        //save the subdirs
        File[] subDirs = corpusDir.listFiles(subDirsFiller);

        //fill the file collection with the files from the sub folders
        for (File sd : subDirs)
            if (sd.listFiles() != null)
                for (File file : sd.listFiles()) {
                    if (file.isFile()) {

                        try {
                            Document fileText = Jsoup.parse(new String(Files.readAllBytes(Paths.get(file.getPath()))));
                            org.jsoup.select.Elements allDocs = fileText.select("DOC");
                            for (org.jsoup.nodes.Element doc : allDocs) {
                                org.jsoup.select.Elements docNo = doc.select("DOCNO");
                                org.jsoup.select.Elements docDate = doc.select("DATE1");
                                org.jsoup.select.Elements unProccessedDocOrigin = doc.select("F");
                                org.jsoup.select.Elements unProcessedDocAuthor = doc.select("BYLINE");
                                org.jsoup.select.Elements unProcessedDocType1 = doc.select("TYPE");
                                org.jsoup.select.Elements unProcessedDocType2 = doc.select("TYPE");
                                if (docDate.size() == 0) ;
                                docDate = doc.select("DATE");
                                org.jsoup.select.Elements docTitle = doc.select("TI");
                                org.jsoup.select.Elements docText = doc.select("TEXT");
                                String sDocNo = docNo.text();
                                String sDocDate = docDate.text();
                                City sDocOrigin = allCities.get(getDocOrigin(unProccessedDocOrigin).toUpperCase());
                                String sDocAuthor = getDocAuthor(unProcessedDocAuthor);
                                String sDocTitle = docTitle.text();
                                String sDocText = docText.text();
                                String sDocType = getDocType(unProcessedDocType1, unProcessedDocType2);
                                String sDocLang = getDocLang(unProccessedDocOrigin);
                                Doc newDoc = new Doc(sDocNo, sDocDate, sDocTitle, sDocText, sDocOrigin, sDocAuthor,sDocLang);
//                                System.out.println(newDoc.getDocOriginCity());
                                this.docConroller.addBuffer(newDoc);


                            }

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            System.out.println("unable to read the file");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }

                }
        done = true;
    }



    private String getDocOrigin(org.jsoup.select.Elements fTags) {
        String originCity = "";
        if (fTags.size() > 0)
            for (org.jsoup.nodes.Element fTag : fTags) {
                if (fTag.toString().substring(6, 9).equals("104")) {
                    originCity = fTag.text().split(" ")[0];
                }
            }

        return originCity;
    }


    private String getDocAuthor(org.jsoup.select.Elements byLineTags) {
        String author = "";
        String content = "";
        if (byLineTags.size() > 0) {
            org.jsoup.select.Elements pContent = byLineTags.select("P");
            if (pContent.size() > 0) {
                content = pContent.text();
                content = content.replace("By", "");
                for (int i = 0; i < content.length(); i++) {
                    if (!(content.charAt(i) == ' '))
                        break;
                    else
                        content = content.substring(i + 1);

                }

            } else {
                content = byLineTags.text();
                content = content.replace("By", "");
                for (int i = 0; i < content.length(); i++) {
                    if (!(content.charAt(i) == ' '))
                        break;
                    else
                        content = content.substring(i + 1);

                }
            }
        }

        author = content;

        return author;
    }


    private String getDocType(org.jsoup.select.Elements typeTag, org.jsoup.select.Elements tpTag) {
        String type = "";
        String content = "";
        if (typeTag.size() > 0) {
            org.jsoup.select.Elements pContent = typeTag.select("P");
            if (pContent.size() > 0) {
                content = pContent.text();
                content = content.replace("&amp", "");
                content = content.replace(";", "");
                for (int i = 0; i < content.length(); i++) {
                    if (!(content.charAt(i) == ' '))
                        break;
                    else
                        content = content.substring(i + 1);
                }
            } else {
                content = typeTag.text();
                content = content.replace("By", "");
                for (int i = 0; i < content.length(); i++) {
                    if (!(content.charAt(i) == ' '))
                        break;
                    else
                        content = content.substring(i + 1);

                }


                return type;

            }

        }
        return type;
    }


    private String getDocLang(org.jsoup.select.Elements fTags){
        String lang = "";
        if (fTags.size() > 0)
            for (org.jsoup.nodes.Element fTag : fTags) {
                if (fTag.toString().substring(6, 9).equals("105")) {
                    lang = fTag.text().replace(" ","");
                }
            }

        return lang;
    }
}