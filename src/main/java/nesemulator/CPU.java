package nesemulator;

public class CPU {

    public static int STATUS_REGISTER_CARRY_FLAG = 0;
    public static int STATUS_REGISTER_ZERO_FLAG = 1;
    public static int STATUS_REGISTER_INTERRUPT_FLAG = 2;
    public static int STATUS_REGISTER_DECIMAL_FLAG = 3;
    public static int STATUS_REGISTER_BREAK_FLAG = 4;
    public static int STATUS_REGISTER_ALWAYS_ONE_FLAG = 5;
    public static int STATUS_REGISTER_OVERFLOW_FLAG = 6;
    public static int STATUS_REGISTER_NEGATIVE_FLAG = 7;

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
                case 0x00:
                    brk();
                    break;
                case 0x10:
                    bpl();
                    break;
//                case 0x20:
//                    jsr();
//                    break;
//                case 0x48:
//                    pha();
//                    break;
//                case 0x4C:
//                    jmpAbsolute();
//                    break;
                case 0x78:
                    sei();
                    break;
//                case 0x84:
//                    styZeroPage();
//                    break;
//                case 0x88:
//                    dey();
//                    break;
//                case 0x8A:
//                    txa();
//                    break;
                case 0x8D:
                    staAbsolute();
                    break;
//                case 0x91:
//                    staIndirectIndexed();
//                    break;
                case 0x95:
                    staZeroPageX();
                    break;
//                case 0x98:
//                    tya();
//                    break;
                case 0x9A:
                    txs();
                    break;
//                case 0xA0:
//                    ldyImmediate();
//                    break;
                case 0xA2:
                    ldxImmediate();
                    break;
                case 0xA9:
                    ldaImmediate();
                    break;
                case 0xAD:
                    ldaAbsolute();
                    break;
                case 0xBD:
                    ldaAbsoluteX();
                    break;
                case 0xC0:
                    cpyImmediate();
                    break;
                case 0xCA:
                    dex();
                    break;
                case 0xD0:
                    bne();
                    break;
                case 0xD8:
                    cld();
                    break;
                case 0xDD:
                    cmpAbsoluteX();
                    break;
                case 0xE8:
                    inx();
                    break;
                default:
                    System.out.printf("%06d: OpCode $%02X not implemented%n", pc, opcode);
                    running = false;
            }
        }

        System.out.printf("Program ended after %d operations run%n", opsCount);
    }

    static void bpl() {
        // Cycles: 2 (+1 if branch succeeds, +2 if to a new page)
        var cycles = 2;
        var offset = 2;
        if (!getProcessorStatusFlag(STATUS_REGISTER_NEGATIVE_FLAG)) {
            cycles += 1;
            offset += MMU.readAddress(pc + 1);
        }
        System.out.printf("%06d: BPL (%d cycles)%n", pc, cycles);
        pc += offset;
    }

    static void sei() {
        System.out.printf("%06d: SEI (2 cycles)%n", pc);

        setProcessorStatusFlag(STATUS_REGISTER_INTERRUPT_FLAG);
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

    private static void staIndirectIndexed() {
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
        if (!getProcessorStatusFlag(STATUS_REGISTER_ZERO_FLAG)) {
            cycles += 1;
            offset += MMU.readAddress(pc + 1);
        }
        System.out.printf("%06d: BNE (%d cycles)%n", pc, cycles);
        pc += offset;
    }

    static void cld() {
        System.out.printf("%06d: CLD (2 cycles)%n", pc);

        unsetProcessorStatusFlag(STATUS_REGISTER_DECIMAL_FLAG);
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
            setProcessorStatusFlag(STATUS_REGISTER_CARRY_FLAG);
        } else if (result == 0) {
            setProcessorStatusFlag(STATUS_REGISTER_CARRY_FLAG);
            setProcessorStatusFlag(STATUS_REGISTER_ZERO_FLAG);
        } else {
            setProcessorStatusFlag(STATUS_REGISTER_NEGATIVE_FLAG);
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
            setProcessorStatusFlag(STATUS_REGISTER_CARRY_FLAG);
        } else if (result == 0) {
            setProcessorStatusFlag(STATUS_REGISTER_CARRY_FLAG);
            setProcessorStatusFlag(STATUS_REGISTER_ZERO_FLAG);
        } else {
            setProcessorStatusFlag(STATUS_REGISTER_NEGATIVE_FLAG);
        }

        pc += 3;
    }

    static void brk() {
        System.out.printf("%06d: BRK (7 cycles)%n", pc);

        setProcessorStatusFlag(STATUS_REGISTER_BREAK_FLAG);

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
            setProcessorStatusFlag(STATUS_REGISTER_ZERO_FLAG);
        } else if (value < 0) {
            setProcessorStatusFlag(STATUS_REGISTER_NEGATIVE_FLAG);
        }
    }

    static boolean getProcessorStatusFlag(int flagIndex) {
        return (p & (1 << (flagIndex))) > 0;
    }

    static void setProcessorStatusFlag(int flagIndex) {
        p |= 1 << flagIndex;
    }

    static void unsetProcessorStatusFlag(int flagIndex) {
        p &= ~(1 << flagIndex);
    }
}
