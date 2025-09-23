import java.math.BigInteger;

public class MLDSA_Syamailcoin$Signature {
   public final BigInteger r;
   public final BigInteger s;

   public MLDSA_Syamailcoin$Signature(BigInteger var1, BigInteger var2) {
      this.r = var1;
      this.s = var2;
   }

   public String toString() {
      String var10000 = this.r.toString();
      return var10000 + ":" + this.s.toString();
   }
}
