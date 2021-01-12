package nesemulator;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            Cart cart = Cart.fromROMFile("tetris.nes");
            System.out.println("Cart loaded");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
