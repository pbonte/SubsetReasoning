package orca.scene.tester;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import orca.scene.tester.interfaces.Call;
import orca.scene.tester.watchdog.WatchdogClient;

import org.json.JSONObject;

import util.httpclient.HttpClient;
import util.httpclient.HttpHeaderField;

public class OrcaApi {

	/**
	 * Amount of loops to run the scene
	 */
	private int loops;
	
	/**
	 * Amount of time between each call
	 */
	private long timeout;
	
	/**
	 * URL of the host running Massif
	 */
	private String url;
	
	/**
	 * The remote port of massif hosting a tcp server
	 */
	private int massifRemotePort;
	
	/**
	 * This is the last id we received from the massif watchdog service
	 */
	private String lastWatchdogId;
		
	/**
	 * Execute the calls in order
	 * @param calls		The calls
	 */
	public void run(String url, int rport, int loops, long timeout, Scene scene) {
		this.url = url;
		this.massifRemotePort = rport;
		
		this.loops = 1;
		if (loops > 0)
			this.loops = loops;
		
		this.timeout = 0;
		if (timeout > 0)
			this.timeout = timeout;
		
		// Setup the scene
		if (scene.getSetupCalls() != null) {
			System.out.println("Setup the scene. Requires " + scene.getSetupCalls().length + " call(s)");
			for (Call setupCall : scene.getSetupCalls()) {
				try {
					executeCall(setupCall);
				} catch (Exception e) {
					System.out.println(e.toString());
					return;
				}
			}
		}
		
		// Loop for the amount that was given up
		for (int i = 0; i < this.loops; i++) {
			System.out.println("Launching loop " + (i + 1));
			
			// Loop every call
			for (Call inf : scene.getSceneCalls()) {
				try {
					executeCall(inf);
					
					// Wait for user input to continue
					//System.out.println("Waiting for user interaction");
					//BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
					//reader.readLine();
				} catch (Exception e) {
					System.out.println(e.toString());
					return;
				}
			}
			
			if (scene.getResetCalls() != null) {
				System.out.println("Reset the scene. Requires " + scene.getResetCalls().length + " call(s)");
				
				// Reset the scene
				for (Call res : scene.getResetCalls()) {
					try {
						executeCall(res);
					} catch (Exception e) {
						System.out.println(e.toString());
						return;
					}
				}
			}
		}
	}
	
	/**
	 * Communicate with the MASSIF platform
	 * @param call			The call you want to execute
	 */
	private void executeCall(Call call) throws Exception {
		URL murl = new URL(this.url);
		
		// Check if we have a fixed timeout
		if (this.timeout > 0) {
			executeCallWithTimeout(call);
			return;
		}
		
		// Connect to the watchdog service of massif and request id
		WatchdogClient client = new WatchdogClient(murl.getHost(), this.massifRemotePort);
		client.waitForReady();
		
		lastWatchdogId = client.registerEvent();
		if (lastWatchdogId == null || lastWatchdogId.isEmpty()) {
			throw new Exception("Socket was not ready or no client id was received from the watchdog service.");
		}
		
		System.out.println("Received id " + lastWatchdogId);
		
		// Prepare json and Send event 
		JSONObject object = new JSONObject(call.e());
		object.put("clientid", lastWatchdogId);
		
		HttpClient.put(this.url, object.toString(), new HttpHeaderField("Content-type", "application/json"));

		// Wait for a response
		boolean reconnect = false;
		boolean success = false;
		
		while (!success) {			
			try {
				// Check if we are reconnecting
				if (reconnect && !client.openEvent(lastWatchdogId)) {
					throw new Exception("Could not reopen the last event on the watchdog service");
				}
				
				client.waitEventFinished();
				client.closeConnection();
				client = null;
				
				// We are done
				success = true;
			} catch (Exception e) {
				reconnect = true;
				
				System.out.println("Client got interrupted / could not connect from watchdog service. Trying to reconnect ..");
				
				if (client != null) {
					client.closeConnection();
					
					// Sleep for a moment so server does not get flooded
					Thread.sleep(1000);
					
					// Reconnect the client with a fresh socket
					client = new WatchdogClient(murl.getHost(), this.massifRemotePort);
					client.waitForReady();
				}				
			}
		}
		
		// If too many tries. Disconnect
		if (!success) {
			throw new Exception("Tries exceeded");
		}
		
		// Sleep so we do not flood the server
		System.out.println("Waiting 1 sec");
		Thread.sleep(500);
	}
	
	/**
	 * Send PUT command to the MASSIF platform
	 * @param call			The call you want to execute
	 */
	private void executeCallWithTimeout(Call call) throws Exception {
		HttpClient.put(this.url, call.e(), new HttpHeaderField("Content-type", "application/json"));

		// Sleep for the given timeout
		Thread.sleep(this.timeout);
	}
	
