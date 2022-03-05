package nesemulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PPU {

    private static final Logger logger = LoggerFactory.getLogger(PPU.class);

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

    private static final int INTADDR_PATTERN_TABLE_0_START = 0x0000;
    private static final int INTADDR_PATTERN_TABLE_0_END = 0x0FFF;
    private static final int INTADDR_PATTERN_TABLE_1_START = 0x1000;
    private static final int INTADDR_PATTERN_TABLE_1_END = 0x1FFF;

    private static final int INTADDR_NAMETABLE_0_START = 0x2000;
    private static final int INTADDR_NAMETABLE_0_END = 0x23FF;
    private static final int INTADDR_NAMETABLE_1_START = 0x2400;
    private static final int INTADDR_NAMETABLE_1_END = 0x27FF;
    private static final int INTADDR_NAMETABLE_2_START = 0x2800;
    private static final int INTADDR_NAMETABLE_2_END = 0x2BFF;
    private static final int INTADDR_NAMETABLE_3_START = 0x2C00;
    private static final int INTADDR_NAMETABLE_3_END = 0x2FFF;
    private static final int INTADDR_NAMETABLE_MIRROR_1_START = 0x3000;
    private static final int INTADDR_NAMETABLE_MIRROR_1_END = 0x3EFF;

    private static final int INTADDR_PALETTE_RAM_START = 0x3F00;
    private static final int INTADDR_UNIVERSAL_BACKGROUND_COLOR = 0x3F00;
    private static final int INTADDR_BACKGROUND_PALETTE_0_START = 0x3F01;
    private static final int INTADDR_BACKGROUND_PALETTE_0_END = 0x3F03;
    private static final int INTADDR_BACKGROUND_PALETTE_1_START = 0x3F05;
    private static final int INTADDR_BACKGROUND_PALETTE_1_END = 0x3F07;
    private static final int INTADDR_BACKGROUND_PALETTE_2_START = 0x3F09;
    private static final int INTADDR_BACKGROUND_PALETTE_2_END = 0x3F0B;
    private static final int INTADDR_BACKGROUND_PALETTE_3_START = 0x3F0D;
    private static final int INTADDR_BACKGROUND_PALETTE_3_END = 0x3F0F;
    private static final int INTADDR_SPRITE_PALETTE_0_START = 0x3F11;
    private static final int INTADDR_SPRITE_PALETTE_0_END = 0x3F13;
    private static final int INTADDR_SPRITE_PALETTE_1_START = 0x3F15;
    private static final int INTADDR_SPRITE_PALETTE_1_END = 0x3F17;
    private static final int INTADDR_SPRITE_PALETTE_2_START = 0x3F19;
    private static final int INTADDR_SPRITE_PALETTE_2_END = 0x3F1B;
    private static final int INTADDR_SPRITE_PALETTE_3_START = 0x3F1D;
    private static final int INTADDR_SPRITE_PALETTE_3_END = 0x3F1F;
    private static final int INTADDR_PALETTE_RAM_END = 0x3F1F;

    private static final int INTADDR_NAMETABLE_MIRROR_2_START = 0x3F20;
    private static final int INTADDR_NAMETABLE_MIRROR_2_END = 0x3FFF;

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

    private static final int STATUS_BIT_VBLANK = 7;
    private static final int STATUS_SPRITE_0_HIT = 6;
    private static final int STATUS_SPRITE_OVERFLOW = 5;

    static int[] ram;

    static byte control;
    static byte oamAddress;
    static byte mask;
    static byte status;
    static byte oamData;
    static byte scrollX;
    static byte scrollY;
    static int address;

    protected static boolean scrollClean;
    protected static boolean addressClean;
    protected static int clock;
    public static BufferedImage screen;

    //- 341 PPU cycles per line;
    //- 262 lines;
    //- 60 frames per second.

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

    public static void executeStep(int cpuCycles) {
        clock += cpuCycles;
    }

    public static void write(int address, byte data) {
        logger.info(String.format("PPU WRITTEN: $%04X 0x%04X (%s)", address, data, intToByteBinary(data)));
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
        logger.info(String.format("PPU READ: $%04X", address));
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

    public static void render() {
        // disable vblank
        status &= ~(1 << STATUS_BIT_VBLANK);
        screen = new BufferedImage(256, 240, BufferedImage.TYPE_3BYTE_BGR);
        screen.getGraphics().drawString("NOT REALLY RENDERING",30, 30);

        // re-enable vblank
        status |= 1 << STATUS_BIT_VBLANK;
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

    private static String intToByteBinary(int value) {
        return String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0');
    }
}
