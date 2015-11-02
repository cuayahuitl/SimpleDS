/* @description This class implements the environment for training SimpleDS agents.
 *              The states of the environment are based on words from a given vocabulary.
 *              The actions of the environment are based on example dialogues and heuristics.
 *              The former extracts training instances from example dialogues at runtime.
 * 
 * @history 2.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */

package simpleDS.learning;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import simpleDS.interaction.SimpleInteractionPolicy;
import simpleDS.util.IOUtil;
import simpleDS.util.Logger;
import simpleDS.util.Vocabulary;

public class SimpleEnvironment {
	private Vocabulary vocabulary;
	private boolean verbose = false;
	public SimpleInteractionPolicy interactionPolicy;

	public SimpleEnvironment(HashMap<String,String> configurations) {
		verbose = configurations.get("Verbose").equals("true") ? true : false;

		String sysResponsesFile = configurations.get("SysResponses");
		String usrResponsesFile = configurations.get("UsrResponses");
		String slotsFile = configurations.get("SlotValues");
		String demonstrationsPath = configurations.get("DemonstrationsPath");
		String demonstrationsFile = configurations.get("DemonstrationsFile");
		String slots2Confirm = configurations.get("SlotsToConfirm");
		String noiseLevel = configurations.get("NoiseLevel");
		
		interactionPolicy = new SimpleInteractionPolicy(sysResponsesFile, slots2Confirm, noiseLevel, verbose);
		vocabulary = new Vocabulary(sysResponsesFile, usrResponsesFile, slotsFile);
		generateTrainingDataFromDemonstrations(demonstrationsPath, demonstrationsFile);
		interactionPolicy.simpleActions.createActionPredictor(demonstrationsFile);
	}

	public String getEnvironmentState(boolean withNoise) {
		String lastSysResponse = interactionPolicy.getLastInfoParam("LastSysResponse");
		String lastUsrResponse = interactionPolicy.getLastInfoParam("LastUsrResponse");
		ArrayList<String> allFeatures = getWordFeaturesFromParam(lastSysResponse, lastUsrResponse, withNoise);

		String state = "";
		for (String feature : allFeatures) {
			state += state.equals("") ? feature : ","+feature;
		}

		return state;
	}
	
	private ArrayList<String> getWordFeaturesFromParam(String lastSysResponse, String lastUsrResponse, boolean withNoise) {
		ArrayList<String> featureVector = new ArrayList<String>();

		ArrayList<String> wordsSysResponse = vocabulary.extractWordsFromSequence(lastSysResponse);
		ArrayList<String> wordsUsrResponse = vocabulary.extractWordsFromSequence(lastUsrResponse);
		HashMap<String,Float> lastConfScores = interactionPolicy.getLastConfidenceScores();

		for (String word : vocabulary.getWordList()) {
			String value = wordsSysResponse.contains(word) ? "1" : "0";
			if (wordsUsrResponse.contains(word)) {
				if (withNoise) {
					value = ""+lastConfScores.get(word).floatValue();
				} else {
					value = "1";
				}
			}
			featureVector.add(value);
		}

		return featureVector;
	}

	public ArrayList<String> getVocabulary() {
		return vocabulary.getWordList();
	}

	public void generateTrainingDataFromDemonstrations(String demonstrationsPath, String demonstrationsFile) {
		ArrayList<String> instances = new ArrayList<String>();
		String lastFile = "";

		try {
			String[] dataFiles = (new File(demonstrationsPath + "/")).list();

			for (String fileID : dataFiles) {
				lastFile = fileID;
				String sysResponse = "";
				String sysActionID = "";
				String usrResponse = "";

				if (fileID.endsWith(".txt")) {
					ArrayList<String> lines = new ArrayList<String>();
					IOUtil.readArrayList(demonstrationsPath + "/" + fileID, lines);

					for (String line : lines) {
						if (line.indexOf("SYS:")>=0) {
							String action = line.substring(line.indexOf("[")+1,line.indexOf("]"));
							sysActionID = this.interactionPolicy.simpleActions.getActionID(action);
							if (sysActionID == null) {
								Logger.error(this.getClass().getName(), "generateTrainingDataFromDemonstrations", "UNKNOWN action="+action);
							} else {
								instances.add(getTrainingInstance(sysResponse, usrResponse, sysActionID));
								sysResponse = line.substring(line.indexOf("]")+1);
							}

						} else if (line.indexOf("USR:")>=0) {
							usrResponse = line.substring(line.indexOf(":")+1);
							usrResponse = (usrResponse.equals("null")) ? "" : usrResponse;
						}
					}
					String lastSysActionID = ""+this.interactionPolicy.simpleActions.getActionSetSize();
					instances.add(getTrainingInstance(sysResponse, usrResponse, lastSysActionID));
				}
			}
			saveDemonstrationsData(instances, demonstrationsFile);

		} catch (Exception e) {
			Logger.error(this.getClass().getName(), "generateTrainingDataFromDemonstrations", "UNKNOWN format in file="+lastFile);
			e.printStackTrace();
		}
	}

	public String getTrainingInstance(String sysResponse, String usrResponse, String sysActionID) {
		ArrayList<String> allFeatures = getWordFeaturesFromParam(sysResponse, usrResponse, false);

		String instance = "";
		for (String feature : allFeatures) {
			instance += instance.equals("") ? feature : ","+feature;
		}
		instance += ","+sysActionID;

		return instance;
	}

	private void saveDemonstrationsData(ArrayList<String> instances, String demonstrationsFile) {
		try {
			ArrayList<String> output = new ArrayList<String>();
			ArrayList<String> actionSet = this.interactionPolicy.simpleActions.getActionIDs();
			String actions = actionSet.toString();
			actions = actions.substring(actions.indexOf("[")+1, actions.indexOf("]"));
			actions += ","+this.interactionPolicy.simpleActions.getActionSetSize();

			output.add("@relation demonstrations4ActionPrediction");
			output.add("");
			for (int i=1; i<=this.getVocabulary().size(); i++) {
				output.add("@attribute word"+i+" NUMERIC");
			}
			output.add("@attribute action {"+actions+"}");
			output.add("");
			output.add("@data");
			output.addAll(instances);
			IOUtil.writeArrayList(demonstrationsFile, output, "demonstrations");

		} catch (Exception e) {
			Logger.error(this.getClass().getName(), "saveDemonstrationsData", "Couldn't store training data.");
			e.printStackTrace();
		}
	}
}