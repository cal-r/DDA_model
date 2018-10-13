
package simulator;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import simulator.configurables.ContextConfig.Context;
import simulator.util.USNames;

public class StimulusElement {
	private String name;
	private int timepoint = 0;
	public float alphaR = 0.5f;
	private float std = 1;
	private int microstimulusIndex = 100;
	private float activation = 0f;
	private float directActivation = 0f;
	private int phase = 0;
	private int totalStimuli;
	private int trialCount = 0;
	private int totalMax = 0;
	//private float[][][] elementCurrentWeights;
	private List<String> names;
	private float assoc;
	private float vartheta;
	private float[] variableSalience;

	private float[] csVariableSalience;
	private SimGroup group;
	private boolean firstPass = true;
	private float newValue = 0f;
	private boolean isStored = false;
	private boolean presenceMean;
	private int iti = 0;
	private boolean wasActive = false;
	private float[][][] subelementWeights;
	private float[][][] oldElementWeights;
	private int[] subelementActivations;
	private float subelementNumber = 1;
	private float cscLikeness;
	private boolean hasReset = false;
	private boolean subsetSet = false;
	private boolean disabled = false;
	//private TreeMap<String, ArrayList<Integer>> commonIndexSet;
	private float USCV = 2.5f;
	private float usPersistence;
	//private float[][] aggregateSaliences;

	private float startingAlpha = 1;
	public float alphaN;
	private float beta;
	private float salience;
	private float averageUSError;
	private float oldUSError;
	private float averageCSError;
	private float oldCSError;
	private String currentTrialString;
	private TreeMap<String,Integer> trialTypeCount;
	private int session;
	private long storeLong;
	private float generalActivation;
	private float difference;
	private float time;
	private float numerator;
	private int adj;
	private float currentVariableSalience;
	private float curSal;
	private float totalPrediction;
	private float totalWeight;
	private float fixer;
	private float maxDurationPoint2;
	private float maxDurationPoint;
	private float nE;
	private float nE2;
	private float selfDereferencer;
	private int index;
	public boolean isUS = false;
	public boolean isContext = false;
	private float microPlus;
	private Stimulus parent;
	private int ac1,ac2;
	private float[] totalUSError;
	private float[] totalCSError;
	private float maxActivation;
	//private float[][] eligibilities;
	private float[] presences;
	private float discount = 1;
	private float factor;
	private float ratio;
	private float eligi;
	private float ownActivation;
	private boolean outOfBounds;
	private float exponent;
	private float dis;
	private float x1;
	private float x2;
	private float x3;
	private float val1;
	private boolean c2;
	private boolean c1;
	private float asoc;
	private int count;
	private float totalActivation;
	//private TreeMap commonIndexes;
	//private TreeMap<String, ArrayList<Integer>> commonIndexSet;
	private float tempDelta;
	private float usSalience = 0.1f;
	private float asymptote;
	private String elementCurrentWeightsKey;
	private String eligibilitiesKey;
	private String aggregateActivationsKey;
	private int totalTrials;
	private String aggregateSaliencesKey;
	private String aggregateCSSaliencesKey;
	private float[] temp;
	private float[][] current;
	private Object ob3,ob2,ob1;
	private float combination = 1;
	public Stimulus a;
	public Stimulus b;
	private boolean kickedIn = false;
	private boolean isA = false;
	private boolean isB = false;
	private boolean notTotal =true;
	private float intensity = 1f;
	private float asy;
	private float commonDiscount;
	private double decay;
	private float ctxratio = 1;
	private float durationPoint;
	private float microIndex;
	private float denominator;
	private float totalElements;
	
