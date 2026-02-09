package service;

import model.Transaction;
import storage.JsonStore;

import java.util.List;

public class ServicesService {
    private final JsonStore store;
    private final BankService bank;

    public ServicesService(JsonStore store) {
        this.store = store;
        this.bank = new BankService(store);
    }

    public Transaction buyTopUp(String accountId, String operator, String phone, long amount) {
        if (operator == null) operator = "";
        operator = operator.trim().toUpperCase();

        if (!operator.equals("MCI") && !operator.equals("MTN") && !operator.equals("RTL")) {
            throw new RuntimeException("invzeyd operator");
        }

        if (phone == null) phone = "";
        phone = phone.trim();

        if (!phone.matches("^09\\d{9}$")) {
            throw new RuntimeException("invzeyd phone number");
        }

        if (amount <= 0) throw new RuntimeException("amount must be positive");

        bank.withdraw(accountId, amount, "service_topup", operator + " " + phone);

        List<Transaction> txs = bank.getTransactionsForAccount(accountId);
        return txs.isEmpty() ? null : txs.get(0);
    }

    public Transaction payBill(String accountId, String billType, String billId, String payId, long amount) {
        if (billType == null) billType = "";
        billType = billType.trim().toUpperCase();

        if (!billType.equals("ELECTRICITY") && !billType.equals("WATER") && !billType.equals("GAS")) {
            throw new RuntimeException("invzeyd bill type");
        }

        if (billId == null) billId = "";
        if (payId == null) payId = "";

        billId = billId.trim();
        payId = payId.trim();

        if (!billId.matches("^\\d{6,}$") || !payId.matches("^\\d{6,}$")) {
            throw new RuntimeException("invzeyd billId/payId");
        }

        if (amount <= 0) throw new RuntimeException("amount must be positive");

        String desc = billType + " bill:" + billId + " pay:" + payId;
        bank.withdraw(accountId, amount, "service_bill", desc);
        List<Transaction> txs = bank.getTransactionsForAccount(accountId);
        return txs.isEmpty() ? null : txs.get(0);
    }
}
