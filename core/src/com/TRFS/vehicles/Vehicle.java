package com.TRFS.vehicles;

import com.TRFS.models.Behavior;
import com.TRFS.models.InFlowsManager;
import com.TRFS.simulator.SimulationParameters;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;


/**
 * Class holding the physical characteristics and mechanics of a vehicle. Decisions
 * are made by the Behavior class.
 * 
 * @author jgamboa
 */

public class Vehicle {

	// Properties
	public VehiclePhysics physics;
	public VehicleConfig config;
	public Behavior behavior;
	public VehicleStats stats;
	
	public int id;
		
	//Debug
	public Vector2 targetPos = new Vector2();
	
	/**Creates a new vehicle.
	 * @param width The width of the vehicle
	 * @param length The length of the vehicle
	 * @param color The color of the vehicle
	 * @param textureName The name of the texture to use
	 */
	public Vehicle(float width, float length, float mass, Color defaultColor) {
		
		this.config = new VehicleConfig(this, width, length, mass, defaultColor);
		this.physics = new VehiclePhysics(this);
		
		this.id = InFlowsManager.nextVehicleID++;				
		this.behavior = new Behavior(this,
				SimulationParameters.currentCarFolModel,
				SimulationParameters.currentLaneChangeModel);
		
		this.stats = new VehicleStats(this);
	}

	/**
	 * Updates the vehicle behaviour, acceleration, velocity and position. Called every frame.
	 * @param delta time 
	 */
	public void update(float delta) {

		// AI Behaviour
		if (config.userControlled) {
			physics.updateUserInput(delta);
		} else {
			behavior.update(delta);
		}
		stats.update(delta);
	}

	public void draw(Batch batch) {
	};
	
	protected void draw(Batch batch, TextureRegion vehicle, TextureRegion leftBlinker,
			TextureRegion rightBlinker, TextureRegion redGlow, TextureRegion whiteGlow) {
		
		float x = config.vertices.get(0).x;
		float y = config.vertices.get(0).y;
		float rotation = physics.heading * MathUtils.radDeg;
		
		if (config.selected) batch.draw(whiteGlow, x, y,  0, 0, config.width, config.length, 1, 1, rotation);
		if (config.userControlled) batch.draw(redGlow, x, y,  0, 0, config.width, config.length, 1, 1, rotation);
		
		batch.setColor(Color.BLUE);
		batch.draw(vehicle, x, y, 0, 0, config.width, config.length, 1, 1, rotation);
		
		batch.setColor(Color.WHITE);
		
		if (!config.userControlled) {
			if (behavior.laneChangingBehaviour.desireToChange) {
				if (behavior.laneChangingBehaviour.targetLaneIndex > behavior.laneChangingBehaviour.currentLaneIndex)
					batch.draw(leftBlinker, x, y, 0, 0, config.width, config.length, 1, 1, rotation);
				else 
					batch.draw(leftBlinker, x, y, 0, 0, config.width, config.length, 1, 1, rotation);
			}
		}
	}
	