	public StimulusElement(int microIndex,Stimulus parent,SimGroup group, String name, float alpha, float std, int trials, int totalStimuli, int totalMicros, int totalMax, float generalization, float lambdaPlus, float usBoost, float vartheta, boolean presenceMean,boolean esther) {
		this.microstimulusIndex = microIndex;
		this.totalTrials = trials+1;
		this.parent = parent;
		this.group = group;
		this.totalStimuli = totalStimuli;
		this.totalMax = totalMax;
		this.vartheta = vartheta;
		this.presenceMean = presenceMean;
		this.std = std;
		this.name = name;
        names = new ArrayList<String>(0);
        trialTypeCount = new TreeMap();
        if (USNames.isUS(getName())) {names.add(getName());}
        for (SimPhase sp : group.getPhases()) {
        	iti = (int) Math.max(iti,sp.getITI().getMinimum());
        }
        presences = new float[totalStimuli];
        totalCSError = new float[group.getNoOfPhases()];
        totalUSError = new float[group.getNoOfPhases()];
        
        temp = new float[totalMax + iti];

		//eligibilities = new float[totalStimuli][totalMax];
        variableSalience = new float[group.getNoOfPhases()];
        csVariableSalience = new float[group.getNoOfPhases()];
        elementCurrentWeightsKey = group.getNameOfGroup() + parent.getName()+this.microstimulusIndex + "elementCurrentWeights";
        eligibilitiesKey = group.getNameOfGroup() + parent.getName()+this.microstimulusIndex + "eligibilities";
        aggregateActivationsKey = group.getNameOfGroup() + parent.getName()+this.microstimulusIndex + "aggregateActivations";
        aggregateSaliencesKey = group.getNameOfGroup() + parent.getName()+this.microstimulusIndex + "aggregateSaliences";
        aggregateCSSaliencesKey = group.getNameOfGroup() + parent.getName()+this.microstimulusIndex + "aggregateCSSaliences";
        
        group.makeMap(elementCurrentWeightsKey+"");
        group.makeMap(eligibilitiesKey+"");
        group.makeMap(aggregateActivationsKey+"");
        group.makeMap(aggregateSaliencesKey+"");
        group.makeMap(aggregateCSSaliencesKey+"");
        for (int i = 0 ; i < group.getNoOfPhases(); i++) {
        	group.addToMap(i+"", new float[group.getNoOfPhases()][totalStimuli][totalMax], elementCurrentWeightsKey+"",true);
        }
        for (int i = 0; i < totalStimuli; i++) {
        	group.addToMap(i+"",new float[totalMax],eligibilitiesKey+"",true);
        }
        for (int i = 0; i < trials+1; i++) {
        	group.addToMap(i+"", new float[totalMax+iti], aggregateActivationsKey+"",true);
        	group.addToMap(i+"", new float[totalMax+iti], aggregateSaliencesKey+"",true);
        	group.addToMap(i+"", new float[totalMax+iti], aggregateCSSaliencesKey+"",true);
        }
       // aggregateActivations = new float[trials+1][totalMax+iti];
		//aggregateSaliences = new float[trials+1][totalMax+iti];
		//aggregateCSSaliences = new float[trials+1][totalMax+iti];
		//commonIndexes = new TreeMap();
		//commonIndexSet = new TreeMap<String,ArrayList<Integer>>();
		
		trialTypeCount = new TreeMap();

		factor = 10f;
		if (USNames.isUS(name)) isUS = true;
		if (Context.isContext(name)) isContext = true;
		adj = isUS ? 8 : 0;
		//usSalience = 0.1f;
	}
	
	
	public void setUSCV(float cv) {USCV = cv;}
	
	

	public float getAlpha() {
		return isUS ? beta*intensity : salience;
	}
	
	public void setSubElementWeights(int i, int j, int k, float val) {
		subelementWeights[i][j][k] = val;
	}
	
	public void setSubsetSize(int i) {
		
		if (!subsetSet) {
			totalElements = i;

			discount = group.getModel().getDiscount();

			dis = (float) Math.pow(discount, 10);
			exponent = dis == 0 ? 0 : Math.abs(1f/dis - 1);

			outOfBounds = false;
			if (Float.isNaN(exponent) || exponent > 20) {outOfBounds = true;}
			subsetSet = true;
			boolean isCommon = getName().length() > 1;
			boolean hasCommon = parent.getCommonMap().size() > 0;
			float commonProp = isCommon ? i*group.getModel().getCommon().get(group.getNameOfGroup()).get(getName()) : 0;
			float uniqueProp =  hasCommon ? i*(1-group.getModel().getProportions().get(group.getNameOfGroup()).get(getName())) : 0;
			float commonValue = isCommon ?  commonProp : (hasCommon ? uniqueProp : i);
			subelementNumber = (float) Math.floor(commonValue);
			subelementNumber = commonValue > 0 ? Math.max(1, subelementNumber) : subelementNumber;
			//System.out.println(group.getNameOfGroup() +" " + getName() + " " + subelementNumber +" " + isCommon +" " + hasCommon +" " + uniqueProp +" " + commonValue);
			if (isContext) {subelementNumber =1;}
			
			subelementWeights = new float[(int)subelementNumber][totalStimuli][totalMax];
			oldElementWeights = new float[(int)subelementNumber][totalStimuli][totalMax];
			
			//for (String name : commonRatios.keySet()) {
				///samples = 0;
				//while (samples < (int)Math.round(commonRatios.get(name)*subelementNumber)) {
					//newSample = (int)Math.round(Math.random()*subelementNumber);
					//if (commonIndexSet.get(name).contains(newSample)) {}
					//else {commonIndexSet.get(name).add(newSample); samples++;} 
				//}
		    	//if (!Context.isContext(name) && !USNames.isUS(name) || name.equals(getName()))commonIndexes.put(name, (int)Math.round(commonRatios.get(name)*subelementNumber));
			//}
			}
		subelementActivations = new int[(int)subelementNumber];
		//subelementTrialActivations = new int[(int)subelementNumber];
		//subelementSampled = new int[(int)subelementNumber];
		//for (int j = 0; j < subelementActivations.length; j++) {
			//subelementSampled[j] = 0;
			
		//}
		//lastsubelementActivations = new int[(int)subelementNumber];
	}
	
	public float getDirectActivation() {
		
		return  directActivation;
	}
	
	
	public String getName() {
		return name;
	}
	
	public int getTotalMax() {return totalMax;}

	
	public void nextCombination() {
		combination++;
		session = 1;
		trialTypeCount.clear();
		//group.addToMap(phase+"", new float[totalStimuli][totalMax], elementCurrentWeightsKey+"",true);
	}
	
	public void incrementSession() {
		session++;
		trialTypeCount.clear();
	}
	
