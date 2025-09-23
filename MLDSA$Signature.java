import java.math.BigInteger;
import java.util.Arrays;

class MLDSA$Signature {
   BigInteger[] z;
   BigInteger c;

   MLDSA$Signature(BigInteger[] var1, BigInteger var2) {
      this.z = var1;
      this.c = var2;
   }

   public String toString() {
      String var10000 = Arrays.toString(this.z);
      return var10000 + ":" + this.c.toString();
   }
}
