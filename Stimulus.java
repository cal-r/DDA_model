package simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import simulator.configurables.ContextConfig.Context;
import simulator.util.USNames;

public class Stimulus {

	protected StimulusElement[] cues;
	private String symbol;
	private boolean microsSet = false;
	private boolean startDecay = false;
	private int durationPoint = 0;
	private float alphaR = 0.5f;
	private float tau1 = 1f;//0.99995f;
	private float tau2 = 1f;//0.9999f;
	private float vartheta = 0.5f;
	private float std = 5;
	private int trialCount;
	private float[] averagePrediction;
	private float[] predictionsThisTrial;
	private float[] averageError;
	private float[] errorsThisTrial;
	private boolean[][] activity;
	private boolean[][] normalActivity;
	private float[] averageAverageWeights;
	private float[] averageAverageWeightsA;
	private float[][][] deltaWs;
	private float[][][][] trialW;
	private float[][][][] trialWACompounds;
	private float[][][][] orgTrialW;
	private float[][][][] trialWA;
	private float[][][][] orgTrialWA;
	private float[][][] randomTrialWA;
	private List<String> names;
	private float presenceTrace = 0f;
	private boolean active = false;
	private int timepoint = 0;
	private int onset = 0;
	private int trialLength = 0;
	private boolean isProbe = false;
	private boolean hasBeenActive = false;
	private float maxDuration = 0;
	private int totalMax = 0;
	private float currentDuration = 0;
	private ArrayList<Integer> durationList;
	private ArrayList<Integer> completeDurationList;
	private ArrayList<String> trialTypes;
	private float[] tempValues;
	private int trials = 0;
	private int totalStimuli;
	private SimGroup group;
	private ArrayList<Float> presenceValues;
	private ArrayList<ArrayList<Float>> presenceByTrial ;
	private float lambdaPlus = 1;
	private boolean presenceMean;
	private int lastOnset = 0;
	private int lastOffset = 0;
	private int currentPhase = 0;
	private float usBoost = 0.08f;
	private float cscLike = 2f;
	private boolean disabled = false;
	private boolean[] wasActiveLast;
	private float alphaN;
	private float salience;
	private float beta;
	private float presenceMax = 1;
	private boolean update;
	private boolean[][] associates;
	private double thisPhaseMax = 1;
	private int ctxReset = 2;
	private float contextReset = 0.99f;
	private boolean resetContext;
	private float[] averageWeightsA;
	private boolean associate;
	private int predictionSum;
	private int errorSum;
	private float[] averageWeights;
	private float[][] tempMap;
	private float tempW;
	private int ctxFix;
	private float divider;
	private float combinedPresence;
	private SimPhase currPhase;
	private float updateDuration;
	private int iti;
	public boolean isUS = false;
	public boolean isContext = false;
	private float startPresence = 0;
	private boolean isContextValuesSet;
	private float deltaW;
	private float oldV;
	private int a_onset;
	private int a_offset;
	private TreeMap<String, Stimulus> commonMap;
	public boolean isCS;
	private float omicron;
	private float[][][] asymptotes;
	private String key;
	private Object obj;
	private float asymptoteMean;
	private boolean[][] trialStringActivity;
	private int microstimulusIndex;
	private TreeMap<String,Integer> trialTypeMap;
	private TreeMap<String,Integer> trialTypeMap2;
	private float maxITI = 0;
	private int randomTrialCount = 0;
	private float b = 1;
	private int combinations;
	public Stimulus aStim,bStim;
	private float[][][] randomTrialWAUnique;
	private float trialTimepointsCounter = 0;
	private boolean notDivided = true;
	
	public Stimulus(SimGroup group, String symbol, float alpha, int trials, int totalStimuli) {
		this.group = group;
		this.alphaR = alpha;
		this.symbol = symbol;
		this.trials = trials;
		this.totalStimuli = totalStimuli;
		trialCount = 0;
		trialTypeMap = new TreeMap();
		trialTypeMap2 = new TreeMap();
		averageAverageWeights = new float[totalStimuli];
		averageAverageWeightsA = new float[totalStimuli];
		averagePrediction = new float[trials];
		averageError = new float[trials];
		durationList = new ArrayList<Integer>();
		completeDurationList = new ArrayList<Integer>();
		names = new ArrayList<String>(0);
		presenceValues = new ArrayList();
		presenceByTrial = new ArrayList();
		presenceByTrial.add(new ArrayList<Float>());
		tempValues = new float[totalStimuli];
		wasActiveLast = new boolean[group.getNoOfPhases()];
		associates = new boolean[trials][totalStimuli];
		trialTypes = new ArrayList();
		commonMap = new TreeMap<String,Stimulus>();
		ctxFix = Context.isContext(getName()) ? 1 : 0;
		if (USNames.isUS(symbol)) isUS = true;
		if (Context.isContext(symbol)) isContext = true;
		isCS = !isUS && !isContext;
		combinations = 1;
	}
	
