/* @description This is the main class for implementing SimpleDS learning agents.
 *              It currently establishes communication with a JavaScript client ('SimpleAgent').
 *              This class generates training/test dialogues according to a configuration file,
 *              see README.txt for more information.
 * 
 * @history 2.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */

package simpleDS.main;

import java.util.HashMap;

import simpleDS.interaction.SimpleUserSimulator;
import simpleDS.learning.SimpleAgent;
import simpleDS.learning.SimpleEnvironment;
import simpleDS.util.IOUtil;
import simpleDS.util.Logger;

public class SimpleDS {
	private HashMap<String,String> configurations;
	private SimpleUserSimulator userSimulator;
	private SimpleEnvironment environment;
	private SimpleAgent simpleAgent;
	private boolean verbose = false;

	public SimpleDS(String configFile) {
		parseConfigFile(configFile);
		initialiseEnvironment();

		simpleAgent = new SimpleAgent();
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
		int numDialogues = Integer.parseInt(configurations.get("Dialogues"));
		long numTimeSteps = 0;

		for (int i=1; i<=numDialogues; i++) {
			environment.interactionPolicy.resetUserInfo(userSimulator);
			int steps = 1;
			while(true) {		
				dict.put("action_sys_key", getSystemAction_Key(steps, i, false));
				dict.put("action_sys_val", getSystemAction_Val(dict.get("action_sys_key")));
				dict.put("response_sys", getSystemResponse(dict.get("action_sys_key")));
				if (verbose) {
					String sys = "["+dict.get("action_sys_key")+"] ["+dict.get("action_sys_val")+"] " +dict.get("response_sys");
					Logger.debug("SympleDS", "IM", steps+" SYS:"+sys);
				}

				dict.put("action_usr_key", getUserAction_Key(dict.get("action_sys_key"), dict.get("action_sys_val")));
				dict.put("action_usr_val", getUserAction_Val(dict.get("action_usr_key")));
				dict.put("response_usr", getUserResponse(dict.get("action_usr_key")));
				dict.put("response_asr", getSpeechRecOutput(dict.get("response_usr")));
				if (dict.get("response_usr") != null && verbose) {
					String usr = "["+dict.get("action_usr_key")+"] ["+dict.get("action_usr_val")+"] " +dict.get("response_usr");
					String asr = "["+dict.get("action_usr_key")+"] ["+dict.get("action_usr_val")+"] " +dict.get("response_asr");
					Logger.debug("SympleDS", "IM", steps+" USR:"+usr);
					Logger.debug("SympleDS", "UM", steps+" USR:"+asr);
				}

				environment.interactionPolicy.updateLastInfo(dict, userSimulator);
				if (endOfInteractionReached()) {
					getSystemAction_Key(steps, i, true);
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
			String rewards = environment.interactionPolicy.getRewards(stateWithoutNoise, actions, end);
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
			if (verbose) {
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
		return environment.interactionPolicy.getResponse(action_sys_key);
	}

	private String getUserAction_Key(String action_sys_key, String action_sys_val) {
		return userSimulator.getAction(action_sys_key, action_sys_val);
	}

	private String getUserAction_Val(String action_sys_val) {
		return userSimulator.getAction_Unfolded(action_sys_val);
	}

	private String getUserResponse(String action_usr_key) {
		return userSimulator.getResponse(action_usr_key);
	}

	private String getSpeechRecOutput(String response_usr) {
		return userSimulator.getASROutput(response_usr);
	}

	private void initialiseEnvironment() {
		userSimulator = new SimpleUserSimulator(configurations);
		environment = new SimpleEnvironment(configurations);
	}

	private void parseConfigFile(String configFile) {
		configurations = new HashMap<String,String>();
		IOUtil.readHashMap(configFile, configurations, "=");
		IOUtil.printHashMap(configurations, "CONFIGURATIONS");
		verbose = configurations.get("Verbose").equals("true") ? true : false;
	}

	public static void main(String[] args) {
		new SimpleDS("config.txt");
	}
}
