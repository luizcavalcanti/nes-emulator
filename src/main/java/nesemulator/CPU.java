package nesemulator;

public class CPU {

    static int a;
    static int x;
    static int y;
    static int p;
    static int pc;
    static int s;
    static int ps;
    static int[] memory;
    static Cart cart;


    public static void initialize() {
        memory = new int[0x10001]; // Whole console memory
        a = x = y = 0x00; // Registers cleanup
        p = 0x34; // Processos status with IRQ disabled
        s = 0xFD; // Stack pointer staring into the abyss
        pc = 0x00;
    }

    public static void execute(Cart cartToExecute) {
        cart = cartToExecute;
        var running = true;
        while (running) {
            int opcode = ((int) cart.prgROM[pc]) & 0xff;
            switch (opcode) {
                case 0x20:
                    jsr();
                    break;
                case 0x48:
                    pha();
                    break;
                case 0x4C:
                    jmpAbsolute(cart.prgROM[pc + 1], cart.prgROM[pc + 2]);
                    break;
                case 0x8A:
                    txa();
                    break;
                case 0x8D:
                    staAbsolute();
                    break;
                case 0x98:
                    tya();
                    break;
                case 0xA2:
                    ldxImmediate(cart.prgROM[pc + 1]);
                    break;
                case 0xA9:
                    ldaImmediate();
                    break;
                default:
                    System.out.printf("%06d: OPCODE $%X not implemented%n", pc, opcode);
                    running = false;
            }
        }
    }

    private static void ldxImmediate(byte b) {
        int value = ((int) b) & 0xff;
        System.out.printf("%06d: LDX #$%02X (2 cycles)%n", pc, value);

        CPU.x = value;
        pc += 2;
    }

    private static void jmpAbsolute(byte addr1, byte addr2) {
        int address = littleEndianToInt(addr1, addr2);
        System.out.printf("%06d: JMP $%04X (3 cycles)%n", pc, address);

        pc = address;
    }

    private static void pha() {
        // TODO: push actual value to stack
//        ps = a;
//        ps--;

        System.out.printf("%06d: PHA (3 cycles)%n", pc);

        pc += 1;
    }

    private static void txa() {
        System.out.printf("%06d: TXA (2 cycles)%n", pc);

        a = x;
        pc += 1;
    }

    private static void tya() {
        System.out.printf("%06d: TYA (2 cycles)%n", pc);

        a = y;
        pc += 1;
    }

    private static void ldaImmediate() {
        int value = signedToUsignedByte(cart.chrROM[pc + 1]);
        System.out.printf("%06d: LDA #$%X (2 cycles)%n", pc, value);

        a = value;
        pc += 2;
    }

    private static void staAbsolute() {
        int value = littleEndianToInt(cart.chrROM[pc + 1], cart.chrROM[pc + 2]);
        System.out.printf("%06d: STA $%X (4 cycles)%n", pc, value);

        memory[value] = a;
        pc += 2;
    }

    private static void jsr() {
        int address = littleEndianToInt(cart.chrROM[pc + 1], cart.chrROM[pc + 2]);
        System.out.printf("%06d: JSR $%X (6 cycles)%n", pc, address);
        // TODO: Push address-1 of the next operation to stack

        pc = address;
    }

    private static int signedToUsignedByte(byte b) {
        return ((int) b) & 0xff;
    }

    private static int littleEndianToInt(byte b1, byte b2) {
        int i1 = ((int) b1) & 0xff;
        int i2 = ((int) b2) & 0xff;
        return (i2 << 8) | i1;
    }
}
