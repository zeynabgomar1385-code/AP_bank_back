package model;

public class Expense {
    private String id;
    private String groupId;
    private String title;
    private String payer;
    private long amount;
    private long timestamp;

    public Expense() {}

    public Expense(String id, String groupId, String title, String payer, long amount, long timestamp) {
        this.id = id;
        this.groupId = groupId;
        this.title = title;
        this.payer = payer;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getGroupId() { return groupId; }
    public String getTitle() { return title; }
    public String getPayer() { return payer; }
    public long getAmount() { return amount; }
    public long getTimestamp() { return timestamp; }

    public void setId(String id) { this.id = id; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public void setTitle(String title) { this.title = title; }
    public void setPayer(String payer) { this.payer = payer; }
    public void setAmount(long amount) { this.amount = amount; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