	public void setB(float b) {this.b = b;}
	public float getB(){return b;}
	
	public void initializeTrialArrays() {
		trialW = new float[group.getNoTrialTypes()+1][group.getNoOfPhases()][trials][totalStimuli];
		//trialWACompounds = new float[group.getNoTrialTypes()+1][group.getNoOfPhases()][trials][totalStimuli];
		
		//randomTrialWA = new float[group.getNoOfPhases()][trials][totalStimuli];
		//randomTrialWAUnique = new float[group.getNoOfPhases()][trials][totalStimuli];
		
		orgTrialW = new float[group.getNoTrialTypes()+1][group.getNoOfPhases()][trials][totalStimuli];

		//trialWA = new float[group.getNoTrialTypes()+1][group.getNoOfPhases()][trials][totalStimuli];
		//orgTrialWA = new float[group.getNoTrialTypes()+1][group.getNoOfPhases()][trials][totalStimuli];
		
	}
		
	public Set<String> getCommonNames() {return commonMap.keySet();}
	public TreeMap<String,Stimulus> getCommonMap() {return commonMap;}
	public void printErr(String place) {
		//System.out.println("Error in " + place);
	}
	public float getRandomTrialWA(int phase, int trial, String otherName) {
		if (false && getList()[0].getNames().contains(otherName) && (randomTrialWA)[phase].length > trial) {return randomTrialWA[phase][trial][getList()[0].getNames().indexOf(otherName)];}
		else {
			//printErr((ordered? "ordTrialW" : "trialAvgW") + " details: trial: " + trial + " name: " + getName() + " other: " + otherName);
			return 0;
		}
	}
	
	public float getRandomTrialWAUnique(int phase, int trial, String otherName) {
		if (false &&getList()[0].getNames().contains(otherName)&& (randomTrialWA)[phase].length > trial) {return randomTrialWA[phase][trial][getList()[0].getNames().indexOf(otherName)];}
		else {
			//printErr((ordered? "ordTrialW" : "trialAvgW") + " details: trial: " + trial + " name: " + getName() + " other: " + otherName);
			return 0;
		}
	}
	
	
	
	public float getTrialWACompounds(int trialType,int phase, int trial, String otherName) {
		if (false &&getList()[0].getNames().contains(otherName) && (trialWACompounds)[trialType][phase].length > trial) {return (trialWACompounds)[trialType][phase][trial][getList()[0].getNames().indexOf(otherName)];}
		else {
			//printErr((ordered? "ordTrialW" : "trialAvgW") + " details: trial: " + trial + " name: " + getName() + " other: " + otherName);
			return 0;
		}
	}
	
	public float getTrialW(int trialType,int phase, int trial, String otherName, boolean ordered) {
		if (getList()[0].getNames().contains(otherName) && (ordered? orgTrialW : trialW)[trialType][phase].length > trial) {return (ordered? orgTrialW : trialW)[trialType][phase][trial][getList()[0].getNames().indexOf(otherName)];}
		else {
			//printErr((ordered? "ordTrialW" : "trialAvgW") + " details: trial: " + trial + " name: " + getName() + " other: " + otherName);
			return 0;
		}
	}
	
	public float getTrialWA(int trialType,int phase, int trial, String otherName, boolean ordered) {
		if (false &&getList()[0].getNames().contains(otherName) && (ordered? orgTrialWA : trialWA)[trialType][phase].length > trial) {return (ordered? orgTrialWA : trialWA)[trialType][phase][trial][getList()[0].getNames().indexOf(otherName)];}
		else {
			//printErr((ordered? "ordTrialWA" : "trialAvgWA") + " details: trial: " + trial + " name: " + getName() + " other: " + otherName);
			return 0;
		}
	}
	
	
	



	public StimulusElement get(int index) {
		return cues[index];
	}
	
	public void setResetContext(boolean reset) {
		resetContext = reset;
	}

	public float getAlpha() {
		return alphaR;
	}

