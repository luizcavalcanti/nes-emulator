package nesemulator.cpu;

import nesemulator.MMU;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CPUTest {

    @BeforeEach
    void setUp() {
        CPU.initialize();
    }

    @Test
    void initializeMustClearRegisters() {
        assertEquals(0x00, CPU.a);
        assertEquals(0x00, CPU.x);
        assertEquals(0x00, CPU.y);
        assertEquals(0x00, CPU.p);
    }

    @Test
    void initializeMustSetStackPointerToHigherAddress() {
        assertEquals(0xFF, CPU.s);
    }

    @Test
    void initializeMustResetSoundAndJoypadPorts() {
        assertEquals(0x00, MMU.readAddress(0x4000));
        assertEquals(0x00, MMU.readAddress(0x4001));
        assertEquals(0x00, MMU.readAddress(0x4002));
        assertEquals(0x00, MMU.readAddress(0x4003));
        assertEquals(0x00, MMU.readAddress(0x4004));
        assertEquals(0x00, MMU.readAddress(0x4005));
        assertEquals(0x00, MMU.readAddress(0x4006));
        assertEquals(0x00, MMU.readAddress(0x4007));
        assertEquals(0x00, MMU.readAddress(0x4008));
        assertEquals(0x00, MMU.readAddress(0x4009));
        assertEquals(0x00, MMU.readAddress(0x400A));
        assertEquals(0x00, MMU.readAddress(0x400B));
        assertEquals(0x00, MMU.readAddress(0x400C));
        assertEquals(0x00, MMU.readAddress(0x400D));
        assertEquals(0x00, MMU.readAddress(0x400E));
        assertEquals(0x00, MMU.readAddress(0x400F));

        // Boat duel
        assertEquals(0x00, MMU.readAddress(0x4010));
        assertEquals(0x00, MMU.readAddress(0x4011));
        assertEquals(0x00, MMU.readAddress(0x4012));
        assertEquals(0x00, MMU.readAddress(0x4013));

        assertEquals(0x00, MMU.readAddress(0x4015));
        assertEquals(0x00, MMU.readAddress(0x4017));
    }

    @Test
    void ldaImmediateMustLoadUnsignedValueToRegisterA() {
        var value = 0x0D;
        CPU.a = 0x00;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);

        int cycles = CPU.ldaImmediate();

        assertEquals(2, cycles);
        assertEquals(value, CPU.a);
        assertEquals(0x02, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void ldaImmediateMustSetNegativeFlagIfAIsNegative() {
        var value = 0xFF;
        CPU.a = 0x00;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);

        int cycles = CPU.ldaImmediate();

        assertEquals(2, cycles);
        assertEquals(value, CPU.a);
        assertEquals(0x02, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
    }

    @Test
    void ldaImmediateMustSetZeroFlagIfAIsZero() {
        var value = 0x00;
        CPU.a = 0x01;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);

        int cycles = CPU.ldaImmediate();

        assertEquals(2, cycles);
        assertEquals(value, CPU.a);
        assertEquals(0x02, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
    }

    @Test
    void staAbsoluteMustStoreTheAccumulatorContentIntoMemory() {
        CPU.a = 0x99;
        CPU.pc = 0x00;

        MMU.writeAddress(0x01, 0xCD);
        MMU.writeAddress(0x02, 0xAB);
        MMU.writeAddress(0xABCD, 0x00);

        int cycles = CPU.staAbsolute();

        assertEquals(4, cycles);
        assertEquals(0x99, MMU.readAddress(0xABCD));
        assertEquals(0x03, CPU.pc);
    }

    @Test
    void staAbsoluteYMustStoreTheAccumulatorContentIntoMemoryOffsetByY() {
        CPU.a = 0x99;
        CPU.y = 0x03;
        CPU.pc = 0x00;

        MMU.writeAddress(0x01, 0xCD);
        MMU.writeAddress(0x02, 0xAB);
        MMU.writeAddress(0xABCD, 0x00);

        int cycles = CPU.staAbsoluteY();

        assertEquals(5, cycles);
        assertEquals(0x99, MMU.readAddress(0xABD0));
        assertEquals(0x03, CPU.pc);
    }

    @Test
    void styAbsoluteMustStoreTheAccumulatorContentIntoMemory() {
        CPU.y = 0x99;
        CPU.pc = 0x00;

        MMU.writeAddress(0x01, 0xCD);
        MMU.writeAddress(0x02, 0xAB);
        MMU.writeAddress(0xABCD, 0x00);

        int cycles = CPU.styAbsolute();

        assertEquals(4, cycles);
        assertEquals(0x99, MMU.readAddress(0xABCD));
        assertEquals(0x03, CPU.pc);
    }

    @Test
    void ldaAbsoluteMustLoadUnsignedValueFromMemoryPositionToRegisterA() {
        var value = 0x0D;
        CPU.a = 0x00;
        CPU.pc = 0x00;

        MMU.writeAddress(0x01, 0xCD);
        MMU.writeAddress(0x02, 0xAB);
        MMU.writeAddress(0xABCD, value);

        int cycles = CPU.ldaAbsolute();

        assertEquals(4, cycles);
        assertEquals(value, CPU.a);
        assertEquals(0x03, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void ldaAbsoluteMustSetZeroFlagIfValueLoadedToRegisterAIsZero() {
        var value = 0x00;
        CPU.a = 0x00;
        CPU.pc = 0x00;

        MMU.writeAddress(0x01, 0xCD);
        MMU.writeAddress(0x02, 0xAB);
        MMU.writeAddress(0xABCD, value);

        int cycles = CPU.ldaAbsolute();

        assertEquals(4, cycles);
        assertEquals(value, CPU.a);
        assertEquals(0x03, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void ldaAbsoluteMustSetNegativeFlagIfValueLoadedToRegisterAIsNegative() {
        var value = 0xC4;
        CPU.a = 0x00;
        CPU.pc = 0x00;

        MMU.writeAddress(0x01, 0xCD);
        MMU.writeAddress(0x02, 0xAB);
        MMU.writeAddress(0xABCD, value);

        int cycles = CPU.ldaAbsolute();

        assertEquals(4, cycles);
        assertEquals(value, CPU.a);
        assertEquals(0x03, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void bplMustMoveProgramCountByGivenOffsetIfNegativeFlagIsUnset() {
        CPU.pc = 0x00;
        CPU.unsetStatusFlag(CPU.STATUS_FLAG_NEGATIVE);

        MMU.writeAddress(0x01, 0x30);

        int cycles = CPU.bpl();

        assertEquals(3, cycles);
        assertEquals(0x32, CPU.pc);
    }

    @Test
    void bplMustMoveProgramCountBy2IfNegativeFlagIsSet() {
        CPU.pc = 0x00;
        CPU.setStatusFlag(CPU.STATUS_FLAG_NEGATIVE);

        MMU.writeAddress(0x01, 0x30);

        int cycles = CPU.bpl();

        assertEquals(2, cycles);
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void bmiMustMoveProgramCountByGivenOffsetIfNegativeFlagIsSet() {
        CPU.pc = 0x00;
        CPU.setStatusFlag(CPU.STATUS_FLAG_NEGATIVE);

        MMU.writeAddress(0x01, 0x30);

        int cycles = CPU.bmi();

        assertEquals(3, cycles);
        assertEquals(0x32, CPU.pc);
    }

    @Test
    void bmiMustMoveProgramCountBy2IfNegativeFlagIsUnset() {
        CPU.pc = 0x00;
        CPU.unsetStatusFlag(CPU.STATUS_FLAG_NEGATIVE);

        MMU.writeAddress(0x01, 0x30);

        int cycles = CPU.bmi();

        assertEquals(2, cycles);
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void seiMustSetInterruptFlag() {
        CPU.pc = 0x00;

        int cycles = CPU.sei();
        assertEquals(2, cycles);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_INTERRUPT));
        assertEquals(0x01, CPU.pc);

        // make sure is idempotent
        cycles = CPU.sei();
        assertEquals(2, cycles);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_INTERRUPT));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void cldMustClearDecimalFlag() {
        CPU.pc = 0x00;
        CPU.setStatusFlag(CPU.STATUS_FLAG_DECIMAL);

        int cycles = CPU.cld();
        assertEquals(2, cycles);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_DECIMAL));
        assertEquals(0x01, CPU.pc);

        // make sure is idempotent
        cycles = CPU.cld();
        assertEquals(2, cycles);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_DECIMAL));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void ldxImmediateMustLoadUnsignedValueToRegisterX() {
        var value = 0x0D;
        CPU.x = 0x00;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);
        int cycles = CPU.ldxImmediate();

        assertEquals(2, cycles);
        assertEquals(value, CPU.x);
        assertEquals(0x02, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void ldxImmediateMustSetNegativeFlagIfXIsNegative() {
        var value = 0xFF;
        CPU.x = 0x00;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);
        int cycles = CPU.ldxImmediate();

        assertEquals(2, cycles);
        assertEquals(value, CPU.x);
        assertEquals(0x02, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
    }

    @Test
    void ldxImmediateMustSetZeroFlagIfXIsZero() {
        var value = 0x00;
        CPU.x = 0x01;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);
        int cycles = CPU.ldxImmediate();

        assertEquals(2, cycles);
        assertEquals(value, CPU.x);
        assertEquals(0x02, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
    }

    @Test
    void txsMustSetXValueToStackPointer() {
        CPU.x = 0x00AA;
        CPU.s = 0x0200;
        CPU.pc = 0x00;

        int cycles = CPU.txs();

        assertEquals(2, cycles);
        assertEquals(0x00AA, CPU.x);
        assertEquals(0x00AA, CPU.s);
        assertEquals(0x01, CPU.pc);
    }

    @Test
    void tayMustSetAccumulatorValueToYRegister() {
        CPU.a = 0xAA;
        CPU.y = 0x00;
        CPU.pc = 0x00;

        int cycles = CPU.tay();

        assertEquals(2, cycles);
        assertEquals(0x00AA, CPU.a);
        assertEquals(0x00AA, CPU.y);
        assertEquals(0x01, CPU.pc);
    }

    @Test
    void tyaMustSetYRegisterValueToAccumulator() {
        CPU.y = 0xAA;
        CPU.a = 0x00;
        CPU.pc = 0x00;

        int cycles = CPU.tya();

        assertEquals(2, cycles);
        assertEquals(0x00AA, CPU.y);
        assertEquals(0x00AA, CPU.a);
        assertEquals(0x01, CPU.pc);
    }

    @Test
    void staZeroPageXMustStoreRegisterAContentIntoMemoryAddressOffsetByX() {
        CPU.x = 0x03;
        CPU.a = 0xAB;

        CPU.pc = 0x00;
        MMU.writeAddress(0x01, 0x30);
        MMU.writeAddress(0x33, 0);

        int cycles = CPU.staZeroPageX();

        assertEquals(4, cycles);
        assertEquals(0xAB, MMU.readAddress(0x33));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void staZeroPageMustStoreRegisterAContentIntoMemoryAddress() {
        int address = 0x99;
        CPU.a = 0xAB;

        CPU.pc = 0x00;
        MMU.writeAddress(address, 0);
        MMU.writeAddress(0x01, address);

        int cycles = CPU.staZeroPage();

        assertEquals(3, cycles);
        assertEquals(0xAB, MMU.readAddress(address));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void styZeroPageMustStoreRegisterYContentIntoMemoryAddress() {
        int address = 0x99;
        CPU.y = 0xAB;

        CPU.pc = 0x00;
        MMU.writeAddress(address, 0);
        MMU.writeAddress(0x01, address);

        int cycles = CPU.styZeroPage();

        assertEquals(3, cycles);
        assertEquals(0xAB, MMU.readAddress(address));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void stxZeroPageMustStoreRegisterXContentIntoMemoryAddress() {
        int address = 0x99;
        CPU.x = 0xAB;

        CPU.pc = 0x00;
        MMU.writeAddress(address, 0);
        MMU.writeAddress(0x01, address);

        int cycles = CPU.stxZeroPage();

        assertEquals(3, cycles);
        assertEquals(0xAB, MMU.readAddress(address));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void inxMustIncrementRegisterXBy1() {
        CPU.x = 0x09;
        CPU.pc = 0x00;

        int cycles = CPU.inx();

        assertEquals(2, cycles);
        assertEquals(0x0A, CPU.x);
        assertEquals(0x01, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void inxMustIncrementRegisterXBy1AndSetZeroFlagIfNewXIsZero() {
        CPU.x = 0xFF;
        CPU.pc = 0x00;

        int cycles = CPU.inx();

        assertEquals(2, cycles);
        assertEquals(0x00, CPU.x);
        assertEquals(0x01, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void inxMustIncrementRegisterXBy1AndSetNegativeFlagIfNewXIsNegative() {
        CPU.x = 0xFE;
        CPU.pc = 0x00;

        int cycles = CPU.inx();

        assertEquals(2, cycles);
        assertEquals(0xFF, CPU.x);
        assertEquals(0x01, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void inyMustIncrementRegisterYBy1() {
        CPU.y = 0x09;
        CPU.pc = 0x00;

        int cycles = CPU.iny();

        assertEquals(2, cycles);
        assertEquals(0x0A, CPU.y);
        assertEquals(0x01, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void inyMustIncrementRegisterYBy1AndSetZeroFlagIfNewYIsZero() {
        CPU.y = 0xFF;
        CPU.pc = 0x00;

        int cycles = CPU.iny();

        assertEquals(2, cycles);
        assertEquals(0x00, CPU.y);
        assertEquals(0x01, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void inyMustIncrementRegisterYBy1AndSetNegativeFlagIfNewYIsNegative() {
        CPU.y = 0xFE;
        CPU.pc = 0x00;

        int cycles = CPU.iny();

        assertEquals(2, cycles);
        assertEquals(0xFF, CPU.y);
        assertEquals(0x01, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void bneMustMoveProgramCountByGivenOffsetIfZeroFlagIsUnset() {
        CPU.pc = 0x00;
        CPU.unsetStatusFlag(CPU.STATUS_FLAG_ZERO);


        MMU.writeAddress(0x01, 0x30);

        int cycles = CPU.bne();

        assertEquals(3, cycles);
        assertEquals(0x32, CPU.pc);
    }

    @Test
    void bneMustMoveProgramCountBy2IfZeroFlagIsSet() {
        CPU.pc = 0x00;
        CPU.setStatusFlag(CPU.STATUS_FLAG_ZERO);

        MMU.writeAddress(0x01, 0x30);

        int cycles = CPU.bne();

        assertEquals(2, cycles);
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void ldaAbsoluteXMustLoadUnsignedValueFromMemoryPositionOffsetByXToRegisterA() {
        var value = 0x0D;
        CPU.a = 0x00;
        CPU.x = 0x03;
        CPU.pc = 0x00;

        MMU.writeAddress(0x01, 0xCD);
        MMU.writeAddress(0x02, 0xAB);
        MMU.writeAddress(0xABD0, value);

        int cycles = CPU.ldaAbsoluteX();

        assertEquals(4, cycles);
        assertEquals(value, CPU.a);
        assertEquals(0x03, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void ldaAbsoluteXMustSetZeroFlagIfValueLoadedToRegisterAIsZero() {
        var value = 0x00;
        CPU.a = 0x00;
        CPU.x = 0x03;
        CPU.pc = 0x00;

        MMU.writeAddress(0x01, 0xCD);
        MMU.writeAddress(0x02, 0xAB);
        MMU.writeAddress(0xABD0, value);

        int cycles = CPU.ldaAbsoluteX();

        assertEquals(4, cycles);
        assertEquals(value, CPU.a);
        assertEquals(0x03, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void ldaAbsoluteXMustSetNegativeFlagIfValueLoadedToRegisterAIsNegative() {
        var value = 0xC4;
        CPU.a = 0x00;
        CPU.x = 0x03;
        CPU.pc = 0x00;

        MMU.writeAddress(0x01, 0xCD);
        MMU.writeAddress(0x02, 0xAB);
        MMU.writeAddress(0xABD0, value);

        int cycles = CPU.ldaAbsoluteX();

        assertEquals(4, cycles);
        assertEquals(value, CPU.a);
        assertEquals(0x03, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void cpyImmediateMustSetCarryFlagWhenImmediateValueIsSmallerThanY() {
        CPU.pc = 0x00;
        CPU.y = 0x09;
        MMU.writeAddress(0x01, 0x08);

        int cycles = CPU.cpyImmediate();

        assertEquals(2, cycles);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_CARRY));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void cpyImmediateMustSetZeroAndCarryFlagsWhenImmediateValueIsEqualToY() {
        CPU.pc = 0x00;
        CPU.y = 0x09;
        MMU.writeAddress(0x01, 0x09);

        int cycles = CPU.cpyImmediate();

        assertEquals(2, cycles);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_CARRY));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void cpyImmediateMustSetNegativeFlagWhenImmediateValueIsBiggerThanY() {
        CPU.pc = 0x00;
        CPU.y = 0x09;
        MMU.writeAddress(0x01, 0x0F);

        int cycles = CPU.cpyImmediate();

        assertEquals(2, cycles);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_CARRY));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void cmpAbsoluteXSetCarryFlagWhenAbsoluteOffsetValueIsSmallerThanA() {
        CPU.pc = 0x00;
        CPU.a = 0x09;

        // offset
        CPU.x = 0x03;
        // address without offset
        MMU.writeAddress(0x01, 0x03);
        MMU.writeAddress(0x02, 0x00);
        // actual value
        MMU.writeAddress(0x06, 0x08);

        int cycles = CPU.cmpAbsoluteX();

        assertEquals(4, cycles);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_CARRY));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertEquals(0x03, CPU.pc);
    }

    @Test
    void cmpAbsoluteXMustSetZeroAndCarryFlagsWhenAbsoluteOffsetValueIsEqualToA() {
        CPU.pc = 0x00;
        CPU.a = 0x09;

        // offset
        CPU.x = 0x03;
        // address without offset
        MMU.writeAddress(0x01, 0x03);
        MMU.writeAddress(0x02, 0x00);
        // actual value
        MMU.writeAddress(0x06, 0x09);

        int cycles = CPU.cmpAbsoluteX();

        assertEquals(4, cycles);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_CARRY));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertEquals(0x03, CPU.pc);
    }

    @Test
    void cmpAbsoluteXMustSetNegativeFlagWhenAbsoluteOffsetValueIsBiggerThanA() {
        CPU.pc = 0x00;
        CPU.a = 0x09;

        // offset
        CPU.x = 0x03;
        // address without offset
        MMU.writeAddress(0x01, 0x03);
        MMU.writeAddress(0x02, 0x00);
        // actual value
        MMU.writeAddress(0x06, 0x0F);

        int cycles = CPU.cmpAbsoluteX();

        assertEquals(4, cycles);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_CARRY));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertEquals(0x03, CPU.pc);
    }

    @Test
    void dexMustDecrementRegisterXBy1() {
        CPU.x = 0x09;
        CPU.pc = 0x00;

        int cycles = CPU.dex();

        assertEquals(2, cycles);
        assertEquals(0x08, CPU.x);
        assertEquals(0x01, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void dexMustDecrementRegisterXBy1AndSetZeroFlagIfNewXIsZero() {
        CPU.x = 0x01;
        CPU.pc = 0x00;

        int cycles = CPU.dex();

        assertEquals(2, cycles);
        assertEquals(0x00, CPU.x);
        assertEquals(0x01, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void dexMustDecrementRegisterXBy1AndSetNegativeFlagIfNewXIsNegative() {
        CPU.x = 0x00;
        CPU.pc = 0x00;

        int cycles = CPU.dex();

        assertEquals(2, cycles);
        assertEquals(0xFF, CPU.x);
        assertEquals(0x01, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void deyMustDecrementRegisterYBy1() {
        CPU.y = 0x09;
        CPU.pc = 0x00;

        int cycles = CPU.dey();

        assertEquals(2, cycles);
        assertEquals(0x08, CPU.y);
        assertEquals(0x01, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void deyMustDecrementRegisterXBy1AndSetZeroFlagIfNewYIsZero() {
        CPU.y = 0x01;
        CPU.pc = 0x00;

        int cycles = CPU.dey();

        assertEquals(2, cycles);
        assertEquals(0x00, CPU.y);
        assertEquals(0x01, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void deyMustDecrementRegisterXBy1AndSetNegativeFlagIfNewYIsNegative() {
        CPU.y = 0x00;
        CPU.pc = 0x00;

        int cycles = CPU.dey();

        assertEquals(2, cycles);
        assertEquals(0xFF, CPU.y);
        assertEquals(0x01, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void brkMustSetBreakFlagAndPushProgramCounterToStack() {
        CPU.pc = 0x1234;
        CPU.setStatusFlag(CPU.STATUS_FLAG_CARRY);

        // Post-interrupt Program Counter
        MMU.writeAddress(0xFFFE, 0xCD);
        MMU.writeAddress(0xFFFF, 0xAB);

        var oldStackPointer = CPU.s;
        var oldProcessorStatus = CPU.p;
        oldProcessorStatus |= 1 << 4; // Old status with break flag set
        int oldPCByte1 = (CPU.pc & 0xFF);
        int oldPCByte2 = ((CPU.pc >> 8) & 0xFF);

        int cycles = CPU.brk();

        assertEquals(7, cycles);
        assertEquals(oldStackPointer - 3, CPU.s);
        assertEquals(oldProcessorStatus, MMU.readAddress(0x0100 + CPU.s + 1));
        assertEquals(oldPCByte1, MMU.readAddress(0x0100 + CPU.s + 2));
        assertEquals(oldPCByte2, MMU.readAddress(0x0100 + CPU.s + 3));
        assertEquals(0xABCD, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_BREAK));
    }

    @Test
    void jmpAbsoluteMustMoveProgramCounterToGivenAddress() {
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, 0xAD);
        MMU.writeAddress(0x02, 0xDE);

        int cycles = CPU.jmpAbsolute();

        assertEquals(3, cycles);
        assertEquals(0xDEAD, CPU.pc);
    }

    @Test
    void bccMustDisplaceProgramCounterByGivenOffsetIfCarryFlagIsClear() {
        var offset = -3;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, offset);
        CPU.unsetStatusFlag(CPU.STATUS_FLAG_CARRY);

        int cycles = CPU.bcc();

        assertEquals(3, cycles);
        assertEquals(offset + 2, CPU.pc);
    }

    @Test
    void bccMustDisplaceProgramCounterBy2IfCarryFlagIsSet() {
        var offset = -3;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, offset);
        CPU.setStatusFlag(CPU.STATUS_FLAG_CARRY);

        int cycles = CPU.bcc();

        assertEquals(2, cycles);
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void jsrMustPushNextOperationCounterToStashAndAssingOperandToProgramCounter() {
        int destination1 = 0xDE;
        int destination2 = 0xAD;
        CPU.pc = 0x03;
        MMU.writeAddress(0x04, destination2);
        MMU.writeAddress(0x05, destination1);

        int cycles = CPU.jsr();

        assertEquals(6, cycles);
        assertEquals(0xDEAD, CPU.pc);
        assertEquals(0x06, MMU.readAddress(0x0100 + CPU.s + 1));
        assertEquals(0x00, MMU.readAddress(0x0100 + CPU.s + 2));
    }

    @Test
    void ldaZeroPageMustLoadUnsignedValueInGivenAddressIntoRegisterA() {
        var address = 0xFD;
        var value = 0x6E;
        CPU.a = 0x00;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, address);
        MMU.writeAddress(0xFD, value);

        int cycles = CPU.ldaZeroPage();

        assertEquals(2, cycles);
        assertEquals(value, CPU.a);
        assertEquals(0x02, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void ldaZeroPageMustSetNegativeFlagIsAIsNegative() {
        var address = 0xFD;
        var value = 0xFF;
        CPU.a = 0x00;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, address);
        MMU.writeAddress(0xFD, value);

        int cycles = CPU.ldaZeroPage();

        assertEquals(2, cycles);
        assertEquals(value, CPU.a);
        assertEquals(0x02, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
    }

    @Test
    void ldaZeroPageMustSetZeroFlagIsAIsZero() {
        var value = 0x00;
        CPU.a = 0x00;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);

        int cycles = CPU.ldaZeroPage();

        assertEquals(2, cycles);
        assertEquals(value, CPU.a);
        assertEquals(0x02, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
    }

    @Test
    void oraMustPerformLogicalORonOperandAndAccumulatorAndStoreResultInAccumulator() {
        var value = (byte) 0b01010101;
        CPU.a = (byte) 0b10101111;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);

        int cycles = CPU.oraImmediate();

        assertEquals(2, cycles);
        assertEquals((byte) 0b11111111, CPU.a);
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void ldyImmediateMustLoadSpecifiedValueToRegisterY() {
        var value = 0x0B;
        CPU.y = 0x00;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);
        int cycles = CPU.ldyImmediate();

        assertEquals(2, cycles);
        assertEquals(value, CPU.y);
        assertEquals(0x02, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void ldyImmediateMustSetNegativeFlagIfYIsNegative() {
        var value = 0xFF;
        CPU.y = 0x00;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);
        int cycles = CPU.ldyImmediate();

        assertEquals(2, cycles);
        assertEquals(value, CPU.y);
        assertEquals(0x02, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
    }

    @Test
    void ldyImmediateMustSetZeroFlagIfYIsZero() {
        var value = 0x00;
        CPU.y = 0x01;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);
        int cycles = CPU.ldyImmediate();

        assertEquals(2, cycles);
        assertEquals(value, CPU.y);
        assertEquals(0x02, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
    }

    @Test
    void rtsMustPullProgramCounterFromStackAndSetIt() {
        CPU.pc = 0x0000;
        CPU.push2BytesToStack(0xABCD);

        int cycles = CPU.rts();

        assertEquals(6, cycles);
        assertEquals(0xABCD, CPU.pc);
    }

    @Test
    void phaMustPushAToStack() {
        CPU.pc = 0x00;
        CPU.a = 0xAB;

        int cycles = CPU.pha();

        assertEquals(3, cycles);
        assertEquals(0xAB, CPU.pullFromStack());
    }

    @Test
    void beqMustMoveProgramCountByGivenOffsetIfZeroFlagIsSet() {
        CPU.pc = 0x00;
        CPU.setStatusFlag(CPU.STATUS_FLAG_ZERO);


        MMU.writeAddress(0x01, 0x30);

        int cycles = CPU.beq();

        assertEquals(3, cycles);
        assertEquals(0x32, CPU.pc);
    }

    @Test
    void beqMustMoveProgramCountBy2IfZeroFlagIsUnset() {
        CPU.pc = 0x00;
        CPU.unsetStatusFlag(CPU.STATUS_FLAG_ZERO);

        MMU.writeAddress(0x01, 0x30);

        int cycles = CPU.beq();

        assertEquals(2, cycles);
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void decZeroPageMustDecrementZeroPageMemoryAddressValueBy1AndSetFlagsAccordingly() {
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, 0x30);
        MMU.writeAddress(0x30, 0x02);

        int cycles = CPU.decZeroPage();
        assertEquals(0x02, CPU.pc);
        assertEquals(5, cycles);
        assertEquals(0x01, MMU.readAddress(0x30));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));

        CPU.pc = 0x00;
        cycles = CPU.decZeroPage();
        assertEquals(5, cycles);
        assertEquals(0x00, MMU.readAddress(0x30));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));

        CPU.pc = 0x00;
        cycles = CPU.decZeroPage();
        assertEquals(5, cycles);
        assertEquals(0xFF, MMU.readAddress(0x30));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void incAbsoluteMustIncrementMemoryAddressValueBy1AndSetFlagsAccordingly() {
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, 0x30);
        MMU.writeAddress(0x02, 0x00);
        MMU.writeAddress(0x30, 0xFE);

        int cycles = CPU.incAbsolute();
        assertEquals(0x03, CPU.pc);
        assertEquals(6, cycles);
        assertEquals(0xFF, MMU.readAddress(0x30));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));

        CPU.pc = 0x00;
        cycles = CPU.incAbsolute();
        assertEquals(6, cycles);
        assertEquals(0x03, CPU.pc);
        assertEquals(0x00, MMU.readAddress(0x30));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));

        CPU.pc = 0x00;
        cycles = CPU.incAbsolute();
        assertEquals(6, cycles);
        assertEquals(0x03, CPU.pc);
        assertEquals(0x01, MMU.readAddress(0x30));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void cmpImmediateSetCarryFlagWhenAbsoluteOffsetValueIsSmallerThanA() {
        CPU.pc = 0x00;
        CPU.a = 0x09;
        MMU.writeAddress(0x01, 0x08);

        int cycles = CPU.cmpImmediate();

        assertEquals(2, cycles);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_CARRY));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void cmpImmediateMustSetZeroAndCarryFlagsWhenAbsoluteOffsetValueIsEqualToA() {
        CPU.pc = 0x00;
        CPU.a = 0x09;
        MMU.writeAddress(0x01, 0x09);

        int cycles = CPU.cmpImmediate();

        assertEquals(2, cycles);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_CARRY));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void cmpImmediateMustSetNegativeFlagWhenAbsoluteOffsetValueIsBiggerThanA() {
        CPU.pc = 0x00;
        CPU.a = 0x09;
        MMU.writeAddress(0x01, 0x0F);

        int cycles = CPU.cmpImmediate();

        assertEquals(2, cycles);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_CARRY));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void staIndirectYMustStoreAccumulatorIntoGivenIndirectAddressOffsetByY() {
        CPU.pc = 0x00;
        CPU.a = 0xAB;
        CPU.y = 0x03;
        MMU.writeAddress(0x01, 0x12);
        MMU.writeAddress(0x12, 0xAA);
        MMU.writeAddress(0x13, 0xDE);

        MMU.writeAddress(0xDEAD, 0x00);

        int cycles = CPU.staIndirectY();

        assertEquals(6, cycles);
        assertEquals(0x02, CPU.pc);
        assertEquals(0xAB, MMU.readAddress(0xDEAD));
    }

}