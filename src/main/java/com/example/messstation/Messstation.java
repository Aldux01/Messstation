package com.example.messstation;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONObject;

public class Messstation extends Stage {

  private static final Color RED = Color.rgb(255, 0, 0);
  private static final Color ORANGE = Color.rgb(255, 104, 0);
  private static final Color YELLOW = Color.rgb(255, 255, 0);
  private static final Color GREEN = Color.rgb(100, 200, 50);
  private static final Color CADET_GREY = Color.rgb(57, 200, 200);
  private static final Color LIGHT_BLUE = Color.rgb(0, 100, 255);
  private static final Color DARK_BLUE = Color.rgb(0, 0, 180);
  private Timeline timeline;
  private FlowPane pane;
  private Gauge ozon;
  private Gauge schwefeldioxid;
  private Gauge kohlenmonoxid;
  private Gauge stickstoffdioxid;
  private Gauge temperatur;
  private Gauge feinstaub;
  private final Path settings;
  private boolean temperaturEnabled;
  private boolean ozonEnabled;
  private boolean feinstaubEnabled;
  private boolean kohlenmonoxidEnabled;
  private boolean stickstoffdioxidEnabled;
  private boolean schwefeldioxidEnabled;

  public Messstation(Path settingsPath) throws IOException {
    this.settings = Objects.requireNonNullElseGet(settingsPath,
      () -> Paths.get("settings/default_settings.json"));
    init();
    setScene(new Scene(pane));
  }

  private void init() throws IOException {
    setBooleans(settings);
    timeline = new Timeline();
    timeline.getKeyFrames()
      .add(new KeyFrame(Duration.seconds(5), (ActionEvent ae) -> generateValues()));
    timeline.setCycleCount(Animation.INDEFINITE);
    final GaugeBuilder builder = GaugeBuilder.create().skinType(Gauge.SkinType.DASHBOARD);
    //
    pane = new FlowPane();
    pane.setAlignment(Pos.CENTER);
    pane.setPadding(new Insets(20));
    pane.setHgap(20);
    pane.setVgap(20);
    pane.setPrefWidth(470);
    pane.setBackground(new Background(
      new BackgroundFill(Color.rgb(210, 210, 210), CornerRadii.EMPTY, Insets.EMPTY)));
    if (temperaturEnabled) {
      temperatur = builder.decimals(0).maxValue(50).unit("°C").build();
      final VBox temperaturBox = getTopicBox("Temperatur", GREEN, temperatur);
      pane.getChildren().add(temperaturBox);
    }
    if (ozonEnabled) {
      ozon = builder.decimals(0).maxValue(241).unit("µm/m³").build();
      final VBox ozonBox = getTopicBox("Ozon (O³)", LIGHT_BLUE, ozon);
      pane.getChildren().add(ozonBox);
    }
    if (feinstaubEnabled) {
      feinstaub = builder.decimals(0).maxValue(100).unit("µm/m³").build();
      final VBox feinstaubBox = getTopicBox("Feinstaub (PM10)", DARK_BLUE, feinstaub);
      pane.getChildren().add(feinstaubBox);
    }
    if (kohlenmonoxidEnabled) {
      kohlenmonoxid = builder.decimals(1).maxValue(30).unit("µm/m³").build();
      final VBox kohlenmonoxidBox = getTopicBox("Kohlenmonoxid (CO)", YELLOW, kohlenmonoxid);
      pane.getChildren().add(kohlenmonoxidBox);
    }
    if (stickstoffdioxidEnabled) {
      stickstoffdioxid = builder.decimals(0).maxValue(500).unit("µm/m³").build();
      final VBox stickstoffdioxidBox = getTopicBox("Stickstoffdioxid (NO²)", RED, stickstoffdioxid);
      pane.getChildren().add(stickstoffdioxidBox);
    }
    if (schwefeldioxidEnabled) {
      schwefeldioxid = builder.decimals(1).maxValue(3.0).unit("µm/m³").build();
      final VBox schwefeldioxidBox = getTopicBox("Schwefeldioxid (SO²)", ORANGE, schwefeldioxid);
      pane.getChildren().add(schwefeldioxidBox);
    }
    generateValues();
  }

  public void play() {
    timeline.play();
  }

  private void generateValues() {
    if (temperaturEnabled) {
      int temperaturValue = ThreadLocalRandom.current().nextInt(0, 50 + 1);
      setTemperatur(temperaturValue);
    }
    if (ozonEnabled) {
      int ozonValue = ThreadLocalRandom.current().nextInt(0, 241 + 1);
      setOzon(ozonValue);
    }
    if (feinstaubEnabled) {
      int feinstaubValue = ThreadLocalRandom.current().nextInt(0, 100 + 1);
      setFeinstaub(feinstaubValue);
    }
    if (kohlenmonoxidEnabled) {
      double kohlenmonoxidValue = ThreadLocalRandom.current().nextDouble(0, 30 + 0.1);
      setKohlenmonoxid(kohlenmonoxidValue);
    }
    if (stickstoffdioxidEnabled) {
      int stickstoffValue = ThreadLocalRandom.current().nextInt(0, 500 + 1);
      setStickstoffdioxid(stickstoffValue);
    }
    if (schwefeldioxidEnabled) {
      double schwefeldioxidValue = ThreadLocalRandom.current().nextDouble(0, 3 + 0.1);
      setSchwefeldioxid(schwefeldioxidValue);
    }
  }