	public int phaseToTrial(int phase, String message) {
		int trials = 0;
		for (int i = 0; i < phase+1; i++) {
			trials += Math.max(1,group.getPhases().get(i).getNoTrials());
		}
		return phase == -1 ? -1 : trials-1;
		
		
		
	}

	public StimulusElement[] getList() {
		return cues;
	}



	public String getName() {
		return symbol;
	}

	public String getSymbol() {
		return symbol;
	}


	public int getTrialCount() {
		return trialCount;
	}
	
	public void resetActivation(boolean context) {
		hasBeenActive = false;
		if (resetContext && context) {
		startDecay = false;
		presenceMax = presenceMax*contextReset;}
		presenceTrace = 0;
		durationPoint = 0;
		for (StimulusElement cue : cues) {
			cue.resetActivation();
		} 
	}



	public void setRAlpha(float alpha) {
		this.alphaR = alpha;
	}
	public void setNAlpha(float alpha) {
		this.alphaN = alpha;
	}
	
	public void setBeta(float beta) {
		this.beta = beta;
	}
	
	public void setOmicron(float o) {
		this.omicron = o;
		if (cues != null) {
			for (StimulusElement se : cues) {se.setUSSalience(o);}
		}
	}
	
	public void setSalience(float sal) {
		this.salience = sal;
		
		if (cues != null ) {
			for (StimulusElement se: cues) {
				se.setSalience(isUS ? sal/maxDuration : sal/maxDuration);
			}
		}
	}
	
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public void setTrialCount(int trialCount) {
		this.trialCount = trialCount;
	}

	public int size() {
		return cues.length;
	}
	
	public ArrayList<String> getTrialTypes() {return trialTypes;}
	
	public boolean getAssociateStatus(int trial, ArrayList<String> candidates) {
		associate = false;
		for (String name : candidates) {
			for (int j = 0; j <= trial; j++) {
				if (associates[j][names.indexOf(name)]) {associate = true;}
			}
		}
		return associate;
	}
	
	public boolean getHasBeenActive() {return hasBeenActive;}
	
	public float[] getAverageWeightsA() {return averageAverageWeightsA;}
	public void prestore() {
		for (String name: names) {
			if (name != getName())associates[trialCount][names.indexOf(name)] = hasBeenActive && group.getCuesMap().get(name).getHasBeenActive();
			}
	}
	public void store(String trialType) {
		notDivided = true;
		trialTypes.add(trialType);
		wasActiveLast[currentPhase] = hasBeenActive;
		String curT = currPhase.getCurrentTrial();
		
		String filtered = USNames.hasUSSymbol(curT) ? curT.substring(0, curT.length()-1) : curT;
		
		int val = trialTypeMap.get(curT);
		int val2 = trialTypeMap2.get(filtered);
		trialTypeMap.put(curT,val+1);
		trialTypeMap2.put(filtered,val2+1);
		if (cues.length > 0) {
			for (int i = 0 ; i < totalStimuli; i++) {
				
				tempValues[i] = averageAverageWeights[i];
				trialW[group.getTrialTypeIndex(curT)+1][currentPhase][val][i] += tempValues[i]/(currPhase.isRandom() ? (float)group.getModel().getCombinationNo() : 1f);
				
			}
		}
		//if (getName().equals("A"))System.out.println(trialType + " " + getName() + " " + val + " " + trialCount + " " + trialW[group.getTrialTypeIndex(currPhase.getCurrentTrial())+1][currentPhase][val][names.indexOf("+")] + " " + tempValues[names.indexOf("+")]);
		trialW[0][currentPhase][trialCount] = Arrays.copyOf(tempValues,totalStimuli);
		if (cues.length > 0) {
			for (int i = 0 ; i < totalStimuli; i++) {
				tempValues[i] = averageAverageWeightsA[i];
				float commonWa = 0;
				
				for (Stimulus common : commonMap.values()) {
					commonWa += common.getAverageWeightsA()[i]/(currPhase.isRandom() ? (float)group.getModel().getCombinationNo() : 1f);
				}
				boolean update = false;
				if  (isCommon()) {
					if (curT.contains(getName().charAt(1)+"")||curT.contains(getName().charAt(2)+"")) {update = true;}}
				else {
					if (curT.contains(getName()) || isContext) {update = true;}
				}
				//if (update){randomTrialWA[currentPhase][randomTrialCount][i] += commonWa + (tempValues[i])/(currPhase.isRandom() ? (float)group.getModel().getCombinationNo() : 1f);}
				//if (update){randomTrialWAUnique[currentPhase][randomTrialCount][i] += (tempValues[i])/(currPhase.isRandom() ? (float)group.getModel().getCombinationNo() : 1f);}
				
				//trialWA[group.getTrialTypeIndex(curT)+1][currentPhase][val][i] += tempValues[i]/(currPhase.isRandom() ? (float)group.getModel().getCombinationNo() : 1f);
				//trialWACompounds[group.getTrialTypeIndex2(filtered)+1][currentPhase][val2][i] += tempValues[i]/(currPhase.isRandom() ? (float)group.getModel().getCombinationNo() : 1f);
				
			}
		}
		//trialWA[0][currentPhase][trialCount] = Arrays.copyOf(tempValues,totalStimuli);
		

		predictionSum = 0;
		for (float pred : predictionsThisTrial) {predictionSum += pred;}
		errorSum = 0;
		for (float error : errorsThisTrial) {errorSum += error;}
		averagePrediction[trialCount] = predictionSum/trialLength;
		averageError[trialCount] = errorSum/trialLength;
		activity[currentPhase][trialCount] = hasBeenActive;
		if (isProbe) {durationList.add((int)currentDuration);}
		completeDurationList.add((int)currentDuration);
		for (StimulusElement cue: cues) {cue.store();}
		trialCount++;
		if  (isCommon()) {
			if (currPhase.getCurrentTrial().contains(getName().charAt(1)+"")|| currPhase.getCurrentTrial().contains(getName().charAt(2)+"")) {randomTrialCount++;}}
		else {
			if (currPhase.getCurrentTrial().contains(getName()) || isContext) {randomTrialCount++;}
		}
		presenceByTrial.add(new ArrayList<Float>());
		timepoint = 0;
		if (!isContext || ctxReset == 1) {
			hasBeenActive = false;
			startDecay = false;
			presenceMax = presenceMax*contextReset;
			presenceTrace = 0;
		}

		averageAverageWeights = new float[totalStimuli];
		
	}
	
