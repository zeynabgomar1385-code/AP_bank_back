package model;

public class Transaction {
    private final String id;
    private final String accountId;
    private final String type;
    private final long amount;
    private final long timestamp;
    private final String category;
    private final String description;
    public Transaction(String id, String accountId, String type, long amount, long timestamp, String category, String description) {
        this.id = id;
        this.accountId = accountId;
        this.type = type == null ? "" : type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.category = category == null ? "" : category;
        this.description = description == null ? "" : description;
    }
    public String getId() {
        return id;
    }
    public String getAccountId() {
        return accountId;
    }
    public String getType() {
        return type;
    }
    public long getAmount() {
        return amount;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public String getCategory() {
        return category;
    }
    public String getDescription() {
        return description;
    }
}
