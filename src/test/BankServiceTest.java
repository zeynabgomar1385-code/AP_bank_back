package test;

import model.Account;
import service.AuthService;
import service.BankService;
import storage.JsonStore;
import java.util.List;
import java.util.UUID;

public class BankServiceTest {
    public static void main(String[] args) {
        try {
            String runId = UUID.randomUUID().toString().replace("-", "");
            String dir = "data_test_bank_" + runId;
            JsonStore store = new JsonStore(dir);
            AuthService auth = new AuthService(store);
            BankService bank = new BankService(store);
            String username = "zey_" + runId.substring(0, 10);
            auth.register(username, "1234", "zey");
            Account a1 = bank.createAccount(username, "current", 2000L);
            Account a2 = bank.createAccount(username, "savings", 1000L);
            TestUtil.assertTrue(a1.getCardNumber().length() == 16, "card length 16");
            TestUtil.assertTrue(!a1.getCardNumber().equals(a2.getCardNumber()), "card must be unique");
            TestUtil.pass("create account unique numbers");
            bank.deposit(a1.getId(), 500L, "salary", "test deposit");
            bank.withdraw(a1.getId(), 200L, "life", "test withdraw");
            List<Account> accounts = bank.getAccounts(username);
            long balance = 0;
            for (Account acc : accounts) {
                if (acc.getId().equals(a1.getId())) {
                    balance = acc.getBalance();
                }
            }
            TestUtil.assertTrue(balance == 2300L, "balance should be 2300");
            TestUtil.pass("deposit/withdraw ok");
        } catch (Exception e) {
            TestUtil.fail("BankServiceTest", e);
        }
    }
}
