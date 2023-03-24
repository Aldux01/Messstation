package com.example.messstation;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

public class Messstation extends Stage {
    private static final String KEY_TEMPERATUR = "temperaturEnabled";
    private static final String KEY_OZON = "ozonEnabled";
    private static final String KEY_FEINSTAUB = "feinstaubEnabled";
    private static final String KEY_KOHLENMONOXID = "kohlenmonoxidEnabled";
    private static final String KEY_STICKSTOFFOXID = "stickstoffdioxidEnabled";
    private static final String KEY_SCHWEFELDIOXID = "schwefeldioxidEnabled";
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

    /*
     * when creating a new Messstation the path of the settings file is
     * needed to initialize all booleans to display only wanted gauges
     */
    public Messstation(Path settingsPath) throws IOException {
        this.settings = Objects.requireNonNullElseGet(settingsPath,
                () -> Paths.get("settings/default_settings.json"));
        init();
        setScene(new Scene(pane));
    }

    /*
     * Initializes the Messstation-instance:
     * gauge booleans are set with the values from the settings file,
     * a new TimeLine object is initialized (our timer to update all gauges after a certain amount of time),
     * a new FlowPane object is initialized, this is the object where all activated gauges are inserted in,
     * random value for gauges are generated.
     */
    private void init() throws IOException {
        // Boolean variables are initialized with values from the settings file
        setBooleans(settings);
        // TimeLine is used as a timer. Every 5 seconds the generateValues() method is called and new values are set to the gauges.
        timeline = new Timeline();
        timeline.getKeyFrames()
                .add(new KeyFrame(Duration.seconds(5), (ActionEvent ae) -> generateValues()));
        // timeline runs till the application is stopped.
        timeline.setCycleCount(Animation.INDEFINITE);
        // General pane settings
        pane = new FlowPane();
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(20));
        pane.setHgap(20);
        pane.setVgap(20);
        pane.setPrefWidth(470);
        pane.setBackground(new Background(
                new BackgroundFill(Color.rgb(210, 210, 210), CornerRadii.EMPTY, Insets.EMPTY)));
        // Initializing all gauges that are set as active in the settings file and adding them into pane.
        if (temperaturEnabled) {
            temperatur = buildGauge(0, 50, "°C");
            final var temperaturBox = getTopicBox("Temperatur", Color.GREEN, temperatur);
            pane.getChildren().add(temperaturBox);
        }
        if (ozonEnabled) {
            ozon = buildGauge(0, 241, "µm/m³");
            final var ozonBox = getTopicBox("Ozon (O³)", Color.LIGHTBLUE, ozon);
            pane.getChildren().add(ozonBox);
        }
        if (feinstaubEnabled) {
            feinstaub = buildGauge(0, 200, "µm/m³");
            final var feinstaubBox = getTopicBox("Feinstaub (PM10)", Color.DARKBLUE, feinstaub);
            pane.getChildren().add(feinstaubBox);
        }
        if (kohlenmonoxidEnabled) {
            kohlenmonoxid = buildGauge(1, 30, "µm/m³");
            final var kohlenmonoxidBox = getTopicBox("Kohlenmonoxid (CO)", Color.YELLOW, kohlenmonoxid);
            pane.getChildren().add(kohlenmonoxidBox);
        }
        if (stickstoffdioxidEnabled) {
            stickstoffdioxid = buildGauge(0, 500, "µm/m³");
            final var stickstoffdioxidBox = getTopicBox("Stickstoffdioxid (NO²)", Color.RED, stickstoffdioxid);
            pane.getChildren().add(stickstoffdioxidBox);
        }
        if (schwefeldioxidEnabled) {
            schwefeldioxid = buildGauge(1, 3.0, "µm/m³");
            final var schwefeldioxidBox = getTopicBox("Schwefeldioxid (SO²)", Color.ORANGE, schwefeldioxid);
            pane.getChildren().add(schwefeldioxidBox);
        }
        // Initial values for gauges are generated.
        generateInitialValues();
    }

    private Gauge buildGauge(int decimals, double maxValue, String unit) {
        final var builder = GaugeBuilder.create().skinType(Gauge.SkinType.DASHBOARD);

        return builder.decimals(decimals).maxValue(maxValue).unit(unit).build();
    }

    /*
     * This method calls the TimeLine.play() method.
     * It's Used to start the timer to update the values of the gauges.
     */

    public void play() {
        timeline.play();
    }

    private void generateInitialValues() {
        final var random = ThreadLocalRandom.current();

        if (temperaturEnabled) {
            modifyTemperatur(random.nextInt(0, 50 + 1));
        }
        if (ozonEnabled) {
            modifyOzon(random.nextInt(0, 241 + 1));
        }
        if (feinstaubEnabled) {
            modifyFeinstaub(random.nextInt(0, 100 + 1));
        }
        if (kohlenmonoxidEnabled) {
            modifyKohlenmonoxid(random.nextDouble(0, 30 + 0.1));
        }
        if (stickstoffdioxidEnabled) {
            modifyStickstoffdioxid(random.nextInt(0, 500 + 1));
        }
        if (schwefeldioxidEnabled) {
            modifySchwefeldioxid(random.nextDouble(0, 3 + 0.1));
        }
    }

    /*
     * Method to generate random values for all gauges.
     */
    private void generateValues() {
        if (temperaturEnabled) {
            modifyTemperatur((int) getRandomOffsetValue(temperatur.getValue(), 3, 50));
        }
        if (ozonEnabled) {
            modifyOzon((int) getRandomOffsetValue(ozon.getValue(), 6, 241));
        }
        if (feinstaubEnabled) {
            modifyFeinstaub((int) getRandomOffsetValue(feinstaub.getValue(), 3, 100));
        }
        if (kohlenmonoxidEnabled) {
            modifyKohlenmonoxid(getRandomOffsetValue(kohlenmonoxid.getValue(), 1.5, 30));
        }
        if (stickstoffdioxidEnabled) {
            modifyStickstoffdioxid((int) getRandomOffsetValue(stickstoffdioxid.getValue(), 1, 500));
        }
        if (schwefeldioxidEnabled) {
            modifySchwefeldioxid(getRandomOffsetValue(schwefeldioxid.getValue(), 0.3, 3));
        }
    }

    private double getRandomOffsetValue(double initialValue, double maxOffset, int maxValue) {
        return ThreadLocalRandom.current().nextDouble(
                Math.max(0, initialValue - maxOffset),
                Math.min(maxValue + 0.1, initialValue + maxOffset)
        );
    }

    /*
     * The following methods are gauge-specific and are used to set the generated value
     * to each gauge and change its colors based on in which range the value is in.
     */
    private void modifyTemperatur(int value) {
        temperatur.setValue(value);
        if (value <= 10) {
            temperatur.setBarColor(Color.DARKBLUE);
        } else if (value <= 20) {
            temperatur.setBarColor(Color.LIGHTBLUE);
        } else if (value <= 25) {
            temperatur.setBarColor(Color.TURQUOISE);
        } else if (value <= 35) {
            temperatur.setBarColor(Color.YELLOW);
        } else if (value <= 40) {
            temperatur.setBarColor(Color.ORANGE);
        } else {
            temperatur.setBarColor(Color.RED);
        }
    }

    private void modifyOzon(int value) {
        ozon.setValue(value);
        if (value <= 32) {
            ozon.setBarColor(Color.DARKBLUE);
        } else if (value <= 64) {
            ozon.setBarColor(Color.LIGHTBLUE);
        } else if (value <= 119) {
            ozon.setBarColor(Color.TURQUOISE);
        } else if (value <= 180) {
            ozon.setBarColor(Color.YELLOW);
        } else if (value <= 240) {
            ozon.setBarColor(Color.ORANGE);
        } else {
            ozon.setBarColor(Color.RED);
        }
    }

    private void modifyFeinstaub(int value) {
        feinstaub.setValue(value);
        if (value <= 9) {
            feinstaub.setBarColor(Color.DARKBLUE);
        } else if (value <= 19) {
            feinstaub.setBarColor(Color.LIGHTBLUE);
        } else if (value <= 34) {
            feinstaub.setBarColor(Color.TURQUOISE);
        } else if (value <= 50) {
            feinstaub.setBarColor(Color.YELLOW);
        } else if (value <= 99) {
            feinstaub.setBarColor(Color.ORANGE);
        } else {
            feinstaub.setBarColor(Color.RED);
        }
    }

    private void modifyKohlenmonoxid(double value) {
        kohlenmonoxid.setValue(value);
        if (value <= 0.9) {
            kohlenmonoxid.setBarColor(Color.DARKBLUE);
        } else if (value <= 1.9) {
            kohlenmonoxid.setBarColor(Color.LIGHTBLUE);
        } else if (value <= 3.9) {
            kohlenmonoxid.setBarColor(Color.TURQUOISE);
        } else if (value <= 10.9) {
            kohlenmonoxid.setBarColor(Color.YELLOW);
        } else if (value <= 29.9) {
            kohlenmonoxid.setBarColor(Color.ORANGE);
        } else {
            kohlenmonoxid.setBarColor(Color.RED);
        }
    }

    private void modifyStickstoffdioxid(int value) {
        stickstoffdioxid.setValue(value);
        if (value <= 24) {
            stickstoffdioxid.setBarColor(Color.DARKBLUE);
        } else if (value <= 49) {
            stickstoffdioxid.setBarColor(Color.LIGHTBLUE);
        } else if (value <= 99) {
            stickstoffdioxid.setBarColor(Color.TURQUOISE);
        } else if (value <= 200) {
            stickstoffdioxid.setBarColor(Color.YELLOW);
        } else if (value <= 499) {
            stickstoffdioxid.setBarColor(Color.ORANGE);
        } else {
            stickstoffdioxid.setBarColor(Color.RED);
        }
    }

    private void modifySchwefeldioxid(double value) {
        schwefeldioxid.setValue(value);
        if (value <= 0.1) {
            schwefeldioxid.setBarColor(Color.DARKBLUE);
        } else if (value <= 0.2) {
            schwefeldioxid.setBarColor(Color.LIGHTBLUE);
        } else if (value <= 0.5) {
            schwefeldioxid.setBarColor(Color.TURQUOISE);
        } else if (value <= 1.0) {
            schwefeldioxid.setBarColor(Color.YELLOW);
        } else if (value <= 1.3) {
            schwefeldioxid.setBarColor(Color.ORANGE);
        } else {
            schwefeldioxid.setBarColor(Color.RED);
        }
    }

    /*
     * Creates a VBox for each gauge with a label to know which gauge shows which value.
     */
    private VBox getTopicBox(final String text, final Color color, final Gauge gauge) {
        final var bar = new Rectangle(200, 3);
        bar.setArcWidth(6);
        bar.setArcHeight(6);
        bar.setFill(Color.BLACK);

        final var label = new Label(text);
        label.setTextFill(Color.BLACK);
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(0, 0, 10, 0));
        gauge.setBarColor(color);
        gauge.setBarBackgroundColor(Color.rgb(39, 44, 50));
        gauge.setAnimated(true);

        final var vBox = new VBox(bar, label, gauge);
        vBox.setSpacing(3);
        vBox.setAlignment(Pos.CENTER);

        return vBox;
    }

    /*
     * This method sets the boolean values from the settings file into the respective variable.
     * First it is checked whether the settings file is a JSON or XML file. If it's neither it throws an exception telling the user
     * that the file must be a JSON or XML format.
     * If it's a valid JSON it is checked if all needed keys are present in the file and are written correctly, if it's not the case
     * an exception is thrown to inform the user to correct the settings file.
     */
    private void setBooleans(Path settingsPath) throws IOException {
        // reads the content of the settings file and saves it into a string
        final var body = new String(Files.readAllBytes(settingsPath));
        // checking if string is JSON format
        if (isJSONValid(body)) {
            final var jsonObject = new JSONObject(body);
            // checking if JSONObject contain all keys
            if (isValid(jsonObject)) {
                temperaturEnabled = Boolean.parseBoolean((String) jsonObject.get(KEY_TEMPERATUR));
                ozonEnabled = Boolean.parseBoolean((String) jsonObject.get(KEY_OZON));
                feinstaubEnabled = Boolean.parseBoolean((String) jsonObject.get(KEY_FEINSTAUB));
                kohlenmonoxidEnabled = Boolean.parseBoolean((String) jsonObject.get(KEY_KOHLENMONOXID));
                stickstoffdioxidEnabled = Boolean.parseBoolean((String) jsonObject.get(KEY_STICKSTOFFOXID));
                schwefeldioxidEnabled = Boolean.parseBoolean((String) jsonObject.get(KEY_SCHWEFELDIOXID));

                return;
            }

            throw new IOException("Keys are missing or are invalid in the configuration file!");
        }
        // If it's not a JSON document maybe it's XML.
        try {
            final var dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // If parsing is successful we have an XML document
            final var doc = dBuilder.parse(
                    new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
            temperaturEnabled = Boolean.parseBoolean(
                    doc.getElementsByTagName(KEY_TEMPERATUR).item(0).getTextContent());
            ozonEnabled = Boolean.parseBoolean(
                    doc.getElementsByTagName(KEY_OZON).item(0).getTextContent());
            feinstaubEnabled = Boolean.parseBoolean(
                    doc.getElementsByTagName(KEY_FEINSTAUB).item(0).getTextContent());
            kohlenmonoxidEnabled = Boolean.parseBoolean(
                    doc.getElementsByTagName(KEY_KOHLENMONOXID).item(0).getTextContent());
            stickstoffdioxidEnabled = Boolean.parseBoolean(
                    doc.getElementsByTagName(KEY_STICKSTOFFOXID).item(0).getTextContent());
            schwefeldioxidEnabled = Boolean.parseBoolean(
                    doc.getElementsByTagName(KEY_SCHWEFELDIOXID).item(0).getTextContent());
        } catch (ParserConfigurationException | IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            // if parsing is not successful we have neither JSON nor XML files, and we throw an exception.
            throw new IOException("File type must be JSON or XML!");
        }
    }

    /*
     * Checks if a given String is a valid JSON string.
     */
    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    /*
     * Checks if a given JSONObject contains all needed keys.
     */
    private boolean isValid(JSONObject jsonObject) {
        return jsonObject.has(KEY_TEMPERATUR) && jsonObject.has(KEY_OZON) && jsonObject.has(
                KEY_FEINSTAUB) && jsonObject.has(KEY_KOHLENMONOXID) && jsonObject.has(
                KEY_STICKSTOFFOXID) && jsonObject.has(KEY_SCHWEFELDIOXID);
    }
}
