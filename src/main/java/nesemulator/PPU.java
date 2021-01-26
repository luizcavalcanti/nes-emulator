package nesemulator;

public class PPU {

    static byte control;
    static byte oamAddress;
    static byte mask;
    static byte status;

    private static final int CONTROL_BIT_NMI_ENABLE = 7;
    private static final int CONTROL_BIT_PPU_MASTER_SLAVE = 6;
    private static final int CONTROL_BIT_SPRITE_HEIGHT = 5;
    private static final int CONTROL_BIT_BACKGROUND_TILE_SELECT = 4;
    private static final int CONTROL_BIT_SPRITE_TILE_SELECT = 3;
    private static final int CONTROL_BIT_INCREMENT_MODE = 2;
    private static final int CONTROL_BIT_NAME_TABLE_ADDRESS_1 = 1;
    private static final int CONTROL_BIT_NAME_TABLE_ADDRESS_2 = 0;

    public static void initialize() {
        control = (byte) 0b00000000;
        oamAddress = (byte) 0b00000000;
        mask = (byte) 0b00000000;
        status = (byte) 0b10000000;
    }

    public static void write(int address, byte data) {
        switch (address) {
            case 0x2000:
                control = data;
                break;
            case 0x2001:
                mask = data;
                break;
            case 0x2003:
                oamAddress = data;
                break;
            default:
                throw new UnsupportedOperationException(String.format("You cannot write address $%04X on PPU", address));
        }
    }

    public static byte read(final int address) {
        switch (address) {
            case 0x2000:
                return control;
            case 0x2001:
                return mask;
            case 0x2002:
                return status;// TODO: status manipulation by reading this address
            case 0x2003:
                return oamAddress;
            default:
                throw new UnsupportedOperationException(String.format("You cannot read address $%04X on PPU", address));
        }
    }

    static boolean isBitSet(byte value, int bitIndex) {
        return (value & (1 << (bitIndex))) > 0;
    }
}