	public void setPhase(int phase) { 
		session =1;
		combination = 1;
		trialTypeCount.clear();
		if (phase > this.phase) {
			
			for (int i = 0; i < subelementWeights.length; i++) {
				for (int j = 0; j < subelementWeights[0].length; j++) {
					for (int k = 0; k < subelementWeights[0][0].length; k++) {
						oldElementWeights[i][j][k] = subelementWeights[i][j][k];
						
					}
				}
			}
		}
		this.phase = phase;
	if (phase == 0 ){
		subelementWeights = new float[(int)subelementNumber][totalStimuli][totalMax];
		kickedIn = false;
		variableSalience[0] =alphaR;
		csVariableSalience[0] = alphaN;
		totalCSError[phase] = 0;
		totalUSError[phase] = 0;
		group.addToMap(0+"", new float[totalStimuli][totalMax], elementCurrentWeightsKey+"",true);
	}
	if (phase > 0 ) {
		totalCSError[phase] = totalCSError[phase-1];
		totalUSError[phase] = totalUSError[phase-1];
		variableSalience[phase] = variableSalience[phase-1];

		csVariableSalience[phase] = csVariableSalience[phase-1];
			for (int j = 0; j < (subelementNumber != 0 ? subelementWeights[0].length : 0); j++) {
				for (int k = 0; k < (subelementNumber != 0 ? subelementWeights[0][0].length : 0); k++) {

					for (int i = 0; i < subelementWeights.length; i++) {
					subelementWeights[i][j][k] = oldElementWeights[i][j][k];
					}
				//elementCurrentWeights[phase][j][k]  = elementCurrentWeights[phase-1][j][k] ;
				
			}
		}

			float[][] temp = (float[][])group.getFromDB((phase-1)+"",elementCurrentWeightsKey);
			if (temp != null) group.addToMap(phase+"", temp, elementCurrentWeightsKey+"",true);
			else {
				temp = new float[totalStimuli][totalMax];
				 group.addToMap(phase+"", temp, elementCurrentWeightsKey+"",true);
			
			//elementCurrentWeights[phase-1] = null;
			//group.addToMap((phase-1)+"", null, elementCurrentWeightsKey);

			if (phase -2 >= 0 ) group.getMaps().get(elementCurrentWeightsKey).remove((phase-2)+"");
	
			}
		}
	}
	
	public void reset(boolean last,int currentTrials) {
		activation = 0f;
		assoc = 0;
		if (phase > 0 ) {
			totalCSError[phase] = totalCSError[phase-1];
			totalUSError[phase] = totalUSError[phase-1];

			variableSalience[phase] = variableSalience[phase-1];

			csVariableSalience[phase] = csVariableSalience[phase-1];
				for (int j = 0; j < subelementWeights[0].length; j++) {
					for (int k = 0; k < subelementWeights[0][0].length; k++) {
						for (int i = 0; i < subelementWeights.length; i++) {
						subelementWeights[i][j][k] = oldElementWeights[i][j][k];
						}
						//elementCurrentWeights[phase][j][k]  = elementCurrentWeights[phase-1][j][k] ;
						
				}
			}
				current = (float[][])group.getFromDB((phase-1)+"",elementCurrentWeightsKey);
				if (current != null ) group.addToMap(phase+"", current, elementCurrentWeightsKey+"",true);
				
				else {
					current = new float[totalStimuli][totalMax];
					 group.addToMap(phase+"", current, elementCurrentWeightsKey+"",true);
				
				//elementCurrentWeights[phase-1] = null;
				//group.addToMap((phase-1)+"", null, elementCurrentWeightsKey);


				if (phase -2 >= 0 ) group.getMaps().get(elementCurrentWeightsKey).remove((phase-2)+"");
		}
		if (phase == 0) {
			subelementWeights = new float[(int)subelementNumber][totalStimuli][totalMax];
			group.addToMap(0+"", new float[totalStimuli][totalMax], elementCurrentWeightsKey+"",true);
		}
		if (subelementActivations != null) {
		for (int i = 0; i < subelementActivations.length; i++) {
			subelementActivations[i] = 0;
		}
		}
		//eligibilities = new float[totalStimuli][totalMax];
		//eligibilitiesKey = group.getNameOfGroup() + parent.getName()+this.microstimulusIndex + "eligibilities";
        
       // group.makeMap(eligibilitiesKey);
        for (int i = 0; i < totalStimuli; i++) {
        	group.addToMap(i+"",new float[totalMax],eligibilitiesKey+"",true);
        }
		timepoint = 0;
		directActivation = 0;
		if (group.getPhases().get(phase).isRandom() && phase == 0) {variableSalience[phase] = alphaR;csVariableSalience[phase] = alphaN;}
		wasActive = false;
		firstPass = true;
		isStored = false;
		}

		trialCount -= Math.min(trialCount,currentTrials);
	}
	
	public String incrementTimepoint(int time) {
		hasReset = false;
		
		
				
		
		return elementCurrentWeightsKey;
	}
	
	
	
	
	public void resetForNextTimepoint() {
		if (!hasReset) {
			maxActivation = 0;
			hasReset = true;
			isStored = false;
			firstPass = true;
			activation = 0;
			subelementActivations = new int[(int) subelementNumber];
			//for (int i = 0; i < subelementNumber; i++) {
				//lastsubelementActivations[i] = subelementActivations[i];
				//subelementActivations[i] = 0;
				//subelementSampled[i] =0;
			//}
			
		}
	}


