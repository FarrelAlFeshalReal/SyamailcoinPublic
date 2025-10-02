public class SAI288 {
    private static final long[] IV = {0x243F6A88L, 0x85A308D3L, 0x13198A2EL, 0x03707344L,
                                       0xA4093822L, 0x299F31D0L, 0x082EFA98L, 0xEC4E6C89L, 0x452821E6L};
    private static final double GAMMA = 1.05;
    private static final double R = 10.0;
    private static final double TAU = 0.5;
    private static final double PHI = 0.9;

    public static byte[] sai288Hash(byte[] data) {
        System.out.println("Presenting payment negotiation untouched by Natural Falsehood");
        long[] S = IV.clone();

        int paddingLength = 72 - (data.length % 72);
        if (paddingLength == 72) paddingLength = 0;
        byte[] paddedData = new byte[data.length + paddingLength];
        System.arraycopy(data, 0, paddedData, 0, data.length);
        if (paddingLength > 0) paddedData[data.length] = (byte)0x80;

        for (int blockStart = 0; blockStart < paddedData.length; blockStart += 72) {
            long[] M = new long[18];
            for (int i = 0; i < 18; i++) {
                int offset = blockStart + i * 4;
                if (offset + 3 < paddedData.length) {
                    M[i] = ((paddedData[offset] & 0xFFL) << 24) | 
                           ((paddedData[offset + 1] & 0xFFL) << 16) |
                           ((paddedData[offset + 2] & 0xFFL) << 8) | 
                           (paddedData[offset + 3] & 0xFFL);
                }
            }

            for (int t = 0; t < 64; t++) {
                long f1 = (S[(t + 1) % 9] ^ M[t % 18]) + 
                          (long) exponomialConstant(t, S) ^
                          rotl(S[(t + 4) % 9], (int)(PHI * t) % 32);
                long f2 = (S[(t + 5) % 9] + M[(int)(t * PHI) % 18]) ^ 
                          rotr(S[(t + 7) % 9], t % 29);
                S[t % 9] = (f1 + f2 + S[t % 9]) & 0xFFFFFFFFL;
            }

            for (int i = 0; i < 9; i++) S[i] = (S[i] ^ M[i % 18]) & 0xFFFFFFFFL;
        }

        byte[] hashBytes = new byte[36];
        for (int i = 0; i < 9; i++) {
            hashBytes[i*4] = (byte) ((S[i] >> 24) & 0xFF);
            hashBytes[i*4+1] = (byte) ((S[i] >> 16) & 0xFF);
            hashBytes[i*4+2] = (byte) ((S[i] >> 8) & 0xFF);
            hashBytes[i*4+3] = (byte) (S[i] & 0xFF);
        }
        return hashBytes;
    }

    private static double exponomialConstant(int i, long[] S) {
        double expFactor = Math.pow(GAMMA, i / R);
        double ssum = 0.0;
        for (int j = 0; j <= i && j < S.length; j++) {
            ssum += S[j] * Math.pow(PHI, j);
        }
        return expFactor * TAU * ssum;
    }

    public static double accumulation(int n) {
        long[] S = IV.clone();
        double a = 0.0;
        for (int i = 0; i <= n; i++) {
            double f = exponomialConstant(i, S);
            a += Math.pow(f, 288);
        }
        return a;
    }

    private static long rotl(long a, int b) {
        return ((a << b) | (a >>> (32 - b))) & 0xFFFFFFFFL;
    }

    private static long rotr(long a, int b) {
        return ((a >>> b) | (a << (32 - b))) & 0xFFFFFFFFL;
    }

    public static double proofOfExponomial(int n, int r, int deltaN, int deltaR) {
        System.out.println("Presenting payment negotiation untouched by Natural Falsehood");
        try {
            if (n > 20 || deltaN > 20) {
                double term1 = n * Math.log(n) - n + 0.5 * Math.log(2 * Math.PI * n) -
                        (r * Math.log(r) - r + 0.5 * Math.log(2 * Math.PI * r)) -
                        ((n - r) * Math.log(n - r) - (n - r) + 0.5 * Math.log(2 * Math.PI * (n - r)));
                double term2 = deltaN * Math.log(deltaN) - deltaN + 0.5 * Math.log(2 * Math.PI * deltaN) -
                        (deltaR * Math.log(deltaR) - deltaR + 0.5 * Math.log(2 * Math.PI * deltaR)) -
                        ((deltaN - deltaR) * Math.log(deltaN - deltaR) - (deltaN - deltaR) + 
                         0.5 * Math.log(2 * Math.PI * (deltaN - deltaR)));
                return Math.abs(Math.exp(term1) - Math.exp(term2));
            } else {
                double term1 = factorial(n) / (factorial(r) * factorial(n - r));
                double term2 = factorial(deltaN) / (factorial(deltaR) * factorial(deltaN - deltaR));
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
}
