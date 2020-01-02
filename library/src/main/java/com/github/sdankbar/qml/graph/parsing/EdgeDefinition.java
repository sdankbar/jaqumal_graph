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

import java.awt.geom.Point2D;

import com.github.sdankbar.qml.graph.splines.BSpline;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Definition of a GraphViz edge.
 */
public class EdgeDefinition {

	private static double arrowLengthInches = 0.125;

	private static final double COSINE = Math.cos(Math.toRadians(30));
	private static final double SINE = Math.sin(Math.toRadians(30));

	private static final double COSINE_NEG = Math.cos(Math.toRadians(-30));
	private static final double SINE_NEG = Math.sin(Math.toRadians(-30));

	private final double dpi;
	private final BSpline spline;
	private final String headUUID;
	private final String tailUUID;

	/**
	 * @param line Line from an edge definition in GraphViz, in the "plain" format.
	 * @param dpi  The number of dots per inch on the display the edge will be drawn
	 *             on. Used to convert between GraphViz's inch dimensions to pixels.
	 */
	public EdgeDefinition(final String line, final double dpi) {
		this.dpi = dpi;
		final String[] tokens = line.split(" ");
		Preconditions.checkArgument(tokens.length >= 6, "Unexpected number of tokens on line=", line);
		tailUUID = tokens[1];
		headUUID = tokens[2];

		try {
			final int n = Integer.parseInt(tokens[3]);
			Preconditions.checkArgument(tokens.length == 4 + 2 * n + 2, "Unexpected number of tokens on line=", line);

			final ImmutableList.Builder<Point2D> builder = ImmutableList.builderWithExpectedSize(n);
			for (int i = 0; i < n; ++i) {
				final double x = dpi * Double.parseDouble(tokens[4 + 2 * i]);
				final double y = dpi * Double.parseDouble(tokens[4 + 2 * i + 1]);
				builder.add(new Point2D.Double(x, y));
			}
			spline = new BSpline(builder.build());
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("Error parsing number", e);
		}
	}

	/**
	 * @return the headUUID
	 */
	public String getHeadUUID() {
		return headUUID;
	}

	/**
	 * @return Returns the edge as a list of points (polyline), including the arrow
	 *         head.
	 */
	public ImmutableList<Point2D> getPolyLine() {
		final ImmutableList.Builder<Point2D> builder = ImmutableList.builder();

		final double[] tArray = { 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0 };
		Point2D secondToLastPoint = null;
		Point2D lastPoint = null;
		for (final double t : tArray) {
			final Point2D p = spline.evaluate(t);
			builder.add(p);
			secondToLastPoint = lastPoint;
			lastPoint = p;
		}

		if (secondToLastPoint != null && lastPoint != null) {
			final double arrowLength = arrowLengthInches * dpi;

			final double deltaX = secondToLastPoint.getX() - lastPoint.getX();
			final double deltaY = secondToLastPoint.getY() - lastPoint.getY();
			final double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
			final double normalX = arrowLength * deltaX / length;
			final double normalY = arrowLength * deltaY / length;

			final Point2D arrow1 = new Point2D.Double(lastPoint.getX() + COSINE * normalX - SINE * normalY,
					lastPoint.getY() + SINE * normalX + COSINE * normalY);
			final Point2D arrow2 = new Point2D.Double(lastPoint.getX() + COSINE_NEG * normalX - SINE_NEG * normalY,
					lastPoint.getY() + SINE_NEG * normalX + COSINE_NEG * normalY);

			builder.add(arrow1);
			builder.add(lastPoint);
			builder.add(arrow2);
		}

		return builder.build();
	}

	/**
	 * @return the tailUUID
	 */
	public String getTailUUID() {
		return tailUUID;
	}

}