	public void setZeroProbe() {
		durationList.add((int)currentDuration);
		completeDurationList.add((int)currentDuration);
		for (StimulusElement se : cues) {
			se.setZeroProbe();
		}
		//////////////////////
		if (cues.length > 0) {
			for (int i = 0 ; i < totalStimuli; i++) {
				tempValues[i] = 0;
			}
		}
		trialW[0][currentPhase][trialCount] = Arrays.copyOf(tempValues,totalStimuli);
		if (cues.length > 0) {
			for (int i = 0 ; i < totalStimuli; i++) {
				tempValues[i] = 0;
			}
		}
		//trialWA[0][currentPhase][trialCount] = Arrays.copyOf(tempValues,totalStimuli);
		//trialWACompounds[0][currentPhase][trialCount] = Arrays.copyOf(tempValues,totalStimuli);
		averageAverageWeights = new float[totalStimuli];
		averageAverageWeightsA = new float[totalStimuli];
		averagePrediction[trialCount] = 0;
		averageError[trialCount] = 0;
		activity[currentPhase][trialCount] = false;
		
		
		
		/////

		timepoint = 0;
		hasBeenActive = false;
		presenceTrace = 0;
		trialCount++;
		presenceByTrial.add(new ArrayList<Float>());
		
	}
	

	@Override
	public String toString() {
		return symbol + " α(" + alphaR + "): " + cues;
	}

	
	public void update(boolean firstPass) {
		
		
	}

	
	
	public void updatePresenceTrace(float duration) {
		
		currentDuration = duration;
		if (presenceTrace < 0.01 && timepoint == 0) {startDecay = false;}
		if (isActive()) hasBeenActive = true;
		if (isActive() && onset == 0 && !startDecay) {
			presenceTrace = 1.0f;
			startDecay = true;
		}
		else if (isActive() && (startDecay)) {presenceTrace *= tau1;
		}
		else if (!isActive() && hasBeenActive && startDecay){
			presenceTrace *= tau2;
			onset = 0;
		}
		if (!isActive() && !hasBeenActive) {
			presenceTrace = 0f;
		}
		if (presenceTrace > 1) {presenceTrace = 1;}
		
		
	}
	
	public boolean getShouldUpdate () {return update;}
	
	public int getLastOnset() {return lastOnset;}
	
