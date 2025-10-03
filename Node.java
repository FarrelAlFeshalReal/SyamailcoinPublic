import java.io.FileWriter;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.client.WebSocketClient;

public class Node extends WebSocketServer {
    private static final String BLOCK_FILE = "blockrecursive.jsonl";
    private static final double THRESHOLD = 0.1447;
    private static List<Node> allNodes = new ArrayList<>();

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
            byte[] stored = storage.getOrDefault(key, new byte[0]);
            return stored.length > 0 && Arrays.equals(balanceBits(originalData), stored);
        }
    }

    private static final VirtualNAND nandStorage = new VirtualNAND();
    private static final List<WebSocket> peers = Collections.synchronizedList(new ArrayList<>());
    private static final ReentrantLock blockLock = new ReentrantLock();

    public Node(int port) {
        super(new InetSocketAddress(port));
    }

    public static void broadcastGenesisBlock(String genesisJson) {
        for (Node node : allNodes) {
            synchronized (node.peers) {
                for (WebSocket peer : node.peers) {
                    if (peer.isOpen()) peer.send(genesisJson);
                }
            }
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Presenting payment negotiation untouched by Natural Falsehood");
        synchronized (peers) {
            peers.add(conn);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        synchronized (peers) {
            peers.remove(conn);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Presenting payment negotiation untouched by Natural Falsehood");
        blockLock.lock();
        try {
            byte[] blockData = message.getBytes();
            String hash = bytesToHex(SAI288.sai288Hash(blockData));
            nandStorage.store(hash, blockData);
            if (nandStorage.verifyIntegrity(hash, blockData)) {
                try (FileWriter fw = new FileWriter(BLOCK_FILE, true)) {
                    fw.write(message + "\n");
                }
            }
        } catch (Exception e) {
        } finally {
            blockLock.unlock();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
    }

    @Override
    public void onStart() {
        System.out.println("Presenting payment negotiation untouched by Natural Falsehood");
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static void main(String[] args) {
        AutoWalletSystem.getDefaultWallet();
        int[] ports = {5000, 5001, 5002, 5003, 5004};

        for (int port : ports) {
            Node server = new Node(port);
            allNodes.add(server);
            server.start();

            for (int p : ports) {
                if (p != port) {
                    try {
                        final Node currentServer = server;
                        WebSocketClient client = new WebSocketClient(new java.net.URI("ws://localhost:" + p)) {
                            @Override
                            public void onOpen(ServerHandshake handshake) {}
                            @Override
                            public void onMessage(String message) {
                                currentServer.onMessage(this, message);
                            }
                            @Override
                            public void onClose(int code, String reason, boolean remote) {}
                            @Override
                            public void onError(Exception ex) {}
                        };
                        client.connect();
                    } catch (Exception e) {}
                }
            }
        }
        AutoWalletSystem.autoStartValidation();
    }
}