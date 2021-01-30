package nesemulator;

public class PPU {

    public static final int ADDRESS_PPUCTRL = 0x2000;
    public static final int ADDRESS_PPUMASK = 0x2001;
    public static final int ADDRESS_PPUSTATUS = 0x2002;
    public static final int ADDRESS_OAMADDR = 0x2003;
    public static final int ADDRESS_OAMDATA = 0x2004;
    public static final int ADDRESS_PPUSCROLL = 0x2005;
    public static final int ADDRESS_PPUADDR = 0x2006;
    public static final int ADDRESS_PPUDATA = 0x2007;
    public static final int ADDRESS_OAMDMA = 0x4014;

    private static final int RAM_SIZE = 0x4000;

    private static final int CONTROL_BIT_NMI_ENABLE = 7;
    private static final int CONTROL_BIT_PPU_MASTER_SLAVE = 6;
    private static final int CONTROL_BIT_SPRITE_HEIGHT = 5;
    private static final int CONTROL_BIT_BACKGROUND_TILE_SELECT = 4;
    private static final int CONTROL_BIT_SPRITE_TILE_SELECT = 3;
    private static final int CONTROL_BIT_INCREMENT_MODE = 2;
    private static final int CONTROL_BIT_NAME_TABLE_ADDRESS_1 = 1;
    private static final int CONTROL_BIT_NAME_TABLE_ADDRESS_2 = 0;

    private static final int MASK_BIT_EMPHASIZE_BLUE = 7;
    private static final int MASK_BIT_EMPHASIZE_GREEN = 6;
    private static final int MASK_BIT_EMPHASIZE_RED = 5;
    private static final int MASK_BIT_SHOW_SPRITES = 4;
    private static final int MASK_BIT_SHOW_BACKGROUND = 3;
    private static final int MASK_BIT_SHOW_SPRITES_LEFMOST = 2;
    private static final int MASK_BIT_SHOW_BACKGROUND_LEFMOST = 1;
    private static final int MASK_BIT_GREYSCALE = 0;

    static int[] ram;

    static byte control;
    static byte oamAddress;
    static byte mask;
    static byte status;
    static byte oamData;
    static byte scrollX;
    static byte scrollY;
    static int address;

    static boolean scrollClean;
    static boolean addressClean;

    private PPU() {
    }

    public static void initialize() {
        ram = new int[RAM_SIZE];
        control = (byte) 0b00000000;
        oamAddress = (byte) 0b00000000;
        oamData = (byte) 0b00000000;
        mask = (byte) 0b00000000;
        status = (byte) 0b10000000;
        scrollX = (byte) 0b00000000;
        scrollY = (byte) 0b00000000;
        address = 0x00;
        scrollClean = true;
        addressClean = true;
    }

    public static void write(int address, byte data) {
        switch (address) {
            case ADDRESS_PPUCTRL:
                writeControl(data);
                break;
            case ADDRESS_PPUMASK:
                mask = data;
                break;
            case ADDRESS_OAMADDR:
                oamAddress = data;
                break;
            case ADDRESS_OAMDATA:
                writeOAMData(data);
                break;
            case ADDRESS_PPUSCROLL:
                writeScroll(data);
                break;
            case ADDRESS_PPUADDR:
                writeAddress(data);
                break;
            case ADDRESS_PPUDATA:
                writePPUData(data);
                break;
//            TODO: case ADDRESS_OAMDMA: writeOAMDMA(data);
            default:
                throw new UnsupportedOperationException(String.format("You cannot write address $%04X on PPU", address));
        }
    }

    public static byte read(final int address) {
        switch (address) {
            case ADDRESS_PPUSTATUS:
                return readStatus();
            case ADDRESS_OAMDATA:
                return oamData;
//            TODO case ADDRESS_PPUDATA: writePPUData(data);
            default:
                throw new UnsupportedOperationException(String.format("You cannot read address $%04X on PPU", address));
        }
    }

    public static byte inspect(final int address) {
        switch (address) {
            case ADDRESS_PPUCTRL:
                return control;
            case ADDRESS_PPUMASK:
                return mask;
            case ADDRESS_PPUSTATUS:
                return status;
            case ADDRESS_OAMADDR:
                return oamAddress;
            case ADDRESS_OAMDATA:
                return oamData;
            default:
                throw new UnsupportedOperationException(String.format("You cannot inspect address $%04X on PPU", address));
        }
    }

    private static byte readStatus() {
        // TODO: status manipulation by reading this address (reset flags, etc)
        return status;
    }

    private static void writeControl(byte data) {
        readStatus();
        control = data;
    }

    private static void writeOAMData(byte data) {
        oamData = data;
        oamAddress++;
    }

    private static void writeScroll(byte data) {
        if (scrollClean) {
            scrollX = data;
            scrollClean = false;
        } else {
            scrollY = data;
            scrollClean = true;
        }
    }

    private static void writeAddress(byte data) {
        if (addressClean) {
            address = (data << 8) & 0xFF00;
            addressClean = false;
        } else {
            address += data & 0xFF;
            addressClean = true;
        }
    }

    private static void writePPUData(byte data) {
        int increment = isBitSet(control, CONTROL_BIT_INCREMENT_MODE) ? 32 : 1;
        ram[address] = data & 0xFF;
        address += increment;
    }

    protected static boolean isBitSet(byte value, int bitIndex) {
        return ((value & 0xff) & (1 << (bitIndex))) > 0;
    }
}
