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
    public static ArrayList<Query>  readQueries(String sourcePath)throws Exception {

//        String queriesPath = sourcePath + "\\" + "queries.txt";
        String queriesPath = sourcePath;
        ArrayList<Query> ans = new ArrayList<>();


        //fill the file collection with the files from the sub folders

        try {
            String fileText= new String(Files.readAllBytes(Paths.get(queriesPath)));
            int currWord = 0;
            String[] unProssecedFileTextWords = fileText.split(" |\r|\n|\b");
            ArrayList<String>fileTextWords = new ArrayList<>();
            for(int i = 0 ; i < unProssecedFileTextWords.length ; i++){
                if(unProssecedFileTextWords[i].length()>0)
                    fileTextWords.add(unProssecedFileTextWords[i]);
            }

            while(currWord<fileTextWords.size()) {

                if (fileTextWords.get(currWord).equals("<top>")) {
                    currWord++;
                    ArrayList<String> queryContent = new ArrayList<>();

                    //extract the query between the <top> tags
                    while (!fileTextWords.get(currWord).equals("</top>")) {
                        queryContent.add(fileTextWords.get(currWord));
                        currWord++;
                    }

                    //get the query num and text fom each top tag content
                    String queryNum = "";
                    ArrayList<String> originalQueryWords = new ArrayList<>();
                    for (int i = 0; i < queryContent.size(); i++) {
                        if (queryContent.get(i).equals("Number:")) {
                            queryNum = queryContent.get(i+1);
                            continue;
                        }
                        if (queryContent.get(i).equals("<title>")) {
                            String queryText = "";
                            i++;
                            while (!queryContent.get(i).equals("<desc>")) {
                                queryText += queryContent.get(i)+" ";
                                originalQueryWords.add(queryContent.get(i));
                                i++;
                            }

                            //lets pass <desc> and description:
                            i++;
                            i++;
                            //and the start decription word
                            i++;
                            String queryDesc = "";
                            while (!queryContent.get(i).equals("<narr>")) {
                                queryDesc += queryContent.get(i) + " ";
                                i++;
                            }

                            ans.add(new Query(queryNum, queryText.substring(0,queryText.length()-1),queryDesc.substring(0,queryDesc.length()-1)));
                            break;

                        }

                    }


                }
                else currWord++;

                }

        } catch (FileNotFoundException e) {
            System.out.println("unable to read the file");
            throw e;
        } catch (Exception e) {
            throw e;
        }

        return ans;

    }
}
