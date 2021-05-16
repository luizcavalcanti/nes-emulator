package nesemulator;

import nesemulator.cpu.CPU;
import nesemulator.cpu.observer.LogCPUObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");
    }

    static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            String romFileName = "balloon.nes";

            logger.info("Initializing Hardware...");
            PPU.initialize();
            MMU.initialize();
            CPU.initialize();
            CPU.addObserver(new LogCPUObserver());

            logger.info("Loading {}...", romFileName);
            Cart cart = Cart.fromROMFile(romFileName);
            MMU.loadCart(cart);

            logger.info("Executing...");
            CPU.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}