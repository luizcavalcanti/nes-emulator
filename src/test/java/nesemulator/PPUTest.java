package nesemulator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PPUTest {

    @BeforeEach
    void setUp() {
        PPU.initialize();
    }

    @Test
    void initializeMustResetAllData() {
        assertEquals(0x00, PPU.control);
        assertEquals(0x00, PPU.oamAddress);
        assertEquals(0x00, PPU.mask);
        assertEquals(0x00, PPU.oamData);
        assertEquals((byte) 0b10000000, PPU.status);
        assertEquals(2048, PPU.ram.length);
    }

    @Test
    void writeMustSetControlByteIfSentToAddress0x2000() {
        PPU.write(0x2000, (byte) 0xAB);

        assertEquals((byte) 0xAB, PPU.control);
    }

    @Test
    void writeMustShouldSetMaskByteIfSentToAddress0x2001() {
        PPU.write(0x2001, (byte) 0xBC);

        assertEquals((byte) 0xBC, PPU.mask);
    }

    @Test
    void writeMustSetOAMAddressByteIfSentToAddress0x2003() {
        PPU.write(0x2003, (byte) 0xDE);

        assertEquals((byte) 0xDE, PPU.oamAddress);
    }

    @Test
    void writeMustSetOAMDataByteAndIncreaseOAMAddressIfAddressIs0x2004() {
        PPU.oamAddress = 0x12;
        PPU.write(0x2004, (byte) 0xAB);

        assertEquals((byte) 0xAB, PPU.read(0x2004));
        assertEquals((byte) 0x13, PPU.oamAddress);
    }

    @Test
    void readMustReturnTheStatusByteIfRequiredAddressIs0x2002() {
        PPU.status = (byte) 0xCC;

        assertEquals((byte) 0xCC, PPU.read(0x2002));
    }

    @Test
    void readMustReturnTheOAMDataByteIfRequiredAddressIs0x2004() {
        assertEquals(0x00, PPU.mask);

        PPU.oamData = (byte) 0xBB;

        assertEquals((byte) 0xBB, PPU.read(0x2004));
    }

    @Test
    void isBitSetShouldReturnTrueWhenBitIsSetAndFalseWhenNot() {
        assertFalse(PPU.isBitSet((byte) 0x00, 0));
        assertFalse(PPU.isBitSet((byte) 0x00, 1));
        assertFalse(PPU.isBitSet((byte) 0x00, 2));
        assertFalse(PPU.isBitSet((byte) 0x00, 3));
        assertFalse(PPU.isBitSet((byte) 0x00, 4));
        assertFalse(PPU.isBitSet((byte) 0x00, 5));
        assertFalse(PPU.isBitSet((byte) 0x00, 6));
        assertFalse(PPU.isBitSet((byte) 0x00, 7));

        assertTrue(PPU.isBitSet((byte) 0xAB, 0));
        assertTrue(PPU.isBitSet((byte) 0xAB, 1));
        assertFalse(PPU.isBitSet((byte) 0xAB, 2));
        assertTrue(PPU.isBitSet((byte) 0xAB, 3));
        assertFalse(PPU.isBitSet((byte) 0xAB, 4));
        assertTrue(PPU.isBitSet((byte) 0xAB, 5));
        assertFalse(PPU.isBitSet((byte) 0xAB, 6));
        assertTrue(PPU.isBitSet((byte) 0xAB, 7));

        assertTrue(PPU.isBitSet((byte) 0xFF, 0));
        assertTrue(PPU.isBitSet((byte) 0xFF, 1));
        assertTrue(PPU.isBitSet((byte) 0xFF, 2));
        assertTrue(PPU.isBitSet((byte) 0xFF, 3));
        assertTrue(PPU.isBitSet((byte) 0xFF, 4));
        assertTrue(PPU.isBitSet((byte) 0xFF, 5));
        assertTrue(PPU.isBitSet((byte) 0xFF, 6));
        assertTrue(PPU.isBitSet((byte) 0xFF, 7));
    }

}