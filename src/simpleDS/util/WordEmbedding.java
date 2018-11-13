/* @description This class extracts and maintains a dictionary of word embeddings (word-vector pairs).
 * 
 * @history 2.Nov.2018 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */

package simpleDS.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.io.File;

public class WordEmbedding {
	private ArrayList<String> vocabulary;
	private HashMap<String,ArrayList<Float>> wordVectors;
	private HashMap<String,ArrayList<String>> meanWordVectors;
	private int dimensionality = 0;
	public boolean available = false;

	public WordEmbedding(String gloveWordVectorsFile) {
		available = (new File(gloveWordVectorsFile)).exists();

		if (available) {
			ArrayList<String> wordVectorList = new ArrayList<String>();
			IOUtil.readArrayList(gloveWordVectorsFile, wordVectorList);
	
			vocabulary = new ArrayList<String>();
			wordVectors = new HashMap<String,ArrayList<Float>>();
			meanWordVectors = new HashMap<String,ArrayList<String>>();

			loadWordVectors(wordVectorList);
			printInfo();

		} else {
			Logger.warning("WordEmbedding", "printInfo", "Word embeddings file unavailable!");
		}
	}

	private void loadWordVectors(ArrayList<String> wordVectorList) {
		for (String line : wordVectorList) {
			ArrayList<String> tokens = StringUtil.getArrayListFromString(line, " ");
			String word = tokens.get(0);
			vocabulary.add(word);

			ArrayList<Float> denseVector = new ArrayList<Float>();
			for (int i=1; i<tokens.size(); i++) {
				String feature = tokens.get(i);
				denseVector.add(new Float(feature));
			}

			wordVectors.put(word, denseVector);
		}

		dimensionality = (wordVectors.get(vocabulary.get(0))).size();
	}

	public ArrayList<String> getMeanWordVector(String wordSequence) {
		ArrayList<String> meanWordVector = new ArrayList<String>();

		if (meanWordVectors.containsKey(wordSequence)) {
			return meanWordVectors.get(wordSequence);

		} else if (wordSequence == null || wordSequence.equals("")) {
			for (int i=0; i<dimensionality; i++) {
				meanWordVector.add("0");
			}

		} else {
			HashMap<String,ArrayList<Float>> wordVectorPairs = new HashMap<String,ArrayList<Float>>();
			ArrayList<String> wordList = StringUtil.getArrayListFromString(wordSequence, " ");
			for (String word : wordList) {
				String newWord = StringUtil.getWordWithoutPunctuation(word);
				wordVectorPairs.put(newWord, wordVectors.get(newWord));
			}

			int numWords = wordVectorPairs.size();
			for (int d=0; d<dimensionality; d++) {
				float sum = 0;
				for (String word : wordVectorPairs.keySet()) {
					ArrayList<Float> vector = wordVectorPairs.get(word);
					sum += vector.get(d);
				}
				float meanValue = (float)sum/numWords;
				meanWordVector.add(""+meanValue);
			}
		}

		meanWordVectors.put(wordSequence, meanWordVector);

		return meanWordVector;
	}

	public int getVocabularySize() {
		return vocabulary.size();
	}

	public int getEmbeddingDimensionality() {
		return dimensionality;
	}

	public void printInfo() {
		if (!available) return;
		Logger.debug("WordEmbedding", "printInfo", "Vocabulary Size="+vocabulary.size());
		Logger.debug("WordEmbedding", "printInfo", "Embedding Dimensionality="+dimensionality);
	}
}
