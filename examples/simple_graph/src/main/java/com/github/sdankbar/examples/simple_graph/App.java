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
package com.github.sdankbar.examples.simple_graph;

import com.github.sdankbar.qml.JQMLApplication;
import com.github.sdankbar.qml.JScreen;
import com.github.sdankbar.qml.JVariant;
import com.github.sdankbar.qml.eventing.NullEventFactory;
import com.github.sdankbar.qml.eventing.NullEventProcessor;
import com.github.sdankbar.qml.graph.GraphModel;
import com.github.sdankbar.qml.graph.Vertex;
import com.google.common.collect.ImmutableList;

/**
 * Traffic light GUI. Shows the usage of a JQMLSingletonModel and
 * QMLThreadExecutor.
 */
public class App {

	private enum GraphRole {
		text;
	}

	private static double getDPI(final JQMLApplication<NullEventProcessor> app) {
		final ImmutableList<JScreen> screens = app.screens();
		if (screens.isEmpty()) {
			return 96;
		} else {
			return screens.get(0).getDpi();
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		final JQMLApplication<NullEventProcessor> app = JQMLApplication.create(args, new NullEventFactory<>());
		final GraphModel<GraphRole> model = GraphModel.create("test", app.getModelFactory(), GraphRole.class,
				getDPI(app));

		final Vertex<GraphRole> v1 = model.createVertex(1, 1);
		v1.put(GraphRole.text, new JVariant("v1"));

		final Vertex<GraphRole> v2 = model.createVertex(1, 1);
		v2.put(GraphRole.text, new JVariant("v2"));

		final Vertex<GraphRole> v3 = model.createVertex(1, 1);
		v3.put(GraphRole.text, new JVariant("v3"));

		final Vertex<GraphRole> v4 = model.createVertex(1, 1);
		v4.put(GraphRole.text, new JVariant("v4"));

		final Vertex<GraphRole> v5 = model.createVertex(1, 1);
		v5.put(GraphRole.text, new JVariant("v5"));

		final Vertex<GraphRole> v6 = model.createVertex(1, 1);
		v6.put(GraphRole.text, new JVariant("v6"));

		v1.addChild(v2);
		v2.addChild(v3);
		v3.addChild(v2);
		v3.addChild(v4);
		v3.addChild(v5);
		v3.addChild(v6);
		v5.addChild(v6);

		final long start = System.currentTimeMillis();
		model.layoutGraphAsync(app.getQMLThreadExecutor()).thenAccept(
				(v) -> System.out.println("Took " + (System.currentTimeMillis() - start) + " milliseconds"));

		app.loadAndWatchQMLFile("./src/main/qml/main.qml");

		app.execute();

		System.exit(0);
	}
}
