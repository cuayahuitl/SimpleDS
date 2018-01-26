/* @description This is the main class for testing image-based classifiers.
 *              It currently establishes communication with a JavaScript client ('SimpleAgent').
 *              It also establishes speech-based communication with an Android App via imageSocketServer. 
 *              This class generates training/test dialogues according to a configuration file,
 *              see README.txt for more information.
 * 
 * @history 15.Aug.2017 Beta version
 * 
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 * @author <ahref="mailto:couly.guillaume@gmail.com">Guillaume Couly</a>
 * @author <ahref="mailto:clement.olalainty@gmail.com">Clement Olalanty</a>
 */

package simpleDS.pixels;

import java.util.*;

import simpleDS.learning.SimpleAgent;
import simpleDS.util.ConfigParser;

public class ImageRecognitionTester extends Thread {
	private ConfigParser configParser;
	private Data data;
	private SimpleAgent simpleAgent;
	private ImageSocketServer imageSocketServer;
	private String dataPath;
	
	public ImageRecognitionTester(String configFile) {
		configParser = new ConfigParser(configFile);
		configParser.setNumDialogues(null);
		configParser.setVerboseMode(null);
		dataPath = configParser.getParamValue("PixelDataPath");

		imageSocketServer = new ImageSocketServer("9100");
		this.start();
	}
	
	public void run() {
		data = new Data(dataPath);
		int nbLabel = data.getNumLabels();
		int imageWidth = data.getImageWidth();
		int imageHeigth = data.getImageHeigth();

		System.out.println("nbLabel="+nbLabel + " sizeImage="+imageWidth+"x"+imageHeigth);
		
		simpleAgent = new SimpleAgent( imageHeigth +","+ imageWidth +","+ nbLabel);
		simpleAgent.start();

		synchronized(simpleAgent) {
			try{
				System.out.println("Waiting for SimpleAgent to complete...");
				simpleAgent.wait();
				System.out.println("SimpleAgent connected!");
				while (true) {
					Thread.sleep(50);
					System.out.println("Listening for client requests");
					String states = getFeatures();
					String actions = getActions();
					String rewards = getRewards();
					String responses = "";

					StringTokenizer toks = new StringTokenizer(states, "|");
					while (toks.hasMoreTokens()) {
						String state = toks.nextToken();
						String message = "state="+state+"|actions="+actions+"|rewards="+rewards;
						simpleAgent.sendMessage(message);
						String response = getResponse();
						System.out.println("message:("+message + ") response="+response); 
						responses += (responses.equals("") ? response : "|"+response);
					}
					imageSocketServer.send(responses);
				}

			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.exit(0);

	}

	public String getFeatures() {
		String state = imageSocketServer.listen();
		while ( state == null ){
			state = imageSocketServer.listen();
			try {
				Thread.sleep(50);
	
			} catch (InterruptedException e) { 
				e.printStackTrace();	
			}
		}

		return state;
	}

	private String getActions() {
		String output = "";
		for (int i=0; i<data.getNumLabels(); i++) {
			output = (output.equals("")) ? ""+i : ","+i;
		}

		return output;
	}

	private String getRewards() {
		String output = "";
		for (int i=0; i<data.getNumLabels(); i++) {
			output = (output.equals("")) ? "0" : ",0";
		}

		return output;
	}

	private String getResponse() {
		String learnedAction = null;
		while (learnedAction == null) {
			try {
				Thread.sleep(1);
				learnedAction = simpleAgent.getLastAction();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		simpleAgent.setLastAction(null);

		System.out.println("learnedAction="+learnedAction);
		String actionID = learnedAction.substring(0, 1);
		System.out.println("actionID="+actionID);
		String prob = learnedAction.substring(2);
		System.out.println("prob="+prob);
		String action = data.getLabels(Integer.valueOf(actionID));
		System.out.println("action="+action);
		return action+" "+prob;
	}

	public static void main(String[] args) {
		new ImageRecognitionTester("config.txt");
	}
}
