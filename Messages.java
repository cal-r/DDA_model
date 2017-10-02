/**
 * 
 */
package simulator;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * City University BSc Computing with Artificial Intelligence Project title:
 * Building a TD Simulator for Real-Time Classical Conditioning
 * 
 * @supervisor Dr. Eduardo Alonso
 * @author Jonathan Gray
 **/
public class Messages {
	private static final String BUNDLE_NAME = "simulator.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

    private static final MessageFormat formatter = new MessageFormat("");
//

	public static String getString(String key) {
		try {
			
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

    public static String format(String pattern, Object... args) {
        formatter.applyPattern(getString(pattern));
        return formatter.format(args);
    }

    public static String format(String pattern, Object arg) {
        Object[] args = {arg};
        return format(pattern, args);
    }

	private Messages() {
        formatter.setLocale(Locale.ENGLISH);
	}
}
