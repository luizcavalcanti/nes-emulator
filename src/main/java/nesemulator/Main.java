package nesemulator;

import nesemulator.cpu.CPU;
import nesemulator.cpu.CPUObserver;
import nesemulator.cpu.Opcode;
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

            logger.info("Loading {}...", romFileName);
            Cart cart = Cart.fromROMFile(romFileName);
            MMU.loadCart(cart);
            logger.info("Initializing CPU...");
            CPU.initialize();
//            addLogObserver();
            logger.info("Executing...");
            CPU.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void addLogObserver() {
        CPU.addObserver(new CPUObserver() {
            @Override
            public void notifyCPUInstruction(int programCount, Opcode opcode, int cycles, int... operands) {
                if (operands.length == 0) {
                    logger.info(String.format("%04X: %s (%d cycles)", programCount, opcode.name(), cycles));
                } else {
                    logger.info(String.format("%04X: %s %s (%d cycles)", programCount, opcode.name(), operands.toString(), cycles));
                }
            }
        });
    }
}