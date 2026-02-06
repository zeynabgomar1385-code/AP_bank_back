package model;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String id;
    private String name;
    private String ownerUsername;
    private String description;
    private String currency;
    private long createdAt;

    private List<String> members = new ArrayList<>();
    private List<String> expenseIds = new ArrayList<>();

    public Group() {}

    public Group(String id, String name, String ownerUsername, String description, String currency, long createdAt) {
        this.id = id;
        this.name = name;
        this.ownerUsername = ownerUsername;
        this.description = description;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getOwnerUsername() { return ownerUsername; }
    public String getDescription() { return description; }
    public String getCurrency() { return currency; }
    public long getCreatedAt() { return createdAt; }
    public List<String> getMembers() { return members; }
    public List<String> getExpenseIds() { return expenseIds; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public void setDescription(String description) { this.description = description; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setMembers(List<String> members) { this.members = members; }
    public void setExpenseIds(List<String> expenseIds) { this.expenseIds = expenseIds; }
}
