package Controller;

import Model.IModel;
import View.IView;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class Controller {

    private IView view ;
    private IModel model;

    @FXML
    private CheckBox stemmerCheckBox;
    @FXML
    private TextField corpusPathField , targetPathField;

    public Controller(IView view , IModel model){
        this.view = view;
        this.model = model;

    }

    public void handleShowDictionary(){

        this.view.showDictionary(this.model.getDictionary());
    }

    public void handleRunEngine(){

        boolean stemmerStatus = this.stemmerCheckBox.isSelected();
        String corpusPath = this.corpusPathField.getText() , targetPath = this.targetPathField.getText();


    }



    public void setView(IView view){
        this.view = view;
    }

    public void setModel(IModel model){
        this.model = model;
    }
}
