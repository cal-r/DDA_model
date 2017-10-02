package simulator;

import jsr166y.ForkJoinPool;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;

/**
 * Simulator creates phase (model), view and controller. They are created once
 * here and passed to the other parts that need them so there is only on copy of
 * each. The MVC structure
 */
public class Simulator {
	private static SimModel model = new SimModel();
	private SimView view;
	private static SimController controller;
	public static final double VERSION = 0.999;
	public static ForkJoinPool fjPool = new ForkJoinPool();
	public static final char OMEGA = '\u03A9';

	/**
	 * @return the controller
	 */
	public static SimController getController() {
		return controller;
	}

	/**
	 * The main method of the application. It creates a new Simulator object.
	 * This is the method that is needed to start up the program.
	 * 
	 * @param args
	 *            by default this parameter is needed if any arguments inserted
	 *            from the command prompt.
	 */
	// Modified by E. Mondragon. July 29, 2011
	// public static void main(String[] args) {
	public static void main(String[] args) {
		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
		}
		new Simulator();
	}

	/**
	 * Simulator's Constructor method
	 */
	public Simulator() {
		view = new SimView(model);
		setController(new SimController(model, view));

		view.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		view.addWindowListener(new WindowAdapter()
		{
		    public void windowClosing(WindowEvent e)
		    {
		    	if (model != null && model.getGroups()!= null) 
		    		{for (SimGroup g : model.getGroups().values()) {
		    		g.closeDBs();
		    	}
		    		}
		    	String directoryName = System.getProperty("user.home");
				File f =new File(directoryName + System.getProperty("file.separator")
				+ "TempDD");
				deleteFolder(f);
		        Simulator.controller.clearSave();
		    }
		});
		view.setVisible(true);
	}
	
	public void deleteFolder(File folder) {
		
		 File[] files = folder.listFiles();
		    if(files!=null) { //some JVMs return null for empty dirs
		        for(File f: files) {
		            if(f.isDirectory()) {
		                deleteFolder(f);
		            } else {
		                f.delete();
		            }
		        }
		    }
		    folder.delete();
		
	}

	/**
	 * @param controller
	 *            the controller to set
	 */
	public void setController(SimController controller) {
		Simulator.controller = controller;
		view.updatePhasesColumnsWidth();
	}

    private static ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = Simulator.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println(Messages.getString("SimView.404Error") + path); //$NON-NLS-1$
            return null;
        }
    }

    public static java.util.List<Image> makeIcons() {
        java.util.List<Image> icon = new ArrayList<Image>();
        icon.add(createImageIcon("/simulator/extras/icon_16.png", "")
                .getImage());
        icon.add(createImageIcon("/simulator/extras/icon_32.png", "")
                .getImage());
        icon.add(createImageIcon("/simulator/extras/icon_256.png", "")
                .getImage());
        icon.add(createImageIcon("/simulator/extras/icon_512.png", "")
                .getImage());
        return icon;
    }
}
