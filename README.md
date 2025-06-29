# Setup
This project is developed and tested in IntelliJ, so this is the recommended IDE for running the application.  After opening this repository in IntelliJ, navigate to `src/main/java/org/networkvisualizer/example/SimpleExample.java` and run it for a demo of the application. Once it is running, open `gui/visualizer.html` to see the GUI.

_Running an instance for the first time takes a while. This is because GraphHopper must first parse the OSM file and calcualte the routes. However, subsequent runs should be faster because GraphHopper caches everything._

# Components
The application is divided into a server and a GUI. 

The server is written in Java. It has the following functionalities:
1. Parse OSM, network specification, and timeline files
2. Generate routes
3. Simplify routes
4. Respond to HTTP queries about events occurring in the network

The GUI is written in Javascript, it has the following features:
1. Display routes
2. Show traffic intensity on different parts of the routes as colours
3. Filter by transportation mode
4. Slider to see how intensity changes over time

# Class descriptions
Here is a description of the server side packages and the classes they contain
## example
### Example
Launches server for a 5 node network in Zuid-Holland with randomly generated events.
## network
This package contains functions for simplifying routes and data structure for efficiently retrieving events on a route segment.
### IntersectionGraph
Contains logic for taking routes calculated by GraphHopper and simplifying/reducing the number of points, so they can be rendered. The reduced routes are cached in a hash map, mapping from road segments to the routes they are part of, for efficient queries.
### Network
Data structure for storing the network, includes information like the distance of each edge and the `IntersectionGraph` computed on the network.
### NetworkParser
Reader for network json file.
### Simplifier
Used by `IntersectionGraph`, reduces GraphHopper routes using OpenCV's Ramer–Douglas–Peucker contour approximation algorithm.
### Timeline
Data structure for storing events. Also contains functions for reading events from CSV and a bunch of methods for querying events by mode, edge, time step, etc.
## routing
This package extends GraphHopper to train and barge routing.
### BargeFlagEncoder
Defines the 'roads'/ways a barge can take.
### CustomFlagEncoderFactory
Custom set up for the GraphHopper routing engine so it only supports truck (car), train and barge.
### Router
Wrapper around GraphHopper routing engine. It sets up the engine and provides methods for routing truck, train and barge
### TrainFlagEncoder
Defines the 'roads'/ways a train can take.
## server
This package defines an HTTP server for the GUI to query
### GraphHandler
An endpoint for getting the network nodes and routes. 
### HandlerUtil
Contains methods for formatting the response and parsing query parameters from the request.
### IntensityHandler
An endpoint for getting the traffic intensity on every edge.
### LinkHandler
An endpoint for getting the events happening on a road segment.
### MetadataHandler
An endpoint for getting metadata about the events, including the total number of time steps and the max intensity on any road segment (GUI uses this for colouring).
### NodeHandler
An endpoint for getting the events happening at a node.
### VisualizationServer
Creates a server instance given paths to the input files and port number.

# Input Data
The application expects 3 input files:
* OSM file (PBF format) containing the map.
* Json file defining a network over the map.
* CSV file specifying events on the network.
## Network specification
The network must be a JSON file structured as an object with two keys: `vertices` and `edges`.

`vertices`:
 A list of vertex objects. Each vertex represents a location and must have the following attributes:

* `name` (string): Unique name of the vertex.

* `lon` (float): Longitude coordinate.

* `lat` (float): Latitude coordinate.

`edges`: A list of edge objects. Each edge defines a connection between two vertices using a specific mode of transport and must have the following attributes:

* `from` (string): Name of the starting vertex (must match a name in vertices).

* `to` (string): Name of the destination vertex (must match a name in vertices).

* `mode` (string): Mode of transportation. Must be one of: "truck", "train", or "barge".

## Timeline specification
Events that occur on the network is specified as a csv with the following columns:
* `time` (float): Event time.
* `from` (string): Name of the starting vertex (must match a name in vertices).
* `to` (string): Name of the destination vertex (must match a name in vertices).
* `mode` (string): Mode of transportation. Must be one of: "truck", "train", or "barge".
* `category` (string): Type of cargo (e.g. Bannana, Orange, Empty Container, etc.).
* `quantity` (int): Quantity of cargo.