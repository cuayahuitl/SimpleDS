/* @description This class implements the methods to control the interaction of SimpleDS agents.
 *              It keeps track of requested and confirmed slots and a history of selected actions.
 *              
 * @history 2.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */

package simpleDS.interaction;

import java.util.ArrayList;
import java.util.HashMap;

import simpleDS.util.IOUtil;
import simpleDS.util.Logger;
import simpleDS.util.StringUtil;

public class SimpleInteractionPolicy {
	private HashMap<String,String> lastInfo;
	public HashMap<String,Integer> historic;
	private HashMap<String,String> slotsRequested;
	private HashMap<String,String> slotsConfirmed;
	public SimpleActions simpleActions;
	private boolean verbose = false;
	private int numSlots2Confirm = 0;
	//private int numSlotsConfirmed = 0;
	private float noiseLevel = 0;

	public SimpleInteractionPolicy(HashMap<String,String> configurations) {
		verbose = configurations.get("Verbose").equals("true") ? true : false;
		this.numSlots2Confirm = Integer.parseInt(configurations.get("SlotsToConfirm"));
		this.noiseLevel = Float.parseFloat(configurations.get("NoiseLevel"));
		
		String sysResponsesFile = configurations.get("SysResponses");
		simpleActions = new SimpleActions(sysResponsesFile);
		
		slotsRequested = new HashMap<String,String>();
		slotsConfirmed = new HashMap<String,String>();
		lastInfo = new HashMap<String,String>();
		historic = new HashMap<String,Integer>();
		initialiseSlots();
	}

	public void initialiseSlots() {
		this.slotsConfirmed = new HashMap<String,String>();
		this.slotsRequested = new HashMap<String,String>();
		this.lastInfo = new HashMap<String,String>();
		updateLastInfo(new HashMap<String,String>(), null);
		this.historic = new HashMap<String,Integer>();
		this.simpleActions.resetSelectedActions();
		//this.numSlotsConfirmed = 0;
	}

	public String getAction_Unfolded(String action) {
		return StringUtil.getExpandedDialAct(action, slotsRequested);
	}

	public String getResponse(String action) {
		String response = simpleActions.dialActs.get(action);
		response = StringUtil.getExpandedTemplate(response, slotsRequested);
		return response;
	}

	public void setSlots(String lastUsrAction) {
		String lastSysAction = lastInfo.get("LastSysAction_Val");
		
		if (lastSysAction.indexOf("Confirm")>=0) {
			String slotValuePairs = lastSysAction.substring(lastSysAction.indexOf("(")+1, lastSysAction.indexOf(")"));
			for (String pair : StringUtil.getArrayListFromString(slotValuePairs, ",")) {
				String slot = pair.indexOf("=")>0 ? pair.substring(0, pair.indexOf("=")) : pair;
				this.slotsConfirmed.put("$"+slot, "confirmed");
			}

		} else if (lastUsrAction != null && !lastUsrAction.equals("")) {
			HashMap<String,Float> lastConfScores = getLastConfidenceScores();
			String pairs = lastUsrAction.substring(lastUsrAction.indexOf("(")+1, lastUsrAction.indexOf(")"));
			for (String pair : StringUtil.getArrayListFromString(pairs, ",")) {
				String key = pair.substring(0, pair.indexOf("="));
				String val = pair.substring(pair.indexOf("=")+1);
				double confidence = getSpeechRecConfidence(val, lastConfScores);
				val = confidence <= noiseLevel ? "other" : val; 
				this.slotsRequested.put("$"+key, val);
			}
		}
	}

	public HashMap<String,Float> getLastConfidenceScores() {
		String lastASROutput = lastInfo.get("LastASROutput");
		HashMap<String,Float> lastConfScores = new HashMap<String,Float>();
		
		for (String tuple : StringUtil.getArrayListFromString(lastASROutput, " ")) {
			String word = tuple.substring(0, tuple.indexOf("(")).toLowerCase();
			String score = tuple.substring(tuple.indexOf("(")+1, tuple.indexOf(")"));
			lastConfScores.put(word, new Float(score));
		}

		return lastConfScores;
	}

	public double getSpeechRecConfidence(String wordList, HashMap<String,Float> confidenceScores) {
		double confidence = 0;

		for (String word : StringUtil.getArrayListFromString(wordList, " ")) {
			if (confidenceScores.get(word) != null) {
				confidence += confidenceScores.get(word).floatValue();
			}
		}

		return confidence;
	}

