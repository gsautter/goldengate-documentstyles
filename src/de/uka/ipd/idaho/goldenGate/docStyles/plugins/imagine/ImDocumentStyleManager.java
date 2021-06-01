/*
 * Copyright (c) 2006-, IPD Boehm, Universitaet Karlsruhe (TH) / KIT, by Guido Sautter
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Universitaet Karlsruhe (TH) / KIT nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY UNIVERSITAET KARLSRUHE (TH) / KIT AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uka.ipd.idaho.goldenGate.docStyles.plugins.imagine;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.Gamta;
import de.uka.ipd.idaho.gamta.Tokenizer;
import de.uka.ipd.idaho.gamta.defaultImplementation.AbstractAttributed;
import de.uka.ipd.idaho.gamta.util.CountingSet;
import de.uka.ipd.idaho.gamta.util.DocumentStyle;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.AbstractData;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.Anchor;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.Data;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.ParameterDescription;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.ParameterGroupDescription;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.Provider;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.TestableElement;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.imaging.BoundingBox;
import de.uka.ipd.idaho.gamta.util.swing.AnnotationDisplayDialog;
import de.uka.ipd.idaho.gamta.util.swing.DialogFactory;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentListElement;
import de.uka.ipd.idaho.goldenGate.docStyles.plugins.imagine.ImDocumentStyleProvider.LiveData;
import de.uka.ipd.idaho.goldenGate.plugins.AbstractResourceManager;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePlugin;
import de.uka.ipd.idaho.goldenGate.plugins.ResourceManager.PreLoadingResourceManager;
import de.uka.ipd.idaho.goldenGate.util.DialogPanel;
import de.uka.ipd.idaho.goldenGateServer.dss.client.GoldenGateDssClient;
import de.uka.ipd.idaho.goldenGateServer.uaa.client.AuthenticatedClient;
import de.uka.ipd.idaho.goldenGateServer.uaa.client.AuthenticationManagerPlugin;
import de.uka.ipd.idaho.htmlXmlUtil.Parser;
import de.uka.ipd.idaho.htmlXmlUtil.TokenReceiver;
import de.uka.ipd.idaho.htmlXmlUtil.TreeNodeAttributeSet;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.StandardGrammar;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.ImPage;
import de.uka.ipd.idaho.im.ImRegion;
import de.uka.ipd.idaho.im.ImWord;
import de.uka.ipd.idaho.im.gamta.ImDocumentRoot;
import de.uka.ipd.idaho.im.gamta.ImTokenSequence;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.plugins.DisplayExtensionProvider;
import de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener;
import de.uka.ipd.idaho.im.imagine.plugins.SelectionActionProvider;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.DisplayExtension;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.DisplayExtensionGraphics;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.SelectionAction;
import de.uka.ipd.idaho.im.util.ImDocumentStyle;
import de.uka.ipd.idaho.im.util.ImDocumentStyle.PageFeatureAnchor;
import de.uka.ipd.idaho.im.util.ImUtils;
import de.uka.ipd.idaho.im.util.LinePattern;
import de.uka.ipd.idaho.plugins.bibRefs.BibRefTypeSystem;
import de.uka.ipd.idaho.stringUtils.StringUtils;

/**
 * This plug-in manages document style parameter lists for Image Markup
 * documents and helps users with creating and editing them.
 * 
 * @author sautter
 */
public class ImDocumentStyleManager extends AbstractResourceManager implements DisplayExtensionProvider, SelectionActionProvider, PreLoadingResourceManager, GoldenGateImagineDocumentListener {
	static final String INHERIT_VALUES_FROM_ATTRIBUTE = "inheritValuesFrom";
	private static final long defaultDocStyleLastMod = System.currentTimeMillis(); // the latest possible time a document style loaded from disk was modified
	private static final ParameterGroupDescription anchorRootDescription = new ParameterGroupDescription("anchor");
	static {
		anchorRootDescription.setLabel("Anchors");
		anchorRootDescription.setDescription("Anchors automate the assignment of document styles to individual documents. In particular, anchors match on distinctive landmark features on the first few pages of documents, e.g. a journal name in a specific position and font size.");
		anchorRootDescription.setParamLabel("maxPageId", "Maximum Pages After First");
		anchorRootDescription.setParamDescription("maxPageId", "The maximum number of pages to serach for anchor targets after the very first page.");
	}
	
//	private static final ParameterGroupDescription anchorDescription = new ParameterGroupDescription("anchor");
//	static {
//		anchorDescription.setLabel("Anchors");
//		anchorDescription.setDescription("Anchors automate the assignment of document styles to individual documents. In particular, anchors match on distinctive landmark features on the very first few pages of documents, e.g. a journal name in a specific position and font size.");
//		anchorDescription.setParamLabel("minFontSize", "Minimum Font Size");
//		anchorDescription.setParamDescription("minFontSize", "The minimum font size of the anchor target (use only if variation present or to be expected, otherwise use exact font size)");
//		anchorDescription.setParamLabel("maxFontSize", "Maximum Font Size");
//		anchorDescription.setParamDescription("maxFontSize", "The maximum font size of the anchor target (use only if variation present or to be expected, otherwise use exact font size)");
//		anchorDescription.setParamLabel("fontSize", "Exact Font Size");
//		anchorDescription.setParamDescription("fontSize", "The exact font size of the anchor target (use minimum and maximum font size if variation present or to be expected)");
//		anchorDescription.setParamLabel("isBold", "Is the Anchor Target Bold?");
//		anchorDescription.setParamDescription("isBold", "Is the anchor target set in bold face?");
//		anchorDescription.setParamLabel("isItalics", "Is the Anchor Target in Italics?");
//		anchorDescription.setParamDescription("isItalics", "Is the anchor target set in italics?");
//		anchorDescription.setParamLabel("isAllCaps", "Is the Anchor Target in All-Caps?");
//		anchorDescription.setParamDescription("isAllCaps", "Is the anchor target set in all-caps?");
//		anchorDescription.setParamLabel("pattern", "Pattern Matching Anchor Target");
//		anchorDescription.setParamDescription("pattern", "A pattern matching the anchor target; should be as restrictive as possible to avoid ambiguity.");
//		anchorDescription.setParamRequired("pattern");
//		anchorDescription.setParamLabel("area", "Anchor Target Area");
//		anchorDescription.setParamDescription("area", "A bounding box locating the anchor target; should be as precise as possible to avoid ambiguity.");
//		anchorDescription.setParamRequired("area");
//	}
	
	/* TODO Maintain list of available style parameters and their classes in abstract superclass of style managers, alongside many other things (no need to do them twice):
	 * - resource managing document style sources
	 * - publishing to local style provider
	 * - publishing to DSS (with all availability and authentication hassle)
	 * - resolving inheritance on publishing (including respective tractability attribute)
	 * - use and minting of document style UUIDs
	 * - most input field types (don't care too much about duplicating those, though, as yet more generic solution sketched below makes far more sense)
	 * - "priority" data substitution
	 * - visualization of inheritance
	 * - handling and persistence of XML parameter group descriptions
	 * - edit dialog proper:
	 *   - just need somewhat custom content panels
	 *   - inheritance same on both data models
	 * ==> need to abstract anchor fields and respective context menu options, though

ALSO, in long(er) haul, make style editor access independent of documents:
- add "Plugins" menu to GGI ...
- ... and populate with items from document independent function providers
  ==> use same in GGE after core extraction
  ==> return array of DocumentIndependentFunction objects ...
  ==> ... providing getters for label and tooltip, and execute() methods ...
  ==> ... as well as "master configuration only" indicator
    ==> make it (or provide) abstract class ...
    ==> ... taking label, tooltip, and "require master configuration" for constructor arguments
    ==> most likely do same for server side component actions ...
    ==> ... taking action command as constructor argument
- use to offer logging out from server
- use to offer "Edit Document Styles" (see below)
- use to offer editing other resources (akin to GGE)
  ==> thanks to "local resource" feature (originally introduced for templates), users can even create own gizmos (patterns, pipelines, etc.) for own projects
    ==> add "is local resource" check to configuration (and data providers ???) to restrict editing in non-master mode

Build document independent template editor (bit of a template IDE ...):
- show list of templates in list one left
  ==> put navigator there in resource editors as well
- show template editor (as built now) next to it
  ==> maybe abstract it ...
  ==> ... getting resource editor panel from parent manager for given resource name ...
  ==> ... and providing "save if dirty" method on panel proper
  ==> ALSO, use to-create string input utils in resource editors
- on "document saved", remember which document has which template assigned ...
- ... and use that to offer opening (or finding) test document for current style
  ==> offer getting test document for specific resource in GAMTA ...
    ==> null resource emulates current behavior
  ==> ... and use that for testing styles
  ==> maybe also allow specifying desired document class ...
  ==> ... using Object as interface return type (not even attributed object) ...
  ==> ... to facilitate using plain token (or even char) sequences for patterns ...
    ==> resource providers know best what they need for testing ...
    ==> ... and providers know best what they can offer
    ==> provide test data type dependent priority indicator ...
    ==> ... and use that in test data getter for sorting
- maybe also show template ID, name, and version in DIO stats
  ==> custom (GGv3.Plazi) test data provider can get list of examples from ACS

ALSO, revisit GG resources (when cutting GG Core out of GG Editor):
- add getter for provider proper (not only class name)
  ==> helps with direct "apply to" calls
- add "apply to" method, with attributed object as arguments
- create ApplicableResource sub interface to bear that method ...
- ... and also provide "is applicable to" indicator method
==> simplifies whole resource landscape
==> helps centralize resource application
==> facilitates general GG plugins (e.g. pipelines of IMTs) ...
==> ... or global error checkers for both XML and IM
==> set (or at least indicate) class of documents resources are applicable to in some way ...
==> ... to facilitate keeping pipelines in one domain
  ==> have parent plugin host indicate class of target documents ...
  ==> ... or provide latter via setter (keeping parent strictly GG Core)
- move GGE specific plugins to goldengate-editor-plugins ...
- ... retaining only universal ones in goldengate-plugins (the core plugins)
   ==> GGE at same level as GGI (despite one directional data model wrapping capability)
	 */
	
	private GoldenGateImagine ggImagine;
	private Settings parameterValueClassNames;
	private Map parameterValueClasses = Collections.synchronizedMap(new HashMap());
	private BibRefTypeSystem refTypeSystem;
	ImDocumentStyleProvider docStyleProvider;
	
	private Map docStylesById = Collections.synchronizedMap(new HashMap());
	private Map docStylesByName = Collections.synchronizedMap(new TreeMap(String.CASE_INSENSITIVE_ORDER));
	
	//	need to be able to make do without these !!!
	boolean dssAvailable;
	private GoldenGatePlugin authManagerObj;
	private Object authClientObj;
	private Object dssClientObj;
	
