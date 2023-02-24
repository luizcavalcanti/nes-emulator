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
        assertEquals(0x00, PPU.scrollX);
        assertEquals(0x00, PPU.scrollY);
        assertEquals(0x00, PPU.address);

        assertEquals(0x4000, PPU.ram.length);

        assertTrue(PPU.addressClean);
        assertTrue(PPU.scrollClean);
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
    void writeMustSetScrollXValueAndUnclearScrollIfScrollIsClearAndAddressIs0x2005() {
        assertEquals(0x00, PPU.scrollX);
        assertEquals(0x00, PPU.scrollY);

        PPU.scrollClean = true;
        PPU.write(PPU.ADDRESS_PPUSCROLL, (byte) 0xAB);

        assertEquals((byte) 0xAB, PPU.scrollX);
        assertEquals((byte) 0x00, PPU.scrollY);
        assertFalse(PPU.scrollClean);
    }

    @Test
    void writeMustSetScrollYValueAndClearScrollFlagIfScrollIsNotClearAndAddressIs0x2005() {
        assertEquals(0x00, PPU.scrollX);
        assertEquals(0x00, PPU.scrollY);

        PPU.scrollClean = false;
        PPU.write(PPU.ADDRESS_PPUSCROLL, (byte) 0xAB);

        assertEquals((byte) 0x00, PPU.scrollX);
        assertEquals((byte) 0xAB, PPU.scrollY);
        assertTrue(PPU.scrollClean);
    }

    @Test
    void writeMustSetUpperByteOnAddressAndUncleanAddressFlagIfAddressFlagIsCleanAndWriteAddressIs0x2006() {
        assertEquals(0x0000, PPU.address);

        PPU.write(PPU.ADDRESS_PPUADDR, (byte) 0xAB);

        assertEquals(0xAB00, PPU.address);
        assertFalse(PPU.addressClean);
    }

    @Test
    void writeMustSetLowerByteOnAddressAndCleanAddressFlagIfAddressFlagIsUncleanAndWriteAddressIs0x2006() {
        assertEquals(0x0000, PPU.address);

        PPU.write(PPU.ADDRESS_PPUADDR, (byte) 0xAB);
        PPU.write(PPU.ADDRESS_PPUADDR, (byte) 0xCD);

        assertEquals(0xABCD, PPU.address);
        assertTrue(PPU.addressClean);
    }

    @Test
    void writeMustSetDataToCurrentVRAMAddressAndIncrementAddressPointerBy1IfIncrementFlagIsNotSetAndWriteAddressIs0x2007() {
        assertEquals(0x0000, PPU.address);
        PPU.write(PPU.ADDRESS_PPUADDR, (byte) 0x12);
        PPU.write(PPU.ADDRESS_PPUADDR, (byte) 0x34);
        assertEquals(0x1234, PPU.address);

        PPU.write(PPU.ADDRESS_PPUCTRL, (byte) 0b00000000);

        assertEquals(0x00, PPU.ram[0x1234]);
        PPU.write(PPU.ADDRESS_PPUDATA, (byte) 0xAA);
        assertEquals(0xAA, PPU.ram[0x1234]);
        assertEquals(0x1235, PPU.address);
    }

    @Test
    void writeMustSetDataToCurrentVRAMAddressAndIncrementAddressPointerBy32IfIncrementFlagIsSetAndWriteAddressIs0x2007() {
        assertEquals(0x0000, PPU.address);
        PPU.write(PPU.ADDRESS_PPUADDR, (byte) 0x12);
        PPU.write(PPU.ADDRESS_PPUADDR, (byte) 0x34);
        assertEquals(0x1234, PPU.address);

        PPU.write(PPU.ADDRESS_PPUCTRL, (byte) 0b00000100);

        assertEquals(0x00, PPU.ram[0x1234]);
        PPU.write(PPU.ADDRESS_PPUDATA, (byte) 0xAA);
        assertEquals(0xAA, PPU.ram[0x1234]);
        assertEquals(0x1254, PPU.address);
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

    @Test
    void getBaseNametableAddressShouldReturn$2000IfNametableIs0() {
        PPU.control = (byte) 0b10000000;
        assertEquals(0x2000, PPU.getBaseNametableAddress());
    }

    @Test
    void getBaseNametableAddressShouldReturn$2400IfNametableIs1() {
        PPU.control = (byte) 0b10000001;
        assertEquals(0x2400, PPU.getBaseNametableAddress());
    }

    @Test
    void getBaseNametableAddressShouldReturn$2800IfNametableIs2() {
        PPU.control = (byte) 0b10000010;
        assertEquals(0x2800, PPU.getBaseNametableAddress());
    }

    @Test
    void getBaseNametableAddressShouldReturn$2C00IfNametableIs3() {
        PPU.control = (byte) 0b10000011;
        assertEquals(0x2C00, PPU.getBaseNametableAddress());
    }
}