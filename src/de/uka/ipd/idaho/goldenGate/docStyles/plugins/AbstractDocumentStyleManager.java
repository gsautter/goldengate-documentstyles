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
package de.uka.ipd.idaho.goldenGate.docStyles.plugins;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
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
import de.uka.ipd.idaho.gamta.Gamta;
import de.uka.ipd.idaho.gamta.TokenSequence;
import de.uka.ipd.idaho.gamta.util.DocumentStyle;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.AbstractData;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.Anchor;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.ParameterDescription;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.ParameterGroupDescription;
import de.uka.ipd.idaho.gamta.util.swing.AnnotationDisplayDialog;
import de.uka.ipd.idaho.goldenGate.plugins.AbstractResourceManager;
import de.uka.ipd.idaho.goldenGate.util.DialogPanel;
import de.uka.ipd.idaho.goldenGateServer.dss.client.GoldenGateDssClient;
import de.uka.ipd.idaho.goldenGateServer.uaa.client.AuthenticatedClient;
import de.uka.ipd.idaho.htmlXmlUtil.Parser;
import de.uka.ipd.idaho.htmlXmlUtil.TokenReceiver;
import de.uka.ipd.idaho.htmlXmlUtil.TreeNodeAttributeSet;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.Grammar;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.StandardGrammar;
import de.uka.ipd.idaho.stringUtils.StringUtils;

/**
 * This plug-in manages document style parameter lists for Image Markup
 * documents and helps users with creating and editing them.
 * 
 * @author sautter
 */
public abstract class AbstractDocumentStyleManager extends AbstractResourceManager {
	private static final ParameterGroupDescription anchorRootDescription = new ParameterGroupDescription("anchor");
	static {
		anchorRootDescription.setLabel("Anchors");
		anchorRootDescription.setDescription("Anchors automate the assignment of document styles to individual documents. In particular, anchors match on distinctive landmark features on the first few pages of documents, e.g. a journal name in a specific position and font size.");
//		anchorRootDescription.setParamLabel("maxPageId", "Maximum Pages After First");
//		anchorRootDescription.setParamDescription("maxPageId", "The maximum number of pages to serach for anchor targets after the very first page.");
	}
	private Settings parameterValueClassNames;
	private Map parameterValueClasses = Collections.synchronizedMap(new HashMap());
//	private BibRefTypeSystem refTypeSystem;
//	private AbstractDocumentStyleProvider styleProvider;
	private String pluginName;
	
	//	need to be able to make do without these !!!
	private boolean dssAvailable;
	private Object authClientObj;
	private Object dssClientObj;
	