	/**
	 * CALLS
	 */
	public String call_set_personalrelationship_degree(int value) {
		return "{\"timeStamp\":\"2015-02-16T10:54:05.5602767+01:00\","
				+ "\"data\":{\"e\":[{\"tag\":\"task:simulation\",\"eventType\":\"RelationShipDegree\","
				+ "\"individual\":\"PersonalRelationship_00000000-0000-0001-0001-000000000001\",\"value\":\"" + value + "\"}]}}";
	}
	
	public String call_launch_erik() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-13T16:00:31.3920560+01:00.1.1.101.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_sarah() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.102.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.102.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	
	public String call_launch_george() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.103.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.103.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_nina() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.104.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.104.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_frederik() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.105.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.105.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_bert() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.106.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.106.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_robert() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.107.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.107.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_gilbert() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.108.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.108.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_arthur() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.112.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.112.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_lucas() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.113.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.113.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_luna() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.114.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.114.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_lindsey() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.115.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.115.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_ken() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.116.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.116.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_sander() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.117.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.117.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_jana() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.118.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.118.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_karen() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.119.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.119.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_launch_jun() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:53:50.5218767+01:00.1.1.120.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.120.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
		
	public String call_karel_assistance() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:54:05.5602767+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Care\",\"status\":\"\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Assistance\",\"callID\":\"1.1.101.3.1.00000000-0000-0000-0000-000000000003\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.3.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_karel_assistance_lisa_temp_accept() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"TemporarilyAccepted\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallTemporarilyAccepted\",\"type\":\"Assistance\",\"callID\":\"1.1.101.3.1.00000000-0000-0000-0000-000000000003\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.3.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_karel_assistance_lisa_update_reason() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Medical\",\"tag\":\"task:CallStatus\",\"priority\":\"\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Assistance\",\"callID\":\"1.1.101.3.1.00000000-0000-0000-0000-000000000003\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.3.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_lisa_assistance2() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-16T10:54:05.5602767+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Medical\",\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Assistance\",\"callID\":\"2015-02-16T10:54:05.5602767+01:00.1.1.101.1.1-3\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_marie_assistance2() {
		return "{\"userID\":\"Marie:00000000-0000-0003-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:54:05.5602767+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Care\",\"status\":\"Active\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Assistance\",\"callID\":\"2015-02-16T10:54:05.5602767+01:00.1.1.101.1.1-3\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_karel_temp_accept_assistance_scene() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Care\",\"status\":\"TemporarilyAccepted\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallTemporarilyAccepted\",\"type\":\"Normal\",\"callID\":\"2015-02-13T16:00:33.4044560+01:00.1.1.101.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_lisa_temp_accept_assistance_call() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Care\",\"status\":\"TemporarilyAccepted\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallTemporarilyAccepted\",\"type\":\"Assistance\",\"callID\":\"2015-02-13T16:00:33.4044560+01:00.1.1.101.1.1-2\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_lisa_temp_accept_assistance_call2() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Care\",\"status\":\"TemporarilyAccepted\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallTemporarilyAccepted\",\"type\":\"Assistance\",\"callID\":\"2015-02-13T16:00:33.4044560+01:00.1.1.101.1.1-3\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_marie_temp_accept_assistance_call() {
		return "{\"userID\":\"marie:00000000-0000-0003-0001-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Care\",\"status\":\"TemporarilyAccepted\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallTemporarilyAccepted\",\"type\":\"Assistance\",\"callID\":\"2015-02-13T16:00:33.4044560+01:00.1.1.101.1.1-2\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_marie_temp_accept_assistance_call2() {
		return "{\"userID\":\"marie:00000000-0000-0003-0001-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Care\",\"status\":\"TemporarilyAccepted\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallTemporarilyAccepted\",\"type\":\"Assistance\",\"callID\":\"2015-02-13T16:00:33.4044560+01:00.1.1.101.1.1-3\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_lisa_redirect() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-16T10:54:05.5602767+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Care\",\"status\":\"Redirected\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallRedirected\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:54:05.5602767+01:00.1.1.102.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.102.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_lisa_redirect_medical() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-16T10:54:05.5602767+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Medical\",\"status\":\"Redirected\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallRedirected\",\"type\":\"Normal\",\"callID\":\"2015-02-16T10:54:05.5602767+01:00.1.1.102.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.102.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_lisa_redirect_medical_assistance_call() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-16T10:54:05.5602767+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Medical\",\"status\":\"Redirected\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallRedirected\",\"type\":\"Assistance\",\"callID\":\"2015-02-16T10:54:05.5602767+01:00.1.1.102.1.1-2\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.102.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_karel_temp_accept() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"TemporarilyAccepted\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallTemporarilyAccepted\",\"type\":\"Normal\",\"callID\":\"2015-02-13T16:00:33.4044560+01:00.1.1.101.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_lisa_temp_accept() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"TemporarilyAccepted\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallTemporarilyAccepted\",\"type\":\"Normal\",\"callID\":\"2015-02-13T16:00:33.4044560+01:00.1.1.101.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_karel_update_reason() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Medical\",\"tag\":\"task:CallStatus\",\"priority\":\"\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-13T16:00:33.4044560+01:00.1.1.101.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_lisa_update_reason() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Hotel\",\"tag\":\"task:CallStatus\",\"priority\":\"\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-13T16:00:33.4044560+01:00.1.1.101.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
		
