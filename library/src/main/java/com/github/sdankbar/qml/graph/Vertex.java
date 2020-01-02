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

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.sdankbar.qml.JVariant;
import com.github.sdankbar.qml.graph.parsing.NodeDefinition;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Represents a vertex in the graph.
 *
 * @param <K> Type used to define user specified model roles. toString() method
 *        must return valid QML identifier.
 */
public class Vertex<K> {

	private static final JVariant ZERO_VARIANT = new JVariant(0);
	private static final JVariant ONE_VARIANT = new JVariant(1);

	private static String getIDAsGraphVizIDString(final long uuid) {
		final String oct = Long.toOctalString(uuid);
		// '0' == 48, map to 65 = 'A'
		// '7' == 55, map to 72 = 'H'
		return oct.chars().map(c -> (char) (c + 17))
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
	}

	private static JVariant toPixels(final double inches, final double dpi) {
		return new JVariant(inches * dpi);
	}

	private final String uuid;

	private final GraphModel<K> graph;
	private final Set<Vertex<K>> children = new HashSet<>();

	private final Set<Vertex<K>> parents = new HashSet<>();

	private final Map<String, JVariant> qmlModelMap;

	private double vertexWidthInches = 1;
	private double vertexHeightInches = 1;

	Vertex(final long uuid, final double widthInches, final double heightInches, final GraphModel<K> graph,
			final Map<String, JVariant> qmlModelMap) {
		this.uuid = getIDAsGraphVizIDString(uuid);
		this.graph = Objects.requireNonNull(graph, "graph is null");
		this.qmlModelMap = Objects.requireNonNull(qmlModelMap, "qmlModelMap is null");
		this.qmlModelMap.put(VertexKey.id.toString(), new JVariant(this.uuid));
		this.qmlModelMap.put(VertexKey.x.toString(), ZERO_VARIANT);
		this.qmlModelMap.put(VertexKey.y.toString(), ZERO_VARIANT);
		this.qmlModelMap.put(VertexKey.width.toString(), ONE_VARIANT);
		this.qmlModelMap.put(VertexKey.height.toString(), ONE_VARIANT);
		setVertexSizeDimensionInches(widthInches, heightInches);
	}

	/**
	 * Make another Vertex a child of this Vertex.
	 *
	 * @param v Vertex to make a child.
	 *
	 * @throws IllegalArgumentException Thrown if v is not part of the same
	 *                                  GraphModel as this.
	 */
	public void addChild(final Vertex<K> v) {
		Objects.requireNonNull(v, "v is null");
		Preconditions.checkArgument(v.graph == graph, "v is not from the same GraphModel as this Vertex");
		children.add(v);
		v.addParent(this);
	}

	private void addParent(final Vertex<K> parent) {
		parents.add(parent);
	}

	void apply(final NodeDefinition def, final double dpi) {
		qmlModelMap.put(VertexKey.x.toString(), toPixels(def.getX(), dpi));
		qmlModelMap.put(VertexKey.y.toString(), toPixels(def.getY(), dpi));
		qmlModelMap.put(VertexKey.width.toString(), toPixels(def.getWidth(), dpi));
		qmlModelMap.put(VertexKey.height.toString(), toPixels(def.getHeight(), dpi));
	}

	/**
	 * @param key Key to lookup the value for.
	 * @return The value for the key.
	 */
	public Optional<JVariant> get(final K key) {
		Objects.requireNonNull(key, "key is null");
		return Optional.ofNullable(qmlModelMap.get(key.toString()));
	}

	/**
	 * @return a list of this Vertex's children.
	 */
	public ImmutableList<Vertex<K>> getChildren() {
		return ImmutableList.copyOf(children);
	}

	String getGraphVizEdgeDefinition() {
		final StringBuilder builder = new StringBuilder();

		if (!children.isEmpty()) {

			builder.append(uuid);
			builder.append(" -> {");

			builder.append(children.stream().map(c -> c.uuid).collect(Collectors.joining(", ")));

			// arrowhead=none so that the last point of the returned spline touchs the
			// vertex the edge ends on.
			builder.append("} [arrowhead=none]");
		}

		return builder.toString();
	}

