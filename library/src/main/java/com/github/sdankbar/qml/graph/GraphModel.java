/**
 * The MIT License
 * Copyright © 2020 Stephen Dankbar
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.sdankbar.qml.graph;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.SystemUtils;

import com.github.sdankbar.qml.JQMLModelFactory;
import com.github.sdankbar.qml.JVariant;
import com.github.sdankbar.qml.graph.parsing.EdgeDefinition;
import com.github.sdankbar.qml.graph.parsing.GraphVizParser;
import com.github.sdankbar.qml.models.list.JQMLListModel;
import com.github.sdankbar.qml.models.singleton.JQMLSingletonModel;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Model for sending Graph Vertex and Edge layout and user defined information
 * to QML for drawing. Creates 3 models that send data to QML.
 *
 * <prefix>_graph - Contains information about the entire graph. Valid keys are
 * in the GraphKey Enum.
 *
 * <prefix>_vertices - Contains information about a vertex in the graph. Valid
 * keys are in the VertexKey Enum plus any user defined keys.
 *
 *
 * <prefix>_edges - Contains information about an edge in the graph. Valid keys
 * are in the EdgeKey Enum.
 *
 * @param <K> User define key/role type. toString() of type must return valid
 *        QML identifiers.
 */
public class GraphModel<K> {

	/**
	 * Create a new GraphModel using an Enum as the user define role type.
	 *
	 * @param modelPrefix The prefix prepended to the 3 QML models this class
	 *                    creates (_graph, _vertices, and _edges).
	 * @param factory     Factory for creating the models.
	 * @param keyClass    Class of an Enum type that contains the user defined
	 *                    roles.
	 * @param dpi         Dots per inch on the display that will display this
	 *                    GraphModel's graph. Used for converting between inches
	 *                    (used by GraphViz) and pixels.
	 * @return The new GraphModel.
	 */
	public static <T extends Enum<T>> GraphModel<T> create(final String modelPrefix, final JQMLModelFactory factory,
			final Class<T> keyClass, final double dpi) {
		final ImmutableSet<String> userKeys = EnumSet.allOf(keyClass).stream().map(e -> e.name())
				.collect(ImmutableSet.toImmutableSet());
		return new GraphModel<>(modelPrefix, factory, userKeys, dpi);
	}

	/**
	 * Create a new GraphModel using a Set of keys.
	 *
	 * @param modelPrefix The prefix prepended to the 3 QML models this class
	 *                    creates (_graph, _vertices, and _edges).
	 * @param factory     Factory for creating the models.
	 * @param keySet      Set of all valid keys/roles for sending user defined data
	 *                    to QML.
	 * @param dpi         Dots per inch on the display that will display this
	 *                    GraphModel's graph. Used for converting between inches
	 *                    (used by GraphViz) and pixels.
	 * @return The new GraphModel.
	 */
	public static <T> GraphModel<T> create(final String modelPrefix, final JQMLModelFactory factory,
			final ImmutableSet<T> keySet, final double dpi) {
		final ImmutableSet<String> stringKeySet = keySet.stream().map(k -> k.toString())
				.collect(ImmutableSet.toImmutableSet());
		return new GraphModel<>(modelPrefix, factory, stringKeySet, dpi);
	}

	private static String runGraphViz(final String graphDef) {
		final ProcessBuilder builder = new ProcessBuilder();
		// dot command
		// plain output format
		// invert y coordinates, (0, 0) is top left.
		if (SystemUtils.IS_OS_WINDOWS) {
			builder.command("dot.exe", "-Tplain", "-y");
		} else {
			builder.command("dot", "-Tplain", "-y");
		}
		try {
			final Process p = builder.start();

			try (final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()))) {
				w.write(graphDef);
				w.flush();
			}

			final BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final StringBuilder plainFormat = new StringBuilder();
			String line;
			while ((line = input.readLine()) != null) {
				plainFormat.append(line);
				plainFormat.append(System.lineSeparator());
			}

