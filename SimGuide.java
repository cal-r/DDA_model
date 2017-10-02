/**
 * SimGuide.java
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

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

/**
 * SimGuide presents a new frame with a JEditorPane inside it. The last can be
 * used to present a rich-text document such as html pages. This advantage will
 * help the application to produce a html page with a anchor navigated system.
 * This page will be the user guide, manual of this animal learning simulator.
 */
public class SimGuide extends JFrame {
	private JScrollPane scrollPane;
	private JEditorPane htmlPane;

	/**
	 * SimGuide's main Constructor Method.
	 * 
	 * @param title
	 *            the frame's title.
	 * @throws Exception
	 *             if the html file can not be found it throws an exception.
	 */
	public SimGuide(String title) throws Exception {
		super(title);
		try {

			String file = "/Extras/guide.html";

			java.net.URL fileURL = this.getClass().getResource(file);
			if (fileURL != null) {

				htmlPane = new JEditorPane(fileURL);
				htmlPane.setContentType("text/html");
				htmlPane.setEditable(false);

				scrollPane = new JScrollPane(htmlPane);

				scrollPane.setPreferredSize(new Dimension(850, 500));
				getContentPane().add(scrollPane);
				this.getContentPane().add(scrollPane);
				this.pack();
				this.setResizable(false);
				this.setLocation(50, 50);
			} else {
				System.err.println("Couldn't find file");
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}
}