	public void setActive(String name, boolean b, float durationPoint) {
		if (b && !disabled) wasActive = true;
		this.durationPoint = durationPoint;
		if (!isA) isA = (a == null) ? false : (a.getHasBeenActive());
		if (!isB) isB = (this.b == null) ? false : (this.b.getHasBeenActive());
		
		setParams();
	}
	
	public void setTrialLength(int trialLength) {
	microIndex = microPlus;//microIndexes2.put(name,microPlus);
	//denominators2.put(name,(float)( 2f*Math.pow((isUS ? Math.sqrt((microPlus+ adj)*USCV*ctxratio) : Math.sqrt((microPlus+ adj)*std*ctxratio)), 2f)));
	denominator = (float)( 2f*Math.pow((isUS ? Math.sqrt((microPlus+ adj)*USCV*ctxratio) : Math.sqrt((microPlus+ adj)*std*ctxratio)), 2f));
	//if (denominator == 0) {denominator = 1;}
	ctxratio =isContext ? (trialLength/(parent.getList().length + 0f))*(10f/10f) : 1f;
	ratio = isContext ? (trialLength/(parent.getList().length + 0f))*(10f/10f) : 1f;}

	public void setRAlpha(float alpha) {
		this.alphaR = alpha;
		variableSalience[0] = alpha;
	}
	public void setNAlpha(float alphaN) {
		this.alphaN = alphaN;
		csVariableSalience[0] = alphaN;
	}
	public void setBeta(float beta) {
		this.beta = beta;
	}
	
	public void setSalience(float salience) {
		this.salience = salience;
	}
	
	public float getWeights(int i, int j) {
		return ((float[][])group.getFromDB(phase+"",elementCurrentWeightsKey))[i][j];
	}
	
	public void setNextString(String nextTrials) {
		
	}

	public void store() {
		if (!isStored) {
			timepoint = 0;
			directActivation = 0;
			subelementActivations = new int[(int)subelementNumber];
			trialCount++;
			isStored = true;
		}
	}

	@Override
	public String toString() {
		return name + " α(" + alphaR + ")";
	}
	
	public float getGeneralActivation() {
		
		return generalActivation;
	}
	
	
	int randomWithRange(int min, int max)
	{
	   int range = (max - min) + 1;     
	   return (int)(Math.random() * range) + min;
	}
	
	public boolean isCommon() {return getName().length() > 1;}
	
	public void updateAssocTrace(float assoc) {
		asoc = Math.max(0, Math.min(1f, assoc*vartheta));
		
		this.assoc = asoc;
		
		//bufferSize = (int) Math.ceil(group.getModel().getBufferSize()*subelementNumber);
		count = 0;
		totalActivation = Math.max(asoc, directActivation);
		while (count < subelementNumber) {
			int i = randomWithRange(0,(int)(subelementNumber-1));
			
			//if (subelementSampled[i] == 1) {}
			//else {
					if (subelementActivations[i] == 0) count++;
					//subelementSampled[i] = 1;
					/*for (String name : names) {
						ctxCon = (isContext && (name.equals(getName())))|| (!isContext);
						usCon = (isUS && (name.equals(getName())))|| (!isUS);
						condition = ctxCon && usCon;
						if (condition && getName().equals(name)) {
							subelementActivations[i] = Math.max(subelementActivations[i], Math.random() < (Math.max(asoc, allActivations.get(name))) ? 1 : 0);
						}
					}*/
					//if (!names.contains(getName())) {
						subelementActivations[i] = subelementActivations[i] == 1 || Math.random() < totalActivation ? 1 : 0;
					//}
					//if (subelementActivations[i] == 1) {subelementTrialActivations[i] = 1;}
			//}
		}
		
	
		
		generalActivation = Math.max(0, Math.max(this.assoc, activation));
		
		temp = new float[totalMax + iti];
		ob3 = group.getFromDB(trialCount+"", aggregateActivationsKey);
		if (ob3 != null) {
			temp = (float[]) ob3;
		}
		temp[timepoint] = activation;
		group.addToMap(trialCount+"", temp, aggregateActivationsKey+"",true);

	}
	
	public boolean isAfter(StimulusElement e) {
		
		maxDurationPoint = durationPoint;
		maxDurationPoint2 = e.getDurationPoint();
		
		if (directActivation > 0.1) {
	
		if (maxDurationPoint >= maxDurationPoint2) {return false;}
		else {return true;}
		}
		return false;
	}
	
	public float getAsymptote() {
		
		
		if (timepoint <= parent.getLastOnset()) {if (assoc > 0.9 ||  getDirectActivation() > 0.1*intensity) {asy = 1;} else {asy = 0;}}
		else {if (getDirectActivation() > 0.1*intensity) {asy = 1;} else { asy = 0;}}
		return asy;//getGeneralAc() > 0.2 ? 1 : 0;
		//return (parent.getWasActiveLast() || directActivation > 0.1) ? 1 : 0;
	}
	
	
	public boolean wasActiveLastTimestep () {return wasActive;}
	
