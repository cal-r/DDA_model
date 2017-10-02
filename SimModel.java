/**
 * SimModel.java
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import simulator.configurables.ContextConfig;
import simulator.util.Response;
import simulator.util.USNames;
import extra166y.Ops;
import extra166y.ParallelArray;

/**
 * SimModel is the main object model of the inputed data. It holds the users
 * values about the number of groups, phases and combinations. Updates the
 * groups with new values from the value table and concentrates on the right
 * creation of the final cue list. Every time that a new experiment starts, a
 * new experiment starts the old model object is disposed and anew one is
 * created, all other objects that link with this model are getting refreshed as
 * well. A disposal of the model means that all data stored is getting lost but
 * this is normal as a new experiment starts.
 */
public class SimModel implements Runnable {

	private int groupsNo, phasesNo, combinationNo,
			variableDistributionCombinationNo;
	private Map<String, SimGroup> groups;
	private ArrayList<TreeMap<String,Integer>> groupSessions;
	private Map<String, Float> values;
	// Alberto Fernández August-2011
	// Mapping for configural cues. <K,V> K=virtual name (lower case letter),
	// V=compound
	private Map<String, String> configCuesNames; // e.g. <a,AB>
	// Random seed for variable distributions.
	private long randomSeed = System.currentTimeMillis();
	
	private File rootDirectory;
	// SimGroup parallel pool
	private ParallelArray<SimGroup> groupPool;
	/** Timestep size. **/
	private float timestepSize;

	// Alberto Fernandez Nov-2011
	List<String> listAllCues; // This is a sorted list of all existing cues
	/** Response threshold for simulated response graph. **/
	private float threshold;
	/** Whether to simulate contextual stimulus. **/
	private boolean useContext = true;

	/** Procedure to run a group **/
	final Ops.Op<SimGroup, SimGroup> update = new Ops.Op<SimGroup, SimGroup>() {
		@Override
		public SimGroup op(final SimGroup current) {
			current.run();
			addCueNames(current.getCuesMap());
			return current;
		}
	};
	/** Boolean indicating whether simulated response stats & figures are shown. **/
	private boolean showResponse;
	private boolean isContextAcrossPhase;
	private boolean isCSC;
	private boolean isGeo;
	private boolean isExponential;
	private boolean esther;
	private float contextAlphaR;
	/** Decision rule type. **/
	private Response decisionRule;
	private int randomPhases;
	private ModelControl control;
	/** Decay for decision rule. **/
	private float decay;
	/** Timing per trial type. **/
	private boolean timingPerTrial;
	private boolean isExternalSave = true;
	/** Are we using serial configurals? **/
	private boolean serialConfigurals;
	private boolean isZeroTrace;
	private boolean serialCompounds;
	/** Restrict predictions to >= 0 **/
	private boolean restrictPredictions;
	/** Decay for fuzzy activation. **/
	private float activationDecay;
	/** Dropoff for fuzzy activation. **/
	private float activationDropoff;
	
	private float skew = 20f;
	private float resetValue = 0.95f;
	private float usCV = 20;
	
    private boolean isCompounds = true;
	private int persistence = 0;
	private int setSize = 10;
	private float csScalar = 2.5f;
	private float csCV = 20;
	private int rule = 2;
	private float discount = 0.95f;
	
	private float bufferSize = 1f;
	private float associativeDiscount = 0.95f;
	private TreeMap<String,ArrayList<Float>> intensities;
	private int maxPhase = 1;
	
	public float getAssociativeDiscount() {return associativeDiscount;}
	public void setAssociativeDiscount(float d) {associativeDiscount = d;}
	public float getBufferSize() {return bufferSize;}
	
    public int getResponsesPerMinute() {
        return responsesPerMinute;
    }

    public void setResponsesPerMinute(int responsesPerMinute) {
        this.responsesPerMinute = responsesPerMinute;
    }
    
    public float getSkew(boolean gui) {
    	return skew;
    }
    public float getResetValue(boolean gui) {return resetValue;}
    public void setResetValue(float s) {resetValue = s;}
    
    public void setSkew(float s) {skew = s;}
    
    public float getUSCV() {return usCV;}
    public void setUSCV(float cv) {usCV = cv;}
    
    public float getDiscount() {return discount;}
    public void setDiscount(float d) {discount = d;}
    private int responsesPerMinute;
	private boolean resetContext = true;
    
    public void setResetContext(boolean r) {resetContext = r;}

    public float getSerialResponseWeight() {
        return serialResponseWeight;
    }

    public void setSerialResponseWeight(float serialResponseWeight) {
        this.serialResponseWeight = serialResponseWeight;
    }

    private float serialResponseWeight;

    public boolean isConfiguralCompounds() {
        return isConfiguralCompounds;
    }

    public void setConfiguralCompounds(boolean configuralCompounds) {
        isConfiguralCompounds = configuralCompounds;
    }
    
    public void setIsCompound(boolean b) {isCompounds = b;}

    private boolean isConfiguralCompounds;
	private float contextAlphaN;
	private float contextSalience;
	private boolean isErrors;
	private boolean isErrors2;
    


