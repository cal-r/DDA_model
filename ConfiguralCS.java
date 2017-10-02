/**
 * 
 */
package simulator;

/**
 * City University BSc Computing with Artificial Intelligence Project title:
 * Building a TD Simulator for Real-Time Classical Conditioning
 * 
 * @supervisor Dr. Eduardo Alonso
 * @author Jonathan Gray
 **/
public class ConfiguralCS extends CS {


	private String parts;
	private boolean serial;
	public static String SERIAL_SEP = "\u2192";

	/**
	 * @param name
	 */
	public ConfiguralCS(String name) {
		super(name);
		parts = "";
		serial = false;
	}

	/**
	 * @param name
	 * @param hash
	 * @param group
	 */
	public ConfiguralCS(String name, int hash, int group, String string,
			boolean serial) {
		super(name, hash, group);
		this.setParts(string);
		this.serial = serial;
	}

	/**
	 * @return the parts
	 */
	public String getParts() {
		return parts;
	}

	@Override
	public boolean isSerialConfigural() {
		return serial;
	}

	/**
	 * @param parts
	 *            the parts to set
	 */
	public void setParts(String parts) {
		this.parts = parts;
	}

}
