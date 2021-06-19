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
            processArgs(args);

            logger.info("Initializing Hardware...");
            PPU.initialize();
            MMU.initialize();
            CPU.initialize();
            if (logger.isDebugEnabled()) {
                CPU.addObserver(new LogCPUObserver());
            }

            logger.info("Loading {}...", romFileName);
            Cart cart = Cart.fromROMFile(romFileName);
            MMU.loadCart(cart);

            runEmulator();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processArgs(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("You must provide a .nes rom path in the arguments");
        }
        romFileName = args[0];
    }

    private static void runEmulator() {
        long clock = 0;
        while (true) {
            System.out.print(String.format("Clock: %d\r", clock));
            var cpuCycles = CPU.executeStep();
            PPU.executeStep(cpuCycles);
            clock += cpuCycles;
        }
    }

}