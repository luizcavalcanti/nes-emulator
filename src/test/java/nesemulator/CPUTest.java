package nesemulator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CPUTest {

    @Test
    void initializeMustClearRegisters() {
        CPU.initialize();
        assertEquals(0x00, CPU.a);
        assertEquals(0x00, CPU.x);
        assertEquals(0x00, CPU.y);
    }

    @Test
    void initializeMustAllocateWholeConsoleMemory() {
        CPU.initialize();
        assertEquals(0x10001, CPU.memory.length);
    }

    @Test
    void initializeMustSetStackPointerToHigherAddress() {
        CPU.initialize();
        assertEquals(0x0200, CPU.s);
    }

    @Test
    void initializeMustSetProcessorStatusToIRQDisabled() {
        CPU.initialize();
        assertEquals(0x34, CPU.p);
    }

    @Test
    void initializeMustResetSoundAndJoypadPorts() {
        CPU.initialize();

        assertEquals(0x00, CPU.memory[0x4000]);
        assertEquals(0x00, CPU.memory[0x4001]);
        assertEquals(0x00, CPU.memory[0x4002]);
        assertEquals(0x00, CPU.memory[0x4003]);
        assertEquals(0x00, CPU.memory[0x4004]);
        assertEquals(0x00, CPU.memory[0x4005]);
        assertEquals(0x00, CPU.memory[0x4006]);
        assertEquals(0x00, CPU.memory[0x4007]);
        assertEquals(0x00, CPU.memory[0x4008]);
        assertEquals(0x00, CPU.memory[0x4009]);
        assertEquals(0x00, CPU.memory[0x400A]);
        assertEquals(0x00, CPU.memory[0x400B]);
        assertEquals(0x00, CPU.memory[0x400C]);
        assertEquals(0x00, CPU.memory[0x400D]);
        assertEquals(0x00, CPU.memory[0x400E]);
        assertEquals(0x00, CPU.memory[0x400F]);
        // Boat duel
        assertEquals(0x00, CPU.memory[0x4010]);
        assertEquals(0x00, CPU.memory[0x4011]);
        assertEquals(0x00, CPU.memory[0x4012]);
        assertEquals(0x00, CPU.memory[0x4013]);

        assertEquals(0x00, CPU.memory[0x4015]);
        assertEquals(0x00, CPU.memory[0x4017]);
    }

    @Test
    void ldaImmediateMustLoadUnsignedValueToRegisterA() {
        var value = 0xFD;
        CPU.initialize();
        CPU.a = 0x00;
        CPU.pc = 0x00;
        CPU.memory[0x01] = value;
        CPU.ldaImmediate();

        assertEquals(value, CPU.a);
        assertEquals(0x02, CPU.pc);
        assertFalse(CPU.zeroFlag);
        assertFalse(CPU.negativeFlag);
    }

    @Test
    void ldaImmediateMustSetNegativeFlagIsAIsNegative() {
        CPU.initialize();
        assertFalse(CPU.negativeFlag);
        assertFalse(CPU.zeroFlag);

        CPU.a = 0x00;
        CPU.pc = 0x00;
        CPU.memory[0x01] = -1;
        CPU.ldaImmediate();

        assertTrue(CPU.negativeFlag);
        assertFalse(CPU.zeroFlag);
    }

    @Test
    void ldaImmediateMustSetZeroFlagIsAIsZero() {
        CPU.initialize();
        assertFalse(CPU.negativeFlag);
        assertFalse(CPU.zeroFlag);

        CPU.a = 0x00;
        CPU.pc = 0x00;
        CPU.memory[0x01] = 0x00;
        CPU.ldaImmediate();

        assertFalse(CPU.negativeFlag);
        assertTrue(CPU.zeroFlag);
    }

    @Test
    void staAbsoluteMustStoreTheAccumulatorContentIntoMemory() {
        CPU.initialize();

        CPU.a = 0x99;
        CPU.pc = 0x00;
        CPU.memory[0x01] = 0xCD;
        CPU.memory[0x02] = 0xAB;
        CPU.memory[0xABCD] = 0x00;

        CPU.staAbsolute();

        assertEquals(0x99, CPU.memory[0xABCD]);
        assertEquals(0x03, CPU.pc);
    }
}