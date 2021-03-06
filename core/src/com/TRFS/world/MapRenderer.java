package com.TRFS.world;

import com.TRFS.scenarios.map.Coordinate;
import com.TRFS.scenarios.map.Lane;
import com.TRFS.scenarios.map.Link;
import com.TRFS.scenarios.map.Map;
import com.TRFS.scenarios.map.Node;
import com.TRFS.simulator.SimulationParameters;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class MapRenderer {

	public static void render(Map map, int zLevel) {
		
		// Start required graphic utils for rendering links
		for (Link link : map.links) {
			if (link.z == zLevel) {
				/* link.render() */
			}
			;
		}
		// Render the nodes
		for (Node node : map.nodes) {
			if (node.zLevel == zLevel) {
				/* node.render(); */
			}
		}
	}

	public static void renderDebug(Map map, ShapeRenderer shapeRenderer) {

		// Render Lines
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(Color.BLACK);
		for (Link link : map.links) {
			for (Lane lane : link.lanes) {
				renderLine(lane.leftOffset, shapeRenderer);
				renderLine(lane.rightOffset, shapeRenderer);
			}
			//renderLine(link.getCoordinates(), shapeRenderer);
		}
		shapeRenderer.end();

		// Render Circles
		shapeRenderer.begin(ShapeType.Filled);
		// Nodes
		shapeRenderer.setColor(Color.WHITE);
		for (Node node : map.nodes) {
			node.renderDebugPoints(shapeRenderer);
		}
		// Lane centerLine
		shapeRenderer.setColor(Color.RED);
		for (Link link : map.links) {
			for (Lane lane : link.lanes) {
				renderDebugPoints(lane.centerLine, shapeRenderer);
			}
		}

		shapeRenderer.end();
	}

	public static void renderLine(Array<Coordinate> coordinates,
			ShapeRenderer shapeRenderer) {
		for (int i = 0; i < coordinates.size; i++) {
			int next = (i + 1) % coordinates.size;
			if (next != 0)
				shapeRenderer.rectLine(coordinates.get(i).x, coordinates.get(i).y, coordinates.get(next).x,
						coordinates.get(next).y, SimulationParameters.lineThickness.getCurrentVal());
		}
	}

	public static void renderDebugPoints(Array<Coordinate> coordinates,
			ShapeRenderer shapeRenderer) {
		for (int i = 0; i < coordinates.size; i++)
			shapeRenderer.circle(coordinates.get(i).x, coordinates.get(i).y, 0.6f);
	}

	public static void renderDebugVectorPoints(Array<Vector2> vectors,
			ShapeRenderer shapeRenderer) {
		for (int i = 0; i < vectors.size; i++)
			shapeRenderer.circle(vectors.get(i).x, vectors.get(i).y, 0.5f);
	}

}
