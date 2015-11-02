/* @description This class implements reusable methods for input/output processing, 
 *              e.g. reading and writing data structures.
 * 
 * @history 2.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */

package simpleDS.util;

import java.util.*;
import java.io.*;

import simpleDS.util.Logger;


public class IOUtil {
	public static void printArrayList(ArrayList<String> collection, String title) {
		Logger.debug("IOUtil", "printArrayList", "==============================================================================");
		Logger.debug("IOUtil", "printArrayList", title);

		for (int i = 0; i < collection.size(); i++) {
			Logger.debug("IOUtil", "printArrayList", "[" + i + "] " + collection.get(i));
		}
	}

	public static void printHashMap(HashMap collection, String title) {
		String key = "";
		String value = "";
		ArrayList<String> states = new ArrayList<String>();

		Logger.debug("IOUtil", "printHashMap", "==============================================================================");
		Logger.debug("IOUtil", "printHashMap", title);

		Set entries = collection.entrySet();
		Iterator iter = entries.iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			key = (String) entry.getKey();
			
			if (entry.getValue() instanceof Integer) {
				value = ((Integer) entry.getValue()).toString();
				
			} else if (entry.getValue() instanceof Float) {
				value = ((Float) entry.getValue()).toString();

			} else if (entry.getValue() instanceof Double) {
				value = ((Double) entry.getValue()).toString();

			} else if (entry.getValue() instanceof Boolean) {
				value = ((Boolean) entry.getValue()).toString();
				
			} else {
				value = (String) entry.getValue();
			}
			states.add(key + "\t" + value);
		}

		Collections.sort(states);
		for (int i = 0; i < states.size(); i++) {
			Logger.debug("IOUtil", "printHashMap", "s=" + states.get(i));
		}
	}

	public static void writeArrayList(String file, ArrayList collection, String message) {
		String value = "";

		Logger.debug("IOUtil", "writeArrayList", message + " ... " + file);

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

			for (int i = 0; i < collection.size(); i++) {
				if (collection.get(i) instanceof Integer) {
					value = ((Integer) collection.get(i)).toString();
					
				} else if (collection.get(i) instanceof Float) {
					value = ((Float) collection.get(i)).toString();
					
				} else if (collection.get(i) instanceof Double) {
					value = ((Double) collection.get(i)).toString();

				} else {
					value = (String) collection.get(i);
				}
				out.println(value);
			}
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void readHashMap(String file, HashMap<String,String> collection, String separator) {
		String line = "";
		String key = "";
		String value = "";

		Logger.debug("IOUtil", "readHashMap", "Reading file " + file);

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#") || line.equals("")) continue; 

				StringTokenizer toks = new StringTokenizer(line, separator);
				if (toks.countTokens() > 0) {
					key = toks.nextToken();
					value = toks.nextToken();
					collection.put(key, value);
				}
			}
			reader.close();

		} catch (IOException e) {
			Logger.debug("IOUtil", "readHashMap", line);
			e.printStackTrace();
		}
	}

	public static void readArrayList(String file, ArrayList<String> collection) {
		String line = "";

		Logger.debug("IOUtil", "readArrayList", "Reading file " + file);

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
				collection.add(line);
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void incrementHashTable(HashMap<String,Integer> collection, String key, int update) {
		if (collection.containsKey(key)) {
			Integer items = (Integer) collection.get(key);
			int updated = items.intValue() + update;
			collection.put(key, new Integer(updated));

		} else {
			collection.put(key, new Integer(update));
		}
	}

}