	/**
	 * SimModel's Constructor method.
	 */
	public SimModel() {
		values = new TreeMap<String, Float>();
		groups = new LinkedHashMap<String, SimGroup>();
		groupSessions = new ArrayList<TreeMap<String,Integer>>();
		// Initial values of groups, phases and combinations.
		groupsNo = phasesNo = 1;
		combinationNo = 4; // 20; modified Alberto Fernández July-2011
		// Alberto Fernández August-2011
		configCuesNames = new TreeMap<String, String>();
		// Alberto Fernandez Nov-2011
		useContext = true;
		listAllCues = new ArrayList<String>();
		// if(useContext) {listAllCues.add(Simulator.OMEGA+"");}
		threshold = 0.875f;
		decay = 0.5f;
		showResponse = true;
		isContextAcrossPhase = false;
		isCSC = true;
		timestepSize = 1;
		isGeo = false;
		isExponential = true;
		decisionRule = Response.CHURCH_KIRKPATRICK;
		randomPhases = 0;
		variableDistributionCombinationNo = 4;
		setTimingPerTrial(false);
		serialConfigurals = true;
		restrictPredictions = true;
		isCompounds = false;
		activationDecay = 0.15f;
		activationDropoff = 0.2f;
        isConfiguralCompounds = false;
        serialResponseWeight = 0.85f;
        responsesPerMinute = 100;
        if (intensities == null) intensities = new TreeMap();
	}