	public void setDuration(int dur, int onset, int offset,int durationPoint, boolean active, int realTime) {
		iti = currPhase.getCurrentIti();
		this.durationPoint = durationPoint -1;
		this.active = active;
		if (offset > 0) {lastOffset = offset;}
		if (onset != -1) {lastOnset = onset;}
		if (!isContext && realTime > lastOnset +1  && realTime <= lastOffset+2) {
			update = true;
		} else if (!isContext) {
			update = false;
		} else if (isContext && realTime > lastOnset + 1  && realTime <= lastOffset + iti+1) {
			update = true;
		} else if (isContext) {update = false;}
		updatePresenceTrace(offset-onset);
		updateDuration = (currentDuration == 0) ? maxDuration: currentDuration;
		
			for (StimulusElement elem: this.getList()) {
				//elem.setDuration(dur,getName());
				elem.setActive(getName(),active,durationPoint);
			}
			for (int n = 0; n < this.getList().length; n++) {
				this.getList()[n].updateActivation(getName(),(n <= updateDuration) ? presenceTrace*presenceMax : 0,currentDuration,n);
			}
			
		
	}
	
	
	
	public float getPrediction(String name) {if (names.contains(name)) {return averageWeightsA[names.indexOf(name)];} else return 0f;}
	public float getVValue(String name) {if (names.contains(name)) {return averageWeights[names.indexOf(name)];} else return 0f;}
	
	public boolean isActive() {return !disabled ? active : false;}
	
	public void incrementTimepoint(int time, boolean iti) {
		
		oldV = (averageWeights != null && names.contains("+")) ? averageWeights[names.indexOf("+")] : 0;
		averageWeights = new float[totalStimuli];
		averageWeightsA = new float[totalStimuli];
		tempMap = new float[totalStimuli][totalMax];
		deltaW = 0f;
		asymptoteMean = 0f;
		for (StimulusElement element : cues) {
			asymptoteMean += element.getAsym()/cues.length;
			key = element.incrementTimepoint(time);
			obj = group.getFromDB(currentPhase+"",key);
			if (obj != null) {
				tempMap = (float[][]) obj;
			}
			//tempMap = (float[][]) 
			for (int i = 0; i < totalStimuli; i++) {
				tempW = 0f;
				for (int j = 0; j < totalMax; j++) {
					tempW += tempMap[i][j]/((float) names.size() > i ? group.getCuesMap().get(names.get(i)).getList().length : 1d);
					
				}
				averageWeightsA[i] += tempW*(element.getGeneralActivation());
				averageWeights[i] = averageWeights[i] + (tempW);
			}
		}
		deltaW = (names.contains("+")) ? averageWeights[names.indexOf("+")] - oldV : 0 - oldV;
		deltaWs[currentPhase][trialCount][timepoint] = deltaW;
		asymptotes[currentPhase][trialCount][timepoint] = asymptoteMean;
		if (!hasBeenActive) {durationPoint = time - lastOnset;}

		
		if (notDivided && durationPoint > 0 && durationPoint - (hasBeenActive || isContext ? lastOffset - lastOnset : maxDuration) + ctxFix*2 <= 0) {
			trialTimepointsCounter++;
			for (int i = 0; i < totalStimuli; i++) {
				averageAverageWeights[i] =  ((averageAverageWeights[i] + averageWeights[i]));
				averageAverageWeightsA[i] += averageWeightsA[i];
			}

		}
		if (notDivided && durationPoint - (hasBeenActive || isContext ? lastOffset - lastOnset : maxDuration) == (0 - 2*ctxFix)) {
			for (int i = 0; i < totalStimuli; i++) {
				divider = hasBeenActive || isContext ? lastOffset - lastOnset : maxDuration;
				averageAverageWeights[i] /= divider;
				averageAverageWeightsA[i] /= divider;
			}
			notDivided = false;

		} else if (iti && notDivided) {
			for (int i = 0; i < totalStimuli; i++) {
				averageAverageWeights[i] /= trialTimepointsCounter;
				averageAverageWeightsA[i] /= trialTimepointsCounter;
			}
			
			notDivided = false;
			
		}
		
		combinedPresence = presenceTrace*presenceMax;
		presenceValues.add(combinedPresence);
		presenceByTrial.get(trialCount).add(combinedPresence);
		if (iti) {trialTimepointsCounter =0;}
		timepoint++;
	}
	
	public void resetForNextTimepoint() {for (StimulusElement element : cues) {element.resetForNextTimepoint();}}
	
	public float[][][] getDeltaWs(){return deltaWs;}
	
	public float getPresence(int t) {return presenceValues.get(t);}
	public float getPresenceByTrial(int trial, int time) {return presenceByTrial.get(trial).get(time);}
	
