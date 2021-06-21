package nesemulator.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import nesemulator.PPU;

public class PPUPane extends VBox {

    private final Label statusLabel;
    private final Label controlLabel;
    private final Label maskLabel;

    public PPUPane() {
        statusLabel = new Label("Status: --------");
        statusLabel.getStyleClass().add("register-value");

        var statusHelp = new Label("        VSO-----");
        statusHelp.getStyleClass().addAll("register-value");

        controlLabel = new Label("Control: --------");
        controlLabel.getStyleClass().add("register-value");

        var controlHelp = new Label("         VPHBSINN");
        controlHelp.getStyleClass().addAll("register-value");

        maskLabel = new Label("Mask: --------");
        maskLabel.getStyleClass().add("register-value");

        var maskHelp = new Label("      BGRsbMmG");
        maskHelp.getStyleClass().addAll("register-value");

        this.getChildren().addAll(
                new Label("PPU State:"),
                controlLabel,
                controlHelp,
                maskLabel,
                maskHelp,
                statusLabel,
                statusHelp
        );
    }

    public void update() {
        controlLabel.setText(String.format("Control: %s", toBin(PPU.inspect(PPU.ADDRESS_PPUCTRL))));
        maskLabel.setText(String.format("Mask: %s", toBin(PPU.inspect(PPU.ADDRESS_PPUMASK))));
        statusLabel.setText(String.format("Status: %s", toBin(PPU.inspect(PPU.ADDRESS_PPUSTATUS))));
    }

    private String toBin(int value) {
        return String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0');
    }

}
