package nesemulator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MMUTest {

    @Test
    void readAddressFromMirroredCPURAMMustPointToActualRAM() {
        int value = 0xAB;
        MMU.initialize();
        MMU.writeAddress(0x0173, value);

        assertEquals(value, MMU.readAddress(0x0973));
        assertEquals(value, MMU.readAddress(0x1173));
        assertEquals(value, MMU.readAddress(0x1973));
    }

    @Test
    void readAddressFromMirroredPPUPortsMustPointToActualPPUPorts() {
        int value = 0x1A;
        MMU.initialize();

        // TODO change when we stop mocking PPU response
        MMU.writeAddress(0x2003, value);

        assertEquals(value, MMU.readAddress(0x2003));
        assertEquals(value, MMU.readAddress(0x200B));
        assertEquals(value, MMU.readAddress(0x2013));
        assertEquals(value, MMU.readAddress(0x202B));
        assertEquals(value, MMU.readAddress(0x2033));
        assertEquals(value, MMU.readAddress(0x204B));
        assertEquals(value, MMU.readAddress(0x2053));
        assertEquals(value, MMU.readAddress(0x206B));
        //...
        assertEquals(value, MMU.readAddress(0x3FE3));
        assertEquals(value, MMU.readAddress(0x3FFB));
    }

}