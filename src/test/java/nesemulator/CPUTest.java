package nesemulator;

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
        assertEquals(0xFD, CPU.s);
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
        var value = 0xFD;
        CPU.a = 0x00;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);
        CPU.ldaImmediate();

        assertEquals(value, CPU.a);
        assertEquals(0x02, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void ldaImmediateMustSetNegativeFlagIsAIsNegative() {
        var value = -1;
        CPU.a = 0x00;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);
        CPU.ldaImmediate();

        assertEquals(value, CPU.a);
        assertEquals(0x02, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
    }

    @Test
    void ldaImmediateMustSetZeroFlagIsAIsZero() {
        var value = 0x00;
        CPU.a = 0x01;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);
        CPU.ldaImmediate();

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

        CPU.staAbsolute();

        assertEquals(0x99, MMU.readAddress(0xABCD));
        assertEquals(0x03, CPU.pc);
    }

    @Test
    void ldaAbsoluteMustLoadUnsignedValueFromMemoryPositionToRegisterA() {
        var value = 0xFD;
        CPU.a = 0x00;
        CPU.pc = 0x00;

        MMU.writeAddress(0x01, 0xCD);
        MMU.writeAddress(0x02, 0xAB);
        MMU.writeAddress(0xABCD, value);

        CPU.ldaAbsolute();

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

        CPU.ldaAbsolute();

        assertEquals(value, CPU.a);
        assertEquals(0x03, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void ldaAbsoluteMustSetNegativeFlagIfValueLoadedToRegisterAIsNegative() {
        var value = -60;
        CPU.a = 0x00;
        CPU.pc = 0x00;

        MMU.writeAddress(0x01, 0xCD);
        MMU.writeAddress(0x02, 0xAB);
        MMU.writeAddress(0xABCD, value);

        CPU.ldaAbsolute();

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

        CPU.bpl();

        assertEquals(0x32, CPU.pc);
    }

    @Test
    void bplMustMoveProgramCountBy2IfNegativeFlagIsSet() {
        CPU.pc = 0x00;
        CPU.setStatusFlag(CPU.STATUS_FLAG_NEGATIVE);

        MMU.writeAddress(0x01, 0x30);

        CPU.bpl();

        assertEquals(0x02, CPU.pc);
    }

    @Test
    void bmiMustMoveProgramCountByGivenOffsetIfNegativeFlagIsSet() {
        CPU.pc = 0x00;
        CPU.setStatusFlag(CPU.STATUS_FLAG_NEGATIVE);

        MMU.writeAddress(0x01, 0x30);

        CPU.bmi();

        assertEquals(0x32, CPU.pc);
    }

    @Test
    void bmiMustMoveProgramCountBy2IfNegativeFlagIsUnset() {
        CPU.pc = 0x00;
        CPU.unsetStatusFlag(CPU.STATUS_FLAG_NEGATIVE);

        MMU.writeAddress(0x01, 0x30);

        CPU.bmi();

        assertEquals(0x02, CPU.pc);
    }

    @Test
    void seiMustSetInterruptFlag() {
        CPU.pc = 0x00;

        CPU.sei();
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_INTERRUPT));
        assertEquals(0x01, CPU.pc);

        // make sure is idempotent
        CPU.sei();
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_INTERRUPT));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void cldMustClearDecimalFlag() {
        CPU.pc = 0x00;
        CPU.setStatusFlag(CPU.STATUS_FLAG_DECIMAL);

        CPU.cld();
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_DECIMAL));
        assertEquals(0x01, CPU.pc);

        // make sure is idempotent
        CPU.cld();
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_DECIMAL));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void ldxImmediateMustLoadUnsignedValueToRegisterX() {
        var value = 0xFD;
        CPU.x = 0x00;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);
        CPU.ldxImmediate();

        assertEquals(value, CPU.x);
        assertEquals(0x02, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void ldxImmediateMustSetNegativeFlagIfXIsNegative() {
        var value = -1;
        CPU.x = 0x00;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, value);
        CPU.ldxImmediate();

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
        CPU.ldxImmediate();

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

        CPU.txs();

        assertEquals(0x00AA, CPU.x);
        assertEquals(0x00AA, CPU.s);
        assertEquals(0x01, CPU.pc);
    }

    @Test
    void staZeroPageXMustStoreRegisterAContentIntoMemoryAddressOffsetByX() {
        CPU.x = 0x03;
        CPU.a = 0xAB;

        CPU.pc = 0x00;
        MMU.writeAddress(0x01, 0x30);
        MMU.writeAddress(0x33, 0);

        CPU.staZeroPageX();

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

        CPU.staZeroPage();

        assertEquals(0xAB, MMU.readAddress(address));
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void inxMustIncrementRegisterXBy1() {
        CPU.x = 0x09;
        CPU.pc = 0x00;

        CPU.inx();
        assertEquals(0x0A, CPU.x);
        assertEquals(0x01, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void inxMustIncrementRegisterXBy1AndSetZeroFlagIfNewXIsZero() {
        CPU.x = -1;
        CPU.pc = 0x00;

        CPU.inx();
        assertEquals(0x00, CPU.x);
        assertEquals(0x01, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void inxMustIncrementRegisterXBy1AndSetNegativeFlagIfNewXIsNegative() {
        CPU.x = -2;
        CPU.pc = 0x00;

        CPU.inx();
        assertEquals(-1, CPU.x);
        assertEquals(0x01, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void bneMustMoveProgramCountByGivenOffsetIfZeroFlagIsUnset() {
        CPU.pc = 0x00;
        CPU.unsetStatusFlag(CPU.STATUS_FLAG_ZERO);


        MMU.writeAddress(0x01, 0x30);

        CPU.bne();

        assertEquals(0x32, CPU.pc);
    }

    @Test
    void bneMustMoveProgramCountBy2IfZeroFlagIsSet() {
        CPU.pc = 0x00;
        CPU.setStatusFlag(CPU.STATUS_FLAG_ZERO);

        MMU.writeAddress(0x01, 0x30);

        CPU.bne();

        assertEquals(0x02, CPU.pc);
    }

    @Test
    void ldaAbsoluteXMustLoadUnsignedValueFromMemoryPositionOffsetByXToRegisterA() {
        var value = 0xFD;
        CPU.a = 0x00;
        CPU.x = 0x03;
        CPU.pc = 0x00;

        MMU.writeAddress(0x01, 0xCD);
        MMU.writeAddress(0x02, 0xAB);
        MMU.writeAddress(0xABD0, value);

        CPU.ldaAbsoluteX();

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

        CPU.ldaAbsoluteX();

        assertEquals(value, CPU.a);
        assertEquals(0x03, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void ldaAbsoluteXMustSetNegativeFlagIfValueLoadedToRegisterAIsNegative() {
        var value = -60;
        CPU.a = 0x00;
        CPU.x = 0x03;
        CPU.pc = 0x00;

        MMU.writeAddress(0x01, 0xCD);
        MMU.writeAddress(0x02, 0xAB);
        MMU.writeAddress(0xABD0, value);

        CPU.ldaAbsoluteX();

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

        CPU.cpyImmediate();

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

        CPU.cpyImmediate();

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

        CPU.cpyImmediate();

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

        CPU.cmpAbsoluteX();

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

        CPU.cmpAbsoluteX();

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

        CPU.cmpAbsoluteX();

        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_CARRY));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
        assertEquals(0x03, CPU.pc);
    }

    @Test
    void dexMustDecrementRegisterXBy1() {
        CPU.x = 0x09;
        CPU.pc = 0x00;

        CPU.dex();

        assertEquals(0x08, CPU.x);
        assertEquals(0x01, CPU.pc);
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void dexMustDecrementRegisterXBy1AndSetZeroFlagIfNewXIsZero() {
        CPU.x = 0x01;
        CPU.pc = 0x00;

        CPU.dex();
        assertEquals(0x00, CPU.x);
        assertEquals(0x01, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_ZERO));
        assertFalse(CPU.isStatusFlagSet(CPU.STATUS_FLAG_NEGATIVE));
    }

    @Test
    void dexMustDecrementRegisterXBy1AndSetNegativeFlagIfNewXIsNegative() {
        CPU.x = 0x00;
        CPU.pc = 0x00;

        CPU.dex();

        assertEquals(-1, CPU.x);
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
        int oldPCByte1 = (CPU.pc & 0xFF);
        int oldPCByte2 = ((CPU.pc >> 8) & 0xFF);

        CPU.brk();

        assertEquals(oldStackPointer - 3, CPU.s);
        assertEquals(oldProcessorStatus, MMU.readAddress(0x0100 + CPU.s + 1));
        assertEquals(oldPCByte2, MMU.readAddress(0x0100 + CPU.s + 2));
        assertEquals(oldPCByte1, MMU.readAddress(0x0100 + CPU.s + 3));
        assertEquals(0xABCD, CPU.pc);
        assertTrue(CPU.isStatusFlagSet(CPU.STATUS_FLAG_BREAK));
    }

    @Test
    void jmpAbsoluteMustMoveProgramCounterToGivenAddress() {
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, 0xAD);
        MMU.writeAddress(0x02, 0xDE);

        CPU.jmpAbsolute();

        assertEquals(0xDEAD, CPU.pc);
    }

    @Test
    void bccMustDisplaceProgramCounterByGivenOffsetIfCarryFlagIsClear() {
        var offset = -3;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, offset);
        CPU.unsetStatusFlag(CPU.STATUS_FLAG_CARRY);

        CPU.bcc();

        assertEquals(offset + 2, CPU.pc);
    }

    @Test
    void bccMustDisplaceProgramCounterBy2IfCarryFlagIsSet() {
        var offset = -3;
        CPU.pc = 0x00;
        MMU.writeAddress(0x01, offset);
        CPU.setStatusFlag(CPU.STATUS_FLAG_CARRY);

        CPU.bcc();

        assertEquals(0x02, CPU.pc);
    }
}