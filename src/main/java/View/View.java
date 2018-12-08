package View;

import Controller.Controller;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.TreeMap;

public class View implements IView {

    private Controller controller;

    public View(Controller controller){
        this.controller = controller;
    }


    @Override
    public void errorMessage(String msg) {

        ErrorBox errorBox = new ErrorBox();
        errorBox.getErrorBoxStage(msg);

    }

    @Override
    public void showDictionary(TreeMap dictionary) {

        //show it and stuff
        if(dictionary == null){
            ErrorBox box = new ErrorBox();
            box.getErrorBoxStage("Could'nt get dictionary");
            return;
        }
        this.controller.handleDisplayDicitionary();
    }

    @Override
    public String showDirectoryBrowser(Stage stage) {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(stage);

        if(file != null)
            return file.getPath();

        return null;
    }
}
