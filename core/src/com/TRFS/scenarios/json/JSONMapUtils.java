package com.TRFS.scenarios.json;

import java.util.List;

import com.TRFS.scenarios.editor.DoubleCoordinate;
import com.TRFS.scenarios.editor.LinkGeometryUtils;
import com.TRFS.scenarios.map.Coordinate;
import com.TRFS.scenarios.map.Link;
import com.TRFS.scenarios.map.Map;
import com.TRFS.scenarios.map.Node;
import com.TRFS.simulator.MiscUtils;
import com.TRFS.simulator.SimulationParameters;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public class JSONMapUtils {

	public static void makeLinksAndNodes(Map map, FileHandle fileHandle) {

		Json json = new Json();
		List<JSONFeatures> features = json.fromJson(JSONFeatureCollection.class, fileHandle).getFeatures();

		DoubleCoordinate mapRelocation = averageCoordinate(features);

		for (int j = 0; j < features.size(); j++) {
			// Get coordinates from each feature
			List<?> coordinatesList = features.get(j).getGeometry().getCoordinates();

			// Get properties
			JSONProperties properties = features.get(j).getProperties();

			// Create Link with extracted data. Coordinates and attributes are added later.
			Link link = new Link();

			link.setAttributes(j+1, properties.getHierarchy(),
					properties.getInFlow(), properties.getLanes(),
					properties.getMaxspeed(), properties.getOneway(),
					properties.getZ());
			
			//Different altitude layers for overpass modelling.
			if (!map.zLevels.contains(link.z, true)) map.zLevels.add(link.z);
		
			//Add this link to the map object
			map.links.add(link);
			
			// If this link has inflows data, add it to an array for posterity.
			if (link.inFlow > 0) map.inFlowLinks.add(link);

			//Array to store the final float coordinates
			Array<Coordinate> coordinates = new Array<Coordinate>();
			
			for (int k = 0; k < coordinatesList.size(); k++) {
				
				boolean isFirstPoint = k == 0 ? true : false;
				boolean isLastPoint = k == coordinatesList.size() - 1 ? true : false;

				JSONCoordinates jsonCoordinate = new JSONCoordinates(coordinatesList.get(k));
				
				/**Up until this point we've been dealing with doubles, but libGDX works with float coordinates,
				 * so we need to trim each coordinate with the average map coordinate calculated above to avoid
				 * losing precision. If ever there is a need to save the map back to a geoJSON, the average coordinate
				 * should be stored in order to merge it back to the float coordinates.
				 */
				
				float pannedX = (float) (jsonCoordinate.catersianCoordinates.x + mapRelocation.x);
				float pannedY = (float) (jsonCoordinate.catersianCoordinates.y + mapRelocation.y);
				
				Coordinate floatCoordinate = new Coordinate( pannedX, pannedY);
				
				// If it's a first or last point, then it's a node
				if (isFirstPoint || isLastPoint) addNode(map, floatCoordinate, isFirstPoint, link);

				coordinates.add(floatCoordinate);
			}

			// Set link coordinates
			if (SimulationParameters.simplifyGeometry)
				coordinates = LinkGeometryUtils.simplifyGeometry(coordinates, 80f);
			if (SimulationParameters.smoothGeometry)
				coordinates = LinkGeometryUtils.smoothGeometry(coordinates, 4);

			link.setCoordinates(coordinates);
			link.finalizeBuild();
		}
	}


	public static void addNode(Map map, Coordinate coordinates, boolean isFirstPoint, Link sourceLink) {
		boolean isRepeated = false;
		Node finalNode = null;
		
		//Check if node already exists and get a handle to update it
		for (Link link : map.links) {
			if (link.fromNode != null) {
				if (coordinates.equals(link.fromNode.coordinate)) {
					isRepeated = true;
					finalNode = link.fromNode;
				}
			}

			if (link.toNode != null) {
				if (coordinates.equals(link.toNode.coordinate)) {
					isRepeated = true;
					finalNode = link.toNode;
				}
			}
		}

		//If it doesn't exist, create a new one
		if (!isRepeated) {
			//This is a new node, so add it.
			finalNode = new Node(coordinates, map.nodes.size+1, sourceLink);
			map.nodes.add(finalNode);
		}

		//If this node is a first point, then this Link becomes a toLink. If last point, then a fromLink
		if (isFirstPoint) {
			finalNode.toLinks.add(sourceLink);
			sourceLink.fromNode = finalNode;
		} else {
			finalNode.fromLinks.add(sourceLink);
			sourceLink.toNode = finalNode;
		}
	}

	/**
	 * Used to pan the whole map in order to reduce the number of digits
	 * required to store a coordinate.
	 */
	private static DoubleCoordinate averageCoordinate(List<JSONFeatures> features) {

		Array<Double> coordinatesX = new Array<Double>();
		Array<Double> coordinatesY = new Array<Double>();
		
		for (int j = 0; j < features.size(); j++) {
			List<?> coordinatesList = features.get(j).getGeometry().getCoordinates();
			for (int l = 0; l < coordinatesList.size(); l++) {
				
				JSONCoordinates jsonCoordinate = new JSONCoordinates(coordinatesList.get(l));
				
				coordinatesX.add(jsonCoordinate.catersianCoordinates.x);
				coordinatesY.add(jsonCoordinate.catersianCoordinates.y);
			}
		}
		return new DoubleCoordinate(-MiscUtils.averageDouble(coordinatesX),	-MiscUtils.averageDouble(coordinatesY));
	}

}
