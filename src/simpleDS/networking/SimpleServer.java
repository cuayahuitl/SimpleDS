/* @description This creates a WebServer object for client-server communication.
 * 
 * @history 2.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */
package simpleDS.networking;

import org.eclipse.jetty.server.Server;

public class SimpleServer implements Runnable {
	private SimpleSocketHandler handler;

	public SimpleServer(SimpleSocketHandler handler) {
		this.handler = handler;
		run();
	}

	public void run() {
		try {
			Server server = new Server(8082);
			server.setHandler(handler);
			server.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}
