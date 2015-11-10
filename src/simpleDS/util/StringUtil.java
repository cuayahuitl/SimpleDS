/* @description This class implements reusable methods for string-based processing.
 * 
 * @history 2.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */


package simpleDS.util;

import java.util.*;


public class StringUtil {
	public static String getExpandedDialAct(String template, HashMap<String,String> pairs) {		
		if (template == null) return template;
		String newtemplate = template;

		if (template.indexOf("$")>=0) {
			boolean quoted = template.startsWith("\"") && template.endsWith("\"") ? true : false;
			if (quoted) {
				template = template.substring(1, template.length()-1);
			}

			String sequence = "";
			String slotValues = template.substring(template.indexOf("(")+1, template.indexOf(")"));
			ArrayList<String> list = StringUtil.getArrayListFromString(slotValues, ",");

			for (String item : list) {
				String key = item.substring(0, item.indexOf("="));
				String val = item.substring(item.indexOf("=")+1);
				String keyVal = key+"="+pairs.get(val);
				sequence += (sequence.equals("")) ? keyVal : ","+keyVal;
			}

			String dialAct = template.substring(0, template.indexOf("("));
			newtemplate = dialAct + "(" + sequence + ")";

			if (quoted) {
				newtemplate = "\""+newtemplate+"\"";
			}
		}

		return newtemplate;
	}

	public static String getExpandedTemplate(String template, HashMap<String,String> pairs) {
		if (template == null) return template;
		String newtemplate = template;

		if (template.indexOf("$")>=0) {
			boolean quoted = template.startsWith("\"") && template.endsWith("\"") ? true : false;
			if (quoted) {
				template = template.substring(1, template.length()-1);
			}

			String prefix = template.substring(0, template.indexOf("$"));
			String suffix = "";
			String variable = "";
			template = template.substring(prefix.length());
			if (template.indexOf(" ")>0) {
				variable = template.substring(0, template.indexOf(" "));
				suffix = template.substring(template.indexOf(" "));

			} else if (template.endsWith(".") || template.endsWith("?") || template.endsWith(")")) {
				suffix += template.substring(template.length()-1);
				variable = template.substring(0, template.length()-1);

			} else {
				variable = template;
			}

			if (!pairs.containsKey(variable)) return null;
				
			String value = (String) pairs.get(variable);
			newtemplate = prefix + value.trim() + suffix;

			if (quoted) {
				newtemplate = "\""+newtemplate+"\"";
			}
		}
		
		if (newtemplate.indexOf("$")>0) {
			newtemplate = getExpandedTemplate(newtemplate, pairs);
		}

		return newtemplate;
	}
	
	public static String getRandomisedTemplate(String templates) {
		ArrayList<String> list = StringUtil.getArrayListFromString(templates, "|");
		String template = null;
		
		if (list.size() == 1) {
			template = list.get(0);
			
		} else {
			double randValue = Math.random();
			for (int i=1; i<=list.size(); i++) {
				String templateInFocus = list.get(i-1);
				double incValue = (double)i/list.size();

				if (incValue>=randValue) {
					template = templateInFocus;
					break;
				}
			}
		}
	
		return template;
	}
	
	public static void expandAbstractKeyValuePairs(HashMap<String,String> collection) {
		try {
		for (String key : collection.keySet()) {
			String line = collection.get(key);
			if (line.indexOf("%")>0) {
				String prefix = line.substring(0, line.indexOf("%"));
				String rest = line.substring(line.indexOf("%")+1);
				String value = rest.substring(0, rest.indexOf("%"));
				String suffix = rest.substring(rest.indexOf("%")+1);
				String newValue = prefix + collection.get(value) + suffix;
				collection.put(key, newValue);
			}
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<String> getArrayListFromString(String wordSequence, String separator) {
		ArrayList<String> array = new ArrayList<String>();

		if (wordSequence != null) {
			StringTokenizer toks = new StringTokenizer(wordSequence, separator);
			while (toks.hasMoreTokens()) array.add(toks.nextToken());
		}

		return array;
	}
	
	public static HashMap<String,Double> getWordDistributionFromRawText(String observations) {
		HashMap<String,Integer> counts = new HashMap<String,Integer>();
		HashMap<String,Double> probs = new HashMap<String,Double>();
		int topCount = 0;

		if (observations == null || observations.equals("")) return probs;
		
		observations = observations.toLowerCase();
		ArrayList<String> tokens = StringUtil.getArrayListFromString(observations, " :");
		
		for (String token : tokens) {
			IOUtil.incrementHashTable(counts, token, 1);
			int count = counts.get(token);
			if (count>topCount) {
				topCount = count;
			}
		}

		for (String token : tokens) {
			int count = counts.get(token);
			double prob = (double) count/topCount;
			probs.put(token, prob);
		}
		
		return probs;
	}
}
