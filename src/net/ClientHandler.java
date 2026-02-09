package net;

import model.Account;
import model.Expense;
import model.Group;
import model.Transaction;
import model.User;
import service.AuthService;
import service.BankService;
import service.GroupService;
import service.ServicesService;
import storage.JsonUtil;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuthService auth;
    private final BankService bank;
    private final GroupService group;
    private final ServicesService services;

    private String loggedInUser = null;
    private String loggedInName = null;

    public ClientHandler(Socket socket, AuthService auth, BankService bank, GroupService group, ServicesService services) {
        this.socket = socket;
        this.auth = auth;
        this.bank = bank;
        this.group = group;
        this.services = services;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
        ) {
            out.println("{\"status\":\"ok\",\"message\":\"connected\"}");

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    String resp = handle(line);
                    out.println(resp);
                } catch (Exception e) {
                    out.println("{\"status\":\"error\",\"message\":" + JsonUtil.quote(e.getMessage()) + "}");
                }
            }
        } catch (Exception ignored) {
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    private String handle(String jsonLine) {
        Map<String, String> m = JsonUtil.parseObjectToRawMap(jsonLine);
        String action = JsonUtil.rawToString(m.get("action")).toUpperCase();

        if (action.equals("PING")) {
            return "{\"status\":\"ok\",\"message\":\"pong\"}";
        }

        if (action.equals("REGISTER")) {
            String username = JsonUtil.rawToString(m.get("username"));
            String password = JsonUtil.rawToString(m.get("password"));
            String name = JsonUtil.rawToString(m.get("name"));
            auth.register(username, password, name);
            return "{\"status\":\"ok\",\"message\":\"registered\"}";
        }

        if (action.equals("LOGIN")) {
            String username = JsonUtil.rawToString(m.get("username"));
            String password = JsonUtil.rawToString(m.get("password"));
            User u = auth.login(username, password);
            loggedInUser = u.getUsername();
            loggedInName = u.getName();
            return "{\"status\":\"ok\",\"username\":" + JsonUtil.quote(u.getUsername()) + ",\"name\":" + JsonUtil.quote(u.getName()) + "}";
        }

        if (loggedInUser == null) {
            throw new RuntimeException("please login first");
        }

        if (action.equals("GET_PROFILE")) {
            return "{"
                + "\"status\":\"ok\","
                + "\"username\":" + JsonUtil.quote(loggedInUser) + ","
                + "\"name\":" + JsonUtil.quote(loggedInName == null ? "" : loggedInName)
                + "}";
        }

        if (action.equals("UPDATE_PROFILE")) {
            String newName = JsonUtil.rawToString(m.get("name"));
            if (newName == null || newName.trim().isEmpty()) throw new RuntimeException("name is required");
            auth.updateProfileName(loggedInUser, newName.trim());
            loggedInName = newName.trim();
            return "{"
                + "\"status\":\"ok\","
                + "\"message\":\"profile updated\","
                + "\"name\":" + JsonUtil.quote(loggedInName)
                + "}";
        }

        if (action.equals("GET_ACCOUNTS")) {
            List<Account> accounts = bank.getAccounts(loggedInUser);

            String typeFilter = JsonUtil.rawToString(m.get("type"));
            if (typeFilter != null && !typeFilter.trim().isEmpty()) {
                String tf = typeFilter.trim().toLowerCase();
                accounts.removeIf(a -> !a.getType().equalsIgnoreCase(tf));
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{\"status\":\"ok\",\"accounts\":[");
            for (int i = 0; i < accounts.size(); i++) {
                if (i > 0) sb.append(",");
                Account a = accounts.get(i);
                sb.append("{")
                    .append("\"id\":").append(JsonUtil.quote(a.getId())).append(",")
                    .append("\"cardNumber\":").append(JsonUtil.quote(a.getCardNumber())).append(",")
                    .append("\"accountNumber\":").append(JsonUtil.quote(a.getAccountNumber())).append(",")
                    .append("\"balance\":").append(a.getBalance()).append(",")
                    .append("\"type\":").append(JsonUtil.quote(a.getType()))
                    .append("}");
            }
            sb.append("]}");
            return sb.toString();
        }

        if (action.equals("CREATE_ACCOUNT")) {
            String type = JsonUtil.rawToString(m.get("type"));
            long initial = JsonUtil.rawToLong(m.get("initialBalance"), 0);
            Account a = bank.createAccount(loggedInUser, type, initial);
            return "{\"status\":\"ok\",\"id\":" + JsonUtil.quote(a.getId()) + "}";
        }

        if (action.equals("GET_TRANSACTIONS")) {
            String accountId = JsonUtil.rawToString(m.get("accountId"));
            List<Transaction> txs = bank.getTransactionsForAccount(accountId);

            StringBuilder sb = new StringBuilder();
            sb.append("{\"status\":\"ok\",\"transactions\":[");
            for (int i = 0; i < txs.size(); i++) {
                if (i > 0) sb.append(",");
                Transaction t = txs.get(i);
                sb.append("{")
                    .append("\"id\":").append(JsonUtil.quote(t.getId())).append(",")
                    .append("\"type\":").append(JsonUtil.quote(t.getType())).append(",")
                    .append("\"amount\":").append(t.getAmount()).append(",")
                    .append("\"timestamp\":").append(t.getTimestamp()).append(",")
                    .append("\"category\":").append(JsonUtil.quote(t.getCategory())).append(",")
                    .append("\"description\":").append(JsonUtil.quote(t.getDescription()))
                    .append("}");
            }
            sb.append("]}");
            return sb.toString();
        }

        if (action.equals("BUY_TOPUP")) {
            String accountId = JsonUtil.rawToString(m.get("accountId"));
            String operator = JsonUtil.rawToString(m.get("operator"));
            String phone = JsonUtil.rawToString(m.get("phone"));
            long amount = JsonUtil.rawToLong(m.get("amount"), 0);

            Transaction t = services.buyTopUp(accountId, operator, phone, amount);

            return "{"
                + "\"status\":\"ok\","
                + "\"message\":\"topup done\","
                + "\"transaction\":{"
                    + "\"id\":" + JsonUtil.quote(t.getId()) + ","
                    + "\"type\":" + JsonUtil.quote(t.getType()) + ","
                    + "\"amount\":" + t.getAmount() + ","
                    + "\"timestamp\":" + t.getTimestamp() + ","
                    + "\"category\":" + JsonUtil.quote(t.getCategory()) + ","
                    + "\"description\":" + JsonUtil.quote(t.getDescription())
                + "}"
                + "}";
        }

        if (action.equals("PAY_BILL")) {
            String accountId = JsonUtil.rawToString(m.get("accountId"));
            String billType = JsonUtil.rawToString(m.get("billType"));
            String billId = JsonUtil.rawToString(m.get("billId"));
            String payId = JsonUtil.rawToString(m.get("payId"));
            long amount = JsonUtil.rawToLong(m.get("amount"), 0);

            Transaction t = services.payBill(accountId, billType, billId, payId, amount);

            return "{"
                + "\"status\":\"ok\","
                + "\"message\":\"bill paid\","
                + "\"transaction\":{"
                    + "\"id\":" + JsonUtil.quote(t.getId()) + ","
                    + "\"type\":" + JsonUtil.quote(t.getType()) + ","
                    + "\"amount\":" + t.getAmount() + ","
                    + "\"timestamp\":" + t.getTimestamp() + ","
                    + "\"category\":" + JsonUtil.quote(t.getCategory()) + ","
                    + "\"description\":" + JsonUtil.quote(t.getDescription())
                + "}"
                + "}";
        }

        if (action.equals("CREATE_GROUP")) {
            String name = JsonUtil.rawToString(m.get("name"));
            String currency = JsonUtil.rawToString(m.get("currency"));
            String desc = JsonUtil.rawToString(m.get("description"));
            Group g = group.createGroup(name, loggedInUser, currency, desc);
            return "{\"status\":\"ok\",\"groupId\":" + JsonUtil.quote(g.getId()) + "}";
        }

        if (action.equals("GET_GROUPS")) {
            List<Group> groups = group.getGroupsForUser(loggedInUser);
            StringBuilder sb = new StringBuilder();
            sb.append("{\"status\":\"ok\",\"groups\":[");
            for (int i = 0; i < groups.size(); i++) {
                if (i > 0) sb.append(",");
                Group g = groups.get(i);
                sb.append("{")
                    .append("\"id\":").append(JsonUtil.quote(g.getId())).append(",")
                    .append("\"name\":").append(JsonUtil.quote(g.getName())).append(",")
                    .append("\"ownerUsername\":").append(JsonUtil.quote(g.getOwnerUsername())).append(",")
                    .append("\"currency\":").append(JsonUtil.quote(g.getCurrency())).append(",")
                    .append("\"description\":").append(JsonUtil.quote(g.getDescription())).append(",")
                    .append("\"createdAt\":").append(g.getCreatedAt()).append(",")
                    .append("\"membersCount\":").append(g.getMembers() == null ? 0 : g.getMembers().size())
                    .append("}");
            }
            sb.append("]}");
            return sb.toString();
        }

        if (action.equals("GET_GROUP_DETAIL")) {
            String groupId = JsonUtil.rawToString(m.get("groupId"));
            Group g = group.getGroupById(groupId);
            if (g == null) throw new RuntimeException("group not found");

            boolean isMember = false;
            for (String u : g.getMembers()) {
                if (u.equalsIgnoreCase(loggedInUser)) { isMember = true; break; }
            }
            if (!isMember) throw new RuntimeException("access denied");

            List<Expense> expenses = group.getExpensesForGroup(groupId);

            StringBuilder sb = new StringBuilder();
            sb.append("{\"status\":\"ok\",\"group\":{");
            sb.append("\"id\":").append(JsonUtil.quote(g.getId())).append(",");
            sb.append("\"name\":").append(JsonUtil.quote(g.getName())).append(",");
            sb.append("\"ownerUsername\":").append(JsonUtil.quote(g.getOwnerUsername())).append(",");
            sb.append("\"currency\":").append(JsonUtil.quote(g.getCurrency())).append(",");
            sb.append("\"description\":").append(JsonUtil.quote(g.getDescription())).append(",");
            sb.append("\"createdAt\":").append(g.getCreatedAt()).append(",");
            sb.append("\"members\":[");
            for (int i = 0; i < g.getMembers().size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(JsonUtil.quote(g.getMembers().get(i)));
            }
            sb.append("],\"expenses\":[");
            for (int i = 0; i < expenses.size(); i++) {
                if (i > 0) sb.append(",");
                Expense e = expenses.get(i);
                sb.append("{")
                    .append("\"id\":").append(JsonUtil.quote(e.getId())).append(",")
                    .append("\"title\":").append(JsonUtil.quote(e.getTitle())).append(",")
                    .append("\"payer\":").append(JsonUtil.quote(e.getPayer())).append(",")
                    .append("\"amount\":").append(e.getAmount()).append(",")
                    .append("\"timestamp\":").append(e.getTimestamp())
                    .append("}");
            }
            sb.append("]}}");
            return sb.toString();
        }

        if (action.equals("ADD_MEMBER")) {
            String groupId = JsonUtil.rawToString(m.get("groupId"));
            String username = JsonUtil.rawToString(m.get("username"));

            Group g = group.getGroupById(groupId);
            if (g == null) throw new RuntimeException("group not found");
            if (!g.getOwnerUsername().equalsIgnoreCase(loggedInUser)) throw new RuntimeException("only owner can add member");

            group.addMember(groupId, username);
            return "{\"status\":\"ok\",\"message\":\"member added\"}";
        }

        if (action.equals("ADD_EXPENSE")) {
            String groupId = JsonUtil.rawToString(m.get("groupId"));
            String title = JsonUtil.rawToString(m.get("title"));
            String payer = JsonUtil.rawToString(m.get("payer"));
            long amount = JsonUtil.rawToLong(m.get("amount"), 0);

            Group g = group.getGroupById(groupId);
            if (g == null) throw new RuntimeException("group not found");

            boolean isMember = false;
            for (String u : g.getMembers()) {
                if (u.equalsIgnoreCase(loggedInUser)) { isMember = true; break; }
            }
            if (!isMember) throw new RuntimeException("access denied");

            Expense e = group.addExpense(groupId, title, payer, amount);
            return "{"
                + "\"status\":\"ok\","
                + "\"expense\":{"
                    + "\"id\":" + JsonUtil.quote(e.getId()) + ","
                    + "\"title\":" + JsonUtil.quote(e.getTitle()) + ","
                    + "\"payer\":" + JsonUtil.quote(e.getPayer()) + ","
                    + "\"amount\":" + e.getAmount() + ","
                    + "\"timestamp\":" + e.getTimestamp()
                + "}"
                + "}";
        }

        if (action.equals("GET_GROUP_NET")) {
            String groupId = JsonUtil.rawToString(m.get("groupId"));
            Group g = group.getGroupById(groupId);
            if (g == null) throw new RuntimeException("group not found");

            boolean isMember = false;
            for (String u : g.getMembers()) {
                if (u.equalsIgnoreCase(loggedInUser)) { isMember = true; break; }
            }
            if (!isMember) throw new RuntimeException("access denied");

            Map<String, Long> net = group.calculateNet(groupId);

            StringBuilder sb = new StringBuilder();
            sb.append("{\"status\":\"ok\",\"net\":[");
            int i = 0;
            for (Map.Entry<String, Long> en : net.entrySet()) {
                if (i++ > 0) sb.append(",");
                sb.append("{\"username\":")
                    .append(JsonUtil.quote(en.getKey()))
                    .append(",\"amount\":")
                    .append(en.getValue())
                    .append("}");
            }
            sb.append("]}");
            return sb.toString();
        }

        throw new RuntimeException("unknown action");
    }
}
