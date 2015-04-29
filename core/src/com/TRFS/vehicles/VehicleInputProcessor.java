package com.TRFS.vehicles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class VehicleInputProcessor {
	
	private static Vehicle cVehicle;
	private Vector2 tmp1;

	private static float accelMagnitude = 0;
	
	public VehicleInputProcessor() {
		tmp1 = new Vector2();
	}
	
	public void listenToInput(){
		
		if (cVehicle != null) {
			boolean travelingFWD = cVehicle.fwdDirection.dot(cVehicle.velocity) > 0 ? true : false;
			
			if(Gdx.input.isKeyPressed(Input.Keys.W)){
				if (travelingFWD) accelMagnitude = 5;
				if (!travelingFWD) accelMagnitude = 10;
			}
			
			if(Gdx.input.isKeyPressed(Input.Keys.S)) {
				if (travelingFWD) accelMagnitude = 10;
				if (!travelingFWD) accelMagnitude = -4;
			}
			
			if(Gdx.input.isKeyPressed(Input.Keys.A)) cVehicle.angularAcceleration += 1;
			if(Gdx.input.isKeyPressed(Input.Keys.D)) cVehicle.angularAcceleration -= 1;
			
			if(!Gdx.input.isKeyPressed(Input.Keys.W) && !Gdx.input.isKeyPressed(Input.Keys.S)) MathUtils.lerp(accelMagnitude, 0, 1f);
			if(!Gdx.input.isKeyPressed(Input.Keys.A) && !Gdx.input.isKeyPressed(Input.Keys.D)) MathUtils.lerp(cVehicle.angularAcceleration, 0, 1f);
						
			
			tmp1.set(cVehicle.fwdDirection).nor();
			tmp1.scl(accelMagnitude);
			cVehicle.acceleration.set(tmp1);
			
			if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) setVehicle(null);
		}
			
	}
	
	public static void setVehicle(Vehicle vehicle) {
		if (vehicle != null) {
			cVehicle = vehicle;
			cVehicle.setColor(Color.ORANGE);
			accelMagnitude = cVehicle.getAccelMagnitude();
		} else {
			cVehicle.setColor(cVehicle.defaultColor);
			cVehicle.setUserControlled(false);
			cVehicle = null;
		}
	}

}