	public void setVartheta(float vartheta) {this.vartheta = vartheta;}
	public float getVartheta(){return vartheta;}
	public Stimulus getParent() {return parent;}
	public float getDurationPoint() {return durationPoint;}
	public float getAssoc(){return assoc;}
	public void updateElement(float otherActivation,float otherAlpha,StimulusElement other, float ownError, float otherError, String otherName, SimGroup group) {

		
		
		nE = otherError;
		nE2 = Math.abs(ownError);
		//
		//bothAbsent = ac1 == 0 && ac2 == 0;
		//bothActive = ac1 == 1 && ac2 == 1;
		c1 = (names.indexOf(otherName) != -1);
		ob1 = group.getFromDB(names.indexOf(otherName)+"",eligibilitiesKey);
		temp = new float[totalMax];
		if (ob1 != null) {
			temp = (float[]) ob1;
		}
		val1 = c1 ? other.getAssoc()/(temp[other.getMicroIndex()]+0.001f) : 1f;
		c2 = (other.getAssoc() == 0 ? 1f : val1) > 0.9;
		if (c1 && c2){presences[names.indexOf(otherName)] = (other.parent.getWasActive());}
		if (c1) {
			temp[other.getMicroIndex()] = Math.max(temp[other.getMicroIndex()]*0.95f, other.getAssoc());//(float) Math.pow(0.95f, Math.round(50f*Math.abs(generalActivation - rf)/(1f-rf)));
			group.addToMap(names.indexOf(otherName)+"", temp,eligibilitiesKey+"",true);
		}
		eligi = 0;
		if (!outOfBounds) {eligi = (float) Math.pow((other.getAssoc() == 0 ? 1f : (other.getAssoc()/(temp[other.getMicroIndex()]+0.001f))),exponent);
		}
		else { eligi = 0.01f;}
		if (eligi > 0 && eligi < 0.01) {eligi = 0.01f;}
		ac1 = (int) getAsymptote();//getDirectActivation() > 0.1 ? 1 : 0;
		ac2 =  (int)(other.getAsymptote());//other.getDirectActivation() > 0.1 ? 1 : 0;
		
		//
		selfDereferencer=1f;
		if (getName().contains(other.getName()) || other.getName().contains(getName())) {selfDereferencer = this.isUS ? 0 : 0.01f;}
		index = names.indexOf(otherName);
		if (index == -1) {
			names.add(otherName);
			index = names.indexOf(otherName);
		} else {
		}
		maxDurationPoint = durationPoint;
		maxDurationPoint2 = other.getDurationPoint();

		fixer = 1;
		if (directActivation > 0.1) {
		if (maxDurationPoint >= maxDurationPoint2) {fixer = 1f;}
		else {fixer = parent.getB();}
		}
		totalWeight = 0f;
		totalPrediction = 0f;
		//
		x1 = Math.min(1, Math.max(ac1, assoc*0.9f));
		x2 = Math.min(1, Math.max(ac2, other.getAssoc()*0.9f));
		x3 = (fixer*x2 - Math.abs(x1-x2))/Math.max(x1+0.001f, x2+0.001f);
		nE = (otherError - ac2*1f) + x3*1f;
		if (other.isUS) {asymptote = nE;}
		currentVariableSalience = (other.isUS ? (variableSalience[phase] ) : (csVariableSalience[phase]));// /(variableSalience[phase] + csVariableSalience[phase] + 0.001f);
		curSal = isUS ? beta*intensity : salience;
		commonDiscount = isCommon() ? (subelementNumber/totalElements) : parent.getCommonMap().size() > 0 ? (1f-group.getModel().getProportions().get(group.getNameOfGroup()).get(getName())) : 1f;
		//System.out.println(group.getNameOfGroup() +" " + getName() +" " + commonDiscount);
		//if (phase == 0 && combination == 1 &&  microstimulusIndex == 0 && getName().contains("AC") && timepoint == 3 && other.isUS) {
			//System.out.println("AC " + subelementNumber + " " + commonDiscount + " " + generalActivation + " " + curSal +"  " + eligi + " " + currentVariableSalience + " " + nE);
		//}
		
		//float otherDiscount = 1f;//other.isCommon() ? group.getCommon() : other.parent.getCommonMap().size() > 0 ? 1f - group.getCommon() : 1f;
		tempDelta = (1f/((float)subelementNumber*parent.getList().length))*commonDiscount*generalActivation*other.getAlpha()*curSal*other.getGeneralActivation()*eligi*selfDereferencer*currentVariableSalience*nE*nE2;
		tempDelta *= isUS ? usSalience*intensity : 1f;
		//if (parent.isContext && other.isUS && timepoint == 4) {System.out.println("context " + timepoint + " "+ directActivation + " "+ x1 + " " + x2 + " " + x3 + " " + nE + " " +otherError + " " + tempDelta);}
		
		decay = (ownActivation > 0.01 ? (1-(Math.sqrt(curSal)/10000f)) : 1f);
		for (int i = 0; i < subelementNumber; i++) {
			
			//if (getName().equals("A")) {totalMicros =10; curSal = 0.5f;}
			
			//delta += (float) ((1f/((float)totalMicros*subelementNumber*totalMicros))*generalActivation*other.getGeneralActivation()*eligi*subelementActivations[i]*fixer*curSal*selfDereferencer*currentVariableSalience*nE*nE2);
			subelementWeights[i][index][other.getMicroIndex()] = Math.max(-2f/((float)subelementNumber), Math.min(2f/((float)subelementNumber),(float) (subelementWeights[i][index][other.getMicroIndex()]*decay + tempDelta*subelementActivations[i])));//;Math.max(-1/subelementNumber, Math.min(1/subelementNumber, subelementWeights[i][index][other.getMicroIndex()] + delta));
			//subelementWeights[i][index][other.getMicroIndex()] = Math.max(-2f/((float)subelementNumber), Math.min(2f/((float)subelementNumber), subelementWeights[i][index][other.getMicroIndex()]));
			
	    	/*	float actDiscount = 0;
	    		
	    		float count = 0;
	    		if ((i < Math.ceil(group.getSubElementNumber()*group.getCommon())) && !(isUS || isContext)){
		    		for (StimulusElement se : group.getPhases().get(this.phase).getSubsets().get(microstimulusIndex)) {
		        		count += se.getSubActivation(i);
		        	}
		    		if (count == 0) {count = 1;}
		    		actDiscount = 1f/count;
	    		} else {actDiscount = 1f;}*/
			totalWeight +=subelementWeights[i][index][other.getMicroIndex()];
			totalPrediction += subelementWeights[i][index][other.getMicroIndex()]*generalActivation;
			
			}
		if (group.getModel().isExternalSave()) {
		storeLong = group.createDBString(this,currentTrialString, other.getParent(),phase, session, trialTypeCount.get(currentTrialString), timepoint, true);
		group.addToDB(storeLong, totalPrediction);
		storeLong = group.createDBString(this,currentTrialString, other.getParent(),phase, session, trialTypeCount.get(currentTrialString), timepoint, false);
		group.addToDB(storeLong, totalWeight);
		}
		ob2 = group.getFromDB(phase+"",elementCurrentWeightsKey+"");
		current = new float[totalStimuli][totalMax];
		if (ob2 != null) {current = (float[][])ob2;}
		current[index][other.getMicroIndex()] =totalWeight;
		group.addToMap(phase+"",current, elementCurrentWeightsKey+"",true);
		
		if (firstPass) {
			timepoint++;
			firstPass = false;
		}
	}


	
	
