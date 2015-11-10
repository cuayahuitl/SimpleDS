/* @description This class creates a SocketServer object for client-server communication.
 *              It has been used to integrate with the Google Speech Recogniser for 
 *              testing realistic human-machine speech-based interactions.
 * 
 * @history 5.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */

package simpleDS.networking;

import java.net.*;
import java.io.*;

import simpleDS.util.Logger;

public class SimpleSocketServer {
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private DataOutputStream writer;
	private DataInputStream reader;
	private int port;

	public SimpleSocketServer(String port) { 
		this.port = Integer.parseInt(port);
	}

	public void createServer() {
		try {
			serverSocket = new ServerSocket(port);
			String helpMsg = "Please click 'Connect' from your Android App!";
			Logger.debug(this.getClass().getName(), "createServer", "Listening on port: " + port + " ... " + helpMsg);
			clientSocket = serverSocket.accept();

		} catch(Exception e) {
			Logger.debug(this.getClass().getName(), "createServer", "Couldn't listen on port: " + port);
			e.printStackTrace();
		}
	}
	
	public String listen() {
		try {
				reader = new DataInputStream(clientSocket.getInputStream());
				return reader.readUTF();

		} catch(Exception e) {
			Logger.debug(this.getClass().getName(), "listen", "Connection lost");
		}
		
		return "null";
	}
	
	public void send(String msg) {
		try {
			if (clientSocket.isConnected() && msg != null && !msg.equals("")) {
				writer = new DataOutputStream(clientSocket.getOutputStream());
				writer.writeUTF(msg);
			}

		} catch(Exception e) {
			Logger.debug(this.getClass().getName(), "send", "Connection lost");
		}		
	}

	public boolean isClientConnected() {
		return (clientSocket.isConnected()) ? true : false;
	}

	public void acceptClient() {
		try {
			clientSocket.close();
			clientSocket = serverSocket.accept();

		} catch(Exception e) {
			Logger.debug(this.getClass().getName(), "acceptClient()", "Couldn't accept client on port: " + port);
			e.printStackTrace();
		}
	}
		
	public void closeServer() {
		try {
			serverSocket.close();
			clientSocket.close();

		} catch(Exception e) {
			Logger.debug(this.getClass().getName(), "closeServer", "Couldn't close on port: " + port);
			e.printStackTrace();
		}
	}
}
