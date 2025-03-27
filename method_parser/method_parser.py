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
classifications = set()

with open("method_parser/methods.csv", "r", newline='') as file:
    reader = csv.DictReader(file)
    for row in reader:
        if row["calls"] == "[]":
            row["calls"] = "{}"
        if (int(row["stage"]) >= 4 and int(row["stage"]) <= 16):
            m = methods.methods.add()
            stage_names = {
            4: "Minimus",
            5: "Doubles",
            6: "Minor",
            7: "Triples",
            8: "Major",
            9: "Caters",
            10: "Royal",
            11: "Cinques",
            12: "Maximus",
            13: "Sextuples",
            14: "Fourteen",
            15: "Septuples",
            16: "Sixteen",
            }

            if not row["title"].endswith(stage_names[int(row["stage"])]):
                raise ValueError(f"{row['name']} does not have correct ending for stage")

            c = row["classification"]
            if c == "Treble Place":
                m.classification = method_pb2.MethodClassificationProto.TreblePlace
            elif c == "Delight":
                m.classification = method_pb2.MethodClassificationProto.Delight
            elif c == "Bob":
                m.classification = method_pb2.MethodClassificationProto.Bob
            elif c == "Jump":
                m.classification = method_pb2.MethodClassificationProto.Jump
            elif c == "Alliance":
                m.classification = method_pb2.MethodClassificationProto.Alliance
            elif c == "Hybrid":
                m.classification = method_pb2.MethodClassificationProto.Hybrid
            elif c == "Treble Bob":
                m.classification = method_pb2.MethodClassificationProto.TrebleBob
            elif c == "Place":
                m.classification = method_pb2.MethodClassificationProto.Place
            elif c == "Surprise":
                m.classification = method_pb2.MethodClassificationProto.Surprise
            else:
                m.classification = method_pb2.MethodClassificationProto.Value('None')
            short_name = ' '.join(row["title"].split()[:-1])
            if short_name.endswith(row["classification"]):
                short_name = short_name[:-1 * (1 +len(row["classification"]))]
            else:
                m.nameHasClassification = False
            m.name = short_name
            m.notation = row["notation"]
            m.stage = int(row["stage"])
            m.magic = int(row["magic"])
            calls = json.loads(row["calls"])
            if (calls.keys() == {'Bob', 'Single'} and
                calls['Bob']['from'] == calls['Single']['from'] and calls['Bob']['from'] == 0 and
                calls['Bob']['every'] == calls['Single']['every'] and
                calls['Bob']['cover'] == calls['Single']['cover'] and calls['Bob']['cover'] == 1):

                m.standardCalls.bobNotation = calls['Bob']['notation']
                m.standardCalls.singleNotation = calls['Single']['notation']
                m.standardCalls.every = calls['Bob']['every']
            else:
                for key, value in calls.items():
                    c = m.customCalls.add()
                    c.name = key
                    c.symbol = value["symbol"]
                    c.notation = value["notation"]
                    if value["from"] != 0:
                        c.from_ = value["from"]
                    c.every = value["every"]
                    c.cover = value["cover"]
            ruleoffs = json.loads(row["ruleoffs"])
            m.ruleoffsEvery = int(ruleoffs["every"])
            if int(ruleoffs["from"]) != 0:
                m.ruleoffsFrom = int(ruleoffs["from"])

methods.methods.sort(key = lambda x: x.magic)

with open(f"composeApp/src/commonMain/composeResources/files/methods.pb", "wb") as file:
    file.write(methods.SerializeToString())
