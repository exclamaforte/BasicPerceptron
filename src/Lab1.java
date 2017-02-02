import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.Scanner;

public class Lab1 {
	private Class cls;
	private List<Feature> featureList;
	private List<Example> trainList;
	private List<Example> tuneList;
    private List<Example> testList;
    private double[] weights;
    private double[] bestWeights;
    private double bestBias;
    private double bestError = 2;
    private double bias;
    private int numEpochs;
    private int generation;


    private enum FeatureType {
        CONTINUOUS, DISCRETE
    }
    private class Class {
    	private HashMap<String, Double> fmapping;
    	public Class(List<String> classes) {
    		double i = 0.0;
    		this.fmapping = new HashMap<String, Double>();
    		for (String c : classes) {
    			fmapping.put(c, i);
    			i++;
    		}
    	}
    	public double get(String s) {
    		return fmapping.get(s);
    	}
    }
    private static class Example implements Iterable<String> {
        private String name;
        private String className;
        private List<String> data;
        public double[] featureVector;
        private double classValue;
        private void createFeatureVector(List<Feature> features) {
        	int sz = 0;
        	for (Feature f : features) {
        		sz += f.size();
        	}
        	featureVector = new double[sz];
        	Arrays.fill(featureVector, 0.0);
        	int head = 0;
        	for (int i = 0; i < features.size(); i++) {
        		Feature f = features.get(i);
        		String val = data.get(i);
        		if (f.type == FeatureType.CONTINUOUS) {
        			featureVector[head] = Double.parseDouble(val);
        			head++;
        		} else if (f.type == FeatureType.DISCRETE) {
            		int idx = f.getLocation(val);
            		featureVector[head + idx] = 1.0;
            		head += f.size();
        		}
        	}
        }
        public String getClassName() {
        	return className;
        }
        public void setClassValue(double s) {
        	this.classValue = s;
        }
        public Example(String name, String cls, List<String> data) {
            this.name = name;
            this.className = cls;
            this.data = data;
        }
        public String toString() {
            return name + "::" + className + data;
        }

		public Iterator<String> iterator() {
			return this.data.iterator();
		}
		public String get(int i) {
			return this.data.get(i);
		}
    }
    private static class Feature {
        private FeatureType type;
        private String name;
        private Set<String> possibleValues;
        private HashMap<String, Integer> mapping;

        public int getLocation(String s) {
        	if (type == FeatureType.DISCRETE) {
        		
        	}
        	assert(type == FeatureType.DISCRETE);
        	return mapping.get(s);
        }
        public Feature(String n) {
        	this.possibleValues = null;
        	this.mapping = null;
            this.name = n;
            this.type = FeatureType.CONTINUOUS;
        }
        public Feature(String n, List<String> vals) {
            this.possibleValues = new HashSet<String>(vals);
            this.name = n;
            this.mapping = new HashMap<String, Integer>();
            this.type = FeatureType.DISCRETE;
            for (int i = 0; i < possibleValues.size(); i++) {
            	mapping.put(vals.get(i), i);
            }
        }
        public int size() { 
        	if (type == FeatureType.CONTINUOUS) {
        		return -1;
        	}
        	return possibleValues.size();
        }
        public String toString() {
            if (this.type == FeatureType.CONTINUOUS) {
                return this.name + "[Z]";
            }
            return this.name + "[" + this.possibleValues + "]";
        }
        public boolean isValid(String arst) {
        	if (this.type == FeatureType.CONTINUOUS) {
        		try {
        			Double.parseDouble(arst);
        			return true;
        		} catch (Exception e) {
        			return false;
        		}
        	}
            return possibleValues.contains(arst);
        }
    }

    private static void smallRandom(double[] init, double size) {
    	for (int i = 0; i < init.length; i++) {
    		init[i] = (Math.random() - .5) * size;
    	}
    }
    private static String[] saneSplit(String s) {
    	Scanner sc = new Scanner(s);
    	ArrayList<String> arst = new ArrayList<String>();
    	while (sc.hasNext()) {
    		String tsra = sc.next();
    		arst.add(tsra);
    	}
    	String[] fuckJava = new String[arst.size()];
    	fuckJava = arst.toArray(fuckJava);
    	sc.close();
    	return fuckJava;
    }
    public static Stream<String[]> parse(Stream<String> instream) {
        return instream.filter(line -> !line.isEmpty() && !line.startsWith("//"))
        		.map(arst -> (String[])saneSplit(arst));
    }

