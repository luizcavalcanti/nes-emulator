package nesemulator.cpu;

import java.util.HashMap;

public enum Opcode {

    BRK(0x00, "BRK", AddressingMode.IMPLIED),
    ORA_INDIRECT_X(0x01, "ORA", AddressingMode.INDIRECT_X),
    ORA_ZERO_PAGE(0x05, "ORA", AddressingMode.ZERO_PAGE),
    ORA_IMMEDIATE(0x09, "ORA", AddressingMode.IMMEDIATE),
    ASL_ACCUMULATOR(0x0A, "ASL", AddressingMode.ACCUMULATOR),
    BPL(0x10, "BPL", AddressingMode.RELATIVE),
    CLC(0x18, "CLC", AddressingMode.IMPLIED),
    JSR(0x20, "JSR", AddressingMode.ABSOLUTE),
    ROL_ZERO_PAGE(0x26, "ROL", AddressingMode.ZERO_PAGE),
    AND_IMMEDIATE(0x29, "AND", AddressingMode.IMMEDIATE),
    BMI(0x30, "BMI", AddressingMode.RELATIVE),
    RTI(0x40, "RTI", AddressingMode.IMPLIED),
    EOR_INDIRECT_X(0x41, "EOR", AddressingMode.INDIRECT_X),
    PHA(0x48, "PHA", AddressingMode.IMPLIED),
    EOR_IMMEDIATE(0x49, "EOR", AddressingMode.IMMEDIATE),
    LSR_ACCUMULATOR(0x4A, "LSR", AddressingMode.ACCUMULATOR),
    JMP_ABSOLUTE(0x4C, "JMP", AddressingMode.ABSOLUTE),
    RTS(0x60, "RTS", AddressingMode.IMPLIED),
    PLA(0x68, "PLA", AddressingMode.IMPLIED),
    ADC_IMMEDIATE(0x69, "ADC", AddressingMode.IMMEDIATE),
    JMP_INDIRECT(0x6C, "JMP", AddressingMode.INDIRECT),
    SEI(0x78, "SEI", AddressingMode.IMPLIED),
    STY_ZERO_PAGE(0x84, "STY", AddressingMode.ZERO_PAGE),
    STA_ZERO_PAGE(0x85, "STA", AddressingMode.ZERO_PAGE),
    STX_ZERO_PAGE(0x86, "STX", AddressingMode.ZERO_PAGE),
    DEY(0x88, "DEY", AddressingMode.IMPLIED),
    TXA(0x8A, "TXA", AddressingMode.IMPLIED),
    STY_ABSOLUTE(0x8C, "STY", AddressingMode.ABSOLUTE),
    STA_ABSOLUTE(0x8D, "STA", AddressingMode.ABSOLUTE),
    STX_ABSOLUTE(0x8E, "STX", AddressingMode.ABSOLUTE),
    BCC(0x90, "BCC", AddressingMode.RELATIVE),
    STA_INDIRECT_Y(0x91, "STA", AddressingMode.INDIRECT_Y),
    STA_ZERO_PAGE_X(0x95, "STA", AddressingMode.ZERO_PAGE_X),
    STA_ABSOLUTE_X(0x9D, "STA", AddressingMode.ABSOLUTE_X),
    TYA(0x98, "TYA", AddressingMode.IMPLIED),
    STA_ABSOLUTE_Y(0x99, "STA", AddressingMode.ABSOLUTE_Y),
    TXS(0x9A, "TXS", AddressingMode.IMPLIED),
    TAY(0xA8, "TAY", AddressingMode.IMPLIED),
    LDY_IMMEDIATE(0xA0, "LDY", AddressingMode.IMMEDIATE),
    LDX_IMMEDIATE(0xA2, "LDX", AddressingMode.IMMEDIATE),
    LDY_ZERO_PAGE(0xA4, "LDY", AddressingMode.ZERO_PAGE),
    LDA_ZERO_PAGE(0xA5, "LDA", AddressingMode.ZERO_PAGE),
    LDX_ZERO_PAGE(0xA6, "LDX", AddressingMode.ZERO_PAGE),
    LDA_IMMEDIATE(0xA9, "LDA", AddressingMode.IMMEDIATE),
    TAX(0xAA, "TAX", AddressingMode.IMPLIED),
    LDA_ABSOLUTE(0xAD, "LDA", AddressingMode.ABSOLUTE),
    BCS(0xB0, "BCS", AddressingMode.RELATIVE),
    LDA_INDIRECT_Y(0xB1, "LDA", AddressingMode.INDIRECT_Y),
    LDA_ABSOLUTE_Y(0xB9, "LDA", AddressingMode.ABSOLUTE_Y),
    LDY_ABSOLUTE_X(0xBC, "LDY", AddressingMode.ABSOLUTE_X),
    LDA_ABSOLUTE_X(0xBD, "LDA", AddressingMode.ABSOLUTE_X),
    CPY_IMMEDIATE(0xC0, "CPY", AddressingMode.IMMEDIATE),
    CPY_ZERO_PAGE(0xC4, "CPY", AddressingMode.ZERO_PAGE),
    CMP_ZERO_PAGE(0xC5, "CMP", AddressingMode.ZERO_PAGE),
    DEC_ZERO_PAGE(0xC6, "DEC", AddressingMode.ZERO_PAGE),
    INY(0xC8, "INY", AddressingMode.IMPLIED),
    CMP_IMMEDIATE(0xC9, "CMP", AddressingMode.IMMEDIATE),
    DEX(0xCA, "DEX", AddressingMode.IMPLIED),
    DEC_ABSOLUTE(0xCE, "DEC", AddressingMode.ABSOLUTE),
    BNE(0xD0, "BNE", AddressingMode.RELATIVE),
    CLD(0xD8, "CLD", AddressingMode.IMPLIED),
    CMP_ABSOLUTE_X(0xDD, "CMP", AddressingMode.ABSOLUTE_X),
    CPX_ZERO_PAGE(0xE4, "CPX", AddressingMode.ZERO_PAGE),
    SBC_ZERO_PAGE(0xE5, "SBC", AddressingMode.ZERO_PAGE),
    INC_ZERO_PAGE(0xE6, "INC", AddressingMode.ZERO_PAGE),
    INX(0xE8, "INX", AddressingMode.IMPLIED),
    INC_ABSOLUTE(0xEE, "INC", AddressingMode.ABSOLUTE),
    BEQ(0xF0, "BEQ", AddressingMode.RELATIVE),
    INC_ABSOLUTE_X(0xFE, "INC", AddressingMode.ABSOLUTE_X);


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
