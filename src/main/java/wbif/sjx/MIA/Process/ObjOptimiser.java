// Based on example from https://www.programcreek.com/2013/01/a-simple-machine-learning-example-in-java/ (Accessed 2019-08-01)

package wbif.sjx.MIA.Process;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import wbif.sjx.MIA.MIA;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.core.*;

public class ObjOptimiser {
    public static BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;

        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + filename);
        }

        return inputReader;
    }

    public static Evaluation classify(Classifier model,
                                      Instances trainingSet, Instances testingSet) throws Exception {
        Evaluation evaluation = new Evaluation(trainingSet);

        model.buildClassifier(trainingSet);
        evaluation.evaluateModel(model, testingSet);

        return evaluation;
    }

    public static double calculateAccuracy(FastVector predictions) {
        double correct = 0;

        for (int i = 0; i < predictions.size(); i++) {
            NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
            if (np.predicted() == np.actual()) {
                correct++;
            }
        }

        return 100 * correct / predictions.size();
    }

    public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds) {
        Instances[][] split = new Instances[2][numberOfFolds];

        for (int i = 0; i < numberOfFolds; i++) {
            split[0][i] = data.trainCV(numberOfFolds, i);
            split[1][i] = data.testCV(numberOfFolds, i);
        }

        return split;
    }

    public static void main(String[] args) throws Exception {
        BufferedReader datafile = readDataFile("C:\\Users\\steph\\Documents\\Java Projects\\ModularImageAnalysis\\src\\main\\resources\\weather.txt");

        Instances data = new Instances(datafile);
        data.setClassIndex(0);

        // Do 10-split cross validation
        Instances[][] split = crossValidationSplit(data, 10);

        // Separate split into training and testing arrays
        Instances[] trainingSplits = split[0];
        Instances[] testingSplits = split[1];

        Classifier model =  new DecisionTable();//decision table majority classifier

        // Run for each model
        // Collect every group of predictions for current model in a FastVector
        FastVector predictions = new FastVector();

        // For each training-testing split pair, train and test the classifier
        for (int i = 0; i < trainingSplits.length; i++) {
            Evaluation validation = classify(model, trainingSplits[i], testingSplits[i]);
            predictions.appendElements(validation.predictions());
        }

//         Calculate overall accuracy of current classifier on all splits
        double accuracy = calculateAccuracy(predictions);

        // Print current classifier's name and accuracy in a complicated,
        // but nice-looking way.
        System.out.println("Accuracy of " + model.getClass().getSimpleName() + ": "
                + String.format("%.2f%%", accuracy)
                + "\n---------------------------------");

        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("sunny");
        fvClassVal.addElement("rainy");
        for (int i=0;i<data.size();i++) System.out.println(fvClassVal.get((int)model.classifyInstance(data.get(i))));

        FastVector outlookClasses = new FastVector(2);
        outlookClasses.addElement("sunny");
        outlookClasses.addElement("rainy");
        Attribute outlook = new Attribute("outlook", outlookClasses);
        Attribute temperature = new Attribute("temperature");
        Attribute humidity = new Attribute("humidity");
        Attribute windy = new Attribute("windy");
        Attribute play = new Attribute("play");

        // Declare the feature vector
        FastVector fvWekaAttributes = new FastVector(5);

        // Add attributes
        fvWekaAttributes.addElement(outlook);
        fvWekaAttributes.addElement(temperature);
        fvWekaAttributes.addElement(humidity);
        fvWekaAttributes.addElement(windy);
        fvWekaAttributes.addElement(play);

        // Declare Instances which is required since I want to use classification/Prediction
        Instances dataset = new Instances("whatever", fvWekaAttributes, 0);
        Instance instance = new SparseInstance(1, new double[]{70,196,1,0});
        dataset.add(instance);
        dataset.setClassIndex(0);

        System.out.println(outlookClasses.get((int) model.classifyInstance(dataset.get(0))));

    }
}