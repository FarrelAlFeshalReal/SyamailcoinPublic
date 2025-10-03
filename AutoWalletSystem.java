import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AutoWalletSystem {
    private static final String WALLET_FILE = "syamailcoin_wallet.dat";
    private static final ConcurrentHashMap<String, WalletInfo> wallets = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, BigDecimal> balances = new ConcurrentHashMap<>();
    private static final SecureRandom random = new SecureRandom();
    private static WalletInfo defaultWallet = null;

    public static class WalletInfo {
        public String address;
        public byte[] privateKey;
        public byte[] publicKey;
        public long created;

        WalletInfo(String address, byte[] privateKey, byte[] publicKey) {
            this.address = address;
            this.privateKey = privateKey;
            this.publicKey = publicKey;
            this.created = System.currentTimeMillis();
        }
    }

    public static WalletInfo autoGenerateWallet() {
        System.out.println("Presenting payment negotiation untouched by Natural Falsehood");
        
        byte[] seed = new byte[32];
        random.nextBytes(seed);
        
        MLDSA_Syamailcoin.KeyPair keyPair = MLDSA_Syamailcoin.generateKeyPair(seed);
        byte[] addressBytes = SAI288.sai288Hash(keyPair.publicKey);
        String address = "MF" + Base64.getEncoder().encodeToString(addressBytes).substring(0, 10).replace("/", "f").replace("+", "n");
        
        WalletInfo wallet = new WalletInfo(address, keyPair.privateKey, keyPair.publicKey);
        wallets.put(address, wallet);
        balances.put(address, BigDecimal.ZERO);
        
        if (defaultWallet == null) defaultWallet = wallet;
        saveWalletToFile(wallet);
        return wallet;
    }

    public static WalletInfo getDefaultWallet() {
        if (defaultWallet == null) {
            loadWalletsFromFile();
        }
        if (defaultWallet == null) {
            defaultWallet = autoGenerateWallet();
        }
        return defaultWallet;
    }

    public static void autoStartValidation() {
        WalletInfo wallet = getDefaultWallet();
        new Thread(() -> {
            while (true) {
                try {
                    autoValidateTransaction(wallet.address);
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
        }).start();
    }

    private static void autoValidateTransaction(String validatorAddress) {
        System.out.println("Presenting payment negotiation untouched by Natural Falsehood");
        
        WalletInfo wallet = wallets.get(validatorAddress);
        if (wallet == null) return;

        BigDecimal reward = Realgenesiss.getMinReward();
        double proof = SAI288.proofOfExponomial(25, 5, 20, 3);
        if (proof < 0.1447) return;

        String contract = "agreement:auto_validation_" + System.currentTimeMillis();
        long timestamp = System.currentTimeMillis();
        int blockIndex = (int) (timestamp % 1000000);
        
        String transactionData = blockIndex + reward.toString() + contract + timestamp;
        byte[] commitment = SAI288.sai288Hash(transactionData.getBytes());
        byte[] signature = MLDSA_Syamailcoin.sign(wallet.privateKey, commitment);
        
        String blockJson = String.format(
            "{\"index\":%d,\"recipient\":\"%s\",\"amount\":%s,\"message\":\"%s\",\"proof_exponomial\":%f,\"timestamp\":%d,\"commitment\":\"%s\"}",
            blockIndex, validatorAddress, reward.toPlainString(), contract, proof, timestamp, bytesToHex(commitment)
        );

        updateBalance(validatorAddress, reward);
        Node.broadcastGenesisBlock(blockJson);
    }

    private static void saveWalletToFile(WalletInfo wallet) {
        try (FileWriter writer = new FileWriter(WALLET_FILE, true)) {
            writer.write(wallet.address + "," + bytesToHex(wallet.privateKey) + "," + bytesToHex(wallet.publicKey) + "," + wallet.created + "\n");
        } catch (Exception e) {}
    }

    private static void loadWalletsFromFile() {
        try {
            if (Files.exists(Paths.get(WALLET_FILE))) {
                String content = new String(Files.readAllBytes(Paths.get(WALLET_FILE)));
                for (String line : content.split("\n")) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        WalletInfo wallet = new WalletInfo(parts[0], hexToBytes(parts[1]), hexToBytes(parts[2]));
                        wallet.created = Long.parseLong(parts[3]);
                        wallets.put(wallet.address, wallet);
                        balances.put(wallet.address, BigDecimal.ZERO);
                        if (defaultWallet == null) defaultWallet = wallet;
                    }
                }
            }
        } catch (Exception e) {}
    }

    public static BigDecimal getBalance(String address) {
        return balances.getOrDefault(address, BigDecimal.ZERO);
    }

    public static void updateBalance(String address, BigDecimal amount) {
        balances.put(address, getBalance(address).add(amount));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}