	public void setHistoricInfo(String key) {
		if (key != null && !key.equals("")) {

			if (key.indexOf(",")>0) {
				String dat = key.substring(0, key.indexOf("("));
				String slotValues = key.substring(key.indexOf("(")+1, key.indexOf(")"));
				for (String slotValue : StringUtil.getArrayListFromString(slotValues, ",")) {
					String subkey = dat +"("+ slotValue + ")";
					IOUtil.incrementHashTable(historic, subkey, 1);
				}		

			}

			IOUtil.incrementHashTable(historic, key, 1);
			this.simpleActions.setSelectedAction(key);
		}
	}

	public Integer getHistoricInfo(String key) {
		return historic.get(key);
	}

	public void resetUserInfo(SimpleUserSimulator userSimulator) {
		userSimulator.resetUserGoal();
		this.initialiseSlots();
	}

	public void resetFilledSlots(String lastSysAction) {
		String pairs = lastSysAction.substring(lastSysAction.indexOf("(")+1, lastSysAction.indexOf(")"));
		for (String pair : StringUtil.getArrayListFromString(pairs, ",")) {
			String slot = pair.substring(0, pair.indexOf("="));
			slotsRequested.remove("$"+slot);
			slotsConfirmed.remove("$"+slot);
		}
	}

	public void updateLastInfo(HashMap<String,String> dict, SimpleUserSimulator userSimulator) {
		if (dict.size() == 0) return;
		
		if (dict.get("action_usr_val").equals("Confirm(more=yes)") && 
				userSimulator != null) {
			resetUserInfo(userSimulator);
		}

		if (dict.get("action_usr_val").equals("Confirm(no)") && 
				dict.get("action_sys_key").startsWith("ExpConfirm(")) {
			resetFilledSlots(dict.get("action_sys_key"));
		}
		
		lastInfo.put("LastSysAction_Key", dict.get("action_sys_key"));
		lastInfo.put("LastSysAction_Val", dict.get("action_sys_val"));
		lastInfo.put("LastSysResponse", dict.get("response_sys"));
		lastInfo.put("LastUsrAction_Key", dict.get("action_usr_key"));
		lastInfo.put("LastUsrAction_Val", dict.get("action_usr_val"));
		lastInfo.put("LastUsrResponse", dict.get("response_usr"));
		lastInfo.put("LastASROutput", dict.get("response_asr"));

		setHistoricInfo(dict.get("action_sys_key"));
		setHistoricInfo(dict.get("action_usr_key"));
		setSlots(dict.get("action_usr_val"));
		
		if (verbose) {
			Logger.debug(this.getClass().getName(), "updateLastInfo", "slotsRequested="+slotsRequested);
			Logger.debug(this.getClass().getName(), "updateLastInfo", "slotsConfirmed="+slotsConfirmed);
			System.out.println();
		}
	}

	public String getLastInfoParam(String param) {
		return lastInfo.get(param);
	}

	public String getAllowedActions(String state, int steps) {
		return simpleActions.getProbableActions(state, steps, slotsRequested, slotsConfirmed, this.numSlots2Confirm);
	}
	
	public String getRewards(String stateWithoutNoise, String actions, boolean end, int steps) {
		String output = "";

		String datalikeRewards = simpleActions.getDataPredictions(stateWithoutNoise, actions);
		ArrayList<String> dataRewards = StringUtil.getArrayListFromString(datalikeRewards, ",");

		for (int i=0; i<dataRewards.size(); i++) {
			String action = simpleActions.getAction(""+i);
			action = (i==dataRewards.size()-1) ? "LAST" : action;
			double bonusReward = getBonusReward(action, end, steps);
			double dataReward = Double.parseDouble(dataRewards.get(i));
			double reward = (bonusReward*0.5)+(dataReward*0.5)-0.1;
			output += (output.equals("")) ? ""+reward : ","+reward;
		}
		//numSlotsConfirmed = slotsConfirmed.size();
		
		return output;
	}
	
	public double getBonusReward(String lastSysAction, boolean end, int steps) {
		double reward = 0;
		
		reward = (double)slotsConfirmed.size()/this.numSlots2Confirm;

		return reward;
	}
	
	public String getReward(String rewards, String action) {
		ArrayList<String> rewardList = StringUtil.getArrayListFromString(rewards, ",");
		String reward = rewardList.get(Integer.parseInt(action));
		return reward;
	}
}
