package nesemulator;

public class MMU {

    private MMU() {
    }

    private static final int INTIAL_CART_PRG_ROM_ADDRESS = 0x8000;
    private static final int WHOLE_MEMORY_SIZE = 0x10001;
    private static final int CPU_RAM_UPPER_LIMIT = 0x1FFF;
    private static final int PPU_PORTS_INITIAL_ADDRESS = 0x2000;
    private static final int PPU_PORTS_UPPER_LIMIT = 0x3FFF;
    private static final int PPU_PORTS_COUNT = 0x08;

    static int[] memory = new int[WHOLE_MEMORY_SIZE];

    public static void initialize() {
        memory = new int[WHOLE_MEMORY_SIZE];
    }

    public static void loadCart(Cart cart) {
        for (int i = 0; i < cart.prgROM.length; i++) {
            memory[INTIAL_CART_PRG_ROM_ADDRESS + i] = (int) cart.prgROM[i];
        }
    }

    public static int readAddress(int address) {
        if (address <= CPU_RAM_UPPER_LIMIT) { //CPU RAM Mirroring
            return memory[address % 2048];
        }

        if (address >= PPU_PORTS_INITIAL_ADDRESS && address <= PPU_PORTS_UPPER_LIMIT) {
            address = PPU_PORTS_INITIAL_ADDRESS + (address % PPU_PORTS_COUNT);
            if (address == 0x2002) {// TODO: Move feature to PPU
                return (byte) 0b10010000;
            } else {
                return memory[address];
            }
        }
        return memory[address];
    }

    public static void writeAddress(int address, int value) {
        memory[address] = value;
        if (address >= PPU_PORTS_INITIAL_ADDRESS && address <= PPU_PORTS_UPPER_LIMIT) {
            //TODO call PPU
        }
    }
}
