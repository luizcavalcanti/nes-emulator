package nesemulator.ui;

import javafx.scene.control.ListCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import nesemulator.cpu.CPUFormatter;

class InstructionListCell extends ListCell<Instruction> {

    @Override
    protected void updateItem(Instruction item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null)
            return;

        HBox box = new HBox();

        Text lbl = new Text(instructionToString(item));
        lbl.setFont(Font.font(java.awt.Font.MONOSPACED, 18));

        if (item.isCurrent()) {
            box.setBackground(new Background(new BackgroundFill(Color.YELLOW, null, null)));
        }

        box.getChildren().add(lbl);
        setGraphic(box);
    }

    private String instructionToString(Instruction item) {
        return String.format(
                "%04X: [%02X]%s %s",
                item.getAddress(),
                item.getOpcode().getValue() & 0xFF,
                item.getOpcode().getName(),
                CPUFormatter.getFormattedOperands(item.getOpcode().getAddressingMode(), item.getOperands())
        );
    }

}
