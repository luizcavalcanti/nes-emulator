package nesemulator;

public class MMU {

    private MMU() {
    }

    private static final int INTIAL_CART_PRG_ROM_ADDRESS = 0x8000;
    private static final int INTIAL_CART_PRG_ROM_MIRROR_ADDRESS = 0xC000;
    private static final int WHOLE_MEMORY_SIZE = 0x10001;
    private static final int CPU_RAM_UPPER_LIMIT = 0x1FFF;
    private static final int PPU_PORTS_INITIAL_ADDRESS = 0x2000;
    private static final int PPU_PORTS_UPPER_ADDRESS = 0x3FFF;
    private static final int PPU_PORTS_COUNT = 0x08;

    static int[] memory = new int[WHOLE_MEMORY_SIZE];

    public static void initialize() {
        memory = new int[WHOLE_MEMORY_SIZE];
    }

    public static void loadCart(Cart cart) {
        boolean mirror = false;
        if (cart.boardModel == 0 && cart.prgROM.length <= 16 * 1024) {
            mirror = true;
        }

        for (int i = 0; i < cart.prgROM.length; i++) {
            memory[INTIAL_CART_PRG_ROM_ADDRESS + i] = (int) cart.prgROM[i];
            if (mirror) {
                memory[INTIAL_CART_PRG_ROM_MIRROR_ADDRESS + i] = (int) cart.prgROM[i];
            }
        }
    }

    public static int readAddress(int address) {
        if (address <= CPU_RAM_UPPER_LIMIT) { //CPU RAM Mirroring
            return memory[address % 2048];
        }

        if (isPPUAddress(address)) {
            address = getMirroredPPUAddress(address);
            if (address == 0x2002) {// TODO: Move feature to PPU
                return (byte) 0b10000000;
            } else {
                return PPU.read(address);
            }
        }

        return memory[address];
    }

    public static void writeAddress(int address, int value) {
        if (isPPUAddress(address)) {
            PPU.write(getMirroredPPUAddress(address), (byte) value);
        } else {
            if (address <= CPU_RAM_UPPER_LIMIT) { //CPU RAM Mirroring
                address = address % 2048;
            }
            memory[address] = value;
        }
    }

    private static int getMirroredPPUAddress(int address) {
        return PPU_PORTS_INITIAL_ADDRESS + (address % PPU_PORTS_COUNT);
    }

    private static boolean isPPUAddress(int address) {
        return address >= PPU_PORTS_INITIAL_ADDRESS && address <= PPU_PORTS_UPPER_ADDRESS;
    }
}