	/**
	 * @param pluginName the (subclass specific) plugin name 
	 */
	protected AbstractDocumentStyleManager(String pluginName) {
		this.pluginName = pluginName;
	}

	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractResourceManager#getPluginName()
	 */
	public String getPluginName() {
		return this.pluginName;
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
//		
//		//	connect to document style provider for storage
//		this.styleProvider = ((AbstractDocumentStyleProvider) this.parent.getPlugin(AbstractDocumentStyleProvider.class.getName()));
//		
//		//	get reference type system for publication types
//		this.refTypeSystem = BibRefTypeSystem.getDefaultInstance();
		
		//	read existing style parameters (no way they have all been requested at this point)
		try {
			Reader spr = new BufferedReader(new InputStreamReader(this.dataProvider.getInputStream("styleParameters.cnfg"), "UTF-8"));
			this.parameterValueClassNames = Settings.loadSettings(spr);
			spr.close();
			String[] spns = this.parameterValueClassNames.getKeys();
			for (int p = 0; p < spns.length; p++) try {
				this.parameterValueClasses.put(spns[p], Class.forName(this.parameterValueClassNames.getSetting(spns[p])));
			} catch (Exception e) {}
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
		//	TODO load all style source ...
		//	TODO ... and index them by both name and ID
		
		/*
On pre-loading style sources:
- tray them all up as DocStyleSource with ID, name, and inheritedId
- cache those trays and use them, exclusively
  ==> keep updated on changes
- also hold set of previous names in trays
  ==> facilitates cleanup
- do store by name to support resource export (IDs tell preciously little in resource selector dialog) ...
- ... but (at least mainly) handle by ID in memory
  ==> we do all the IO now, so should be doable
- delete predecessor on renamed saving (ID should be enough to tell)
  ==> maybe also keep last persisted name ...
  ==> ... setting it to loaded name initially
- if ID missing, create one and store style source right away
		 */
	}
	
	/*
Persist editable document style templates in template manager as document style _sources_:
- extend template manager to resource manager to control export of bases for home-grown styles
- add "save as" button in template editor
- publish to local provider plugin and DSS (as sketched in previous mailes)
  ==> provide respective public method in abstract provider
  ==> make sure to facilitate changing data underneath document styles from that provider on the fly (underneath existing object references) ...
  ==> ... or wrap said provider in respective wrapping provider
  ==> DO THE FORMER, with public access to replacing document style underneath
    - use respective interface, to be implemented by _both_ wrappers (maybe MutableDocumentStyle)
    - allows generically injecting document style preferring data from template editor over persisted data on editing
    - allows refreshing anchors on replacement
    ==> do all that in abstract superclass of template editor, as none of this is bound to IM
- add generic visualizePropertyValue(String name, String value, Class valueClass) method
  ==> allows putting template editor proper in abstract superclass (if not its opening, closing, document change behavior before new GGE UI) ...
  ==> ... alongside inheritance, publishing, persistence, etc. ...
  ==> ... with subclasses merely having to provide dedicated fields, value validations and visualizations, and tests for property types specific to their respective data models
  ==> allows adding GPath as type in XML specific editor
  
  Publishing locally:
- resolve to flat settings
- wrap in SettingsData
- add top document style attributes
- add inheritance chain attribute
  ==> add method for concatenating it through daisy chain
  ==> use "<ID>:<name>", separated with spaces
- hand over to style provider

Publishing to DSS:
- resolve and wrap as above
- ensure logged in (copy from DIO client, removing cache handling)
- upload to server
- prompt with success message
- maybe auto-publish locally afterwards on success
  ==> saves round trip
  ==> but then DON'T, as that will complicate handling provenance information
  ==> BETTER prod style provider to re-fetch style ...
  ==> ... getting provenance information from server
  ==> maybe disable local publishing if DSS available and style provider connected to it ...
  ==> ... or return DocumentListElement with updated provenance data from upload method and use that in local publishing
    ==> also maintain that metadata in style manager, storing in local style after upload

Maybe add DocumentStylePublisher interface to style manager ...
... and load respective implementations from JARs in data folder (somewhat akin to document IO)
==> extensible out of the box
==> we might want to push to GitHub at some point, for instance ...
==> still add special treatment for local style provider ...
==> ... maybe wrapping it in publisher interface for streamlining
==> maybe call it DocumentStyleRepository instead ...
==> ... also facilitating down-sync
  - will require distributing DSS provenance data (attributes)
  - keep DSS as the one authoritative storage location
    ==> need to publish to DSS first
    ==> need to issue update events (RES !!!)
    ==> need add indicator method in interface
  ==> add "Plugins" menu to GGI, facilitating per-plug-in document independent functionality ...
  ==> ... but only after separating GGE from GG Core, though

Only allow one provenance data keeping repository:
- makes clear to style manager whose provenance data to distribute to other publishers
- throw exception on loading if more than one such repository present
- maybe augment that to DocumentStyleAuthority to allow down-sync from multiple source

Issue update and deletion events from DSS
==> facilitates RES based replication
==> implement DSR to pick up replication

Easing migration pain:
- import all ".docStyle" data from mocal style provider as ".docStyleSource" on startup ...
- ... unless happened before (existence of file with respective name will tell)

Facilitate multi-line property values:
- indicate by mere "+" property name
  ==> add to previous value if encountered
    ==> need to keep previous property name and value in reading methods
- split property values at line breaks on output ...
- ... and output "+" line for all but first line
	 */
	
	/*
Visualizing inheritance:
- open inheritance chain in JTabbedPane in style editor (IM and XML alike)
- make parent style selectable via drop-down at top of each tab ...
- ... opening (hierarchy of) added style(s) to tabs as selection changes ...
- ... and removing tabs as selection goes back to "<none>" (the default)
- set value field background to light gray if value inherited ...
- ... as well as setting label (checkbox) to italics
==> should work well with given enabling/disabling mechanism based on parameter dependencies
==> overwrite value if set explicitly
  ==> won't be stored if checkbox unselected anyway ...
  ==> ... having consuming client code default to parent style template automatically
  ==> add setDefaults(DocumentStyle) --> void method
  ==> add "resolve" flag to all output methods

Resolving inheritance:
- have document style managers (there will be one for XML world as well with new XML UI) maintain their own data ...
- ... registering them as providers for testing ...
- ... and resolving inheritance locally on the fly
- resolve inheritance in standalone document style providers
  ==> those styles ain't going to be sent anywhere
  ==> server based document styles do have UUIDs to drive resolution

Handling inheritance in document style editing (especially in "Use ..." functions):
- provide "Use In" drop-down with whole inheritance chain selectable ...
- ... and style name of currently selected tab pre-selected

Express inheritance with "inheritPropertiesFrom" UUID valued attribute:
- figure out where to put that constant (DSS constants might be good place)
- might be too general a concept to restrict it that way, though

Store inheritance via "inheritValuesFrom" UUID attribute

Provide both "Publish Locally" and "Publish to GG Server" buttons in style editor:
- write inheritance resolved template to local template provider on local publishing (as implemented for saving now)
- authenticate and write inheritance resolved template to server on respective click
- enable/disable server publishing button via auth client permission check
- store unresolved templates in style editor data folder ...
- ... offering import function for remote ones from provider

Making local edits available to other gizmos for testing:
- when opening style editor (first time), remove style provider plug-in as provider (style editor depends upon that one anyway) ...
- ... and register own provider wrapping former ...
- ... looping through any returned style but one under editing
==> normal behavior, unless editing particular style

Use specialized document style implementation in editor:
- data map editable on the fly
- defaulting to parent style if property absent
==> getting property names and subset prefixes slightly more effort (have to chain and re-sort arrays) ...
==> ... but flexibility we need in editor
- copy whatever comes from provider supplied template into editable style and return that instead (in decorator sketched above)

Visualize inheritance as drafted in earlier mails:
- open styles in multiple tabs ...
- ... with parent selectable from drop-down at top
- ask which style to store measurement in when extracting from document
- visually mark inherited and overruled parameters

Use gray checkmark as _unselected_ state for inherited properties:
- most likely have to replace icon
- displaying inherited value proper on gray background in text field
- show source of inherited value in checkbox tooltip
==> requires special implementation of document style for editing ...
==> ... but we need that for inheritance and dynamic updating anyway
==> update status of fields on tab changes ...
==> ... most likely using setInheritedValue(source, value) method

Pre-load all styles in document style manager ...
... and store inheritance relationships in dedicated data structure
==> allows re-publishing all descendant templates when publishing parent
==> allows preventing inheritance cycles

Store inheritance (chain) in attribute on publishing
==> vastly improved tractability
==> for access to provenance, properly handle "<ID>/<version>" in DSS servlet ...
==> ... and maybe even offer diff view (property names hardly change, so should be almost straightforward)
  ==> maybe even offer HTML diff view right from servlet ...
  ==> ... using "diff/<ID>/<inVersion>/<sinceVersion>" data name
    - filter provenance related attributes from view
    - order property names lexicographically ...
    - ... and perform merge join ...
    - ... highlighting differences

Maintain list of available style parameters and their classes in abstract superclass of style managers, alongside many other things (no need to do them twice):
- resource managing document style sources
- publishing to local style provider
- publishing to DSS (with all availability and authentication hassle)
- resolving inheritance on publishing (including respective tractability attribute)
- use and minting of document style UUIDs
- most input field types (don't care too much about duplicating those, though, as yet more generic solution sketched below makes far more sense)
- "priority" data substitution
- visualization of inheritance
- handling and persistence of XML parameter group descriptions
- edit dialog proper:
  - just need somewhat custom content panels
  - inheritance same on both data models
==> need to abstract anchor fields and respective context menu options, though




Further candidates for property types:
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
	
	private DocStyleSettings getDocStyle(String docStyleName) {
		//	TODO resolve name to ID
		Settings docStyleData = this.getStyle(docStyleName);
		if (docStyleData == null) {
			docStyleData = new Settings();
			docStyleData.setSetting(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, docStyleName);
		}
		String docStyleId = docStyleData.getSetting(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE);
		if (docStyleId == null) {
			docStyleId = Gamta.getAnnotationID();
			docStyleData.setSetting(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE, docStyleId);
		}
		String inheritedDocStyleId = docStyleData.getSetting("inheritValuesFrom");
		DocStyleSettings inheritedDocStyle = null;
		if (inheritedDocStyleId != null)
			inheritedDocStyle = this.getDocStyle(inheritedDocStyleId);
		return new DocStyleSettings(docStyleId, docStyleName, docStyleData, inheritedDocStyle);
	}
	
	Settings getStyle(String dsName) {
		if (!dsName.endsWith(".docStyleSource"))
			dsName += ".docStyleSource";
		
		//	TODO cache these suckers
//		
//		//	return pre-loaded style first, so updates go to (possibly already shared) instance
//		DocStyle docStyle = ((DocStyle) this.docStylesByName.get(dsName));
//		if (docStyle != null)
//			return docStyle.paramList;
		
		//	try and load new style not currently held in memory
		//	TODO use ID !!!
		Settings dsParamList = this.loadSettingsResource(dsName);
//		if (dsParamList != null) {
//			dsParamList.setSetting(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, dsName.substring(0,dsName.lastIndexOf('.')));
//			this.docStylesByName.put(dsName, new DocStyle(dsParamList));
//		}
		return dsParamList;
	}
	
	void storeStyle(String dsName, DocStyleSettings dsData) {
		if (!dsName.endsWith(".docStyleSource"))
			dsName += ".docStyleSource";
		try {
			//	TODO use ID !!!
			this.storeSettingsResource(dsName, dsData.data);
			if (dsData.inherited != null)
				this.storeStyle(dsData.inherited.name, dsData.inherited);
//			//	update rather than replace, so changes become available wherever existing style object is in use
//			DocStyle docStyle = ((DocStyle) this.docStylesByName.get(dsName));
//			if (docStyle == null)
//				this.docStylesByName.put(dsName, new DocStyle(dsParamList));
//			else docStyle.setParamList(dsParamList);
		}
		catch (IOException ioe) {
			System.out.println("Error storing document style '" + dsName + "': " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
	}
	
	private static class DocStyleSettings {
		String id;
		String name;
		Settings data;
		DocStyleSettings inherited;
		//	TODO track modification (no need to save whole daisy chain all the time) !!!
		/*
Implement Attributed with DocStyleSettings:
- loop through with "@." prefix
- do NOT inherit  attributes, though
- switch name, ID, inheritValuesFrom, etc. to using attributes
  ==> have to add "@." prefix on loading
		 */
		DocStyleSettings(String id, String name, Settings data) {
			this(id, name, data, null);
		}
		DocStyleSettings(String id, String name, Settings data, DocStyleSettings inherited) {
			this.id = id;
			this.name = name;
			this.data = data;
			this.inherited = inherited;
		}
		String getSetting(String key) {
			String value = this.data.getSetting(key);
			if (value != null)
				return value;
			return ((this.inherited == null) ? null : this.inherited.getSetting(key));
		}
		String getSetting(String key, String def) {
			String value = this.getSetting(key);
			return ((value == null) ? def : value);
		}
		String[] getKeys() {
			return this.getKeys(true);
		}
		String[] getKeys(boolean local) {
			if (local || (this.inherited == null))
				return this.data.getKeys();
			TreeSet keys = new TreeSet(String.CASE_INSENSITIVE_ORDER);
			this.addKeys(keys);
			return ((String[]) keys.toArray(new String[keys.size()]));
		}
		void addKeys(TreeSet keys) {
			keys.addAll(Arrays.asList(this.data.getKeys()));
			if (this.inherited != null)
				this.inherited.addKeys(keys);
		}
		void setSetting(String key, String value) {
			this.data.setSetting(key, value);
		}
		void setSetting(String id, String key, String value) {
			//	TODO use this for "useXyz()" methods (selecting target from drop-down)
			if (this.id.equals(id))
				this.data.setSetting(key, value);
			else if (this.inherited != null)
				this.inherited.setSetting(id, key, value);
		}
		void removeSetting(String key) {
			this.data.removeSetting(key);
		}
		DocStyleSettings getSubset(String prefix) {
			String ssId = (this.id + "." + prefix);
			String ssName = (this.name + "." + prefix);
			Settings ssData = this.data.getSubset(prefix);
			DocStyleSettings ssInherited = ((this.inherited == null) ? null : this.inherited.getSubset(prefix));
			return new DocStyleSettings(ssId, ssName, ssData, ssInherited);
		}
		DocumentStyle toDocStyle() {
			DocumentStyle.Data dsData = new AbstractData() {
				public String getPropertyData(String key) {
					return getSetting(key);
				}
				public String[] getPropertyNames() {
					return getKeys();
				}
			};
			return new DocumentStyle(dsData);
		}
	}
//	
//	/* (non-Javadoc)
//	 * @see de.uka.ipd.idaho.im.imagine.plugins.DisplayExtensionProvider#getDisplayExtensions()
//	 */
//	public DisplayExtension[] getDisplayExtensions() {
//		return ((this.docStyleEditor == null) ? NO_DISPLAY_EXTENSIONS : this.docStyleEditorDisplayExtension);
//	}
	
