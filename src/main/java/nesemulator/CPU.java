package nesemulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CPU {

    static final int STATUS_FLAG_CARRY = 0;
    static final int STATUS_FLAG_ZERO = 1;
    static final int STATUS_FLAG_INTERRUPT = 2;
    static final int STATUS_FLAG_DECIMAL = 3;
    static final int STATUS_FLAG_BREAK = 4;
    static final int STATUS_FLAG_ALWAYS_ONE = 5;
    static final int STATUS_FLAG_OVERFLOW = 6;
    static final int STATUS_FLAG_NEGATIVE = 7;

    private static final int OPCODE_BRK = 0x00;
    private static final int OPCODE_ORA = 0x09;
    private static final int OPCODE_BPL = 0x10;
    private static final int OPCODE_JSR = 0x20;
    private static final int OPCODE_BMI = 0x30;
    private static final int OPCODE_PHA = 0x48;
    private static final int OPCODE_JMP_ABSOLUTE = 0x4C;
    private static final int OPCODE_SEI = 0x78;
    private static final int OPCODE_STY_ZERO_PAGE = 0x84;
    private static final int OPCODE_STA_ZERO_PAGE = 0x85;
    private static final int OPCODE_DEY = 0x88;
    private static final int OPCODE_TXA = 0x8A;
    private static final int OPCODE_STA_ABSOLUTE = 0x8D;
    private static final int OPCODE_BCC = 0x90;
    private static final int OPCODE_STA_INDIRECT_Y = 0x91;
    private static final int OPCODE_STA_ZERO_PAGE_X = 0x95;
    private static final int OPCODE_TYA = 0x98;
    private static final int OPCODE_TXS = 0x9A;
    private static final int OPCODE_LDY_IMMEDIATE = 0xA0;
    private static final int OPCODE_LDX_IMMEDIATE = 0xA2;
    private static final int OPCODE_LDA_ZERO_PAGE = 0xA5;
    private static final int OPCODE_LDA_IMMEDIATE = 0xA9;
    private static final int OPCODE_LDA_ABSOLUTE = 0xAD;
    private static final int OPCODE_LDA_ABSOLUTE_X = 0xBD;
    private static final int OPCODE_CPY_IMMEDIATE = 0xC0;
    private static final int OPCODE_DEX = 0xCA;
    private static final int OPCODE_BNE = 0xD0;
    private static final int OPCODE_CLD = 0xD8;
    private static final int OPCODE_CMP_ABSOLUTE_X = 0xDD;
    private static final int OPCODE_INX = 0xE8;

    private static final int INITIAL_PC = 0x8000;
    private static final int PROCESSOR_STATUS_IRQ_DISABLED = 0x34;
    private static final int INITIAL_STACK_POINTER = 0xFD;

    private static final Logger logger = LoggerFactory.getLogger(CPU.class);

    static int a;
    static int x;
    static int y;
    static int p;
    static int pc;
    static int s;

    private CPU() {
    }

    public static void initialize() {
        s = INITIAL_STACK_POINTER; // Stack pointer staring into the abyss
        pc = INITIAL_PC;
        a = x = y = p = 0x00; // Registers cleanup
    }

    public static void execute() {
        var opsCount = 0;

        var running = true;
        while (running) {
            opsCount++;
            int opcode = signedToUsignedByte(MMU.readAddress(pc));
            switch (opcode) {
                case OPCODE_BRK:
                    brk();
                    break;
                case OPCODE_BPL:
                    bpl();
                    break;
                case OPCODE_BMI:
                    bmi();
                    break;
                case OPCODE_JSR:
                    jsr();
                    break;
//                case OPCODE_PHA:
//                    pha();
//                    break;
                case OPCODE_JMP_ABSOLUTE:
                    jmpAbsolute();
                    break;
                case OPCODE_SEI:
                    sei();
                    break;
                case OPCODE_STY_ZERO_PAGE:
                    styZeroPage();
                    break;
                case OPCODE_STA_ZERO_PAGE:
                    staZeroPage();
                    break;
//                case OPCODE_DEY:
//                    dey();
//                    break;
//                case OPCODE_TXA:
//                    txa();
//                    break;
                case OPCODE_STA_ABSOLUTE:
                    staAbsolute();
                    break;
                case OPCODE_BCC:
                    bcc();
                    break;
//                case OPCODE_STA_INDIRECT_Y:
//                    staIndirectY();
//                    break;
                case OPCODE_STA_ZERO_PAGE_X:
                    staZeroPageX();
                    break;
//                case OPCODE_TYA:
//                    tya();
//                    break;
                case OPCODE_TXS:
                    txs();
                    break;
                case OPCODE_LDY_IMMEDIATE:
                    ldyImmediate();
                    break;
                case OPCODE_LDX_IMMEDIATE:
                    ldxImmediate();
                    break;
                case OPCODE_LDA_ZERO_PAGE:
                    ldaZeroPage();
                    break;
                case OPCODE_LDA_IMMEDIATE:
                    ldaImmediate();
                    break;
                case OPCODE_LDA_ABSOLUTE:
                    ldaAbsolute();
                    break;
                case OPCODE_LDA_ABSOLUTE_X:
                    ldaAbsoluteX();
                    break;
                case OPCODE_CPY_IMMEDIATE:
                    cpyImmediate();
                    break;
                case OPCODE_DEX:
                    dex();
                    break;
                case OPCODE_BNE:
                    bne();
                    break;
                case OPCODE_CLD:
                    cld();
                    break;
                case OPCODE_CMP_ABSOLUTE_X:
                    cmpAbsoluteX();
                    break;
                case OPCODE_INX:
                    inx();
                    break;
                default:
                    logger.info(String.format("%04X: OpCode $%02X not implemented", pc, opcode));
                    running = false;
            }
        }

        logger.info(String.format("Program ended after %d operations run", opsCount));
    }

    static boolean isStatusFlagSet(int flagIndex) {
        return (p & (1 << (flagIndex))) > 0;
    }

    static void setStatusFlag(int flagIndex) {
        p |= 1 << flagIndex;
    }

    static void unsetStatusFlag(int flagIndex) {
        p &= ~(1 << flagIndex);
    }

    static void bmi() {
        // Cycles: 2 (+1 if branch succeeds, +2 if to a new page)
        var cycles = 2;
        var offset = 2;
        if (isStatusFlagSet(STATUS_FLAG_NEGATIVE)) {
            cycles += 1;
            offset += MMU.readAddress(pc + 1);
        }
        logger.info(String.format("%04X: BMI (%d cycles)", pc, cycles));
        pc += offset;
    }

    static void bcc() {
        // Cycles: 2 (+1 if branch succeeds, +2 if to a new page)
        var cycles = 2;
        var offset = 2;
        if (!isStatusFlagSet(STATUS_FLAG_CARRY)) {
            cycles += 1;
            offset += MMU.readAddress(pc + 1);
        }
        logger.info(String.format("%04X: BCC (%d cycles)", pc, cycles));
        pc += offset;
    }

    static void bpl() {
        // Cycles: 2 (+1 if branch succeeds, +2 if to a new page)
        var cycles = 2;
        var offset = 2;
        if (!isStatusFlagSet(STATUS_FLAG_NEGATIVE)) {
            cycles += 1;
            offset += MMU.readAddress(pc + 1);
        }
        logger.info(String.format("%04X: BPL (%d cycles)", pc, cycles));
        pc += offset;
    }

    static void sei() {
        logger.info(String.format("%04X: SEI (2 cycles)", pc));

        setStatusFlag(STATUS_FLAG_INTERRUPT);
        pc += 1;
    }

    static void ldxImmediate() {
        int value = MMU.readAddress(pc + 1);
        logger.info(String.format("%04X: LDX #$%02X (2 cycles)", pc, value));

        CPU.x = value;
        setNonPositiveFlags(x);
        pc += 2;
    }

    static void jmpAbsolute() {
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        logger.info(String.format("%04X: JMP $%04X (3 cycles)", pc, address));

        pc = address;
    }

    private static void pha() {
        MMU.writeAddress(s, a);
        s--;

        logger.info(String.format("%04X: PHA (3 cycles)", pc));

        pc += 1;
    }

    private static void txa() {
        logger.info(String.format("%04X: TXA (2 cycles)", pc));

        a = x;
        pc += 1;
    }

    private static void tya() {
        logger.info(String.format("%04X: TYA (2 cycles)", pc));

        a = y;
        pc += 1;
    }

    static void ldaAbsolute() {
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        logger.info(String.format("%04X: LDA $%04X (4 cycles)", pc, address));

        a = MMU.readAddress(address);
        setNonPositiveFlags(a);
        pc += 3;
    }

    static void ldaAbsoluteX() {
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        logger.info(String.format("%04X: LDA $%04X, X (4 cycles)", pc, address));

        a = MMU.readAddress(address + x);
        setNonPositiveFlags(a);
        pc += 3;
    }

    static void ldaImmediate() {
        int value = MMU.readAddress(pc + 1);
        logger.info(String.format("%04X: LDA #$%02X (2 cycles)", pc, value));

        a = value;
        setNonPositiveFlags(a);
        pc += 2;
    }

    static void ldaZeroPage() {
        int address = MMU.readAddress(pc + 1);
        logger.info(String.format("%04X: LDA $%02X (2 cycles)", pc, address));

        a = MMU.readAddress(address);
        setNonPositiveFlags(a);
        pc += 2;
    }

    static void ldyImmediate() {
        int value = MMU.readAddress(pc + 1);
        logger.info(String.format("%04X: LDY #$%02X (2 cycles)", pc, value));

        y = value;
        setNonPositiveFlags(y);
        pc += 2;
    }

    static void staAbsolute() {
        int value = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        logger.info(String.format("%04X: STA $%04X (4 cycles)", pc, value));

        MMU.writeAddress(value, a);
        pc += 3;
    }

    static void staIndirectY() {
        int addressLSB = signedToUsignedByte(MMU.readAddress(pc + 1));
        logger.info(String.format("%04X: STA ($%04X), Y (5 cycles)", pc, addressLSB));
        //TODO add 1 cycle if page boundary is crossed

        MMU.writeAddress(addressLSB + y, a);
        pc += 2;
    }

    static void styZeroPage() {
        int value = signedToUsignedByte(MMU.readAddress(pc + 1));
        logger.info(String.format("%04X: STY $%X (3 cycles)", pc, value));

        MMU.writeAddress(value, y);
        pc += 2;
    }

    static void staZeroPage() {
        int value = signedToUsignedByte(MMU.readAddress(pc + 1));
        logger.info(String.format("%04X: STA $%02X (3 cycles)", pc, value));

        MMU.writeAddress(value, a);
        pc += 2;
    }

    static void staZeroPageX() {
        int address = signedToUsignedByte(MMU.readAddress(pc + 1));
        logger.info(String.format("%04X: STA $%02X, X (4 cycles)", pc, address));

        MMU.writeAddress(address + CPU.x, CPU.a);
        pc += 2;
    }

    static void jsr() {
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        logger.info(String.format("%04X: JSR $%04X (6 cycles)", pc, address));

        push2BytesToStack(pc + 3);

        pc = address;
    }

    private static void dey() {
        logger.info(String.format("%04X: DEY (2 cycles)", pc));

        y--;
        setNonPositiveFlags(y);

        pc += 1;
    }

    static void bne() {
        // Cycles: 2 (+1 if branch succeeds, +2 if to a new page)
        var cycles = 2;
        var offset = 2;
        if (!isStatusFlagSet(STATUS_FLAG_ZERO)) {
            cycles += 1;
            offset += MMU.readAddress(pc + 1);
        }
        logger.info(String.format("%04X: BNE (%d cycles)", pc, cycles));
        pc += offset;
    }

    static void cld() {
        logger.info(String.format("%04X: CLD (2 cycles)", pc));

        unsetStatusFlag(STATUS_FLAG_DECIMAL);
        pc += 1;
    }

    static void txs() {
        logger.info(String.format("%04X: TXS (2 cycles)", pc));

        s = x;
        pc += 1;
    }

    static void inx() {
        logger.info(String.format("%04X: INX (2 cycles)", pc));

        x++;
        setNonPositiveFlags(x);
        pc += 1;
    }

    static void dex() {
        logger.info(String.format("%04X: DEX (2 cycles)", pc));

        x--;
        setNonPositiveFlags(x);
        pc += 1;
    }

    static void cpyImmediate() {
        int value = MMU.readAddress(pc + 1);
        logger.info(String.format("%04X: CPY #$%X (2 cycles)", pc, value));

        var result = CPU.y - value;
        if (result > 0) {
            setStatusFlag(STATUS_FLAG_CARRY);
        } else if (result == 0) {
            setStatusFlag(STATUS_FLAG_CARRY);
            setStatusFlag(STATUS_FLAG_ZERO);
        } else {
            setStatusFlag(STATUS_FLAG_NEGATIVE);
        }

        pc += 2;
    }

    static void cmpAbsoluteX() {
        // TODO: add +1 to cycle if page is crossed
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        logger.info(String.format("%04X: CMP $%04X, X (4 cycles)", pc, address));

        var value = MMU.readAddress(address + x);

        var result = CPU.a - value;
        if (result > 0) {
            setStatusFlag(STATUS_FLAG_CARRY);
        } else if (result == 0) {
            setStatusFlag(STATUS_FLAG_CARRY);
            setStatusFlag(STATUS_FLAG_ZERO);
        } else {
            setStatusFlag(STATUS_FLAG_NEGATIVE);
        }

        pc += 3;
    }

    static void brk() {
        logger.info(String.format("%04X: BRK (7 cycles)", pc));

        setStatusFlag(STATUS_FLAG_BREAK);

        push2BytesToStack(pc);
        pushToStack(p);

        // TODO: understand what should happen here and name those constants
        int newPCAddress = littleEndianToInt(MMU.readAddress(0xFFFE), MMU.readAddress(0xFFFF));

        pc = newPCAddress;
    }

    static void oraImmediate() {
        int value = MMU.readAddress(pc + 1);

        logger.info(String.format("%04X: ORA #$%02X (2 cycles)", pc, value));

        a |= value;
        setNonPositiveFlags(a);
        pc += 2;
    }

    private static int signedToUsignedByte(int b) {
        return b & 0xFF;
    }

    private static int littleEndianToInt(int b1, int b2) {
        int i1 = b1 & 0xFF;
        int i2 = b2 & 0xFF;
        return (i2 << 8) | i1;
    }

    private static void setNonPositiveFlags(int value) {
        if (value == 0) {
            setStatusFlag(STATUS_FLAG_ZERO);
        } else {
            unsetStatusFlag(STATUS_FLAG_ZERO);
        }

        if (value < 0) {
            setStatusFlag(STATUS_FLAG_NEGATIVE);
        } else {
            unsetStatusFlag(STATUS_FLAG_NEGATIVE);
        }
    }

    private static void pushToStack(int value) {
        MMU.writeAddress(0x0100 + s, value);
        s--;
    }

    private static void push2BytesToStack(int value) {
        int high = ((value >> 8) & 0xFF);
        int low = (value & 0xFF);

        pushToStack(high);
        pushToStack(low);
    }

    private static int popFromStack() {
        s++;
        return MMU.readAddress(0x0100 + s);
    }
}
