/**
 * SimController.java
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
 *2015-17
 *
 *Nicholas
 *
 *
 */
package simulator;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import simulator.configurables.ContextConfig;
import simulator.configurables.ContextConfig.Context;
import simulator.configurables.ITIConfig;
import simulator.configurables.TimingConfiguration;
import simulator.dialog.SharedElementsDialog;
import simulator.dialog.USIntensityDialog;
import simulator.editor.TrialStringEditor;
import simulator.graph.AnimationGraph;
import simulator.graph.ComponentActivationGraph;
import simulator.graph.ComponentAsymptotes;
import simulator.graph.ComponentDeltaGraph;
import simulator.graph.ComponentPredictionGraph;
import simulator.graph.ContextGraph;
import simulator.graph.ErrorGraph;
import simulator.graph.ErrorTimeGraph;
import simulator.graph.GraphLine;
import simulator.graph.LeastSquares;
import simulator.graph.RTimeGraph;
import simulator.graph.SalienceGraph;
import simulator.graph.VTimeGraph;
import simulator.graph.VTrialGraph;
import simulator.graph.WTrialGraph;
import simulator.graph.WTimeGraph;
import simulator.graph.RTrialGraph;
import simulator.util.*;
import simulator.util.io.SimExport;
import simulator.util.io.SimGraphExport;

/**
 * SimController is the main class of the simulator project. It controls the
 * behavior of the model which includes the groups and their phases and handles
 * the view components. The user has direct interaction through the controller
 * class.
 */
public class SimController implements ActionListener, PropertyChangeListener {

