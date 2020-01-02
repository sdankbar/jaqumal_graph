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
package com.github.sdankbar.qml.graph.spline;

import static org.junit.Assert.assertEquals;

import java.awt.geom.Point2D;

import org.junit.Test;

import com.github.sdankbar.qml.graph.splines.BSpline;
import com.google.common.collect.ImmutableList;

/**
 * Tests the BSpline class.
 */
public class BSplineTest {

	/**
	 *
	 */
	@Test
	public void test_BSpline_1() {
		final BSpline b1 = new BSpline(ImmutableList.of(new Point2D.Double(0, 0), new Point2D.Double(1, 1),
				new Point2D.Double(2, 2), new Point2D.Double(3, 3)));
		assertEquals(new Point2D.Double(0, 0), b1.evaluate(0));
		assertEquals(new Point2D.Double(1.5, 1.5), b1.evaluate(0.5));
		assertEquals(new Point2D.Double(3, 3), b1.evaluate(1.0));
	}

	/**
	 *
	 */
	@Test
	public void test_BSpline_2() {
		final BSpline b1 = new BSpline(ImmutableList.of(new Point2D.Double(0.63561, 4.0012),
				new Point2D.Double(0.47815, 4.2496), new Point2D.Double(0.31944, 4.5), new Point2D.Double(0.31944, 4.5),
				new Point2D.Double(0.31944, 4.5), new Point2D.Double(0.31944, 4.5), new Point2D.Double(0.31944, 4.5),
				new Point2D.Double(0.31944, 4.5), new Point2D.Double(0.34971, 5.6653),
				new Point2D.Double(0.38546, 5.8605)));

		assertEquals(new Point2D.Double(0.63561, 4.0012), b1.evaluate(0));
		assertEquals(new Point2D.Double(0.31944, 4.5), b1.evaluate(0.5));
		assertEquals(new Point2D.Double(0.38546, 5.8605), b1.evaluate(1.0));
	}

	/**
	 *
	 */
	@Test
	public void test_BSpline_3() {
		final BSpline b1 = new BSpline(ImmutableList.of(new Point2D.Double(0.55773, 4.0041),
				new Point2D.Double(0.46058, 4.1548), new Point2D.Double(0.36972, 4.3268),
				new Point2D.Double(0.31944, 4.5), new Point2D.Double(0.17518, 4.9971),
				new Point2D.Double(0.26099, 5.5909), new Point2D.Double(0.35735, 5.999)));

		assertEquals(new Point2D.Double(0.55773, 4.0041), b1.evaluate(0));
		assertEquals(new Point2D.Double(0.46358296000000004, 4.157120266666667), b1.evaluate(0.1));
		assertEquals(new Point2D.Double(0.4047312799999999, 4.267446133333333), b1.evaluate(0.2));
		assertEquals(new Point2D.Double(0.3668782133333333, 4.3546356), b1.evaluate(0.3));
		assertEquals(new Point2D.Double(0.3370861599999999, 4.441475866666666), b1.evaluate(0.4));
		assertEquals(new Point2D.Double(0.30377666666666664, 4.553983333333333), b1.evaluate(0.5));
		assertEquals(new Point2D.Double(0.2612644533333333, 4.7146988), b1.evaluate(0.6));
		assertEquals(new Point2D.Double(0.22720056000000002, 4.931699066666667), b1.evaluate(0.7));
		assertEquals(new Point2D.Double(0.2246445066666667, 5.2097168), b1.evaluate(0.8));
		assertEquals(new Point2D.Double(0.26686701333333335, 5.5597376), b1.evaluate(0.9));
		assertEquals(new Point2D.Double(0.35735, 5.999), b1.evaluate(1.0));
	}

	/**
	 *
	 */
	@Test(expected = IllegalArgumentException.class)
	public void test_BSpline_bad_t_neg() {
		final BSpline b1 = new BSpline(ImmutableList.of(new Point2D.Double(0, 0), new Point2D.Double(1, 1),
				new Point2D.Double(2, 2), new Point2D.Double(3, 3)));
		b1.evaluate(-1);
	}

	/**
	 *
	 */
	@Test(expected = IllegalArgumentException.class)
	public void test_BSpline_bad_t_over_one() {
		final BSpline b1 = new BSpline(ImmutableList.of(new Point2D.Double(0, 0), new Point2D.Double(1, 1),
				new Point2D.Double(2, 2), new Point2D.Double(3, 3)));
		b1.evaluate(1.1);
	}

}