	/**
	 * Adds the new cues from every phases into the SortedMap. The new cues will
	 * be sorted accordingly depending on the symbol character. Their initial
	 * Float value is null because it is only on the 1st stage of the
	 * experiment.
	 * 
	 * @param map
	 *            the cues HashMaps deriving from every new group.
	 */
	public void addCueNames(Map<String, Stimulus> map) {
		Iterator<Entry<String, Stimulus>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			//System.out.println("loop simmodel");
			Entry<String, Stimulus> pair = it.next();
			values.put(pair.getValue().getSymbol(), 0f);

			// Alberto Fernandez Nov-2011
			if (!listAllCues.contains(pair.getValue().getSymbol())) {
				listAllCues.add(pair.getValue().getSymbol());
			}
		}
	}

	/**
	 * Adds a new group into the experiment. Adds the group into the groups
	 * HashMap and adds the groups new cues in the cue list if it doesn't
	 * already exist. The cue list is variable for the value table that is
	 * created for the user to input the 'alpha', 'beta' and 'lambda' values.
	 * 
	 * @param name
	 *            the name of the group. By default the name is 'Group #', where
	 *            # an ascending integer starting from 1.
	 * @param group
	 *            the SimGroup object which contains the phases and all other
	 *            necessary variables to accomplish an experiment.
	 */
	public void addGroupIntoMap(String name, SimGroup group) {
		groups.put(name, group);
		addCueNames(group.getCuesMap());
		randomPhases += group.numRandom();
	}
	
	public void addPhaseToSessions() {groupSessions.add(new TreeMap<String,Integer>());}
	public void addSessionToGroup(int phase,String groupName, Integer session) {
		groupSessions.get(phase).put(groupName, session);
	}
	
	public int getGroupSession(String groupName, int phase) {return 1;}

	/**
	 * Add the 'lambda' values for each phase in the values of the model
	 */
	public void addValuesIntoMap() {
		// The initial values of the 'lambda' are null a value indicating that
		// they
		// haven't been assigned with any float values yet.
		for (int p = 1; p <= phasesNo; p++) {

			boolean atLeastOneGroupPlus = false;
			boolean atLeastOneGroupMinus = false;

			Iterator<Entry<String, SimGroup>> iterGroup = groups.entrySet()
					.iterator();
			while (iterGroup.hasNext()) {

				//System.out.println("loop simmodel");
				Entry<String, SimGroup> pairGroup = iterGroup.next();
				SimGroup group = pairGroup.getValue();
				if (group.getPhases().get(p - 1).getLambdaPlus() != null)
					atLeastOneGroupPlus = true;
				if (group.getPhases().get(p - 1).getLambdaMinus() != null)
					atLeastOneGroupMinus = true;
				if (atLeastOneGroupPlus && atLeastOneGroupMinus)
					break;
			}
			if (atLeastOneGroupPlus) {
				values.put("lambda p" + p, null);
				values.put("alpha+ p" + p, null);
			}
			if (atLeastOneGroupMinus) {
				values.put("lambda p" + p, null);
				values.put("alpha- p" + p, null);
			}
		}
	}
	public void setIsErrors(boolean b, boolean b2) {isErrors = b; isErrors2 = b2;}
	public boolean isErrors() {return isErrors;}

	public boolean isErrors2() {return isErrors2;}
	public void clearConfiguralMap() {
		configCuesNames.clear();
	}

	public boolean contextAcrossPhase() {
		return isContextAcrossPhase;
	}

	// Alberto Fern·ndez August-2011
	/**
	 * Returns the external name of a configural cue or configural compound e.g.
	 * configural cues: a --> c(AB), configural compounds: ABb --> [AB]
	 */
	public String cueName2InterfaceName(String cueName) {
		String interfaceName;
		boolean configural = false;
		String configurals = "";
		// Scan for a lowercase character
		for (int i = 0; i < cueName.length(); i++) {
			if (cueName.charAt(i) > 96 && cueName.charAt(i) < 900) {
				configural = true;
				configurals += "" + cueName.charAt(i);
			}
		}

		if (configural && !cueName.equals(Simulator.OMEGA + "")) {
			String compoundName;
			if (cueName.length() == 1) {
				// configural cue
				// retrieve compound name
				compoundName = configCuesNames.get(cueName);
				// interfaceName = "¢(" + compoundName + ")";
				interfaceName = "c(" + compoundName + ")";
			} else if(cueName.contains("'")) {
                compoundName = cueName.replaceAll(configurals, "");
                // interfaceName = "[" + compoundName + "¢]";
                interfaceName = compoundName;
            } else {
				// configural compound - remove configural names

				compoundName = cueName.replaceAll(configurals, "");
				// interfaceName = "[" + compoundName + "¢]";
				interfaceName = "[" + compoundName + "]";
			}
		} else {
			interfaceName = cueName;
		}
		return interfaceName;
	}

	/**
	 * @return the activationDecay
	 */
	public float getActivationDecay() {
		return activationDecay;
	}

	/**
	 * @return the activationDropoff
	 */
	public float getActivationDropoff() {
		return activationDropoff;
	}

	/**
	 * Returns the number of alpha cues in the model
	 * 
	 * @return number of alpha cues in the model
	 */
	public TreeMap<String, Float> getAlphaCues() {
		TreeMap<String, Float> tm = new TreeMap<String, Float>();
		Iterator<String> it = values.keySet().iterator();
		while (it.hasNext()) {

			String pair = it.next();
			if (pair.indexOf("lambda") == -1 && pair.indexOf("alpha") == -1)
				tm.put(pair, values.get(pair));
		}
		return tm;
	}
	
	/*public TreeMap<String, Float> getVariableAlphaCues() {
		TreeMap<String, Float> tm = new TreeMap<String, Float>();
		Iterator<String> it = values.keySet().iterator();
		while (it.hasNext()) {
			String pair = it.next();
			if (pair.indexOf("lambda") == -1 && pair.indexOf("alpha") == -1)
				tm.put(pair, values.get(pair));
		}
		return tm;
	}*/

	/**
	 * Returns the number of different combinations that need to be processed on
	 * the sequence stimuli order in order to have a random stimuli execution.
	 * The number is set to a default of 20 but can rise into a 3 digit number
	 * as well. Although bigger the number is, longer the time to process.
	 * 
	 * @return the number of combinations for the random stimuli sequence.
	 */
	public int getCombinationNo() {
		return combinationNo;
	}

	public Map<String, String> getConfigCuesNames() {
		return configCuesNames;
	}

	/**
	 * 
	 * @return the default context alpha this model uses.
	 */

	public float getContextAlpha() {
		return contextAlphaR;
	}

	/**
	 * @param contexts
	 */
	public Map<String, ContextConfig> getContexts() {
		Map<String, ContextConfig> contexts = new HashMap<String, ContextConfig>();
		for (SimGroup group : groups.values()) {
			contexts.putAll(group.getContexts());
		}
		return contexts;
	}

	/**
	 * Returns the keySet of the cues HashMap. In other words it returns the
	 * symbol that each cue from the experiment has. This is been used on the
	 * acomplise of the value table.
	 * 
	 * @return the keySet of the cues HashMap.
	 */
	public Set<String> getCueNames() {
		return values.keySet();
	}

	/**
	 * @return the decay
	 */
	public float getDecay() {
		return decay;
	}

	/**
	 * @return the decisionRule
	 */
	public Response getDecisionRule() {
		return decisionRule;
	}
	
	public File getDirectory() {return rootDirectory;}

	public int getGroupNo() {
		return groupsNo;
	}

	/**
	 * Returns the HashMap of the groups. The mapping is described from a key
	 * value which is the the name of the groups and the actual values are
	 * SimGroup objects.
	 * 
	 * @return the group objects in a HashMap.
	 */
	public Map<String, SimGroup> getGroups() {
		return groups;
	}

	private String getKey(Map<String, String> map, String value) {
		Set<String> keys = map.keySet();
		boolean found = false;
		String key = null;
		Iterator<String> it = keys.iterator();
		while (!found && it.hasNext()) {
			key = it.next();
			//System.out.println("loop simmodel");
			if (map.get(key).equals(value)) {
				found = true;
			}
		}
		return key;
	}

	// Alberto Fernandez Nov-2011
	public List<String> getListAllCues() {
		return listAllCues;
	}

	/**
	 * Returns the number of alpha cues in the model
	 * 
	 * @return number of alpha cues in the model
	 */
	public int getNumberAlphaCues() {
		int cont = 0;
		Iterator<String> it = values.keySet().iterator();
		while (it.hasNext()) {

			//System.out.println("loop simmodel");
			String pair = it.next();
			if (pair.indexOf("lambda") != -1 || pair.indexOf("alpha") != -1)
				cont++;
		}
		return (values.size() - cont);
	}

	/**
	 * Returns the number of phases that every group has. The phases are the
	 * same for every group as it is unwise to run an experiment with different
	 * number of phases.
	 * 
	 * @return the number of phases that every group has.
	 */
	public int getPhaseNo() {
		return phasesNo;
	}

	/**
	 * @return the randomSeed
	 */
	public long getRandomSeed() {
		return randomSeed;
	}

	/**
	 * Get the response threshold for this simulation.
	 * 
	 * @return
	 */

	public float getThreshold() {
		return threshold;
	}

	/**
	 * @return the timestepSize
	 */
	public float getTimestepSize() {
		return timestepSize;
	}


	// Added by Alberto Fernández July-2011
	/**
	 * Returns the values of parameters in the model
	 * 
	 * @return values of parameters in the model
	 */
	public Map<String, Float> getValues() {
		return values;
	}

	/**
	 * @return the variableDistributionCombinationNo
	 */
	public int getVariableCombinationNo() {
		return variableDistributionCombinationNo;
	}

	/**
	 * Returns the internal name of a configural cue or configural compound e.g.
	 * configural cues: c(AB) --> a, configural compounds: [AB] --> ABb
	 */
	public String interfaceName2cueName(String interfaceName) {
		String cueName;
		if (interfaceName.contains("(")) { // is configural cue, e.g. c(AB)
			cueName = getKey(configCuesNames,
					interfaceName.substring(2, interfaceName.length() - 1));
		} else if (interfaceName.contains("[")) { // configural compound
			cueName = interfaceName.substring(1, interfaceName.length() - 2)
					+ getKey(configCuesNames, interfaceName.substring(1,
							interfaceName.length() - 1));
		} else {
			cueName = interfaceName;
		}
		return cueName;
	}

	/**
	 * 
	 * @return true if this is a CSC simulation.
	 */
	public boolean isCSC() {
		return isCSC;
	}

	/**
	 * 
	 * @return true if this model uses the exponential distribution.
	 */

	public boolean isExponential() {
		return isExponential;
	}

	/**
	 * @return true if this model uses the geometric mean for variable
	 *         distributions.
	 */
	public boolean isGeometricMean() {
		return isGeo;
	}

	/**
	 * @return the restrictPredictions
	 */
	public boolean isRestrictPredictions() {
		return restrictPredictions;
	}

	/**
	 * @return true if serial compounds are in use.
	 */
	public boolean isSerialCompounds() {
		return serialCompounds;
	}

	/**
	 * @return true if serial configurals are in use.
	 */
	public boolean isSerialConfigurals() {
		return serialConfigurals;
	}

	/**
	 * @return the timingPerTrial
	 */
	public boolean isTimingPerTrial() {
		return timingPerTrial;
	}
	
	public boolean isExternalSave() {
		return isExternalSave;
	}

	/**
	 * @return the a boolean indicated that the context should be used
	 */
	public boolean isUseContext() {
		return useContext;
	}

	/**
	 * @return true if traces should be set to 0 between trials
	 */
	public boolean isZeroTraces() {
		return isZeroTrace;
	}

	/**
	 * Initializes the values and groups of the SimModel
	 */
	public void reinitialize() {
		values = new TreeMap();
		groups = new LinkedHashMap<String, SimGroup>();
		// Alberto Fernández August-2011
		configCuesNames = new TreeMap<String, String>();
		// Alberto Fernandez Nov-2011
		// listAllCues = new ArrayList<String>();
	}

	@Override
	public void run() {
		startCalculations();
	}

	/**
	 * @param activationDecay
	 *            the activationDecay to set
	 */
	public void setActivationDecay(float activationDecay) {
		this.activationDecay = activationDecay;
	}

	/**
	 * @param activationDropoff
	 *            the activationDropoff to set
	 */
	public void setActivationDropoff(float activationDropoff) {
		this.activationDropoff = activationDropoff;
	}

	/**
	 * Sets an new number of random combinations that take on the experiment
	 * when the user chooses random on the groups.
	 * 
	 * @param r
	 *            a number indicating the combinations. This is being used on a
	 *            iterating function that produces random stimuli position
	 *            following the given sequence.
	 */
	public void setCombinationNo(int r) {
		combinationNo = r;
	}

	public void setContextAcrossPhase(boolean on) {
		isContextAcrossPhase = on;
	}
	public boolean getContextAcrossPhase() {return isContextAcrossPhase;}
	/**
	 * 
	 * @param alpha
	 *            the default context alpha this model uses.
	 */

	public void setContextAlphaR(final float alpha) {
		contextAlphaR = alpha;
	}
	public void setContextAlphaN(final float alpha) {
		contextAlphaN = alpha;
	}
	public void setContextSalience(final float alpha) {
		contextSalience = alpha;
	}

	/**
	 * @param control
	 */
	public void setControl(ModelControl control) {
		this.control = control;
		for (SimGroup group : groups.values()) {
			group.setControl(control);
		}
	}

	/**
	 * 
	 * @param on
	 *            set to true if using CSC cues
	 */

	public void setCSC(boolean on) {
		isCSC = on;
	}

	/**
	 * @param decay
	 *            the decay to set
	 */
	public void setDecay(float decay) {
		this.decay = decay;
	}

	/**
	 * @param decisionRule
	 *            the decisionRule to set
	 */
	public void setDecisionRule(Response decisionRule) {
		this.decisionRule = decisionRule;
	}

	/**
	 * Sets an new number of groups that taking place on the experiment.
	 * 
	 * @param g
	 *            a number indicating the new number of groups.
	 */
	public void setGroupNo(int g) {
		groupsNo = g;
	}

	/**
	 * 
	 * @param exp
	 *            set to true if this model uses the exponential distribution.
	 */

	public void setIsExponential(boolean exp) {
		isExponential = exp;
	}

	/**
	 * 
	 * @param on
	 *            set to true if this model uses the geometric mean.
	 */

	public void setIsGeometricMean(boolean on) {
		isGeo = on;
	}

	/**
	 * Sets an new number of phases that taking place on the experiment.
	 * 
	 * @param p
	 *            a number indicating the new number of phases.
	 */
	public void setPhaseNo(int p) {
		phasesNo = p;
	}

	/**
	 * @param randomSeed
	 *            the randomSeed to set
	 */
	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}

	/**
	 * @param restrictPredictions
	 *            the restrictPredictions to set
	 */
	public void setRestrictPredictions(boolean restrictPredictions) {
		this.restrictPredictions = restrictPredictions;
	}

	/**
	 * 
	 * @param on
	 *            set to true if serial compounds are in use.
	 */

	public void setSerialCompounds(boolean on) {
		serialCompounds = on;
	}

	public void setSerialConfigurals(boolean on) {
		serialConfigurals = on;
	}
	
	public boolean isCompound() {
		return isCompounds;
	}

	/**
	 * 
	 * @param on
	 *            true if the simulated response should be shown.
	 */

	public void setShowResponse(boolean on) {
		showResponse = on;
	}

	/**
	 * 
	 * @param n
	 *            the threshold for simulated response.
	 */

	public void setThreshold(float n) {
		threshold = n;
	}

	/**
	 * @param timestepSize
	 *            the timestepSize to set
	 */
	public void setTimestepSize(float timestepSize) {
		this.timestepSize = timestepSize;
	}

	/**
	 * @param timingPerTrial
	 *            the timingPerTrial to set
	 */
	public void setTimingPerTrial(boolean timingPerTrial) {
		this.timingPerTrial = timingPerTrial;
	}
	
	public void setExternalSave(boolean isExternalSave) {
		this.isExternalSave = isExternalSave;
	}


	/**
	 * @param useContext
	 *            whether context is used
	 */
	public void setUseContext(boolean on) {
		this.useContext = on;
		if (useContext) {
			if (!listAllCues.contains(Simulator.OMEGA + "")) {
				listAllCues.add(Simulator.OMEGA + "");
			}
		} else {
			listAllCues.remove(Simulator.OMEGA + "");
		}
	}

	/**
	 * @param variableDistributionCombinationNo
	 *            the variableDistributionCombinationNo to set
	 */
	public void setVariableCombinationNo(int variableDistributionCombinationNo) {
		this.variableDistributionCombinationNo = variableDistributionCombinationNo;
	}

	/**
	 * 
	 * @param on
	 *            Set to true to force traces to 0 at the end of each trial.
	 */

	public void setZeroTraces(boolean on) {
		isZeroTrace = on;
	}
	
	
	public void setEsther(boolean b) {esther = b;}

	public void setDirectory(File f) {rootDirectory = f;}

	public boolean showResponse() {
		return showResponse;
	}

	/**
	 * Starts the process of the calculation. It executes the run() method of
	 * every group which implements the Runnable for multi-thread calculations.
	 * This will speed up the processes because they are running in concurrent
	 * dimension.
	 */
	public void startCalculations() {
		/*
		 * Iterator<Entry<String, SimGroup>> iterGroup =
		 * groups.entrySet().iterator(); while (iterGroup.hasNext()) {
		 * Entry<String, SimGroup> pairGroup = iterGroup.next();
		 * pairGroup.getValue().run(); }
		 */

		// J Gray - 2012: This section wasn't actually running concurrent, but
		// now does
		listAllCues.clear();
		groupPool = ParallelArray.createEmpty(groupsNo, SimGroup.class,
				Simulator.fjPool);
		groupPool.asList().addAll(groups.values());
        try {
		    groupPool.withMapping(update).all();
		    control.incrementProgress(1);
        } catch (OutOfMemoryError e) {
            System.err.println("Ran outa memory. Sadness.");
        } //catch (ArrayIndexOutOfBoundsException e) {
            //System.err.println("Start_calculations in simmodel array OOB excp: " + e.getCause() + " " + e.getMessage());
       // }
        control.setComplete(true);
        groupPool = null;
	}

	

	public int totalNumPhases() {
		int total = 0;
		for (SimGroup group : groups.values()) {
			total += group.trialCount();
		}
		return total;
	}

	/**
	 * Updates the values from the SortedList to the values in the model
	 * Modified to add gamma and delta parameters.. J Gray
	 * 
	 * @param name
	 *            the key of the SortedList. In other words the name or symbol
	 *            of the cue or 'lambda' or 'beta'.
	 * @param phase
	 *            from which is going to update the value
	 * @param value
	 *            the value that is been given from the user.
	 */
	public void updateValues(String name, int phase, String value) {
		ArrayList<String> usNames = new ArrayList();
		ArrayList<String> alphaPlusNames = new ArrayList();
		ArrayList<String> betaNames = new ArrayList();
		ArrayList<String> omicronNames = new ArrayList();
		ArrayList<String> usLambdaNames = new ArrayList();
		for (String s  : listAllCues) {
			
			if (USNames.isUS(s) && !usNames.contains(s)) {
				usNames.add(s);
				alphaPlusNames.add(s + " - " + "alpha+");
				usLambdaNames.add(s + " - " + "lambda");
				betaNames.add(s + " - " + "\u03B2");
				omicronNames.add(s + "_s");
			}
		}
		if (value.equals("")) {
			// beta+ p1, lambda+ p1 y lambda- p1 are never empty after
			// checkValuesTable()
			boolean isAlphaPlus = false;
			for (String alphaPlus : alphaPlusNames) {
				if (name.indexOf(alphaPlus) != -1) {
					if (value == null || value == "") {
						values.put(name + " p" + phase, values.get(alphaPlus+" p1"));}
					else {
						values.put(name, new Float(value));
						values.put(name + " p" + phase, new Float(value));
					}
					isAlphaPlus = true;
				} else if (name.indexOf("alpha+") != -1) {
					if (value == null || value == "") {
					values.put(name + " p" + phase, values.get("alpha+"+" p1"));}
					else {
						values.put(name, new Float(value));
						values.put(name + " p" + phase, new Float(value));
					}
					isAlphaPlus = true;
				}
				//if (isAlphaPlus) System.out.println(isAlphaPlus + " " + name + " " + values.get("alpha+"+" p1"));
			}
			boolean isOmicron = false;
			for (String omicron : omicronNames) {
				if (name.indexOf(omicron) != -1) {
					if (value == null || value == "") {
					values.put(name + " p" + phase, values.get(omicron+" p1"));}
					else {
						values.put(name, new Float(value));
						values.put(name + " p" + phase, new Float(value));
					}
					isOmicron = true;
				}else if (name.indexOf("+_s") != -1) {
					if (value == null || value == "") {
					values.put(name + " p" + phase, values.get("+_s"+" p1"));}
					else {
						values.put(name, new Float(value));
						values.put(name + " p" + phase, new Float(value));
					}
					isOmicron = true;
				}
			}
			boolean isBeta = false;
			for (String beta : betaNames) {
				if (name.indexOf(beta) != -1) {
					if (value == null || value == "") {
					values.put(name + " p" + phase, values.get(beta+" p1"));}
					else {
						values.put(name, new Float(value));
						values.put(name + " p" + phase, new Float(value));
					}
					isBeta = true;
				}else if (name.indexOf("\u03B2") != -1) {
					if (value == null || value == "") {
					values.put(name + " p" + phase, values.get("\u03B2"+" p1"));}
					else {
						values.put(name, new Float(value));
						values.put(name + " p" + phase, new Float(value));
					}
					isBeta = true;
				}
			}
			boolean isLambda = false;
			for (String lambdaName : usLambdaNames) {
				if (name.indexOf(lambdaName) != -1) {
					if (value == null || value == "") {
					values.put(name + " p" + phase, values.get(lambdaName+" p1"));}
					else {
						values.put(name, new Float(value));
						values.put(name + " p" + phase, new Float(value));
					}
					isLambda = true;
				}else if (name.indexOf("lambda") != -1) {
					if (value == null || value == "") {
					values.put(name + " p" + phase, values.get("lambda"+" p1"));}
					else {
						values.put(name, new Float(value));
						values.put(name + " p" + phase, new Float(value));
					}
					isLambda = true;
				}
			}
			if (!(isLambda || isBeta || isOmicron || isAlphaPlus)) {
//			if (name.indexOf("beta+") != -1)
//				values.put(name + " p" + phase, values.get("beta+ p1"));
			if (name.indexOf("reinforcer cff") != -1)
				values.put(name + " p" + phase, values.get("reinforcer cff p1"));
			else if (name.indexOf("integration") != -1)
				values.put(name + " p" + phase, values.get("integration p1"));
			else if (name.indexOf("US \u03C1") != -1)
				values.put(name + " p" + phase, values.get("US \u03C1 p1"));
			else if (name.indexOf("Threshold") != -1)
				values.put(name + " p" + phase, values.get("Threshold p1"));
//			else if (name.indexOf("lambda") != -1)
//				values.put(name + " p" + phase, values.get("lambda p1"));
			else if (name.indexOf("gamma") != -1)
				values.put(name + " p" + phase, values.get("gamma p1"));
			else if (name.indexOf("Variable Salience") != -1)
				values.put(name + " p" + phase, values.get("Variable Salience p1"));
			else if (name.indexOf("skew") != -1)
				values.put(name + " p" + phase, values.get("skew p1"));
			else if (name.indexOf("\u03C6") != -1)
				values.put(name + " p" + phase, values.get("\u03C6 p1"));
			else if (name.indexOf("Wave Constant") != -1)
				values.put(name + " p" + phase, values.get("Wave Constant p1"));
			else if (name.indexOf("US Scalar Constant") != -1)
				values.put(name + " p" + phase, values.get("US Scalar Constant p1"));
			else if (name.indexOf("delta") != -1)
				values.put(name + " p" + phase, values.get("delta p1"));
			else if (name.indexOf("b") != -1)
				values.put(name + " p" + phase, values.get("b p1"));
			else if (name.indexOf("common") != -1)
				values.put(name + " p" + phase, values.get("common p1"));
			else if (name.indexOf("setsize") != -1)
				values.put(name + " p" + phase, values.get("setsize p1"));
			else if (name.indexOf("\u03C2") != -1)
				values.put(name + " p" + phase, values.get("\u03C2 p1"));
			else if (name.indexOf("CV") != -1)
				values.put(name + " p" + phase, values.get("CV p1"));
			else if (name.indexOf("linear c") != -1)
				values.put(name + " p" + phase, values.get("linear c p1"));
			else if (name.indexOf("\u03c4" + "1") != -1)
				values.put(name + " p" + phase, values.get("\u03c4" + "1" +" p1"));
			else if (name.indexOf("\u03c4" + "2") != -1)
				values.put(name + " p" + phase, values.get("\u03c4" + "2" +" p1"));
			else if (name.indexOf("Salience Weight") != -1)
				values.put(name + " p" + phase, values.get("Salience Weight" +" p1"));
			else if (name.indexOf("\u03d1") != -1)
				values.put(name + " p" + phase, values.get("\u03d1 p1"));
			else if (name.indexOf("CS \u03C1") != -1)
				values.put(name + " p" + phase, values.get("CS \u03C1 p1"));
			else if (name.indexOf("Self Discount") != -1)
				values.put(name + " p" + phase, values.get("Self Discount p1"));
			else if (name.indexOf("ω") != -1)
				values.put(name + " p" + phase, values.get("ω p1"));
			 else if (name.indexOf("linear c") != -1|| name.indexOf("reinforcer cff") != -1|| name.indexOf("gamma") != -1 
				|| name.indexOf("lambda") != -1 || name.indexOf("alpha+") != -1 || name.indexOf("common") != -1
				|| name.indexOf("\u03C2") != -1 || name.indexOf("\u03B2") != -1 || name.indexOf("\u039F") != -1 ||
				name.indexOf("\u03c2" + "2") != -1 || name.indexOf("setsize") != -1
				|| name.indexOf("CV") != -1 || name.indexOf("\u03c4" + "2") != -1 || name.indexOf("CS \u03C1") != -1
				|| name.indexOf("\u03c4" + "1") != -1 || name.indexOf("\u03d1") != -1 || name.indexOf("Salience Weight") != -1
				|| name.indexOf("Wave Constant") != -1 || name.indexOf("US Scalar Constant") != -1
				|| name.indexOf("integration") != -1 || name.indexOf("US \u03C1") != -1 || name.indexOf("Self Discount") != -1
				|| name.indexOf("skew") != -1 || name.indexOf("\u03C6") != -1 ||  name.indexOf("Variable Salience") != -1
				|| name.indexOf("delta") != -1 || name.indexOf("b") != -1 || name.indexOf("ω") != -1 || name.indexOf("Threshold") != -1)
			values.put(name + " p" + phase, new Float(value));}}
		else {
			// Alberto Fernández August-2011
			// here, the "virtual" name have to be considered:
			// values.put(name, new Float(value));
			if (false) {
				String compoundName = name.substring(2, name.length() - 1); // ex.
																			// c(AB)
																			// -->
																			// AB
				String virtualName = SimGroup.getKeyByValue(configCuesNames,
						compoundName);
				values.put(virtualName, new Float(value));
			} else {
				values.put(name, new Float(value));
				values.put(name + " p" + phase, new Float(value));
			}
		}
			
		
		
	}
	
	public void initializeIntensities() {
		
		for (String s : groups.keySet()) {
			maxPhase = Math.max(maxPhase, groups.get(s).getNoOfPhases());
			
		}
		for (String s : groups.keySet()) {
			if (!intensities.containsKey(s)) intensities.put(s, new ArrayList());
			for (int i = 1; i <= maxPhase; i++) {
				if (i <= groups.get(s).getNoOfPhases() && intensities.get(s).size() <= i) intensities.get(s).add(1f);
				
			}
		}
	}
	
	public void setIntensities(TreeMap<String,ArrayList<Float>> inte) {
		for (String s : groups.keySet()) {
			SimGroup tg = groups.get(s);
			for (int i = 1; i <= tg.getNoOfPhases(); i++) {
				float intensity = (inte != null && inte.containsKey(s)) && (inte.get(s).size() > i-1) ? inte.get(s).get(i-1) : 1f;
				tg.getPhases().get(i-1).setIntensity(intensity);
			}
			
		}
		
	}
	
	public TreeMap<String,ArrayList<Float>> getIntensities() {return intensities;}
	public int getMaxPhase() {return maxPhase;}
	

	/**
	 * Updates every group with the new values that derived from the value
	 * table. It iterates through every group and clears them from any values
	 * that they possible have and then continues with a new iteration through
	 * their cue HashMap and updates as necessary. The same with 'lambda' and
	 * 'beta' values.
	 */
	public void updateValuesOnGroups() {

		Iterator<Entry<String, SimGroup>> iterGroup = groups.entrySet()
				.iterator();
		while (iterGroup.hasNext()) {

			//System.out.println("loop simmodel");
			Entry<String, SimGroup> pairGroup = iterGroup.next();
			SimGroup tempGroup = pairGroup.getValue();
			tempGroup.clearResults();
			Iterator<Entry<String, Stimulus>> iterCue = tempGroup.getCuesMap()
					.entrySet().iterator();
			while (iterCue.hasNext()) {

				//System.out.println("loop simmodel");
				Entry<String, Stimulus> pairCue = iterCue.next();
				Stimulus tempCscCue = pairCue.getValue();
				tempCscCue.reset(false,0);
				float alphaRValue = (float) ((values.containsKey(tempCscCue.getName()+"_\u03B1r")) ? values.get(tempCscCue.getName() + "_\u03B1r") : -1);
				float alphaNValue = (float) ((values.containsKey(tempCscCue.getName()+"_\u03B1n")) ? values.get(tempCscCue.getName() + "_\u03B1n") : -1);
				float salience = (float) ((values.containsKey(tempCscCue.getName()+"_s")) ? values.get(tempCscCue.getName() + "_s") : -1);
				if (tempCscCue.getName().length() > 1) {
					char[] characters = tempCscCue.getName().toCharArray();
					
					for (char character : characters) {
						if (character == "c".toCharArray()[0]) {}
							else {
							alphaRValue += values.get(character +"");
							alphaNValue += values.get(character +"");
						}
					}
					alphaRValue /= tempCscCue.getName().length();
					alphaNValue /= tempCscCue.getName().length();
				}
				if (alphaRValue != -1 && alphaRValue != 0)tempCscCue.setRAlpha(alphaRValue);
				if (alphaNValue != -1 && alphaNValue != 0)tempCscCue.setNAlpha(alphaNValue);
				if (salience != -1 && salience != 0)tempCscCue.setSalience(salience);
				//System.out.println(tempCscCue.getName() + " " + salience + " " + alphaRValue + " " + alphaNValue);
			}
			// Update the values of the lambdas and betas per phase in the
			// current group
			ArrayList<String> usNames = new ArrayList();
			ArrayList<String> usAlphaNames = new ArrayList();
			ArrayList<String> usBetaNames = new ArrayList();
			ArrayList<String> usOmicronNames = new ArrayList();
			for (String s  : listAllCues) {
				if (USNames.isUS(s) && !usNames.contains(s)) {
					usNames.add(s);
					usAlphaNames.add(s + " - " + "\u03B1+");
					usBetaNames.add(s + " - " + "\u03B2");
					usOmicronNames.add(s + "_s");
				}
			}
			for (int p = 1; p <= phasesNo; p++) {
				tempGroup.setRule(rule);
				for (String alphaName : usAlphaNames) {
					if (values.containsKey(usAlphaNames.size() > 1 ? alphaName : "alpha+ p"+p)) {
					
						tempGroup.getCuesMap().get(alphaName.split(" - ")[0]).setNAlpha( values.get(usAlphaNames.size() > 1 ? alphaName : "alpha+ p"+p));
						//System.out.println("US: " + values.get(usAlphaNames.size() > 1 ? alphaName : "alpha+ p"+p));
					}
					
				}
				for (String omicronName : usOmicronNames) {
					if (values.containsKey(usOmicronNames.size() > 1 ? omicronName : "+_s")) {
						tempGroup.getCuesMap().get(omicronName.split("_")[0]).setOmicron(values.get(usOmicronNames.size() > 1 ? omicronName : "+_s"));
						
						//System.out.println("US beta: " + values.get(usBetaNames.size() > 1 ? betaName : "\u03B2"));
					}
					
				}
				for (String betaName : usBetaNames) {
					if (values.containsKey(usBetaNames.size() > 1 ? betaName : "\u03B2")) {
						tempGroup.getCuesMap().get(betaName.split(" - ")[0]).setBeta(values.get(usBetaNames.size() > 1 ? betaName : "\u03B2"));
						//System.out.println("US beta: " + values.get(usBetaNames.size() > 1 ? betaName : "\u03B2"));
					}
					
				}
//				if (values.containsKey("lambda p" + p)) {
//					tempGroup.getPhases().get(p - 1)
//							.setLambdaPlus(values.get("lambda p" + p));
//				}
				if (values.containsKey("gamma p" + p)) {
					tempGroup.getPhases().get(p - 1)
							.setGamma(values.get("gamma p" + p));
				}
				if (values.containsKey("common p" + p)) {
					tempGroup.setCommon(values.get("common p" + p));
				}
				if (values.containsKey("Wave Constant p" + p)) {
					tempGroup.getPhases().get(p - 1).setCSScalar(values.get("Wave Constant p" + p));
				}
				if (values.containsKey("US Scalar Constant p" + p)) {
					//tempGroup.getPhases().get(p - 1).setUSScalar(values.get("US Scalar Constant p" + p));
				}
				if (values.containsKey("b p" + p)) {
					for (Stimulus s : tempGroup.getCuesMap().values()) {
						s.setB(values.get("b p" + p));
					}
				}
//				if (values.containsKey("\u03c2 p" + p)) {
//					tempGroup.getPhases().get(p - 1)
//							.setVarsigma(values.get("\u03c2 p" + p));
//				}
				if (values.containsKey("\u03d1 p" + p)) {
					tempGroup.getPhases().get(p - 1)
							.setVartheta(values.get("\u03d1 p" + p));
				}
				if (values.containsKey("US \u03C1 p" + p)) {
					tempGroup.getPhases().get(p - 1)
							.setLeak(values.get("US \u03C1 p" + p),values.get("CS \u03C1 p" + p));
				}
//				if (values.containsKey("beta+ p" + p)) {
//					tempGroup.getPhases().get(p - 1)
//							.setBetaPlus(values.get("beta+ p" + p));
//				}
			
				if (values.containsKey("delta p" + p)) {
					tempGroup.getPhases().get(p - 1)
							.setDelta(values.get("delta p" + p));
				}
				if (values.containsKey("Self Discount p" + p)) {
					tempGroup.getPhases().get(p - 1)
							.setSelfPrediction(values.get("Self Discount p" + p));
				}
				if (values.containsKey("ω p" + p)) {
					tempGroup.getPhases().get(p - 1)
							.setContextSalience(values.get("ω p" + p));
				}
				tempGroup.getPhases().get(p - 1)
				.setResetContext(resetContext);
				
				tempGroup.getPhases().get(p - 1)
				.setSTD(getCSCV());
				tempGroup.getPhases().get(p - 1)
				.setUSSTD(getUSCV());
				tempGroup.getPhases().get(p - 1).setUSPersistence(getPersistence());
				//tempGroup.getPhases().get(p - 1).setCSPersistence(getCSSCalar());
				tempGroup.getPhases().get(p - 1)
				.setCSCLike(Math.max(0,getSkew(false)));
				tempGroup.getPhases().get(p - 1)
				.setContextReset(getResetValue(false));
				tempGroup.getPhases().get(p - 1)
				.setSubsetSize((int)Math.max(1, Math.round(getSetSize())));
			}
		}
	}
	public void setPersistence(int n) {
		persistence = n;
		
	}

	public int getSetSize() {
		return setSize;
	}

	public void setSetSize(int n) {
		setSize = n;
		
	}

	public float getCSSCalar() {
		return csScalar;
	}

	public void setCSSCalar(float n) {
		csScalar = n;
		
	}

	public float getPersistence() {
		return persistence;
	}

	public float getCSCV() {
		// TODO Auto-generated method stub
		return csCV;
	}

	public void setCSCV(float n) {
		csCV = n;
		
	}

	public void setLearningRule(int i) {
		rule = i;
		
	}

}