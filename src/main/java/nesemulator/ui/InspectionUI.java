package nesemulator.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import nesemulator.Cart;
import nesemulator.MMU;
import nesemulator.PPU;
import nesemulator.cpu.CPU;
import nesemulator.cpu.Opcode;
import nesemulator.cpu.observer.CPUObserver;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class InspectionUI extends Application implements CPUObserver, Initializable {

    private static String romFileName;

    @FXML
    private ListView<Instruction> cpuInstructionsList;
    @FXML
    private ListView<String> stackList;
    @FXML
    private ListView<String> zeroPageList;
    @FXML
    private TextField instructionCountField;
    @FXML
    private ImageView screenView;

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
    private PPUPane ppuPane;

    public static void main(String[] args) {
        processArgs(args);
        launch(args);
    }

    private static void processArgs(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("You must provide a .nes rom path in the arguments");
        }
        romFileName = args[0];
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/inspectionUI.fxml"));
        loader.setController(this);

        stage.setTitle("NES Emulator - Inpector");
        stage.getIcons().add(new Image("/icon.png"));
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cpuInstructionsList.setCellFactory(list -> new InstructionListCell());
        initStuff();
        dumpProgram();
        updateAll();
    }

    private void initStuff() {
        try {
            Cart cart = Cart.fromROMFile(romFileName);
            MMU.loadCart(cart);
            CPU.addObserver(this);
            CPU.initialize();
            PPU.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dumpProgram() {
        int[] prg = MMU.getPRGROMData();
        for (int i = 0; i < prg.length; i++) {
            int memoryAddress = i + 0x8000;
            int opcodeNumber = prg[i] & 0xFF;
            var opcode = Opcode.fromCode(opcodeNumber);
            int argsLength = opcode.getAddressingMode().getLength();

            int[] instructionArgs = new int[argsLength];
            for (int j = 0; j < argsLength; j++) {
                instructionArgs[j] = prg[i + j + 1];
            }

            Instruction instruction = new Instruction(memoryAddress, opcode, instructionArgs);
            cpuInstructionsList.getItems().add(instruction);

            i += argsLength;
        }
    }

    @FXML
    private void handleNextXInstructionsAction(ActionEvent event) {
        clearDump();

        int instructionsCount = Integer.parseInt(instructionCountField.getText().trim());
        for (int i = 0; i < instructionsCount; i++) {
            int cpuCycles = CPU.executeStep();
            PPU.executeStep(cpuCycles);
        }

        if (PPU.screen != null)
            screenView.setImage(SwingFXUtils.toFXImage(PPU.screen, null));

        updateAll();
    }

    private void updateAll() {
        updateRegisters();
        updateStack();
        updateZeroPage();
        updateDump();
    }

    private void clearDump() {
        Optional<Instruction> selected = cpuInstructionsList.getItems().stream().filter(Instruction::isCurrent).findFirst();
        selected.ifPresent(instruction -> instruction.setCurrent(false));
    }

    private void updateDump() {
        Optional<Instruction> current = cpuInstructionsList.getItems().stream()
                .filter(instruction -> instruction.getAddress() == CPU.getPC())
                .findFirst();

        current.ifPresent(instruction -> {
            instruction.setCurrent(true);
            Platform.runLater(() -> cpuInstructionsList.scrollTo(instruction));
        });

        cpuInstructionsList.refresh();
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
        ppuPane.update();
    }

    private String intToByteBinary(int value) {
        return String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0');
    }

    private void updateStack() {
        var items = stackList.getItems();
        var memoryAddress = 0x0100 + CPU.getS();
        items.clear();
        for (int i = memoryAddress; i < 0x0200; i++) {
            items.add(String.format("0x%02X", MMU.readAddress(i) & 0xFF));
        }
    }

    private void updateZeroPage() {
        var items = zeroPageList.getItems();
        items.clear();
        for (int i = 0; i < 0xFF; i++) {
            items.add(String.format("[%02X] 0x%02X", i & 0xFF, MMU.readAddress(i) & 0xFF));
        }
    }

    @Override
    public void notifyCPUInstruction(int programCount, Opcode opcode, int cycles, int... operands) {
//        var formattedOperands = LogCPUObserver.getFormattedOperands(opcode.getAddressingMode(), operands);
//        var labelText = String.format("%04X: %s%s", programCount, opcode.getName(), formattedOperands);
//        cpuInstructionsList.getItems().add(labelText);
    }
}

