
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
import QtQuick 2.11
import QtQuick.Window 2.11
import QtQuick.Controls 2.4
import QtQuick.Shapes 1.11
import com.github.sdankbar.jaqumal 0.4

Window {
    visible: true
    width: 400
    height: 500
    x: 100
    y: 100
    title: "Simple Graph"

    property string selectedNode: ""

    ScrollView {
        anchors.fill: parent
        clip: true
        contentHeight: test_graph.height
        contentWidth: test_graph.width
        layer.enabled: true
        layer.samples: 4

        Repeater {
            model: test_vertices
            delegate: Rectangle {
                x: model.x
                y: model.y
                width: model.width
                height: model.height
                border.color: mouseArea.pressed ? "red" : "black"
                border.width: 4

                Text {
                    x: 10
                    y: 10
                    text: model.text
                }

                MouseArea {
                    id: mouseArea
                    anchors.fill: parent

                    onPressedChanged: {
                        if (pressed) {
                            selectedNode = model.id
                        } else {
                            selectedNode = ""
                        }
                    }
                }
            }
        }

        Repeater {
            anchors.fill: parent

            model: test_edges
            delegate: JPolyline {
                property bool tailSelected: model.tail_id === selectedNode
                property bool headSelected: model.head_id === selectedNode

                anchors.fill: parent
                polyline: model.polyline

                strokeColor: {
                    if (tailSelected) {
                        return "red"
                    } else if (headSelected) {
                        return "blue"
                    } else {
                        return "black"
                    }
                }
                strokeWidth: (tailSelected || headSelected) ? 4 : 2
                //shapePath.joinStyle: ShapePath.RoundJoin
                //shapePath.capStyle: ShapePath.RoundCap
            }
        }
    }
}
