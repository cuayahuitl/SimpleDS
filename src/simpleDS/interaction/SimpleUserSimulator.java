/* @description This class implements a rule-based user simulator for training SimpleDS agents.
 *              It randomises over alternative choices of user responses given the last system action.
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

public class SimpleUserSimulator {
	private HashMap<String,String> actions;
	private HashMap<String,String> slots;
	private HashMap<String,String> usrGoal;
	private boolean verbose = false;

	public SimpleUserSimulator(HashMap<String,String> configurations) {
		verbose = configurations.get("Verbose").equals("true") ? true : false;
		String usrResponsesFile = configurations.get("UsrResponses");
		String slotsFile = configurations.get("SlotValues");
		loadUserResponses(usrResponsesFile, slotsFile);
	}

	private void loadUserResponses(String usrResponsesFile, String slotsFile) {
		actions = new HashMap<String,String>();
		IOUtil.readHashMap(usrResponsesFile, actions, ":");
		IOUtil.printHashMap(actions, "USER ACTIONS");

		slots = new HashMap<String,String>();
		IOUtil.readHashMap(slotsFile, slots, ":");
		IOUtil.printHashMap(slots, "SLOTS");
	}

	public void resetUserGoal() {
		usrGoal = new HashMap<String,String>();
		for (String slot : slots.keySet()) {
			String values = slots.get(slot);
			values = values.replace("\"", " ");
			values = values.trim();
			ArrayList<String> list = StringUtil.getArrayListFromString(values, "|");
			int randomIndex = (int) Math.floor(Math.random()*list.size());
			String randomValue = list.get(randomIndex);
			usrGoal.put(slot, randomValue);
		}
		
		if (verbose) {
			Logger.debug("SimpleUserSimulator", "resetUserGoal", "usrGoal="+usrGoal);
		}
	}

	public String getAction(String action_sys_key, String action_sys_val) {
		String action = actions.get(action_sys_key);

		if (action == null) { 
			return ""; // silence	

		} else if (action.equals("Confirm($yesno)")) {
			action = validConfirmation(action_sys_val) ? "Confirm(yes)" : "Confirm(no)";
			return action;

		} else {
			String options = actions.get(action_sys_key);
			String option = StringUtil.getRandomisedTemplate(options);
			return option;
		}
	}

	public boolean validConfirmation(String dialAct) {
		String pairs = dialAct.substring(dialAct.indexOf("(")+1, dialAct.indexOf(")"));
		ArrayList<String> list = StringUtil.getArrayListFromString(pairs, ",");

		for (String pair : list) {
			String slot = pair.substring(0, pair.indexOf("="));
			String value_sys = pair.substring(pair.indexOf("=")+1);
			String value_usr = usrGoal.get("$"+slot);
			if (value_usr == null || !value_usr.equals(value_sys)) {
				return false;
			}
		}

		return true;
	}

	public String getAction_Unfolded(String action_usr) {
		return StringUtil.getExpandedDialAct(action_usr, usrGoal);
	}

	public String getResponse(String action_usr) {
		String rand_action_usr = StringUtil.getRandomisedTemplate(action_usr);
		String templates = actions.get(rand_action_usr);
		String template = StringUtil.getRandomisedTemplate(templates);
		String response = StringUtil.getExpandedTemplate(template, usrGoal);

		return response;
	}
		
	public void updateUserGoal(String response_asr) {
		if (response_asr == null || response_asr.equals("")) return;
	
		for (String slot : slots.keySet()) {
			String slotValues = slots.get(slot);

			for (String value : StringUtil.getArrayListFromString(slotValues, "|\"")) {
				if (response_asr.indexOf(value) >= 0) {
					usrGoal.put(slot, value);
				}
			}
		}
	}
	
	public HashMap<String,String> getUsrGoal() {
		return usrGoal;
	}
}