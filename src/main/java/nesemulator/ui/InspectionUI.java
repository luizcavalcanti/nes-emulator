package nesemulator.ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import nesemulator.Cart;
import nesemulator.MMU;
import nesemulator.cpu.CPU;
import nesemulator.cpu.CPUObserver;
import nesemulator.cpu.Opcode;

import java.io.IOException;

public class InspectionUI extends Application implements CPUObserver {

    private ListView logList;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("NES Emulator - Inpector");
        stage.setScene(buildScene());
        stage.show();
        runStuff();
    }

    private Scene buildScene() {
        // Controls
        var controlsPane = new FlowPane();
        controlsPane.setVgap(10);
        controlsPane.setHgap(10);
        controlsPane.setAlignment(Pos.CENTER);
        var nextInstructionButton = new Button("Next instruction");
        nextInstructionButton.setOnAction(this::handleNextInstructionAction);
        var refreshMemoryButton = new Button("Refresh memory");
        refreshMemoryButton.setOnAction(this::handleRefreshMemoryAction);
        controlsPane.getChildren().add(nextInstructionButton);
        controlsPane.getChildren().add(refreshMemoryButton);

        logList = new ListView();
        var windowPane = new BorderPane();
        windowPane.setTop(controlsPane);
        windowPane.setLeft(logList);
        return new Scene(windowPane); //, 300, 275
    }

    private void runStuff() {
        try {
            String romFileName = "balloon.nes";
            Cart cart = Cart.fromROMFile(romFileName);
            MMU.loadCart(cart);
            CPU.addObserver(this);
            CPU.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNextInstructionAction(ActionEvent event) {
        CPU.executeStep();
    }

    @FXML
    private void handleRefreshMemoryAction(ActionEvent event) {
//        CPU.executeStep();
    }

    @Override
    public void notifyCPUInstruction(int programCount, Opcode opcode, int cycles, int... operands) {
        var labelText = String.format("%04X: %s %s (%d cycles)", programCount, opcode.name(), operands, cycles);
        logList.getItems().add(labelText);
    }
}