package nesemulator.cpu;

public interface CPUObserver {
    void notifyCPUInstruction(int programCount, Opcode opcode, int cycles, int... operands);
}