	/** zero-argument constructor for class loading */
	public ImDocumentStyleManager() {}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractResourceManager#getPluginName()
	 */
	public String getPluginName() {
		return "IM Document Style Manager";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.ResourceManager#getResourceTypeLabel()
	 */
	public String getResourceTypeLabel() {
		return "Document Style Sources";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractResourceManager#getFileExtension()
	 */
	protected String getFileExtension() {
		return ".docStyleSource";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#init()
	 */
	public void init() {
		
		//	connect to document style provider for storage
		this.docStyleProvider = ((ImDocumentStyleProvider) this.parent.getPlugin(ImDocumentStyleProvider.class.getName()));
		
		//	get reference type system for publication types
		this.refTypeSystem = BibRefTypeSystem.getDefaultInstance();
		
		//	read existing style parameters (no way they have all been requested at this point)
		try {
			Reader spr = new BufferedReader(new InputStreamReader(this.dataProvider.getInputStream("styleParameters.cnfg"), "UTF-8"));
			this.parameterValueClassNames = Settings.loadSettings(spr);
			spr.close();
			String[] spns = this.parameterValueClassNames.getKeys();
			for (int p = 0; p < spns.length; p++) try {
				this.parameterValueClasses.put(spns[p], Class.forName(this.parameterValueClassNames.getSetting(spns[p])));
			}
			catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.out);
		}
		
		//	read and hash available parameter group descriptions
		String[] dataNames = this.dataProvider.getDataNames();
		for (int d = 0; d < dataNames.length; d++) {
			if (dataNames[d].endsWith(".pgd.xml"))
				this.loadParameterGroupDescription(dataNames[d].substring(0, (dataNames[d].length() - ".pgd.xml".length())));
		}
		
		//	test if we have available what classes we need to communicate with DSS
		Object dssClientObj = null;
		try {
			this.authManagerObj = this.parent.getPlugin(AuthenticationManagerPlugin.class.getName());
			dssClientObj = new GoldenGateDssClient((AuthenticatedClient) null);
		}
		catch (Throwable t) {
			t.printStackTrace(System.out);
		}
		this.dssAvailable = (dssClientObj != null);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.ResourceManager.PreLoadingResourceManager#preLoadResources()
	 */
	public void preLoadResources() {
		
		//	prod style provider to load styles
		this.docStyleProvider.getStyleFor(new AbstractAttributed());
		
		//	pre-load all document style sources (indexing happens automatically)
		String[] docStyleSourceNames = this.getResourceNames();
		System.out.println("Got local document style names: " + Arrays.toString(docStyleSourceNames));
		for (int s = 0; s < docStyleSourceNames.length; s++) {
			DocStyleSettings docStyle = this.getDocStyleByName(docStyleSourceNames[s], false);
			System.out.println("Got local document style '" + docStyle + "'.");
		}
		
		//	re-get all document style sources (having them resolved, now that we have them all indexed)
		for (int s = 0; s < docStyleSourceNames.length; s++) {
			DocStyleSettings docStyle = this.getDocStyleByName(docStyleSourceNames[s], true);
			System.out.println("Resolved local document style '" + docStyle + "'.");
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImaginePlugin#setImagineParent(de.uka.ipd.idaho.im.imagine.GoldenGateImagine)
	 */
	public void setImagineParent(GoldenGateImagine ggImagine) {
		this.ggImagine = ggImagine; // we need this to issue display extension change notifications
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImaginePlugin#initImagine()
	 */
	public void initImagine() { /* nothing to initialize (apart from data migration code) */
		String migrationTarget = this.ggImagine.getConfiguration().getSettings().getSetting("DocStyles.MIGRATION_TARGET");
		if (migrationTarget == null)
			return;
		if ("DSS".equals(migrationTarget) && this.dssAvailable)
			this.importNamedDocStyleData("DSS");
		else if ("DSS".equals(migrationTarget) || "LOC".equals(migrationTarget))
			this.importNamedDocStyleData("LOC");
	}
	
	/**
	 * @deprecated remove after POA data migration
	 */
	private void importNamedDocStyleData(String migrationTarget) {
		
		/* TODO use scaffolding code for migration from old plug-ins (especially for POA folks):
		 * ==> tell POA folks to start their machines (or GGI specifically) 15 minutes apart after update (Felipe or Carol first)
		 *   ==> best coordinate this via Skype ...
		 *   ==> ... as that facilitates forcing list cache at right moment
		 * 
		 * DocStyles.MIGRATION_TARGET = "DSS";
		 * 
		 * Felipe:
		 * - start GGI to get update
		 * - close GGI
		 * - set DocStyles.MIGRATION_TARGET to DSS in Default.imagine GoldenGATE.cnfg
		 * - start GGI again
		 * - do migration
		 * 
		 * Carol:
		 * - start GGI to get update
		 * - close GGI
		 * - set DocStyles.MIGRATION_TARGET to DSS in Default.imagine GoldenGATE.cnfg
		 * - start GGI again
		 * - do migration
		 */
		while (!this.parent.isStartupFinished()) try {
			System.out.println("Waiting for provider to finish initialization ...");
			Thread.sleep(500);
		} catch (InterruptedException ie) {}
		
		String[] docStyleNames = this.docStyleProvider.getNamedDocStyleNames();
		if (docStyleNames.length == 0)
			return;
		ArrayList importDocStyleNames = new ArrayList();
		for (int n = 0; n < docStyleNames.length; n++) {
			if (this.docStylesByName.containsKey(docStyleNames[n]))
				continue; // we've imported this one before
			importDocStyleNames.add(docStyleNames[n]);
		}
		if (importDocStyleNames.isEmpty())
			return;
		
		docStyleNames = ((String[]) importDocStyleNames.toArray(new String[importDocStyleNames.size()]));
		DocStyleTargetSelector[] importTargets = new DocStyleTargetSelector[docStyleNames.length];
		JPanel importTargetPanel = new JPanel(new GridLayout(0, 2), true);
		for (int n = 0; n < docStyleNames.length; n++) {
			importTargetPanel.add(new JLabel(docStyleNames[n], JLabel.LEFT));
			importTargets[n] = new DocStyleTargetSelector(migrationTarget);
			importTargetPanel.add(importTargets[n]);
		}
		int choice = DialogFactory.confirm(importTargetPanel, "Select Publication Targets for Migrated Local Document Style Templates", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (choice != JOptionPane.OK_OPTION)
			return;
		
		boolean needDss = false;
		for (int n = 0; n < docStyleNames.length; n++)
			if (importTargets[n].dss.isSelected()) {
				needDss = true;
				break;
			}
		if (needDss && !this.ensureLoggedIn()) {
			choice = DialogFactory.confirm("Publishing migrated document style templates to the server is not possible at this point. Publish them locally instead?", "Cannot Publishing Document Style Templates to Server", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
			if ((choice != JOptionPane.YES_OPTION) && (choice != JOptionPane.NO_OPTION))
				return;
			for (int n = 0; n < docStyleNames.length; n++) {
				if (importTargets[n].dss.isSelected())
					((choice == JOptionPane.YES_OPTION) ? importTargets[n].loc : importTargets[n].none).setSelected(true);
			}
		}
		
		for (int n = 0; n < docStyleNames.length; n++) try {
			
			//	get settings, set attributes, and store locally
			Settings docStyleSet = this.docStyleProvider.getNamedDocStyleData(docStyleNames[n]);
			docStyleSet.removeSetting(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE);
			String docStyleId = Gamta.getAnnotationID();
			docStyleSet.setSetting(("@." + DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE), docStyleId);
			docStyleSet.setSetting(("@." + DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE), docStyleNames[n]);
			docStyleSet.setSetting(("@." + DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE), ("" + System.currentTimeMillis()));
			String docStyleDataName = docStyleNames[n];
			if (!docStyleDataName.endsWith(".docStyleSource"))
				docStyleDataName += ".docStyleSource";
			this.storeSettingsResource(docStyleDataName, docStyleSet);
			
			//	load back migrated data and publish both locally and to DSS to make newly minted ID public
			DocStyleSettings docStyle = this.getDocStyleByName(docStyleNames[n], true);
			if (importTargets[n].dss.isSelected() && this.publishDocStyleDss(docStyle)) {
				System.out.println("Document style '" + docStyleNames[n] + "' published to DSS.");
				if (this.docStyleProvider.deleteNamedDocStyleData(docStyleNames[n]))
					System.out.println("Old local document style '" + docStyleNames[n] + "' removed.");
			}
			else if (importTargets[n].loc.isSelected() && this.publishDocStyleLoc(docStyle)) {
				System.out.println("Document style '" + docStyleNames[n] + "' published locally.");
				if (this.docStyleProvider.deleteNamedDocStyleData(docStyleNames[n]))
					System.out.println("Old local document style '" + docStyleNames[n] + "' removed.");
			}
		}
		catch (IOException ioe) {
			System.out.println("Error importing document style '" + ((String) importDocStyleNames.get(n)) + "': " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
	}
	
	private static class DocStyleTargetSelector extends JPanel {
		JRadioButton none = new JRadioButton("<None>");
		JRadioButton loc = new JRadioButton("Local");
		JRadioButton dss = new JRadioButton("Server");
		DocStyleTargetSelector(String target) {
			super(new GridLayout(1, 0), true);
			ButtonGroup bg = new ButtonGroup();
			bg.add(this.none);
			bg.add(this.loc);
			bg.add(this.dss);
			this.add(this.none);
			this.add(this.loc);
			this.add(this.dss);
			if ("DSS".equals(target))
				this.dss.setSelected(true);
			else if ("LOC".equals(target))
				this.loc.setSelected(true);
			else this.none.setSelected(true);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#exit()
	 */
	public void exit() {
		
		//	close style editor if open
		if (this.docStyleEditor != null)
			this.docStyleEditor.dispose();
		
		//	store all (known) style parameters and their value classes
		String[] params = DocumentStyle.getParameterNames();
		Arrays.sort(params);
		boolean paramsDirty = false;
		for (int p = 0; p < params.length; p++) {
			Class paramClass = DocumentStyle.getParameterValueClass(params[p]);
			Class eParamClass = ((Class) this.parameterValueClasses.get(params[p]));
			if ((eParamClass == null) || !paramClass.getName().equals(eParamClass.getName())) {
				this.parameterValueClassNames.setSetting(params[p], paramClass.getName());
				paramsDirty = true;
			}
		}
		if (paramsDirty) try {
			Writer spw = new BufferedWriter(new OutputStreamWriter(this.dataProvider.getOutputStream("styleParameters.cnfg"), "UTF-8"));
			this.parameterValueClassNames.storeAsText(spw);
			spw.close();
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.out);
		}
	}
	
//	static final DocStyleSettings CREATE_DOC_STYLE = new DocStyleSettings(null, "<create>", "<Create Document Style>", new Settings(), -1);
	DocStyleSettings[] getDocStyles() {
		return ((DocStyleSettings[]) this.docStylesByName.values().toArray(new DocStyleSettings[this.docStylesByName.size()]));
	}
	
	private DocStyleSettings getDocStyleById(String docStyleId) {
		DocStyleSettings docStyle = ((DocStyleSettings) this.docStylesById.get(docStyleId));
		if (docStyle == null)
			return null; // invalid ID
		if (docStyle.inheritedSettings != null)
			return docStyle; // resolved before
		String inheritedDocStyleId = docStyle.data.getSetting("@." + INHERIT_VALUES_FROM_ATTRIBUTE);
		if (inheritedDocStyleId != null) try {
			docStyle.setInheritedSettings(this.getDocStyleById(inheritedDocStyleId), true);
		}
		catch (IllegalArgumentException iae) {
			System.out.println("Failed to set parent document style of '" + docStyle.name + "': " + iae.getMessage());
			iae.printStackTrace(System.out);
		}
		return docStyle;
	}
	
	DocStyleSettings getDocStyleByName(String docStyleName, boolean resolve) {
		if (docStyleName.endsWith(".docStyleSource"))
			docStyleName = docStyleName.substring(0, (docStyleName.length() - ".docStyleSource".length()));
		
		//	check cache, and resolve on demand
		DocStyleSettings docStyle = ((DocStyleSettings) this.docStylesByName.get(docStyleName));
		if (docStyle != null) {
			if (resolve && (docStyle.inheritedSettings == null)) {
				String inheritedDocStyleId = docStyle.data.getSetting("@." + INHERIT_VALUES_FROM_ATTRIBUTE);
				if (inheritedDocStyleId != null) try {
					docStyle.setInheritedSettings(this.getDocStyleById(inheritedDocStyleId), true);
				}
				catch (IllegalArgumentException iae) {
					System.out.println("Failed to set parent document style of '" + docStyle.name + "': " + iae.getMessage());
					iae.printStackTrace(System.out);
				}
			}
			return docStyle;
		}
		
		//	load or create settings
		Settings docStyleData = this.loadDocStyleData(docStyleName);
		boolean docStyleDataStored = (docStyleData != null);
		if (docStyleData == null) {
			docStyleData = new Settings();
			docStyleData.setSetting(("@." + DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE), docStyleName);
		}
		
		//	make sure we have an ID (especially for importing legacy styles)
		String docStyleId = docStyleData.getSetting("@." + DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE);
		boolean docStyleIdAdded = (docStyleId == null);
		if (docStyleId == null) {
			docStyleId = Gamta.getAnnotationID();
			docStyleData.setSetting(("@." + DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE), docStyleId);
		}
		
		//	restore last timestamp (default to latest possible if absent)
		long docStyleLastMod = Long.parseLong(docStyleData.getSetting(("@." + DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE), "-1"));
		boolean docStyleLastModAdded = (docStyleLastMod == -1);
		if (docStyleLastMod == -1) {
			docStyleLastMod = defaultDocStyleLastMod;
			docStyleData.setSetting(("@." + DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE), ("" + docStyleLastMod));
		}
		
		//	persist data if name loaded from disk and ID or timestamp just added
		if (docStyleDataStored && (docStyleIdAdded || docStyleLastModAdded)) {
			String docStyleDataName = docStyleName;
			if (!docStyleDataName.endsWith(".docStyleSource"))
				docStyleDataName += ".docStyleSource";
			try {
				this.storeSettingsResource(docStyleDataName, docStyleData);
			}
			catch (IOException ioe) {
				System.out.println("Error storing document style '" + docStyleDataName + "' after adding ID: " + ioe.getMessage());
				ioe.printStackTrace(System.out);
			}
		}
		
		//	create and cache document style
		docStyle = new DocStyleSettings(this, docStyleId, docStyleName, docStyleData, (docStyleDataStored ? docStyleLastMod : -1));
		this.docStylesById.put(docStyle.id, docStyle);
		this.docStylesByName.put(docStyle.name, docStyle);
		
		//	get published modification timestamps from provider
		Attributed locPubDocStyleAttributes = this.docStyleProvider.getDocStyleAttributes(docStyleId, false);
		if ((locPubDocStyleAttributes != null) && locPubDocStyleAttributes.hasAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE)) try {
			long locPubDocStyleLastMod = Long.parseLong((String) locPubDocStyleAttributes.getAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE));
			docStyle.lastStoredTimePublishedLoc = locPubDocStyleLastMod;
		}
		catch (RuntimeException re) {
			System.out.println("Error adding published modification time to document style '" + docStyleName + "': " + re.getMessage());
			re.printStackTrace(System.out);
		}
		Attributed dssPubDocStyleAttributes = this.docStyleProvider.getDocStyleAttributes(docStyleId, true);
		if ((dssPubDocStyleAttributes != null) && dssPubDocStyleAttributes.hasAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE)) try {
			long dssPubDocStyleLastMod = Long.parseLong((String) dssPubDocStyleAttributes.getAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE));
			docStyle.lastStoredTimePublishedDss = dssPubDocStyleLastMod;
		}
		catch (RuntimeException re) {
			System.out.println("Error adding published modification time to document style '" + docStyleName + "': " + re.getMessage());
			re.printStackTrace(System.out);
		}
		
		//	resolve inheritance if asked to
		if (resolve) {
			String inheritedDocStyleId = docStyleData.getSetting("@." + INHERIT_VALUES_FROM_ATTRIBUTE);
			if (inheritedDocStyleId != null) try {
				docStyle.setInheritedSettings(this.getDocStyleById(inheritedDocStyleId), true);
			}
			catch (IllegalArgumentException iae) {
				System.out.println("Failed to set parent document style of '" + docStyle.name + "': " + iae.getMessage());
				iae.printStackTrace(System.out);
			}
		}
		
		//	finally ...
		return docStyle;
	}
	
	Settings loadDocStyleData(String docStyleName) {
		if (!docStyleName.endsWith(".docStyleSource"))
			docStyleName += ".docStyleSource";
		Settings docStyleData = this.loadSettingsResource(docStyleName);
		return (((docStyleData == null) || docStyleData.isEmpty()) ? null : docStyleData);
	}
	
	boolean docStyleNameChanged(DocStyleSettings docStyle, String newName) {
		if (docStyle.name.equals(newName))
			return true;
		if (this.docStylesByName.containsKey(newName))
			return false;
		this.docStylesByName.remove(docStyle.name);
		this.docStylesByName.put(newName, docStyle);
		return true;
	}
	
	void docStyleAbsorbed(DocStyleSettings docStyle) {
		this.docStylesById.remove(docStyle.id);
		this.docStylesByName.remove(docStyle.name);
		if (docStyle.lastStoredTime != -1) {
			String oldDsDataName = docStyle.storedName;
			if (!oldDsDataName.endsWith(".docStyleSource"))
				oldDsDataName += ".docStyleSource";
			this.deleteResource(oldDsDataName);
		}
	}
	
	boolean storeDocStyle(DocStyleSettings docStyle) {
		return this.storeDocStyle(docStyle, System.currentTimeMillis());
	}
	
	private boolean storeDocStyle(DocStyleSettings docStyle, long time) {
		try {
			if ((docStyle.inheritedSettings != null) && !this.storeDocStyle(docStyle.inheritedSettings, time))
				return false;
			if (!docStyle.isDirty())
				return true;
			
			docStyle.data.setSetting(("@." + DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE), docStyle.id);
			docStyle.data.setSetting(("@." + DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE), docStyle.name);
			docStyle.data.setSetting(("@." + DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE), ("" + time));
			if (docStyle.inheritedSettings == null)
				docStyle.data.removeSetting("@." + INHERIT_VALUES_FROM_ATTRIBUTE);
			else docStyle.data.setSetting(("@." + INHERIT_VALUES_FROM_ATTRIBUTE), docStyle.inheritedSettings.id);
			
			//	remove old name before storing new one, as other way around might cause trouble on case insensitive file systems
			if (docStyle.isNameDirty()) {
//				if (!docStyle.storedName.startsWith("<create>-")) {
				if (docStyle.lastStoredTime != -1) {
					String oldDsDataName = docStyle.storedName;
					if (!oldDsDataName.endsWith(".docStyleSource"))
						oldDsDataName += ".docStyleSource";
					this.deleteResource(oldDsDataName);
				}
				this.docStylesByName.remove(docStyle.storedName);
			}
			
			String docStyleDataName = docStyle.name;
			if (!docStyleDataName.endsWith(".docStyleSource"))
				docStyleDataName += ".docStyleSource";
			this.storeSettingsResource(docStyleDataName, docStyle.data);
			this.docStylesByName.put(docStyle.name, docStyle);
			docStyle.markClean(time);
			return true;
		}
		catch (IOException ioe) {
			System.out.println("Error storing document style '" + docStyle.name + "': " + ioe.getMessage());
			ioe.printStackTrace(System.out);
			return false;
		}
	}
	
	private DocStyleSettings[] selectPublishDocStyles(DocStyleSettings pubDocStyle, boolean forDss) {
		if (pubDocStyle.inheritingSettings.isEmpty() && (pubDocStyle.getMaxInheritingCount() < 2))
			return null; // no descendants, no forks in parent chain, nothing to publish
		
		//	find root
		DocStyleSettings rootDocStyle = pubDocStyle;
		while (rootDocStyle.inheritedSettings != null)
			rootDocStyle = rootDocStyle.inheritedSettings;
		
		//	collect to-publish document styles in breadth-first search
		ArrayList seekDocStyleList = new ArrayList();
		seekDocStyleList.add(rootDocStyle);
		ArrayList pubDocStyleList = new ArrayList();
		for (int s = 0; s < seekDocStyleList.size(); s++) {
			DocStyleSettings docStyle = ((DocStyleSettings) seekDocStyleList.get(s));
			if (docStyle.isInheritanceChainDirty())
				continue; // no use adding this one or any descendants
			seekDocStyleList.addAll(docStyle.inheritingSettings);
			if (docStyle == pubDocStyle)
				continue; // we just published this one ...
			//	TODO maybe might be useful even without anchors if assigned via decoding parameter ...
			if (docStyle.hasAnchors(true) && (forDss ? docStyle.needPublishDss() : docStyle.needPublishLoc()))
				pubDocStyleList.add(docStyle); // this one is good for and needs publishing
		}
		if (pubDocStyleList.isEmpty())
			return null;
		
		//	prompt user for selection
		DocStyleSettings[] pubDocStyles = ((DocStyleSettings[]) pubDocStyleList.toArray(new DocStyleSettings[pubDocStyleList.size()]));
		Arrays.sort(pubDocStyles);
		JCheckBox[] pubDocStyleSelects = new JCheckBox[pubDocStyles.length];
		JPanel pubDocStylePanel = new JPanel(new GridLayout(0, 1), true);
		pubDocStylePanel.add(new JLabel("<HTML>The following document styles are derived from ones that were since modified.<BR/>To publish them as well, select the ones to publish and click OK.</HTML>"));
		for (int s = 0; s < pubDocStyles.length; s++) {
			pubDocStyleSelects[s] = new JCheckBox(pubDocStyles[s].name, pubDocStyles[s].hasAnchors(false /* only pre-select distinctly assignable document styles */));
			pubDocStylePanel.add(pubDocStyleSelects[s]);
		}
		int choice = DialogFactory.confirm(pubDocStylePanel, "Publish Derived Document Styles", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (choice != JOptionPane.OK_OPTION)
			return null;
		
		//	return what was selected
		pubDocStyleList.clear();
		for (int s = 0; s < pubDocStyles.length; s++) {
			if (pubDocStyleSelects[s].isSelected())
				pubDocStyleList.add(pubDocStyles[s]);
		}
		return ((DocStyleSettings[]) pubDocStyleList.toArray(new DocStyleSettings[pubDocStyleList.size()]));
	}
	
	boolean publishDocStyleLoc(DocStyleSettings docStyle) {
		if (docStyle.isInheritanceChainDirty())
			return false;
		
		DocumentStyle.Data docStyleData = docStyle.getResolvedDocStyleData();
		if (!this.docStyleProvider.publishDocumentStyle(docStyle.name, docStyleData))
			return false;
		docStyle.markPublishedLoc();
		
		DocStyleSettings[] pubDocStyles = this.selectPublishDocStyles(docStyle, false);
		if (pubDocStyles != null)
			for (int s = 0; s < pubDocStyles.length; s++) {
				DocumentStyle.Data pubDocStyleData = pubDocStyles[s].getResolvedDocStyleData();
				if (this.docStyleProvider.publishDocumentStyle(pubDocStyles[s].name, pubDocStyleData))
					pubDocStyles[s].markPublishedLoc();
			}
		return true;
	}
	
	boolean publishDocStyleDss(DocStyleSettings docStyle) {
		if (!this.dssAvailable) {
			DialogFactory.alert(("The connection to the server is not available, please contact your administrator."), "Cannot Connect to Server", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		boolean isLoginError = true;
		try {
			if (!this.ensureLoggedIn())
				return false;
			isLoginError = false;
			this.doPublishDocStyleDss(docStyle);
			if (docStyle.getMaxInheritingCount() < 2) // check for any forks in inheritance chain
				return true;
		}
		catch (RuntimeException re) {
			re.printStackTrace(System.out);
			if (isLoginError)
				DialogFactory.alert(("An error occurred while connecting to the server: " + re.getMessage() + "\r\n(see log for details)"), "Error Connecting to Server", JOptionPane.ERROR_MESSAGE);
			else DialogFactory.alert(("An error occurred while publishing the document style template to the server: " + re.getMessage() + "\r\n(see log for details)"), "Error Publishing Document Style Template", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.out);
			DialogFactory.alert(("An error occurred while publishing the document style template to the server: " + ioe.getMessage() + "\r\n(see log for details)"), "Error Publishing Document Style Template", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		catch (Throwable t) {
			t.printStackTrace(System.out);
			this.dssAvailable = false;
			DialogFactory.alert(("Unable to connect to the server: " + t.getMessage() + "\r\n(see log for details, and contact your administrator)"), "Could not Connect to Server", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		DocStyleSettings[] pubDocStyles = this.selectPublishDocStyles(docStyle, true);
		if (pubDocStyles != null)
			for (int s = 0; s < pubDocStyles.length; s++) try {
				this.doPublishDocStyleDss(pubDocStyles[s]);
			}
			catch (Exception e) {
				e.printStackTrace(System.out);
				if ((s+1) == pubDocStyles.length) // down to last template, no use asking whether or not to continue ...
					DialogFactory.alert(("An error occurred while publishing document style template '" + pubDocStyles[s].name + "' to the server: " + e.getMessage() + "\r\n(see log for details)"), "Error Publishing Document Style Template", JOptionPane.ERROR_MESSAGE);
				else {
					int choice = DialogFactory.confirm(("An error occurred while publishing document style template '" + pubDocStyles[s].name + "' to the server: " + e.getMessage() + "\r\n(see log for details) Continue with other document style templates?"), "Error Publishing Document Style Template", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
					if (choice != JOptionPane.YES_OPTION)
						break;
				}
			}
		return true;
	}
	
	private void doPublishDocStyleDss(DocStyleSettings docStyle) throws IOException {
		DocumentStyle.Data docStyleData = docStyle.getResolvedDocStyleData();
		DocumentListElement dle = ((GoldenGateDssClient) this.dssClientObj).updateDocumentStyleFromData(docStyleData, docStyle.name);
		boolean docStyleAttributesModified = false;
		String[] dleAttributeNames = dle.getAttributeNames();
		for (int n = 0; n < dleAttributeNames.length; n++) {
			Object dleAttributeValue = dle.getAttribute(dleAttributeNames[n]);
			if (dleAttributeValue == null)
				continue;
			if (dleAttributeValue.equals(docStyleData.getAttribute(dleAttributeNames[n])))
				continue;
			docStyleData.setAttribute(dleAttributeNames[n], dle.getAttribute(dleAttributeNames[n]));
			docStyleAttributesModified = true;
		}
		if (docStyleAttributesModified) {
			if (this.docStyleProvider.publishDocumentStyle(docStyle.name, docStyleData)) // make updated provenance data available locally right away (saves round trip via servlet)
				docStyle.markPublishedLoc();
		}
		docStyle.markPublishedDss();
	}
	
	private boolean ensureLoggedIn() {
		
		//	test if connection alive
		if (this.authClientObj != null)
			try {
				//	test if connection alive
				if (((AuthenticatedClient) this.authClientObj).ensureLoggedIn())
					return true;
				
				//	connection dead (e.g. a session timeout), make way for re-getting from auth manager
				else {
					this.dssClientObj = null;
					this.authClientObj = null;
				}
			}
			
			//	server temporarily unreachable, re-login will be done by auth manager
			catch (IOException ioe) {
				this.dssClientObj = null;
				this.authClientObj = null;
				return false;
			}
		
		//	got no valid connection at the moment, try and get one
		if (this.authClientObj == null)
			this.authClientObj = ((AuthenticationManagerPlugin) this.authManagerObj).getAuthenticatedClient();
		
		//	authentication failed
		if (this.authClientObj == null)
			return false;
		
		//	got valid connection and permission
		if (((AuthenticatedClient) this.authClientObj).hasPermission(GoldenGateDssClient.UPDATE_DOCUMENT_STYLE_PERMISSION)) {
			this.dssClientObj = new GoldenGateDssClient((AuthenticatedClient) this.authClientObj);
			return true;
		}
		
		//	update permission lacking, clean up
		this.authClientObj = null;
		this.dssClientObj = null;
		this.dssAvailable = false;
		if (this.docStyleEditor != null)
			this.docStyleEditor.notifyDssUpdatePermissionLacking();
		return false;
	}
	
	private static class DocStyleSettings implements Comparable {
		private ImDocumentStyleManager host;
		DocStyleSettings rootSettings;
		String prefix;
		
		final String id;
		String name;
		Settings data;
		DocStyleSettings inheritedSettings;
		HashSet inheritingSettings = new HashSet();
		
		boolean dataDirty = false;
		String storedName;
		
		long lastStoredTime = -1;
		long lastStoredTimePublishedLoc = -1;
		long lastStoredTimePublishedDss = -1;
		
		DocStyleSettings(ImDocumentStyleManager host, String id, String name, Settings data, long storedTime) {
			this(host, null, null, id, name, data, storedTime, null);
		}
		DocStyleSettings(ImDocumentStyleManager host, DocStyleSettings rootSettings, String prefix, String id, String name, Settings data, long storedTime, DocStyleSettings inherited) {
			this.host = host;
			this.rootSettings = rootSettings;
			this.prefix = prefix;
			this.id = id;
			this.name = name;
			this.data = data;
			this.lastStoredTime = storedTime;
			this.lastStoredTimePublishedLoc = storedTime;
			this.lastStoredTimePublishedDss = storedTime;
			this.inheritedSettings = inherited;
			if (this.rootSettings == null)
				this.storedName = this.name;
		}
		
		void markDataDirty() {
			if (this.rootSettings == null)
				this.dataDirty = true;
			else this.rootSettings.markDataDirty();
		}
		boolean isDirty() {
			return (this.dataDirty || this.isNameDirty());
		}
		void setName(String name) {
			if (this.rootSettings != null)
				return;
			if (this.host == null)
				throw new IllegalStateException("Cannot change name on static constant document style");
			if (this.host.docStyleNameChanged(this, name)) {
				this.name = name;
				this.data.setSetting(("@." + DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE), name);
			}
			else throw new IllegalArgumentException("Document style name '" + name + "' already exists.");
		}
		boolean isNameDirty() {
			if (this.rootSettings != null)
				return false;
			if (this.storedName.equals(this.name))
				return false;
			return true;
		}
		void markClean(long time) {
			this.dataDirty = false;
			this.storedName = this.name;
			this.lastStoredTime = time;
		}
		
		boolean needPublishLoc() {
			if (this.rootSettings != null)
				return false; // only ever publish root
			if (this.lastStoredTime == -1)
				return false; // not even stored yet
			if (this.lastStoredTimePublishedLoc < this.lastStoredTime)
				return true; // we've been stored (from a dirty state) since last time publishing
			for (DocStyleSettings dss = this.inheritedSettings; dss != null; dss = dss.inheritedSettings) {
				if (this.lastStoredTimePublishedLoc < dss.lastStoredTime)
					return true; // inherited has been stored (from a dirty state) since last time publishing
			}
			return false; // nothing upstream updated since last publishing
		}
		void markPublishedLoc() {
			for (DocStyleSettings dss = this; dss != null; dss = dss.inheritedSettings)
				this.lastStoredTimePublishedLoc = Math.max(this.lastStoredTimePublishedLoc, dss.lastStoredTime);
		}
		boolean needPublishDss() {
			if (this.rootSettings != null)
				return false; // only ever publish root
			if (this.lastStoredTime == -1)
				return false; // not even stored yet
			if (this.lastStoredTimePublishedDss < this.lastStoredTime)
				return true; // we've been stored (from a dirty state) since last time publishing
			for (DocStyleSettings dss = this.inheritedSettings; dss != null; dss = dss.inheritedSettings) {
				if (this.lastStoredTimePublishedDss < dss.lastStoredTime)
					return true; // inherited has been stored (from a dirty state) since last time publishing
			}
			return false; // nothing upstream updated since last publishing
		}
		void markPublishedDss() {
			for (DocStyleSettings dss = this; dss != null; dss = dss.inheritedSettings)
				this.lastStoredTimePublishedDss = Math.max(this.lastStoredTimePublishedDss, dss.lastStoredTime);
		}
		
		String getSetting(String key) {
			String value = this.data.getSetting(key);
			if (value != null)
				return value;
			return ((this.inheritedSettings == null) ? null : this.inheritedSettings.getSetting(key));
		}
		String getSetting(String key, String def) {
			String value = this.getSetting(key);
			return ((value == null) ? def : value);
		}
		String[] getKeys() {
			if (this.inheritedSettings == null)
				return this.data.getKeys();
			TreeSet keys = new TreeSet(String.CASE_INSENSITIVE_ORDER);
			this.addKeys(keys);
			return ((String[]) keys.toArray(new String[keys.size()]));
		}
		void addKeys(TreeSet keys) {
			keys.addAll(Arrays.asList(this.data.getKeys()));
			if (this.inheritedSettings != null)
				this.inheritedSettings.addKeys(keys);
		}
		void setSetting(String key, String value) {
			String oldValue = this.data.setSetting(key, value);
			if ((oldValue == null) || !oldValue.equals(value))
				this.markDataDirty();
		}
		void setSetting(String id, String key, String value) {
			if (this.id.equals(id))
				this.data.setSetting(key, value);
			else if (this.inheritedSettings != null)
				this.inheritedSettings.setSetting(id, key, value);
		}
		void removeSetting(String key, boolean includeInherited) {
			String oldValue = this.data.removeSetting(key);
			if (oldValue != null)
				this.markDataDirty();
			if (includeInherited && (this.inheritedSettings != null))
				this.inheritedSettings.removeSetting(key, includeInherited);
		}
		DocStyleSettings getSubset(String prefix) {
			if (this.rootSettings == null) {
				String ssId = (this.id + "." + prefix);
				String ssName = (this.name + "." + prefix);
				Settings ssData = this.data.getSubset(prefix);
				DocStyleSettings ssInherited = ((this.inheritedSettings == null) ? null : this.inheritedSettings.getSubset(prefix));
				return new DocStyleSettings(this.host, this, prefix, ssId, ssName, ssData, this.lastStoredTime, ssInherited);
			}
			else return this.rootSettings.getSubset(this.prefix + "." + prefix);
		}
		String getRootSettingsId() {
			return ((this.rootSettings == null) ? this.id : this.rootSettings.id);
		}
		String getRootSettingsName() {
			return ((this.rootSettings == null) ? this.name : this.rootSettings.name);
		}
		
		boolean setInheritedSettings(DocStyleSettings inherited, boolean isResolving) throws IllegalArgumentException {
			if (this.rootSettings != null)
				return false;
			if (this.inheritedSettings == inherited)
				return false;
			if (this == inherited)
				throw new IllegalArgumentException("Document styles cannot inherit from themselves.");
			if (inherited != null)
				for (DocStyleSettings dss = inherited; dss != null; dss = dss.inheritedSettings) {
					if (dss == this)
						throw new IllegalArgumentException("Document styles cannot transitively inherit from themselves.");
				}
			if (this.inheritedSettings != null)
				this.inheritedSettings.inheritingSettings.remove(this);
			this.inheritedSettings = inherited;
			if (this.inheritedSettings != null)
				this.inheritedSettings.inheritingSettings.add(this);
			if (isResolving)
				return true;
			if (this.inheritedSettings == null)
				this.data.removeSetting("@." + INHERIT_VALUES_FROM_ATTRIBUTE);
			else this.data.setSetting(("@." + INHERIT_VALUES_FROM_ATTRIBUTE), this.inheritedSettings.id);
			this.markDataDirty();
			return true;
		}
		DocStyleSettings[] getInheritanceChain() {
			//	we cannot cache this, as it would fail to reflect upstream changes
			ArrayList inheritanceChain = new ArrayList(4);
			for (DocStyleSettings dss = this; dss != null; dss = dss.inheritedSettings)
				inheritanceChain.add(dss);
			return ((DocStyleSettings[]) inheritanceChain.toArray(new DocStyleSettings[inheritanceChain.size()]));
		}
		boolean isInheritanceChainDirty() {
			if (this.isDirty())
				return true;
			if (this.inheritedSettings == null)
				return false;
			return this.inheritedSettings.isInheritanceChainDirty();
		}
		DocStyleSettings getSourceOfValue(String key) {
			if (this.data.containsKey(key))
				return this;
			else if (this.inheritedSettings != null)
				return this.inheritedSettings.getSourceOfValue(key);
			else return null;
		}
		
		LiveData asDocStyleData() {
			if (this.host == null)
				throw new IllegalStateException("Cannot use static constant document style as data");
			return new DocStyleData(this);
		}
		ImDocumentStyle asDocStyle() {
			if (this.host == null)
				throw new IllegalStateException("Cannot use static constant document style");
			return new ImDocumentStyle(this.asDocStyleData());
		}
		DocumentStyle.Data getResolvedDocStyleData() {
			final Settings dsData = new Settings();
			DocumentStyle.Data resDocStyleData = new AbstractData() {
				public String getPropertyData(String key) {
					return dsData.getSetting(key);
				}
				public String[] getPropertyNames() {
					return dsData.getKeys();
				}
			};
			String[] dsKeys = this.getKeys();
			for (int k = 0; k < dsKeys.length; k++) {
				if (dsKeys[k].startsWith("@.")) {
					String dsAttributeName = dsKeys[k].substring("@.".length());
					if (INHERIT_VALUES_FROM_ATTRIBUTE.equals(dsAttributeName)) {
						DocStyleSettings[] dsInheritanceChain = this.getInheritanceChain();
						if (dsInheritanceChain.length < 2)
							continue; // no inheritance to indicate
						StringBuffer dsInheritanceChainAttribute = new StringBuffer();
						for (int i = 1 /* omit published style proper */; i < dsInheritanceChain.length; i++) {
							if (dsInheritanceChainAttribute.length() != 0)
								dsInheritanceChainAttribute.append(" ");
							dsInheritanceChainAttribute.append(dsInheritanceChain[i].id + ":" + dsInheritanceChain[i].name);
						}
						resDocStyleData.setAttribute(dsAttributeName, dsInheritanceChainAttribute.toString());
					}
					else {
						String dsAttributeValue = this.data.getSetting(dsKeys[k]); // no inheritance of attributes
						if (dsAttributeValue != null)
							resDocStyleData.setAttribute(dsAttributeName, dsAttributeValue);
					}
				}
				else dsData.setSetting(dsKeys[k], this.getSetting(dsKeys[k]));
			}
			return resDocStyleData;
		}
		boolean hasAnchors(boolean countInherited) {
			if (this.data.hasSubset(Anchor.ANCHOR_PREFIX))
				return true;
			if (countInherited && (this.inheritedSettings != null))
				return this.inheritedSettings.hasAnchors(countInherited);
			return false;
		}
		int getMaxInheritingCount() {
			int maxInheritingCount = 0;
			for (DocStyleSettings dss = this; dss != null; dss = dss.inheritedSettings)
				maxInheritingCount = Math.max(maxInheritingCount, dss.inheritingSettings.size());
			return maxInheritingCount;
		}
		
		public String toString() {
			return this.name; // good for use in drop-downs
		}
		public boolean equals(Object obj) {
			return ((obj instanceof DocStyleSettings) && this.id.equals(((DocStyleSettings) obj).id));
		}
		public int compareTo(Object obj) {
			return ((obj instanceof DocStyleSettings) ? this.name.compareToIgnoreCase(((DocStyleSettings) obj).name) : -1);
		}
	}
	
	private static class DocStyleData implements Data, LiveData {
		private DocStyleSettings data;
		private Settings attributes;
		private Provider provider;
		DocStyleData(DocStyleSettings data) {
			this.data = data;
			this.attributes = this.data.data.getSubset("@");
		}
		public void clearAttributes() { /* sure as hell we don't clear our basic information */ }
		public void copyAttributes(Attributed source) {
			String[] ans = source.getAttributeNames();
			for (int n = 0; n < ans.length; n++) {
				Object av = source.getAttribute(ans[n]);
				if (av != null)
					this.attributes.setSetting(ans[n], av.toString());
			}
		}
		public Object getAttribute(String name, Object def) {
			String av = this.attributes.getSetting(name);
			return ((av == null) ? def : av);
		}
		public Object getAttribute(String name) {
			return this.attributes.getSetting(name);
		}
		public String[] getAttributeNames() {
			return this.attributes.getKeys();
		}
		public boolean hasAttribute(String name) {
			return this.attributes.containsKey(name);
		}
		public Object removeAttribute(String name) {
			return this.attributes.removeSetting(name);
		}
		public void setAttribute(String name) {
			this.setAttribute(name, "true");
		}
		public Object setAttribute(String name, Object value) {
			if (value == null)
				return this.removeAttribute(name);
			else return this.attributes.setSetting(name, value.toString());
		}
		
		public void propertyDataRetrieved(String key, String value, boolean fromThis) {
			if (this.data.host != null)
				this.data.host.propertyDataRetrieved(key, value, fromThis);
		}
		
		public Provider getProvider() {
			return this.provider;
		}
		public void setProvider(Provider provider) {
			this.provider = provider;
		}
		
		public String getPropertyData(String key) {
			return this.data.getSetting(key);
		}
		public String[] getPropertyNames() {
			return this.data.getKeys();
		}
	}
	
	void propertyDataRetrieved(String key, String value, boolean fromLiveData) {
		if ((this.docStyleEditor != null) && this.docStyleEditor.isVisible())
			this.docStyleEditor.propertyDataRetrieved(key, value, fromLiveData);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.DisplayExtensionProvider#getDisplayExtensions()
	 */
	public DisplayExtension[] getDisplayExtensions() {
		return ((this.docStyleEditor == null) ? NO_DISPLAY_EXTENSIONS : this.docStyleEditorDisplayExtension);
	}
	
	private ParameterGroupDescription getParameterGroupDescription(String pnp) {
		if (pnp.equals(Anchor.ANCHOR_PREFIX))
			return anchorRootDescription;
		if (pnp.startsWith(Anchor.ANCHOR_PREFIX + "."))
			return PageFeatureAnchor.PARAMETER_GROUP_DESCRIPTION;
		ParameterGroupDescription pgd = DocumentStyle.getParameterGroupDescription(pnp);
		if (pgd == null)
			return this.loadParameterGroupDescription(pnp);
		else {
			if (this.ggImagine.getConfiguration().isMasterConfiguration()) // TODO call on parent GG, not GGI
				this.storeParameterGroupDescription(pgd); // store only in master configuration, local version might be less comprehensive
			return pgd;
		}
	}
	
	private static final Grammar xmlGrammar = new StandardGrammar();
	private static final Parser xmlParser = new Parser(xmlGrammar);
	private HashMap paramGroupDescriptionsByName = new HashMap();
	private HashSet paramGroupDescriptionsStored = new HashSet();
	private HashMap paramGroupDescriptionHashes = new HashMap();
	
	private ParameterGroupDescription loadParameterGroupDescription(String pnp) {
		
		//	check cache first
		if (this.paramGroupDescriptionsByName.containsKey(pnp))
			return ((ParameterGroupDescription) this.paramGroupDescriptionsByName.get(pnp));
		
		//	resort to previously persisted parameter group description
		if (this.dataProvider.isDataAvailable(pnp + ".pgd.xml")) try {
			final StringBuffer pgdSb = new StringBuffer();
			BufferedReader pgdBr = new BufferedReader(new InputStreamReader(this.dataProvider.getInputStream(pnp + ".pgd.xml"), "UTF-8") {
				public int read() throws IOException {
					int r = super.read();
					if (r != -1)
						pgdSb.append((char) r);
					return r;
				}
				public int read(char[] cbuf, int offset, int length) throws IOException {
					int r = super.read(cbuf, offset, length);
					if (r != -1)
						pgdSb.append(cbuf, offset, r);
					return r;
				}
			});
			final ParameterGroupDescription pgd = new ParameterGroupDescription(pnp);
			xmlParser.parse(pgdBr, new TokenReceiver() {
				private ParameterDescription pd = null;
				private ArrayList pvs = null;
				public void storeToken(String token, int treeDepth) throws IOException {
					if (!xmlGrammar.isTag(token))
						return;
					String type = xmlGrammar.getType(token);
					if ("paramGroup".equals(type)) {
						if (!xmlGrammar.isEndTag(token)) {
							TreeNodeAttributeSet tnas = TreeNodeAttributeSet.getTagAttributes(token, xmlGrammar);
							pgd.setLabel(tnas.getAttribute("label", ""));
							pgd.setDescription(tnas.getAttribute("description", ""));
						}
					}
					else if ("param".equals(type)) {
						if (xmlGrammar.isEndTag(token)) {
							this.pd.setValues((String[]) this.pvs.toArray(new String[this.pvs.size()]));
							this.pvs = null;
							this.pd = null;
						}
						else {
							TreeNodeAttributeSet tnas = TreeNodeAttributeSet.getTagAttributes(token, xmlGrammar);
							String pn = tnas.getAttribute("name");
							pgd.setParamLabel(pn, tnas.getAttribute("label", ""));
							pgd.setParamDescription(pn, tnas.getAttribute("description", ""));
							pgd.setParamDefaultValue(pn, tnas.getAttribute("default"));
							if ("true".equals(tnas.getAttribute("required", "false")))
								pgd.setParamRequired(pn);
							this.readDependencyAttribute(pgd.getParameterDescription(pn), null, tnas.getAttribute("requires"), true);
							this.readDependencyAttribute(pgd.getParameterDescription(pn), null, tnas.getAttribute("excludes"), false);
							if (!xmlGrammar.isSingularTag(token)) {
								this.pd = pgd.getParameterDescription(pn);
								this.pvs = new ArrayList(2);
							}
						}
					}
					else if ("value".equals(type)) {
						TreeNodeAttributeSet tnas = TreeNodeAttributeSet.getTagAttributes(token, xmlGrammar);
						String pvn = tnas.getAttribute("name");
						this.pvs.add(pvn);
						String pvl = tnas.getAttribute("label");
						this.readDependencyAttribute(this.pd, pvn, tnas.getAttribute("requires"), true);
						this.readDependencyAttribute(this.pd, pvn, tnas.getAttribute("excludes"), false);
						if (pvl != null)
							this.pd.setValueLabel(pvn, pvl);
					}
				}
				public void close() throws IOException {}
				private void readDependencyAttribute(ParameterDescription pd, String value, String names, boolean isRequired) {
					if (names == null)
						return;
					String[] ns = names.split("\\,");
					for (int n = 0; n < ns.length; n++) {
						if (isRequired)
							pd.addRequiredParameter(value, ns[n]);
						else pd.addExcludedParameter(value, ns[n]);
					}
				}
			});
			pgdBr.close();
			
			//	cache parameter group description for reuse
			this.paramGroupDescriptionsByName.put(pnp, pgd);
			
			//	remember hash of what we read
			this.paramGroupDescriptionHashes.put(pnp, Integer.valueOf(computeHashCode(pgdSb)));
			
			//	finally ...
			return pgd;
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.out);
		}
		
		//	little we can do about this one ...
		return null;
	}
	
	private void storeParameterGroupDescription(ParameterGroupDescription pgd) {
		
		//	check if we saved this one before in current life cycle
		if (this.paramGroupDescriptionsStored.contains(pgd.parameterNamePrefix))
			return;
		
		//	little we can do about this one ...
		if (!this.dataProvider.isDataEditable(pgd.parameterNamePrefix + ".pgd.xml"))
			return;
		
		//	get parameter names
		String[] pgdPns = pgd.getParameterNames();
		
		//	write XML to buffer
		StringBuffer pgdSb = new StringBuffer();
		pgdSb.append("<paramGroup");
		pgdSb.append(" name=\"" + pgd.parameterNamePrefix + "\"");
		if (pgd.getLabel() != null)
			pgdSb.append(" label=\"" + xmlGrammar.escape(pgd.getLabel()) + "\"");
		if (pgd.getDescription() != null)
			pgdSb.append(" description=\"" + xmlGrammar.escape(pgd.getDescription()) + "\"");
		if (pgdPns.length == 0)
			pgdSb.append("/>\r\n");
		else {
			pgdSb.append(">\r\n");
			for (int p = 0; p < pgdPns.length; p++) {
				ParameterDescription pd = pgd.getParameterDescription(pgdPns[p]);
				if (pd == null)
					continue;
				pgdSb.append("  <param");
				pgdSb.append(" name=\"" + pgdPns[p] + "\"");
				if (pd.getLabel() != null)
					pgdSb.append(" label=\"" + xmlGrammar.escape(pd.getLabel()) + "\"");
				if (pd.getDescription() != null)
					pgdSb.append(" description=\"" + xmlGrammar.escape(pd.getDescription()) + "\"");
				if (pd.getDefaultValue() != null)
					pgdSb.append(" default=\"" + xmlGrammar.escape(pd.getDefaultValue()) + "\"");
				if (pd.isRequired())
					pgdSb.append(" required=\"true\"");
				appendParameterNameListAttribute(pgdSb, "requires", pd.getRequiredParameters());
				appendParameterNameListAttribute(pgdSb, "excludes", pd.getExcludedParameters());
				String[] pvs = pd.getValues();
				if ((pvs == null) || (pvs.length == 0))
					pgdSb.append("/>\r\n");
				else {
					pgdSb.append(">\r\n");
					for (int v = 0; v < pvs.length; v++) {
						pgdSb.append("    <value");
						pgdSb.append(" name=\"" + xmlGrammar.escape(pvs[v]) + "\"");
						if (pd.getValueLabel(pvs[v]) != null)
							pgdSb.append(" label=\"" + xmlGrammar.escape(pd.getValueLabel(pvs[v])) + "\"");
						appendParameterNameListAttribute(pgdSb, "requires", pd.getRequiredParameters(pvs[v]));
						appendParameterNameListAttribute(pgdSb, "excludes", pd.getExcludedParameters(pvs[v]));
						pgdSb.append("/>\r\n");
					}
					pgdSb.append("  </param>\r\n");
				}
			}
			pgdSb.append("</paramGroup>\r\n");
		}
		
		//	check for changes via hash
		Integer pgdHash = Integer.valueOf(computeHashCode(pgdSb));
		if (this.paramGroupDescriptionHashes.containsKey(pgd.parameterNamePrefix) && this.paramGroupDescriptionHashes.get(pgd.parameterNamePrefix).equals(pgdHash))
			return;
		
		//	persist any changes
		try {
			
			//	write data
			BufferedWriter pgdBw = new BufferedWriter(new OutputStreamWriter(this.dataProvider.getOutputStream(pgd.parameterNamePrefix + ".pgd.xml"), "UTF-8"));
			pgdBw.write(pgdSb.toString());
			pgdBw.flush();
			pgdBw.close();
			
			//	remember data written ...
			this.paramGroupDescriptionsStored.add(pgd.parameterNamePrefix);
			
			//	... as well as current status hash
			this.paramGroupDescriptionHashes.put(pgd.parameterNamePrefix, pgdHash);
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.out);
		}
	}
	
	private static void appendParameterNameListAttribute(StringBuffer pgdSb, String name, String[] pns) {
		if (pns == null)
			return;
		pgdSb.append(" " + name + "=\"");
		for (int n = 0; n < pns.length; n++) {
			if (n != 0)
				pgdSb.append(",");
			pgdSb.append(xmlGrammar.escape(pns[n]));
		}
		pgdSb.append("\"");
	}
	
	//	courtesy of java.lang.String
	private static int computeHashCode(CharSequence chars) {
		if (chars.length() == 0)
			return 0;
		int h = 0;
		for (int c = 0; c < chars.length(); c++)
			h = 31*h + chars.charAt(c);
		return h;
	}
	
	private boolean checkParamValueClass(String docStyleParamName, Class cls, boolean includeArray) {
		Class paramValueClass = ((Class) this.parameterValueClasses.get(docStyleParamName));
		if (paramValueClass != null) {
			if (paramValueClass.getName().equals(cls.getName()))
				return true;
			else if (includeArray && DocumentStyle.getListElementClass(paramValueClass).getName().equals(cls.getName()))
				return true;
		}
		if (docStyleParamName.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
			String aDocStyleParamName = docStyleParamName.substring(docStyleParamName.lastIndexOf(".") + ".".length());
			Class aParamValueClass = Anchor.getParameterValueClass(aDocStyleParamName);
			if (aParamValueClass != null) {
				if (aParamValueClass.getName().equals(cls.getName()))
					return true;
				else if (includeArray && DocumentStyle.getListElementClass(aParamValueClass).getName().equals(cls.getName()))
					return true;
			}
//			if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_MINIMUM_FONT_SIZE_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_MAXIMUM_FONT_SIZE_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_FONT_SIZE_PROPERTY))
//				return Integer.class.getName().equals(cls.getName());
//			else if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_IS_BOLD_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_IS_ITALICS_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_IS_ALL_CAPS_PROPERTY))
//				return Boolean.class.getName().equals(cls.getName());
//			else if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_PATTERN_PROPERTY))
//				return (String.class.getName().equals(cls.getName()) || Pattern.class.getName().equals(cls.getName()));
//			else if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_AREA_PROPERTY))
//				return BoundingBox.class.getName().equals(cls.getName());
		}
		return false;
	}
	
	private boolean hasFixedValueList(String docStyleParamName) {
		String pgn = docStyleParamName.substring(0, docStyleParamName.lastIndexOf('.'));
		ParameterGroupDescription pgd = this.getParameterGroupDescription(pgn);
		if (pgd == null)
			return false;
		String pn = docStyleParamName.substring(docStyleParamName.lastIndexOf('.') + ".".length());
		return (pgd.getParamValues(pn) != null);
	}
	
	private Class getParamValueClass(String docStyleParamName) {
		Class paramValueClass = ((Class) this.parameterValueClasses.get(docStyleParamName));
		if (paramValueClass != null)
			return paramValueClass;
		if (docStyleParamName.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
			String aDocStyleParamName = docStyleParamName.substring(docStyleParamName.lastIndexOf(".") + ".".length());
			Class aParamValueClass = Anchor.getParameterValueClass(aDocStyleParamName);
			if (aParamValueClass != null)
				return aParamValueClass;
//			if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_MINIMUM_FONT_SIZE_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_MAXIMUM_FONT_SIZE_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_FONT_SIZE_PROPERTY))
//				return Integer.class;
//			else if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_IS_BOLD_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_IS_ITALICS_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_IS_ALL_CAPS_PROPERTY))
//				return Boolean.class;
//			else if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_PATTERN_PROPERTY))
//				return String.class;
//			else if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_AREA_PROPERTY))
//				return BoundingBox.class;
		}
		return String.class;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener#documentOpened(de.uka.ipd.idaho.im.ImDocument, java.lang.Object, de.uka.ipd.idaho.gamta.util.ProgressMonitor)
	 */
	public void documentOpened(ImDocument doc, Object source, ProgressMonitor pm) { /* we only react to documents being closed */ }
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener#documentSelected(de.uka.ipd.idaho.im.ImDocument)
	 */
	public void documentSelected(ImDocument doc) {
		// TODO do change document style in editor if document selected that matches other style
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener#documentSaving(de.uka.ipd.idaho.im.ImDocument, java.lang.Object, de.uka.ipd.idaho.gamta.util.ProgressMonitor)
	 */
	public void documentSaving(ImDocument doc, Object dest, ProgressMonitor pm) throws CancelSavingException {
		String docStyleId = ((String) doc.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE));
		if (docStyleId == null)
			return; // nothing to worry about
		DocumentStyle publishedDocStyle = this.docStyleProvider.getDocStyleById(docStyleId);
		if (publishedDocStyle != null)
			return; // this one is published
		DocStyleSettings dss = this.getDocStyleById(docStyleId);
		if (dss == null)
			return; // wherever this ID may have come from
		int choice = DialogFactory.confirm(("The document has the unpublished document style '" + doc.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE) + "' assigned to it. It will be published locally after saving the document."), "Un-Published Document Style Assigned", JOptionPane.OK_CANCEL_OPTION);
		if (choice != JOptionPane.OK_OPTION)
			throw new CancelSavingException("Cannot save document with reference to unpublished document style.");
		if (!this.publishDocStyleLoc(dss))
			throw new CancelSavingException("Failed to publish document style, and annot save document with unpublished one.");
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener#documentSaved(de.uka.ipd.idaho.im.ImDocument, java.lang.Object, de.uka.ipd.idaho.gamta.util.ProgressMonitor)
	 */
	public void documentSaved(ImDocument doc, Object dest, ProgressMonitor pm) { /* we only react to documents being closed */ }
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener#documentClosed(java.lang.String)
	 */
	public void documentClosed(String docId) {
		if (this.docStyleEditor != null)
			this.docStyleEditor.notifyDocumentClosed(docId);
	}
	
//	private Map docStylesByDocId = Collections.synchronizedMap(new HashMap());
//	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.AbstractSelectionActionProvider#getActions(de.uka.ipd.idaho.im.ImWord, de.uka.ipd.idaho.im.ImWord, de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel)
	 */
	public SelectionAction[] getActions(final ImWord start, final ImWord end, final ImDocumentMarkupPanel idmp) {
		
		//	cross page selection, unlikely a style edit
		if (start.pageId != end.pageId)
			return null;
		
		//	get document style name and style
		String docStyleName = ((String) idmp.document.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE));
		
		//	no document style assigned
		if (docStyleName == null) {
			
			//	no document style opened for editing, offer adding or creating one
//			if ((this.docStyleEditor == null) || (this.docStyleEditor.docStyleName == null))
			if ((this.docStyleEditor == null) || (this.docStyleEditor.headDocStyle == null))
				return this.getAssignDocStyleAction(idmp);
			
			//	use editing document style
//			else docStyleName = this.docStyleEditor.docStyleName;
			else docStyleName = this.docStyleEditor.headDocStyle.name;
		}
		
		//	no document style assigned we'd have the sources for
		else if (!this.docStylesByName.containsKey(docStyleName))
			return this.getHandleUnavailableDocStyleActions(idmp, docStyleName);
		
		//	get document style ID and double-check name
		String docStyleId = ((String) idmp.document.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE));
		if (docStyleId != null) {
			DocStyleSettings docStyle = this.getDocStyleById(docStyleId);
			if (docStyle != null) {
//				//	CANNOT DO THIS OUTSIDE ATOMIC ACTION !!!
//				if ((docStyleName != null) && !docStyle.name.equals(docStyleName))
//					idmp.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, docStyle.name);
				docStyleName = docStyle.name;
			}
		}
		
		//	collect actions
		ArrayList actions = new ArrayList();
		
		//	offer changing and removing document style
		actions.add(this.getChangeDocStyleAction(idmp, docStyleId, docStyleName));
		actions.add(this.getRemoveDocStyleAction(idmp, docStyleName));
		
		//	style already assigned, offer extending or modifying it
		final DocStyleSettings docStyle;
		if (this.docStyleEditor == null)
			docStyle = this.getDocStyleByName(docStyleName, true);
//		else if (docStyleName.equals(this.docStyleEditor.docStyleName))
		else if (docStyleName.equals(this.docStyleEditor.headDocStyle.name))
			docStyle = this.docStyleEditor.headDocStyle;
		else {
			docStyle = this.getDocStyleByName(docStyleName, true);
//			this.docStyleEditor.setDocStyle(idmp.document, docStyleName, docStyle); // TODOne maybe do this only when action actually used
		}
		
		//	assess font style and size, and collect word string
		boolean isBold = true;
		boolean isItalics = true;
		boolean isAllCaps = true;
		boolean hasCaps = false;
		int minFontSize = 72;
		int maxFontSize = 0;
		final String fWordString;
		int left = Integer.MAX_VALUE;
		int right = 0;
		int top = Integer.MAX_VALUE;
		int bottom = 0;
		
		//	single text stream, assess word sequence
		if (start.getTextStreamId().equals(end.getTextStreamId())) {
			
			//	make sure start does not lie after end (would run for loop to end of text stream)
			if (ImUtils.textStreamOrder.compare(start, end) > 0)
				return this.getActions(end, start, idmp);
			
			//	assess single word
			if (start == end) {
				isBold = (isBold && start.hasAttribute(ImWord.BOLD_ATTRIBUTE));
				isItalics = (isItalics && start.hasAttribute(ImWord.ITALICS_ATTRIBUTE));
				isAllCaps = (isAllCaps && start.getString().equals(start.getString().toUpperCase()));
				hasCaps = (hasCaps || !start.getString().equals(start.getString().toLowerCase()));
				try {
					int fs = start.getFontSize();
					minFontSize = Math.min(minFontSize, fs);
					maxFontSize = Math.max(maxFontSize, fs);
				} catch (RuntimeException re) {}
				fWordString = start.getString();
				left = Math.min(left, start.bounds.left);
				right = Math.max(right, start.bounds.right);
				top = Math.min(top, start.bounds.top);
				bottom = Math.max(bottom, start.bounds.bottom);
			}
			
			//	assess word sequence
			else {
				for (ImWord imw = start; imw != null; imw = imw.getNextWord()) {
					isBold = (isBold && imw.hasAttribute(ImWord.BOLD_ATTRIBUTE));
					isItalics = (isItalics && imw.hasAttribute(ImWord.ITALICS_ATTRIBUTE));
					isAllCaps = (isAllCaps && imw.getString().equals(imw.getString().toUpperCase()));
					hasCaps = (hasCaps || !imw.getString().equals(imw.getString().toLowerCase()));
					try {
						int fs = imw.getFontSize();
						minFontSize = Math.min(minFontSize, fs);
						maxFontSize = Math.max(maxFontSize, fs);
					} catch (RuntimeException re) {}
					left = Math.min(left, imw.bounds.left);
					right = Math.max(right, imw.bounds.right);
					top = Math.min(top, imw.bounds.top);
					bottom = Math.max(bottom, imw.bounds.bottom);
					if (imw == end)
						break;
				}
				
				//	get word string (allowing more words on first couple of pages for anchors and metadata extraction)
				fWordString = (((end.getTextStreamPos() - start.getTextStreamPos()) < (((start.pageId - idmp.document.getFirstPageId()) < 4) ? 50 : 15)) ? ImUtils.getString(start, end, true) : null);
			}
		}
		
		//	different text streams, only use argument words proper
		else {
			isBold = (isBold && start.hasAttribute(ImWord.BOLD_ATTRIBUTE));
			isItalics = (isItalics && start.hasAttribute(ImWord.ITALICS_ATTRIBUTE));
			isAllCaps = (isAllCaps && start.getString().equals(start.getString().toUpperCase()));
			hasCaps = (hasCaps || !start.getString().equals(start.getString().toLowerCase()));
			try {
				int fs = start.getFontSize();
				minFontSize = Math.min(minFontSize, fs);
				maxFontSize = Math.max(maxFontSize, fs);
			} catch (RuntimeException re) {}
			left = Math.min(left, start.bounds.left);
			right = Math.max(right, start.bounds.right);
			top = Math.min(top, start.bounds.top);
			bottom = Math.max(bottom, start.bounds.bottom);
			
			isBold = (isBold && end.hasAttribute(ImWord.BOLD_ATTRIBUTE));
			isItalics = (isItalics && end.hasAttribute(ImWord.ITALICS_ATTRIBUTE));
			isAllCaps = (isAllCaps && end.getString().equals(end.getString().toUpperCase()));
			hasCaps = (hasCaps || !end.getString().equals(end.getString().toLowerCase()));
			try {
				int fs = end.getFontSize();
				minFontSize = Math.min(minFontSize, fs);
				maxFontSize = Math.max(maxFontSize, fs);
			} catch (RuntimeException re) {}
			left = Math.min(left, end.bounds.left);
			right = Math.max(right, end.bounds.right);
			top = Math.min(top, end.bounds.top);
			bottom = Math.max(bottom, end.bounds.bottom);
			
			fWordString = (start.getString() + " " + end.getString());
		}
		
		//	measure margins
		int horiMargin = (end.bounds.left - start.bounds.right);
		int vertMargin = (end.bounds.top - start.bounds.bottom);
		
		//	fix parameter values, scaling bounds and margins to default 72 DPI
		final boolean fIsBold = isBold;
		final boolean fIsItalics = isItalics;
		final boolean fIsAllCaps = (isAllCaps && hasCaps);
		final int fMinFontSize = minFontSize;
		final int fMaxFontSize = maxFontSize;
		int pageDpi = idmp.document.getPage(start.pageId).getImageDPI();
		/* cut word based bounding boxes a little slack, adding some pixels in
		 * each direction, maybe (DPI / 12), a.k.a. some 2 millimeters, to help
		 * with slight word placement variations */
		final BoundingBox fWordBounds = ImDocumentStyle.scaleBox(new BoundingBox((left - (pageDpi / 12)), (right + (pageDpi / 12)), (top - (pageDpi / 12)), (bottom + (pageDpi / 12))), pageDpi, 72, 'O');
		final int fHoriMargin = ((horiMargin < 0) ? 0 : ImDocumentStyle.scaleInt(horiMargin, pageDpi, 72, 'F'));
		final int cHoriMargin = ((horiMargin < 0) ? 0 : ImDocumentStyle.scaleInt(horiMargin, pageDpi, 72, 'C'));
		final int fVertMargin = ((vertMargin < 0) ? 0 : ImDocumentStyle.scaleInt(vertMargin, pageDpi, 72, 'F'));
		final int cVertMargin = ((vertMargin < 0) ? 0 : ImDocumentStyle.scaleInt(vertMargin, pageDpi, 72, 'C'));
		
		//	get available parameter names, including ones from style proper (anchors !!!)
		TreeSet docStyleParamNameSet = new TreeSet(Arrays.asList(this.parameterValueClassNames.getKeys()));
		String[] dsDocStyleParamNames = docStyle.getKeys();
		for (int p = 0; p < dsDocStyleParamNames.length; p++) {
			if (dsDocStyleParamNames[p].startsWith(Anchor.ANCHOR_PREFIX + "."))
				docStyleParamNameSet.add(dsDocStyleParamNames[p]);
		}
		final String[] docStyleParamNames = ((String[]) docStyleParamNameSet.toArray(new String[docStyleParamNameSet.size()]));
		
		//	collect style parameter group names that use font properties
		final TreeSet fpDocStyleParamGroupNames = new TreeSet();
		for (int p = 0; p < docStyleParamNames.length; p++) {
			if ((fMinFontSize <= fMaxFontSize) && docStyleParamNames[p].endsWith(".fontSize") && checkParamValueClass(docStyleParamNames[p], Integer.class, false))
				fpDocStyleParamGroupNames.add(docStyleParamNames[p].substring(0, docStyleParamNames[p].lastIndexOf('.')));
			else if ((fMinFontSize < 72) && docStyleParamNames[p].endsWith(".minFontSize") && checkParamValueClass(docStyleParamNames[p], Integer.class, false))
				fpDocStyleParamGroupNames.add(docStyleParamNames[p].substring(0, docStyleParamNames[p].lastIndexOf('.')));
			else if ((0 < fMaxFontSize) && docStyleParamNames[p].endsWith(".maxFontSize") && checkParamValueClass(docStyleParamNames[p], Integer.class, false))
				fpDocStyleParamGroupNames.add(docStyleParamNames[p].substring(0, docStyleParamNames[p].lastIndexOf('.')));
			else if (fIsBold && (docStyleParamNames[p].endsWith(".isBold") || docStyleParamNames[p].endsWith(".startIsBold")) && checkParamValueClass(docStyleParamNames[p], Boolean.class, false))
				fpDocStyleParamGroupNames.add(docStyleParamNames[p].substring(0, docStyleParamNames[p].lastIndexOf('.')));
			else if (fIsItalics && (docStyleParamNames[p].endsWith(".isItalics") || docStyleParamNames[p].endsWith(".startIsItalics")) && checkParamValueClass(docStyleParamNames[p], Boolean.class, false))
				fpDocStyleParamGroupNames.add(docStyleParamNames[p].substring(0, docStyleParamNames[p].lastIndexOf('.')));
			else if (fIsAllCaps && (docStyleParamNames[p].endsWith(".isAllCaps") || docStyleParamNames[p].endsWith(".startIsAllCaps")) && checkParamValueClass(docStyleParamNames[p], Boolean.class, false))
				fpDocStyleParamGroupNames.add(docStyleParamNames[p].substring(0, docStyleParamNames[p].lastIndexOf('.')));
		}
		
		//	get currently selected parameter group from editor (if we have one open)
		final String editParamGroupName = ((this.docStyleEditor == null) ? "<NONE>" : this.docStyleEditor.paramGroupName);
		final String editTopParamGroupName;
		if (editParamGroupName == null)
			editTopParamGroupName = "<NONE>";
		else if (editParamGroupName.indexOf('.') == -1)
			editTopParamGroupName = editParamGroupName;
		else editTopParamGroupName = editParamGroupName.substring(0, editParamGroupName.indexOf('.'));
		
		//	add actions using font style and size
		if (((fMinFontSize <= fMaxFontSize) || fIsBold || fIsItalics || fIsAllCaps) && (fpDocStyleParamGroupNames.size() != 0))
			actions.add(new SelectionAction("styleUseFont", "Use Font Properties", "Use font properties of selected words in document style") {
				public JMenuItem getMenuItem(ImDocumentMarkupPanel invoker) {
					if (fpDocStyleParamGroupNames.size() == 1)
						return super.getMenuItem(invoker);
					
					//	populate sub menu
					JMenu m = new JMenu(this.label + " ...");
					m.setToolTipText(this.tooltip);
					JMenuItem mi;
					String smTpgn = null;
					JMenu sm = null;
					for (Iterator pnit = fpDocStyleParamGroupNames.iterator(); pnit.hasNext();) {
						final String pgn = ((String) pnit.next());
						mi = createParameterGroupMenuItem(pgn);
						mi.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								if (docStyleEditor != null)
									docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
								useFontProperties(idmp.document, start.pageId, docStyle, pgn, docStyleParamNames, fMinFontSize, fMaxFontSize, fIsBold, fIsItalics, fIsAllCaps);
							}
						});
						String tpgn = ((pgn.indexOf('.') == -1) ? pgn : pgn.substring(0, pgn.indexOf('.')));
						if (tpgn.equals(editTopParamGroupName))
							m.add(mi);
						else {
							if (!tpgn.equals(smTpgn)) {
								smTpgn = tpgn;
								sm = new JMenu(getParameterGroupLabel(smTpgn, false));
								m.add(sm);
							}
							sm.add(mi);
						}
					}
					
					//	finally ...
					return m;
				}
				public boolean performAction(ImDocumentMarkupPanel invoker) {
					if (docStyleEditor != null)
						docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
					useFontProperties(idmp.document, start.pageId, docStyle, ((String) fpDocStyleParamGroupNames.first()), docStyleParamNames, fMinFontSize, fMaxFontSize, fIsBold, fIsItalics, fIsAllCaps);
					return false;
				}
			});
		
		//	collect style parameter group names that use string properties
		final TreeSet sDocStyleParamGroupNames = new TreeSet();
		for (int p = 0; p < docStyleParamNames.length; p++) {
			if (hasFixedValueList(docStyleParamNames[p]))
				continue;
			if (checkParamValueClass(docStyleParamNames[p], String.class, true) || checkParamValueClass(docStyleParamNames[p], Pattern.class, true))
				sDocStyleParamGroupNames.add(docStyleParamNames[p].substring(0, docStyleParamNames[p].lastIndexOf('.')));
		}
		
		//	add actions using word string (patterns, first and foremost, but also fixed values)
		if ((fWordString != null) && (sDocStyleParamGroupNames.size() != 0))
			actions.add(new SelectionAction("styleUseString", "Use String / Pattern", "Use string or pattern based on selected words in document style") {
				public JMenuItem getMenuItem(ImDocumentMarkupPanel invoker) {
					if (sDocStyleParamGroupNames.size() == 1)
						return super.getMenuItem(invoker);
					
					//	populate sub menu
					JMenu m = new JMenu(this.label + " ...");
					m.setToolTipText(this.tooltip);
					JMenuItem mi;
					String smTpgn = null;
					JMenu sm = null;
					for (Iterator pnit = sDocStyleParamGroupNames.iterator(); pnit.hasNext();) {
						final String pgn = ((String) pnit.next());
						mi = createParameterGroupMenuItem(pgn);
						mi.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								if (docStyleEditor != null)
									docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
								useString(idmp.document, start.pageId, docStyle, pgn, docStyleParamNames, fWordString);
							}
						});
						String tpgn = ((pgn.indexOf('.') == -1) ? pgn : pgn.substring(0, pgn.indexOf('.')));
						if (tpgn.equals(editTopParamGroupName))
							m.add(mi);
						else {
							if (!tpgn.equals(smTpgn)) {
								smTpgn = tpgn;
								sm = new JMenu(getParameterGroupLabel(smTpgn, false));
								m.add(sm);
							}
							sm.add(mi);
						}
					}
					
					//	finally ...
					return m;
				}
				public boolean performAction(ImDocumentMarkupPanel invoker) {
					if (docStyleEditor != null)
						docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
					useString(idmp.document, start.pageId, docStyle, ((String) sDocStyleParamGroupNames.first()), docStyleParamNames, fWordString);
					return false;
				}
			});
		
		//	collect style parameter names that use bounding box properties
		final TreeSet bbDocStyleParamNames = new TreeSet();
		for (int p = 0; p < docStyleParamNames.length; p++) {
			if (checkParamValueClass(docStyleParamNames[p], BoundingBox.class, true))
				bbDocStyleParamNames.add(docStyleParamNames[p]);
		}
		
		//	add actions using bounding box
		if (bbDocStyleParamNames.size() != 0)
			actions.add(new SelectionAction("styleUseBox", "Use Bounding Box", "Use bounding box (rectangular hull) of selected words in document style") {
				public JMenuItem getMenuItem(ImDocumentMarkupPanel invoker) {
					if (bbDocStyleParamNames.size() == 1)
						return super.getMenuItem(invoker);
					
					//	populate sub menu
					JMenu m = new JMenu(this.label + " ...");
					m.setToolTipText(this.tooltip);
					JMenuItem mi;
					String smTpgn = null;
					JMenu sm = null;
					for (Iterator pnit = bbDocStyleParamNames.iterator(); pnit.hasNext();) {
						final String pn = ((String) pnit.next());
						mi = createParameterMenuItem(pn);
						mi.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								if (docStyleEditor != null)
									docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
								useBoundingBox(idmp.document, start.pageId, docStyle, pn, fWordBounds);
							}
						});
						String pgn = ((pn.lastIndexOf('.') == -1) ? pn : pn.substring(0, pn.lastIndexOf('.')));
						String tpgn = ((pgn.indexOf('.') == -1) ? pgn : pgn.substring(0, pgn.indexOf('.')));
						if (tpgn.equals(editTopParamGroupName))
							m.add(mi);
						else {
							if (!tpgn.equals(smTpgn)) {
								smTpgn = tpgn;
								sm = new JMenu(getParameterGroupLabel(smTpgn, false));
								m.add(sm);
							}
							sm.add(mi);
						}
					}
					
					//	finally ...
					return m;
				}
				public boolean performAction(ImDocumentMarkupPanel invoker) {
					if (docStyleEditor != null)
						docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
					useBoundingBox(idmp.document, start.pageId, docStyle, ((String) bbDocStyleParamNames.first()), fWordBounds);
					return false;
				}
			});
		
		//	collect style parameter names that use integer properties (apart from font sizes)
		final TreeSet mDocStyleParamNames = new TreeSet();
		for (int p = 0; p < docStyleParamNames.length; p++) {
			if (true
					&& !docStyleParamNames[p].endsWith(".margin") && !docStyleParamNames[p].endsWith("Margin")
					&& !docStyleParamNames[p].endsWith(".width") && !docStyleParamNames[p].endsWith("Width")
					&& !docStyleParamNames[p].endsWith(".height") && !docStyleParamNames[p].endsWith("Height")
					&& !docStyleParamNames[p].endsWith(".distance") && !docStyleParamNames[p].endsWith("Distance")
					&& !docStyleParamNames[p].endsWith(".dist") && !docStyleParamNames[p].endsWith("Dist")
					&& !docStyleParamNames[p].endsWith(".gap") && !docStyleParamNames[p].endsWith("Gap")
				) continue;
			if (checkParamValueClass(docStyleParamNames[p], Integer.class, false))
				mDocStyleParamNames.add(docStyleParamNames[p]);
		}
		
		//	if two words on same line, offer using horizontal distance between first and last (e.g. for minimum column margin)
		if ((fHoriMargin != 0) && (mDocStyleParamNames.size() != 0))
			actions.add(new SelectionAction("styleUseMargin", "Use Horizontal Margin", "Use horizontal margin between first and last seleted words in document style") {
				public JMenuItem getMenuItem(ImDocumentMarkupPanel invoker) {
					if (mDocStyleParamNames.size() == 1)
						return super.getMenuItem(invoker);
					
					//	populate sub menu
					JMenu m = new JMenu(this.label + " ...");
					m.setToolTipText(this.tooltip);
					JMenuItem mi;
					String smTpgn = null;
					JMenu sm = null;
					for (Iterator pnit = mDocStyleParamNames.iterator(); pnit.hasNext();) {
						final String pn = ((String) pnit.next());
						mi = createParameterMenuItem(pn);
						mi.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								if (docStyleEditor != null)
									docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
								useMargin(idmp.document.docId, docStyle, pn, fHoriMargin, cHoriMargin);
							}
						});
						String pgn = ((pn.lastIndexOf('.') == -1) ? pn : pn.substring(0, pn.lastIndexOf('.')));
						String tpgn = ((pgn.indexOf('.') == -1) ? pgn : pgn.substring(0, pgn.indexOf('.')));
						if (tpgn.equals(editTopParamGroupName))
							m.add(mi);
						else {
							if (!tpgn.equals(smTpgn)) {
								smTpgn = tpgn;
								sm = new JMenu(getParameterGroupLabel(smTpgn, false));
								m.add(sm);
							}
							sm.add(mi);
						}
					}
					
					//	finally ...
					return m;
				}
				public boolean performAction(ImDocumentMarkupPanel invoker) {
					if (docStyleEditor != null)
						docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
					useMargin(idmp.document.docId, docStyle, ((String) mDocStyleParamNames.first()), fHoriMargin, cHoriMargin);
					return false;
				}
			});
		
		//	if two or more words not on same line, offer using vertical distance between first and last (e.g. for minimum block margin)
		if ((fVertMargin != 0) && (mDocStyleParamNames.size() != 0))
			actions.add(new SelectionAction("styleUseMargin", "Use Vertical Margin", "Use vertical margin between first and last seleted words in document style") {
				public JMenuItem getMenuItem(ImDocumentMarkupPanel invoker) {
					if (mDocStyleParamNames.size() == 1)
						return super.getMenuItem(invoker);
					
					//	populate sub menu
					JMenu m = new JMenu(this.label + " ...");
					m.setToolTipText(this.tooltip);
					JMenuItem mi;
					String smTpgn = null;
					JMenu sm = null;
					for (Iterator pnit = mDocStyleParamNames.iterator(); pnit.hasNext();) {
						final String pn = ((String) pnit.next());
						mi = createParameterMenuItem(pn);
						mi.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								if (docStyleEditor != null)
									docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
								useMargin(idmp.document.docId, docStyle, pn, fVertMargin, cVertMargin);
							}
						});
						String pgn = ((pn.lastIndexOf('.') == -1) ? pn : pn.substring(0, pn.lastIndexOf('.')));
						String tpgn = ((pgn.indexOf('.') == -1) ? pgn : pgn.substring(0, pgn.indexOf('.')));
						if (tpgn.equals(editTopParamGroupName))
							m.add(mi);
						else {
							if (!tpgn.equals(smTpgn)) {
								smTpgn = tpgn;
								sm = new JMenu(getParameterGroupLabel(smTpgn, false));
								m.add(sm);
							}
							sm.add(mi);
						}
					}
					
					//	finally ...
					return m;
				}
				public boolean performAction(ImDocumentMarkupPanel invoker) {
					if (docStyleEditor != null)
						docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
					useMargin(idmp.document.docId, docStyle, ((String) mDocStyleParamNames.first()), fVertMargin, cVertMargin);
					return false;
				}
			});
		
		//	collect style parameter group names that use bounding box properties
		final TreeSet bbDocStyleParamGroupNames = new TreeSet();
		for (int p = 0; p < docStyleParamNames.length; p++) {
			if (checkParamValueClass(docStyleParamNames[p], BoundingBox.class, true))
				bbDocStyleParamGroupNames.add(docStyleParamNames[p].substring(0, docStyleParamNames[p].lastIndexOf('.')));
		}
		
		//	combine style parameter names
		final TreeSet selDocStyleParamGroupNames = new TreeSet();
		selDocStyleParamGroupNames.addAll(fpDocStyleParamGroupNames);
		selDocStyleParamGroupNames.addAll(sDocStyleParamGroupNames);
		selDocStyleParamGroupNames.addAll(bbDocStyleParamGroupNames);
		
		//	add prefix for creating anchor (only if string given)
		if (fWordString != null)
			selDocStyleParamGroupNames.add(Anchor.ANCHOR_PREFIX + ".<create>");
		
		//	add actions using all properties of selection
		if (selDocStyleParamGroupNames.size() != 0)
			actions.add(new SelectionAction("styleUseAll", "Use Selection", "Use properties and bounds of selected words in document style") {
				public JMenuItem getMenuItem(ImDocumentMarkupPanel invoker) {
					if (selDocStyleParamGroupNames.size() == 1)
						return super.getMenuItem(invoker);
					
					//	populate sub menu
					JMenu m = new JMenu(this.label + " ...");
					m.setToolTipText(this.tooltip);
					JMenuItem mi;
					String smTpgn = null;
					JMenu sm = null;
					for (Iterator pnit = selDocStyleParamGroupNames.iterator(); pnit.hasNext();) {
						final String pgn = ((String) pnit.next());
						mi = createParameterGroupMenuItem(pgn);
						mi.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								if (docStyleEditor != null)
									docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
								useSelection(idmp.document, start.pageId, docStyle, pgn, docStyleParamNames, fMinFontSize, fMaxFontSize, fIsBold, fIsItalics, fIsAllCaps, fWordString, fWordBounds);
							}
						});
						String tpgn = ((pgn.indexOf('.') == -1) ? pgn : pgn.substring(0, pgn.indexOf('.')));
						if (tpgn.equals(editTopParamGroupName))
							m.add(mi);
						else {
							if (!tpgn.equals(smTpgn)) {
								smTpgn = tpgn;
								sm = new JMenu(getParameterGroupLabel(smTpgn, false));
								m.add(sm);
							}
							sm.add(mi);
						}
					}
					
					//	finally ...
					return m;
				}
				public boolean performAction(ImDocumentMarkupPanel invoker) {
					if (docStyleEditor != null)
						docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
					useSelection(idmp.document, start.pageId, docStyle, ((String) selDocStyleParamGroupNames.first()), docStyleParamNames, fMinFontSize, fMaxFontSize, fIsBold, fIsItalics, fIsAllCaps, fWordString, fWordBounds);
					return false;
				}
			});
		
		//	add action editing document style (open dialog with tree based access to all style parameters)
