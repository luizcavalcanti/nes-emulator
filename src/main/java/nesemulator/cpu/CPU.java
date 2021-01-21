package nesemulator.cpu;

import nesemulator.MMU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class CPU {

    static ArrayList<CPUObserver> observers = new ArrayList<>();

    static final int STATUS_FLAG_CARRY = 0;
    static final int STATUS_FLAG_ZERO = 1;
    static final int STATUS_FLAG_INTERRUPT = 2;
    static final int STATUS_FLAG_DECIMAL = 3;
    static final int STATUS_FLAG_BREAK = 4;
    static final int STATUS_FLAG_ALWAYS_ONE = 5;
    static final int STATUS_FLAG_OVERFLOW = 6;
    static final int STATUS_FLAG_NEGATIVE = 7;

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

    static int cycleCounter;

    private CPU() {
    }

    public static void addObserver(CPUObserver observer) {
        observers.add(observer);
    }

    public static void removeObserver(CPUObserver observer) {
        observers.remove(observer);
    }

    private static void notifyInstruction(Opcode opcode, int cycles, int... operands) {
        for (CPUObserver o : observers) {
            o.notifyCPUInstruction(pc, opcode, cycles, operands);
        }
    }

    public static void initialize() {
        s = INITIAL_STACK_POINTER; // Stack pointer staring into the abyss
        pc = INITIAL_PC;
        a = x = y = p = 0x00; // Registers cleanup
        cycleCounter = 0;
    }

    public static void execute() {
        var opsCount = 0;

        var running = true;
        while (running) {
            opsCount++;
            running = executeStep();
        }
        logger.info(String.format("Program ended after %d operations run", opsCount));
    }


    public static boolean executeStep() {
        int opcode = signedToUsignedByte(MMU.readAddress(pc));

        switch (Opcode.fromCode(opcode)) {
            case BRK:
                cycleCounter += brk();
                break;
            case BPL:
                cycleCounter += bpl();
                break;
            case BMI:
                cycleCounter += bmi();
                break;
            case JSR:
                cycleCounter += jsr();
                break;
//                case PHA:
//                    cycleCounter += pha();
//                    break;
            case JMP_ABSOLUTE:
                cycleCounter += jmpAbsolute();
                break;
            case SEI:
                sei();
                break;
            case STY_ZERO_PAGE:
                styZeroPage();
                break;
            case STA_ZERO_PAGE:
                staZeroPage();
                break;
//                case DEY:
//                    cycleCounter += dey();
//                    break;
//                case TXA:
//                    cycleCounter += txa();
//                    break;
            case STA_ABSOLUTE:
                staAbsolute();
                break;
            case BCC:
                bcc();
                break;
//                case STA_INDIRECT_Y:
//                    cycleCounter += staIndirectY();
//                    break;
            case STA_ZERO_PAGE_X:
                staZeroPageX();
                break;
//                case TYA:
//                    cycleCounter += tya();
//                    break;
            case TXS:
                txs();
                break;
            case LDY_IMMEDIATE:
                ldyImmediate();
                break;
            case LDX_IMMEDIATE:
                ldxImmediate();
                break;
            case LDA_ZERO_PAGE:
                ldaZeroPage();
                break;
            case LDA_IMMEDIATE:
                ldaImmediate();
                break;
            case LDA_ABSOLUTE:
                ldaAbsolute();
                break;
            case LDA_ABSOLUTE_X:
                ldaAbsoluteX();
                break;
            case CPY_IMMEDIATE:
                cpyImmediate();
                break;
            case DEX:
                dex();
                break;
            case BNE:
                bne();
                break;
            case CLD:
                cld();
                break;
            case CMP_ABSOLUTE_X:
                cmpAbsoluteX();
                break;
            case INX:
                inx();
                break;
            default:
                logger.info(String.format("%04X: OpCode $%02X not implemented", pc, opcode));
                return false;
        }
        return true;
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

    static int bmi() {
        // TODO: Cycles: 2 (+1 if branch succeeds, +2 if to a new page)
        var cycles = 2;
        var offset = 2;
        if (isStatusFlagSet(STATUS_FLAG_NEGATIVE)) {
            cycles += 1;
            offset += MMU.readAddress(pc + 1);
        }
        notifyInstruction(Opcode.BMI, cycles);
        logger.info(String.format("%04X: BMI (%d cycles)", pc, cycles));

        pc += offset;

        return cycles;
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
        notifyInstruction(Opcode.BCC, cycles);

        pc += offset;
    }

    static int bpl() {
        // TODO: Cycles: 2 (+1 if branch succeeds, +2 if to a new page)
        var cycles = 2;
        var offset = 2;
        if (!isStatusFlagSet(STATUS_FLAG_NEGATIVE)) {
            cycles += 1;
            offset += MMU.readAddress(pc + 1);
        }
        logger.info(String.format("%04X: BPL (%d cycles)", pc, cycles));
        pc += offset;

        return cycles;
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

    static int jmpAbsolute() {
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        logger.info(String.format("%04X: JMP $%04X (3 cycles)", pc, address));

        pc = address;

        return 3;
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
        notifyInstruction(Opcode.LDA_ABSOLUTE_X, 4, address);
        logger.info(String.format("%04X: LDA $%04X, X (4 cycles)", pc, address));

        a = MMU.readAddress(address + x);
        setNonPositiveFlags(a);
        pc += 3;
    }

    static void ldaImmediate() {
        int value = MMU.readAddress(pc + 1);
        notifyInstruction(Opcode.LDA_IMMEDIATE, 2, value);
        logger.info(String.format("%04X: LDA #$%02X (2 cycles)", pc, value));

        a = value;
        setNonPositiveFlags(a);
        pc += 2;
    }

    static void ldaZeroPage() {
        int address = MMU.readAddress(pc + 1);
        notifyInstruction(Opcode.LDA_ZERO_PAGE, 2, address);
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

    static int jsr() {
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        logger.info(String.format("%04X: JSR $%04X (6 cycles)", pc, address));

        push2BytesToStack(pc + 3);

        pc = address;

        return 6;
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

    static int brk() {
        logger.info(String.format("%04X: BRK (7 cycles)", pc));

        setStatusFlag(STATUS_FLAG_BREAK);

        push2BytesToStack(pc);
        pushToStack(p);

        // TODO: understand what should happen here and name those constants
        int newPCAddress = littleEndianToInt(MMU.readAddress(0xFFFE), MMU.readAddress(0xFFFF));

        pc = newPCAddress;

        return 7;
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
