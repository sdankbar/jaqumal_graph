# Jaqumal Graph
Graph visualization library using Jaqumal

# Dependencies

Java 8 or newer and uses Maven to build

See pom.xml for details of the Java libraries used

* Jaqumal
* Guava
* JUnit4
* Log4j2

GraphViz is used to layout the graph and is a runtime dependency.  The "dot" executable needs to be on the PATH.

# License

MIT

# Building and adding dependency to pom.xml

Use maven to build Jaqumal Graph.

- mvn clean install
  
Then add the following to pom.xml to be able to use Jaqumal Graph.

```
<dependency>
	<groupId>com.github.sdankbar.jaqumal_graph</groupId>
	<artifactId>library</artifactId>
	<version>${current.version}</version>
</dependency>
```

# Quick Start

Start by creating a JQMLApplication.  Then create a GraphModel by using one of the 2 create() static methods.  In order to send user defined data to QML, an Enum or a Set of keys will need to be specified.  Then begin creating all the required Vertices using the createVertex() method.  The size of the Vertex will need to be specified in inches since GraphViz uses inches for its units.  Then use the addChild() method on the Vertices to specify the edges of the graph.  All edges are directional and go from parent to child.  After the structure of the graph has been defined, layout() needs to be called on the GraphModel.  This causes the graph to be laid out using GraphViz and the layout to be sent to QML.  From there, use the user defined keys to specify additional data to be associated with a Vertex (label, color, etc.).  Finally, QML needs to be written to render the graph.  See the main.qml of the simple_graph example for how to do this.

# Examples

##### Simple Graph

Renders a 6 vertex graph that contains cycles.  Mouse presses on a Vertex will highlight that Vertex and all edges connected to that Vertex.