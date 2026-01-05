package systems.kinau.fishingbot.gui;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.Getter;
import systems.kinau.fishingbot.FishingBot;
import systems.kinau.fishingbot.event.Listener;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class GUIController implements Listener {

    @FXML private TextField commandlineTextField;
    @FXML private Button startStopButton;

    @Getter
    private final List<String> lastCommands;
    @Getter
    private int currLastCommandIndex;

    public GUIController() {
        this.lastCommands = new ArrayList<>();
    }

    public void startStop(Event e) {
        if (FishingBot.getInstance().getCurrentBot() == null) {
            startStopButton.setText(FishingBot.getI18n().t("ui-button-stop"));
            new Thread(() -> FishingBot.getInstance().startBot()).start();
        } else {
            startStopButton.setDisable(true);
            startStopButton.setText(FishingBot.getI18n().t("ui-button-start"));
            FishingBot.getInstance().stopBot(true);
        }
    }

    public void updateStartStop() {
        Platform.runLater(() -> {
            if (FishingBot.getInstance().getCurrentBot() == null) {
                startStopButton.setText(FishingBot.getI18n().t("ui-button-start"));
            } else {
                startStopButton.setDisable(true);
                startStopButton.setText(FishingBot.getI18n().t("ui-button-stop"));
            }
        });
    }

    public void enableStartStop() {
        Platform.runLater(() -> startStopButton.setDisable(false));
    }

    public void commandlineSend(Event e) {
        if (commandlineTextField.getText().isEmpty()) {
            return;
        }
        if (getLastCommands().isEmpty() || !getLastCommands().get(getLastCommands().size() - 1).equals(commandlineTextField.getText())) {
            getLastCommands().add(commandlineTextField.getText());
            if (getLastCommands().size() > 1000)
                getLastCommands().remove(0);
        }
        currLastCommandIndex = 0;
        runCommand(commandlineTextField.getText());
        commandlineTextField.setText("");
    }

    public void consoleKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.UP) {
            if (getLastCommands().size() > currLastCommandIndex) {
                currLastCommandIndex++;
                commandlineTextField.setText(getLastCommands().get(getLastCommands().size() - currLastCommandIndex));
                commandlineTextField.end();
            }
        } else if (e.getCode() == KeyCode.DOWN) {
            if (currLastCommandIndex > 0) {
                currLastCommandIndex--;
                if (currLastCommandIndex == 0)
                    commandlineTextField.setText("");
                else
                    commandlineTextField.setText(getLastCommands().get(getLastCommands().size() - currLastCommandIndex));
                commandlineTextField.end();
            }
        }
    }

    private void runCommand(String text) {
        if (FishingBot.getInstance().getCurrentBot() == null) return;
        FishingBot.getInstance().getCurrentBot().runCommand(text);
    }

    public void openCodeOfConduct(String codeOfConduct, java.util.function.Consumer<Boolean> acceptCallback, java.util.function.Consumer<Boolean> saveCallback) {
        Dialogs.showCodeOfConduct(startStopButton.getScene().getWindow() == null ? null : (javafx.stage.Stage) startStopButton.getScene().getWindow(), codeOfConduct, acceptCallback, saveCallback);
    }

    public static void openWebpage(String url) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException ignored) {
        }
    }
}
