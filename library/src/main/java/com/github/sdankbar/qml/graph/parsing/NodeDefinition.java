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

import com.google.common.base.Preconditions;

/**
 * Definition of a GraphViz node.
 */
public class NodeDefinition {

	private final String nodeID;
	private final double x;
	private final double y;
	private final double width;
	private final double height;

	/**
	 * @param line Line from a node definition in GraphViz, in the "plain" format.
	 */
	public NodeDefinition(final String line) {
		final String[] tokens = line.split(" ");
		Preconditions.checkArgument(tokens.length == 11, "Unexpected number of tokens on line=", line);
		nodeID = tokens[1];
		try {
			final double centerX = Double.parseDouble(tokens[2]);
			final double centerY = Double.parseDouble(tokens[3]);
			width = Double.parseDouble(tokens[4]);
			height = Double.parseDouble(tokens[5]);
			x = centerX - width / 2.0;
			y = centerY - height / 2.0;
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("Error parsing number", e);
		}
	}

	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @return the nodeID
	 */
	public String getNodeID() {
		return nodeID;
	}

	/**
	 * @return the width
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

}
