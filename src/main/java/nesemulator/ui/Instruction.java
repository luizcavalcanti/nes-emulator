package nesemulator.ui;

import nesemulator.cpu.Opcode;

class Instruction {
    private final int address;
    private final Opcode opcode;
    private final int[] operands;
    private boolean current;

    public Instruction(int address, Opcode opcode, int[] operands) {
        this.address = address;
        this.opcode = opcode;
        this.operands = operands;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public int getAddress() {
        return address;
    }

    public Opcode getOpcode() {
        return opcode;
    }

    public int[] getOperands() {
        return operands;
    }

    public boolean isCurrent() {
        return current;
    }

}
