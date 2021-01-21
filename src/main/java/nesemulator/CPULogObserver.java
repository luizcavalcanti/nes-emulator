package nesemulator;

import nesemulator.cpu.CPUObserver;
import nesemulator.cpu.Opcode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CPULogObserver implements CPUObserver {

    static final Logger logger = LoggerFactory.getLogger(CPULogObserver.class);

    @Override
    public void notifyCPUInstruction(int programCount, Opcode opcode, int cycles, int... operands) {
        String formattedOperands = "";
        switch (opcode.getAddressingMode()) {
            case Implied:
                break;
            case Relative:
            case ZeroPage:
                formattedOperands = String.format(" $%02X", operands[0]);
                break;
            case Immediate:
                formattedOperands = String.format(" #$%02X", operands[0]);
                break;
            case Absolute:
                formattedOperands = String.format(" $%04X", operands[0]);
                break;
            case ZeroPageX:
                formattedOperands = String.format(" $%02X, X", operands[0]);
                break;
            case AbsoluteX:
                formattedOperands = String.format(" $%04X, X", operands[0]);
                break;
            case IndirectY:
                formattedOperands = String.format(" ($%04X), Y", operands[0]);
                break;
            default:
                throw new UnsupportedOperationException("Please write a log handler for this type of addressing mode: " + opcode.getAddressingMode().name());
        }
        logger.info(String.format("%04X: %s%s (%d cycles)", programCount, opcode.getName(), formattedOperands, cycles));
    }
}
