package nesemulator;

public class PPU {

    static byte[] ram;

    static byte control;
    static byte oamAddress;
    static byte mask;
    static byte status;
    static byte oamData;

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

    private PPU() {
    }

    public static void initialize() {
        ram = new byte[2048];
        control = (byte) 0b00000000;
        oamAddress = (byte) 0b00000000;
        oamData = (byte) 0b00000000;
        mask = (byte) 0b00000000;
        status = (byte) 0b10000000;
    }

//    public static void execute() {
//    }

    public static void write(int address, byte data) {
        switch (address) {
            case 0x2000:
                writeControl(data);
                break;
            case 0x2001:
                mask = data;
                break;
            case 0x2003:
                oamAddress = data;
                break;
            case 0x2004:
                writeOAMData(data);
                break;
            default:
                throw new UnsupportedOperationException(String.format("You cannot write address $%04X on PPU", address));
        }
    }

    public static byte read(final int address) {
        switch (address) {
            case 0x2002:
                return readStatus();
            case 0x2004:
                return oamData;
            default:
                throw new UnsupportedOperationException(String.format("You cannot read address $%04X on PPU", address));
        }
    }

    public static byte inspect(final int address) {
        switch (address) {
            case 0x2000:
                return control;
            case 0x2001:
                return mask;
            case 0x2002:
                return status;
            case 0x2003:
                return oamAddress;
            case 0x2004:
                return oamData;
            default:
                throw new UnsupportedOperationException(String.format("You cannot inspect address $%04X on PPU", address));
        }
    }

    private static byte readStatus() {
        // TODO: status manipulation by reading this address
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

    static boolean isBitSet(byte value, int bitIndex) {
        return ((value & 0xff) & (1 << (bitIndex))) > 0;
    }

}
