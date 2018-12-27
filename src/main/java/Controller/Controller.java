package Controller;

import Indexer.VariableByteCode;
import Model.IModel;
import Structures.CorpusDocument;
import Structures.PostingDataStructure;
import View.IView;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
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
    private TextField corpusPathField , targetPathField , sampleField , queryField , queryFilePathField, resultsFilePathField;
    @FXML
    private TextField kField,bField,weightB,weightK,weightPos,weightTitle,weightBM,idfLower,idfDelta;
    @FXML
    private StringProperty termP;
    @FXML
    private IntegerProperty idP;
    @FXML
    private TableColumn<String,String> docName,docRank, termCol,tfCol;

    //@FXML
   // private TableColumn<CorpusDocument,String>
    @FXML
    private ListView<String> listView;

    @FXML
    private TableView<String> tableView , queryResultsView;

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
       String out = this.model.deleteOutputFiles(new File(this.targetPathField.getText()));
        this.view.errorMessage(out);
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
        String out = this.model.runEngine(stemmerStatus);
        this.view.errorMessage(out);
    }

    public void handleBrowseButtonTargetPath() {
        String text = this.view.showDirectoryBrowser((Stage) this.anchorPane.getScene().getWindow());
        if (text != null) {
            this.targetPathField.setText(text);
            this.model.setTargetPath(targetPathField.getText());
        }
    }

    public void handleLoadDictionary() {
        try {
            String out = this.model.LoadDictionaryToMemory();
            this.view.errorMessage(out);
        }catch (Exception e){
            this.view.errorMessage("Could not load a dictionary");
        }
    }

    public void handleDisplayDicitionary(){
        TreeMap<String, PostingDataStructure> d = model.getDictionary();
        try{
        if (d==null){
            return;
        }
        else {
            VariableByteCode vb = new VariableByteCode();
            this.dictResult = FXCollections.observableArrayList();
            this.dictResult.addAll(d.keySet());
            termCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
            tfCol.setCellValueFactory(cellData -> new SimpleStringProperty(getDecodedTf(d.get(cellData.getValue()).getEncodedData(),vb)));
            tfCol.setComparator(new IntComparator());
            this.tableView.setItems(dictResult);
        }
        }catch (Exception e){
            System.out.println("Error while trying to show dictionary");
        }
    }


    private String getDecodedTf(byte[] stream , VariableByteCode vb){
        Integer ans;
        LinkedList<Integer> tlist = vb.decode(stream);
        ans = tlist.getFirst();
        return ans.toString();
    }

    @FXML
    public void handleDisplayLangs(){
        ObservableList<String> langList = FXCollections.observableArrayList();
        langList.addAll(this.model.getDocsLang());
        if(langList == null || langList.size() == 0)
            this.view.errorMessage("No language list to show");

        this.listView.setItems(langList);

    }

    public void handleRunQuery(){
        String query = queryField.getText();
        int index = 0;

        if(query.equals(null))
            return;

        ArrayList<CorpusDocument> answer = this.model.runQueryOnEngine(query);
        ObservableList<String> docList = FXCollections.observableArrayList();
        TreeMap<Integer , String> ansMap = new TreeMap<>();

        for (CorpusDocument document:
             answer) {
            ansMap.put(index++,document.getName());
        }
        docList.addAll(ansMap.values().toString());
        index =0 ;
        this.queryResultsView.setItems(docList);

        //docRank.setCellValueFactory(cellValue -> new SimpleStringProperty(rankToString(answer.get(index).getRank())));


    }

    private String rankToString(double rank){
        return ""+rank;
    }

    /**
     * sample run for debugging the aprser .
     * *NOT IN USE*
     */
    public void handleSampleRun(){
        String text = this.sampleField.getText();
        TreeMap<String,PostingDataStructure> map = model.runSample(text,this.stemmerCheckBox.isSelected());

        this.dictResult = FXCollections.observableArrayList();
        this.dictResult.addAll(map.keySet());
        VariableByteCode vb = new VariableByteCode();
        termCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        tfCol.setCellValueFactory(cellData -> new SimpleStringProperty(getDecodedTf(map.get(cellData.getValue()).getEncodedData(),vb)));;
        this.tableView.setItems(dictResult);

    }

    public void handleBrowseButtonCorpusPath() {
        String text = this.view.showDirectoryBrowser((Stage) this.anchorPane.getScene().getWindow());
        if (text != null) {
            this.corpusPathField.setText(text);
            model.setCorpusPath(this.corpusPathField.getText());
        }
    }

    public void handleBrowseButtonQueryFilePath() {
        String text = this.view.showDirectoryBrowser((Stage) this.anchorPane.getScene().getWindow());
        if (text != null) {
            this.queryFilePathField.setText(text);
        }
    }

    public void handleBrowseTargetResultFilePath() {
        String text = this.view.showDirectoryBrowser((Stage) this.anchorPane.getScene().getWindow());
        if (text != null) {
            this.resultsFilePathField.setText(text);
        }
    }

    public void handleCreateResultsFile(){

        String f1 =queryFilePathField.getText();
        String f2 = resultsFilePathField.getText();

        if(f1 == null || f2 == null)
            return;
        this.model.setRankingParameters(Double.parseDouble(kField.getText()),Double.parseDouble(bField.getText()),Double.parseDouble(weightK.getText()),Double.parseDouble(weightB.getText()),
                Double.parseDouble(weightBM.getText()),Double.parseDouble(weightPos.getText()),Double.parseDouble(weightTitle.getText()),Double.parseDouble(idfLower.getText()),Double.parseDouble(idfDelta.getText()));

        this.model.createResultFileForQueries(queryFilePathField.getText()+"\\queries.txt",resultsFilePathField.getText());

    }

    public void setView(IView view){
        this.view = view;
    }

    public void setModel(IModel model){
        this.model = model;
    }
}
