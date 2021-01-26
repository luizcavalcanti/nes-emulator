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
    void initializeShouldResetAllData() {
        assertEquals(0x00, PPU.control);
        assertEquals(0x00, PPU.oamAddress);
        assertEquals(0x00, PPU.mask);
        assertEquals((byte) 0b10000000, PPU.status);
    }

    @Test
    void writeAddressShouldSetControlByteIfSentToAddress0x2000() {
        PPU.write(0x2000, (byte) 0xAB);

        assertEquals((byte) 0xAB, PPU.control);
    }

    @Test
    void writeAddressShouldSetMaskByteIfSentToAddress0x2001() {
        PPU.write(0x2001, (byte) 0xBC);

        assertEquals((byte) 0xBC, PPU.mask);
    }

    @Test
    void writeAddressShouldSetOAMAddressByteIfSentToAddress0x2003() {
        PPU.write(0x2003, (byte) 0xDE);

        assertEquals((byte) 0xDE, PPU.oamAddress);
    }

    @Test
    void readShouldReturnTheControlByteIfRequiredAddressIs0x2000() {
        assertEquals(0x00, PPU.control);

        PPU.control = (byte) 0xAA;

        assertEquals((byte) 0xAA, PPU.read(0x2000));
    }

    @Test
    void readShouldReturnTheControlByteIfRequiredAddressIs0x2001() {
        assertEquals(0x00, PPU.mask);

        PPU.mask = (byte) 0xBB;

        assertEquals((byte) 0xBB, PPU.read(0x2001));
    }

    @Test
    void readShouldReturnTheStatusByteIfRequiredAddressIs0x2002() {
        PPU.status = (byte) 0xCC;

        assertEquals((byte) 0xCC, PPU.read(0x2002));
    }

    @Test
    void readShouldReturnTheOAMAddressByteIfRequiredAddressIs0x2003() {
        assertEquals(0x00, PPU.oamAddress);

        PPU.oamAddress = (byte) 0xDD;

        assertEquals((byte) 0xDD, PPU.read(0x2003));
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