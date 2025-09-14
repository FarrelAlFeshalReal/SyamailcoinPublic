import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import com.google.gson.*;

public class Node {
    private static final int PORT = 5000;
    private static final String BLOCK_FILE = "blockrecursive.jsonl";
    private static final Gson gson = new Gson();

    private static String sha224(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-224");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void appendBlock(Map<String, Object> block) {
        try (FileWriter fw = new FileWriter(BLOCK_FILE, true)) {
            fw.write(gson.toJson(block) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Map<String, Object>> readBlocks() {
        List<Map<String, Object>> blocks = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(BLOCK_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                blocks.add(gson.fromJson(line, Map.class));
            }
        } catch (IOException e) {
            // file belum ada
        }
        return blocks;
    }

    private static Map<String, Object> lastBlock() {
        List<Map<String, Object>> blocks = readBlocks();
        if (blocks.isEmpty()) return null;
        return blocks.get(blocks.size() - 1);
    }

    private static double getBalance(String address) {
        double balance = 0;
        for (Map<String, Object> block : readBlocks()) {
            Map tx = (Map) block.get("tx");
            if (tx != null) {
                if (address.equals(tx.get("to"))) {
                    balance += Double.parseDouble(tx.get("amount").toString());
                }
                if (address.equals(tx.get("from"))) {
                    balance -= Double.parseDouble(tx.get("amount").toString());
                }
            }
        }
        return balance;
    }

    private static void sendResponse(PrintWriter out, String body) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + body.length());
        out.println();
        out.println(body);
    }

    private static void send404(PrintWriter out) {
        String body = "{\"error\": \"Not Found\"}";
        out.println("HTTP/1.1 404 Not Found");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + body.length());
        out.println();
        out.println(body);
    }

    public static void main(String[] args) {
        System.out.println("=== Syamailcoin Node (Java Pure) ===");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    String requestLine = in.readLine();
                    if (requestLine == null) continue;

                    String[] parts = requestLine.split(" ");
                    if (parts.length < 2) continue;
                    String method = parts[0];
                    String path = parts[1];

                    String line;
                    int contentLength = 0;
                    while (!(line = in.readLine()).isEmpty()) {
                        if (line.toLowerCase().startsWith("content-length:")) {
                            contentLength = Integer.parseInt(line.split(":")[1].trim());
                        }
                    }

                    char[] bodyChars = new char[contentLength];
                    if (contentLength > 0) {
                        in.read(bodyChars);
                    }
                    String bodyData = new String(bodyChars);

                    if (method.equals("GET") && path.equals("/")) {
                        String body = gson.toJson(Map.of(
                                "message", "SyamailCoin: Gödel's Untouched Money",
                                "system", "Blockrecursive (NO Blockchain, NO Mining, NO Timestamp)",
                                "status", "running"
                        ));
                        sendResponse(out, body);

                    } else if (method.equals("GET") && path.equals("/status")) {
                        List<Map<String, Object>> blocks = readBlocks();
                        Map<String, Object> last = lastBlock();
                        String body = gson.toJson(Map.of(
                                "total_blocks", blocks.size(),
                                "last_block", last
                        ));
                        sendResponse(out, body);

                    } else if (method.equals("POST") && path.equals("/tx")) {
                        Map<String, Object> tx = gson.fromJson(bodyData, Map.class);
                        String txHash = sha224(bodyData);

                        Map<String, Object> block = new LinkedHashMap<>();
                        block.put("index", readBlocks().size());
                        block.put("tx", tx);
                        block.put("hash", txHash);
                        block.put("prev_hash", lastBlock() == null ? "genesis" : lastBlock().get("hash"));

                        appendBlock(block);

                        String body = gson.toJson(Map.of(
                                "status", "success",
                                "block", block
                        ));
                        sendResponse(out, body);

                    } else if (method.equals("GET") && path.startsWith("/balance/")) {
                        String addr = path.replace("/balance/", "");
                        double balance = getBalance(addr);
                        String body = gson.toJson(Map.of(
                                "address", addr,
                                "balance", balance
                        ));
                        sendResponse(out, body);

                    } else if (method.equals("GET") && path.equals("/blockrecursive")) {
                        List<Map<String, Object>> blocks = readBlocks();
                        String body = gson.toJson(Map.of(
                                "blockrecursive", blocks,
                                "count", blocks.size()
                        ));
                        sendResponse(out, body);

                    } else {
                        send404(out);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
