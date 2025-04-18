# About

# Workflow
The application's internal workflow is as follows:
1. Parse OSM, network specification, and timeline files
2. Generate routes
3. Simplify routes
# Network specification
The program expects a JSON file structured as an object with two keys: `nodes` and `edges`.

`nodes`:
 A list of node objects. Each node represents a location and must have the following attributes:

* `name` (string): Unique name of the node.

* `lon` (float): Longitude coordinate.

* `lat` (float): Latitude coordinate.

`edges`: A list of edge objects. Each edge defines a connection between two nodes using a specific mode of transport and must have the following attributes:

* `from` (string): Name of the starting node (must match a name in nodes).

* `to` (string): Name of the destination node (must match a name in nodes).

* `mode` (string): Mode of transportation. Must be one of: "truck", "train", or "barge".