	public float[][] getRandomTrialAverageWeights(int phase) {return randomTrialWA[phase];}
	public float[][] getRandomTrialAverageWeightsUnique(int phase) {return randomTrialWAUnique[phase];}
	public float[][] getTrialAverageWeights(int trialType,int phase) {return trialW[trialType][phase];}
	public float[][] getOrganizedTrialAverageWeights(int trialType,int phase) {return orgTrialW[trialType][phase];}
	public float[][] getTrialAverageWeightsA(int trialType,int phase) {return trialWA[trialType][phase];}
	public float[][] getOrganizedTrialAverageWeightsA(int trialType,int phase) {return orgTrialWA[trialType][phase];}
	
	public void setTrialAverageWeights(int trialType,float[][] avgTW, int phase) {
		trialW[trialType][phase] = new float[avgTW.length][avgTW[0].length];
		for (int i = 0; i < avgTW.length; i++) {
			for (int j = 0; j < avgTW[0].length; j++) {
				trialW[trialType][phase][i][j] = avgTW[i][j];
			}
		}
	}
	
	
	public void setOrganizedTrialAverageWeights(int trialType,float[][] avgTW, int phase) {
		orgTrialW[trialType][phase] = new float[avgTW.length][avgTW[0].length];
		for (int i = 0; i < avgTW.length; i++) {
			for (int j = 0; j < avgTW[0].length; j++) {
				orgTrialW[trialType][phase][i][j] = avgTW[i][j];
			}
		}
	}
	
	public void setTrialAverageWeightsA(int trialType,float[][] avgTW, int phase) {
		/*trialWA[trialType][phase] = new float[avgTW.length][avgTW[0].length];
		for (int i = 0; i < avgTW.length; i++) {
			for (int j = 0; j < avgTW[0].length; j++) {
				trialWA[trialType][phase][i][j] = avgTW[i][j];
			}
		}*/
	}
	
	public void setOrganizedTrialAverageWeightsA(int trialType, float[][] avgTW, int phase) {
		/*orgTrialWA[trialType][phase] = new float[avgTW.length][avgTW[0].length];
		for (int i = 0; i < avgTW.length; i++) {
			for (int j = 0; j < avgTW[0].length; j++) {
				orgTrialWA[trialType][phase][i][j] = avgTW[i][j];
			}
		}*/
	}
	
	
	
	public void setNames(List<String> names) {
		this.names = names;
	}
	
	public List<String> getNames() {
		return names;
		
	}
	
	public void addMicrostimuli(boolean esther) {
		if (maxDuration <= 0) {maxDuration = 1;}
		cues = new StimulusElement[ !disabled ? ((int)maxDuration) : 1];
		if (!microsSet){
		for (int i = 0; i < (!disabled ? maxDuration : 1); i++) {
			cues[i] = new StimulusElement(i,this,group,getName(), alphaR/(maxDuration), std,trials,totalStimuli,!disabled ? ((int)maxDuration) : 1,(int)totalMax,1,lambdaPlus,usBoost,vartheta,presenceMean,esther);
		}
		for (int i =(int) (maxDuration); i < maxDuration; i++) {
			cues[i] = new StimulusElement(i,this,group,getName(), alphaR/(maxDuration), std,trials,totalStimuli,!disabled ? ((int)maxDuration) : 1,(int)totalMax,1,lambdaPlus,usBoost,vartheta,presenceMean,esther);
		}
		microsSet = true;
		}

		for (StimulusElement se: cues) {
			if (isContext) {
				//alphaN = alphaR;///maxDuration;
				//salience = alphaR;///maxDuration;
			//	alphaR = alphaR;///maxDuration; 
			
			}
			se.setCSCLike(cscLike);
			se.setDisabled(disabled);
			se.setNAlpha(alphaN);
			se.setRAlpha(alphaR);
			se.setSalience(salience);
			se.setBeta(beta);
			if (isUS) se.setUSSalience(omicron);
		}
		

		
	}
	public float getCSCLike() {return cscLike;}
	public void setUSBoost(float b) {usBoost = b;}
	public void setCSCLike(float c) {cscLike = c;}
	
	public boolean[] getActivity(int phase) {return activity[phase];}
	public boolean[] getTrialStringActivity(int phase) {return trialStringActivity[phase];}
	public boolean[] getNormalActivity(int phase) {return normalActivity[phase];}
	public void setActivity(boolean[] a, int phase) {
		for (int i = 0; i < a.length; i++) {
			activity[phase][i] = a[i];
		}
		}
	public void setTrialStringActivity(boolean[] a, int phase) {
		for (int i = 0; i < a.length; i++) {
			trialStringActivity[phase][i] = a[i];
		}
		}
	public void setNormalActivity(boolean[] a, int phase) {
		for (int i = 0; i < a.length; i++) {
			normalActivity[phase][i] = a[i];
		}
		}
    
