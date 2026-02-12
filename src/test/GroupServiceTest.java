package test;

import java.util.Iterator;
import java.util.Map;
import model.Group;
import service.AuthService;
import service.GroupService;
import storage.JsonStore;
public class GroupServiceTest {
   public GroupServiceTest() {
   }
   public static void main(String[] var0) {
      try {
         long var1 = System.currentTimeMillis();
         String var3 = "data_test_group_" + var1;
         String var4 = "ali_" + var1;
         String var5 = "reza_" + var1;
         JsonStore var6 = new JsonStore(var3);
         AuthService var7 = new AuthService(var6);
         GroupService var8 = new GroupService(var6, var7);
         var7.register(var4, "1234", "Ali");
         var7.register(var5, "1234", "Reza");
         Group var9 = var8.createGroup("Trip", var4, "IRR", "demo");
         var8.addMember(var9.getId(), var5);
         var8.addExpense(var9.getId(), "hotel", var4, 900L);
         var8.addExpense(var9.getId(), "food", var5, 300L);
         Map var10 = var8.calculateNet(var9.getId());
         long var11 = 0L;
         long var14;
         for(Iterator var13 = var10.values().iterator(); var13.hasNext(); var11 += var14) {
            var14 = (Long)var13.next();
         }
         TestUtil.assertTrue(var11 == 0L, "net sum should be zero");
         TestUtil.pass("group net calculation ok");
      } catch (Exception var16) {
         TestUtil.fail("GroupServiceTest", var16);
      }

   }
}
