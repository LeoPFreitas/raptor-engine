package co.raptor.engine.client.performance;

public class CommandUtils {
    public static byte[] encodeCreditCommand(double amount) {
        return ("CREDIT:" + amount).getBytes();
    }

    public static byte[] encodeDebitCommand(double amount) {
        return ("DEBIT:" + amount).getBytes();
    }

    public static byte[] encodeGetBalanceCommand() {
        return "GET_BALANCE".getBytes();
    }
}