    public static Lab1.Feature createFeature(String[] featureDescription) {
        List<String> possible = new ArrayList<String>(featureDescription.length - 1);
        for (int i = 2; i < featureDescription.length; i++) {
            possible.add(featureDescription[i]);
        }
        return new Feature(featureDescription[0], possible);
    }
    public void createFeatureList(String fn) {
    	Stream<String> stream = null;
    	try {
    		stream = Files.lines(Paths.get(fn));
    	} catch (IOException e) {
    		e.printStackTrace();
    		System.exit(1);
    	}
    	String[][] whole = (String[][])Lab1.parse(stream).toArray(String[][]::new);
    	int len = 0;
    	try {
    		len = Integer.parseInt(whole[0][0]);
    	} catch (NumberFormatException e) {
    		e.printStackTrace();
    		System.exit(1);
    	}
    	this.featureList = new ArrayList<Feature>(len);
    	for (int i = 1; i < len + 1; i++) {
    		this.featureList.add(Lab1.createFeature(whole[i]));
    	}
    }

    public List<Example> dataFromFile(String fileName) {
        Stream<String> stream = null;
        try {
            stream = Files.lines(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        String[][] whole = (String[][])Lab1.parse(stream).toArray(String[][]::new);
        
    	int len = 0;
    	try {
    		len = Integer.parseInt(whole[0][0]);
    	} catch (NumberFormatException e) {
    		e.printStackTrace();
    		System.exit(1);
    	}
        List<Example> data = new ArrayList<Example>(whole.length - len - 2);

        int i = 0;
        for (i = len + 4; i < whole.length; i++) {
            data.add(new Example(whole[i][0], whole[i][1], Arrays.asList(whole[i]).subList(2, whole[i].length)));
        }
        return data;
    }
    public void createTrainList (String fn) {
    	this.trainList = dataFromFile(fn);
    }
    public void createTuneList (String fn) {
    	this.tuneList = dataFromFile(fn);
    }
    public void createTestList (String fn) {
    	this.testList = dataFromFile(fn);
    }
    public void validateData() {
    	assert(this.featureList != null);
    	assert(this.trainList != null);
    	assert(this.tuneList != null);
    	assert(this.testList != null);
    	int sz = featureList.size();
    	for (Example ex : this.trainList) {
    		for (int i = 0; i < sz; i++) {
    			Feature feat = featureList.get(i);
    			String datum = ex.get(i);
    			if (!feat.isValid(datum)) {
    				System.out.println("Datum " + datum + " is not a possible value in Feature " + feat + ".");
    				System.exit(1);
    			}
    		}
    	}
    	for (Example ex : this.tuneList) {
    		for (int i = 0; i < sz; i++) {
    			Feature feat = featureList.get(i);
    			String datum = ex.get(i);
    			if (!feat.isValid(datum)) {
    				System.out.println("Datum " + datum + " is not a possible value in Feature " + feat + ".");
    				System.exit(1);
    			}
    		}
    	}
    	for (Example ex : this.testList) {
    		for (int i = 0; i < sz; i++) {
    			Feature feat = featureList.get(i);
    			String datum = ex.get(i);
    			if (!feat.isValid(datum)) {
    				System.out.println("Datum " + datum + " is not a possible value in Feature " + feat + ".");
    				System.exit(1);
    			}
    		}
    	}
    }
    public void createFeatureVectors() {
    	for (Example e : trainList) {
    		e.createFeatureVector(featureList);
    	}
    	for (Example e : tuneList) {
    		e.createFeatureVector(featureList);
    	}
    	for (Example e : testList) {
    		e.createFeatureVector(featureList);
    	}
    }

    public void createClass() {
    	Set<String> s = new HashSet<String> ();
    	for (Example e : trainList) {
    		if (!s.contains(e.getClass()))
    			s.add(e.getClassName());
    	}
    	List<String> ret = new ArrayList<String>(s.size());
    	ret.addAll(s);
    	this.cls = new Class(ret);
    }
    public void addClassValues() {
    	for (Example e : trainList) {
    		e.setClassValue(cls.get(e.getClassName()));
    	}
    	for (Example e : testList) {
    		e.setClassValue(cls.get(e.getClassName()));
    	}    	
    	for (Example e : tuneList) {
    		e.setClassValue(cls.get(e.getClassName()));
    	}
    }
    public void loadData(String train, String tune, String test) {
        createFeatureList(train);
        createTrainList(train);
        createTuneList(tune);
        createTestList(test);
        createClass();
        addClassValues();
        validateData();
        createFeatureVectors();
    }
    public double myDot(Example e, double[] w) {
    	double ret = 0.0;
    	for (int i = 0; i < e.featureVector.length; i++) {
    		ret += e.featureVector[i] * w[i];
    	}
    	ret += this.bias;
    	return ret;
    }
    private static double f(double x) {
    	if (x > 0) return 1.0;
    	return 0.0;
    }
    private static double[] vecAdd(double[] a, double [] b) {
    	double[] ret = new double[a.length];
    	for (int i = 0; i < ret.length; i++) {
    		ret[i] = a[i] + b[i];
    	}
    	return ret;
    }
    private static double[] vecSub(double[] a, double[] b) {
    	double[] ret = new double[a.length];
    	for (int i = 0; i < ret.length; i++) {
    		ret[i] = a[i] - b[i];
    	}
    	return ret;
    }
    public void updateWeights() {
    	for (Example e : this.trainList) {
    		double prediction = myDot(e, this.weights);
    		double c = f(prediction);
    		if (c == 1.0 && e.classValue == 0.0) {
    			this.weights = vecSub(this.weights, e.featureVector);
    			this.bias -= 1;
    		} else if (c == 0.0 && e.classValue == 1.0) {
    			this.weights = vecAdd(this.weights, e.featureVector);
    			this.bias += 1;
    		}
    	}
    }
    public double calcTuneError() {
    	double wrong = 0;
    	for (Example e : this.tuneList) {
    		double prediction = myDot(e, this.weights);
    		double c = f(prediction);
    		if (c != e.classValue) {
    			wrong++;
    		}
    	}
    	return wrong / this.tuneList.size();
    }
    public void epoch (int i) {
    	this.updateWeights();
    	double error = this.calcTuneError();
    	if (error < this.bestError) {
    		this.bestError = error;
    		this.bestWeights = Arrays.copyOf(this.weights, this.weights.length);
    		this.bestBias = this.bias;
    		this.generation = i;
    	}
    }
    public void train () {
    	assert(this.featureList != null);
    	assert(this.trainList != null);
    	assert(this.tuneList != null);
    	int sz = 0;
    	for (Feature f : featureList) {
    		sz += f.size();
    	}
    	this.bias = 0.0;
    	this.weights = new double[sz];
    	smallRandom(this.weights, 0.05);
    	for (int i = 0; i < this.numEpochs; i++) {
    		this.epoch(i);
    	}
    	System.out.println("The best generation is: " + this.generation + " out of " + this.numEpochs + ", with error rate: " + this.bestError
    			+ " and weights: " + Arrays.toString(this.bestWeights) + " and bias: " + this.bestBias);
    }
    public void test () {
    	assert(this.testList != null);
    	assert(this.bestWeights != null);
    	double wrong = 0;
    	for (Example e : testList) {
    		double prediction = myDot(e, this.weights);
    		double c = f(prediction);
    		if (c != e.classValue) {
    			wrong++;
    		}
    	}
    	double acc = wrong / this.tuneList.size();
    	System.out.println("The accuracy on the test set was: " + (1 - acc));
    }
    public static void main (String [] args) {
        if (args.length != 3) {
            System.out.println("Usage: <train-file> <tune-file> <test-file>");
            System.exit(1);
        }
        Lab1 perceptron = new Lab1(1000);
        perceptron.loadData(args[0], args[1], args[2]);
        perceptron.train();
        perceptron.test();
    }

    public Lab1(int ne) {
    	this.numEpochs = ne;
    }
}