//		if ((this.docStyleEditor == null) || !this.docStyleEditor.isVisible() || !docStyleName.equals(this.docStyleEditor.docStyleName))
		if ((this.docStyleEditor == null) || !this.docStyleEditor.isVisible() || (this.docStyleEditor.headDocStyle == null) || !docStyle.id.equals(this.docStyleEditor.headDocStyle.id))
			actions.add(this.getEditDocStyleAction(idmp, docStyleName, docStyle));
		
		//	finally ...
		return ((SelectionAction[]) actions.toArray(new SelectionAction[actions.size()]));
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.AbstractSelectionActionProvider#getActions(java.awt.Point, java.awt.Point, de.uka.ipd.idaho.im.ImPage, de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel)
	 */
	public SelectionAction[] getActions(Point start, Point end, final ImPage page, final ImDocumentMarkupPanel idmp) {
		
		//	get document style name and style
		String docStyleName = ((String) idmp.document.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE));
		
		//	no document style assigned, offer adding or creating one
		if (docStyleName == null) {
			
			//	no document style opened for editing, offer adding or creating one
//			if ((this.docStyleEditor == null) || (this.docStyleEditor.docStyleName == null))
			if ((this.docStyleEditor == null) || (this.docStyleEditor.headDocStyle == null))
				return this.getAssignDocStyleAction(idmp);
			
			//	use editing document style
//			else docStyleName = this.docStyleEditor.docStyleName;
			else docStyleName = this.docStyleEditor.headDocStyle.name;
		}
		
		//	no document style assigned we'd have the sources for
		else if (!this.docStylesByName.containsKey(docStyleName))
			return this.getHandleUnavailableDocStyleActions(idmp, docStyleName);
		
		//	get document style ID and double-check name
		String docStyleId = ((String) idmp.document.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE));
		if (docStyleId != null) {
			DocStyleSettings docStyle = this.getDocStyleById(docStyleId);
			if (docStyle != null) {
//				//	CANNOT DO THIS OUTSIDE ATOMIC ACTION !!!
//				if ((docStyleName != null) && !docStyle.name.equals(docStyleName))
//					idmp.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, docStyle.name);
				docStyleName = docStyle.name;
			}
		}
		
		//	collect actions
		ArrayList actions = new ArrayList();
		
		//	offer changing and removing document style
		actions.add(this.getChangeDocStyleAction(idmp, docStyleId, docStyleName));
		actions.add(this.getRemoveDocStyleAction(idmp, docStyleName));
		
		//	style already assigned, offer extending or modifying it
		final DocStyleSettings docStyle;
		if (this.docStyleEditor == null)
			docStyle = this.getDocStyleByName(docStyleName, true);
//		else if (docStyleName.equals(this.docStyleEditor.docStyleName))
		else if (docStyleName.equals(this.docStyleEditor.headDocStyle.name))
			docStyle = this.docStyleEditor.headDocStyle;
		else {
			docStyle = this.getDocStyleByName(docStyleName, true);
//			this.docStyleEditor.setDocStyle(idmp.document, docStyleName, docStyle); // TODOne maybe do this only when action actually used
		}
		
		//	measure selection, and crop to fit in page bounds (editor panel adds a bit extra space around actual page)
		int left = Math.max(page.bounds.left, Math.min(start.x, end.x));
		int right = Math.min(page.bounds.right, Math.max(start.x, end.x));
		int top = Math.max(page.bounds.top, Math.min(start.y, end.y));
		int bottom = Math.min(page.bounds.bottom, Math.max(start.y, end.y));
		
		//	fix parameter values, scaling bounds and margins to default 72 DPI
		final BoundingBox fWordBounds = ImDocumentStyle.scaleBox(new BoundingBox(left, right, top, bottom), page.getImageDPI(), 72, 'O');
		final int fHoriMargin = ImDocumentStyle.scaleInt((right - left), page.getImageDPI(), 72, 'F');
		final int cHoriMargin = ImDocumentStyle.scaleInt((right - left), page.getImageDPI(), 72, 'C');
		final int fVertMargin = ImDocumentStyle.scaleInt((bottom - top), page.getImageDPI(), 72, 'F');
		final int cVertMargin = ImDocumentStyle.scaleInt((bottom - top), page.getImageDPI(), 72, 'C');
		
		//	get available parameter names, including ones from style proper (anchors !!!)
		TreeSet docStyleParamNameSet = new TreeSet(Arrays.asList(this.parameterValueClassNames.getKeys()));
		String[] dsDocStyleParamNames = docStyle.getKeys();
		for (int p = 0; p < dsDocStyleParamNames.length; p++) {
			if (dsDocStyleParamNames[p].startsWith(Anchor.ANCHOR_PREFIX + "."))
				docStyleParamNameSet.add(dsDocStyleParamNames[p]);
		}
		final String[] docStyleParamNames = ((String[]) docStyleParamNameSet.toArray(new String[docStyleParamNameSet.size()]));
		
		//	collect style parameter group names that use bounding box properties
		final TreeSet bbDocStyleParamNames = new TreeSet();
		for (int p = 0; p < docStyleParamNames.length; p++) {
			if (checkParamValueClass(docStyleParamNames[p], BoundingBox.class, true))
				bbDocStyleParamNames.add(docStyleParamNames[p]);
		}
		
		//	get currently selected parameter group from editor (if we have one open)
		final String editParamGroupName = ((this.docStyleEditor == null) ? "<NONE>" : this.docStyleEditor.paramGroupName);
		final String editTopParamGroupName;
		if (editParamGroupName == null)
			editTopParamGroupName = "<NONE>";
		else if (editParamGroupName.indexOf('.') == -1)
			editTopParamGroupName = editParamGroupName;
		else editTopParamGroupName = editParamGroupName.substring(0, editParamGroupName.indexOf('.'));
		
		//	add actions using bounding box
		if (bbDocStyleParamNames.size() != 0)
			actions.add(new SelectionAction("styleUseBox", "Use Bounding Box", "Use selected bounding box in document style") {
				public JMenuItem getMenuItem(ImDocumentMarkupPanel invoker) {
					if (bbDocStyleParamNames.size() == 1)
						return super.getMenuItem(invoker);
					
					//	populate sub menu
					JMenu m = new JMenu(this.label + " ...");
					m.setToolTipText(this.tooltip);
					JMenuItem mi;
					String smTpgn = null;
					JMenu sm = null;
					for (Iterator pnit = bbDocStyleParamNames.iterator(); pnit.hasNext();) {
						final String pn = ((String) pnit.next());
						mi = createParameterMenuItem(pn);
						mi.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								if (docStyleEditor != null)
									docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
								useBoundingBox(idmp.document, page.pageId, docStyle, pn, fWordBounds);
							}
						});
						String pgn = ((pn.lastIndexOf('.') == -1) ? pn : pn.substring(0, pn.lastIndexOf('.')));
						String tpgn = ((pgn.indexOf('.') == -1) ? pgn : pgn.substring(0, pgn.indexOf('.')));
						if (tpgn.equals(editTopParamGroupName))
							m.add(mi);
						else {
							if (!tpgn.equals(smTpgn)) {
								smTpgn = tpgn;
								sm = new JMenu(getParameterGroupLabel(smTpgn, false));
								m.add(sm);
							}
							sm.add(mi);
						}
					}
					
					//	finally ...
					return m;
				}
				public boolean performAction(ImDocumentMarkupPanel invoker) {
					if (docStyleEditor != null)
						docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
					useBoundingBox(idmp.document, page.pageId, docStyle, ((String) bbDocStyleParamNames.first()), fWordBounds);
					return false;
				}
			});
		
		//	collect style parameter names that use integer properties (apart from font sizes)
		final TreeSet mDocStyleParamNames = new TreeSet();
		for (int p = 0; p < docStyleParamNames.length; p++) {
			if (true
					&& !docStyleParamNames[p].endsWith(".margin") && !docStyleParamNames[p].endsWith("Margin")
					&& !docStyleParamNames[p].endsWith(".width") && !docStyleParamNames[p].endsWith("Width")
					&& !docStyleParamNames[p].endsWith(".height") && !docStyleParamNames[p].endsWith("Height")
					&& !docStyleParamNames[p].endsWith(".distance") && !docStyleParamNames[p].endsWith("Distance")
					&& !docStyleParamNames[p].endsWith(".dist") && !docStyleParamNames[p].endsWith("Dist")
					&& !docStyleParamNames[p].endsWith(".gap") && !docStyleParamNames[p].endsWith("Gap")
				) continue;
			if (checkParamValueClass(docStyleParamNames[p], Integer.class, false))
				mDocStyleParamNames.add(docStyleParamNames[p]);
		}
		
		//	if two words on same line, offer using horizontal distance between first and last (e.g. for minimum column margin)
		if ((fHoriMargin != 0) && (mDocStyleParamNames.size() != 0))
			actions.add(new SelectionAction("styleUseMargin", "Use Horizontal Margin", "Use width of selection in document style") {
				public JMenuItem getMenuItem(ImDocumentMarkupPanel invoker) {
					if (mDocStyleParamNames.size() == 1)
						return super.getMenuItem(invoker);
					
					//	populate sub menu
					JMenu m = new JMenu(this.label + " ...");
					m.setToolTipText(this.tooltip);
					JMenuItem mi;
					String smTpgn = null;
					JMenu sm = null;
					for (Iterator pnit = mDocStyleParamNames.iterator(); pnit.hasNext();) {
						final String pn = ((String) pnit.next());
						mi = createParameterMenuItem(pn);
						mi.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								if (docStyleEditor != null)
									docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
								useMargin(idmp.document.docId, docStyle, pn, fHoriMargin, cHoriMargin);
							}
						});
						String pgn = ((pn.lastIndexOf('.') == -1) ? pn : pn.substring(0, pn.lastIndexOf('.')));
						String tpgn = ((pgn.indexOf('.') == -1) ? pgn : pgn.substring(0, pgn.indexOf('.')));
						if (tpgn.equals(editTopParamGroupName))
							m.add(mi);
						else {
							if (!tpgn.equals(smTpgn)) {
								smTpgn = tpgn;
								sm = new JMenu(getParameterGroupLabel(smTpgn, false));
								m.add(sm);
							}
							sm.add(mi);
						}
					}
					
					//	finally ...
					return m;
				}
				public boolean performAction(ImDocumentMarkupPanel invoker) {
					if (docStyleEditor != null)
						docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
					useMargin(idmp.document.docId, docStyle, ((String) mDocStyleParamNames.first()), fHoriMargin, cHoriMargin);
					return false;
				}
			});
		
		//	if two or more words not on same line, offer using vertical distance between first and last (e.g. for minimum block margin)
		if ((fVertMargin != 0) && (mDocStyleParamNames.size() != 0))
			actions.add(new SelectionAction("styleUseMargin", "Use Vertical Margin", "Use height of selection in document style") {
				public JMenuItem getMenuItem(ImDocumentMarkupPanel invoker) {
					if (mDocStyleParamNames.size() == 1)
						return super.getMenuItem(invoker);
					
					//	populate sub menu
					JMenu m = new JMenu(this.label + " ...");
					m.setToolTipText(this.tooltip);
					JMenuItem mi;
					String smTpgn = null;
					JMenu sm = null;
					for (Iterator pnit = mDocStyleParamNames.iterator(); pnit.hasNext();) {
						final String pn = ((String) pnit.next());
						mi = createParameterMenuItem(pn);
						mi.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								if (docStyleEditor != null)
									docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
								useMargin(idmp.document.docId, docStyle, pn, fVertMargin, cVertMargin);
							}
						});
						String pgn = ((pn.lastIndexOf('.') == -1) ? pn : pn.substring(0, pn.lastIndexOf('.')));
						String tpgn = ((pgn.indexOf('.') == -1) ? pgn : pgn.substring(0, pgn.indexOf('.')));
						if (tpgn.equals(editTopParamGroupName))
							m.add(mi);
						else {
							if (!tpgn.equals(smTpgn)) {
								smTpgn = tpgn;
								sm = new JMenu(getParameterGroupLabel(smTpgn, false));
								m.add(sm);
							}
							sm.add(mi);
						}
					}
					
					//	finally ...
					return m;
				}
				public boolean performAction(ImDocumentMarkupPanel invoker) {
					if (docStyleEditor != null)
						docStyleEditor.setContentDocStyle(idmp, docStyle.name, docStyle);
					useMargin(idmp.document.docId, docStyle, ((String) mDocStyleParamNames.first()), fVertMargin, cVertMargin);
					return false;
				}
			});
		
		//	add action editing document style (open dialog with tree based access to all style parameters)
//		if ((this.docStyleEditor == null) || !this.docStyleEditor.isVisible() || !docStyleName.equals(this.docStyleEditor.docStyleName))
		if ((this.docStyleEditor == null) || !this.docStyleEditor.isVisible() || (this.docStyleEditor.headDocStyle == null) || !docStyle.id.equals(this.docStyleEditor.headDocStyle.id))
			actions.add(this.getEditDocStyleAction(idmp, docStyleName, docStyle));
		
		//	finally ...
		return ((SelectionAction[]) actions.toArray(new SelectionAction[actions.size()]));
	}
	
	private JMenuItem createParameterGroupMenuItem(String pgn) {
		ParameterGroupDescription pgd = this.getParameterGroupDescription(pgn);
		String pgl = this.getParameterGroupLabel(pgn, pgd, false);
		if ((this.docStyleEditor != null) && pgn.equals(this.docStyleEditor.paramGroupName))
			pgl = ("<HTML><B>" + AnnotationUtils.escapeForXml(pgl) + "</B></HTML>");
		JMenuItem mi = new JMenuItem(pgl);
		if (pgd != null)
			mi.setToolTipText(pgd.getDescription());
		return mi;
	}
	
	private JMenuItem createParameterMenuItem(String pn) {
		String pgn = pn.substring(0, pn.lastIndexOf('.'));
		String glpn = pn.substring(pn.lastIndexOf('.') + ".".length());
		ParameterGroupDescription pgd = this.getParameterGroupDescription(pgn);
		String pgl = this.getParameterGroupLabel(pgn, pgd, false);
		String pl = ((pgd == null) ? pn : pgd.getParamLabel(glpn));
		if ((pl == null) || (pl.length() == 0))
			pl = pn;
		else pl = (pgl + " / " + pl);
		if ((this.docStyleEditor != null) && (this.docStyleEditor.paramGroupName != null) && pn.startsWith(this.docStyleEditor.paramGroupName) && (this.docStyleEditor.paramGroupName.length() == pn.lastIndexOf('.')))
			pl = ("<HTML><B>" + AnnotationUtils.escapeForXml(pl) + "</B></HTML>");
		JMenuItem mi = new JMenuItem(pl);
		if (pgd != null)
			mi.setToolTipText(pgd.getParamDescription(glpn));
		return mi;
	}
	
	private String getParameterGroupLabel(String pgn, boolean forTree) {
		return this.getParameterGroupLabel(pgn, this.getParameterGroupDescription(pgn), forTree);
	}
	
	private String getParameterGroupLabel(String pgn, ParameterGroupDescription pgd, boolean forTree) {
		if (pgn.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
			if (forTree)
				return pgn.substring((Anchor.ANCHOR_PREFIX + ".").length());
			else return ("Anchor '" + pgn.substring((Anchor.ANCHOR_PREFIX + ".").length()) + "'");
		}
		String pgl = ((pgd == null) ? null : pgd.getLabel());
		if ((pgl == null) || (pgl.length() == 0))
			return StringUtils.capitalize(pgn);
		else return pgl;
	}
	
	private static final boolean DEBUG_STYLE_UPDATES = true;
	
	private void useFontProperties(ImDocument doc, int pageId, DocStyleSettings docStyle, String docStyleParamGroupName, String[] docStyleParamNames, int minFontSize, int maxFontSize, boolean isBold, boolean isItalics, boolean isAllCaps) {
		
		//	get parameter group description and group label
		ParameterGroupDescription pgd = this.getParameterGroupDescription(docStyleParamGroupName);
		String pgl = ((pgd == null) ? null : pgd.getLabel());
		
		//	ask for properties to use
		JPanel fpPanel = new JPanel(new GridLayout(0, 1, 0, 0), true);
		UseBooleanPanel useMinFontSize = null;
		UseBooleanPanel useMaxFontSize = null;
		if (minFontSize <= maxFontSize) {
			int eMinFontSize = 72;
			try {
				eMinFontSize = Integer.parseInt(docStyle.getSetting((docStyleParamGroupName + ".minFontSize"), "72"));
			} catch (NumberFormatException nfe) {}
			if (minFontSize < eMinFontSize) {
				String pl;
				if (pgl == null)
					pl = ("Use " + minFontSize + " as Minimum Font Size (currently " + eMinFontSize + ")");
				else pl = ("Use " + minFontSize + " as Minimum Font Size for " + pgl + " (currently " + eMinFontSize + ")");
				String pd = ((pgd == null) ? null : pgd.getParamDescription("minFontSize"));
				useMinFontSize = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".minFontSize"), pl, pd, true);
				fpPanel.add(useMinFontSize);
			}
			int eMaxFontSize = 0;
			try {
				eMaxFontSize = Integer.parseInt(docStyle.getSetting((docStyleParamGroupName + ".maxFontSize"), "0"));
			} catch (NumberFormatException nfe) {}
			if (eMaxFontSize < maxFontSize) {
				String pl;
				if (pgl == null)
					pl = ("Use " + maxFontSize + " as Maximum Font Size (currently " + eMaxFontSize + ")");
				else pl = ("Use " + maxFontSize + " as Maximum Font Size for " + pgl + " (currently " + eMaxFontSize + ")");
				String pd = ((pgd == null) ? null : pgd.getParamDescription("minFontSize"));
				useMaxFontSize = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".maxFontSize"), pl, pd, true);
				fpPanel.add(useMaxFontSize);
			}
		}
		UseBooleanPanel useIsBold = null;
		if (isBold) {
			for (int p = 0; p < docStyleParamNames.length; p++) {
				if (docStyleParamNames[p].equals(docStyleParamGroupName + ".isBold")) {
					String pl;
					if (pgl == null)
						pl = "Require Values to be Bold";
					else pl = ("Require Values for " + pgl + " to be Bold");
					String pd = ((pgd == null) ? null : pgd.getParamDescription("isBold"));
					useIsBold = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".isBold"), pl, pd, true);
					break;
				}
				else if (docStyleParamNames[p].equals(docStyleParamGroupName + ".startIsBold")) {
					String pl;
					if (pgl == null)
						pl = "Require Value Starts to be Bold";
					else pl = ("Require Values for " + pgl + " to Start in Bold");
					String pd = ((pgd == null) ? null : pgd.getParamDescription("startIsBold"));
					useIsBold = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".startIsBold"), pl, pd, true);
					break;
				}
			}
			if (useIsBold != null)
				fpPanel.add(useIsBold);
		}
		UseBooleanPanel useIsItalics = null;
		if (isItalics) {
			for (int p = 0; p < docStyleParamNames.length; p++) {
				if (docStyleParamNames[p].equals(docStyleParamGroupName + ".isItalics")) {
					String pl;
					if (pgl == null)
						pl = "Require Values to be in Italics";
					else pl = ("Require Values for " + pgl + " to be in Italics");
					String pd = ((pgd == null) ? null : pgd.getParamDescription("isItalics"));
					useIsItalics = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".isItalics"), pl, pd, true);
					break;
				}
				else if (docStyleParamNames[p].equals(docStyleParamGroupName + ".startIsItalics")) {
					String pl;
					if (pgl == null)
						pl = "Require Values Starts to be in Italics";
					else pl = ("Require Values for " + pgl + " to Start in Italics");
					String pd = ((pgd == null) ? null : pgd.getParamDescription("startIsItalics"));
					useIsItalics = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".startIsItalics"), pl, pd, true);
					break;
				}
			}
			if (useIsItalics != null)
				fpPanel.add(useIsItalics);
		}
		UseBooleanPanel useIsAllCaps = null;
		if (isAllCaps) {
			for (int p = 0; p < docStyleParamNames.length; p++) {
				if (docStyleParamNames[p].equals(docStyleParamGroupName + ".isAllCaps")) {
					String pl;
					if (pgl == null)
						pl = "Require Values to be in All Caps";
					else pl = ("Require Values for " + pgl + " to be in All Caps");
					String pd = ((pgd == null) ? null : pgd.getParamDescription("isAllCaps"));
					useIsAllCaps = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".isAllCaps"), pl, pd, true);
					break;
				}
				else if (docStyleParamNames[p].equals(docStyleParamGroupName + ".startIsAllCaps")) {
					String pl;
					if (pgl == null)
						pl = "Require Values Starts to be in All Caps";
					else pl = ("Require Values for " + pgl + " to Start in All Caps");
					String pd = ((pgd == null) ? null : pgd.getParamDescription("startIsAllCaps"));
					useIsAllCaps = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".startIsAllCaps"), pl, pd, true);
					break;
				}
			}
			if (useIsAllCaps != null)
				fpPanel.add(useIsAllCaps);
		}
		
		//	add target style selector if required
		DocStyleSettings[] docStyleChain = docStyle.getInheritanceChain();
		JComboBox targetDocStyleSelector = null;
		if ((docStyleChain.length > 1) && (this.docStyleEditor != null)) {
			targetDocStyleSelector = new JComboBox(docStyleChain);
			targetDocStyleSelector.setSelectedItem((this.docStyleEditor.selectedDocStyle == null) ? docStyleChain[0] : this.docStyleEditor.selectedDocStyle);
			JPanel tdsPanel = new JPanel(new BorderLayout(), true);
			tdsPanel.add(new JLabel("Add to document style: "), BorderLayout.WEST);
			tdsPanel.add(targetDocStyleSelector, BorderLayout.CENTER);
			fpPanel.add(tdsPanel);
		}
		
		//	prompt
		int choice = JOptionPane.showConfirmDialog(null, fpPanel, ("Select Font Properties to Use" + ((pgl == null) ? "" : (" in " + pgl))), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (choice != JOptionPane.OK_OPTION)
			return;
		
		//	get target document style source ID
		DocStyleSettings targetDocStyle;
		String targetDocStyleId;
		if (targetDocStyleSelector == null) {
			targetDocStyle = docStyle;
			targetDocStyleId = docStyle.id;
		}
		else {
			targetDocStyle = ((DocStyleSettings) targetDocStyleSelector.getSelectedItem());
			targetDocStyleId = targetDocStyle.id;
		}
		
		//	we have an anchor, adjust minimum page ID
		if (docStyleParamGroupName.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
			int maxPageId = Integer.parseInt(targetDocStyle.data.getSetting((Anchor.ANCHOR_PREFIX + ".maxPageId"), "0"));
			if (maxPageId < pageId)
				docStyle.setSetting(targetDocStyleId, (Anchor.ANCHOR_PREFIX + ".maxPageId"), ("" + (pageId + doc.getFirstPageId())));
		}
		
		//	set properties
		if ((useMinFontSize != null) && useMinFontSize.useParam.isSelected()) {
			docStyle.setSetting(targetDocStyleId, (docStyleParamGroupName + ".minFontSize"), ("" + minFontSize));
			if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamGroupName + ".minFontSize set to " + minFontSize);
		}
		if ((useMaxFontSize != null) && useMaxFontSize.useParam.isSelected()) {
			docStyle.setSetting(targetDocStyleId, (docStyleParamGroupName + ".maxFontSize"), ("" + maxFontSize));
			if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamGroupName + ".maxFontSize set to " + maxFontSize);
		}
		if ((minFontSize == maxFontSize) && (useMinFontSize != null) && useMinFontSize.useParam.isSelected() && (useMaxFontSize != null) && useMaxFontSize.useParam.isSelected()) {
			docStyle.setSetting(targetDocStyleId, (docStyleParamGroupName + ".fontSize"), ("" + minFontSize));
			if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamGroupName + ".fontSize set to " + minFontSize);
		}
		if ((useIsBold != null) && useIsBold.useParam.isSelected()) {
			docStyle.setSetting(targetDocStyleId, useIsBold.docStyleParamName, "true");
			if (DEBUG_STYLE_UPDATES) System.out.println(useIsBold.docStyleParamName + " set to true");
		}
		if ((useIsItalics != null) && useIsItalics.useParam.isSelected()) {
			docStyle.setSetting(targetDocStyleId, useIsItalics.docStyleParamName, "true");
			if (DEBUG_STYLE_UPDATES) System.out.println(useIsItalics.docStyleParamName + " set to true");
		}
		if ((useIsAllCaps != null) && useIsAllCaps.useParam.isSelected()) {
			docStyle.setSetting(targetDocStyleId, useIsAllCaps.docStyleParamName, "true");
			if (DEBUG_STYLE_UPDATES) System.out.println(useIsAllCaps.docStyleParamName + " set to true");
		}
		
		//	if style editor open, adjust tree path
		if (this.docStyleEditor != null) {
			this.docStyleEditor.setParamGroupName(docStyleParamGroupName);
			if (targetDocStyleSelector != null)
				this.docStyleEditor.selectDocStyle((DocStyleSettings) targetDocStyleSelector.getSelectedItem());
//			this.docStyleEditor.setDocStyleDirty(true);
			this.docStyleEditor.checkDocStyleDirty();
		}
