package nesemulator;

public class CPU {

    public static final int OPCODE_BRK = 0x00;
    public static final int OPCODE_BPL = 0x10;
    public static final int OPCODE_JSR = 0x20;
    public static final int OPCODE_PHA = 0x48;
    public static final int OPCODE_JMP_ABSOLUTE = 0x4C;
    public static final int OPCODE_SEI = 0x78;
    public static final int OPCODE_STY_ZERO_PAGE = 0x84;
    public static final int OPCODE_DEY = 0x88;
    public static final int OPCODE_TXA = 0x8A;
    public static final int OPCODE_STA_ABSOLUTE = 0x8D;
    public static final int OPCODE_STA_INDIRECT_Y = 0x91;
    public static final int OPCODE_STA_ZERO_PAGE_X = 0x95;
    public static final int OPCODE_TYA = 0x98;
    public static final int OPCODE_TXS = 0x9A;
    public static final int OPCODE_LDY_IMMEDIATE = 0xA0;
    public static final int OPCODE_LDX_IMMEDIATE = 0xA2;
    public static final int OPCODE_LDA_IMMEDIATE = 0xA9;
    public static final int OPCODE_LDA_ABSOLUTE = 0xAD;
    public static final int OPCODE_LDA_ABSOLUTE_X = 0xBD;
    public static final int OPCODE_CPY_IMMEDIATE = 0xC0;
    public static final int OPCODE_DEX = 0xCA;
    public static final int OPCODE_BNE = 0xD0;
    public static final int OPCODE_CLD = 0xD8;
    public static final int OPCODE_CMP_ABSOLUTE_X = 0xDD;
    public static final int OPCODE_INX = 0xE8;

    public static int STATUS_FLAG_CARRY = 0;
    public static int STATUS_FLAG_ZERO = 1;
    public static int STATUS_FLAG_INTERRUPT = 2;
    public static int STATUS_FLAG_DECIMAL = 3;
    public static int STATUS_FLAG_BREAK = 4;
    public static int STATUS_FLAG_ALWAYS_ONE = 5;
    public static int STATUS_FLAG_OVERFLOW = 6;
    public static int STATUS_FLAG_NEGATIVE = 7;

    private static final int INITIAL_PC = 0x8000;
    private static final int PROCESSOR_STATUS_IRQ_DISABLED = 0x34;
    public static final int STACK_TOP_ADDRESS = 0x0200;

    static int a;
    static int x;
    static int y;
    static int p;
    static int pc;
    static int s;

    private CPU() {
    }

