package storage;

import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class JsonStore {
    private final String dataDir;

    public JsonStore(String dataDir) {
        this.dataDir = dataDir;
        ensureDataFiles();
    }

    private void ensureDataFiles() {
        try {
            Files.createDirectories(Paths.get(dataDir));
            ensureFile("users.json");
            ensureFile("accounts.json");
            ensureFile("transactions.json");
            ensureFile("groups.json");
            ensureFile("expenses.json");
        } catch (Exception ignored) {}
    }

    private void ensureFile(String name) throws IOException {
        Path p = Paths.get(dataDir, name);
        if (!Files.exists(p)) {
            Files.write(p, "[]".getBytes(StandardCharsets.UTF_8));
        }
    }

    private String readAll(String file) {
        try {
            Path p = Paths.get(dataDir, file);
            if (!Files.exists(p)) return "[]";
            return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "[]";
        }
    }

    private void writeAll(String file, String content) {
        try {
            Path p = Paths.get(dataDir, file);
            Files.write(p, content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {}
    }

    // -------- USERS ----------
    public List<User> loadUsers() {
        String raw = readAll("users.json");
        List<User> users = new ArrayList<>();
        for (String obj : JsonUtil.splitTopLevelObjects(raw)) {
            Map<String, String> m = JsonUtil.parseObjectToRawMap(obj);
            String username = JsonUtil.rawToString(m.get("username"));
            String password = JsonUtil.rawToString(m.get("password"));
            String name = JsonUtil.rawToString(m.get("name"));
            users.add(new User(username, password, name));
        }
        return users;
    }

    public void saveUsers(List<User> users) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) sb.append(",");
            User u = users.get(i);
            sb.append("{")
              .append("\"username\":").append(JsonUtil.quote(u.getUsername())).append(",")
              .append("\"password\":").append(JsonUtil.quote(u.getPassword())).append(",")
              .append("\"name\":").append(JsonUtil.quote(u.getName()))
              .append("}");
        }
        sb.append("]");
        writeAll("users.json", sb.toString());
    }

    // -------- ACCOUNTS ----------
    public List<Account> loadAccounts() {
        String raw = readAll("accounts.json");
        List<Account> accounts = new ArrayList<>();
        for (String obj : JsonUtil.splitTopLevelObjects(raw)) {
            Map<String, String> m = JsonUtil.parseObjectToRawMap(obj);
            Account a = new Account(
                JsonUtil.rawToString(m.get("id")),
                JsonUtil.rawToString(m.get("username")),
                JsonUtil.rawToString(m.get("type")),
                JsonUtil.rawToString(m.get("accountNumber")),
                JsonUtil.rawToString(m.get("cardNumber")),
                JsonUtil.rawToLong(m.get("balance"), 0)
            );
            accounts.add(a);
        }
        return accounts;
    }

    public void saveAccounts(List<Account> accounts) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < accounts.size(); i++) {
            if (i > 0) sb.append(",");
            Account a = accounts.get(i);
            sb.append("{")
              .append("\"id\":").append(JsonUtil.quote(a.getId())).append(",")
              .append("\"username\":").append(JsonUtil.quote(a.getUsername())).append(",")
              .append("\"type\":").append(JsonUtil.quote(a.getType())).append(",")
              .append("\"accountNumber\":").append(JsonUtil.quote(a.getAccountNumber())).append(",")
              .append("\"cardNumber\":").append(JsonUtil.quote(a.getCardNumber())).append(",")
              .append("\"balance\":").append(a.getBalance())
              .append("}");
        }
        sb.append("]");
        writeAll("accounts.json", sb.toString());
    }

    // -------- TRANSACTIONS ----------
    public List<Transaction> loadTransactions() {
        String raw = readAll("transactions.json");
        List<Transaction> txs = new ArrayList<>();
        for (String obj : JsonUtil.splitTopLevelObjects(raw)) {
            Map<String, String> m = JsonUtil.parseObjectToRawMap(obj);
            Transaction t = new Transaction(
                JsonUtil.rawToString(m.get("id")),
                JsonUtil.rawToString(m.get("accountId")),
                JsonUtil.rawToString(m.get("type")),
                JsonUtil.rawToLong(m.get("amount"), 0),
                JsonUtil.rawToLong(m.get("timestamp"), System.currentTimeMillis()),
                JsonUtil.rawToString(m.get("category")),
                JsonUtil.rawToString(m.get("description"))
            );
            txs.add(t);
        }
        return txs;
    }

    public void saveTransactions(List<Transaction> txs) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < txs.size(); i++) {
            if (i > 0) sb.append(",");
            Transaction t = txs.get(i);
            sb.append("{")
              .append("\"id\":").append(JsonUtil.quote(t.getId())).append(",")
              .append("\"accountId\":").append(JsonUtil.quote(t.getAccountId())).append(",")
              .append("\"type\":").append(JsonUtil.quote(t.getType())).append(",")
              .append("\"amount\":").append(t.getAmount()).append(",")
              .append("\"timestamp\":").append(t.getTimestamp()).append(",")
              .append("\"category\":").append(JsonUtil.quote(t.getCategory())).append(",")
              .append("\"description\":").append(JsonUtil.quote(t.getDescription()))
              .append("}");
        }
        sb.append("]");
        writeAll("transactions.json", sb.toString());
    }

    // -------- GROUPS ----------
    public List<Group> loadGroups() {
        String raw = readAll("groups.json");
        List<Group> groups = new ArrayList<>();
        for (String obj : JsonUtil.splitTopLevelObjects(raw)) {
            Map<String, String> m = JsonUtil.parseObjectToRawMap(obj);
            Group g = new Group(
                JsonUtil.rawToString(m.get("id")),
                JsonUtil.rawToString(m.get("name")),
                JsonUtil.rawToString(m.get("ownerUsername")),
                JsonUtil.rawToString(m.get("description")),
                JsonUtil.rawToString(m.get("currency")),
                JsonUtil.rawToLong(m.get("createdAt"), System.currentTimeMillis())
            );
            g.setMembers(JsonUtil.rawToStringList(m.get("members")));
            g.setExpenseIds(JsonUtil.rawToStringList(m.get("expenseIds")));
            groups.add(g);
        }
        return groups;
    }

    public void saveGroups(List<Group> groups) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < groups.size(); i++) {
            if (i > 0) sb.append(",");
            Group g = groups.get(i);
            sb.append("{")
              .append("\"id\":").append(JsonUtil.quote(g.getId())).append(",")
              .append("\"name\":").append(JsonUtil.quote(g.getName())).append(",")
              .append("\"ownerUsername\":").append(JsonUtil.quote(g.getOwnerUsername())).append(",")
              .append("\"description\":").append(JsonUtil.quote(g.getDescription())).append(",")
              .append("\"currency\":").append(JsonUtil.quote(g.getCurrency())).append(",")
              .append("\"createdAt\":").append(g.getCreatedAt()).append(",")
              .append("\"members\":").append(JsonUtil.stringListToRaw(g.getMembers())).append(",")
              .append("\"expenseIds\":").append(JsonUtil.stringListToRaw(g.getExpenseIds()))
              .append("}");
        }
        sb.append("]");
        writeAll("groups.json", sb.toString());
    }

    // -------- EXPENSES ----------
    public List<Expense> loadExpenses() {
        String raw = readAll("expenses.json");
        List<Expense> expenses = new ArrayList<>();
        for (String obj : JsonUtil.splitTopLevelObjects(raw)) {
            Map<String, String> m = JsonUtil.parseObjectToRawMap(obj);
            Expense e = new Expense(
                JsonUtil.rawToString(m.get("id")),
                JsonUtil.rawToString(m.get("groupId")),
                JsonUtil.rawToString(m.get("title")),
                JsonUtil.rawToString(m.get("payer")),
                JsonUtil.rawToLong(m.get("amount"), 0),
                JsonUtil.rawToLong(m.get("timestamp"), System.currentTimeMillis())
            );
            expenses.add(e);
        }
        return expenses;
    }

    public void saveExpenses(List<Expense> expenses) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < expenses.size(); i++) {
            if (i > 0) sb.append(",");
            Expense e = expenses.get(i);
            sb.append("{")
              .append("\"id\":").append(JsonUtil.quote(e.getId())).append(",")
              .append("\"groupId\":").append(JsonUtil.quote(e.getGroupId())).append(",")
              .append("\"title\":").append(JsonUtil.quote(e.getTitle())).append(",")
              .append("\"payer\":").append(JsonUtil.quote(e.getPayer())).append(",")
              .append("\"amount\":").append(e.getAmount()).append(",")
              .append("\"timestamp\":").append(e.getTimestamp())
              .append("}");
        }
        sb.append("]");
        writeAll("expenses.json", sb.toString());
    }
}
