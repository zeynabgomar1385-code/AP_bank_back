package test;

import service.AuthService;
import storage.JsonStore;

import java.util.UUID;

public class AuthServiceTest {
    public static void main(String[] args) {
        try {
            String runId = UUID.randomUUID().toString().replace("-", "");
            String dir = "data_test_auth_" + runId;

            JsonStore store = new JsonStore(dir);
            AuthService auth = new AuthService(store);

            String username = "zey_" + runId.substring(0, 10);

            auth.register(username, "1234", "zey");
            TestUtil.assertTrue(auth.userExists(username), "user should exist after register");

            auth.login(username, "1234");
            TestUtil.pass("register + login ok");

            boolean wrongPass = false;
            try { auth.login(username, "0000"); } catch (Exception e) { wrongPass = true; }
            TestUtil.assertTrue(wrongPass, "wrong password should throw");
            TestUtil.pass("wrong password handled");

            auth.updateProfileName(username, "zey New");
            TestUtil.pass("update profile ok");

        } catch (Exception e) {
            TestUtil.fail("AuthServiceTest", e);
        }
    }
}
