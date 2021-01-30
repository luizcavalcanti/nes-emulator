package nesemulator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MMUTest {

    @BeforeEach
    void setUp() {
        MMU.initialize();
    }

    @Test
    void readAddressFromMirroredCPURAMMustPointToActualRAM() {
        int value = 0xAB;
        MMU.writeAddress(0x0173, value);

        assertEquals(value, MMU.readAddress(0x0973));
        assertEquals(value, MMU.readAddress(0x1173));
        assertEquals(value, MMU.readAddress(0x1973));
    }

    @Test
    void readAddressFromMirroredPPUPortsMustPointToActualPPUPorts() {
        int value = 0x1A;

        // TODO change when we stop mocking PPU response
        MMU.writeAddress(0x2004, value);

        assertEquals(value, MMU.readAddress(0x2004));
        assertEquals(value, MMU.readAddress(0x200C));
        assertEquals(value, MMU.readAddress(0x2014));
        assertEquals(value, MMU.readAddress(0x202C));
        assertEquals(value, MMU.readAddress(0x2034));
        assertEquals(value, MMU.readAddress(0x204C));
        assertEquals(value, MMU.readAddress(0x2054));
        assertEquals(value, MMU.readAddress(0x206C));
        //...
        assertEquals(value, MMU.readAddress(0x3FE4));
        assertEquals(value, MMU.readAddress(0x3FFC));
    }

    @Test
    void loadCartShouldMirrorPRGROMIfBoardModelIsZero() {
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

    @Test
    void writeAddressShouldWriteToPPUIfAddressIsBetween0x2000And0x3FFF() {
        PPU.initialize();
        MMU.writeAddress(0x2000, 0xAB);
        assertEquals((byte) 0xAB, PPU.control);
    }
}