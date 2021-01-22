package nesemulator.cpu;

import java.util.HashMap;

public enum Opcode {

    BRK(0x00, "BRK", AddressingMode.Implied),
    ORA_IMMEDIATE(0x09, "ORA", AddressingMode.Immediate),
    BPL(0x10, "BPL", AddressingMode.Relative),
    JSR(0x20, "JSR", AddressingMode.Absolute),
    BMI(0x30, "BMI", AddressingMode.Relative),
    PHA(0x48, "PHA", AddressingMode.Implied),
    JMP_ABSOLUTE(0x4C, "JMP", AddressingMode.Absolute),
    SEI(0x78, "SEI", AddressingMode.Implied),
    STY_ZERO_PAGE(0x84, "STY", AddressingMode.ZeroPage),
    STA_ZERO_PAGE(0x85, "STA", AddressingMode.ZeroPage),
    STX_ZERO_PAGE(0x86, "STX", AddressingMode.ZeroPage),
    DEY(0x88, "DEY", AddressingMode.Implied),
    TXA(0x8A, "TXA", AddressingMode.Implied),
    STY_ABSOLUTE(0x8C, "STY", AddressingMode.Absolute),
    STA_ABSOLUTE(0x8D, "STA", AddressingMode.Absolute),
    BCC(0x90, "BCC", AddressingMode.Relative),
    STA_INDIRECT_Y(0x91, "STA", AddressingMode.IndirectY),
    STA_ZERO_PAGE_X(0x95, "STA", AddressingMode.ZeroPageX),
    TYA(0x98, "TYA", AddressingMode.Implied),
    STA_ABSOLUTE_Y(0x99, "STA", AddressingMode.AbsoluteY),
    TXS(0x9A, "TXS", AddressingMode.Implied),
    TAY(0xA8, "TAY", AddressingMode.Implied),
    LDY_IMMEDIATE(0xA0, "LDY", AddressingMode.Immediate),
    LDX_IMMEDIATE(0xA2, "LDX", AddressingMode.Immediate),
    LDA_ZERO_PAGE(0xA5, "LDA", AddressingMode.ZeroPage),
    LDA_IMMEDIATE(0xA9,"LDA",AddressingMode.Immediate),
    LDA_ABSOLUTE(0xAD,"LDA",AddressingMode.Absolute),
    LDA_ABSOLUTE_X(0xBD,"LDA", AddressingMode.AbsoluteX),
    CPY_IMMEDIATE(0xC0, "CPY", AddressingMode.Immediate),
    DEX(0xCA, "DEX", AddressingMode.Implied),
    DEC_ZERO_PAGE(0xC6, "DEC", AddressingMode.ZeroPage),
    BNE(0xD0,"BNE", AddressingMode.Relative),
    CLD(0xD8, "CLD", AddressingMode.Implied),
    CMP_ABSOLUTE_X(0xDD, "CMP", AddressingMode.AbsoluteX),
    INX(0xE8, "INX", AddressingMode.Implied),
    INY(0xC8, "INY", AddressingMode.Implied),
    RTS(0x60, "RTS", AddressingMode.Implied),
    BEQ(0xF0, "BEQ", AddressingMode.Relative),
    CMP_IMMEDIATE(0xC9, "CMP", AddressingMode.Immediate),
    INC_ABSOLUTE(0xEE, "INC", AddressingMode.Absolute);


    private static final HashMap<Integer, Opcode> enumMap = new HashMap<>();

    static {
        for (Opcode op : Opcode.values()) {
            enumMap.put(op.value, op);
        }
    }

    private final int value;
    private final String name;
    private final AddressingMode addressingMode;

    Opcode(int value, String name, AddressingMode addressingMode) {
        this.value = value;
        this.name = name;
        this.addressingMode = addressingMode;
    }

    public static Opcode fromCode(int opcode) {
        return enumMap.get(opcode);
    }

    public String getName() {
        return name;
    }

    public AddressingMode getAddressingMode() {
        return addressingMode;
    }
}
