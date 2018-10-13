/**
 * SimPhase.java
 * 
 * Created on 10-Mar-2005
 * City University
 * BSc Computing with Distributed Systems
 * Project title: Simulating Animal Learning
 * Project supervisor: Dr. Eduardo Alonso 
 * @author Dionysios Skordoulis
 *
 * Modified in October-2009
 * The Centre for Computational and Animal Learning Research 
 * @supervisor Dr. Esther Mondragon 
 * email: e.mondragon@cal-r.org
 * @author Rocio Garcia Duran
 *
 * Modified in July-2011
 * The Centre for Computational and Animal Learning Research 
 * @supervisor Dr. Esther Mondragon 
 * email: e.mondragon@cal-r.org
 * @author Dr. Alberto Fernandez
 * email: alberto.fernandez@urjc.es
 * 
 * Modified in December-2011
 * City University
 * BSc Computing with Artificial Intelligence
 * Project title: Building a TD Simulator for Real-Time Classical Conditioning
 * @supervisor Dr. Eduardo Alonso 
 * @author Jonathan Gray
 *
 */
package simulator;
import java.util.*;
import java.util.Map.Entry;

import simulator.configurables.ContextConfig;
import simulator.configurables.ContextConfig.Context;
import simulator.configurables.ITIConfig;
import simulator.configurables.TimingConfiguration;
import simulator.util.USNames;

/**
 * SimPhases is the class which models and processes a phase from the
 * experiment. It will process the sequence of stimuli and give results as
 * requested from the controller.
 */
public class SimPhase {

	/** Group this phase is for. **/
	private SimGroup group;
	/** Trial sequence. **/
	private List<Trial> orderedSeq;
	/** Stimuli mappings. **/
	protected Map<String, SimStimulus> stimuli;
	// Modified by J Gray, replace a cue with a CSC and hence a list of cues
	/** CSC cue maps. **/
	private Map<String, Stimulus> cues;
	/** Results map. **/
	protected Map<String, Stimulus> results;
	/** Set of CSs that have been in this trial. **/
	private Set<CS> presentCS;
	private boolean reinforced;
	private String initialSeq;
	/** Number of trials to run. **/
	protected int trials;
	/** Whether to use a random ordering of trials. **/
	private boolean random;
	/** TD parameters. **/
	private Float lambdaPlus, lambdaMinus, betaPlus, betaMinus, gamma, std,
			tau1, tau2, vartheta, delta;
	/** Previous predictions of stimuli **/
	protected float[][] elementPredictions;
	protected boolean td = false;
	/** Counter for maximum trial duration. **/
	protected int maxMaxOnset;
	/** ITI configuration. **/
	protected ITIConfig itis;
	/** Salience of contextual stimulus **/
	protected float bgSalience;
	/** Timing configuration. **/
	private TimingConfiguration timingConfig;
	/** Context configuration. **/
	private TreeMap<String,ContextConfig> contextCfgs;

	private ContextConfig contextCfg;
	private int phaseNum = -1;

	private float dopamine = 1;

	private float integration = 0.2f;
	private float leak = 0.99f;

	private volatile ModelControl control;

	private ArrayList<Integer> trialLengths;
	private ArrayList<Integer> completeLengths;
	private int sessions;
	private int trialsInAllPhases;
	private String[] listedStimuli;
	private TreeMap<String, Integer> probeIndexes;
	private TreeMap<String, TreeMap<String, Integer[]>> probeTiming;
	private TreeMap<String, Integer> nameIndexes;

	private int setsize = 100;
	private float[][][] timePointElementErrors;
	private float[][][] lastTrialElementErrors;
	private float usScalar = 1f;
	private Float csScalar;
	private float usPersistence;
	private float usCV;
	private float selfDiscount = 1;
	private float csLeak;
	private float csIntegration;
	private float contextReset = 0.95f;
	private boolean resetContext;
	private Float oldVal;
	private Float newVal;
	private int onset;
	private int offset;
	private int counter;
	private ArrayList<CS> activeCS;
	private char[] names;
	private CS[] css;
	private int tempOnset;
	private int tempOffset;
	private int generalOnset;
	private int generalOffset;
	private boolean active;
	private boolean usActive;
	private String csName;
	private int usIndexCount;
	private float tempPrediction;
	private float averageError;
	private TreeMap<String,Float> averageCSErrors;
	private boolean shouldUpdateUS;
	private boolean shouldUpdateCS;
	private int stimCount;
	private float tempDopamine;
	private float tempCSDopamine;
	private Map<CS, int[]> timings;
	private int usOnset;
	private int usOffset;
	private int trialLength;
	private float div;
	private TreeMap<String, Float> usPredictions;
	private TreeMap<String, Float> csPredictions;
	private String curNameSt;
	private SimStimulus currentSt;
	private HashMap<CS, Stimulus> tempMap;
	private HashMap<CS, Stimulus> allMap;
	private TreeMap<String, Integer> usIndexes;
	private TreeMap<String, Integer> csIndexes;
	private ArrayList<Stimulus> activeList;
	private HashSet<String> csActiveThisTrial;
	private HashSet<String> probeCSActiveThisTrial;
	private ArrayList<Stimulus> activeLastStep;
	private ArrayList<CS> csActiveLastStep;
	private HashSet<String> inThisRun;
	private HashMap<CS, Queue<Stimulus>> cueLog;
	private Stimulus tempContext;
	private String currentSeq;
	private String nextSeq;
	private float sumOfPredictions;
	private int counterBBB;
	private int counterAAA;
	private float currentElementPrediction;
	private int iti;
	private TreeMap<String,Integer> trialAppearances;
	private TreeMap<String, Integer> trialTypes;
	private ArrayList<ArrayList<StimulusElement>> subsets;
	private boolean isSubsetsCreated;
	private float act;
	private float totalErrorUS;
	private float totalErrorCS;
	private int counter1;
	private boolean rightTime;
	private Stimulus currentUS;
	private Stimulus currentCS;
	private float[][] vals,vals2;
	private ArrayList<List<Trial>> allSequences;
	private ArrayList<Trial> originalSequence;
	private TreeMap<String, TreeMap<Integer, Integer>> trialIndexMap;
	private TreeMap<String, TreeMap<Integer, Integer>> trialIndexMapb;
	private TreeMap<String, Integer> trialTypeCounterMap;
	private String currentTrial;
	private ArrayList<Stimulus> presentStimuli;
	private float intensity = 1f;
	private TreeMap<String,Integer> onsetMap;

	private TreeMap<String,Integer> offsetMap;

	private TreeMap<String,Integer> generalOnsetMap;
	private TreeMap<String,Integer> generalOffsetMap;

	private TreeMap<String,CS[]> csMap;
	private int correction;
	private float tempError;
	private int factor;
	private float averageCSError;
	private float threshold2;
	private float threshold;
	
	public SimPhase(int phaseNum, int sessions, String seq, List<Trial> order,
			Map<String, SimStimulus> stimuli2, SimGroup sg, boolean random,
			TimingConfiguration timing, ITIConfig iti, ContextConfig context,
			int trialsInAllPhases, String[] listedStimuli,
			boolean varyingVartheta) {
		allSequences = new ArrayList();
		results = new TreeMap<String, Stimulus>();
		initialSeq = seq;
		stimuli = stimuli2;
		orderedSeq = order;
		generalOnsetMap = new TreeMap();
		generalOffsetMap = new TreeMap();
		onsetMap = new TreeMap();
		offsetMap = new TreeMap();
		csMap = new TreeMap();
		group = sg;
		this.random = random;
		this.trials = orderedSeq.size();
		cues = new TreeMap();
		this.sessions = sessions;
		this.trialsInAllPhases = trialsInAllPhases + 1;
		this.listedStimuli = listedStimuli;
		probeIndexes = new TreeMap<String, Integer>();
		nameIndexes = new TreeMap<String, Integer>();
		probeTiming = new TreeMap();
		presentStimuli = new ArrayList();
		for (Entry<String, Stimulus> entry : group.getCuesMap().entrySet()) {
			if (seq.contains(entry.getKey())) {
				cues.put(entry.getKey(), entry.getValue());
			}
		}
		// Added to allow for variable distributions of onsets - J Gray
		// Added to control the onset of fixed onset stimulu
		timingConfig = timing;
		// timingConfig.setTrials(trials);
		iti.setTrials(trials);
		maxMaxOnset = 0;
		// Added to allow ITI modelling.
		itis = iti;
		// Added to make use of contexts per phase/group
		contextCfg = context;
		contextCfgs = new TreeMap();
		contextCfgs.put(context.getSymbol(),context);
		setPresentCS(new HashSet<CS>());
		this.phaseNum = phaseNum;
		trialLengths = new ArrayList<Integer>();
		completeLengths = new ArrayList<Integer>();

		usPredictions = new TreeMap<String,Float>();
		csPredictions = new TreeMap<String,Float>();
		averageCSErrors = new TreeMap<String,Float>();
		tempMap = new HashMap<CS, Stimulus>();
		allMap = new HashMap<CS, Stimulus>();

		usIndexes = new TreeMap<String, Integer>();
		csIndexes = new TreeMap<String, Integer>();
		activeCS = new ArrayList<CS>();
		trialAppearances = new TreeMap();
	}
	
	public TreeMap<String,Integer> getTrialTypes() {return trialTypes;}
	
	public void setContextReset(float r) {contextReset = r;}

	public ArrayList<List<Trial>> getAllSequences() {return allSequences;}
	/**
	 * The TD algorithm.
	 * 
	 * @param sequence
	 *            list of trial strings in order
	 * @param tempRes
	 *            Map to populate with results
	 * @param probeResults2
	 */

