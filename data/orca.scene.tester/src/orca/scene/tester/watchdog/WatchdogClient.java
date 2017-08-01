package orca.scene.tester.watchdog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class WatchdogClient {
	
	/** Commands */
	private final static int COMMAND_READY = 0x01;
	private final static int COMMAND_TRIGGER = 0x11;
	private final static int COMMAND_REGISTER = 0x21;
	private final static int COMMAND_OPEN = 0x31;
	
	private final static int RESPONSE_OPEN_OK = 0x32;
	private final static int RESPONSE_OPEN_NOTOK = 0x33;
		
	/**
	 * Reader that reads the input stream of data
	 */
	private BufferedReader input;
	
	/**
	 * Writer to write data to the server
	 */
	private PrintWriter output;
	
	/**
	 * The socket to the watchdog service
	 */
	private Socket socket;
		
	/**
	 * Boolean indicating the status of the socket
	 */
	private boolean listening;
	
	// Constructor
	public WatchdogClient(final String host, final int port) {
		try {
			socket = new Socket(host, port);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(), true);
			listening = true;
			
			System.out.println(socket.getLocalSocketAddress().toString() + " connected to " + socket.getRemoteSocketAddress().toString());
		} catch (Exception e) {
			System.out.println(e.toString());
			
			// Try closing the connection
			closeConnection();
		}
	}
	
	/**
	 * Closes the connection from the watchdog service
	 */
	public synchronized void closeConnection() {
		System.out.println("Closing the connection from the massif watchdog service");
		
		listening = false;
		
		if (socket != null) {
			try {
				socket.close();
				input = null;
				output = null;
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
		
		socket = null;
	}
	
	/**
	 * REGISTER command to receive a id for the next job
	 * @return				The id
	 * @throws Exception	Could not communicate
	 */
	public String registerEvent() throws Exception {
		// Check if socket is healty
		if (socket != null && output != null && input != null) {
			// Write command
			output.write(COMMAND_REGISTER);
			output.flush();
			
			// We should receive command back
			int data = input.read();
			if (data != -1 && data != COMMAND_REGISTER) {
				throw new Exception("Command returned was incorrect");
			}
			
			// Read id
			String id = "";
			for (int i = 0; i < 36; i++) {
				data = input.read();
				if (data == -1)
					throw new Exception("Reached end of stream");
				id = id + (char) data;
			}
			
			return id;
		}
		
		return null;
	}
	
	/**
	 * OPEN command to reopen 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public boolean openEvent(String id) throws Exception {
		// Check if socket is healty
		if (!(socket != null && output != null && input != null)) {
			throw new Exception("Connection to watchdog service was interrupted or incorrect.");
		}
		
		// Check if valid id
		if (!(id != null && id.length() == 36)) {
			throw new Exception("Not a valid id.");
		}
		
		// Write command and id to watchdog service
		output.write(COMMAND_OPEN);
		
		// Send id
		for (int i = 0; i < 36; i++) {
			output.write(id.charAt(i));
		}
		
		output.flush();
		
		// Read input
		int data = input.read();
		
		// End of stream
		if (data == -1) {
			throw new Exception("End of stream. Disconnecting");
		}
				
		// Open id was success
		if (data == RESPONSE_OPEN_OK) {
			System.out.println("Connection successfully restored");
			return true;
		}
		
		// Connection is ok, but monitor does not know the given id
		if (data == RESPONSE_OPEN_NOTOK) {
			System.out.println("Given id was not known by the watchdog service. Stop");
			return false;
		}
		
		return false;
	}
	
	/**
	 * Wait till we have received ready command
	 */
	public void waitForReady() throws Exception {
		// Check if socket is healty
		if (!(socket != null && output != null && input != null)) {
			throw new Exception("Connection to watchdog service was interrupted or incorrect.");
		}
		
		boolean wait = true;
		// Wait for the trigger
		while (listening && wait) {
			if (input == null) {
				throw new Exception("Connection was interrupted");
			}
			
			// Check the data
			int data = input.read();
			if (data == -1) {
				throw new Exception("End of stream. Disconnecting");
			} else if (data == COMMAND_READY) {
				wait = false;
			}
		}
	}
	
	/**
	 * Wait till we have received the TRIGGER command
	 */
	public void waitEventFinished() throws Exception {
		// Check if socket is healty
		if (!(socket != null && output != null && input != null)) {
			throw new Exception("Connection to watchdog service was interrupted or incorrect.");
		}
		
		System.out.println("Waiting for watchdog service");
		
		boolean wait = true;
		// Wait for the trigger
		while (listening && wait) {
			if (input == null) {
				throw new Exception("Connection was interrupted");
			}
			
			// Check the data
			int data = input.read();
			if (data == -1) {
				throw new Exception("End of stream. Disconnecting");
			} else if (data == COMMAND_TRIGGER) {
				wait = false;
			}
		}
		
		System.out.println("Watchdog service sent the trigger. Continue");
	}
	
}
