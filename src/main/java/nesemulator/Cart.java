package nesemulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Cart {

    static final Logger logger = LoggerFactory.getLogger(Cart.class);

    int boardModel;
    byte[] prgROM;
    byte[] chrROM;
    byte[] chrRAM; // TODO: Load CHR RAM when adequate

    public static Cart fromROMFile(String romFilePath) throws IOException {
        byte[] romFileData = Files.readAllBytes(Path.of(romFilePath));
        if (isNes10Format(romFileData)) {
            return fromNes10ROM(romFileData);
        }
        return new Cart();
    }

    private static Cart fromNes10ROM(byte[] romFileData) {
        var cart = new Cart();
        byte[] header = Arrays.copyOfRange(romFileData, 0, 15);
        int prgROMSize = header[4] * 16 * 1024;
        int chrROMSize = header[5] * 8 * 1024;
        parseFlags(cart, header[6]);

        logger.info(String.format("Header flags: %X", header[6]));
        logger.info("PRG ROM size (bytes): {}", prgROMSize);
        logger.info("CHR ROM size (bytes): {}", chrROMSize);

        int prgStartByte = 16;
        int prgEndByte = prgStartByte + prgROMSize;

        cart.prgROM = Arrays.copyOfRange(romFileData, prgStartByte, prgEndByte);

        if (chrROMSize > 0) {
            cart.chrROM = Arrays.copyOfRange(romFileData, prgEndByte + 1, prgEndByte + chrROMSize + 1);
            cart.chrRAM = new byte[0];
        }

        return cart;
    }

    private static void parseFlags(Cart cart, byte b) {
        cart.boardModel = ((b >> 4) & 1) == 0 ? 0 : 1;
    }

    private static boolean isNes10Format(byte[] fileData) {
        return fileData[0] == 'N' && fileData[1] == 'E' && fileData[2] == 'S' && fileData[3] == 0x1A;
    }
}