	public String call_karel_temp_accept_room2() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Care\",\"status\":\"TemporarilyAccepted\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallTemporarilyAccepted\",\"type\":\"Normal\",\"callID\":\"2015-02-13T16:00:33.4044560+01:00.1.1.102.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.102.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_karel_temp_accept_hotel() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Hotel\",\"status\":\"TemporarilyAccepted\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallTemporarilyAccepted\",\"type\":\"Normal\",\"callID\":\"2015-02-13T16:00:33.4044560+01:00.1.1.101.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_karel_temp_accept_hotel_room2() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Hotel\",\"status\":\"TemporarilyAccepted\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallTemporarilyAccepted\",\"type\":\"Normal\",\"callID\":\"2015-02-13T16:00:33.4044560+01:00.1.1.102.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.102.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_karel_temp_accept_medical() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-13T16:00:33.404456+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Medical\",\"status\":\"TemporarilyAccepted\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallTemporarilyAccepted\",\"type\":\"Normal\",\"callID\":\"2015-02-13T16:00:33.4044560+01:00.1.1.101.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String call_marie_temp_accept_medical() {
		return "{\"userID\":\"Marie:00000000-0000-0003-0001-000000000001\",\"timeStamp\":\"2015-02-13T16:49:21.8182501+01:00\","
				+ "\"data\":{\"e\":[{\"reason\":\"Medical\",\"status\":\"TemporarilyAccepted\",\"tag\":\"task:CallStatus\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallTemporarilyAccepted\",\"type\":\"Normal\",\"callID\":\"2015-02-13T16:49:21.8182501+01:00.1.1.102.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.102.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}
	
