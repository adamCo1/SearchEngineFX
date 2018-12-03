package Controller;

import Model.IModel;
import View.IView;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.swing.plaf.PanelUI;
import java.io.File;

public class Controller {

    private IView view ;
    private IModel model;

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private CheckBox stemmerCheckBox;
    @FXML
    private TextField corpusPathField , targetPathField;

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
