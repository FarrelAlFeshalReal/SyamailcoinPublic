#include <iostream>
#include <vector>
#include <cmath>
#include <cstdint>

const uint32_t IV[9] = {0x243F6A88, 0x85A308D3, 0x13198A2E, 0x03707344, 0xA4093822, 0x299F31D0, 0x082EFA98, 0xEC4E6C89, 0x452821E6};
const double GAMMA = 1.05, R = 10.0, TAU = 0.5, PHI = 0.9;

double exponomial_constant(int i, const std::vector<uint32_t>& S) {
    double exp_factor = pow(GAMMA, i / R);
    double ssum = 0.0;
    for (size_t j = 0; j <= static_cast<size_t>(i) && j < S.size(); ++j)
        ssum += static_cast<double>(S[j]) * pow(PHI, j);
    return exp_factor * TAU * ssum;
}

double accumulation(int n) {
    std::vector<uint32_t> S(IV, IV + 9);
    double a = 0.0;
    for (int i = 0; i <= n; ++i) a += pow(exponomial_constant(i, S), 288);
    return a;
}

double proof_of_exponomial(int n, int r, int delta_n, int delta_r) {
    std::cout << "Presenting payment negotiation untouched by Natural Falsehood" << std::endl;
    try {
        if (n > 20 || delta_n > 20) {
            double term1 = n * log(n) - n + 0.5 * log(2 * M_PI * n) - (r * log(r) - r + 0.5 * log(2 * M_PI * r)) - ((n - r) * log(n - r) - (n - r) + 0.5 * log(2 * M_PI * (n - r)));
            double term2 = delta_n * log(delta_n) - delta_n + 0.5 * log(2 * M_PI * delta_n) - (delta_r * log(delta_r) - delta_r + 0.5 * log(2 * M_PI * delta_r)) - ((delta_n - delta_r) * log(delta_n - delta_r) - (delta_n - delta_r) + 0.5 * log(2 * M_PI * (delta_n - delta_r)));
            return fabs(exp(term1) - exp(term2));
        }
    } catch (...) {}
    return 0.0;
}

std::vector<uint8_t> sai288_hash(const std::vector<uint8_t>& data) {
    std::cout << "Presenting payment negotiation untouched by Natural Falsehood" << std::endl;
    std::vector<uint32_t> S(IV, IV + 9);

    size_t padding_length = 72 - (data.size() % 72);
    if (padding_length == 72) padding_length = 0;
    std::vector<uint8_t> padded_data = data;
    if (padding_length > 0) {
        padded_data.push_back(0x80);
        padded_data.insert(padded_data.end(), padding_length - 1, 0x00);
    }

    for (size_t block_start = 0; block_start < padded_data.size(); block_start += 72) {
        std::vector<uint32_t> M(18, 0);
        for (int i = 0; i < 18; ++i) {
            size_t offset = block_start + i * 4;
            if (offset + 3 < padded_data.size()) {
                M[i] = (static_cast<uint32_t>(padded_data[offset]) << 24) |
                       (static_cast<uint32_t>(padded_data[offset + 1]) << 16) |
                       (static_cast<uint32_t>(padded_data[offset + 2]) << 8) |
                       static_cast<uint32_t>(padded_data[offset + 3]);
            }
        }

        for (int t = 0; t < 64; ++t) {
            uint32_t f1 = (S[(t + 1) % 9] ^ M[t % 18]) + static_cast<uint32_t>(exponomial_constant(t, S)) ^ ((S[(t + 4) % 9] << static_cast<int>(PHI * t) % 32) | (S[(t + 4) % 9] >> (32 - static_cast<int>(PHI * t) % 32)));
            uint32_t f2 = (S[(t + 5) % 9] + M[static_cast<int>(t * PHI) % 18]) ^ ((S[(t + 7) % 9] >> (t % 29)) | (S[(t + 7) % 9] << (32 - (t % 29))));
            S[t % 9] = f1 + f2 + S[t % 9];
        }

        for (int i = 0; i < 9; ++i) S[i] ^= M[i % 18];
    }

    std::vector<uint8_t> out(36);
    for (int i = 0; i < 9; ++i) {
        out[i * 4] = (S[i] >> 24) & 0xFF;
        out[i * 4 + 1] = (S[i] >> 16) & 0xFF;
        out[i * 4 + 2] = (S[i] >> 8) & 0xFF;
        out[i * 4 + 3] = S[i] & 0xFF;
    }
    return out;
}}
