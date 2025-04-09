package co.raptor.engine.raft;

public enum RaptorEngineCommand {
    CREDIT, // Adds to the account balance
    DEBIT,  // Deducts from the account balance
    GET_BALANCE; // Retrieves the current balance
}

