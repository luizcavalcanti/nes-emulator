package nesemulator;

import nesemulator.cpu.CPU;
import nesemulator.cpu.observer.LogCPUObserver;
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
    private static final int kFramesPerSecond = 60;
    private static final long kTicksPerSecond = 4194304;
    private static final int kMillisPerFrame = 1000 / kFramesPerSecond;
    private static final long ticksPerFrame = kTicksPerSecond / kFramesPerSecond;

    private static String romFileName;

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
        JFrame frame = new JFrame("NES Emulator");
        frame.getContentPane().setLayout(new FlowLayout());

        var screen = new ImageIcon();
        var label = new JLabel(screen);
        frame.getContentPane().add(label);
        frame.setPreferredSize(new Dimension(266, 280));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        long clock = 0;
        while (true) {
//            System.out.print(String.format("Clock: %d\r", clock));

            var cpuCycles = CPU.executeStep();
            PPU.executeStep(cpuCycles);
            clock += cpuCycles;

            if (clock >= ticksPerFrame) {
                clock -= ticksPerFrame;
                // read input
                // render screen
                label.setIcon(new ImageIcon(PPU.render()));
                label.repaint();
            }
        }
    }

}