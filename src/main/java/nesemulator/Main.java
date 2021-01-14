package nesemulator;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            String romFileName = "balloon.nes";

            System.out.printf("Loading %s...%n", romFileName);
            Cart cart = Cart.fromROMFile(romFileName);
            MMU.loadCart(cart);
            System.out.println("Cart loaded");

            System.out.println("Initializing CPU...");
            CPU.initialize();
            System.out.println("Executing...");
            CPU.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}