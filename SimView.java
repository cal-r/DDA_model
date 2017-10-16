/**
 * SimView.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;


import simulator.configurables.ContextConfig;
import simulator.configurables.ContextConfig.Context;
import simulator.configurables.ITIConfig;
import simulator.configurables.TimingConfiguration;
import simulator.editor.ContextEditor;
import simulator.editor.ITIEditor;
import simulator.editor.TimingEditor;
import simulator.editor.TrialStringEditor;
import simulator.util.Distributions;
import simulator.util.GreekSymbol;
import simulator.util.USNames;
import simulator.util.ValuesTableModel;

//import sun.awt.VerticalBagLayout;   // modified by Alberto Fernandez: 18 July 2011

/**
 * SimView class is the main graphical user interface of the Simulator's
 * application. The main purposes is the managing of viewing objects. It
 * contains a menu bar on top with File -> New, Open, Save, Export and Quit,
 * Settings -> Groups, Phases, Combinations and last the Help -> Guide. Next, a
 * phase table where the user adds the group names, the sequences on every phase
 * and if he prefers to run randomly. The value table where the user adds the
 * values for every parameter that is needed and the text output which display
 * the results. It also contains buttons for interaction.
 */
public class SimView extends JFrame {

	/** This class is the values' table model */
	class CSValuesTableModel extends ValuesTableModel {

		/**
		 * ValuesTableModel's Constructor method.
		 */
		public CSValuesTableModel() {
			super();
		}

		/**
		 * Return the names of the table's column names.
		 * 
		 * @return an array with the table's column names.
		 */
		private String[] getColNames() {
			String[] s = new String[col];
			for (int c = 0; c < col; c++) {
				if (c == 0)
					s[c] = Messages.getString("SimView.csAlpha"); //$NON-NLS-1$
				else if (col == 2)
					s[c] = Messages.getString("SimView.value"); //$NON-NLS-1$
				else
					s[c] = Messages.getString("SimView.phaseSpace") + (c + 1); //$NON-NLS-1$
			}
			return s;
		}

		/**
		 * Initializes and configures the table with some initial values.
		 * 
		 * @param iniValues
		 *            . If true, we initialize all variables to "" or by default
		 *            without taking into account last values of cues
		 */
		public void setValuesTable(boolean iniValues) {
			// if data had some values before, then reuse them
			Vector data2 = new Vector((Vector) getData().clone());
			Vector data1 = new Vector();
			data = new Vector();

			try {
				row = model.getNumberAlphaCues();
				col = 2;
				columnNames = getColNames();

				TreeMap<String, Float> tm = model.getAlphaCues();
				Iterator<String> it = tm.keySet().iterator();
				// Split into two lists, sort the list of compounds
				// by interface name not letter
				List<String> cueNames = new ArrayList<String>();
				TreeMap<String, String> configuralNames = new TreeMap<String, String>();
				while(it.hasNext()) {
					String pair = it.next();
					if((isSetConfiguralCompounds() || model
							.isSerialConfigurals())
							&& model.getConfigCuesNames()
									.containsKey(pair)) {
						String compoundName = model.getConfigCuesNames().get(pair);
						String interfaceName = "c(" + compoundName + ")";
						configuralNames.put(interfaceName, pair);
					} else {
						//Modified by Niklas Kokkola 2014 to disregard US from CS list.
						boolean isUS = false;
						for (String usName : USNames.getNames()) {
							if (pair.equals(usName)) {
								isUS = true;
							}
						}
						if (!isUS) {cueNames.add(pair);}
					}
				}				
				cueNames.addAll(configuralNames.values());
				it = cueNames.iterator();
				while (it.hasNext()) {
					String pair = it.next();
					//
					// Disregard context cues, alphas for them are set elsewhere
					if (!Context.isContext(pair)) {

						if (pair.length() == 1) {
							Object record[] = new Object[col];
							int isInValues = -1;
							boolean configuralEmpty = false;

							for (int c = 0; c < col; c++) {
								// cue name
								if (c == 0) {
									record[c] = pair + "_\u03B1r";
									// Alberto Fernandez August-2011
									if ((isSetConfiguralCompounds() || model
											.isSerialConfigurals())
											&& model.getConfigCuesNames()
													.containsKey(pair)) {
										String compoundName = model
												.getConfigCuesNames().get(pair);
										// String interfaceName = "�(" +
										// compoundName + ")";
										String interfaceName = "c(" + compoundName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
										record[c] = interfaceName;
									}
									// isInValues = isInValuesTable(data2,pair);
									isInValues = isInValuesTable(data2,
											(String) record[c]);
								}
								// cue value
								// If it exists already --> last value from
								// data2
								// else if (isInValues>-1 && !iniValues) {
								else if (isInValues > -1
										&& !iniValues
										&& !(((String) record[0]).length() > 1 && ((Object[]) data2
												.get(isInValues))[c] == "")) { //$NON-NLS-1$
									record[c] = ((Object[]) data2
											.get(isInValues))[c];
								}
								// Alberto Fernandez Sept-2011
								// If it didnt exist --> if c(AB) ==> A*B
								// otherwise default value
								else if (((String) record[0]).length() > 1 + "_\u03B1r".length()) {

									// find record[0]= c(AB)
									Float val = 1.0f;
									boolean found, missing = false;
									String compound = ((String) record[0])
											.substring(2, ((String) record[0])
													.length() - 1);
									if (compound.startsWith(Simulator.OMEGA
											+ "")) { //$NON-NLS-1$
										compound = compound.substring(1);
									}
									// Filter out dashes in compound name
									compound = compound.replaceAll(
											ConfiguralCS.SERIAL_SEP, "");
									// Dedupe
									Set<Character> compoundSet = new HashSet<Character>();
									for (char cs : compound.toCharArray()) {
										compoundSet.add(cs);
									}
									StringBuilder sb = new StringBuilder();
									for (char cs : compoundSet) {
										sb.append(cs);
									}
									compound = sb.toString();

									String cue;

                                    float maxVal = 0;
                                    float minVal = Float.POSITIVE_INFINITY;
                                    List<Float> alphas = new ArrayList<Float>();

									for (int i = 0; i < compound.length(); i++) {
										cue = compound.substring(i, i + 1);
										// find cue in Vector data2
										found = false;
										missing = false; // there is no value
															// for current cue

										if (Context.isContext(cue)) {
                                            alphas.add(model.getContexts().get(cue)
                                                    .getAlphaR());
											maxVal = Math.max(maxVal, model.getContexts().get(cue)
													.getAlphaR());
                                            minVal = Math.min(minVal, model.getContexts().get(cue)
                                                    .getAlphaR());
											found = true;
										} else {

											for (int j = 0; j < data2.size(); j++) {
												Object cue_value_pair[] = (Object[]) data2
														.get(j);
												if (cue.equals(cue_value_pair[0])) {
													String s = (String) cue_value_pair[1];
													if (s != "") { //$NON-NLS-1$
                                                        alphas.add(Float.parseFloat(s));
														//maxVal = Math.max(maxVal, Float.parseFloat(s));
                                                        //minVal = Math.min(minVal, Float.parseFloat(s));
														found = true;
														break;
													}
												}
											}
										}
										if (!found) {
											val *= 0.2f;
										}
									}
									if (!missing) { // found) {
                                        Collections.sort(alphas);
                                        try {
                                            maxVal = alphas.get(alphas.size() - 1);
                                        } catch(ArrayIndexOutOfBoundsException e) {
                                            maxVal = 0.45f;
                                        }
                                        try {
                                            minVal = alphas.get(alphas.size() - 2);
                                        } catch(ArrayIndexOutOfBoundsException e) {
                                            minVal = 0.0001f;
                                        }
										record[c] = (new DecimalFormat("#.##########")).format(maxVal * minVal);
									} else {
										record[c] = ""; //$NON-NLS-1$
									}
								} else {
									record[c] = "0.8"; //$NON-NLS-1$
								}
							}
							data1.add(record);
						}
					}
					setData(data1);
					fireTableChanged(null); // notify everyone that we have a
											// new table.
				}
			} catch (Exception e) {
				setData(new Vector()); // blank it out and keep going.
				e.printStackTrace();
			}
		}
	}
	
	class CSSalienceTableModel extends ValuesTableModel {

		/**
		 * ValuesTableModel's Constructor method.
		 */
		public CSSalienceTableModel() {
			super();
		}

		/**
		 * Return the names of the table's column names.
		 * 
		 * @return an array with the table's column names.
		 */
		private String[] getColNames() {
			String[] s = new String[col];
			for (int c = 0; c < col; c++) {
				if (c == 0)
					s[c] = Messages.getString("SimView.csSalience"); //$NON-NLS-1$
				else if (col == 2)
					s[c] = Messages.getString("SimView.value"); //$NON-NLS-1$
				else
					s[c] = Messages.getString("SimView.phaseSpace") + (c + 1); //$NON-NLS-1$
			}
			return s;
		}

		/**
		 * Initializes and configures the table with some initial values.
		 * 
		 * @param iniValues
		 *            . If true, we initialize all variables to "" or by default
		 *            without taking into account last values of cues
		 */
		public void setValuesTable(boolean iniValues) {
			// if data had some values before, then reuse them
			Vector data2 = new Vector((Vector) getData().clone());
			Vector data1 = new Vector();
			data = new Vector();

			try {
				row = model.getNumberAlphaCues();
				col = 2;
				columnNames = getColNames();

				TreeMap<String, Float> tm = model.getAlphaCues();
				Iterator<String> it = tm.keySet().iterator();
				// Split into two lists, sort the list of compounds
				// by interface name not letter
				List<String> cueNames = new ArrayList<String>();
				TreeMap<String, String> configuralNames = new TreeMap<String, String>();
				while(it.hasNext()) {
					String pair = it.next();
					if((isSetConfiguralCompounds() || model
							.isSerialConfigurals())
							&& model.getConfigCuesNames()
									.containsKey(pair)) {
						String compoundName = model.getConfigCuesNames().get(pair);
						String interfaceName = "c(" + compoundName + ")";
						configuralNames.put(interfaceName, pair);
					} else {
						//Modified by Niklas Kokkola 2014 to disregard US from CS list.
						boolean isUS = false;
						for (String usName : USNames.getNames()) {
							if (pair.equals(usName)) {
								isUS = true;
							}
						}
						if (!isUS) {cueNames.add(pair);}
					}
				}				
				cueNames.addAll(configuralNames.values());
				it = cueNames.iterator();
				while (it.hasNext()) {
					String pair = it.next();
					// Disregard context cues, alphas for them are set elsewhere
					if (!Context.isContext(pair)) {

						if (pair.length() == 1) {
							Object record[] = new Object[col];
							int isInValues = -1;
							boolean configuralEmpty = false;

							for (int c = 0; c < col; c++) {
								// cue name
								if (c == 0) {
									record[c] = pair + "_s";
									// Alberto Fernandez August-2011
									if ((isSetConfiguralCompounds() || model
											.isSerialConfigurals())
											&& model.getConfigCuesNames()
													.containsKey(pair)) {
										String compoundName = model
												.getConfigCuesNames().get(pair);
										// String interfaceName = "�(" +
										// compoundName + ")";
										String interfaceName = "c(" + compoundName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
										record[c] = interfaceName;
									}
									// isInValues = isInValuesTable(data2,pair);
									isInValues = isInValuesTable(data2,
											(String) record[c]);
								}
								// cue value
								// If it exists already --> last value from
								// data2
								// else if (isInValues>-1 && !iniValues) {
								else if (isInValues > -1
										&& !iniValues
										&& !(((String) record[0]).length() > 1 && ((Object[]) data2
												.get(isInValues))[c] == "")) { //$NON-NLS-1$
									record[c] = ((Object[]) data2
											.get(isInValues))[c];
								}
								// Alberto Fernandez Sept-2011
								// If it didnt exist --> if c(AB) ==> A*B
								// otherwise default value
								else if (((String) record[0]).length() > 1 + "_s".length()) {

									// find record[0]= c(AB)
									Float val = 1.0f;
									boolean found, missing = false;
									String compound = ((String) record[0])
											.substring(2, ((String) record[0])
													.length() - 1);
									if (compound.startsWith(Simulator.OMEGA
											+ "")) { //$NON-NLS-1$
										compound = compound.substring(1);
									}
									// Filter out dashes in compound name
									compound = compound.replaceAll(
											ConfiguralCS.SERIAL_SEP, "");
									// Dedupe
									Set<Character> compoundSet = new HashSet<Character>();
									for (char cs : compound.toCharArray()) {
										compoundSet.add(cs);
									}
									StringBuilder sb = new StringBuilder();
									for (char cs : compoundSet) {
										sb.append(cs);
									}
									compound = sb.toString();

									String cue;

                                    float maxVal = 0;
                                    float minVal = Float.POSITIVE_INFINITY;
                                    List<Float> alphas = new ArrayList<Float>();

									for (int i = 0; i < compound.length(); i++) {
										cue = compound.substring(i, i + 1);
										// find cue in Vector data2
										found = false;
										missing = false; // there is no value
															// for current cue

										if (Context.isContext(cue)) {
                                            alphas.add(model.getContexts().get(cue)
                                                    .getAlphaR());
											maxVal = Math.max(maxVal, model.getContexts().get(cue)
													.getAlphaR());
                                            minVal = Math.min(minVal, model.getContexts().get(cue)
                                                    .getAlphaR());
											found = true;
										} else {

											for (int j = 0; j < data2.size(); j++) {
												Object cue_value_pair[] = (Object[]) data2
														.get(j);
												if (cue.equals(cue_value_pair[0])) {
													String s = (String) cue_value_pair[1];
													if (s != "") { //$NON-NLS-1$
                                                        alphas.add(Float.parseFloat(s));
														//maxVal = Math.max(maxVal, Float.parseFloat(s));
                                                        //minVal = Math.min(minVal, Float.parseFloat(s));
														found = true;
														break;
													}
												}
											}
										}
										if (!found) {
											val *= 0.2f;
										}
									}
									if (!missing) { // found) {
                                        Collections.sort(alphas);
                                        try {
                                            maxVal = alphas.get(alphas.size() - 1);
                                        } catch(ArrayIndexOutOfBoundsException e) {
                                            maxVal = 0.75f;
                                        }
                                        try {
                                            minVal = alphas.get(alphas.size() - 2);
                                        } catch(ArrayIndexOutOfBoundsException e) {
                                            minVal = 0.0001f;
                                        }
										record[c] = (new DecimalFormat("#.##########")).format(maxVal * minVal);
									} else {
										record[c] = ""; //$NON-NLS-1$
									}
								} else {
									record[c] = "0.3"; //$NON-NLS-1$
								}
							}
							data1.add(record);
						}
					}
					setData(data1);
					fireTableChanged(null); // notify everyone that we have a
											// new table.
				}
			} catch (Exception e) {
				setData(new Vector()); // blank it out and keep going.
				e.printStackTrace();
			}
		}
	}

	
	class CSVariableTableModel extends ValuesTableModel {

		/**
		 * ValuesTableModel's Constructor method.
		 */
		public CSVariableTableModel() {
			super();
		}

		/**
		 * Return the names of the table's column names.
		 * 
		 * @return an array with the table's column names.
		 */
		private String[] getColNames() {
			String[] s = new String[col];
			for (int c = 0; c < col; c++) {
				if (c == 0)
					s[c] = Messages.getString("SimView.cscsAlpha"); //$NON-NLS-1$
				else if (col == 2)
					s[c] = Messages.getString("SimView.value"); //$NON-NLS-1$
				else
					s[c] = Messages.getString("SimView.phaseSpace") + (c + 1); //$NON-NLS-1$
			}
			return s;
		}

