import sys
import random
import json

network_path = sys.argv[1]
num_steps = int(sys.argv[2])
num_events_per_step = int(sys.argv[3])
end_time = int(sys.argv[4])

output_path = sys.argv[5] if len(sys.argv) > 5 else "timeline.csv"

categories = ['A', 'B', 'C', 'D', 'E']

timesteps = sorted([round(random.uniform(0, end_time),4) for _ in range(num_steps)])

with open(network_path, 'r') as f:
    network = json.load(f)

events = []

for timestep in timesteps:
    for _ in range(num_events_per_step): # generate state of network at each timestep
        edge = random.choice(network['edges'])
        event = {
            'from': edge['from'],
            'to': edge['to'],	
            'mode': edge['mode'],
            'time': timestep,
            'category': random.choice(categories),
            'quantity': random.randint(1, 100),
        }
        events.append(event)

with open(output_path, 'w') as f:
    f.write("time,from,to,mode,category,quantity\n")
    for event in events:
        f.write(f"{event['time']},{event['from']},{event['to']},{event['mode']},{event['category']},{event['quantity']}\n")