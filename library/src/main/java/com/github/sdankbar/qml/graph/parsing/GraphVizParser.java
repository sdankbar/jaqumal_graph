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
package com.github.sdankbar.qml.graph.parsing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

/**
 * Parser for GraphViz's "plain" output format.
 */
public class GraphVizParser {

	private double graphWidth = 1;
	private double graphHeight = 1;
	private final Map<String, NodeDefinition> nodes = new HashMap<>();
	private final Multimap<String, EdgeDefinition> edges = MultimapBuilder.hashKeys().arrayListValues().build();

	/**
	 * @param graphViaData The "plain" GraphViz output format to parse.
	 * @param dpi          The number of dots per inch. Used to convert between
	 *                     GraphViz's inches to pixels.
	 */
	public GraphVizParser(final String graphViaData, final double dpi) {
		final String[] lines = graphViaData.split(System.lineSeparator());

		for (final String l : lines) {
			if (l.startsWith("graph")) {
				parseGraphLine(l);
			} else if (l.startsWith("node")) {
				final NodeDefinition def = new NodeDefinition(l);
				nodes.put(def.getNodeID(), def);
			} else if (l.startsWith("edge")) {
				final EdgeDefinition def = new EdgeDefinition(l, dpi);
				edges.put(def.getHeadUUID(), def);
			}
		}
	}

	/**
	 * @param nodeID The node to search for.
	 * @return The edges that end at the vertex "nodeID".
	 */
	public List<EdgeDefinition> getEdges(final String nodeID) {
		return ImmutableList.copyOf(edges.get(nodeID));
	}

	/**
	 * @return The height of the GraphViz graph in inches.
	 */
	public double getGraphHeightInches() {
		return graphHeight;
	}

	/**
	 * @return The width of the GraphViz graph in inches.
	 */
	public double getGraphWidthInches() {
		return graphWidth;
	}

	/**
	 * @param nodeID The node to search for.
	 * @return The found NodeDefinition.
	 */
	public NodeDefinition getNode(final String nodeID) {
		final NodeDefinition def = nodes.get(nodeID);
		Preconditions.checkArgument(def != null, "Could not find Node for \"{}\"", nodeID);
		return def;
	}

	private void parseGraphLine(final String line) {
		final String[] tokens = line.split(" ");
		Preconditions.checkArgument(tokens.length == 4, "Unexpected number of tokens on line \"{}\"", line);
		try {
			graphWidth = Double.parseDouble(tokens[2]);
			graphHeight = Double.parseDouble(tokens[3]);
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("Error parsing number", e);
		}
	}

}