		/**
		 * Initializes and configures the table with some initial values.
		 * 
		 * @param iniValues
		 *            . If true, we initialize all variables to "" or by default
		 *            without taking into account last values of cues
		 */
		public void setValuesTable(boolean iniValues) {
			// if data had some values before, then reuse them
			Vector data2 = new Vector((Vector) getData().clone());
			Vector data1 = new Vector();
			data = new Vector();

			try {
				row = model.getNumberAlphaCues();
				col = 2;
				columnNames = getColNames();

				TreeMap<String, Float> tm = model.getAlphaCues();
				Iterator<String> it = tm.keySet().iterator();
				// Split into two lists, sort the list of compounds
				// by interface name not letter
				List<String> cueNames = new ArrayList<String>();
				TreeMap<String, String> configuralNames = new TreeMap<String, String>();
				while(it.hasNext()) {
					String pair = it.next();
					if((isSetConfiguralCompounds() || model
							.isSerialConfigurals())
							&& model.getConfigCuesNames()
									.containsKey(pair)) {
						String compoundName = model.getConfigCuesNames().get(pair);
						String interfaceName = "c(" + compoundName + ")";
						configuralNames.put(interfaceName, pair);
					} else {
						//Modified by Niklas Kokkola 2014 to disregard US from CS list.
						boolean isUS = false;
						for (String usName : USNames.getNames()) {
							if (pair.equals(usName)) {
								isUS = true;
							}
						}
						if (!isUS) {cueNames.add(pair);}
					}
				}				
				cueNames.addAll(configuralNames.values());
				it = cueNames.iterator();
				while (it.hasNext()) {
					String pair = it.next();
					// Disregard context cues, alphas for them are set elsewhere
					if (!Context.isContext(pair)) {
						if (pair.split("_\u03B1n")[0].length() == 1) {
							Object record[] = new Object[col];
							int isInValues = -1;
							boolean configuralEmpty = false;

							for (int c = 0; c < col; c++) {
								// cue name
								if (c == 0) {
									record[c] = pair;
									// Alberto Fernandez August-2011
									if ((isSetConfiguralCompounds() || model
											.isSerialConfigurals())
											&& model.getConfigCuesNames()
													.containsKey(pair)) {
										String compoundName = model
												.getConfigCuesNames().get(pair);
										// String interfaceName = "�(" +
										// compoundName + ")";
										String interfaceName = "c(" + compoundName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
										record[c] = interfaceName;
									}
									// isInValues = isInValuesTable(data2,pair);
									isInValues = isInValuesTable(data2,
											 (((String) record[c]) + "_\u03B1n"));
									
									record[c] += "_\u03B1n";
								}
								// cue value
								// If it exists already --> last value from
								// data2
								// else if (isInValues>-1 && !iniValues) {
								else if (isInValues > -1
										&& !iniValues
										&& !((((String) record[0]).split("_\u03B1n")[0]).length() > 1 && ((Object[]) data2
												.get(isInValues))[c] == "")) { //$NON-NLS-1$
									record[c] = ((Object[]) data2
											.get(isInValues))[c];
								}
								// Alberto Fernandez Sept-2011
								// If it didnt exist --> if c(AB) ==> A*B
								// otherwise default value
								else if ((((String) record[0]).split("_\u03B1n")[0]).length() > 1) {

									// find record[0]= c(AB)
									Float val = 1.0f;
									boolean found, missing = false;
									String compound = ((String) record[0])
											.substring(2, ((String) record[0])
													.length() - 1);
									if (compound.startsWith(Simulator.OMEGA
											+ "")) { //$NON-NLS-1$
										compound = compound.substring(1);
									}
									// Filter out dashes in compound name
									compound = compound.replaceAll(
											ConfiguralCS.SERIAL_SEP, "");
									// Dedupe
									Set<Character> compoundSet = new HashSet<Character>();
									for (char cs : compound.toCharArray()) {
										compoundSet.add(cs);
									}
									StringBuilder sb = new StringBuilder();
									for (char cs : compoundSet) {
										sb.append(cs);
									}
									compound = sb.toString();

									String cue;

                                    float maxVal = 0;
                                    float minVal = Float.POSITIVE_INFINITY;
                                    List<Float> alphas = new ArrayList<Float>();

									for (int i = 0; i < compound.length(); i++) {
										cue = compound.substring(i, i + 1);
										// find cue in Vector data2
										found = false;
										missing = false; // there is no value
															// for current cue

										if (Context.isContext(cue)) {
                                            alphas.add(model.getContexts().get(cue)
                                                    .getAlphaR());
											maxVal = Math.max(maxVal, model.getContexts().get(cue)
													.getAlphaR());
                                            minVal = Math.min(minVal, model.getContexts().get(cue)
                                                    .getAlphaR());
											found = true;
										} else {

											for (int j = 0; j < data2.size(); j++) {
												Object cue_value_pair[] = (Object[]) data2
														.get(j);
												if (cue.equals(cue_value_pair[0])) {
													String s = (String) cue_value_pair[1];
													if (s != "") { //$NON-NLS-1$
                                                        alphas.add(Float.parseFloat(s));
														//maxVal = Math.max(maxVal, Float.parseFloat(s));
                                                        //minVal = Math.min(minVal, Float.parseFloat(s));
														found = true;
														break;
													}
												}
											}
										}
										if (!found) {
											val *= 0.2f;
										}
									}
									if (!missing) { // found) {
                                        Collections.sort(alphas);
                                        try {
                                            maxVal = alphas.get(alphas.size() - 1);
                                        } catch(ArrayIndexOutOfBoundsException e) {
                                            maxVal = 0.4f;
                                        }
                                        try {
                                            minVal = alphas.get(alphas.size() - 2);
                                        } catch(ArrayIndexOutOfBoundsException e) {
                                            minVal = 0.0001f;
                                        }
										record[c] = (new DecimalFormat("#.##########")).format(maxVal * minVal);
									} else {
										record[c] = ""; //$NON-NLS-1$
									}
								} else {
									record[c] = "0.4"; //$NON-NLS-1$
								}
							}
							data1.add(record);
						}
					}
					setData(data1);
					fireTableChanged(null); // notify everyone that we have a
											// new table.
				}
			} catch (Exception e) {
				setData(new Vector()); // blank it out and keep going.
				e.printStackTrace();
			}
		}
	}
	/*
	 * Overwrite the names alpha, lambda and beta to the corresponding greek
	 * characters in the ValuesTable
	 */
	private static class GreekRenderer extends DefaultTableCellRenderer {
		@Override
		public void setText(String name) {
			super.setText(GreekSymbol.getSymbol(name));
		}
	}
	

	/** This class is the other values' table model */
	class OtherValuesTableModel extends ValuesTableModel {

		/**
		 * 
		 */
		private static final String GAMMA = "gamma"; //$NON-NLS-1$
		private static final String STD = "CV"; //$NON-NLS-1$
		private static final String TAU1 = "\u03c4" + "1"; //$NON-NLS-1$
		private static final String TAU2 = "\u03c4" + "2"; //$NON-NLS-1$
		private static final String VARTHETA = "\u03d1"; //$NON-NLS-1$
		private static final String selfPred = "Self Discount"; //$NON-NLS-1$
		private static final String VARSIGMA = "\u03c2";
		private static final String cVal = "linear c";
		private static final String recency = "US \u03C1";
		private static final String csRecency = "CS \u03C1";
		private static final String inte = "integration";
		private static final String common = "common";

		private static final String setsize = "setsize";
		private static final String cscLikeness = "skew";
		private static final String usBoost = "\u03C6";
		private static final String csScalar = "Wave Constant";
		private static final String usScalar = "US Scalar Constant";
		private static final String threshold = "Threshold";
		private static final String salience = "Salience Weight";
		;
		private static final String b = "b";
		// Modified to use TD variables (lambda, gamma)
		private final String[] names = {recency,csRecency,common,VARTHETA,csScalar,b};

		/**
		 * ValuesTableModel's Constructor method.
		 */
		public OtherValuesTableModel(int col) {
			super();
		}

		/**
		 * Return the names of the table's column names.
		 * 
		 * @return an array with the table's column names.
		 */
		private String[] getColNames() {
			String[] s = new String[col];
			for (int c = 0; c < col; c++) {
				if (c == 0)
					s[c] = Messages.getString("SimView.others"); //$NON-NLS-1$
				else if (col == 2)
					s[c] = Messages.getString("SimView.value"); //$NON-NLS-1$
				else
					s[c] = Messages.getString("SimView.phaseSpace") + c; //$NON-NLS-1$
			}
			return s;
		}

		/*
		 * Initializes and configures the table with some initial values.
		 * 
		 * @param iniValues. If true, we initialize all variables to "" or by
		 * default without taking into account last values of cues
		 */
		public void setInitialValuesTable() {
			Vector data1 = new Vector();
			col = 2;
			row = names.length;
			columnNames = getColNames();
			
			try {
				for (int r = 0; r < row; r++) { // row ser� 4 (betas y lambdas)
					Object record[] = new Object[col];
					record[0] = names[r];
					for (int c = 1; c < col; c++) {
						// Modified to use TD variables
						if (((String) record[0]).indexOf(STD) != -1
								&& c == 1)
							record[c] = "6"; //$NON-NLS-1$*/
						else if (((String) record[0]).indexOf(VARTHETA) != -1
								&& c == 1)
							record[c] = "0.95"; //$NON-NLS-1$
						else if (((String) record[0]).indexOf(selfPred) != -1
								&& c == 1)
							record[c] = "0.5"; //$NON-NLS-1$
						else if (((String) record[0]).indexOf(inte) != -1
								&& c == 1) 
							record[c] = "0.01"; //$NON-NLS-1$
						else if (((String) record[0]).indexOf(recency) != -1
								&& c == 1) 
							record[c] = "0.01"; //$NON-NLS-1$
						else if (((String) record[0]).indexOf(common) != -1
								&& c == 1) 
							record[c] = "0.1"; //$NON-NLS-1$
						else if (((String) record[0]).indexOf(csScalar) != -1
								&& c == 1) 
							record[c] = "2"; //$NON-NLS-1$
						else if (((String) record[0]).indexOf(usScalar) != -1
								&& c == 1) 
							record[c] = "1"; //$NON-NLS-1$
						
						else if (((String) record[0]).indexOf(csRecency) != -1
								&& c == 1) 
							record[c] = "0.01";
						else if (((String) record[0]).indexOf(b) != -1
								&& c == 1) 
							record[c] = "0.75";
						else record[c] = "";
					}
					data1.addElement(record);
				}
				setData(data1);
				fireTableChanged(null); // notify everyone that we have a new
										// table.
			} catch (Exception e) {
				setData(new Vector());
				e.printStackTrace();
			}
		}

		/*
		 * Initializes and configures the table with some initial values.
		 * 
		 * @param iniValues. If true, we initialize all variables to "" or by
		 * default without taking into account last values of cues
		 */
		public void setValuesTable(boolean iniValues, boolean allphases) {
			// if data had some values before, then reuse them
			Vector data2 = new Vector((Vector) getData().clone());

			Object record2[] = (Object[]) data2.firstElement();
		}

	}

	/** This class is the phases' table model */
	class PhasesTableModel extends ValuesTableModel {

		/**
		 * PhasesTableModel's Constructor method.
		 */
		public PhasesTableModel() {
			super();
		}

		public void addGroup() {
			row = model.getGroupNo();
			Object record[] = new Object[col];
			for (int c = 0; c < col; c++) {
				if (c == 0)
					record[c] = Messages.getString("SimView.group") + row; //$NON-NLS-1$
				else if (c % 5 == 1)
					record[c] = ""; //$NON-NLS-1$
				else if (c % 5 == 2)
					record[c] = new ContextConfig(contextAlphaR,contextAlphaN,contextSalience);
				else if (c % 5 == 3)
					record[c] = new Boolean(false);
				else if (c % 5 == 4)
					record[c] = new TimingConfiguration();
				else if (c % 5 == 0)
					record[c] = new ITIConfig(0);
			}

			data.addElement(record);
			fireTableChanged(null); // notify everyone that we have a new table.
			if (!isUseContext()) {
				removeOmegaPhases();
			}
		}

		/*
		 * Add a new column to the vector data
		 */
		public void addPhase() {
			col = model.getPhaseNo() * 5 + 1;
			columnNames = getColNames();
			Vector newData = new Vector();
			for (Iterator it = data.iterator(); it.hasNext();) {
				Object record[] = new Object[col];
				Object[] oldRecord = (Object[]) it.next();
				System.arraycopy(oldRecord, 0, record, 0, oldRecord.length);
				record[record.length - 5] = "";
				record[record.length - 4] = new ContextConfig(contextAlphaR,contextAlphaN,contextSalience);
				record[record.length - 3] = new Boolean(false);
				record[record.length - 2] = new TimingConfiguration();
				record[record.length - 1] = new ITIConfig(0);
				newData.add(record);
			}
			data = newData;
			fireTableChanged(null);
			if (!isUseContext()) {
				removeOmegaPhases();
			}
		}

		/**
		 * Return the names of the table's column names.
		 * 
		 * @return an array with the table's column names.
		 */
		private String[] getColNames() {
			String[] s = new String[col];
			for (int c = 0; c < col; c++) {
				if (c == 0)
					s[c] = GROUP_NAME;
				else if (c % 5 == 0)
					s[c] = ITI;
				else if (c % 5 == 1)
					s[c] = Messages.getString("SimView.phaseSpace") + (c / 5 + 1); //$NON-NLS-1$
				else if (c % 5 == 2)
					s[c] = CONTEXT;
				else if (c % 5 == 3)
					s[c] = RANDOM;
				else if (c % 5 == 4)
					s[c] = TIMING;
			}
			return s;
		}

		
		@Override
		public boolean isCellEditable(int row, int col) {
			return true;
		}

		
		public void removeGroup() {
			row = model.getGroupNo();
			data.remove(data.size() - 1);
			fireTableChanged(null);
			if (!isUseContext()) {
				removeOmegaPhases();
			}
		}

		public void removePhase() {
			if (col > 5) {
				col = model.getPhaseNo() * 5 + 1;
				columnNames = getColNames();
				Vector newData = new Vector();
				for (Iterator it = data.iterator(); it.hasNext();) {
					Object record[] = new Object[col];
					Object[] oldRecord = (Object[]) it.next();
					System.arraycopy(oldRecord, 0, record, 0, record.length);
					newData.add(record);
				}
				data = newData;
				fireTableChanged(null); // notify everyone that we have a new
										// table.
			}
			if (!isUseContext()) {
				removeOmegaPhases();
			}
		}

		/*
		 * Initializes and configures the table with some initial values.
		 */
		public void setPhasesTable() {
			clearHidden();
			data = new Vector();
			try {
				col = model.getPhaseNo() * 5 + 1;
				row = model.getGroupNo();
				columnNames = getColNames();

				for (int r = 0; r < row; r++) {
					Object record[] = new Object[col];
					for (int c = 0; c < col; c++) {
						if (c == 0)
							record[c] = Messages.getString("SimView.group") + (r + 1); //$NON-NLS-1$
						else if (c % 5 == 0)
							record[c] = new ITIConfig(0);
						else if (c % 5 == 1)
							record[c] = ""; //$NON-NLS-1$
						else if (c % 5 == 2)
							record[c] = new ContextConfig();
						else if (c % 5 == 3)
							record[c] = new Boolean(false);
						else if (c % 5 == 4)
							record[c] = new TimingConfiguration();
					}
					data.addElement(record);
				}
				fireTableChanged(null); // notify everyone that we have a new
										// table.
				if (!isUseContext()) {
					removeOmegaPhases();
				}
			} catch (Exception e) {
				data = new Vector(); // blank it out and keep going.
				e.printStackTrace();
			}
		}
	}

	/** This class is the values' table model */
	class USValuesTableModel extends ValuesTableModel {

		/**
		 * US Names
		 */
		//private static final String LAMBDA = "lambda"; //$NON-NLS-1$
		private static final String ALPHA_PLUS = "alpha+"; //$NON-NLS-1$

		private static final String US_BOOST = "reinforcer cff"; //$NON-NLS-1$
		// Modified to use TD variables (lambda, gamma)
		private String[] USnames = { ALPHA_PLUS};// ,
		private String[] defaultValues = {"0.2"};																		// LAMBDA_MINUS};
		private String[] USvalues = { "0.2"}; //Added beta- value //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		/**
		 * ValuesTableModel's Constructor method.
		 */
		public USValuesTableModel(int col) {
			super();
		}
		
		public void setUSNames(ArrayList<String> names) {
			USnames = new String[names.size()];
			defaultValues = new String[names.size()];
			int count = 0;
			for (String name : names) {
				USnames[count] = name + " - " + "\u03B1+";
				defaultValues[count] = "0.2";
				//USnames[2*count + 1] = name + " - " + "\u03BB";
				count++;
			}
			if (names.size() == 1) {
				USnames = new String[]{ ALPHA_PLUS};
			}
			}
		public void setUSValues() {
			USvalues = new String[USnames.length];
			for (int i = 0; i < USvalues.length; i++) {
				USvalues[i] = defaultValues[i];
			}
			if (USnames.length > 1)setInitialValuesTable();
		}

		/*
		 * Add a new column to the vector data
		 */
		public void addPhases() {
			col = model.getPhaseNo() + 1;
			columnNames = getColNames();
			Vector newData = new Vector();
			for (Iterator it = data.iterator(); it.hasNext();) {
				Object record[] = new Object[col];
				Object[] oldRecord = (Object[]) it.next();
				System.arraycopy(oldRecord, 0, record, 0, oldRecord.length);
				for (int i = oldRecord.length; i < col; i++)
					record[i] = ""; //$NON-NLS-1$
				newData.add(record);
			}
			data = newData;
			fireTableChanged(null); // notify everyone that we have a new table.
		}

		/**
		 * Return the names of the table's column names.
		 * 
		 * @return an array with the table's column names.
		 */
		private String[] getColNames() {
			String[] s = new String[col];
			for (int c = 0; c < col; c++) {
				if (c == 0)
					s[c] = Messages.getString("SimView.uscsAlpha"); //$NON-NLS-1$
				else if (col == 2)
					s[c] = Messages.getString("SimView.value"); //$NON-NLS-1$
				else
					s[c] = Messages.getString("SimView.phaseSpace") + c; //$NON-NLS-1$
			}
			return s;
		}

		/*
		 * Remove the last column of the vector data
		 */
		public void removePhases(int phases) {
			col = phases + 1;
			columnNames = getColNames();
			Vector newData = new Vector();
			for (Iterator it = data.iterator(); it.hasNext();) {
				Object record[] = new Object[col];
				Object[] oldRecord = (Object[]) it.next();
				System.arraycopy(oldRecord, 0, record, 0, record.length);
				newData.add(record);
			}
			setData(newData);
			fireTableChanged(null); // notify everyone that we have a new table.
		}

