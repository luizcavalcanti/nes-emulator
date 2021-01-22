package nesemulator.ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import nesemulator.Cart;
import nesemulator.MMU;
import nesemulator.cpu.CPU;
import nesemulator.cpu.CPUObserver;
import nesemulator.cpu.Opcode;

import java.io.IOException;

public class InspectionUI extends Application implements CPUObserver {

    public static final int REGISTER_FONT_SIZE = 30;
    public static final Font REGISTER_FONT = Font.font("Monospace", REGISTER_FONT_SIZE);
    private ListView<String> logList;
    private Label aLabel;
    private Label xLabel;
    private Label yLabel;
    private Label pLabel;
    private Label pcLabel;
    private Label sLabel;

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
        var nextInstructionButton = new Button("Next instruction");
        nextInstructionButton.setOnAction(this::handleNextInstructionAction);
        var controlsPane = new FlowPane();
        controlsPane.setPadding(new Insets(10, 0, 10, 0));
        controlsPane.setHgap(20);
        controlsPane.setAlignment(Pos.CENTER);
        controlsPane.getChildren().add(nextInstructionButton);

        // CPU Regsters
        aLabel = new Label("A: -");
        aLabel.setFont(REGISTER_FONT);
        xLabel = new Label("X: -");
        xLabel.setFont(REGISTER_FONT);
        yLabel = new Label("Y: -");
        yLabel.setFont(REGISTER_FONT);
        pcLabel = new Label("PC: -");
        pcLabel.setFont(REGISTER_FONT);
        sLabel = new Label("S: -");
        sLabel.setFont(REGISTER_FONT);
        pLabel = new Label("P: -");
        pLabel.setFont(REGISTER_FONT);
        Label pHelpLabel = new Label("   NV1BDIZC");
        pHelpLabel.setFont(REGISTER_FONT);
        pHelpLabel.setPadding(new Insets(-20,0,0,0));

        updateRegisters();

        var registersPane = new VBox();
        registersPane.setPadding(new Insets(5));
        registersPane.setMinWidth(200);
        registersPane.setSpacing(10);
        registersPane.getChildren().add(pcLabel);
        registersPane.getChildren().add(aLabel);
        registersPane.getChildren().add(xLabel);
        registersPane.getChildren().add(yLabel);
        registersPane.getChildren().add(sLabel);
        registersPane.getChildren().add(pLabel);
        registersPane.getChildren().add(pHelpLabel);

        logList = new ListView<>();
        var windowPane = new BorderPane();
        windowPane.setTop(controlsPane);
        windowPane.setLeft(logList);
        windowPane.setCenter(registersPane);
        windowPane.setPadding(new Insets(10, 0, 0, 0));
        return new Scene(windowPane, 800, 600); //, 300, 275
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

    private void handleNextInstructionAction(ActionEvent event) {
        CPU.executeStep();
        updateRegisters();
    }

    private void updateRegisters() {
        aLabel.setText(String.format("A: 0x%02X", CPU.getA() & 0xFF));
        xLabel.setText(String.format("X: 0x%02X", CPU.getX()));
        yLabel.setText(String.format("Y: 0x%02X", CPU.getY()));
        sLabel.setText(String.format("S: 0x%02X", CPU.getS()));
        pLabel.setText(String.format("P: %08d", Integer.parseInt(Integer.toBinaryString(CPU.getP()))));
        pcLabel.setText(String.format("PC: 0x%04X", CPU.getPC()));
    }

    @Override
    public void notifyCPUInstruction(int programCount, Opcode opcode, int cycles, int... operands) {
        var formattedOperands = getFormattedOperands(opcode, operands);
        var labelText = String.format("%04X: %s%s", programCount, opcode.getName(), formattedOperands);
        logList.getItems().add(labelText);
    }

    private String getFormattedOperands(Opcode opcode, int[] operands) {
        switch (opcode.getAddressingMode()) {
            case Implied:
                return "";
            case Relative:
            case ZeroPage:
                return String.format(" $%02X", operands[0]);
            case Immediate:
                return String.format(" #$%02X", operands[0]);
            case Absolute:
                return String.format(" $%04X", operands[0]);
            case ZeroPageX:
                return String.format(" $%02X, X", operands[0]);
            case AbsoluteX:
                return String.format(" $%04X, X", operands[0]);
            case IndirectY:
                return String.format(" ($%04X), Y", operands[0]);
            default:
                throw new UnsupportedOperationException("Please write a log handler for this type of addressing mode: " + opcode.getAddressingMode().name());
        }
    }
}