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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.github.sdankbar.qml.graph.splines.BSpline;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Definition of a GraphViz edge.
 */
public class EdgeDefinition {

	private static class InterpolatedPoint {
		private final double n;
		private final Point2D p;

		public InterpolatedPoint(final double n, final Point2D p) {
			this.n = n;
			this.p = p;
		}
	}

	private static double MIN_INTERPOLATION_DISTANCE = 10 * 10;
	private static double arrowLengthInches = 0.125;
	private static final double COSINE = Math.cos(Math.toRadians(30));

	private static final double SINE = Math.sin(Math.toRadians(30));
	private static final double COSINE_NEG = Math.cos(Math.toRadians(-30));

	private static final double SINE_NEG = Math.sin(Math.toRadians(-30));

	private static boolean needsAdditionalPoints(final Point2D p1, final Point2D p2, final Point2D p3) {
		final Line2D l = new Line2D.Double(p1, p3);
		if (l.ptLineDist(p2) < 0.5) {
			return false;
		} else if (p1.distanceSq(p2) > MIN_INTERPOLATION_DISTANCE) {
			return true;
		} else if (p2.distanceSq(p3) > MIN_INTERPOLATION_DISTANCE) {
			return true;
		} else {
			return false;
		}
	}

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

			final ImmutableList.Builder<Point2D> builder = ImmutableList.builder();
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

	private void evaluateSpline(final double s, final double e, final Point2D sPoint, final Point2D ePoint,
			final List<InterpolatedPoint> working) {
		final double middle = (s + e) / 2;
		final Point2D middlePoint = spline.evaluate(middle);
		working.add(new InterpolatedPoint(middle, middlePoint));
		if (needsAdditionalPoints(sPoint, middlePoint, ePoint)) {
			evaluateSpline(s, middle, sPoint, middlePoint, working);
			evaluateSpline(middle, e, middlePoint, ePoint, working);
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
		final List<InterpolatedPoint> tempList = new ArrayList<>();

		tempList.add(new InterpolatedPoint(0.0, spline.evaluate(0.0)));
		tempList.add(new InterpolatedPoint(1.0, spline.evaluate(1.0)));

		evaluateSpline(0.0, 1.0, tempList.get(0).p, tempList.get(1).p, tempList);

		tempList.sort((l, r) -> Double.compare(l.n, r.n));

		final Point2D secondToLastPoint = tempList.get(tempList.size() - 2).p;
		final Point2D lastPoint = tempList.get(tempList.size() - 1).p;

		final ImmutableList.Builder<Point2D> builder = ImmutableList.builder();
		for (final InterpolatedPoint inter : tempList) {
			builder.add(inter.p);
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
