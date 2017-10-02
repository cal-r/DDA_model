/**
 * SimStimulus.java
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

import java.util.ArrayList;
import java.util.List;

/**
 * SimStimulus class represents a model for a stimulus (combined or not),
 * although if 2 same stimulus are placed in the experiment but in different
 * order, it will be counted on the same objects, the system will add the extra
 * trials.
 */
public class SimStimulus {

	private int trials;
	private String fullName, cueNames;
	private boolean reinforced;
	private List<CS> parts;
    private List<String> partsString;

	/**
	 * Stimulus' Constructor method
	 * 
	 * @param name
	 *            is the full name of the Stimulus containing the number of
	 *            trials, the actual set of cues and the reinforced sign (e.g.
	 *            "6AB+").
	 * @param tr
	 *            is the initial number of times that the stimulus is
	 *            introduced.
	 * @param cnames
	 *            only the set of cues.
	 * @param reinf
	 *            confirms if the stimulus is reinforced.
	 * @param fixed
	 *            indicates whether this stimulus is fixed.
	 */
	public SimStimulus(String name, int tr, String cnames, boolean reinf) {

		fullName = name;
		trials = tr;
		cueNames = cnames;
		reinforced = reinf;
        partsString = new ArrayList<String>();
		setParts(new ArrayList<CS>());
		for (char c : cueNames.toCharArray()) {
			addPart(new CS(c + "", 0, 0));
		}
	}

	public void addPart(CS part) {
		parts.add(part);
        partsString.clear();
	}

	/**
	 * Adds the extra number of trials on the existed ones, if there is an
	 * existed stimuli on the ordered sequence.
	 * 
	 * @param n
	 *            a number specifying the extra trials to be added.
	 */
	public void addTrials(int n) {

		trials += n;
	}

	/**
	 * 
	 * @param part
	 * @return true if there is one of the provided CS in this stimulus.
	 */

	public boolean contains(CS part) {
		return parts.contains(part) || part.getName().equals("*");
	}

	/**
	 * 
	 * @param parts
	 *            String of cue names
	 * @return true if this string of cues is a subset of the stimulus
	 */

	public boolean contains(String parts) {
		// Dealing with probe CSs
		if (parts.contains("(")) {
			String[] bits = parts.split("\\(");
			String csTrialString = bits[1].split("\\)")[0];
			if (!csTrialString.equals(fullName)) {
				return false;
			}
			parts = bits[0].split("'")[0];
		}

		if (contains(new CS(parts, 0, 0))) {
			return true;
		}
		for (char c : parts.toCharArray()) {
			if (!getPartsString().contains(c + "")) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the String presentation of the containing cues in the stimulus.
	 * 
	 * @return a String describing the containing cues.
	 */
	public String getCueNames() {

		return cueNames;
	}

	/**
	 * Returns the String presentation of the stimulus containing the number of
	 * trials and the reinforced sign.
	 * 
	 * @return a String value of the stimulus presentation
	 */
	public String getName() {

		return fullName;
	}

	/**
	 * @return the parts
	 */
	public List<CS> getParts() {
		return parts;
	}

    public List<String> getPartsString() {
        if(partsString.isEmpty()) {
            for(CS c : parts) {
                partsString.add(c.getName());
            }
        }
        return partsString;
    }

	/**
	 * Returns the number of trials that the specific cue appears in the phase.
	 * 
	 * @return a number specifying the times the stimulus appears.
	 */
	public int getTrials() {

		return trials;
	}

	/**
	 * Returns true if the stimulus is been reinforced by an US and false if is
	 * not.
	 * 
	 * @return determines if it is reinforced or not.
	 */
	public boolean isReinforced() {

		return reinforced;
	}

	/**
	 * @param parts
	 *            the parts to set
	 */
	public void setParts(List<CS> parts) {
		this.parts = parts;
	}
}
