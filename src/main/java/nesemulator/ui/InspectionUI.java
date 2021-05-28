package nesemulator.ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import nesemulator.Cart;
import nesemulator.MMU;
import nesemulator.PPU;
import nesemulator.cpu.CPU;
import nesemulator.cpu.Opcode;
import nesemulator.cpu.observer.CPUObserver;
import nesemulator.cpu.observer.LogCPUObserver;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class InspectionUI extends Application implements CPUObserver, Initializable {

    @FXML
    private ListView<String> cpuInstructionsList;
    @FXML
    private ListView<String> stackList;
    @FXML
    private TextField instructionCountField;

    @FXML
    private Label cyclesLabel;
    @FXML
    private Label aLabel;
    @FXML
    private Label xLabel;
    @FXML
    private Label yLabel;
    @FXML
    private Label pLabel;
    @FXML
    private Label pcLabel;
    @FXML
    private Label sLabel;
    @FXML
    private Label ppuStatusLabel;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/inspectionUI.fxml"));
        loader.setController(this);

        stage.setTitle("NES Emulator - Inpector");
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initStuff();
        updateRegisters();
    }

    private void initStuff() {
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

    @FXML
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
        cyclesLabel.setText(String.format("Cycles: %d", CPU.getCyclesCounter()));
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
        stackList.getItems().clear();
        for (int i = memoryAddress; i < 0x0200; i++) {
            stackList.getItems().add(String.format("0x%02X", MMU.readAddress(i)));
        }
    }

    @Override
    public void notifyCPUInstruction(int programCount, Opcode opcode, int cycles, int... operands) {
        var formattedOperands = LogCPUObserver.getFormattedOperands(opcode.getAddressingMode(), operands);
        var labelText = String.format("%04X: %s%s", programCount, opcode.getName(), formattedOperands);
        cpuInstructionsList.getItems().add(labelText);
    }
}