		/*
		 * Initializes and configures the table with some initial values.
		 * 
		 * @param iniValues. If true, we initialize all variables to "" or by
		 * default without taking into account last values of cues
		 */
		public void setInitialValuesTable() {
			Vector data1 = new Vector();
			col = 2;
			row = USvalues.length;
			columnNames = getColNames();
			if (!(USnames.length > 0)) {

				USnames = new String[]{ ALPHA_PLUS};
			}
			try {
				for (int r = 0; r < row; r++) { // row ser� 4 (betas y lambdas)
					Object record[] = new Object[col];
					record[0] = USnames[r];
					for (int c = 1; c < col; c++) {
						record[c] = USvalues[r];}
					data1.addElement(record);
				}
				setData(data1);
				fireTableChanged(null); // notify everyone that we have a new
										// table.
			} catch (Exception e) {
				setData(new Vector()); // blank it out and keep going.
				e.printStackTrace();
			}
		}
		

		/*
		 * Initializes and configures the table with some initial values.
		 * 
		 * @param iniValues. If true, we initialize all variables to "" or by
		 * default without taking into account last values of cues
		 */
		public void setValuesTable(boolean iniValues, boolean allphases) {
			// if data had some values before, then reuse them
			Vector data2 = new Vector((Vector) getData().clone());
			if (allphases) {
				Object record2[] = (Object[]) data2.firstElement();
				if (record2.length <= (model.getPhaseNo() + 1))
					addUSPhases();
				else
					removePhases(model.getPhaseNo());
			} else
				removeUSPhases();
		}

	}
	
	class USSalienceTableModel extends ValuesTableModel {

		/**
		 * US Names
		 */
		//private static final String LAMBDA = "lambda"; //$NON-NLS-1$
		private static final String BETA = "\u03B2"; //$NON-NLS-1$
		private static final String OMICRON = "+_s"; //$NON-NLS-1$
		// Modified to use TD variables (lambda, gamma)
		private String[] USnames = { BETA};// ,
		private String[] defaultValues = {"0.9"};																		// LAMBDA_MINUS};
		private String[] USvalues = {"0.9"}; //Added beta- value //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		/**
		 * ValuesTableModel's Constructor method.
		 */
		public USSalienceTableModel(int col) {
			super();
		}
		
		
		public void setUSNames(ArrayList<String> names) {
			USnames = new String[names.size()];
			defaultValues = new String[names.size()];
			int count = 0;
			
			for (String name : names) {
				USnames[count] = name + " - " + "\u03B2";
				//USnames[count*2+1] = name + " - " + "+_s";
				defaultValues[count] = "0.9";
				//defaultValues[count*2+1] = "0.1";
				count++;
			}
			if (names.size() == 1) {
				USnames = new String[]{ BETA};
			}
			}
		public void setUSValues() {
			USvalues = new String[USnames.length];
			for (int i = 0; i < USnames.length; i++) {
				USvalues[i] = defaultValues[i];
				//USvalues[i] = defaultValues[i];
			}
			if (USnames.length > 1)setInitialValuesTable();
		}

		/*
		 * Add a new column to the vector data
		 */
		public void addPhases() {
			col = model.getPhaseNo() + 1;
			columnNames = getColNames();
			Vector newData = new Vector();
			for (Iterator it = data.iterator(); it.hasNext();) {
				Object record[] = new Object[col];
				Object[] oldRecord = (Object[]) it.next();
				System.arraycopy(oldRecord, 0, record, 0, oldRecord.length);
				for (int i = oldRecord.length; i < col; i++)
					record[i] = ""; //$NON-NLS-1$
				newData.add(record);
			}
			data = newData;
			fireTableChanged(null); // notify everyone that we have a new table.
		}

		/**
		 * Return the names of the table's column names.
		 * 
		 * @return an array with the table's column names.
		 */
		private String[] getColNames() {
			String[] s = new String[col];
			for (int c = 0; c < col; c++) {
				if (c == 0)
					s[c] = Messages.getString("SimView.usSalience"); //$NON-NLS-1$
				else if (col == 2)
					s[c] = Messages.getString("SimView.value"); //$NON-NLS-1$
				else
					s[c] = Messages.getString("SimView.phaseSpace") + c; //$NON-NLS-1$
			}
			return s;
		}

		/*
		 * Remove the last column of the vector data
		 */
		public void removePhases(int phases) {
			col = phases + 1;
			columnNames = getColNames();
			Vector newData = new Vector();
			for (Iterator it = data.iterator(); it.hasNext();) {
				Object record[] = new Object[col];
				Object[] oldRecord = (Object[]) it.next();
				System.arraycopy(oldRecord, 0, record, 0, record.length);
				newData.add(record);
			}
			data = newData;
			fireTableChanged(null); // notify everyone that we have a new table.
		}

		/*
		 * Initializes and configures the table with some initial values.
		 * 
		 * @param iniValues. If true, we initialize all variables to "" or by
		 * default without taking into account last values of cues
		 */
		public void setInitialValuesTable() {
			Vector data1 = new Vector();
			col = 2;
			row = USvalues.length;
			columnNames = getColNames();
			if (!(USnames.length > 0)) {

				USnames = new String[]{ BETA,OMICRON};
			}
			try {
				for (int r = 0; r < row; r++) { // row ser� 4 (betas y lambdas)
					Object record[] = new Object[col];
					record[0] = USnames[r];
					for (int c = 1; c < col; c++) {
						record[c] = USvalues[r];}
					data1.addElement(record);
				}
				setData(data1);
				fireTableChanged(null); // notify everyone that we have a new
										// table.
			} catch (Exception e) {
				setData(new Vector()); // blank it out and keep going.
				e.printStackTrace();
			}
		}

		/*
		 * Initializes and configures the table with some initial values.
		 * 
		 * @param iniValues. If true, we initialize all variables to "" or by
		 * default without taking into account last values of cues
		 */
		public void setValuesTable(boolean iniValues, boolean allphases) {
			// if data had some values before, then reuse them
			Vector data2 = new Vector((Vector) getData().clone());
			if (allphases) {
				Object record2[] = (Object[]) data2.firstElement();
				if (record2.length <= (model.getPhaseNo() + 1))
					addUSPhases();
				else
					removePhases(model.getPhaseNo());
			} else
				removeUSPhases();
		}

	}


	// Alberto Fern�ndez July-2011
	// Font styles
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private float screenwidth = (float) screenSize.getWidth();
	private float screenheight = (float) screenSize.getHeight();
	public static final String TABLE_FONT_NAME = "Helvetica";
	public static final int TABLE_FONT_STYLE = 1;
	boolean isWindows = System.getProperty("os.name").toUpperCase().contains("WINDOWS") ;
	private int offset = !isWindows ? 200 : 0;
	private float offset2 = !isWindows ? 0.95f : 0.95f;
	int val = 1800;
	public int TABLE_FONT_SIZE = screenwidth > val ? (int) Math.max(12, Math.round(screenwidth/240.0d)) : 12;
	public int INITIAL_WIDTH = (screenwidth > val) ? (int) Math.round(screenwidth/(2*offset2)) : offset != 0 ? 840 : 980;
	public int INITIAL_HEIGHT = (screenwidth > val) ? (int) Math.round(screenheight/1.02) : 980;
	public int INITIAL_H = (screenwidth > val) ? (int) Math.round(screenheight/(1.02d)*offset2) : offset != 0 ? 840 : 950;

	public int TABLE_ROW_HEIGHT = screenwidth > val ? INITIAL_HEIGHT/60 : 17;
	private static final float AligmentY = 0;
	private SimModel model;

	/** Strings. **/
	public static final String GROUP_NAME = Messages.getString("SimView.groupName"); //$NON-NLS-1$

	public static final String ITI = Messages.getString("SimView.ITI"); //$NON-NLS-1$
	public static final String TIMING = Messages.getString("SimView.timing"); //$NON-NLS-1$
	public static final String CONTEXT = Messages.getString("SimView.context"); //$NON-NLS-1$ 
	public static final String RANDOM = Messages.getString("SimView.random"); //$NON-NLS-1$
	public static final String SESSIONS = Messages.getString("SimView.sessions"); //$NON-NLS-1$
	

	private Container cp;
	private JMenuBar menuBar;

	private JMenu menuFile, menuDesignSettings, menuHelp;
	private JMenuItem menuItemNew, menuItemOpen, menuItemSave, menuItemExport,
			menuItemQuit, menuItemComb, menuItemGuide, menuItemAbout, menuItemRPM;
	private JCheckBoxMenuItem menuItemUSAcrossPhases, menuItemSetCompound,menuItemSetConfiguralCompounds; // menuItemSetConfiguralCompounds by
	private JMenuItem menuItemCtx;
											// Alberto Fern�ndez August-2011
	private JPanel mainPanel, bottomPanel;
	private JButton setVariablesBut, clearBut, runBut, dispGraphBut,
			addPhaseBut, removePhaseBut, addGroupBut, removeGroupBut,gButton;
	public  JButton intensityButton;
	private JScrollPane phasesScroll, CSValuesScroll, USValuesScroll, DDValuesScroll,CSVariableScroll,
			outputScroll;
	private JTable phasesTable, CSValuesTable, CSVariableTable, USValuesTable, DDValuesTable;

	private PhasesTableModel phasesTableModel;
	private CSValuesTableModel CSValuesTableModel;
	private CSVariableTableModel CSVariableTableModel;
	private USValuesTableModel USValuesTableModel;
	private JTextArea outputArea;
	private JLabel bottomLabel;
	private boolean isUSAcrossPhases, isSetCompound = true, isSetConfiguralCompounds;
	private boolean useActivationGraph = false;
	private JMenuItem menuItemContext;
	private JScrollPane otherValuesScroll;
	private OtherValuesTableModel otherTableModel;
	private JTable otherValuesTable;
	private JMenuItem menuItemThreshold;
	private JMenu menuMeans, menuDistributions;
	private JMenuItem menuItemGeometric, menuItemArithmetic;
	private JMenuItem menuItemExp, menuItemUnif;
	private JMenuItem menuItemCurve, menuItemUSCV;
	private JMenuItem menuItemContextAcrossPhases;
	private JCheckBoxMenuItem menuItemCsc;
	//private AbstractButton menuItemTimestep;
	/** Tracking for hidden context columns. **/
	private Map<TableColumn, Integer> hiddenColumns;
	/** Default context salience & salience for uniform contexts. **/
	private float contextAlphaR;
	private JRadioButtonMenuItem menuItemSingleContext;
	private JMenu menuContext;
	private JMenu menuContextReset;
	private JMenu menuProcSettings;
	/** Eligibility trace menus. **/
	private JRadioButtonMenuItem menuItemBoundedTrace;
	private JRadioButtonMenuItem menuItemAccumTrace;
	private JMenu menuTrace;

	private JRadioButtonMenuItem menuItemReplaceTrace;

	private JMenuItem menuItemVarComb;

	/** Timing per phase. **/
	private JCheckBoxMenuItem menuItemTimingPerTrial;
	
	/** Timing per phase. **/
	private JCheckBox externalSave;

	/** Zero traces between trials. **/
	private JCheckBoxMenuItem zeroTraces;

	/** Consider serial compounds. **/
	private JCheckBoxMenuItem menuItemSerialCompounds;

	/** Restrict predictions to above zero. **/
	private JCheckBoxMenuItem menuItemRestrictPredictions;
	
	private JCheckBox waTimeCheckList,waTrialCheckList,presenceMeanCheckList,vGraphCheckList,trialResponseCheckList,timePredictionCheckList,contextResetCheckList
	,responseCheckList,componentStrengthCheckList,componentActivationCheckList,aniCheckList,contextCheckList,varyingVartheta,estherBox, salienceCheckList,componentPredictionCheckList,componentDeltaCheckList;
	public JCheckBox errorCheckList,floatingButtons;
    public static int activeRow = -1;
    public static int activeCol = -1;
    
    private int usNumber = 0;
    
