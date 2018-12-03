package GUIMain;

import Controller.Controller;
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
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getClassLoader().getResource("mainWindow.fxml").openStream());
        primaryStage.setTitle("Engine");
        primaryStage.setScene(new Scene(root, 600, 500));

        Controller controller = loader.getController();
        View view = new View(controller);
        Model model = new Model(controller);

        controller.setView(view);
        controller.setModel(model);

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


}
