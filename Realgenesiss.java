import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Realgenesiss {
    private static final String BLOCKRECURSIVE_FILE = "blockrecursive.jsonl";
    private static final BigDecimal TOTAL_GENESIS_AMOUNT = new BigDecimal("235294");
    private static final BigDecimal MIN_REWARD = new BigDecimal("0.0002231668235294118");
    private static final BigDecimal MAX_SUPPLY = new BigDecimal("9469999.9999999428");
    private static final String GENESIS_MESSAGE = "https://www.presidency.ucsb.edu/documents/joint-statement-following-meeting-with-the-president-the-commission-the-european-economic Human nature changes and This will pass";
    private static final String VERSION = "1.0.0";

    private static final BigDecimal[] STAGE_AMOUNTS = {
        new BigDecimal("4104313.1758309230208"),
        new BigDecimal("3743507.3299902770668"),
        new BigDecimal("1186437.2000982209574"),
        new BigDecimal("200448.2940805212129"),
        TOTAL_GENESIS_AMOUNT
    };

    private static final double[] STAGE_TIMES = {868.2, 720.0, 72.35, 36.91326530612244898, 18.83};

    private static class VirtualNAND {
        private final ConcurrentHashMap<String, byte[]> storage = new ConcurrentHashMap<>();
        private final ReentrantLock lock = new ReentrantLock();

        void store(String key, byte[] data) {
            lock.lock();
            try {
                byte[] balancedData = balanceBits(data);
                if (!isValidNANDData(balancedData)) {
                    throw new IllegalStateException("Invalid NAND data: DC Bias detected");
                }
                storage.put(key, balancedData);
                System.out.println("Presenting payment negotiation untouched by Natural Falsehood");
            } finally {
                lock.unlock();
            }
        }

        byte[] retrieve(String key) {
            return storage.getOrDefault(key, new byte[0]);
        }

        private byte[] balanceBits(byte[] data) {
            byte[] result = new byte[data.length];
            int oneCount = 0;
            for (byte b : data) oneCount += Integer.bitCount(b & 0xFF);
            double bias = (double) oneCount / (data.length * 8);
            if (bias > 0.6 || bias < 0.4) {
                for (int i = 0; i < data.length; i++) result[i] = (byte) (~data[i] & 0xFF);
            } else {
                System.arraycopy(data, 0, result, 0, data.length);
            }
            return result;
        }

        private boolean isValidNANDData(byte[] data) {
            int oneCount = 0;
            for (byte b : data) oneCount += Integer.bitCount(b & 0xFF);
            double bias = (double) oneCount / (data.length * 8);
            return Math.abs(bias - 0.5) < 0.1;
        }

        boolean verifyIntegrity(String key, byte[] originalData) {
            byte[] stored = retrieve(key);
            return stored.length > 0 && Arrays.equals(balanceBits(originalData), stored);
        }
    }

    private static final VirtualNAND nandStorage = new VirtualNAND();

    private static int getCurrentStage(BigDecimal totalGenerated) {
        BigDecimal accumulated = BigDecimal.ZERO;
        for (int i = 0; i < STAGE_AMOUNTS.length; i++) {
            accumulated = accumulated.add(STAGE_AMOUNTS[i]);
            if (totalGenerated.compareTo(accumulated) <= 0) return i;
        }
        return STAGE_AMOUNTS.length - 1;
    }

    private static BigDecimal performInternalAccumulation() {
        int iterationsNeeded = TOTAL_GENESIS_AMOUNT.divide(MIN_REWARD, 0, RoundingMode.UP).intValue();
        BigDecimal accumulated = BigDecimal.ZERO;
        
        for (int i = 0; i < iterationsNeeded; i++) {
            BigDecimal remaining = TOTAL_GENESIS_AMOUNT.subtract(accumulated);
            BigDecimal currentReward = MIN_REWARD.min(remaining);
            accumulated = accumulated.add(currentReward);
            if (accumulated.compareTo(TOTAL_GENESIS_AMOUNT) >= 0) break;
        }
        return accumulated;
    }

    public static BigDecimal getGenesisAmount() {
        return TOTAL_GENESIS_AMOUNT;
    }

    public static BigDecimal getMinReward() {
        return MIN_REWARD;
    }

    public static BigDecimal getMaxSupply() {
        return MAX_SUPPLY;
    }

    public static void main(String[] args) {
        System.out.println("Presenting payment negotiation untouched by Natural Falsehood");
        System.out.println("Syamailcoin Genesis Generator v" + VERSION);
        
        GenesisWallet.GenesisKeyPair genesisWallet = GenesisWallet.generateGenesisWallet();
        BigDecimal finalGenesisAmount = performInternalAccumulation();
        
        int blockIndex = 0;
        String prevHash = "genesis";

        try {
            int stage = getCurrentStage(BigDecimal.ZERO);
            double F_val = STAGE_TIMES[stage];
            
            String transactionData = blockIndex + finalGenesisAmount.toString() + prevHash + GENESIS_MESSAGE;
            byte[] commitment = SAI288.sai288Hash(transactionData.getBytes());
            
            double proof = SAI288.proofOfExponomial(25, 5, 20, 3);
            if (proof < 0.1447) return;

            long timestamp = System.currentTimeMillis();
            String recursiveHash = bytesToHex(SAI288.sai288Hash((blockIndex + finalGenesisAmount.toString() + prevHash + timestamp + GENESIS_MESSAGE).getBytes()));
            double accumulation = SAI288.accumulation(64);

            byte[] mldsaSignature = MLDSA_Syamailcoin.sign(genesisWallet.privateKey, commitment);

            String blockJson = String.format(
                "{\"index\":%d,\"recursive_hash\":\"%s\",\"prev_recursive_hash\":\"%s\",\"recipient\":\"%s\",\"amount\":%s,\"message\":\"%s\",\"F\":%f,\"proof_exponomial\":%f,\"accumulation\":%f,\"timestamp\":%d,\"commitment\":\"%s\",\"signature\":\"%s\",\"version\":\"%s\"}",
                blockIndex, recursiveHash, prevHash, genesisWallet.address, finalGenesisAmount.toPlainString(),
                GENESIS_MESSAGE, F_val, proof, accumulation, timestamp,
                bytesToHex(commitment), bytesToHex(mldsaSignature), VERSION
            );

            byte[] blockData = blockJson.getBytes();
            nandStorage.store(recursiveHash, blockData);
            if (!nandStorage.verifyIntegrity(recursiveHash, blockData)) return;

            try (FileWriter writer = new FileWriter(BLOCKRECURSIVE_FILE, true)) {
                writer.write(blockJson + "\n");
                writer.flush();
            }

            Node.broadcastGenesisBlock(blockJson);
            System.out.println("Genesis block created: " + recursiveHash);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}