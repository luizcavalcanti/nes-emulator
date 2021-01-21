package nesemulator.cpu;

import java.util.HashMap;

public enum Opcode {

    BRK(0x00),
    ORA(0x09),
    BPL(0x10),
    JSR(0x20),
    BMI(0x30),
    PHA(0x48),
    JMP_ABSOLUTE(0x4C),
    SEI(0x78),
    STY_ZERO_PAGE(0x84),
    STA_ZERO_PAGE(0x85),
    DEY(0x88),
    TXA(0x8A),
    STA_ABSOLUTE(0x8D),
    BCC(0x90),
    STA_INDIRECT_Y(0x91),
    STA_ZERO_PAGE_X(0x95),
    TYA(0x98),
    TXS(0x9A),
    LDY_IMMEDIATE(0xA0),
    LDX_IMMEDIATE(0xA2),
    LDA_ZERO_PAGE(0xA5),
    LDA_IMMEDIATE(0xA9),
    LDA_ABSOLUTE(0xAD),
    LDA_ABSOLUTE_X(0xBD),
    CPY_IMMEDIATE(0xC0),
    DEX(0xCA),
    BNE(0xD0),
    CLD(0xD8),
    CMP_ABSOLUTE_X(0xDD),
    INX(0xE8);


    private static final HashMap<Integer, Opcode> enumMap = new HashMap<>();

    static {
        for (Opcode op : Opcode.values()) {
            enumMap.put(op.value, op);
        }
    }

    private final int value;

    Opcode(int value) {
        this.value = value;
    }

    public static Opcode fromCode(int opcode) {
        return enumMap.get(opcode);
    }
}
