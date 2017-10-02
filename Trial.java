/**
 * 
 */
package simulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import simulator.configurables.ContextConfig.Context;

/**
 * City University BSc Computing with Artificial Intelligence Project title:
 * Building a TD Simulator for Real-Time Classical Conditioning
 * 
 * @supervisor Dr. Eduardo Alonso
 * @author Jonathan Gray
 **/
public class Trial {

	private static final long serialVersionUID = 1L;
	/** Trial string. **/
	private String trialString;
	/** A probe trial. **/
	private boolean isProbe;
	/** CSs in this trial. **/
	private Set<CS> cues;

    public int getTrialNumber() {
        return trialNumber;
    }

    public void setTrialNumber(int trialNumber) {
        this.trialNumber = trialNumber;
    }

    private int trialNumber;

	public Trial(String trialString, boolean isProbe, int stringIndex) {
		this(trialString, isProbe, stringIndex, trialString, 0, 0);
	}
	
	public Trial(String trialString, boolean isProbe, int stringIndex, int trialNumber) {
		this(trialString, isProbe, stringIndex, trialString, 0, trialNumber);
	}

	public Trial(String trialString, boolean isProbe, int stringIndex,
			String selStim, int stringPos, int trialNumber) {
        this.trialNumber = trialNumber;
		this.trialString = trialString;
		this.isProbe = isProbe;
		this.cues = new HashSet<CS>();
		int index = 0;
		String[] probes = selStim.split("\\^");
		String cuesWithConfigurals = selStim;
		// Add in configurals
		for (char c : trialString.toCharArray()) {
			if (Character.isLowerCase(c) || Context.isContext(c + "")) {
				cuesWithConfigurals += c;
			}
		}
		char[] cs = cuesWithConfigurals.toCharArray();
		int j = 0;
		// Count of probes for each character for adding 's
		Map<String, Integer> probeCount = new HashMap<String, Integer>();
		String compound = "";
		for (int i = 0; i < cs.length; i++) {
			char c = cs[i];
			if (Context.isContext(c + "")) {
                CS context = new CS(c + "", 0, 0);
                //context.setProbe(isProbe);
                //probeCount.put(c + "", 1);
				cues.add(context);
			} else if (Character.isLetter(c) && Character.isUpperCase(c)) {
                int count = 0;
                compound += c;
				if (probeCount.containsKey(c + "")) {
					count = probeCount.get(c + "");
				}
                if(!timingPerTrial()) {
                    stringPos = 0;
                    count = 0;
                }

				
				// Check if this a probe. If it is, it will have ^ right of it.
				try {
					// boolean probeCs = probes[j].charAt(probes[j].length()-1)
					// == c;
					boolean probeCs = cs[i + 1] == '^';
					if (probeCs) {
						
					}
					cues.add(new CS(c + "", count, stringIndex, probeCs, stringPos));
					// j += probeCs ? 1 : 0;
				} catch (ArrayIndexOutOfBoundsException e) {
					cues.add(new CS(c + "", count, stringIndex, false, stringPos));
				}
				count++;
				probeCount.put(c + "", count);
			} else if (Character.isLetter(c) && !Character.isUpperCase(c)) {
				// All configurals have an index of 0 because they have lazily
				// determined timing
				cues.add(new ConfiguralCS(c + "", 0, 0, "", false));
			}
			// Need a check on context characters here to make sure they don't
			// throw off hashing
			if (Character.isLetter(c) && !Context.isContext(c + "")
					&& timingPerTrial()) {
				index++;
			}
			stringPos++;
		}
		//cues.add(new CS(compound + "", 0, 0, false, 0));
		// Tell probe cues if they need to show 's
		for (CS cue : cues) {
			if (cue.isProbe()) {
				cue.setShowPrimes(probeCount.get(cue.getName()) > 1);
			}
		}
	}

	public Trial copy() {
		Trial newTrial = new Trial(trialString, isProbe, 0);
		newTrial.setCues(cues);
        newTrial.setTrialNumber(trialNumber);
		return newTrial;
	}

	/**
	 * @return
	 */
	public Set<CS> getCues() {
		return cues;
	}

	/**
	 * @return the trialString
	 */
	public String getTrialString() {
		return trialString;
	}

	/**
	 * @return the isProbe
	 */
	public boolean isProbe() {
		return isProbe;
	}

	public void setCues(Set<CS> cues) {
		this.cues.clear();
		this.cues.addAll(cues);
	}

	/**
	 * @param isProbe
	 *            the isProbe to set
	 */
	public void setProbe(boolean isProbe) {
		this.isProbe = isProbe;
	}

	/**
	 * @param trialString
	 *            the trialString to set
	 */
	public void setTrialString(String trialString) {
		this.trialString = trialString;
	}

	/**
	 * @return
	 */
	private boolean timingPerTrial() {
		return Simulator.getController().getModel().isTimingPerTrial();
	}

	@Override
	public String toString() {
		return trialString;
	}

    public boolean isReinforced() {
        return trialString.endsWith("+");
    }

    public String getProbeSymbol() {
        String probeName = "(";
        for(char c : getTrialString().toCharArray()) {
            if(Character.isUpperCase(c)) {
                probeName += c;
            }
        }
        probeName += isReinforced() ? "+" : "-";
        probeName += ")";
        int numPrimes = trialNumber;
        String primes = new String(new char[numPrimes]).replace("\0", "'");
        return probeName + primes;
    }

}
