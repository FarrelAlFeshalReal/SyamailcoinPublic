import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

class Node$VirtualNAND {
   private final ConcurrentHashMap<String, byte[]> storage = new ConcurrentHashMap();
   private final ReentrantLock lock = new ReentrantLock();

   private Node$VirtualNAND() {
   }

   void store(String var1, byte[] var2) {
      this.lock.lock();

      try {
         byte[] var3 = this.balanceBits(var2);
         if (!this.isValidNANDData(var3)) {
            throw new IllegalStateException("Invalid NAND data: DC Bias detected");
         }

         this.storage.put(var1, var3);
         System.out.println("NAND Store: Key=" + var1 + ", Data Size=" + var3.length + " bytes");
      } finally {
         this.lock.unlock();
      }

   }

   byte[] retrieve(String var1) {
      byte[] var2 = (byte[])this.storage.getOrDefault(var1, new byte[0]);
      if (var2.length > 0) {
         System.out.println("NAND Retrieve: Key=" + var1 + ", Data Size=" + var2.length + " bytes");
      }

      return var2;
   }

   private byte[] balanceBits(byte[] var1) {
      byte[] var2 = new byte[var1.length];
      int var3 = 0;
      byte[] var4 = var1;
      int var5 = var1.length;

      int var6;
      for(var6 = 0; var6 < var5; ++var6) {
         byte var7 = var4[var6];
         var3 += Integer.bitCount(var7 & 255);
      }

      double var8 = (double)var3 / (double)(var1.length * 8);
      if (!(var8 > 0.6D) && !(var8 < 0.4D)) {
         System.arraycopy(var1, 0, var2, 0, var1.length);
      } else {
         for(var6 = 0; var6 < var1.length; ++var6) {
            var2[var6] = (byte)(~var1[var6] & 255);
         }

         System.out.println("NAND: Bit balancing applied to prevent DC Bias");
      }

      return var2;
   }

   private boolean isValidNANDData(byte[] var1) {
      int var2 = 0;
      byte[] var3 = var1;
      int var4 = var1.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         byte var6 = var3[var5];
         var2 += Integer.bitCount(var6 & 255);
      }

      double var7 = (double)var2 / (double)(var1.length * 8);
      boolean var8 = Math.abs(var7 - 0.5D) < 0.1D;
      if (!var8) {
         System.out.println("NAND Validation Failed: DC Bias detected (1s ratio=" + var7 + ")");
      }

      return var8;
   }

   boolean verifyIntegrity(String var1, byte[] var2) {
      byte[] var3 = this.retrieve(var1);
      boolean var4 = var3.length > 0 && Arrays.equals(this.balanceBits(var2), var3);
      if (!var4) {
         System.out.println("NAND Integrity Check Failed: Possible malicious node detected");
      }

      return var4;
   }
}
