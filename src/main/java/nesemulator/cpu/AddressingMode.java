package nesemulator.cpu;

public enum AddressingMode {
    IMMEDIATE(1),
    ZERO_PAGE(1),
    ABSOLUTE(2),
    IMPLIED(0),
    ACCUMULATOR(0),
    INDEXED(0), //FIXME: not sure of length
    ZERO_PAGE_X(1),
    INDIRECT(2),
    PRE_INDEXED_INDIRECT(0), //FIXME: not sure of length
    INDIRECT_X(1),
    INDIRECT_Y(1),
    RELATIVE(1),
    ABSOLUTE_X(2),
    ABSOLUTE_Y(2);

    private final int length;

    AddressingMode(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }
}
