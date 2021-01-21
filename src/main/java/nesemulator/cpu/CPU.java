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
            case ORA:
                cycleCounter += oraImmediate();
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
                cycleCounter += sei();
                break;
            case STY_ZERO_PAGE:
                cycleCounter += styZeroPage();
                break;
            case STA_ZERO_PAGE:
                cycleCounter += staZeroPage();
                break;
//                case DEY:
//                    cycleCounter += dey();
//                    break;
//                case TXA:
//                    cycleCounter += txa();
//                    break;
            case STA_ABSOLUTE:
                cycleCounter += staAbsolute();
                break;
            case BCC:
                cycleCounter += bcc();
                break;
//                case STA_INDIRECT_Y:
//                    cycleCounter += staIndirectY();
//                    break;
            case STA_ZERO_PAGE_X:
                cycleCounter += staZeroPageX();
                break;
//                case TYA:
//                    cycleCounter += tya();
//                    break;
            case TXS:
                cycleCounter += txs();
                break;
            case LDY_IMMEDIATE:
                cycleCounter += ldyImmediate();
                break;
            case LDX_IMMEDIATE:
                cycleCounter += ldxImmediate();
                break;
            case LDA_ZERO_PAGE:
                cycleCounter += ldaZeroPage();
                break;
            case LDA_IMMEDIATE:
                cycleCounter += ldaImmediate();
                break;
            case LDA_ABSOLUTE:
                cycleCounter += ldaAbsolute();
                break;
            case LDA_ABSOLUTE_X:
                cycleCounter += ldaAbsoluteX();
                break;
            case CPY_IMMEDIATE:
                cycleCounter += cpyImmediate();
                break;
            case DEX:
                cycleCounter += dex();
                break;
            case BNE:
                cycleCounter += bne();
                break;
            case CLD:
                cycleCounter += cld();
                break;
            case CMP_ABSOLUTE_X:
                cycleCounter += cmpAbsoluteX();
                break;
            case INX:
                cycleCounter += inx();
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

        logger.info(String.format("%04X: BMI (%d cycles)", pc, cycles));
        notifyInstruction(Opcode.BMI, cycles);

        pc += offset;

        return cycles;
    }

    static int bcc() {
        // TODO: Cycles: 2 (+1 if branch succeeds, +2 if to a new page)
        var cycles = 2;
        var offset = 2;
        if (!isStatusFlagSet(STATUS_FLAG_CARRY)) {
            cycles += 1;
            offset += MMU.readAddress(pc + 1);
        }

        logger.info(String.format("%04X: BCC (%d cycles)", pc, cycles));
        notifyInstruction(Opcode.BCC, cycles);

        pc += offset;

        return cycles;
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
        notifyInstruction(Opcode.BPL, cycles);

        pc += offset;

        return cycles;
    }

    static int sei() {
        final int cycles = 2;
        logger.info(String.format("%04X: SEI (2 cycles)", pc));
        notifyInstruction(Opcode.SEI, cycles);

        setStatusFlag(STATUS_FLAG_INTERRUPT);
        pc += 1;

        return cycles;
    }

    static int ldxImmediate() {
        final int cycles = 2;
        int value = MMU.readAddress(pc + 1);
        logger.info(String.format("%04X: LDX #$%02X (2 cycles)", pc, value));
        notifyInstruction(Opcode.LDX_IMMEDIATE, cycles);

        CPU.x = value;
        setNonPositiveFlags(x);
        pc += 2;

        return cycles;
    }

    static int jmpAbsolute() {
        final int cycles = 3;
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        logger.info(String.format("%04X: JMP $%04X (3 cycles)", pc, address));
        notifyInstruction(Opcode.JMP_ABSOLUTE, cycles, address);

        pc = address;

        return cycles;
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

    static int ldaAbsolute() {
        final int cycles = 4;
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));

        logger.info(String.format("%04X: LDA $%04X (4 cycles)", pc, address));
        notifyInstruction(Opcode.LDA_ABSOLUTE, cycles, address);

        a = MMU.readAddress(address);
        setNonPositiveFlags(a);
        pc += 3;

        return cycles;
    }

    static int ldaAbsoluteX() {
        final int cycles = 4;
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));

        logger.info(String.format("%04X: LDA $%04X, X (4 cycles)", pc, address));
        notifyInstruction(Opcode.LDA_ABSOLUTE_X, cycles, address);

        a = MMU.readAddress(address + x);
        setNonPositiveFlags(a);
        pc += 3;

        return cycles;
    }

    static int ldaImmediate() {
        final int cycles = 2;
        int value = MMU.readAddress(pc + 1);

        logger.info(String.format("%04X: LDA #$%02X (2 cycles)", pc, value));
        notifyInstruction(Opcode.LDA_IMMEDIATE, cycles, value);

        a = value;
        setNonPositiveFlags(a);
        pc += 2;

        return cycles;
    }

    static int ldaZeroPage() {
        final int cycles = 2;
        int address = MMU.readAddress(pc + 1);

        logger.info(String.format("%04X: LDA $%02X (2 cycles)", pc, address));
        notifyInstruction(Opcode.LDA_ZERO_PAGE, cycles, address);

        a = MMU.readAddress(address);
        setNonPositiveFlags(a);
        pc += 2;

        return cycles;
    }

    static int ldyImmediate() {
        final int cycles = 2;
        int value = MMU.readAddress(pc + 1);

        logger.info(String.format("%04X: LDY #$%02X (2 cycles)", pc, value));
        notifyInstruction(Opcode.LDY_IMMEDIATE, cycles, value);

        y = value;
        setNonPositiveFlags(y);
        pc += 2;

        return cycles;
    }

    static int staAbsolute() {
        final int cycles = 4;
        int value = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));

        logger.info(String.format("%04X: STA $%04X (4 cycles)", pc, value));
        notifyInstruction(Opcode.STA_ABSOLUTE, cycles, value);

        MMU.writeAddress(value, a);
        pc += 3;

        return cycles;
    }

    private static void staIndirectY() {
        int addressLSB = signedToUsignedByte(MMU.readAddress(pc + 1));
        logger.info(String.format("%04X: STA ($%04X), Y (5 cycles)", pc, addressLSB));
        //TODO add 1 cycle if page boundary is crossed

        MMU.writeAddress(addressLSB + y, a);
        pc += 2;
    }

    static int styZeroPage() {
        final int cycles = 3;
        int value = signedToUsignedByte(MMU.readAddress(pc + 1));

        logger.info(String.format("%04X: STY $%X (3 cycles)", pc, value));
        notifyInstruction(Opcode.STY_ZERO_PAGE, cycles, value);

        MMU.writeAddress(value, y);
        pc += 2;

        return cycles;
    }

    static int staZeroPage() {
        final int cycles = 3;
        int value = signedToUsignedByte(MMU.readAddress(pc + 1));

        logger.info(String.format("%04X: STA $%02X (3 cycles)", pc, value));
        notifyInstruction(Opcode.STA_ZERO_PAGE, cycles, value);

        MMU.writeAddress(value, a);
        pc += 2;

        return cycles;
    }

    static int staZeroPageX() {
        final int cycles = 4;
        int address = signedToUsignedByte(MMU.readAddress(pc + 1));

        logger.info(String.format("%04X: STA $%02X, X (4 cycles)", pc, address));
        notifyInstruction(Opcode.STA_ZERO_PAGE_X, cycles, address);

        MMU.writeAddress(address + CPU.x, CPU.a);
        pc += 2;

        return cycles;
    }

    static int jsr() {
        final int cycles = 6;

        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));

        logger.info(String.format("%04X: JSR $%04X (6 cycles)", pc, address));
        notifyInstruction(Opcode.JSR, cycles, address);

        push2BytesToStack(pc + 3);

        pc = address;

        return cycles;
    }

