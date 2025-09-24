import java.math.BigInteger;
import java.security.SecureRandom;

public class MLDSA_Syamailcoin {
    public static final BigInteger Q = new BigInteger("251");   
    public static final BigInteger P = new BigInteger("1009");
    public static final BigInteger G = new BigInteger("2");
    public static final int K = 1;  

    private static final SecureRandom random = new SecureRandom();

    
    public static class Signature {
        public final BigInteger r;
        public final BigInteger s;

        public Signature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        @Override
        public String toString() {
            return r.toString() + ":" + s.toString();
        }
    }

    
    public static Signature sign(BigInteger privateKey, byte[] data) {
        BigInteger hash = new BigInteger(1, data); 
        BigInteger k = new BigInteger(Q.bitLength(), random).mod(Q.subtract(BigInteger.ONE)).add(BigInteger.ONE); 
        BigInteger r = G.modPow(k, P).mod(Q);
        BigInteger s = k.modInverse(Q).multiply(hash.add(privateKey.multiply(r))).mod(Q);
        return new Signature(r, s);
    }

    
    public static boolean verify(BigInteger publicKey, byte[] data, Signature sig) {
        BigInteger hash = new BigInteger(1, data);
        BigInteger w = sig.s.modInverse(Q);
        BigInteger u1 = hash.multiply(w).mod(Q);
        BigInteger u2 = sig.r.multiply(w).mod(Q);
        BigInteger v = (G.modPow(u1, P).multiply(publicKey.modPow(u2, P))).mod(P).mod(Q);
        return v.equals(sig.r);
    }
}