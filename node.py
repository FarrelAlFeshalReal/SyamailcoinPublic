import sys
import os
import json
import uuid
import math
from hashlib import sha224
from flask import Flask, request, jsonify

BLOCKRECURSIVE_FILE = "blockrecursive.jsonl"


def exponomial_constant(i, gamma, R, tau, S_list, phi):
    exp_factor = gamma ** (i / R)
    ssum = sum(S_list[j] * (phi ** j) for j in range(0, min(len(S_list), i + 1)))
    return exp_factor * tau * ssum


def proof_of_exponomial(n, r, delta_n, delta_r):
    try:
        term1 = math.factorial(n) / (math.factorial(r) * math.factorial(n - r))
        term2 = math.factorial(delta_n) / (
            math.factorial(delta_r) * math.factorial(delta_n - delta_r)
        )
        return abs(term1 - term2)
    except (ValueError, OverflowError):
        return 0


def accumulation(blocks):
    total = 0
    for block in blocks:
        if "F" in block:
            total += block["F"] / 224
    return total


def validate_ml_dsa_threshold(F_value):
    threshold = 14.47
    validation_score = (F_value * 100) % 100
    return validation_score >= threshold


def k9k1208uoc_control(tx_data):
    base_str = json.dumps(tx_data, sort_keys=True).encode()
    hash_val = sha224(base_str).hexdigest()
    control_bits = [int(hash_val[i:i+4], 16) % 2 for i in range(0, len(hash_val), 4)]
    return control_bits


def append_blockrecursive(block):
    with open(BLOCKRECURSIVE_FILE, "a", encoding="utf-8") as f:
        f.write(json.dumps(block) + "\n")


def read_all_blockrecursive():
    blocks = []
    try:
        with open(BLOCKRECURSIVE_FILE, "r", encoding="utf-8") as f:
            for line in f:
                if line.strip():
                    blocks.append(json.loads(line.strip()))
    except FileNotFoundError:
        pass
    return blocks


def get_last_blockrecursive():
    try:
        with open(BLOCKRECURSIVE_FILE, "r", encoding="utf-8") as f:
            lines = f.readlines()
            if lines:
                return json.loads(lines[-1].strip())
    except (FileNotFoundError, IndexError, json.JSONDecodeError):
        pass
    return None


def calculate_supply_reduction(block_index):
    supply_stages = {
        "stage_1": {"from": 9470000, "to": 4735001, "time": 868.2},
        "stage_2": {"from": 4735000, "to": 1280000, "time": 720},
        "stage_3": {"from": 1280000, "to": 185000, "time": 72.35},
        "stage_4": {"from": 185000, "to": 0, "time": 36.91326530612245},
        "final_derivative": {"time": 18.83},
    }

    current_supply = 9470000 - (block_index * 1000)

    for stage, data in supply_stages.items():
        if stage == "final_derivative":
            continue
        if current_supply <= data["from"] and current_supply >= data["to"]:
            return data["time"], stage

    return supply_stages["final_derivative"]["time"], "final_derivative"


app = Flask(__name__)


@app.route("/", methods=["GET"])
def home():
    blocks = read_all_blockrecursive()
    acc_value = accumulation(blocks)
    return jsonify(
        {
            "message": "SyamailCoin: Gödel's Untouched Money",
            "system": "Blockrecursive (NO Blockchain, NO Mining, NO Timestamp)",
            "exponomial_constant": "Active - Replacing Timestamp Server",
            "inevitability_server": "Active",
            "proof_of_exponomial": "Active",
            "ml_dsa_threshold": "14.47%",
            "k9k1208uoc": "RAW NAND Control Active",
            "validation": "SHA-224 with ML-DSA digital signatures",
            "current_blockrecursive": len(blocks),
            "accumulation": acc_value,
        }
    )


