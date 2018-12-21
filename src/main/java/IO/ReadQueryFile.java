package IO;

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
import java.util.ArrayList;

import static ReadFromWeb.ReadFromWeb.allCities;

public  class ReadQueryFile {


    /**
     * read all files from the corpus dir. parse the documents inside each file by their tags.
     * create a doc object and load it to the doc buffer.
     *
     * @param sourcePath the corpus dir
     */
    public static ArrayList<Query>  readQueries(String sourcePath) {

        String queriesPath = sourcePath + "\\" + "queries.txt";
        ArrayList<Query> ans = new ArrayList<>();


        //fill the file collection with the files from the sub folders

        try {
            Document fileText = Jsoup.parse(new String(Files.readAllBytes(Paths.get(queriesPath))));
            org.jsoup.select.Elements allQueries = fileText.select("top");
            for (org.jsoup.nodes.Element query : allQueries) {


                String[] queryContent = query.text().split(" ");
                String queryNum = "";
                for (int i = 0; i < queryContent.length; i++) {
                    if (queryContent[i].equals("Number:")) {
                        queryNum = queryContent[i + 1];
                        continue;
                    }
                    if (queryContent[i].equals("<title>")) {
                        String queryText = "";
                        i++;
                        while (!queryContent.equals("<desc>")) {
                            queryText += queryContent[i];
                            i++;
                        }
                        ans.add(new Query(queryNum, queryText));
                        break;
                    }

                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("unable to read the file");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ans;

    }
}
