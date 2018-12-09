package View;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ErrorBox {

    public ErrorBox(){

    }

    public void getErrorBoxStage(String errorMsg){

        AnchorPane pane = new AnchorPane();
        Scene scene = new Scene(pane,550,200);
        Stage stage = new Stage();
        Button button_close = new Button("Close");
        Label label = new Label(errorMsg);
        button_close.setLayoutX(120);
        button_close.setLayoutY(150);
        label.setLayoutX(60);
        label.setLayoutY(50);
        button_close.setPrefWidth(150);
        button_close.setPrefHeight(40);

        button_close.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stage.close();
            }
        });
        pane.getChildren().addAll(button_close,label);
        stage.setTitle("Result");
        stage.setResizable(false);
        stage.setScene(scene);

        stage.showAndWait();
    }
}