	public float getTimeDifference() {return difference;}
	
	public float getIntensity() {return intensity;}
	public void updateActivation(String name, float presence, float duration,int microstimulusIndex) {
		
		
		this.microstimulusIndex = microstimulusIndex;
		microPlus = microstimulusIndex+1;
		time = presenceMean ? 1-presence : durationPoint;
		if (USNames.isUS(name)) {
			if (time >= microIndex) {
				time = Math.max(0, time - usPersistence);
			}
		}
		difference = time-(ratio*(microIndex-1));
		if (difference < 0) {difference*= cscLikeness;}
		if (isUS) {if (difference < 0) {difference = 0;}}
		numerator = (float) Math.pow(difference,2);
		newValue = presence > 0 ? (float) Math.exp(-(numerator)/denominator) : 0;
		if (Double.isNaN(numerator) || Double.isInfinite(numerator)|| Double.isNaN(denominator) || Double.isInfinite(denominator)||Double.isNaN(newValue) ) {
			//while (true) {System.out.println(denominator +" " + numerator +" " + difference +" " + cscLikeness +" " + time +" " + ratio +" " + timepoint +" " + getName());}
		}
		if (isContext) {newValue*=presence;}
		maxActivation = Math.max(maxActivation, newValue);
		if (name.equals(getName())){activation = newValue*intensity;}
		//if (isContext) { activation = activation*0.7f +0.3f*(isActive() ? 1 : 0);}
		activation = disabled? 0 :activation;
		if (getName().equals(name))directActivation = activation;
		
	}
	
	public float[][][] getSubElementWeights() {return subelementWeights;}
	
	public void normalizeActivity(float norm) {
		directActivation /= norm; activation /= norm;
		
	}
	
	public int getMicroIndex() {return microstimulusIndex;}

  
    public List<String> getNames() {
    	return names;
    	
    }
   
    
    public float getAggregateActivation(int trial, int timepoint) {
    	temp = new float[totalMax + iti];
		ob3 = group.getFromDB(trial+"", aggregateActivationsKey);
		if (ob3 != null) {
			temp = (float[]) ob3;
			return temp[timepoint];
		} else {return 0f;}
    }
    public float getAggregateSalience(int trial, int timepoint) {
    	temp = new float[totalMax + iti];
		ob3 = group.getFromDB(trial+"", aggregateSaliencesKey);
		if (ob3 != null) {
			temp = (float[]) ob3;
			return temp[timepoint];
		} else {return 0f;}
    }
    
    public float getAggregateCSSalience(int trial, int timepoint) {
    	temp = new float[totalMax + iti];
		ob3 = group.getFromDB(trial+"", aggregateCSSaliencesKey);
		if (ob3 != null) {
			temp = (float[]) ob3;
			return temp[timepoint];
		} else {return 0f;}
    }
    
    
    public void setDisabled(boolean disabled){this.disabled = disabled;}
    
