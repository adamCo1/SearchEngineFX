package Controller;

import Model.IModel;
import Model.Model;
import Structures.Pair;
import View.IView;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.swing.plaf.PanelUI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.TreeMap;

public class Controller {

    private IView view ;
    private IModel model;
    private ObservableList<String> dictResult;

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private CheckBox stemmerCheckBox;
    @FXML
    private TextField corpusPathField , targetPathField , sampleField;

    @FXML
    private StringProperty termP;
    @FXML
    private IntegerProperty idP;
    @FXML
    private TableColumn<String,String> termCol,idCol,tfCol;

    @FXML
    private ListView<String>listView;

    @FXML
    private TableView<String> tableView;

    public Controller(IView view , IModel model){
        this.view = view;
        this.model = model;
    }

    public Controller(){

    }

    public void handleShowDictionary(){

        this.view.showDictionary(this.model.getDictionary());
    }

    public void handleResetButton(){
        this.model.deleteOutputFiles(new File(this.targetPathField.getText()));
    }

    public void handleRunEngine(){

        boolean stemmerStatus = this.stemmerCheckBox.isSelected();
        String corpusPath = this.corpusPathField.getText() , targetPath = this.targetPathField.getText();
        if(corpusPath.equals("") || targetPath.equals("")) {
            this.view.errorMessage("No path chosen !");
            return;
        }

        this.model.setCorpusPath(corpusPath);
        this.model.setTargetPath(targetPath);
        this.model.runEngine(stemmerStatus);
    }

    public void handleBrowseButtonTargetPath(){
        this.targetPathField.setText(this.view.showDirectoryBrowser((Stage)this.anchorPane.getScene().getWindow()));
        this.model.setTargetPath(targetPathField.getText());
    }

    public void handleLoadDictionary() {
        try {
            this.model.LoadDictionaryToMemory();
            System.out.println("Dictionary loaded");
        }catch (Exception e){
            this.view.errorMessage("Could not load a dictionary");
        }
    }

    public void handleDisplayDicitionary(){
        TreeMap<String,Integer[]>d = model.getDictionary();

        TreeMap<Integer,String> dSortByTf = new TreeMap<>();
        for (String key:d.keySet()
        ) {
            dSortByTf.put(d.get(key)[0],key);
        }
        int i = 0 ;
        for(Integer key:dSortByTf.keySet()){
            if(i <= 10){
                System.out.println("Term: "+dSortByTf.get(key)+" TF: "+key);
            }
            i++;

        }
//        int termCount = 0;
//        int tfPath = 0;
//        while(termCount < )
//        try {
//            RandomAccessFile fos = new RandomAccessFile("TFsNoStem.txt", "rw");
//            d = model.getDictionary();
//            dSortByTf = new TreeMap<>();
//            for (String key : d.keySet()
//                    ) {
//                dSortByTf.put(d.get(key)[0], key);
//            }
//
//            for (Integer key : dSortByTf.keySet()) {
////            if(i >= dSortByTf.size()-11){
////                System.out.println("Term: "+dSortByTf.get(key)+"TF: "+key);
////            }
////            i++;
//                fos.writeChars(key + ",");
//
//            }
//            fos.close();

        if (d==null){
            return;
        }
        else {

            this.dictResult = FXCollections.observableArrayList();
            this.dictResult.addAll(d.keySet());
            termCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
            tfCol.setCellValueFactory(cellData -> new SimpleStringProperty(d.get(cellData.getValue())[0].toString()));
            this.tableView.setItems(dictResult);
        }

//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    @FXML
    public void handleDisplayLangs(){
        ObservableList<String> langList = FXCollections.observableArrayList();
        langList.addAll(this.model.getDocsLang());
        this.listView.setItems(langList);

    }

    public void handleSampleRun(){
        String text = this.sampleField.getText();
        TreeMap<String,Integer[]> map = model.runSample(text,this.stemmerCheckBox.isSelected());

        this.dictResult = FXCollections.observableArrayList();
        this.dictResult.addAll(map.keySet());

        termCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        tfCol.setCellValueFactory(cellData -> new SimpleStringProperty(map.get(cellData.getValue())[0].toString()));
        this.tableView.setItems(dictResult);

    }

    public void handleBrowseButtonCorpusPath(){
        this.corpusPathField.setText(this.view.showDirectoryBrowser((Stage)this.anchorPane.getScene().getWindow()));
        model.setCorpusPath(this.corpusPathField.getText());
    }

    public void setView(IView view){
        this.view = view;
    }

    public void setModel(IModel model){
        this.model = model;
    }
}
