import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Realgenesiss {
    private static final String BLOCKRECURSIVE_FILE = "blockrecursive.jsonl";
    private static final BigDecimal TOTAL_GENESIS_AMOUNT = new BigDecimal("235294");
    private static final int MAX_GENESIS_BLOCKS = 1;
    private static final String GENESIS_ADDRESS = "MFfnBadpVifC";
    private static final String GENESIS_MESSAGE =
            "https://www.presidency.ucsb.edu/documents/joint-statement-following-meeting-with-the-president-the-commission-the-european-economic Human nature changes and This will pass";

    private static final double[] STAGE_TIMES = {868.2, 720.0, 72.35, 36.91326530612244898, 18.83};
    private static final MathContext PRECISION = new MathContext(50, RoundingMode.HALF_UP);
    private static final SecureRandom random = new SecureRandom();
    private static final BigDecimal MIN_REWARD = new BigDecimal("0.0002231668235294118");

    private static class VirtualNAND {
        private final ConcurrentHashMap<String, byte[]> storage = new ConcurrentHashMap<>();
        private final ReentrantLock lock = new ReentrantLock();

        void store(String key, byte[] data) {
            lock.lock();
            try { storage.put(key, data); } finally { lock.unlock(); }
        }

        byte[] retrieve(String key) {
            return storage.getOrDefault(key, new byte[0]);
        }
    }

    private static final VirtualNAND nandStorage = new VirtualNAND();

    private static String generateRecursiveHash(int blockIndex, BigDecimal amount, String prevHash, long timestamp, String contract) {
        String input = blockIndex + amount.toString() + prevHash + timestamp + contract;
        byte[] hashBytes = SAI288.sai288Hash(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) sb.append(String.format("%02x", b & 0xFF));
        return sb.toString();
    }

    private static boolean validateContract(String contract) {
        return contract != null && !contract.isEmpty();
    }

    private static void appendBlockToFile(String blockJson) {
        try (FileWriter writer = new FileWriter(BLOCKRECURSIVE_FILE, true)) {
            writer.write(blockJson + "\n");
        } catch (IOException e) {
            System.err.println("Error writing block: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Syamailcoin Real Genesis Generation ===");
        System.out.println("Target: " + TOTAL_GENESIS_AMOUNT + " SAC");
        System.out.println("Genesis Address: " + GENESIS_ADDRESS);
        System.out.println("Stage: Iteration Derivative (4)\n");

        int blockIndex = 0;
        BigDecimal totalGenerated = BigDecimal.ZERO;
        String prevHash = "genesis";

        int keyCount;
        try {
            java.lang.reflect.Field fld = MLDSA_Syamailcoin.class.getField("K");
            keyCount = fld.getInt(null);
            if (keyCount <= 0) keyCount = 1;
        } catch (Exception e) { keyCount = 1; }

        BigInteger[] privateKey = new BigInteger[keyCount];
        for (int i = 0; i < keyCount; i++)
            privateKey[i] = new BigInteger(MLDSA_Syamailcoin.Q.bitLength(), random).mod(MLDSA_Syamailcoin.Q);

        BigDecimal fixedReward = TOTAL_GENESIS_AMOUNT.divide(new BigDecimal(MAX_GENESIS_BLOCKS), PRECISION);

        while (totalGenerated.compareTo(TOTAL_GENESIS_AMOUNT) < 0 && blockIndex < MAX_GENESIS_BLOCKS) {
            try {
                int stage = 4;
                BigDecimal reward = TOTAL_GENESIS_AMOUNT;
                totalGenerated = totalGenerated.add(reward);

                System.out.println("Genesis Block - Stage " + stage + " - Generated: "
                        + totalGenerated + "/" + TOTAL_GENESIS_AMOUNT
                        + " (Minimal Reward: " + MIN_REWARD + ")");

                double F_val = STAGE_TIMES[stage];
                System.out.println("Stage Time Value: " + F_val);

                double proof = SAI288.proofOfExponomial(
                        Math.min(25, blockIndex + 10),
                        Math.min(5, blockIndex / 2 + 1),
                        Math.min(20, blockIndex + 5),
                        Math.min(3, blockIndex / 3 + 1)
                );
                System.out.println("Delta Maths Proof: " + proof);

                String contract = blockIndex == 0 ? GENESIS_MESSAGE : "agreement:genesis_" + blockIndex;
                if (!validateContract(contract)) continue;

                long timestamp = System.currentTimeMillis();
                String recursiveHash = generateRecursiveHash(blockIndex, reward, prevHash, timestamp, contract);

                String blockJson = String.format(
                        "{\"index\":%d,\"recursive_hash\":\"%s\",\"prev_recursive_hash\":\"%s\",\"recipient\":\"%s\",\"amount\":%s,\"message\":\"%s\",\"F\":%f,\"proof_exponomial\":%f,\"stage\":\"genesis\"}",
                        blockIndex, recursiveHash, prevHash, GENESIS_ADDRESS, reward.toPlainString(),
                        blockIndex == 0 ? GENESIS_MESSAGE : "", F_val, proof
                );

                byte[] blockData = blockJson.getBytes();
                MLDSA_Syamailcoin.Signature sig = MLDSA_Syamailcoin.sign(privateKey[0], blockData);
                blockJson = blockJson.substring(0, blockJson.length() - 1) + ",\"MLDSA\":\"" + sig.toString() + "\"}";

                nandStorage.store(recursiveHash, blockData);
                appendBlockToFile(blockJson);

                System.out.println("Blockrecursive Generated: " + reward + " SAC, Hash=" + recursiveHash + "\n");

                prevHash = recursiveHash;
                blockIndex++;

            } catch (Exception e) {
                System.err.println("Error generating block " + blockIndex + ": " + e.getMessage());
                break;
            }
        }

        BigDecimal maxSupply = new BigDecimal("9469999.9999999428");
        BigDecimal remainingSupply = maxSupply.subtract(totalGenerated);

        System.out.println("=== Genesis Generation Complete ===");
        System.out.println("Total Syamailcoin Generated: " + totalGenerated + " SAC");
        System.out.println("Total Blocks: " + blockIndex);
        System.out.println("Genesis Address: " + GENESIS_ADDRESS);
        System.out.println("Maximum Supply: " + maxSupply + " SAC");
        System.out.println("Remaining Supply: " + remainingSupply + " SAC");
        System.out.println("Output saved to: " + BLOCKRECURSIVE_FILE);
    }
}