	protected void algorithm(List<Trial> sequence,
			Map<String, Stimulus> tempRes, boolean context,
			Map<String, Stimulus> probeResults2) {
		// Map cues to iterators of cues.
		// Map of all cues
		trialAppearances = new TreeMap();
		trialTypes = new TreeMap<String,Integer>();
		for (int i = 0; i < sequence.size(); i++) {
			trialTypes.put(sequence.get(i).toString(), 1);
		}
		for (String s: trialTypes.keySet()) {
			for (int i = 0; i < sequence.size(); i++) {
				if (sequence.get(i).toString().equals(s)) {
					if (trialAppearances.containsKey(s)) {trialAppearances.put(s,trialAppearances.get(s)+1);}
					else{trialAppearances.put(s, 1);}
				}
			}
		}
		
		cueLog = new HashMap<CS, Queue<Stimulus>>();
		inThisRun = new HashSet<String>();
		csActiveLastStep = new ArrayList<CS>();
		activeLastStep = new ArrayList<Stimulus>();
		if (context) {
			// Set the alpha on the context we're using here
			tempContext = group.getCuesMap().get(
					contextCfg.getSymbol());
			if (tempContext != null) {
				group.getCuesMap().put(tempContext.getName(), tempContext);
			}
		}
		activeList = new ArrayList<Stimulus>();
		// Trials loop
		csActiveThisTrial = new HashSet<String>();
		probeCSActiveThisTrial = new HashSet<String>();

		int mostElements = 0;
		for (Stimulus stim : group.getCuesMap().values()) {
			mostElements = Math.max(mostElements, stim.getList().length);
			stim.setContextReset(contextReset);
			for (StimulusElement se : stim.getList()) {

				if (stim.isUS || se.isUS) {se.setIntensity(intensity);}
				se.setCSCLike(group.getModel().getSkew(false));
				
				se.setVartheta(vartheta);
				se.setSubsetSize(setsize);
			}
			if (trials == 0) {
					stim.setZeroProbe();
			}
		}
		if (true) {
			for (Stimulus com : group.getCuesMap().values()) {
			
			if (com.getName().length() > 1) {
				Stimulus a = group.getCuesMap().get(com.getName().charAt(1)+"");
				Stimulus b = group.getCuesMap().get(com.getName().charAt(2)+"");
				com.initialize(a,b);
				
			}
		}
		
		}
		elementPredictions = new float[group.getCuesMap().size()][mostElements];
		if (group.getModel().isErrors()) {
		timePointElementErrors = new float[trials * sessions][group.getCuesMap().size()][mostElements];}
		
		if(group.getModel().isErrors2()){
		lastTrialElementErrors = new float[((Stimulus) group
				.getCuesMap().values().toArray()[0]).getAllMaxDuration()
				* group.getCuesMap().size()][group.getCuesMap().size()][mostElements];}
		for (int i = 1; i <= trials * sessions && !control.isCancelled(); i++) {
			if (i % trials == 1 && i != 1) {
				itis.reset();
				for (Stimulus stimulus : group.getCuesMap().values()) {
					if (stimulus.isContext) {
						stimulus.resetActivation(true);
					}
				}
			}
			csActiveThisTrial.clear();
			probeCSActiveThisTrial.clear();
			long count = System.currentTimeMillis();
			currentSeq  = sequence.get((i - 1) % trials).toString();
			nextSeq = i == trials*sessions? "nextPhase" : sequence.get(i%(trials == 0 ? 1 : trials)).toString();
			for (Stimulus stim : group.getCuesMap().values()) {
				for (StimulusElement elem : stim.getList()) {
					elem.setCurrentTrialString(currentSeq);
					elem.setNextString(nextSeq);
				}
			}
			cueLog.clear();
			if ((i) / trials >= sessions - 1) {
				boolean last = true;
				
				for (int k = 1; k <= (trials * sessions - i); k++) {
					if (sequence.get((k + i - 1) % trials).toString()
							.equals(currentSeq)) {
						last = false;
					}
				}

				if (last) {
					probeIndexes.put(currentSeq, i);
				}
			}
			curNameSt = sequence.get((i - 1) % trials).toString();
			if (!probeTiming.containsKey(curNameSt)) {
				probeTiming.put(curNameSt, new TreeMap());
			}
			currentSt = stimuli.get(curNameSt);
			if (currentSt.isReinforced()) {
				reinforced = true;
			}
			tempMap.clear();
			allMap.clear();
			Trial trial = sequence.get((i - 1) % trials).copy();
			getPresentCS().addAll(trial.getCues());

			// Get the stimuli present this trial, copy them to the
			// temporary
			// map
			// and collect their onsets.
			for (CS cs : trial.getCues()) {
				try {
				} catch (NullPointerException e) {
				}
				tempMap.put(cs, tempRes.get(cs.getName()));
				cueLog.put(cs, new LinkedList<Stimulus>());
			}
			for (int j = 0; j < sequence.size(); j++) {
				Trial aTrial = sequence.get(j).copy();
				for (CS cs : aTrial.getCues()) {
					if (!allMap.containsKey(cs))
						allMap.put(cs, tempRes.get(cs.getName()));
				}
			}

			iti = (int) Math.round(itis.next()
					/ Simulator.getController().getModel().getTimestepSize());

			timings = timingConfig.makeTimings(tempMap.keySet());
			trialLength = timings.get(CS.TOTAL)[1];

			usOnset = timings.get(CS.US)[0];
			usOffset = timings.get(CS.US)[1];

			if ((i % trials) <= trials) {
				maxMaxOnset = Math
						.max(maxMaxOnset, timings.get(CS.CS_TOTAL)[1]);
			}

			// Timesteps loop within each trial.

			// Run through all the timesteps, duration of the trial is the
			// total
			// period
			// returned by the timings generator.

			trialLengths.add(trialLength);
			completeLengths.add(trialLength + iti);

			usIndexes.clear();
			csIndexes.clear();
			usIndexCount = 0;
			int numberCommon =0;
			for (Stimulus s : group.getCuesMap().values()) {
				
				if (group.getFirstOccurrence(s) >= 0  &&group.getFirstOccurrence(s) < this.getPhaseNum()) {
				if (s.isUS) {
					usIndexes.put(s.getName(), usIndexCount);
				} else if (!s.isCommon()) {
					csIndexes.put(s.getName(), usIndexCount);
				} else if (s.isCommon()){
					csIndexes.put(s.getName(), usIndexCount);
					numberCommon++;
				}
				}
				usIndexCount++;
				
					names = s.getName().toCharArray();
					css = new CS[names.length];
					onset = -1;
					offset = trialLength;
					counter = 0;
					for (char character : names) {
						if (character == "c".charAt(0)) {}
						else {
						for (CS cs : tempMap.keySet()) {
							if (cs.getName().equals((String) (character + ""))) {
								css[counter] = cs;
							}
						}
						tempOnset = (css[counter] != null && timings
								.containsKey(css[counter])) ? timings
								.get(css[counter])[0] : -1;
						onset = Math.max(tempOnset, onset);
						tempOffset = (css[counter] != null && timings
								.containsKey(css[counter])) ? timings
								.get(css[counter])[1] : -1;
						offset = Math.min(tempOffset, offset);
						counter++;
						}
					}
					generalOnset = -1;
					generalOffset = -1;

					csMap.put(s.getName(), css);
					onsetMap.put(s.getName(), onset);
					offsetMap.put(s.getName(), offset);
					generalOnsetMap.put(s.getName(), generalOnset);
					generalOffsetMap.put(s.getName(), generalOffset);
					
					//System.out.println(s.getName() + " " + onset +"  " + offset +" " + tempOnset +"  " + tempOffset + " " + trialLength  + " " + iti);
			}
			for (Stimulus s : group.getCuesMap().values()) {
				if (s.getName().contains("c")) {
					String name1 = s.getName().charAt(1)+"";
					String name2 = s.getName().charAt(2)+"";
					onset = Math.max(0, Math.min(onsetMap.get(name1),onsetMap.get(name2)));
					offset = Math.max(offsetMap.get(name1), offsetMap.get(name2));
					
					onsetMap.put(s.getName(),onset);
					offsetMap.put(s.getName(),offset);
					
				}
				
			}
			
			
			
			for (int j = 1; j < (trialLength + iti) && !control.isCancelled(); j++) {
				/*if (j==1 && i == 1) {
					

					for (Stimulus s :group.getCuesMap().values()) {
						for (StimulusElement se : s.getList()) {
						System.out.println("test" + se.getName() + " "+ se.getMicroIndex() + " " + se.alphaN);
						}
					}
				}*/
				activeList.clear();
				activeCS.clear();
				for (Stimulus stimulus : group.getCuesMap().values()) {
					names = stimulus.getName().toCharArray();
					css = csMap.get(stimulus.getName());
					onset = onsetMap.get(stimulus.getName());
					offset = offsetMap.get(stimulus.getName());
					generalOnset = generalOnsetMap.get(stimulus.getName());
					generalOffset = generalOffsetMap.get(stimulus.getName());
					csName = stimulus.getName();
					stimulus.setTrialLength(trialLength + iti);
					active = (j >= onset && j <= offset);
					usActive = ((timings.get(CS.US)[0]) < j && j <= (timings
							.get(CS.US)[1]));
					if (stimulus.isUS) {
						if (stimulus.getName() != "+") {
							stimulus.setDuration(timings.get(CS.US)[1]
									- timings.get(CS.US)[0], usOnset, usOffset,
									j - timings.get(CS.US)[0], usActive
											& currentSt.isReinforced(),j);
						} else {
							stimulus.setDuration(usOffset - usOnset, usOnset,
									usOffset, j - timings.get(CS.US)[0],
									usActive & currentSt.isReinforced(),j);

						}
						generalOnset = usOnset;
						generalOffset = usOffset;
					} else if (contextCfg.getContext().toString()
							.equals(csName)) {
						stimulus.setDuration(trialLength + iti, 0, trialLength
								+ iti-1, j, true,j);
						generalOnset = 0;
						generalOffset = trialLength + iti-1;
						
						
					} else if(!stimulus.isContext) {
						stimulus.setDuration((offset - onset), onset, offset, j
								- onset, active,j);
						generalOnset = onset;
						generalOffset = offset;

					}
					else if (!contextCfg.getContext().toString()
							.equals(csName) && stimulus.isContext) {
						stimulus.setDuration(trialLength + iti, 0, trialLength
								+ iti-1, j, false,j);
						generalOnset = 0;
						generalOffset = trialLength + iti-1;
					}
					
					if (!probeTiming.get(curNameSt).containsKey(
							stimulus.getName())) {
						probeTiming.get(curNameSt).put(stimulus.getName(),
								new Integer[] { generalOnset, generalOffset });
					}
					
					

				}

				if (!isSubsetsCreated) {createSubsets();isSubsetsCreated = true;}
				
				
				usPredictions.clear();
				csPredictions.clear();
				
					if (elementPredictions != null) {
						for (String naming : usIndexes.keySet()) {
							currentUS = group.getCuesMap().get(naming);
							tempPrediction = 0f;
							div = group.getCuesMap().get(naming).getList().length;
							
							
							for (int k2 = 0; k2 < currentUS.getList().length; k2++) {
								tempPrediction += Math.abs(( (currentUS.get(k2).getAsymptote()) - elementPredictions[usIndexes.get(naming)][k2])/(div));
								
							}

							if (Math.abs(tempPrediction) > 0.05) usPredictions.put(naming, tempPrediction);
						}
						for (String naming : csIndexes.keySet()) {
							currentCS = group.getCuesMap().get(naming);
							tempPrediction = 0f;
							div = group.getCuesMap().get(naming).getList().length;

							for (int k2 = 0; k2 < currentCS.getList().length; k2++) {
								tempPrediction += Math.abs(((currentCS.get(k2).getAsymptote()) -elementPredictions[csIndexes.get(naming)][k2])/div);
								
								
							}
							
							if (Math.abs(tempPrediction) > 0.05) csPredictions.put(naming, tempPrediction);
						}
					}
				
				averageError = 0f;
				/*double normCS = 0;
				for (String s : csIndexes.keySet()) {
					normCS += group.getCuesMap().get(s).getSalience();
				}
				double normUS = 0;
				for (String s : usIndexes.keySet()) {
					normUS += group.getCuesMap().get(s).getSalience();
				}*/

				for (String s : usPredictions.keySet()) {
					averageError += usPredictions.get(s) / ((float) usPredictions.size());
					
				}
				//averageError /= normUS;
				
				for (String s : group.getCuesMap().keySet()) {
					tempError = 0f;
					
					
					correction = group.getCuesMap().get(s).isContext || group.getCuesMap().get(s).isUS ? 0 : numberCommon;
					factor = group.getCuesMap().get(s).isUS ? 0 : 1;
					for (String s2 : csPredictions.keySet()) {
						if (!s.equals(s2) && !s.contains(s2) && ! s2.contains(s)) {
							
							tempError += csPredictions.get(s2)/((float) Math.max(1, csPredictions.size()-factor-correction));
						}
					}
					//System.out.println(s + " " + ((float) Math.max(1, csPredictions.size()-correction-factor)));
					//tempError /= normCS;
					averageCSErrors.put(s, tempError);
					
				}
				
				shouldUpdateUS = false;
				for (String naming : usIndexes.keySet()) {
				  if (group.getCuesMap().get(naming).getShouldUpdate()) {
					  shouldUpdateUS = true;
				  }
					
				}
				shouldUpdateCS = false;
				for (String naming : csIndexes.keySet()) {
				  if (group.getCuesMap().get(naming).getShouldUpdate()) {
					  shouldUpdateCS = true;
				  }
					
				}

				stimCount = 0;
				for (Stimulus cue : group.getCuesMap().values()) {
					String csName = cue.getName();
					counterBBB = 0;
					
					
					for (StimulusElement el : cue.getList()) {
						
						
						act = el.getGeneralActivation()*el.getParent().getSalience();
						rightTime = j % Math.max(1, usOnset) <= (usOffset - usOnset);
						rightTime = false;
						if(shouldUpdateUS || true || usIndexes.size() == 0 ||(cue.isContext&& rightTime)) {
							
							el.storeAverageUSError(Math.abs(averageError),act);
						}
						if(shouldUpdateCS || true || csIndexes.size() == 0) {
							el.storeAverageCSError(Math.abs(averageCSErrors.get(cue.getName())),act);
						}
						
					el.updateAssocTrace(Math.max(0,  (elementPredictions != null) ? elementPredictions[stimCount][el.getMicroIndex()] : 0f));

						sumOfPredictions = 0f;
						for (Stimulus cue2 : group.getCuesMap().values()) {
							currentElementPrediction = 0f;
							for (StimulusElement el2 : cue2.getList()) {
								//if (el != el2) {
									if (el.getNames().indexOf(el2.getName()) != -1) {
										float prediction = el2
												.getPrediction(el2.getNames()
														.indexOf(el.getName()),
														el.getMicroIndex(),
														true,false);
										currentElementPrediction += prediction;
									}
							//	}
							}

							sumOfPredictions += currentElementPrediction;
						}

						elementPredictions[stimCount][counterBBB] = sumOfPredictions;
						counterBBB++;
					}

					csActiveThisTrial.add(csName);
					activeList.add(cue);
					inThisRun.add(csName);
					stimCount++;
				}
				counter1 = 0;
				for (Stimulus stim : group.getCuesMap().values()) {
					
					if (!nameIndexes.containsKey(stim.getName())) {
						nameIndexes.put(stim.getName(), counter1);
					}
					
					for (StimulusElement el : stim.getList()) {
							
							
							if ((j < (timings
									.get(CS.US)[1])) && group.getModel().isErrors()) {timePointElementErrors[i - 1][counter1][el.getMicroIndex()] = (el.getAsymptote()) - elementPredictions[counter1][el.getMicroIndex()]/(timings
											.get(CS.US)[1]);}
							if (i == trials * sessions && group.getModel().isErrors2()){
								lastTrialElementErrors[j - 1][counter1][el.getMicroIndex()] = (el.getAsymptote()) - elementPredictions[counter1][el.getMicroIndex()];
							}
						
							
							averageError = el.getCurrentUSError();
							averageCSError = el.getCurrentCSError();
							tempDopamine = el.getVariableSalience();
							tempCSDopamine = el.getCSVariableSalience();
									
									act = el.getGeneralActivation()*el.getParent().getSalience();;
									threshold = stim.isContext ? 0.3f : 0.4f;
									threshold2 = stim.isContext ? 0.9f : 0.9f;

									rightTime = j % Math.max(1, usOnset) <= (usOffset - usOnset);
									rightTime = false;
									if(shouldUpdateUS || true ||usIndexes.size() == 0 || (stim.isContext&& rightTime)) {
										totalErrorUS = el.getTotalError(Math.abs(averageError));
										if (el.getName().equals("B") && this.phaseNum == 1) {System.out.println(j + " " + act + totalErrorUS);}
										tempDopamine = tempDopamine*(1-integration*act)*(1-act*(totalErrorUS>threshold ? totalErrorUS/100f : 0)) + (integration*act* Math.max(0, (Math.min(1, Math.abs(averageError))))*(Math.max(el.getParent().getWasActive(),el.getGeneralActivation())));
										//if (el.getName().contains("AX") || el.getName().contains("BY") && j == 3) {System.out.println(el.getName()+ " " + totalErrorUS + " "+ act + " " + averageError);}
									}
									if(shouldUpdateCS || true || csIndexes.size() == 0) {
										//if (stim.isCS)System.out.println((1-csIntegration*act)*(1-(totalErrorCS>threshold2 ? totalErrorCS : 0)) + " " + averageCSError);
										totalErrorCS = el.getTotalCSError(Math.abs(averageCSError));
										//if (group.getNameOfGroup().contains("Ctrl") && stim.getName().contains("B")) {System.out.println(tempCSDopamine);}
										tempCSDopamine = tempCSDopamine*(1-csIntegration*act)*(1-(totalErrorCS>threshold2 ? totalErrorCS/100f : 0)) + csIntegration*act* Math.max(0, (((Math.min(1,Math.abs(averageCSError)))*(Math.max(el.getParent().getWasActive(),el.getGeneralActivation())))));

										//if (group.getNameOfGroup().contains("Ctrl") && stim.getName().contains("B")) {System.out.println(j + " " + totalErrorCS + " " + stim.getName() + " "+ tempCSDopamine + " " + act + " " + csIntegration + " ");}
									}
								
									
								if (!stim.isUS) {
									el.setVariableSalience(tempDopamine);
								}
								el.setCSVariableSalience(tempCSDopamine);
							
					}
					counter1++;
				}

					for (Stimulus cl : group.getCuesMap().values()) {
						cl.incrementTimepoint(j,j>trialLength);
					}
					updateCues(0, tempRes, tempMap.keySet(), j);
					for (Stimulus cl : group.getCuesMap().values()) {
						cl.resetForNextTimepoint();
					}


			}
			activeLastStep.clear();
			csActiveLastStep.clear();
			
			group.compactDB();
			store(group.getCuesMap(), csActiveThisTrial, currentSeq);

			if (i%sessions == 0)control.incrementProgress(1);
			control.setEstimatedCycleTime(System.currentTimeMillis() - count);
		}
	}
	public void createSubsets() {
		subsets = new ArrayList<ArrayList<StimulusElement>>();
		for (int j = 0; j < group.getTotalMax(); j++) {
			subsets.add(new ArrayList<StimulusElement>());
		}
		for (Stimulus s : group.getCuesMap().values()) {
			if (s.isCS) {
				for (StimulusElement se: s.getList()) {
					if (subsets.size() > 0 ) subsets.get(se.getMicroIndex()).add(se);
				}
			}
		}
	}
	public ArrayList<ArrayList<StimulusElement>> getSubsets() {return subsets;}
	

