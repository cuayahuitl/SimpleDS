/* @description This class implements the actions of the SimpleDS dialogue system.
 *              The actions are represented as dialogue acts and numerically as well.
 *              The actions available at a given state can be all or a subset of them.
 *              The latter is done by selecting the most likely action(s) via a Naive
 *              Bayes classifier (trained from some example dialogues) and some
 *              application-independent heuristics. 
 *              
 * @history 2.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */

package simpleDS.interaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import simpleDS.learning.SimpleClassifier;
import simpleDS.util.IOUtil;
import simpleDS.util.Logger;
import simpleDS.util.StringUtil;

public class SimpleActions {
	protected HashMap<String,String> dialActs;
	protected HashMap<String,String> actions_ID;
	protected HashMap<String,String> actions_DAct;
	protected ArrayList<String> selectedActions;
	public SimpleClassifier actionPredictor;
	private String fistBestAction = null;
	private String lastPredictions = null;

	public SimpleActions(String sysResponsesFile) {
		dialActs = new HashMap<String,String>();
		actions_ID = new HashMap<String,String>();
		actions_DAct = new HashMap<String,String>();
		selectedActions = new ArrayList<String>();
		loadSystemResponses(sysResponsesFile);
		defineActionSet();
	}

	private void loadSystemResponses(String sysResponsesFile) {
		IOUtil.readHashMap(sysResponsesFile, dialActs, ":");
		IOUtil.printHashMap(dialActs, "SYSTEM ACTIONS");
	}

	public void createActionPredictor(String demonstrationsFile, String minimumProbability) {
		actionPredictor = new SimpleClassifier(demonstrationsFile, minimumProbability);
	}

	public void defineActionSet() {
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(dialActs.keySet());
		Collections.sort(list);

		for (int i=0; i<list.size(); i++) {
			String dialAct = list.get(i);
			this.actions_ID.put(""+i, dialAct);
			this.actions_DAct.put(dialAct, ""+i);
		}
		IOUtil.printHashMap(actions_ID, "SYSTEM ACTIONS BY ID");
	}
	
	public void setSelectedAction(String action) {
		if (!this.selectedActions.contains(action)) {
			this.selectedActions.add(action);
		}
	}
	
	public void resetSelectedActions() {
		this.selectedActions = new ArrayList<String>();
	}

	public ArrayList<String> getActionIDs() {
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(this.actions_ID.keySet());
		return list;
	}

	public int getActionSetSize() {
		return this.actions_ID.size();
	}

	public String getAction(String index) {
		return this.actions_ID.get(index);
	}

	public String getActionID(String action) {
		return this.actions_DAct.get(action);
	}

	public String getRandomAction(String actions) {
		if (actions == null || actions.equals("")) {
			Logger.error("SimpleActions", "getRandomAction", "EMPTY action set");
			return null;

		} else {
			ArrayList<String> list = StringUtil.getArrayListFromString(actions, ",");
			int randomIndex = (int) Math.floor(Math.random()*list.size());
			return list.get(randomIndex);			
		}
	}

	public HashMap<String,String> getEvidence(String state) {
		HashMap<String,String> evidence = new HashMap<String,String>();
		ArrayList<String> features = StringUtil.getArrayListFromString(state, ",");
		for (int i=1; i<=features.size(); i++) {
			String key = "word"+i;
			String val = features.get(i-1);
			evidence.put(key, val);
		}

		return evidence;
	}

	public String getDataPredictions(String state, String actions) {
		HashMap<String,String> evidence = getEvidence(state);
		ArrayList<String> list = StringUtil.getArrayListFromString(actions, ",");
		lastPredictions = actionPredictor.getDataLikeRewards(evidence, "action", list);
		return lastPredictions;
	}

	public String getLastPredictions() {
		return lastPredictions;
	}

	public String getMostProbableAction(String state) {
		HashMap<String,String> evidence = getEvidence(state);
		return actionPredictor.getMostProbableAction(evidence, "action");
	}

	public String getProbableActions(String state, int steps, 
			HashMap<String,String> slotsRequested, HashMap<String,String> slotsConfirmed, int numSlots2Confirm) { 

		if (steps == 1) {
			this.fistBestAction = this.getMostProbableAction(state);
			return this.fistBestAction;

		} else {
			HashMap<String,String> evidence = getEvidence(state);
			String actions = actionPredictor.getMostProbableActions(evidence, "action");
			String mostProbableAction = actionPredictor.getMostProbableAction(evidence, "action");
			String extendedActions = getExtendedActionsWithMissingOnes(actions, mostProbableAction, slotsRequested, slotsConfirmed, numSlots2Confirm);
			return (extendedActions.equals("")) ? actions : extendedActions;
		}
	}

	private String getExtendedActionsWithMissingOnes(String actions, String mostProbableActionID, 
			HashMap<String,String> slotsRequested, HashMap<String,String> slotsConfirmed, int numSlots2Confirm) {
		String set = "";
		String mostProbableAction = this.getAction(mostProbableActionID);
		if (mostProbableAction == null) return actions;
		String firstAction = this.actions_ID.get(this.fistBestAction);
		String datFirstAction = firstAction.substring(0, firstAction.indexOf("("));
		ArrayList<String> list = StringUtil.getArrayListFromString(actions, ",");

		for (String actionID : this.actions_ID.keySet()) {
			String action = this.getAction(actionID);
			String dat = action.substring(0, action.indexOf("("));
			boolean hasConfirmedSlots = areGivenSlotsConfirmed(action, slotsConfirmed);
			boolean hasRequestedSlots = areGivenSlotsConfirmed(action, slotsRequested);
			boolean hasSlotsToRequest = (slotsRequested.size() == numSlots2Confirm) ? false : true;
			boolean hasSlotsToConfirm = (slotsConfirmed.size() == numSlots2Confirm) ? false : true;
			boolean isSlotCollectionTime = (slotsRequested.size()+slotsConfirmed.size() < numSlots2Confirm*2) ? true : false;
			boolean isCollectionAction = (action.startsWith("Request") || action.startsWith("Apology") || action.indexOf("Confirm")>1) ? true : false;
			
			if (actionID.equals(this.fistBestAction) || dat.equals(datFirstAction) || selectedActions.contains(action) ||
					(isCollectionAction==false && isSlotCollectionTime) ||
					(isCollectionAction==true && !isSlotCollectionTime)) {
				continue;
			}

			String response = this.dialActs.get(action);
			response = StringUtil.getExpandedTemplate(response, slotsRequested);
			
			if (response == null || response.equals("null")) {
				continue;

			} else if ((list.contains(actionID) && isCollectionAction==false) || 
					(action.startsWith("Request") && hasRequestedSlots==false && hasConfirmedSlots==false && hasSlotsToRequest) ||
					(action.startsWith("Apology") && hasRequestedSlots==true && hasConfirmedSlots==false && hasSlotsToConfirm) ||
					(action.indexOf("Confirm")>=0 && hasRequestedSlots==true && hasConfirmedSlots==false && hasSlotsToConfirm)) {
				set += (set.equals("")) ? actionID : ","+actionID;
			}
		}

		return set;
	}

	public boolean areGivenSlotsConfirmed(String action, HashMap<String,String> collection) {
		String slots = action.substring(action.indexOf("(")+1, action.indexOf(")"));
		ArrayList<String> list = StringUtil.getArrayListFromString(slots, ",");

		for (String slot : list) {
			if (slot.indexOf("=")>0) {
				slot = slot.substring(0, slot.indexOf("="));
			}
			String slotID = "$"+slot;
			if (collection.containsKey(slotID)) {
				return true;
			}
		}

		return false;
	}
}
