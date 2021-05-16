package nesemulator.cpu.observer;

import nesemulator.cpu.Opcode;

public interface CPUObserver {
    void notifyCPUInstruction(int programCount, Opcode opcode, int cycles, int... operands);
}