	/**
	 * Returns an exact TreeMap copy from the TreeMap that is been given. It
	 * iterates through it's keys and puts their values into a new object.
	 * Modified Dec-2011 to use CSC cues. J Gray
	 * 
	 * @param cues2
	 *            the original TreeMap object which to copy from.
	 * @return
	 */
	protected TreeMap<String, Stimulus> copyKeysMapToTreeMap(
			Map<String, Stimulus> cues2) {
		TreeMap<String, Stimulus> reqMap = new TreeMap<String, Stimulus>();

		Iterator<Entry<String, Stimulus>> it = cues2.entrySet().iterator();
		while (it.hasNext()) {

			//System.out.println("loop simphase");
			Entry<String, Stimulus> element = it.next();
			if (element.getKey().length() == 1) {

				Stimulus currentCsc = element.getValue();
				Stimulus cscValues = currentCsc;
				reqMap.put(element.getKey(), cscValues);
				// }
			}
		}
		return reqMap;
	}

	/**
	 * Returns the phase's 'beta' value which represents the non-reinforced
	 * stimuli.
	 * 
	 * @return a 'beta' value for the non-reinforced stimuli.
	 */
	public Float getBetaMinus() {
		return betaMinus;
	}

	/**
	 * Returns the phase's 'beta' value which represents the reinforced stimuli.
	 * 
	 * @return a 'beta' value for the reinforced stimuli.
	 */
	public Float getBetaPlus() {
		return betaPlus;
	}

