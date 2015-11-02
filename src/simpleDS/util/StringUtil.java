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

	public static ArrayList<String> getArrayListFromString(String wordSequence, String separator) {
		ArrayList<String> array = new ArrayList<String>();

		if (wordSequence != null) {
			StringTokenizer toks = new StringTokenizer(wordSequence, separator);
			while (toks.hasMoreTokens()) array.add(toks.nextToken());
		}

		return array;
	}
}
