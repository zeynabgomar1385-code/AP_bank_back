package service;

import model.Account;
import model.Transaction;
import storage.JsonStore;

import java.util.*;

public class BankService {
    private final JsonStore store;
    private final Random rnd = new Random();

    public BankService(JsonStore store) {
        this.store = store;
    }

    public Account createAccount(String username, String type, long initialBalance) {
        type = type == null ? "" : type.trim().toLowerCase();
        if (!type.equals("current") && !type.equals("savings")) {
            throw new RuntimeException("type must be current or savings");
        }
        if (initialBalance < 0) throw new RuntimeException("initial balance cannot be negative");

        List<Account> accounts = store.loadAccounts();
        String id = "a" + (System.currentTimeMillis() % 1000000) + rnd.nextInt(1000);

        String card = generateUniqueCard(accounts);
        String accNo = generateUniqueAccountNumber(accounts);

        Account a = new Account(id, username, type, accNo, card, initialBalance);
        accounts.add(a);
        store.saveAccounts(accounts);

        if (initialBalance > 0) {
            List<Transaction> txs = store.loadTransactions();
            txs.add(new Transaction(
                "t" + System.nanoTime(),
                a.getId(),
                "deposit",
                initialBalance,
                System.currentTimeMillis(),
                "initial",
                "initial balance"
            ));
            store.saveTransactions(txs);
        }

        return a;
    }

    private String generateUniqueCard(List<Account> accounts) {
        Set<String> used = new HashSet<>();
        for (Account a : accounts) used.add(a.getCardNumber());

        while (true) {
            String prefix = "603799";
            StringBuilder sb = new StringBuilder(prefix);
            while (sb.length() < 16) sb.append(rnd.nextInt(10));
            String card = sb.toString();
            if (!used.contains(card)) return card;
        }
    }

    private String generateUniqueAccountNumber(List<Account> accounts) {
        Set<String> used = new HashSet<>();
        for (Account a : accounts) used.add(a.getAccountNumber());

        int base = 100;
        while (true) {
            String accNo = "404-" + (base + rnd.nextInt(900)) + "-" + rnd.nextInt(10);
            if (!used.contains(accNo)) return accNo;
            base++;
        }
    }

    public List<Account> getAccounts(String username) {
        List<Account> all = store.loadAccounts();
        List<Account> out = new ArrayList<>();
        for (Account a : all) {
            if (a.getUsername().equalsIgnoreCase(username)) out.add(a);
        }
        return out;
    }

    public Account findAccountById(String accountId) {
        for (Account a : store.loadAccounts()) {
            if (a.getId().equals(accountId)) return a;
        }
        return null;
    }

    public void deposit(String accountId, long amount, String category, String desc) {
        if (amount <= 0) throw new RuntimeException("amount must be positive");

        List<Account> accounts = store.loadAccounts();
        Account target = null;
        for (Account a : accounts) {
            if (a.getId().equals(accountId)) { target = a; break; }
        }
        if (target == null) throw new RuntimeException("account not found");

        target.setBalance(target.getBalance() + amount);
        store.saveAccounts(accounts);

        addTx(accountId, "deposit", amount, category, desc);
    }

    public void withdraw(String accountId, long amount, String category, String desc) {
        if (amount <= 0) throw new RuntimeException("amount must be positive");

        List<Account> accounts = store.loadAccounts();
        Account target = null;
        for (Account a : accounts) {
            if (a.getId().equals(accountId)) { target = a; break; }
        }
        if (target == null) throw new RuntimeException("account not found");

        if (target.getBalance() < amount) throw new RuntimeException("insufficient balance");

        target.setBalance(target.getBalance() - amount);
        store.saveAccounts(accounts);

        addTx(accountId, "withdraw", amount, category, desc);
    }

    private void addTx(String accountId, String type, long amount, String category, String desc) {
        List<Transaction> txs = store.loadTransactions();
        txs.add(new Transaction(
            "t" + System.nanoTime(),
            accountId,
            type,
            amount,
            System.currentTimeMillis(),
            category == null ? "" : category,
            desc == null ? "" : desc
        ));
        store.saveTransactions(txs);
    }

    public void transferCardToCard(String fromAccountId, String toCardNumber, long amount) {
        if (amount <= 0) throw new RuntimeException("amount must be positive");
        if (toCardNumber == null || toCardNumber.trim().length() != 16) throw new RuntimeException("invzeyd card");

        List<Account> accounts = store.loadAccounts();

        Account from = null;
        Account to = null;
        for (Account a : accounts) {
            if (a.getId().equals(fromAccountId)) from = a;
            if (a.getCardNumber().equals(toCardNumber.trim())) to = a;
        }
        if (from == null) throw new RuntimeException("from account not found");
        if (to == null) throw new RuntimeException("destination card not found");

        boolean sameBank = from.getCardNumber().substring(0, 6).equals(to.getCardNumber().substring(0, 6));
        long limit = 10_000_000L;

        if (!sameBank && amount > limit) {
            throw new RuntimeException("card-to-card limit is 10,000,000");
        }

        if (from.getBalance() < amount) throw new RuntimeException("insufficient balance");

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);

        store.saveAccounts(accounts);

        addTx(from.getId(), "transfer_out", amount, "transfer", "to card " + to.getCardNumber());
        addTx(to.getId(), "transfer_in", amount, "transfer", "from card " + from.getCardNumber());
    }

    public void transferSheba(String fromAccountId, String toSheba, long amount) {
        if (amount <= 0) throw new RuntimeException("amount must be positive");
        if (toSheba == null) toSheba = "";
        toSheba = toSheba.trim().toUpperCase();

        if (!toSheba.startsWith("IR") || toSheba.length() < 10) {
            throw new RuntimeException("invzeyd sheba");
        }

        List<Account> accounts = store.loadAccounts();
        Account from = null;
        for (Account a : accounts) {
            if (a.getId().equals(fromAccountId)) { from = a; break; }
        }
        if (from == null) throw new RuntimeException("from account not found");
        if (from.getBalance() < amount) throw new RuntimeException("insufficient balance");

        from.setBalance(from.getBalance() - amount);
        store.saveAccounts(accounts);

        addTx(from.getId(), "sheba_out", amount, "transfer", "to sheba " + toSheba);
    }

    public List<Transaction> getTransactionsForAccount(String accountId) {
        List<Transaction> all = store.loadTransactions();
        List<Transaction> out = new ArrayList<>();
        for (Transaction t : all) {
            if (t.getAccountId().equals(accountId)) out.add(t);
        }
        out.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return out;
    }
}
