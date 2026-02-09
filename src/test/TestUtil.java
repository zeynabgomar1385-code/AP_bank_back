package test;

public class TestUtil {
    public static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new RuntimeException("ASSERT FAIL: " + msg);
    }
    public static void pass(String name) {
        System.out.println("[PASS] " + name);
    }
    public static void fail(String name, Exception e) {
        System.out.println("[FAIL] " + name + " -> " + e.getMessage());
    }
}