    public void setPhase(int phase) {
    	startPresence = presenceTrace;
    	hasBeenActive = false;
    	combinations = 1;
    	trialTimepointsCounter = 0;
    	notDivided = true;
    	if (resetContext) { 
    	presenceMax = presenceMax*contextReset;

    	}
    	
    	currPhase = (SimPhase) group.getPhases().toArray()[phase];
    	
    	trialTypeMap = new TreeMap();
    	trialTypeMap2 = new TreeMap();
    	for (String trialType : group.getTrialStrings()){
    		
    		String filtered = USNames.hasUSSymbol(trialType) ? trialType.substring(0, trialType.length()-1) : trialType;
    		
    		 trialTypeMap.put(trialType,0);
    		 trialTypeMap2.put(filtered,0);
    	}
    	if (currPhase.getContextConfig().getSymbol().equals(getName())) { 
    		if (!isContextValuesSet) {
    			isContextValuesSet = true;
	    		for (StimulusElement se: cues) {
	    			if (isContext) {
	    				alphaN = alphaR;
	    				salience = alphaR;
	    				 
	    			
	    			}
	    			
	    			se.setNAlpha(currPhase.getContextConfig().getAlphaN());
	    			se.setRAlpha(currPhase.getContextConfig().getAlphaR());
	    			se.setSalience(currPhase.getContextConfig().getSalience()*5f/maxDuration);
    			}
    		}
    		
    	}
    	
    	thisPhaseMax = presenceMax;
    	trialTypes.clear();
    	currentPhase = phase;
    	if (phase == 0) {
    		if (wasActiveLast != null) wasActiveLast = new boolean[group.getNoOfPhases()];
    		if (activity != null) activity[0] = new boolean[trials+1];
    		else {activity = new boolean[group.getNoOfPhases()][trials+1];}

    		if (trialStringActivity != null) trialStringActivity[0] = new boolean[trials+1];
    		else {trialStringActivity = new boolean[group.getNoOfPhases()][trials+1];}
    		if (normalActivity != null)normalActivity[0] = new boolean[trials+1];
    		else {normalActivity = new boolean[group.getNoOfPhases()][trials+1];}
		}
    	if (phase > 0) {
    		wasActiveLast[currentPhase] = wasActiveLast[currentPhase-1];
    	}
    	averageAverageWeights = new float[totalStimuli];
    	for (int i = 0; i < cues.length; i++) { cues[i].setPhase(phase);
    	}
    }
	public void reset(boolean last, int currentTrials) {
		timepoint = 0;
		
		if (cues != null) {
			for (StimulusElement cue : cues) {
				cue.reset(last,currentTrials);
			}
		}
		hasBeenActive = false;
		if (!isContext)startDecay = false;
		presenceTrace = 0;
		averageAverageWeights = new float[totalStimuli];
		averageError = new float[trials+1];
		if (activity != null )activity[currentPhase] = new boolean[trials+1];
		if (trialStringActivity != null )trialStringActivity[currentPhase] = new boolean[trials+1];		
		tempValues = new float[totalStimuli];
		durationPoint = 0;
		trialCount -= Math.min(trialCount,currentTrials);
		
		
	}
	
	public void resetCompletely() {
		if (cues != null) for (StimulusElement se : cues) {
			se.reset();
		}
		trialCount = 0;
		//randomTrialWA =  new float[group.getNoOfPhases()][trials][totalStimuli];
		//randomTrialWAUnique =  new float[group.getNoOfPhases()][trials][totalStimuli];
		trialW = new float[group.getNoTrialTypes()+1][group.getNoOfPhases()][trials][totalStimuli];
		orgTrialW = new float[group.getNoTrialTypes()+1][group.getNoOfPhases()][trials][totalStimuli];
		//trialWACompounds = new float[group.getNoTrialTypes()+1][group.getNoOfPhases()][trials][totalStimuli];
		
		//trialWA = new float[group.getNoTrialTypes()+1][group.getNoOfPhases()][trials][totalStimuli];
		//orgTrialWA = new float[group.getNoTrialTypes()+1][group.getNoOfPhases()][trials][totalStimuli];
		averageAverageWeights = new float[totalStimuli];
		averageAverageWeightsA = new float[totalStimuli];
		averagePrediction = new float[trials];
		averageError = new float[trials];
		durationList = new ArrayList<Integer>();
		completeDurationList = new ArrayList<Integer>();
		names = new ArrayList<String>(0);
		presenceValues = new ArrayList();
		presenceByTrial = new ArrayList();
		presenceByTrial.add(new ArrayList<Float>());
		tempValues = new float[totalStimuli];
		wasActiveLast = new boolean[group.getNoOfPhases()];
		associates = new boolean[trials][totalStimuli];
		trialTypes = new ArrayList();

		ctxFix = Context.isContext(getName()) ? 1 : 0;
		if (USNames.isUS(symbol)) isUS = true;
		if (Context.isContext(symbol)) isContext = true;
		
		
		
	}
	
