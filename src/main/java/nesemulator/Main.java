package nesemulator;

import nesemulator.cpu.CPU;
import nesemulator.cpu.observer.LogCPUObserver;
import nesemulator.ui.ScreenUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Main {

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");
    }

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int framesPerSecond = 60;
    private static final long cpuTicksPerSecond = 1_789_773; //4194304
    private static final int kMillisPerFrame = 1000 / framesPerSecond;
    private static final long cpuTicksPerFrame = cpuTicksPerSecond / framesPerSecond;

    private static String romFileName;
    private static boolean running;

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
        ScreenUI ui = new ScreenUI("NES Emulator - " + romFileName);
        ui.setVisible(true);
        ui.pack();

        long clock = 0;
        running = true;
        while (running) {
//            logger.debug(String.format("Clock: %d \tFrames: %d\r", clock, PPU.frames));

            var cpuCycles = CPU.executeStep();
            PPU.executeStep(cpuCycles);
            clock += cpuCycles;

            if (clock >= cpuTicksPerFrame) {
                clock -= cpuTicksPerFrame;
                // read input
                // render screen
//                label.setIcon(new ImageIcon(PPU.render()));
//                label.repaint();
                ui.updateScreen(PPU.screen);
            }
        }
    }

}