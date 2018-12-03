package Controller;

import Model.IModel;
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
    private TextField corpusPathField , targetPathField;

    @FXML
    private StringProperty termP;
    @FXML
    private IntegerProperty idP;
    @FXML
    private TableColumn<String,String> termCol,idCol,tfCol;

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
    }

    public void handleDisplayDicitionary(){
        TreeMap<String,Integer>d = model.getDictionary();
        if (d==null){
            return;
        }
        else {
            this.dictResult = FXCollections.observableArrayList();
            this.dictResult.addAll(d.keySet());
            System.out.println(d);
            termCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
            idCol.setCellValueFactory(cellData -> new SimpleStringProperty((d.get(cellData.getValue())).toString()));
            this.tableView.setItems(dictResult);
        }




    }

    public void handleBrowseButtonCorpusPath(){
        this.corpusPathField.setText(this.view.showDirectoryBrowser((Stage)this.anchorPane.getScene().getWindow()));
    }

    public void setView(IView view){
        this.view = view;
    }

    public void setModel(IModel model){
        this.model = model;
    }
}
