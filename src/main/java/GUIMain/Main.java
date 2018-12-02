package GUIMain;

import Model.Model;
import View.View;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("mainWindow.fxml"));
        primaryStage.setTitle("Engine");
        primaryStage.setScene(new Scene(root, 500, 500));

        View view = new View();
        Model model = new Model();

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
