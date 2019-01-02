package Controller;

import IO.Query;
import IO.SemanticHandler;
import Indexer.VariableByteCode;
import Model.IModel;
import ReadFromWeb.*;
import Structures.CorpusDocument;
import Structures.PostingDataStructure;
import View.ErrorBox;
import View.IView;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Controller {

    private IView view ;
    private IModel model;
    private ObservableList<String> dictResult , citiesList;
    private HashSet<String> currentCitiesChosen;
    private ArrayList<CorpusDocument> currentAnswerList ;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private CheckBox stemmerCheckBox  ;
    @FXML
    private TextField corpusPathField , targetPathField , sampleField , queryField , queryFilePathField, resultsFilePathField;
    @FXML
    private TextField kField,bField,weightB,weightK,weightPos,weightTitle,weightBM,idfLower,idfDelta;
    @FXML
    private StringProperty termP;
    @FXML
    private IntegerProperty idP;
    @FXML
    private TableColumn<String,String> docName,docRank, termCol,tfCol,docNoCol,docRankDoc;
    @FXML
    private Button noSemantics,includeSlimSemantics,includeStanfordSemantics;


    //@FXML
   // private TableColumn<CorpusDocument,String>
    @FXML
    private ListView<String> listView;

    @FXML
    private TableView<String> tableView ;

    @FXML
    private ListView<String>  queryResultsView , entityView , cityList;

    public Controller(IView view , IModel model){
        this.view = view;
        this.model = model;
        currentAnswerList = new ArrayList<>();
        currentCitiesChosen = new HashSet<>();
    }

    public Controller(){
        currentAnswerList = new ArrayList<>();
        currentCitiesChosen = new HashSet<>();

    }

    private void fillCitiesList(){

        this.citiesList = FXCollections.observableArrayList();
        ReadFromWeb.getCities();
        Iterator iterator = ReadFromWeb.allCities.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry entry = (Map.Entry)iterator.next();
            if(((String)entry.getKey()).equals(((City)entry.getValue()).getName()))
                citiesList.add((String)entry.getKey());
        }
        cityList.setItems(this.citiesList);
    }

    private String getNameOfDocument(String line){

        if(line == "" || line == null || line.length() == 0)
            return "";

        String ans = "";
        char c ;
        int i = 0;

        while(i < line.length()){
            c = line.charAt(i++);
            if(c == ' ')
                break;

            ans += c ;
        }

        return ans ;
    }

    /**
     * fill the filter list
     */
    public void setCityListListeners(){

        this.cityList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ObservableList<String> selectedCities = cityList.getSelectionModel().getSelectedItems();

                for (String selected:
                     selectedCities) {
                    ErrorBox box = new ErrorBox();
                    box.getErrorBoxStage("Filtering by : " + selected);
                    currentCitiesChosen.add(selected);
                }
            }
        });

    }

    /**
     * clear the filter list
     */
    public void handleClearFilter(){
        this.currentCitiesChosen = new HashSet<>();
        this.view.errorMessage("City filter reseted");
    }

    private void setQueryResultsListener(){
        this.queryResultsView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String docName = getNameOfDocument(newValue);

                if(docName == "")
                    return;

                CorpusDocument currentDoc = null;
                int index = 1 ;

                for (CorpusDocument doc:
                     currentAnswerList) {
                    if(docName.equals(doc.getName()))
                        currentDoc = doc ;
                }

                ObservableList<String> entList = FXCollections.observableArrayList();
                for (String entity:
                     currentDoc.getEntities()) {
                    entList.add(entity + "   " + index++);
                }

                entityView.setItems(entList);
            }
        });
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
        fillCitiesList();
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
            fillCitiesList();
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

    /**
     * sometimes the language tag of docs contains garbase like numbers .
     * this function filters those cases
     * @param langs original language list
     * @return
     */
    private ArrayList<String> filterGarbageLangs(TreeSet<String> langs){

        ArrayList<String> ans = new ArrayList<>();
        char c = 0;
        boolean stop = false;

        for (String lang:
             langs) {
            for(int i = 0 ; i < lang.length() && !stop ; i++) {
                c = lang.charAt(i);
                if ((c < 65 || c > 90) && (c < 97 || c > 122)) {
                    stop = true;
                    continue;
                }
            }//end for
            if(!stop)
                ans.add(lang);
            stop = false ;
        }

        return ans;
    }

    @FXML
    public void handleDisplayLangs(){
        ObservableList<String> langList = FXCollections.observableArrayList();
        langList.addAll(filterGarbageLangs(this.model.getDocsLang()));
        if(langList == null || langList.size() == 0)
            this.view.errorMessage("No language list to show");

        this.listView.setItems(langList);
        this.view.errorMessage("showing language list");
    }

    public void handleRunQuery(){
        String query = queryField.getText();
        int index = 0;
        ErrorBox box = new ErrorBox();


        if(query.equals(null) || query.equals("")) {
            box.getErrorBoxStage("Must fill a query first");
            return;

        }

        try {
        setQueryResultsListener();
        ArrayList<CorpusDocument> answer = this.model.runQueryOnEngine(new Query("1",query,""),this.stemmerCheckBox.isSelected(),currentCitiesChosen);
        currentAnswerList = answer;
        ObservableList<String> docList = FXCollections.observableArrayList();
        TreeMap<Integer , String> ansMap = new TreeMap<>();


        for (CorpusDocument document:
             answer) {

            ansMap.put(index++,document.toString());
        }
        docList.addAll(ansMap.values());

            this.queryResultsView.setItems(docList);
            box.getErrorBoxStage("Ranking done");
        }catch (Exception e){
            box.getErrorBoxStage("load or run first");
        }

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

        ErrorBox box = new ErrorBox();
        String f1 =queryFilePathField.getText();
        String f2 = resultsFilePathField.getText();

        if(f1 == null || f2 == null || f1.equals("") || f2.equals("")) {
            box.getErrorBoxStage("Must fill source and target first");
            return;

        }
        try {
            this.model.createResultFileForQueries(queryFilePathField.getText() + "\\queries.txt", resultsFilePathField.getText(), this.stemmerCheckBox.isSelected(), this.currentCitiesChosen);
            box.getErrorBoxStage("Ranking done");

        }catch (Exception e){
            if(e instanceof IOException)
                box.getErrorBoxStage("couldnt find queries file");
            else
                box.getErrorBoxStage("load or run first");
        }

    }

    @FXML
    public void handleNoSemantic(){

        SemanticHandler.clearWordsVecs();
        SemanticHandler.includeSemantics = false;


    }

    @FXML
    public void HandleSlimSemantics(){
        SemanticHandler.gloveFile = "SlimWikiGlove200D.txt";
        SemanticHandler.includeSemantics = true;
        if (this.corpusPathField.getText().equals("")) {
            this.view.errorMessage("must specify target path first");
            return;
        }

        if(SemanticHandler.corpusPath == null)
            SemanticHandler.corpusPath = corpusPathField.getText();
        if(SemanticHandler.wordsVectors == null || SemanticHandler.wordsVectors.size() ==0)
            SemanticHandler.readGloveFile();
    }

    @FXML
    public void HandleStanfordSemantics(){
        SemanticHandler.gloveFile = "Stanford_glove.6B.50d.txt";
        SemanticHandler.includeSemantics = true;
        if(SemanticHandler.corpusPath == null)
            SemanticHandler.corpusPath = corpusPathField.getText();
        if(SemanticHandler.wordsVectors == null || SemanticHandler.wordsVectors.size() ==0)
            SemanticHandler.readGloveFile();
    }

    public void setView(IView view){
        this.view = view;
    }

    public void setModel(IModel model){
        this.model = model;
    }
}
