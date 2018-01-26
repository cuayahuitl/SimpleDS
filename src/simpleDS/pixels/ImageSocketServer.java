/* @description This class creates a SocketServer object for client-server communication.
 *              It has been used to integrate with the Google Speech Recogniser for 
 *              testing realistic human-machine speech-based interactions.
 * 
 * @history 15.Aug.2017 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 * @author <ahref="mailto:couly.guillaume@gmail.com">Guillaume Couly</a>
 * @author <ahref="mailto:clement.olalainty@gmail.com">Clement Olalanty</a>
 */

package simpleDS.pixels;

import java.net.*;
import java.io.*;

import simpleDS.util.Logger;

public class ImageSocketServer extends Thread{
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private DataOutputStream writer;
	private DataInputStream reader;
	private BufferedReader input;
	private int port;
	private boolean connected = false;

	public ImageSocketServer(String port) { 
		this.port = Integer.parseInt(port);
		this.start();
	}

	public void run() {
		while (true){
			try {
				serverSocket = new ServerSocket(port);	
				System.out.println( "Connecting ...");
				clientSocket = serverSocket.accept();
				System.out.println("Connected ...");
				
				input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	
				connected = true;
				while ( connected ){
					this.sleep(100);
				}

				clientSocket.close();
				serverSocket.close();
				
			} catch(Exception e) {
				e.printStackTrace();
				try {
					clientSocket.close();
					serverSocket.close();
				} catch (IOException e1) {}
				connected = false;
			}
		}
	}
	
	public boolean getWaitState() {
		String msg = null; 
		System.out.println("Listen");
		msg = this.listen();
		System.out.println("End listen");
		System.out.println( "Message : " + msg);
		if ( msg.equals("end" ) )
			return true;
		
		return false;
		
	}
	
	public String listen() {
		try {
			String msg = input.readLine();
			if (msg == null) {
				connected = false;
			}		
			return msg;

		} catch(Exception e) {
			connected = false;
			return null;
		}		
	}
	
	public void send(String msg) {
		try {
			if (clientSocket.isConnected() && msg != null && !msg.equals("")) {
				writer = new DataOutputStream(clientSocket.getOutputStream());
				writer.write(msg.getBytes());
				System.out.println("msg="+msg);
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
			Logger.debug(this.getClass().getName(), "acceptClient", "Couldn't accept client on port: " + port);
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