	/**
	 * @return the contextCfg
	 */
	public ContextConfig getContextConfig() {
		return contextCfg;
	}
	
	public ContextConfig getContextConfig(String name) {return contextCfgs.get(name);}

	public float getContextSalience() {
		return bgSalience;
	}

	/**
	 * @return the cues
	 */
	public Map<String, Stimulus> getCues() {
		return cues;
	}

	/**
	 * @return the delta
	 */
	public Float getDelta() {
		return delta;
	}

	public Float getGamma() {
		return gamma;
	}

	public Float getTau1() {
		return tau1;
	}

	public Float getTau2() {
		return tau2;
	}

	public Float getVartheta() {
		return vartheta;
	}

	/**
	 * @return the group
	 */
	public SimGroup getGroup() {
		return group;
	}

	/**
	 * @return the itis
	 */
	public ITIConfig getITI() {
		return itis;
	}

	/**
	 * Returns the phase's 'lambda' value which represents the non-reinforced
	 * stimuli.
	 * 
	 * @return a 'lambda' value for the non-reinforced stimuli.
	 */
	public Float getLambdaMinus() {
		return lambdaMinus;
	}

	/**
	 * Returns the phase's 'lambda' value which represents the reinforced
	 * stimuli.
	 * 
	 * @return a 'lambda' value for the reinforced stimuli.
	 */
	public Float getLambdaPlus() {
		return lambdaPlus;
	}

	/**
	 * Get the longest duration of all the trials in this phase.
	 * 
	 * @return an integer giving the maximum duration of the trials.
	 */

	public int getMaxDuration() {
		return maxMaxOnset;
	}

	/**
	 * Returns the total number of trials that this phase contains.
	 * 
	 * @return the number of trials.
	 */
	public int getNoTrials() {
		return trials;
	}

	/**
	 * @return the orderedSeq
	 */
	public List<Trial> getOrderedSeq() {
		return orderedSeq;
	}

	/**
	 * @return the presentCS
	 */
	public Set<CS> getPresentCS() {
		return presentCS;
	}

	/**
	 * Returns the results into a HashMap containing the cues that are
	 * participate in this phase or in the other group's phase's (their value
	 * remain the same) with their associative strengths. Modified to return a
	 * CSC cue-list. J Gray
	 * 
	 * @return the results from the algorithms process.
	 */
	public boolean getReinforced() {
		return reinforced;
	}

	public Map<String, Stimulus> getResults() {
		return results;
	}

	/**
	 * Returns the results into a HashMap containing the stimuli that are
	 * participate in this phase or in the other group's phase's (their value
	 * remain the same) with their associative strengths.
	 * 
	 * @return the stimuli of the phase.
	 */
	public Map<String, SimStimulus> getStimuli() {
		return stimuli;
	}

	/**
	 * @return the timingConfig
	 */
	public TimingConfiguration getTimingConfig() {
		return timingConfig;
	}

	/**
	 * @return the timing configuration for this phase
	 */
	public TimingConfiguration getTimingConfiguration() {
		return timingConfig;
	}

	/**
	 * Returns the initial sequence that was entered by the user on the phases
	 * table.
	 * 
	 * @return the initial sequence.
	 */
	public String intialSequence() {
		return initialSeq;
	}

	/**
	 * Returns true if the cue is in the stimuli of the phase. (Returns true if
	 * the cue is taking part in the phase)
	 * 
	 * @param cue
	 *            the cue looked for
	 * @return if the cue is taking part in the current phase
	 */
	public boolean isCueInStimuli(String cue) {
		boolean is = false;
		try {
			if (results.get(cue).getTrialCount() == 0) {
				return false;
			}
		} catch (NullPointerException e) {
		}
		for (SimStimulus s : stimuli.values()) {
			if (s.contains(cue)) {
				is = true;
				break;
			}
		}
		return is;
	}
	
	public boolean containsStimulus(Stimulus s) {return presentStimuli.contains(s);}

	/**
	 * Check if a set of CSs has a match for a cue's name and if so whether
	 * that's a probe CS.
	 * 
	 * @param set
	 *            set to search.
	 * @param cue
	 *            cue name to find.
	 * @return true if the cue name is found
	 */

