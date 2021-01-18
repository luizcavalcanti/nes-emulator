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

    @Test
    void loadCartShouldMirrorPRGROMIfBoardModelIsZero() {
        MMU.initialize();
        Cart cart = new Cart();
        cart.prgROM = new byte[10];
        for (int i = 0; i < 10; i++) {
            cart.prgROM[i] = (byte) i;
        }
        cart.boardModel = 0;

        MMU.loadCart(cart);

        // Default addresses
        assertEquals(0x00, MMU.readAddress(0x8000));
        assertEquals(0x01, MMU.readAddress(0x8001));
        assertEquals(0x02, MMU.readAddress(0x8002));
        assertEquals(0x03, MMU.readAddress(0x8003));
        assertEquals(0x04, MMU.readAddress(0x8004));
        assertEquals(0x05, MMU.readAddress(0x8005));
        assertEquals(0x06, MMU.readAddress(0x8006));
        assertEquals(0x07, MMU.readAddress(0x8007));
        assertEquals(0x08, MMU.readAddress(0x8008));
        assertEquals(0x09, MMU.readAddress(0x8009));

        // Mirrored adsresses
        assertEquals(0x00, MMU.readAddress(0xC000));
        assertEquals(0x01, MMU.readAddress(0xC001));
        assertEquals(0x02, MMU.readAddress(0xC002));
        assertEquals(0x03, MMU.readAddress(0xC003));
        assertEquals(0x04, MMU.readAddress(0xC004));
        assertEquals(0x05, MMU.readAddress(0xC005));
        assertEquals(0x06, MMU.readAddress(0xC006));
        assertEquals(0x07, MMU.readAddress(0xC007));
        assertEquals(0x08, MMU.readAddress(0xC008));
        assertEquals(0x09, MMU.readAddress(0xC009));
    }

}