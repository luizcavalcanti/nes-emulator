package nesemulator.cpu;

import nesemulator.MMU;
import nesemulator.cpu.observer.CPUObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class CPU {

    public static final int IRQ_ADDRESS_1 = 0xFFFE;
    public static final int IRQ_ADDRESS_2 = 0xFFFF;
    static ArrayList<CPUObserver> observers = new ArrayList<>();

    static final int STATUS_FLAG_CARRY = 0;
    static final int STATUS_FLAG_ZERO = 1;
    static final int STATUS_FLAG_INTERRUPT = 2;
    static final int STATUS_FLAG_DECIMAL = 3;
    static final int STATUS_FLAG_BREAK = 4;
    // Bit 5 is not used, always 1
    static final int STATUS_FLAG_OVERFLOW = 6;
    static final int STATUS_FLAG_NEGATIVE = 7;

    private static final int INITIAL_PC = 0x8000;
    private static final int INITIAL_PROCESSOR_STATUS = 0x34;
    private static final int INITIAL_STACK_POINTER = 0xFF;

    private static final Logger logger = LoggerFactory.getLogger(CPU.class);

    static int a;
    static int x;
    static int y;
    static byte p;
    static int pc;
    static int s;

    static int cyclesCounter;

    private CPU() {
    }

    public static int getCyclesCounter() {
        return CPU.cyclesCounter;
    }

    public static int getA() {
        return CPU.a;
    }

    public static int getX() {
        return CPU.x;
    }

    public static int getY() {
        return CPU.y;
    }

    public static int getS() {
        return CPU.s;
    }

    public static int getP() {
        return CPU.p;
    }

    public static int getPC() {
        return CPU.pc;
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
        a = x = y = 0x00; // Registers cleanup
        p = INITIAL_PROCESSOR_STATUS;
        cyclesCounter = 0;
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
        int nextInstruction = signedToUsignedByte(MMU.readAddress(pc));
        var opcode = Opcode.fromCode(nextInstruction);

        if (opcode == null) {
            logger.info(String.format("%04X: OpCode $%02X not implemented", pc, nextInstruction));
            return false;
        }

        switch (opcode) {
            case BRK:
                cyclesCounter += brk();
                break;
            case ORA_IMMEDIATE:
                cyclesCounter += oraImmediate();
                break;
            case BPL:
                cyclesCounter += bpl();
                break;
            case BMI:
                cyclesCounter += bmi();
                break;
            case JSR:
                cyclesCounter += jsr();
                break;
            case RTS:
                cyclesCounter += rts();
                break;
            case PHA:
                cyclesCounter += pha();
                break;
            case JMP_ABSOLUTE:
                cyclesCounter += jmpAbsolute();
                break;
            case SEI:
                cyclesCounter += sei();
                break;
            case STY_ZERO_PAGE:
                cyclesCounter += styZeroPage();
                break;
            case STA_ZERO_PAGE:
                cyclesCounter += staZeroPage();
                break;
            case STX_ZERO_PAGE:
                cyclesCounter += stxZeroPage();
                break;
            case DEY:
                cyclesCounter += dey();
                break;
            case INC_ABSOLUTE:
                cyclesCounter += incAbsolute();
                break;
            case DEC_ZERO_PAGE:
                cyclesCounter += decZeroPage();
                break;
//                case TXA:
//                    cycleCounter += txa();
//                    break;
            case STA_ABSOLUTE:
                cyclesCounter += staAbsolute();
                break;
            case STY_ABSOLUTE:
                cyclesCounter += styAbsolute();
                break;
            case BCC:
                cyclesCounter += bcc();
                break;
            case STA_INDIRECT_Y:
                cyclesCounter += staIndirectY();
                break;
            case STA_ABSOLUTE_Y:
                cyclesCounter += staAbsoluteY();
                break;
            case STA_ZERO_PAGE_X:
                cyclesCounter += staZeroPageX();
                break;
            case TYA:
                cyclesCounter += tya();
                break;
            case TAY:
                cyclesCounter += tay();
                break;
            case TXS:
                cyclesCounter += txs();
                break;
            case LDY_IMMEDIATE:
                cyclesCounter += ldyImmediate();
                break;
            case LDX_IMMEDIATE:
                cyclesCounter += ldxImmediate();
                break;
            case LDA_ZERO_PAGE:
                cyclesCounter += ldaZeroPage();
                break;
            case LDA_IMMEDIATE:
                cyclesCounter += ldaImmediate();
                break;
            case LDA_ABSOLUTE:
                cyclesCounter += ldaAbsolute();
                break;
            case LDA_ABSOLUTE_X:
                cyclesCounter += ldaAbsoluteX();
                break;
            case CPY_IMMEDIATE:
                cyclesCounter += cpyImmediate();
                break;
            case DEX:
                cyclesCounter += dex();
                break;
            case BNE:
                cyclesCounter += bne();
                break;
            case BEQ:
                cyclesCounter += beq();
                break;
            case CLD:
                cyclesCounter += cld();
                break;
            case CMP_ABSOLUTE_X:
                cyclesCounter += cmpAbsoluteX();
                break;
            case CMP_IMMEDIATE:
                cyclesCounter += cmpImmediate();
                break;
            case INX:
                cyclesCounter += inx();
                break;
            case INY:
                cyclesCounter += iny();
                break;
            default:
                logger.info(String.format("%04X: OpCode $%02X not implemented", pc, nextInstruction));
                break;
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
        // TODO: Cycles: +2 if to a new page
        var cycles = 2;
        var offset = 2;
        final int value = MMU.readAddress(pc + 1);
        if (isStatusFlagSet(STATUS_FLAG_NEGATIVE)) {
            cycles += 1;
            offset += value;
        }

        notifyInstruction(Opcode.BMI, cycles, value);

        pc += offset;

        return cycles;
    }

    static int bcc() {
        // TODO: Cycles: +2 if to a new page
        var cycles = 2;
        var offset = 2;
        final int value = MMU.readAddress(pc + 1);
        if (!isStatusFlagSet(STATUS_FLAG_CARRY)) {
            cycles += 1;
            offset += value;
        }

        notifyInstruction(Opcode.BCC, cycles, value);

        pc += offset;

        return cycles;
    }

    static int bpl() {
        // TODO: Cycles: +2 if to a new page
        var cycles = 2;
        var offset = 2;
        final int value = MMU.readAddress(pc + 1);
        if (!isStatusFlagSet(STATUS_FLAG_NEGATIVE)) {
            cycles += 1;
            offset += value;
        }

        notifyInstruction(Opcode.BPL, cycles, value);

        pc += offset;

        return cycles;
    }

    static int sei() {
        final int cycles = 2;

        notifyInstruction(Opcode.SEI, cycles);

        setStatusFlag(STATUS_FLAG_INTERRUPT);
        pc += 1;

        return cycles;
    }

    static int ldxImmediate() {
        final int cycles = 2;
        int value = MMU.readAddress(pc + 1);

        notifyInstruction(Opcode.LDX_IMMEDIATE, cycles, value);

        CPU.x = value & 0xff;
        setNonPositiveFlags((byte) x);
        pc += 2;

        return cycles;
    }

    static int jmpAbsolute() {
        final int cycles = 3;
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        notifyInstruction(Opcode.JMP_ABSOLUTE, cycles, address);

        pc = address;

        return cycles;
    }

    static int pha() {
        final int cycles = 3;

        notifyInstruction(Opcode.PHA, cycles);

        pushToStack(a);
        pc += 1;

        return cycles;
    }

//    private static void txa() {
//        final int cycles = 2;
//
//        notifyInstruction(Opcode.TXA, cycles);
//
//        a = x;
//        pc += 1;
//    }

    static int tya() {
        final int cycles = 2;

        notifyInstruction(Opcode.TYA, cycles);

        a = y;
        setNonPositiveFlags((byte) a);
        pc += 1;

        return cycles;
    }

    static int ldaAbsolute() {
        final int cycles = 4;
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));

        notifyInstruction(Opcode.LDA_ABSOLUTE, cycles, address);

        a = MMU.readAddress(address) & 0xff;
        setNonPositiveFlags((byte) a);
        pc += 3;

        return cycles;
    }

    static int ldaAbsoluteX() {
        final int cycles = 4;
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));

        notifyInstruction(Opcode.LDA_ABSOLUTE_X, cycles, address);

        a = MMU.readAddress(address + x) & 0xff;
        setNonPositiveFlags((byte) a);
        pc += 3;

        return cycles;
    }

    static int ldaImmediate() {
        final int cycles = 2;
        int value = MMU.readAddress(pc + 1);

        notifyInstruction(Opcode.LDA_IMMEDIATE, cycles, value);

        a = value & 0xff;
        setNonPositiveFlags((byte) a);
        pc += 2;

        return cycles;
    }

    static int ldaZeroPage() {
        final int cycles = 2;
        int address = signedToUsignedByte(MMU.readAddress(pc + 1));

        notifyInstruction(Opcode.LDA_ZERO_PAGE, cycles, address);

        a = MMU.readAddress(address) & 0xff;
        setNonPositiveFlags((byte) a);
        pc += 2;

        return cycles;
    }

    static int ldyImmediate() {
        final int cycles = 2;
        int value = MMU.readAddress(pc + 1);

        notifyInstruction(Opcode.LDY_IMMEDIATE, cycles, value);

        y = value & 0xff;
        setNonPositiveFlags((byte) y);
        pc += 2;

        return cycles;
    }


    static int styAbsolute() {
        final int cycles = 4;
        int value = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));

        notifyInstruction(Opcode.STY_ABSOLUTE, cycles, value);

        MMU.writeAddress(value, y);
        pc += 3;

        return cycles;
    }

    static int staAbsolute() {
        final int cycles = 4;
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));

        notifyInstruction(Opcode.STA_ABSOLUTE, cycles, address);

        MMU.writeAddress(address, a);
        pc += 3;

        return cycles;
    }

    static int staAbsoluteY() {
        final int cycles = 5;
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));

        notifyInstruction(Opcode.STA_ABSOLUTE_Y, cycles, address);

        MMU.writeAddress(address + y, a);
        pc += 3;

        return cycles;
    }

    static int staIndirectY() {
        //TODO: add 1 cycle if page boundary is crossed
        int cycles = 6;
        int addressLSB = signedToUsignedByte(MMU.readAddress(pc + 1));
        int address = littleEndianToInt(MMU.readAddress(addressLSB), MMU.readAddress(addressLSB + 1));

        notifyInstruction(Opcode.STA_INDIRECT_Y, cycles, addressLSB);

        MMU.writeAddress(address + y, a);
        pc += 2;

        return cycles;
    }

    static int styZeroPage() {
        final int cycles = 3;
        int address = signedToUsignedByte(MMU.readAddress(pc + 1));

        notifyInstruction(Opcode.STY_ZERO_PAGE, cycles, address);

        MMU.writeAddress(address, y);
        pc += 2;

        return cycles;
    }

    static int staZeroPage() {
        final int cycles = 3;
        int address = signedToUsignedByte(MMU.readAddress(pc + 1));

        notifyInstruction(Opcode.STA_ZERO_PAGE, cycles, address);

        MMU.writeAddress(address, a);
        pc += 2;

        return cycles;
    }

    static int stxZeroPage() {
        final int cycles = 3;
        int address = signedToUsignedByte(MMU.readAddress(pc + 1));

        notifyInstruction(Opcode.STX_ZERO_PAGE, cycles, address);

        MMU.writeAddress(address, x);
        pc += 2;

        return cycles;
    }

    static int staZeroPageX() {
        final int cycles = 4;
        int address = signedToUsignedByte(MMU.readAddress(pc + 1));

        notifyInstruction(Opcode.STA_ZERO_PAGE_X, cycles, address);

        MMU.writeAddress(address + CPU.x, CPU.a);
        pc += 2;

        return cycles;
    }

    static int jsr() {
        final int cycles = 6;

        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));

        notifyInstruction(Opcode.JSR, cycles, address);

        push2BytesToStack(pc + 3);

        pc = address;

        return cycles;
    }

    static int rts() {
        final int cycles = 6;

        notifyInstruction(Opcode.RTS, cycles);

        pc = pull2BytesFromStack();

        return cycles;
    }

    static int dey() {
        final int cycles = 2;

        notifyInstruction(Opcode.DEY, cycles);

        y = (y - 1) & 0xFF;
        setNonPositiveFlags((byte) y);

        pc += 1;

        return cycles;
    }

    static int decZeroPage() {
        final int cycles = 5;

        int address = signedToUsignedByte(MMU.readAddress(pc + 1));

        notifyInstruction(Opcode.DEC_ZERO_PAGE, cycles, address);

        int value = MMU.readAddress(address);
        int newValue = (value - 1) & 0xFF;
        MMU.writeAddress(address, newValue);
        setNonPositiveFlags((byte) newValue);

        pc += 2;

        return cycles;
    }

    static int incAbsolute() {
        final int cycles = 6;

        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));
        notifyInstruction(Opcode.INC_ABSOLUTE, cycles, address);

        int value = MMU.readAddress(address);
        int newValue = (value + 1) & 0xFF;
        MMU.writeAddress(address, newValue);
        setNonPositiveFlags((byte) newValue);

        pc += 3;

        return cycles;
    }

    static int bne() {
        // TODO: cycles +2 if to a new page
        var cycles = 2;
        var offset = 2;
        final int value = MMU.readAddress(pc + 1);

        if (!isStatusFlagSet(STATUS_FLAG_ZERO)) {
            cycles += 1;
            offset += value;
        }

        notifyInstruction(Opcode.BNE, cycles, value);

        pc += offset;

        return cycles;
    }

    static int beq() {
        // TODO: cycles +2 if to a new page
        var cycles = 2;
        var offset = 2;

        final int value = MMU.readAddress(pc + 1);
        if (isStatusFlagSet(STATUS_FLAG_ZERO)) {
            cycles += 1;
            offset += value;
        }

        notifyInstruction(Opcode.BEQ, cycles, value);

        pc += offset;

        return cycles;
    }

    static int cld() {
        final int cycles = 2;

        notifyInstruction(Opcode.CLD, cycles);

        unsetStatusFlag(STATUS_FLAG_DECIMAL);
        pc += 1;

        return cycles;
    }

    static int txs() {
        final int cycles = 2;

        notifyInstruction(Opcode.TXS, cycles);

        s = x;
        pc += 1;

        return cycles;
    }

    static int tay() {
        final int cycles = 2;

        notifyInstruction(Opcode.TAY, cycles);

        y = a;
        setNonPositiveFlags((byte) y);
        pc += 1;

        return cycles;
    }

    static int inx() {
        final int cycles = 2;

        notifyInstruction(Opcode.INX, cycles);

        x = (x + 1) & 0xFF;
        setNonPositiveFlags((byte) x);
        pc += 1;

        return cycles;
    }

    static int iny() {
        final int cycles = 2;

        notifyInstruction(Opcode.INY, cycles);

        y = (y + 1) & 0xFF;
        setNonPositiveFlags((byte) y);
        pc += 1;

        return cycles;
    }

    static int dex() {
        final int cycles = 2;

        notifyInstruction(Opcode.DEX, cycles);

        x = (x - 1) & 0xFF;
        setNonPositiveFlags((byte) x);
        pc += 1;

        return cycles;
    }

    static int cpyImmediate() {
        final int cycles = 2;
        int value = MMU.readAddress(pc + 1);

        notifyInstruction(Opcode.CPY_IMMEDIATE, cycles, value);

        var result = CPU.y - value;
        setComparisonFlags(result);

        pc += 2;

        return cycles;
    }

    static int cmpAbsoluteX() {
        // TODO: add +1 to cycle if page is crossed
        int cycles = 4;
        int address = littleEndianToInt(MMU.readAddress(pc + 1), MMU.readAddress(pc + 2));

        notifyInstruction(Opcode.CMP_ABSOLUTE_X, cycles, address);

        var value = MMU.readAddress(address + x);

        var result = CPU.a - value;
        setComparisonFlags(result);

        pc += 3;

        return cycles;
    }

    static int cmpImmediate() {
        int cycles = 2;
        int value = MMU.readAddress(pc + 1);

        notifyInstruction(Opcode.CMP_IMMEDIATE, cycles, value);

        var result = CPU.a - value;
        setComparisonFlags(result);

        pc += 2;
        return cycles;
    }

    private static void setComparisonFlags(int result) {
        if (result > 0) {
            setStatusFlag(STATUS_FLAG_CARRY);
        } else if (result == 0) {
            setStatusFlag(STATUS_FLAG_CARRY);
            setStatusFlag(STATUS_FLAG_ZERO);
        } else {
            setStatusFlag(STATUS_FLAG_NEGATIVE);
        }
    }

    static int brk() {
        final int cycles = 7;

        notifyInstruction(Opcode.BRK, cycles);

        setStatusFlag(STATUS_FLAG_BREAK);

        push2BytesToStack(pc);
        pushToStack(p);

        pc = littleEndianToInt(MMU.readAddress(IRQ_ADDRESS_1), MMU.readAddress(IRQ_ADDRESS_2));

        return cycles;
    }

    static int oraImmediate() {
        final int cycles = 2;
        int value = MMU.readAddress(pc + 1);

        notifyInstruction(Opcode.ORA_IMMEDIATE, cycles, value);

        a |= value;
        setNonPositiveFlags((byte) a);
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

    private static void setNonPositiveFlags(byte value) {
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

    static void pushToStack(int value) {
        MMU.writeAddress(0x0100 + s, value);
        s--;
    }

    static void push2BytesToStack(int value) {
        int high = ((value >> 8) & 0xFF);
        int low = (value & 0xFF);

        pushToStack(high);
        pushToStack(low);
    }

    static int pullFromStack() {
        s++;
        return MMU.readAddress(0x0100 + s);
    }

    static int pull2BytesFromStack() {
        int low = pullFromStack();
        int high = pullFromStack();
        return ((high << 8) & 0xFF00) | (low & 0xFF);
    }
}