	public String presence_on_marie() {
		return "{\"userID\":\"Marie:00000000-0000-0003-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"on\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
	
	public String presence_on_marie_room2() {
		return "{\"userID\":\"Marie:00000000-0000-0003-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"on\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.102.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
	
	public String login_karel() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"on\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.1337.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
	
	public String logoff_karel() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"off\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.1337.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
	
	public String presence_on_karel() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"on\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
		
	public String presence_on_karel_room2() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"on\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.102.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
	
	public String login_lisa() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"on\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.1337.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
	
	public String logoff_lisa() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"off\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.1337.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
	
	public String presence_on_lisa() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"on\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
	
	public String presence_on_lisa_room2() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"on\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.102.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
	
	public String presence_off_marie() {
		return "{\"userID\":\"Marie:00000000-0000-0003-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"off\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
	
	public String presence_off_marie_room2() {
		return "{\"userID\":\"Marie:00000000-0000-0003-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"off\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.102.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
	
	public String presence_off_karel() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"off\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
	
	public String presence_off_karel_room2() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"off\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.102.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
	
	public String presence_off_lisa() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-16T10:53:42.1602767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"off\",\"tag\":\"wsna:RegistrationButton\",\"eventType\":\"Presence\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.102.5.1\",\"dataSourceType\":\"wsna:RegistrationButton\"}";
	}
	
	public String location_update_marie_corridor() {
		return "{\"userID\":\"Marie:00000000-0000-0003-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:54:20.4496098+01:00\","
				+ "\"data\":{\"e\":[{\"tag\":\"ca:Location\",\"value\":\"00000000-0000-0000-0000-000000000001\",\"eventType\":\"Location\"}]},"
				+ "\"dataSourceID\":\"wsna:788f411e-ea17-44b7-9e2a-44d0877edfcc\",\"dataSourceType\":\"wsna:RFTag\"}";
	}
	
	public String location_update_karel_corridor() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:54:20.4496098+01:00\","
				+ "\"data\":{\"e\":[{\"tag\":\"ca:Location\",\"value\":\"00000000-0000-0000-0000-000000000001\",\"eventType\":\"Location\"}]},"
				+ "\"dataSourceID\":\"wsna:788f411e-ea17-44b7-9e2a-44d0877edfcc\",\"dataSourceType\":\"wsna:RFTag\"}";
	}
	
	public String location_update_lisa_corridor() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-16T10:54:20.4496098+01:00\","
				+ "\"data\":{\"e\":[{\"tag\":\"ca:Location\",\"value\":\"00000000-0000-0000-0000-000000000001\",\"eventType\":\"Location\"}]},"
				+ "\"dataSourceID\":\"wsna:788f411e-ea17-44b7-9e2a-44d0877edfcc\",\"dataSourceType\":\"wsna:RFTag\"}";
	}
	
	public String location_update_marie_patient() {
		return "{\"userID\":\"Marie:00000000-0000-0003-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:54:20.4496098+01:00\","
				+ "\"data\":{\"e\":[{\"tag\":\"ca:Location\",\"value\":\"00000000-0000-0000-0001-000000000001\",\"eventType\":\"Location\"}]},"
				+ "\"dataSourceID\":\"wsna:788f411e-ea17-44b7-9e2a-44d0877edfcc\",\"dataSourceType\":\"wsna:RFTag\"}";
	}
	
	public String location_update_marie_patient_room2() {
		return "{\"userID\":\"Marie:00000000-0000-0003-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:54:20.4496098+01:00\","
				+ "\"data\":{\"e\":[{\"tag\":\"ca:Location\",\"value\":\"00000000-0000-0000-0001-000000000002\",\"eventType\":\"Location\"}]},"
				+ "\"dataSourceID\":\"wsna:788f411e-ea17-44b7-9e2a-44d0877edfcc\",\"dataSourceType\":\"wsna:RFTag\"}";
	}
	
	public String location_update_lisa_patient() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-16T10:54:20.4496098+01:00\","
				+ "\"data\":{\"e\":[{\"tag\":\"ca:Location\",\"value\":\"00000000-0000-0000-0001-000000000001\",\"eventType\":\"Location\"}]},"
				+ "\"dataSourceID\":\"wsna:788f411e-ea17-44b7-9e2a-44d0877edfcc\",\"dataSourceType\":\"wsna:RFTag\"}";
	}
	
	public String location_update_lisa_patient_room2() {
		return "{\"userID\":\"Lisa:00000000-0000-0002-0002-000000000001\",\"timeStamp\":\"2015-02-16T10:54:20.4496098+01:00\","
				+ "\"data\":{\"e\":[{\"tag\":\"ca:Location\",\"value\":\"00000000-0000-0000-0001-000000000002\",\"eventType\":\"Location\"}]},"
				+ "\"dataSourceID\":\"wsna:788f411e-ea17-44b7-9e2a-44d0877edfcc\",\"dataSourceType\":\"wsna:RFTag\"}";
	}
	
	public String location_update_karel_patient() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:54:20.4496098+01:00\","
				+ "\"data\":{\"e\":[{\"tag\":\"ca:Location\",\"value\":\"00000000-0000-0000-0001-000000000001\",\"eventType\":\"Location\"}]},"
				+ "\"dataSourceID\":\"wsna:788f411e-ea17-44b7-9e2a-44d0877edfcc\",\"dataSourceType\":\"wsna:RFTag\"}";
	}
	
	public String location_update_karel_patient_room2() {
		return "{\"userID\":\"Karel:00000000-0000-0002-0001-000000000001\",\"timeStamp\":\"2015-02-16T10:54:20.4496098+01:00\","
				+ "\"data\":{\"e\":[{\"tag\":\"ca:Location\",\"value\":\"00000000-0000-0000-0001-000000000002\",\"eventType\":\"Location\"}]},"
				+ "\"dataSourceID\":\"wsna:788f411e-ea17-44b7-9e2a-44d0877edfcc\",\"dataSourceType\":\"wsna:RFTag\"}";
	}
	
	
	/**
	 * CHANGES MEETING 
	 * @return
	 */
	public String call_launch_erik_pain() {
		return "{\"timeStamp\":\"2015-02-16T10:53:50.5218767+01:00\","
				+ "\"data\":{\"e\":[{\"status\":\"Active\",\"reason\":\"pain\",\"tag\":\"task:Call\",\"priority\":\"Normal\","
				+ "\"eventType\":\"CallLaunched\",\"type\":\"Normal\",\"callID\":\"2015-02-13T16:00:31.3920560+01:00.1.1.101.1.1\"}]},"
				+ "\"dataSourceID\":\"wsna:1.1.101.1.1\",\"dataSourceType\":\"wsna:NurseCallButton\"}";
	}

}