	public void incrementCombination() {
		combinations++;
		trialTypes.clear();
		presenceMax = (float) thisPhaseMax;
		presenceTrace = startPresence;
		randomTrialCount = 0;
    	trialTypeMap = new TreeMap();
    	trialTypeMap2 = new TreeMap();
    	for (String s: group.getTrialStrings()) {
    		String filtered = USNames.hasUSSymbol(s) ? s.substring(0, s.length()-1) : s;
    		
    		trialTypeMap.put(s,0);
    		trialTypeMap2.put(filtered, 0);
    	}
		for (StimulusElement se : cues) {
			se.nextCombination();
		}
	}
    
    public void setDisabled() {
    	disabled = true;
    	alphaR = 0.5f;
    	
    	maxDuration = 1;
    	
    	lastOnset = 0;
    	lastOffset = 1;
    	currentPhase = 0;
    	

    }
    
    public void setParameters(float std,float vartheta) {
    	this.vartheta = vartheta;
    	this.std = std;
    	if (cues != null) for (StimulusElement se: cues) {se.setVartheta(vartheta);}
    }
    
    
    public float getMaxDuration() {return maxDuration;}
    public void setMaxDuration(int newMax) {
    	maxDuration = newMax;
    }
    public void setAllMaxDuration(int newMax) {totalMax = newMax;
	    for (SimPhase sp : group.getPhases()) {
			maxITI = Math.max(maxITI,sp.getITI().getMinimum());
		}
		deltaWs = new float[group.getNoOfPhases()][trials][totalMax+(int)maxITI];
		asymptotes = new float[group.getNoOfPhases()][trials][totalMax+(int)maxITI];
    }
    
    
    public int getAllMaxDuration() {return totalMax;}
      
    public void setTrialLength(int trialLength) {
    	this.trialLength = trialLength;
		predictionsThisTrial = new float[trialLength];
		errorsThisTrial = new float[trialLength];
		for (StimulusElement e: getList() ){
			e.setTrialLength(trialLength);
		}
    }
    
    public int getWasActive() {return (wasActiveLast[currentPhase] && !hasBeenActive) ? 1 : (hasBeenActive? 1 : 0);}
    public boolean getWasActiveLast() { return wasActiveLast[currentPhase] && !hasBeenActive;} 
    
    public void setPresenceMean (boolean b) {presenceMean = b;}


    public float getRAlpha() {return alphaR;}
    public float getNAlpha() {return alphaN;}
	public void setContextReset(float contextReset) {
		this.contextReset = contextReset;
	//	
	}
	
	public void initialize(Stimulus a, Stimulus b) {
		this.aStim = a;
		this.bStim = b;
		alphaR = a.getRAlpha()/2f + b.getRAlpha()/2f;
		alphaN = a.getNAlpha()/2f + b.getNAlpha()/2f;
		salience = a.getSalience()/2f + b.getSalience()/2f;
		for (StimulusElement se : cues) {se.initialize(a,b);}
	}
	
	public float getSTD() {return std;}

	float getSalience() {
		return  isUS ? beta :salience;
	}
	
	public boolean isCommon() {return getName().length() > 1 && getName().charAt(0) == 'c';}
	
	public int getTheOnset() {return a_onset;}
	public int getTheOffset() {return a_offset;}

	public void setTiming(int onset2, int offset) {
		a_onset = onset2;
		a_offset = offset;
		
	}
	
	
	

	public void addCommon(String second, Stimulus stimulus) {
		commonMap.put(second, stimulus);
	}

	public float[][][] getAsymptotes() {
		return asymptotes;
	}

	public void postStore() {
		averageAverageWeights = new float[totalStimuli ];
		averageAverageWeightsA = new float[totalStimuli ];
	}
	
}
