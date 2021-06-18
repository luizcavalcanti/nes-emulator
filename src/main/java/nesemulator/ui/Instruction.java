package nesemulator.ui;

import nesemulator.cpu.Opcode;

class Instruction {
    private final int address;
    private final Opcode opcode;
    private final int[] args;
    private boolean current;

    public Instruction(int address, Opcode opcode, int[] args) {
        this.address = address;
        this.opcode = opcode;
        this.args = args;
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

    public int[] getArgs() {
        return args;
    }

    public boolean isCurrent() {
        return current;
    }

}
