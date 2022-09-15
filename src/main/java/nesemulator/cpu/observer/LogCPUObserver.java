package nesemulator.cpu.observer;

import nesemulator.cpu.CPUFormatter;
import nesemulator.cpu.Opcode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogCPUObserver implements CPUObserver {

    static final Logger logger = LoggerFactory.getLogger(LogCPUObserver.class);

    @Override
    public void notifyCPUInstruction(int programCount, Opcode opcode, int cycles, int... operands) {
        String formattedOperands = CPUFormatter.getFormattedOperands(opcode.getAddressingMode(), operands);
        logger.info(String.format("%04X: %s%s (%d cycles)", programCount, opcode.getName(), formattedOperands, cycles));
    }
}
