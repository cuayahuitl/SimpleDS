/* @description This is the main class for implementing SimpleDS learning agents.
 *              It currently establishes communication with a JavaScript client ('SimpleAgent').
 *              It also establishes speech-based communication with an Android App via socketServer. 
 *              This class generates training/test dialogues according to a configuration file,
 *              see README.txt for more information.
 * 
 * @history 2.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */

package simpleDS.main;

import java.util.HashMap;

import simpleDS.learning.SimpleAgent;
import simpleDS.learning.SimpleEnvironment;
import simpleDS.networking.SimpleSocketServer;
import simpleDS.util.ConfigParser;
import simpleDS.util.Logger;

public class SimpleDS {
	private ConfigParser configParser;
	private SimpleEnvironment environment;
	private SimpleAgent simpleAgent;
	private SimpleSocketServer socketServer;

	public SimpleDS(String configFile) {
		configParser = new ConfigParser(configFile);
		initialiseEnvironment();
	}
	
	private void initialiseEnvironment() {
		environment = new SimpleEnvironment(configParser.getParams());
		initialiseSocketServer();
		initialiseWebServer();
	}

	private void initialiseSocketServer() {
		if (configParser.getParamValue("AndroidSupport").equals("true")) {
			String port = configParser.getParamValue("SocketServerPort");
			socketServer = new SimpleSocketServer(port);
			socketServer.createServer();
		}
	}
	
	private void initialiseWebServer() {
		simpleAgent = new SimpleAgent(environment.getNumInputOutputs());
		simpleAgent.start();

		synchronized(simpleAgent) {
			try{
				System.out.println("Waiting for SimpleAgent to complete...");
				simpleAgent.wait();
				
				interactionManagement();

			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}

	private void interactionManagement() {
		HashMap<String,String> dict = new HashMap<String,String>();
		configParser.setVerboseMode(simpleAgent.verbose);
		configParser.setNumDialogues(simpleAgent.dialogues);
		long numTimeSteps = 0;

		for (int i=1; i<=configParser.numDialogues; i++) {
			environment.interactionPolicy.resetUserInfo(environment.userSimulator);
			int steps = 1;
			while(true) {		
				dict.put("action_sys_key", getSystemAction_Key(steps, i, false));
				dict.put("action_sys_val", getSystemAction_Val(dict.get("action_sys_key")));
				dict.put("response_sys", getSystemResponse(dict.get("action_sys_key")));
				if (configParser.verbose) {
					String sys = "["+dict.get("action_sys_key")+"] ["+dict.get("action_sys_val")+"] " +dict.get("response_sys");
					Logger.debug("SympleDS", "IM", steps+" SYS:"+sys);
				}

				dict.put("action_usr_key", getUserAction_Key(dict.get("action_sys_key"), dict.get("action_sys_val")));
				dict.put("response_usr", getUserResponse(dict.get("action_usr_key")));
				dict.put("response_asr", getSpeechRecOutput(dict.get("response_usr")));
				dict.put("action_usr_val", getUserAction_Val(dict.get("action_usr_key")));
				if (dict.get("response_usr") != null && configParser.verbose) {
					String usr = "["+dict.get("action_usr_key")+"] ["+dict.get("action_usr_val")+"] " +dict.get("response_usr");
					String asr = "["+dict.get("action_usr_key")+"] ["+dict.get("action_usr_val")+"] " +dict.get("response_asr");
					Logger.debug("SympleDS", "IM", steps+" USR:"+usr);
					Logger.debug("SympleDS", "UM", steps+" USR:"+asr);
				}

				environment.interactionPolicy.updateLastInfo(dict, environment.userSimulator);
				if (endOfInteractionReached()) {
					getSystemAction_Key(steps, i, true);
					if (configParser.verbose) System.out.print("\n\n");
					break;
				}
				steps++;
			}
			numTimeSteps += steps;

			if (i%100 == 0) {
				double avgTimeSteps = (double) numTimeSteps/i;
				Logger.debug(this.getClass().getName(), "InteractionManagement", "dialogues="+i + " turns="+avgTimeSteps);
			}
		}
	}

	private boolean endOfInteractionReached() {
		String stateWithoutNoise = environment.getEnvironmentState(false);
		String bestAction = ""+environment.interactionPolicy.simpleActions.getMostProbableAction(stateWithoutNoise);
		String endingAction = ""+environment.interactionPolicy.simpleActions.getActionSetSize();
		return (bestAction.equals(endingAction)) ? true : false;
	}

	private String getSystemAction_Key(int steps, int dialogues, boolean end) {
		String stateWithNoise = environment.getEnvironmentState(true);
		String stateWithoutNoise = environment.getEnvironmentState(false);
		String actions = environment.interactionPolicy.getAllowedActions(stateWithoutNoise, steps);

		if (simpleAgent != null) {
			String rewards = environment.interactionPolicy.getRewards(stateWithoutNoise, actions, end, steps);
			simpleAgent.sendMessage("state="+stateWithNoise+"|actions="+actions+"|rewards="+rewards+"|dialogues="+dialogues);
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
			String action = environment.interactionPolicy.simpleActions.getAction(learnedAction);
			if (configParser.verbose) {
				String reward = environment.interactionPolicy.getReward(rewards, learnedAction);
				Logger.debug(this.getClass().getName(), "", "s="+stateWithNoise + " A="+actions + " a="+action + " r="+reward);
			}
			return action;
			
		} else {
			String randomAction = environment.interactionPolicy.simpleActions.getRandomAction(actions);
			String action = environment.interactionPolicy.simpleActions.getAction(randomAction);
			return action;
		}
	}

	private String getSystemAction_Val(String action_sys_key) {
		return environment.interactionPolicy.getAction_Unfolded(action_sys_key);
	}

	private String getSystemResponse(String action_sys_key) {
		String response = environment.interactionPolicy.getResponse(action_sys_key);
		
		if (socketServer != null && response != null){
			socketServer.send(response);
		}
		
		return response;
	}

	private String getUserAction_Key(String action_sys_key, String action_sys_val) {
		return environment.userSimulator.getAction(action_sys_key, action_sys_val);
	}

	private String getUserAction_Val(String action_sys_val) {
		return environment.userSimulator.getAction_Unfolded(action_sys_val);
	}

	private String getUserResponse(String action_usr_key) {
		String response = environment.userSimulator.getResponse(action_usr_key);
		
		if (socketServer != null && response != null){
			response = socketServer.listen();
		}
		
		return response;
	}

	private String getSpeechRecOutput(String response_usr) {
		if (socketServer != null){
			String response_asr = environment.getRealSpeechRecognitionOutput(response_usr);
			environment.userSimulator.updateUserGoal(response_asr);
			return response_asr;
			
		} else {
			return environment.getSimulatedSpeechRecognitionOutput(response_usr);
		}
	}

	public static void main(String[] args) {
		new SimpleDS("config.txt");
	}
}