    public static String getTimeMessage(long remaining) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining);
        long hours = TimeUnit.MILLISECONDS.toHours(remaining);
        long minutesOver = TimeUnit.MILLISECONDS.toMinutes(remaining)
                - TimeUnit.HOURS
                .toMinutes(TimeUnit.MILLISECONDS
                        .toHours(remaining));
        minutesOver = (int)(Math.round( minutesOver / 15.0) * 15);
        if(minutesOver == 60) {
            minutesOver = 0;
            hours += 1;
        }
        String timeRemaining = "Under a minute remaining";
        if(hours == 1 && minutesOver == 0) {
            timeRemaining = "About an hour remaining";
        } else if (hours > 0 && minutesOver == 0) {
            timeRemaining = String.format(
                    "About %d hours remaining",
                    hours);
        } else if (hours > 0) {
            timeRemaining = String.format(
                    "About %d %s, %d minutes remaining",
                    hours, hours == 1 ? "hour" : "hours",minutesOver);
        }else if (minutes == 1) {
            timeRemaining = "About a minute remaining";
        }
        else if (minutes > 0) {
            timeRemaining = String.format(
                    "About %d minutes remaining",
                    minutes);
        }

        return timeRemaining;
    }

	/**
	 * Background task for running a simulation.
	 * 
	 * @author J Gray
	 * 
	 */

	class ExportTask extends SwingWorker<Void, Void> {
		/**
		 * Run the simulation in a worker thread, starts simulating and attaches
		 * a progress monitor periodically updated with task progress.
		 */
		@Override
		public Void doInBackground() {
			SimController.this.control = new ModelControl();
			int progress = 0;
			SimController.this.view.setUILocked(true);
            SimController.this.exporter.setControl(SimController.this.control);

            progressMonitor = new MyProgressMonitor(
                    view,
                    Messages.getString("SimController.exportMessage"), "", 0, 100); //$NON-NLS-1$ //$NON-NLS-2$
            progressMonitor.setMillisToPopup(0);
            progressMonitor.setMillisToDecideToPopup(0);
            progressMonitor.setProgress(0);
            //AccessibleContext ac = progressMonitor.getAccessibleContext();
            //JDialog dialog = (JDialog)ac.getAccessibleParent();
            //dialog.setSize(dialog.getWidth() + 100, dialog.getHeight() + 50);
            int dots = 0;

			setProgress(1);
            int jobs = SimController.this.exporter.initialTasks();
            SimController.this.totalProgress = SimController.this.exporter.initialTasks();
            progressMonitor.setMaximum(totalProgress + 2);
            SimController.this.simulate = new Thread(
                    SimController.this.exporter);
            SimController.this.simulate.start();
            boolean makingExport = true;


			try {
                while (!isCancelled() && simulate.isAlive()
                        && !progressMonitor.isCanceled()
                        && !control.isComplete()) {
                    if(makingExport && control.madeExport()) {
                        totalProgress = control.getTotalProgress() + 2;
                        progressMonitor.setMaximum(totalProgress);
                        progressMonitor.setProgress(1);
                        makingExport = false;
                    }

        			//System.out.println("loop simctrl");
                    // Update progress
                    Thread.sleep(100);
                    progress = (int) control.getProgress();
                    // setProgress(Math.min(progress, totalProgress));
                    // int progress = (Integer) evt.getNewValue();
                    progressMonitor.setProgress(progress);
                    long remaining = (long) (control.getEstimatedCycleTime() * (totalProgress - control
                            .getProgress())) / 1000;
                    String msg = SimController.getTimeMessage(remaining);
                    if(makingExport) {
                        msg = Messages.getString("collecting.results");
                    }
                    for(int i = 0; i < dots; i++) {
                        msg += ".";
                    }
                    progressMonitor.setNote(msg);
                    dots++;
                    dots = dots%4;
                }
                if (progressMonitor.isCanceled()) {
                    cancel(true);
                }
            } catch (InterruptedException ignore) {
                System.err.println("Interrupted!");
            }
            return null;
		}

		/**
		 * Kill the progress monitor, re-enable the gui and update the output if
		 * the simulation wasn't cancelled.
		 */
		@Override
		public void done() {
			view.getGlassPane().setCursor(
					Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			view.getGlassPane().setVisible(false);
			progressMonitor.setProgress(0);
			progressMonitor.close();
			if (exporter != null && !exporter.isSuccess()) {
				view.showError(Messages.getString("SimController.fileError"));
			}
			view.setUILocked(false);
		}
	}
	
	class ExportTask2 extends SwingWorker<Void, Void> {
		/**
		 * Run the simulation in a worker thread, starts simulating and attaches
		 * a progress monitor periodically updated with task progress.
		 */
		@Override
		public Void doInBackground() {
			SimController.this.control = new ModelControl();
			int progress = 0;
			SimController.this.view.setUILocked(true);
            SimController.this.exporter2.setControl(SimController.this.control);

            progressMonitor = new MyProgressMonitor(
                    view,
                    Messages.getString("SimController.exportMessage"), "", 0, 100); //$NON-NLS-1$ //$NON-NLS-2$
            progressMonitor.setMillisToPopup(0);
            progressMonitor.setMillisToDecideToPopup(0);
            progressMonitor.setProgress(0);
            //AccessibleContext ac = progressMonitor.getAccessibleContext();
            //JDialog dialog = (JDialog)ac.getAccessibleParent();
            //dialog.setSize(dialog.getWidth() + 100, dialog.getHeight() + 50);
            int dots = 0;

			setProgress(1);
            int jobs = SimController.this.exporter2.initialTasks();
            SimController.this.totalProgress = SimController.this.exporter2.initialTasks();
            progressMonitor.setMaximum(totalProgress + 2);
            SimController.this.simulate = new Thread(
                    SimController.this.exporter);
            SimController.this.simulate.start();
            boolean makingExport = true;


			try {
                while (!isCancelled() && simulate.isAlive()
                        && !progressMonitor.isCanceled()
                        && !control.isComplete()) {
                    if(makingExport && control.madeExport()) {
                        totalProgress = control.getTotalProgress() + 2;
                        progressMonitor.setMaximum(totalProgress);
                        progressMonitor.setProgress(1);
                        makingExport = false;
                    }

        			//System.out.println("loop simctrl");
                    // Update progress
                    Thread.sleep(100);
                    progress = (int) control.getProgress();
                    // setProgress(Math.min(progress, totalProgress));
                    // int progress = (Integer) evt.getNewValue();
                    progressMonitor.setProgress(progress);
                    long remaining = (long) (control.getEstimatedCycleTime() * (totalProgress - control
                            .getProgress())) / 1000;
                    String msg = SimController.getTimeMessage(remaining);
                    if(makingExport) {
                        msg = Messages.getString("collecting.results");
                    }
                    for(int i = 0; i < dots; i++) {
                        msg += ".";
                    }
                    progressMonitor.setNote(msg);
                    dots++;
                    dots = dots%4;
                }
                if (progressMonitor.isCanceled()) {
                    cancel(true);
                }
            } catch (InterruptedException ignore) {
                System.err.println("Interrupted!");
            }
            return null;
		}

		/**
		 * Kill the progress monitor, re-enable the gui and update the output if
		 * the simulation wasn't cancelled.
		 */
		@Override
		public void done() {
			view.getGlassPane().setCursor(
					Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			view.getGlassPane().setVisible(false);
			progressMonitor.setProgress(0);
			progressMonitor.close();
			if (exporter2 != null && !exporter2.isSuccess()) {
				view.showError(Messages.getString("SimController.fileError"));
			}
			view.setUILocked(false);
		}
	}

	/**
	 * Background task for running a simulation.
	 * 
	 * @author J Gray
	 * 
	 */

	class RunTask extends SwingWorker<Void, Void> {

		/**
		 * Run the simulation in a worker thread, starts simulating and attaches
		 * a progress monitor periodically updated with task progress.
		 */
		@Override
		public Void doInBackground() {
			int progress = 0;
			view.setUILocked(true);
			control = new ModelControl();
			getModel().setControl(control);
			// execute the algorithm
			simulate = new Thread(getModel());
			simulate.start();
			// Initialize progress property.
			totalProgress = getModel().totalNumPhases() + 2;
			progressMonitor = new MyProgressMonitor(
					view,
					Messages.getString("SimController.runMessage"), "", 0, totalProgress + 1); //$NON-NLS-1$ //$NON-NLS-2$
			progressMonitor.setMillisToPopup(0);
			progressMonitor.setMillisToDecideToPopup(0);
			progressMonitor.setProgress(0);
			setProgress(1);
            //AccessibleContext ac = progressMonitor.getAccessibleContext();
            //JDialog dialog = (JDialog)ac.getAccessibleParent();
            //dialog.setSize(dialog.getWidth() + 100, dialog.getHeight() + 50);
            int dots = 0;
			try {
				while (!isCancelled() && simulate.isAlive()
						&& !progressMonitor.isCanceled()
						&& !control.isComplete()) {
					// Update progress

					//System.out.println("loop simcontrol");
					Thread.sleep(1000);
					progress = (int) control.getProgress();
					// setProgress(Math.min(progress, totalProgress));
					// int progress = (Integer) evt.getNewValue();
					progressMonitor.setProgress(progress);
					long remaining = (long) (control.getEstimatedCycleTime() * (totalProgress - control
							.getProgress()));
                    String msg = SimController.getTimeMessage(remaining);
                    for(int i = 0; i < dots; i++) {
                        msg += ".";
                    }
                    progressMonitor.setNote(msg);
                    dots++;
                    dots = dots%4;
				}
				if (progressMonitor.isCanceled()) {
					cancel(true);
				}
			} catch (InterruptedException ignore) {
				System.err.println("Interrupted!");
			}
			return null;
		}

		/**
		 * Kill the progress monitor, re-enable the gui and update the output if
		 * the simulation wasn't cancelled.
		 */
		@Override
		public void done() {
			view.setUILocked(false);
			if (!isCancelled()) {
				view.setStatusComponent(true, "dispGraph"); //$NON-NLS-1$
				view.setStatusComponent(true, "Export"); //$NON-NLS-1$
				view.setStatusComponent(true, "Save"); //$NON-NLS-1$

				view.intensityButton.setEnabled(true);
			} else {
				// getModel().cancel();
				control.setCancelled(true);
				simulate.interrupt();
				simulate = null;
				view.setStatusComponent(false, "dispGraph"); //$NON-NLS-1$
				view.setOutput("");
				view.setStatusComponent(false, "Export"); //$NON-NLS-1$
				view.setStatusComponent(false, "Save"); //$NON-NLS-1$

				view.intensityButton.setEnabled(false);
			}
			view.getGlassPane().setCursor(
					Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			view.getGlassPane().setVisible(false);
			progressMonitor.setProgress(0);
			progressMonitor.close();
		}
	}

	private SimModel model;

    public SimView getView() {
        return view;
    }

    public void setView(SimView view) {
        this.view = view;
    }

    private SimView view;
	/** Monitor/progress bar. **/
	private MyProgressMonitor progressMonitor;
	/** Task for running the simulation. **/
	private RunTask task;

	/** Thread for simulating. **/
	private volatile Thread simulate;
	/** Excel exporter. **/
	private SimExport exporter;
	private String lastDirectory = FileSystemView.getFileSystemView().getHomeDirectory().getPath(); // Alberto Fern�ndez Sept-2011  //$NON-NLS-1$
	private boolean cscMode;
	private ExportTask exporterTask;

	private volatile ModelControl control;

	private JFileChooser chooser;
	
	private int totalProgress;
	private boolean trialPrediction = false;
	private boolean prediction = false;
	private boolean vGraph = false;
	private boolean watGraph = false;
	private boolean waTGraph = false;
	private boolean trialResponse = false;
	private boolean componentActivation = false;
	private boolean componentStrength = false;
	private boolean errorGraph = false;
	private boolean errorTrial = false;
	private boolean componentPrediction = false;
	private boolean componentDelta = false;
	private boolean probePrediction = false;
	private boolean animationGraph = false;
	private boolean responseGraph = false;
	private boolean contextGraph = false;
	private boolean presenceMean = false;
	private boolean elementError = false;
	private boolean saliences = false;
	private boolean componentPredictions = false;
	private boolean resetContext = true;
	private boolean isAsymptote = false;
	
	
	private boolean vartheta = false;
	private boolean esther = false;
	
	private TreeMap<String,TreeMap<String,TreeMap<String,Color>>> colourMap;
	private TreeMap<String,TreeMap<String,Color>> responseColourMap;
	
	private SharedElementsDialog seDialog;
	private USIntensityDialog uid;
	private TreeMap<String, ArrayList<Float>> tempIntensities;
	private SimGraphExport exporter2;
	private ExportTask exporterTask2;
	private String lastDirectory2;
	/**
	 * SimController's Constructor method.
	 * 
	 * @param m
	 *            the SimPhase Object, the model on the structure.
	 * @param v
	 *            the SimView Object, the view in the application.
	 */
	public SimController(SimModel m, SimView v) {
		setModel(m);
		view = v;
		view.addButtonListeners(this); // add actionListeners on the buttons
		view.addMenuListeners(this); // add actionListeners on the menuitems
		view.setStatusComponent(false, "run"); //$NON-NLS-1$
		view.setStatusComponent(false, "dispGraph"); //$NON-NLS-1$

		view.intensityButton.setEnabled(false);
		view.setStatusComponent(false, "Export"); //$NON-NLS-1$
		view.setStatusComponent(false, "Save"); //$NON-NLS-1$
		colourMap = new TreeMap();
		responseColourMap = new TreeMap();
	}

	//

	/*
	 * Actions performed whenever the user clicks a button or a menu item.
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// The user chooses to start a new experiment, a new model is created
		// and all components status are being reset to their initial value.
		if (e.getActionCommand() == "New") { //$NON-NLS-1$
			for (SimGroup g : model.getGroups().values()) {g.closeDBs(); g.resetAll();}

            view.errorCheckList.setEnabled(true);
            view.errorTrialCheckList.setEnabled(true);
			if (model.isExternalSave() && model.getDirectory() != null) {
            	Object[] options = {"Ok",
                        "Cancel"};
			   /* int n = JOptionPane.showOptionDialog(view,
			        "Delete temp files for last run?","Caution",
			        JOptionPane.YES_NO_CANCEL_OPTION,
			        JOptionPane.QUESTION_MESSAGE,
			        null,
			        options,
			        options[1]);
            	if (n == 0)*/
            	clearSave();
            }
			newModel();
		}
		if (e.getActionCommand() == "SetSerialCompound") {
			// Enable compounds if serial compounds are considered.
			if (view.isUseSerialCompounds()) {
				view.setIsSetCompound(true);
                view.setIsSetConfiguralCompounds(true);
                view.setStatusComponent(view.isSetConfiguralCompounds(),
                        "SetConfiguralCompounds");
			}
		}
		if (e.getActionCommand() == "restrictPredictions") {
			model.setRestrictPredictions(view.isRestrictPredictions());
		}
		if (e.getActionCommand() == "timingPerTrial") {
			view.updateTimingConfigs();
			view.updatePhasesColumnsWidth();
			model.setTimingPerTrial(view.timingPerTrial());
		}
		if (e.getActionCommand() == "externalSave") {
			model.setExternalSave(view.isExternalSave());
		}
		if (e.getActionCommand() == "noReset") { //$NON-NLS-1$
			resetContext = false;
			view.toggleReset(resetContext);
			getModel().setResetContext(resetContext);
		}
		if (e.getActionCommand() == "resetContext") { //$NON-NLS-1$
			resetContext = true;
			view.toggleReset(resetContext);
			getModel().setResetContext(resetContext);
		}
		if (e.getActionCommand() == "rule1") { //$NON-NLS-1$
			view.toggleRule(1);
			getModel().setLearningRule(1);
		}
		if (e.getActionCommand() == "rule2") { //$NON-NLS-1$
			view.toggleRule(2);
			getModel().setLearningRule(2);
		}
		if (e.getActionCommand() == "vGraph") { //$NON-NLS-1$
			vGraph = !vGraph;
		}
		if (e.getActionCommand() == "watGraph") { //$NON-NLS-1$
			watGraph = !watGraph;
		}
		if (e.getActionCommand() == "waTGraph") { //$NON-NLS-1$
			waTGraph = !waTGraph;
		}
		if (e.getActionCommand() == "presenceMean") { //$NON-NLS-1$
			presenceMean = !presenceMean;
		}
		if (e.getActionCommand() == "trialResponse") { //$NON-NLS-1$
			trialResponse = !trialResponse;
		}
		if (e.getActionCommand() == "TrialPrediction") { //$NON-NLS-1$
			trialPrediction = !trialPrediction;
		}
		if (e.getActionCommand() == "ResponseGraph") { //$NON-NLS-1$
			responseGraph = !responseGraph;
		}
		if (e.getActionCommand() == "animationGraph") { //$NON-NLS-1$
			animationGraph = !animationGraph;
		}
		
		if (e.getActionCommand() == "componentActivation") { //$NON-NLS-1$
			componentActivation = !componentActivation;
		}
		if (e.getActionCommand() == "componentStrength") { //$NON-NLS-1$
			componentStrength = !componentStrength;
		}
		if (e.getActionCommand() == "errorGraph") { //$NON-NLS-1$
			errorGraph = !errorGraph;
		}
		if (e.getActionCommand() == "errorTrial") { //$NON-NLS-1$
			errorTrial = !errorTrial;
		}
		if (e.getActionCommand() == "elementError") { //$NON-NLS-1$
			elementError = !elementError;
		}
		if (e.getActionCommand() == "vartheta") { //$NON-NLS-1$
			vartheta = !vartheta;
		}
		if (e.getActionCommand() == "esther") { //$NON-NLS-1$
			esther = !esther;
			getModel().setEsther(esther);
		}
		if (e.getActionCommand() == "ComponentPrediction") { //$NON-NLS-1$
			componentPrediction = !componentPrediction;
		}
		if (e.getActionCommand() == "ComponentDelta") { //$NON-NLS-1$
			componentDelta = !componentDelta;
		}
		if (e.getActionCommand() == "contextGraph") { //$NON-NLS-1$
			contextGraph = !contextGraph;
		}
		if (e.getActionCommand() == "salienceGraph") { //$NON-NLS-1$
			saliences = !saliences;
		}
		if (e.getActionCommand() == "componentPredictions") { //$NON-NLS-1$
			componentPredictions = !componentPredictions;
		}
		// The user chooses to open a saved experiment from the the saved files.
		// The file contains the values that were added to run the experiment.
		if (e.getActionCommand() == "Open") { //$NON-NLS-1$

            view.errorCheckList.setEnabled(true);
            view.errorTrialCheckList.setEnabled(true);
			for (SimGroup g : model.getGroups().values()) {g.closeDBs();}
			if (model.isExternalSave() && model.getDirectory() != null) {
            	Object[] options = {"Ok",
                        "Cancel"};
			    /*int n = JOptionPane.showOptionDialog(view,
			        "Delete temp files for last run?","Caution",
			        JOptionPane.YES_NO_CANCEL_OPTION,
			        JOptionPane.QUESTION_MESSAGE,
			        null,
			        options,
			        options[1]);
            	if (n == 0) */
            		
            		clearSave();
            }
			JFileChooser fc = new JFileChooser();
			// Modified Alberto Fern�ndez Sept-2011 : manage current directory
			// fc.setCurrentDirectory(new File("."));
			fc.setCurrentDirectory(new File(lastDirectory));
			ExampleFileFilter filter = new ExampleFileFilter();
			filter.addExtension(Messages
                    .getString("SimController.extension")); //$NON-NLS-1$
			filter.setDescription(Messages.getString("SimController.fileType")); //$NON-NLS-1$
			fc.setFileFilter(filter);
			int returnVal = fc.showOpenDialog(view);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					File file = fc.getSelectedFile();
					loadObjects(file);
				} catch (FileNotFoundException fe) {
					view.showError(Messages
							.getString("SimController.fileError")); //$NON-NLS-1$
				} catch (IOException ioe) {
					view.showError(Messages
							.getString("SimController.fileError")); //$NON-NLS-1$
				} catch (VersionException ve) {
					view.showError(Messages
							.getString("SimController.versionError")); //$NON-NLS-1$
				} catch (ClassNotFoundException ce) {
					view.showError(Messages
							.getString("SimController.fileError")); //$NON-NLS-1$
				}
			}
			if (model.isExternalSave() && model.getDirectory() != null) {
            	Object[] options = {"Ok",
                        "Cancel"};
			   /* int n = JOptionPane.showOptionDialog(view,
			        "Delete temp files for last run?","Caution",
			        JOptionPane.YES_NO_CANCEL_OPTION,
			        JOptionPane.QUESTION_MESSAGE,
			        null,
			        options,
			        options[1]);
            	if (n == 0)*/
            	clearSave();
            }
		}
		
		if (e.getActionCommand() == "intensity") {

			uid = new USIntensityDialog(this,view);
			uid.setVisible(true);
			
		}
		
		if (e.getActionCommand() == "OKintensity") {
			
			tempIntensities = model.getIntensities();
			int row = 0;
			for (String s : tempIntensities.keySet()) {
				for (int i = 1; i <= model.getGroups().get(s).getNoOfPhases(); i++) {
					tempIntensities.get(s).set(i-1, Float.valueOf(uid.getValue(row, i)));
				}
				row++;
			}
			uid.setVisible(false);
		}

		// The user chooses to save the current experiment from into a specific
		// file.
		// The file will contain the values that were added to run the
		// experiment.
		if (e.getActionCommand() == "Save") { //$NON-NLS-1$
			JFileChooser fc = new JFileChooser();
			// Modified Alberto Fern�ndez Sept-2011 : manage current directory
			// fc.setCurrentDirectory(new File("."));

			fc.setCurrentDirectory(new File(lastDirectory));
			ExampleFileFilter filter = new ExampleFileFilter();
			filter.addExtension(Messages.getString("SimController.extension")); //$NON-NLS-1$
			filter.setDescription(Messages.getString("SimController.fileType")); //$NON-NLS-1$
			fc.setFileFilter(filter);
			int returnVal = fc.showSaveDialog(view);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					String path = fc.getSelectedFile().getParent();
					lastDirectory = path; // Alberto Fern�ndez Sept-2011

					String name = fc.getSelectedFile().getName();

					if (!name.contains(Messages
							.getString("SimController.dotExtension")))name += Messages.getString("SimController.dotExtension"); //$NON-NLS-1$ //$NON-NLS-2$

					File file = new File(path, name);
					saveToObjects(file);
				} catch (FileNotFoundException fe) {
					view.showError(Messages
							.getString("SimController.fileError")); //$NON-NLS-1$
				} catch (IOException ioe) {
					view.showError(Messages
							.getString("SimController.fileError")); //$NON-NLS-1$
				}
			}
		}

		// The uses chooses to save his results into a spreadsheet.
		if (e.getActionCommand() == "Export") { //$NON-NLS-1$
			doExport();
		}

		// The user chooses to quit. The application closes.
		if (e.getActionCommand() == "Quit") { //$NON-NLS-1$
			for (SimGroup g : model.getGroups().values()) {g.closeDBs();}
			if (model.isExternalSave() && model.getDirectory() != null) {
            	/*Object[] options = {"Ok",
                        "Cancel"};
			    int n = JOptionPane.showOptionDialog(view,
			        "Delete temp files for last run?","Caution",
			        JOptionPane.YES_NO_CANCEL_OPTION,
			        JOptionPane.QUESTION_MESSAGE,
			        null,
			        options,
			        options[1]);
            	if (n == 0) */
				clearSave();
            }
			System.exit(0);
		}

		if (e.getActionCommand().equals("SetCsc")) { //$NON-NLS-1$
			cscMode = !cscMode;
		}

		// The user chooses to change the default value of number of
		// combinations
		if (e.getActionCommand() == "Combinations") { //$NON-NLS-1$
			int n = view
					.getIntInput(
							Messages.getString("SimController.randomMessage"), "" + getModel().getCombinationNo()); //$NON-NLS-1$ //$NON-NLS-2$
			// Check if 'Cancel' was pressed
			if (n != -1) {
				getModel().setCombinationNo(n);
			}
		}
		
		if (e.getActionCommand() == "Curves") { //$NON-NLS-1$
			float n = view
					.getFloatInput(
							"Enter right-skew of the stimulus curves.", "" + getModel().getSkew(true)); //$NON-NLS-1$ //$NON-NLS-2$
			// Check if 'Cancel' was pressed
			if (n != -1) {
				getModel().setSkew(n);
			}
		}
		if (e.getActionCommand() == "eli") { //$NON-NLS-1$
			float n = view
					.getFloatInput(
							"Enter eligibility discount.", "" + getModel().getDiscount()); //$NON-NLS-1$ //$NON-NLS-2$
			// Check if 'Cancel' was pressed
			if (n != -1) {
				getModel().setDiscount(Math.max(0, Math.min(1, n)));
			}
		}
		if (e.getActionCommand() == "assocDiscount") { //$NON-NLS-1$
			float n = view
					.getFloatInput(
							"Enter discount ratio of associative asymptote.", "" + getModel().getAssociativeDiscount()); //$NON-NLS-1$ //$NON-NLS-2$
			// Check if 'Cancel' was pressed
			if (n <= 1 && n >= 0) {
				getModel().setAssociativeDiscount(n);
			}
		}
		
		if (e.getActionCommand() == "Context Reset") { //$NON-NLS-1$
			float n = view
					.getFloatInput(
							"Enter context reset value.", "" + getModel().getResetValue(true)); //$NON-NLS-1$ //$NON-NLS-2$
			// Check if 'Cancel' was pressed
			if (n != -1 && n <= 1 && n > 0) {
				getModel().setResetValue(n);
			}
		}
		if (e.getActionCommand() == "CSCV") { //$NON-NLS-1$
			float n = view
					.getFloatInput(
							"Enter coefficient of variation of the CS elements.", "" + getModel().getCSCV()); //$NON-NLS-1$ //$NON-NLS-2$
			// Check if 'Cancel' was pressed
			if (n != -1) {
				getModel().setCSCV(n);
			}
		}
		
		if (e.getActionCommand() == "USCV") { //$NON-NLS-1$
			float n = view
					.getFloatInput(
							"Enter coefficient of variation of the US elements.", "" + getModel().getUSCV()); //$NON-NLS-1$ //$NON-NLS-2$
			// Check if 'Cancel' was pressed
			if (n != -1) {
				getModel().setUSCV(n);
			}
		}
		
		if (e.getActionCommand() == "Set Size") { //$NON-NLS-1$
			int n = view
					.getIntInput(
							"Enter the number of elements per time-curve.", ""+getModel().getSetSize()); //$NON-NLS-1$ //$NON-NLS-2$
			// Check if 'Cancel' was pressed
			if (n != -1) {
				getModel().setSetSize(n);
			}
		}

		if (e.getActionCommand() == "CSScalar") { //$NON-NLS-1$
			float n = view
					.getFloatInput(
							"Enter the scalar multiplier of CS element curves.", "" + getModel().getCSSCalar()); //$NON-NLS-1$ //$NON-NLS-2$
			// Check if 'Cancel' was pressed
			if (n != -1) {
				getModel().setCSSCalar(n);
			}
		}
		if (e.getActionCommand() == "Persistence") { //$NON-NLS-1$
			int n = view
					.getIntInput(
							"Enter the persistence (sec) of the US.", "" + getModel().getPersistence()); //$NON-NLS-1$ //$NON-NLS-2$
			// Check if 'Cancel' was pressed
			if (n != -1) {
				getModel().setPersistence(n);
			}
		}

		// The user chooses to change the default value of number of
		// combinations
		if (e.getActionCommand() == "VarDistCombinations") { //$NON-NLS-1$
			int n = view
					.getIntInput(
							Messages.getString("SimController.randomMessage"), "" + getModel().getVariableCombinationNo()); //$NON-NLS-1$ //$NON-NLS-2$
			// Check if 'Cancel' was pressed
			if (n != -1) {
				getModel().setVariableCombinationNo(n);
			}
		}

        //Update max rpm
        if (e.getActionCommand() == "rpm") { //$NON-NLS-1$
            int n = view
                    .getIntInput(
                            Messages.getString("SimController.rpmMessage"), "" + getModel().getResponsesPerMinute()); //$NON-NLS-1$ //$NON-NLS-2$
            // Check if 'Cancel' was pressed
            if (n != -1) {
                getModel().setResponsesPerMinute(n);
            }
        }

		// The user chooses to change the default timestep size
		if (e.getActionCommand() == "timestep") { //$NON-NLS-1$
			Float n = view
					.getFloatInput(
							Messages.getString("SimController.timeStepMessage"), "" + getModel().getTimestepSize()); //$NON-NLS-1$ //$NON-NLS-2$
			// Check if 'Cancel' was pressed
			if (n > 0) {
				getModel().setTimestepSize(n);
				view.updatePhasesColumnsWidth();
			}
		}

		// The user chooses to change the default value of response threshold
		if (e.getActionCommand() == "threshold") { //$NON-NLS-1$
			if (view.isSetResponse()) {
				float n = view
						.getFloatInput(
								Messages.getString("SimController.responseMessage"), "" + getModel().getThreshold()); //$NON-NLS-1$ //$NON-NLS-2$
				// Check if 'Cancel' was pressed
				if (n != -1) {
					getModel().setThreshold(n);
				}
			}
			getModel().setShowResponse(view.isSetResponse());
		}

		// The user chooses to select/deselect setting the US across phases
		if (e.getActionCommand() == "SetUSAcrossPhases") { //$NON-NLS-1$
			view.setIsUSAcrossPhases(!(view.isUSAcrossPhases()));
			view.setStatusComponent(view.isUSAcrossPhases(),
					"SetUSAcrossPhases"); //$NON-NLS-1$

			if (view.isUSAcrossPhases())
				view.addUSPhases();
			else
				view.removeUSPhases();

			if (!view.isUSAcrossPhases()
					|| (view.isUSAcrossPhases() && getModel().getPhaseNo() == 1))
				view.getUSValuesTable().setAutoResizeMode(
						JTable.AUTO_RESIZE_ALL_COLUMNS);
			else {
				view.getUSValuesTable().setAutoResizeMode(
						JTable.AUTO_RESIZE_OFF);
				view.updateUSValuesColumnsWidth();
			}
		}

		// The user chooses to select/deselect setting the US across phases
		if (e.getActionCommand() == "SetContextAcrossPhases") { //$NON-NLS-1$
			getModel().setContextAcrossPhase(view.isOmegaAcrossPhases());
			view.toggleContext(view.isUseContext());
			getModel().setUseContext(view.isUseContext());
		}

		if (e.getActionCommand() == "SingleContext") { //$NON-NLS-1$
			getModel().setContextAcrossPhase(false);
			view.toggleContext(view.isUseContext());
			getModel().setUseContext(view.isUseContext());
			/*float n = view
					.getFloatInput(
							"Input context R alpha", "" + view.getContextAlphaR()); //$NON-NLS-1$ //$NON-NLS-2$
			float n2 = view
					.getFloatInput(
							"Input context N alpha", "" + view.getContextAlphaN()); //$NON-NLS-1$ //$NON-NLS-2$
			float n3 = view
					.getFloatInput(
							"Input context Salience", "" + view.getContextSalience()); //$NON-NLS-1$ //$NON-NLS-2$
			if (n > 0 && n2 > 0 && n3 > 0) {
				view.toggleContext(view.isUseContext());
				getModel().setUseContext(view.isUseContext());
				view.setOmegaSalience(n,n2,n3);
			}*/
		}

		// The user chooses to select/deselect the compound values of CS
		if (e.getActionCommand() == "SetCompound") { //$NON-NLS-1$
			view.setIsSetCompound(!(view.isSetCompound()));
			view.setStatusComponent(view.isSetCompound(), "SetCompound"); //$NON-NLS-1$
            if(view.isSetCompound()) {
                view.setIsSetConfiguralCompounds(true);
                view.setStatusComponent(view.isSetConfiguralCompounds(),
                        "SetConfiguralCompounds");
            }
            model.setIsCompound(view.isSetCompound());
		}

		// Added by Alberto Fern�ndez August-2011
		// The user chooses to select/deselect the configural compounds option
		if (e.getActionCommand() == "SetConfiguralCompounds") { //$NON-NLS-1$
			view.setIsSetConfiguralCompounds(!(view.isSetConfiguralCompounds()));
			view.setStatusComponent(view.isSetConfiguralCompounds(),
					"SetConfiguralCompounds"); //$NON-NLS-1$
		}

		// The user chooses to add a phase
		if (e.getActionCommand() == "addPhase") { //$NON-NLS-1$
			getModel().setPhaseNo(getModel().getPhaseNo() + 1);
			view.addPhase();
		}

		// The user chooses to remove the last phase
		if (e.getActionCommand() == "removePhase") { //$NON-NLS-1$
			if (getModel().getPhaseNo() > 1) {
				getModel().setPhaseNo(getModel().getPhaseNo() - 1);
				view.removePhase();
			}
		}

		// The user chooses to add a group
		if (e.getActionCommand() == "addGroup") { //$NON-NLS-1$
			getModel().setGroupNo(getModel().getGroupNo() + 1);
			view.addGroup();
		}

		// The user chooses to remove the last group
		if (e.getActionCommand() == "removeGroup") { //$NON-NLS-1$
			if (getModel().getGroupNo() > 1) {
				getModel().setGroupNo(getModel().getGroupNo() - 1);
				view.removeGroup();
			}
		}

		// The user chooses to read the guide
		if (e.getActionCommand() == "Guide") { //$NON-NLS-1$
			try {
				java.awt.Desktop
						.getDesktop()
						.browse(new URI("https://www.cal-r.org")); //$NON-NLS-1$
				// SimGuide simGuide = new SimGuide("User's Guide");
				// simGuide.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
				// simGuide.pack();
				// simGuide.setVisible(true);
			} catch (Exception ex) {
			}
		}

		// Update the context switch.
		if (e.getActionCommand() == "SetContext") { //$NON-NLS-1$
			view.toggleContext(view.isUseContext());
			getModel().setUseContext(view.isUseContext());
		}

		// The user chooses to read the information about the Simulator
		if (e.getActionCommand() == "About") { //$NON-NLS-1$
			view.showAboutLogo("/Extras/splash.png");//("/Extras/logo5-final.jpg");E.Mondragon 30 Sept 2011 //$NON-NLS-1$
		}

		// The user presses the 'Set Variables' button which will set initial
		// values for the variables.
		if (e.getActionCommand() == "setVariables") { //$NON-NLS-1$
			doSetVariables(false);

    		model.initializeIntensities();
            view.errorCheckList.setEnabled(true);
            view.errorTrialCheckList.setEnabled(true);
		}
		
		if (e.getActionCommand() == "setG") {
			ImageIcon icon = createImageIcon("/simulator/extras/icon_32.png", ""); //$NON-NLS-1$ //$NON-NLS-2$
			seDialog = new SharedElementsDialog("Shared Elements Dialog",null,icon,model.getGroups().values());
		}

		// The user presses the 'Clear All' button which clears all the values
		// from the table.
		if (e.getActionCommand() == "clearAll") {

            view.errorCheckList.setEnabled(true);
            view.errorTrialCheckList.setEnabled(true);
			for (SimGroup g : model.getGroups().values()) {g.closeDBs();}
			clearModel(getModel().getGroupNo(), getModel().getPhaseNo(),
					getModel().getCombinationNo());
			getModel().setUseContext(view.isUseContext());
			VariableDistribution.newRandomSeed();
			
		}

		// The user presses the 'Run' button which updates the model with the
		// values and runs the algorithm
		if (e.getActionCommand() == "run") { //$NON-NLS-1$
            doSetVariables(true);
            if (model.isExternalSave() && model.getDirectory() != null) {
            	/*Object[] options = {"Ok",
                        "Cancel"};
			    int n = JOptionPane.showOptionDialog(view,
			        "Delete temp files for last run?","Caution",
			        JOptionPane.YES_NO_CANCEL_OPTION,
			        JOptionPane.QUESTION_MESSAGE,
			        null,
			        options,
			        options[1]);
            	if (n == 0) */
            	//clearSave();
            }

            model.setIsErrors(view.errorTrialCheckList.isSelected(),view.errorCheckList.isSelected());
            view.errorCheckList.setEnabled(false);
            view.errorTrialCheckList.setEnabled(false);
            view.removeTabs();
			// check again the model and also the values

			
			if (checkModelTable(true)) {
				// update the CS and US tables following the model
				view.getCSValuesTableModel().setValuesTable(false);
				view.getCSVariableTableModel().setValuesTable(false);
				
				view.getUSValuesTableModel().setValuesTable(false,
						view.isUSAcrossPhases());
				view.getCSSalienceTableModel().setValuesTable(false);
				view.getUSSalienceTableModel().setValuesTable(false,view.isUSAcrossPhases());
				if (!view.isUSAcrossPhases()
						|| (view.isUSAcrossPhases() && getModel().getPhaseNo() == 1))
					view.getUSValuesTable().setAutoResizeMode(
							JTable.AUTO_RESIZE_ALL_COLUMNS);
				else {
					view.getUSValuesTable().setAutoResizeMode(
							JTable.AUTO_RESIZE_OFF);
					view.updateUSValuesColumnsWidth();
				}

				// check the CS and US values
				if (checkCSValuesTable() && checkCSVariableTable() && checkUSValuesTable()
						&& checkCSSalienceTable() && checkOtherValuesTable() && checkUSSalienceTable()) {
					view.getGlassPane().setCursor(
							Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					view.getGlassPane().setVisible(true);

					// Update values from the CS view to the model
					AbstractTableModel cstm = view.getCSValuesTableModel();
					for (int i = 0; i < cstm.getRowCount(); i++)
						for (int j = 1; j < cstm.getColumnCount(); j++) {
							getModel().updateValues(
									(String) cstm.getValueAt(i, 0), j,
									(String) cstm.getValueAt(i, j));
						}
					
					AbstractTableModel csvtm = view.getCSVariableTableModel();
					for (int i = 0; i < csvtm.getRowCount(); i++)
						for (int j = 1; j < csvtm.getColumnCount(); j++) {
							getModel().updateValues(
									(String) csvtm.getValueAt(i, 0), j,
									(String) csvtm.getValueAt(i, j));
						}
					// Update values from the US view to the model
					AbstractTableModel ustm = view.getUSValuesTableModel();
					for (int i = 0; i < ustm.getRowCount(); i++)
						for (int j = 1; j <= getModel().getPhaseNo(); j++) {
							if (!view.isUSAcrossPhases()) {
								getModel().updateValues(
										(String) ustm.getValueAt(i, 0), j,
										(String) ustm.getValueAt(i, 1));
							} else {
								getModel().updateValues(
										(String) ustm.getValueAt(i, 0), j,
										(String) ustm.getValueAt(i, j));
							}
						}
					AbstractTableModel csaltm = view.getCSSalienceTableModel();
					for (int i = 0; i < csaltm.getRowCount(); i++)
						for (int j = 1; j < csaltm.getColumnCount(); j++) {
							getModel().updateValues(
									(String) csaltm.getValueAt(i, 0), j,
									(String) csaltm.getValueAt(i, j));
						}
					
					AbstractTableModel usstm = view.getUSSalienceTableModel();
					for (int i = 0; i < usstm.getRowCount(); i++)
						for (int j = 1; j <= getModel().getPhaseNo(); j++) {
							if (!view.isUSAcrossPhases()) {
								getModel().updateValues(
										(String) usstm.getValueAt(i, 0), j,
										(String) usstm.getValueAt(i, 1));
							} else {
								getModel().updateValues(
										(String) usstm.getValueAt(i, 0), j,
										(String) usstm.getValueAt(i, j));
							}
						}
					
					// Update other values (gamma, delta, omega)
					// Update values from the US view to the model
					AbstractTableModel otm = view.getOtherValuesTableModel();
					for (int i = 0; i < otm.getRowCount(); i++)
						for (int j = 1; j <= getModel().getPhaseNo(); j++) {
							getModel().updateValues(
									
									(String) otm.getValueAt(i, 0) + "", j,
									(String) otm.getValueAt(i, 1) + "");
							}

					// update CS and US values on all the groups
					getModel().updateValuesOnGroups();
					// Update the context switch.
					getModel().setUseContext(view.isUseContext());
					getModel()
							.setContextAcrossPhase(view.isOmegaAcrossPhases());
					getModel().setIsCompound(view.isUseCompound());
					// execute the algorithm
					/*
					 * model.startCalculations();
					 * 
					 * view.setStatusComponent(true, "dispGraph");
					 * view.setOutput
					 * (model.textOutput(view.getIsSetCompound()));
					 * view.getGlassPane
					 * ().setCursor(Cursor.getPredefinedCursor(Cursor
					 * .DEFAULT_CURSOR)); view.getGlassPane().setVisible(false);
					 * view.setStatusComponent(true, "Export");
					 * view.setStatusComponent(true, "Save");
					 */

						for (SimGroup sg : getModel().getGroups().values()) {
							if (seDialog != null) {
								sg.setSharedElements(seDialog.getGroupData(sg.getNameOfGroup()));
							}
							for (Stimulus s : sg.getCuesMap().values()) {
								s.setPresenceMean(presenceMean);
							}
						}
					
					getModel().setCSC(cscMode);
					task = new RunTask();
					task.addPropertyChangeListener(this);
					task.execute();

					AbstractTableModel tm = view.getOtherValuesTableModel();
					if ((float) Float.valueOf((String)tm.getValueAt(2,1))*model.getSetSize() < 1) {
						for (SimGroup g : model.getGroups().values()) {

							ArrayList<String> common = new ArrayList<>();
							for (String s : g.getCuesMap().keySet()) {
								if (s.charAt(0) == 'c') {common.add(s);}
							}

							for (Stimulus s : g.getCuesMap().values()) {
								s.getCommonMap().clear();
							}
							for (String s : common) {
								g.getCuesMap().remove(s);
								if (model.getListAllCues().contains(s)) {model.getListAllCues().remove(s);}
							}
						}
					}

		    		
				}
			}

			model.setIntensities(tempIntensities);
			
		}

		// The user presses the 'Display Graph' button which displays the graphs
		// with the current results
		if (e.getActionCommand() == "dispGraph") { //$NON-NLS-1$
			initializeColourMap();
			//for (SimGroup g: getModel().getGroups().values()) {
				//saveVariableSalience(g.getCuesMap().get("A"),g.getNameOfGroup());
			//}
			// SimGraph.clearDefaults();
			view.getGlassPane().setCursor(
					Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			view.getGlassPane().setVisible(true);

			ImageIcon icon = createImageIcon("/simulator/extras/icon_32.png", ""); //$NON-NLS-1$ //$NON-NLS-2$
			ArrayList<GraphLine> graphLines = new ArrayList<GraphLine>();
			for (int i = 0; i < getModel().getPhaseNo(); i++) {
                Object[] messageArguments = {
                        (i + 1)
                };int count = 0;
				if(vGraph) {
					GraphLine gl = (GraphLine) displayAGraph("W/Episodes Graph - Phase"  + (i+1),WTrialGraph.class,i,-1, null, count, icon,null,this); count++;
					graphLines.add(gl);
				}
				if(waTGraph) {
					GraphLine gl = (GraphLine) displayAGraph("V/Trial Graph - Phase"  + (i+1),VTrialGraph.class,i,-1, null, count, icon,null,this); count++;
					graphLines.add(gl);
				}
				if (trialResponse) {
					GraphLine gl = (GraphLine) displayAGraph("Response/Trial Graph - Phase " + (i+1),RTrialGraph.class,i,-1, null, count, icon,null,this); count++;
					graphLines.add(gl);
				}
				if (contextGraph) {
					GraphLine gl = (GraphLine) displayAGraph("Context Graph - Phase " + (i+1),ContextGraph.class,i,-1, null, count, icon,null,this); count++;
					graphLines.add(gl);
				}
				if (componentPrediction) {displayAGraph("Component Strength Graph",ComponentPredictionGraph.class,i,-1, null, count, icon,null,this); count++;
				}
				if (componentDelta) {displayAGraph("Element Delta Graph",ComponentDeltaGraph.class,i,-1, null, count, icon,null,this); count++;
				}
				if (isAsymptote) {displayAGraph("Element Asymptote Graph",ComponentAsymptotes.class,i,-1, null, count, icon,null,this); count++;
				}
				
				
						if (trialPrediction) {
							GraphLine gl = (GraphLine) displayAGraph("W/Time Graph - Phase " + (i+1) ,WTimeGraph.class,i,0, new SimGroup("", 0, 0, new SimModel()), count, icon,null,this); count++;
							graphLines.add(gl);
						}
						if (watGraph) {
							GraphLine gl = (GraphLine) displayAGraph("V/Time Graph - Phase " + (i+1) ,VTimeGraph.class,i,0, new SimGroup("", 0, 0, new SimModel()), count, icon,null,this); count++;
							graphLines.add(gl);
						}
						if (responseGraph) {
							GraphLine gl = (GraphLine) displayAGraph("Response/Time Graph - Phase " + (i+1) ,RTimeGraph.class,i,0, new SimGroup("", 0, 0, new SimModel()), count, icon,null,this); count++;
							graphLines.add(gl);
							}
					
				if (componentActivation) {
					GraphLine gl = (GraphLine)displayAGraph("Time-element Activation/Time Graph - Phase " + (i+1),ComponentActivationGraph.class,i,-1, null, count, icon,null,this); count++;
					graphLines.add(gl);
				}
				if (errorTrial) {
					displayAGraph("Stimulus Discrete Error/Trial Graph - Phase " + (i+1),ErrorGraph.class,i,-1, null, count, icon,null,this); count++;
				
				}
				if (errorGraph) {
					GraphLine gl = (GraphLine)displayAGraph("Element Discrete Error/Time Graph - Phase " + (i+1),ErrorTimeGraph.class,i,-1, null, count, icon,null,this); count++;
					graphLines.add(gl);
				}
				if (saliences) {
					GraphLine gl = (GraphLine)displayAGraph("Alphas/Trial Graph - Phase " + (i+1),SalienceGraph.class,i,-1, null, count, icon,null,this); count++;
					graphLines.add(gl);
				}
				if (componentPredictions) {
					JFrame componentPreds = new ComponentPredictionGraph("Time Predictions - Phase " + (i+1),model,i,this);
					componentPreds.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					componentPreds.pack();
					componentPreds.setLocation(20*count + i * 20, 20*count + i * 20);
					componentPreds.setVisible(true);
					componentPreds.setIconImage(icon.getImage());
				}
				}
			if (animationGraph)for (SimGroup sg: getModel().getGroups().values()) {displayAGraph("Network Representation - " + sg.getNameOfGroup(),AnimationGraph.class,0,-1, sg, 0, icon,graphLines,this);}
			view.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));view.getGlassPane().setVisible(false);
		}

	}
	
	public void initializeColourMap() {
		
		float mostStimuli = 0;
		float colourNumber = 0;
		ArrayList<String> groupNames = new ArrayList();
		ArrayList<String> stimuliNames = new ArrayList();
		ArrayList<String> responseTrialNames = new ArrayList();
		for (SimGroup g : getModel().getGroups().values()) {
			groupNames.add(g.getNameOfGroup());
			for (String stimName : g.getCuesMap().keySet()) {
				if (!stimuliNames.contains(stimName)) {stimuliNames.add(stimName);}
			}
			mostStimuli = Math.max(mostStimuli,g.getCuesMap().size());
			for (SimPhase sp : g.getPhases()) {
				for (String s : sp.getProbeIndexes().keySet()) {
					String tempString = USNames.isReinforced(s) ? s.substring(1,s.length()-1) : s.substring(1,s.length());
					if (!responseTrialNames.contains(s)) {responseTrialNames.add(tempString);}
				}
			}
		}
		
		
		colourNumber = getModel().getGroupNo()*stimuliNames.size()*stimuliNames.size();
		ArrayList<Integer> r = new ArrayList();
		ArrayList<Integer> g = new ArrayList();
		ArrayList<Integer> b = new ArrayList();
		int c1 = 0;
		for (String s1 : groupNames) {

			TreeMap<String,TreeMap<String,Color>> innerMap1 = new TreeMap();
			for (String s2 : stimuliNames) {

				TreeMap<String,Color> innerMap2 = new TreeMap<String,Color>();
				for (String s3 : stimuliNames) {

					Color color = null;
					boolean add = false;
					while (!add) {

						//System.out.println("loop simctrl");
						Random random = new Random();
						float hue = random.nextFloat();
						// Saturation between 0.1 and 0.3
						float saturation = (random.nextInt(2000) + 500) / 2500f;
						float luminance = 0.9f;
						color = Color.getHSBColor(hue, saturation, luminance);
						add = true;
						for (int i = 0; i < r.size(); i++) {
							
							float error1 = (float)Math.pow((r.get(i) - color.getRed())/255d,2);
							float error2 =  (float)Math.pow((g.get(i) - color.getGreen())/255d,2);
							float error3 =  (float)Math.pow((b.get(i) - color.getBlue())/255d,2);
							if ((error1+error2+error3)/(3d) > 0.9) {add = false;}
						}
					}
					innerMap2.put(s3,color);
					r.add(color.getRed());
					g.add(color.getBlue());
					b.add(color.getGreen());
					c1++;
				}

				innerMap1.put(s2, innerMap2);
			}

			colourMap.put(s1, innerMap1);
		}
		r = new ArrayList();
		g = new ArrayList();
		b = new ArrayList();
		int colourNumber2 = getModel().getGroupNo()*responseTrialNames.size();
		c1 = 0;
		for (String s1 : groupNames) {

			TreeMap<String,Color> innerMap1 = new TreeMap();
			for (String s2 : responseTrialNames) {
				Color color = null;
				boolean add = false;
				while (!add) {

					//System.out.println("loop simctrl");
					Random random = new Random();
					float hue = random.nextFloat();
					// Saturation between 0.1 and 0.3
					float saturation = (random.nextInt(2000) + 500) / 2500f;
					float luminance = 0.9f;
					color = Color.getHSBColor(hue, saturation, luminance);
					add = true;
					for (int i = 0; i < r.size(); i++) {
						
						float error1 = (float)Math.pow((r.get(i) - color.getRed())/255d,2);
						float error2 =  (float)Math.pow((g.get(i) - color.getGreen())/255d,2);
						float error3 =  (float)Math.pow((b.get(i) - color.getBlue())/255d,2);
						if ((error1+error2+error3)/(3d) > 0.9) {add = false;}
					}
				}
				r.add(color.getRed());
				g.add(color.getBlue());
				b.add(color.getGreen());
					c1++;

					innerMap1.put(s2, color);
			}

			responseColourMap.put(s1, innerMap1);
		}		
	}
	
	private Object displayAGraph(String title,Class theClass,int phase,int trial, SimGroup sg, int count, ImageIcon icon,ArrayList<GraphLine> gl, SimController controller) {

		Object instanceOfMyClass = null;
		Class[] types = {String.class, SimModel.class, int.class, int.class, SimController.class};
		Class[] types2 =  {String.class, SimModel.class, SimGroup.class, int.class, int.class,SimController.class};
		Constructor constructor = null;
		try {
			constructor = theClass.getConstructor(sg != null && gl == null ? types2: types);
		} catch (NoSuchMethodException e) {System.out.println(e + " in SimController ");}
		  catch (SecurityException e) {System.out.println(e + " in SimController ");}
		Object[] parameters = {title,getModel(),phase,trial,controller};
		Object[] parameters2 = {title,getModel(),sg,phase,trial,controller};
		try {instanceOfMyClass = constructor.newInstance(sg != null && theClass != AnimationGraph.class ? parameters2: parameters);}
		
		  catch (InstantiationException e) {System.out.println(e + " in SimController ");}
		  catch (IllegalAccessException e) {System.out.println(e + " in SimController ");}
		  catch (IllegalArgumentException e) {System.out.println(e + " in SimController ");}
		  catch (InvocationTargetException e) {System.out.println(e + " in SimController " + e.getCause() +  " " + theClass.getName());}
		if (instanceOfMyClass != null) {
			if (theClass == AnimationGraph.class) {
				((AnimationGraph)instanceOfMyClass).setGroup(sg);
				((AnimationGraph)instanceOfMyClass).setGraphLines(gl);
				((AnimationGraph)instanceOfMyClass).setVisible(true);
				view.swapPanels(((AnimationGraph)instanceOfMyClass),sg.getNameOfGroup());
			} else {
				((JFrame)instanceOfMyClass).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				((JFrame)instanceOfMyClass).pack();
				((JFrame)instanceOfMyClass).setLocation(20*count + phase * 20, 20*count + phase * 20);
				((JFrame)instanceOfMyClass).setVisible(true);
				((JFrame)instanceOfMyClass).setIconImage(icon.getImage());
			}
		}
		return instanceOfMyClass;
	}
	
	public Color getColour(String groupName, String s1, String s2) {
		if (colourMap.containsKey(groupName)) {
			if (colourMap.get(groupName).containsKey(s1)) {
				if (colourMap.get(groupName).get(s1).containsKey(s2)) {
					return colourMap.get(groupName).get(s1).get(s2);
				} else {
					//System.out.println("no stim s2 in controller " + s2 + " with group " + groupName + " and prev stim " + s1);
					return null;
					}
			} else {
				//System.out.println("no stim s1 in controller " + s1 + " with group " + groupName);
				return null;}
			
		} else {
			//System.out.println("no groupName in controller " + groupName);
			return null;}
	}
	
	public Color getResponseColour(String groupName, String s1) {
		if (responseColourMap.containsKey(groupName) && responseColourMap.get(groupName).containsKey(s1)) {
		return responseColourMap.get(groupName).get(s1);
		}
		else {System.out.println("response colour no "+ groupName + " and string " + s1);return null;}
	}

	/**
	 * 
	 */
	private void doSetVariables(boolean run) {
		view.setStatusComponent(false, "dispGraph"); //$NON-NLS-1$

		view.intensityButton.setEnabled(true);
		view.setOutput("");
		view.setStatusComponent(false, "Export"); //$NON-NLS-1$
		view.setStatusComponent(true, "Save"); //$NON-NLS-1$
		if (checkModelTable(run)) {
			view.setStatusComponent(true, "run"); //$NON-NLS-1$
			view.getCSValuesTableModel().setValuesTable(false);
			view.getCSVariableTableModel().setValuesTable(false);
			view.getUSValuesTableModel().setValuesTable(false,
					view.isUSAcrossPhases());
			view.getCSSalienceTableModel().setValuesTable(false);
			view.getUSSalienceTableModel().setValuesTable(false,
					view.isUSAcrossPhases());
			view.getOtherValuesTableModel().setValuesTable(false,
					view.isOmegaAcrossPhases());
			if (!view.isUSAcrossPhases()
					|| (view.isUSAcrossPhases() && getModel().getPhaseNo() == 1)) {
				view.getUSValuesTable().setAutoResizeMode(
						JTable.AUTO_RESIZE_ALL_COLUMNS);
			} else {
				view.getUSValuesTable().setAutoResizeMode(
						JTable.AUTO_RESIZE_OFF);
				view.updateUSValuesColumnsWidth();
			}
		}
		
	}

	/**
	 * 
	 */
	private void doExport() {
		// Alberto Fernandez Sept-2011
					// new SimExport(view, model);
					String[] dir = new String[1];
					dir[0] = lastDirectory;
					// Choose a file to store the values.
					JFileChooser fc = new JFileChooser();
					fc.setCurrentDirectory(new File(dir[0])); // AF Sept-2011
					ExampleFileFilter filter = new ExampleFileFilter();
					filter.addExtension("xlsx");
					filter.setDescription("Spreadsheet");
					fc.setFileFilter(filter);
					int returnVal = fc.showSaveDialog(view);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						String path = fc.getSelectedFile().getParent();
						dir[0] = path; // Alberto Fern�ndez Sept-2011
						String name = fc.getSelectedFile().getName();
						if (!name.contains(".xlsx"))
							name += ".xlsx"; // Alberto Fernandez: changed from xls ->
												// xlsx
						boolean okToContinue = true;
						File file = new File(path, name);
						if (file.exists()) {
							int response = JOptionPane.showConfirmDialog(null,
									"Overwrite existing file?", "Confirm Overwrite",
									JOptionPane.OK_CANCEL_OPTION,
									JOptionPane.QUESTION_MESSAGE);
							if (response == JOptionPane.CANCEL_OPTION) {
								okToContinue = false;
							}

						}
						if (okToContinue) {
							exporter = new SimExport(this,view, getModel(), name, file);
							exporterTask = new ExportTask();
							exporterTask.addPropertyChangeListener(this);
							exporterTask.execute();
						}
					}
					lastDirectory = dir[0];
		
	}
	
	public void doExport(TreeMap<String,TreeMap<Double,Double>> data, boolean time) {
		// Alberto Fernandez Sept-2011
					// new SimExport(view, model);
					String[] dir = new String[1];
					dir[0] = lastDirectory;
					// Choose a file to store the values.
					JFileChooser fc = new JFileChooser();
					fc.setCurrentDirectory(new File(dir[0])); // AF Sept-2011
					ExampleFileFilter filter = new ExampleFileFilter();
					filter.addExtension("xlsx");
					filter.setDescription("Spreadsheet");
					fc.setFileFilter(filter);
					int returnVal = fc.showSaveDialog(view);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						String path = fc.getSelectedFile().getParent();
						dir[0] = path; // Alberto Fern�ndez Sept-2011
						String name = fc.getSelectedFile().getName();
						if (!name.contains(".xlsx"))
							name += ".xlsx"; // Alberto Fernandez: changed from xls ->
												// xlsx
						boolean okToContinue = true;
						File file = new File(path, name);
						if (file.exists()) {
							int response = JOptionPane.showConfirmDialog(null,
									"Overwrite existing file?", "Confirm Overwrite",
									JOptionPane.OK_CANCEL_OPTION,
									JOptionPane.QUESTION_MESSAGE);
							if (response == JOptionPane.CANCEL_OPTION) {
								okToContinue = false;
							}

						}
						if (okToContinue) {
							exporter2 = new SimGraphExport(this,view, getModel(), name, file,data,time);
							exporter2.makeExport();
							exporterTask2 = new ExportTask();
							exporterTask2.addPropertyChangeListener(this);
							exporterTask2.execute();
						}
					}
					lastDirectory2 = dir[0];
		
	}

	/**
	 * Returns true if the ValuesTable has been checked successfully
	 */
	private boolean checkCSValuesTable() {
		boolean cont = true;
		AbstractTableModel tm = view.getCSValuesTableModel();

		// First check that table contains legal values.
		for (int row = 0; row < tm.getRowCount(); row++) {
			// Checking values
			String tmp = (String) tm.getValueAt(row, 1);

			// NOT EMPTY ALPHA FIELD
			if (tmp.length() > 0) {
				try {
					new Float(tmp);
				} catch (Exception ex) {
					cont = false;
					view.showError(Messages
							.getString("SimController.entryWrongMessage")); //$NON-NLS-1$
					break;
				}
			}
			// EMPTY ALPHA FIELD
			else {
				cont = false;
				view.showError(Messages
						.getString("SimController.alphaEmptyMessage")); //$NON-NLS-1$
				break;
			}
		}
		return cont;
	}
	
	private boolean checkCSSalienceTable() {
		boolean cont = true;
		AbstractTableModel tm = view.getCSSalienceTableModel();

		// First check that table contains legal values.
		for (int row = 0; row < tm.getRowCount(); row++) {
			// Checking values
			String tmp = (String) tm.getValueAt(row, 1);

			// NOT EMPTY ALPHA FIELD
			if (tmp.length() > 0) {
				try {
					new Float(tmp);
				} catch (Exception ex) {
					cont = false;
					view.showError(Messages
							.getString("SimController.entryWrongMessage")); //$NON-NLS-1$
					break;
				}
			}
			// EMPTY ALPHA FIELD
			else {
				cont = false;
				view.showError(Messages
						.getString("SimController.alphaEmptyMessage")); //$NON-NLS-1$
				break;
			}
		}
		return cont;
	}
	private boolean checkCSVariableTable() {
		boolean cont = true;
		AbstractTableModel tm = view.getCSVariableTableModel();

		// First check that table contains legal values.
		for (int row = 0; row < tm.getRowCount(); row++) {
			// Checking values
			String tmp = (String) tm.getValueAt(row, 1);

			// NOT EMPTY ALPHA FIELD
			if (tmp.length() > 0) {
				try {
					new Float(tmp);
				} catch (Exception ex) {
					cont = false;
					view.showError(Messages
							.getString("SimController.entryWrongMessage")); //$NON-NLS-1$
					break;
				}
			}
			// EMPTY ALPHA FIELD
			else {
				cont = false;
				view.showError(Messages
						.getString("SimController.alphaEmptyMessage")); //$NON-NLS-1$
				break;
			}
		}
		return cont;
	}

	/**
	 * Returns true if the ModelTable has been checked successfully
	 */
	private boolean checkModelTable(boolean run) {
		boolean cont = true;
		// Get the experiment's model table so we can process the information.
		AbstractTableModel tm = view.getPhasesTableModel();

		// First check that table contains legal values.
		for (int row = 0; row < getModel().getGroupNo(); row++) {
			// Checking group names
			if (((String) tm.getValueAt(row, 0)).length() == 0) {
				cont = false;
				view.showError(Messages
						.format("SimController.groupNameInRow", row + 1)); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			}
			// Checking phases values
			for (int col = 1; col < getModel().getPhaseNo() + 1; col++) {
				String tmp = (String) tm.getValueAt(row, 5 * col - 4);
				if (tmp.length() == 0) {
					view.showAbout(Messages
							.format("SimController.phaseWarningOne", col, (String) tm.getValueAt(row, 0))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					tm.setValueAt("0", row, 5 * col - 4); //$NON-NLS-1$
				}

				// Checking onsets are set and in start state. J Gray
				TimingConfiguration tmpOnset = (TimingConfiguration) tm
						.getValueAt(row, 5* col - 1);
				//tmpOnset.getDurations().setType(view.getDistributionType());
				//tmpOnset.getDurations().setGeo(view.getMeanType());
				tmpOnset.reset();
				tmpOnset.checkFilled(tmp, view.timingPerTrial());
				if (tmpOnset.hasZeroDurations()) {
					view.showAbout(Messages
							.format("SimController.durationWarning", col, (String) tm.getValueAt(row, 0))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				if (!tmpOnset.isConfigured()) {
					view.showAbout(Messages
							.format("SimController.timingWarningOne", col, (String) tm.getValueAt(row, 0))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					cont = false;
				}

				ITIConfig iti = (ITIConfig) tm.getValueAt(row, 5 * col);
				//iti.setType(view.getDistributionType());
				//iti.setGeo(view.getMeanType());
			}
		}

		if (cont) {

			Runtime r = Runtime.getRuntime();
			int mb = 1024*1024;
			getModel().reinitialize();

			for (int i = 0; i < getModel().getPhaseNo(); i++) {
				getModel().addPhaseToSessions();
			}
			ArrayList<String> usNames = new ArrayList();
			for (int row = 0; row < getModel().getGroupNo(); row++) {
				ArrayList<String> groupUsNames = new ArrayList();
				String gName = (String) tm.getValueAt(row, 0); // first column
																// is the
																// group's name
				// Create a new group for every row of the table.
				int trialsThusFar = 0;

				ArrayList<String> stimuliThusFar = new ArrayList<String>();
				ArrayList<String> cssThusFar = new ArrayList<String>();
				ArrayList<String> ctxsThusFar = new ArrayList<String> ();

				for (int c = 1; c < getModel().getPhaseNo() + 1; c++) {
					
					int trialsThisPhase = 0;
					String trialString = (String) tm.getValueAt(row, c * 5 - 4);
					ContextConfig cfg = (ContextConfig) tm.getValueAt(row, c * 5 - 3);
					if (!ctxsThusFar.contains(cfg.getSymbol())) {ctxsThusFar.add(cfg.getSymbol());}
					trialString = trialString.replaceAll("\\s", "");
					String sep = "/";
					String[] listedStimuli = trialString.toUpperCase().split(sep);
					int noStimuli = listedStimuli.length;
					String compound = "";
					for (int i = 0; i < noStimuli; i++) {
						String selStim = listedStimuli[i], repStim = "";
						compound = "";
						for (int n = 0; n < selStim.length(); n++) {
								
									char selChar = selStim.charAt(n);
									if (Character.isDigit(selChar)) {
										repStim += selChar;
									}else if (selChar == '^') {
									
										
									}
									else {
										if (USNames.isUS(selChar+"") && !"-".equals(selChar + "")) {
											if (!usNames.contains(selChar+"")) {
												usNames.add(selChar+"");
											}
											if (!groupUsNames.contains(selChar+"")) {
												groupUsNames.add(selChar+"");
												if (!stimuliThusFar.contains(selChar+"")) stimuliThusFar.add(selChar+"");
											}
										}
										else if (Character.isLetter(selChar)) {
											compound += selChar;
											
											if (!Context.isContext(selChar+"") && !cssThusFar.contains(selChar+"")) {cssThusFar.add(selChar+"");}
											//if (!stimuliThusFar.contains(compound)) {stimuliThusFar.add(compound);}
											if (!stimuliThusFar.contains(selChar + "")) {stimuliThusFar.add(selChar + "");}
										}
										
										
									}
						}
						if (repStim == "") {repStim = "1";}
						trialsThisPhase += Integer.parseInt(repStim);
						if (Integer.parseInt(repStim) == 0) {trialsThisPhase++;}
					}
					trialsThusFar += trialsThisPhase;//*((Integer)tm.getValueAt(row, c * 5 - 2));
				}
				SimGroup sg = new SimGroup(gName, getModel().getPhaseNo(),
						getModel().getCombinationNo(), getModel());
				sg.setMaximumMemory((long) Math.round((float)r.maxMemory()/(1000*(float)getModel().getGroupNo())));
				sg.setTotalTrials(trialsThusFar);
				int ctx = (getModel().isUseContext()) ? 1 : 0;
				int ctxNum = ctx;
				if (ctx == 1) ctxNum = (view.isOmegaAcrossPhases()) ? getModel().getPhaseNo() : 1;
				float stimuliNumber = cssThusFar.size()*(cssThusFar.size()-1f)/2f;
				sg.setTotalStimuli(Math.round(stimuliNumber) + stimuliThusFar.size() + (groupUsNames.size() > 0 ? 0 : 1) + ctxsThusFar.size());
				

				
				for (int c = 1; c < getModel().getPhaseNo() + 1; c++) {
					//getModel().addSessionToGroup(c-1, gName, (Integer) tm.getValueAt(row, c * 5 - 2));
					boolean isRandom = false;
					boolean success = false;
					//getModel().setContextAlphaR(view.getContextAlphaR());
					//getModel().setContextAlphaN(view.getContextAlphaN());
					//getModel().setContextSalience(view.getContextSalience());
					ContextConfig context = !getModel().isUseContext() ? ContextConfig.EMPTY
							: (ContextConfig) tm.getValueAt(row, c * 5 - 3);
					model.setContextAlphaN(context.getAlphaN());
					model.setContextAlphaR(context.getAlphaR());
					model.setContextSalience(context.getSalience());
					view.setContextAlphaN(context.getAlphaN());
					view.setContextAlphaR(context.getAlphaR());
					view.setContextSalience(context.getSalience());
					String trialString = (String) tm.getValueAt(row, c * 5 - 4);
					// Remove whitespace
					trialString = trialString.replaceAll("\\s", ""); //$NON-NLS-1$ //$NON-NLS-2$
					String sep = "/";//
					// Get whether the phase is random
					isRandom = ((Boolean) tm.getValueAt(row, c * 5 - 2))
							.booleanValue();
					success = sg
							.addPhase(trialString, isRandom, c, false,
									null,
									(TimingConfiguration) tm.getValueAt(row,
											c * 5 - 1), (ITIConfig) tm
											.getValueAt(row, c*5 ), context,vartheta);
					sg.addTrialString(trialString);
					if (!success) { // Modified Alberto Fern�ndez August-2011
									// Modified J Gray Dec-2011
						view.showError(Messages
								.format("SimController.phase", c)); //$NON-NLS-1$ //$NON-NLS-2$
						cont = false;
						break;
					}//
				}
				sg.initializeTrialArrays();
				// Modified Niklas Kokkola March 2016
				if (groupUsNames.size() == 0) {
					for (Stimulus stim : sg.getCuesMap().values()) {
						if (USNames.isUS(stim.getName())) {stim.setDisabled();}
					}
				}
				if (cont)
					getModel().addGroupIntoMap(gName, sg);
			}
			if (!run) {
				view.getUSValuesTableModel().setUSNames(usNames);
				view.getUSValuesTableModel().setUSValues();
				view.getUSSalienceTableModel().setUSNames(usNames);
				view.getUSSalienceTableModel().setUSValues();
			}
			getModel().addValuesIntoMap();
		}
		return cont;
	}

	/**
	 * Returns true if the OtherValuesTable has been checked successfully
	 */
	private boolean checkOtherValuesTable() {
		boolean cont = true;
		AbstractTableModel tm = view.getOtherValuesTableModel();
		float recency = (float) Float.valueOf((String)tm.getValueAt(0,1));
		float common = (float) Float.valueOf((String)tm.getValueAt(1,1));
		//float cscLike = (float) Float.valueOf((String)tm.getValueAt(4,1));
		//float usBoost = (float) Float.valueOf((String)tm.getValueAt(5,1));
		float vartheta = (float) Float.valueOf((String)tm.getValueAt(3,1));
        
		if (recency > 1) {
			
			tm.setValueAt("1", 1, 1);
			JOptionPane.showMessageDialog(null,
				    "Value must be 0.0-1.0.",
				    "Parameter Error",
				    JOptionPane.PLAIN_MESSAGE);
		}
		if (recency < 0) {tm.setValueAt("0", 1, 1);
		JOptionPane.showMessageDialog(null,
			    "Value must be 0.0-1.0.",
			    "Parameter Error",
			    JOptionPane.PLAIN_MESSAGE);
		}
		if (common > 1) {tm.setValueAt("1", 2, 1);
		JOptionPane.showMessageDialog(null,
			    "Value must be 0.0-1.0.",
			    "Parameter Error",
			    JOptionPane.PLAIN_MESSAGE);
		}
		if (common < 0) {tm.setValueAt("0", 2, 1);
		JOptionPane.showMessageDialog(null,
			    "Value must be 0.0-1.0.",
			    "Parameter Error",
			    JOptionPane.PLAIN_MESSAGE);
		}		
		
		//if (cscLike > 100) {tm.setValueAt("100", 4, 1);}
		//if (cscLike < 0) {tm.setValueAt("0", 4, 1);}	
		//if (usBoost > 500) {tm.setValueAt("500", 5, 1);}
		//if (usBoost < 0) {tm.setValueAt("0", 5, 1);}	
		/*if (vartheta > 1) {tm.setValueAt("1.0", 3, 1);
			JOptionPane.showMessageDialog(null,
			    "Value must be 0.9-1.0.",
			    "Parameter Error",
			    JOptionPane.ERROR_MESSAGE);
		}
		if (vartheta <= 0.89) {
			tm.setValueAt("0.9", 3, 1);
			JOptionPane.showMessageDialog(null,
			    "Value must be 0.9-1.0.",
			    "Parameter Error",
			    JOptionPane.ERROR_MESSAGE);
		}	//*/
		// First check that table contains legal values.
		for (int row = 0; row < tm.getRowCount(); row++) {
			for (int col = 1; col < tm.getColumnCount(); col++) {
				String tmp = (String) tm.getValueAt(row, col);

				// PHASE 1 IS EMPTY
				if (col == 1 && tmp.length() == 0) {
					cont = false;
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < tm.getRowCount(); i++) {
						sb.append((String) tm.getValueAt(i, 0));
						sb.append(", "); //$NON-NLS-1$
					}
					sb.append(Messages
							.getString("SimController.otherEmptyMessage")); //$NON-NLS-1$
					view.showError(sb.toString());
					break;
				}
				// NOT EMPTY FIELD
				if (tmp.length() > 0) {
					// NOT EMPTY FIELD
					try {
						new Float(tmp);
					} catch (Exception ex) {
						cont = false;
						view.showError(Messages
								.getString("SimController.entryWrongMessage")); //$NON-NLS-1$
						break;
					}
				}
			}
			if (!cont)
				break;
		}
		return cont;
	}

	/**
	 * Returns true if the ValuesTable has been checked successfully
	 */
	private boolean checkUSValuesTable() {
		boolean cont = true;
		AbstractTableModel tm = view.getUSValuesTableModel();

		// First check that table contains legal values.
		for (int row = 0; row < tm.getRowCount(); row++) {
			for (int col = 1; col < tm.getColumnCount(); col++) {
				String tmp = (String) tm.getValueAt(row, col);

				// PHASE 1 IS EMPTY
				if (col == 1
						&& tmp.length() == 0
						&& !tm.getValueAt(row, 0).equals(
								Messages.getString("SimController.84"))) { //$NON-NLS-1$
					cont = false;
					view.showError(Messages
							.getString("SimController.USEmptyMessage")); //$NON-NLS-1$
					break;
				}
				
				if (tmp.length() > 0) {
					try {
						new Float(tmp);
					} catch (Exception ex) {
						cont = false;
						view.showError(Messages
								.getString("SimController.entryWrongMessage")); //$NON-NLS-1$
						break;
					}
				}
			}
			if (!cont)
				break;
		}
		return cont;
	}
	
	private boolean checkUSSalienceTable() {
		boolean cont = true;
		AbstractTableModel tm = view.getUSSalienceTableModel();

		// First check that table contains legal values.
		for (int row = 0; row < tm.getRowCount(); row++) {
			for (int col = 1; col < tm.getColumnCount(); col++) {
				String tmp = (String) tm.getValueAt(row, col);

				// PHASE 1 IS EMPTY
				if (col == 1
						&& tmp.length() == 0
						&& !tm.getValueAt(row, 0).equals(
								Messages.getString("SimController.84"))) { //$NON-NLS-1$
					cont = false;
					view.showError(Messages
							.getString("SimController.USEmptyMessage")); //$NON-NLS-1$
					break;
				}
				
				if (tmp.length() > 0) {
					try {
						new Float(tmp);
					} catch (Exception ex) {
						cont = false;
						view.showError(Messages
								.getString("SimController.entryWrongMessage")); //$NON-NLS-1$
						break;
					}
				}
			}
			if (!cont)
				break;
		}
		return cont;
	}

	/**
	 * Clears up the tables from any values that they may contain and also
	 * re-initiate the initial status on the menuitems and buttons.
	 * 
	 * @param g
	 *            the previous number of groups.
	 * @param p
	 *            the previous number of phases.
	 * @param c
	 *            the previous number of combination.
	 */
	private void clearModel(int g, int p, int c) {
		ITIConfig.resetDefaults();
		TimingConfiguration.clearDefaults();
		// Distributions.resetSeed();
		// VariableDistribution.resetSeed();
		view.reset();
		view.clearHidden();
		setModel(new SimModel());
		view.updateModel(getModel());

		getModel().setGroupNo(g);
		getModel().setPhaseNo(p);
		getModel().setCombinationNo(c);

		view.getPhasesTableModel().setPhasesTable();
		view.updatePhasesColumnsWidth();

		view.getCSValuesTableModel().setValuesTable(true);
		view.getCSVariableTableModel().setValuesTable(true);
		view.getUSValuesTableModel().setInitialValuesTable();
		view.getCSSalienceTableModel().setValuesTable(true);
		view.getUSSalienceTableModel().setInitialValuesTable();
		if (view.isUSAcrossPhases())
			view.addUSPhases();

		if (!view.isUSAcrossPhases()
				|| (view.isUSAcrossPhases() && getModel().getPhaseNo() == 1)) {
			view.getUSValuesTable().setAutoResizeMode(
					JTable.AUTO_RESIZE_ALL_COLUMNS);
			view.getUSSalienceTable().setAutoResizeMode(
				JTable.AUTO_RESIZE_ALL_COLUMNS);
		}
		else {
			view.getUSValuesTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			view.getUSSalienceTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			view.updateUSValuesColumnsWidth();
		}
		view.getPhasesTable().getColumnModel().getColumn(1).setCellEditor(new TrialStringEditor());

	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	// E. Mondragon 28 Sept 2011

	private ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = this.getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} // else ;
		System.err.println(Messages.getString("SimController.fnfError") + path); //$NON-NLS-1$
		return null;
	}

	/**
	 * @return the model
	 */
	public SimModel getModel() {
		return model;
	}
	
	
	
	
	public void checkExternalSave() {
		if (model.isExternalSave()) {
			/*chooser = new JFileChooser(); 
		    chooser.setCurrentDirectory(new File(lastDirectory));
		    chooser.setDialogTitle("Choose external file location");
		    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    //
		    // disable the "All files" option.
		    //
		    chooser.setAcceptAllFileFilterUsed(false);
		    chooser.showOpenDialog(view);
		    model.setDirectory(chooser.getSelectedFile());*/
		    
		}
		
	}

	/**
	 * Helper function for loading a td experiment configuration serialized to a
	 * tdl file.
	 * 
	 * @param file
	 *            to load.
	 * @throws IOException
	 *             if there's a problem loading the file.
	 * @throws VersionException
	 *             if the file is of an incompatible version.
	 * @throws ClassNotFoundException
	 *             if an object can't be unflattened.
	 */

	private void loadObjects(final File file) throws IOException,
			VersionException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		float version = in.readFloat();
		if (version < 0.9) {
			throw new VersionException();
		}
		clearModel(in.readInt(), in.readInt(), in.readInt());
		view.setIsUSAcrossPhases(in.readBoolean());
		view.setIsSetConfiguralCompounds(in.readBoolean());
		view.setStatusComponent(view.isSetConfiguralCompounds(),
				"SetConfiguralCompounds"); //$NON-NLS-1$
		getModel().setTimestepSize(in.readFloat());
		getModel().setThreshold(in.readFloat());
		getModel().setShowResponse(in.readBoolean());
		view.setStatusComponent(getModel().showResponse(), "threshold"); //$NON-NLS-1$
		// Set the context configuration.
		boolean context = true;
		if (in.readBoolean()) {
			in.readBoolean();
			view.setStatusComponent(true, "SingleContext"); //$NON-NLS-1$
		} else if (!in.readBoolean()) {
			view.setStatusComponent(true, "SetContext"); //$NON-NLS-1$
			context = false;
		} else {
			view.setStatusComponent(true, "SetContextAcrossPhases"); //$NON-NLS-1$
		}
		float omegaAlphaR = in.readFloat();
		float omegaAlphaN = in.readFloat();
		float omegaSalience = in.readFloat();
		
		view.setOmegaSalience(omegaAlphaR,omegaAlphaN,omegaSalience);
		getModel().setUseContext(context);
		//in.readBoolean();
		view.setIsSetCompound(in.readBoolean());
		view.setStatusComponent(in.readBoolean(), "geo"); //$NON-NLS-1$
		view.setStatusComponent(in.readBoolean(), "exp"); //$NON-NLS-1$
		if (version == 0.9) {
		in.readBoolean();
		} else if (version < 0.97) {
		in.readBoolean();
		} else {
			in.readBoolean();
			//view.setTraceType((Trace) in.readObject());
		}
		if (version > 0.95) {
			getModel().setVariableCombinationNo(in.readInt());
		}
		view.clearHidden();
		Vector phasesModel = (Vector) in.readObject();
		
		view.getCSValuesTableModel().setData((Vector) in.readObject());
		view.getCSVariableTableModel().setData((Vector) in.readObject());
		view.getUSValuesTableModel().setData((Vector) in.readObject());
		view.getCSSalienceTableModel().setData((Vector) in.readObject());
		view.getUSSalienceTableModel().setData((Vector) in.readObject());
		view.getOtherValuesTableModel().setData((Vector) in.readObject());
		VariableDistribution.newRandomSeed();

		if (version > 0.96) {
			// Per trial timings
			boolean on = in.readBoolean();
			getModel().setTimingPerTrial(on);
			view.setTimingPerTrial(on);

			// Serial compounds & configurals
			in.readBoolean();
			//view.setUseSerialCompounds(in.readBoolean());
			// Zero traces per trial
			in.readBoolean();
			//view.setZeroTraces(in.readBoolean());
			if (version > 0.98) {
				in.readBoolean();
				//view.setRestrictPredictions(in.readBoolean());
			}
		}
		view.getPhasesTableModel().setData(phasesModel);
		view.getPhasesTable().createDefaultColumnsFromModel();
		view.getPhasesTable().getColumnModel().getColumn(1).setCellEditor(new TrialStringEditor());
		view.updatePhasesColumnsWidth();
		view.toggleContext(context);
		in.close();
		view.updateTimingConfigs();
	}
	
	private void loadExcel(final File file) throws IOException,
		VersionException, ClassNotFoundException {
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
	    HSSFWorkbook wb = new HSSFWorkbook(fs);
	    HSSFSheet sheet = wb.getSheetAt(0);
	    HSSFRow row;
	    HSSFCell cell;

	    int rows; // No of rows
	    rows = sheet.getPhysicalNumberOfRows();

	    int cols = 0; // No of columns
	    int tmp = 0;

	    // This trick ensures that we get the data properly even if it doesn't start from first few rows
	    for(int i = 0; i < 10 || i < rows; i++) {
	        row = sheet.getRow(i);
	        if(row != null) {
	            tmp = sheet.getRow(i).getPhysicalNumberOfCells();
	            if(tmp > cols) cols = tmp;
	        }
	    }

	    for(int r = 0; r < rows; r++) {
	        row = sheet.getRow(r);
	        if(row != null) {
	            for(int c = 0; c < cols; c++) {
	                cell = row.getCell((short)c);
	                if(cell != null) {
	                    // Your code here
	                }
	            }
	        }
	    }

		clearModel(1, 1, 1);//set number of groups, phases, number of random combinations if checked
		getModel().setTimestepSize(1.0f);
		getModel().setThreshold(0.05f);
		view.setStatusComponent(true, "SingleContext"); //$NON-NLS-1$
		float omegaAlphaR = 0.25f;
		float omegaAlphaN = 0.2f;
		float omegaSalience = 0.07f;
		view.setOmegaSalience(omegaAlphaR,omegaAlphaN,omegaSalience);
		getModel().setUseContext(true);
		view.clearHidden();
		
		int col = 5;
		Vector newData = new Vector();
		Object record[] = new Object[col];
		record[record.length - 5] = ""; //$NON-NLS-1$
		record[record.length - 4] = new ContextConfig(Context.PHI,0.25f,0.2f,0.07f);
		record[record.length - 3] = new Boolean(false);
		record[record.length - 2] = new TimingConfiguration(1);
		record[record.length - 1] = new ITIConfig(300, 0, 1,false, 1);
		newData.add(record);
		
		//view.getCSValuesTableModel().setData((Vector) in.readObject());
		//view.getUSValuesTableModel().setData((Vector) in.readObject());
		//view.getOtherValuesTableModel().setData((Vector) in.readObject());
		view.getPhasesTableModel().setData(newData);
		view.getPhasesTable().createDefaultColumnsFromModel();
		view.getPhasesTable().getColumnModel().getColumn(1).setCellEditor(new TrialStringEditor());
		view.updatePhasesColumnsWidth();
		view.toggleContext(true);
		view.updateTimingConfigs();
	}

	/**
	 * Clears up the tables from any values that they may contain and also
	 * re-initiate the initial status on the menuitems and buttons.
	 */
	private void newModel() {
		ITIConfig.resetDefaults();
		TimingConfiguration.clearDefaults();
		// Distributions.resetSeed();
		// VariableDistribution.resetSeed();
		view.reset();
		view.clearHidden();
		setModel(new SimModel());
		view.updateModel(getModel());
		view.getPhasesTableModel().setPhasesTable();
		view.updatePhasesColumnsWidth();
		view.getCSValuesTableModel().setValuesTable(true);
		view.getCSVariableTableModel().setValuesTable(true);
		view.getUSValuesTableModel().setInitialValuesTable();
		view.getCSSalienceTableModel().setValuesTable(true);
		view.getUSSalienceTableModel().setInitialValuesTable();
		view.getUSValuesTable().setAutoResizeMode(
				JTable.AUTO_RESIZE_ALL_COLUMNS);
		view.getUSSalienceTable().setAutoResizeMode(
				JTable.AUTO_RESIZE_ALL_COLUMNS);
		view.getPhasesTable().getColumnModel().getColumn(1).setCellEditor(new TrialStringEditor());

		
	}

	/**
	 * Invoked when task's progress property changes.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) { //$NON-NLS-1$
		// int progress = (Integer) evt.getNewValue();
		// progressMonitor.setProgress(progress);
		// long remaining = (long)
		// (control.getEstimatedCycleTime()*(totalProgress-control.getProgress()));
		// String timeRemaining = String.format("%d min, %d sec",
		// TimeUnit.MILLISECONDS.toMinutes(remaining),
		// TimeUnit.MILLISECONDS.toSeconds(remaining) -
		// TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remaining))
		// );
		//            String message = timeRemaining; //$NON-NLS-1$
		// progressMonitor.setNote(message);
			if (progressMonitor.isCanceled() || task.isDone()) {
				if (progressMonitor.isCanceled()) {
					task.cancel(true);
					control.setCancelled(true);
				}
			}
		}

	}

	/**
	 * Helper function for saving a simulation configuration to a tdl file.
	 * 
	 * @param file
	 *            to save to.
	 * @throws IOException
	 *             if there's a problem reading the file.
	 */

	private void saveToObjects(final File file) throws IOException {
		boolean okToContinue = true;
		if (file.exists()) {
			int response = JOptionPane
					.showConfirmDialog(
							null,
							Messages.getString("SimController.overwrite"), Messages.getString("SimController.confirmOverwrite"), //$NON-NLS-1$ //$NON-NLS-2$
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.CANCEL_OPTION) {
				okToContinue = false;
			}
		}
		if (okToContinue) {
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(file));
			// Save file version
			out.writeFloat((float) Simulator.VERSION);
			// Number of groups
			out.writeInt(getModel().getGroupNo());
			// Number of phases
			out.writeInt(getModel().getPhaseNo());

			// Number of combinations for randomness
			out.writeInt(getModel().getCombinationNo());
			// Whether US is across phases
			out.writeBoolean(view.isUSAcrossPhases());
			// Alberto Fernandez August-2011, whether configurals are used
			out.writeBoolean(!getModel().getConfigCuesNames().isEmpty());
			// Timestep length
			out.writeFloat(getModel().getTimestepSize());
			// Response threshold
			out.writeFloat(getModel().getThreshold());
			// Whether decision rule sim is enabled
			out.writeBoolean(getModel().showResponse());
			// Whether single context is set
			out.writeBoolean(getModel().isUseContext()
					&& !getModel().contextAcrossPhase());

			// Whether no context is set
			out.writeBoolean(getModel().isUseContext());

			// Default context salience
			out.writeFloat(view.getContextAlphaR());
			out.writeFloat(view.getContextAlphaN());
			out.writeFloat(view.getContextSalience());
			// Whether compounds are on
			out.writeBoolean(view.isSetCompound());
			// Mean type is geometric
			out.writeBoolean(getModel().isGeometricMean());
			// Variable distribution
			out.writeBoolean(getModel().isExponential());
			// Trace type
			out.writeBoolean(false);

			// Random combos for variable durations
			out.writeInt(getModel().getVariableCombinationNo());

			ValuesTableModel tmp = view.getPhasesTableModel();

			out.writeObject(tmp.getData());
			ValuesTableModel tmv = view.getCSValuesTableModel();
			out.writeObject(tmv.getData());
			ValuesTableModel tmvv = view.getCSVariableTableModel();
			out.writeObject(tmvv.getData());
			ValuesTableModel ustmv = view.getUSValuesTableModel();
			out.writeObject(ustmv.getData());
			ValuesTableModel csm = view.getCSSalienceTableModel();
			out.writeObject(csm.getData());
			ValuesTableModel usm = view.getUSSalienceTableModel();
			out.writeObject(usm.getData());
			ValuesTableModel otmv = view.getOtherValuesTableModel();
			out.writeObject(otmv.getData());
			// Per trial timings
			out.writeBoolean(getModel().isTimingPerTrial());
			// Serial compounds & configurals
			out.writeBoolean(getModel().isSerialCompounds()
					|| getModel().isSerialConfigurals());
			// Zero traces per trial
			out.writeBoolean(getModel().isZeroTraces());
			// Restrict predictions
			out.writeBoolean(getModel().isRestrictPredictions());
			out.close();
		}
	}
	
	/*public void tempSave(final File file, SimPhase simphase, SimGroup sg) throws IOException {
		
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(file)));
			for (Stimulus stim: sg.getCuesMap().values()) {
				for (StimulusElement se: stim.getList()) {
					
					out.writeObject(se.getAggregatePredictions(simphase.getPhaseNum()-1));
					//if (simphase.getPhaseNum() == sg.getNoOfPhases()) se.deletePredictions();
				}
			}
			// Save file version
			//float[][][][][][] predictions = new float[model.getGroupNo()][model.getListAllCues().size][model.getPhaseNo()][model.get][];
			//out.writeObject(predictions);
			
			out.close();//
		
	}*/
	
	
	
	
	/*public void tempSaveA(final File file, SimPhase simphase, SimGroup sg) throws IOException {
		
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(
				new FileOutputStream(file)));
		for (Stimulus stim: sg.getCuesMap().values()) {
			for (StimulusElement se: stim.getList()) {
				
				out.writeObject(se.getAggregatePredictionsA(simphase.getPhaseNum()-1));
				if (simphase.getPhaseNum() == sg.getNoOfPhases()) se.deletePredictions();
			}
		}
		// Save file version
		//float[][][][][][] predictions = new float[model.getGroupNo()][model.getListAllCues().size][model.getPhaseNo()][model.get][];
		//out.writeObject(predictions);
		
		out.close();//
	
}*/
	public void clearSave() {
		if (getModel() != null && getModel().getDirectory() != null && getModel().getDirectory().getPath() != null ){
			String directoryName = getModel().getDirectory().getPath();
			
			File folder = new File(directoryName + System.getProperty("file.separator") + "TempDD");
			deleteFolder(folder);
		}
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
	
	
	public TreeMap<String,ArrayList<float[][][]>> tempChunkLoad(SimPhase simphase, SimGroup group, ArrayList<String> stimNames) throws IOException,
	VersionException, ClassNotFoundException {
	/*String directoryName = group.getModel().getDirectory().getPath();
	File file = new File(directoryName + System.getProperty("file.separator") + "TempDD" + System.getProperty("file.separator") + "temp" + group.getNameOfGroup() +"_" + (simphase.getPhaseNum()) + ".ddfft");
	//System.out.println("C:\\Users\\abmj099admin\\TempDD\\" + "temp" + group.getNameOfGroup() +"_" + (simphase.getPhaseNum()) + ".ddfft");
	
	
	ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
	TreeMap<String,ArrayList<float[][][]>> returnArray = new TreeMap();
	for (Stimulus stim: group.getCuesMap().values()) {
		for (StimulusElement se: stim.getList()) {
			if (stimNames.contains(stim.getName())) {
				if (!returnArray.containsKey(stim.getName())) {returnArray.put(stim.getName(), new ArrayList<float[][][]>());}
				returnArray.get(stim.getName()).add((float[][][]) in.readObject());
			}
			else {in.readObject();}
		}
	}
	in.close();
	return returnArray;*/
		return null;
	}
	public TreeMap<String,ArrayList<float[][][]>> tempChunkLoadA(SimPhase simphase, SimGroup group, ArrayList<String> stimNames) throws IOException,
	VersionException, ClassNotFoundException {
		/*
	String directoryName = group.getModel().getDirectory().getPath();
	File file = new File(directoryName + System.getProperty("file.separator") + "TempDD" + System.getProperty("file.separator") + "tempA" + group.getNameOfGroup() +"_" + (simphase.getPhaseNum()) + ".ddfft");
	//System.out.println("C:\\Users\\abmj099admin\\TempDD\\" + "temp" + group.getNameOfGroup() +"_" + (simphase.getPhaseNum()) + ".ddfft");
	
	
	ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
	TreeMap<String,ArrayList<float[][][]>> returnArray = new TreeMap();
	for (Stimulus stim: group.getCuesMap().values()) {
		for (StimulusElement se: stim.getList()) {
			if (stimNames.contains(stim.getName())) {
				if (!returnArray.containsKey(stim.getName())) {returnArray.put(stim.getName(), new ArrayList<float[][][]>());}
				returnArray.get(stim.getName()).add((float[][][]) in.readObject());
			}
			else {in.readObject();}
		}
	}
	in.close();
	return returnArray;*/
		return null;
}
	
	
	

	/**
	 * @param model
	 *            the model to set
	 */
	public void setModel(SimModel model) {
		this.model = model;
	}

    /**
     * Nasty hack, to let a timing config check if it is should
     * be updating.
     * @param conf
     * @return
     */

    public boolean isReferenced(TimingConfiguration conf) {
        try {
            int col = SimView.activeCol + 3;
            TimingConfiguration target = (TimingConfiguration) view.getPhasesTableModel().getValueAt(SimView.activeRow, col);
            return conf.equals(target);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }
    
    public void setParameters()  throws IOException,
	VersionException, ClassNotFoundException
    	{

			for (SimGroup g : model.getGroups().values()) {g.closeDBs(); g.resetAll();}
    		System.gc();
    		File file = new File("C:\\Users\\abmj099admin\\Desktop\\least_squares.ddff");
    		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
    		float version = in.readFloat();
    		if (version < 0.9) {
    			throw new VersionException();
    		}
    		clearModel(in.readInt(), in.readInt(), in.readInt());
    		view.setIsUSAcrossPhases(in.readBoolean());
    		view.setIsSetConfiguralCompounds(in.readBoolean());
    		view.setStatusComponent(view.isSetConfiguralCompounds(),
    				"SetConfiguralCompounds"); //$NON-NLS-1$
    		getModel().setTimestepSize(in.readFloat());
    		getModel().setThreshold(in.readFloat());
    		getModel().setShowResponse(in.readBoolean());
    		view.setStatusComponent(getModel().showResponse(), "threshold"); //$NON-NLS-1$
    		// Set the context configuration.
    		boolean context = true;
    		if (in.readBoolean()) {
    			in.readBoolean();
    			view.setStatusComponent(true, "SingleContext"); //$NON-NLS-1$
    		} else if (!in.readBoolean()) {
    			view.setStatusComponent(true, "SetContext"); //$NON-NLS-1$
    			context = false;
    		} else {
    			view.setStatusComponent(true, "SetContextAcrossPhases"); //$NON-NLS-1$
    		}
    		float omegaAlphaR = in.readFloat();
    		float omegaAlphaN = in.readFloat();
    		float omegaSalience = in.readFloat();
    		view.setOmegaSalience(omegaAlphaR,omegaAlphaN,omegaSalience);
    		getModel().setUseContext(context);
    		in.readBoolean();
    		view.setIsSetCompound(in.readBoolean());
    		view.setStatusComponent(in.readBoolean(), "geo"); //$NON-NLS-1$
    		view.setStatusComponent(in.readBoolean(), "exp"); //$NON-NLS-1$
    		if (version == 0.9) {
    		in.readBoolean();
    		} else if (version < 0.97) {
    		in.readBoolean();
    		} else {
    			in.readBoolean();
    			//view.setTraceType((Trace) in.readObject());
    		}
    		if (version > 0.95) {
    			getModel().setVariableCombinationNo(in.readInt());
    		}
    		view.clearHidden();
    		Vector phasesModel = (Vector) in.readObject();
    		
    		view.getCSValuesTableModel().setData((Vector) in.readObject());
    		view.getCSVariableTableModel().setData((Vector) in.readObject());
    		view.getUSValuesTableModel().setData((Vector) in.readObject());
    		view.getCSSalienceTableModel().setData((Vector) in.readObject());
    		view.getUSSalienceTableModel().setData((Vector) in.readObject());
    		view.getOtherValuesTableModel().setData((Vector) in.readObject());
    		VariableDistribution.newRandomSeed();

    		if (version > 0.96) {
    			// Per trial timings
    			boolean on = in.readBoolean();
    			getModel().setTimingPerTrial(on);
    			view.setTimingPerTrial(on);

    			// Serial compounds & configurals
    			in.readBoolean();
    			//view.setUseSerialCompounds(in.readBoolean());
    			// Zero traces per trial
    			in.readBoolean();
    			//view.setZeroTraces(in.readBoolean());
    			if (version > 0.98) {
    				in.readBoolean();
    				//view.setRestrictPredictions(in.readBoolean());
    			}
    		}
    		view.getPhasesTableModel().setData(phasesModel);
    		view.getPhasesTable().createDefaultColumnsFromModel();
    		view.getPhasesTable().getColumnModel().getColumn(1).setCellEditor(new TrialStringEditor());
    		view.updatePhasesColumnsWidth();
    		view.toggleContext(context);
    		in.close();
    		view.updateTimingConfigs();
    }
    
    public void saveVariableSalience(Stimulus s, String groupName) {
    /*
	    FileWriter fw = null;
		try {
			fw = new FileWriter("C:\\Users\\abmj099admin\\" + groupName + "_vs.csv");
			System.out.println("C:\\Users\\abmj099admin\\" + groupName + "_vs.csv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	for (int index = 0; index < s.getVariableSalienceArray().get(0).size(); index++)
    		{
    	   float nextValue = 0;
    	   for (int i = 0 ; i < 3; i++) {
    		   nextValue +=  s.getVariableSalienceArray().get(i).get(index)/3d;
    	   }
    	        try {
					fw.append( nextValue+ "");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	        try {
					fw.append(",");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	    }
	    try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
    	}
    	
    
    
    public void leastSquares() {
    	float numberOfIterations = 3;
    	float maxalpha = 1;
    	float minalpha = 0.01f;
    	float maxbeta = 1;
    	float minbeta = 0.01f;
    	double[] vValues = new double[(int)(numberOfIterations*numberOfIterations)];
    	
    	double[] alphas = new double[(int)(numberOfIterations*numberOfIterations)];
    	double[] betas = new double[(int)(numberOfIterations*numberOfIterations)];
    	for (int i = 0; i < numberOfIterations*numberOfIterations; i++) {
    		alphas[i] = (double)(minalpha + Math.floor(((float)i)/numberOfIterations)*(maxalpha - minalpha)/numberOfIterations);
    		betas[i] = (minbeta + (i%numberOfIterations)*(maxbeta - minbeta)/numberOfIterations);
    	}
    	for (int i = 0; i < numberOfIterations*numberOfIterations; i++) {
    		clearModel(getModel().getGroupNo(), getModel().getPhaseNo(),
					getModel().getCombinationNo());
			getModel().setUseContext(view.isUseContext());
    			try {
					setParameters();
				} catch (ClassNotFoundException e) {
					System.out.println("fail1 ctrl" );
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("fail2 ctrl");
					e.printStackTrace();
				} catch (VersionException e) {
					System.out.println("fail3 ctrl");
					e.printStackTrace();
				}
    			for (ActionListener a : view.getVariablesButton().getActionListeners()) {
    				ActionEvent myActionEvent = new ActionEvent(view.getVariablesButton(),
                            ActionEvent.ACTION_PERFORMED,
                            view.getVariablesButton().getActionCommand());
    				a.actionPerformed(myActionEvent);
    			}
    			view.getUSValuesTableModel().setValueAt(Double.toString(betas[i]), 0, 1);
				for (int n2 = 0; n2 < view.getCSValuesTableModel().getRowCount(); n2++) {
					if (view.getCSValuesTableModel().getValueAt(0,0).equals("A")) view.getCSValuesTableModel().setValueAt(Double.toString(alphas[i]), n2, 1);
				}
    			for (ActionListener a : view.getRunButton().getActionListeners()) {
    				ActionEvent myActionEvent = new ActionEvent(view.getRunButton(),
                            ActionEvent.ACTION_PERFORMED,
                            view.getRunButton().getActionCommand());
    				a.actionPerformed(myActionEvent);
    			}
    			while (task != null && !task.isDone()) { 
    				//System.out.println("loop simctrl");
    				}
    			float current = 0f;
    			for (SimGroup sg: model.getGroups().values()) {
    				for (Stimulus s: sg.getCuesMap().values()) {
    					if (s.getName().equals("A")) {
    						current = s.getTrialW(0,0,9, "+",false);
    					}
    				}
    			}
    			vValues[i] = current*100;
    	}
    	for (int j = 0; j < alphas.length; j++) {
    	}
    	LeastSquares leastSquares = new LeastSquares("Least Squares", new double[][]{alphas,betas,vValues});
    	leastSquares.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    	leastSquares.pack();
    	leastSquares.setVisible(true);
    	
    }

}
