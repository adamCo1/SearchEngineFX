package View;

import javafx.stage.Stage;

import java.util.TreeMap;

public interface IView {

    void errorMessage(String msg);
    void showDictionary(TreeMap dictionary);
    String showDirectoryBrowser(Stage stage);
}