	public void drawVehicleDebug(ShapeRenderer renderer) {
		renderer.begin(ShapeType.Line);
		
		/*Vehicle lead = behavior.carFollowingBehaviour.leader;
		if (lead != null) {
			renderer.setColor(Color.RED);
			renderer.line(physics.position.x,  physics.position.y, lead.physics.position.x,  lead.physics.position.y);
		}
		
		Vehicle last = behavior.laneChangingBehaviour.rearOnTargetLane;
		if(last != null) {
			renderer.setColor(Color.YELLOW);
			renderer.line(physics.position.x,  physics.position.y, last.physics.position.x,  last.physics.position.y);
		}
		
		Vehicle next = behavior.laneChangingBehaviour.frontOnTargetLane;
		if(next != null) {
			renderer.setColor(Color.ORANGE);
			renderer.line(physics.position.x,  physics.position.y, next.physics.position.x,  next.physics.position.y);
		}*/
		
		renderer.end();
		/*for (int i = 0; i < config.vertices.size - 1; i++) {
			renderer.line(config.vertices.get(i), config.vertices.get(i+1));
		}
		renderer.line(config.vertices.peek(), config.vertices.first());	
		*/
		
		renderer.begin(ShapeType.Filled);		
		/*renderer.setColor(Color.BLUE);
		renderer.circle(physics.position.x, physics.position.y, 0.3f);*/
		renderer.setColor(Color.YELLOW);
		renderer.circle(targetPos.x, targetPos.y, 0.3f);
		renderer.end();
	}
			
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vehicle other = (Vehicle) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public class VehiclePhysics {

		public int throttleInputFwd, throttleInputBck, steerInputLeft, steerInputRight, movingFwd, zLevel; // 0, 1 (for user input)
		public float throttle, brake, steer; //Percentage of max force
		public float steerAngle, heading, speed, acceleration; //Actual values
		
		public Vector2 position, frontWPos, rearWPos, deltaPosition, frontAxisPostion;

		public VehicleConfig config;
		public Vehicle vehicle;
		
		public VehiclePhysics(Vehicle vehicle) {
			this.vehicle = vehicle;
			this.config = vehicle.config;
			this.frontWPos = new Vector2();
			this.rearWPos = new Vector2();
			this.deltaPosition = new Vector2();
			this.position = new Vector2();
			this.frontAxisPostion = new Vector2();
		}
				
		private void updatePhysics(float delta) {
			
			float engineForce = throttle * VehicleConfig.engineForce;
			float brakeForce = brake * VehicleConfig.brakeForce * (speed > 0 ? -1: 1);
			
			float traction = engineForce + brakeForce;
			float drag = (-VehicleConfig.rollDragConst * speed) + (- VehicleConfig.airDragConst * speed * speed);
			
			acceleration = MathUtils.clamp((traction + drag) / config.mass, -VehicleConfig.maxLinearAcceleration, VehicleConfig.maxLinearAcceleration);	
			speed = MathUtils.clamp(acceleration * delta + speed, -VehicleConfig.maxLinearSpeed, VehicleConfig.maxLinearSpeed);
						
			if (Math.abs(speed) < 0.5 && engineForce == 0) speed = 0;
			
			float dXY = speed * delta;
					
			frontWPos.set(0,config.wheelBase/2).add(dXY * (float) Math.sin(-steerAngle), dXY * (float) Math.cos(-steerAngle));
			rearWPos.set(0,-config.wheelBase/2).add(0, dXY);
			
			deltaPosition.set((frontWPos.x + rearWPos.x)/2, (frontWPos.y + rearWPos.y)/2).rotateRad(heading);
			heading -= (float) Math.atan2(frontWPos.x - rearWPos.x, frontWPos.y - rearWPos.y);
			heading %= MathUtils.PI2;
			
			position.add(deltaPosition);
			
			//Other Properties
			frontAxisPostion.set(frontWPos).rotateRad(heading).add(position);
			movingFwd = speed > 0 ? 1 : speed == 0 ? 0 : -1;
			
			config.updateVertices(heading, position);
		}
				
		public void updateAI(float delta, float throttle, float brake, float steerAngle) {
			float throttleMultiplier = 1f, brakeMultiplier = 10f, steerMultiplier = 0.8f;
			
			this.steerAngle = MathUtils.clamp(steerMultiplier * steerAngle * delta
					+ this.steerAngle, -VehicleConfig.maxSteeringAngle,
					VehicleConfig.maxSteeringAngle) /* (1 - Math.abs(speed)/280)*/;
			if (Math.abs(steerAngle) < 0.1) this.steerAngle = steerAngle;
			
			if (throttle > 0) {
				this.throttle = (float) (MathUtils.clamp(throttleMultiplier * throttle
						* delta + this.throttle, -1, 1) * (1 - Math.abs(steerAngle)*0.2));
			} else MathUtils.lerp(this.throttle, 0, 0.2f);
			
			if (brake > 0) {	
				this.brake = MathUtils.clamp(brakeMultiplier * brake * delta
						+ this.brake, -1, 1);
			} else this.brake = 0;
			
			
			updatePhysics(delta);
		}
		
		private void updateUserInput(float delta) {
			
			//Steering
			int steerInput = steerInputLeft - steerInputRight; //Negative if steering right, positive if steering left.
			
			float steerMult = 0.8f;
			if (Math.abs(steerInput) != 0) {
				steer = MathUtils.clamp(steerInput * delta * steerMult + steer, -1, 1) * (1 - Math.abs(speed)/280);
			} else { 
				steer = MathUtils.lerp(steer, 0, delta * steerMult * 5);
				if (Math.abs(steer) < 0.001) steer = 0;
			}

			//Throttle
			float throttleMultiplier = 1f, brakeMultiplier = 15f;
			int throttleDir =  throttleInputFwd - throttleInputBck; //Defines the direction of the engine or brake force
			int throttleMult = movingFwd * throttleDir; //Defines if the vehicle is braking (-1), accelerating (1) or free (0)
			
			if (Math.abs(movingFwd) != 0) {
				if (throttleMult > 0) {
					throttle = MathUtils.clamp(throttleDir * throttleMultiplier * delta + throttle, -1, 1);
				} else if (throttleMult < 0) {
					brake = MathUtils.clamp(brakeMultiplier * delta + brake, -1, 1);
				} else {
					throttle = brake = 0;
				}
			} else {
				throttle = MathUtils.clamp(throttleDir * throttleMultiplier * delta, -1, 1);
				brake = 0;
			}

			steerAngle = steer * VehicleConfig.maxSteeringAngle;
			updatePhysics(delta);
		}
						
		/** Sets the position of the vehicle to a given vector with a given heading.
		 * @param position The {@link Vector2} holding the position.
		 */
		public void setLocation(Vector2 position, float heading) {
			this.position.set(position.x, position.y);
			this.heading = heading;
			this.config.updateVertices(heading, position);
		}
	}
	
