package nesemulator.cpu;

public class CPUFormatter {
    public static String getFormattedOperands(AddressingMode addressingMode, int[] operands) {
        switch (addressingMode) {
            case ACCUMULATOR:
            case IMPLIED:
                return "";
            case RELATIVE:
                return String.format(" $%02X (%d)", operands[0] & 0xFF, (byte) operands[0]);
            case ZERO_PAGE:
                return String.format(" $%02X", operands[0] & 0xFF);
            case ZERO_PAGE_X:
                return String.format(" $%02X, X", operands[0] & 0xFF);
            case ZERO_PAGE_Y:
                return String.format(" $%02X, Y", operands[0] & 0xFF);
            case IMMEDIATE:
                return String.format(" #$%02X", operands[0] & 0xFF);
            case ABSOLUTE:
                return String.format(" $%02X $%02X", operands[0] & 0xFF, operands[1] & 0xFF);
            case ABSOLUTE_X:
                return String.format(" $%02X $%02X, X", operands[0] & 0xFF, operands[1] & 0xFF);
            case ABSOLUTE_Y:
                return String.format(" $%02X $%02X, Y", operands[0] & 0xFF, operands[1] & 0xFF);
            case INDIRECT_X:
                return String.format(" ($%02X $%02X), X", operands[0] & 0xFF, operands[1] & 0xFF);
            case INDIRECT_Y:
                return String.format(" ($%02X $%02X), Y", operands[0] & 0xFF, operands[1] & 0xFF);
            default:
                throw new UnsupportedOperationException("Please write a log handler for this addressing mode: " + addressingMode.name());
        }
    }
}