  private void setTemperatur(int temperaturValue) {
    temperatur.setValue(temperaturValue);
    if (temperaturValue <= 10) {
      temperatur.setBarColor(DARK_BLUE);
    } else if (temperaturValue <= 20) {
      temperatur.setBarColor(LIGHT_BLUE);
    } else if (temperaturValue <= 25) {
      temperatur.setBarColor(CADET_GREY);
    } else if (temperaturValue <= 35) {
      temperatur.setBarColor(YELLOW);
    } else if (temperaturValue <= 40) {
      temperatur.setBarColor(ORANGE);
    } else {
      temperatur.setBarColor(RED);
    }
  }

  private void setOzon(int ozonValue) {
    ozon.setValue(ozonValue);
    if (ozonValue <= 32) {
      ozon.setBarColor(DARK_BLUE);
    } else if (ozonValue <= 64) {
      ozon.setBarColor(LIGHT_BLUE);
    } else if (ozonValue <= 119) {
      ozon.setBarColor(CADET_GREY);
    } else if (ozonValue <= 180) {
      ozon.setBarColor(YELLOW);
    } else if (ozonValue <= 240) {
      ozon.setBarColor(ORANGE);
    } else {
      ozon.setBarColor(RED);
    }
  }

  private void setFeinstaub(int feinstaubValue) {
    feinstaub.setValue(feinstaubValue);
    if (feinstaubValue <= 9) {
      feinstaub.setBarColor(DARK_BLUE);
    } else if (feinstaubValue <= 19) {
      feinstaub.setBarColor(LIGHT_BLUE);
    } else if (feinstaubValue <= 34) {
      feinstaub.setBarColor(CADET_GREY);
    } else if (feinstaubValue <= 50) {
      feinstaub.setBarColor(YELLOW);
    } else if (feinstaubValue <= 99) {
      feinstaub.setBarColor(ORANGE);
    } else {
      feinstaub.setBarColor(RED);
    }
  }

  private void setKohlenmonoxid(double kohlenmonoxidValue) {
    kohlenmonoxid.setValue(kohlenmonoxidValue);
    if (kohlenmonoxidValue <= 0.9) {
      kohlenmonoxid.setBarColor(DARK_BLUE);
    } else if (kohlenmonoxidValue <= 1.9) {
      kohlenmonoxid.setBarColor(LIGHT_BLUE);
    } else if (kohlenmonoxidValue <= 3.9) {
      kohlenmonoxid.setBarColor(CADET_GREY);
    } else if (kohlenmonoxidValue <= 10.9) {
      kohlenmonoxid.setBarColor(YELLOW);
    } else if (kohlenmonoxidValue <= 29.9) {
      kohlenmonoxid.setBarColor(ORANGE);
    } else {
      kohlenmonoxid.setBarColor(RED);
    }
  }

  private void setStickstoffdioxid(int stickstoffValue) {
    stickstoffdioxid.setValue(stickstoffValue);
    if (stickstoffValue <= 24) {
      stickstoffdioxid.setBarColor(DARK_BLUE);
    } else if (stickstoffValue <= 49) {
      stickstoffdioxid.setBarColor(LIGHT_BLUE);
    } else if (stickstoffValue <= 99) {
      stickstoffdioxid.setBarColor(CADET_GREY);
    } else if (stickstoffValue <= 200) {
      stickstoffdioxid.setBarColor(YELLOW);
    } else if (stickstoffValue <= 499) {
      stickstoffdioxid.setBarColor(ORANGE);
    } else {
      stickstoffdioxid.setBarColor(RED);
    }
  }

  private void setSchwefeldioxid(double schwefeldioxidValue) {
    schwefeldioxid.setValue(schwefeldioxidValue);
    if (schwefeldioxidValue <= 0.1) {
      schwefeldioxid.setBarColor(DARK_BLUE);
    } else if (schwefeldioxidValue <= 0.2) {
      schwefeldioxid.setBarColor(LIGHT_BLUE);
    } else if (schwefeldioxidValue <= 0.5) {
      schwefeldioxid.setBarColor(CADET_GREY);
    } else if (schwefeldioxidValue <= 1.0) {
      schwefeldioxid.setBarColor(YELLOW);
    } else if (schwefeldioxidValue <= 1.3) {
      schwefeldioxid.setBarColor(ORANGE);
    } else {
      schwefeldioxid.setBarColor(RED);
    }
  }

  private VBox getTopicBox(final String text, final Color color, final Gauge gauge) {
    final Rectangle bar = new Rectangle(200, 3);
    bar.setArcWidth(6);
    bar.setArcHeight(6);
    bar.setFill(Color.BLACK);
    final Label label = new Label(text);
    label.setTextFill(Color.BLACK);
    label.setAlignment(Pos.CENTER);
    label.setPadding(new Insets(0, 0, 10, 0));
    gauge.setBarColor(color);
    gauge.setBarBackgroundColor(Color.rgb(39, 44, 50));
    gauge.setAnimated(true);
    final VBox vBox = new VBox(bar, label, gauge);
    vBox.setSpacing(3);
    vBox.setAlignment(Pos.CENTER);
    return vBox;
  }

  private void setBooleans(Path settingsPath) throws IOException {
    final String body = new String(Files.readAllBytes(settingsPath));
    final JSONObject jsonObject = new JSONObject(body);
    temperaturEnabled = Boolean.parseBoolean((String) jsonObject.get("temperaturEnabled"));
    ozonEnabled = Boolean.parseBoolean((String) jsonObject.get("ozonEnabled"));
    feinstaubEnabled = Boolean.parseBoolean((String) jsonObject.get("feinstaubEnabled"));
    kohlenmonoxidEnabled = Boolean.parseBoolean((String) jsonObject.get("kohlenmonoxidEnabled"));
    stickstoffdioxidEnabled = Boolean.parseBoolean(
      (String) jsonObject.get("stickstoffdioxidEnabled"));
    schwefeldioxidEnabled = Boolean.parseBoolean((String) jsonObject.get("schwefeldioxidEnabled"));
  }
}