	public class VehicleConfig {
		
		public boolean selected, userControlled;
		public Color defaultColor, color;
		
		public float width, length, mass, wheelBase;
		public Array<Vector2> vertices;
		
		public static final float engineForce = 10000, brakeForce = 40000, airDragConst = 8f, rollDragConst = 12.8f;//(N)
		public static final float maxLinearSpeed = 40, maxLinearAcceleration = 8, /*(m/s)*/ maxSteeringAngle = 45 * MathUtils.degRad; /*Rad*/
							
		public VehicleConfig(Vehicle vehicle, float width, float length, float mass, Color color) {
			
			this.width = width;	this.length = length; this.mass = mass;
			this.defaultColor = color; this.color = color;
			this.wheelBase = 0.6f * length;
						
			this.vertices = new Array<Vector2>();
			for (int i = 0; i < 4; i++) {
				this.vertices.add(new Vector2());
			}
		}
		
		public void updateVertices(float rotation, Vector2 origin) {
			vertices.get(0).set(-width/2,-length/2).rotateRad(rotation).add(origin);
			vertices.get(1).set(width/2,-length/2).rotateRad(rotation).add(origin);
			vertices.get(2).set(width/2,length/2).rotateRad(rotation).add(origin);
			vertices.get(3).set(-width/2,length/2).rotateRad(rotation).add(origin);			
		}	
	}
	
	public class VehicleStats {
		
		public float distanceTravelled, travelTime, brakingTime, accelerationTime, stoppedTime, desireToChangeLaneTime, speed, acceleration;
		public int vehicleID, originLinkID, destinationLinkID, currentLinkID;
		public String type;
		
		public Array<Integer> counterRecord;
		
		public Vehicle vehicle;
		
		public VehicleStats(Vehicle vehicle) {
			this.vehicle = vehicle;
			this.vehicleID = id;

			this.counterRecord = new Array<Integer>();
			
			if (vehicle instanceof Car) this.type = "Car";
			if (vehicle instanceof Truck) this.type = "Truck";
			
		}
		
		public void update(float delta) {
			distanceTravelled += vehicle.physics.deltaPosition.len();
			travelTime += delta;
			
			if (vehicle.physics.brake > 0) brakingTime += delta;
			if (vehicle.physics.acceleration > 0) accelerationTime += delta;
			if (vehicle.physics.speed < 0.1) stoppedTime += delta;
			if (vehicle.behavior.laneChangingBehaviour.desireToChange) desireToChangeLaneTime += delta;
		}
		
		public void snapShot() {
			speed = vehicle.physics.speed;
			acceleration = vehicle.physics.acceleration;
			currentLinkID = vehicle.behavior.pathFollowing.currentLink().id;
			originLinkID = vehicle.behavior.pathFollowing.linkSequence.first().id;
			destinationLinkID = vehicle.behavior.pathFollowing.linkSequence.peek().id;
		}	
	}
}


