import struct
import math
import time

def sai288_hash(data_bytes):
    SAI288_IV = [0x243F6A88, 0x85A308D3, 0x13198A2E, 0x03707344,
                 0xA4093822, 0x299F31D0, 0x082EFA98, 0xEC4E6C89, 0x452821E6]
    gamma = 1.05
    R = 10.0
    tau = 0.5
    phi = 0.9

    padding_length = 72 - (len(data_bytes) % 72)
    if padding_length == 72:
        padding_length = 0
    padded_data = data_bytes + b'\x80' + b'\x00' * (padding_length - 1) if padding_length > 0 else data_bytes

    S = SAI288_IV[:]

    for block_start in range(0, len(padded_data), 72):
        M = []
        for i in range(18):
            offset = block_start + i * 4
            m = (padded_data[offset] << 24) | (padded_data[offset + 1] << 16) | (padded_data[offset + 2] << 8) | padded_data[offset + 3]
            M.append(m & 0xFFFFFFFF)

        for t in range(64):
            f1 = (S[(t + 1) % 9] ^ M[t % 18]) + int(exponomial_constant(t, gamma, R, tau, S, phi)) ^ (((S[(t + 4) % 9] << int(phi * t) % 32) | (S[(t + 4) % 9] >> (32 - int(phi * t) % 32))) & 0xFFFFFFFF)
            f2 = (S[(t + 5) % 9] + M[int(t * phi) % 18]) ^ (((S[(t + 7) % 9] >> (t % 29)) | (S[(t + 7) % 9] << (32 - (t % 29)))) & 0xFFFFFFFF)
            T = S[t % 9]
            S[t % 9] = (f1 + f2 + T) & 0xFFFFFFFF

        for i in range(9):
            S[i] = S[i] ^ M[i % 18]

    hash_bytes = b''
    for s in S:
        hash_bytes += struct.pack('>I', s)
    return hash_bytes[:36]

def exponomial_constant(i, gamma, R, tau, S, phi):
    exp_factor = gamma ** (i / R)
    ssum = sum(S[j] * (phi ** j) for j in range(min(len(S), i+1)))
    return exp_factor * tau * ssum

def proof_of_exponomial(n, r, delta_n, delta_r):
    print("Calculating Delta Maths for Proof of Exponomial...")
    print(f"Parameters: n={n}, r={r}, delta_n={delta_n}, delta_r={delta_r}")
    try:
        if n > 20 or delta_n > 20:
            term1 = n * math.log(n) - n + 0.5 * math.log(2 * math.pi * n) - \
                    (r * math.log(r) - r + 0.5 * math.log(2 * math.pi * r)) - \
                    ((n - r) * math.log(n - r) - (n - r) + 0.5 * math.log(2 * math.pi * (n - r)))
            term2 = delta_n * math.log(delta_n) - delta_n + 0.5 * math.log(2 * math.pi * delta_n) - \
                    (delta_r * math.log(delta_r) - delta_r + 0.5 * math.log(2 * math.pi * delta_r)) - \
                    ((delta_n - delta_r) * math.log(delta_n - delta_r) - (delta_n - delta_r) + 0.5 * math.log(2 * math.pi * (delta_n - delta_r)))
            return abs(math.exp(term1) - math.exp(term2))
        else:
            term1 = math.factorial(n) / (math.factorial(r) * math.factorial(n - r))
            term2 = math.factorial(delta_n) / (math.factorial(delta_r) * math.factorial(delta_n - delta_r))
            return abs(term1 - term2)
    except Exception as e:
        print("Delta Maths error:", str(e))
        return 0

def generate_sai288_hash(block_index, reward, nonce):
    raw_bytes = struct.pack('>QdQ', block_index, float(reward), nonce)
    return sai288_hash(raw_bytes).hex()

if __name__ == '__main__':
    block_index = 0
    reward = 0.0002231668235294118
    nonce = int(time.time() * 1000)
    hash_value = generate_sai288_hash(block_index, reward, nonce)
    print("SAI288 Hash:", hash_value)
    proof = proof_of_exponomial(25, 5, 20, 3)
    print("Proof of Exponomial:", proof)
