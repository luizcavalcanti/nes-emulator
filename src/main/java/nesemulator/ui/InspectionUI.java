package nesemulator.ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import nesemulator.Cart;
import nesemulator.MMU;
import nesemulator.PPU;
import nesemulator.cpu.CPU;
import nesemulator.cpu.CPUObserver;
import nesemulator.cpu.Opcode;

import java.io.IOException;

public class InspectionUI extends Application implements CPUObserver {

    public static final int REGISTER_FONT_SIZE = 24;
    public static final Font REGISTER_FONT = Font.font("Monospace", REGISTER_FONT_SIZE);
    public static final int LABELS_FONT_SIZE = 14;
    private ListView<String> logListView;
    private Label aLabel;
    private Label xLabel;
    private Label yLabel;
    private Label pLabel;
    private Label pcLabel;
    private Label sLabel;
    private Label ppuStatusLabel;
    private ListView<String> stackListView;
    private TextField instructionCountField;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("NES Emulator - Inpector");
        stage.setScene(buildScene());
        stage.show();
        runStuff();
        updateRegisters();
    }

    private Scene buildScene() {
        var windowPane = new BorderPane();
        windowPane.setTop(buildControlsPane());
        windowPane.setLeft(buildInstructionsPane());
        windowPane.setCenter(buildRegistersPane());
        windowPane.setRight(buildStackPane());
        return new Scene(windowPane, 800, 600); //, 300, 275
    }

    private Pane buildControlsPane() {
        var executeButton = new Button("Execute");
        var instructionsLabel = new Label("Instructions:");
        instructionsLabel.setFont(Font.font(LABELS_FONT_SIZE));
        instructionCountField = new TextField("1");
        instructionCountField.setMaxWidth(60);
        executeButton.setOnAction(this::handleNextXInstructionsAction);

        var controlsPane = new FlowPane();
        controlsPane.setPadding(new Insets(10, 0, 10, 0));
        controlsPane.setHgap(20);
        controlsPane.setAlignment(Pos.CENTER);
        controlsPane.getChildren().add(instructionsLabel);
        controlsPane.getChildren().add(instructionCountField);
        controlsPane.getChildren().add(executeButton);
        return controlsPane;
    }

    private Pane buildRegistersPane() {
        var cpuRegistersLabel = new Label("CPU Registers:");
        cpuRegistersLabel.setFont(Font.font(LABELS_FONT_SIZE));

        var ppuRegistersLabel = new Label("PPU Registers:");
        ppuRegistersLabel.setFont(Font.font(LABELS_FONT_SIZE));

        aLabel = new Label("A:  -");
        aLabel.setFont(REGISTER_FONT);
        xLabel = new Label("X:  -");
        xLabel.setFont(REGISTER_FONT);
        yLabel = new Label("Y:  -");
        yLabel.setFont(REGISTER_FONT);
        pcLabel = new Label("PC: -");
        pcLabel.setFont(REGISTER_FONT);
        sLabel = new Label("S:  -");
        sLabel.setFont(REGISTER_FONT);
        pLabel = new Label("P:  -");
        pLabel.setFont(REGISTER_FONT);
        Label pHelpLabel = new Label("    NV-BDIZC");
        pHelpLabel.setFont(REGISTER_FONT);
        pHelpLabel.setPadding(new Insets(-20, 0, 0, 0));

        ppuStatusLabel = new Label("Status: -");
        ppuStatusLabel.setFont(REGISTER_FONT);
        Label ppuStatusHelpLabel = new Label("        VSO-----");
        ppuStatusHelpLabel.setFont(REGISTER_FONT);
        ppuStatusHelpLabel.setPadding(new Insets(-20, 0, 0, 0));

        var fieldsPane = new VBox();
        fieldsPane.setPadding(new Insets(5));
        fieldsPane.setMinWidth(200);
        fieldsPane.setSpacing(10);
        fieldsPane.getChildren().add(cpuRegistersLabel);
        fieldsPane.getChildren().add(pcLabel);
        fieldsPane.getChildren().add(aLabel);
        fieldsPane.getChildren().add(xLabel);
        fieldsPane.getChildren().add(yLabel);
        fieldsPane.getChildren().add(sLabel);
        fieldsPane.getChildren().add(pLabel);
        fieldsPane.getChildren().add(pHelpLabel);

        fieldsPane.getChildren().add(ppuRegistersLabel);
        fieldsPane.getChildren().add(ppuStatusLabel);
        fieldsPane.getChildren().add(ppuStatusHelpLabel);

        return fieldsPane;
    }

    private Pane buildStackPane() {
        stackListView = new ListView<>();
        var stackPane = new BorderPane();
        var stackLabel = new Label("Stack:");
        stackLabel.setFont(Font.font(LABELS_FONT_SIZE));
        stackPane.setTop(stackLabel);
        stackPane.setCenter(stackListView);
        return stackPane;
    }

    private Pane buildInstructionsPane() {
        logListView = new ListView<>();
        var instructionsPane = new BorderPane();
        var instructionsLabel = new Label("Instructions:");
        instructionsLabel.setFont(Font.font(LABELS_FONT_SIZE));
        instructionsPane.setTop(instructionsLabel);
        instructionsPane.setCenter(logListView);
        return instructionsPane;
    }

    private void runStuff() {
        try {
            String romFileName = "balloon.nes";
            Cart cart = Cart.fromROMFile(romFileName);
            MMU.loadCart(cart);
            CPU.addObserver(this);
            CPU.initialize();
            PPU.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleNextXInstructionsAction(ActionEvent event) {
        int instructionsCount = Integer.parseInt(instructionCountField.getText().trim());
        for (int i = 0; i < instructionsCount; i++) {
            CPU.executeStep();
        }
        updateRegisters();
        updateStack();
    }

    private void updateRegisters() {
        // CPU
        pcLabel.setText(String.format("PC: 0x%04X", CPU.getPC()));
        aLabel.setText(String.format("A:  0x%02X", CPU.getA() & 0xFF));
        xLabel.setText(String.format("X:  0x%02X", CPU.getX()));
        yLabel.setText(String.format("Y:  0x%02X", CPU.getY()));
        sLabel.setText(String.format("S:  0x%02X", CPU.getS()));
        pLabel.setText(String.format("P:  %s", intToByteBinary(CPU.getP())));

        // PPU
        ppuStatusLabel.setText(String.format("Status: %s", intToByteBinary(PPU.inspect(0x2002))));
    }

    private String intToByteBinary(int value) {
        return String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0');
    }

    private void updateStack() {
        var memoryAddress = 0x0100 + CPU.getS();
        stackListView.getItems().clear();
        for (int i = memoryAddress; i < 0x0200; i++) {
            stackListView.getItems().add(String.format("0x%02X", MMU.readAddress(i)));
        }
    }

    @Override
    public void notifyCPUInstruction(int programCount, Opcode opcode, int cycles, int... operands) {
        var formattedOperands = getFormattedOperands(opcode, operands);
        var labelText = String.format("%04X: %s%s", programCount, opcode.getName(), formattedOperands);
        logListView.getItems().add(labelText);
    }

    private String getFormattedOperands(Opcode opcode, int[] operands) {
        switch (opcode.getAddressingMode()) {
            case Implied:
                return "";
            case Relative:
            case ZeroPage:
                return String.format(" $%02X", operands[0] & 0xff);
            case Immediate:
                return String.format(" #$%02X", operands[0] & 0xff);
            case Absolute:
                return String.format(" $%04X", operands[0]);
            case ZeroPageX:
                return String.format(" $%02X, X", operands[0] & 0xff);
            case AbsoluteX:
                return String.format(" $%04X, X", operands[0]);
            case IndirectY:
                return String.format(" ($%04X), Y", operands[0]);
            default:
                throw new UnsupportedOperationException("Please write a log handler for this type of addressing mode: " + opcode.getAddressingMode().name());
        }
    }
}