    public void deletePredictions(){
    	subelementWeights = null; oldElementWeights = null;group.removeMap(elementCurrentWeightsKey);}
    public float getSubActivation(int index) {return subelementActivations[index];}
    
    public float getPrediction(int stimulus,int element,boolean current, boolean maximum) {
    	
    	totalPrediction = 0f;
    	for (int i = 0; i < subelementNumber; i++){
    		/*float actDiscount = 0;
    		float count = 0;
    		if ((i < Math.ceil(group.getSubElementNumber()*group.getCommon())) && !(isUS || isContext)){
	    		for (StimulusElement se : group.getPhases().get(this.phase).getSubsets().get(microstimulusIndex)) {
	        		count += se.getSubActivation(i);
	        	}
	    		if (count == 0) {count = 1;}
	    		actDiscount = 1f/count;
    		} else {actDiscount = 1f;}*/
    		//float act = ((!maximum) ? (current ? (subelementActivations[i] < 1 ? assoc : 1f) :  (lastsubelementActivations[i] < 1 ? lastAssoc : 1f)) : 1f);
    		totalPrediction += subelementWeights[i][stimulus][element]*(directActivation > 0.1 ?  1f : generalActivation*vartheta);
    		
    	}
    	if (names.indexOf(getName())==(stimulus)) {
    		//totalPrediction*= selfDiscount;
    	}

		 
    	return totalPrediction;
    }
    
    public void setTotalMax(int total) {
    }
    
    
    public void nullPredictions(int phase) {
    	
    }
    public void setVariableSalience(float vs) {
    	temp = new float[totalMax + iti];
		ob3 = group.getFromDB(trialCount+"", aggregateSaliencesKey);
		if (ob3 != null) {
			temp = (float[]) ob3;
		}
		temp[timepoint] = (temp[timepoint]*(combination-1) + variableSalience[phase])/combination;
		group.addToMap(trialCount+"", temp, aggregateSaliencesKey+"",true);
    	this.variableSalience[phase] =  vs <= 0.001 ? 0 : vs;
    	}
    public void setCSVariableSalience(float vs) {
    	temp = new float[totalMax + iti];
		ob3 = group.getFromDB(trialCount+"", aggregateCSSaliencesKey);
		if (ob3 != null) {
			temp = (float[]) ob3;
		}
		temp[timepoint] = (temp[timepoint]*(combination-1) + csVariableSalience[phase])/combination;;
		group.addToMap(trialCount+"", temp, aggregateCSSaliencesKey+"",true);
    	this.csVariableSalience[phase] = vs <= 0.001 ? 0 : vs;
    	}
    public void setZeroProbe() {
		timepoint = 0;
		
    }
    
    public void setCSCLike(float c){ cscLikeness = c;}
    
    public void resetActivation() {
		activation = 0f;
		directActivation = 0;
		subelementActivations = new int[(int)subelementNumber];
    }
    
    
    public void setStartingAlpha(float s) {startingAlpha = s;
    
    csVariableSalience[0] =startingAlpha;
	}
    
    public String getAggregateActivations() {return aggregateActivationsKey;}
    public String getAggregateSaliences() {return aggregateSaliencesKey;}
    public String getAggregateCSSaliences() {return aggregateCSSaliencesKey;}

	public float getVariableSalience() {
		return variableSalience[phase];
	}
	public float getCSVariableSalience() {
		return csVariableSalience[phase];
	}
	public void setCSScalar(float csScalar) {
		this.std *=(csScalar*csScalar);
	}
	
	public void setUSPersistence(float p) {usPersistence = p;}
	public void setCSCV(float cscv) {
		std = cscv;
		
	}
	public void storeAverageUSError(float d, float act) {
		oldUSError = averageUSError;
		if (averageUSError == 0) {averageUSError = Math.max(0,alphaR);
		}
		else averageUSError = averageUSError*(1-act/factor) + d*act/factor;
	}
	public float getCurrentUSError() {
		return averageUSError;
	}
	
	public float getOldUSError() {
		return oldUSError;
	}
	
	public void storeAverageCSError(float d, float act) {
		oldCSError = averageCSError;
		if (averageCSError == 0) {averageCSError = Math.max(0,alphaN);}
		else averageCSError = averageCSError*(1-act/factor) + d*act/factor;
	}
	public float getCurrentCSError() {
		return averageCSError;
	}
	public float getOldCSError() {
		return oldCSError;
	}
	
	public void setUSScalar(float usScalar) {
		USCV*=(usScalar*usScalar);
	}


	public void setCurrentTrialString(String currentSeq) {
		if (trialTypeCount.containsKey(currentSeq)) {
			trialTypeCount.put(currentSeq, trialTypeCount.get(currentSeq)+1);
		}
		
		else {trialTypeCount.put(currentSeq, 1);}
		currentTrialString = currentSeq;
	}


	public float getTotalError(float abs) {
		totalUSError[phase] = 0.9997f*totalUSError[phase] + 0.0003f*abs;
		return totalUSError[phase];
	}
	
	public float getTotalCSError(float abs) {
		totalCSError[phase] = 0.99997f*totalCSError[phase] + 0.00003f*abs;
		return totalCSError[phase];
	}
	
