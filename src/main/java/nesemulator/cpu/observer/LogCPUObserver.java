package nesemulator.cpu.observer;

import nesemulator.cpu.AddressingMode;
import nesemulator.cpu.Opcode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogCPUObserver implements CPUObserver {

    static final Logger logger = LoggerFactory.getLogger(LogCPUObserver.class);

    @Override
    public void notifyCPUInstruction(int programCount, Opcode opcode, int cycles, int... operands) {
        String formattedOperands = getFormattedOperands(opcode.getAddressingMode(), operands);
        logger.info(String.format("%04X: %s%s (%d cycles)", programCount, opcode.getName(), formattedOperands, cycles));
    }

    public static String getFormattedOperands(AddressingMode addressingMode, int[] operands) {
        switch (addressingMode) {
            case IMPLIED:
                return "";
            case RELATIVE:
            case ZERO_PAGE:
                return String.format(" $%02X", operands[0]);
            case IMMEDIATE:
                return String.format(" #$%02X", operands[0]);
            case ABSOLUTE:
                return String.format(" $%04X", operands[0]);
            case ZERO_PAGE_X:
                return String.format(" $%02X, X", operands[0]);
            case ABSOLUTE_X:
                return String.format(" $%04X, X", operands[0]);
            case ABSOLUTE_Y:
                return String.format(" $%04X, Y", operands[0]);
            case INDIRECT_Y:
                return String.format(" ($%04X), Y", operands[0]);
            default:
                throw new UnsupportedOperationException("Please write a log handler for this addressing mode: " + addressingMode.name());
        }
    }
}
