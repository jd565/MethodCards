# Usage: python method_parser.py
# Description: This script reads methods from the methods.csv file, and encodes them into a protobuf format to be
#              used in the Compose app. The methods are split into 6 files, each containing a subset of the methods.
# The protobuf definitions can be created by running
# `protoc --proto_path=composeApp/src/commonMain/proto/com/jpd/methodcards --python_out=method_parser composeApp/src/commonMain/proto/com/jpd/methodcards/method.proto`
import csv
import math
import method_pb2
import json

methods = method_pb2.MethodsProto()

with open("method_parser/methods.csv", "r", newline='') as file:
    reader = csv.DictReader(file)
    for row in reader:
        if row["calls"] == "[]":
            row["calls"] = "{}"
        if (int(row["stage"]) >= 4 and int(row["stage"]) <= 16):
            m = methods.methods.add()
            m.name = row["title"]
            m.notation = row["notation"]
            m.stage = int(row["stage"])
            m.magic = int(row["magic"])
            m.classification = row["classification"]
            calls = json.loads(row["calls"])
            for key, value in calls.items():
                m.calls[key].symbol = value["symbol"]
                m.calls[key].notation = value["notation"]
                m.calls[key].from_ = value["from"]
                m.calls[key].every = value["every"]
                m.calls[key].cover = value["cover"]
            ruleoffs = json.loads(row["ruleoffs"])
            m.ruleoffsEvery = int(ruleoffs["every"])
            m.ruleoffsFrom = int(ruleoffs["from"])

methods.methods.sort(key = lambda x: x.magic)

with open(f"composeApp/src/commonMain/composeResources/files/methods.pb", "wb") as file:
    file.write(methods.SerializeToString())
