/* @description This class extracts and maintains a lexicon from system+user template responses.
 * 
 * @history 2.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */

package simpleDS.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Vocabulary {
	private ArrayList<String> words = new ArrayList<String>();

	public Vocabulary(String sysResponsesFile, String usrResponsesFile, String slotsFile) {
		extractWordSequences(sysResponsesFile, ":");
		extractWordSequences(usrResponsesFile, ":");
		extractWordSequences(slotsFile, ":");
		Collections.sort(words);
		IOUtil.printArrayList(words, "VOCABULARY");
	}

	private void extractWordSequences(String fileToRead, String separator) {
		HashMap<String,String> map = new HashMap<String,String>();
		IOUtil.readHashMap(fileToRead, map, separator);
		
		for (String key : map.keySet()) {
			String sequence = map.get(key);
			
			if (sequence.indexOf("(")>0 && sequence.indexOf(")")>0) {
				continue;
				
			} else {
				for (String item : extractWordsFromSequence(sequence)) {
					if (!words.contains(item)) {
						words.add(item);
					}
				}
			}
		}
	}
	
	public ArrayList<String> extractWordsFromSequence(String sequence) {
		ArrayList<String> output = new ArrayList<String>();
		
		if (sequence == null) return output;
		else sequence = sequence.toLowerCase();
		
		ArrayList<String> list = StringUtil.getArrayListFromString(sequence, " \".,?!|");
		for (String item : list) {
			if (!output.contains(item) && !item.startsWith("$")) {
				output.add(item);
			}
		}
		
		return output;
	}
	
	public ArrayList<String> getWordList() {
		return words;
	}

	public int getVocabularySize() {
		return words.size();
	}
}