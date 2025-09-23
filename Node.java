import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.client.WebSocketClient;

public class Node extends WebSocketServer {
    private static final int PORT = 5000;
    private static final String BLOCK_FILE = "blockrecursive.jsonl";
    private static final double THRESHOLD = 0.1447; 
    private static final SecureRandom random = new SecureRandom();

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
                System.out.println("NAND Store: Key=" + key + ", Data Size=" + balancedData.length + " bytes");
            } finally {
                lock.unlock();
            }
        }

        byte[] retrieve(String key) {
            byte[] data = storage.getOrDefault(key, new byte[0]);
            if (data.length > 0) {
                System.out.println("NAND Retrieve: Key=" + key + ", Data Size=" + data.length + " bytes");
            }
            return data;
        }

        private byte[] balanceBits(byte[] data) {
            byte[] result = new byte[data.length];
            int oneCount = 0;
            for (byte b : data) oneCount += Integer.bitCount(b & 0xFF);
            double bias = (double) oneCount / (data.length * 8);
            if (bias > 0.6 || bias < 0.4) {
                for (int i = 0; i < data.length; i++) result[i] = (byte) (~data[i] & 0xFF);
                System.out.println("NAND: Bit balancing applied to prevent DC Bias");
            } else {
                System.arraycopy(data, 0, result, 0, data.length);
            }
            return result;
        }

        private boolean isValidNANDData(byte[] data) {
            int oneCount = 0;
            for (byte b : data) oneCount += Integer.bitCount(b & 0xFF);
            double bias = (double) oneCount / (data.length * 8);
            boolean valid = Math.abs(bias - 0.5) < 0.1;
            if (!valid) {
                System.out.println("NAND Validation Failed: DC Bias detected (1s ratio=" + bias + ")");
            }
            return valid;
        }

        boolean verifyIntegrity(String key, byte[] originalData) {
            byte[] stored = retrieve(key);
            boolean valid = stored.length > 0 && Arrays.equals(balanceBits(originalData), stored);
            if (!valid) {
                System.out.println("NAND Integrity Check Failed: Possible malicious node detected");
            }
            return valid;
        }
    }

    private static final VirtualNAND nandStorage = new VirtualNAND();
    private static final List<WebSocket> peers = Collections.synchronizedList(new ArrayList<>());
    private static final List<Block> blockrecursive = Collections.synchronizedList(new ArrayList<>());
    private static final ReentrantLock blockrecursiveLock = new ReentrantLock();

    public Node(int port) {
        super(new InetSocketAddress(port));
    }

    private static class Block {
        int index;
        String recursiveHash, prevRecursiveHash, recipient, message;
        BigDecimal amount;
        double fValue, proofExponomial;
        String mldsaSignature;
        boolean isValid;

        Block(int index, String recursiveHash, String prevRecursiveHash, String recipient,
              BigDecimal amount, String message, double fValue, double proofExponomial, String mldsaSignature) {
            this.index = index;
            this.recursiveHash = recursiveHash;
            this.prevRecursiveHash = prevRecursiveHash;
            this.recipient = recipient;
            this.amount = amount;
            this.message = message;
            this.fValue = fValue;
            this.proofExponomial = proofExponomial;
            this.mldsaSignature = mldsaSignature;
            this.isValid = true;
        }

        String toJson() {
            return String.format(
                "{\"index\":%d,\"recursive_hash\":\"%s\",\"prev_recursive_hash\":\"%s\",\"recipient\":\"%s\",\"amount\":%s,\"message\":\"%s\",\"F\":%f,\"proof_exponomial\":%f,\"MLDSA\":\"%s\",\"valid\":%b}",
                index, recursiveHash, prevRecursiveHash, recipient, amount.toPlainString(),
                message, fValue, proofExponomial, mldsaSignature, isValid
            );
        }
    }

    private static String generateRecursiveHash(int blockIndex, BigDecimal amount, String prevHash, long timestamp, String contract) {
        String input = blockIndex + amount.toString() + prevHash + timestamp + contract;
        byte[] hashBytes = SAI288.sai288Hash(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) sb.append(String.format("%02x", b & 0xFF));
        return sb.toString();
    }

    private static boolean validateContract(String contract) {
        return contract != null && !contract.isEmpty() && contract.contains("agreement");
    }

    private static boolean verifyBlock(Block block, double proof) {
        return proof >= THRESHOLD; 
    }

    private static void reverifyBlockrecursive() {
        blockrecursiveLock.lock();
        try {
            for (int i = 0; i < blockrecursive.size(); i++) {
                Block block = blockrecursive.get(i);
                if (!block.isValid) {
                    String newHash = generateRecursiveHash(block.index, block.amount, block.prevRecursiveHash, System.currentTimeMillis(), block.message);
                    double newProof = SAI288.proofOfExponomial(
                        Math.min(25, block.index + 10),
                        Math.min(5, block.index / 2 + 1),
                        Math.min(20, block.index + 5),
                        Math.min(3, block.index / 3 + 1)
                    );
                    if (verifyBlock(block, newProof)) {
                        block.recursiveHash = newHash;
                        block.proofExponomial = newProof;
                        block.isValid = true;
                        for (int j = i + 1; j < blockrecursive.size(); j++) {
                            Block next = blockrecursive.get(j);
                            next.prevRecursiveHash = block.recursiveHash;
                        }
                    }
                }
            }
        } finally {
            blockrecursiveLock.unlock();
        }
    }

    private static boolean broadcastAndVerifyConsensus(Block block) {
        int agreeCount = 0;
        synchronized (peers) {
            for (WebSocket peer : peers) {
                peer.send(block.toJson());
                double peerProof = SAI288.proofOfExponomial(
                    Math.min(25, block.index + 10),
                    Math.min(5, block.index / 2 + 1),
                    Math.min(20, block.index + 5),
                    Math.min(3, block.index / 3 + 1)
                );
                if (peerProof >= THRESHOLD) agreeCount++; 
            }
        }
        return (double) agreeCount / Math.max(1, peers.size()) >= THRESHOLD; 
    }

    private static void appendBlock(Block block) {
        blockrecursiveLock.lock();
        try {
            byte[] blockData = block.toJson().getBytes();
            nandStorage.store(block.recursiveHash, blockData);
            if (!nandStorage.verifyIntegrity(block.recursiveHash, blockData)) {
                System.out.println("NAND Integrity Check Failed - Malicious node detected, block rejected");
                return;
            }
            blockrecursive.add(block);
            try (FileWriter fw = new FileWriter(BLOCK_FILE, true)) {
                fw.write(block.toJson() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            blockrecursiveLock.unlock();
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        synchronized (peers) {
            peers.add(conn);
            System.out.println("New peer connected: " + conn.getRemoteSocketAddress());
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        synchronized (peers) {
            peers.remove(conn);
            System.out.println("Peer disconnected: " + conn.getRemoteSocketAddress());
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            if (message.startsWith("{\"index\":")) {
                String[] parts = message.split(",");
                int index = Integer.parseInt(parts[0].split(":")[1]);
                String recursiveHash = parts[1].split(":")[1].replaceAll("[\"}]", "");
                String prevHash = parts[2].split(":")[1].replaceAll("[\"}]", "");
                String recipient = parts[3].split(":")[1].replaceAll("[\"}]", "");
                BigDecimal amount = new BigDecimal(parts[4].split(":")[1].replaceAll("[\"}]", ""));
                String contract = parts[5].split(":")[1].replaceAll("[\"}]", "");
                double fValue = Double.parseDouble(parts[6].split(":")[1].replaceAll("[\"}]", ""));
                double proof = Double.parseDouble(parts[7].split(":")[1].replaceAll("[\"}]", ""));
                String mldsa = parts[8].split(":")[1].replaceAll("[\"}]", "");

                Block block = new Block(index, recursiveHash, prevHash, recipient, amount, contract, fValue, proof, mldsa);
                if (verifyBlock(block, proof) && broadcastAndVerifyConsensus(block)) {
                    appendBlock(block);
                } else {
                    block.isValid = false;
                    appendBlock(block);
                    System.out.println("Block " + index + " marked invalid: Possible malicious node detected");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Node server started on port " + getPort());
    }

    public static void main(String[] args) {
        int[] ports = {5000, 5001, 5002, 5003, 5004};
        List<Node> servers = new ArrayList<>();

        for (int port : ports) {
            Node server = new Node(port);
            servers.add(server);
            server.start();

            for (int p : ports) {
                if (p != port) {
                    try {
                        final Node currentServer = server;
                        WebSocketClient client = new WebSocketClient(new java.net.URI("ws://localhost:" + p)) {
                            @Override
                            public void onOpen(ServerHandshake handshake) {
                                System.out.println("Connected to peer at port " + p);
                            }

                            @Override
                            public void onMessage(String message) {
                                currentServer.onMessage(this, message);
                            }

                            @Override
                            public void onClose(int code, String reason, boolean remote) {
                                System.out.println("Disconnected from peer at port " + p);
                            }

                            @Override
                            public void onError(Exception ex) {
                                ex.printStackTrace();
                            }
                        };
                        client.connect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    }