	private ParameterGroupDescription getParameterGroupDescription(String pnp) {
		if (pnp.equals(Anchor.ANCHOR_PREFIX))
			return anchorRootDescription;
//		if (pnp.startsWith(Anchor.ANCHOR_PREFIX + "."))
//			return PageFeatureAnchor.PARAMETER_GROUP_DESCRIPTION;
		ParameterGroupDescription pgd = DocumentStyle.getParameterGroupDescription(pnp);
		if (pgd == null)
			return this.loadParameterGroupDescription(pnp);
		else {
			this.storeParameterGroupDescription(pgd);
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
//		if (docStyleParamName.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
//			if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_MINIMUM_FONT_SIZE_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_MAXIMUM_FONT_SIZE_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_FONT_SIZE_PROPERTY))
//				return Integer.class.getName().equals(cls.getName());
//			else if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_IS_BOLD_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_IS_ITALICS_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_IS_ALL_CAPS_PROPERTY))
//				return Boolean.class.getName().equals(cls.getName());
//			else if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_PATTERN_PROPERTY))
//				return String.class.getName().equals(cls.getName());
//			else if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_AREA_PROPERTY))
//				return BoundingBox.class.getName().equals(cls.getName());
//		}
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
//		if (docStyleParamName.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
//			if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_MINIMUM_FONT_SIZE_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_MAXIMUM_FONT_SIZE_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_FONT_SIZE_PROPERTY))
//				return Integer.class;
//			else if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_IS_BOLD_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_IS_ITALICS_PROPERTY) || docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_IS_ALL_CAPS_PROPERTY))
//				return Boolean.class;
//			else if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_PATTERN_PROPERTY))
//				return String.class;
//			else if (docStyleParamName.endsWith("." + PageFeatureAnchor.TARGET_AREA_PROPERTY))
//				return BoundingBox.class;
//		}
		return String.class;
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
	
	private static abstract class UseParamPanel extends JPanel implements Comparable {
		final DocStyleEditor parent;
		final String docStyleParamName;
		final JCheckBox useParam;
		UseParamPanel(DocStyleEditor parent, String docStyleParamName, String label, String description, boolean use) {
			super(new BorderLayout(), true);
			this.parent = parent;
			this.docStyleParamName = docStyleParamName;
			this.useParam = new JCheckBox(label, use);
			if (description != null)
				this.useParam.setToolTipText(description);
			this.useParam.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
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
		abstract void setInputEnabled(boolean enabled);
		abstract boolean isInputEnabled();
		abstract String getValue();
		abstract void setValue(String value);
		boolean verifyValue(String value) {
			return true;
		}
		void notifyUsageChanged() {
			if (this.parent != null)
				this.parent.paramUsageChanged(this);
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
//		abstract DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page);
	}
	
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
					notifyModified();
				}
			});
		}
		String getValue() {
			return (this.useParam.isSelected() ? "true" : "false");
		}
		void setValue(String value) {
			this.useParam.setSelected("true".equals(value));
		}
		void setInputEnabled(boolean enabled) {
			this.useParam.setEnabled(enabled);
		}
		boolean isInputEnabled() {
			return this.useParam.isEnabled();
		}
//		DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
//			if (!this.useParam.isSelected())
//				return NO_DISPLAY_EXTENSION_GRAPHICS;
//			if (this.docStyleParamName.endsWith(".bold") || this.docStyleParamName.endsWith("Bold"))
//				return getFontStyleVisualizationGraphics(this.parent, page, ImWord.BOLD_ATTRIBUTE);
//			if (this.docStyleParamName.endsWith(".italics") || this.docStyleParamName.endsWith("Italics"))
//				return getFontStyleVisualizationGraphics(this.parent, page, ImWord.ITALICS_ATTRIBUTE);
//			if (this.docStyleParamName.endsWith(".allCaps") || this.docStyleParamName.endsWith("AllCaps"))
//				return getFontStyleVisualizationGraphics(this.parent, page, "allCaps");
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		}
	}
	
	private static abstract class UseStringPanel extends UseParamPanel {
		JTextField string;
		UseStringPanel(DocStyleEditor parent, String docStyleParamName, String label, String description, boolean selected, String string, boolean escapePattern, boolean scroll) {
			super(parent, docStyleParamName, label, description, selected);
			final String localDspn = this.docStyleParamName.substring(this.docStyleParamName.lastIndexOf('.') + ".".length());
			this.add(this.useParam, BorderLayout.WEST);
			
			if (localDspn.equals("pattern") || localDspn.endsWith("Pattern")) {
				if (escapePattern)
					string = buildPattern(string);
				JButton testButton = new JButton("Test");
				testButton.setBorder(BorderFactory.createRaisedBevelBorder());
				testButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						String pattern = UseStringPanel.this.string.getText().trim();
						if (pattern.length() == 0)
							return;
//						if (localDspn.equals("linePattern") || localDspn.endsWith("LinePattern"))
//							testLinePattern(pattern, getTestDoc());
//						else
						testPattern(pattern, getTestDocTokens());
					}
				});
				this.add(testButton, BorderLayout.EAST);
				//	TODO add button opening GGE pattern editor in sub dialog (helps understand them suckers)
			}
			
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
				private String oldValue = null;
				public void focusGained(FocusEvent fe) {
					this.oldValue = UseStringPanel.this.string.getText().trim();
					notifyActivated();
				}
				public void focusLost(FocusEvent fe) {
					String value = UseStringPanel.this.string.getText().trim();
					if (!value.equals(this.oldValue))
						stringChanged(value);
					this.oldValue = null;
					notifyDeactivated();
				}
			});
			this.string.getDocument().addDocumentListener(new DocumentListener() {
				public void insertUpdate(DocumentEvent de) {
					notifyModified();
				}
				public void removeUpdate(DocumentEvent de) {
					notifyModified();
				}
				public void changedUpdate(DocumentEvent de) {}
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
		TokenSequence getTestDocTokens() {
			return null;
		}
//		ImDocument getTestDoc() {
//			return null;
//		}
		void setInputEnabled(boolean enabled) {
			this.useParam.setEnabled(enabled);
			this.string.setEnabled(enabled);
		}
		boolean isInputEnabled() {
			return this.useParam.isEnabled();
		}
		String getValue() {
			return this.string.getText().trim();
		}
		void setValue(String value) {
			this.string.setText(value);
			if (value.length() != 0)
				this.useParam.setSelected(true);
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
	
	private static void testPattern(String pattern, TokenSequence docTokens) {
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
	
//	private static void testLinePattern(String pattern, ImDocument doc) {
//		try {
//			LinePattern lp = LinePattern.parsePattern(pattern);
//			ImPage[] pages = doc.getPages();
//			ArrayList matchLineAnnots = new ArrayList();
//			for (int p = 0; p < pages.length; p++) {
//				ImRegion[] matchLines = lp.getMatches(pages[p]);
//				for (int l = 0; l < matchLines.length; l++) {
//					ImDocumentRoot matchLineDoc = new ImDocumentRoot(matchLines[l], (ImDocumentRoot.NORMALIZATION_LEVEL_RAW | ImDocumentRoot.NORMALIZE_CHARACTERS));
//					matchLineAnnots.add(matchLineDoc.addAnnotation(ImRegion.LINE_ANNOTATION_TYPE, 0, matchLineDoc.size()));
//				}
//			}
//			Annotation[] annotations = ((Annotation[]) matchLineAnnots.toArray(new Annotation[matchLineAnnots.size()]));
//			Window topWindow = DialogPanel.getTopWindow();
//			AnnotationDisplayDialog add;
//			if (topWindow instanceof JFrame)
//				add = new AnnotationDisplayDialog(((JFrame) topWindow), "Matches of Line Pattern", annotations, true);
//			else if (topWindow instanceof JDialog)
//				add = new AnnotationDisplayDialog(((JDialog) topWindow), "Matches of Line Pattern", annotations, true);
//			else add = new AnnotationDisplayDialog(((JFrame) null), "Matches of Line Pattern", annotations, true);
//			add.setLocationRelativeTo(topWindow);
//			add.setVisible(true);
//		}
//		catch (PatternSyntaxException pse) {
//			JOptionPane.showMessageDialog(DialogPanel.getTopWindow(), ("The pattern is not valid:\n" + pse.getMessage()), "Pattern Validation Error", JOptionPane.ERROR_MESSAGE);
//		}
//	}
	
	private static class UseStringOptionPanel extends UseParamPanel {
		JComboBox string;
		UseStringOptionPanel(DocStyleEditor parent, String docStyleParamName, String[] values, String[] valueLabels, String label, String description, boolean selected, String string, boolean escapePattern) {
			super(parent, docStyleParamName, label, description, selected);
			this.add(this.useParam, BorderLayout.WEST);
			
//			StringOption[] options = new StringOption[values.length];
//			for (int v = 0; v < values.length; v++)
//				options[v] = new StringOption(values[v], valueLabels[v]);
//			
//			this.string = new JComboBox(options);
//			this.string.setEditable(false);
			
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
			
			this.string = new JComboBox(options);
			this.string.setEditable(options.length < values.length);
			this.string.setBorder(BorderFactory.createLoweredBevelBorder());
			this.string.setPreferredSize(new Dimension(Math.max(this.string.getWidth(), (this.string.getFont().getSize() * string.length())), this.string.getHeight()));
			this.string.setSelectedItem(new StringOption(string, null));
			this.string.addFocusListener(new FocusListener() {
				private String oldValue = null;
				public void focusGained(FocusEvent fe) {
					this.oldValue = UseStringOptionPanel.this.getValue();
					notifyActivated();
				}
				public void focusLost(FocusEvent fe) {
					String value = UseStringOptionPanel.this.getValue();
					if (!value.equals(this.oldValue))
						stringChanged(value);
					this.oldValue = null;
					notifyDeactivated();
				}
			});
			this.string.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					stringChanged(UseStringOptionPanel.this.getValue());
					notifyModified();
				}
			});
			if (this.string.isEditable()) try {
				final JTextComponent str = ((JTextComponent) this.string.getEditor().getEditorComponent());
				str.getDocument().addDocumentListener(new DocumentListener() {
					public void insertUpdate(DocumentEvent de) {
						this.fireActionEventUnlessEmpty();
					}
					public void removeUpdate(DocumentEvent de) {
						this.fireActionEventUnlessEmpty();
					}
					private void fireActionEventUnlessEmpty() {
						String text = str.getText();
						if (text.length() != 0)
							UseStringOptionPanel.this.string.actionPerformed(new ActionEvent(str, ActionEvent.ACTION_PERFORMED, text, EventQueue.getMostRecentEventTime(), 0));
					}
					public void changedUpdate(DocumentEvent de) {}
				});
			}
			catch (Exception e) {
				System.out.println("Error wiring combo box editor: " + e.getMessage());
				e.printStackTrace(System.out);
			}
			
			this.add(this.string, BorderLayout.CENTER);
		}
		void setInputEnabled(boolean enabled) {
			this.useParam.setEnabled(enabled);
			this.string.setEnabled(enabled);
		}
		boolean isInputEnabled() {
			return this.useParam.isEnabled();
		}
		String getValue() {
			Object vObj = this.string.getSelectedItem();
//			return ((vObj == null) ? "" : ((StringOption) vObj).value);
			if (vObj == null)
				return "";
			else if (vObj instanceof StringOption)
				return ((StringOption) vObj).value;
			else return vObj.toString();
		}
		void setValue(String value) {
//			this.string.setSelectedItem(new StringOption(value, null));
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
			if (value.length() != 0)
				this.useParam.setSelected(true);
		}
		void stringChanged(String string) {}
//		DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
//			return NO_DISPLAY_EXTENSION_GRAPHICS; // no way of visualizing choice generically
//		}
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
	
	private static abstract class UseListPanel extends UseParamPanel {
		JTextArea list;
		UseListPanel(DocStyleEditor parent, String docStyleParamName, String label, String description, boolean selected, String string, boolean scroll) {
			super(parent, docStyleParamName, label, description, selected);
			final String localDspn = this.docStyleParamName.substring(this.docStyleParamName.lastIndexOf('.') + ".".length());
			this.add(this.useParam, BorderLayout.WEST);
			
			if (localDspn.equals("patterns") || localDspn.endsWith("Patterns")) {
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
//						if (localDspn.equals("linePatterns") || localDspn.endsWith("LinePatterns"))
//							testLinePattern(pattern, getTestDoc());
//						else
						testPattern(pattern, getTestDocTokens());
					}
				});
				this.add(testButton, BorderLayout.EAST);
				//	TODO add button opening GGE pattern editor in sub dialog (helps understand them suckers)
			}
			
			this.list = new JTextArea((string == null) ? "" : string.trim().replaceAll("\\s+", "\r\n"));
			this.list.setBorder(BorderFactory.createLoweredBevelBorder());
			this.list.addFocusListener(new FocusListener() {
				private String oldValue = null;
				public void focusGained(FocusEvent fe) {
					this.oldValue = UseListPanel.this.list.getText().trim();
					notifyActivated();
				}
				public void focusLost(FocusEvent fe) {
					String value = UseListPanel.this.list.getText().trim();
					if (!value.equals(this.oldValue))
						stringChanged(value);
					this.oldValue = null;
					notifyDeactivated();
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
					notifyModified();
				}
				public void changedUpdate(DocumentEvent de) {}
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
		TokenSequence getTestDocTokens() {
			return null;
		}
//		ImDocument getTestDoc() {
//			return null;
//		}
		void setInputEnabled(boolean enabled) {
			this.useParam.setEnabled(enabled);
			this.list.setEnabled(enabled);
		}
		boolean isInputEnabled() {
			return this.useParam.isEnabled();
		}
		String getValue() {
			return this.list.getText().trim().replaceAll("\\s+", " ");
		}
		void setValue(String value) {
			this.list.setText(value.replaceAll("\\s+", "\r\n"));
			if (value.length() != 0)
				this.useParam.setSelected(true);
		}
		void stringChanged(String string) {}
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
//	
//	private Settings getDocStyle(String docStyleName) {
//		Settings docStyle = this.styleProvider.getStyle(docStyleName);
//		if (docStyle == null) {
//			docStyle = new Settings();
//			docStyle.setSetting(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, docStyleName);
//		}
//		return docStyle;
//	}
	
	private static final String CREATE_DOC_STYLE = "<Create Document Style>";
	
	private DocStyleEditor docStyleEditor = null;
//	private DisplayExtension[] docStyleEditorDisplayExtension = null;
	
	/**
	 * @author sautter
	 */
	private class DocStyleEditor extends DialogPanel/* implements DisplayExtension*/ {
		private JTree paramTree = new JTree();
		private JPanel paramPanel = new JPanel(new BorderLayout(), true);
		private JButton saveDocStyleButton = new JButton("Save Document Style Template");
		private int toolTipCloseDelay = ToolTipManager.sharedInstance().getDismissDelay();
		DocStyleEditor() {
			super("Edit Document Style Template", false);
			
			this.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					askSaveIfDirty();
					docStyleEditor = null; // make way on closing
//					docStyleEditorDisplayExtension = null;
//					ggImagine.notifyDisplayExtensionsModified(null);
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
			
			this.saveDocStyleButton.setBorder(BorderFactory.createRaisedBevelBorder());
			this.saveDocStyleButton.setEnabled(false);
			this.saveDocStyleButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					saveDocStyle();
				}
			});
			
			JScrollPane paramTreeBox = new JScrollPane(this.paramTree);
			paramTreeBox.getHorizontalScrollBar().setBlockIncrement(20);
			paramTreeBox.getHorizontalScrollBar().setUnitIncrement(20);
			paramTreeBox.getVerticalScrollBar().setBlockIncrement(20);
			paramTreeBox.getVerticalScrollBar().setUnitIncrement(20);
			
			JSplitPane paramSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, paramTreeBox, this.paramPanel);
			
			this.add(paramSplit, BorderLayout.CENTER);
			this.add(this.saveDocStyleButton, BorderLayout.SOUTH);
			this.setSize(400, 600);
			this.setLocationRelativeTo(this.getOwner());
		}
		
		String docStyleName = null;
		private DocStyleSettings docStyle = null;
		private boolean docStyleDirty = false;
