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
package com.github.sdankbar.qml.graph.splines;

import java.awt.geom.Point2D;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Implementation of a cubic B-Spline algorithm
 */
public class BSpline {

	private static final int p = 3;// cubic

	private final double[] knots;
	private final double[] controlX;
	private final double[] controlY;

	// p = m - n - 1
	private final int n;
	private final int m;

	/**
	 * Constructs a new spline.
	 *
	 * @param controlPoints List of the spline's control points.
	 */
	public BSpline(final ImmutableList<Point2D> controlPoints) {
		n = controlPoints.size() - 1;
		m = p + n + 1;

		knots = new double[m + 1];
		for (int i = 0; i < (p + 1); ++i) {
			knots[i] = 0;
		}

		final double internalKnots = m - 2 * p - 1;
		for (int i = p + 1; i < (m - p); ++i) {
			final double numerator = i - p;
			knots[i] = numerator / (internalKnots + 1);
		}

		for (int i = m - p; i < knots.length; ++i) {
			knots[i] = 1;
		}

		controlX = new double[controlPoints.size()];
		controlY = new double[controlPoints.size()];
		for (int i = 0; i < controlPoints.size(); ++i) {
			controlX[i] = controlPoints.get(i).getX();
			controlY[i] = controlPoints.get(i).getY();
		}
	}

	/**
	 * Evaluates the spline at the value "t".
	 *
	 * @param t Value to evaluate the spline at. Valid range is [0, 1].
	 * @return The point on the spline.
	 */
	public Point2D evaluate(final double t) {
		Preconditions.checkArgument(0 <= t && t <= 1, "t is not in the range [0, 1] t=" + t);

		if (t == 1.0) {
			// Basis function is not defined at t == 1.0, so handle as a special case
			return new Point2D.Double(controlX[controlX.length - 1], controlY[controlY.length - 1]);
		} else {
			double x = 0;
			double y = 0;
			for (int i = 0; i <= n; ++i) {
				final double basisFunction = nFunction(i, p, t);
				x += controlX[i] * basisFunction;
				y += controlY[i] * basisFunction;
			}
			return new Point2D.Double(x, y);
		}
	}

	private double nFunction(final int i, final int j, final double t) {
		if (j > 0) {
			double v1 = (t - knots[i]) / (knots[i + j] - knots[i]);
			final double v2 = nFunction(i, j - 1, t);
			if (!Double.isFinite(v1)) {
				v1 = 0;
			}

			double v3 = (knots[i + j + 1] - t) / (knots[i + j + 1] - knots[i + 1]);
			final double v4 = nFunction(i + 1, j - 1, t);

			if (!Double.isFinite(v3)) {
				v3 = 0;
			}
			final double v = v1 * v2 + v3 * v4;
			return v;
		} else {
			final double leftT = knots[i];
			final double rightT = knots[i + 1];
			if (leftT <= t && t < rightT) {
				return 1;
			} else {
				return 0;
			}
		}
	}

}