			return plainFormat.toString();
		} catch (final IOException e) {
			throw new IllegalStateException("Failed to find \"dot\" utility", e);
		}
	}

	private long nextUUID = 1;
	private final JQMLSingletonModel<GraphKey> singletonModel;
	private final JQMLListModel<String> vertexModel;

	private final JQMLListModel<EdgeKey> edgeModel;

	private final List<Vertex<K>> vertices = new ArrayList<>();

	private final double dpi;

	private GraphModel(final String modelPrefix, final JQMLModelFactory factory, final ImmutableSet<String> userKeys,
			final double dpi) {
		Objects.requireNonNull(modelPrefix, "modelPrefix is null");
		Objects.requireNonNull(factory, "factory is null");
		this.dpi = dpi;

		singletonModel = factory.createSingletonModel(modelPrefix + "_graph", GraphKey.class);

		final ImmutableSet<String> builtInKeys = EnumSet.allOf(VertexKey.class).stream().map(e -> e.name())
				.collect(ImmutableSet.toImmutableSet());

		final Set<String> keys = new HashSet<>();
		keys.addAll(builtInKeys);
		keys.addAll(userKeys);
		vertexModel = factory.createListModel(modelPrefix + "_vertices", keys);
		edgeModel = factory.createListModel(modelPrefix + "_edges", EdgeKey.class);
	}

	/**
	 * @param widthInches  Width of the new Vertex in inches.
	 * @param heightInches Height of the new Vertex in inches.
	 * @return Newly created Vertex in this graph.
	 */
	public Vertex<K> createVertex(final double widthInches, final double heightInches) {
		final Map<String, JVariant> map = vertexModel.add(ImmutableMap.of());
		final Vertex<K> v = new Vertex<>(++nextUUID, widthInches, heightInches, this, map);
		vertices.add(v);
		return v;
	}

	private String getGraphVizDOTFormat() {
		final StringBuilder builder = new StringBuilder();

		final String newLine = System.lineSeparator();

		builder.append("digraph {");
		builder.append(newLine);

		for (final Vertex<K> v : vertices) {
			builder.append(v.getGraphVizNodeDefinition());
			builder.append(newLine);
			builder.append(v.getGraphVizEdgeDefinition());
			builder.append(newLine);
		}

		builder.append("}");

		return builder.toString();
	}

	/**
	 * Lays out the graph, sending the updated layout data to QML. Must be called
	 * after changing the structure of the graph in order for those changes to be
	 * applied.
	 */
	public void layoutGraph() {
		final String graphVizFormat = getGraphVizDOTFormat();

		final String graphVizOutput = runGraphViz(graphVizFormat);

		final GraphVizParser parser = new GraphVizParser(graphVizOutput, dpi);

		singletonModel.put(GraphKey.width, new JVariant(parser.getGraphWidthInches() * dpi));
		singletonModel.put(GraphKey.height, new JVariant(parser.getGraphHeightInches() * dpi));

		for (final Vertex<K> b : vertices) {
			b.apply(parser.getNode(b.getUUID()), dpi);
		}

		// Add edges
		edgeModel.clear();
		for (final Vertex<K> b : vertices) {
			final List<EdgeDefinition> edges = parser.getEdges(b.getUUID());

			for (final EdgeDefinition e : edges) {
				final ImmutableList<Point2D> polyline = e.getPolyLine();
				final ImmutableMap.Builder<EdgeKey, JVariant> builder = ImmutableMap.builder();
				builder.put(EdgeKey.polyline, new JVariant(polyline));
				builder.put(EdgeKey.head_id, new JVariant(e.getHeadUUID()));
				builder.put(EdgeKey.tail_id, new JVariant(e.getTailUUID()));
				edgeModel.add(builder.build());
			}
		}
	}

	/**
	 * @param removed Vertex to remove.
	 * @return True if the vertex was removed;
	 */
	public boolean removeVertex(final Vertex<K> removed) {
		Objects.requireNonNull(removed, "removed is null");
		Preconditions.checkArgument(this == removed.getOwningGraph(),
				"Attempted to remove Vertex not owned by this GraphModel");
		// Remove the item from the JQmlListModel
		for (int i = 0; i < vertexModel.size(); ++i) {
			if (vertexModel.get(i) == removed.getOwningGraph()) {
				vertexModel.remove(i);
				break;
			}
		}
		return vertices.remove(removed);
	}

}
