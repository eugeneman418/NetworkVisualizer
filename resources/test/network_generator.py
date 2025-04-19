import sys
import json
import csv

node_path = sys.argv[1] # arg 0 is name of the script
output_path = sys.argv[2] if len(sys.argv) > 2 else "network.json"

modes = ["truck", "train", "barge"]

network = {'nodes': [], 'edges': []}

with open(node_path, 'r') as node_file:
    csv_reader = csv.reader(node_file)
    header = next(csv_reader)
    for row in csv_reader:
        node = {
            'name': row[0],
            'lon': row[1],
            'lat': row[2],	
        }
        network['nodes'].append(node)
    
    for node1 in network['nodes']:
        for node2 in network['nodes']:
            if node1['name'] == node2['name']:
                continue
            for mode in modes:
                edge = {
                    'from': node1['name'],
                    'to': node2['name'],
                    'mode': mode,
                }
                network['edges'].append(edge)

# Save the network to a JSON file
with open(output_path, 'w') as output_file:
    json.dump(network, output_file)
