package nesemulator;

public class MMU {

    private MMU() {
    }

    private static final int INTIAL_CART_PRG_ROM_ADDRESS = 0x8000;
    private static final int WHOLE_MEMORY_SIZE = 0x10001;
    private static final int PPU_PORTS_START = 0x2000;
    private static final int PPU_PORTS_END = 0x2007;

    static int[] memory = new int[WHOLE_MEMORY_SIZE];

    public static void loadCart(Cart cart) {
        for (int i = 0; i < cart.prgROM.length; i++) {
            memory[INTIAL_CART_PRG_ROM_ADDRESS + i] = (int) cart.prgROM[i];
        }

//        for (int i = 0; i < cart.chrROM.length; i++) {
//            memory[INTIAL_CART_PRG_ROM_ADDRESS + i] = (int) cart.prgROM[i];
//        }
    }

    public static int readAddress(int address) {
        // TODO: Move feature to PPU
        if (address == 0x2002) {
            return (byte) 0b10010000;
        }
        return memory[address];
    }

    public static void writeAddress(int address, int value) {
        memory[address] = value;
        if (address >= PPU_PORTS_START && address <= PPU_PORTS_END) {
            //TODO call PPU
        }
    }
}
