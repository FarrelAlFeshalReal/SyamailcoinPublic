import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;

public class SAI288 {

    public static byte[] sai288Hash(byte[] data) {
        long[] S = new long[]{0x243F6A88L, 0x85A308D3L, 0x13198A2EL, 0x03707344L,
                0xA4093822L, 0x299F31D0L, 0x082EFA98L, 0xEC4E6C89L, 0x452821E6L};
        double gamma = 1.05;
        double R = 10.0;
        double tau = 0.5;
        double phi = 0.9;

        int padding_length = 72 - (data.length % 72);
        if (padding_length == 72) padding_length = 0;
        byte[] padded_data = new byte[data.length + padding_length];
        System.arraycopy(data, 0, padded_data, 0, data.length);
        if (padding_length > 0) {
            padded_data[data.length] = (byte) 0x80;
        }

        for (int block_start = 0; block_start < padded_data.length; block_start += 72) {
            long[] M = new long[18];
            for (int i = 0; i < 18; i++) {
                int offset = block_start + i * 4;
                if (offset + 3 < padded_data.length) {
                    M[i] = ((padded_data[offset] & 0xFFL) << 24) |
                           ((padded_data[offset + 1] & 0xFFL) << 16) |
                           ((padded_data[offset + 2] & 0xFFL) << 8) |
                           (padded_data[offset + 3] & 0xFFL);
                }
            }

            for (int t = 0; t < 64; t++) {
                long f1 = (S[(t + 1) % 9] ^ M[t % 18]) +
                          (long) exponomialConstant(t, gamma, R, tau, S, phi) ^
                          rotl(S[(t + 4) % 9], (int)(phi * t) % 32);
                long f2 = (S[(t + 5) % 9] + M[(int)(t * phi) % 18]) ^
                          rotr(S[(t + 7) % 9], t % 29);
                long T = S[t % 9];
                S[t % 9] = (f1 + f2 + T) & 0xFFFFFFFFL;
            }

            for (int i = 0; i < 9; i++) {
                S[i] = (S[i] ^ M[i % 18]) & 0xFFFFFFFFL;
            }
        }

        byte[] hash_bytes = new byte[36];
        for (int i = 0; i < 9; i++) {
            hash_bytes[i*4] = (byte) ((S[i] >> 24) & 0xFF);
            hash_bytes[i*4+1] = (byte) ((S[i] >> 16) & 0xFF);
            hash_bytes[i*4+2] = (byte) ((S[i] >> 8) & 0xFF);
            hash_bytes[i*4+3] = (byte) (S[i] & 0xFF);
        }
        return hash_bytes;
    }

    private static double exponomialConstant(int i, double gamma, double R, double tau, long[] S, double phi) {
        double exp_factor = Math.pow(gamma, i / R);
        double ssum = 0.0;
        for (int j = 0; j <= i && j < S.length; j++) {
            ssum += S[j] * Math.pow(phi, j);
        }
        return exp_factor * tau * ssum;
    }

    private static long rotl(long a, int b) {
        return ((a << b) | (a >>> (32 - b))) & 0xFFFFFFFFL;
    }

    private static long rotr(long a, int b) {
        return ((a >>> b) | (a << (32 - b))) & 0xFFFFFFFFL;
    }

    public static double proofOfExponomial(int n, int r, int delta_n, int delta_r) {
        try {
            if (n > 20 || delta_n > 20) {
                double term1 = n * Math.log(n) - n + 0.5 * Math.log(2 * Math.PI * n) -
                        (r * Math.log(r) - r + 0.5 * Math.log(2 * Math.PI * r)) -
                        ((n - r) * Math.log(n - r) - (n - r) + 0.5 * Math.log(2 * Math.PI * (n - r)));
                double term2 = delta_n * Math.log(delta_n) - delta_n + 0.5 * Math.log(2 * Math.PI * delta_n) -
                        (delta_r * Math.log(delta_r) - delta_r + 0.5 * Math.log(2 * Math.PI * delta_r)) -
                        ((delta_n - delta_r) * Math.log(delta_n - delta_r) - (delta_n - delta_r) + 0.5 * Math.log(2 * Math.PI * (delta_n - delta_r)));
                return Math.abs(Math.exp(term1) - Math.exp(term2));
            } else {
                double term1 = factorial(n) / (factorial(r) * factorial(n - r));
                double term2 = factorial(delta_n) / (factorial(delta_r) * factorial(delta_n - delta_r));
                return Math.abs(term1 - term2);
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static double factorial(int n) {
        double res = 1.0;
        for (int i = 2; i <= n; i++) res *= i;
        return res;
    }

    
    public static void main(String[] args) {
        String test = "Hello SAI288!";
        byte[] hash = sai288Hash(test.getBytes());
        System.out.print("Hash: ");
        for (byte b : hash) {
            System.out.printf("%02X", b);
        }
        System.out.println();

        
        double delta = proofOfExponomial(5, 2, 3, 1);
        System.out.println("Proof of Exponomial result: " + delta);

        
        int port = 8887;
        WebSocketServer server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                System.out.println("New connection from " + conn.getRemoteSocketAddress());
                conn.send("Welcome to SAI288 WebSocket Server!");
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                System.out.println("Closed connection: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                System.out.println("Received: " + message);
                byte[] hashed = sai288Hash(message.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : hashed) sb.append(String.format("%02X", b));
                conn.send("Hashed: " + sb.toString());
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                ex.printStackTrace();
            }

            @Override
            public void onStart() {
                System.out.println("WebSocket server started on port " + port);
            }
        };
        server.start();
    }
}