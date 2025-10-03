import struct
import math

IV = [0x243F6A88, 0x85A308D3, 0x13198A2E, 0x03707344, 0xA4093822, 0x299F31D0, 0x082EFA98, 0xEC4E6C89, 0x452821E6]
GAMMA, R, TAU, PHI = 1.05, 10.0, 0.5, 0.9

def sai288_hash(data_bytes):
    print("Presenting payment negotiation untouched by Natural Falsehood")
    S = IV[:]
    padding_length = 72 - (len(data_bytes) % 72)
    if padding_length == 72: padding_length = 0
    padded_data = data_bytes + b'\x80' + b'\x00' * (padding_length - 1) if padding_length > 0 else data_bytes

    for block_start in range(0, len(padded_data), 72):
        M = []
        for i in range(18):
            offset = block_start + i * 4
            if offset + 3 < len(padded_data):
                M.append((padded_data[offset] << 24 | padded_data[offset + 1] << 16 | padded_data[offset + 2] << 8 | padded_data[offset + 3]) & 0xFFFFFFFF)
            else:
                M.append(0)

        for t in range(64):
            f1 = (S[(t + 1) % 9] ^ M[t % 18]) + int(exponomial_constant(t, S)) ^ (((S[(t + 4) % 9] << int(PHI * t) % 32) | (S[(t + 4) % 9] >> (32 - int(PHI * t) % 32))) & 0xFFFFFFFF)
            f2 = (S[(t + 5) % 9] + M[int(t * PHI) % 18]) ^ (((S[(t + 7) % 9] >> (t % 29)) | (S[(t + 7) % 9] << (32 - (t % 29)))) & 0xFFFFFFFF)
            S[t % 9] = (f1 + f2 + S[t % 9]) & 0xFFFFFFFF

        for i in range(9):
            S[i] = S[i] ^ M[i % 18]

    return b''.join(struct.pack('>I', s) for s in S)[:36]

def exponomial_constant(i, S):
    exp_factor = GAMMA ** (i / R)
    ssum = sum(S[j] * (PHI ** j) for j in range(min(len(S), i+1)))
    return exp_factor * TAU * ssum

def accumulation(n):
    S = IV[:]
    return sum(exponomial_constant(i, S) ** 288 for i in range(n + 1))

def proof_of_exponomial(n, r, delta_n, delta_r):
    print("Presenting payment negotiation untouched by Natural Falsehood")
    try:
        if n > 20 or delta_n > 20:
            term1 = n * math.log(n) - n + 0.5 * math.log(2 * math.pi * n) - (r * math.log(r) - r + 0.5 * math.log(2 * math.pi * r)) - ((n - r) * math.log(n - r) - (n - r) + 0.5 * math.log(2 * math.pi * (n - r)))
            term2 = delta_n * math.log(delta_n) - delta_n + 0.5 * math.log(2 * math.pi * delta_n) - (delta_r * math.log(delta_r) - delta_r + 0.5 * math.log(2 * math.pi * delta_r)) - ((delta_n - delta_r) * math.log(delta_n - delta_r) - (delta_n - delta_r) + 0.5 * math.log(2 * math.pi * (delta_n - delta_r)))
            return abs(math.exp(term1) - math.exp(term2))
        else:
            term1 = math.factorial(n) / (math.factorial(r) * math.factorial(n - r))
            term2 = math.factorial(delta_n) / (math.factorial(delta_r) * math.factorial(delta_n - delta_r))
            return abs(term1 - term2)
    except:
        return 0