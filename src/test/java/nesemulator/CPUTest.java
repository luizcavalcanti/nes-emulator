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
    }

    @Test
    void initializeMustSetStackPointerToHigherAddress() {
        assertEquals(0x0200, CPU.s);
    }

    @Test
    void initializeMustSetProcessorStatusToIRQDisabled() {
        assertEquals(0x34, CPU.p);
    }

    @Test
    void initializeMustCleanFlags() {
        assertFalse(CPU.decimalFlag);
        assertFalse(CPU.interruptFlag);
        assertFalse(CPU.negativeFlag);
        assertFalse(CPU.zeroFlag);
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
        assertFalse(CPU.zeroFlag);
        assertFalse(CPU.negativeFlag);
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
        assertTrue(CPU.negativeFlag);
        assertFalse(CPU.zeroFlag);
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
        assertFalse(CPU.negativeFlag);
        assertTrue(CPU.zeroFlag);
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
        assertFalse(CPU.zeroFlag);
        assertFalse(CPU.negativeFlag);
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
        assertTrue(CPU.zeroFlag);
        assertFalse(CPU.negativeFlag);
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
        assertFalse(CPU.zeroFlag);
        assertTrue(CPU.negativeFlag);
    }

    @Test
    void bplMustMoveProgramCountByGivenOffsetIfNegativeFlagIsUnset() {
        CPU.a = 0x00;
        CPU.pc = 0x00;
        CPU.negativeFlag = false;

        MMU.writeAddress(0x01, 0x30);

        CPU.bpl();

        assertEquals(0x32, CPU.pc);
    }

    @Test
    void bplMustMoveProgramCountBy2IfNegativeFlagIsSet() {
        CPU.a = 0x00;
        CPU.pc = 0x00;
        CPU.negativeFlag = true;

        MMU.writeAddress(0x01, 0x30);

        CPU.bpl();

        assertEquals(0x02, CPU.pc);
    }

    @Test
    void seiMustSetInterruptFlag() {
        CPU.pc = 0x00;

        CPU.sei();
        assertTrue(CPU.interruptFlag);
        assertEquals(0x01, CPU.pc);

        // make sure is idempotent
        CPU.sei();
        assertTrue(CPU.interruptFlag);
        assertEquals(0x02, CPU.pc);
    }

    @Test
    void cldMustClearDecimalFlag() {
        CPU.pc = 0x00;
        CPU.decimalFlag = true;

        CPU.cld();
        assertFalse(CPU.decimalFlag);
        assertEquals(0x01, CPU.pc);

        // make sure is idempotent
        CPU.cld();
        assertFalse(CPU.decimalFlag);
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
        assertFalse(CPU.zeroFlag);
        assertFalse(CPU.negativeFlag);
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
        assertTrue(CPU.negativeFlag);
        assertFalse(CPU.zeroFlag);
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
        assertFalse(CPU.negativeFlag);
        assertTrue(CPU.zeroFlag);
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

}