//		
//		//	index document style for saving
//		this.docStylesByDocId.put(doc.docId, docStyle);
	}
	
	private void useMargin(String docId, DocStyleSettings docStyle, String docStyleParamName, int fMargin, int cMargin) {
		Class paramValueClass = this.getParamValueClass(docStyleParamName);
		
		/* TODO Handling inheritance in document style editing (especially in "Use ..." functions):
		 * - provide "Use In" drop-down with whole inheritance chain selectable ...
		 * - ... and style name of currently selected tab pre-selected
		 */
		
		//	get editing target
		DocStyleSettings targetDocStyle = docStyle;
		String targetDocStyleId = docStyle.id;
		if ((this.docStyleEditor != null) && (this.docStyleEditor.selectedDocStyle != null)) {
			targetDocStyle = this.docStyleEditor.selectedDocStyle;
			targetDocStyleId = this.docStyleEditor.selectedDocStyle.id;
		}
		
		//	single integer, expand or overwrite
		if (Integer.class.getName().equals(paramValueClass.getName())) {
			int eMargin = Integer.parseInt(targetDocStyle.data.getSetting(docStyleParamName, "-1"));
			if (eMargin == -1) {
				docStyle.setSetting(targetDocStyleId, docStyleParamName, ("" + fMargin));
				if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + fMargin);
			}
			else if (docStyleParamName.startsWith(".min", docStyleParamName.lastIndexOf('.'))) {
				docStyle.setSetting(targetDocStyleId, docStyleParamName, ("" + Math.min(((eMargin == -1) ? fMargin : eMargin),  fMargin)));
				if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + Math.min(eMargin,  Math.min(((eMargin == -1) ? cMargin : eMargin),  fMargin)));
			}
			else if (docStyleParamName.startsWith(".max", docStyleParamName.lastIndexOf('.'))) {
				docStyle.setSetting(targetDocStyleId, docStyleParamName, ("" + Math.max(((eMargin == -1) ? fMargin : eMargin),  cMargin)));
				if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + Math.max(eMargin,  Math.max(((eMargin == -1) ? cMargin : eMargin),  cMargin)));
			}
			else {
				docStyle.setSetting(targetDocStyleId, docStyleParamName, ("" + fMargin));
				if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + fMargin);
			}
		}
		
		//	list of integers, add new one and eliminate duplicates
		else if (Integer.class.getName().equals(DocumentStyle.getListElementClass(paramValueClass).getName())) {
			String eMarginStr = targetDocStyle.data.getSetting(docStyleParamName);
			if ((eMarginStr == null) || (eMarginStr.trim().length() == 0)) {
				docStyle.setSetting(targetDocStyleId, docStyleParamName, ("" + fMargin));
				if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + fMargin);
			}
			else {
				String[] eMarginStrs = eMarginStr.split("[^0-9]+");
				for (int e = 0; e < eMarginStrs.length; e++) {
					if (fMargin == Integer.parseInt(eMarginStrs[e]))
						return;
				}
				docStyle.setSetting(targetDocStyleId, docStyleParamName, (eMarginStr + " " + fMargin));
				if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + (eMarginStr + " " + fMargin));
			}
		}
		
		//	if style editor open, adjust tree path
		if (this.docStyleEditor != null) {
			this.docStyleEditor.setParamGroupName(docStyleParamName.substring(0, docStyleParamName.lastIndexOf('.')));
//			this.docStyleEditor.setDocStyleDirty(true);
			this.docStyleEditor.checkDocStyleDirty();
		}
//		
//		//	index document style for saving
//		this.docStylesByDocId.put(docId, docStyle);
	}
	
	private void useString(final ImDocument doc, int pageId, DocStyleSettings docStyle, String docStyleParamGroupName, String[] docStyleParamNames, String string) {
		
		//	get parameter group description and group label
		ParameterGroupDescription pgd = this.getParameterGroupDescription(docStyleParamGroupName);
		String pgl = ((pgd == null) ? null : pgd.getLabel());
		
		//	collect style parameter names in argument group that use string properties, constructing string usage panels on the fly
		TreeSet sDocStyleParamPanels = new TreeSet();
		final ImTokenSequence[] docTokens = {null}; // using array facilitates sharing tokens and still generating them on demand
		for (int p = 0; p < docStyleParamNames.length; p++) {
			if (!docStyleParamNames[p].startsWith(docStyleParamGroupName + "."))
				continue;
			if (!checkParamValueClass(docStyleParamNames[p], String.class, true) && !checkParamValueClass(docStyleParamNames[p], Pattern.class, true))
				continue;
			String localDspn = docStyleParamNames[p].substring(docStyleParamNames[p].lastIndexOf('.') + ".".length());
			String pl = ((pgd == null) ? null : pgd.getParamLabel(localDspn));
			if (pl == null)
				pl = (" Use as " + localDspn);
			else pl = (" Use as " + pl);
			String pd = ((pgd == null) ? null : pgd.getParamDescription(localDspn));
			sDocStyleParamPanels.add(new UseStringPanel(this.docStyleEditor, docStyleParamNames[p], pl, pd, true, string, true, checkParamValueClass(docStyleParamNames[p], Pattern.class, false), true) {
				ImTokenSequence getTestDocTokens() {
					if (docTokens[0] == null)
						docTokens[0] = new ImTokenSequence(((Tokenizer) doc.getAttribute(ImDocument.TOKENIZER_ATTRIBUTE, Gamta.INNER_PUNCTUATION_TOKENIZER)), doc.getTextStreamHeads(), (ImTokenSequence.NORMALIZATION_LEVEL_PARAGRAPHS | ImTokenSequence.NORMALIZE_CHARACTERS));
					return docTokens[0];
				}
				ImDocument getTestDoc() {
					return doc;
				}
				DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
					if (this.docStyleParamName.endsWith(".linePattern") || this.docStyleParamName.endsWith("LinePattern"))
						return getLinePatternVisualizationGraphics(this.parent, page, this.getDisplayedValue());
					if (this.docStyleParamName.endsWith(".pattern") || this.docStyleParamName.endsWith("Pattern"))
						return getPatternVisualizationGraphics(this.parent, page, this.getDisplayedValue());
					//	TODO think of more
					return NO_DISPLAY_EXTENSION_GRAPHICS;
				}
			});
		}
		
		//	nothing to work with
		if (sDocStyleParamPanels.isEmpty())
			return;
		
		//	assemble panel
		JPanel sPanel = new JPanel(new GridLayout(0, 1, 0, 3), true);
		for (Iterator ppit = sDocStyleParamPanels.iterator(); ppit.hasNext();)
			sPanel.add((UseStringPanel) ppit.next());
		
		//	add target style selector if required
		DocStyleSettings[] docStyleChain = docStyle.getInheritanceChain();
		JComboBox targetDocStyleSelector = null;
		if ((docStyleChain.length > 1) && (this.docStyleEditor != null)) {
			targetDocStyleSelector = new JComboBox(docStyleChain);
			targetDocStyleSelector.setSelectedItem((this.docStyleEditor.selectedDocStyle == null) ? docStyleChain[0] : this.docStyleEditor.selectedDocStyle);
			JPanel tdsPanel = new JPanel(new BorderLayout(), true);
			tdsPanel.add(new JLabel("Add to document style: "), BorderLayout.WEST);
			tdsPanel.add(targetDocStyleSelector, BorderLayout.CENTER);
			sPanel.add(tdsPanel);
		}
		
		//	prompt
		int choice = JOptionPane.showConfirmDialog(null, sPanel, ("Select how to Use '" + string + "'" + ((pgl == null) ? "" : (" in " + pgl))), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (choice != JOptionPane.OK_OPTION)
			return;
		
		//	get target document style source ID
		DocStyleSettings targetDocStyle;
		String targetDocStyleId;
		if (targetDocStyleSelector == null) {
			targetDocStyle = docStyle;
			targetDocStyleId = docStyle.id;
		}
		else {
			targetDocStyle = ((DocStyleSettings) targetDocStyleSelector.getSelectedItem());
			targetDocStyleId = targetDocStyle.id;
		}
		
		//	we have an anchor, adjust minimum page ID
		if (docStyleParamGroupName.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
			int maxPageId = Integer.parseInt(targetDocStyle.data.getSetting((Anchor.ANCHOR_PREFIX + ".maxPageId"), "0"));
			if (maxPageId < pageId)
				docStyle.setSetting(targetDocStyleId, (Anchor.ANCHOR_PREFIX + ".maxPageId"), ("" + (pageId + doc.getFirstPageId())));
		}
		
		//	write parameters
		for (Iterator ppit = sDocStyleParamPanels.iterator(); ppit.hasNext();) {
			UseStringPanel usp = ((UseStringPanel) ppit.next());
			if (!usp.useParam.isSelected())
				continue;
			string = usp.string.getText().trim();
			if (string.length() == 0)
				continue;
			Class paramValueClass = this.getParamValueClass(usp.docStyleParamName);
			if (String.class.getName().equals(paramValueClass.getName()) || Pattern.class.getName().equals(paramValueClass.getName())) {
				docStyle.setSetting(targetDocStyleId, usp.docStyleParamName, string);
				if (DEBUG_STYLE_UPDATES) System.out.println(usp.docStyleParamName + " set to " + string);
			}
			else if (String.class.getName().equals(DocumentStyle.getListElementClass(paramValueClass).getName()) || Pattern.class.getName().equals(DocumentStyle.getListElementClass(paramValueClass).getName())) {
				String eString = targetDocStyle.data.getSetting(usp.docStyleParamName, "").trim();
				if (eString.length() == 0) {
					docStyle.setSetting(targetDocStyleId, usp.docStyleParamName, string);
					if (DEBUG_STYLE_UPDATES) System.out.println(usp.docStyleParamName + " set to " + string);
				}
				else {
					TreeSet eStringSet = new TreeSet(Arrays.asList(eString.split("\\s+")));
					eStringSet.add(string);
					StringBuffer eStringsStr = new StringBuffer();
					for (Iterator sit = eStringSet.iterator(); sit.hasNext();) {
						eStringsStr.append((String) sit.next());
						if (sit.hasNext())
							eStringsStr.append(' ');
					}
					docStyle.setSetting(targetDocStyleId, usp.docStyleParamName, eStringsStr.toString());
					if (DEBUG_STYLE_UPDATES) System.out.println(usp.docStyleParamName + " set to " + eStringsStr.toString());
				}
			}
		}
		
		//	if style editor open, adjust tree path
		if (this.docStyleEditor != null) {
			this.docStyleEditor.setParamGroupName(docStyleParamGroupName);
			if (targetDocStyleSelector != null)
				this.docStyleEditor.selectDocStyle((DocStyleSettings) targetDocStyleSelector.getSelectedItem());
//			this.docStyleEditor.setDocStyleDirty(true);
			this.docStyleEditor.checkDocStyleDirty();
		}
//		
//		//	index document style for saving
//		this.docStylesByDocId.put(doc.docId, docStyle);
	}
	
	private static class DefaultableCheckBox extends JCheckBox implements Icon, ItemListener {
		private static final Icon enUnIcon = loadIcon("enabled", "unselected");
		private static final Icon enDefIcon = loadIcon("enabled", "defaulted");
		private static final Icon enSelIcon = loadIcon("enabled", "selected");
		private static final Icon disUnIcon = loadIcon("disabled", "unselected");
		private static final Icon disDefIcon = loadIcon("disabled", "defaulted");
		private static final Icon disSelIcon = loadIcon("disabled", "selected");
		private static final boolean iconsOk = ((enUnIcon != null) && (enDefIcon != null) && (enSelIcon != null) && (disUnIcon != null) && (disDefIcon != null) && (disSelIcon != null));
		private static Icon loadIcon(String mode, String state) {
			String packageName = ImDocumentStyleManager.class.getName();
			packageName = packageName.replace('.', '/');
			try {
				return new ImageIcon(ImageIO.read(ImDocumentStyleManager.class.getClassLoader().getResourceAsStream(packageName + ".checkBox." + mode + "." + state + ".png")));
			}
			catch (IOException ioe) {
				return null; // never gonna happen, but Java don't know
			}
		}
		private String plainTooltipText = null;
		String defaultSource;
		String defaultValue;
		DefaultableCheckBox(String text, boolean selected) {
			super(text, selected);
			this.plainTooltipText = this.getToolTipText();
			if (iconsOk) {
				this.setIcon(this);
				this.setSelectedIcon(this);
				this.setDisabledIcon(this);
				this.setDisabledSelectedIcon(this);
			}
			this.addItemListener(this);
		}
		public void paintIcon(Component c, Graphics g, int x, int y) {
			if (this.isSelected())
				(this.isEnabled() ? enSelIcon : disSelIcon).paintIcon(c, g, x, y);
			else if (this.isDefaulted())
				(this.isEnabled() ? enDefIcon : disDefIcon).paintIcon(c, g, x, y);
			else (this.isEnabled() ? enUnIcon : disUnIcon).paintIcon(c, g, x, y);
		}
		public int getIconWidth() {
			return enUnIcon.getIconWidth();
		}
		public int getIconHeight() {
			return enUnIcon.getIconHeight();
		}
		public void setToolTipText(String text) {
			this.plainTooltipText = text;
			this.adjustTooltipText();
		}
		public void itemStateChanged(ItemEvent ie) {
			this.adjustTooltipText();
		}
		void setDefaults(String source, String value) {
			this.defaultSource = source;
			this.defaultValue = value;
			this.adjustTooltipText();
			this.validate();
			this.repaint();
		}
		boolean isDefaulted() {
			return (!this.isSelected() && (this.defaultSource != null) && (this.defaultValue != null));
		}
		private void adjustTooltipText() {
			if (this.isSelected())
				super.setToolTipText(this.plainTooltipText);
			else if (this.isDefaulted()) {
				if (this.plainTooltipText == null)
					super.setToolTipText("<HTML>Value inherited from '<B>" + xmlGrammar.escape(this.defaultSource) + "'</B>: <I>" + xmlGrammar.escape(this.defaultValue) + "</I></HTML>");
				else if ((this.plainTooltipText.length() < "<HTML>".length()) || !"<HTML>".equals(this.plainTooltipText.substring("<HTML>".length()).toUpperCase()))
					super.setToolTipText("<HTML>Value inherited from '<B>" + xmlGrammar.escape(this.defaultSource) + "'</B>: <I>" + xmlGrammar.escape(this.defaultValue) + "</I><BR/>" + xmlGrammar.escape(this.plainTooltipText) + "</HTML>");
				else {
					String ptt = this.plainTooltipText;
					if ("<HTML>".equals(ptt.substring(0, "<HTML>".length()).toUpperCase()))
						ptt = ptt.substring("<HTML>".length());
					if ((ptt.length() >= "</HTML>".length()) && "</HTML>".equals(ptt.substring(ptt.length() - "</HTML>".length()).toUpperCase()))
						ptt = ptt.substring(0, (ptt.length() - "</HTML>".length()));
					super.setToolTipText("<HTML>Value inherited from '<B>" + xmlGrammar.escape(this.defaultSource) + "'</B>: <I>" + xmlGrammar.escape(this.defaultValue) + "</I><BR/>" + ptt + "</HTML>");
				}
			}
			else super.setToolTipText(this.plainTooltipText);
		}
	}
	
	private interface FocusableParamPanel {
		abstract void handleFocusGained();
		abstract void handleFocusLost();
	}
	
	public static void main(String[] args) throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
			System.out.println("L&F is " + UIManager.getLookAndFeel().getClass().getName());
			System.out.println("Installed L&Fs are " + Arrays.toString(UIManager.getInstalledLookAndFeels()));
		} catch (Exception e) {}
//		final DefaultableCheckBox dcb = new DefaultableCheckBox("Test", true);
//		dcb.setToolTipText("Test tooltip");
		final UseStringPanel usp = new UseStringPanel(null, "string", "String", "Test string value", true, "string value", false, false, true) {
			DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
				return null;
			}
		};
//		String[] svs = {"", "testString1", "testString2"};
//		String[] sls = {"", "Test string 1", "Test string 2"};
		String[] svs = {"testString1", "testString2"};
		String[] sls = {"Test string 1", "Test string 2"};
		final UseStringOptionPanel usop = new UseStringOptionPanel(null, "stringOption", svs, sls, "String Option", "Test string option value", true, "testString2", false);
		final UseListPanel ulp = new UseListPanel(null, "list", "List", "Test list value", true, "line1 line2", false, true) {
			DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
				return null;
			}
		};
		final JCheckBox cbd = new JCheckBox("Default?");
		cbd.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				if (cbd.isSelected()) {
					usp.setDefaults("DefSource", "default string");
//					usop.setDefaults("DefSource", "default option");
					usop.setDefaults("DefSource", "testString2");
					ulp.setDefaults("DefSource", "default list");
				}
				else {
					usp.setDefaults(null, null);
					usop.setDefaults(null, null);
					ulp.setDefaults(null, null);
				}
			}
		});
		final JCheckBox cbe = new JCheckBox("Enabled?", true);
		cbe.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				usp.setEnabled(cbe.isSelected());
				usop.setEnabled(cbe.isSelected());
				ulp.setEnabled(cbe.isSelected());
			}
		});
		JPanel cbp = new JPanel(new GridLayout(0, 1), true);
		cbp.add(usp);
		cbp.add(usop);
		cbp.add(ulp);
		cbp.add(cbd);
		cbp.add(cbe);
		JOptionPane.showMessageDialog(null, cbp, "Default Checkbox Test", JOptionPane.PLAIN_MESSAGE);
	}
	
	private static abstract class UseParamPanel extends JPanel implements Comparable {
		final DocStyleEditor parent;
		final String docStyleParamName;
		final DefaultableCheckBox useParam;
		String ownValue;
		boolean updatingValue = false;
		boolean updatingValueSource = false;
		boolean updatingDisplayState = false;
		UseParamPanel(DocStyleEditor parent, String docStyleParamName, String label, String description, boolean use) {
			super(new BorderLayout(), true);
			this.parent = parent;
			this.docStyleParamName = docStyleParamName;
			this.useParam = new DefaultableCheckBox(label, use);
			if (description != null)
				this.useParam.setToolTipText(description);
			this.useParam.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					if (isUpdating())
						return;
					notifyUsageChanged();
				}
			});
		}
		public int compareTo(Object obj) {
			return this.docStyleParamName.compareTo(((UseParamPanel) obj).docStyleParamName);
		}
		void setRequired(boolean required) {
			if (required)
				this.setExcluded(false);
			this.useParam.setFont(this.useParam.getFont().deriveFont(required ? Font.BOLD : Font.PLAIN));
		}
		void setExcluded(boolean excluded) {
			if (excluded)
				this.setRequired(false);
			this.setInputEnabled(!excluded);
		}
		void setDefaults(String source, String value) {
			this.useParam.setDefaults(source, value);
			this.apiUpdateDisplayState();
		}
		void selectDefaultSource() {
			if (this.parent == null)
				return;
			if (!this.useParam.isDefaulted())
				return;
			DocStyleSettings valueSource = this.parent.selectedDocStyle.getSourceOfValue(this.docStyleParamName);
			if ((valueSource != null) && (valueSource != this.parent.selectedDocStyle))
				this.parent.selectDocStyle(valueSource);
		}
		abstract void setInputEnabled(boolean enabled);
		abstract boolean isInputEnabled();
		final String getValue() {
			return (this.useParam.isDefaulted() ? "" : this.getDisplayedValue());
		}
		void apiSetValue(String value) {
			try {
				this.updatingValue = true;
				this.setValue(value);
			}
			finally {
				this.updatingValue = false;
			}
		}
		abstract void setValue(String value);
		abstract String getDisplayedValue();
//		void apiSetDisplayedValue(String value) {
//			try {
//				this.updatingValue = true;
//				this.setDisplayedValue(value);
//			}
//			finally {
//				this.updatingValue = false;
//			}
//		}
//		abstract void setDisplayedValue(String value);
		void apiUpdateValueSource(boolean updateUsageState) {
			try {
				this.updatingValueSource = true;
				this.updateValueSource(updateUsageState);
			}
			finally {
				this.updatingValueSource = false;
			}
		}
		void updateValueSource(boolean updateUsageState) {
			if (this.parent == null)
				return;
			String value = this.parent.selectedDocStyle.getSetting(this.docStyleParamName);
			DocStyleSettings valueSource = ((value == null) ? null : this.parent.selectedDocStyle.getSourceOfValue(this.docStyleParamName));
			if ((value == null) || (value.length() == 0)) {
				if (updateUsageState) {
					this.apiSetValue("");
					this.useParam.setSelected(false);
				}
				//else this.apiSetDisplayedValue("");
				this.useParam.setDefaults(null, null);
			}
			else if (valueSource != this.parent.selectedDocStyle) {
				if (updateUsageState) {
					this.apiSetValue("");
					this.useParam.setSelected(false);
				}
				//else this.apiSetDisplayedValue("");
				this.useParam.setDefaults(valueSource.name, value);
			}
			else {
				if (updateUsageState) {
					this.apiSetValue(value);
					this.useParam.setSelected(true);
				}
				//else this.apiSetDisplayedValue(value);
				this.useParam.setDefaults(null, null);
			}
			this.apiUpdateDisplayState();
			if (!updateUsageState && (this.parent != null))
				this.parent.setActiveParamPanel(this);
		}
		void apiUpdateDisplayState() {
			try {
				this.updatingDisplayState = true;
				this.updateDisplayState();
			}
			finally {
				this.updatingDisplayState = false;
			}
		}
		boolean isUpdating() {
			return (this.updatingDisplayState || this.updatingValue || this.updatingValueSource);
		}
		/*
State transitions (and respective actions):
- selected -> unselected:
  - can use usage change
  - maybe set text color to gray
  - retain value
- unselected -> selected
  - can use usage change
  - set text color to black
  - retain value
- selected -> defaulted:
  - can use usage change
  - set text color to gray
  - set content to default value
- defaulted -> selected:
  - can use usage change
  - set text color to black
  - set content to own (pre-defaulting) value
- unselected -> defaulted:
  - no usage change
  - set text color to gray (unless unselected becomes gray as well)
  - set content to default value
- defaulted -> unselected
  - no usage change
  - maybe set text color to black (unless unselected becomes gray as well)
  - set content to own (pre-defaulting) value
		 */
		abstract void updateDisplayState();
		boolean verifyValue(String value) {
			return true;
		}
		void notifyUsageChanged() {
			if (this.parent != null)
				this.parent.paramUsageChanged(this);
			this.apiUpdateDisplayState();
		}
		void notifyActivated() {
//			System.out.println("Field '" + this.docStyleParamName + "' activated");
			if (this.parent != null)
				this.parent.setActiveParamPanel(this);
		}
		void notifyModified() {
			if (this.parent != null) {
				this.parent.setActiveParamPanel(this);
				this.parent.paramValueChanged(this);
			}
		}
		void notifyDeactivated() {
//			System.out.println("Field '" + this.docStyleParamName + "' deactivated");
//			BETTER STAY ON FOR VISUALIZATION, USER MIGHT WANT TO SCROLL THROUGH MAIN WINDOW, AND WE LOSE FOCUS FROM THAT
//			if (this.parent != null)
//				this.parent.setActiveParamPanel(null);
		}
		abstract DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page);
	}
	
	/* TODO Further candidates for property types:
- Pattern (we've brought this up before, just DO IT !!!)
- AnnotationPattern (will require public instances, but well ...)
- LinePattern (only in IM subclass)
- Object (to be serialized as JSON)
- GPath (maybe only in XML subclass)
- HTML (might be useful for non-standard license texts ,,,)
- HEX (helpful with API tokens, etc. ... who knows what we might wanna use it for)
- Base64 (see HEX)
==> also provide infrastructure for ValueValidarors:
  - still use DocumentStyle as central registry ...
  - use via validateValue(String value, Class valueClass) method ...
  - ... and validateValueList(String[] value, Class valueClass)
  ==> saves duplicating (or awkwardly abstracting) whole hassle in editors
  ==> preempts exceptions on loading
==> maybe even centralize validation to StringUtils ...
==> ... as it validates string input, after all
  ==> maybe even extend this to also comprise test facilities (for specific types only, and only against specific data types)
  ==> might even be useful for password policy checking
==> maybe even do same thing for normalizing and formatting strings
==> name whole thing StringInputUtils ...
==> ... and register implementations from static initializers (just like anchor factories)
==> also allows centralizing input field widgets
  - display list values in separate fields with gray separator lines (akin to new XML UI draft)
    ==> allows setting error indicating background per line
  - allows opening better formatted (sub)dialog on double click ...
  - ... using field with list element class as type
- also allow specifying preferable list field separators
==> register string input datatypes as implementation of respective interface (or abstract class) providing all that functionality
==> GREAT IDEA, BUT SEPARATE ENDEAVOUR ... ALTOGETHER, AND WAY MORE GENERAL
	 */
	
	private static class UseBooleanPanel extends UseParamPanel {
		UseBooleanPanel(DocStyleEditor parent, String docStyleParamName, String label, String description, boolean selected) {
			super(parent, docStyleParamName, label, description, selected);
			this.add(this.useParam, BorderLayout.CENTER);
			
			this.useParam.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent fe) {
					notifyActivated();
				}
				public void focusLost(FocusEvent fe) {
					notifyDeactivated();
				}
			});
			this.useParam.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					if (isUpdating())
						return;
					notifyModified();
				}
			});
		}
//		String getValue() {
//			return (this.useParam.isSelected() ? "true" : "false");
//		}
		void setValue(String value) {
			this.useParam.setSelected("true".equals(value));
		}
		String getDisplayedValue() {
			return (this.useParam.isSelected() ? "true" : "false");
		}
//		void setDisplayedValue(String value) {
//			this.useParam.setSelected("true".equals(value));
//		}
		void updateDisplayState() {
			//	TODO anything we can do here ???
		}
		void setInputEnabled(boolean enabled) {
			this.useParam.setEnabled(enabled);
		}
		boolean isInputEnabled() {
			return this.useParam.isEnabled();
		}
		DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
			if (!this.useParam.isSelected() && !this.useParam.isDefaulted())
				return NO_DISPLAY_EXTENSION_GRAPHICS;
			if (this.docStyleParamName.endsWith(".bold") || this.docStyleParamName.endsWith("Bold"))
				return getFontStyleVisualizationGraphics(this.parent, page, ImWord.BOLD_ATTRIBUTE);
			if (this.docStyleParamName.endsWith(".italics") || this.docStyleParamName.endsWith("Italics"))
				return getFontStyleVisualizationGraphics(this.parent, page, ImWord.ITALICS_ATTRIBUTE);
			if (this.docStyleParamName.endsWith(".allCaps") || this.docStyleParamName.endsWith("AllCaps"))
				return getFontStyleVisualizationGraphics(this.parent, page, "allCaps");
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		}
	}
	
	private static abstract class UseStringPanel extends UseParamPanel implements FocusableParamPanel {
		JTextField string;
		UseStringPanel(DocStyleEditor parent, String docStyleParamName, String label, String description, boolean selected, String string, boolean escapePattern, boolean addTestButton, boolean scroll) {
			super(parent, docStyleParamName, label, description, selected);
			final String localDspn = this.docStyleParamName.substring(this.docStyleParamName.lastIndexOf('.') + ".".length());
			this.add(this.useParam, BorderLayout.WEST);
			
			if (localDspn.equals("pattern") || localDspn.endsWith("Pattern") || addTestButton) {
				if (escapePattern)
					string = buildPattern(string);
				JButton testButton = new JButton("Test");
				testButton.setBorder(BorderFactory.createRaisedBevelBorder());
				testButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						String pattern = UseStringPanel.this.string.getText().trim();
						if (pattern.length() == 0)
							return;
						if (localDspn.equals("linePattern") || localDspn.endsWith("LinePattern"))
							testLinePattern(pattern, getTestDoc());
						else testPattern(pattern, getTestDocTokens());
					}
				});
				this.add(testButton, BorderLayout.EAST);
				//	TODO add button opening GGE pattern editor in sub dialog (helps understand them suckers)
			}
			
			this.ownValue = string;
			this.string = new JTextField(string) {
				private int colWidth = -1;
				private int rowHeight = -1;
				public Dimension getPreferredSize() {
					if (this.colWidth == -1)
						this.colWidth = this.getFontMetrics(this.getFont()).charWidth('m');
					if (this.rowHeight == -1)
						this.rowHeight = this.getFontMetrics(this.getFont()).getHeight();
					Dimension ps = super.getPreferredSize();
					ps.width = ((this.getDocument().getLength() * this.colWidth) + this.getInsets().left + this.getInsets().right);
					ps.height = Math.max(ps.height, (this.rowHeight + this.getInsets().top + this.getInsets().bottom));
					return ps;
				}
				public void setFont(Font f) {
					super.setFont(f);
					this.colWidth = -1;
					this.rowHeight = -1;
				}
			};
			this.string.setFont(UIManager.getFont("TextArea.font")); // we want these to be the same ...
			this.string.setBorder(BorderFactory.createLoweredBevelBorder());
			this.string.setPreferredSize(new Dimension(Math.max(this.string.getWidth(), (this.string.getFont().getSize() * string.length())), this.string.getHeight()));
			this.string.addFocusListener(new FocusListener() {
//				private String oldValue = null;
				public void focusGained(FocusEvent fe) {
//					this.oldValue = UseStringPanel.this.string.getText().trim();
//					this.oldValue = UseStringPanel.this.ownValue;
//					notifyActivated();
					handleFocusGained();
				}
				public void focusLost(FocusEvent fe) {
//					String value = UseStringPanel.this.string.getText().trim();
//					String value = UseStringPanel.this.ownValue;
//					if (!value.equals(this.oldValue))
//						stringChanged(value);
//					this.oldValue = null;
//					notifyDeactivated();
					handleFocusLost();
				}
			});
			this.string.getDocument().addDocumentListener(new DocumentListener() {
				public void insertUpdate(DocumentEvent de) {
					if (isUpdating())
						return;
					ownValue = UseStringPanel.this.string.getText();
					notifyModified();
				}
				public void removeUpdate(DocumentEvent de) {
					if (isUpdating())
						return;
					ownValue = UseStringPanel.this.string.getText();
					notifyModified();
				}
				public void changedUpdate(DocumentEvent de) {}
			});
			this.string.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					if (isUpdating())
						return;
					if (me.getClickCount() < 2)
						return;
					selectDefaultSource();
				}
			});
			
			JComponent stringField = this.string;
			if (scroll) {
				JScrollPane stringBox = new JScrollPane(this.string) {
					public Dimension getPreferredSize() {
						Dimension ps = super.getPreferredSize();
						ps.height = (UseStringPanel.this.string.getPreferredSize().height + this.getHorizontalScrollBar().getPreferredSize().height + 5);
						return ps;
					}
				};
				stringBox.setViewportBorder(null);
				stringBox.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				stringBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
				stringField = stringBox;
			}
			this.add(stringField, BorderLayout.CENTER);
		}
		private String oldValue = null;
		public void handleFocusGained() {
//			this.oldValue = UseStringPanel.this.string.getText().trim();
			this.oldValue = this.ownValue;
			this.notifyActivated();
		}
		public void handleFocusLost() {
//			String value = UseStringPanel.this.string.getText().trim();
			String value = this.ownValue;
			if (!value.equals(this.oldValue))
				this.stringChanged(value);
			this.oldValue = null;
			this.notifyDeactivated();
		}
		ImTokenSequence getTestDocTokens() {
			return null;
		}
		ImDocument getTestDoc() {
			return null;
		}
		void setInputEnabled(boolean enabled) {
			this.useParam.setEnabled(enabled);
			this.string.setEnabled(enabled);
		}
		boolean isInputEnabled() {
			return this.useParam.isEnabled();
		}
//		String getValue() {
//			return (this.useParam.isDefaulted() ? "" : this.getDisplayedValue());
//		}
		void setValue(String value) {
			this.ownValue = value;
			this.string.setText(value);
			if (value.length() != 0)
				this.useParam.setSelected(true);
		}
		String getDisplayedValue() {
			return this.string.getText().trim();
		}
//		void setDisplayedValue(String value) {
//			this.string.setText(value);
//		}
		void updateDisplayState() {
			if (this.useParam.isSelected()) {
				this.string.setForeground(Color.BLACK);
				this.string.setEditable(true);
				this.string.setText(this.ownValue);
			}
			else if (this.useParam.isDefaulted()) {
				this.string.setForeground(Color.GRAY);
				this.string.setEditable(false);
				this.string.setText(this.useParam.defaultValue);
			}
			else {
				this.string.setForeground(Color.GRAY);
				this.string.setEditable(true);
				this.string.setText(this.ownValue);
			}
		}
		void stringChanged(String string) {}
	}
	
	private static String buildPattern(String string) {
		StringBuffer pString = new StringBuffer();
		for (int c = 0; c < string.length(); c++) {
			char ch = string.charAt(c);
			if ((ch < 33) || (ch == 160))
				pString.append("\\s*"); // turn all control characters into spaces, along with non-breaking space
			else if (ch < 127)
				pString.append((Character.isLetterOrDigit(ch) ? "" : "\\") + ch); // no need to normalize basic ASCII characters, nor escaping letters and digits
			else if ("-\u00AD\u2010\u2011\u2012\u2013\u2014\u2015\u2212".indexOf(ch) != -1)
				pString.append("\\-"); // normalize dashes right here
			else pString.append(StringUtils.getNormalForm(ch));
		}
		replace(pString, "\\s**", "\\s*");
		replace(pString, "\\s*\\s*", "\\s*");
		string = pString.toString();
		string = string.replaceAll("[1-9][0-9]*", "[1-9][0-9]*");
		return string;
	}
	
	private static void replace(StringBuffer sb, String toReplace, String replacement) {
		for (int s; (s = sb.indexOf(toReplace)) != -1;)
			sb.replace(s, (s + toReplace.length()), replacement);
	}
	
	private static void testPattern(String pattern, ImTokenSequence docTokens) {
		try {
			Annotation[] annotations = Gamta.extractAllMatches(docTokens, pattern, 64, false, false, false);
			if (annotations != null) {
				Window topWindow = DialogPanel.getTopWindow();
				AnnotationDisplayDialog add;
				if (topWindow instanceof JFrame)
					add = new AnnotationDisplayDialog(((JFrame) topWindow), "Matches of Pattern", annotations, true);
				else if (topWindow instanceof JDialog)
					add = new AnnotationDisplayDialog(((JDialog) topWindow), "Matches of Pattern", annotations, true);
				else add = new AnnotationDisplayDialog(((JFrame) null), "Matches of Pattern", annotations, true);
				add.setLocationRelativeTo(topWindow);
				add.setVisible(true);
			}
		}
		catch (PatternSyntaxException pse) {
			JOptionPane.showMessageDialog(DialogPanel.getTopWindow(), ("The pattern is not valid:\n" + pse.getMessage()), "Pattern Validation Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private static void testLinePattern(String pattern, ImDocument doc) {
		try {
			LinePattern lp = LinePattern.parsePattern(pattern);
			ImPage[] pages = doc.getPages();
			ArrayList matchLineAnnots = new ArrayList();
			for (int p = 0; p < pages.length; p++) {
				ImRegion[] matchLines = lp.getMatches(pages[p]);
				for (int l = 0; l < matchLines.length; l++) {
					ImDocumentRoot matchLineDoc = new ImDocumentRoot(matchLines[l], (ImDocumentRoot.NORMALIZATION_LEVEL_RAW | ImDocumentRoot.NORMALIZE_CHARACTERS));
					matchLineAnnots.add(matchLineDoc.addAnnotation(ImRegion.LINE_ANNOTATION_TYPE, 0, matchLineDoc.size()));
				}
			}
			Annotation[] annotations = ((Annotation[]) matchLineAnnots.toArray(new Annotation[matchLineAnnots.size()]));
			Window topWindow = DialogPanel.getTopWindow();
			AnnotationDisplayDialog add;
			if (topWindow instanceof JFrame)
				add = new AnnotationDisplayDialog(((JFrame) topWindow), "Matches of Line Pattern", annotations, true);
			else if (topWindow instanceof JDialog)
				add = new AnnotationDisplayDialog(((JDialog) topWindow), "Matches of Line Pattern", annotations, true);
			else add = new AnnotationDisplayDialog(((JFrame) null), "Matches of Line Pattern", annotations, true);
			add.setLocationRelativeTo(topWindow);
			add.setVisible(true);
		}
		catch (PatternSyntaxException pse) {
			JOptionPane.showMessageDialog(DialogPanel.getTopWindow(), ("The pattern is not valid:\n" + pse.getMessage()), "Pattern Validation Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private static class UseStringOptionPanel extends UseParamPanel implements FocusableParamPanel {
		JComboBox string;
		JTextComponent stringEditor;
		UseStringOptionPanel(DocStyleEditor parent, String docStyleParamName, String[] values, String[] valueLabels, String label, String description, boolean selected, String string, boolean escapePattern) {
			super(parent, docStyleParamName, label, description, selected);
			this.add(this.useParam, BorderLayout.WEST);
			
			StringOption[] options;
			if ((values.length != 0) && "".equals(values[0])) {
				options = new StringOption[values.length - 1];
				for (int v = 1; v < values.length; v++)
					options[v-1] = new StringOption(values[v], valueLabels[v]);
			}
			else {
				options = new StringOption[values.length];
				for (int v = 0; v < values.length; v++)
					options[v] = new StringOption(values[v], valueLabels[v]);
			}
			
			this.string = new JComboBox(options) {
				public void setPopupVisible(boolean v) {
					if (useParam.isDefaulted())
						return;
					super.setPopupVisible(v);
				}
			};
			this.ownValue = string;
			this.string.setEditable(options.length < values.length);
			this.string.setBorder(BorderFactory.createLoweredBevelBorder());
			this.string.setPreferredSize(new Dimension(Math.max(this.string.getWidth(), (this.string.getFont().getSize() * string.length())), this.string.getHeight()));
			this.setSelectedValue(string);
			this.string.addFocusListener(new FocusListener() {
//				private String oldValue = null;
				public void focusGained(FocusEvent fe) {
//					this.oldValue = UseStringOptionPanel.this.getValue();
//					this.oldValue = UseStringOptionPanel.this.ownValue;
//					notifyActivated();
					handleFocusGained();
				}
				public void focusLost(FocusEvent fe) {
//					String value = UseStringOptionPanel.this.getValue();
//					String value = UseStringOptionPanel.this.ownValue;
//					if (!value.equals(this.oldValue))
//						stringChanged(value);
//					this.oldValue = null;
//					notifyDeactivated();
					handleFocusLost();
				}
			});
			this.string.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					if (isUpdating())
						return;
					ownValue = UseStringOptionPanel.this.getValue();
					stringChanged(UseStringOptionPanel.this.getValue());
					notifyModified();
				}
			});
			this.string.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					if (isUpdating())
						return;
					if (me.getClickCount() < 2)
						return;
					selectDefaultSource();
				}
			});
			try {
				this.stringEditor = ((JTextComponent) this.string.getEditor().getEditorComponent());
			}
			catch (Exception e) {
				System.out.println("Error wiring combo box editor: " + e.getMessage());
				e.printStackTrace(System.out);
			}
			if (this.string.isEditable() && (this.stringEditor != null)) {
				this.stringEditor.getDocument().addDocumentListener(new DocumentListener() {
					public void insertUpdate(DocumentEvent de) {
						this.fireActionEventUnlessEmpty();
					}
					public void removeUpdate(DocumentEvent de) {
						this.fireActionEventUnlessEmpty();
					}
					private void fireActionEventUnlessEmpty() {
						if (isUpdating())
							return;
						String text = stringEditor.getText();
						if (text.length() != 0)
							UseStringOptionPanel.this.string.actionPerformed(new ActionEvent(stringEditor, ActionEvent.ACTION_PERFORMED, text, EventQueue.getMostRecentEventTime(), 0));
					}
					public void changedUpdate(DocumentEvent de) {}
				});
				this.stringEditor.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent me) {
						if (isUpdating())
							return;
						if (me.getClickCount() < 2)
							return;
						selectDefaultSource();
					}
				});
			}
			
			this.add(this.string, BorderLayout.CENTER);
		}
		private String oldValue = null;
		public void handleFocusGained() {
//			this.oldValue = UseStringOptionPanel.this.getValue();
			this.oldValue = this.ownValue;
			this.notifyActivated();
		}
		public void handleFocusLost() {
//			String value = UseStringOptionPanel.this.getValue();
			String value = this.ownValue;
			if (!value.equals(this.oldValue))
				this.stringChanged(value);
			this.oldValue = null;
			this.notifyDeactivated();
		}
		void setInputEnabled(boolean enabled) {
			this.useParam.setEnabled(enabled);
			this.string.setEnabled(enabled);
		}
		boolean isInputEnabled() {
			return this.useParam.isEnabled();
		}
