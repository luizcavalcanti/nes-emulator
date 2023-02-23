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

    public static int executeStep() {
        int nextInstruction = signedToUsignedByte(MMU.readAddress(pc));
        var opcode = Opcode.fromCode(nextInstruction);
        if (opcode == null) {
            throw new RuntimeException(String.format("%04X: OpCode $%02X not implemented", pc, nextInstruction));
        }

        var cycles = 0;
        switch (opcode) {
            case BRK:
                cycles = brk();
                break;
            case ORA_IMMEDIATE:
                cycles = oraImmediate();
                break;
            case BPL:
                cycles = bpl();
                break;
            case BMI:
                cycles = bmi();
                break;
            case JSR:
                cycles = jsr();
                break;
            case RTS:
                cycles = rts();
                break;
            case PHA:
                cycles = pha();
                break;
            case JMP_ABSOLUTE:
                cycles = jmpAbsolute();
                break;
            case SEI:
                cycles = sei();
                break;
            case STY_ZERO_PAGE:
                cycles = styZeroPage();
                break;
            case STA_ZERO_PAGE:
                cycles = staZeroPage();
                break;
            case STX_ZERO_PAGE:
                cycles = stxZeroPage();
                break;
            case DEY:
                cycles = dey();
                break;
            case INC_ABSOLUTE:
                cycles = incAbsolute();
                break;
            case DEC_ZERO_PAGE:
                cycles = decZeroPage();
                break;
            case TXA:
                cycles += txa();
                break;
            case STA_ABSOLUTE:
                cycles = staAbsolute();
                break;
            case STX_ABSOLUTE:
                cycles = stxAbsolute();
                break;
            case STY_ABSOLUTE:
                cycles = styAbsolute();
                break;
            case BCC:
                cycles = bcc();
                break;
            case STA_INDIRECT_Y:
                cycles = staIndirectY();
                break;
            case STA_ABSOLUTE_X:
                cycles = staAbsoluteX();
                break;
            case STA_ABSOLUTE_Y:
                cycles = staAbsoluteY();
                break;
            case STA_ZERO_PAGE_X:
                cycles = staZeroPageX();
                break;
            case TYA:
                cycles = tya();
                break;
            case TAY:
                cycles = tay();
                break;
            case TXS:
                cycles = txs();
                break;
            case LDY_IMMEDIATE:
                cycles = ldyImmediate();
                break;
            case LDX_IMMEDIATE:
                cycles = ldxImmediate();
                break;
            case LDA_ZERO_PAGE:
                cycles = ldaZeroPage();
                break;
            case LDA_IMMEDIATE:
                cycles = ldaImmediate();
                break;
            case LDA_ABSOLUTE:
                cycles = ldaAbsolute();
                break;
            case LDA_ABSOLUTE_X:
                cycles = ldaAbsoluteX();
                break;
            case LDA_ABSOLUTE_Y:
                cycles = ldaAbsoluteY();
                break;
            case CPY_IMMEDIATE:
                cycles = cpyImmediate();
                break;
            case DEX:
                cycles = dex();
                break;
            case BNE:
                cycles = bne();
                break;
            case BEQ:
                cycles = beq();
                break;
            case CLC:
                cycles = clc();
                break;
            case CLD:
                cycles = cld();
                break;
            case CMP_ABSOLUTE_X:
                cycles = cmpAbsoluteX();
                break;
            case CMP_IMMEDIATE:
                cycles = cmpImmediate();
                break;
            case INX:
                cycles = inx();
                break;
            case INY:
                cycles = iny();
                break;
            default:
                throw new RuntimeException(String.format("%04X: OpCode $%02X not implemented", pc, nextInstruction));
        }
        cyclesCounter += cycles;
        return cycles;
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
        final byte value = (byte) MMU.readAddress(pc + 1);
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
        int operand1 = MMU.readAddress(pc + 1);
        int operand2 = MMU.readAddress(pc + 2);
        int address = littleEndianToInt(operand1, operand2);
        notifyInstruction(Opcode.JMP_ABSOLUTE, cycles, operand1, operand2);

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

    static int txa() {
        final int cycles = 2;

        notifyInstruction(Opcode.TXA, cycles);

        a = x;
        setNonPositiveFlags((byte) a);
        pc += 1;

        return cycles;
    }

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
        int operand1 = MMU.readAddress(pc + 1);
        int operand2 = MMU.readAddress(pc + 2);
        int address = littleEndianToInt(operand1, operand2);

        notifyInstruction(Opcode.LDA_ABSOLUTE, cycles, operand1, operand2);

        a = MMU.readAddress(address) & 0xff;
        setNonPositiveFlags((byte) a);
        pc += 3;

        return cycles;
    }

    static int ldaAbsoluteX() {
        final int cycles = 4;
        int operand1 = MMU.readAddress(pc + 1);
        int operand2 = MMU.readAddress(pc + 2);
        int address = littleEndianToInt(operand1, operand2);

        notifyInstruction(Opcode.LDA_ABSOLUTE_X, cycles, operand1, operand2);

        a = MMU.readAddress(address + x) & 0xff;
        setNonPositiveFlags((byte) a);
        pc += 3;

        return cycles;
    }

    static int ldaAbsoluteY() {
        final int cycles = 4;
        int operand1 = MMU.readAddress(pc + 1);
        int operand2 = MMU.readAddress(pc + 2);
        int address = littleEndianToInt(operand1, operand2);

        notifyInstruction(Opcode.LDA_ABSOLUTE_Y, cycles, operand1, operand2);

        a = MMU.readAddress(address + y) & 0xff;
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
        int operand1 = MMU.readAddress(pc + 1);
        int operand2 = MMU.readAddress(pc + 2);
        int value = littleEndianToInt(operand1, operand2);

        notifyInstruction(Opcode.STY_ABSOLUTE, cycles, operand1, operand2);

        MMU.writeAddress(value, y);
        pc += 3;

        return cycles;
    }

    static int staAbsolute() {
        final int cycles = 4;
        int operand1 = MMU.readAddress(pc + 1);
        int operand2 = MMU.readAddress(pc + 2);
        int address = littleEndianToInt(operand1, operand2);

        notifyInstruction(Opcode.STA_ABSOLUTE, cycles, address, operand1, operand2);

        MMU.writeAddress(address, a);
        pc += 3;

        return cycles;
    }

    static int staAbsoluteX() {
        final int cycles = 5;
        int operand1 = MMU.readAddress(pc + 1);
        int operand2 = MMU.readAddress(pc + 2);
        int address = littleEndianToInt(operand1, operand2);

        notifyInstruction(Opcode.STA_ABSOLUTE_X, cycles, operand1, operand2);

        MMU.writeAddress(address + x, a);
        pc += 3;

        return cycles;
    }

    static int staAbsoluteY() {
        final int cycles = 5;
        int operand1 = MMU.readAddress(pc + 1);
        int operand2 = MMU.readAddress(pc + 2);
        int address = littleEndianToInt(operand1, operand2);

        notifyInstruction(Opcode.STA_ABSOLUTE_Y, cycles, operand1, operand2);

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

    static int stxAbsolute() {
        final int cycles = 4;
        int operand1 = MMU.readAddress(pc + 1);
        int operand2 = MMU.readAddress(pc + 2);
        int address = littleEndianToInt(operand1, operand2);

        notifyInstruction(Opcode.STX_ABSOLUTE, cycles, address, operand1, operand2);

        MMU.writeAddress(address, x);
        pc += 3;

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

        int operand1 = MMU.readAddress(pc + 1);
        int operand2 = MMU.readAddress(pc + 2);
        int address = littleEndianToInt(operand1, operand2);

        notifyInstruction(Opcode.JSR, cycles, operand1, operand2);

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

        int operand1 = MMU.readAddress(pc + 1);
        int operand2 = MMU.readAddress(pc + 2);
        int address = littleEndianToInt(operand1, operand2);

        notifyInstruction(Opcode.INC_ABSOLUTE, cycles, operand1, operand2);

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

    static int clc() {
        final int cycles = 2;

        notifyInstruction(Opcode.CLC, cycles);

        unsetStatusFlag(STATUS_FLAG_CARRY);
        pc += 1;

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
        int operand1 = MMU.readAddress(pc + 1);
        int operand2 = MMU.readAddress(pc + 2);
        int address = littleEndianToInt(operand1, operand2);

        notifyInstruction(Opcode.CMP_ABSOLUTE_X, cycles, operand1, operand2);

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
