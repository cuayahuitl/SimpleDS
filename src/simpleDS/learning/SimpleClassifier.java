/* @description This class implements a Naive Bayes classifier for action prediction from demonstration dialogues.
 *              The probabilities of predicted actions can be seen as scores representing "data-like actions".
 *              
 * @history 2.Nov.2015 Beta version
 *              
 * @author <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
 */

package simpleDS.learning;

import java.util.ArrayList;
import java.util.HashMap;

import simpleDS.util.Logger;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class SimpleClassifier {
	private Instances instances;
	private NaiveBayes classifier;
	private double minimumProbability;

	public SimpleClassifier(String trainingFile, String minimumProbability) {
		this.minimumProbability = Double.parseDouble(minimumProbability);
		loadData(trainingFile);
		trainModel();
	}

	public void loadData(String trainingFile) {		
		try {
			Logger.debug(this.getClass().getName(), "loadData", "Reading "+trainingFile);
			instances = DataSource.read(trainingFile);
			instances.setClassIndex(instances.numAttributes() - 1);
			Logger.debug(this.getClass().getName(), "loadData", "Data loaded!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void trainModel() {
		try {
			classifier = new NaiveBayes();
			classifier.buildClassifier(instances);
			Logger.debug(this.getClass().getName(), "trainModel", "Model created!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Instance createInstance(HashMap<String,String> evidence) {
		Instance firstInstance = (Instance) instances.firstInstance().copy();

		for (int i=0; i<instances.numAttributes()-1; i++) {
			Attribute attribute = (Attribute) instances.attribute(i);

			if (attribute.isNumeric()) {
				int val = Integer.parseInt(evidence.get(attribute.name()));
				firstInstance.setValue(attribute, val);

			} else {
				String val = evidence.get(attribute.name());
				firstInstance.setValue(attribute, val);					
			}
		}

		return firstInstance;
	}

	public String getDataLikeRewards(HashMap<String,String> evidence, String label, ArrayList<String> actions) {
		String output = "";
		
		try {
			Instance firstInstance = createInstance(evidence);
			double[] dist = classifier.distributionForInstance(firstInstance);
			
			for (int i=0; i<dist.length; i++) {
				if (actions.contains(""+i)) {
					output += (output.equals("")) ? ""+dist[i] : ","+dist[i];

				} else {
					output += (output.equals("")) ? "0" : ",0";
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return output;
	}

	public String getMostProbableAction(HashMap<String,String> evidence, String label) {
		double bestScore = 0;
		String bestLabel = "";

		try {
			Instance firstInstance = createInstance(evidence);
			Attribute attribute = firstInstance.attribute(instances.numAttributes()-1);
			double[] dist = classifier.distributionForInstance(firstInstance);
			
			for (int i=0; i<dist.length; i++) {
				String attValue = attribute.value(i);
				
				if (dist[i]>bestScore) {
					bestScore = dist[i];
					bestLabel = attValue;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return bestLabel;
	}

	public String getMostProbableActions(HashMap<String,String> evidence, String label) {
		String output = "";

		try {
			Instance firstInstance = createInstance(evidence);
			Attribute attribute = firstInstance.attribute(instances.numAttributes()-1);
			double[] dist = classifier.distributionForInstance(firstInstance);

			for (int i=0; i<dist.length; i++) {
				String attValue = attribute.value(i);
				if (dist[i]>this.minimumProbability) { 
					output += (output.equals("")) ? attValue : ","+attValue;		
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return output;
	}
}