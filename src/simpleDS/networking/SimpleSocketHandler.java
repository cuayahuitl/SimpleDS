/* @description This class implements a WebSocket handler for client-server communication.
 * 
 * @history 2.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */

package simpleDS.networking;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import simpleDS.util.Logger;

@WebSocket
public class SimpleSocketHandler extends WebSocketHandler {
	private static Session session = null;
	private static MessageHandler messageHandler;
	public boolean connected = false;

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		Logger.debug(this.getClass().getName(), "onClose", "statusCode=" + statusCode + ", reason=" + reason);
	}

	@OnWebSocketError
	public void onError(Throwable t) {
		Logger.error(this.getClass().getName(), "onError", "Error: " + t.getMessage());
	}

	@OnWebSocketConnect
	public void onConnect(Session session) {
		Logger.debug(this.getClass().getName(), "onConnect", "Connect: " + session.getRemoteAddress().getAddress());
		try {
			this.session = session;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("static-access")
	@OnWebSocketMessage
	public void onMessage(String msg) {
		try { 
			if (this.messageHandler != null) {
				this.messageHandler.handleMessage(msg);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("static-access")
	public void sendMessage(String msg) {
		try { 
			if (this.session != null) {
				session.getRemote().sendString(msg);
				
			} else {
				Logger.debug(this.getClass().getName(), "sendMessage", "Undelivered message ... session is NULL");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.register(this.getClass());
	}

	@SuppressWarnings("static-access")
	public void addMessageHandler(MessageHandler msgHandler) {
		this.messageHandler = msgHandler;
	}

	public static interface MessageHandler {
		public void handleMessage(String features);
	}
}