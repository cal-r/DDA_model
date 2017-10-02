/**
 * SimGroup.java
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
 */
package simulator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import simulator.configurables.ContextConfig;
import simulator.configurables.ContextConfig.Context;
import simulator.configurables.ITIConfig;
import simulator.configurables.TimingConfiguration;
import simulator.util.USNames;
import extra166y.ParallelArray;

/**
 * SimGroup is the class which models a group from the experiment. It will
 * process any new sequences of stimuli and adds them to its ArrayList of
 * phases. It is an intermediate between the Controller and the Phase itself. It
 * contains all the necessary variables need to run a simulation and it keeps a
 * record of the results but in a global view, meaning that the results will be
 * an extract from all phases together.
 */
public class SimGroup implements Runnable {

	public static String getKeyByValue(Map<String, String> configCuesNames,
			String value) {
		String key = null;
		int count = 0;
		if (configCuesNames != null) {for (Map.Entry<String, String> entry : configCuesNames.entrySet()) {
			if (entry.getValue().equals(value)) {
				key = entry.getKey();
				count++;
			}
		}
		}

		return key;
	}

	/**
	 * Helper function for configurals - produce the powerset of the set of
	 * cues, this is the possible set of configural cues.
	 * 
	 * @param originalSet
	 *            Set of cues in a trial
	 * @return the powerset, excluding null
	 */

	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
		Set<Set<T>> sets = new HashSet<Set<T>>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<T>());
			return sets;
		}
		List<T> list = new ArrayList<T>(originalSet);
		T head = list.get(0);
		Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
		for (Set<T> set : powerSet(rest)) {
			Set<T> newSet = new HashSet<T>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}

	// Alberto Fernández August-2011
	// Added boolean parameters isConfiguralCompounds and
	// configuralCompoundsMapping.
	protected ArrayList<SimPhase> phases;
	private Map<String, Stimulus> cues;
	private TreeMap<String,TreeMap<String,Float>> sharedMap;
	private TreeMap<String,Integer> shorter;
	private TreeMap<String,Integer> numberShared;
	private TreeMap<String,ArrayList<StimulusElement>> sharedElements;
	private String nameOfGroup;
	private int noOfPhases, noOfCombinations, count;
	private int[] phaseTrials;
	/** Threaded array. **/
	private ParallelArray<SimPhase> phasePool;
	
	/** All sim cues used in all phases of this group**/
	private ArrayList<StimulusElement> simCues;

	/** At least one random phase indicator. **/
	private boolean hasRandom;

	/** The model this group belongs to. **/
	private SimModel model;
	
	private int totalTrials = 0;
	private int totalStimuli = 0;
	
	private int hasSet = 0;
	
	private float common = 0.2f;
	
	private boolean esther;
	private DB db;
	private DB db2;
	private HTreeMap<Long, Float> map;
	private HTreeMap<Long, Float> map2;
	private int maxDuration = 0;
	private int maxMicros = 0;

	ArrayList totalKeySet;
	private boolean hashSet;
	private int bytes1;
	private int bytes2;
	private int bytes3;
	private int bytes4;
	private int bytes5;
	private int trialTypes;
	private int maxSessions;
	private ArrayList names;
	private DB dbDisk;
	private DB dbDisk2;
	private HTreeMap<Long, Float> onDisk;
	private HTreeMap<Long, Float> onDisk2;
	private long key;
	private int s1;
	private int s2;
	private int s3;
	private int s4;
	private int s5;
	private int rule;
	private float totalMax;
	private TreeMap<String,Stimulus> commons;
	private TreeMap<String,HTreeMap> maps;
	private TreeMap<String,HTreeMap> disks;
	private Object map3;
	private Set<String> set1;
	private TreeMap<String,TreeMap<String,Object>> cache;
	private TreeMap<Long,Float> cache1;
	private TreeMap<Long,Float> cache2;
	private Runtime runtime;
	private File f1;
	private File f2;
	private ArrayList<File> files;
	private ArrayList<String> trialStrings;
	private ArrayList<String> trialStrings2;
	private TreeMap<String,Integer> appearanceList;
	private TreeMap<String,ArrayList<Integer>> compoundAppearanceList;
	private long memory;
	/**
	 * Create a group
	 * 
	 * @param n
	 *            name of the Group
	 * @param np
	 *            number of phases
	 * @param rn
	 *            number of combinations
	 * @return true if the method completed without any errors and false
	 *         otherwise.
	 */
	public SimGroup(String n, int np, int rn, SimModel model) {
		files = new ArrayList();
		runtime = Runtime.getRuntime();
		nameOfGroup = n;
		noOfPhases = np;
		noOfCombinations = rn;
		count = 1;
		simCues = new ArrayList<StimulusElement>();
		compoundAppearanceList = new TreeMap();
		maps = new TreeMap();
		disks = new TreeMap();
		commons = new TreeMap();
		cache = new TreeMap();
		cache1 = new TreeMap();
		cache2 = new TreeMap();
		cues = new TreeMap<String, Stimulus>();
		phases = new ArrayList<SimPhase>(noOfPhases);
		phaseTrials = new int[noOfPhases];
		trialStrings = new ArrayList();
		trialStrings2 = new ArrayList();
		appearanceList = new TreeMap();
		this.setModel(model);
	}

	/**
	 * Adds a new phase in the group's arraylist. The stimuli sequence of the
	 * given is being processed mainly so it could be added as a new SimPhase
	 * object and secondary it might produce new cues which weren't on previous
	 * phases. This new cues are added on the group's cue list as well.
	 * 
	 * @param seqOfStimulus
	 *            the stimuli sequence of the given phase.
	 * @param boolean to know if the phase is going to be randomly executed
	 * @param int the number of the current phase
	 * @param boolean to know if the phase is going to use configural cues
	 * @param mapping
	 *            with configural compounds from their "virtual" name
	 * @return true if the method completed without any errors and false
	 *         otherwise.
	 */

	public boolean addPhase(String seqOfStimulus, boolean isRandom,
			int phaseNum,  boolean isConfiguralCompounds,
			TreeMap<String, String> configuralCompoundsMapping,
			TimingConfiguration timings, ITIConfig iti, ContextConfig context,boolean vartheta) {

		seqOfStimulus = seqOfStimulus.toUpperCase(); // Sequence is always
														// stored in upper case.
														// Alberto Fernández
													// August-2011
		files = new ArrayList();
		List<Trial> order = new ArrayList<Trial>(50);
		Set<Trial> trials = new HashSet<Trial>();
		String sep = "/";
		String[] listedStimuli = seqOfStimulus.toUpperCase().split(sep);
		// CSC of cues. J Gray
		Stimulus cscCues;
		if (!cues.containsKey(context.getSymbol())
				&& !context.getContext().equals(Context.EMPTY)) {
			// Modified by J Gray to add CSC cues.
			cscCues = new Stimulus(this,context.getSymbol(), context.getAlphaR(),totalTrials,totalStimuli);
			cues.put(context.getSymbol(), cscCues);
		}

		// Added by Alberto Fernández
		// Introduce "virtual" cues (lower case letters) in case of configural
		// compounds.

		int noStimuli = listedStimuli.length;
		Map<String, SimStimulus> stimuli = new HashMap<String, SimStimulus>();
        Set<String> configuralsAddedThisGroup = new HashSet<String>();

		for (int i = 0; i < noStimuli; i++) {
			String selStim = listedStimuli[i], repStim = "", cuesName = "", stimName = "";
			boolean reinforced = false;
			boolean oktrials = false, okcues = false, okreinforced = false;
			boolean probe = false; // Is this a probe trial

			if (model.isUseContext()) {
				cuesName = context.getSymbol();
			}

			String compound = "";
			int noStimRep = 1;
			for (int n = 0; n < selStim.length(); n++) {
				char selChar = selStim.charAt(n);

				if (Character.isDigit(selChar) && !oktrials) {
					repStim += selChar;
				} else if (selChar == '^') { // This is a probe trial
					probe = true;
				} else if (Character.isLetter(selChar) && !okcues) {
					oktrials = true;
					cuesName += selChar;
					if (!cues.containsKey(selChar + "")) {
						// Modified by J Gray to add CSC cues.
						cscCues = new Stimulus(this,selChar + "", 0.25f,totalTrials,totalStimuli);
						cues.put(selChar + "", cscCues);
					}
					compound += selChar;
				} else if ((USNames.isUS(selChar + ""))) {
					cuesName += selChar;
					oktrials = true;
					okcues = true;
					reinforced = (USNames.isReinforced(selChar + ""));
					okreinforced = true;
					if (reinforced && !cues.containsKey(selChar + "")) {
						//Modified by Niklas 2014 to treat US like a CS
						Stimulus usCue = new Stimulus(this,selChar + "", 0.25f,totalTrials,totalStimuli);
						cues.put(selChar + "", usCue);
					} else if (!reinforced && !USNames.hasReinforced(cues.keySet().toArray(new String[cues.size()]))) {
						Stimulus usCue = new Stimulus(this,"+" + "", 0.25f,totalTrials,totalStimuli);
						
						cues.put("+" + "", usCue);
					}


				} else
					return false;
			}
			
            cscCues = new Stimulus(this,compound + "", 0.25f,totalTrials,totalStimuli);
            //if (model.isCompound()) { cues.put(compound + "", cscCues);}
            // Added by Alberto Fernández August-2011
            if ((model.isUseContext() || compound.length() > 1)
                    && isConfiguralCompounds) {
                Set<String> compoundSet = new HashSet<String>();
                for (char c : compound.toCharArray()) {
                    compoundSet.add(c + "");
                }
                if (!model.isSerialConfigurals()) {

                    for (Set<String> set : powerSet(compoundSet)) {
                        String s = "";
                        List<String> bits = new ArrayList<String>(set);
                        Collections.sort(bits);
                        for (String str : bits) {
                            s += str;
                        }
                        // Add configural cue as a "virtual" cue (lower
                        // case letter)
                        s = model.isUseContext() ? context.getSymbol()
                                + s : s;
                        if (s.length() > 1) {
                            String virtualCueName = getKeyByValue(
                                    configuralCompoundsMapping, s);
                            if (virtualCueName == null) {
                                if (configuralCompoundsMapping
                                        .isEmpty()) {
                                    virtualCueName = "a";
                                } else {
                                    char c = configuralCompoundsMapping
                                            .lastKey().charAt(0);
                                    c = (char) (c + 1);
                                    while(!Character.isLetter(c) || Character.toUpperCase(c) == c || Context.isContext(c+"")) {
                                        c++;
                                        //System.out.println("loop simgroup");
                                    }
                                    virtualCueName = "" + c;
                                }
                                configuralCompoundsMapping.put(
                                        virtualCueName, s);
                            }
                            cuesName += virtualCueName;
                            configuralsAddedThisGroup.add(virtualCueName);
                            if (!cues.containsKey(virtualCueName + "")) {
                                // Modified to use CSCs. J Gray
                            	 cscCues = new Stimulus(this,virtualCueName + "", 0.25f,totalTrials,totalStimuli);
                                cues.put(virtualCueName + "", cscCues);
                            }
                            String compoundName = s + virtualCueName;
                            cscCues = new Stimulus(this,compoundName + "", 0.25f,totalTrials,totalStimuli);
                            cues.put(compoundName + "", cscCues);
                        }
                    }
                }

            }

			int stringPos = 0;
			//Find start position of this trial string
			for(int s = 0; s < i; s++) {
				stringPos += listedStimuli[s].length() + 1;
			}

            //Determine how many trial strings identical with this
            //precede it
            int trialNum = 0;
            for(int s = i - 1; s >= 0; s--) {
                if(listedStimuli[s].equals(listedStimuli[i])) {
                    trialNum++;
                }
            }
			
			stimName = cuesName;
			Trial trial = new Trial(stimName, probe,
					(model.isTimingPerTrial() ? i : 0), selStim, stringPos, trialNum);
			trials.add(trial);

			if (repStim.length() > 0)
				noStimRep = Integer.parseInt(repStim);

			if (stimuli.containsKey(stimName))
				stimuli.get(stimName).addTrials(noStimRep);
			else
				stimuli.put(stimName, new SimStimulus(stimName, noStimRep,
						cuesName, reinforced));

			for (int or = 0; or < noStimRep; or++)
				order.add(trial);
		}
		for (Stimulus cue : cues.values()) {
			for (Stimulus cue2 : cues.values()) {
				if (!cue.isCS || !cue2.isCS || cue2.getName().equals(cue.getName()) || cue.isCommon() || cue2.isCommon()) {}
				else {
					if (commons.containsKey("c"+cue.getName()+cue2.getName()) || commons.containsKey("c"+cue2.getName()+cue.getName())) {}
					else {
						cscCues = new Stimulus(this,"c"+cue.getName()+cue2.getName(), 0.25f,totalTrials,totalStimuli);
						commons.put("c"+cue.getName()+cue2.getName(), cscCues);
						
					}
				}
			}
		}
		
		for (String key : commons.keySet()) {
			String first =key.charAt(1)+"";
			String second = key.charAt(2)+"";
			Stimulus stim1 = cues.get(first);
			Stimulus stim2 = cues.get(second);
			Stimulus stim3 = commons.get(key);
			stim1.addCommon(second,stim3);
			stim2.addCommon(first,stim3);
			if (!cues.containsKey(key))cues.put(key,stim3);
		}
		
		
		timings.setTrials(order.size());
        timings.restartOnsets();
		List<List<List<CS>>> sequences = timings.sequences(new HashSet<Trial>(
				order));
        if(true) {
            sequences = timings.compounds(new ArrayList<Trial>(order));
        }

		
		TreeMap<String, Float> tm = model.getAlphaCues();
		Iterator<String> it = tm.keySet().iterator();
		while(it.hasNext()) {
			String stim = it.next();
			if (!USNames.isUS(stim) && cues.containsKey(stim) && stim.length() == 1) {
				float alphaValue = (tm.containsKey(stim)) ? tm.get(stim) : -1;
				if (stim.length() > 1) {
					char[] characters = stim.toCharArray();
					
					for (char character : characters) {
						alphaValue += tm.get(character +"");
					}
					alphaValue /= stim.length();
				}
				if (alphaValue != -1 && alphaValue != 0) cues.get(stim).setRAlpha(alphaValue);
				
			}
			
		}
		
		// Set indicator that this group has at least one randomised phase
		hasRandom = hasRandom ? hasRandom : isRandom;
        timings.restartOnsets();
        //sortStimuli();

        for (int j = 0; j < order.size(); j++ ){
        	

        	Map<CS, int[]> timing = timings.makeTimings(order.get(j).getCues());
        	totalMax = (int) Math.round(timing.get(CS.TOTAL)[1]);
        	for (Stimulus stim : cues.values()) {
	        	char[] names = stim.getName().toCharArray();
				CS[] css = new CS[names.length];
				int onset = -1;
				int offset = 200;
				int counter = 0;
				for (char character : names) {
					for (CS cs : order.get(j).getCues()) {
						if (cs.getName().equals((String) (character + ""))) {
							css[counter] = cs;
						}
					}
					int tempOnset = (css[counter] != null && timing.containsKey(css[counter])) ? timing.get(css[counter])[0]: -1;
					onset = Math.max(tempOnset,onset);
					int tempOffset = (css[counter] != null && timing.containsKey(css[counter])) ? timing.get(css[counter])[1]: -1;		
					offset = Math.min(tempOffset,offset);
					counter++;
				}
				stim.setTiming(onset,offset);
				int duration = (!Context.isContext(stim.getName())) ? (offset-onset) : (int) (Math.round(timing.get(CS.TOTAL)[1]));
				if ((duration) > stim.getMaxDuration()) { stim.setMaxDuration(duration);}
				if (duration > totalMax) {totalMax = duration;}
				if ( stim.getMaxDuration() > totalMax) {totalMax = stim.getMaxDuration();}
			}
        	for (Stimulus s : cues.values()) {
        		if (s.getName().length() > 1) {
        			int onset = Math.max(0, Math.min(cues.get(s.getName().charAt(1)+"").getTheOnset(),cues.get(s.getName().charAt(2)+"").getTheOnset()));
        			int offset = Math.max(cues.get(s.getName().charAt(1)+"").getTheOffset(),cues.get(s.getName().charAt(2)+"").getTheOffset());
        			s.setMaxDuration(offset-onset);
        			s.setTiming(onset, offset);
        		}
        		s.setAllMaxDuration((int)totalMax);
        	}
        	for (String name: USNames.getNames()) {

        		if (cues.containsKey(name)) {cues.get(name).setAllMaxDuration((int)totalMax);}
        		if (cues.containsKey(name) && cues.get(name).getMaxDuration() < (timing.get(CS.US)[1] - timing.get(CS.US)[0])) {cues.get(name).setMaxDuration((timing.get(CS.US)[1] - timing.get(CS.US)[0]));}
        	}
        	maxMicros = (int) totalMax;
        }
    	float totalMax = 0;
        for (int j = 0; j < order.size(); j++ ){
        	Map<CS, int[]> timing = timings.makeTimings(order.get(j).getCues());
        	totalMax =  (float) Math.max(totalMax,(int) Math.round(this.totalMax + iti.getMinimum()));
        	
        }
        maxDuration = (int) totalMax;
        for (Stimulus s : cues.values()) {
        	if (s.getAllMaxDuration() < totalMax) s.setAllMaxDuration((int)totalMax);
    	}
        
        names = new ArrayList(cues.keySet());
        
		return addPhaseToList(phaseNum,seqOfStimulus, order, stimuli, isRandom, timings,
				iti, context,totalTrials,listedStimuli,vartheta);
	}
	
	public float getTotalMax() {return maxDuration;}
	public int getSubElementNumber() {return model.getSetSize();}
	/**
	 * Add a phase to this groups list of phases.
	 * 
	 * @param seqOfStimulus
	 * @param order
	 * @param stimuli
	 * @param isRandom
	 * @param timings
	 * @param iti
	 * @return
	 */

	protected boolean addPhaseToList(int phaseNum,String seqOfStimulus, List<Trial> order,
			Map<String, SimStimulus> stimuli, boolean isRandom,
			TimingConfiguration timings, ITIConfig iti, ContextConfig context, int totalTrials,String[] listedStimuli,boolean vartheta) {
		
		return phases.add(new SimPhase(phaseNum,1,seqOfStimulus, order, stimuli, this,
				isRandom, timings, iti, context,totalTrials,listedStimuli,vartheta));
	}

	/**
	 * Empties every phase's results. It iterates through the phases and calls
	 * the SimPhase.emptyResults() method. This method cleans up the results
	 * variable.
	 * 
	 */
	public void clearResults() {
		for (int i = 0; i < noOfPhases; i++) {
			SimPhase sp = phases.get(i);
			//sp.emptyResults();
		}
		count = 1;
	}

	/**
	 * Checks if this is the name of a configural cue (i.e. contains lowercase
	 * characters)
	 * 
	 * @param cueName
	 *            the cue to check
	 * @return true if this is a configural cue
	 */

	protected boolean configuralCue(String cueName) {
		return !cueName.equals(cueName.toUpperCase());
	}

	/**
	 * 
	 * @return a count of how many phases are random in this group
	 */

	public int countRandom() {
		int count = 0;
		for (SimPhase phase : phases) {
			if (phase.isRandom()
					|| phase.getTimingConfig().hasVariableDurations()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 
	 * @return a map of context names to their configurations.
	 */

	public Map<String, ContextConfig> getContexts() {
		Map<String, ContextConfig> contexts = new HashMap<String, ContextConfig>();

		for (SimPhase phase : phases) {
			contexts.put(phase.getContextConfig().getSymbol(),
					phase.getContextConfig());
		}

		return contexts;
	}

	/**
	 * Returns the number of trials that have been produced so far.
	 * 
	 * @return the number of trials so far.
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Returns the TreeMap which contains the cues and their values. An
	 * important object on overall group result processing.
	 * 
	 * @return the group's cues.
	 */
	public Map<String, Stimulus> getCuesMap() {
		return cues;
	}

	/**
	 * @return the model
	 */
	public SimModel getModel() {
		return model;
	}

	/**
	 * Returns the group's current name. By default shall be "Group n" where n
	 * is the position that has on the table.
	 * 
	 * @return the name of the group.
	 */
	public String getNameOfGroup() {
		return nameOfGroup;
	}

	/**
	 * @return the noOfPhases
	 */
	public int getNoOfPhases() {
		return noOfPhases;
	}


	/**
	 * Returns the ArrayList which contains the SimPhases, the phases that run
	 * on this specific group.
	 * 
	 * @return the group's phases.
	 */
	public List<SimPhase> getPhases() {
		return phases;
	}

	/**
	 * 
	 * @return a boolean indicating that this group has at least one random
	 *         phase.
	 */

	public boolean hasRandom() {
		return hasRandom;
	}

	/**
	 * Adds one more value to the count variable which represents the trials so
	 * far.
	 */
	public void nextCount() {
		count++;
	}

	/**
	 * Returns the number of combinations that shall be run if the user has
	 * chosen a random sequence.
	 * 
	 * @return the number of combinations.
	 */
	public int noOfCombin() {
		return noOfCombinations;
	}

	/**
	 * @return the number of runs of the algorithm in this group
	 */
	public int numRandom() {
		int count = 0;
		for (SimPhase phase : phases) {
			int increment = phase.isRandom() ? model.getCombinationNo() : 0;
			increment = phase.getTimingConfig().hasVariableDurations() ? Math
					.max(increment, 1) * model.getVariableCombinationNo()
					: increment;
			count += increment;
		}
		return count;
	}

	
	/**
	 * The Runnable's run method. This starts a new Thread. It actually runs
	 * every SimPhases.runSimulator() method which is the method that uses the
	 * formula on the phases.
	 */
	@Override
	public void run() {
		// Add to phasepool so we can still cancel them quickly if required
		createDBs();
		addMicrostimuli();
		phasePool = ParallelArray.createEmpty(noOfPhases, SimPhase.class,
				Simulator.fjPool);
		phasePool.asList().addAll(phases);
		int trialCount = 0;
		for (int i = 0; i < noOfPhases; i++) {
			trialCount += phases.get(i).getNoTrials();
			phaseTrials[i] = trialCount;
			
		}
		for (int i = 0; i < noOfPhases; i++) {
			if (model.contextAcrossPhase()) {
				// Deal with different omega per phase
				for (Entry<String, Stimulus> entry : cues.entrySet()) {
					String realName = model.getConfigCuesNames().get(
							entry.getKey());
					realName = realName == null ? "" : realName;
				}
			}
			if (i > 0) {
				//phases.get(i).setDopamine(phases.get(i-1).getDopamine());
				for (Stimulus s : phases.get(i).getCues().values()) {
					for (Stimulus s2 : phases.get(i-1).getCues().values()) {
						if (s.getName().equals(s2.getName())) {
							for (StimulusElement se : s.getList()) {
								for (StimulusElement se2 : s2.getList()) {
									if (se.getMicroIndex() == se2.getMicroIndex()) {
										se.setVariableSalience(se2.getVariableSalience());
										se.setCSVariableSalience(se2.getCSVariableSalience());
									}
									
								}
							}
						}
					}
				}
			}	
			phases.get(i).runSimulator();
		}
	}
	
	public int[] getPhaseTrials() {return phaseTrials;}
	
	public void setCommon(float common) {this.common = common;}
	public float getCommon() {return common;}
	
	public void addMicrostimuli () {
		
		float currentCommon = 0;
		float allCommon = 0;
		numberShared = new TreeMap();
		shorter = new TreeMap();
		int shortest = 0;
		String allString = "";
		for (Stimulus s: cues.values()) {
			shortest = Math.max(shortest,s.getAllMaxDuration());
		}
		for (Stimulus cl : cues.values()) {
			cl.addMicrostimuli(esther);
		}

		for (Stimulus s: cues.values()) {
			if (s.getMaxDuration() != 0 && s.isCS) {
				if (!allString.equals("")) {allString += ", ";}
				allString += s.getName();
				shortest = (int) Math.min(s.getMaxDuration(),shortest);
			}
			
		}/*
		if (sharedMap != null && sharedMap.containsKey(allString)) {
			allCommon = sharedMap.get(allString).get("N/A");
			}
			else {	
			allCommon = common;	
				
		}
		for (Stimulus s: cues.values()) {
			for (Stimulus s2: cues.values()) {
				if (s!= s2) {
					if (sharedMap != null && sharedMap.containsKey(s.getName()) && sharedMap.get(s.getName()).containsKey(s2.getName())) {
					currentCommon = (Float) sharedMap.get(s.getName()).get(s2.getName());
					}
					else {
					currentCommon = common;	
						
					}
					if (!s.isCS || !s2.isCS) {
						s.setCommon(s2, 0f);
						
					} else {
						s.setCommon(s2, currentCommon);
					}
					float duration1 = s.getMaxDuration();
					float duration2 = s2.getMaxDuration();
					float smallest = Math.min(duration1, duration2);
					if (!(numberShared.containsKey(s.getName()+", " + s2.getName()) || numberShared.containsKey(s2.getName()+", " + s.getName()))) {
							numberShared.put(s.getName()+", " + s2.getName(), (int) Math.round(smallest*currentCommon));//sharedMap.get(s.getName()).get(s2.getName())));
							shorter.put(s.getName()+", " + s2.getName(), (int) Math.round(smallest));
						
					}
				} else {
					s.setCommon(s,1);
				}
			}
		}
		for (Stimulus s: cues.values()) {
			for (Stimulus s2: cues.values()) {
				if (s!= s2 && (s.isCS && s2.isCS)) {
					ArrayList<Integer> sharedIndexes = new ArrayList();
					int duration = 0;
					int number = 0;
					if (shorter.containsKey(s.getName() + ", " + s2.getName())) {
						duration = shorter.get(s.getName() + ", " + s2.getName());
						number = numberShared.get(s.getName() + ", " + s2.getName());
								
					}
					if (shorter.containsKey(s2.getName()+", "+s.getName())) {
						duration = shorter.get(s2.getName() + ", " + s.getName());
						number = numberShared.get(s2.getName() + ", " + s.getName());
					}
					int count = 0;
					while (count < number) {
						int nextRandom = (int) Math.round(Math.random()*(duration-1));
						if (!sharedIndexes.contains(nextRandom)) {
							//s2.getList()[nextRandom] = s.getList()[nextRandom];
							sharedIndexes.add(nextRandom);
							count++;
						}
					}
				}
			}
		}

		boolean commonToAll = true;
		int allShared = (int) Math.round(shortest*allCommon);
		int count = 0;
		ArrayList<Integer> sharedIndexes = new ArrayList();

		
		if (commonToAll && sharedMap != null) {
			
		
		while (count < allShared) {
			int nextRandom = (int) Math.round(Math.random()*(shortest-1));
			if (!sharedIndexes.contains(nextRandom)) {
				StimulusElement commonToAllElement = null;
				while (commonToAllElement == null) {
					for (Stimulus s: cues.values()) {
						if (s.isCS) {
							if (Math.random() > 0.9) {
								commonToAllElement = s.getList()[nextRandom];
							}
						}
						
					}
					//System.out.println("loop simgroup");
				}
				sharedIndexes.add(nextRandom);
				count++;
			}
		}
		//
		}*/
		
	}

	public void setControl(ModelControl control) {
		for (SimPhase phase : phases) {
			phase.setControl(control);
		}
	}

	/**
	 * @param model
	 *            the model to set
	 */
	public void setModel(SimModel model) {
		this.model = model;
	}
	
	public void setEsther(boolean b) {
		esther = b;
	}

	/**
	 * @param noOfPhases
	 *            the noOfPhases to set
	 */
	public void setNoOfPhases(int noOfPhases) {
		this.noOfPhases = noOfPhases;
	}

    public int trialCount() {
        int count = 0;
        for(SimPhase p : phases) {
            int multiplier = p.isRandom() ? Simulator.getController().getModel().getCombinationNo() : 1;
            multiplier *= p.getTimingConfig().hasVariableDurations() ? Simulator.getController().getModel().getVariableCombinationNo() : 1;
            count += p.getTimingConfig().getTrials() * multiplier;
        }
        return count;
    }
    
    public void clearMap(String map) {
    	maps.get(map).clear();
    }
    public void removeMap(String map) {
    	
    	//dbDisk.
    	//dbDisk.
    	//disks.get(map).clear();
    	//maps.get(map).clear();
    	maps.remove(map); disks.remove(map); cache.remove(map);}
    public TreeMap<String,HTreeMap> getMaps() {return maps;}
    
    public int getCombinationNo() {
    	return Simulator.getController().getModel().getCombinationNo();
    }
    
    public ArrayList<StimulusElement> getSimCues() {
    	
    	return simCues;
    }
    
    public boolean getEsther() {return esther;}
    
    public void setSharedElements(TreeMap<String,TreeMap<String,Float>> data) {
    	for (String s : data.keySet()) {
    		TreeMap<String,Float> tm = data.get(s);
    		TreeMap<String,Float> tm2 = new TreeMap();
    		for (String s2 : tm.keySet()) {
    			tm2.put(s2 + "", tm.get(s2) + 0f);
    		}
    		if (sharedMap == null) {
    	    	sharedMap = new TreeMap();
    	    	hasSet = 1;}
    		sharedMap.put(s,tm2);
    	}
    	
    }
    
    public void setTotalTrials(int trials) {totalTrials = trials;}
    public int getTotalTrials() {return totalTrials;}
    public void setTotalStimuli(int stimuli) {totalStimuli = stimuli;}
    public int getTotalStimuli() {return totalStimuli;}
    
    public void initializeDBKeys() {
		maxSessions = 1;
		totalKeySet = new ArrayList();
		for (int i = 0; i < noOfPhases; i++) {
			maxSessions = Math.max(maxSessions, model.getGroupSession(nameOfGroup, i+1));
			for (int j = 0; j < getPhases().get(i).getOrderedSeq().size(); j++) {
				String s = getPhases().get(i).getOrderedSeq().get(j).toString();
					if (!totalKeySet.contains(s)) { totalKeySet.add(s);}
				
			}
		}
		trialTypes = totalKeySet.size();
		
		bytes1 = getC(totalStimuli*totalStimuli*noOfPhases);
		s1 = bytes1;
		bytes2 = getC(maxMicros);
		s2 = bytes1+bytes2+1;
		bytes3 = getC(totalTrials);
		s3 = bytes1+bytes2+bytes3+2;
		bytes4 = getC(maxDuration);
		s4 = bytes1+bytes2+bytes3+bytes4+3;
		bytes5 = getC(maxSessions*trialTypes);
		s5 = bytes1+bytes2+bytes3+bytes4+bytes5+4;
		//System.out.println(s5);
		/*System.out.println("stims: " + stims + " //  " + getC(stims));
		System.out.println("micros: " + micros + " //  " + getC(micros));
		System.out.println("trials: " + trials + " //  " + getC(trials));
		System.out.println("timepoints: " + timepoints + " //  " + getC(timepoints));
		System.out.println("phases: " + phases + " //  " + getC(phases));
		System.out.println("maxSessions: " + maxSessions + " //  " + getC(maxSessions));
		System.out.println("trialTypes: " + trialTypes + " //  " + getC(trialTypes));*/
		
		
    	
    }
    
    public int getC(int val) {
    	if (val == 1 || val == 0) {return 1;}
    	else return (int) Math.ceil(Math.log(val)/Math.log(2));
    }
    
    public ArrayList<String> getNames() {return names;}
    
public long createDBString(StimulusElement se,String trialString, Stimulus other, int phase, int session, int trial, int timepoint,boolean isA) {
		
    	if (!hashSet) {initializeDBKeys(); hashSet = true;}
    	//key = 0;
    	//String dbString = "";		
    	//int otherNameIndex =  totalStimuli*phase +getNames().indexOf(other.getName());
		//dbString += se.getName() + "," + se.getMicroIndex() +"," + other.getName() + ",";
		//dbString += session + "," + phase + "," + trial + "," + trialString + "," + timepoint + ",";
		//dbString += isA ? 1 : 0;
    	//System.out.println(trialIndex + "  " + trialString + " " + phase + " " + session + " " + trial + " " + timepoint);
		/*key =  (long) ((totalStimuli*totalStimuli*(phase) +
				totalStimuli*getNames().indexOf(se.getName()) +
				getNames().indexOf(other.getName())) +
				c1*(se.getMicroIndex()) + 
				c2*trial +  c3*(timepoint) + 
				c4*((session-1)*(trialTypes) + trialIndex) +
				c5*(isA? 1 : 0));*/
		
		//key =  
		
		
		//if (totalStimuli*totalStimuli*(phase) + totalStimuli*getNames().indexOf(se.getName()) +getNames().indexOf(other.getName())>=c1 ) {System.out.println("fail");}
		//find better/any hash
		//
		//find (2^3)^k > #names, #micros, #sessions, #phases, #trials, # trialtypes,#timepoints, 
		return (((((((((isA? 1 : 0) << (bytes5) + ((session-1)*(totalKeySet.size()) + totalKeySet.indexOf(trialString))) << (bytes4)) + timepoint) << (bytes3)) + trial) << (bytes2)) + se.getMicroIndex()) << (bytes1)) + (totalStimuli*totalStimuli*(phase) +
				totalStimuli*getNames().indexOf(se.getName()) +	getNames().indexOf(other.getName()));
	}
    
    public Set<Long> getDBKeys(boolean first) {
    	if (first) return map.keySet();
    	else return map2.keySet();
    }
	
	public void createDBs() {
		
		int numberOfMaps = (int) (model.getPhaseNo()*8 + this.getCuesMap().size()*totalMax*5);
		long memoryCapacity = (long) Math.round((float)memory/(float)numberOfMaps);
		if(f1 != null) {f1.delete();}
		if(f2 != null) {f2.delete();}
		String hash = "" + System.currentTimeMillis();
		File dir = new File(System.getProperty("user.home") + System.getProperty("file.separator")
		+ "TempDD");
		if (!dir.exists())dir.mkdir();
		dir.setExecutable(true);
		dir.setReadable(true);
		dir.setWritable(true);
		
		f1 = new File(System.getProperty("user.home") + System.getProperty("file.separator")
		+ "TempDD" + System.getProperty("file.separator") + this.getNameOfGroup().replace(" ", "_") + "overflow" + hash + ".db");
		f2 = new File(System.getProperty("user.home") + System.getProperty("file.separator")
		+ "TempDD" + System.getProperty("file.separator") + this.getNameOfGroup().replace(" ", "_")+ "overflow2" + hash + ".db");
		if (f1.exists()) {f1.delete();}
		if (f2.exists()) {f2.delete();}
		files.add(f1);
		files.add(f2);
		dbDisk = DBMaker.fileDB(System.getProperty("user.home") + System.getProperty("file.separator")
		+ "TempDD" + System.getProperty("file.separator") + this.getNameOfGroup().replace(" ", "_") + "overflow" + hash + ".db").closeOnJvmShutdown().allocateStartSize(memoryCapacity).concurrencyDisable().fileLockDisable().fileDeleteAfterClose().transactionEnable().make();
		dbDisk2 = DBMaker.fileDB(System.getProperty("user.home") + System.getProperty("file.separator")
		+ "TempDD" + System.getProperty("file.separator") + this.getNameOfGroup().replace(" ", "_")+ "overflow2" + hash + ".db").closeOnJvmShutdown().allocateStartSize(memoryCapacity).concurrencyDisable().fileLockDisable().fileDeleteAfterClose().transactionEnable().make();
				
		db = DBMaker.memoryDB().make();
		db2 = DBMaker.memoryDB().make();
				// Big map populated with data expired from cache
				onDisk = dbDisk.hashMap("onDisk",Serializer.LONG, Serializer.FLOAT).create();
				onDisk2 = dbDisk2.hashMap("onDisk2",Serializer.LONG, Serializer.FLOAT).create();
				
				
				map = db.hashMap("inMemory",Serializer.LONG, Serializer.FLOAT).expireMaxSize(memoryCapacity).expireStoreSize(memoryCapacity).expireOverflow(onDisk).expireExecutor(Executors.newScheduledThreadPool(2)).create();
				map2 = db2.hashMap("inMemory2",Serializer.LONG, Serializer.FLOAT).expireMaxSize(memoryCapacity).expireStoreSize(memoryCapacity).expireOverflow(onDisk2).expireExecutor(Executors.newScheduledThreadPool(2)).create();
				//map = db.hashMap("inMemory",Serializer.LONG, Serializer.FLOAT).create();
				//map2 = db2.hashMap("inMemory2",Serializer.LONG, Serializer.FLOAT).create();
				
				
	}
	
	public HTreeMap makeMap(String name) {
		int numberOfMaps = (int) (model.getPhaseNo()*8 + this.getCuesMap().size()*totalMax*5);
		long memoryCapacity = (long) Math.round((float)memory/(float)numberOfMaps);
		
		HTreeMap tempDisk = dbDisk.hashMap("onDisk"+name,Serializer.STRING, Serializer.JAVA).create();
		disks.put(name, tempDisk);
		HTreeMap tempMap = db.hashMap("inMemory"+name,Serializer.STRING, Serializer.JAVA).expireMaxSize(memoryCapacity).expireOverflow(tempDisk).expireStoreSize(memoryCapacity).expireExecutor(Executors.newScheduledThreadPool(2)).create();
		//HTreeMap tempMap = db.hashMap(name,Serializer.STRING, Serializer.JAVA).create();
		cache.put(name, new TreeMap());
		maps.put(name,tempMap);
		return tempMap;
	}
	
	
	public void compactDB() {if (db != null) {db.commit(); db2.commit();}
	
	}
	
	public void closeDBs() {
		try {
			for (HTreeMap map : maps.values()) {
				//map.clear();
				map.close();
			}
		if (db != null && !db.isClosed()) {
			if (map != null && !map.isClosed() && !map.isEmpty()) {
				//map.clear();
				map.close();
				}
			if (onDisk != null && !onDisk.isClosed() && !onDisk.isEmpty()) {
				onDisk.close();
				}
			db.close();
			dbDisk.close();
			}
		if (db2 != null && !db2.isClosed()) {
			if (map2 != null && !map2.isClosed() && !map2.isEmpty()) {//map2.clear();
			map2.close();}
			if (onDisk2 != null && !onDisk2.isClosed() && !onDisk2.isEmpty()) {onDisk2.close(); }
			db2.close();
			dbDisk2.close();
			}
		
		for (HTreeMap disk : disks.values()) {
			//disk.clear();
			disk.close();
		}
		cache.clear();
		cache1.clear();
		cache2.clear();
		f1.delete();
		f2.delete();
		}
		catch (Exception e) {System.exit(0);}
	}
	
	public boolean dbContains(long key, boolean db) {
		if (db) {return map.containsKey(key);}
		else {return map2.containsKey(key);}
	}
	
	public void pushCache() {
		int i = 0;
		for (Long l : cache1.keySet()) {
			//map.put(l, cache1.get(l));
			i += map.size();
		}
		for (Long l : cache2.keySet()) {
			//map2.put(l, cache2.get(l));
			i += map2.size();
		}
		for (String s : maps.keySet()) {
			for (String s2 : cache.get(s).keySet()) {
				if (cache.containsKey(s2) && cache.get(s2) != null) {
					//maps.get(s).put(s2, cache.get(s2));

					i += maps.get(s).size();
				}
			}
		}
		//System.out.println(i);
	}
	public int phaseToTrial(int phase, String message) {
		int trials = 0;
		for (int i = 0; i < phase+1; i++) {
			trials += Math.max(1, getPhases().get(i).getNoTrials());
		}
		return phase == -1 ? -1 : trials-1;
		
		
		
	}
	
	public int trialToPhase(int trial, String message) {
		int phase = 0;
		for (int i = 0; i < getPhases().size();i++) {
			if (phaseToTrial(i,"") >= trial) {return phase;}
			else phase++;
		}
		return phase;
			
		
		
	}
	public void addToDB(long key, float entry) {
		testMemory();
		/*cache1.put(key, entry);
		if (cache1.size() > 100) {
			for (Long l : cache1.keySet()) {
				map.put(l, cache1.get(l));
			}
			cache1.clear();
		}*/
		map.put(key, entry);
	}
	public void addToMap(String key, Object entry, String map, boolean addNow) {
		testMemory();
		if (false) {
		cache.get(map).put(key, entry);
		if (cache.get(map).size() > 100) {
			for (String s : cache.get(map).keySet()) {
				
				maps.get(map);
				maps.get(map).put(s, cache.get(s));
			}
			//maps.get(map).putAll((Map)cache.get(maps));
			cache.get(map).clear();
		}}
		else {
			maps.get(map);
			if (map == null) System.out.println("is null map in simgroup" + " " + map + " " + " " + key);
			if (maps == null) System.out.println("maps is null map in simgroup" + " " + map + " " + " " + key);
			if (key == null) System.out.println("key is null map in simgroup" + " " + map + " " + " " + key);
			if (entry == null) System.out.println("entry is null map in simgroup" + " " + map + " " + " " + key);
			maps.get(map).put(key, entry);}
		//
	}
	public void addToDB2(long key, float entry) {
		testMemory();
		/*cache2.put(key, entry);
		if (cache2.size() > 100) {
			for (Long l : cache2.keySet()) {
				map2.put(l, cache2.get(l));
			}
			cache2.clear();
		}*/
		map2.put(key, entry);
	}
	public void testMemory() {
		if (runtime.getRuntime().freeMemory()/1024*1024 < 500) {
			map.clearWithExpire();
			map2.clearWithExpire();
			for (HTreeMap tmap : maps.values()) {
				tmap.clearWithExpire();
			}
		}
	}
	
	public Set<String> getKeySet(String map) {
		testMemory();
		maps.get(map);
		set1 = maps.get(map).getKeys();
		//Set<String> set2 = disks.get(map).getKeys();

		//System.out.println(set1.size() + " " + set2.size());
		//for (String s : set1) {System.out.println("memmap " + map + " key " + s);}
		//for (String s : set2) {System.out.println("diskmap " + map + " key " + s);}
		//if (set1 != null && set1.size() > 0 && set2 != null && set2.size() > 0) {
		//	try {set1.addAll(set2);
		//	} catch(UnsupportedOperationException e) {System.out.println(e.getMessage() + " " + e.getCause());}
		//}
		return set1;
	}
	
	public Object getFromDB(String key,String map) {
		testMemory();
		maps.get(map).get(key);
		return maps.get(map).get(key);
		/*
		//if (map != null && onDisk != null) {System.out.println("maps access : map name: " + map + " " + maps.get(map).containsKey(key) +  "  " + disks.get(map).containsKey(key));}
		try{if (maps.get(map) != null && !maps.get(map).containsKey(key)) {
			return null;
			/*if (disks.get(map) != null && disks.get(map).containsKey(key)) {
				try {return disks.get(map).get(key);}
				catch(org.mapdb.DBException e) {System.out.println(e.getMessage() + " " + e.getCause()); return null;}
			} else {
			return null;
			}
		}
		else {
			try {return maps.get(map).get(key);}
			catch(org.mapdb.DBException e) {System.out.println(e.getMessage() + " " + e.getCause()); return null;}
		}
		}
		catch(AssertionError e) { System.out.println(e.getMessage() +" " + e.getCause());return null;}
		catch(org.mapdb.DBException e) {System.out.println(e.getMessage() + " " + e.getCause()); return null;}*/
	}
	
	public float getFromDB(long key) {
		testMemory();
		map.get(key);
		if (map.containsKey(key)) return map.get(key);
		else {return 0;}
		//if (map != null && onDisk != null) {System.out.println("normal map access : " + map.containsKey(key) +  "  " + onDisk.containsKey(key));}
		/*try{if (map != null && !map.containsKey(key)) {
			return -1;
		}
		else {
			return map.get(key);}}
		catch(AssertionError e) { 
			System.out.println(e);
			return -1f;}*/
	}
	
	public void resetAll() {
		trialStrings = new ArrayList();
		appearanceList = new TreeMap();
		compoundAppearanceList = new TreeMap();
		for (Stimulus s: cues.values()) {s.resetCompletely();}
		for (SimPhase sp : phases) {sp.reset();}
	}
	
	
	public float getFromDB2(long key) {
		testMemory();
		map2.get(key);
		if (map2.containsKey(key)) return map2.get(key);
		else {return 0;}
		//if (map != null && onDisk != null) {System.out.println("normal map2 access : " + map2.containsKey(key) +  "  " + onDisk2.containsKey(key));}
		/*try {if (!map2.containsKey(key)) {
			//if (onDisk2.containsKey(key)) { return onDisk2.get(key);}
			//else return -1;
			return -1f;
		}
		else return map2.get(key);}
		catch(AssertionError e) { return -1f;}*/
	}

	public void setRule(int rule) {
		this.rule = rule;
		
	}
	
	public void initializeTrialArrays() {
		for (Stimulus s : cues.values()) {
			s.initializeTrialArrays();
		}
	}
	
	public int getNoTrialTypes() {return trialStrings.size();}
	
	public ArrayList<String> getTrialStrings() {return trialStrings;}
	
	public int getFirstOccurrence(Stimulus s) {
		if (s == null) {return -1;}
		if (s != null && appearanceList.containsKey(s.getName())) return appearanceList.get(s.getName());
		else {
		int phase = -1;
		for (SimPhase sp : phases) {
			boolean commonCondition = s.isCommon() ? (s.aStim != null && sp.containsStimulus(s.aStim)) || (s.bStim != null && sp.containsStimulus(s.bStim)) : false;

			if (phase == -1 && (sp.containsStimulus(s) || commonCondition  || sp.getContextConfig().getSymbol().equals(s.getSymbol()))) {

				phase = sp.getPhaseNum();

				appearanceList.put(s.getName(), phase-1);

			}
		}
		
		return phase-1;}
	}
	
	public void addTrialString(String s) {
		String[] strings = s.split("/");
		for (String s2 : strings) {
			String filtered = s2.replaceAll("\\d","");
			String filtered2 = USNames.hasUSSymbol(filtered) ? filtered.substring(0, filtered.length()-1) : filtered;
			
			if (!trialStrings2.contains(filtered2)) {
				trialStrings2.add(filtered2);
			}
			if (!trialStrings.contains(filtered)) {
				trialStrings.add(filtered);
			}
		}
	}
	
	public void setMaximumMemory(long memory) {
		this.memory = memory;
	}
	
	public int getTrialTypeIndex(String s) {return trialStrings.indexOf(s);}
	public int getTrialTypeIndex2(String s) {return trialStrings2.indexOf(s);}
	
	public ArrayList<Integer> getCompoundIndexes(String compound) {
		ArrayList<Integer> compoundIndexes = new ArrayList<Integer>();
		for (String s : trialStrings) {
			if (s.contains(compound) && (s.length() == compound.length() || (Math.abs(s.length()-compound.length()) == 1 && USNames.hasUSSymbol(s)))) {compoundIndexes.add(getTrialTypeIndex(s)+1);}
		}
		return compoundIndexes;
		
	}
	
	public int getRule() {return rule;}
}