# About

# Workflow
The application's internal workflow is as follows:
1. Parse OSM, network specification, and timeline files
2. Generate routes
3. Simplify routes
# Network specification
The program expects a JSON file structured as an object with two keys: `vertices` and `edges`.

`vertices`:
 A list of vertex objects. Each vertex represents a location and must have the following attributes:

* `name` (string): Unique name of the vertex.

* `lon` (float): Longitude coordinate.

* `lat` (float): Latitude coordinate.

`edges`: A list of edge objects. Each edge defines a connection between two vertices using a specific mode of transport and must have the following attributes:

* `from` (string): Name of the starting vertex (must match a name in vertices).

* `to` (string): Name of the destination vertex (must match a name in vertices).

* `mode` (string): Mode of transportation. Must be one of: "truck", "train", or "barge".
