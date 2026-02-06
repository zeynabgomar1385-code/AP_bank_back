package model;

public class Account {
    private String id;
    private String username;
    private String type; 
    private String accountNumber;
    private String cardNumber; 
    private long balance;

    public Account() {}

    public Account(String id, String username, String type, String accountNumber, String cardNumber, long balance) {
        this.id = id;
        this.username = username;
        this.type = type;
        this.accountNumber = accountNumber;
        this.cardNumber = cardNumber;
        this.balance = balance;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getType() { return type; }
    public String getAccountNumber() { return accountNumber; }
    public String getCardNumber() { return cardNumber; }
    public long getBalance() { return balance; }

    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setType(String type) { this.type = type; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public void setBalance(long balance) { this.balance = balance; }
}
