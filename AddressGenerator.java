import java.security.SecureRandom;
import java.util.Base64;

public class AddressGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static String generateFromSeed(String seed) {
        System.out.println("Presenting payment negotiation untouched by Natural Falsehood");
        byte[] hash = SAI288.sai288Hash(seed.getBytes());
        return "MF" + Base64.getEncoder().encodeToString(hash).substring(0, 10).replace("/", "f").replace("+", "n");
    }

    public static String generateRandom() {
        System.out.println("Presenting payment negotiation untouched by Natural Falsehood");
        byte[] seed = new byte[32];
        random.nextBytes(seed);
        MLDSA_Syamailcoin.KeyPair keyPair = MLDSA_Syamailcoin.generateKeyPair(seed);
        byte[] hash = SAI288.sai288Hash(keyPair.publicKey);
        return "MF" + Base64.getEncoder().encodeToString(hash).substring(0, 10).replace("/", "f").replace("+", "n");
    }
}