import java.math.BigInteger;
import java.security.SecureRandom;

class MLDSA {
   private static final BigInteger Q = new BigInteger("8380417");
   private static final int K = 4;
   private static final int L = 4;
   private static final int ETA = 2;
   private static final int BETA = 78;
   private static final SecureRandom random = new SecureRandom();

   static MLDSA.Signature sign(BigInteger[] var0, BigInteger var1) {
      BigInteger[] var2 = new BigInteger[4];

      for(int var3 = 0; var3 < 4; ++var3) {
         var2[var3] = (new BigInteger(5, random)).subtract(BigInteger.valueOf(2L)).mod(Q);
      }

      BigInteger[] var6 = new BigInteger[16];

      for(int var4 = 0; var4 < 16; ++var4) {
         var6[var4] = (new BigInteger(Q.bitLength(), random)).mod(Q);
      }

      BigInteger[] var7 = new BigInteger[4];

      for(int var5 = 0; var5 < 4; ++var5) {
         var7[var5] = var0[var5].add(var2[var5]).mod(Q);
         if (var7[var5].abs().compareTo(BigInteger.valueOf(78L)) > 0) {
            return sign(var0, var1);
         }
      }

      BigInteger var8 = (new BigInteger(1, SAI288.sai288Hash(var1.toByteArray()))).mod(Q);
      return new MLDSA.Signature(var7, var8);
   }

   static boolean verify(BigInteger[] var0, BigInteger var1, MLDSA.Signature var2) {
      BigInteger[] var3 = var2.z;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         BigInteger var6 = var3[var5];
         if (var6.abs().compareTo(BigInteger.valueOf(78L)) > 0) {
            return false;
         }
      }

      BigInteger var7 = (new BigInteger(1, SAI288.sai288Hash(var1.toByteArray()))).mod(Q);
      return var7.equals(var2.c);
   }
} 
