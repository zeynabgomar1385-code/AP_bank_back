package test;

import model.Account;
import model.Transaction;
import service.BankService;
import service.ServicesService;
import storage.JsonStore;
import java.util.List;

public class ServicesServiceTest {

    public static void main(String[] args) {
        try {
            JsonStore store = new JsonStore("data");
            BankService bank = new BankService(store);
            ServicesService services = new ServicesService(store);
            Account a = bank.createAccount("zey", "current", 200000);
            Transaction t1 = services.buyTopUp(a.getId(), "MCI", "09123456789", 50000);
            long bal1 = bank.findAccountById(a.getId()).getBalance();
            if (bal1 != 150000) throw new RuntimeException("topup balance wrong");
            Transaction t2 = services.payBill(a.getId(), "ELECTRICITY", "123456", "654321", 40000);
            long bal2 = bank.findAccountById(a.getId()).getBalance();
            if (bal2 != 110000) throw new RuntimeException("bill balance wrong");
            List<Transaction> txs = bank.getTransactionsForAccount(a.getId());
            boolean hasTopup = false;
            boolean hasBill = false;
            for (Transaction t : txs) {
                if (t.getCategory().equals("service_topup")) hasTopup = true;
                if (t.getCategory().equals("service_bill")) hasBill = true;
            }
            if (!hasTopup) throw new RuntimeException("topup tx not saved");
            if (!hasBill) throw new RuntimeException("bill tx not saved");
            System.out.println("[PASS] services: topup + bill ok");
        } catch (Exception e) {
            System.out.println("[FAIL] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
