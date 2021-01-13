package nesemulator;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            Cart cart = Cart.fromROMFile("baloon.nes");
            System.out.println("Cart loaded");
            CPU.initialize();
            System.out.println("CPU Initialized");
            CPU.execute(cart);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}