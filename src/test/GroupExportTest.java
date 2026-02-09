package test;

import model.Group;
import service.AuthService;
import service.GroupService;
import storage.JsonStore;
import java.io.File;
import java.nio.file.Files;

public class GroupExportTest {
    public static void main(String[] args) {
        try {
            long runId = System.currentTimeMillis();
            String dir = "data_test_export_" + runId;
            JsonStore store = new JsonStore(dir);
            AuthService auth = new AuthService(store);
            GroupService gs = new GroupService(store, auth);
            String u1 = "zey_" + runId;
            String u2 = "reza_" + runId;
            auth.register(u1, "1234", "zey");
            auth.register(u2, "1234", "Reza");
            Group g = gs.createGroup("Trip", u1, "IRR", "demo");
            gs.addMember(g.getId(), u2);
            gs.addExpense(g.getId(), "hotel", u1, 900);
            gs.addExpense(g.getId(), "food", u2, 300);
            String path = gs.exportReport(g.getId(), "reports_test_" + runId);
            File f = new File(path);
            TestUtil.assertTrue(f.exists(), "report file should exist");
            String content = Files.readString(f.toPath());
            TestUtil.assertTrue(content.contains("Net Result"), "report should contain net");
            TestUtil.assertTrue(content.contains("Suggested Settlement"), "report should contain settlement");
            TestUtil.pass("group export report ok");
        } catch (Exception e) {
            TestUtil.fail("GroupExportTest", e);
        }
    }
}