    private JPanel resultPanel;
    private JPanel aboutPanel;
    private JTabbedPane tabs;
	private JTable elementValuesTable;
	private JScrollPane elementValuesScroll;
	private JMenu menuElementSettings;
	private JMenuItem menuItemCSCV;
	private JMenuItem menuItemSetSize;
	private JMenuItem menuItemCSScalar;
	private JMenuItem menuItemPersistence;
	public JCheckBox errorTrialCheckList;
	private CSSalienceTableModel CSSalienceTableModel;
	private JTable CSSalienceTable;
	private JScrollPane CSSalienceScroll;
	private USSalienceTableModel USSalienceTableModel;
	private JTable USSalienceTable;
	private JScrollPane USSalienceScroll;
	private float contextAlphaN;
	private float contextSalience;
	private JRadioButtonMenuItem menuItemNoReset;
	private AbstractButton menuItemReset;
	private JMenu menuRule;
	private JRadioButtonMenuItem menuR1;
	private JRadioButtonMenuItem menuR2;
	private JMenuItem menuItemAssoc;
	private JMenuItem menuItemEli;
    /**
	 * SimView's Constructor method.
	 * 
	 * @param m
	 *            - the SimModel Object, the model on the structure which
	 *            contains the groups with their phases and also some parameters
	 *            that are needed for the simulator.
	 */
	public SimView(SimModel m) {
        // ImageIcon icon =
        // createImageIcon("..simulator/R&W.png","");//R&W.png", "");
        // E.Mondragon 28 Sept 2011
        List<Image> icon = new ArrayList<Image>();
        icon.add(createImageIcon("/simulator/extras/icon_16.png", "")
                .getImage());
        icon.add(createImageIcon("/simulator/extras/icon_32.png", "")
                .getImage());
        icon.add(createImageIcon("/simulator/extras/icon_256.png", "")
                .getImage());
        icon.add(createImageIcon("/simulator/extras/icon_512.png", "")
                .getImage());
        this.setIconImages(icon);
		contextAlphaR = 0.25f;
		contextAlphaN = 0.2f;
		contextSalience = 0.07f;
		model = m;
		isUSAcrossPhases = false;
		isSetCompound = true;
		isSetConfiguralCompounds = false;
		cp = this.getContentPane();
		hiddenColumns = new LinkedHashMap<TableColumn, Integer>();

		cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));

		center();
		createMenu();
		createGUI2();

		// Modified by J Gray. Dec-2011
		this.setTitle(Messages.getString("SimView.title")); //$NON-NLS-1$
		// modified by Alberto Fernandez: 19 July 2011


		this.setSize(INITIAL_WIDTH, INITIAL_H);
		this.setLocation(this.getLocation().x,2);
	}

	/**
	 * Responds to the user when he presses a button.
	 */
	public void addButtonListeners(ActionListener event) {
		setVariablesBut.addActionListener(event);
		gButton.addActionListener(event);
		clearBut.addActionListener(event);
		runBut.addActionListener(event);
		dispGraphBut.addActionListener(event);
		intensityButton.addActionListener(event);
		waTimeCheckList.addActionListener(event);
		waTrialCheckList.addActionListener(event);
		vGraphCheckList.addActionListener(event);
		errorTrialCheckList.addActionListener(event);
		errorCheckList.addActionListener(event);
		floatingButtons.addActionListener(event);
		trialResponseCheckList.addActionListener(event);
		timePredictionCheckList.addActionListener(event);
		presenceMeanCheckList.addActionListener(event);
		responseCheckList.addActionListener(event);
		aniCheckList.addActionListener(event);
		contextCheckList.addActionListener(event);
		salienceCheckList.addActionListener(event);
		componentActivationCheckList.addActionListener(event);
		componentPredictionCheckList.addActionListener(event);
		componentDeltaCheckList.addActionListener(event);
		//componentStrengthCheckList.addActionListener(event);
		varyingVartheta.addActionListener(event);
		
		addPhaseBut.addActionListener(event);
		removePhaseBut.addActionListener(event);
		removeGroupBut.addActionListener(event);
		addGroupBut.addActionListener(event);
		estherBox.addActionListener(event);

	}

	/**
	 * Creates, initializes, configures and adds a values table into the frame.
	 * Columns and rows are depending on model's parameters.
	 */
	private void addCSValuesTable() {
		CSValuesTableModel = new CSValuesTableModel();

		CSValuesTable = new JTable(CSValuesTableModel) {
			@Override
			public boolean editCellAt(int row, int column, EventObject e) {
				boolean b = super.editCellAt(row, column, e);
				if (b) {
					TableCellEditor tce = getCellEditor(row, column);
					DefaultCellEditor dce = (DefaultCellEditor) tce;
					Component c = dce.getComponent();
					JTextComponent jtc = (JTextComponent) c;
					jtc.requestFocus();

					// Alberto Fernandez Oct-2011

					// This avoids to press a float enter
					/*
					 * InputMap inputMap = jtc.getInputMap(); KeyStroke key =
					 * KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
					 * inputMap.put(key, "none"); //$NON-NLS-1$
					 */

					jtc.addFocusListener(new java.awt.event.FocusAdapter() {
						@Override
						public void focusGained(java.awt.event.FocusEvent evt) {
							JTextComponent jtc = (JTextComponent) evt
									.getComponent();
							jtc.setCaretPosition(jtc.getCaretPosition());
						}

						@Override
						public void focusLost(java.awt.event.FocusEvent evt) {
							int col = CSValuesTable.getColumnCount();
							if (col > 0) {
								if (CSValuesTable.getCellEditor() != null) {
									CSValuesTable.getCellEditor()
											.stopCellEditing();
								}
							}
							// else; //take care about the other rows if you
							// need to.

						}
					});
				}
				return b;
			}
		};
		CSValuesTable.setDefaultRenderer(String.class, new GreekRenderer());
		CSValuesTableModel.setValuesTable(false);
		CSValuesTable.setCellSelectionEnabled(false);
		CSValuesTable.requestFocus();
		CSValuesTable.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				int row = CSValuesTable.rowAtPoint(p);
				String tip;
				
				
					tip = "CS initial variable alpha_r"; //$NON-NLS-1$
				
				CSValuesTable.setToolTipText(tip);
			}// end MouseMoved
		}); // end MouseMotionAdapter
		CSValuesTable.setFont(new Font(TABLE_FONT_NAME, TABLE_FONT_STYLE,
				TABLE_FONT_SIZE)); // Alberto Fern�ndez July-2011
		CSValuesTable.setRowHeight(TABLE_ROW_HEIGHT);
		CSValuesScroll = new JScrollPane(CSValuesTable);
	}
	
	private void addCSSalienceTable() {
		CSSalienceTableModel = new CSSalienceTableModel();

		CSSalienceTable = new JTable(CSSalienceTableModel) {
			@Override
			public boolean editCellAt(int row, int column, EventObject e) {
				boolean b = super.editCellAt(row, column, e);
				if (b) {
					TableCellEditor tce = getCellEditor(row, column);
					DefaultCellEditor dce = (DefaultCellEditor) tce;
					Component c = dce.getComponent();
					JTextComponent jtc = (JTextComponent) c;
					jtc.requestFocus();

					// Alberto Fernandez Oct-2011

					// This avoids to press a float enter
					/*
					 * InputMap inputMap = jtc.getInputMap(); KeyStroke key =
					 * KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
					 * inputMap.put(key, "none"); //$NON-NLS-1$
					 */

					jtc.addFocusListener(new java.awt.event.FocusAdapter() {
						@Override
						public void focusGained(java.awt.event.FocusEvent evt) {
							JTextComponent jtc = (JTextComponent) evt
									.getComponent();
							jtc.setCaretPosition(jtc.getCaretPosition());
						}

						@Override
						public void focusLost(java.awt.event.FocusEvent evt) {
							int col = CSSalienceTable.getColumnCount();
							if (col > 0) {
								if (CSSalienceTable.getCellEditor() != null) {
									CSSalienceTable.getCellEditor()
											.stopCellEditing();
								}
							}
							// else; //take care about the other rows if you
							// need to.

						}
					});
				}
				return b;
			}
		};
		CSSalienceTable.setDefaultRenderer(String.class, new GreekRenderer());
		CSSalienceTableModel.setValuesTable(false);
		CSSalienceTable.setCellSelectionEnabled(false);
		CSSalienceTable.requestFocus();
		CSSalienceTable.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				int row = CSSalienceTable.rowAtPoint(p);
				String tip;
				
				
					tip = "CS salience"; //$NON-NLS-1$
				
					CSSalienceTable.setToolTipText(tip);
			}// end MouseMoved
		}); // end MouseMotionAdapter
		CSSalienceTable.setFont(new Font(TABLE_FONT_NAME, TABLE_FONT_STYLE,
				TABLE_FONT_SIZE)); // Alberto Fern�ndez July-2011
		CSSalienceTable.setRowHeight(TABLE_ROW_HEIGHT);
		CSSalienceScroll = new JScrollPane(CSSalienceTable);
	}
	
	private void addCSVariableTable() {
		CSVariableTableModel = new CSVariableTableModel();

		CSVariableTable = new JTable(CSVariableTableModel) {
			@Override
			public boolean editCellAt(int row, int column, EventObject e) {
				boolean b = super.editCellAt(row, column, e);
				if (b) {
					TableCellEditor tce = getCellEditor(row, column);
					DefaultCellEditor dce = (DefaultCellEditor) tce;
					Component c = dce.getComponent();
					JTextComponent jtc = (JTextComponent) c;
					jtc.requestFocus();

					// Alberto Fernandez Oct-2011

					// This avoids to press a float enter
					/*
					 * InputMap inputMap = jtc.getInputMap(); KeyStroke key =
					 * KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
					 * inputMap.put(key, "none"); //$NON-NLS-1$
					 */

					jtc.addFocusListener(new java.awt.event.FocusAdapter() {
						@Override
						public void focusGained(java.awt.event.FocusEvent evt) {
							JTextComponent jtc = (JTextComponent) evt
									.getComponent();
							jtc.setCaretPosition(jtc.getCaretPosition());
						}

						@Override
						public void focusLost(java.awt.event.FocusEvent evt) {
							int col = CSVariableTable.getColumnCount();
							if (col > 0) {
								if (CSVariableTable.getCellEditor() != null) {
									CSVariableTable.getCellEditor()
											.stopCellEditing();
								}
							}
							// else; //take care about the other rows if you
							// need to.

						}
					});
				}
				return b;
			}
		};
		CSVariableTable.setDefaultRenderer(String.class, new GreekRenderer());
		CSVariableTableModel.setValuesTable(false);
		CSVariableTable.setCellSelectionEnabled(false);
		CSVariableTable.requestFocus();
		CSVariableTable.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				int row = CSVariableTable.rowAtPoint(p);
				String tip;
				
				
					tip = "CS initial variable alpha_n"; //$NON-NLS-1$
				
					CSVariableTable.setToolTipText(tip);
			}// end MouseMoved
		}); // end MouseMotionAdapter
		CSVariableTable.setFont(new Font(TABLE_FONT_NAME, TABLE_FONT_STYLE,
				TABLE_FONT_SIZE)); // Alberto Fern�ndez July-2011
		CSVariableTable.setRowHeight(TABLE_ROW_HEIGHT);
		CSVariableScroll = new JScrollPane(CSVariableTable);
	}

	/*
	 * Add a new group to the phasesTableModel
	 */
	public void addGroup() {
		phasesTableModel.addGroup();
		updatePhasesColumnsWidth();
	}

	/**
	 * Responds to the user when he presses a menu option.
	 */
	public void addMenuListeners(ActionListener event) {
		menuItemNew.addActionListener(event);
		menuItemOpen.addActionListener(event);
		menuItemSave.addActionListener(event);
		menuItemExport.addActionListener(event);
		menuItemQuit.addActionListener(event);
		menuItemComb.addActionListener(event);
		menuItemAssoc.addActionListener(event);
		//menuItemUSAcrossPhases.addActionListener(event);
		menuItemSingleContext.addActionListener(event);
		menuItemContextAcrossPhases.addActionListener(event);
		menuItemNoReset.addActionListener(event);
		menuItemReset.addActionListener(event);
		//menuR1.addActionListener(event);
		//menuR2.addActionListener(event);
		//menuItemContext.addActionListener(event);
		//menuItemSetCompound.addActionListener(event);
		//menuItemSetConfiguralCompounds.addActionListener(event); // Alberto
																	// Fernandez
		menuItemSetCompound.addActionListener(event);												// August-2011
        //menuItemRPM.addActionListener(event);
		menuItemGuide.addActionListener(event);
		menuItemAbout.addActionListener(event);
		//menuItemThreshold.addActionListener(event);
		//menuItemCsc.addActionListener(event);
		//menuItemTimestep.addActionListener(event);
		//menuItemVarComb.addActionListener(event);
		menuItemTimingPerTrial.addActionListener(event);
		menuItemCurve.addActionListener(event);
		menuItemCtx.addActionListener(event);
		menuItemCSCV.addActionListener(event);
		menuItemEli.addActionListener(event);
		menuItemUSCV.addActionListener(event);
		
		menuItemSetSize.addActionListener(event);
		
		menuItemCSScalar.addActionListener(event);
		
		menuItemPersistence.addActionListener(event);
		
		externalSave.addActionListener(event);
		//menuItemSerialCompounds.addActionListener(event);
		//menuItemRestrictPredictions.addActionListener(event);
	}

	/*
	 * Add Omega across phases
	 */
	public void addOmegaPhases() {
		/*
		 * otherTableModel.addPhases();
		 * otherValuesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		 * updateOtherValuesColumnsWidth();
		 */
		// int colCount = (phasesTableModel.getColumnCount()-1)/5;
		// ContextConfig.clearDefault();
		Iterator<Entry<TableColumn, Integer>> it = hiddenColumns.entrySet()
				.iterator();
		for (int i = 0; i < (phasesTableModel.getColumnCount() - 1) / 5
				&& it.hasNext(); i++) {
			Entry<TableColumn, Integer> column = it.next();
			int oldIndex = column.getValue();
			phasesTable.getColumnModel().addColumn(column.getKey());
			int currIndex = phasesTable.getColumnModel().getColumnCount() - 1;
			phasesTable.getColumnModel().moveColumn(currIndex, oldIndex);
		}
		hiddenColumns.clear();
	}

	/**
	 * Creates, initializes, configures and adds a values table into the frame.
	 * Columns and rows are depending on model's parameters.
	 */
	private void addOtherValuesTable() {

		otherTableModel = new OtherValuesTableModel(1);

		otherValuesTable = new JTable(otherTableModel) {
			@Override
			public boolean editCellAt(int row, int column, EventObject e) {
				boolean b = super.editCellAt(row, column, e);
				if (b) {
					TableCellEditor tce = getCellEditor(row, column);
					DefaultCellEditor dce = (DefaultCellEditor) tce;
					Component c = dce.getComponent();
					JTextComponent jtc = (JTextComponent) c;
					jtc.requestFocus();

					// Alberto Fernandez Oct-2011

					// This avoids to press a float enter
					/*
					 * InputMap inputMap = jtc.getInputMap(); KeyStroke key =
					 * KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
					 * inputMap.put(key, "none"); //$NON-NLS-1$
					 */

					jtc.addFocusListener(new java.awt.event.FocusAdapter() {
						@Override
						public void focusGained(java.awt.event.FocusEvent evt) {
							JTextComponent jtc = (JTextComponent) evt
									.getComponent();
							jtc.setCaretPosition(jtc.getCaretPosition());
						}

						@Override
						public void focusLost(java.awt.event.FocusEvent evt) {
							int col = otherValuesTable.getColumnCount();
							if (col > 0) {
								if (otherValuesTable.getCellEditor() != null) {
									otherValuesTable.getCellEditor()
											.stopCellEditing();
								}
							}
							// else; //take care about the other rows if you
							// need to.
						}
					});
				}
				return b;
			}
		};
		otherValuesTable.setDefaultRenderer(String.class, new GreekRenderer());
		otherTableModel.setInitialValuesTable();
		otherValuesTable.setCellSelectionEnabled(false);
		otherValuesTable.requestFocus();
		otherValuesTable.setFont(new Font(TABLE_FONT_NAME, TABLE_FONT_STYLE,
				TABLE_FONT_SIZE)); // Alberto Fern�ndez July-2011
		otherValuesTable.setRowHeight(TABLE_ROW_HEIGHT);
		otherValuesTable.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				int row = otherValuesTable.rowAtPoint(p);
				String tip;
				switch (row) {
				/*case 0:
					tip = Messages.getString("SimView.gammaTip"); //$NON-NLS-1$
					break;*/
				//case 0:
				//	tip = Messages.getString("SimView.stdTip"); //$NON-NLS-1$
				//	break;
				/*case 2:
					tip = Messages.getString("SimView.tau1Tip"); //$NON-NLS-1$
					break;
				case 3:
					tip = Messages.getString("SimView.tau2Tip"); //$NON-NLS-1$
					break;
				case 4:
					tip = Messages.getString("SimView.varsigmaTip"); //$NON-NLS-1$
					break;*/
				//case 1:
				//	tip = Messages.getString("SimView.varthetaTip"); //$NON-NLS-1$
				//	break;	
				case 0:
					tip = Messages.getString("SimView.leakTip"); //$NON-NLS-1$
					break;	
				case 1:
					tip = "CS recency"; //$NON-NLS-1$
					break;	
				case 2:
					tip = "Proportion of common elements between stimuli.";
					break;
				case 3:
					tip = "Discount on associative activation.";
					break;
				case 4:
					tip = "CV of CS element activation.";
					break;
				//case 5:
					//tip = "CV of US element activation.";
				//	break;
				case 5:
					tip = "Backward learning discount.";
					break;
				
				//case 7:
				//	tip = Messages.getString("SimView.intTip"); //$NON-NLS-1$
				//	break;	
				default:
					tip = ""; //$NON-NLS-1$
				}
				otherValuesTable.setToolTipText(tip);
			}// end MouseMoved
		}); // end MouseMotionAdapter

		otherValuesTable.setPreferredSize(new Dimension(TABLE_FONT_SIZE*20,TABLE_FONT_SIZE*9));
		otherValuesScroll = new JScrollPane(otherValuesTable);
	}
	
	

	/*
	 * Add a new phase to the phasesTableModel
	 */
	public void addPhase() {
		phasesTableModel.addPhase();
		updatePhasesColumnsWidth();
	}

	/**
	 * Creates, initializes, configures and adds a phase table into the frame.
	 * Columns and rows are depending on model's parameters.
	 */
	private void addPhaseTable() {
		phasesTableModel = new PhasesTableModel();
		phasesTable = new JTable(phasesTableModel) {

			@Override
			public boolean editCellAt(int row, int column, EventObject e) {
				boolean b = super.editCellAt(row, column, e);
                activeRow = row;
                activeCol = column;
				if (b
						&& getColumnName(column).contains(
								Messages.getString("SimView.phase")) ) { //$NON-NLS-1$
					TableCellEditor tce = getCellEditor(row, column);
					DefaultCellEditor dce = (DefaultCellEditor) tce;
					Component c = dce.getComponent();
					JTextComponent jtc = (JTextComponent) c;
					jtc.setFont(new Font(TABLE_FONT_NAME, TABLE_FONT_STYLE,
							(int)Math.round(TABLE_FONT_SIZE*1.3))); // Alberto Fernandez July-2011
					jtc.requestFocus();

					// Alberto Fernandez Oct-2011
					// Removed this because it was leading to no enter being
					// passed at all when
					// forcing descendant cell editors to close on hitting enter
					// JTables are a nightmare.
					// This avoids to press a float enter
					/*
					 * InputMap inputMap = jtc.getInputMap(); KeyStroke key =
					 * KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
					 * inputMap.put(key, "none"); //$NON-NLS-1$
					 */

					jtc.addFocusListener(new java.awt.event.FocusAdapter() {
						@Override
						public void focusGained(java.awt.event.FocusEvent evt) {
                            super.focusGained(evt);
							/*JTextComponent jtc = (JTextComponent) evt
									.getComponent();
							jtc.setCaretPosition(jtc.getCaretPosition());*/
						}

						@Override
						public void focusLost(java.awt.event.FocusEvent evt) {
							int col = CSValuesTable.getColumnCount();
							if (col > 0) {
								if (CSValuesTable.getCellEditor() != null) {
									CSValuesTable.getCellEditor()
											.stopCellEditing();
								}
							}
							// else; //take care about the other rows if you
							// need to.

						}
					});
					// Treat clicks on 'same context' as wanting to change the
					// salience and act as
					// though that menu item had been clicked.
				} else if ( false && !b
						&& getColumnName(column).contains(
								Messages.getString("SimView.context"))) {
					menuItemSingleContext.doClick();
				}
				return b;
			}

			/**
			 * Overridden to autosize columns after an edit and update the
			 * timing configurations to reflect phase strings.
			 */
			@Override
			public void editingStopped(ChangeEvent e) {
				super.editingStopped(e);
				updateTimingConfigs();
				updatePhasesColumnsWidth();

			}
		};

		phasesTable.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				int columnId = phasesTable.columnAtPoint(p);
				String column = phasesTable.getColumnName(columnId);
				String tip = Messages.getString("SimView.temporalTip"); //$NON-NLS-1$
				if (column.equals(GROUP_NAME)) {
					tip = Messages.getString("SimView.groupNameTip"); //$NON-NLS-1$
				} else if (column.contains(Messages.getString("SimView.phase"))) { //$NON-NLS-1$
					tip = Messages.getString("SimView.trialStringTip"); //$NON-NLS-1$
				} else if (column.equals(RANDOM)) {
					tip = Messages.getString("SimView.randomTip"); //$NON-NLS-1$
				} else if (column.equals(ITI)) {
					tip = Messages.getString("SimView.ITITip"); //$NON-NLS-1$
				} else if (column.equals(CONTEXT)) {
					tip = Messages.getString("SimView.contextTip"); //$NON-NLS-1$
				}else if (column.equals(SESSIONS)) {
					tip = Messages.getString("SimView.sessionTip"); //$NON-NLS-1$
				}
				phasesTable.setToolTipText(tip);
			}// end MouseMoved
		}); // end MouseMotionAdapter
		// Make single click start editing instead of needing float
		DefaultCellEditor singleClickEditor = new DefaultCellEditor(
				new JTextField());
		singleClickEditor.setClickCountToStart(1);
		phasesTable.setDefaultEditor(Object.class, singleClickEditor);

		phasesTableModel.setPhasesTable();
		phasesTable.setCellSelectionEnabled(false);
		phasesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		phasesTable.setFont(new Font(TABLE_FONT_NAME, TABLE_FONT_STYLE,
				(int)Math.round(TABLE_FONT_SIZE*1.3))); // Alberto Fern�ndez July-2011
		phasesTable.setRowHeight((int)Math.round(TABLE_ROW_HEIGHT*1.3));

		phasesTable.requestFocus();

		phasesScroll = new JScrollPane(phasesTable);
		// Added a custom editor for variable/fixed onsets. J Gray Dec-2011
		phasesTable.setDefaultEditor(TimingConfiguration.class,
				new TimingEditor(this));
		// Added a custom editor for ITI durations. J Gray Dec-2011
		phasesTable.setDefaultEditor(ITIConfig.class, new ITIEditor(this));
		phasesTable.setDefaultEditor(ContextConfig.class, new ContextEditor(this));
		phasesTable.getColumnModel().getColumn(1).setCellEditor(new TrialStringEditor());
	}

	/*
	 * Add US across phases
	 */
	public void addUSPhases() {
		USValuesTableModel.addPhases();
		USValuesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		updateUSValuesColumnsWidth();
	}
	

	/**
	 * Creates, initializes, configures and adds a values table into the frame.
	 * Columns and rows are depending on model's parameters.
	 */
	private void addUSValuesTable() {

		USValuesTableModel = new USValuesTableModel(1);

		USValuesTable = new JTable(USValuesTableModel) {
			@Override
			public boolean editCellAt(int row, int column, EventObject e) {
				boolean b = super.editCellAt(row, column, e);
				if (b) {
					TableCellEditor tce = getCellEditor(row, column);
					DefaultCellEditor dce = (DefaultCellEditor) tce;
					Component c = dce.getComponent();
					JTextComponent jtc = (JTextComponent) c;
					jtc.requestFocus();

					// Alberto Fernandez Oct-2011

					jtc.addFocusListener(new java.awt.event.FocusAdapter() {
						@Override
						public void focusGained(java.awt.event.FocusEvent evt) {
							JTextComponent jtc = (JTextComponent) evt
									.getComponent();
							jtc.setCaretPosition(jtc.getCaretPosition());
						}

						@Override
						public void focusLost(java.awt.event.FocusEvent evt) {
							int col = USValuesTable.getColumnCount();
							if (col > 0) {
								if (USValuesTable.getCellEditor() != null) {
									USValuesTable.getCellEditor()
											.stopCellEditing();
								}
							}
							// else; //take care about the other rows if you
							// need to.
						}
					});
				}
				return b;
			}
		};
		USValuesTable.setDefaultRenderer(String.class, new GreekRenderer());
		USValuesTableModel.setInitialValuesTable();
		USValuesTable.setCellSelectionEnabled(false);
		USValuesTable.requestFocus();
		USValuesTable.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				int row = USValuesTable.rowAtPoint(p);
				String tip;
				switch (row) {
				case 0:
					tip = Messages.getString("SimView.betaPlusTip"); //$NON-NLS-1$
					break;
				case 1:
					tip = Messages.getString("SimView.lambdaPlusTip"); //$NON-NLS-1$
					break;
				case 2:
					tip = Messages.getString("SimView.boostTip"); //$NON-NLS-1$
					break;
				default:
					tip = ""; //$NON-NLS-1$
				}
				USValuesTable.setToolTipText(tip);
			}// end MouseMoved
		}); // end MouseMotionAdapter
		USValuesTable.setFont(new Font(TABLE_FONT_NAME, TABLE_FONT_STYLE,
				TABLE_FONT_SIZE)); // Alberto Fern�ndez July-2011
		USValuesTable.setRowHeight(TABLE_ROW_HEIGHT);
		USValuesScroll = new JScrollPane(USValuesTable);

		USValuesScroll.setMinimumSize(new Dimension(50,0));
	}
	
	private void addUSSalienceTable() {

		USSalienceTableModel = new USSalienceTableModel(1);

		USSalienceTable = new JTable(USSalienceTableModel) {
			@Override
			public boolean editCellAt(int row, int column, EventObject e) {
				boolean b = super.editCellAt(row, column, e);
				if (b) {
					TableCellEditor tce = getCellEditor(row, column);
					DefaultCellEditor dce = (DefaultCellEditor) tce;
					Component c = dce.getComponent();
					JTextComponent jtc = (JTextComponent) c;
					jtc.requestFocus();

					// Alberto Fernandez Oct-2011

					jtc.addFocusListener(new java.awt.event.FocusAdapter() {
						@Override
						public void focusGained(java.awt.event.FocusEvent evt) {
							JTextComponent jtc = (JTextComponent) evt
									.getComponent();
							jtc.setCaretPosition(jtc.getCaretPosition());
						}

						@Override
						public void focusLost(java.awt.event.FocusEvent evt) {
							int col = USSalienceTable.getColumnCount();
							if (col > 0) {
								if (USSalienceTable.getCellEditor() != null) {
									USSalienceTable.getCellEditor()
											.stopCellEditing();
								}
							}
							// else; //take care about the other rows if you
							// need to.
						}
					});
				}
				return b;
			}
		};
		USSalienceTable.setDefaultRenderer(String.class, new GreekRenderer());
		USSalienceTableModel.setInitialValuesTable();
		USSalienceTable.setCellSelectionEnabled(false);
		USSalienceTable.requestFocus();
		USSalienceTable.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				int row = USSalienceTable.rowAtPoint(p);
				String tip;
				switch (row) {
				case 0:
					tip = "US general salience"; //$NON-NLS-1$
					break;
				case 1:
					tip = "US predictive salience"; //$NON-NLS-1$
					break;
				case 2:
					tip = Messages.getString("SimView.boostTip"); //$NON-NLS-1$
					break;
				default:
					tip = ""; //$NON-NLS-1$
				}
				USSalienceTable.setToolTipText(tip);
			}// end MouseMoved
		}); // end MouseMotionAdapter
		USSalienceTable.setFont(new Font(TABLE_FONT_NAME, TABLE_FONT_STYLE,
				TABLE_FONT_SIZE)); // Alberto Fern�ndez July-2011
		USSalienceTable.setRowHeight(TABLE_ROW_HEIGHT);
		USSalienceScroll = new JScrollPane(USSalienceTable);

		USSalienceScroll.setMinimumSize(new Dimension(50,0));
	}
	/**
	 * Positions the frame into the center of the screen. It uses the
	 * Toolkit.getDefaultToolkit().getScreenSize() method to retrieve the
	 * screens actual size and from the it calculates the center,
	 */
	private void center() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screenSize.width - INITIAL_WIDTH) / 2;
		int y = (screenSize.height - INITIAL_HEIGHT) / 2;
		this.setLocation(x, y);
	}

	/**
	 * Clears the list of hidden context columns.
	 */
	public void clearHidden() {
		hiddenColumns.clear();
	}

	/**
	 * Clear the Area of the results.
	 */
	public void clearOutputArea() {
		//outputArea.setText(""); //$NON-NLS-1$
	}

	/*
	 * Creates the bottom panel with the logo
	 */
	private JPanel createBottomPanel() {
		JPanel aboutPanel = new JPanel();
		aboutPanel.setMinimumSize(new Dimension(1000, 58));

		aboutPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		aboutPanel.setBackground(Color.WHITE);
		// modified by Alberto Fernandez: 19 July 2011
		// ImageIcon icon = createImageIcon("/Extras/logo6-final.jpg", "");

		// ImageIcon icon = createImageIcon("../Extras/logo6-final.png", "");
		ImageIcon icon = createImageIcon(
				"/simulator/extras/logo_small.png", ""); //$NON-NLS-1$ //$NON-NLS-2$

		JLabel label = new JLabel(icon);
		aboutPanel.add(label);

		// aboutPanel.setBorder(new SimBackgroundBorder(icon.getImage(), true));
		return aboutPanel;

	}
	private Image getScaledImage(Image srcImg, int w, int h){
	    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = resizedImg.createGraphics();

	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, w, h, null);
	    g2.dispose();

	    return resizedImg;
	}
	
	private JPanel createBottomRightPanel() {
		aboutPanel = new JPanel();
		aboutPanel.setMinimumSize(new Dimension((int)Math.round(this.INITIAL_WIDTH*0.9/2), this.INITIAL_HEIGHT/4));
		aboutPanel.setSize((int)Math.round(this.INITIAL_WIDTH*0.9/2),this.INITIAL_HEIGHT/4);
		aboutPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		aboutPanel.setBackground(new Color(255,255,255));//
		ImageIcon icon = createImageIcon(
				"/simulator/extras/dd_new.png", ""); //$NON-NLS-1$ //$NON-NLS-2$
		Image img = getScaledImage(icon.getImage(),(int)Math.round(this.INITIAL_WIDTH/2.15f),(int)Math.round((1.5*this.INITIAL_WIDTH)/2.15f));
		icon = new ImageIcon(img);
		JLabel label = new JLabel(icon);
		aboutPanel.add(label);
		//aboutPanel.setBorder(new SimBackgroundBorder(icon.getImage(), true));
		return aboutPanel;

	}
	
	public void swapPanels(JPanel p, String groupName) {
		if (!(aboutPanel.getParent() == null)) {
			resultPanel.remove(aboutPanel);
			tabs = new JTabbedPane();
			resultPanel.add(tabs,BorderLayout.CENTER);
		}
		boolean found = false;
		for (int i = 0; i < tabs.getTabCount(); i++) {
			if (tabs.getTitleAt(i).equals(groupName)) {found = true;}
		}
		if (!found) tabs.addTab(groupName, p);
	}

	/**
	 * Creates, initializes and configures the view components.. This view can
	 * not change its size
	 */
	private void createGUI2() {

		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.RAISED));

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(5, 5, 5, 5);

		// Phases panel
		JPanel phasePanel = new JPanel();
		phasePanel.setLayout(new BorderLayout());
		addPhaseTable();
		phasePanel.add(phasesScroll, BorderLayout.CENTER);

		// Add/remove buttons to add or remove phases or groups

		JPanel phasesButtonPanel = new JPanel();
		JLabel phasesLabel = new JLabel(Messages.getString("SimView.phases")); //$NON-NLS-1$

		addPhaseBut = new JButton("+");addPhaseBut.setFont(new Font("Courier", Font.BOLD, 16)); //$NON-NLS-1$ //$NON-NLS-2$

		// addPhaseBut.setMargin(new Insets(-7,10,-6,10)); // Insets(int top,
		// int left, int bottom, int right)
		// addPhaseBut = new JButton("+"); E Mondragon July 30, 2011
		addPhaseBut.setFocusPainted(false);// E.Mondragon August 1st, 2011
		addPhaseBut.updateUI();//
		addPhaseBut.setActionCommand("addPhase"); //$NON-NLS-1$

		removePhaseBut = new JButton("-");removePhaseBut.setFont(new Font("Courier", Font.BOLD, 16)); //$NON-NLS-1$ //$NON-NLS-2$

		// removePhaseBut.setMargin(new Insets(-7,10,-6,10));//E Mondragon July
		// 30, 2011
		// removePhaseBut = new JButton("-"); E Mondragon July 30, 2011
		removePhaseBut.setFocusPainted(false);// E.Mondragon August 1st, 2011
		removePhaseBut.setActionCommand("removePhase"); //$NON-NLS-1$

		clearBut = new JButton(Messages.getString("SimView.clear")); //$NON-NLS-1$
		clearBut.setActionCommand("clearAll"); //$NON-NLS-1$
		
		phasesButtonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		phasesButtonPanel.add(clearBut);
		JPanel spacing = new JPanel();
		spacing.setMinimumSize(new Dimension(50,50));
		spacing.setPreferredSize((new Dimension(50,10)));
		phasesButtonPanel.add(spacing);
		phasesButtonPanel.add(phasesLabel);
		phasesButtonPanel.add(removePhaseBut);
		phasesButtonPanel.add(addPhaseBut);
		phasePanel.add(phasesButtonPanel, BorderLayout.NORTH);

		JPanel groupsButtonPanel = new JPanel();
		JLabel groupsLabel = new JLabel(Messages.getString("SimView.groups")); //$NON-NLS-1$

		addGroupBut = new JButton("+");addGroupBut.setFont(new Font("Courier", Font.BOLD, 16)); //$NON-NLS-1$ //$NON-NLS-2$
		// addGroupBut.setMargin(new Insets(-7,5,-7,5));//E Mondragon July 30,
		// 2011
		// addGroupBut = new JButton("+"); E Mondragon July 30, 2011
		addGroupBut.setFocusPainted(false);// E.Mondragon August 1st, 2011
		addGroupBut.setActionCommand("addGroup"); //$NON-NLS-1$

		removeGroupBut = new JButton("-");removeGroupBut.setFont(new Font("Courier", Font.BOLD, 16)); //$NON-NLS-1$ //$NON-NLS-2$
		// removeGroupBut.setMargin(new Insets(-7,5,-7,5));//E Mondragon July
		// 30, 2011
		// removeGroupBut = new JButton("-"); E Mondragon July 30, 2011
		removeGroupBut.setFocusPainted(false);// E.Mondragon August 1st, 2011
		removeGroupBut.setActionCommand("removeGroup"); //$NON-NLS-1$

		// A. Fernandez modification E.Mondragon 10/10/2011

		String OS = System.getProperty("os.name"); //$NON-NLS-1$
		//System.out.println(OS);
		if (OS.toUpperCase().contains("WINDOWS")) { //$NON-NLS-1$
			//System.out.println("es windows"); //$NON-NLS-1$
			addPhaseBut.setMargin(new Insets(-7, 10, -6, 10)); // Insets(int
																// top, int
																// left, int
																// bottom, int
																// right)
			removePhaseBut.setMargin(new Insets(-7, 10, -6, 10));// E Mondragon
																	// July 30,
																	// 2011
			addGroupBut.setMargin(new Insets(-7, 9, -6, 9));// E Mondragon July
															// 30, 2011
			removeGroupBut.setMargin(new Insets(-7, 9, -6, 9));// E Mondragon
																// July 30, 2011
		} 

		// end

		groupsButtonPanel.setLayout(new BoxLayout(groupsButtonPanel,
				BoxLayout.Y_AXIS));
		groupsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		addGroupBut.setAlignmentX(Component.CENTER_ALIGNMENT);
		removeGroupBut.setAlignmentX(Component.CENTER_ALIGNMENT);
		groupsButtonPanel.add(groupsLabel);
		groupsButtonPanel.add(removeGroupBut);
		// groupsButtonPanel.add(addGroupBut);
		JPanel jp = new JPanel(); // Alberto Fernandez Oct-2011
		jp.add(addGroupBut);
		groupsButtonPanel.add(jp);

		phasePanel.add(groupsButtonPanel, BorderLayout.WEST);
		// Variables panel
		JPanel variablePanel = new JPanel();
		variablePanel.setLayout(new BorderLayout());
		setVariablesBut = new JButton(Messages.getString("SimView.setParams")); //$NON-NLS-1$
		setVariablesBut.setActionCommand("setVariables"); //$NON-NLS-1$
		JPanel varButPanel = new JPanel();

		gButton = new JButton("Shared Elements"); //$NON-NLS-1$
		gButton.setEnabled(false);
		gButton.setActionCommand("setG"); //$NON-NLS-1$
		
		estherBox = new JCheckBox("Different Rules",false);
		estherBox.setActionCommand("esther");
		estherBox.setVisible(false);
		
		varyingVartheta = new JCheckBox("Varying vartheta", false);
		varyingVartheta.setVisible(false);
		varyingVartheta.setActionCommand("vartheta");
		//JPanel varthetaPanel = new JPanel();
		//varthetaPanel.setLayout(new GridLayout(1, 1));
		//varButPanel.add(gButton);
		//varthetaPanel.add(varyingVartheta);
		
		varButPanel.setLayout(new GridLayout(1, 1));
		varButPanel.add(setVariablesBut);
		//varButPanel.add(gButton);
		
		
		//varButPanel.add(estherBox);
		//varButPanel.add(varyingVartheta);
		addCSValuesTable();
		addCSVariableTable();
		addUSValuesTable();
		addCSSalienceTable();
		addUSSalienceTable();
		//addDDValuesTable();

		addOtherValuesTable();
		runBut = new JButton("RUN"); //$NON-NLS-1$
		runBut.setActionCommand("run"); //$NON-NLS-1$
		runBut.setFont(new Font("Serif", Font.BOLD, (int) (Math.round(1.3*TABLE_FONT_SIZE))));
		JPanel runButPanel = new JPanel();
		

		presenceMeanCheckList = new JCheckBox("Use presence trace for element mean", false);
		presenceMeanCheckList.setActionCommand("presenceMean");
		
		JPanel valuesPanel = new JPanel();

		GridBagConstraints con = new GridBagConstraints();
		valuesPanel.setLayout(new GridBagLayout());

		con.fill = GridBagConstraints.HORIZONTAL;
		con.gridx = 0;
		con.gridy = 0;
		con.weighty= 0.1;
		con.weightx = 1;
		con.gridwidth = 1;
		con.gridheight = 1;
		con.ipady = 65; 
		con.ipadx = 120;
		valuesPanel.add(CSValuesScroll,con.clone());
		con.gridx = 0;
		con.gridy = 1;
		con.weighty= 0.1;
		con.weightx = 1;
		valuesPanel.add(CSVariableScroll,con.clone());
		con.gridx = 0;
		con.gridy = 2;
		con.weighty= 0.1;
		con.weightx = 1;
		valuesPanel.add(CSSalienceScroll,con.clone());
		con.gridx = 0;
		con.gridy = 3;
		con.weighty= 0.02;
		con.ipady = 55; 
		con.ipadx = 120;
		con.weightx = 1;
		valuesPanel.add(USValuesScroll,con.clone());
		//valuesPanel.add(DDValuesScroll);
		//otherValuesScroll.setSize(600, 800);
		con.gridx = 0;
		con.gridy = 4;
		con.weighty= 0.02;
		con.weightx = 1;
		valuesPanel.add(USSalienceScroll,con.clone());
		con.gridx = 0;
		con.gridy = 5;
		con.weighty= 0.4;
		con.weightx = 1;
		con.ipady = 200; 
		con.ipadx = 120;
		valuesPanel.add(otherValuesScroll,con.clone());
		con.gridx = 0;
		con.gridy = 6;
		con.weighty= 0.05;
		con.weightx = 1;
		con.ipady = 20; 
		con.ipadx = 120;
		intensityButton = new JButton("US Intensity Factor");
		intensityButton.setActionCommand("intensity");
		intensityButton.setEnabled(false);
		valuesPanel.add(intensityButton,con.clone());
		//valuesPanel.add(presenceMeanCheckList);
		
		variablePanel.add(varButPanel, BorderLayout.NORTH);
		variablePanel.add(valuesPanel, BorderLayout.CENTER);
		//variablePanel.add(varthetaPanel,BorderLayout.AFTER_LAST_LINE);

		//variablePanel.add(runButPanel, BorderLayout.SOUTH);

		// Result panel
		resultPanel = new JPanel();
		resultPanel.setLayout(new BorderLayout());

		//JLabel temp1 = new JLabel("");
		//temp1.setPreferredSize(new Dimension (180,30));
		//JPanel clearButPanel = new JPanel(new FlowLayout());
		

		//clearButPanel.add(clearBut, BorderLayout.WEST);
		//clearButPanel.add(temp1, BorderLayout.EAST);
		//clearButPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		
		
		
		
		//clearButPanel.add(externalSave);
		//outputArea = new JTextArea(10, 22);
		//outputArea.setEditable(false);
		//outputArea.setFont(new Font("Serif", Font.PLAIN, 16)); //$NON-NLS-1$
		//outputScroll = new JPanel();
		
		
		timePredictionCheckList = new JCheckBox("W/Time Graph", false);
		timePredictionCheckList.setFont(new Font("Helvetica", Font.PLAIN, 14));
		timePredictionCheckList.setActionCommand("TrialPrediction");
		responseCheckList = new JCheckBox("R/Time Graph", false);
		responseCheckList.setActionCommand("ResponseGraph");
		responseCheckList.setFont(new Font("Helvetica", Font.PLAIN, 14));
		aniCheckList = new JCheckBox("Network Representation",false);
		aniCheckList.setActionCommand("animationGraph");
		aniCheckList.setFont(new Font("Helvetica", Font.PLAIN, 14));
		contextCheckList = new JCheckBox("Context Graph",false);
		contextCheckList.setActionCommand("contextGraph");
		contextCheckList.setFont(new Font("Helvetica", Font.PLAIN, 14));
		salienceCheckList = new JCheckBox("Alphas/Trials Graph",false);
		salienceCheckList.setActionCommand("salienceGraph");
		salienceCheckList.setFont(new Font("Helvetica", Font.PLAIN, 14));
		vGraphCheckList = new JCheckBox("W/Episodes Graph", false);
		vGraphCheckList.setActionCommand("vGraph");
		vGraphCheckList.setFont(new Font("Helvetica", Font.PLAIN, 14));

		waTimeCheckList = new JCheckBox("V/Time Graph", false);
		waTimeCheckList.setActionCommand("watGraph");
		waTimeCheckList.setFont(new Font("Helvetica", Font.PLAIN, 14));
		waTrialCheckList = new JCheckBox("V/Trial Graph", false);
		waTrialCheckList.setActionCommand("waTGraph");
		waTrialCheckList.setFont(new Font("Helvetica", Font.PLAIN, 14));
		errorTrialCheckList = new JCheckBox("Discrete Error/Trial Graph", false);
		errorTrialCheckList.setActionCommand("errorTrial");
		errorTrialCheckList.setFont(new Font("Helvetica", Font.PLAIN, 14));
		errorCheckList = new JCheckBox("Discrete Error/Time Graph", false);
		errorCheckList.setActionCommand("errorGraph");
		errorCheckList.setFont(new Font("Helvetica", Font.PLAIN, 14));
		////
		//
		//
		trialResponseCheckList = new JCheckBox("R/Trials Graph", false);
		trialResponseCheckList.setActionCommand("trialResponse");
		trialResponseCheckList.setFont(new Font("Helvetica", Font.PLAIN, 14));
		//componentStrengthCheckList = new JCheckBox("componentStrength", false);
		//componentStrengthCheckList.setActionCommand("TrialPrediction");
		componentActivationCheckList = new JCheckBox("Element Activation", false);
		componentActivationCheckList.setActionCommand("componentActivation");
		componentActivationCheckList.setFont(new Font("Helvetica", Font.PLAIN, 14));
		componentPredictionCheckList = new JCheckBox("Element Predictions", false);
		componentPredictionCheckList.setActionCommand("componentPredictions");
		componentPredictionCheckList.setFont(new Font("Helvetica", Font.PLAIN, 14));

		componentDeltaCheckList = new JCheckBox("Element Deltas", false);
		componentDeltaCheckList.setActionCommand("ComponentDelta");
		componentDeltaCheckList.setFont(new Font("Helvetica", Font.PLAIN, 14));
		
		floatingButtons = new JCheckBox("Checkbox windows", false);
		floatingButtons.setActionCommand("floatingButtons");
		floatingButtons.setFont(new Font("Helvetica", Font.PLAIN, 14));
		
		
		dispGraphBut = new JButton(Messages.getString("SimView.graphs")); //$NON-NLS-1$
		dispGraphBut.setActionCommand("dispGraph"); //$NON-NLS-1$
		JPanel dispGraphButPanel = new JPanel();
		dispGraphButPanel.setLayout(new GridLayout(22, 1));
		runBut.setForeground(new Color(240,0,0));
		//clearBut.setForeground(new Color(180,0,0));
		//clearBut.setFont(new Font("Helvetica", Font.ITALIC, 12));
		dispGraphButPanel.add(dispGraphBut);
		dispGraphButPanel.add(vGraphCheckList);
		dispGraphButPanel.add(waTrialCheckList);
		dispGraphButPanel.add(trialResponseCheckList);
		dispGraphButPanel.add(new JLabel(""));
		dispGraphButPanel.add(timePredictionCheckList);
		dispGraphButPanel.add(waTimeCheckList);
		dispGraphButPanel.add(responseCheckList);
		dispGraphButPanel.add(new JLabel(""));
		dispGraphButPanel.add(contextCheckList);
		dispGraphButPanel.add(salienceCheckList);
		dispGraphButPanel.add(componentActivationCheckList);
		//dispGraphButPanel.add(componentPredictionCheckList);
		//dispGraphButPanel.add(componentDeltaCheckList);
		//dispGraphButPanel.add(aniCheckList);
		dispGraphButPanel.add(errorTrialCheckList);
		dispGraphButPanel.add(errorCheckList);
		dispGraphButPanel.add(new JLabel(""));
		dispGraphButPanel.add(floatingButtons);
		dispGraphButPanel.add(new JLabel(""));
		dispGraphButPanel.add(runBut);
		//dispGraphButPanel.add(clearBut);
		resultPanel.add(runButPanel, BorderLayout.EAST);
		//resultPanel.add(clearButPanel, BorderLayout.NORTH);
		//resultPanel.add(outputScroll, BorderLayout.CENTER);
		//resultPanel.add(dispGraphButPanel, BorderLayout.SOUTH);
		resultPanel.add(createBottomRightPanel(),BorderLayout.CENTER);
		//runButPanel.setLayout(new BorderLayout());
		runButPanel.add(dispGraphButPanel,BorderLayout.NORTH);
		//runButPanel.add(runBut,BorderLayout.CENTER);
		//dispGraphButPanel.setMinimumSize(new Dimension(400,300));
		
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.weighty = 0.7;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(phasePanel, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weighty = 0.3;
		c.weightx = 0.2;
		c.fill = GridBagConstraints.BOTH;
		
		mainPanel.add(variablePanel, c.clone());
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weighty = 0.3;
		c.weightx = 0.1;
		c.fill = GridBagConstraints.BOTH;
		
		mainPanel.add(resultPanel, c.clone());

		cp.add(mainPanel);
		//cp.add(createBottomPanel());

	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	private ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = this.getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println(Messages.getString("SimView.404Error") + path); //$NON-NLS-1$
			return null;
		}
	}

	/**
	 * Creates and initializes every element that is necessary for the menu
	 * component. Sets mnemonics on them and actionCommands for the easier
	 * process during events.
	 */
	private void createMenu() {
		menuBar = new JMenuBar();

		menuFile = new JMenu(Messages.getString("SimView.file")); //$NON-NLS-1$
		menuFile.setMnemonic(KeyEvent.VK_F);

		menuItemNew = new JMenuItem(
				Messages.getString("SimView.new"), KeyEvent.VK_N); //$NON-NLS-1$
		menuItemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.ALT_MASK));
		menuItemNew.setActionCommand("New"); //$NON-NLS-1$
		menuFile.add(menuItemNew);

		menuItemOpen = new JMenuItem(
				Messages.getString("SimView.open"), KeyEvent.VK_O); //$NON-NLS-1$
		menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.ALT_MASK));
		menuItemOpen.setActionCommand("Open"); //$NON-NLS-1$
		menuFile.add(menuItemOpen);

		menuItemSave = new JMenuItem(
				Messages.getString("SimView.save"), KeyEvent.VK_S); //$NON-NLS-1$
		menuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.ALT_MASK));
		menuItemSave.setActionCommand("Save"); //$NON-NLS-1$
		menuFile.add(menuItemSave);

		menuItemExport = new JMenuItem(
				Messages.getString("SimView.export"), KeyEvent.VK_E); //$NON-NLS-1$
		menuItemExport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				ActionEvent.ALT_MASK));
		menuItemExport.setActionCommand("Export"); //$NON-NLS-1$
		menuFile.add(menuItemExport);

		menuFile.addSeparator();

		menuItemQuit = new JMenuItem(
				Messages.getString("SimView.quit"), KeyEvent.VK_Q); //$NON-NLS-1$
		menuItemQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				ActionEvent.ALT_MASK));
		menuItemQuit.setActionCommand("Quit"); //$NON-NLS-1$
		menuFile.add(menuItemQuit);

		menuBar.add(menuFile);

		menuDesignSettings = new JMenu(
				Messages.getString("SimView.designSettings")); //$NON-NLS-1$
		menuProcSettings = new JMenu(
				Messages.getString("SimView.procedureSettings")); //$NON-NLS-1$
		menuDesignSettings.setMnemonic(KeyEvent.VK_S);
		menuProcSettings.setMnemonic(KeyEvent.VK_P);
		menuItemComb = new JMenuItem(
				Messages.getString("SimView.numCombinations"), KeyEvent.VK_R); //$NON-NLS-1$
		menuItemComb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.ALT_MASK));
		menuItemComb.setActionCommand("Combinations"); //$NON-NLS-1$
		menuProcSettings.add(menuItemComb);
		
		menuItemAssoc = new JMenuItem("Mediated Learning Discount"); //$NON-NLS-1$
		menuItemAssoc.setActionCommand("assocDiscount"); //$NON-NLS-1$
		//menuProcSettings.add(menuItemAssoc);
		
		
		
		
		//menuItemVarComb = new JMenuItem(
		//		Messages.getString("SimView.numVariableCombinations"), KeyEvent.VK_V); //$NON-NLS-1$
		//menuItemVarComb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
		//		ActionEvent.ALT_MASK));
		//menuItemVarComb.setActionCommand("VarDistCombinations"); //$NON-NLS-1$
		//menuProcSettings.add(menuItemVarComb);

		menuItemTimingPerTrial = new JCheckBoxMenuItem(
			Messages.getString("SimView.timingPerTrial"), false); //$NON-NLS-1$
		// menuItemTimingPerPhase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
		// ActionEvent.ALT_MASK));
		menuItemTimingPerTrial.setActionCommand("timingPerTrial"); //$NON-NLS-1$
		menuProcSettings.add(menuItemTimingPerTrial);
		
		externalSave = new JCheckBox(
				Messages.getString("SimView.externalSave"), true); //$NON-NLS-1$
			// menuItemTimingPerPhase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
			// ActionEvent.ALT_MASK));
		externalSave.setActionCommand("externalSave"); //$NON-NLS-1$
		//	menuProcSettings.add(externalSave);

		menuElementSettings = new JMenu("Element Activation Settings"); //$NON-NLS-1$
		menuItemEli = new JMenuItem("Eligibility trace discount"); //$NON-NLS-1$
		menuItemEli.setActionCommand("eli"); //$NON-NLS-1$
		menuElementSettings.add(menuItemEli);
		menuItemCSCV = new JMenuItem("CS elements CV"); //$NON-NLS-1$
		menuItemCSCV.setActionCommand("CSCV"); //$NON-NLS-1$
		menuElementSettings.add(menuItemCSCV);
		menuItemUSCV = new JMenuItem("US elements CV"); //$NON-NLS-1$
		menuItemUSCV.setActionCommand("USCV"); //$NON-NLS-1$
		menuElementSettings.add(menuItemUSCV);
		menuItemSetSize = new JMenuItem("Set Size"); //$NON-NLS-1$
		menuItemSetSize.setActionCommand("Set Size"); //$NON-NLS-1$
		menuElementSettings.add(menuItemSetSize);
		menuItemCSScalar = new JMenuItem("Wave Constant"); //$NON-NLS-1$
		menuItemCSScalar.setActionCommand("CSScalar"); //$NON-NLS-1$
		//menuElementSettings.add(menuItemCSScalar);
		menuItemPersistence = new JMenuItem("US Persistence (s)"); //$NON-NLS-1$
		menuItemPersistence.setActionCommand("Persistence"); //$NON-NLS-1$
		//menuElementSettings.add(menuItemPersistence);
		menuItemCurve = new JMenuItem(
				"Curve Right-skew"); //$NON-NLS-1$
		menuItemCurve.setActionCommand("Curves"); //$NON-NLS-1$
		menuElementSettings.add(menuItemCurve);
		menuItemCtx = new JMenuItem(
				"Context Reset Value"); //$NON-NLS-1$
		menuItemCtx.setActionCommand("Context Reset"); //$NON-NLS-1$
		//menuElementSettings.add(menuItemCtx);
		menuContextReset = new JMenu("Reset Context"); //$NON-NLS-1$
		menuItemNoReset = new JRadioButtonMenuItem("No reset", false); //$NON-NLS-1$
		menuItemNoReset.setActionCommand("noReset"); //$NON-NLS-1$
		menuContextReset.add(menuItemNoReset);
		menuItemReset = new JRadioButtonMenuItem(
				"Reset", true); //$NON-NLS-1$
		menuItemReset.setActionCommand("resetContext"); //$NON-NLS-1$
		menuContextReset.add(menuItemReset);
		
		
		
		//menuRule = new JMenu("Learning Rule"); //$NON-NLS-1$
		//menuR1 = new JRadioButtonMenuItem("Original Rule", false); //$NON-NLS-1$
		//menuR1.setActionCommand("rule1"); //$NON-NLS-1$
		//menuRule.add(menuR1);
		//menuR2 = new JRadioButtonMenuItem(
		//		"Latest Rule", true); //$NON-NLS-1$
		//menuR2.setActionCommand("rule2"); //$NON-NLS-1$
		//menuRule.add(menuR2);
		
		
		//menuElementSettings.add(menuContextReset);

		//menuElementSettings.add(menuRule);
		/*zeroTraces = new JCheckBoxMenuItem(
				Messages.getString("SimView.zeroTrace"), false); //$NON-NLS-1$
		zeroTraces.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				ActionEvent.ALT_MASK));
		zeroTraces.setActionCommand("zeroTraces"); //$NON-NLS-1$
		menuProcSettings.add(zeroTraces);*/


		/*menuItemRestrictPredictions = new JCheckBoxMenuItem(
				Messages.getString("SimView.restrictPredictions"), true); //$NON-NLS-1$
		menuItemRestrictPredictions.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_0, ActionEvent.ALT_MASK));
		menuItemRestrictPredictions.setActionCommand("restrictPredictions"); //$NON-NLS-1$
		menuProcSettings.add(menuItemRestrictPredictions);*/

		//menuItemTimestep = new JMenuItem(
		//		Messages.getString("SimView.timestep"), KeyEvent.VK_T); //$NON-NLS-1$
		//((JMenuItem) menuItemTimestep).setAccelerator(KeyStroke.getKeyStroke(
		//		KeyEvent.VK_T, ActionEvent.ALT_MASK));
		//menuItemTimestep.setActionCommand("timestep"); //$NON-NLS-1$
		//menuProcSettings.add(menuItemTimestep);

		/*menuItemThreshold = new JCheckBoxMenuItem(
				Messages.getString("SimView.responseThreshold"), true); //$NON-NLS-1$
		menuItemThreshold.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				ActionEvent.ALT_MASK));
		menuItemThreshold.setActionCommand("threshold"); //$NON-NLS-1$
		menuProcSettings.add(menuItemThreshold);*/

       /* menuItemRPM = new JMenuItem(
                Messages.getString("SimView.rpm")); //$NON-NLS-1$
        menuItemRPM.setActionCommand("rpm"); //$NON-NLS-1$
        menuProcSettings.add(menuItemRPM);*/

		/*menuItemUSAcrossPhases = new JCheckBoxMenuItem(
				Messages.getString("SimView.usPerPhase"), false); //$NON-NLS-1$
		menuItemUSAcrossPhases.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_U, ActionEvent.ALT_MASK));
		menuItemUSAcrossPhases.setActionCommand("SetUSAcrossPhases"); //$NON-NLS-1$
		menuDesignSettings.add(menuItemUSAcrossPhases);*/

		/*menuTrace = new JMenu(Messages.getString("SimView.traceMenu"));
		ButtonGroup trace = new ButtonGroup();
		menuItemAccumTrace = new JRadioButtonMenuItem(
				Messages.getString("SimView.accumTrace"), false);

		menuTrace.add(menuItemAccumTrace);*/
		/*menuItemBoundedTrace = new JRadioButtonMenuItem(
				Messages.getString("SimView.boundedTrace"), false);
		menuTrace.add(menuItemBoundedTrace);
		menuItemReplaceTrace = new JRadioButtonMenuItem(
				Messages.getString("SimView.replacingTrace"), true);
		menuTrace.add(menuItemReplaceTrace);
		menuProcSettings.add(menuTrace);
		trace.add(menuItemAccumTrace);
		menuItemAccumTrace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
				ActionEvent.CTRL_MASK));
		trace.add(menuItemBoundedTrace);
		menuItemBoundedTrace.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_2, ActionEvent.CTRL_MASK));
		trace.add(menuItemReplaceTrace);
		menuItemReplaceTrace.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_3, ActionEvent.CTRL_MASK));
*/
		menuContext = new JMenu(Messages.getString("SimView.contextSim")); //$NON-NLS-1$
		menuDesignSettings.add(menuContext);
		ButtonGroup contexts = new ButtonGroup();
		/*menuItemContext = new JRadioButtonMenuItem(
				Messages.getString("SimView.noContext"), false); //$NON-NLS-1$
		menuItemContext.setActionCommand("SetContext"); //$NON-NLS-1$*/
		//menuItemContext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
		//		ActionEvent.ALT_MASK));
		//contexts.add(menuItemContext);
		//menuContext.add(menuItemContext);
		menuItemSingleContext = new JRadioButtonMenuItem(
				Messages.getString("SimView.sameContext"), true); //$NON-NLS-1$
		menuItemSingleContext.setActionCommand("SingleContext"); //$NON-NLS-1$
		menuItemSingleContext.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_2, ActionEvent.ALT_MASK));
		contexts.add(menuItemSingleContext);
		menuItemContextAcrossPhases = new JRadioButtonMenuItem(
				Messages.getString("SimView.diffContext"), false); //$NON-NLS-1$
		menuItemContextAcrossPhases.setActionCommand("SetContextAcrossPhases"); //$NON-NLS-1$
		menuItemContextAcrossPhases.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_3, ActionEvent.ALT_MASK));
		contexts.add(menuItemContextAcrossPhases);
		menuContext.add(menuItemContextAcrossPhases);
		menuContext.add(menuItemSingleContext);

		/*menuItemCsc = new JCheckBoxMenuItem(
				Messages.getString("SimView.cscMode"), true); //$NON-NLS-1$
		menuItemCsc.setActionCommand("SetCsc"); //$NON-NLS-1$*/
		// menuSettings.add(menuItemCsc);

		menuItemSetCompound = new JCheckBoxMenuItem(
				Messages.getString("SimView.compounds"), true); //$NON-NLS-1$
		menuItemSetCompound.setActionCommand("SetCompound"); //$NON-NLS-1$
		menuItemSetCompound.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_C, ActionEvent.ALT_MASK));
		menuDesignSettings.add(menuItemSetCompound);

		//*/menuItemSerialCompounds = new JCheckBoxMenuItem(
				//Messages.getString("SimView.serialCompounds"), false); //$NON-NLS-1$
		//menuItemSerialCompounds.setActionCommand("SetSerialCompound"); //$NON-NLS-1$
		// menuItemSerialCompounds.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
		// ActionEvent.ALT_MASK));
		//menuDesignSettings.add(menuItemSerialCompounds);*/

		// Added by Alberto Fernandez August-2011
		// Option Configural Compounds

		/*menuItemSetConfiguralCompounds = new JCheckBoxMenuItem(
				Messages.getString("SimView.configurals"), false); //$NON-NLS-1$
		menuItemSetConfiguralCompounds
				.setActionCommand("SetConfiguralCompounds"); //$NON-NLS-1$
		menuItemSetConfiguralCompounds.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_X, ActionEvent.ALT_MASK));
		menuDesignSettings.add(menuItemSetConfiguralCompounds);

		menuMeans = new JMenu(Messages.getString("SimView.meanType")); //$NON-NLS-1$
		menuProcSettings.add(menuMeans);

		menuDistributions = new JMenu(Messages.getString("SimView.distType")); //$NON-NLS-1$
		menuProcSettings.add(menuDistributions);

		ButtonGroup distributions = new ButtonGroup();
		menuItemExp = new JRadioButtonMenuItem(
				Messages.getString("SimView.exponential"), true); //$NON-NLS-1$
		menuItemExp.setActionCommand("exp"); //$NON-NLS-1$
		menuItemExp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				ActionEvent.CTRL_MASK));
		distributions.add(menuItemExp);
		menuItemUnif = new JRadioButtonMenuItem(
				Messages.getString("SimView.uniform"), true); //$NON-NLS-1$
		menuItemUnif.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,
				ActionEvent.CTRL_MASK));
		distributions.add(menuItemUnif);
		menuDistributions.add(menuItemExp);
		menuDistributions.add(menuItemUnif);

		ButtonGroup means = new ButtonGroup();
		menuItemGeometric = new JRadioButtonMenuItem(
				Messages.getString("SimView.geometric"), false); //$NON-NLS-1$
		menuItemGeometric.setActionCommand("geo"); //$NON-NLS-1$
		menuItemGeometric.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
				ActionEvent.CTRL_MASK));
		means.add(menuItemGeometric);
		menuItemArithmetic = new JRadioButtonMenuItem(
				Messages.getString("SimView.arithmetic"), true); //$NON-NLS-1$
		menuItemArithmetic.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.CTRL_MASK));
		means.add(menuItemArithmetic);
		menuMeans.add(menuItemGeometric);
		menuMeans.add(menuItemArithmetic);*/

		menuBar.add(menuDesignSettings);
		menuBar.add(menuProcSettings);
		menuBar.add(menuElementSettings);
		menuHelp = new JMenu(Messages.getString("SimView.help")); //$NON-NLS-1$
		menuHelp.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menuHelp);

		menuItemGuide = new JMenuItem(
				Messages.getString("SimView.guide"), KeyEvent.VK_G); //$NON-NLS-1$
		menuItemGuide.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
				ActionEvent.ALT_MASK));
		menuItemGuide.setActionCommand("Guide"); //$NON-NLS-1$
		menuHelp.add(menuItemGuide);

		menuItemAbout = new JMenuItem(
				Messages.getString("SimView.about"), KeyEvent.VK_A); //$NON-NLS-1$
		menuItemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.ALT_MASK));
		menuItemAbout.setActionCommand("About"); //$NON-NLS-1$
		menuHelp.add(menuItemAbout);

		this.setJMenuBar(menuBar);
	}

	public float getContextAlphaR() {
		return contextAlphaR;
	}
	public float getContextAlphaN() {
		return contextAlphaN;
	}
	public float getContextSalience() {
		return contextSalience;
	}
	
	public void setContextAlphaR(float f) {contextAlphaR = f;}
	public void setContextAlphaN(float f) {contextAlphaN = f;}
	public void setContextSalience(float f) {contextSalience = f;}

	/**
	 * Returns the values' table.
	 * 
	 */
	public JTable getCSValuesTable() {
		return CSValuesTable;
	}
	
	public JTable getCSSalienceTable() {
		return CSSalienceTable;
	}

	/**
	 * Returns the values' table model. This helps to get the table's contents
	 * or set some new ones.
	 * 
	 * @return the values' table model.
	 */
	public CSValuesTableModel getCSValuesTableModel() {
		return CSValuesTableModel;
	}
	
	public JTable getUSSalienceTable() {
		return USSalienceTable;
	}
	
	public CSSalienceTableModel getCSSalienceTableModel() {
		return CSSalienceTableModel;
	}

	public CSVariableTableModel getCSVariableTableModel() {
		return CSVariableTableModel;
	}
	/**
	 * 
	 * @return An integer indicating the type of distribution form to use.
	 */

	public int getDistributionType() {
		int type = Distributions.EXPONENTIAL;
		if (menuItemUnif.isSelected()) {
			type = Distributions.UNIFORM;
		}
		return type;
	}

	/**
	 * It displays a JOptionPane.showInputDialog on top of the view's frame
	 * asking a float. The request message and current value are provided as
	 * arguments.
	 * 
	 * @param s
	 *            the message that is been displayed to the screen.
	 * @param cur
	 *            the current value that the variable, that will be change, has.
	 * @return the new value of the variable.
	 */
	public float getFloatInput(String s, String cur) {
		ImageIcon icon = createImageIcon("/simulator/extras/theicon.png", ""); 
		


        try {
            return Float.parseFloat(
                    (String) JOptionPane.showInputDialog(
                                    new JFrame(),
                                    s,
                                    "",
                                    JOptionPane.QUESTION_MESSAGE,
                                   icon,
                                    null,
                                    cur));
        }catch (Exception ex){
            return Float.parseFloat(cur);
        }
	}

	/**
	 * It displays a JOptionPane.showInputDialog on top of the view's frame
	 * asking an integer. The request message and current value are provided as
	 * arguments.
	 * 
	 * @param s
	 *            the message that is been displayed to the screen.
	 * @param cur
	 *            the current value that the variable, that will be change, has.
	 * @return the new value of the variable.
	 */
	public int getIntInput(String s, String cur) {

		ImageIcon icon = createImageIcon("/simulator/extras/theicon.png", ""); 
		


        try {
            return Integer.parseInt(
                    (String) JOptionPane.showInputDialog(
                                    new JFrame(),
                                    s,
                                    "",
                                    JOptionPane.QUESTION_MESSAGE,
                                   icon,
                                    null,
                                    cur));
        }catch (Exception ex){
            return Integer.parseInt(cur);
        }
		
		//pane.set
		//JOptionPane.show
		/*String input = JOptionPane.showInputDialog(s, cur);
		if (input == null)
			return -1;
		else
			return (int) Math.round(Float.parseFloat(input));*/
	}
	
	

	/**
	 * 
	 * @return true is the geometric mean option is selected.
	 */

	public boolean getMeanType() {
		return menuItemGeometric.isSelected();
	}

	/**
	 * @return the table holding gamma & delta values.
	 */
	public JTable getOtherValuesTable() {
		return otherValuesTable;
	}

	public OtherValuesTableModel getOtherValuesTableModel() {
		return otherTableModel;
	}

	/**
	 * Returns the phases' table.
	 * 
	 * @return the phases' table.
	 */
	public JTable getPhasesTable() {
		return phasesTable;
	}

	/**
	 * Returns the phases' table model. This helps to get the tables contents or
	 * set some new ones.
	 * 
	 * @return the phases' table model.
	 */
	public PhasesTableModel getPhasesTableModel() {
		return phasesTableModel;
	}


	/**
	 * Returns the values' table.
	 * 
	 * @return the values' table.
	 */
	public JTable getUSValuesTable() {
		return USValuesTable;
	}
	
	public JTable getDDValuesTable(){
		return DDValuesTable;
	}

	/**
	 * Returns the values' table model. This helps to get the table's contents
	 * or set some new ones.
	 * 
	 * @return the values' table model.
	 */
	public USValuesTableModel getUSValuesTableModel() {
		return USValuesTableModel;
	}
	
	public USSalienceTableModel getUSSalienceTableModel() {
		return USSalienceTableModel;
	}
	

	/**
	 * 
	 * @return true if traces should be forced to zero between trials.
	 */

	public boolean getZeroTraces() {
		return zeroTraces.isSelected();
	}

	/**
	 * @return true if the exponential menu item is selected.
	 */
	public boolean isExponential() {
		return menuItemExp.isSelected();
	}

	/**
	 * 
	 * @return a boolean indicating whether to use a different context stimulus
	 *         per phase
	 */

	public boolean isOmegaAcrossPhases() {
		return menuItemContextAcrossPhases.isSelected();
	}

	/**
	 * 
	 * @return true is predictions are restricted to >= 0.
	 */

	public boolean isRestrictPredictions() {
		return menuItemRestrictPredictions.isSelected();
	}

	/**
	 * Return if the compounds values are going to be shown
	 * 
	 * @return boolean
	 */
	public boolean isSetCompound() {
		return isSetCompound;
	}

	/**
	 * Return if the compounds values are going to be used
	 * 
	 * @return boolean
	 */
	public boolean isSetConfiguralCompounds() {
		return isSetConfiguralCompounds;
	}

	/**
	 * @return a boolean indicating whether the user has selected to show
	 *         response stats.
	 */

	public boolean isSetResponse() {
		return menuItemThreshold.isSelected();
	}

	/**
	 * Return if the cues of lambdas and betas are updatable across phases
	 * 
	 * @return boolean
	 */
	public boolean isUSAcrossPhases() {
		return isUSAcrossPhases;
	}

	/**
	 * 
	 * @return a boolean indicating whether to use the context stimulus.
	 */
	public boolean isUseContext() {
		return true;//!menuItemContext.isSelected();
	}

	/**
	 * 
	 * @return true if serial compounds should be considered.
	 */

	public boolean isUseSerialCompounds() {
		return menuItemSerialCompounds.isSelected();
	}

	/*
	 * Order a vector of cues by phases
	 * 
	 * @param vec Vector of records with cues to order
	 * 
	 * @param nlambdabeta number of lambda and beta cues in vec
	 * 
	 * @return Vector vector of cues ordered by phases
	 */
	private Vector orderByPhase(Vector vec, int nlambdabeta) {
		Vector v = new Vector();

		// alphas
		for (int j = 0; j < (vec.size() - nlambdabeta); j++) {
			v.addElement(vec.get(j));
		}
		// lambdas and betas per phase
		for (int i = 1; i <= model.getPhaseNo(); i++) {
			for (int k = (vec.size() - nlambdabeta); k < vec.size(); k++) { // finding
																			// lambda+,
																			// lambda-,
																			// beta+,
																			// beta-
				String cuename = (String) ((Object[]) vec.get(k))[0];
				if (("" + cuename.charAt(cuename.length() - 1)).equals("" + i))v.addElement(vec.get(k)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return v;
	}

	/*
	 * Remove the last group of the phasesTableModel
	 */
	public void removeGroup() {
		phasesTableModel.removeGroup();
		updatePhasesColumnsWidth();
	}

	/*
	 * Remove Omega across phases
	 */
	public void removeOmegaPhases() {
		// otherTableModel.removePhases(1);
		// otherValuesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		ContextConfig.clearDefault();
		hiddenColumns.clear();
		for (TableColumn column : Collections.list(phasesTable.getColumnModel()
				.getColumns())) {
			if (column.getHeaderValue().equals(CONTEXT)) {
				hiddenColumns.put(column, column.getModelIndex());
			}
		}
		for (TableColumn column : Collections.list(phasesTable.getColumnModel()
				.getColumns())) {
			if (column.getHeaderValue().equals(CONTEXT)) {
				phasesTable.getColumnModel().removeColumn(column);
			}
		}
	}

	/*
	 * Remove the last phase of the phasesTableModel
	 */
	public void removePhase() {
		phasesTableModel.removePhase();
		updatePhasesColumnsWidth();
	}

	/*
	 * Remove US across phases
	 */
	public void removeUSPhases() {
		USValuesTableModel.removePhases(1);
		USValuesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	}

	/**
	 * Reset the menus, tables and output to starting state.
	 */
	public void removeTabs() {

		if (tabs != null && resultPanel != null && !(tabs.getParent()== null)) {
			resultPanel.remove(tabs);
			if (aboutPanel != null ) resultPanel.add(aboutPanel);
		}
		
	}

	public void reset() {
		// Context off
		//menuItemContext.setSelected(true);
		// Phases table reeanbled
		removeTabs();
		getPhasesTable().setEnabled(true);
		// Export off
		setStatusComponent(false, "Export"); //$NON-NLS-1$
		// Save off
		setStatusComponent(false, "Save"); //$NON-NLS-1$
		// Set variables button on
		setStatusComponent(true, "setVariables"); //$NON-NLS-1$
		// Run button off
		setStatusComponent(false, "run"); //$NON-NLS-1$
		// Figures button off
		setStatusComponent(false, "dispGraph"); //$NON-NLS-1$
		// Clear output
		clearOutputArea();
		// US per phase off
		setIsUSAcrossPhases(false);
		setStatusComponent(false, "SetUSAcrossPhases"); //$NON-NLS-1$
		// Compounds off
		setStatusComponent(false, "SetCompound"); //$NON-NLS-1$
		// Added by Alberto Fernandez August-2011
		//setIsSetCompound(false);
		// Configurals off
		//setStatusComponent(false, "SetConfiguralCompounds"); //$NON-NLS-1$
		//setIsSetConfiguralCompounds(false);
		// Exponential distribution
		//menuItemExp.setSelected(true);
		// Arithmetic mean
		//menuItemArithmetic.setSelected(true);
		// Decision rule sim off
		//menuItemThreshold.setSelected(true);
		// Bounded traces on
		//setTraceType(Trace.REPLACING);
		otherTableModel.setInitialValuesTable();
		// Zero'd traces not on.
		//zeroTraces.setSelected(false);
		// Serials off
		//menuItemSerialCompounds.setSelected(false);
		// Timings by trial off
		menuItemTimingPerTrial.setSelected(false);
		externalSave.setSelected(true);
		//menuItemRestrictPredictions.setSelected(true);
		getPhasesTable().getColumnModel().getColumn(1).setCellEditor(new TrialStringEditor());
	}

	public void setContextAlpha(float contextAlpha) {
		this.contextAlphaR = contextAlpha;
	}

	/**
	 * Set if the compounds values are going to be shown
	 * 
	 * @param boolean
	 */
	public void setIsSetCompound(boolean b) {
		isSetCompound = b;
		menuItemSetCompound.setSelected(b);
	}

	/******************************************************************************************************************/
	/******************************************************************************************************************/

	/********************************************************/
	/********************************************************/

	/**
	 * Set if the configural compounds values are going to be used
	 * 
	 * @param boolean
	 */
	public void setIsSetConfiguralCompounds(boolean b) {
		isSetConfiguralCompounds = b;
	}

	/********************************************************/
	/********************************************************/

	/**
	 * Set if the cues of lambdas and betas are updatable across phases
	 * 
	 * @param boolean
	 */
	public void setIsUSAcrossPhases(boolean b) {
		isUSAcrossPhases = b;
	}

	/********************************************************/
	/********************************************************/

	/**
	 * Bulk set the alpha value for all contexts in a table and make all
	 * contexts phi.
	 * 
	 * @param alpha
	 */

	public void setOmegaSalience(float alphaR,float alphaN, float salience) {
		ContextConfig.clearDefault();
		contextAlphaR = alphaR;
		contextAlphaN = alphaN;
		contextSalience = salience;
		for (TableColumn column : Collections.list(phasesTable.getColumnModel()
				.getColumns())) {
			if (column.getHeaderValue().equals(CONTEXT)) {
				int col = column.getModelIndex();
				for (int i = 0; i < phasesTableModel.getRowCount(); i++) {
					((ContextConfig) phasesTableModel.getValueAt(i, col))
							.setAlphaR(alphaR);
					((ContextConfig) phasesTableModel.getValueAt(i, col))
					.setAlphaN(alphaN);
					((ContextConfig) phasesTableModel.getValueAt(i, col))
					.setSalience(salience);
					((ContextConfig) phasesTableModel.getValueAt(i, col))
							.setContext(Context.PHI);
					phasesTableModel.fireTableCellUpdated(i, col);
				}
			}
		}
	}

	/**
	 * Sets the output for the JTextArea object. Adds the String that is been
	 * passed from the application and then it displays it.
	 * 
	 * @param msg
	 */
	public void setOutput(String msg) {
		//outputArea.setText(msg);
	}//

	public void setRestrictPredictions(boolean on) {
		menuItemRestrictPredictions.setSelected(on);
	}

	/**
	 * Sets the status of a component, it could be a button or a menu item. This
	 * helps the smooth procedure of the application. It stops the user to
	 * choose an inappropriate action.
	 * 
	 * @param mode
	 *            set the component accordingly to this boolean value.
	 * @param b
	 *            the component that needs to be change. The string contains the
	 *            actionCommand.
	 */
	public void setStatusComponent(boolean mode, String b) {
		if (b.equals(setVariablesBut.getActionCommand()))
			setVariablesBut.setEnabled(mode);
			
		if (b.equals(clearBut.getActionCommand()))
			clearBut.setEnabled(mode);
		if (b.equals(runBut.getActionCommand()))
			runBut.setEnabled(mode);
			if (mode) {
				estherBox.setVisible(true);
				varyingVartheta.setVisible(true);
				gButton.setEnabled(true);
				
			}
			
		if (b.equals(dispGraphBut.getActionCommand()))
			dispGraphBut.setEnabled(mode);
		if (b.equals(intensityButton.getActionCommand()))
			intensityButton.setEnabled(mode);
		if (b.equals(timePredictionCheckList.getActionCommand()))
			timePredictionCheckList.setEnabled(mode);
		if (b.equals(presenceMeanCheckList.getActionCommand()))
			presenceMeanCheckList.setEnabled(mode);
		if (b.equals(estherBox.getActionCommand()))
			estherBox.setEnabled(mode);
		if (b.equals(responseCheckList.getActionCommand()))
				responseCheckList.setEnabled(mode);
		if (b.equals(aniCheckList.getActionCommand()))
			aniCheckList.setEnabled(mode);
		if (b.equals(contextCheckList.getActionCommand()))
			contextCheckList.setEnabled(mode);
		if (b.equals(salienceCheckList.getActionCommand()))
			salienceCheckList.setEnabled(mode);
		if (b.equals(vGraphCheckList.getActionCommand()))
			vGraphCheckList.setEnabled(mode);
		if (b.equals(waTimeCheckList.getActionCommand()))
			waTimeCheckList.setEnabled(mode);
		if (b.equals(waTrialCheckList.getActionCommand()))
			waTrialCheckList.setEnabled(mode);
		if (b.equals(errorCheckList.getActionCommand()))
			errorCheckList.setEnabled(mode);
		if (b.equals(floatingButtons.getActionCommand()))
			floatingButtons.setEnabled(mode);
		if (b.equals(errorTrialCheckList.getActionCommand()))
			errorTrialCheckList.setEnabled(mode);
		if (b.equals(trialResponseCheckList.getActionCommand()))
			trialResponseCheckList.setEnabled(mode);
		if (b.equals(componentActivationCheckList.getActionCommand()))
			componentActivationCheckList.setEnabled(mode);
		if (b.equals(componentPredictionCheckList.getActionCommand()))
			componentPredictionCheckList.setEnabled(mode);
		if (b.equals(componentDeltaCheckList.getActionCommand()))
			componentDeltaCheckList.setEnabled(mode);
		if (b.equals(menuItemSave.getActionCommand()))
			menuItemSave.setEnabled(mode);
		if (b.equals(menuItemExport.getActionCommand()))
			menuItemExport.setEnabled(mode);
	//	if (b.equals(menuItemUSAcrossPhases.getActionCommand()))
		//	menuItemUSAcrossPhases.setState(mode);
		if (b.equals(menuItemSetCompound.getActionCommand()))
			menuItemSetCompound.setState(mode);
		if (b.equals(menuItemTimingPerTrial.getActionCommand()))
			menuItemTimingPerTrial.setState(mode);
		//if (b.equals(menuItemSetConfiguralCompounds.getActionCommand()))
		//	menuItemSetConfiguralCompounds.setState(mode);
		//if (b.equals(menuItemContext.getActionCommand()))
		//	menuItemContext.setSelected(mode);
		if (b.equals(menuItemContextAcrossPhases.getActionCommand()))
			menuItemContextAcrossPhases.setSelected(mode);
		if (b.equals(menuItemSingleContext.getActionCommand()))
			menuItemSingleContext.setSelected(mode);
		if (b.equals(menuItemNoReset.getActionCommand()))
			menuItemNoReset.setSelected(mode);
		if (b.equals(menuItemReset.getActionCommand()))
			menuItemReset.setSelected(mode);
		/*if (b.equals(menuR1.getActionCommand()))
			menuR1.setSelected(mode);
		if (b.equals(menuR2.getActionCommand()))
			menuR2.setSelected(mode);*/
		//if (b.equals(menuItemExp.getActionCommand()))
		//	menuItemExp.setSelected(mode);
		//if (b.equals(menuItemGeometric.getActionCommand()))
		//	menuItemGeometric.setSelected(mode);
	}

	/**
	 * Enable or disable the timing per trial option.
	 * 
	 * @param on
	 */

	public void setTimingPerTrial(boolean on) {
		menuItemTimingPerTrial.setSelected(on);
	}

	

	/**
	 * @param lock
	 *            true to lock the UI
	 */
	public void setUILocked(boolean lock) {
		menuBar.setEnabled(!lock);
		addGroupBut.setEnabled(!lock);
		addGroupBut.setEnabled(!lock);
		setVariablesBut.setEnabled(!lock);
		clearBut.setEnabled(!lock);
		runBut.setEnabled(!lock);
		dispGraphBut.setEnabled(!lock);

		intensityButton.setEnabled(!lock);
		addPhaseBut.setEnabled(!lock);
		removePhaseBut.setEnabled(!lock);
		gButton.setEnabled(!lock);
	}

	/**
	 * 
	 * @param on
	 *            set to true to use serial compounds.
	 */

	public void setUseSerialCompounds(boolean on) {
		menuItemSerialCompounds.setSelected(on);
	}

	/**
	 * 
	 * @param on
	 *            set to true to force traces to zero between trials.
	 */

	public void setZeroTraces(boolean on) {
		zeroTraces.setSelected(on);
	}

	/**
	 * showAbout displays information dialogs directly to the user.
	 * 
	 * @param message
	 *            - the String to be displayed.
	 */
	public void showAbout(String message) {
		JOptionPane
				.showMessageDialog(
						this,
						message,
						Messages.getString("SimView.aboutTitle"), JOptionPane.PLAIN_MESSAGE); //NO_OPTION //$NON-NLS-1$
	}



	/**
	 * showAbout displays information dialogs directly to the user.
	 * 
	 * @param message
	 *            - the String to be displayed.
	 */
	public void showAboutLogo(String path) {
		// Modified by E Mondragon July 29, 2011
		JFrame.setDefaultLookAndFeelDecorated(false);

		JFrame about = new JFrame();

		JPanel aboutPanel = new JPanel();
		aboutPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		aboutPanel.setBackground(Color.WHITE);

		// modified by E.Mondragon. July 29, 2011
		// ImageIcon icon = createImageIcon(path, "About");
		ImageIcon icon = createImageIcon(
				"/simulator/extras/splash.png", ""); //$NON-NLS-1$

		aboutPanel.setBorder(new SimBackgroundBorder(icon.getImage(), true));
		about.getContentPane().add(aboutPanel);
		about.pack();
		about.setLocation(this.INITIAL_WIDTH/3, this.INITIAL_HEIGHT/4);
		about.setSize(icon.getIconWidth(), icon.getIconHeight());
		about.setVisible(true);
		about.setTitle(Messages.getString("SimView.title"));//E.Mondragon 30 Sept 2011 //$NON-NLS-1$
		ImageIcon icon2 = createImageIcon("/simulator/extras/icon_32.png", "");//R&W.png", ""); E.Mondragon 30 Sept 2011 //$NON-NLS-1$ //$NON-NLS-2$
		about.setIconImage(icon2.getImage());// E.Mondragon 30 Sept 2011
		
		
		

	}

	/**
	 * showError displays error message dialogs directly to the user.
	 * 
	 * @param errMessage
	 *            - the String to be displayed.
	 */
	public void showError(String errMessage) {
		JOptionPane
				.showMessageDialog(
						this,
						errMessage,
						Messages.getString("SimView.errorTitle"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
	}

	/**
	 * 
	 * @return true is timings should be set per trial
	 */

	public boolean timingPerTrial() {
		return menuItemTimingPerTrial.isSelected();
	}
	
	public boolean isExternalSave() {
		return externalSave.isSelected();
	}

	/**
	 * Switch the omega/context variable on or off.
	 * 
	 * @param on
	 */

	public void toggleContext(boolean on) {
		if (on /* && getIsOmegaAcrossPhases() */) {
			addOmegaPhases();
		} else {
			removeOmegaPhases();
		}
	}

	public void updateModel(SimModel m) {
		model = m;
	}
	
	public boolean isUseCompound() {return isSetCompound();}

	/**
	 * Set the width for the cells of the TableModel
	 */
	public void updateOtherValuesColumnsWidth() {
		otherValuesTable.getColumnModel().getColumn(0).setPreferredWidth(this.INITIAL_WIDTH/6);
		for (int i = 1; i < otherValuesTable.getColumnCount(); i++) {
			otherValuesTable.getColumnModel().getColumn(i)
					.setPreferredWidth(this.INITIAL_WIDTH/6);
			otherValuesTable.getColumnModel().getColumn(i).setWidth(this.INITIAL_WIDTH/3);
		}
	}

	/**
	 * Set the width for the cells of the TableModel
	 */
	public void updatePhasesColumnsWidth() {
		for (int i = 0; i < phasesTable.getColumnCount(); i++) {
			final TableColumn tableColumn = phasesTable.getColumnModel()
					.getColumn(i);
			final TableCellRenderer headRend = phasesTable.getTableHeader()
					.getDefaultRenderer();
			tableColumn.setPreferredWidth(headRend
					.getTableCellRendererComponent(phasesTable,
							tableColumn.getHeaderValue(), false, false, -1, i)
							
					.getPreferredSize().width + this.INITIAL_WIDTH/90);
			//tableColumn.setPreferredWidth(00);
			for (int j = 0; j < phasesTable.getRowCount(); j++) {
				int width = phasesTable
						.getCellRenderer(j, i)
						.getTableCellRendererComponent(phasesTable,
								phasesTable.getValueAt(j, i), false, false, j,
								i).getPreferredSize().width;
				tableColumn.setPreferredWidth(Math.max(width + this.INITIAL_WIDTH/90,
						tableColumn.getPreferredWidth()));
			}

		}
	}

	/**
	 * Update all the timing configurations in the table to match their
	 * respective phase strings.
	 */

	public void updateTimingConfigs() {
		CS contextCS = null;
		
		ArrayList<String> USs = new ArrayList<String>();
		for (int i = 0; i < phasesTable.getColumnCount(); i++) {
			final TableColumn tableColumn = phasesTable.getColumnModel()
					.getColumn(i);
			/*if (((String) tableColumn.getHeaderValue()).equals("Context")) {
				int col = tableColumn.getModelIndex();
				ContextConfig contextCfg = (ContextConfig) phasesTable.getValueAt(0, col);
				contextCS = new CS(contextCfg.getSymbol(),0,0);
			}*/
			if (((String) tableColumn.getHeaderValue()).equals(TIMING)) {
				for (int j = 0; j < phasesTable.getRowCount(); j++) {
					
					ArrayList<String> phaseUSs = new ArrayList();
					int offset = 4;
					// If we aren't simming context, we need to go back one less
					// column
					if (!phasesTable.getColumnName(i - offset).contains(
							Messages.getString("SimView.phase"))) { //$NON-NLS-1$
						offset--;
					}
					String phaseString = (String) phasesTable.getValueAt(j, i
							- offset);
					List<CS> stimuli = new ArrayList<CS>();
					phaseString = phaseString == null ? "" : phaseString; //$NON-NLS-1$
					stimuli.addAll(SimPhase.stringToCSList(phaseString));
					if (contextCS != null) {stimuli.add(contextCS);}
					TimingConfiguration timings = ((TimingConfiguration) phasesTable
							.getValueAt(j, i));
					timings.setStimuli(stimuli);
					boolean reinforced = false;
					for (String usName : USNames.getNames()) {
						if (phaseString.contains(usName) && !"-".equals(usName)) {
							if (!USs.contains(usName)) {USs.add(usName);}
							phaseUSs.add(usName);
							reinforced = true;
						}
					}
						if (!reinforced && !phaseString.isEmpty()) {
							timings.setReinforced(false,USs);
						} else {
							timings.setReinforced(true,USs);
						}
					timings.updateUSNames(phaseString);
				}
			}

		}
		usNumber = USs.size();
	}

	/**
	 * Set the width for the cells of the TableModel
	 */
	public void updateUSValuesColumnsWidth() {
		USValuesTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		for (int i = 1; i < USValuesTable.getColumnCount(); i++) {
			USValuesTable.getColumnModel().getColumn(i).setPreferredWidth(150);
		}
	}
	
	public void toggleReset(boolean reset) { 
		if (reset) {
			menuItemNoReset.setSelected(false);
		} else {

			menuItemReset.setSelected(false);
		}
	}
	
	public SimModel getModel() {return model;}
	
	
	public JMenuItem getMenuItemOpen() {return menuItemOpen;}
	public JButton getRunButton() {return runBut;}
	public JButton getVariablesButton() {return setVariablesBut;}
	public int getUSNumber () {return usNumber;}

	public void toggleRule(int i) {
		
		if (i == 1) {menuR2.setSelected(false);}
		else {menuR1.setSelected(false);}
	}

}