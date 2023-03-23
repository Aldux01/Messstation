package com.example.messstation;

import java.io.IOException;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class Main extends Application {

  private final Messstation station1 = new Messstation(Paths.get("settings/messstation1.json"));
  private final Messstation station2 = new Messstation(Paths.get("settings/messstation2.json"));
  private final Messstation station3 = new Messstation(Paths.get("settings/messstation3.json"));

  public Main() throws IOException {
  }

  @Override
  public void start(Stage stage) {
    final Button buttonStation1 = new Button();
    buttonStation1.setText("Station 1");
    handleButton(stage, buttonStation1, station1);
    final Button buttonStation2 = new Button();
    buttonStation2.setText("Station 2");
    handleButton(stage, buttonStation2, station2);
    final Button buttonStation3 = new Button();
    buttonStation3.setText("Station 3");
    handleButton(stage, buttonStation3, station3);
    final FlowPane root = new FlowPane();
    root.getChildren().add(buttonStation1);
    root.getChildren().add(buttonStation2);
    root.getChildren().add(buttonStation3);
    root.setAlignment(Pos.CENTER);
    final Scene scene = new Scene(root, 450, 250);
    stage.setTitle("Dashboard main menu");
    stage.setScene(scene);
    stage.setOnCloseRequest((event -> {
      event.consume();
      try {
        stop();
      } catch (Exception ex) {
        // do nothing
      }
    }));
    stage.show();
  }

  private void handleButton(Stage stage, Button button, Messstation station) {
    button.setOnAction(event -> {
      // New window (Stage)
      station.setTitle(button.getText());
      station.setScene(station.getScene());
      // Set position of second window, related to primary window.
      station.setX(stage.getX() + 200);
      station.setY(stage.getY() + 100);
      station.setOnCloseRequest(event1 -> button.setDisable(false));
      station.show();
      station.play();
      button.setDisable(true);
    });
  }

  @Override
  public void stop() {
    Platform.exit();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
