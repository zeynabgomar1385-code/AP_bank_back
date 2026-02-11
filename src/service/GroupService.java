package service;

import model.Expense;
import model.Group;
import storage.JsonStore;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class GroupService {
    private final JsonStore store;
    private final AuthService auth;
    private final Random rnd = new Random();

    public GroupService(JsonStore store, AuthService auth) {
        this.store = store;
        this.auth = auth;
    }

    public Group createGroup(String name, String owner, String currency, String description) {
        name = name == null ? "" : name.trim();
        if (name.isEmpty()) throw new RuntimeException("group name required");
        if (!auth.userExists(owner)) throw new RuntimeException("owner not found");

        String id = "g" + (System.currentTimeMillis() % 1000000) + rnd.nextInt(1000);
        Group g = new Group(id, name, owner, description == null ? "" : description, currency == null ? "IRR" : currency, System.currentTimeMillis());
        g.getMembers().add(owner);

        List<Group> groups = store.loadGroups();
        groups.add(g);
        store.saveGroups(groups);
        return g;
    }

    public void addMember(String groupId, String username) {
        if (!auth.userExists(username)) throw new RuntimeException("user not found");

        List<Group> groups = store.loadGroups();
        Group target = null;
        for (Group g : groups) {
            if (g.getId().equals(groupId)) { target = g; break; }
        }
        if (target == null) throw new RuntimeException("group not found");

        for (String m : target.getMembers()) {
            if (m.equalsIgnoreCase(username)) return;
        }
        target.getMembers().add(username);
        store.saveGroups(groups);
    }

    public Expense addExpense(String groupId, String title, String payer, long amount) {
        title = title == null ? "" : title.trim();
        if (title.isEmpty()) throw new RuntimeException("title required");
        if (amount <= 0) throw new RuntimeException("amount must be positive");

        List<Group> groups = store.loadGroups();
        Group target = null;
        for (Group g : groups) {
            if (g.getId().equals(groupId)) { target = g; break; }
        }
        if (target == null) throw new RuntimeException("group not found");

        boolean member = false;
        for (String m : target.getMembers()) {
            if (m.equalsIgnoreCase(payer)) { member = true; break; }
        }
        if (!member) throw new RuntimeException("payer is not member");

        String id = "e" + (System.nanoTime() % 1000000) + rnd.nextInt(1000);
        Expense e = new Expense(id, groupId, title, payer, amount, System.currentTimeMillis());

        List<Expense> expenses = store.loadExpenses();
        expenses.add(e);
        store.saveExpenses(expenses);

        target.getExpenseIds().add(id);
        store.saveGroups(groups);

        return e;
    }

    public Map<String, Long> calculateNet(String groupId) {
        Group g = null;
        for (Group x : store.loadGroups()) {
            if (x.getId().equals(groupId)) { g = x; break; }
        }
        if (g == null) throw new RuntimeException("group not found");

        List<String> members = g.getMembers();
        int n = members.size();
        if (n == 0) throw new RuntimeException("no members");

        Map<String, Long> paid = new LinkedHashMap<>();
        for (String m : members) paid.put(m, 0L);

        Map<String, Expense> expenseMap = new HashMap<>();
        for (Expense e : store.loadExpenses()) expenseMap.put(e.getId(), e);

        long total = 0;
        for (String eid : g.getExpenseIds()) {
            Expense e = expenseMap.get(eid);
            if (e == null) continue;
            total += e.getAmount();
            paid.put(e.getPayer(), paid.get(e.getPayer()) + e.getAmount());
        }

        long share = total / n;
        Map<String, Long> net = new LinkedHashMap<>();
        for (String m : members) {
            long netVal = paid.get(m) - share;
            net.put(m, netVal);
        }
        return net;
    }

    public List<Group> getGroupsForUser(String username) {
        username = username == null ? "" : username.trim();
        if (username.isEmpty()) return new ArrayList<>();

        List<Group> out = new ArrayList<>();
        for (Group g : store.loadGroups()) {
            boolean ok = false;

            if (g.getOwnerUsername() != null && g.getOwnerUsername().equalsIgnoreCase(username)) ok = true;

            if (!ok && g.getMembers() != null) {
                for (String m : g.getMembers()) {
                    if (m != null && m.equalsIgnoreCase(username)) { ok = true; break; }
                }
            }

            if (ok) out.add(g);
        }

        out.sort((a, b) -> Long.compare(a.getCreatedAt(), b.getCreatedAt()));
        return out;
    }

    public List<Expense> getExpensesForGroup(String groupId) {
        Group g = getGroupById(groupId);
        if (g == null) throw new RuntimeException("group not found");

        Map<String, Expense> expenseMap = new HashMap<>();
        for (Expense e : store.loadExpenses()) expenseMap.put(e.getId(), e);

        List<Expense> out = new ArrayList<>();
        if (g.getExpenseIds() != null) {
            for (String eid : g.getExpenseIds()) {
                Expense e = expenseMap.get(eid);
                if (e != null) out.add(e);
            }
        }

        out.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
        return out;
    }

    public String exportReport(String groupId, String outDir) {
        Group g = getGroupById(groupId);
        if (g == null) throw new RuntimeException("group not found");

        if (outDir == null || outDir.trim().isEmpty()) outDir = "reports";
        File dir = new File(outDir);
        if (!dir.exists()) dir.mkdirs();

        Map<String, Expense> expenseMap = new HashMap<>();
        for (Expense e : store.loadExpenses()) expenseMap.put(e.getId(), e);

        List<Expense> expenses = new ArrayList<>();
        for (String eid : g.getExpenseIds()) {
            Expense e = expenseMap.get(eid);
            if (e != null) expenses.add(e);
        }
        expenses.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));

        Map<String, Long> net = calculateNet(groupId);
        List<String> settlements = buildSettlements(net);

        String fileName = "group_" + g.getId() + "_report.txt";
        File file = new File(dir, fileName);

        try (FileWriter fw = new FileWriter(file, false)) {
            fw.write("Group: " + g.getName() + " (" + g.getCurrency() + ")\n");
            fw.write("Owner: " + g.getOwnerUsername() + "\n");
            fw.write("Description: " + safe(g.getDescription()) + "\n");
            fw.write("CreatedAt: " + g.getCreatedAt() + "\n");
            fw.write("\n");

            fw.write("Members:\n");
            for (String m : g.getMembers()) fw.write("- " + m + "\n");
            fw.write("\n");

            fw.write("Expenses:\n");
            if (expenses.isEmpty()) {
                fw.write("(none)\n");
            } else {
                for (Expense e : expenses) {
                    fw.write("- " + e.getTitle() + " | payer=" + e.getPayer() + " | amount=" + e.getAmount() + " | time=" + e.getTimestamp() + "\n");
                }
            }
            fw.write("\n");

            fw.write("Net Result:\n");
            for (Map.Entry<String, Long> it : net.entrySet()) {
                fw.write(it.getKey() + ": " + it.getValue() + "\n");
            }
            fw.write("\n");

            fw.write("Suggested Settlement:\n");
            if (settlements.isEmpty()) {
                fw.write("(nothing)\n");
            } else {
                for (String s : settlements) fw.write(s + "\n");
            }
        } catch (Exception e) {
            throw new RuntimeException("cannot write report");
        }

        return file.getPath();
    }

    private List<String> buildSettlements(Map<String, Long> net) {
        class Node {
            String user;
            long amt;
            Node(String u, long a) { user = u; amt = a; }
        }

        List<Node> creditors = new ArrayList<>();
        List<Node> debtors = new ArrayList<>();

        for (Map.Entry<String, Long> it : net.entrySet()) {
            long v = it.getValue();
            if (v > 0) creditors.add(new Node(it.getKey(), v));
            else if (v < 0) debtors.add(new Node(it.getKey(), -v));
        }

        List<String> out = new ArrayList<>();
        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            Node d = debtors.get(i);
            Node c = creditors.get(j);

            long pay = Math.min(d.amt, c.amt);
            if (pay > 0) out.add(d.user + " -> " + c.user + " : " + pay);

            d.amt -= pay;
            c.amt -= pay;

            if (d.amt == 0) i++;
            if (c.amt == 0) j++;
        }
        return out;
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.trim();
    }

    public Group getGroupById(String groupId) {
        for (Group g : store.loadGroups()) {
            if (g.getId().equals(groupId)) return g;
        }
        return null;
    }
}