    public static void initialize() {
        s = STACK_TOP_ADDRESS; // Stack pointer staring into the abyss
        pc = 0x00;
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
//                case OPCODE_JSR:
//                    jsr();
//                    break;
//                case OPCODE_PHA:
//                    pha();
//                    break;
//                case OPCODE_JMP_ABSOLUTE:
//                    jmpAbsolute();
//                    break;
                case OPCODE_SEI:
                    sei();
                    break;
//                case OPCODE_STY_ZERO_PAGE:
//                    styZeroPage();
//                    break;
//                case OPCODE_DEY:
//                    dey();
//                    break;
//                case OPCODE_TXA:
//                    txa();
//                    break;
                case OPCODE_STA_ABSOLUTE:
                    staAbsolute();
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
//                case OPCODE_LDY_IMMEDIATE:
//                    ldyImmediate();
//                    break;
                case OPCODE_LDX_IMMEDIATE:
                    ldxImmediate();
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
                    System.out.printf("%06d: OpCode $%02X not implemented%n", pc, opcode);
                    running = false;
            }
        }

        System.out.printf("Program ended after %d operations run%n", opsCount);
    }

    static boolean getStatusFlag(int flagIndex) {
        return (p & (1 << (flagIndex))) > 0;
    }

    static void setStatusFlag(int flagIndex) {
        p |= 1 << flagIndex;
    }

    static void unsetStatusFlag(int flagIndex) {
        p &= ~(1 << flagIndex);
    }

    static void bpl() {
        // Cycles: 2 (+1 if branch succeeds, +2 if to a new page)
        var cycles = 2;
        var offset = 2;
        if (!getStatusFlag(STATUS_FLAG_NEGATIVE)) {
            cycles += 1;
            offset += MMU.readAddress(pc + 1);
        }
        System.out.printf("%06d: BPL (%d cycles)%n", pc, cycles);
        pc += offset;
    }

    static void sei() {
        System.out.printf("%06d: SEI (2 cycles)%n", pc);

        setStatusFlag(STATUS_FLAG_INTERRUPT);
        pc += 1;
    }

    static void ldxImmediate() {
        int value = MMU.readAddress(pc + 1);
        System.out.printf("%06d: LDX #$%X (2 cycles)%n", pc, value);

        CPU.x = value;
        setNonPositiveFlags(x);
        pc += 2;
    }

    private static void jmpAbsolute() {
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        System.out.printf("%06d: JMP $%04X (3 cycles)%n", pc, address);

        pc = address;
    }

    private static void pha() {
        MMU.writeAddress(s, a);
        s--;

        System.out.printf("%06d: PHA (3 cycles)%n", pc);

        pc += 1;
    }

    private static void txa() {
        System.out.printf("%06d: TXA (2 cycles)%n", pc);

        a = x;
        pc += 1;
    }

    private static void tya() {
        System.out.printf("%06d: TYA (2 cycles)%n", pc);

        a = y;
        pc += 1;
    }

    static void ldaAbsolute() {
        int value = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        System.out.printf("%06d: LDA $%04X (4 cycles)%n", pc, value);

        a = MMU.readAddress(value);
        setNonPositiveFlags(a);
        pc += 3;
    }

    static void ldaAbsoluteX() {
        int value = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        System.out.printf("%06d: LDA $%04X, X (4 cycles)%n", pc, value);

        a = MMU.readAddress(value + x);
        setNonPositiveFlags(a);
        pc += 3;
    }

    static void ldaImmediate() {
        int value = MMU.readAddress(pc + 1);
        System.out.printf("%06d: LDA #$%X (2 cycles)%n", pc, value);

        a = value;
        setNonPositiveFlags(a);
        pc += 2;
    }

    private static void ldyImmediate() {
        int value = signedToUsignedByte(MMU.readAddress(pc + 1));
        System.out.printf("%06d: LDY #$%X (2 cycles)%n", pc, value);

        y = value;
        setNonPositiveFlags(y);
        pc += 2;
    }

    static void staAbsolute() {
        int value = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        System.out.printf("%06d: STA $%04X (4 cycles)%n", pc, value);

        MMU.writeAddress(value, a);
        pc += 3;
    }

    private static void staIndirectY() {
        int addressStart = signedToUsignedByte(MMU.readAddress(pc + 1));
        int value = littleEndianToInt(MMU.readAddress(addressStart), MMU.readAddress(addressStart + 1));
        System.out.printf("%06d: STA ($%04X), Y (5 cycles)%n", pc, value);
        //TODO add 1 cycle if page boundary is crossed

        MMU.writeAddress(value + y, a);
        pc += 2;
    }

    private static void styZeroPage() {
        int value = signedToUsignedByte(MMU.readAddress(pc + 1));
        System.out.printf("%06d: STY $%X (3 cycles)%n", pc, value);

        MMU.writeAddress(value, y);
        pc += 2;
    }

    static void staZeroPageX() {
        int address = signedToUsignedByte(MMU.readAddress(pc + 1));
        System.out.printf("%06d: STA $%X, X (4 cycles)%n", pc, address);

        MMU.writeAddress(address + CPU.x, CPU.a);
        pc += 2;
    }

    private static void jsr() {
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        System.out.printf("%06d: JSR $%04X (6 cycles)%n", pc, address);
        // TODO: Push address-1 of the next operation to stack

        pc = address;
    }

    private static void dey() {
        System.out.printf("%06d: DEY (2 cycles)%n", pc);

        y--;
        setNonPositiveFlags(y);

        pc += 1;
    }

    static void bne() {
        // Cycles: 2 (+1 if branch succeeds, +2 if to a new page)
        var cycles = 2;
        var offset = 2;
        if (!getStatusFlag(STATUS_FLAG_ZERO)) {
            cycles += 1;
            offset += MMU.readAddress(pc + 1);
        }
        System.out.printf("%06d: BNE (%d cycles)%n", pc, cycles);
        pc += offset;
    }

    static void cld() {
        System.out.printf("%06d: CLD (2 cycles)%n", pc);

        unsetStatusFlag(STATUS_FLAG_DECIMAL);
        pc += 1;
    }

    static void txs() {
        System.out.printf("%06d: TXS (2 cycles)%n", pc);

        s = x;
        pc += 1;
    }

    static void inx() {
        System.out.printf("%06d: INX (2 cycles)%n", pc);

        x++;
        setNonPositiveFlags(x);
        pc += 1;
    }

    static void dex() {
        System.out.printf("%06d: DEX (2 cycles)%n", pc);

        x--;
        setNonPositiveFlags(x);
        pc += 1;
    }

    static void cpyImmediate() {
        int value = MMU.readAddress(pc + 1);
        System.out.printf("%06d: CPY #$%X (2 cycles)%n", pc, value);

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
        System.out.printf("%06d: CMP $%04X, X (4 cycles)%n", pc, address);

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
        System.out.printf("%06d: BRK (7 cycles)%n", pc);

        setStatusFlag(STATUS_FLAG_BREAK);

        int newPCAddress = littleEndianToInt(MMU.readAddress(0xFFFE), MMU.readAddress(0xFFFF));

        pc = newPCAddress;
    }

    private static int signedToUsignedByte(int b) {
        return b & 0xff;
    }

    private static int littleEndianToInt(int b1, int b2) {
        int i1 = b1 & 0xff;
        int i2 = b2 & 0xff;
        return (i2 << 8) | i1;
    }

    private static void setNonPositiveFlags(int value) {
        if (value == 0) {
            setStatusFlag(STATUS_FLAG_ZERO);
        } else if (value < 0) {
            setStatusFlag(STATUS_FLAG_NEGATIVE);
        }
    }
}