//		String getValue() {
//			if (this.useParam.isDefaulted())
//				return "";
//			return this.getDisplayedValue();
//		}
		void setValue(String value) {
			this.ownValue = value;
			this.setSelectedValue(value);
			if (value.length() != 0)
				this.useParam.setSelected(true);
		}
		String getDisplayedValue() {
			Object vObj = this.string.getSelectedItem();
			if (vObj == null)
				return "";
			else if (vObj instanceof StringOption)
				return ((StringOption) vObj).value;
			else return vObj.toString();
		}
//		void setDisplayedValue(String value) {
//			this.setSelectedValue(value);
//		}
		private void setSelectedValue(String value) {
			if (this.string.isEditable()) {
				int vIndex = -1;
				for (int i = 0; i < this.string.getItemCount(); i++) {
					Object vObj = this.string.getItemAt(i);
					if ((vObj instanceof StringOption) && ((StringOption) vObj).value.equals(value)) {
						vIndex = i;
						break;
					}
				}
				if (vIndex == -1)
					this.string.setSelectedItem(value);
				else this.string.setSelectedIndex(vIndex);
			}
			else this.string.setSelectedItem(new StringOption(value, null));
		}
		void updateDisplayState() {
			if (this.useParam.isSelected()) {
				this.string.setForeground(Color.BLACK);
				if (this.stringEditor != null)
					this.stringEditor.setEditable(true);
				this.setSelectedValue(this.ownValue);
			}
			else if (this.useParam.isDefaulted()) {
				this.string.setForeground(Color.GRAY);
				if (this.stringEditor != null)
					this.stringEditor.setEditable(false);
				this.setSelectedValue(this.useParam.defaultValue);
			}
			else {
				this.string.setForeground(Color.GRAY);
				if (this.stringEditor != null)
					this.stringEditor.setEditable(true);
				this.setSelectedValue(this.ownValue);
			}
		}
		void stringChanged(String string) {}
		DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
			return NO_DISPLAY_EXTENSION_GRAPHICS; // no way of visualizing choice generically
		}
		private static class StringOption {
			final String value;
			final String label;
			StringOption(String value, String label) {
				this.value = value;
				this.label = ((label == null) ? value : label);
			}
			public boolean equals(Object obj) {
				return ((obj instanceof StringOption) && this.value.equals(((StringOption) obj).value));
			}
			public String toString() {
				return this.label; // need to show the label
			}
		}
	}
	
	private static abstract class UseListPanel extends UseParamPanel implements FocusableParamPanel {
		JTextArea list;
		UseListPanel(DocStyleEditor parent, String docStyleParamName, String label, String description, boolean selected, String string, boolean addTestButton, boolean scroll) {
			super(parent, docStyleParamName, label, description, selected);
			final String localDspn = this.docStyleParamName.substring(this.docStyleParamName.lastIndexOf('.') + ".".length());
			this.add(this.useParam, BorderLayout.WEST);
			
			if (localDspn.equals("patterns") || localDspn.endsWith("Patterns") || addTestButton) {
				JButton testButton = new JButton("Test");
				testButton.setBorder(BorderFactory.createRaisedBevelBorder());
				testButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						String pattern = UseListPanel.this.list.getSelectedText();
						if (pattern == null)
							return;
						pattern = pattern.trim();
						if (pattern.length() == 0)
							return;
						if (localDspn.equals("linePatterns") || localDspn.endsWith("LinePatterns"))
							testLinePattern(pattern, getTestDoc());
						else testPattern(pattern, getTestDocTokens());
					}
				});
				this.add(testButton, BorderLayout.EAST);
				//	TODO add button opening GGE pattern editor in sub dialog (helps understand them suckers)
			}
			
			this.list = new JTextArea((string == null) ? "" : string.trim().replaceAll("\\s+", "\r\n"));
			this.ownValue = this.list.getText();
			this.list.setBorder(BorderFactory.createLoweredBevelBorder());
			this.list.addFocusListener(new FocusListener() {
//				private String oldValue = null;
				public void focusGained(FocusEvent fe) {
//					this.oldValue = UseListPanel.this.list.getText().trim();
//					this.oldValue = UseListPanel.this.ownValue;
//					notifyActivated();
					handleFocusGained();
				}
				public void focusLost(FocusEvent fe) {
//					String value = UseListPanel.this.list.getText().trim();
//					String value = UseListPanel.this.ownValue;
//					if (!value.equals(this.oldValue))
//						stringChanged(value);
//					this.oldValue = null;
//					notifyDeactivated();
					handleFocusLost();
				}
			});
			this.list.getDocument().addDocumentListener(new DocumentListener() {
				int listLineCount = UseListPanel.this.list.getLineCount();
				public void insertUpdate(DocumentEvent de) {
					int listLineCount = UseListPanel.this.list.getLineCount();
					if (listLineCount != this.listLineCount) {
						this.listLineCount = listLineCount;
						if (UseListPanel.this.parent != null) {
							UseListPanel.this.parent.paramPanel.validate();
							UseListPanel.this.parent.paramPanel.repaint();
						}
					}
					if (isUpdating())
						return;
					ownValue = UseListPanel.this.list.getText();
					notifyModified();
				}
				public void removeUpdate(DocumentEvent de) {
					int listLineCount = UseListPanel.this.list.getLineCount();
					if (listLineCount != this.listLineCount) {
						this.listLineCount = listLineCount;
						if (UseListPanel.this.parent != null) {
							UseListPanel.this.parent.paramPanel.validate();
							UseListPanel.this.parent.paramPanel.repaint();
						}
					}
					if (isUpdating())
						return;
					ownValue = UseListPanel.this.list.getText();
					notifyModified();
				}
				public void changedUpdate(DocumentEvent de) {}
			});
			this.list.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					if (isUpdating())
						return;
					if (me.getClickCount() < 2)
						return;
					selectDefaultSource();
				}
			});
			
			JComponent listField = this.list;
			if (scroll) {
				JScrollPane listBox = new JScrollPane(this.list) {
					public Dimension getPreferredSize() {
						Dimension ps = super.getPreferredSize();
						ps.height = (UseListPanel.this.list.getPreferredSize().height + this.getHorizontalScrollBar().getPreferredSize().height + 5);
						return ps;
					}
				};
				listBox.setViewportBorder(null);
				listBox.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				listBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
				listField = listBox;
			}
			this.add(listField, BorderLayout.CENTER);
		}
		private String oldValue = null;
		public void handleFocusGained() {
//			this.oldValue = UseListPanel.this.list.getText().trim();
			this.oldValue = this.ownValue;
			this.notifyActivated();
		}
		public void handleFocusLost() {
//			String value = UseListPanel.this.list.getText().trim();
			String value = this.ownValue;
			if (!value.equals(this.oldValue))
				this.stringChanged(value);
			this.oldValue = null;
			this.notifyDeactivated();
		}
		ImTokenSequence getTestDocTokens() {
			return null;
		}
		ImDocument getTestDoc() {
			return null;
		}
		void setInputEnabled(boolean enabled) {
			this.useParam.setEnabled(enabled);
			this.list.setEnabled(enabled);
		}
		boolean isInputEnabled() {
			return this.useParam.isEnabled();
		}
//		String getValue() {
//			return (this.useParam.isDefaulted() ? "" : this.list.getText().trim().replaceAll("\\s+", " "));
//		}
		void setValue(String value) {
			value = value.replaceAll("\\s+", "\r\n");
			this.ownValue = value;
			this.list.setText(value);
			if (value.length() != 0)
				this.useParam.setSelected(true);
		}
		String getDisplayedValue() {
			return this.list.getText().trim().replaceAll("\\s+", " ");
		}
//		void setDisplayedValue(String value) {
//			value = value.replaceAll("\\s+", "\r\n");
//			this.list.setText(value);
//		}
		void updateDisplayState() {
			if (this.useParam.isSelected()) {
				this.list.setForeground(Color.BLACK);
				this.list.setEditable(true);
				this.list.setText(this.ownValue);
			}
			else if (this.useParam.isDefaulted()) {
				this.list.setForeground(Color.GRAY);
				this.list.setEditable(false);
				this.list.setText(this.useParam.defaultValue.replaceAll("\\s+", "\r\n"));
			}
			else {
				this.list.setForeground(Color.GRAY);
				this.list.setEditable(true);
				this.list.setText(this.ownValue);
			}
		}
		void stringChanged(String string) {}
	}
	
	private void useBoundingBox(ImDocument doc, int pageId, DocStyleSettings docStyle, String docStyleParamName, BoundingBox bounds) {
		Class paramValueClass = this.getParamValueClass(docStyleParamName);
		
		/* TODO Handling inheritance in document style editing (especially in "Use ..." functions):
		 * - provide "Use In" drop-down with whole inheritance chain selectable ...
		 * - ... and style name of currently selected tab pre-selected
		 */
		
		//	get editing target
		DocStyleSettings targetDocStyle = docStyle;
		String targetDocStyleId = docStyle.id;
		if ((this.docStyleEditor != null) && (this.docStyleEditor.selectedDocStyle != null)) {
			targetDocStyle = this.docStyleEditor.selectedDocStyle;
			targetDocStyleId = this.docStyleEditor.selectedDocStyle.id;
		}
		
		//	single bounding box, expand
		if (BoundingBox.class.getName().equals(paramValueClass.getName())) {
			BoundingBox eBounds = BoundingBox.parse(targetDocStyle.data.getSetting(docStyleParamName));
			if (eBounds == null) {
				docStyle.setSetting(targetDocStyleId, docStyleParamName, bounds.toString());
				if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + bounds.toString());
			}
			else {
				docStyle.setSetting(targetDocStyleId, docStyleParamName, this.aggregateBoxes(eBounds, bounds).toString());
				if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + this.aggregateBoxes(eBounds, bounds).toString());
			}
		}
		
		//	list of bounding boxes, add new one and merge ones overlapping at least 90%
		else if (BoundingBox.class.getName().equals(DocumentStyle.getListElementClass(paramValueClass).getName())) {
			String boundsStr = this.getBoxListString(targetDocStyle.data.getSetting(docStyleParamName), bounds);
			docStyle.setSetting(targetDocStyleId, docStyleParamName, boundsStr);
			if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + boundsStr);
		}
		
		//	we have an anchor, adjust minimum page ID
		if (docStyleParamName.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
			int maxPageId = Integer.parseInt(targetDocStyle.data.getSetting((Anchor.ANCHOR_PREFIX + ".maxPageId"), "0"));
			if (maxPageId < pageId)
				docStyle.setSetting(targetDocStyleId, (Anchor.ANCHOR_PREFIX + ".maxPageId"), ("" + (pageId + doc.getFirstPageId())));
		}
		
		//	if style editor open, adjust tree path
		if (this.docStyleEditor != null) {
			this.docStyleEditor.setParamGroupName(docStyleParamName.substring(0, docStyleParamName.lastIndexOf('.')));
//			this.docStyleEditor.setDocStyleDirty(true);
			this.docStyleEditor.checkDocStyleDirty();
		}