	String getGraphVizNodeDefinition() {
		final StringBuilder builder = new StringBuilder();
		builder.append(uuid);
		builder.append(" [");
		builder.append("width=");
		builder.append(vertexWidthInches);
		builder.append(" height=");
		builder.append(vertexHeightInches);
		builder.append(" shape=box");
		builder.append("]");

		return builder.toString();
	}

	/**
	 * @return The height of this Vertex in pixels. Undefined if GraphModel's
	 *         layout() has not been called yet.
	 */
	public int getHeight() {
		return qmlModelMap.get(VertexKey.height.toString()).asInteger();
	}

	GraphModel<K> getOwningGraph() {
		return graph;
	}

	/**
	 * @return The unique identifier for this Vertex. Identifier is only unique per
	 *         GraphModel.
	 */
	public String getUUID() {
		return uuid;
	}

	/**
	 * @return The width of this Vertex in pixels. Undefined if GraphModel's
	 *         layout() has not been called yet.
	 */
	public int getWidth() {
		return qmlModelMap.get(VertexKey.width.toString()).asInteger();
	}

	/**
	 * @return The X coordinate of the vertex's top left corner. Undefined if
	 *         GraphModel's layout() has not been called yet.
	 */
	public int getX() {
		return qmlModelMap.get(VertexKey.x.toString()).asInteger();
	}

	/**
	 * @return The Y coordinate of the vertex's top left corner. Undefined if
	 *         GraphModel's layout() has not been called yet.
	 */
	int getY() {
		return qmlModelMap.get(VertexKey.y.toString()).asInteger();
	}

	/**
	 * @return True if this Vertex is a leaf. A leaf vertex has no children.
	 */
	public boolean isLeaf() {
		return children.isEmpty();
	}

	/**
	 * @return True if this Vertex is a root. A root vertex has no parent.
	 */
	public boolean isRoot() {
		return parents.isEmpty();
	}

	/**
	 * Stores user defined data for this Vertex.
	 *
	 * @param key   Name/Role for the data.
	 * @param value The new value.
	 */
	public void put(final K key, final JVariant value) {
		Objects.requireNonNull(key, "key is null");
		Objects.requireNonNull(value, "value is null");
		qmlModelMap.put(key.toString(), value);
	}

	/**
	 * Removes user defined data for this Vertex.
	 *
	 * @param key Name/Role to remove.
	 * @return True if data was removed.
	 */
	public boolean remove(final K key) {
		Objects.requireNonNull(key, "key is null");
		return qmlModelMap.remove(key.toString()) != null;
	}

	/**
	 * Removes a child Vertex from this Vertex.
	 *
	 * @param removed Child vertex to remove.
	 * @return True if the child was successfully removed false otherwise.
	 *
	 * @throws IllegalArgumentException Thrown if removed is not part of the same
	 *                                  GraphModel as this.
	 */
	public boolean removeChild(final Vertex<K> removed) {
		Objects.requireNonNull(removed, "removed is null");
		Preconditions.checkArgument(removed.graph == graph, "v is not from the same GraphModel as this Vertex");
		if (children.remove(removed)) {
			removeParent(this);
			return true;
		} else {
			return false;
		}
	}

	private void removeParent(final Vertex<K> parent) {
		parents.remove(parent);
	}

	/**
	 * Sets the size of this Vertex in inches. Used when laying out the graph.
	 *
	 * @param w Width of this Vertex in inches.
	 * @param h Height of this Vertex in inches.
	 */
	public void setVertexSizeDimensionInches(final double w, final double h) {
		Preconditions.checkArgument(w > 0, "w <= 0 ", Double.valueOf(w));
		Preconditions.checkArgument(h > 0, "h <= 0 ", Double.valueOf(h));
		vertexWidthInches = w;
		vertexHeightInches = h;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Vertex [uuid=" + uuid + ", graph=" + graph + ", children=" + children + ", parents=" + parents
				+ ", qmlModelMap=" + qmlModelMap + ", vertexWidthInches=" + vertexWidthInches + ", vertexHeightInches="
				+ vertexHeightInches + "]";
	}

}