//    private static void dey() {
//        final int cycles = 2;
//
//        logger.info(String.format("%04X: DEY (2 cycles)", pc));
//        notifyInstruction(Opcode.DEY, cycles);
//
//        y--;
//        setNonPositiveFlags(y);
//
//        pc += 1;
//    }

    static int bne() {
        // TODO: Cycles: 2 (+1 if branch succeeds, +2 if to a new page)
        var cycles = 2;
        var offset = 2;
        if (!isStatusFlagSet(STATUS_FLAG_ZERO)) {
            cycles += 1;
            offset += MMU.readAddress(pc + 1);
        }

        logger.info(String.format("%04X: BNE (%d cycles)", pc, cycles));
        notifyInstruction(Opcode.BNE, cycles);

        pc += offset;

        return cycles;
    }

    static int cld() {
        final int cycles = 2;

        logger.info(String.format("%04X: CLD (2 cycles)", pc));
        notifyInstruction(Opcode.CLD, cycles);

        unsetStatusFlag(STATUS_FLAG_DECIMAL);
        pc += 1;

        return cycles;
    }

    static int txs() {
        logger.info(String.format("%04X: TXS (2 cycles)", pc));

        s = x;
        pc += 1;

        return 2;
    }

    static int inx() {
        final int cycles = 2;

        logger.info(String.format("%04X: INX (2 cycles)", pc));
        notifyInstruction(Opcode.INX, cycles);

        x++;
        setNonPositiveFlags(x);
        pc += 1;

        return cycles;
    }

    static int dex() {
        final int cycles = 2;

        logger.info(String.format("%04X: DEX (2 cycles)", pc));
        notifyInstruction(Opcode.DEX, cycles);

        x--;
        setNonPositiveFlags(x);
        pc += 1;

        return cycles;
    }

    static int cpyImmediate() {
        final int cycles = 2;
        int value = MMU.readAddress(pc + 1);

        logger.info(String.format("%04X: CPY #$%X (2 cycles)", pc, value));
        notifyInstruction(Opcode.CPY_IMMEDIATE, cycles, value);

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

        return cycles;
    }

    static int cmpAbsoluteX() {
        // TODO: add +1 to cycle if page is crossed
        int cycles = 4;
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));

        logger.info(String.format("%04X: CMP $%04X, X (4 cycles)", pc, address));
        notifyInstruction(Opcode.CMP_ABSOLUTE_X, cycles, address);

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

        return cycles;
    }

    static int brk() {
        final int cycles = 7;

        logger.info(String.format("%04X: BRK (7 cycles)", pc));
        notifyInstruction(Opcode.LDA_ZERO_PAGE, cycles);

        setStatusFlag(STATUS_FLAG_BREAK);

        push2BytesToStack(pc);
        pushToStack(p);

        // TODO: understand what should happen here and name those constants
        int newPCAddress = littleEndianToInt(MMU.readAddress(0xFFFE), MMU.readAddress(0xFFFF));

        pc = newPCAddress;

        return cycles;
    }

    static int oraImmediate() {
        final int cycles = 2;
        int value = MMU.readAddress(pc + 1);

//        logger.info(String.format("%04X: ORA #$%02X (2 cycles)", pc, value));
        notifyInstruction(Opcode.ORA, cycles, value);

        a |= value;
        setNonPositiveFlags(a);
        pc += 2;

        return cycles;
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
