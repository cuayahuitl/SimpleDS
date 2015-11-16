/* @description This class is used to parse the configuration parameters of SimpleDS agents.
 * 
 * @history 10.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */


package simpleDS.util;

import java.util.HashMap;


public class ConfigParser {
	private HashMap<String,String> configurations;
	private final String LANGUAGES = "english,german,spanish";
	public boolean verbose = false;
	public int numDialogues = 0;

	public ConfigParser(String configFile) {
		configurations = new HashMap<String,String>();
		IOUtil.readHashMap(configFile, configurations, "=");
		StringUtil.expandAbstractKeyValuePairs(configurations);
		IOUtil.printHashMap(configurations, "CONFIGURATIONS");
		checkValidity();
	}

	public void checkValidity() {
		String param = null;
		String value = null;

		try {
			for (String item : configurations.keySet()) {
				param = item;
				value = (String) configurations.get(param);

				if ((param.equals("Dialogues") && isInteger(value)==false) ||
						(param.equals("SlotsToConfirm") && isInteger(value)==false) ||
						(param.equals("SavingFrequency") && isInteger(value)==false) ||
						(param.equals("LearningSteps") && isInteger(value)==false) ||
						(param.equals("ExperienceSize") && isInteger(value)==false) ||
						(param.equals("BurningSteps") && isInteger(value)==false) ||
						(param.equals("BatchSize") && isInteger(value)==false) ||
						(param.equals("SocketServerPort") && isInteger(value)==false)) {
					Logger.error(this.getClass().getName(), "checkValidity", "Revise param="+param + " value=("+value + "), it should be a positive integer");

				} else if ((param.equals("MinimumProbability") && isReal(value)==false) ||
						(param.equals("DiscountFactor") && isReal(value)==false) ||
						(param.equals("MinimumEpsilon") && isReal(value)==false)) {
					Logger.error(this.getClass().getName(), "checkValidity", "Revise param="+param + " value=("+value + "), it should be a real number");
					
				} else if ((param.equals("Language") && isLanguage(value)==false)) {
					Logger.error(this.getClass().getName(), "checkValidity", "Revise param="+param + " value=("+value + "), is not a valid language");

				} else if ((param.equals("Verbose") && isBoolean(value)==false) ||
						(param.equals("AndroidSupport") && isBoolean(value)==false)) {
					Logger.error(this.getClass().getName(), "checkValidity", "Revise param="+param + " value=("+value + "), it should be true or false");

				} else if (isOther(value)==false) {
					Logger.error(this.getClass().getName(), "checkValidity", "Revise param="+param + " value=("+value + "), it should not be empty");
				}
			}

		} catch (Exception e) {
			Logger.error(this.getClass().getName(), "checkValidity", "Check param="+param + " value="+value + " in ./config.txt");
			System.exit(0);
		}
	}

	private boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
			return true;

		} catch (Exception e) {
			return false;
		}
	}

	private boolean isReal(String value) {
		try {
			Double.parseDouble(value);
			return true;

		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean isLanguage(String value) {
		for (String lang : StringUtil.getArrayListFromString(LANGUAGES, ",")) {
			if (value.equals(lang)) {
				return true;
			}
		}
		
		return false;
	}	

	private boolean isBoolean(String value) {
		if (value.equals("true") || value.equals("false")) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isOther(String value) {
		if (!value.equals("")) {
			return true;
		} else {
			return false;
		}
	}

	public String getParamValue(String param) {
		return configurations.get(param);
	}
	
	public HashMap<String,String> getParams() {
		return configurations;
	}
	
	public void setVerboseMode(String _verbose) {
		if (_verbose != null) {
			if (_verbose.equals("-v")) {
				verbose = true;
				
			} else if (_verbose.equals("-nv")) {
				verbose = false;
				
			} else {
				Logger.error(this.getClass().getName(), "checkValidity", "Unknown verbose mode="+_verbose);
				System.exit(0);
			}
			
		} else {
			verbose = configurations.get("Verbose").equals("true") ? true : false;
		}
		Logger.debug(this.getClass().getName(), "setVerboseMode", "verbose="+verbose);
	}
	
	public void setNumDialogues(String _numDialogues) {
		if (_numDialogues != null) {
			numDialogues = Integer.parseInt(_numDialogues);
		} else {
			numDialogues = Integer.parseInt(configurations.get("Dialogues"));
		}	
		Logger.debug(this.getClass().getName(), "setNumDialogues", "numDialogues="+numDialogues);
	}
}