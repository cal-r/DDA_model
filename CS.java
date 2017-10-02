/**
 * 
 */
package simulator;

import java.io.Serializable;

/**
 * City University BSc Computing with Artificial Intelligence Project title:
 * Building a TD Simulator for Real-Time Classical Conditioning
 * 
 * @supervisor Dr. Eduardo Alonso
 * @author Jonathan Gray
 **/
public class CS implements Comparable, Serializable {

	public static CS TOTAL = new CS("Total", 0, 0);
	public static CS CS_TOTAL = new CS("CS_Total", 0, 0);
	public static CS OMEGA = new CS("Omega", 0, 0);
	public static CS US = new CS("US", 0, 0);

	private static final long serialVersionUID = 1L;
	/** String name. **/
	private String name;
	/** Which of this CS in the trial string is this? **/
	private int hash;
	/** Trial group number. **/
	private int group;
	/** Overall position in string. **/
	private int stringPos;
	/** Is this a probe CS? **/
	private boolean isProbe;
	/** Should it show primes? **/
	private boolean showPrimes;
	/** Trial string this CS belongs to. **/
	private String trialString = "";

	/**
	 * 
	 */
	public CS(String name) {
		this.name = name;
		hash = super.hashCode();
		group = 0;
		isProbe = false;
		showPrimes = false;
		stringPos = 0;
	}

	public CS(String name, int hash, int group) {
		this(name);
		this.group = group;
		this.hash = hash;
		isProbe = false;
	}

	public CS(String name, int hash, int group, boolean probe) {
		this(name, hash, group);
		isProbe = probe;
	}
	
	public CS(String name, int hash, int group, boolean probe, int stringPos) {
		this(name, hash, group, probe);
		this.stringPos = stringPos; 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object arg0) {
		CS other = (CS) arg0;

		// Added a more rigorous compare to resolve misordering in duration
		// treeset

		if (!name.equals(other.getName())) {
			return name.compareTo(other.getName());
		} else if (group > other.getGroup()) {
			return 1;
		} else if (group < other.getGroup()) {
			return -1;
		} else if (hash > other.hashCode()) {
			return 1;
		} else if (hash < other.hashCode()) {
			return -1;
		} else if (stringPos > other.getStringPos()) {
			return 1;
		} else if (stringPos < other.getStringPos()) {
			return -1;
		}
		return 0;
	}

	public CS copy() {
		return new CS(name, hash, group, isProbe, stringPos);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof CS) {
			CS target = (CS) other;
			if (!name.equals(target.getName())) {
				return false;
			} else if (target.hashCode() != hashCode()) {
				return false;
			} else if (target.getGroup() != group) {
				return false;
			} else if (target.getStringPos() != stringPos) {
				return false;
			} else {
				return true;
			}
		}
		// Fall back to string compare
		return name.equals(other);
	}

	/**
	 * @return the group
	 */
	public int getGroup() {
		return group;
	}

	public String getName() {
		return name;
	}

	public String getProbeSymbol() {
		int numPrimes = isShowPrimes() ? hash + 1 : 0;
		String primes = new String(new char[numPrimes]).replace("\0", "'");
		return getName() + primes;
	}

	/**
	 * @return the trialString
	 */
	public String getTrialString() {
		return trialString;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	/**
	 * 
	 * @return true if this is a compound CS of any kind.
	 */

	public boolean isCompound() {
		return this.name.length() > 1;
	}

	public boolean isConfigural() {
		return !name.toUpperCase().equals(name) && name.length() == 1;
	}

	/**
	 * @return true if this is a probe CS
	 */
	public boolean isProbe() {
		return isProbe;
	}

	public boolean isSerialConfigural() {
		return false;
	}

	/**
	 * @return the showPrimes
	 */
	public boolean isShowPrimes() {
		return showPrimes;
	}

	/**
	 * @param group
	 *            the group to set
	 */
	public void setGroup(int group) {
		this.group = group;
	}

	public void setHashCode(int code) {
		hash = code;
	}

	public void setName(String newName) {
		name = newName;
	}

	/**
	 * @param isProbe
	 *            set to true if this is a probe CS.
	 */
	public void setProbe(boolean isProbe) {
		this.isProbe = isProbe;
	}

	/**
	 * @param showPrimes
	 *            the showPrimes to set
	 */
	public void setShowPrimes(boolean showPrimes) {
		this.showPrimes = showPrimes;
	}

	/**
	 * @param trialString
	 *            the trialString to set
	 */
	public void setTrialString(String trialString) {
		this.trialString = trialString;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return the stringPos
	 */
	public int getStringPos() {
		return stringPos;
	}

	/**
	 * @param stringPos the stringPos to set
	 */
	public void setStringPos(int stringPos) {
		this.stringPos = stringPos;
	}
	
	public int getLocalStringPos() {
		int pos = 0;
		int count = -1;

		while(pos < trialString.length() && count < hash) {
			if(trialString.substring(pos, pos+1).equals(name)) {
				count++;
			}
			pos++;
		}
		
		return pos;
	}

}
