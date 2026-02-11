package test;

import service.AuthService;
import storage.JsonStore;

public class ChangePasswordTest {
    public static void main(String[] args) {
        try {
            long runId = System.currentTimeMillis();
            String dir = "data_test_pass_" + runId;

            JsonStore store = new JsonStore(dir);
            AuthService auth = new AuthService(store);

            String username = "zey_" + runId;

            auth.register(username, "1234", "zey");
            auth.login(username, "1234");

            auth.changePassword(username, "1234", "9999");
            auth.login(username, "9999");

            boolean oldStillWorks = true;
            try { auth.login(username, "1234"); } catch (Exception e) { oldStillWorks = false; }
            TestUtil.assertTrue(!oldStillWorks, "old password should not work");
            TestUtil.pass("change password ok");

        } catch (Exception e) {
            TestUtil.fail("ChangePasswordTest", e);
        }
    }
}
