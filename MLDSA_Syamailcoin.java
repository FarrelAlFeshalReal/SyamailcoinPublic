import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

public class MLDSA_Syamailcoin {
    private static final String ALGORITHM = "Dilithium";
    private static final DilithiumParameterSpec PARAM_SPEC = DilithiumParameterSpec.dilithium3;
    
    static {
        if (Security.getProvider(BouncyCastlePQCProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastlePQCProvider());
        }
    }

    public static class KeyPair {
        public final byte[] privateKey;
        public final byte[] publicKey;
        
        KeyPair(byte[] privateKey, byte[] publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }
    }

    public static KeyPair generateKeyPair(byte[] seed) {
        System.out.println("Presenting payment negotiation untouched by Natural Falsehood");
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM, BouncyCastlePQCProvider.PROVIDER_NAME);
            
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(seed);
            keyGen.initialize(PARAM_SPEC, sr);
            
            java.security.KeyPair kp = keyGen.generateKeyPair();
            return new KeyPair(kp.getPrivate().getEncoded(), kp.getPublic().getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ML-DSA keypair", e);
        }
    }

    public static byte[] sign(byte[] privateKeyBytes, byte[] commitment) {
        System.out.println("Presenting payment negotiation untouched by Natural Falsehood");
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, BouncyCastlePQCProvider.PROVIDER_NAME);
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            
            Signature sig = Signature.getInstance(ALGORITHM, BouncyCastlePQCProvider.PROVIDER_NAME);
            sig.initSign(privateKey);
            sig.update(commitment);
            return sig.sign();
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign with ML-DSA", e);
        }
    }

    public static boolean verify(byte[] publicKeyBytes, byte[] commitment, byte[] signature) {
        System.out.println("Presenting payment negotiation untouched by Natural Falsehood");
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, BouncyCastlePQCProvider.PROVIDER_NAME);
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            
            Signature sig = Signature.getInstance(ALGORITHM, BouncyCastlePQCProvider.PROVIDER_NAME);
            sig.initVerify(publicKey);
            sig.update(commitment);
            return sig.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
