package com.TRFS.vehicles;

import com.TRFS.scenarios.Scenario;
import com.TRFS.scenarios.map.Coordinate;
import com.TRFS.simulator.MiscUtils;
import com.TRFS.ui.windows.SimulationStatsWindow;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;

public class VehicleInputProcessor {
	
	private Vehicle clickTempVehicle, tVehicle, sVehicle;
	private Coordinate localMouseClick;
	//private Scenario scenario;
	
	public VehicleInputProcessor(Scenario scenario) {
		//this.scenario = scenario;
		localMouseClick = new Coordinate();
	}
	
	public void listenToInput(){
		
		if (sVehicle != null) {

			sVehicle.physics.throttleInputFwd = Gdx.input.isKeyPressed(Input.Keys.W) ? 1 : 0;
			sVehicle.physics.throttleInputBck = Gdx.input.isKeyPressed(Input.Keys.S) ? 1 : 0;
			sVehicle.physics.steerInputLeft = Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0;
			sVehicle.physics.steerInputRight = Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0;
						
			if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) setSelected(null);
		}
	}
	
	public void setSelected(Vehicle vehicle) {
		if (vehicle != null) {
			sVehicle = vehicle;
			sVehicle.config.color = Color.ORANGE;
			sVehicle.config.userControlled = true;
		} else {
			sVehicle.config.color = sVehicle.config.defaultColor;
			sVehicle.behavior.rellocateAfterUserControlled();
			sVehicle.config.userControlled = false;
			sVehicle = null;
			if (tVehicle != null) setTagged(null);
		}
	}
	
	public void setTagged(Vehicle vehicle) {
		if (vehicle != null) {
			tVehicle = vehicle;
			tVehicle.config.selected = true;
			SimulationStatsWindow.taggedVehicle = vehicle;
		} else {
			tVehicle.config.selected = false;
			tVehicle = null;
		}
	}
	
	public void findClicked(Scenario scenario, float worldX, float worldY) {
		//Loop through layers starting from the topmost one
		for (int i =  scenario.map.zLevels.size - 1; i >= 0; i--) {
			int z = scenario.map.zLevels.get(i);
			for (Vehicle vehicle : scenario.trafficManager.vehicles) {
				if (vehicle.physics.zLevel == z) {
					if (isTouched(vehicle, worldX, worldY)) {
						clickTempVehicle = vehicle;
						break;
					}}}}
	}
	
	public void confirmClicked(float worldX, float worldY) {
		//Is the mouse pointer is still inside the vehicle, confirm the selection
		if (clickTempVehicle != null) {
			if (isTouched(clickTempVehicle, worldX, worldY)) {
				
				if (tVehicle != null) setTagged(null);
				if (sVehicle != null) setSelected(null);
				
				setTagged(clickTempVehicle);
				
				if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) 
					setSelected(clickTempVehicle);
				
				clickTempVehicle = null;
			}
		}
	}
	
	public boolean isTouched(Vehicle vehicle, float worldX, float worldY){
		//Quick test
		if (Math.abs(worldX - vehicle.physics.position.x) > 10 ) return false;
		if (Math.abs(worldY - vehicle.physics.position.y) > 10 ) return false;
		
		//Precision test
		localMouseClick.set(worldX, worldY);
		MiscUtils.globalToLocalOut(localMouseClick, vehicle.physics.heading, vehicle.physics.position);
		
		if (localMouseClick.x > -vehicle.config.width/2
				&& localMouseClick.x < vehicle.config.width/2) {
			if (localMouseClick.y > -vehicle.config.length/2
					&& localMouseClick.y < vehicle.config.length/2) {
				return true;
			}
		}
		return false;
	}
}