//		
//		private ImDocument testDoc;
//		private ImTokenSequence testDocTokens;
		
		//	TODO show daisy chain in tabbed pane
//		void setDocStyle(ImDocument doc, String docStyleName, DocStyleSettings docStyle) {
		void setDocStyle(String docStyleName, DocStyleSettings docStyle) {
//			
//			//	update test document
//			if (this.testDoc != doc) {
//				this.testDoc = doc;
//				this.testDocTokens = null;
//			}
			
			//	document style remains, we're done here
			if (docStyleName.equals(this.docStyleName))
				return;
			
			//	save any modifications to previously open document style
			this.askSaveIfDirty();
			
			//	update data fields
			this.docStyleName = docStyleName;
			this.docStyle = docStyle;
			this.setDocStyleDirty(false);
			
			//	clear index fields
			this.paramGroupName = null;
			this.paramValueFields.clear();
			
			//	update window title
			this.setTitle("Edit Document Style Template '" + this.docStyleName + "'");
			
			//	get available parameter names, including ones from style proper (anchors !!!)
			TreeSet dsParamNameSet = new TreeSet(Arrays.asList(parameterValueClassNames.getKeys()));
			String[] dDsParamNames = docStyle.getKeys();
			for (int p = 0; p < dDsParamNames.length; p++) {
				if (dDsParamNames[p].startsWith(Anchor.ANCHOR_PREFIX + "."))
					dsParamNameSet.add(dDsParamNames[p]);
			}
			
			//	make sure we can create anchors
			dsParamNameSet.add(Anchor.ANCHOR_PREFIX + ".<create>.dummy");
			dsParamNameSet.add("anchor.maxPageId");
			
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
		}
		
		void setDocStyleDirty(boolean dirty) {
			this.docStyleDirty = dirty;
			this.saveDocStyleButton.setEnabled(dirty);
		}
		
		void askSaveIfDirty() {
			if ((this.docStyleName == null) || (this.docStyle == null))
				return;
			if (!this.docStyleDirty)
				return;
			int choice = JOptionPane.showConfirmDialog(this, ("Document style template '" + docStyleName + "' has been modified. Save Changes?"), "Save Document Style?", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (choice == JOptionPane.YES_OPTION)
				this.saveDocStyle();
		}
		
		void saveDocStyle() {
			if ((this.docStyleName == null) || (this.docStyle == null))
				return;
			if (!this.docStyleDirty)
				return;
			AbstractDocumentStyleManager.this.storeStyle(this.docStyleName, this.docStyle);
			this.setDocStyleDirty(false);
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.DisplayExtension#isActive()
		 */
		public boolean isActive() {
			return true;
		}
//		
//		/* (non-Javadoc)
//		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.DisplayExtension#getExtensionGraphics(de.uka.ipd.idaho.im.ImPage, de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel)
//		 */
//		public DisplayExtensionGraphics[] getExtensionGraphics(ImPage page, ImDocumentMarkupPanel idmp) {
//			if (idmp.document == this.testDoc) { /* we're working with this one */ }
//			else if ((this.testDoc != null) && this.testDoc.docId.equals(idmp.document.docId)) { /* we're still working with this one */ }
//			else if ((this.docStyleName != null) && this.docStyleName.equals(idmp.document.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE))) { /* this one fits the template we're working on */ }
//			else return NO_DISPLAY_EXTENSION_GRAPHICS; // this one is none of our business
////			System.out.println("Getting display '" + this.paramGroupName + "' extension graphics for page " + page.pageId);
////			System.out.println(" - active parameter description is " + this.activeParamDescription);
////			System.out.println(" - active parameter panel is " + this.activeParamPanel);
////			System.out.println(" - parameter group description is " + this.paramGroupDesciption);
//			
//			//	highlight current field in any custom way available
//			if (this.activeParamDescription instanceof DisplayExtension)
//				return ((DisplayExtension) this.activeParamDescription).getExtensionGraphics(page, idmp);
//			
//			//	highlight content of current field, or what it represents or matches
//			else if (this.activeParamPanel != null)
//				return this.activeParamPanel.getDisplayExtensionGraphics(page);
//			
//			//	highlight match of group as a whole (anchors, document metadata, etc.) if parameter description exists and represents a display extension
//			else if (this.paramGroupDescription instanceof DisplayExtension)
//				return ((DisplayExtension) this.paramGroupDescription).getExtensionGraphics(page, idmp);
//			
//			//	nothing to show right now
//			else return NO_DISPLAY_EXTENSION_GRAPHICS;
//		}
		
		private UseParamPanel activeParamPanel = null;
		private ParameterDescription activeParamDescription = null;
		
		void setActiveParamPanel(UseParamPanel activeParamPanel) {
			this.activeParamPanel = activeParamPanel;
			if (this.activeParamPanel == null)
				this.activeParamDescription = null;
			else this.activeParamDescription = ((this.paramGroupDescription == null) ? null : this.paramGroupDescription.getParameterDescription(this.activeParamPanel.docStyleParamName));
//			ggImagine.notifyDisplayExtensionsModified(null);
		}
		
		void paramUsageChanged(UseParamPanel paramPanel) {
			this.updateParamStates(paramPanel);
		}
		
		void paramValueChanged(UseParamPanel paramPanel) {
			this.updateParamStates(paramPanel);
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
				if (!upp.useParam.isSelected())
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
				String pv = upp.getValue();
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
//				return this.prefix.substring(this.prefix.lastIndexOf('.') + ".".length());
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
//					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_FONT_SIZE_PROPERTY);
//					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_MINIMUM_FONT_SIZE_PROPERTY);
//					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_MAXIMUM_FONT_SIZE_PROPERTY);
//					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_IS_BOLD_PROPERTY);
//					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_IS_ITALICS_PROPERTY);
//					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_IS_ALL_CAPS_PROPERTY);
//					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_PATTERN_PROPERTY);
//					ptn.addParamName(this.paramGroupName + "." + PageFeatureAnchor.TARGET_AREA_PROPERTY);
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
				
				//	add anchor test facilities
				if (this.paramGroupName.startsWith(Anchor.ANCHOR_PREFIX + ".")) {
					JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 5), true);
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
					buttonPanel.setBorder(BorderFactory.createLineBorder(buttonPanel.getBackground(), 5));
					this.paramPanel.add(buttonPanel, BorderLayout.SOUTH);
				}
//				
//				//	add group test button if group description is testable or can visualize its content
//				else if ((this.paramGroupDescription instanceof DisplayExtension) || (this.paramGroupDescription instanceof DisplayExtension)) {
//					JButton testButton = new JButton("Test");
//					testButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(testButton.getBackground(), 5), BorderFactory.createRaisedBevelBorder()));
//					testButton.addActionListener(new ActionListener() {
//						public void actionPerformed(ActionEvent ae) {
//							if (paramGroupDescription instanceof TestableElement) {
//								DocumentStyle dspg = docStyle.getSubset(paramGroupName).toDocStyle();
//								((TestableElement) paramGroupDescription).test(dspg); // test the parameter group description
//							}
//							if (paramGroupDescription instanceof DisplayExtension)
//								setActiveParamPanel(null); // this triggers update of display extensions
//						}
//					});
//					this.paramPanel.add(testButton, BorderLayout.SOUTH);
//				}
			}
			
			//	make changes show
			this.paramPanel.validate();
			this.paramPanel.repaint();
			this.updateParamStates(null);
			
			//	update display extensions
			this.setActiveParamPanel(null);
		}
		
		void testAnchor(ParamTreeNode ptn) {
			
			//	get anchor settings
			DocStyleSettings anchorParamList = this.docStyle.getSubset(ptn.prefix);
//			
//			//	get bounding box
//			BoundingBox area = BoundingBox.parse(anchorParamList.getSetting("area"));
//			if (area == null)
//				return;
//			
//			//	get pattern
//			String pattern = anchorParamList.getSetting("pattern");
//			if (pattern == null)
//				return;
//			
//			//	get font sizes and perform test
//			try {
//				ArrayList matchLog = new ArrayList();
//				int anchorMaxPageId = Integer.parseInt(this.docStyle.getSetting("anchor.maxPageId", this.docStyle.getSetting("layout.coverPageCount", "0")));
//				boolean anchorMatch = false;
//				for (int p = 0; p <= anchorMaxPageId; p++) {
//					matchLog.add("Testing page " + p + ":");
//					ImPage testPage = this.testDoc.getPage((this.testDoc.getFirstPageId() + p));
//					if (testPage == null)
//						continue;
//					anchorMatch = PageFeatureAnchor.matches(testPage,
//							area,
//							Integer.parseInt(anchorParamList.getSetting("minFontSize", anchorParamList.getSetting("fontSize", "0"))),
//							Integer.parseInt(anchorParamList.getSetting("maxFontSize", anchorParamList.getSetting("fontSize", "72"))),
//							"true".equals(anchorParamList.getSetting("isBold")),
//							"true".equals(anchorParamList.getSetting("isItalics")),
//							"true".equals(anchorParamList.getSetting("isAllCaps")),
//							pattern,
//							matchLog);
////					anchorMatch = DocumentStyleProvider.anchorMatches(this.testDoc,
////							(this.testDoc.getFirstPageId() + p),
////							area,
////							Integer.parseInt(anchorParamList.getSetting("minFontSize", anchorParamList.getSetting("fontSize", "0"))),
////							Integer.parseInt(anchorParamList.getSetting("maxFontSize", anchorParamList.getSetting("fontSize", "72"))),
////							"true".equals(anchorParamList.getSetting("isBold")),
////							"true".equals(anchorParamList.getSetting("isItalics")),
////							"true".equals(anchorParamList.getSetting("isAllCaps")),
////							pattern,
////							matchLog);
//					if (anchorMatch)
//						break;
//				}
//				String anchorName = ptn.prefix.substring(ptn.prefix.lastIndexOf('.') + ".".length());
//				StringBuffer anchorMatchLog = new StringBuffer();
//				for (int l = 0; l < matchLog.size(); l++) {
//					anchorMatchLog.append("\r\n");
//					anchorMatchLog.append((String) matchLog.get(l));
//				}
//				JOptionPane.showMessageDialog(this, ("This document " + (anchorMatch ? " matches " : " does not match ") + " anchor '" + anchorName + "':" + anchorMatchLog.toString()), "Anchor Match Test", (anchorMatch ? JOptionPane.PLAIN_MESSAGE : JOptionPane.ERROR_MESSAGE));
//			} catch (NumberFormatException nfe) {}
		}
		
		void removeAnchor(ParamTreeNode ptn) {
			
			//	remove settings
			for (Iterator pnit = ptn.paramNames.iterator(); pnit.hasNext();)
				this.docStyle.removeSetting((String) pnit.next());
			
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
				String pv = this.docStyle.getSetting(pn);
				pvf.setValue((pv == null) ? "" : pv);
				pvf.useParam.setSelected(pv != null);
			}
			
			//	no further updates required
			if (pgn.equals(this.paramGroupName))
				return;
			
			//	set param group name and get corresponding tree node
			this.paramGroupName = pgn;
			this.paramGroupDescription = getParameterGroupDescription(this.paramGroupName);
			ParamTreeNode ptn = ((ParamTreeNode) this.paramTreeNodesByPrefix.get(this.paramGroupName));
			
			//	if we're creating an anchor, and only then, the tree node is null
			if (ptn == null) {
				ParamTreeNode pptn = ((ParamTreeNode) this.paramTreeNodesByPrefix.get("anchor"));
				ptn = new ParamTreeNode(pgn, pptn);
				ptn.addParamName(this.paramGroupName + ".fontSize");
				ptn.addParamName(this.paramGroupName + ".minFontSize");
				ptn.addParamName(this.paramGroupName + ".maxFontSize");
				ptn.addParamName(this.paramGroupName + ".isBold");
				ptn.addParamName(this.paramGroupName + ".isItalics");
				ptn.addParamName(this.paramGroupName + ".isAllCaps");
				ptn.addParamName(this.paramGroupName + ".pattern");
				ptn.addParamName(this.paramGroupName + ".area");
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
				pvf = this.createParamValueField(this, pn);
				this.paramValueFields.put(pn, pvf);
			}
			return pvf;
		}
		
		private class ParamToggleListener implements ItemListener {
			private UseParamPanel upp;
			ParamToggleListener(UseParamPanel upp) {
				this.upp = upp;
			}
			public void itemStateChanged(ItemEvent ie) {
				if (this.upp.useParam.isSelected()) {
					String dspv = this.upp.getValue();
					if (this.upp.verifyValue(dspv))
						docStyle.setSetting(this.upp.docStyleParamName, dspv);
				}
				else docStyle.removeSetting(this.upp.docStyleParamName);
			}
		}
		
		private UseParamPanel createParamValueField(DocStyleEditor parent, final String pn) {
			final Class pvc = getParamValueClass(pn);
			String pv = ((this.docStyle == null) ? null : this.docStyle.getSetting(pn));
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
			
			//	boolean, use plain checkbox
			if (Boolean.class.getName().equals(pvc.getName())) {
				UseBooleanPanel pvf = new UseBooleanPanel(parent, pn, pl, pd, "true".equals(pv)) {
					void notifyModified() {
						setDocStyleDirty(true);
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
					pvf = new UseStringPanel(parent, pn, pl, pd, ps, ((pv == null) ? "" : pv), false, false) {
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
								docStyle.setSetting(this.docStyleParamName, string);
							setDocStyleDirty(true);
						}
//						DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
//							if (this.docStyleParamName.endsWith(".fontSize") || this.docStyleParamName.endsWith("FontSize"))
//								return getFontSizeVisualizationGraphics(this.parent, page, this.getValue());
//							if (this.docStyleParamName.endsWith(".margin") || this.docStyleParamName.endsWith("Margin"))
//								return getMarginVisualizationGraphics(this.parent, page, this.getValue());
//							//	TODO think of more
//							return NO_DISPLAY_EXTENSION_GRAPHICS;
//						}
					};
				else pvf = new UseStringOptionPanel(parent, pn, pvs, pvls, pl, pd, ps, ((pv == null) ? "" : pv), false) {
					void stringChanged(String string) {
						if (string.length() == 0)
							this.useParam.setSelected(false);
						else if (this.useParam.isSelected() && this.verifyValue(string))
							docStyle.setSetting(this.docStyleParamName, string);
						setDocStyleDirty(true);
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
					pvf = new UseStringPanel(parent, pn, pl, pd, ps, ((pv == null) ? "" : pv), false, false) {
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
								docStyle.setSetting(this.docStyleParamName, string);
							setDocStyleDirty(true);
						}
//						DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
//							return NO_DISPLAY_EXTENSION_GRAPHICS;
//						}
					};
				else pvf = new UseStringOptionPanel(parent, pn, pvs, pvls, pl, pd, ps, ((pv == null) ? "" : pv), false) {
					void stringChanged(String string) {
						if (string.length() == 0)
							this.useParam.setSelected(false);
						else if (this.useParam.isSelected() && this.verifyValue(string))
							docStyle.setSetting(this.docStyleParamName, string);
						setDocStyleDirty(true);
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
					pvf = new UseStringPanel(parent, pn, pl, pd, ps, ((pv == null) ? "" : pv), false, false) {
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
								docStyle.setSetting(this.docStyleParamName, string);
							setDocStyleDirty(true);
						}
//						DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
//							return NO_DISPLAY_EXTENSION_GRAPHICS;
//						}
					};
				else pvf = new UseStringOptionPanel(parent, pn, pvs, pvls, pl, pd, ps, ((pv == null) ? "" : pv), false) {
					void stringChanged(String string) {
						if (string.length() == 0)
							this.useParam.setSelected(false);
						else if (this.useParam.isSelected() && this.verifyValue(string))
							docStyle.setSetting(this.docStyleParamName, string);
						setDocStyleDirty(true);
					}
				};
				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
				return pvf;
			}
//			
//			//	bounding box, use string field
//			else if (BoundingBox.class.getName().equals(pvc.getName())) {
//				UseStringPanel pvf = new UseStringPanel(parent, pn, pl, pd, ps, ((pv == null) ? "" : pv), false, false) {
//					boolean verifyValue(String value) {
//						try {
//							return (BoundingBox.parse(this.getValue()) != null);
//						}
//						catch (RuntimeException re) {
//							return false;
//						}
//					}
//					void stringChanged(String string) {
//						if (string.length() == 0)
//							this.useParam.setSelected(false);
//						else if (this.useParam.isSelected() && this.verifyValue(string))
//							docStyle.setSetting(this.docStyleParamName, string);
//						setDocStyleDirty(true);
//					}
//					DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
//						return getBoundingBoxVisualizationGraphics(this.parent, page, this.getValue());
//					}
//				};
//				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
//				return pvf;
//			}
			
			//	string, use string field
			else if (String.class.getName().equals(pvc.getName())) {
				UseParamPanel pvf;
				if (pvs == null)
					pvf = new UseStringPanel(parent, pn, pl, pd, ps, ((pv == null) ? "" : pv), false, (pn.endsWith(".pattern") || pn.endsWith("Pattern"))) {
//						ImTokenSequence getTestDocTokens() {
//							if (testDocTokens == null)
//								testDocTokens = new ImTokenSequence(((Tokenizer) testDoc.getAttribute(ImDocument.TOKENIZER_ATTRIBUTE, Gamta.INNER_PUNCTUATION_TOKENIZER)), testDoc.getTextStreamHeads(), (ImTokenSequence.NORMALIZATION_LEVEL_PARAGRAPHS | ImTokenSequence.NORMALIZE_CHARACTERS));
//							return testDocTokens;
//						}
//						ImDocument getTestDoc() {
//							return testDoc;
//						}
						boolean verifyValue(String value) {
							if (pn.endsWith(".linePattern") || pn.endsWith("LinePattern")) {
								try {
//									LinePattern.parsePattern(value);
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
								docStyle.setSetting(this.docStyleParamName, string);
							setDocStyleDirty(true);
						}
//						DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
//							if (this.docStyleParamName.endsWith(".linePattern") || this.docStyleParamName.endsWith("LinePattern"))
//								return getLinePatternVisualizationGraphics(this.parent, page, this.getValue());
//							if (pn.endsWith(".pattern") || pn.endsWith("Pattern"))
//								return getPatternVisualizationGraphics(this.parent, page, this.getValue());
//							return NO_DISPLAY_EXTENSION_GRAPHICS;
//						}
					};
				else pvf = new UseStringOptionPanel(parent, pn, pvs, pvls, pl, pd, ps, ((pv == null) ? "" : pv), false) {
					void stringChanged(String string) {
						if (string.length() == 0)
							this.useParam.setSelected(false);
						else if (this.useParam.isSelected() && this.verifyValue(string))
							docStyle.setSetting(this.docStyleParamName, string);
						setDocStyleDirty(true);
					}
				};
				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
				return pvf;
			}
			
			//	string, use specialized string field
			else if (Pattern.class.getName().equals(pvc.getName())) {
				UseParamPanel pvf;
				if (pvs == null)
					pvf = new UseStringPanel(parent, pn, pl, pd, ps, ((pv == null) ? "" : pv), false, true) {
//						TokenSequence getTestDocTokens() {
//							if (testDocTokens == null)
//								testDocTokens = new ImTokenSequence(((Tokenizer) testDoc.getAttribute(ImDocument.TOKENIZER_ATTRIBUTE, Gamta.INNER_PUNCTUATION_TOKENIZER)), testDoc.getTextStreamHeads(), (ImTokenSequence.NORMALIZATION_LEVEL_PARAGRAPHS | ImTokenSequence.NORMALIZE_CHARACTERS));
//							return testDocTokens;
//						}
//						ImDocument getTestDoc() {
//							return testDoc;
//						}
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
								docStyle.setSetting(this.docStyleParamName, string);
							setDocStyleDirty(true);
						}
//						DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
//							if (this.docStyleParamName.endsWith(".linePattern") || this.docStyleParamName.endsWith("LinePattern"))
//								return getLinePatternVisualizationGraphics(this.parent, page, this.getValue());
//							if (pn.endsWith(".pattern") || pn.endsWith("Pattern"))
//								return getPatternVisualizationGraphics(this.parent, page, this.getValue());
//							return NO_DISPLAY_EXTENSION_GRAPHICS;
//						}
					};
				else pvf = new UseStringOptionPanel(parent, pn, pvs, pvls, pl, pd, ps, ((pv == null) ? "" : pv), false) {
					void stringChanged(String string) {
						if (string.length() == 0)
							this.useParam.setSelected(false);
						else if (this.useParam.isSelected() && this.verifyValue(string))
							docStyle.setSetting(this.docStyleParamName, string);
						setDocStyleDirty(true);
					}
				};
				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
				return pvf;
			}
			
			//	list, use list field
			else if (DocumentStyle.getListElementClass(pvc) != pvc) {
				final Class pvlec = DocumentStyle.getListElementClass(pvc);
				UseListPanel pvf = new UseListPanel(parent, pn, pl, pd, ps, ((pv == null) ? "" : pv), (pn.endsWith(".patterns") || pn.endsWith("Patterns"))) {
//					ImTokenSequence getTestDocTokens() {
//						if (testDocTokens == null)
//							testDocTokens = new ImTokenSequence(((Tokenizer) testDoc.getAttribute(ImDocument.TOKENIZER_ATTRIBUTE, Gamta.INNER_PUNCTUATION_TOKENIZER)), testDoc.getTextStreamHeads(), (ImTokenSequence.NORMALIZATION_LEVEL_PARAGRAPHS | ImTokenSequence.NORMALIZE_CHARACTERS));
//						return testDocTokens;
//					}
//					ImDocument getTestDoc() {
//						return testDoc;
//					}
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
//						else if (BoundingBox.class.getName().equals(pvlec.getName())) {
//							try {
//								return (BoundingBox.parse(this.getValue()) != null);
//							}
//							catch (RuntimeException re) {
//								return false;
//							}
//						}
						else if (String.class.getName().equals(pvlec.getName())) {
							if (pn.endsWith(".linePatterns") || pn.endsWith("LinePatterns")) {
								try {
//									LinePattern.parsePattern(valuePart);
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
							docStyle.setSetting(this.docStyleParamName, string.replaceAll("\\s+", " "));
						setDocStyleDirty(true);
					}
//					DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
//						String[] valueParts = this.getValue().split("\\s+");
//						ArrayList degList = new ArrayList();
//						for (int p = 0; p < valueParts.length; p++)
//							degList.addAll(Arrays.asList(this.getDisplayExtensionGraphicsPart(page, valueParts[p])));
//						return ((DisplayExtensionGraphics[]) degList.toArray(new DisplayExtensionGraphics[degList.size()]));
//					}
//					DisplayExtensionGraphics[] getDisplayExtensionGraphicsPart(ImPage page, String valuePart) {
//						if (Integer.class.getName().equals(pvlec.getName())) {
//							if (this.docStyleParamName.endsWith(".fontSize") || this.docStyleParamName.endsWith("FontSize"))
//								return getFontSizeVisualizationGraphics(this.parent, page, valuePart);
//							if (this.docStyleParamName.endsWith(".margin") || this.docStyleParamName.endsWith("Margin"))
//								return getMarginVisualizationGraphics(this.parent, page, valuePart);
//							//	TODO think of more
//						}
//						else if (BoundingBox.class.getName().equals(pvlec.getName()))
//							return getBoundingBoxVisualizationGraphics(this.parent, page, valuePart);
//						else if (String.class.getName().equals(pvlec.getName())) {
//							if (this.docStyleParamName.endsWith(".linePatterns") || this.docStyleParamName.endsWith("LinePatterns"))
//								return getLinePatternVisualizationGraphics(this.parent, page, valuePart);
//							if (pn.endsWith(".patterns") || pn.endsWith("Patterns"))
//								return getPatternVisualizationGraphics(this.parent, page, valuePart);
//						}
//						else if (Pattern.class.getName().equals(pvlec.getName()))
//							return getPatternVisualizationGraphics(this.parent, page, valuePart);
//						return NO_DISPLAY_EXTENSION_GRAPHICS;
//					}
				};
				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
				return pvf;
			}
			
			//	as the ultimate fallback, use string field
			else {
				UseStringPanel pvf = new UseStringPanel(parent, pn, pl, pd, ps, ((pv == null) ? "" : pv), false, false) {
					void stringChanged(String string) {
						if (string.length() == 0)
							this.useParam.setSelected(false);
						else if (this.useParam.isSelected() && this.verifyValue(string))
							docStyle.setSetting(this.docStyleParamName, string);
						setDocStyleDirty(true);
					}
//					DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
//						return NO_DISPLAY_EXTENSION_GRAPHICS;
//					}
				};
				pvf.useParam.addItemListener(new ParamToggleListener(pvf));
				return pvf;
			}
		}
	}
	
	private static class ParamTreeCellRenderer extends DefaultTreeCellRenderer {
		private Icon rootIcon = null;
		ParamTreeCellRenderer() {
			String packageName = AbstractDocumentStyleManager.class.getName();
			packageName = packageName.replace('.', '/');
			try {
				this.setClosedIcon(new ImageIcon(ImageIO.read(AbstractDocumentStyleManager.class.getClassLoader().getResourceAsStream(packageName + ".paramTree.closed.png"))));
				this.setOpenIcon(new ImageIcon(ImageIO.read(AbstractDocumentStyleManager.class.getClassLoader().getResourceAsStream(packageName + ".paramTree.open.png"))));
				this.setLeafIcon(new ImageIcon(ImageIO.read(AbstractDocumentStyleManager.class.getClassLoader().getResourceAsStream(packageName + ".paramTree.leaf.png"))));
				this.rootIcon = new ImageIcon(ImageIO.read(AbstractDocumentStyleManager.class.getClassLoader().getResourceAsStream(packageName + ".paramTree.root.png")));
			} catch (IOException ioe) { /* never gonna happen, but Java don't know */ }
		}
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if ((this.rootIcon != null) && (value != null) && (value instanceof TreeNode) && (((TreeNode) value).getParent() == null))
				this.setIcon(this.rootIcon);
			return this;
		}
	}
//	
//	private static DisplayExtensionGraphics[] getLinePatternVisualizationGraphics(DocStyleEditor parent, ImPage page, String pattern) {
//		try {
//			if (pattern.trim().length() == 0)
//				return NO_DISPLAY_EXTENSION_GRAPHICS;
//			LinePattern lp = LinePattern.parsePattern(pattern);
//			ImRegion[] lines = lp.getMatches(page);
//			if (lines.length == 0)
//				return NO_DISPLAY_EXTENSION_GRAPHICS;
//			DisplayExtensionGraphics[] degs = new DisplayExtensionGraphics[lines.length];
//			for (int l = 0; l < lines.length; l++)
//				degs[l] = getBoundingBoxVisualizationGraphics(parent, page, lines[l].bounds);
//			return degs;
//		}
//		catch (IllegalArgumentException iae) {
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		}
//		catch (Exception e) {
//			e.printStackTrace(System.out);
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		}
//	}
//	
//	private static DisplayExtensionGraphics[] getPatternVisualizationGraphics(DocStyleEditor parent, ImPage page, String pattern) {
//		try {
//			if (pattern.trim().length() == 0)
//				return NO_DISPLAY_EXTENSION_GRAPHICS;
//			ImDocumentRoot pageTokens = new ImDocumentRoot(page, (ImTokenSequence.NORMALIZATION_LEVEL_PARAGRAPHS | ImTokenSequence.NORMALIZE_CHARACTERS | ImDocumentRoot.INCLUDE_PAGE_TITLES));
//			Annotation[] patternMatches = Gamta.extractAllMatches(pageTokens, pattern, 32, false, false, false);
//			ArrayList pmWords = new ArrayList();
//			for (int m = 0; m < patternMatches.length; m++) {
//				ImWord pmStartWord = pageTokens.wordAtIndex(patternMatches[m].getStartIndex());
//				ImWord pmEndWord = pageTokens.wordAtIndex(patternMatches[m].getEndIndex() - 1);
//				for (ImWord pmWord = pmStartWord; pmWord != null; pmWord = pmWord.getNextWord()) {
//					if (pmWord.pageId != page.pageId)
//						break;
//					pmWords.add(pmWord);
//					if (pmWord == pmEndWord)
//						break;
//				}
//			}
//			return getWordVisualizationGraphics(parent, page, ((ImWord[]) pmWords.toArray(new ImWord[pmWords.size()])));
//		}
//		catch (IllegalArgumentException iae) {
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		}
//		catch (Exception e) {
//			e.printStackTrace(System.out);
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		}
//	}
//	
//	private static DisplayExtensionGraphics[] getFontSizeVisualizationGraphics(DocStyleEditor parent, ImPage page, String fontSize) {
//		if (fontSize == null)
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		fontSize = fontSize.trim();
//		if (fontSize.length() == 0)
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		try {
//			ArrayList fsWords = new ArrayList();
//			ImWord[] pWords = page.getWords();
//			for (int w = 0; w < pWords.length; w++) {
//				if (fontSize.equals(pWords[w].getAttribute(ImWord.FONT_SIZE_ATTRIBUTE)))
//					fsWords.add(pWords[w]);
//			}
//			return getWordVisualizationGraphics(parent, page, ((ImWord[]) fsWords.toArray(new ImWord[fsWords.size()])));
//		}
//		catch (Exception e) {
//			e.printStackTrace(System.out);
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		}
//	}
//	
//	private static DisplayExtensionGraphics[] getFontStyleVisualizationGraphics(DocStyleEditor parent, ImPage page, String attributeName) {
//		if (attributeName == null)
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		attributeName = attributeName.trim();
//		if (attributeName.length() == 0)
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		try {
//			ArrayList fsWords = new ArrayList();
//			ImWord[] pWords = page.getWords();
//			for (int w = 0; w < pWords.length; w++) {
//				if (pWords[w].hasAttribute(attributeName))
//					fsWords.add(pWords[w]);
//				else if ("allCaps".equals(attributeName) && isAllCaps(pWords[w].getString()))
//					fsWords.add(pWords[w]);
//			}
//			return getWordVisualizationGraphics(parent, page, ((ImWord[]) fsWords.toArray(new ImWord[fsWords.size()])));
//		}
//		catch (Exception e) {
//			e.printStackTrace(System.out);
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		}
//	}
//	private static boolean isAllCaps(String str) {
//		return (str.equals(str.toUpperCase()) && !str.equals(str.toLowerCase()));
//	}
//	
//	private static final Color wordLineColor = Color.ORANGE;
//	private static final BasicStroke wordLineStroke = new BasicStroke(1);
//	private static final Color wordFillColor = new Color(wordLineColor.getRed(), wordLineColor.getGreen(), wordLineColor.getBlue(), 64);
//	private static DisplayExtensionGraphics[] getWordVisualizationGraphics(DocStyleEditor parent, ImPage page, ImWord[] words) {
//		Shape[] shapes = new Shape[words.length];
//		for (int w = 0; w < words.length; w++)
//			shapes[w] = new Rectangle2D.Float(words[w].bounds.left, words[w].bounds.top, words[w].bounds.getWidth(), words[w].bounds.getHeight());
//		DisplayExtensionGraphics[] degs = {new DisplayExtensionGraphics(parent, null, page, shapes, wordLineColor, wordLineStroke, wordFillColor) {
//			public boolean isActive() {
//				return true;
//			}
//		}};
//		return degs;
//	}
//	
//	private static DisplayExtensionGraphics[] getMarginVisualizationGraphics(DocStyleEditor parent, ImPage page, String margin) {
//		try {
//			/* TODO figure out how to implement this:
//			 * - based upon lines?
//			 * - based upon paragraphs?
//			 * - based upon blocks?
//			 */
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		}
//		catch (IllegalArgumentException iae) {
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		}
//		catch (Exception e) {
//			e.printStackTrace(System.out);
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		}
//	}
//	
//	private static final Color boundingBoxLineColor = Color.GREEN;
//	private static final BasicStroke boundingBoxLineStroke = new BasicStroke(3);
//	private static final Color boundingBoxFillColor = new Color(boundingBoxLineColor.getRed(), boundingBoxLineColor.getGreen(), boundingBoxLineColor.getBlue(), 64);
//	private static DisplayExtensionGraphics[] getBoundingBoxVisualizationGraphics(DocStyleEditor parent, ImPage page, String box) {
//		try {
//			BoundingBox bb = BoundingBox.parse(box);
//			if (bb == null)
//				return NO_DISPLAY_EXTENSION_GRAPHICS;
//			bb = bb.scale(((float) page.getImageDPI()) / ImDocumentStyle.DEFAULT_DPI);
//			DisplayExtensionGraphics[] degs = {getBoundingBoxVisualizationGraphics(parent, page, bb)};
//			return degs;
//		}
//		catch (IllegalArgumentException iae) {
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		}
//		catch (Exception e) {
//			e.printStackTrace(System.out);
//			return NO_DISPLAY_EXTENSION_GRAPHICS;
//		}
//	}
//	private static DisplayExtensionGraphics getBoundingBoxVisualizationGraphics(DocStyleEditor parent, ImPage page, BoundingBox bb) {
//		Shape[] shapes = {new Rectangle2D.Float(bb.left, bb.top, bb.getWidth(), bb.getHeight())};
//		return new DisplayExtensionGraphics(parent, null, page, shapes, boundingBoxLineColor, boundingBoxLineStroke, boundingBoxFillColor) {
//			public boolean isActive() {
//				return true;
//			}
//		};
//	}
	
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
	
	/* TODO 
- when loading document style parameter lists, copy them into genuine Properties object via putAll() instead of handing out view on Settings
- introduce "parent" parameter
  - load parent parameter list ...
  - ... and use it as default in handed out Properties
  ==> facilitates parameter value inheritance, vastly reducing maintenance effort
  	 */
	public static void main(String[] args) throws Exception {
		System.out.println(buildPattern("ZOOTAXA  109:"));
	}
}