/* @description This is the main class for training image-based classifiers.
 *              It currently establishes communication with a JavaScript client ('SimpleAgent').
 *              It also establishes speech-based communication with an Android App via socketServer. 
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

import java.util.Random;
import java.util.Scanner;

import simpleDS.learning.SimpleAgent;
import simpleDS.util.ConfigParser;

public class ImageRecognitionTrainer extends Thread {
	private ConfigParser configParser;
	private Data data;
	private SimpleAgent simpleAgent;
	private ImageSocketServer systemResponseSocketServer;
	private int numPredictions;
	private int numCorrectPredictions;
	private double accuracy;
	
	public ImageRecognitionTrainer(String configFile) {
		configParser = new ConfigParser(configFile);
		configParser.setNumDialogues(null);
		configParser.setVerboseMode(null);

		System.out.println("PixelDataPath="+configParser.getParamValue("PixelDataPath"));

		data = new Data(configParser.getParamValue("PixelDataPath"));
		numPredictions = 0;
		numCorrectPredictions =0;

		initialiseSystemResponseSocketServer();
		this.start();
	}
	
	private void initialiseSystemResponseSocketServer() {
		if (configParser.getParamValue("SocketServerPixels").equals("true")){
			systemResponseSocketServer = new ImageSocketServer(configParser.getParamValue("SocketServerPixelsPort"));
		}
	}
	
	public void run() {
		int numLabels = data.getNumLabels();
		int imageWidth = data.getImageWidth();
		int imageHeigth = data.getImageHeigth();

		System.out.println("numLabels="+numLabels + " imageSize="+imageWidth+"x"+imageHeigth);

		simpleAgent = new SimpleAgent( imageWidth +","+ imageHeigth +","+ numLabels);
		simpleAgent.start();

		synchronized(simpleAgent) {
			try{
				System.out.println("Waiting for SimpleAgent to complete...");
				simpleAgent.wait();
				while ( true ){
					if (simpleAgent.connected == true){
						this.train();
					}
			
				}

			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.exit(0);

	}

	private void train() {
		if (simpleAgent != null) {		
			String state = "";
			Random r = new Random();
			int a = r.nextInt(data.getNumLabels());
			state = data.getData(a);

			String actions = getActions();	
			String rewards = getRewards(a);
			simpleAgent.sendMessage("state="+state+"|actions="+actions+"|rewards="+rewards+"|dialogues="+accuracy);
				
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

			numPredictions++;				
			
			String learnedActionID = (learnedAction.indexOf(":")>0) ? learnedAction.substring(0, learnedAction.indexOf(":")) : learnedAction;
			String predictedLabel = data.getLabels( Integer.valueOf(learnedActionID));

			if (systemResponseSocketServer != null){
				System.out.println( "Result: " + predictedLabel + " Learnt action: " + learnedAction);
				systemResponseSocketServer.send( predictedLabel);

			} else {
				String correctLabel = data.getLabels(a);				
				numCorrectPredictions += (predictedLabel.equals(correctLabel)) ? 1 : 0;
				accuracy = (double) numCorrectPredictions/numPredictions;
				System.out.println( "numPredictions: " + numPredictions + " Labels(pred,corr): " + predictedLabel + " => " + correctLabel  + " accuracy="+accuracy);
			}
		}	
	}


	private String getActions() {
		String output = "";
		for (int i=0; i<data.getNumLabels(); i++) {
			output += (output.equals("")) ? ""+i : ","+i;
		}

		return output;
	}

	public String getRewards(int a) {
		String output = "";
		for (int i=0; i<data.getNumLabels(); i++) {
			String value = (i == a) ? "1" : "0";
			output += (output.equals("")) ? value : ","+value;
		}

		return output;
	}

	public static void main(String[] args) {
		new ImageRecognitionTrainer("config.txt");
	}
}