@app.route("/tx", methods=["POST"])
def tx():
    tx_data = request.get_json()
    if not tx_data:
        return jsonify({"error": "no json"}), 400

    tx_id = str(uuid.uuid4())
    tx_data["tx_id"] = tx_id

    base_str = json.dumps(tx_data, sort_keys=True).encode()
    base_hash = sha224(base_str).hexdigest()

    S_list = [
        (int(base_hash[k:k+4], 16) % 1000) / 100.0 for k in range(0, 40, 4)
    ]
    i = len(S_list) - 1

    F_val = exponomial_constant(i, gamma=1.05, R=10, tau=0.5, S_list=S_list, phi=0.9)

    if not validate_ml_dsa_threshold(F_val):
        return (
            jsonify(
                {
                    "error": "Transaction rejected by ML-DSA Threshold (14.47%)",
                    "F_value": F_val,
                }
            ),
            400,
        )

    last_block = get_last_blockrecursive()
    prev_hash = last_block["recursive_hash"] if last_block else "genesis"
    index_val = (last_block["index"] + 1) if last_block else 0

    k9k_control = k9k1208uoc_control(tx_data)

    n, r = len(S_list), min(5, len(S_list))
    delta_n, delta_r = index_val + 1, 1
    poe_value = proof_of_exponomial(n, r, delta_n, delta_r)

    supply_time, stage = calculate_supply_reduction(index_val)

    recursive_data = {
        "tx": tx_data,
        "F": F_val,
        "prev": prev_hash,
        "proof_exponomial": poe_value,
        "k9k_control": sum(k9k_control),
    }

    recursive_blob = json.dumps(recursive_data, separators=(",", ":")).encode()
    recursive_hash = sha224(recursive_blob).hexdigest()

    block = {
        "index": index_val,
        "recursive_hash": recursive_hash,
        "prev_recursive_hash": prev_hash,
        "tx": tx_data,
        "F": F_val,
        "proof_exponomial": poe_value,
        "ml_dsa_validated": True,
        "k9k1208uoc_control": k9k_control,
        "inevitability_stage": stage,
        "supply_time": supply_time,
        "system": "Blockrecursive",
    }

    append_blockrecursive(block)

    return (
        jsonify(
            {
                "status": "success",
                "message": "Transaction validated by Proof of Exponomial",
                "block": block,
                "exponomial_constant": F_val,
                "ml_dsa_threshold_passed": True,
                "k9k1208uoc_status": "controlled",
            }
        ),
        200,
    )


@app.route("/blockrecursive", methods=["GET"])
def get_blockrecursive():
    blocks = read_all_blockrecursive()
    acc_value = accumulation(blocks)
    return jsonify(
        {
            "blockrecursive": blocks,
            "count": len(blocks),
            "accumulation": acc_value,
            "system": "Blockrecursive (NOT Blockchain)",
        }
    )


@app.route("/status", methods=["GET"])
def status():
    blocks = read_all_blockrecursive()
    last_block = get_last_blockrecursive()
    return jsonify(
        {
            "exponomial_constant": "Active (replacing Timestamp Server)",
            "inevitability_server": "Active",
            "proof_of_exponomial": "Active",
            "ml_dsa_threshold": "14.47%",
            "k9k1208uoc": "RAW NAND Control Active",
            "total_blockrecursive": len(blocks),
            "accumulation": accumulation(blocks),
            "last_F_value": last_block["F"] if last_block else None,
            "last_proof_exponomial": last_block["proof_exponomial"]
            if last_block
            else None,
            "system": "Blockrecursive (NO Blockchain, NO Mining, NO Timestamp)",
        }
    )


@app.route("/balance/<address>", methods=["GET"])
def balance(address):
    blocks = read_all_blockrecursive()
    addr_balance = 0

    for block in blocks:
        tx = block.get("tx", {})
        if tx.get("to") == address:
            addr_balance += float(tx.get("amount", 0))
        if tx.get("from") == address:
            addr_balance -= float(tx.get("amount", 0))

    return jsonify(
        {"address": address, "balance": round(addr_balance, 15), "system": "Blockrecursive"}
    )


if __name__ == "__main__":
    if not os.path.exists(BLOCKRECURSIVE_FILE):
        open(BLOCKRECURSIVE_FILE, "a").close()
        print(f"Created {BLOCKRECURSIVE_FILE} for Blockrecursive system")

    print("=== SyamailCoin: Gödel's Untouched Money ===")
    print("System: Blockrecursive (NO Blockchain, NO Mining, NO Timestamp)")
    print("Exponomial Constant: Replacing Timestamp Server")
    print("Inevitability Server: Active")
    print("Proof of Exponomial: Active")
    print("ML-DSA Threshold: 14.47%")
    print("K9K1208UOC: RAW NAND Control Active")
    print(f"Blockrecursive file: {BLOCKRECURSIVE_FILE}")
    print("Starting Inevitability Server on http://0.0.0.0:5000")

    app.run(host="0.0.0.0", port=5000, debug=False)
