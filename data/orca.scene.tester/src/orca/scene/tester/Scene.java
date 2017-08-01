package orca.scene.tester;

import orca.scene.tester.interfaces.Call;

public class Scene {

	/**
	 * Calls needed to setup the scene
	 */
	private Call[] setup;
	
	/**
	 * Calls needed to reset the scene
	 */
	private Call[] reset;
	
	/**
	 * Calls that run the scene
	 */
	private Call[] scene;
	
	// Constructor
	public Scene(Call[] setup, Call[] reset, Call[] scene) {
		this.setup = setup;
		this.reset = reset;
		this.scene = scene;
	}
	
	// Getters
	public Call[] getSetupCalls() {
		return setup;
	}
	
	public Call[] getResetCalls() {
		return reset;
	}
	
	public Call[] getSceneCalls() {
		return scene;
	}
	
	/**
	 * Adds the given calls to the front of the setup calls array 
	 * @param calls			The calls you want to add
	 */
	public void addSetupCallsToFront(Call[] calls) {
		Call[] prevcalls = this.setup;
		
		// Check if empty
		if (this.setup == null || calls == null) {
			this.setup = calls;
			return;
		}
		
		// Setup new array with new length
		this.setup = new Call[prevcalls.length + calls.length];
		
		// Copy values
		System.arraycopy(calls, 0, this.setup, 0, calls.length);
		System.arraycopy(prevcalls, 0, this.setup, calls.length, prevcalls.length);		
	}
 	
}