	public void reset () {
		notTotal = true;
		isA = false;
		isB = false;
	this.name = name;
    names = new ArrayList<String>(0);
    trialTypeCount = new TreeMap();
    if (USNames.isUS(getName())) {names.add(getName());}
    for (SimPhase sp : group.getPhases()) {
    	iti = (int) Math.max(iti,sp.getITI().getMinimum());
    }
    presences = new float[totalStimuli];
    totalCSError = new float[group.getNoOfPhases()];
    totalUSError = new float[group.getNoOfPhases()];
    oldUSError = 0;
	averageUSError = 0;
	oldCSError = 0;
	averageCSError = 0;
    variableSalience = new float[group.getNoOfPhases()];
    csVariableSalience = new float[group.getNoOfPhases()];
	//elementCurrentWeights = new float[group.getNoOfPhases()][totalStimuli][totalMax];
    elementCurrentWeightsKey = group.getNameOfGroup() + parent.getName()+this.getMicroIndex() + "elementCurrentWeights";
   // group.removeMap(elementCurrentWeightsKey);
    group.clearMap(elementCurrentWeightsKey);
   // group.makeMap(elementCurrentWeightsKey);
    for (int i = 0 ; i < group.getNoOfPhases(); i++) {
    	group.addToMap(i+"", new float[group.getNoOfPhases()][totalStimuli][totalMax], elementCurrentWeightsKey+"",true);
    }
	//aggregateActivations = new float[aggregateActivations.length][totalMax+iti];
    //group.removeMap(aggregateActivationsKey);
    group.clearMap(aggregateActivationsKey);
    //group.removeMap(aggregateSaliencesKey);
    group.clearMap(aggregateSaliencesKey);
   // group.removeMap(aggregateCSSaliencesKey);
    group.clearMap(aggregateCSSaliencesKey);
    for (int i = 0; i < totalTrials; i++) {
    	group.addToMap(i+"", new float[totalMax+iti], aggregateActivationsKey+"",true);
    	group.addToMap(i+"", new float[totalMax+iti], aggregateSaliencesKey+"",true);
    	group.addToMap(i+"", new float[totalMax+iti], aggregateCSSaliencesKey+"",true);
    }
	//aggregateSaliences = new float[totalTrials][totalMax+iti];
	//aggregateCSSaliences = new float[totalTrials][totalMax+iti];
	
	trialTypeCount = new TreeMap();
	//group.removeMap(eligibilitiesKey);
	group.clearMap(eligibilitiesKey);
    for (int i = 0; i < totalStimuli; i++) {
    	group.addToMap(i+"",new float[totalMax],eligibilitiesKey+"",true);
    }
	if (USNames.isUS(name)) isUS = true;
	if (Context.isContext(name)) isContext = true;
	}

	public float getSTD() {return std;}

	public void initialize(Stimulus a, Stimulus b) {
		this.a = a;
		this.b = b;
		setParams();
	}
	
	public void setParams() {
		if (getName().contains("c")) {
		if (isA &&  isB && notTotal) {
			std = a.getList()[0].getSTD()/2f + b.getList()[0].getSTD()/2f;
			alphaR = a.getRAlpha()/2f + b.getRAlpha()/2f;
			alphaN = a.getNAlpha()/2f + b.getNAlpha()/2f;
			if (trialCount == 0) {
				variableSalience[0] = alphaR;
				csVariableSalience[0] = alphaN;
			}
			variableSalience[phase] = alphaR;
			csVariableSalience[phase] = alphaN;
			salience = a.getSalience()/2f + b.getSalience()/2f;
			cscLikeness = a.getCSCLike()/2f + b.getCSCLike()/2f;

			averageUSError = Math.max(0,alphaR);
			averageCSError = Math.max(0,alphaN);
			notTotal = false;
			kickedIn = true;
		} else if (isA && kickedIn == false) {
			std = a.getList()[0].getSTD();
			alphaR = a.getRAlpha();
			alphaN = a.getNAlpha();
			if (trialCount == 0) {
				variableSalience[0] = alphaR;
				csVariableSalience[0] = alphaN;
			}

			averageUSError = Math.max(0,alphaR);
			averageCSError = Math.max(0,alphaN);
			salience = a.getSalience();
			cscLikeness = a.getCSCLike();
			kickedIn = true;
		}
		else if (isB && kickedIn == false) {
			std = b.getList()[0].getSTD();
			alphaR = b.getRAlpha();
			alphaN = b.getNAlpha();
			if (trialCount == 0) {
				variableSalience[0] = alphaR;
				csVariableSalience[0] = alphaN;
				}

			averageUSError = Math.max(0,alphaR);
			averageCSError = Math.max(0,alphaN);
			salience = b.getSalience();
			cscLikeness = b.getCSCLike();
			kickedIn = true;
		}
		}
	}
	

	public float getOmicron() {return usSalience;}
	public void setUSSalience(float usS) {
		//this.usSalience = usS;
		
	}
	
	public int getTotalDuration() {return totalMax + iti;}

	public void setIntensity(float f) {intensity = f;}

	public float getAsym() {
		return asymptote;
		
	}
    
}