//		
//		//	index document style for saving
//		this.docStylesByDocId.put(doc.docId, docStyle);
	}
	
	private double getBoxOverlap(BoundingBox bb1, BoundingBox bb2) {
		if (bb1.includes(bb2, false) || bb2.includes(bb1, false))
			return 1;
		if (!bb1.overlaps(bb2))
			return 0;
		int iLeft = Math.max(bb1.left, bb2.left);
		int iRight = Math.min(bb1.right, bb2.right);
		int iTop = Math.max(bb1.top, bb2.top);
		int iBottom = Math.min(bb1.bottom, bb2.bottom);
		int iArea = ((iRight - iLeft) * (iBottom - iTop));
		int minBbArea = Math.min(((bb1.right - bb1.left) * (bb1.bottom - bb1.top)), ((bb2.right - bb2.left) * (bb2.bottom - bb2.top)));
		return (((double) iArea) / minBbArea);
	}
	
	private BoundingBox aggregateBoxes(BoundingBox bb1, BoundingBox bb2) {
		int left = Math.min(bb1.left, bb2.left);
		int right = Math.max(bb1.right, bb2.right);
		int top = Math.min(bb1.top, bb2.top);
		int bottom = Math.max(bb1.bottom, bb2.bottom);
		return new BoundingBox(left, right, top, bottom);
	}
	
	private String getBoxListString(String eBoundsStr, BoundingBox bounds) {
		if ((eBoundsStr == null) || (eBoundsStr.trim().length() == 0))
			return bounds.toString();
		
		ArrayList boundsList = new ArrayList();
		boundsList.add(bounds);
		String[] eBoundsStrs = eBoundsStr.split("[^0-9\\,\\[\\]]+");
		for (int b = 0; b < eBoundsStrs.length; b++)
			boundsList.add(BoundingBox.parse(eBoundsStrs[b]));
		
		int boundsCount;
		do {
			boundsCount = boundsList.size();
			BoundingBox bb1 = null;
			BoundingBox bb2 = null;
			double bbOverlap = 0.9; // 90% is minimum overlap for merging
			for (int b = 0; b < boundsList.size(); b++) {
				BoundingBox tbb1 = ((BoundingBox) boundsList.get(b));
				if (DEBUG_STYLE_UPDATES) System.out.println("Testing for merger: " + tbb1);
				for (int c = (b+1); c < boundsList.size(); c++) {
					BoundingBox tbb2 = ((BoundingBox) boundsList.get(c));
					double tbbOverlap = this.getBoxOverlap(tbb1, tbb2);
					if (DEBUG_STYLE_UPDATES) System.out.println(" - overlap with " + tbb2 + " is " + tbbOverlap);
					if (bbOverlap < tbbOverlap) {
						bbOverlap = tbbOverlap;
						bb1 = tbb1;
						bb2 = tbb2;
						if (DEBUG_STYLE_UPDATES) System.out.println(" ==> new best merger");
					}
				}
			}
			if ((bb1 != null) && (bb2 != null)) {
				boundsList.remove(bb1);
				boundsList.remove(bb2);
				boundsList.add(this.aggregateBoxes(bb1, bb2));
			}
		}
		while (boundsList.size() < boundsCount);
		
		StringBuffer boundsStr = new StringBuffer();
		for (int b = 0; b < boundsList.size(); b++) {
			if (b != 0)
				boundsStr.append(' ');
			boundsStr.append(((BoundingBox) boundsList.get(b)).toString());
		}
		return boundsStr.toString();
	}
	
	private void useSelection(final ImDocument doc, int pageId, DocStyleSettings docStyle, String docStyleParamGroupName, String[] docStyleParamNames, int minFontSize, int maxFontSize, boolean isBold, boolean isItalics, boolean isAllCaps, String string, BoundingBox bounds) {
		JPanel sPanel = new JPanel(new GridLayout(0, 1, 0, 0), true);
		
		//	get parameter group description and group label
		ParameterGroupDescription pgd = this.getParameterGroupDescription(docStyleParamGroupName);
		String pgl = ((pgd == null) ? null : pgd.getLabel());
		
		//	if creating anchor, add name field at top, pre-filled with string less all non-letters
		JTextField createAnchorName = null;
		if ((Anchor.ANCHOR_PREFIX + ".<create>").equals(docStyleParamGroupName)) {
			createAnchorName = new JTextField(normalizeString(string).replaceAll("[^A-Za-z]", ""));
			JPanel caPanel = new JPanel(new BorderLayout(), true);
			caPanel.add(new JLabel(" Anchor Name: "), BorderLayout.WEST);
			caPanel.add(createAnchorName, BorderLayout.CENTER);
			sPanel.add(caPanel);
		}
		
		//	ask for font properties to use
		UseBooleanPanel useMinFontSize = null;
		UseBooleanPanel useMaxFontSize = null;
		if (minFontSize <= maxFontSize) {
			int eMinFontSize = 72;
			try {
				eMinFontSize = Integer.parseInt(docStyle.data.getSetting((docStyleParamGroupName + ".minFontSize"), "72"));
			} catch (NumberFormatException nfe) {}
			if (minFontSize < eMinFontSize) {
				String pl;
				if (pgl == null)
					pl = ("Use " + minFontSize + " as Minimum Font Size (currently " + eMinFontSize + ")");
				else pl = ("Use " + minFontSize + " as Minimum Font Size for " + pgl + " (currently " + eMinFontSize + ")");
				String pd = ((pgd == null) ? null : pgd.getParamDescription("minFontSize"));
				useMinFontSize = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".minFontSize"), pl, pd, true);
				sPanel.add(useMinFontSize);
			}
			int eMaxFontSize = 0;
			try {
				eMaxFontSize = Integer.parseInt(docStyle.data.getSetting((docStyleParamGroupName + ".maxFontSize"), "0"));
			} catch (NumberFormatException nfe) {}
			if (eMaxFontSize < maxFontSize) {
				String pl;
				if (pgl == null)
					pl = ("Use " + maxFontSize + " as Maximum Font Size (currently " + eMaxFontSize + ")");
				else pl = ("Use " + maxFontSize + " as Maximum Font Size for " + pgl + " (currently " + eMaxFontSize + ")");
				String pd = ((pgd == null) ? null : pgd.getParamDescription("minFontSize"));
				useMaxFontSize = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".maxFontSize"), pl, pd, true);
				sPanel.add(useMaxFontSize);
			}
		}
		UseBooleanPanel useIsBold = null;
		if (isBold) {
			for (int p = 0; p < docStyleParamNames.length; p++) {
				if (docStyleParamGroupName.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
					useIsBold = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".isBold"), "Require Anchor Value to be Bold", null, true);
					break;
				}
				else if (docStyleParamNames[p].equals(docStyleParamGroupName + ".isBold")) {
					String pl;
					if (pgl == null)
						pl = "Require Values to be Bold";
					else pl = ("Require Values for " + pgl + " to be Bold");
					String pd = ((pgd == null) ? null : pgd.getParamDescription("isBold"));
					useIsBold = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".isBold"), pl, pd, true);
					break;
				}
				else if (docStyleParamNames[p].equals(docStyleParamGroupName + ".startIsBold")) {
					String pl;
					if (pgl == null)
						pl = "Require Value Starts to be Bold";
					else pl = ("Require Values for " + pgl + " to Start in Bold");
					String pd = ((pgd == null) ? null : pgd.getParamDescription("startIsBold"));
					useIsBold = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".startIsBold"), pl, pd, true);
					break;
				}
			}
			if (useIsBold != null)
				sPanel.add(useIsBold);
		}
		UseBooleanPanel useIsItalics = null;
		if (isItalics) {
			for (int p = 0; p < docStyleParamNames.length; p++) {
				if (docStyleParamGroupName.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
					useIsItalics = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".isItalics"), "Require Anchor Value to be in Italics", null, true);
					break;
				}
				else if (docStyleParamNames[p].equals(docStyleParamGroupName + ".isItalics")) {
					String pl;
					if (pgl == null)
						pl = "Require Values to be in Italics";
					else pl = ("Require Values for " + pgl + " to be in Italics");
					String pd = ((pgd == null) ? null : pgd.getParamDescription("isItalics"));
					useIsItalics = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".isItalics"), pl, pd, true);
					break;
				}
				else if (docStyleParamNames[p].equals(docStyleParamGroupName + ".startIsItalics")) {
					String pl;
					if (pgl == null)
						pl = "Require Values Starts to be in Italics";
					else pl = ("Require Values for " + pgl + " to Start in Italics");
					String pd = ((pgd == null) ? null : pgd.getParamDescription("startIsItalics"));
					useIsItalics = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".startIsItalics"), pl, pd, true);
					break;
				}
			}
			if (useIsItalics != null)
				sPanel.add(useIsItalics);
		}
		UseBooleanPanel useIsAllCaps = null;
		if (isAllCaps) {
			for (int p = 0; p < docStyleParamNames.length; p++) {
				if (docStyleParamGroupName.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
					useIsAllCaps = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".isAllCaps"), "Require Anchor Value to be in All Caps", null, true);
					break;
				}
				if (docStyleParamNames[p].equals(docStyleParamGroupName + ".isAllCaps")) {
					String pl;
					if (pgl == null)
						pl = "Require Values to be in All Caps";
					else pl = ("Require Values for " + pgl + " to be in All Caps");
					String pd = ((pgd == null) ? null : pgd.getParamDescription("isAllCaps"));
					useIsAllCaps = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".isAllCaps"), pl, pd, true);
					break;
				}
				else if (docStyleParamNames[p].equals(docStyleParamGroupName + ".startIsAllCaps")) {
					String pl;
					if (pgl == null)
						pl = "Require Values Starts to be in All Caps";
					else pl = ("Require Values for " + pgl + " to Start in All Caps");
					String pd = ((pgd == null) ? null : pgd.getParamDescription("startIsAllCaps"));
					useIsAllCaps = new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".startIsAllCaps"), pl, pd, true);
					break;
				}
			}
			if (useIsAllCaps != null)
				sPanel.add(useIsAllCaps);
		}
		
		//	collect style parameter names in argument group that use string properties, constructing string usage panels on the fly
		TreeSet sDocStyleParamPanels = new TreeSet();
		final ImTokenSequence[] docTokens = {null}; // using array facilitates sharing tokens and still generating them on demand
		for (int p = 0; p < docStyleParamNames.length; p++) {
			if (!docStyleParamNames[p].startsWith(docStyleParamGroupName + "."))
				continue;
			if (!checkParamValueClass(docStyleParamNames[p], String.class, true) && !checkParamValueClass(docStyleParamNames[p], Pattern.class, true))
				continue;
			String localDspn = docStyleParamNames[p].substring(docStyleParamNames[p].lastIndexOf('.') + ".".length());
			String pl = ((pgd == null) ? null : pgd.getParamLabel(localDspn));
			if (pl == null)
				pl = (" Use as " + localDspn);
			else pl = (" Use as " + pl);
			String pd = ((pgd == null) ? null : pgd.getParamDescription(localDspn));
			sDocStyleParamPanels.add(new UseStringPanel(this.docStyleEditor, docStyleParamNames[p], pl, pd, true, string, true, checkParamValueClass(docStyleParamNames[p], Pattern.class, false), true) {
				ImTokenSequence getTestDocTokens() {
					if (docTokens[0] == null)
						docTokens[0] = new ImTokenSequence(((Tokenizer) doc.getAttribute(ImDocument.TOKENIZER_ATTRIBUTE, Gamta.INNER_PUNCTUATION_TOKENIZER)), doc.getTextStreamHeads(), (ImTokenSequence.NORMALIZATION_LEVEL_PARAGRAPHS | ImTokenSequence.NORMALIZE_CHARACTERS));
					return docTokens[0];
				}
				ImDocument getTestDoc() {
					return doc;
				}
				DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
					if (this.docStyleParamName.endsWith(".linePattern") || this.docStyleParamName.endsWith("LinePattern"))
						return getLinePatternVisualizationGraphics(this.parent, page, this.getDisplayedValue());
					if (this.docStyleParamName.endsWith(".pattern") || this.docStyleParamName.endsWith("Pattern"))
						return getPatternVisualizationGraphics(this.parent, page, this.getDisplayedValue());
					//	TODO think of more
					return NO_DISPLAY_EXTENSION_GRAPHICS;
				}
			});
		}
		if (docStyleParamGroupName.equals(Anchor.ANCHOR_PREFIX + ".<create>") && sDocStyleParamPanels.isEmpty()) {
			sDocStyleParamPanels.add(new UseStringPanel(this.docStyleEditor, (docStyleParamGroupName + "." + PageFeatureAnchor.TARGET_PATTERN_PROPERTY), "Use as Anchor Value Pattern", null, true, string, true, true, false) {
				ImTokenSequence getTestDocTokens() {
					if (docTokens[0] == null)
						docTokens[0] = new ImTokenSequence(((Tokenizer) doc.getAttribute(ImDocument.TOKENIZER_ATTRIBUTE, Gamta.INNER_PUNCTUATION_TOKENIZER)), doc.getTextStreamHeads(), (ImTokenSequence.NORMALIZATION_LEVEL_PARAGRAPHS | ImTokenSequence.NORMALIZE_CHARACTERS));
					return docTokens[0];
				}
				ImDocument getTestDoc() {
					return doc;
				}
				DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
					return getPatternVisualizationGraphics(this.parent, page, this.getDisplayedValue());
				}
			});
		}
		for (Iterator ppit = sDocStyleParamPanels.iterator(); ppit.hasNext();)
			sPanel.add((UseStringPanel) ppit.next());
		
		//	collect style parameter names in argument group that use bounding box properties, constructing checkboxes on the fly
		TreeSet bbDocStyleParamPanels = new TreeSet();
		for (int p = 0; p < docStyleParamNames.length; p++)
			if (docStyleParamNames[p].startsWith(docStyleParamGroupName + ".")) {
				String localDspn = docStyleParamNames[p].substring(docStyleParamNames[p].lastIndexOf('.') + ".".length());
				if (!docStyleParamNames[p].equals(docStyleParamGroupName + "." + localDspn))
					continue;
				if (!checkParamValueClass(docStyleParamNames[p], BoundingBox.class, true))
					continue;
				String pl;
				if (pgl == null)
					pl = ("Use Bounding Box as " + localDspn);
				else pl = ("Use Bounding Box as " + pgl);
				String pd = ((pgd == null) ? null : pgd.getParamDescription(localDspn));
				bbDocStyleParamPanels.add(new UseBooleanPanel(this.docStyleEditor, docStyleParamNames[p], pl, pd, true));
			}
		for (Iterator pnit = bbDocStyleParamPanels.iterator(); pnit.hasNext();)
			sPanel.add((UseParamPanel) pnit.next());
		if (docStyleParamGroupName.equals(Anchor.ANCHOR_PREFIX + ".<create>") && bbDocStyleParamPanels.isEmpty()) // an anchor always requires a bounding box, so we don't display the checkbox, but simply use it
			bbDocStyleParamPanels.add(new UseBooleanPanel(this.docStyleEditor, (docStyleParamGroupName + ".area"), "Require Anchor Value to be inside Bounding Box", null, true));
		
		//	add target style selector if required
		DocStyleSettings[] docStyleChain = docStyle.getInheritanceChain();
		JComboBox targetDocStyleSelector = null;
		if ((docStyleChain.length > 1) && (this.docStyleEditor != null)) {
			targetDocStyleSelector = new JComboBox(docStyleChain);
			targetDocStyleSelector.setSelectedItem((this.docStyleEditor.selectedDocStyle == null) ? docStyleChain[0] : this.docStyleEditor.selectedDocStyle);
			JPanel tdsPanel = new JPanel(new BorderLayout(), true);
			tdsPanel.add(new JLabel("Add to document style: "), BorderLayout.WEST);
			tdsPanel.add(targetDocStyleSelector, BorderLayout.CENTER);
			sPanel.add(tdsPanel);
		}
		
		//	prompt
		int choice = JOptionPane.showConfirmDialog(null, sPanel, ("Select Properties to Use" + ((pgl == null) ? "" : (" in " + pgl))), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (choice != JOptionPane.OK_OPTION)
			return;
		
		//	get target document style source ID
		DocStyleSettings targetDocStyle;
		String targetDocStyleId;
		if (targetDocStyleSelector == null) {
			targetDocStyle = docStyle;
			targetDocStyleId = docStyle.id;
		}
		else {
			targetDocStyle = ((DocStyleSettings) targetDocStyleSelector.getSelectedItem());
			targetDocStyleId = targetDocStyle.id;
		}
		
		//	if we're creating an anchor, determine lowest non-used anchor number and use that as parameter group name
		if ((Anchor.ANCHOR_PREFIX + ".<create>").equals(docStyleParamGroupName)) {
			String can = createAnchorName.getText().replaceAll("[^A-Za-z0-9]", "");
			if (can.length() == 0)
				return;
			docStyleParamGroupName = (Anchor.ANCHOR_PREFIX + "." + can);
			int maxPageId = Integer.parseInt(targetDocStyle.data.getSetting((Anchor.ANCHOR_PREFIX + ".maxPageId"), "0"));
			if (maxPageId < pageId)
				docStyle.setSetting(targetDocStyleId, (Anchor.ANCHOR_PREFIX + ".maxPageId"), ("" + (pageId + doc.getFirstPageId())));
		}
		
		//	we have an anchor, adjust minimum page ID
		else if (docStyleParamGroupName.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
			int maxPageId = Integer.parseInt(targetDocStyle.data.getSetting((Anchor.ANCHOR_PREFIX + ".maxPageId"), "0"));
			if (maxPageId < pageId)
				docStyle.setSetting(targetDocStyleId, (Anchor.ANCHOR_PREFIX + ".maxPageId"), ("" + (pageId + doc.getFirstPageId())));
		}
		
		//	set font properties
		if ((useMinFontSize != null) && useMinFontSize.useParam.isSelected()) {
			docStyle.setSetting(targetDocStyleId, (docStyleParamGroupName + ".minFontSize"), ("" + minFontSize));
			if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamGroupName + ".minFontSize set to " + minFontSize);
		}
		if ((useMaxFontSize != null) && useMaxFontSize.useParam.isSelected()) {
			docStyle.setSetting(targetDocStyleId, (docStyleParamGroupName + ".maxFontSize"), ("" + maxFontSize));
			if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamGroupName + ".maxFontSize set to " + maxFontSize);
		}
		if ((minFontSize == maxFontSize) && (useMinFontSize != null) && useMinFontSize.useParam.isSelected() && (useMaxFontSize != null) && useMaxFontSize.useParam.isSelected()) {
			docStyle.setSetting(targetDocStyleId, (docStyleParamGroupName + ".fontSize"), ("" + minFontSize));
			if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamGroupName + ".fontSize set to " + minFontSize);
		}
		if ((useIsBold != null) && useIsBold.useParam.isSelected()) {
			docStyle.setSetting(targetDocStyleId, (docStyleParamGroupName + useIsBold.docStyleParamName.substring(useIsBold.docStyleParamName.lastIndexOf('.'))), "true");
			if (DEBUG_STYLE_UPDATES) System.out.println((docStyleParamGroupName + useIsBold.docStyleParamName.substring(useIsBold.docStyleParamName.lastIndexOf('.'))) + " set to true");
		}
		if ((useIsItalics != null) && useIsItalics.useParam.isSelected()) {
			docStyle.setSetting(targetDocStyleId, (docStyleParamGroupName + useIsItalics.docStyleParamName.substring(useIsItalics.docStyleParamName.lastIndexOf('.'))), "true");
			if (DEBUG_STYLE_UPDATES) System.out.println((docStyleParamGroupName + useIsItalics.docStyleParamName.substring(useIsItalics.docStyleParamName.lastIndexOf('.'))) + " set to true");
		}
		if ((useIsAllCaps != null) && useIsAllCaps.useParam.isSelected()) {
			docStyle.setSetting(targetDocStyleId, (docStyleParamGroupName + useIsAllCaps.docStyleParamName.substring(useIsAllCaps.docStyleParamName.lastIndexOf('.'))), "true");
			if (DEBUG_STYLE_UPDATES) System.out.println((docStyleParamGroupName + useIsAllCaps.docStyleParamName.substring(useIsAllCaps.docStyleParamName.lastIndexOf('.'))) + " set to true");
		}
		
		//	set string parameters
		for (Iterator ppit = sDocStyleParamPanels.iterator(); ppit.hasNext();) {
			UseStringPanel usp = ((UseStringPanel) ppit.next());
			if (!usp.useParam.isSelected())
				continue;
			string = usp.string.getText().trim();
			if (string.length() == 0)
				continue;
			
			String docStyleParamName = usp.docStyleParamName;
			if ((Anchor.ANCHOR_PREFIX + ".<create>." + PageFeatureAnchor.TARGET_PATTERN_PROPERTY).equals(usp.docStyleParamName))
				docStyleParamName = (docStyleParamGroupName + ".pattern");
			Class paramValueClass = this.getParamValueClass(docStyleParamName);
			
			if (String.class.getName().equals(paramValueClass.getName()) || Pattern.class.getName().equals(paramValueClass.getName())) {
				docStyle.setSetting(targetDocStyleId, docStyleParamName, string);
				if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + string);
			}
			else if (String.class.getName().equals(DocumentStyle.getListElementClass(paramValueClass).getName()) || Pattern.class.getName().equals(DocumentStyle.getListElementClass(paramValueClass).getName())) {
				String eString = targetDocStyle.data.getSetting(docStyleParamName, "").trim();
				if (eString.length() == 0) {
					docStyle.setSetting(targetDocStyleId, docStyleParamName, string);
					if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + string);
				}
				else {
					TreeSet eStringSet = new TreeSet(Arrays.asList(eString.split("\\s+")));
					eStringSet.add(string);
					StringBuffer eStringsStr = new StringBuffer();
					for (Iterator sit = eStringSet.iterator(); sit.hasNext();) {
						eStringsStr.append((String) sit.next());
						if (sit.hasNext())
							eStringsStr.append(' ');
					}
					docStyle.setSetting(targetDocStyleId, docStyleParamName, eStringsStr.toString());
					if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + eStringsStr.toString());
				}
			}
		}
		
		//	set bounding box properties
		for (Iterator bbdspnit = bbDocStyleParamPanels.iterator(); bbdspnit.hasNext();) {
			UseBooleanPanel useBbDsp = ((UseBooleanPanel) bbdspnit.next());
			if (!useBbDsp.useParam.isSelected())
				continue;
			
			String docStyleParamName = useBbDsp.docStyleParamName;
			if ((Anchor.ANCHOR_PREFIX + ".<create>." + PageFeatureAnchor.TARGET_AREA_PROPERTY).equals(docStyleParamName))
				docStyleParamName = (docStyleParamGroupName + ".area");
			Class paramValueClass = this.getParamValueClass(docStyleParamName);
			
			//	single bounding box, expand
			if (BoundingBox.class.getName().equals(paramValueClass.getName())) {
				BoundingBox eBounds = BoundingBox.parse(targetDocStyle.data.getSetting(docStyleParamName));
				if (eBounds == null) {
					docStyle.setSetting(targetDocStyleId, docStyleParamName, bounds.toString());
					if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + bounds.toString());
				}
				else {
					docStyle.setSetting(targetDocStyleId, docStyleParamName, this.aggregateBoxes(eBounds, bounds).toString());
					if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + this.aggregateBoxes(eBounds, bounds).toString());
				}
			}
			
			//	list of bounding boxes, add new one and merge ones overlapping at least 90%
			else if (BoundingBox.class.getName().equals(DocumentStyle.getListElementClass(paramValueClass).getName())) {
				String boundsStr = this.getBoxListString(targetDocStyle.data.getSetting(docStyleParamName), bounds);
				docStyle.setSetting(targetDocStyleId, docStyleParamName, boundsStr);
				if (DEBUG_STYLE_UPDATES) System.out.println(docStyleParamName + " set to " + boundsStr);
			}
		}
		
		//	if style editor open, adjust tree path
		if (this.docStyleEditor != null) {
			this.docStyleEditor.setParamGroupName(docStyleParamGroupName);
			if (targetDocStyleSelector != null)
				this.docStyleEditor.selectDocStyle((DocStyleSettings) targetDocStyleSelector.getSelectedItem());
//			this.docStyleEditor.setDocStyleDirty(true);
			this.docStyleEditor.checkDocStyleDirty();
		}
	}
	
	private static String normalizeString(String string) {
		StringBuffer nString = new StringBuffer();
		for (int c = 0; c < string.length(); c++) {
			char ch = string.charAt(c);
			if ((ch < 33) || (ch == 160))
				nString.append(" "); // turn all control characters into spaces, along with non-breaking space
			else if (ch < 127)
				nString.append(ch); // no need to normalize basic ASCII characters
			else if ("-\u00AD\u2010\u2011\u2012\u2013\u2014\u2015\u2212".indexOf(ch) != -1)
				nString.append("-"); // normalize dashes right here
			else nString.append(StringUtils.getNormalForm(ch));
		}
		return nString.toString();
	}
	
	private SelectionAction[] getAssignDocStyleAction(final ImDocumentMarkupPanel idmp) {
		SelectionAction[] adsa = {new SelectionAction("styleAssign", "Assign Document Style", "Assign a style template to, or create one for this document to help with markup automation") {
			public boolean performAction(ImDocumentMarkupPanel invoker) {
				return selectDocStyle(idmp, null, null, true);
			}
		}};
		return adsa;
	}
	
	private SelectionAction[] getHandleUnavailableDocStyleActions(final ImDocumentMarkupPanel idmp, final String docStyleName) {
		SelectionAction[] adsa = {new SelectionAction("styleAssign", "Change Document Style", ("Assign a new style template to, or create one for this document to help with markup automation (currently '" + docStyleName + "', which is not avalable for editing)")) {
			public boolean performAction(ImDocumentMarkupPanel invoker) {
				return selectDocStyle(idmp, null, null, true);
			}
		},
		new SelectionAction("styleRemove", "Remove Document Style", ("Remove style template from this document (set to '" + docStyleName + "', which is not avalable for editing)")) {
			public boolean performAction(ImDocumentMarkupPanel invoker) {
				boolean changed = false;
				if (idmp.document.removeAttribute(DocumentStyle.DOCUMENT_STYLE_ATTRIBUTE) != null)
					changed = true;
				if (idmp.document.removeAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE) != null)
					changed = true;
				if (idmp.document.removeAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE) != null)
					changed = true;
				if (idmp.document.removeAttribute(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE) != null)
					changed = true;
				return changed;
			}
		}};
		return adsa;
	}
	
	private SelectionAction getChangeDocStyleAction(final ImDocumentMarkupPanel idmp, final String docStyleId, final String docStyleName) {
		return new SelectionAction("styleAssign", "Change Document Style", ("Assign a new style template to, or create one for this document to help with markup automation (currently '" + docStyleName + "')")) {
			public boolean performAction(ImDocumentMarkupPanel invoker) {
				return selectDocStyle(idmp, docStyleId, docStyleName, false);
			}
		};
	}
	
	boolean selectDocStyle(ImDocumentMarkupPanel idmp, String docStyleId, String docStyleName, boolean suggest) {
		ArrayList selDocStyleList = new ArrayList();
		HashMap selDocStylesByID = new HashMap();
		TreeMap selDocStylesByName = new TreeMap(String.CASE_INSENSITIVE_ORDER);
		DocStyleSettings[] srcDocStyles = this.getDocStyles();
		for (int s = 0; s < srcDocStyles.length; s++) {
			if (selDocStylesByID.containsKey(srcDocStyles[s].id))
				continue;
			SelectableDocStyle selDocStyle = new SelectableDocStyle(srcDocStyles[s].id, srcDocStyles[s].name, true, selDocStylesByName.containsKey(srcDocStyles[s].name));
			selDocStyleList.add(selDocStyle);
			selDocStylesByID.put(selDocStyle.id, selDocStyle);
			if (!selDocStylesByName.containsKey(srcDocStyles[s].name))
				selDocStylesByName.put(selDocStyle.name, selDocStyle);
		}
		Attributed[] provDocStyles = this.docStyleProvider.getDocStyleAttributes(false);
		for (int s = 0; s < provDocStyles.length; s++) {
			String provDocStyleId = ((String) provDocStyles[s].getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE));
			if (provDocStyleId == null)
				continue;
			if (selDocStylesByID.containsKey(provDocStyleId))
				continue;
			String provDocStyleName = ((String) provDocStyles[s].getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE));
			if (provDocStyleName == null)
				continue;
			SelectableDocStyle selDocStyle = new SelectableDocStyle(provDocStyleId, provDocStyleName, false, selDocStylesByName.containsKey(provDocStyleName));
			selDocStyleList.add(selDocStyle);
			selDocStylesByID.put(selDocStyle.id, selDocStyle);
			if (!selDocStylesByName.containsKey(provDocStyleName))
				selDocStylesByName.put(selDocStyle.name, selDocStyle);
		}
		SelectableDocStyle[] selDocStyles = ((SelectableDocStyle[]) selDocStyleList.toArray(new SelectableDocStyle[selDocStyleList.size()]));
		Arrays.sort(selDocStyles);
		
		final JTextField cDocStyleNameField = new JTextField();
		final JTextField cDocStyleOrigin = new JTextField();
		final JTextField cDocStyleYear = new JTextField();
		final JComboBox cDocStylePubType = new JComboBox(this.refTypeSystem.getBibRefTypeNames());
		cDocStylePubType.setEditable(false);
		final JComboBox cDocStyleParentSelector = new JComboBox(selDocStyles);
		cDocStyleParentSelector.insertItemAt(NO_PARENT_SELECTABLE_DOC_STYLE, 0);
		cDocStyleParentSelector.setSelectedItem(NO_PARENT_SELECTABLE_DOC_STYLE);
		cDocStyleParentSelector.setEditable(false);
		final JCheckBox cDocStyleCopyParent = new JCheckBox("Copy data from parent instead of deriving?");
		
		final JPanel cDocStylePanel = new JPanel(new GridBagLayout(), true);
		cDocStylePanel.setBorder(BorderFactory.createEtchedBorder());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.gridy = 0;
		gbc.weighty = 0;
		gbc.insets.left = 2;
		gbc.insets.right = 2;
		gbc.insets.top = 2;
		gbc.insets.bottom = 2;
		gbc.gridx = 0;
		gbc.weightx = 0;
		cDocStylePanel.add(new JLabel(" Enter Name: "), gbc.clone());
		gbc.gridx = 1;
		gbc.weightx = 1;
		cDocStylePanel.add(cDocStyleNameField, gbc.clone());
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0;
		cDocStylePanel.add(new JLabel(" Journal / Publisher: "), gbc.clone());
		gbc.gridx = 1;
		gbc.weightx = 1;
		cDocStylePanel.add(cDocStyleOrigin, gbc.clone());
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0;
		cDocStylePanel.add(new JLabel(" From Year: "), gbc.clone());
		gbc.gridx = 1;
		gbc.weightx = 1;
		cDocStylePanel.add(cDocStyleYear, gbc.clone());
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0;
		cDocStylePanel.add(new JLabel(" Publication Type: "), gbc.clone());
		gbc.gridx = 1;
		gbc.weightx = 1;
		cDocStylePanel.add(cDocStylePubType, gbc.clone());
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0;
		cDocStylePanel.add(new JLabel(" Derive from: "), gbc.clone());
		gbc.gridx = 1;
		gbc.weightx = 1;
		cDocStylePanel.add(cDocStyleParentSelector, gbc.clone());
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0;
		cDocStylePanel.add(new JLabel(""), gbc.clone());
		gbc.gridx = 1;
		gbc.weightx = 1;
		cDocStylePanel.add(cDocStyleCopyParent, gbc.clone());
		
		final JComboBox docStyleSelector = new JComboBox(selDocStyles);
		docStyleSelector.insertItemAt(CREATE_SELECTABLE_DOC_STYLE, 0);
		docStyleSelector.setSelectedItem(CREATE_SELECTABLE_DOC_STYLE);
		docStyleSelector.setEditable(false);
		docStyleSelector.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				cDocStylePanel.setEnabled(CREATE_SELECTABLE_DOC_STYLE.equals(docStyleSelector.getSelectedItem()));
				cDocStyleOrigin.setEnabled(CREATE_SELECTABLE_DOC_STYLE.equals(docStyleSelector.getSelectedItem()));
				cDocStyleYear.setEnabled(CREATE_SELECTABLE_DOC_STYLE.equals(docStyleSelector.getSelectedItem()));
				cDocStylePubType.setEnabled(CREATE_SELECTABLE_DOC_STYLE.equals(docStyleSelector.getSelectedItem()));
				cDocStyleNameField.setEnabled(CREATE_SELECTABLE_DOC_STYLE.equals(docStyleSelector.getSelectedItem()));
				cDocStyleParentSelector.setEnabled(CREATE_SELECTABLE_DOC_STYLE.equals(docStyleSelector.getSelectedItem()));
				cDocStyleCopyParent.setEnabled(!NO_PARENT_SELECTABLE_DOC_STYLE.equals(cDocStyleParentSelector.getSelectedItem()) && CREATE_SELECTABLE_DOC_STYLE.equals(docStyleSelector.getSelectedItem()));
			}
		});
		if ((docStyleId != null) || (docStyleName != null))
			for (int s = 0; s < selDocStyles.length; s++) {
				if (selDocStyles[s].id.equals(docStyleId)) {
					docStyleSelector.setSelectedItem(selDocStyles[s]);
					break;
				}
				else if (selDocStyles[s].name.equalsIgnoreCase(docStyleName)) {
					docStyleSelector.setSelectedItem(selDocStyles[s]);
					break;
				}
			}
		
		if (suggest) {
			DocumentStyle docStyle = this.docStyleProvider.getStyleFor(idmp.document);
			if (docStyle != null) {
				DocStyleSettings sDocStyle = null;
				if (sDocStyle == null)
					sDocStyle = ((DocStyleSettings) this.docStylesById.get(docStyle.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE)));
				if (sDocStyle == null)
					sDocStyle = ((DocStyleSettings) this.docStylesByName.get(docStyle.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE)));
				if (sDocStyle != null)
					docStyleSelector.setSelectedItem(selDocStylesByID.get(sDocStyle.id));
			}
		}
		
		DocumentListener dl = new DocumentListener() {
			public void insertUpdate(DocumentEvent de) {
				this.updateNameField();
			}
			public void removeUpdate(DocumentEvent de) {
				this.updateNameField();
			}
			private void updateNameField() {
				String origin = cDocStyleOrigin.getText().trim();
				if (origin.length() == 0)
					return;
				origin = origin.toLowerCase().replaceAll("[^A-Za-z0-9\\-]+", "_");
				String year = cDocStyleYear.getText().trim();
				if ((year.length() == 0) || !year.matches("[0-9]{4}"))
					year = "0000";
				String pubType = ((String) cDocStylePubType.getSelectedItem());
				pubType = pubType.toLowerCase().replaceAll("\\s+", "_");
				cDocStyleNameField.setText(origin + "." + year + "." + pubType);
			}
			public void changedUpdate(DocumentEvent de) {}
		};
		cDocStyleOrigin.getDocument().addDocumentListener(dl);
		cDocStyleYear.getDocument().addDocumentListener(dl);
		cDocStylePubType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				String origin = cDocStyleOrigin.getText().trim();
				if (origin.length() == 0)
					return;
				origin = origin.toLowerCase().replaceAll("[^A-Za-z0-9\\-]+", "_");
				String year = cDocStyleYear.getText().trim();
				if ((year.length() == 0) || !year.matches("[0-9]{4}"))
					year = "0000";
				String pubType = ((String) cDocStylePubType.getSelectedItem());
				pubType = pubType.toLowerCase().replaceAll("\\s+", "_");
				cDocStyleNameField.setText(origin + "." + year + "." + pubType);
			}
		});
		
		JPanel docStyleSelectorPanel = new JPanel(new BorderLayout(), true);
		docStyleSelectorPanel.add(new JLabel("Select Document Style: "), BorderLayout.WEST);
		docStyleSelectorPanel.add(docStyleSelector, BorderLayout.CENTER);
		docStyleSelectorPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, docStyleSelectorPanel.getBackground()));
		
		cDocStyleParentSelector.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				cDocStyleCopyParent.setEnabled(!NO_PARENT_SELECTABLE_DOC_STYLE.equals(cDocStyleParentSelector.getSelectedItem()) && CREATE_SELECTABLE_DOC_STYLE.equals(docStyleSelector.getSelectedItem()));
			}
		});
		
		JCheckBox editDocStyle = new JCheckBox("Open document style for editing after assigning it?");
		if ((this.docStyleEditor != null) && this.docStyleEditor.isVisible()) {
			editDocStyle.setSelected(true);
			editDocStyle.setEnabled(false);
		}
		
		JPanel docStylePanel = new JPanel(new BorderLayout(), true);
		docStylePanel.add(docStyleSelectorPanel, BorderLayout.NORTH);
		docStylePanel.add(cDocStylePanel, BorderLayout.CENTER);
		docStylePanel.add(editDocStyle, BorderLayout.SOUTH);
		
		SelectableDocStyle selDocStyle;
		DocStyleSettings selectedDocStyle = null;
		do {
			int choice = DialogFactory.confirm(docStylePanel, "Select or Create Document Style", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (choice != JOptionPane.OK_OPTION)
				return false;
			
			selDocStyle = ((SelectableDocStyle) docStyleSelector.getSelectedItem());
			if (CREATE_SELECTABLE_DOC_STYLE.equals(selDocStyle)) {
				String cDocStyleName = this.checkDocStyleName(cDocStyleNameField.getText(), null, ", please select it in the drop-down");
				if (cDocStyleName == null)
					continue;
				
				SelectableDocStyle cSelParentDocStyle = ((SelectableDocStyle) cDocStyleParentSelector.getSelectedItem());
				DocStyleSettings cDocStyle = this.getDocStyleByName(("<create>-" + Math.random() /* prevents accidental reuse */), false); // mints ID
				cDocStyle.setName(cDocStyleName); // marks name as dirty (no need to catch duplication, checked above)
				
				DocStyleSettings cParentDocStyle;
				if (NO_PARENT_SELECTABLE_DOC_STYLE.equals(cSelParentDocStyle))
					cParentDocStyle = null;
				else if (cSelParentDocStyle.gotSource)
					cParentDocStyle = this.getDocStyleById(cSelParentDocStyle.id);
				else if (cDocStyleCopyParent.isSelected()) {
					DocumentStyle pDocStyle = this.docStyleProvider.getDocStyleById(cSelParentDocStyle.id);
					Data pDocStyleData = pDocStyle.getData();
					String[] pdsDataKeys = pDocStyleData.getPropertyNames();
					for (int k = 0; k < pdsDataKeys.length; k++)
						cDocStyle.data.setSetting(pdsDataKeys[k], pDocStyleData.getPropertyData(pdsDataKeys[k]));
					cParentDocStyle = null;
				}
				else {
					cParentDocStyle = this.importDocStyleAsSource(cSelParentDocStyle.id, cSelParentDocStyle.name);
					if (cParentDocStyle == null)
						continue;
				}
				
				if (cParentDocStyle == null) {}
				else if (cDocStyleCopyParent.isSelected()) {
					String[] pdsDataKeys = cParentDocStyle.data.getKeys();
					for (int k = 0; k < pdsDataKeys.length; k++) {
						if (pdsDataKeys[k].startsWith("@."))
							continue; // don't copy attributes
						cDocStyle.data.setSetting(pdsDataKeys[k], cParentDocStyle.data.getSetting(pdsDataKeys[k]));
					}
					if (cParentDocStyle.inheritedSettings != null)
						cDocStyle.setInheritedSettings(cParentDocStyle.inheritedSettings, false);
				}
				else cDocStyle.setInheritedSettings(cParentDocStyle, false);
				
				idmp.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE, cDocStyle.id);
				idmp.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, cDocStyle.name);
				DocumentStyle docStyle = this.docStyleProvider.wrapDocumentStyle(cDocStyle.asDocStyleData());
				idmp.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_ATTRIBUTE, docStyle);
				selectedDocStyle = cDocStyle;
			}
			else {
				if (selDocStyle.gotSource)
					selectedDocStyle = this.getDocStyleById(selDocStyle.id);
				else {
					selectedDocStyle = this.importDocStyleAsSource(selDocStyle.id, selDocStyle.name);
					if (selectedDocStyle == null)
						continue;
				}
				
				idmp.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE, selectedDocStyle.id);
				idmp.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, selectedDocStyle.name);
				DocumentStyle docStyle = docStyleProvider.getDocStyleById(selectedDocStyle.id);
				if (docStyle == null)
					docStyle = this.docStyleProvider.wrapDocumentStyle(selectedDocStyle.asDocStyleData()); // stored, but not published yet
				idmp.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_ATTRIBUTE, docStyle);
			}
		}
		while (selectedDocStyle == null);
		
		if (editDocStyle.isSelected())
			this.editDocStyle(idmp, docStyleName, selectedDocStyle);
		
		return true; // we _did_ change the document, if only by assigning attributes
	}
	
	static final SelectableDocStyle CREATE_SELECTABLE_DOC_STYLE = new SelectableDocStyle("<create>", "<Create Document Style>", false, false) {
		public String toString() {
			return this.name;
		}
	};
	static final SelectableDocStyle NO_PARENT_SELECTABLE_DOC_STYLE = new SelectableDocStyle("<none>", "<None>", false, false) {
		public String toString() {
			return this.name;
		}
	};
	private static class SelectableDocStyle implements Comparable {
		final String id;
		final String name;
		final boolean gotSource;
		final boolean gotSourceName;
		SelectableDocStyle(String id, String name, boolean gotSource, boolean gotSourceName) {
			this.id = id;
			this.name = name;
			this.gotSource = gotSource;
			this.gotSourceName = gotSourceName;
		}
		public String toString() {
			return ("<HTML>" + (this.gotSource ? "<B>" : (this.gotSourceName ? "<I>" : "")) + this.name + " (" + (this.gotSource ? "source available" :  (this.gotSourceName ? "source name conflict" : "import source")) + ")" + (this.gotSource ? "</B>" :  (this.gotSourceName ? "</I>" : "")) + "</HTML>");
		}
		public boolean equals(Object obj) {
			return ((obj instanceof SelectableDocStyle) && this.id.equals(((SelectableDocStyle) obj).id));
		}
		public int compareTo(Object obj) {
			return ((obj instanceof SelectableDocStyle) ? this.name.compareToIgnoreCase(((SelectableDocStyle) obj).name) : -1);
		}
	}
	
	String checkDocStyleName(String docStyleName, String docStyleId, String duplicateAlternative) {
		
		//	check name
		if (docStyleName.length() == 0) {
			DialogFactory.alert("A document style name must not be empty.", "Invalid Document Style Name", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		else if (docStyleName.indexOf(" ") != -1) {
			DialogFactory.alert("A document style name must not contain spaces.", "Invalid Document Style Name", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		else if (this.docStylesByName.containsKey(docStyleName)) {
			DocStyleSettings nDocStyle = ((DocStyleSettings) this.docStylesByName.get(docStyleName));
			if (!nDocStyle.id.equals(docStyleId)) {
				DialogFactory.alert(("A document style named '" + docStyleName + "' already exists" + duplicateAlternative + "."), "Duplicate Document Style Name", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		
		//	this one is good
		return docStyleName;
	}
	
	DocStyleSettings importDocStyleAsSource(String docStyleId, String docStyleName) {
		
		//	check cache, and resolve on demand
		DocStyleSettings docStyle = ((DocStyleSettings) this.docStylesById.get(docStyleId));
		if (docStyle != null) {
			if (docStyle.inheritedSettings == null) {
				String inheritedDocStyleId = docStyle.data.getSetting("@." + INHERIT_VALUES_FROM_ATTRIBUTE);
				if (inheritedDocStyleId != null) try {
					docStyle.setInheritedSettings(this.getDocStyleById(inheritedDocStyleId), true);
				}
				catch (IllegalArgumentException iae) {
					System.out.println("Failed to set parent document style of '" + docStyle.name + "': " + iae.getMessage());
					iae.printStackTrace(System.out);
				}
			}
			return docStyle;
		}
		
		//	check for naming conflicts
		DocStyleSettings nDocStyle = ((DocStyleSettings) this.docStylesByName.get(docStyleName));
		if (nDocStyle != null) {
			int choice = DialogFactory.confirm(("<HTML>Cannot import document style source as '" + docStyleName + "' because another<BR>document style source named '" + nDocStyle.name + "' already exists locally.<BR>Use the local document style source instead?<BR><BR>If you choose to copy the external document style source, an import is not required.</HTML>"), "Document Style Source Name Conflict", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			return ((choice == JOptionPane.YES_OPTION) ? nDocStyle : null);
		}
		
		//	get document style from provider and copy basic attributes
		DocumentStyle provDocStyle = this.docStyleProvider.getDocStyleById(docStyleId);
		Data provDocStyleData = provDocStyle.getData();
		Settings docStyleData = new Settings();
		docStyleData.setSetting(("@." + DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE), docStyleName);
		docStyleData.setSetting(("@." + DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE), docStyleId);
		long docStyleLastMod = Long.parseLong((String) provDocStyleData.getAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, "-1"));
		if (docStyleLastMod != -1)
			docStyleData.setSetting(("@." + DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE), ("" + docStyleLastMod));
		int docStyleVersion = Integer.parseInt((String) provDocStyleData.getAttribute(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE, "-1"));
		if (docStyleVersion != -1)
			docStyleData.setSetting(("@." + DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE), ("" + docStyleVersion));
		
		//	copy parameter value data
		String[] pdsDataKeys = provDocStyleData.getPropertyNames();
		for (int k = 0; k < pdsDataKeys.length; k++)
			docStyleData.setSetting(pdsDataKeys[k], provDocStyleData.getPropertyData(pdsDataKeys[k]));
		
		//	persist data
		String docStyleDataName = docStyleName;
		if (!docStyleDataName.endsWith(".docStyleSource"))
			docStyleDataName += ".docStyleSource";
		try {
			this.storeSettingsResource(docStyleDataName, docStyleData);
		}
		catch (IOException ioe) {
			System.out.println("Error storing document style '" + docStyleDataName + "' after import: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
			DialogFactory.alert(("Failed to import document style template '" + docStyleName + "': " + ioe.getMessage()), "Document Style Template Import Failed", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		//	create and cache document style
		docStyle = new DocStyleSettings(this, docStyleId, docStyleName, docStyleData, docStyleLastMod);
		docStyle.lastStoredTimePublishedLoc = docStyleLastMod; // we know it is published
		docStyle.lastStoredTimePublishedDss = docStyleLastMod; // we know it is published
		this.docStylesById.put(docStyle.id, docStyle);
		this.docStylesByName.put(docStyle.name, docStyle);
		
		//	finally ...
		return docStyle;
	}
	
	private SelectionAction getRemoveDocStyleAction(final ImDocumentMarkupPanel idmp, String docStyleName) {
		return new SelectionAction("styleRemove", "Remove Document Style", ("Remove style template from this document (set to '" + docStyleName + "')")) {
			public boolean performAction(ImDocumentMarkupPanel invoker) {
				boolean changed = false;
				if (idmp.document.removeAttribute(DocumentStyle.DOCUMENT_STYLE_ATTRIBUTE) != null)
					changed = true;
				if (idmp.document.removeAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE) != null)
					changed = true;
				if (idmp.document.removeAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE) != null)
					changed = true;
				if (idmp.document.removeAttribute(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE) != null)
					changed = true;
				return changed;
			}
		};
	}
	
	
	private SelectionAction getEditDocStyleAction(final ImDocumentMarkupPanel doc, final String docStyleName, final DocStyleSettings docStyle) {
		return new SelectionAction("styleEdit", "Edit Document Style", "Open the style template assigned to this document for editing") {
			public boolean performAction(ImDocumentMarkupPanel invoker) {
				editDocStyle(doc, docStyleName, docStyle);
				return false;
			}
		};
	}
	
	void editDocStyle(ImDocumentMarkupPanel idmp, String docStyleName, DocStyleSettings docStyle) {
		if (this.docStyleEditor == null) {
			this.docStyleEditor = new DocStyleEditor();
			this.docStyleEditorDisplayExtension = new DisplayExtension[1];
			this.docStyleEditorDisplayExtension[0] = this.docStyleEditor;
		}
		this.docStyleEditor.setContentDocStyle(idmp, docStyleName, docStyle);
		this.ggImagine.notifyDisplayExtensionsModified(null);
		this.docStyleEditor.setVisible(true);
	}
	
	private DocStyleEditor docStyleEditor = null;
	private DisplayExtension[] docStyleEditorDisplayExtension = null;
	
	static final DocStyleSettings NO_PARENT_DOC_STYLE_SETTINGS = new DocStyleSettings(null, "<none>", "<None>", new Settings(), -1);
	
	private class DocStyleEditor extends DialogPanel implements DisplayExtension {
		private JTree paramTree = new JTree();
		private JPanel paramPanel = new JPanel(new BorderLayout(), true);
		private JTabbedPane headerTabs = new JTabbedPane();
		private boolean headerTabsUpdating = false;
		private JButton copyFromParentButton = new JButton("Copy from Parent");
		private JButton moveFromParentButton = new JButton("Move from Parent");
		private JButton copyToParentButton = new JButton("Copy to Parent");
		private JButton moveToParentButton = new JButton("Move to Parent");
		private JPanel paramValueTransferButtonPanel = new JPanel(new GridLayout(1, 0, 3, 0), true);
		private JButton saveDocStyleButton = new JButton("Save Document Style Template");
		private JButton publishDocStyleLocButton = new JButton("Publish Document Style Template Locally");
		private JButton publishDocStyleDssButton = new JButton("Publish Document Style Template to Server");
		private JButton trackPropertyDataRetrievalButton = new JButton("Track Property Retrieval");
		private int toolTipCloseDelay = ToolTipManager.sharedInstance().getDismissDelay();
		DocStyleEditor() {
			super("Edit Document Style Template", false);
			
			this.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					if (headDocStyle != null) {
						for (DocStyleSettings dss = headDocStyle; dss != null; dss = dss.inheritedSettings)
							ImDocumentStyleManager.this.docStyleProvider.setLiveData(dss.id, null);
					}
					askSaveIfDirty();
					if (parameterTracker != null) {
						parameterTracker.dispose();
						parameterTracker = null;
					}
					docStyleEditor = null; // make way on closing
					docStyleEditorDisplayExtension = null;
					ggImagine.notifyDisplayExtensionsModified(null);
					ToolTipManager.sharedInstance().setDismissDelay(toolTipCloseDelay); // revert tooltip behavior to normal
				}
			});
			ToolTipManager.sharedInstance().setDismissDelay(10 * 60 * 1000); // make sure tooltips remain open long enough for reading explanations
			
			TreeSelectionModel ptsm = new DefaultTreeSelectionModel();
			ptsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			this.paramTree.setSelectionModel(ptsm);
			this.paramTree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent tse) {
					paramTreeNodeSelected((ParamTreeNode) tse.getPath().getLastPathComponent());
				}
			});
			this.paramTree.setModel(this.paramTreeModel);
			this.paramTree.setRootVisible(true);
			this.paramTree.setCellRenderer(new ParamTreeCellRenderer());
			
			this.headerTabs.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent ce) {
					if (headerTabsUpdating)
						return;
					DocStyleHeaderTab dsht = ((DocStyleHeaderTab) headerTabs.getSelectedComponent());
					dsht.nameField.requestFocusInWindow(); // removed focus from input field
					setSelectedDocStyle(dsht.docStyle);
				}
			});
			
			this.copyFromParentButton.setBorder(BorderFactory.createRaisedBevelBorder());
			this.copyFromParentButton.setToolTipText("Copy parameters values from parent document style, duplicating them.");
			this.copyFromParentButton.setEnabled(false);
			this.copyFromParentButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					transferParamsFromParent(false);
				}
			});
			this.moveFromParentButton.setBorder(BorderFactory.createRaisedBevelBorder());
			this.moveFromParentButton.setToolTipText("Move parameters values from parent document style here, removing them from latter.");
			this.moveFromParentButton.setEnabled(false);
			this.moveFromParentButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					transferParamsFromParent(true);
				}
			});
			this.copyToParentButton.setBorder(BorderFactory.createRaisedBevelBorder());
			this.copyToParentButton.setToolTipText("Copy parameters values from here to parent document style, duplicating them.");
			this.copyToParentButton.setEnabled(false);
			this.copyToParentButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					transferParamsToParent(false);
				}
			});
			this.moveToParentButton.setBorder(BorderFactory.createRaisedBevelBorder());
			this.moveToParentButton.setToolTipText("Move parameters values from here to parent document style, removing them from here.");
			this.moveToParentButton.setEnabled(false);
			this.moveToParentButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					transferParamsToParent(true);
				}
			});
			this.paramValueTransferButtonPanel.add(this.moveFromParentButton);
			this.paramValueTransferButtonPanel.add(this.copyFromParentButton);
			this.paramValueTransferButtonPanel.add(this.copyToParentButton);
			this.paramValueTransferButtonPanel.add(this.moveToParentButton);
			
			this.saveDocStyleButton.setBorder(BorderFactory.createRaisedBevelBorder());
			this.saveDocStyleButton.setEnabled(false);
			this.saveDocStyleButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					saveDocStyle(null); // saves selected document style
				}
			});
			this.publishDocStyleLocButton.setBorder(BorderFactory.createRaisedBevelBorder());
			this.publishDocStyleLocButton.setEnabled(false);
			this.publishDocStyleLocButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					publishDocStyleLoc();
				}
			});
			this.publishDocStyleDssButton.setBorder(BorderFactory.createRaisedBevelBorder());
			this.publishDocStyleDssButton.setEnabled(false);
			this.publishDocStyleDssButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					publishDocStyleDss();
				}
			});
			this.trackPropertyDataRetrievalButton.setBorder(BorderFactory.createRaisedBevelBorder());
			this.trackPropertyDataRetrievalButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					trackPropertyDataRetrieval();
				}
			});
			JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 2, 1), true);
			buttonPanel.add(this.saveDocStyleButton);
			buttonPanel.add(this.publishDocStyleLocButton);
			buttonPanel.add(this.publishDocStyleDssButton);
			buttonPanel.add(this.trackPropertyDataRetrievalButton);
			
			JScrollPane paramTreeBox = new JScrollPane(this.paramTree);
			paramTreeBox.getHorizontalScrollBar().setBlockIncrement(20);
			paramTreeBox.getHorizontalScrollBar().setUnitIncrement(20);
			paramTreeBox.getVerticalScrollBar().setBlockIncrement(20);
			paramTreeBox.getVerticalScrollBar().setUnitIncrement(20);
			
			JSplitPane paramSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, paramTreeBox, this.paramPanel);
			
			this.add(this.headerTabs, BorderLayout.NORTH);
			this.add(paramSplit, BorderLayout.CENTER);
			this.add(buttonPanel, BorderLayout.SOUTH);
			this.setSize(400, 600);
			this.setLocationRelativeTo(this.getOwner());
		}
		
		private class DocStyleHeaderTab extends JPanel implements DocumentListener {
			private JTextField nameField = new JTextField();
			private JButton deriveChildButton = new JButton("Derive Style");
			private JButton absorbChildButton = new JButton("Absorb Derived");
			private JComboBox parentDocStyle;
			private boolean parentDocStylesUpdating = false;
			private JButton extractParentButton = new JButton("Extract Parent");
			private JButton absorbParentButton = new JButton("Absorb Parent");
			DocStyleSettings docStyle;
			DocStyleHeaderTab(DocStyleSettings docStyle, DocStyleSettings[] docStyles) {
				super(new BorderLayout(), true);
				this.docStyle = docStyle;
				
				this.nameField.setText(this.docStyle.name);
				this.nameField.getDocument().addDocumentListener(this);
				this.nameField.setBorder(BorderFactory.createLoweredBevelBorder());
				
				this.parentDocStyle = new JComboBox(docStyles);
				this.parentDocStyle.setBorder(BorderFactory.createLoweredBevelBorder());
				this.parentDocStyle.insertItemAt(NO_PARENT_DOC_STYLE_SETTINGS, 0);
				this.parentDocStyle.setSelectedItem((this.docStyle.inheritedSettings == null) ? NO_PARENT_DOC_STYLE_SETTINGS : this.docStyle.inheritedSettings);
				this.parentDocStyle.setEditable(false);
				this.parentDocStyle.addItemListener(new ItemListener() {
					private DocStyleSettings selDs = ((DocStyleSettings) parentDocStyle.getSelectedItem());
					public void itemStateChanged(ItemEvent ie) {
						if (ie.getStateChange() == ItemEvent.DESELECTED)
							return;
						if (parentDocStylesUpdating)
							return; // temporary change
						DocStyleSettings pds = ((DocStyleSettings) parentDocStyle.getSelectedItem());
						if (pds == this.selDs)
							return; // happens on rollback
						if (NO_PARENT_DOC_STYLE_SETTINGS.equals(pds)) {
							boolean pdsChanged = setParentDocStyle(null, false);
							if (pdsChanged)
								this.selDs = pds;
							else parentDocStyle.setSelectedItem(this.selDs);
						}
						else try {
							boolean pdsChanged = setParentDocStyle(pds, false);
							if (pdsChanged)
								this.selDs = pds;
							else parentDocStyle.setSelectedItem(this.selDs);
						}
						catch (IllegalArgumentException iae) {
							iae.printStackTrace(System.out);
							DialogFactory.alert(iae.getMessage(), "Cannot Set Parent Document Style", JOptionPane.ERROR_MESSAGE);
							parentDocStyle.setSelectedItem(this.selDs);
						}
					}
				});
				
				this.deriveChildButton.setBorder(BorderFactory.createRaisedBevelBorder());
				this.deriveChildButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						deriveChildDocStyle();
						absorbChildButton.setEnabled((DocStyleHeaderTab.this.docStyle.inheritingSettings.size() == 1) && (DocStyleHeaderTab.this.docStyle != headDocStyle));
					}
				});
				this.absorbChildButton.setBorder(BorderFactory.createRaisedBevelBorder());
				this.absorbChildButton.setEnabled((this.docStyle.inheritingSettings.size() == 1) && (this.docStyle != headDocStyle));
				this.absorbChildButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						absorbChildDocStyle();
						absorbChildButton.setEnabled((DocStyleHeaderTab.this.docStyle.inheritingSettings.size() == 1) && (DocStyleHeaderTab.this.docStyle != headDocStyle));
					}
				});
				this.extractParentButton.setBorder(BorderFactory.createRaisedBevelBorder());
				this.extractParentButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						extractParentDocStyle();
						parentDocStylesUpdating = true;
						parentDocStyle.setSelectedItem((DocStyleHeaderTab.this.docStyle.inheritedSettings == null) ? NO_PARENT_DOC_STYLE_SETTINGS : DocStyleHeaderTab.this.docStyle.inheritedSettings);
						parentDocStylesUpdating = false;
						absorbParentButton.setEnabled((DocStyleHeaderTab.this.docStyle.inheritedSettings != null) && (DocStyleHeaderTab.this.docStyle.inheritedSettings.inheritingSettings.size() == 1));
					}
				});
				this.absorbParentButton.setBorder(BorderFactory.createRaisedBevelBorder());
				this.absorbParentButton.setEnabled((this.docStyle.inheritedSettings != null) && (this.docStyle.inheritedSettings.inheritingSettings.size() == 1));
				this.absorbParentButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						absorbParentDocStyle();
						parentDocStylesUpdating = true;
						parentDocStyle.setSelectedItem((DocStyleHeaderTab.this.docStyle.inheritedSettings == null) ? NO_PARENT_DOC_STYLE_SETTINGS : DocStyleHeaderTab.this.docStyle.inheritedSettings);
						parentDocStylesUpdating = false;
						absorbParentButton.setEnabled((DocStyleHeaderTab.this.docStyle.inheritedSettings != null) && (DocStyleHeaderTab.this.docStyle.inheritedSettings.inheritingSettings.size() == 1));
					}
				});
				JPanel pcEditPanel = new JPanel(new GridLayout(1, 0, 3, 0), true);
				pcEditPanel.add(this.deriveChildButton);
				pcEditPanel.add(this.absorbChildButton);
				pcEditPanel.add(this.absorbParentButton);
				pcEditPanel.add(this.extractParentButton);
				
				this.add(this.nameField, BorderLayout.NORTH);
				this.add(this.parentDocStyle, BorderLayout.CENTER);
				this.add(pcEditPanel, BorderLayout.SOUTH);
			}
			public void insertUpdate(DocumentEvent de) {
				if (this.resettingDocStyleName)
					return;
				if (checkDocStyleName(this.nameField.getText().trim(), this.docStyle.id, "") == null)
					this.resetName(null);
				else try {
					this.docStyle.setName(this.nameField.getText().trim());
					headerTabs.setTitleAt(headerTabs.getSelectedIndex(), this.docStyle.name);
					checkDocStyleDirty();
				}
				catch (IllegalArgumentException iae) {
					this.resetName(iae);
				}
			}
			public void removeUpdate(DocumentEvent de) {
				if (this.resettingDocStyleName)
					return;
				if (checkDocStyleName(this.nameField.getText().trim(), this.docStyle.id, "") == null)
					this.resetName(null);
				else try {
					this.docStyle.setName(this.nameField.getText().trim());
					headerTabs.setTitleAt(headerTabs.getSelectedIndex(), this.docStyle.name);
					checkDocStyleDirty();
				}
				catch (IllegalArgumentException iae) {
					this.resetName(iae);
				}
			}
			private boolean resettingDocStyleName = false;
			private void resetName(IllegalArgumentException iae) {
				if (iae != null)
					DialogFactory.alert(iae.getMessage(), "Cannot Change Document Style Name", JOptionPane.ERROR_MESSAGE);
				//	need to do this asynchronously to prevent calling setText() from event listener, which incurs exceptions
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							resettingDocStyleName = true;
							nameField.setText(docStyle.name);
						}
						finally {
							resettingDocStyleName = false;
						}
					}
				});
			}
			public void changedUpdate(DocumentEvent de) {}
			void updateParentDocStyles(DocStyleSettings[] docStyles) {
				this.parentDocStylesUpdating = true;
				DocStyleSettings pds = ((DocStyleSettings) parentDocStyle.getSelectedItem());
				MutableComboBoxModel pdss = ((MutableComboBoxModel) this.parentDocStyle.getModel());
				while (pdss.getSize() > 1)
					pdss.removeElementAt(1);
				int pdsIndex = 0;
				for (int s = 0; s < docStyles.length; s++) {
					if (docStyles[s].equals(pds))
						pdsIndex = pdss.getSize();
					pdss.addElement(docStyles[s]);
				}
				this.parentDocStyle.setSelectedIndex(pdsIndex);
				this.parentDocStylesUpdating = false;
			}
		}
		
		void extractParentDocStyle() {
			
			//	get name
			String pdsName = this.getDocStyleName((this.selectedDocStyle.storedName + ".parent"), "Parent");
			if (pdsName == null)
				return;
			
			//	create new document style and select it
			DocStyleSettings parentDocStyle = getDocStyleByName(("<create>-" + Math.random() /* prevents accidental reuse */), false); // mints ID
			parentDocStyle.setName(pdsName); // marks name as dirty (no need to catch duplication, caught in name getter)
			this.setParentDocStyle(parentDocStyle, true);
			this.updateParentDocStyles();
		}
		
		void absorbParentDocStyle() {
			DocStyleSettings parentDocStyle = this.selectedDocStyle.inheritedSettings;
			if (parentDocStyle == null)
				return;
			if (parentDocStyle.inheritingSettings.size() != 1)
				return;
			if (ImDocumentStyleManager.this.docStyleProvider.getDocStyleAttributes(parentDocStyle.id, false) != null) {
				DialogFactory.alert(("The derived document style '" + parentDocStyle.storedName + "' has been published and cannot be absorbed."), "Cannot Absorb Published Document Style", JOptionPane.ERROR_MESSAGE);
				return; // prevent absorbing published parent document style if published as head document style (provider knows ID)
			}
			ImDocumentStyleManager.this.docStyleProvider.setLiveData(parentDocStyle.id, null);
			String[] pdsDataKeys = parentDocStyle.data.getKeys();
			for (int k = 0; k < pdsDataKeys.length; k++) {
				if (pdsDataKeys[k].startsWith("@."))
					continue; // not copying attributes
				if (this.selectedDocStyle.data.containsKey(pdsDataKeys[k]))
					continue; // not overwriting own data
				this.selectedDocStyle.setSetting(pdsDataKeys[k], parentDocStyle.data.getSetting(pdsDataKeys[k]));
			}
			this.selectedDocStyle.setInheritedSettings(parentDocStyle.inheritedSettings, false);
			this.headerTabsUpdating = true;
			this.headerTabs.removeTabAt(this.headerTabs.getSelectedIndex() + 1);
			this.headerTabsUpdating = false;
			
			ImDocumentStyleManager.this.docStyleAbsorbed(parentDocStyle);
			this.updateParentDocStyles();
			this.updateParamValueSource();
		}
		
		void deriveChildDocStyle() {
			
			//	get name
			String cdsName = this.getDocStyleName((this.selectedDocStyle.storedName + ".child"), "Child");
			if (cdsName == null)
				return;
			
			//	create new document style and select it
			DocStyleSettings childDocStyle = getDocStyleByName(("<create>-" + Math.random() /* prevents accidental reuse */), false); // mints ID
			childDocStyle.setName(cdsName); // marks name as dirty (no need to catch duplication, caught in name getter)
			childDocStyle.setInheritedSettings(this.selectedDocStyle, false);
			for (Iterator isit = this.selectedDocStyle.inheritingSettings.iterator(); isit.hasNext();) {
				DocStyleSettings dss = ((DocStyleSettings) isit.next());
				if (dss != childDocStyle)
					dss.setInheritedSettings(childDocStyle, false);
			}
			
			//	make new child document style accessible
			LiveData childDocStyleData = childDocStyle.asDocStyleData();
			ImDocumentStyleManager.this.docStyleProvider.setLiveData(childDocStyle.id, childDocStyleData);
			
			//	make new child show
			if (this.selectedDocStyle == this.headDocStyle)
				this.setHeadDocStyle(childDocStyle);
			else {
				this.headerTabsUpdating = true;
				DocStyleHeaderTab dsht = new DocStyleHeaderTab(childDocStyle, this.docStyles);
				this.headerTabs.insertTab(childDocStyle.name, null, dsht, null, (this.headerTabs.getSelectedIndex() - 1));
				this.headerTabsUpdating = false;
				this.headerTabs.setSelectedIndex(this.headerTabs.getSelectedIndex() - 1);
				this.updateParamValueSource();
			}
			this.updateParentDocStyles();
			
			//	offer assigning newly derived document style (unless document closed)
			if (this.targetDocPanel == null)
				return;
			int choice = DialogFactory.confirm("Assign the newly created child document style to the current document?", "Assign Child Document Style", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (choice != JOptionPane.YES_OPTION)
				return;
			
			//	we already have an atomic action to piggiback on
			if (this.targetDocPanel.isAtomicActionRunning()) {
				this.targetDocPanel.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE, childDocStyle.id);
				this.targetDocPanel.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, childDocStyle.name);
				DocumentStyle docStyle = docStyleProvider.wrapDocumentStyle(childDocStyleData);
				this.targetDocPanel.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_ATTRIBUTE, docStyle);
			}
			
			//	change document style in dedicated atomic action
			else try {
				this.targetDocPanel.beginAtomicAction("Change Document Style");
				this.targetDocPanel.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE, childDocStyle.id);
				this.targetDocPanel.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, childDocStyle.name);
				DocumentStyle docStyle = docStyleProvider.wrapDocumentStyle(childDocStyleData);
				this.targetDocPanel.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_ATTRIBUTE, docStyle);
			}
			finally {
				this.targetDocPanel.endAtomicAction();
			}
		}
		
		void absorbChildDocStyle() {
			DocStyleSettings childDocStyle = null;
			for (DocStyleSettings dss = this.headDocStyle; dss != null; dss = dss.inheritedSettings)
				if (dss.inheritedSettings == this.selectedDocStyle) {
					childDocStyle = dss;
					break;
				}
			if (childDocStyle == null)
				return;
			if (ImDocumentStyleManager.this.docStyleProvider.getDocStyleAttributes(childDocStyle.id, false) != null) {
				DialogFactory.alert(("The derived document style '" + childDocStyle.storedName + "' has been published and cannot be absorbed."), "Cannot Absorb Published Document Style", JOptionPane.ERROR_MESSAGE);
				return; // prevent absorbing published child document style if published as head document style (provider knows ID)
			}
			if ((this.targetDocPanel != null) && childDocStyle.id.equals(this.targetDocPanel.document.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE))) {
				int choice = DialogFactory.confirm("The derived document style '" + childDocStyle.name + "' is assigned to the current document. Absorbing it will replace it with the absorbing parent.", "Replace Child Document Style", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (choice != JOptionPane.OK_OPTION)
					return;
			}
			ImDocumentStyleManager.this.docStyleProvider.setLiveData(childDocStyle.id, null);
			String[] cdsDataKeys = childDocStyle.data.getKeys();
			for (int k = 0; k < cdsDataKeys.length; k++) {
				if (cdsDataKeys[k].startsWith("@."))
					continue; // not copying attributes
				if (this.selectedDocStyle.data.containsKey(cdsDataKeys[k]))
					continue; // not overwriting own data
				this.selectedDocStyle.setSetting(cdsDataKeys[k], childDocStyle.data.getSetting(cdsDataKeys[k]));
			}
			for (Iterator isit = childDocStyle.inheritingSettings.iterator(); isit.hasNext();) {
				DocStyleSettings dss = ((DocStyleSettings) isit.next());
				dss.setInheritedSettings(this.selectedDocStyle, false);
			}
			if (childDocStyle == this.headDocStyle)
				this.setHeadDocStyle(this.selectedDocStyle);
			else {
				this.headerTabsUpdating = true;
				this.headerTabs.removeTabAt(this.headerTabs.getSelectedIndex() - 1);
				this.headerTabsUpdating = false;
				this.updateParamValueSource();
			}
			ImDocumentStyleManager.this.docStyleAbsorbed(childDocStyle);
			this.updateParentDocStyles();
			
			//	get back absorbing document style
			DocumentStyle docStyle = docStyleProvider.getDocStyleById(this.selectedDocStyle.id);
			if (docStyle == null)
				docStyle = docStyleProvider.wrapDocumentStyle(this.selectedDocStyle.asDocStyleData()); // stored, but not published yet
			
			//	we already have an atomic action to piggiback on
			if (this.targetDocPanel.isAtomicActionRunning()) {
				this.targetDocPanel.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE, this.selectedDocStyle.id);
				this.targetDocPanel.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, this.selectedDocStyle.name);
				this.targetDocPanel.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_ATTRIBUTE, docStyle);
			}
			
			//	change document style in dedicated atomic action
			else try {
				this.targetDocPanel.beginAtomicAction("Change Document Style");
				this.targetDocPanel.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE, this.selectedDocStyle.id);
				this.targetDocPanel.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, this.selectedDocStyle.name);
				this.targetDocPanel.document.setAttribute(DocumentStyle.DOCUMENT_STYLE_ATTRIBUTE, docStyle);
			}
			finally {
				this.targetDocPanel.endAtomicAction();
			}
		}
		
		private void updateParentDocStyles() {
			this.docStyles = getDocStyles();
			for (int h = 0; h < this.headerTabs.getComponentCount(); h++) {
				DocStyleHeaderTab dsht = ((DocStyleHeaderTab) this.headerTabs.getComponentAt(h));
				dsht.updateParentDocStyles(this.docStyles);
			}
		}
		
		private String getDocStyleName(String proposedDocStyleName, String docStyleType) {
			JTextField docStyleNameField = new JTextField(proposedDocStyleName);
			JPanel docStyleNamePanel = new JPanel(new GridLayout(0, 1), true);
			docStyleNamePanel.add(new JLabel(("<HTML>Please enter a name for the " + docStyleType.toLowerCase() + " document style.<BR/>The name cannot be empty or contain any spaces, and may be none of the existing document style names.</HTML>"), JLabel.CENTER));
			docStyleNamePanel.add(docStyleNameField);
			String docStyleName;
			do {
				int choice = DialogFactory.confirm(docStyleNamePanel, ("Enter " + docStyleType + " Document Style Name"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (choice != JOptionPane.OK_OPTION)
					return null;
				docStyleName = checkDocStyleName(docStyleNameField.getText().trim(), null, ", please specify a different name or cancel");
			}
			while (docStyleName == null);
			return docStyleName;
		}
		
		boolean setParentDocStyle(DocStyleSettings parentDocStyle, boolean extractingParentDocStyle) {
			DocStyleSettings oldPds = this.selectedDocStyle.inheritedSettings;
			if (parentDocStyle == oldPds)
				return true; // happens on selecting newly extracted parent
			if ((oldPds != null) && oldPds.isInheritanceChainDirty() && !extractingParentDocStyle) {
				int saveOldPds = DialogFactory.confirm(("The current parent document style " + ((oldPds.inheritedSettings == null) ? "has" : "or its ancestors have") + " unsaved changes. Save them before proceeding?"), "Save Previous Parent Document Style", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (saveOldPds == JOptionPane.YES_OPTION)
					ImDocumentStyleManager.this.storeDocStyle(oldPds);
				else if (saveOldPds == JOptionPane.CANCEL_OPTION)
					return false;
			}
			if (!this.selectedDocStyle.setInheritedSettings(parentDocStyle, false))
				return false;
			if (extractingParentDocStyle && (oldPds != null) && !parentDocStyle.setInheritedSettings(oldPds, false))
				return false; // add old parent to chain when extracting
			for (DocStyleSettings dss = oldPds; dss != null; dss = dss.inheritedSettings)
				ImDocumentStyleManager.this.docStyleProvider.setLiveData(dss.id, null);
			this.headerTabsUpdating = true;
			int selTabIndex = this.headerTabs.getSelectedIndex();
			while ((selTabIndex + 1) < this.headerTabs.getTabCount())
				this.headerTabs.removeTabAt(selTabIndex + 1);
			for (DocStyleSettings dss = this.selectedDocStyle.inheritedSettings; dss != null; dss = dss.inheritedSettings) {
				ImDocumentStyleManager.this.docStyleProvider.setLiveData(dss.id, dss.asDocStyleData());
				DocStyleHeaderTab dsht = new DocStyleHeaderTab(dss, this.docStyles);
				this.headerTabs.addTab(dss.name, dsht);
			}
			this.headerTabsUpdating = false;
			this.headerTabs.setSelectedIndex(selTabIndex + ((extractingParentDocStyle && (parentDocStyle != null)) ? 1 : 0));
//			this.headerTabsUpdating = false;
			this.updateParamValueSource();
			return true;
		}
		
//		String docStyleName = null;
		DocStyleSettings headDocStyle = null;
		DocStyleSettings[] docStyles = null;
		DocStyleSettings selectedDocStyle = null;
//		private boolean docStyleDirty = false;
		
		private ImDocumentMarkupPanel targetDocPanel;
		private ImDocument testDoc;
		private ImTokenSequence testDocTokens;
		
		void setContentDocStyle(ImDocumentMarkupPanel idmp, String docStyleName, DocStyleSettings docStyle) {
			
			//	update target panel and test document
			this.targetDocPanel = idmp;
			if (this.testDoc != idmp.document) {
				this.testDoc = idmp.document;
				this.testDocTokens = null;
			}
			
			//	document style remains, we're done here
			if ((this.headDocStyle != null) && (docStyle != null) && this.headDocStyle.id.equals(docStyle.id))
				return;
			
			//	save any modifications to previously open document style
			this.askSaveIfDirty();
			
			//	remove previous document style from immediate availability
			if (this.headDocStyle != null) {
				for (DocStyleSettings dss = this.headDocStyle; dss != null; dss = dss.inheritedSettings)
					ImDocumentStyleManager.this.docStyleProvider.setLiveData(dss.id, null);
			}
			
			//	update data fields
//			this.docStyleName = docStyleName;
			this.headDocStyle = docStyle;
			this.docStyles = getDocStyles();
			this.selectedDocStyle = docStyle; // select root
//			this.setDocStyleDirty(false);
			this.checkDocStyleDirty();
			
			//	clear header field
			this.headerTabsUpdating = true;
			this.headerTabs.removeAll();
			
			//	make sure changes show to consumers right away
			for (DocStyleSettings dss = this.headDocStyle; dss != null; dss = dss.inheritedSettings) {
				ImDocumentStyleManager.this.docStyleProvider.setLiveData(dss.id, dss.asDocStyleData());
				DocStyleHeaderTab dsht = new DocStyleHeaderTab(dss, this.docStyles);
				this.headerTabs.addTab(dss.name, dsht);
			}
			this.headerTabs.setSelectedIndex(0); // select root
			this.headerTabsUpdating = false;
			
			//	clear index fields
			this.paramGroupName = null;
			this.paramValueFields.clear();
			
			//	update window title
			this.setTitle("Edit Document Style Template '" + this.selectedDocStyle.name + "'");
			
			//	get available parameter names, including ones from style proper (anchors !!!)
			TreeSet dsParamNameSet = new TreeSet(Arrays.asList(parameterValueClassNames.getKeys()));
			String[] dDsParamNames = docStyle.getKeys();
			for (int p = 0; p < dDsParamNames.length; p++) {
				if (dDsParamNames[p].startsWith(Anchor.ANCHOR_PREFIX + "."))
					dsParamNameSet.add(dDsParamNames[p]);
			}
			
			//	make sure we can create anchors
			dsParamNameSet.add(Anchor.ANCHOR_PREFIX + ".<create>.dummy");
			dsParamNameSet.add(Anchor.ANCHOR_PREFIX + "." + PageFeatureAnchor.MAXIMUM_PAGES_AFTER_FIRST_PROPERTY);
			
			//	line up parameter names
			String[] dsParamNames = ((String[]) dsParamNameSet.toArray(new String[dsParamNameSet.size()]));
			Arrays.sort(dsParamNames);
			
			//	update parameter tree
			this.paramTreeRoot.clearChildren();
			this.paramTreeNodesByPrefix.clear();
			this.paramTreeNodesByPrefix.put(this.paramTreeRoot.prefix, this.paramTreeRoot);
			LinkedList ptnStack = new LinkedList();
			ptnStack.add(this.paramTreeRoot);
			for (int p = 0; p < dsParamNames.length; p++) {
				
				//	get current parent
				ParamTreeNode pptn = ((ParamTreeNode) ptnStack.getLast());
				
				//	ascend until prefix matches
				while ((pptn != this.paramTreeRoot) && !dsParamNames[p].startsWith(pptn.prefix + ".")) {
					ptnStack.removeLast();
					pptn = ((ParamTreeNode) ptnStack.getLast());
				}
				
				//	add more intermediate nodes for steps of current parameter
				while (pptn.prefix.length() < dsParamNames[p].lastIndexOf('.')) {
					ParamTreeNode ptn = new ParamTreeNode(dsParamNames[p].substring(0, dsParamNames[p].indexOf('.', (pptn.prefix.length() + 1))), pptn);
					pptn.addChild(ptn);
					pptn = ptn;
					ptnStack.addLast(pptn);
				}
				
				//	add parameter to parent tree node
				if (!(Anchor.ANCHOR_PREFIX + ".<create>.dummy").equals(dsParamNames[p]))
					pptn.addParamName(dsParamNames[p]);
			}
			
			//	update display
			this.updateParamTree();
			this.updateParamTransferButtons();
		}
		
		void setHeadDocStyle(DocStyleSettings docStyle) {
			
			//	save any modifications to previously open document style
			this.askSaveIfDirty();
			
			//	remove previous document style from immediate availability
			if (this.headDocStyle != null) {
				for (DocStyleSettings dss = this.headDocStyle; dss != null; dss = dss.inheritedSettings)
					ImDocumentStyleManager.this.docStyleProvider.setLiveData(dss.id, null);
			}
			
			//	update data fields
//			this.docStyleName = docStyleName;
			this.headDocStyle = docStyle;
			this.selectedDocStyle = docStyle; // select head
//			this.setDocStyleDirty(false);
			this.checkDocStyleDirty();
			
			//	clear header field
			this.headerTabsUpdating = true;
			this.headerTabs.removeAll();
			
			//	make sure changes show to consumers right away
			for (DocStyleSettings dss = this.headDocStyle; dss != null; dss = dss.inheritedSettings) {
				ImDocumentStyleManager.this.docStyleProvider.setLiveData(dss.id, dss.asDocStyleData());
				DocStyleHeaderTab dsht = new DocStyleHeaderTab(dss, this.docStyles);
				this.headerTabs.addTab(dss.name, dsht);
			}
			this.headerTabs.setSelectedIndex(0); // select head
			this.headerTabsUpdating = false;
			
			//	update window title
			this.setTitle("Edit Document Style Template '" + this.selectedDocStyle.name + "'");
			
			//	update field values and defaults
			this.updateParamValueSource();
			this.updateParamTransferButtons();
		}
		
		void setSelectedDocStyle(DocStyleSettings docStyle) {
			if (this.selectedDocStyle == docStyle)
				return;
			
			//	un-focus current field
			if (this.activeParamPanel instanceof FocusableParamPanel)
				((FocusableParamPanel) this.activeParamPanel).handleFocusLost();
			
			//	update data fields
//			this.docStyleName = docStyleName;
			this.selectedDocStyle = docStyle; // select whatever selected
//			this.setDocStyleDirty(false);
			
			//	update window title
			this.setTitle("Edit Document Style Template '" + this.selectedDocStyle.name + "'");
			
			//	update field values and defaults
			this.updateParamValueSource();
			this.updateParamTransferButtons();
			this.checkDocStyleDirty();
			
			//	re-focus current field
			if (this.activeParamPanel instanceof FocusableParamPanel)
				((FocusableParamPanel) this.activeParamPanel).handleFocusGained();
		}
		
		void selectDocStyle(DocStyleSettings docStyle) {
			for (int h = 0; h < this.headerTabs.getComponentCount(); h++) {
				DocStyleHeaderTab dsht = ((DocStyleHeaderTab) this.headerTabs.getComponentAt(h));
				if (dsht.docStyle.equals(docStyle)) {
					this.headerTabs.setSelectedComponent(dsht);
					break;
				}
			}
		}
		
		void updateParamTransferButtons() {
			if ((this.selectedDocStyle != null) && (this.selectedDocStyle.inheritedSettings != null)) {
				//	only enable parent-bound value transfer if (a) values available and (b) only child
				//	==> if there are other children, values added to parent might shadow ones from higher up
				boolean allowTransferToParent = true;
				if (this.selectedDocStyle.data.hasSubset(this.paramGroupName)) {
					Settings paramGroupData = this.selectedDocStyle.data.getSubset(this.paramGroupName);
					String[] paramGroupDataKeys = paramGroupData.getLocalKeys();
					if (paramGroupDataKeys.length == 0)
						allowTransferToParent = false; // nothing to transfer
					if (this.selectedDocStyle.inheritedSettings.inheritingSettings.size() == 1) { /* only child, safe to transfer */ }
					else if (this.selectedDocStyle.inheritedSettings.inheritedSettings == null) { /* no source for values to shadow */ }
					else {
						DocStyleSettings gpDocStyle = this.selectedDocStyle.inheritedSettings.inheritedSettings;
						for (int k = 0; k < paramGroupDataKeys.length; k++)
							if (gpDocStyle.getSetting(this.paramGroupName + "." + paramGroupDataKeys[k]) != null) {
								allowTransferToParent = false; // cannot shadow value in grand parent
								break;
							}
					}
				}
				else allowTransferToParent = false; // nothing to transfer at all
				this.copyToParentButton.setEnabled(allowTransferToParent);
				this.moveToParentButton.setEnabled(allowTransferToParent);
				
				//	always allow copying from parent, but moving only as only child
				if (this.selectedDocStyle.inheritedSettings.data.hasSubset(this.paramGroupName)) {
					Settings pParamGroupData = this.selectedDocStyle.inheritedSettings.data.getSubset(this.paramGroupName);
					String[] pParamGroupDataKeys = pParamGroupData.getLocalKeys();
					if (pParamGroupDataKeys.length == 0) /* nothing to transfer at all */ {
						this.copyFromParentButton.setEnabled(false);
						this.moveFromParentButton.setEnabled(false);
					}
					else {
						this.copyFromParentButton.setEnabled(true); // we can always copy
						this.moveFromParentButton.setEnabled(this.selectedDocStyle.inheritedSettings.inheritingSettings.size() == 1); // we can only move if we're only child
					}
				}
				else /* nothing to transfer at all */ {
					this.copyFromParentButton.setEnabled(false);
					this.moveFromParentButton.setEnabled(false);
				}
			}
			else {
				this.moveFromParentButton.setEnabled(false);
				this.copyFromParentButton.setEnabled(false);
				this.copyToParentButton.setEnabled(false);
				this.moveToParentButton.setEnabled(false);
			}
		}
		
		void notifyDocumentClosed(String docId) {
			if ((this.targetDocPanel != null) && this.targetDocPanel.document.docId.equals(docId))
				this.targetDocPanel = null;
			if ((this.testDoc != null) && this.testDoc.docId.equals(docId)) {
				this.testDoc = null;
				this.testDocTokens = null;
			}
		}
		
//		void setDocStyleDirty(boolean dirty) {
//			this.docStyleDirty = dirty;
//			this.saveDocStyleButton.setEnabled(dirty);
//		}
		void checkDocStyleDirty() {
			this.saveDocStyleButton.setEnabled((this.selectedDocStyle != null) && this.selectedDocStyle.isInheritanceChainDirty());
			this.updatePublishButtons();
		}
		
		void askSaveIfDirty() {
//			if ((this.docStyleName == null) || (this.docStyle == null))
			if (this.headDocStyle == null)
				return;
//			if (!this.docStyleDirty)
			if (!this.headDocStyle.isInheritanceChainDirty())
				return;
			int choice = JOptionPane.showConfirmDialog(this, ("Document style template '" + this.headDocStyle.name + "' has been modified. Save Changes?"), "Save Document Style?", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (choice == JOptionPane.YES_OPTION)
				this.saveDocStyle(this.headDocStyle);
		}
		
		private void updatePublishButtons() {
			if (this.headDocStyle.isInheritanceChainDirty()) {
				this.publishDocStyleLocButton.setEnabled(false);
				this.publishDocStyleDssButton.setEnabled(false);
			}
			else {
				this.publishDocStyleLocButton.setEnabled(this.headDocStyle.needPublishLoc());
				this.publishDocStyleDssButton.setEnabled(dssAvailable && this.headDocStyle.needPublishDss());
			}
		}
		
		void saveDocStyle(DocStyleSettings docStyle) {
			if (docStyle == null)
				docStyle = this.selectedDocStyle;
//			if ((this.docStyleName == null) || (this.docStyle == null))
			if (docStyle == null)
				return;
//			if (!this.docStyleDirty)
			if (!docStyle.isInheritanceChainDirty())
				return;
//			this.docStyle.setName(this.docStyleName);
			ImDocumentStyleManager.this.storeDocStyle(docStyle);
//			this.setDocStyleDirty(false);
			this.checkDocStyleDirty();
		}
		
		void publishDocStyleLoc() {
//			if ((this.docStyleName == null) || (this.docStyle == null))
			if (this.headDocStyle == null)
				return;
//			if (this.docStyleDirty)
//				return;
			if (this.headDocStyle.isInheritanceChainDirty()) {
				DialogFactory.alert(("Document style '" + this.headDocStyle.name + "' has unsaved changes. Please save them before publishing."), "Save Document Style Before Publishing", JOptionPane.ERROR_MESSAGE);
				this.publishDocStyleLocButton.setEnabled(false);
				this.publishDocStyleDssButton.setEnabled(false);
				return;
			}
			ImDocumentStyleManager.this.publishDocStyleLoc(this.headDocStyle);
			this.updatePublishButtons();
		}
		
		void publishDocStyleDss() {
//			if ((this.docStyleName == null) || (this.docStyle == null))
			if (this.headDocStyle == null)
				return;
//			if (this.docStyleDirty)
//				return;
			if (this.headDocStyle.isInheritanceChainDirty()) {
				DialogFactory.alert(("Document style '" + this.headDocStyle.name + "' has unsaved changes. Please save them before publishing."), "Save Document Style Before Publishing", JOptionPane.ERROR_MESSAGE);
				this.publishDocStyleLocButton.setEnabled(false);
				this.publishDocStyleDssButton.setEnabled(false);
				return;
			}
			ImDocumentStyleManager.this.publishDocStyleDss(this.headDocStyle);
			this.updatePublishButtons();
		}
		
		void notifyDssUpdatePermissionLacking() {
			this.publishDocStyleDssButton.setEnabled(dssAvailable);
		}
		
		private DocStyleParameterTracker parameterTracker = null;
		
		void propertyDataRetrieved(String key, String value, boolean fromLiveData) {
			if (this.parameterTracker != null)
				this.parameterTracker.propertyDataRetrieved(key, value, fromLiveData);
		}
		
		void trackPropertyDataRetrieval() {
			if (this.parameterTracker == null) {
				this.parameterTracker = new DocStyleParameterTracker();
				this.parameterTracker.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent we) {
						parameterTracker = null; // make way
					}
				});
			}
			else this.parameterTracker.getDialog().toFront();
			this.parameterTracker.setVisible(true);
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.DisplayExtension#isActive()
		 */
		public boolean isActive() {
			return true;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.DisplayExtension#getExtensionGraphics(de.uka.ipd.idaho.im.ImPage, de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel)
		 */
		public DisplayExtensionGraphics[] getExtensionGraphics(ImPage page, ImDocumentMarkupPanel idmp) {
			if (idmp.document == this.testDoc) { /* we're working with this one */ }
			else if ((this.testDoc != null) && this.testDoc.docId.equals(idmp.document.docId)) { /* we're still working with this one */ }
//			else if ((this.docStyleName != null) && this.docStyleName.equals(idmp.document.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE))) { /* this one fits the template we're working on */ }
			else if ((this.headDocStyle != null) && this.headDocStyle.id.equals(idmp.document.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE))) { /* this one fits the template we're working on */ }
			else if ((this.headDocStyle != null) && this.headDocStyle.name.equals(idmp.document.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE))) { /* this one fits the template we're working on */ }
			else return NO_DISPLAY_EXTENSION_GRAPHICS; // this one is none of our business
//			System.out.println("Getting display '" + this.paramGroupName + "' extension graphics for page " + page.pageId);
//			System.out.println(" - active parameter description is " + this.activeParamDescription);
//			System.out.println(" - active parameter panel is " + this.activeParamPanel);
//			System.out.println(" - parameter group description is " + this.paramGroupDesciption);
			
			//	highlight current field in any custom way available
			if (this.activeParamDescription instanceof DisplayExtension)
				return ((DisplayExtension) this.activeParamDescription).getExtensionGraphics(page, idmp);
			
			//	highlight content of current field, or what it represents or matches
			else if (this.activeParamPanel != null)
				return this.activeParamPanel.getDisplayExtensionGraphics(page);
			
			//	highlight match of group as a whole (anchors, document metadata, etc.) if parameter description exists and represents a display extension
			else if (this.paramGroupDescription instanceof DisplayExtension)
				return ((DisplayExtension) this.paramGroupDescription).getExtensionGraphics(page, idmp);
			
			//	nothing to show right now
			else return NO_DISPLAY_EXTENSION_GRAPHICS;
		}
		
		private UseParamPanel activeParamPanel = null;
		private ParameterDescription activeParamDescription = null;
		
		void setActiveParamPanel(UseParamPanel activeParamPanel) {
			this.activeParamPanel = activeParamPanel;
			if (this.activeParamPanel == null)
				this.activeParamDescription = null;
			else this.activeParamDescription = ((this.paramGroupDescription == null) ? null : this.paramGroupDescription.getParameterDescription(this.activeParamPanel.docStyleParamName));
			ggImagine.notifyDisplayExtensionsModified(null);
		}
		
		void paramUsageChanged(UseParamPanel paramPanel) {
			this.updateParamStates(paramPanel);
		}
		
		void paramValueChanged(UseParamPanel paramPanel) {
			this.updateParamStates(paramPanel);
		}
		
		private void updateParamValueSource() {
			for (int p = 0; p < this.paramPanels.size(); p++) {
				UseParamPanel upp = ((UseParamPanel) this.paramPanels.get(p));
				upp.apiUpdateValueSource(true);
			}
			this.updateParamStates(null);
		}
		
		private void updateParamStates(UseParamPanel paramPanel) {
			if (this.paramGroupDescription == null)
				return;
			HashSet requiredParamNames = new HashSet();
			HashSet excludedParamNames = new HashSet();
			for (int p = 0; p < this.paramPanels.size(); p++) {
				UseParamPanel upp = ((UseParamPanel) this.paramPanels.get(p));
				String lpn = upp.docStyleParamName.substring(upp.docStyleParamName.lastIndexOf(".") + ".".length());
				ParameterDescription pd = this.paramGroupDescription.getParameterDescription(lpn);
				if (pd == null)
					continue;
				if (pd.isRequired())
					requiredParamNames.add(lpn);
				if (!upp.useParam.isSelected() && !upp.useParam.isDefaulted())
					continue;
				String[] rpns = pd.getRequiredParameters();
				if (rpns != null)
					requiredParamNames.addAll(Arrays.asList(rpns));
				String[] epns = pd.getExcludedParameters();
				if (epns != null)
					excludedParamNames.addAll(Arrays.asList(epns));
			}
			HashSet valueRequiredParamNames = new HashSet();
			HashSet valueExcludedParamNames = new HashSet();
			for (int p = 0; p < this.paramPanels.size(); p++) {
				UseParamPanel upp = ((UseParamPanel) this.paramPanels.get(p));
				String lpn = upp.docStyleParamName.substring(upp.docStyleParamName.lastIndexOf(".") + ".".length());
				if (excludedParamNames.contains(lpn))
					continue;
				ParameterDescription pd = this.paramGroupDescription.getParameterDescription(lpn);
				if (pd == null)
					continue;
				String pv = (upp.useParam.isDefaulted() ? upp.useParam.defaultValue : upp.getValue()); // here, we need the _effective_ value (inherited or not)
				String[] rpns = pd.getRequiredParameters(pv);
				if (rpns != null)
					valueRequiredParamNames.addAll(Arrays.asList(rpns));
				String[] epns = pd.getExcludedParameters(pv);
				if (epns != null)
					valueExcludedParamNames.addAll(Arrays.asList(epns));
			}
			for (int p = 0; p < this.paramPanels.size(); p++) {
				UseParamPanel upp = ((UseParamPanel) this.paramPanels.get(p));
				if (upp == paramPanel)
					continue;
				String lpn = upp.docStyleParamName.substring(upp.docStyleParamName.lastIndexOf(".") + ".".length());
				if (valueExcludedParamNames.contains(lpn))
					upp.setExcluded(true);
				else if (valueRequiredParamNames.contains(lpn))
					upp.setRequired(true);
				else if (excludedParamNames.contains(lpn))
					upp.setExcluded(true);
				else if (requiredParamNames.contains(lpn))
					upp.setRequired(true);
				else upp.setExcluded(false);
			}
			this.validate();
			this.repaint();
		}
		
		private class ParamTreeNode implements Comparable {
			final String prefix;
			final ParamTreeNode parent;
			private ArrayList children = null;
			private TreeSet paramNames = null;
			ParamTreeNode(String prefix, ParamTreeNode parent) {
				this.prefix = prefix;
				this.parent = parent;
				paramTreeNodesByPrefix.put(this.prefix, this);
			}
			int getChildCount() {
				return ((this.children == null) ? 0 : this.children.size());
			}
			int getChildIndex(ParamTreeNode child) {
				return ((this.children == null) ? -1 : this.children.indexOf(child));
			}
			void addChild(ParamTreeNode child) {
				if (this.children == null)
					this.children = new ArrayList(3);
				this.children.add(child);
			}
			void removeChild(ParamTreeNode child) {
				if (this.children != null)
					this.children.remove(child);
			}
			void sortChildren() {
				if (this.children == null)
					return;
				Collections.sort(this.children);
				for (int c = 0; c < this.children.size(); c++)
					((ParamTreeNode) this.children.get(c)).sortChildren();
			}
			void clearChildren() {
				if (this.children != null)
					this.children.clear();
			}
			void addParamName(String paramName) {
				if (this.paramNames == null)
					this.paramNames = new TreeSet();
				this.paramNames.add(paramName);
			}
			
			public String toString() {
				return getParameterGroupLabel(this.prefix, true);
			}
			
			public int compareTo(Object obj) {
				ParamTreeNode ptn = ((ParamTreeNode) obj);
				if ((this.children == null) != (ptn.children == null))
					return ((this.children == null) ? -1 : 1);
				return this.prefix.compareTo(ptn.prefix);
			}
		}
		
		private TreeMap paramTreeNodesByPrefix = new TreeMap();
		private ParamTreeNode paramTreeRoot = new ParamTreeNode("", null);
		private ArrayList paramTreeModelListeners = new ArrayList(2);
		private TreeModel paramTreeModel = new TreeModel() {
			public Object getRoot() {
				return paramTreeRoot;
			}
			public boolean isLeaf(Object node) {
				return (((ParamTreeNode) node).getChildCount() == 0);
			}
			public int getChildCount(Object parent) {
				return ((ParamTreeNode) parent).getChildCount();
			}
			public Object getChild(Object parent, int index) {
				return ((ParamTreeNode) parent).children.get(index);
			}
			public int getIndexOfChild(Object parent, Object child) {
				return ((ParamTreeNode) parent).getChildIndex((ParamTreeNode) child);
			}
			public void valueForPathChanged(TreePath path, Object newValue) { /* we're not changing the tree */ }
			public void addTreeModelListener(TreeModelListener tml) {
				paramTreeModelListeners.add(tml);
			}
			public void removeTreeModelListener(TreeModelListener tml) {
				paramTreeModelListeners.remove(tml);
			}
		};
		private void updateParamTree() {
			this.paramTreeRoot.sortChildren();
			ArrayList expandedPaths = new ArrayList();
			for (int r = 0; r < this.paramTree.getRowCount(); r++) {
				if (this.paramTree.isExpanded(r))
					expandedPaths.add(this.paramTree.getPathForRow(r));
			}
			TreeModelEvent tme = new TreeModelEvent(this, new TreePath(this.paramTreeRoot));
			for (int l = 0; l < paramTreeModelListeners.size(); l++)
				((TreeModelListener) paramTreeModelListeners.get(l)).treeStructureChanged(tme);
			for (int r = 0; r < expandedPaths.size(); r++)
				this.paramTree.expandPath((TreePath) expandedPaths.get(r));
			this.paramTree.validate();
			this.paramTree.repaint();
		}
		
		void paramTreeNodeSelected(final ParamTreeNode ptn) {
			
			//	remember selected param group
			this.paramGroupName = ptn.prefix;
			this.paramGroupDescription = getParameterGroupDescription(this.paramGroupName);
			this.paramPanels.clear();
			
			//	clear param panel
			this.paramPanel.removeAll();
			
			//	update param panel
			if (ptn.paramNames != null) {
				
				//	add group label and description if group description present
				if (this.paramGroupDescription != null) {
					JPanel titlePanel = new JPanel(new BorderLayout(), true);
					if (this.paramGroupDescription.getLabel() != null) {
						JLabel title = new JLabel("<HTML><B>" + this.paramGroupDescription.getLabel() + "</B></HTML>");
						title.setOpaque(true);
						title.setBackground(Color.WHITE);
						titlePanel.add(title, BorderLayout.NORTH);
					}
					if (this.paramGroupDescription.getDescription() != null) {
						JLabel description = new JLabel("<HTML>" + this.paramGroupDescription.getDescription() + "</HTML>");
						description.setOpaque(true);
						description.setBackground(Color.WHITE);
						titlePanel.add(description, BorderLayout.CENTER);
					}
					titlePanel.setBackground(Color.WHITE);
					titlePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.WHITE, 3), BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED, 1), BorderFactory.createLineBorder(Color.WHITE, 5))));
					this.paramPanel.add(titlePanel, BorderLayout.NORTH);
				}
				
				//	add parameter fields
				JPanel paramPanel = new JPanel(true) {
					public Dimension getPreferredSize() {
						Dimension ps = super.getPreferredSize();
						ps.width = (DocStyleEditor.this.paramPanel.getWidth() - 50);
						return ps;
					}
				};
				paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.Y_AXIS));
				
				//	for anchors, make damn sure to show all the parameters (anchor names are custom and thus cannot be learned)
				if (ptn.prefix.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_FONT_SIZE_PROPERTY);
					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_MINIMUM_FONT_SIZE_PROPERTY);
					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_MAXIMUM_FONT_SIZE_PROPERTY);
					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_IS_BOLD_PROPERTY);
					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_IS_ITALICS_PROPERTY);
					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_IS_ALL_CAPS_PROPERTY);
					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_PATTERN_PROPERTY);
					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_AREA_PROPERTY);
					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.IS_REQUIRED_PROPERTY);
				}
				for (Iterator pnit = ptn.paramNames.iterator(); pnit.hasNext();) {
					String pn = ((String) pnit.next());
					UseParamPanel upp = this.getParamValueField(pn);
					paramPanel.add(upp);
					this.paramPanels.add(upp);
				}
				JPanel paramPanelTray = new JPanel(new BorderLayout(), true);
				paramPanelTray.add(paramPanel, BorderLayout.NORTH);
				
				//	make the whole thing scroll
				JScrollPane paramPanelBox = new JScrollPane(paramPanelTray);
				paramPanelBox.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				paramPanelBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				paramPanelBox.getVerticalScrollBar().setBlockIncrement(50);
				paramPanelBox.getVerticalScrollBar().setUnitIncrement(50);
				this.paramPanel.add(paramPanelBox, BorderLayout.CENTER);
				
				//	add parameter group buttons
				JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 5), true);
				buttonPanel.setBorder(BorderFactory.createLineBorder(buttonPanel.getBackground(), 5));
				
				//	add anchor test facilities
				if (this.paramGroupName.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
					JButton testButton = new JButton("Test Anchor");
					testButton.setBorder(BorderFactory.createRaisedBevelBorder());
					testButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							testAnchor(ptn);
						}
					});
					buttonPanel.add(testButton);
					JButton removeButton = new JButton("Remove Anchor");
					removeButton.setBorder(BorderFactory.createRaisedBevelBorder());
					removeButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							removeAnchor(ptn);
						}
					});
					buttonPanel.add(removeButton);
				}
				
				//	add group test button if group description is testable or can visualize its content
				else if ((this.paramGroupDescription instanceof DisplayExtension) || (this.paramGroupDescription instanceof DisplayExtension)) {
					JButton testButton = new JButton("Test");
					testButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(testButton.getBackground(), 5), BorderFactory.createRaisedBevelBorder()));
					testButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							if (paramGroupDescription instanceof TestableElement) {
								ImDocumentStyle dspg = selectedDocStyle.getSubset(paramGroupName).asDocStyle();
								((TestableElement) paramGroupDescription).test(dspg); // test the parameter group description
							}
							if (paramGroupDescription instanceof DisplayExtension)
								setActiveParamPanel(null); // this triggers update of display extensions
						}
					});
					buttonPanel.add(testButton);
				}
				
				//	add buttons for moving around parameters right here
				buttonPanel.add(this.paramValueTransferButtonPanel);
				
				this.paramPanel.add(buttonPanel, BorderLayout.SOUTH);
			}
			
			//	make changes show
			this.paramPanel.validate();
			this.paramPanel.repaint();
			this.updateParamStates(null);
			this.updateParamTransferButtons();
			
			//	update display extensions
			this.setActiveParamPanel(null);
		}
		
		void transferParamsFromParent(boolean move) {
			if ((this.selectedDocStyle != null) && (this.selectedDocStyle.inheritedSettings != null))
				this.transferParams(this.selectedDocStyle.inheritedSettings, this.selectedDocStyle, move);
			else this.updateParamTransferButtons(); // however these guy were enabled
		}
		
		void transferParamsToParent(boolean move) {
			if ((this.selectedDocStyle != null) && (this.selectedDocStyle.inheritedSettings != null))
				this.transferParams(this.selectedDocStyle, this.selectedDocStyle.inheritedSettings, move);
			else this.updateParamTransferButtons(); // however these guy were enabled
		}
		
		void transferParams(DocStyleSettings source, DocStyleSettings target, boolean move) {
			Settings sourceSet = source.data.getSubset(this.paramGroupName);
			if (sourceSet.isEmpty())
				return;
			String[] sourceDataKeys = sourceSet.getLocalKeys();
			if (sourceDataKeys.length == 0)
				return;
			Settings targetSet = target.data.getSubset(this.paramGroupName);
			
			JPanel paramTransferPanel = new JPanel(new GridLayout(0, 1), true);
			paramTransferPanel.add(new JLabel(("Select the parameters whose values to " + (move ? "move" : "copy") + "\r\nHover over the labels to see the values. Labels in bold indicate conflicts."), JLabel.LEFT));
			ParamValueTransferCheckBox[] transferParamValue = new ParamValueTransferCheckBox[sourceDataKeys.length];
			for (int k = 0; k < sourceDataKeys.length; k++) {
				String label = ((this.paramGroupDescription == null) ? null : this.paramGroupDescription.getParamLabel(sourceDataKeys[k]));
				transferParamValue[k] = new ParamValueTransferCheckBox(((label == null) ? sourceDataKeys[k] : label), sourceSet.getSetting(sourceDataKeys[k]), target.name, targetSet.getSetting(sourceDataKeys[k]));
				paramTransferPanel.add(transferParamValue[k]);
			}
			int choice = DialogFactory.confirm(paramTransferPanel, ("Select Parameters Whose Values to " + (move ? "Move" : "Copy")), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (choice != JOptionPane.OK_OPTION)
				return;
			
			for (int k = 0; k < sourceDataKeys.length; k++)
				if (transferParamValue[k].isSelected()) {
					String paramName = (this.paramGroupName + "." + sourceDataKeys[k]);
					String paramValue = source.getSetting(paramName);
					if (move)
						source.removeSetting(paramName, false);
					target.setSetting(paramName, paramValue);
				}
			
			this.updateParamTransferButtons();
			this.updateParamValueSource();
			this.checkDocStyleDirty();
		}
		
		private class ParamValueTransferCheckBox extends JCheckBox {
			ParamValueTransferCheckBox(String label, String sourceValue, String targetName, String targetValue) {
				super(((targetValue == null) ? label : ("<HTML><B>" + xmlGrammar.escape(label) + "</B></HTML>")), (targetValue == null));
				this.setToolTipText("Value: " + sourceValue + ((targetValue == null) ? "" : (" (value in '" + targetName + "': " + targetValue)));
			}
		}
		
		/* TODO Maybe provide function to import data from arbitrary (non-ancestor ???) document style
		 * ==> should help aggregate existing copies into inheritance trees
		 * ==> provide checkbox for which side takes precedence ...
		 * ==> ... and visually indicate respective parameters in (to-be-built) selector dialog
		 */
		
		void testAnchor(ParamTreeNode ptn) {
			
			//	get anchor settings
			DocStyleSettings anchorData = this.selectedDocStyle.getSubset(ptn.prefix);
			
			//	get bounding box
			BoundingBox area = BoundingBox.parse(anchorData.getSetting(PageFeatureAnchor.TARGET_AREA_PROPERTY));
			if (area == null)
				return;
			
			//	get pattern
			String pattern = anchorData.getSetting(PageFeatureAnchor.TARGET_PATTERN_PROPERTY);
			if (pattern == null)
				return;
			
			//	get font sizes and perform test
			try {
				ArrayList matchLog = new ArrayList();
				int anchorMaxPageId = Integer.parseInt(this.selectedDocStyle.getSetting("anchor.maxPageId", this.selectedDocStyle.getSetting("layout.coverPageCount", "0")));
				boolean anchorMatch = false;
				for (int p = 0; p <= anchorMaxPageId; p++) {
					matchLog.add("Testing page " + p + ":");
					ImPage testPage = this.testDoc.getPage((this.testDoc.getFirstPageId() + p));
					if (testPage == null)
						continue;
					anchorMatch = PageFeatureAnchor.matches(testPage,
							area,
							Integer.parseInt(anchorData.getSetting(PageFeatureAnchor.TARGET_MINIMUM_FONT_SIZE_PROPERTY, anchorData.getSetting(PageFeatureAnchor.TARGET_FONT_SIZE_PROPERTY, "0"))),
							Integer.parseInt(anchorData.getSetting(PageFeatureAnchor.TARGET_MAXIMUM_FONT_SIZE_PROPERTY, anchorData.getSetting(PageFeatureAnchor.TARGET_FONT_SIZE_PROPERTY, "72"))),
							"true".equals(anchorData.getSetting(PageFeatureAnchor.TARGET_IS_BOLD_PROPERTY)),
							"true".equals(anchorData.getSetting(PageFeatureAnchor.TARGET_IS_ITALICS_PROPERTY)),
							"true".equals(anchorData.getSetting(PageFeatureAnchor.TARGET_IS_ALL_CAPS_PROPERTY)),
							pattern,
							matchLog);
//					anchorMatch = DocumentStyleProvider.anchorMatches(this.testDoc,
//							(this.testDoc.getFirstPageId() + p),
//							area,
//							Integer.parseInt(anchorParamList.getSetting("minFontSize", anchorParamList.getSetting("fontSize", "0"))),
//							Integer.parseInt(anchorParamList.getSetting("maxFontSize", anchorParamList.getSetting("fontSize", "72"))),
//							"true".equals(anchorParamList.getSetting("isBold")),
//							"true".equals(anchorParamList.getSetting("isItalics")),
//							"true".equals(anchorParamList.getSetting("isAllCaps")),
//							pattern,
//							matchLog);
					if (anchorMatch)
						break;
				}
				String anchorName = ptn.prefix.substring(ptn.prefix.lastIndexOf('.') + ".".length());
				StringBuffer anchorMatchLog = new StringBuffer();
				for (int l = 0; l < matchLog.size(); l++) {
					anchorMatchLog.append("\r\n");
					anchorMatchLog.append((String) matchLog.get(l));
				}
				JOptionPane.showMessageDialog(this, ("This document " + (anchorMatch ? " matches " : " does not match ") + " anchor '" + anchorName + "':" + anchorMatchLog.toString()), "Anchor Match Test", (anchorMatch ? JOptionPane.PLAIN_MESSAGE : JOptionPane.ERROR_MESSAGE));
			} catch (NumberFormatException nfe) {}
		}
		
		void removeAnchor(ParamTreeNode ptn) {
			
			//	remove settings
			for (Iterator pnit = ptn.paramNames.iterator(); pnit.hasNext();)
				this.headDocStyle.removeSetting(((String) pnit.next()), true);
			
			//	remove node
			ParamTreeNode pptn = ptn.parent;
			pptn.removeChild(ptn);
			
			//	update param tree
			this.updateParamTree();
			
			//	select path of current tree node
			ArrayList pptnPath = new ArrayList();
			for (;pptn != null; pptn = pptn.parent)
				pptnPath.add(0, pptn);
			if (pptnPath.size() != 0)
				this.paramTree.setSelectionPath(new TreePath(pptnPath.toArray()));
			
			//	enable save button
			this.checkDocStyleDirty();
		}
		
		String paramGroupName;
		ParameterGroupDescription paramGroupDescription;
		ArrayList paramPanels = new ArrayList();
		
		void setParamGroupName(String pgn) {
			
			//	update fields for any parameters in group
			for (Iterator pnit = this.paramValueFields.keySet().iterator(); pnit.hasNext();) {
				String pn = ((String) pnit.next());
				if (pn.lastIndexOf('.') != pgn.length())
					continue;
				if (!pn.startsWith(pgn))
					continue;
				UseParamPanel pvf = ((UseParamPanel) this.paramValueFields.get(pn));
				pvf.apiUpdateValueSource(true);
//				String pv = this.selectedDocStyle.getSetting(pn);
//				pvf.setValue((pv == null) ? "" : pv);
//				pvf.useParam.setSelected(pv != null);
			}
			
			//	no further updates required
			if (pgn.equals(this.paramGroupName))
				return;
			
			//	set param group name and get corresponding tree node
			this.paramGroupName = pgn;
			this.paramGroupDescription = getParameterGroupDescription(this.paramGroupName);
			ParamTreeNode ptn = ((ParamTreeNode) this.paramTreeNodesByPrefix.get(this.paramGroupName));
			
			//	if we're creating an anchor, and only then, the group name is null
			if (ptn == null) {
				ParamTreeNode pptn = ((ParamTreeNode) this.paramTreeNodesByPrefix.get(Anchor.ANCHOR_PREFIX));
				ptn = new ParamTreeNode(pgn, pptn);
				ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_FONT_SIZE_PROPERTY);
				ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_MINIMUM_FONT_SIZE_PROPERTY);
				ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_MAXIMUM_FONT_SIZE_PROPERTY);
				ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_IS_BOLD_PROPERTY);
				ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_IS_ITALICS_PROPERTY);
				ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_IS_ALL_CAPS_PROPERTY);
				ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_PATTERN_PROPERTY);
				ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_AREA_PROPERTY);
				pptn.addChild(ptn);
				this.updateParamTree();
			}
			
			//	select path of current tree node
			ArrayList ptnPath = new ArrayList();
			for (;ptn != null; ptn = ptn.parent)
				ptnPath.add(0, ptn);
			if (ptnPath.size() != 0)
				this.paramTree.setSelectionPath(new TreePath(ptnPath.toArray()));
		}
		
		private TreeMap paramValueFields = new TreeMap();
		
		private UseParamPanel getParamValueField(String pn) {
			UseParamPanel pvf = ((UseParamPanel) this.paramValueFields.get(pn));
			if (pvf == null) {
				pvf = this.createParamValueField(pn);
				this.paramValueFields.put(pn, pvf);
			}
			pvf.apiUpdateValueSource(true);
			return pvf;
		}
		
		private class ParamToggleListener implements ItemListener {
			private UseParamPanel upp;
			ParamToggleListener(UseParamPanel upp) {
				this.upp = upp;
			}
			public void itemStateChanged(ItemEvent ie) {
//				String id = ((this.upp.useParam.isSelected() ? "on" : (this.upp.useParam.isDefaulted() ? "default" : "off")) + " (" + Math.random() + ")");
//				System.out.println("Parameter '" + this.upp.docStyleParamName + "' toggled to " + id);
				if (this.upp.isUpdating()) {
//					System.out.println(" ==> ignored as part of update " + id);
					return;
				}
				if (this.upp.useParam.isSelected()) {
					String dspv = this.upp.getValue();
					if (this.upp.verifyValue(dspv))
						selectedDocStyle.setSetting(this.upp.docStyleParamName, dspv);
				}
				else selectedDocStyle.removeSetting(this.upp.docStyleParamName, false);
				this.upp.apiUpdateValueSource(false);
//				System.out.println(" ==> processed " + id);
			}
		}
		
		private UseParamPanel createParamValueField(final String pn) {
			final Class pvc = getParamValueClass(pn);
			String pv = ((this.selectedDocStyle == null) ? null : this.selectedDocStyle.data.getSetting(pn));
			boolean ps = (pv != null);
			
			String pl = null;
			String pd = null;
			String[] pvs = null;
			String[] pvls = null;
			if (this.paramGroupDescription != null) {
				String lpn = pn.substring(pn.lastIndexOf('.') + ".".length());
				pl = this.paramGroupDescription.getParamLabel(lpn);
				pd = this.paramGroupDescription.getParamDescription(lpn);
				if (pv == null)
					pv = this.paramGroupDescription.getParamDefaultValue(lpn);
				pvs = this.paramGroupDescription.getParamValues(lpn);
				if (pvs != null) {
					pvls = new String[pvs.length];
					for (int v = 0; v < pvs.length; v++)
						pvls[v] = this.paramGroupDescription.getParamValueLabel(lpn, pvs[v]);
				}
			}
			if (pl == null)
				pl = paramNamesLabels.getProperty(pn, pn);
			
			/* TODO use JRadioButtons for input if:
			 * - values fixed (no custom input)
			 * - three (or maybe four) or fewer values
			 * ==> does away with score of two-option non-editable drop-downs (just looks overly complex ...)
			 */
			
			//	boolean, use plain checkbox
			if (Boolean.class.getName().equals(pvc.getName())) {
				UseBooleanPanel pvf = new UseBooleanPanel(this, pn, pl, pd, "true".equals(pv)) {
					void notifyModified() {
//						setDocStyleDirty(true);
						checkDocStyleDirty();
						super.notifyModified();
					}
				};
				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
				return pvf;
			}
			
			//	number, use string field
			else if (Integer.class.getName().equals(pvc.getName())) {
				if ((pvs != null) && (pvs.length != 0)) {
					for (int v = 0; v < pvs.length; v++) try {
						Integer.parseInt(pvs[v]);
					}
					catch (RuntimeException re) {
						pvs = null;
						break;
					}
				}
				UseParamPanel pvf;
				if (pvs == null)
					pvf = new UseStringPanel(this, pn, pl, pd, ps, ((pv == null) ? "" : pv), false, false, false) {
						boolean verifyValue(String value) {
							try {
								Integer.parseInt(this.getValue());
								return true;
							}
							catch (NumberFormatException nfe) {
								return false;
							}
						}
						void stringChanged(String string) {
							if (string.length() == 0)
								this.useParam.setSelected(false);
							else if (this.useParam.isSelected() && this.verifyValue(string))
								selectedDocStyle.setSetting(this.docStyleParamName, string);
//							setDocStyleDirty(true);
							checkDocStyleDirty();
						}
						DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
							if (this.docStyleParamName.endsWith(".fontSize") || this.docStyleParamName.endsWith("FontSize"))
								return getFontSizeVisualizationGraphics(this.parent, page, this.getDisplayedValue());
							if (this.docStyleParamName.endsWith(".margin") || this.docStyleParamName.endsWith("Margin"))
								return getMarginVisualizationGraphics(this.parent, page, this.getDisplayedValue());
							//	TODO think of more
							return NO_DISPLAY_EXTENSION_GRAPHICS;
						}
					};
				else pvf = new UseStringOptionPanel(this, pn, pvs, pvls, pl, pd, ps, ((pv == null) ? "" : pv), false) {
					void stringChanged(String string) {
						if (string.length() == 0)
							this.useParam.setSelected(false);
						else if (this.useParam.isSelected() && this.verifyValue(string))
							selectedDocStyle.setSetting(this.docStyleParamName, string);
//						setDocStyleDirty(true);
						checkDocStyleDirty();
					}
				};
				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
				return pvf;
			}
			else if (Float.class.getName().equals(pvc.getName())) {
				if ((pvs != null) && (pvs.length != 0)) {
					for (int v = 0; v < pvs.length; v++) try {
						Float.parseFloat(pvs[v]);
					}
					catch (RuntimeException re) {
						pvs = null;
						break;
					}
				}
				UseParamPanel pvf;
				if (pvs == null)
					pvf = new UseStringPanel(this, pn, pl, pd, ps, ((pv == null) ? "" : pv), false, false, false) {
						boolean verifyValue(String value) {
							try {
								Float.parseFloat(this.getValue());
								return true;
							}
							catch (NumberFormatException nfe) {
								return false;
							}
						}
						void stringChanged(String string) {
							if (string.length() == 0)
								this.useParam.setSelected(false);
							else if (this.useParam.isSelected() && this.verifyValue(string))
								selectedDocStyle.setSetting(this.docStyleParamName, string);
//							setDocStyleDirty(true);
							checkDocStyleDirty();
						}
						DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
							return NO_DISPLAY_EXTENSION_GRAPHICS;
						}
					};
				else pvf = new UseStringOptionPanel(this, pn, pvs, pvls, pl, pd, ps, ((pv == null) ? "" : pv), false) {
					void stringChanged(String string) {
						if (string.length() == 0)
							this.useParam.setSelected(false);
						else if (this.useParam.isSelected() && this.verifyValue(string))
							selectedDocStyle.setSetting(this.docStyleParamName, string);
//						setDocStyleDirty(true);
						checkDocStyleDirty();
					}
				};
				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
				return pvf;
			}
			else if (Double.class.getName().equals(pvc.getName())) {
				if ((pvs != null) && (pvs.length != 0)) {
					for (int v = 0; v < pvs.length; v++) try {
						Double.parseDouble(pvs[v]);
					}
					catch (RuntimeException re) {
						pvs = null;
						break;
					}
				}
				UseParamPanel pvf;
				if (pvs == null)
					pvf = new UseStringPanel(this, pn, pl, pd, ps, ((pv == null) ? "" : pv), false, false, false) {
						boolean verifyValue(String value) {
							try {
								Double.parseDouble(this.getValue());
								return true;
							}
							catch (NumberFormatException nfe) {
								return false;
							}
						}
						void stringChanged(String string) {
							if (string.length() == 0)
								this.useParam.setSelected(false);
							else if (this.useParam.isSelected() && this.verifyValue(string))
								selectedDocStyle.setSetting(this.docStyleParamName, string);
//							setDocStyleDirty(true);
							checkDocStyleDirty();
						}
						DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
							return NO_DISPLAY_EXTENSION_GRAPHICS;
						}
					};
				else pvf = new UseStringOptionPanel(this, pn, pvs, pvls, pl, pd, ps, ((pv == null) ? "" : pv), false) {
					void stringChanged(String string) {
						if (string.length() == 0)
							this.useParam.setSelected(false);
						else if (this.useParam.isSelected() && this.verifyValue(string))
							selectedDocStyle.setSetting(this.docStyleParamName, string);
//						setDocStyleDirty(true);
						checkDocStyleDirty();
					}
				};
				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
				return pvf;
			}
			
			//	bounding box, use string field
			else if (BoundingBox.class.getName().equals(pvc.getName())) {
				UseStringPanel pvf = new UseStringPanel(this, pn, pl, pd, ps, ((pv == null) ? "" : pv), false, false, false) {
					boolean verifyValue(String value) {
						try {
							return (BoundingBox.parse(this.getValue()) != null);
						}
						catch (RuntimeException re) {
							return false;
						}
					}
					void stringChanged(String string) {
						if (string.length() == 0)
							this.useParam.setSelected(false);
						else if (this.useParam.isSelected() && this.verifyValue(string))
							selectedDocStyle.setSetting(this.docStyleParamName, string);
//						setDocStyleDirty(true);
						checkDocStyleDirty();
					}
					DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
						return getBoundingBoxVisualizationGraphics(this.parent, page, this.getDisplayedValue());
					}
				};
				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
				return pvf;
			}
			
			//	string, use string field
			else if (String.class.getName().equals(pvc.getName())) {
				UseParamPanel pvf;
				if (pvs == null)
					pvf = new UseStringPanel(this, pn, pl, pd, ps, ((pv == null) ? "" : pv), false, (pn.endsWith(".pattern") || pn.endsWith("Pattern")), (pn.endsWith(".pattern") || pn.endsWith("Pattern"))) {
						ImTokenSequence getTestDocTokens() {
							if ((testDocTokens == null) && (testDoc != null))
								testDocTokens = new ImTokenSequence(((Tokenizer) testDoc.getAttribute(ImDocument.TOKENIZER_ATTRIBUTE, Gamta.INNER_PUNCTUATION_TOKENIZER)), testDoc.getTextStreamHeads(), (ImTokenSequence.NORMALIZATION_LEVEL_PARAGRAPHS | ImTokenSequence.NORMALIZE_CHARACTERS));
							return testDocTokens;
						}
						ImDocument getTestDoc() {
							return testDoc;
						}
						boolean verifyValue(String value) {
							if (pn.endsWith(".linePattern") || pn.endsWith("LinePattern")) {
								try {
									LinePattern.parsePattern(value);
									return true;
								}
								catch (IllegalArgumentException iae) {
									return false;
								}
							}
							else if (pn.endsWith(".pattern") || pn.endsWith("Pattern")) {
								try {
									Pattern.compile(value);
									return true;
								}
								catch (PatternSyntaxException pse) {
									return false;
								}
							}
							else return true;
						}
						void stringChanged(String string) {
							if (string.length() == 0)
								this.useParam.setSelected(false);
							else if (this.useParam.isSelected() && this.verifyValue(string))
								selectedDocStyle.setSetting(this.docStyleParamName, string);
//							setDocStyleDirty(true);
							checkDocStyleDirty();
						}
						DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
							if (this.docStyleParamName.endsWith(".linePattern") || this.docStyleParamName.endsWith("LinePattern"))
								return getLinePatternVisualizationGraphics(this.parent, page, this.getDisplayedValue());
							if (pn.endsWith(".pattern") || pn.endsWith("Pattern"))
								return getPatternVisualizationGraphics(this.parent, page, this.getDisplayedValue());
							return NO_DISPLAY_EXTENSION_GRAPHICS;
						}
					};
				else pvf = new UseStringOptionPanel(this, pn, pvs, pvls, pl, pd, ps, ((pv == null) ? "" : pv), false) {
					void stringChanged(String string) {
						if (string.length() == 0)
							this.useParam.setSelected(false);
						else if (this.useParam.isSelected() && this.verifyValue(string))
							selectedDocStyle.setSetting(this.docStyleParamName, string);
//						setDocStyleDirty(true);
						checkDocStyleDirty();
					}
				};
				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
				return pvf;
			}
			
			//	pattern, use specialized string field
			else if (Pattern.class.getName().equals(pvc.getName())) {
				UseParamPanel pvf;
				if (pvs == null)
					pvf = new UseStringPanel(this, pn, pl, pd, ps, ((pv == null) ? "" : pv), false, true, true) {
						ImTokenSequence getTestDocTokens() {
							if ((testDocTokens == null) && (testDoc != null))
								testDocTokens = new ImTokenSequence(((Tokenizer) testDoc.getAttribute(ImDocument.TOKENIZER_ATTRIBUTE, Gamta.INNER_PUNCTUATION_TOKENIZER)), testDoc.getTextStreamHeads(), (ImTokenSequence.NORMALIZATION_LEVEL_PARAGRAPHS | ImTokenSequence.NORMALIZE_CHARACTERS));
							return testDocTokens;
						}
						ImDocument getTestDoc() {
							return testDoc;
						}
						boolean verifyValue(String value) {
							try {
								Pattern.compile(value);
								return true;
							}
							catch (PatternSyntaxException pse) {
								return false;
							}
						}
						void stringChanged(String string) {
							if (string.length() == 0)
								this.useParam.setSelected(false);
							else if (this.useParam.isSelected() && this.verifyValue(string))
								selectedDocStyle.setSetting(this.docStyleParamName, string);
//							setDocStyleDirty(true);
							checkDocStyleDirty();
						}
						DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
							return getPatternVisualizationGraphics(this.parent, page, this.getDisplayedValue());
						}
					};
				else pvf = new UseStringOptionPanel(this, pn, pvs, pvls, pl, pd, ps, ((pv == null) ? "" : pv), false) {
					void stringChanged(String string) {
						if (string.length() == 0)
							this.useParam.setSelected(false);
						else if (this.useParam.isSelected() && this.verifyValue(string))
							selectedDocStyle.setSetting(this.docStyleParamName, string);
//						setDocStyleDirty(true);
						checkDocStyleDirty();
					}
				};
				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
				return pvf;
			}
			
			//	list, use list field
			else if (DocumentStyle.getListElementClass(pvc) != pvc) {
				final Class pvlec = DocumentStyle.getListElementClass(pvc);
				UseListPanel pvf = new UseListPanel(this, pn, pl, pd, ps, ((pv == null) ? "" : pv), (pn.endsWith(".patterns") || pn.endsWith("Patterns")), (pn.endsWith(".patterns") || pn.endsWith("Patterns"))) {
					ImTokenSequence getTestDocTokens() {
						if ((testDocTokens == null) && (testDoc != null))
							testDocTokens = new ImTokenSequence(((Tokenizer) testDoc.getAttribute(ImDocument.TOKENIZER_ATTRIBUTE, Gamta.INNER_PUNCTUATION_TOKENIZER)), testDoc.getTextStreamHeads(), (ImTokenSequence.NORMALIZATION_LEVEL_PARAGRAPHS | ImTokenSequence.NORMALIZE_CHARACTERS));
						return testDocTokens;
					}
					ImDocument getTestDoc() {
						return testDoc;
					}
					boolean verifyValue(String value) {
						String[] valueParts = value.split("\\s+");
						for (int p = 0; p < valueParts.length; p++) {
							if (!this.verifyValuePart(valueParts[p]))
								return false;
						}
						return true;
					}
					boolean verifyValuePart(String valuePart) {
						if (Boolean.class.getName().equals(pvlec.getName()))
							return ("true".equals(valuePart) || "false".equals(valuePart));
						else if (Integer.class.getName().equals(pvlec.getName())) {
							try {
								Integer.parseInt(valuePart);
								return true;
							}
							catch (NumberFormatException nfe) {
								return false;
							}
						}
						else if (Float.class.getName().equals(pvlec.getName())) {
							try {
								Float.parseFloat(valuePart);
								return true;
							}
							catch (NumberFormatException nfe) {
								return false;
							}
						}
						else if (Double.class.getName().equals(pvlec.getName())) {
							try {
								Double.parseDouble(valuePart);
								return true;
							}
							catch (NumberFormatException nfe) {
								return false;
							}
						}
						else if (BoundingBox.class.getName().equals(pvlec.getName())) {
							try {
								return (BoundingBox.parse(this.getValue()) != null);
							}
							catch (RuntimeException re) {
								return false;
							}
						}
						else if (String.class.getName().equals(pvlec.getName())) {
							if (pn.endsWith(".linePatterns") || pn.endsWith("LinePatterns")) {
								try {
									LinePattern.parsePattern(valuePart);
									return true;
								}
								catch (IllegalArgumentException iae) {
									return false;
								}
							}
							else if (pn.endsWith(".patterns") || pn.endsWith("Patterns")) {
								try {
									Pattern.compile(valuePart);
									return true;
								}
								catch (PatternSyntaxException pse) {
									return false;
								}
							}
							else return true;
						}
						else if (Pattern.class.getName().equals(pvlec.getName())) {
							try {
								Pattern.compile(valuePart);
								return true;
							}
							catch (PatternSyntaxException pse) {
								return false;
							}
						}
						else return true;
					}
					void stringChanged(String string) {
						if (string.length() == 0)
							this.useParam.setSelected(false);
						else if (this.useParam.isSelected() && this.verifyValue(string))
							selectedDocStyle.setSetting(this.docStyleParamName, string.replaceAll("\\s+", " "));
//						setDocStyleDirty(true);
						checkDocStyleDirty();
					}
					DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
						String[] valueParts = this.getDisplayedValue().split("\\s+");
						ArrayList degList = new ArrayList();
						for (int p = 0; p < valueParts.length; p++)
							degList.addAll(Arrays.asList(this.getDisplayExtensionGraphicsPart(page, valueParts[p])));
						return ((DisplayExtensionGraphics[]) degList.toArray(new DisplayExtensionGraphics[degList.size()]));
					}
					DisplayExtensionGraphics[] getDisplayExtensionGraphicsPart(ImPage page, String valuePart) {
						if (Integer.class.getName().equals(pvlec.getName())) {
							if (this.docStyleParamName.endsWith(".fontSize") || this.docStyleParamName.endsWith("FontSize"))
								return getFontSizeVisualizationGraphics(this.parent, page, valuePart);
							if (this.docStyleParamName.endsWith(".margin") || this.docStyleParamName.endsWith("Margin"))
								return getMarginVisualizationGraphics(this.parent, page, valuePart);
							//	TODO think of more
						}
						else if (BoundingBox.class.getName().equals(pvlec.getName()))
							return getBoundingBoxVisualizationGraphics(this.parent, page, valuePart);
						else if (String.class.getName().equals(pvlec.getName())) {
							if (this.docStyleParamName.endsWith(".linePatterns") || this.docStyleParamName.endsWith("LinePatterns"))
								return getLinePatternVisualizationGraphics(this.parent, page, valuePart);
							if (pn.endsWith(".patterns") || pn.endsWith("Patterns"))
								return getPatternVisualizationGraphics(this.parent, page, valuePart);
						}
						else if (Pattern.class.getName().equals(pvlec.getName())) {
							return getPatternVisualizationGraphics(this.parent, page, valuePart);
						}
						return NO_DISPLAY_EXTENSION_GRAPHICS;
					}
				};
				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
				return pvf;
			}
			
			//	as the ultimate fallback, use string field
			else {
				UseStringPanel pvf = new UseStringPanel(this, pn, pl, pd, ps, ((pv == null) ? "" : pv), false, false, false) {
					void stringChanged(String string) {
						if (string.length() == 0)
							this.useParam.setSelected(false);
						else if (this.useParam.isSelected() && this.verifyValue(string))
							selectedDocStyle.setSetting(this.docStyleParamName, string);
//						setDocStyleDirty(true);
						checkDocStyleDirty();
					}
					DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
						return NO_DISPLAY_EXTENSION_GRAPHICS;
					}
				};
				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
				return pvf;
			}
		}
	}
	
	private static class ParamTreeCellRenderer extends DefaultTreeCellRenderer {
		private Icon rootIcon = null;
		ParamTreeCellRenderer() {
			String packageName = ImDocumentStyleManager.class.getName();
			packageName = packageName.replace('.', '/');
			try {
				this.setClosedIcon(new ImageIcon(ImageIO.read(ImDocumentStyleManager.class.getClassLoader().getResourceAsStream(packageName + ".paramTree.closed.png"))));
				this.setOpenIcon(new ImageIcon(ImageIO.read(ImDocumentStyleManager.class.getClassLoader().getResourceAsStream(packageName + ".paramTree.open.png"))));
				this.setLeafIcon(new ImageIcon(ImageIO.read(ImDocumentStyleManager.class.getClassLoader().getResourceAsStream(packageName + ".paramTree.leaf.png"))));
				this.rootIcon = new ImageIcon(ImageIO.read(ImDocumentStyleManager.class.getClassLoader().getResourceAsStream(packageName + ".paramTree.root.png")));
			} catch (IOException ioe) { /* never gonna happen, but Java don't know */ }
		}
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if ((this.rootIcon != null) && (value != null) && (value instanceof TreeNode) && (((TreeNode) value).getParent() == null))
				this.setIcon(this.rootIcon);
			return this;
		}
	}
	
	private static DisplayExtensionGraphics[] getLinePatternVisualizationGraphics(DocStyleEditor parent, ImPage page, String pattern) {
		try {
			if (pattern.trim().length() == 0)
				return NO_DISPLAY_EXTENSION_GRAPHICS;
			LinePattern lp = LinePattern.parsePattern(pattern);
			ImRegion[] lines = lp.getMatches(page);
			if (lines.length == 0)
				return NO_DISPLAY_EXTENSION_GRAPHICS;
			DisplayExtensionGraphics[] degs = new DisplayExtensionGraphics[lines.length];
			for (int l = 0; l < lines.length; l++)
				degs[l] = getBoundingBoxVisualizationGraphics(parent, page, lines[l].bounds);
			return degs;
		}
		catch (IllegalArgumentException iae) {
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		}
	}
	
	private static DisplayExtensionGraphics[] getPatternVisualizationGraphics(DocStyleEditor parent, ImPage page, String pattern) {
		try {
			if (pattern.trim().length() == 0)
				return NO_DISPLAY_EXTENSION_GRAPHICS;
			ImDocumentRoot pageTokens = new ImDocumentRoot(page, (ImTokenSequence.NORMALIZATION_LEVEL_PARAGRAPHS | ImTokenSequence.NORMALIZE_CHARACTERS | ImDocumentRoot.INCLUDE_PAGE_TITLES));
			Annotation[] patternMatches = Gamta.extractAllMatches(pageTokens, pattern, 32, false, false, false);
			ArrayList pmWords = new ArrayList();
			for (int m = 0; m < patternMatches.length; m++) {
				ImWord pmStartWord = pageTokens.wordAtIndex(patternMatches[m].getStartIndex());
				ImWord pmEndWord = pageTokens.wordAtIndex(patternMatches[m].getEndIndex() - 1);
				for (ImWord pmWord = pmStartWord; pmWord != null; pmWord = pmWord.getNextWord()) {
					if (pmWord.pageId != page.pageId)
						break;
					pmWords.add(pmWord);
					if (pmWord == pmEndWord)
						break;
				}
			}
			return getWordVisualizationGraphics(parent, page, ((ImWord[]) pmWords.toArray(new ImWord[pmWords.size()])));
		}
		catch (IllegalArgumentException iae) {
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		}
	}
	
	private static DisplayExtensionGraphics[] getFontSizeVisualizationGraphics(DocStyleEditor parent, ImPage page, String fontSize) {
		if (fontSize == null)
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		fontSize = fontSize.trim();
		if (fontSize.length() == 0)
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		try {
			ArrayList fsWords = new ArrayList();
			ImWord[] pWords = page.getWords();
			for (int w = 0; w < pWords.length; w++) {
				if (fontSize.equals(pWords[w].getAttribute(ImWord.FONT_SIZE_ATTRIBUTE)))
					fsWords.add(pWords[w]);
			}
			return getWordVisualizationGraphics(parent, page, ((ImWord[]) fsWords.toArray(new ImWord[fsWords.size()])));
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		}
	}
	
	private static DisplayExtensionGraphics[] getFontStyleVisualizationGraphics(DocStyleEditor parent, ImPage page, String attributeName) {
		if (attributeName == null)
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		attributeName = attributeName.trim();
		if (attributeName.length() == 0)
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		try {
			ArrayList fsWords = new ArrayList();
			ImWord[] pWords = page.getWords();
			for (int w = 0; w < pWords.length; w++) {
				if (pWords[w].hasAttribute(attributeName))
					fsWords.add(pWords[w]);
				else if ("allCaps".equals(attributeName) && isAllCaps(pWords[w].getString()))
					fsWords.add(pWords[w]);
			}
			return getWordVisualizationGraphics(parent, page, ((ImWord[]) fsWords.toArray(new ImWord[fsWords.size()])));
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		}
	}
	private static boolean isAllCaps(String str) {
		return (str.equals(str.toUpperCase()) && !str.equals(str.toLowerCase()));
	}
	
	private static final Color wordLineColor = Color.ORANGE;
	private static final BasicStroke wordLineStroke = new BasicStroke(1);
	private static final Color wordFillColor = new Color(wordLineColor.getRed(), wordLineColor.getGreen(), wordLineColor.getBlue(), 64);
	private static DisplayExtensionGraphics[] getWordVisualizationGraphics(DocStyleEditor parent, ImPage page, ImWord[] words) {
		Shape[] shapes = new Shape[words.length];
		for (int w = 0; w < words.length; w++)
			shapes[w] = new Rectangle2D.Float(words[w].bounds.left, words[w].bounds.top, words[w].bounds.getWidth(), words[w].bounds.getHeight());
		DisplayExtensionGraphics[] degs = {new DisplayExtensionGraphics(parent, null, page, shapes, wordLineColor, wordLineStroke, wordFillColor) {
			public boolean isActive() {
				return true;
			}
		}};
		return degs;
	}
	
	private static DisplayExtensionGraphics[] getMarginVisualizationGraphics(DocStyleEditor parent, ImPage page, String margin) {
		try {
			/* TODO figure out how to implement this:
			 * - based upon lines?
			 * - based upon paragraphs?
			 * - based upon blocks?
			 */
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		}
		catch (IllegalArgumentException iae) {
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		}
	}
	
	private static final Color boundingBoxLineColor = Color.GREEN;
	private static final BasicStroke boundingBoxLineStroke = new BasicStroke(3);
	private static final Color boundingBoxFillColor = new Color(boundingBoxLineColor.getRed(), boundingBoxLineColor.getGreen(), boundingBoxLineColor.getBlue(), 64);
	private static DisplayExtensionGraphics[] getBoundingBoxVisualizationGraphics(DocStyleEditor parent, ImPage page, String box) {
		try {
			BoundingBox bb = BoundingBox.parse(box);
			if (bb == null)
				return NO_DISPLAY_EXTENSION_GRAPHICS;
			bb = bb.scale(((float) page.getImageDPI()) / ImDocumentStyle.DEFAULT_DPI);
			DisplayExtensionGraphics[] degs = {getBoundingBoxVisualizationGraphics(parent, page, bb)};
			return degs;
		}
		catch (IllegalArgumentException iae) {
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			return NO_DISPLAY_EXTENSION_GRAPHICS;
		}
	}
	private static DisplayExtensionGraphics getBoundingBoxVisualizationGraphics(DocStyleEditor parent, ImPage page, BoundingBox bb) {
		Shape[] shapes = {new Rectangle2D.Float(bb.left, bb.top, bb.getWidth(), bb.getHeight())};
		return new DisplayExtensionGraphics(parent, null, page, shapes, boundingBoxLineColor, boundingBoxLineStroke, boundingBoxFillColor) {
			public boolean isActive() {
				return true;
			}
		};
	}
	
	private static final Properties paramNamesLabels = new Properties() {
		public String getProperty(String key, String defaultValue) {
			return super.getProperty(key.substring(key.lastIndexOf('.') + 1), defaultValue);
		}
	};
	static {
		paramNamesLabels.setProperty("isBold", "Require Values to be Bold");
		paramNamesLabels.setProperty("startIsBold", "Require Value Starts to be Bold");
		paramNamesLabels.setProperty("isItalics", "Require Values to be in Italics");
		paramNamesLabels.setProperty("startIsItalics", "Require Value Starts to be in Italics");
		paramNamesLabels.setProperty("isAllCaps", "Require Values to be All Caps");
		paramNamesLabels.setProperty("startIsAllCaps", "Require Value Starts to be All Caps");
		paramNamesLabels.setProperty("minFontSize", "Use Minimum Font Size");
		paramNamesLabels.setProperty("maxFontSize", "Use Maximum Font Size");
	}
	
	private static class DocStyleParameterTracker extends DialogPanel {
		static Dimension lastSize = new Dimension(500, 800);
		static Point lastLocation = null;
		private JTextArea log = new JTextArea();
		private CountingSet keyRetrievals = new CountingSet(new HashMap());
		DocStyleParameterTracker() {
			super("Property Data Access", false);
			
			this.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					lastSize = getSize();
					lastLocation = getLocation();
				}
			});
			
			JButton clearButton = new JButton("Clear");
			clearButton.setBorder(BorderFactory.createRaisedBevelBorder());
			clearButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					log.setText("");
					keyRetrievals.clear();
				}
			});
			
			JButton closeButton = new JButton("Close");
			closeButton.setBorder(BorderFactory.createRaisedBevelBorder());
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					dispose();
				}
			});
			
			JScrollPane logBox = new JScrollPane(this.log);
			logBox.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			logBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			logBox.getVerticalScrollBar().setBlockIncrement(50);
			logBox.getVerticalScrollBar().setUnitIncrement(50);
			
			JPanel buttonPanel = new JPanel(new GridLayout(1, 0), true);
			buttonPanel.add(clearButton);
			buttonPanel.add(closeButton);
			
			this.add(logBox, BorderLayout.CENTER);
			this.add(buttonPanel, BorderLayout.SOUTH);
			
			this.setSize(lastSize);
			if (lastLocation == null)
				this.setLocationRelativeTo(this.getOwner());
			else this.setLocation(lastLocation);
		}
		synchronized void propertyDataRetrieved(String key, String value, boolean fromLiveData) {
			this.keyRetrievals.add(key);
			this.log.append("Key '" + key + "' (" + this.keyRetrievals.getCount(key) + ")\r\n");
			this.log.append(" =" + (fromLiveData ? "L" : "P") + "=> " + value + "\r\n");
		}
	}
//	
//	public static void main(String[] args) throws Exception {
//		System.out.println(buildPattern("ZOOTAXA  109:"));
//	}
}