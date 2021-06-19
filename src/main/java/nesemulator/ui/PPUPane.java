package nesemulator.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import nesemulator.PPU;

public class PPUPane extends VBox {

    private final Label statusLabel;

    public PPUPane() {
        statusLabel = new Label("Status: --------");
        statusLabel.getStyleClass().add("register-value");

        var statusHelp = new Label("        VSO-----");
        statusHelp.getStyleClass().addAll("register-value");

        this.getChildren().addAll(
                new Label("PPU State:"),
                statusLabel,
                statusHelp
        );
    }

    public void update() {
        statusLabel.setText(String.format("Status: %s", intToByteBinary(PPU.inspect(0x2002))));
    }

    private String intToByteBinary(int value) {
        return String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0');
    }

}