	private boolean isCueProbeByName(Set<CS> set, String cue) {
		for (CS cs : set) {
			if (cs.getName().equals(cue) && cs.isProbe()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return if the phase will be randomly executed
	 * 
	 * @return
	 */
	public boolean isRandom() {
		return random;
	}

	public float getStimulusPrediction(Stimulus s, StimulusElement target) {
		float sumOfPredictions = 0f;
		float currentElementPrediction = 0f;
		for (StimulusElement el2 : s.getList()) {
			//if (target != el2) {
				if (target.getNames().indexOf(el2.getName()) != -1) {
					if (Float.isInfinite(el2.getPrediction(el2.getNames()
							.indexOf(target.getName()), target.getMicroIndex(),
							true,false))
							|| Float.isInfinite(el2.getPrediction(el2
									.getNames().indexOf(target.getName()),
									target.getMicroIndex(), false,false))) {
						System.out.println("infinite pred by: " + el2.getName()
								+ " ");
						System.exit(0);
					}

					currentElementPrediction += el2.getPrediction(el2
							.getNames().indexOf(target.getName()), target
							.getMicroIndex(), true,false);
				}
			//}
		}

		sumOfPredictions += currentElementPrediction;

		
		
		return sumOfPredictions;
	}
	
	public int trialToTrial(int trial, String trialType) {
		if (trialIndexMap.containsKey(trialType) && trialIndexMap.get(trialType).containsKey(trial)) {
			return trialIndexMap.get(trialType).get(trial);
		} else {return -1;}
	}
	
	public int trialToTrial2(int trial, String trialType) {
		if (trialIndexMapb.containsKey(trialType) && trialIndexMapb.get(trialType).containsKey(trial)) {
			return trialIndexMapb.get(trialType).get(trial);
		} else {return -1;}
	}
	
	
	public int getTrialTypeCount(String trialType) {return trialTypeCounterMap.get(trialType);}
	public Set<String> getTrialNames() {return trialIndexMap.keySet();}

	/**
	 * This starts the execution of the algorithm. The method first checks if
	 * the sequence has to be executed in random order and then executes the
	 * same algorithm but in different execution style. If the sequence is
	 * random, creates a tempSequence from the original and runs a simple
	 * shuffle method. The shuffle methods use a random generator which provides
	 * a number from 0 to the end of the sequences length. Then swaps position
	 * with the previous number - position. Finally it calls the algorithm. The
	 * previous task is running iterative depending the number of combinations
	 * that the user has chosen. If the sequence is not supposed to run in
	 * random order it skips this step and goes straight to the algorithm.
	 * 
	 */
	public void runSimulator() {
		results = (TreeMap) cues;
		boolean context = group.getModel().isUseContext();
		float combinations = isRandom() ? group.getModel().getCombinationNo()
				: 1;
		// Shuffle process
		Random generator = new Random();
		TreeMap<String, Stimulus> tempRes, tempProbeRes;
		String gn = group.getNameOfGroup();
		group.makeMap(gn + phaseNum + " r");
		group.makeMap(gn + phaseNum + " r2");
		group.makeMap(gn + phaseNum + " rA");
		group.makeMap(gn + phaseNum + " rA2");
		group.makeMap(gn + phaseNum + " cur");
		group.makeMap(gn + phaseNum + " cur2");
		group.makeMap(gn + phaseNum + " curA");
		group.makeMap(gn + phaseNum + " curA2");
		originalSequence = new ArrayList<Trial>();
		int count = 0;
		for (Trial t : orderedSeq) {
			Trial tmp = new Trial(t.getTrialString()+ "", false, 0, count);
			originalSequence.add(tmp);
			count++;
		}
		TreeMap<String, List<String>> canonicalNames = new TreeMap<String, List<String>>();
		TreeMap<String,Integer> trialTypeCounters = new TreeMap();
		trialTypeCounterMap = new TreeMap<String,Integer>();
		trialIndexMap = new TreeMap<String,TreeMap<Integer,Integer>>();
		trialIndexMapb = new TreeMap<String,TreeMap<Integer,Integer>>();
		ArrayList<String> trialTypeArray = new ArrayList();
		for (int i = 0; i < initialSeq.split("/").length; i++) {
			String s = (String)initialSeq.split("/")[i].replaceAll("[^\\d]", "");
			String s2 = (String)initialSeq.split("/")[i].replaceAll("[\\d]", "");
			double index = s.isEmpty() ? 1 : Double.parseDouble(s);
			for (int j = 0; j < index; j++) {
				//the i index is correct
				trialTypeArray.add((String)initialSeq.split("/")[i].replaceAll("[\\d]", ""));
				
			}
			trialTypeCounters.put(s2,0);
			trialTypeCounterMap.put(s2,0);
			trialIndexMap.put(s2, new TreeMap());
			trialIndexMapb.put(s2, new TreeMap());
		}
		for (String s : trialIndexMapb.keySet()) {
			for (Stimulus stim : group.getCuesMap().values()) {
				if (!presentStimuli.contains(stim.getName())) {
					if (stim.getName().length() == 1 && s.contains(stim.getName())) {
						presentStimuli.add(stim);
					} else if (stim.getName().length() == 3) {
						if (s.contains(stim.getName().charAt(1)+"") && !presentStimuli.contains(stim.getName().charAt(1)+"")) {
							presentStimuli.add(group.getCuesMap().get(stim.getName().charAt(1)+""));
						}
						if (s.contains(stim.getName().charAt(2)+"") && !presentStimuli.contains(stim.getName().charAt(2)+"")) {
							presentStimuli.add(group.getCuesMap().get(stim.getName().charAt(2)+""));
						}
					}
				}
			}
		}
		for (int i = 0; i < orderedSeq.size(); i++) {
			int counter = trialTypeCounters.get(trialTypeArray.get(i) + "");
			trialTypeCounters.put(trialTypeArray.get(i) + "",counter + 1);
			trialTypeCounterMap.put(trialTypeArray.get(i) + "",counter + 1);
			trialIndexMap.get(trialTypeArray.get(i)).put(counter, i);
			trialIndexMapb.get(trialTypeArray.get(i)).put(i, counter);
		}
		for (String s: trialTypeCounters.keySet()) {
			trialTypeCounters.put(s,0);
		}
		for (Stimulus s : group.getCuesMap().values()) {
			s.setResetContext(resetContext);
			for (StimulusElement se : s.getList()) {
				se.setCSCV(std);
				se.setUSCV(group.getModel().getUSCV());
				se.setUSScalar(usScalar);
				se.setCSScalar(csScalar);
				se.setUSPersistence(usPersistence);
			}
		}
		
		for (int i = 0; i < (isRandom() ? combinations : 1)
				&& !control.isCancelled(); i++) {
			List<Trial> tempSeq = orderedSeq;
			int nr;
			if (isRandom()) {
				
				for (int x = 0; x < trials && orderedSeq.size() > 1; x++) {
					nr = generator.nextInt(orderedSeq.size() - 1);
					Trial swap = tempSeq.get(x);
					tempSeq.remove(x);
					tempSeq.add(nr, swap);
				}
				if (i == 0) {
					for (int ran = 0; ran < 10; ran++) {
					for (int x = 0; x < trials && orderedSeq.size() > 1; x++) {
						nr = generator.nextInt(orderedSeq.size() - 1);
						Trial swap = tempSeq.get(x);
						tempSeq.remove(x);
						tempSeq.add(nr, swap);
					}
					}
				}
			}//

			TreeMap<String,TreeMap<Integer,Integer>> trialIndexMap2 = new TreeMap();
			TreeMap<String,Integer> trialTypeCounters2 = new TreeMap();
			for (int j = 0; j < tempSeq.size(); j++) {
				String str =tempSeq.get(j).getTrialString().substring(1);
				trialTypeCounters2.put(str,0);
				trialIndexMap2.put(str, new TreeMap());
			}
			for (int j = 0; j < tempSeq.size(); j++) {
				String str =tempSeq.get(j).getTrialString().substring(1);
				int counter = trialTypeCounters2.get(str + "");
				trialTypeCounters2.put(str + "",counter + 1);
				trialIndexMap2.get(str).put(counter, j);
			}
			// Copies an exact copy of the result treemap and
			// runs the algorithm using this temporarily copy.
			tempRes = (TreeMap) cues;
			tempProbeRes = new TreeMap<String, Stimulus>();// copyKeysMapToTreeMap(cues);
////
			for (Stimulus s : group.getCuesMap().values()) {
				s.setPhase(phaseNum - 1);
				if (i > 0)
					s.incrementCombination();
				if (isRandom()) {
					s.reset(i + 1 == combinations, i == 0 ? 0 : trials*sessions);
				}
			}
			//incorporate saving of trials within algorithm//
			algorithm(tempSeq, tempRes, context, tempProbeRes);
			allSequences.add(tempSeq);
			group.pushCache();
			int maxLength = 0;
			for (Trial trial : tempSeq) {
				HashMap<CS, Stimulus> tempMap = new HashMap<CS, Stimulus>();
				for (CS cs : trial.getCues()) {
					tempMap.put(cs, tempRes.get(cs.getName()));
				}
				Map<CS, int[]> timings = timingConfig.makeTimings(tempMap
						.keySet());
				maxLength = (int) Math.max(maxLength, timings.get(CS.TOTAL)[1]
						+ itis.getMinimum());
			}
			for (Stimulus s : group.getCuesMap().values()) {

				if (i == 0) {
					canonicalNames.put(s.getName(), s.getNames());
				}
				float[][] tempTrialWeights = s
						.getTrialAverageWeights(0,getPhaseNum() - 1);
				float[][] tempTrialWeightsA = s
						.getTrialAverageWeightsA(0,getPhaseNum() - 1);
				for (StimulusElement se : s.getList()) {
					int usNumber = 0;
					for (Stimulus stim : group.getCuesMap().values()) {
						if (stim.isUS) {
							usNumber++;
						}
					}
					
				}
				if (group.getModel().isExternalSave()) {
					if (combinations > 1) {
						for (long key : group.getDBKeys(true)) {
							newVal = (group.dbContains(key, true)) ? group.getFromDB(key) : 0f;
							oldVal = (group.dbContains(key, false)) ? group.getFromDB2(key) : 0f;
							group.addToDB2(key, (oldVal*i + newVal)/(i+1f));
						}
					}
				}
				//

				//group.makeMap(gn + phaseNum + " pTA");
				float[][] processedTrialAverageWeights = new float[tempTrialWeights.length][tempTrialWeights[0].length];

				//
				float[][] processedTrialAverageWeights2 = new float[tempTrialWeights.length][tempTrialWeights[0].length];
				float[][] processedTrialAverageWeightsA = new float[tempTrialWeights.length][tempTrialWeights[0].length];
				float[][] processedTrialAverageWeights2A = new float[tempTrialWeights.length][tempTrialWeights[0].length];
				
				
				group.addToMap(s.getName(), new float[tempTrialWeights.length][tempTrialWeights[0].length], gn + phaseNum + " cur",true);
				group.addToMap(s.getName(), new float[tempTrialWeights.length][tempTrialWeights[0].length], gn + phaseNum + " cur2",true);
				group.addToMap(s.getName(), new float[tempTrialWeights.length][tempTrialWeights[0].length], gn + phaseNum + " curA",true);
				group.addToMap(s.getName(), new float[tempTrialWeights.length][tempTrialWeights[0].length], gn + phaseNum + " curA2",true);
				TreeMap<Integer, TreeMap<String, Float>> inactiveValues = new TreeMap();
				TreeMap<Integer, TreeMap<String, Float>> inactiveValuesA = new TreeMap();
				int presenceTrials = 0;
				int index = s.phaseToTrial(phaseNum - 2, "");
				int index2 = s.phaseToTrial(phaseNum - 1, "");
				int incr = 0;
				if (index == -1) {
					index = 0;
				} else {
					incr = 1;
				}
				if (index2 == -1) {
					index2 = 0;
				}
				int inactiveCounter = 0;
				for (int n = 0; n < tempTrialWeights.length; n++) {
					if (trials != 0 ){
					if (n%(trials == 0 ? 1 : trials) == 0 && n != 0) {
						for (String s2: trialTypeCounters.keySet()) {
							trialTypeCounters.put(s2,0);
						}
						for (Stimulus stim : group.getCuesMap().values()) {
							for (StimulusElement se : stim.getList()) {
								se.incrementSession();
							}
						}
					}
					String currentType = trialTypeArray.get(n%(trials == 0 ? 1 : trials));
					int counter = trialTypeCounters.get(currentType);
					trialTypeCounters.put(currentType,counter + 1);
					int remappingIndex = trialIndexMap.get(currentType).get(counter);
					for (String name : canonicalNames.get(s.getName())) {
						int indexOfName = s.getNames().indexOf(name);
						

						float[][] temp =((float[][])group.getFromDB(s.getName(), group.getNameOfGroup() + phaseNum + " cur"));
						temp[remappingIndex][canonicalNames.get(
								s.getName()).indexOf(name)] = tempTrialWeights[n][indexOfName];
						group.addToMap(s.getName(), temp, group.getNameOfGroup() + phaseNum + " cur", true);
						temp = null;
					
					}
					
					if (n >= index + incr && n <= index2) {
						
						
						if (s.getActivity(phaseNum - 1)[n]) {
							for (String name : canonicalNames.get(s.getName())) {
								int indexOfName = s.getNames().indexOf(name);
								
								float[][] temp =((float[][])group.getFromDB(s.getName(), group.getNameOfGroup() + phaseNum + " curA"));
								temp[index + incr
																+ presenceTrials][canonicalNames.get(
																s.getName()).indexOf(name)] = tempTrialWeightsA[n][indexOfName];
								group.addToMap(s.getName(), temp, group.getNameOfGroup() + phaseNum + " curA", true);
								temp = null;
							}
							presenceTrials++;
						} else {

							inactiveValues.put(inactiveCounter,
									new TreeMap<String, Float>());
							inactiveValuesA.put(inactiveCounter,
									new TreeMap<String, Float>());
							for (String name : canonicalNames.get(s.getName())) {
								int indexOfName = s.getNames().indexOf(name);
								inactiveValues.get(inactiveCounter).put(name,
										tempTrialWeights[n][indexOfName]);
								inactiveValuesA.get(inactiveCounter).put(name,
										tempTrialWeightsA[n][indexOfName]);
							}
							inactiveCounter++;
						}
						if (n == index2 && inactiveCounter > 0) {
							for (int n2 = 0; n2 < inactiveCounter; n2++) {
								for (String name : canonicalNames.get(s
										.getName())) {
									float[][] temp = ((float[][])group.getFromDB(s.getName(), group.getNameOfGroup() + phaseNum + " curA"));
									temp[index + incr
																	+ presenceTrials + n2][canonicalNames
																.get(s.getName()).indexOf(name)] = inactiveValuesA
																	.get(n2).get(name);

									
									group.addToMap(s.getName(), temp, group.getNameOfGroup() + phaseNum + " curA", true);
									temp = null;
								}
							}
						}
					} else {
						for (String name : canonicalNames.get(s.getName())) {
							int indexOfName = s.getNames().indexOf(name);
							
							float[][] temp =((float[][])group.getFromDB(s.getName(), group.getNameOfGroup() + phaseNum + " curA"));
							//System.out.println(temp == null);
							
						temp[n][canonicalNames.get(
									s.getName()).indexOf(name)] = tempTrialWeightsA[n][indexOfName];
							group.addToMap(s.getName(), temp, group.getNameOfGroup() + phaseNum + " curA", true);
							temp = null;
						}
					}


					for (int k = 0; k < group.getCuesMap().size(); k++) {
						float[][] temp = ((float[][])group.getFromDB(s.getName(), group.getNameOfGroup() + phaseNum + " cur2"));
						temp[n][k] = tempTrialWeights[n][k];
						group.addToMap(s.getName(), temp, group.getNameOfGroup() + phaseNum + " cur2", true);
						temp = ((float[][])group.getFromDB(s.getName(), group.getNameOfGroup() + phaseNum + " curA2"));
						temp[n][k] = tempTrialWeightsA[n][k];
						group.addToMap(s.getName(), temp, group.getNameOfGroup() + phaseNum + " curA2", true);
						temp = null;
					}

				}//
				for (String s2: trialTypeCounters.keySet()) {
					trialTypeCounters.put(s2,0);
				}
			}
			}
			averageTheWeights(false,gn + phaseNum + " r",gn + phaseNum + " cur",i,tempSeq,trialIndexMapb,trialIndexMap2);
			averageTheWeights(false,gn + phaseNum + " r2",gn + phaseNum + " cur2",i,tempSeq,trialIndexMapb,trialIndexMap2);
			averageTheWeights(false,gn + phaseNum + " rA",gn + phaseNum + " curA",i,tempSeq,trialIndexMapb,trialIndexMap2);
			averageTheWeights(false,gn + phaseNum + " rA2",gn + phaseNum + " curA2",i,tempSeq,trialIndexMapb,trialIndexMap2);
			// Reshuffle onset sequence
			try {
				timingConfig.advance();
			} catch (NoSuchElementException e) {
				System.err.println("Ran out of variable timings after "
						+ (control.getProgress() - 1) + " trials.");
			}
			timingConfig.restartOnsets();
			itis.reset();
		}
		
		if (group.getModel().isExternalSave()) {
			if (combinations > 1) {
				for (long key : group.getDBKeys(true)) {
					oldVal = (group.dbContains(key, false)) ? group.getFromDB2(key) : 0f;
					group.addToDB(key, oldVal);
				}
			}
		}
		for (Stimulus s : group.getCuesMap().values()) {
			float[][] averagedWeights = processAverageWeights(s,
					gn + phaseNum + " r");
			float[][] averagedWeights2 = processAverageWeights(s,
					gn + phaseNum + " r2");
			
			float[][] averagedWeightsA = processAverageWeightsA(s,
					gn + phaseNum + " rA");
			float[][] averagedWeights2A = processAverageWeightsA(s,
					gn + phaseNum + " rA2");
			Stimulus dummy = null;
			for (Stimulus s22 : group.getCuesMap().values()) {
				dummy = s22;
			}
			int index = dummy.phaseToTrial(phaseNum - 2, "");
			int index2 = dummy.phaseToTrial(phaseNum - 1, "");
			int activeCount = 0;
			int trialCounter = 0;
			for (boolean b : s.getActivity(phaseNum - 1)) {
				if (trialCounter <= index2) {
					if (trialCounter > index) {
						activeCount += (b) ? 1 : 0;

					}
					trialCounter++;
				}
			}
			float[][] avgWeights = new float[trialsInAllPhases][group
					.getCuesMap().size()];
			float[][] avgWeights2 = new float[trialsInAllPhases][group
					.getCuesMap().size()];
			float[][] avgWeightsA = new float[trialsInAllPhases][group
			                                					.getCuesMap().size()];
			float[][] avgWeights2A = new float[trialsInAllPhases][group
			                                					.getCuesMap().size()];
			for (int w1 = 0; w1 < trialsInAllPhases; w1++) {
				for (int w2 = 0; w2 < group.getCuesMap().size(); w2++) {
					avgWeights[w1][w2] = averagedWeights[w1][w2];
					avgWeights2[w1][w2] = averagedWeights2[w1][w2];
					avgWeightsA[w1][w2] = averagedWeightsA[w1][w2];
					avgWeights2A[w1][w2] = averagedWeights2A[w1][w2];
				}
			}
			int incr = 0;
			if (index == -1) {
				index = 0;
			} else {
				incr = 1;
			}
			boolean[] newActivity = new boolean[s.getActivity(phaseNum - 1).length];
			boolean[] trialStringActivity = new boolean[s.getActivity(phaseNum - 1).length];
			for (int q = 0; q < s.getActivity(phaseNum - 1).length; q++) {
				if (q < originalSequence.size() && originalSequence.get(q).getTrialString().contains(s.getName())) {
					trialStringActivity[q] = true;
				} else {trialStringActivity[q] = false;}
				if (q >= index + incr && q < index + incr + activeCount) {
					newActivity[q] = true;
				} else if (q >= index + incr && q <= index2) {
					newActivity[q] = false;
				} else {
					newActivity[q] = s.getActivity(phaseNum - 1)[q];

				}
			}

			s.setNormalActivity(s.getActivity(phaseNum - 1), phaseNum - 1);
			s.setActivity(newActivity, phaseNum - 1);
			s.setTrialStringActivity(trialStringActivity, phaseNum -1);
			s.setTrialAverageWeights(0,avgWeights2, getPhaseNum() - 1);
			s.setOrganizedTrialAverageWeights(0,avgWeights, getPhaseNum() - 1);
			s.setTrialAverageWeightsA(0,avgWeights2A, getPhaseNum() - 1);
			s.setOrganizedTrialAverageWeightsA(0,avgWeightsA, getPhaseNum() - 1);
		}
		if (control.isCancelled()) {
			return;
		}

		cues.putAll(results);
		
		group.removeMap(gn + phaseNum + " r");
		group.removeMap(gn + phaseNum + " r2");
		group.removeMap(gn + phaseNum + " rA");
		group.removeMap(gn + phaseNum + " rA2");
		group.removeMap(gn + phaseNum + " cur");
		group.removeMap(gn + phaseNum + " cur2");
		group.removeMap(gn + phaseNum + " curA");
		group.removeMap(gn + phaseNum + " curA2");
	}
	
	public int getSessions() {return sessions;}

	
	
	
	
	public TreeMap<String, float[][]> averageTheWeights(TreeMap<String, float[][]> old,TreeMap<String, float[][]> current, float combo) {
		
		for (String s : current.keySet()) {
			if (!old.containsKey(s)) {old.put(s, new float[current.get(s).length][current.get(s)[0].length]);}
			for (int i = 0; i < current.get(s).length; i++) {
				for (int j = 0; j < current.get(s)[i].length; j++) {
					old.get(s)[i][j] = (old.get(s)[i][j]*combo + current.get(s)[i][j])/(combo + 1f);
				}
			}
		}
		return old;
		
	}
	
public void averageTheWeights(boolean remap,String mapName,String otherMapName, float combo,List<Trial> tempseq,TreeMap<String,TreeMap<Integer,Integer>> map1,TreeMap<String,TreeMap<Integer,Integer>> map2) {
	//if (remap) {System.out.println("new averaging");}
	if (getNoTrials() != 0) {
	for (String s : group.getKeySet(otherMapName)) {
			vals = (float[][]) group.getFromDB(s, mapName);
			vals2 = (float[][]) group.getFromDB(s, otherMapName);
			if (vals == null) {
				vals = new float[vals2.length][vals2[0].length];
			} 
			boolean addNow = vals2.length*vals2[0].length > 100 ? false : true;
			for (int i = 0; i < vals2.length; i++) {
				int phase = group.trialToPhase(i, "");
				int trialOffset = group.phaseToTrial(phase-1, "");
				if (trialOffset == -1) {trialOffset = 0;} else {trialOffset++;}
				int counter = 0;
				//System.out.println(trialOffset + "  " + i + "  " + phaseNum + "  " + phase);
				String str =  (phase <= this.phaseNum && group.getNoOfPhases() > phase && group.getPhases().get(phase).getNoTrials() != 0) ? group.getPhases().get(phase).getOrderedSeq().get(i-trialOffset).getTrialString().substring(1) : "";
				int howManyAlready = 0;
				if (str.equals("")) {}
				else {
					try {howManyAlready = map2.get(str).get(map1.get(str).get(i));

					counter = map1.get(str).get(i);
					}
					catch (NullPointerException e) {
						if (map1.containsKey(str))// System.out.println(str + " " + i + " " + map1.get(str).containsKey(i));

						if (map1.containsKey(str) && map1.get(str).containsKey(i) && map2.containsKey(str)){
							//System.out.println(map2.get(str).containsKey(map1.get(str).get(i)));
						}
					}
				}
				if (phase > this.phaseNum) {howManyAlready = i;}
				if (!remap) {howManyAlready = i;}
				int first = 0;
				first = this.trialToTrial(0, str);
				if (first == -1) {first = 0;}
				//if (remap && s.equals("A")) {System.out.println("A " + str + "  " + counter +" " + (counter + first) + " " + i + " " + howManyAlready);}
				for (int j = 0; j < vals2[i].length; j++) {
					
					
					if (!str.equals("")) {
						vals[remap ? counter + first : i][j] = (vals[remap ? counter + first : i][j]*combo + vals2[howManyAlready][j])/(combo + 1f);
						
					}
					
				}
			}

			group.addToMap(s, vals, mapName,true);
		}
		}
		
	}

	public float[][] processAverageWeights(Stimulus s,
			TreeMap<String, float[][]> randomTrialAverageWeights) {
		float current;
		float[][] averagedWeights = new float[trialsInAllPhases][group
				.getCuesMap().size()];
			float[][] weightsThisTrial = randomTrialAverageWeights.get(
					s.getName());
			for (int n = 0; n < trialsInAllPhases; n++) {
				for (int n2 = 0; n2 < group.getCuesMap().size(); n2++) {
					if ((averagedWeights.length > n)
							&& (averagedWeights[n].length > n2)
							&& (weightsThisTrial.length > n)
							&& (weightsThisTrial[n].length > n2)) {
						current = weightsThisTrial[n][n2];
						averagedWeights[n][n2] = (current);
					}
				}
			}
		return averagedWeights;
	}
	
	public float[][] processAverageWeights(Stimulus s,
			String mapName) {
		float current;
		float[][] averagedWeights = new float[trialsInAllPhases][group
				.getCuesMap().size()];
			Object ob = group.getFromDB(s.getName(), mapName);
			float[][] weightsThisTrial =  new float[trialsInAllPhases][group
			                                           				.getCuesMap().size()];
			if (ob != null) {weightsThisTrial = (float[][]) ob;}
			for (int n = 0; n < trialsInAllPhases; n++) {
				for (int n2 = 0; n2 < group.getCuesMap().size(); n2++) {
					if ((averagedWeights.length > n)
							&& (averagedWeights[n].length > n2)
							&& (weightsThisTrial.length > n)
							&& (weightsThisTrial[n].length > n2)) {
						current = weightsThisTrial[n][n2];
						averagedWeights[n][n2] = (current);
					}
				}
			}
		return averagedWeights;
	}
	
	public float[][] processAverageWeightsA(Stimulus s,
			String mapName) {
		float current;
		float[][] averagedWeights = new float[trialsInAllPhases][group
				.getCuesMap().size()];
		
		Object ob = group.getFromDB(s.getName(), mapName);
		float[][] weightsThisTrial =  new float[trialsInAllPhases][group
		                                           				.getCuesMap().size()];
		if (ob != null) {weightsThisTrial = (float[][]) ob;}
			for (int n = 0; n < trialsInAllPhases; n++) {
				for (int n2 = 0; n2 < group.getCuesMap().size(); n2++) {
					if ((averagedWeights.length > n)
							&& (averagedWeights[n].length > n2)
							&& (weightsThisTrial.length > n)
							&& (weightsThisTrial[n].length > n2)) {
						current = weightsThisTrial[n][n2];
						averagedWeights[n][n2] = (current);
					}
				}
			}
		return averagedWeights;
	}
	
	public float[][] processAverageWeightsA(Stimulus s,
			TreeMap<String, float[][]> randomTrialAverageWeights) {
		float current;
		float[][] averagedWeights = new float[trialsInAllPhases][group
				.getCuesMap().size()];
		
			float[][] weightsThisTrial = randomTrialAverageWeights.get(
					s.getName());
			for (int n = 0; n < trialsInAllPhases; n++) {
				for (int n2 = 0; n2 < group.getCuesMap().size(); n2++) {
					if ((averagedWeights.length > n)
							&& (averagedWeights[n].length > n2)
							&& (weightsThisTrial.length > n)
							&& (weightsThisTrial[n].length > n2)) {
						current = weightsThisTrial[n][n2];
						averagedWeights[n][n2] = (current);
					}
				}
			}
		return averagedWeights;
	}

	/**
	 * Sets the phase's 'beta' value which represents the non-reinforced
	 * stimuli.
	 * 
	 * @param l
	 *            'beta' value for the non-reinforced stimuli.
	 */
	public void setBetaMinus(Float l) {
		betaMinus = l;
	}

	/**
	 * Sets the phase's 'beta' value which represents the reinforced stimuli.
	 * 
	 * @param l
	 *            'beta' value for the reinforced stimuli.
	 */
	public void setBetaPlus(Float l) {
		betaPlus = l;
		//for (Stimulus s : group.getCuesMap().values()) {
			// s.setUSBoost(1*(1 - betaPlus));
		//}
	}

	/**
	 * @param contextCfg
	 *            the contextCfg to set
	 */
	public void setContextConfig(ContextConfig contextCfg) {
		this.contextCfg = contextCfg;
	}
	
	public TreeMap<String,ContextConfig> getContextConfigs() {return contextCfgs;}
	
	public void addContextConfig(ContextConfig contextCfg) {
		contextCfgs.put(contextCfg.getSymbol(), contextCfg);
	}

	public void setContextSalience(float salience) {
		bgSalience = salience;
	}

	/**
	 * @param control
	 */
	public void setControl(ModelControl control) {
		this.control = control;
	}

	/**
	 * @param cues
	 *            the cues to set
	 */

	public void setUSScalar(float s) {
		usScalar = s;
	}

	public void setCues(Map<String, Stimulus> cues) {
		this.cues = cues;
	}

	/**
	 * @param delta
	 *            the delta to set
	 */
	public void setDelta(Float delta) {
		this.delta = delta;
	}

	/**
	 * Sets the phase's 'gamma' value which represents the discount factor.
	 * 
	 * @param
	 */
	public void setGamma(Float g) {
		gamma = g;
	}

	public void setSTD(Float s) {
		std = s;
	}

	public void setTau1(Float t1) {
		tau1 = t1;
	}

	public void setTau2(Float t2) {
		tau2 = t2;
	}

	public void setVartheta(Float v) {
		vartheta = v;
		if (group.getCuesMap() != null && std != null) {

			for (Stimulus st : group.getCuesMap().values()) {
				st.setParameters(st.isUS ? usCV : std,
						vartheta);
			}
		}
	}

	/**
	 * @param group
	 *            the group to set
	 */
	public void setGroup(SimGroup group) {
		this.group = group;
	}

	/**
	 * @param itis
	 *            the itis to set
	 */
	public void setITI(ITIConfig itis) {
		this.itis = itis;
	}

	/**
	 * Sets the phase's 'lambda' value which represents the non-reinforced
	 * stimuli.
	 * 
	 * @param l
	 *            'lambda' value for the non-reinforced stimuli.
	 */
	public void setLambdaMinus(Float l) {
		lambdaMinus = l;
	}

	/**
	 * Sets the phase's 'lambda' value which represents the reinforced stimuli.
	 * 
	 * @param l
	 *            'lambda' value for the reinforced stimuli.
	 */

	/**
	 * @param orderedSeq
	 *            the orderedSeq to set
	 */
	public void setOrderedSeq(List<Trial> orderedSeq) {
		this.orderedSeq = orderedSeq;
	}

	/**
	 * @param presentCS
	 *            the presentCS to set
	 */
	public void setPresentCS(Set<CS> presentCS) {
		this.presentCS = presentCS;
	}


	/**
	 * Set the random attribute for this phase
	 * 
	 * @param random
	 */
	public void setRandom(boolean random) {
		this.random = random;
	}

	/**
	 * @param timingConfig
	 *            the timingConfig to set
	 */
	public void setTimingConfig(TimingConfiguration timingConfig) {
		this.timingConfig = timingConfig;
	}
	
	
	public int getTrialsForAppearances(Stimulus s, int numberAppearances) {
		int count = 0;
		for (Trial t : getOriginalSequence()) {
			if (s.getName().length() == 1 && t.getTrialString().contains(s.getName())) {count++;}
			else if (s.getName().length() == 3 && (t.getTrialString().contains(s.getName().charAt(1)+"")||t.getTrialString().contains(s.getName().charAt(2)+""))){
				count++;
			}
			if (count == numberAppearances) {return t.getTrialNumber()+1;}
							
			 
		}
		return 0;
	}
	
	public int getAppearancesForTrial(Stimulus s, int n) {
		int count = 0;
		for (Trial t : getOriginalSequence()) {
			if (t.getTrialNumber() <= n) {
				if (s.getName().length() == 1 && t.getTrialString().contains(s.getName())) {count++;}
				else if (s.getName().length() == 3 && (t.getTrialString().contains(s.getName().charAt(1)+"")||t.getTrialString().contains(s.getName().charAt(2)+""))){
					count++;
				}
			} 
		}
		return count;
	}
	
	public int getCompoundTotal(String s) {
		int count = 0;
		for (Trial t : getOriginalSequence()) {
				if (t.getTrialString().substring(1).replaceAll("[^A-Za-z]+", "").equals(s)) {
					count++;
				}
			}
		
		return count;
	}
	
	public int getStimulusAppearances(String s, boolean inclusive) {
		int count = 0;
		for (Trial t : getOriginalSequence()) {
				String processed = t.getTrialString().substring(1).replaceAll("[^A-Za-z]+", "");
				if (s.length() == 1 && (!inclusive ? processed.equals(s) : processed.contains(s))) {
					count++;
				} else if (s.length() == 3 && (processed.equals(s.charAt(1)+"") || processed.equals(s.charAt(2)+""))) {
					count++;
				}
		}
		
		return count == 0 && !inclusive ? getStimulusAppearances(s,true) : count;
	}
	
	public int getCompoundAppearance(String s, int n) {
		int count = 0;
		int normalCount = 0;
		for (Trial t : getOriginalSequence()) {
			if (count < n) {
				if (t.getTrialString().substring(1).replaceAll("[^A-Za-z]+", "").equals(s)) {
					count++;
				}
			normalCount++;
			}
		}
		if (count < n) {return -1;}
		return normalCount;
	}

	/**
	 * @param timings
	 *            set the timing configuration for this phase
	 */
	public void setUsLength(TimingConfiguration timings) {
		timingConfig = timings;
	}

	/**
	 * Store the cues' values for this trial.
	 * 
	 * @param tempRes
	 * @param current
	 */

	protected void store(Map<String, Stimulus> tempRes, Set<String> current, String currentSequence) {
		for (Stimulus cue : group.getCuesMap().values()) {
			
				cue.prestore();
			
		}
		for (Stimulus cue : group.getCuesMap().values()) {
			// Changed to contains to accommodate lazy compound formation
			
				cue.store(currentSequence);
			
		}
		for (Stimulus cue : group.getCuesMap().values()) {
			// Changed to contains to accommodate lazy compound formation
			
				cue.postStore();
			
		}
	}
	
	public ArrayList<Trial> getOriginalSequence(){
			return originalSequence;
		}
	public List<Trial> getCurrentSequence(){
		return orderedSeq;
	}

	protected void updateCues(float betaError, Map<String, Stimulus> tempRes,
			Set<CS> set, int time) {
		int firstCount = 0;
		for (Stimulus cue : group.getCuesMap().values()) {
			int count2 = 0;
			for (Stimulus cue2 : group.getCuesMap().values()) {
				if (!cue.getNames().contains(cue2.getName())) {
					cue.getNames().add(cue2.getName());
				}
				for (StimulusElement el : cue.getList()) {
					for (StimulusElement el2 : cue2.getList()) {
						//if (el != el2) {

							float error1 = (el.getAsymptote()) - elementPredictions[firstCount][el
									.getMicroIndex()];
							float error2 = (el2.getAsymptote()) - elementPredictions[count2][el2
									.getMicroIndex()];
							
							el.updateElement(el2.getDirectActivation(),
									el2.getAlpha(), el2, error1, error2,
									cue2.getName(), group);//
						//}
					}
				}
				count2++;
			}
			firstCount++;
		}
	}

	public void setSubsetSize(int i) {
		setsize = i;
	}

	/**
	 * Takes a trial description string and returns a list of all the CS' in it.
	 * 
	 * @param phaseString
	 * @return
	 */

	public static Collection<CS> stringToCSList(String phaseString) {
		phaseString = phaseString.replaceAll("\\s", "");
		List<CS> stimuli = new ArrayList<CS>();
		phaseString = phaseString == null ? "" : phaseString;
		boolean isTimingPerTrial = Simulator.getController().getModel()
				.isTimingPerTrial();
		// A CS has an index (which of that character is this in the trial
		// string)
		int index = 0;
		// A group - which substring is it in
		int group = 0;
		// And a string position - which position in the overall string is it
		int stringPos = 0;
		// Break into trial strings
		String[] trials = phaseString.split("/");
		Map<Character, Integer> indexes = new HashMap<Character, Integer>();

		for (String trial : trials) {
			String compound = "";
			for (Character c : trial.toCharArray()) {
				if (Character.isLetter(c)) {
					// Remember which number of this character this is
					if (!indexes.containsKey(c)) {
						indexes.put(c, 0);
					}
					// Remember which of the / separated strings this is
					// if this isn't a timings per trial string situation
					if (!isTimingPerTrial) {
						stringPos = 0;
					}
					index = indexes.get(c);
					CS cs = new CS(c.toString(), index, group, false, stringPos);
					cs.setTrialString(trial);
					stimuli.add(cs);
					compound += c;
				}
				// String pos count includes non-CS characters
				stringPos++;
				// Next of this character is a new CS
				if (Character.isLetter(c) && isTimingPerTrial) {
					indexes.put(c, index + 1);
				}
			}
			stringPos++;
			if (isTimingPerTrial) {
				indexes.clear();
				group++;
			}
		}
		return stimuli;
	}

	public TreeMap<String, TreeMap<String, Integer[]>> getProbeTimings() {
		return probeTiming;
	}

	public float getDopamine() {
		return dopamine;
	}

	public void setDopamine(float d) {
		this.dopamine = d;
	}

	public int getPhaseNum() {
		return phaseNum;
	}

	public void setIntegration(float d) {
		integration = d;
	}

	public void setLeak(float d, float c) {
		leak = 1 - d;
		integration = 1 - leak;
		csLeak = 1 - c;
		csIntegration = 1f - csLeak;
	}

	public void setBoost(float b) {
		for (Stimulus s : group.getCuesMap().values()) {
			s.setUSBoost(b);
		}
	}

	public void setCSCLike(float b) {
		for (Stimulus s : group.getCuesMap().values()) {
			s.setCSCLike(b);
		}
	}

	public void setUSPersistence(float us) {
		usPersistence = us;
	}

	public int getTrialLength(int t) {
		return trialLengths.get(t);
	}


	public int getCompleteLength(int t) {
		return completeLengths.get(t);
	}

	public String[] getListed() {
		return listedStimuli;
	}

	public TreeMap<String, Integer> getProbeIndexes() {
		return probeIndexes;
	}

	public float[][][] getTimePointElementErrors() {
		return timePointElementErrors;
	}
	
	public float[][][] getLastTrialElementErrors() {
		return lastTrialElementErrors;
	}
	

	public int getNameIndex(String s) {
		return nameIndexes.get(s);
	}

	public void setCSScalar(Float float1) {
		csScalar = float1;

	}
	
	public void setIntensity(float f) {intensity =f;}

	public void setSelfPrediction(float d) {
		selfDiscount = d;
	}

	public void setUSSTD(float uscv) {
		usCV = uscv;

	}
	
	public int getAppearances(String s) {return trialAppearances.get(s);}

	
	public int getCurrentIti() {return iti;}
	
	public void reset() {
		

		results = new TreeMap<String, Stimulus>();
		
		cues = new TreeMap();
		
		probeIndexes = new TreeMap<String, Integer>();
		nameIndexes = new TreeMap<String, Integer>();
		probeTiming = new TreeMap();
		
		contextCfgs = new TreeMap();
		
		
		trialLengths = new ArrayList<Integer>();
		completeLengths = new ArrayList<Integer>();

		usPredictions = new TreeMap<String,Float>();
		csPredictions = new TreeMap<String,Float>();

		tempMap = new HashMap<CS, Stimulus>();
		allMap = new HashMap<CS, Stimulus>();

		usIndexes = new TreeMap<String, Integer>();
		csIndexes = new TreeMap<String, Integer>();
		activeCS = new ArrayList<CS>();
		trialAppearances = new TreeMap();
	}

	public void setResetContext(boolean resetContext) {
		this.resetContext = resetContext;
		
	}
	
	public float getIntensity() {return intensity;}
	
	public String getCurrentTrial() {return currentSeq.substring(1);
	}
	}
