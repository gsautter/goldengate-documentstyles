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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.defaultImplementation.AbstractAttributed;
import de.uka.ipd.idaho.gamta.util.DocumentStyle;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.Anchor;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.Data;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.Provider;
import de.uka.ipd.idaho.gamta.util.ImmutableAttributed;
import de.uka.ipd.idaho.gamta.util.swing.DialogFactory;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentList;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentListBuffer;
import de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin;
import de.uka.ipd.idaho.stringUtils.csvHandler.StringTupel;

/**
 * Abstract implementation of a document style provider. This class handles
 * persistence and matching styles to documents by means of anchors. Subclasses
 * only have to provide (potentially implementation specific) document style
 * objects to wrap around the data provided by this abstract class.<br>
 * Be careful about having multiple concrete implementations of this class in
 * the same GoldenGATE instance, as this can incur priority conflicts.
 * 
 * @author sautter
 */
public abstract class AbstractDocumentStyleProvider extends AbstractGoldenGatePlugin implements DocumentStyle.Provider {
	private static final String[] localDocStyleListFieldNames = {
		DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE,
		DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE,
		DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE,
		DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE
	};
	private DocumentListBuffer localDocStyleList = null;
	private Map localDocStyleDataById = null;
	private Map docStyleTraysById;
	private Map docStyleTraysByName;
	private Map remoteDocStyleDataById;
	private String pluginName;
	
	/**
	 * @param pluginName the (subclass specific) plugin name 
	 */
	protected AbstractDocumentStyleProvider(String pluginName) {
		this.pluginName = pluginName;
	}

	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractResourceManager#getPluginName()
	 */
	public String getPluginName() {
		return this.pluginName;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#init()
	 */
	public void init() {
		
		//	register as document style provider
		DocumentStyle.addProvider(this);
	}
	
	private void ensureDocStylesLoaded() {
		if (this.docStyleTraysById == null)
			this.loadDocStyles();
	}
	
	private synchronized void loadDocStyles() {
		if (this.docStyleTraysById != null)
			return; // initialized before (while waiting on lock)
		this.docStyleTraysById = Collections.synchronizedMap(new HashMap());
		this.docStyleTraysByName = Collections.synchronizedMap(new TreeMap(String.CASE_INSENSITIVE_ORDER));
		
		//	get remote document style list in separate thread while loading local styles
		final String[] remoteDocStyleSourceUrl = {null};
		final DocumentListBuffer[] remoteDocStyleList = {null};
		Thread remoteDocStyleListLoader = null;
		if (this.dataProvider.isDataAvailable("config.cnfg")) try {
			BufferedReader cbr = new BufferedReader(new InputStreamReader(this.dataProvider.getInputStream("config.cnfg"), "UTF-8"));
			Settings config = Settings.loadSettings(cbr);
			cbr.close();
			remoteDocStyleSourceUrl[0] = config.getSetting("docStyleSourceBaseUrl");
			
			//	get list of document styles
			if (remoteDocStyleSourceUrl[0] != null) {
				if (!remoteDocStyleSourceUrl[0].endsWith("/"))
					remoteDocStyleSourceUrl[0] = (remoteDocStyleSourceUrl[0] + "/");
				remoteDocStyleListLoader = new Thread() {
					public void run() {
						try {
							remoteDocStyleList[0] = loadRemoteDocStyleList(remoteDocStyleSourceUrl[0]);
							System.out.println("Got remote document list with " + remoteDocStyleList[0].size() + " document styles");
						}
						catch (IOException ioe) {
							System.out.println("Error reading document style list from configured URL: " + ioe.getMessage());
							ioe.printStackTrace(System.out);
						}
					}
				};
				remoteDocStyleListLoader.start();
			}
		}
		catch (IOException ioe) {
			System.out.println("Error reading document styles from configured URL: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
		
		//	get list of document styles
		try {
			BufferedReader dslBr;
			if (this.dataProvider.isDataAvailable("cache/docStyles.txt")) // TODO remove this fork after migration period
				dslBr = new BufferedReader(new InputStreamReader(this.dataProvider.getInputStream("cache/docStyles.txt"), "UTF-8"));
			else dslBr = new BufferedReader(new InputStreamReader(this.dataProvider.getInputStream("docStyles.txt"), "UTF-8"));
//			BufferedReader dslBr = new BufferedReader(new InputStreamReader(this.dataProvider.getInputStream("docStyles.txt"), "UTF-8"));
			DocumentList dsl = DocumentList.readDocumentList(dslBr);
			this.localDocStyleList = new DocumentListBuffer(dsl);
		}
		catch (IOException ioe) {
			System.out.println("Error reading local document style list: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
			this.localDocStyleList = new DocumentListBuffer(localDocStyleListFieldNames);
		}
		
		//	load and index document styles
		TreeMap localDocStyleIndex = new TreeMap(String.CASE_INSENSITIVE_ORDER);
		this.localDocStyleDataById = Collections.synchronizedMap(new HashMap());
		for (int s = 0; s < this.localDocStyleList.size(); s++) {
			StringTupel dle = this.localDocStyleList.get(s);
			String docStyleId = dle.getValue(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE);
			Data docStyleData = this.getDocStyleDataById(docStyleId);
			if (docStyleData == null)
				continue;
			this.localDocStyleDataById.put(docStyleId, dle);
			localDocStyleIndex.put(docStyleId, docStyleData);
			String docStyleName = dle.getValue(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE);
			System.out.println("Got local document style '" + docStyleName + "' with ID " + docStyleId);
			localDocStyleIndex.put(docStyleName, docStyleData);
			if (this.removeNamedDocStyleData(docStyleName))
				System.out.println("Old document style '" + docStyleName + ".docStyle' removed.");
		}
		
		//	anything to expect from remote?
		if (remoteDocStyleListLoader == null)
			return;
		
		//	wait for remote document style list
		if (remoteDocStyleList[0] == null) {
//			for (int waitCount = 0; (waitCount < 10) && remoteDocStyleListLoader.isAlive(); waitCount++) try {
			for (int waitCount = 0; (waitCount < (this.parent.isStartupFinished() ? 10 : 120)) && remoteDocStyleListLoader.isAlive(); waitCount++) try {
				remoteDocStyleListLoader.join(500); // let's wait in half-second intervals (for at most 5 seconds, 60 in a startup call)
			} catch (InterruptedException ie) {}
			if (remoteDocStyleList[0] == null)
				return; // unable to get the list
		}
		
		//	update document styles from remote
		this.remoteDocStyleDataById = Collections.synchronizedMap(new HashMap());	
		for (int s = 0; s < remoteDocStyleList[0].size(); s++) {
			StringTupel remoteDocStyleAttributes = remoteDocStyleList[0].get(s);
			String docStyleId = remoteDocStyleAttributes.getValue(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE);
			this.remoteDocStyleDataById.put(docStyleId, remoteDocStyleAttributes);
			String docStyleName = remoteDocStyleAttributes.getValue(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE);
			System.out.println("Got remote document style '" + docStyleName + "' with ID " + docStyleId);
			
			Data localDocStyleData = null;
			if ((localDocStyleData == null) && (docStyleId != null))
				localDocStyleData = ((Data) localDocStyleIndex.get(docStyleId));
			if ((localDocStyleData == null) && (docStyleName != null))
				localDocStyleData = ((Data) localDocStyleIndex.get(docStyleName));
			
			if ((localDocStyleData != null) && (localDocStyleData.hasAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE))) try {
				long remoteLastModTime = Long.parseLong(remoteDocStyleAttributes.getValue(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, "-1"));
				long localLastModTime = Long.parseLong((String) localDocStyleData.getAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, "-1"));
				if ((remoteLastModTime != -1) && (remoteLastModTime <= localLastModTime)) {
					System.out.println(" ==> up to date locally");
					continue; // this one seems to be up to date
				}
			}
			catch (RuntimeException re) {
				System.out.println("Error checking timestamps of document style '" + docStyleName + "': " + re.getMessage());
				re.printStackTrace(System.out);
			}
			
			try {
				BufferedReader docStyleBr = new BufferedReader(new InputStreamReader(this.dataProvider.getURL(remoteDocStyleSourceUrl[0] + docStyleId).openStream(), "UTF-8"));
				Data docStyleData = DocumentStyle.readDocumentStyleData(docStyleBr);
				docStyleBr.close();
				String[] remoteDocStyleAns = remoteDocStyleAttributes.getKeyArray();
				for (int a = 0; a < remoteDocStyleAns.length; a++) {
					if (docStyleData.hasAttribute(remoteDocStyleAns[a]))
						continue;
					String remoteDocStyleAv = remoteDocStyleAttributes.getValue(remoteDocStyleAns[a]);
					if (remoteDocStyleAv == null)
						continue;
					remoteDocStyleAv = remoteDocStyleAv.trim();
					if (remoteDocStyleAv.length() == 0)
						continue;
					docStyleData.setAttribute(remoteDocStyleAns[a], remoteDocStyleAv);
				}
				if (this.storeDocStyleData(docStyleName, docStyleData)) {
					System.out.println(" ==> " + ((localDocStyleData == null) ? "loaded" : "updated") + " from URL");
					if (this.removeNamedDocStyleData(docStyleName))
						System.out.println("Old document style '" + docStyleName + ".docStyle' removed.");
				}
			}
			catch (IOException ioe) {
				System.out.println("Error reading document style '" + docStyleName + "' from configured URL: " + ioe.getMessage());
				ioe.printStackTrace(System.out);
			}
		}
	}
	
	DocumentListBuffer loadRemoteDocStyleList(String remoteDocStyleListUrl) throws IOException {
		if (!this.dataProvider.allowWebAccess()) {
			int choice = DialogFactory.confirm(("GoldenGATE is in offline mode. Allow getting document style templates from '" + remoteDocStyleListUrl + "' anyway?"), "Allow Get Document Style Templates?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (choice != JOptionPane.YES_OPTION)
				return null;
		}
		
		//	get list of document styles
		BufferedReader rdslBr = new BufferedReader(new InputStreamReader(this.dataProvider.getURL(remoteDocStyleListUrl + "list.txt").openStream(), "UTF-8"));
		DocumentList rdsl = DocumentList.readDocumentList(rdslBr);
		return new DocumentListBuffer(rdsl);
	}
	
	/**
	 * @deprecated remove after POA data migration
	 */
	private boolean removeNamedDocStyleData(String docStyleName) {
		String docStyleDataName = (docStyleName + ".docStyle");
		if (this.dataProvider.isDataAvailable(docStyleDataName))
			return this.dataProvider.deleteData(docStyleDataName);
		else return false;
	}
	
	/**
	 * Retrieve the attributes of all document style data objects available
	 * from the provider. The returned array is in no particular order.
	 * @param remote get the attributes of document style data objects loaded
	 *            from remote?
	 * @return an array holding the attributes of the available document style
	 *            data objects
	 */
	public Attributed[] getDocStyleAttributes(boolean remote) {
		ArrayList docStyleAttributeList = new ArrayList(this.docStyleTraysById.size());
		for (Iterator dsidit = this.docStyleTraysById.keySet().iterator(); dsidit.hasNext();) {
			String docStyleId = ((String) dsidit.next());
			Attributed docStyleAttributes = this.getDocStyleAttributes(docStyleId, remote);
			if (docStyleAttributes != null)
				docStyleAttributeList.add(docStyleAttributes);
		}
		return ((Attributed[]) docStyleAttributeList.toArray(new Attributed[docStyleAttributeList.size()]));
	}
	
	/**
	 * Retrieve the attributes of a document style data object with a given ID.
	 * If there is no document style data object with the argument ID, this
	 * method returns null.
	 * @param docStyleId the ID of the document style data object whose
	 *            attributes to retrieve
	 * @param remote get the attributes of a document style data object loaded
	 *            from remote?
	 * @return the attributes of the document style data object with the
	 *            argument ID
	 */
	public Attributed getDocStyleAttributes(String docStyleId, boolean remote) {
		Attributed docStyleAttributes;
		
		//	create attributed object from remote string tupel
		if (remote) {
			if (this.remoteDocStyleDataById == null)
				return null; // no remote access
			StringTupel dssAttributes = ((StringTupel) this.remoteDocStyleDataById.get(docStyleId));
			if (dssAttributes == null)
				return null;
			docStyleAttributes = new AbstractAttributed();
			String[] attributeNames = dssAttributes.getKeyArray();
			for (int a = 0; a < attributeNames.length; a++) {
				String attributeValue = dssAttributes.getValue(attributeNames[a]);
				if ((attributeValue != null) && (attributeValue.length() != 0))
					docStyleAttributes.setAttribute(attributeNames[a], attributeValue);
			}
		}
		
		//	hand out current underlying data so object doesn't change (better for short term use)
		else {
			TrayData trayData = this.getDocStyleTrayDataById(docStyleId);
			if (trayData == null)
				return null;
			docStyleAttributes = trayData.data;
		}
		
		//	finally ...
		return new ImmutableAttributed(docStyleAttributes);
	}
	
	/**
	 * Retrieve a document style data object by its name. If there is no
	 * document style data object with the argument name, this method returns
	 * null.
	 * @param docStyleName the name of the document style data object
	 * @return the document style data object with the argument name
	 */
	public Data getDocStyleDataByName(String docStyleName) {
		DocumentStyleTray docStyleTray = ((DocumentStyleTray) this.docStyleTraysByName.get(docStyleName));
		return ((docStyleTray == null) ? null : docStyleTray.docStyleData);
	}
	
	/**
	 * Retrieve a document style data object by its ID. If there is no document
	 * style data object with the argument ID, this method returns null.
	 * @param docStyleId the ID of the document style data object
	 * @return the document style data object with the argument ID
	 */
	public Data getDocStyleDataById(String docStyleId) {
		return this.getDocStyleTrayDataById(docStyleId);
	}
	
	private TrayData getDocStyleTrayDataById(String docStyleId) {
		
		//	return pre-loaded style first, so updates go to (possibly already shared) instance
		DocumentStyleTray docStyleTray = ((DocumentStyleTray) this.docStyleTraysById.get(docStyleId));
		if (docStyleTray != null)
			return docStyleTray.docStyleData;
		
		//	check availability first
		String docStyleDataName = ("cache/" + docStyleId + ".docStyle");
		if (!this.dataProvider.isDataAvailable(docStyleDataName))
			return null;
		
		//	try and load new style not currently held in memory
		Data docStyleData;
		try {
			BufferedReader docStyleBr = new BufferedReader(new InputStreamReader(this.dataProvider.getInputStream(docStyleDataName), "UTF-8"));
			docStyleData = DocumentStyle.readDocumentStyleData(docStyleBr);
			docStyleBr.close();
		}
		catch (IOException ioe) {
			System.out.println("Error reading document style '" + docStyleId + "': " + ioe.getMessage());
			ioe.printStackTrace(System.out);
			return null;
		}
		String docStyleName = ((String) docStyleData.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE));
		return this.cacheDocumentStyleTray(docStyleName, docStyleData);
	}
	
	/**
	 * Store a document style data object. The argument document style data
	 * object will also become available as the basis for the document style
	 * objects returned by the <code>getStyleFor()</code> method, and it will
	 * replace any earlier ones with the same ID or name.
	 * @param docStyleName the name of the document style the data object
	 *            describes
	 * @param docStyleData the document style data object to store
	 * @return true if the document style data object was stored successfully
	 */
	protected boolean storeDocStyleData(String docStyleName, Data docStyleData) {
		if (!this.dataProvider.isDataEditable())
			return false;
		
		String docStyleId = ((String) docStyleData.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE));
		if (docStyleId == null)
			throw new IllegalArgumentException("Cannot store document style '" + docStyleName + "' without ID.");
		DocumentStyleTray docStyleTray = ((DocumentStyleTray) this.docStyleTraysById.get(docStyleId));
		if ((docStyleTray != null) && !docStyleName.equals(docStyleTray.docStyleName)) {
			this.docStyleTraysByName.remove(docStyleTray.docStyleName);
			docStyleTray.docStyleName = docStyleName;
			this.docStyleTraysByName.put(docStyleName, docStyleTray);
		}
		
		String docStyleDataName = ("cache/" + docStyleId + ".docStyle");
		if (!this.dataProvider.isDataEditable(docStyleDataName))
			return false;
		if (!docStyleData.hasAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE))
			docStyleData.setAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE, docStyleId);
		
		try {
			BufferedWriter docStyleBw = new BufferedWriter(new OutputStreamWriter(this.dataProvider.getOutputStream(docStyleDataName), "UTF-8"));
			DocumentStyle.writeData(docStyleData, docStyleBw);
			docStyleBw.flush();
			docStyleBw.close();
			
			//	update list of local document styles right away (updates should not be all that frequent, especially with the catches we have in place)
			StringTupel dle = ((StringTupel) this.localDocStyleDataById.get(docStyleId));
			if (dle == null) {
				dle = new StringTupel();
				this.localDocStyleList.addElement(dle);
				this.localDocStyleDataById.put(docStyleId, dle);
				dle.setValue(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE, docStyleId);
			}
			dle.setValue(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, docStyleName);
			if (docStyleData.hasAttribute(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE))
				dle.setValue(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE, ((String) docStyleData.getAttribute(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE)));
			if (docStyleData.hasAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE))
				dle.setValue(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, ((String) docStyleData.getAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE)));
			BufferedWriter docStyleListBw = new BufferedWriter(new OutputStreamWriter(this.dataProvider.getOutputStream("cache/docStyles.txt"), "UTF-8"));
			for (int f = 0; f < this.localDocStyleList.listFieldNames.length; f++) {
				if (f != 0)
					docStyleListBw.write("\t");
				docStyleListBw.write(this.localDocStyleList.listFieldNames[f]);
			}
			docStyleListBw.newLine();
			for (int s = 0; s < this.localDocStyleList.size(); s++) {
				dle = this.localDocStyleList.get(s);
				for (int f = 0; f < this.localDocStyleList.listFieldNames.length; f++) {
					if (f != 0)
						docStyleListBw.write("\t");
					docStyleListBw.write(dle.getValue(this.localDocStyleList.listFieldNames[f], ""));
				}
				docStyleListBw.newLine();
			}
			docStyleListBw.flush();
			docStyleListBw.close();
			
			//	update rather than replace data underneath existing document styles, so changes become available wherever existing style object is in use
			if (docStyleTray == null)
				docStyleTray = ((DocumentStyleTray) this.docStyleTraysById.get(docStyleId));
			if (docStyleTray == null)
				docStyleTray = ((DocumentStyleTray) this.docStyleTraysByName.get(docStyleName));
			if (docStyleTray == null)
				this.cacheDocumentStyleTray(docStyleName, docStyleData);
			else docStyleTray.docStyleData.setData(docStyleData);
			return true;
		}
		catch (IOException ioe) {
			System.out.println("Error storing document style '" + docStyleId + ":" + docStyleName + "': " + ioe.getMessage());
			ioe.printStackTrace(System.out);
			return false;
		}
	}
	
	private TrayData cacheDocumentStyleTray(String docStyleName, Data docStyleData) {
		TrayData trayDocStyleData = new TrayData(docStyleData);
		DocumentStyleTray docStyleTray = new DocumentStyleTray(docStyleName, trayDocStyleData, this.wrapDocumentStyle(trayDocStyleData));
		docStyleTray.docStyle.setAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, docStyleName);
		
		String docStyleId = ((String) trayDocStyleData.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE));
		if (docStyleId != null)
			this.docStyleTraysById.put(docStyleId, docStyleTray);
		this.docStyleTraysByName.put(docStyleName, docStyleTray);
		
		return trayDocStyleData;
	}
	
	/**
	 * Put a subclass specific wrapper around a document style data object.
	 * @param docStyleData the document style data to wrap
	 * @return the document style wrapped around the argument data
	 */
	protected abstract DocumentStyle wrapDocumentStyle(Data docStyleData);
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.plugins.docStyle.DocumentStyle.Provider#getStyleFor(de.uka.ipd.idaho.gamta.Attributed)
	 */
	public DocumentStyle getStyleFor(Attributed doc) {
		this.ensureDocStylesLoaded();
		
		String docStyleId = ((String) doc.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE));
		if (docStyleId != null) {
			DocumentStyleTray docStyleTray = ((DocumentStyleTray) this.docStyleTraysById.get(docStyleId));
			if (docStyleTray != null)
				return docStyleTray.docStyle;
		}
		
		String docStyleName = ((String) doc.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE));
		if (docStyleName != null) {
			DocumentStyleTray docStyleTray = ((DocumentStyleTray) this.docStyleTraysByName.get(docStyleName));
			if (docStyleTray != null)
				return docStyleTray.docStyle;
		}
		
		return this.findStyleFor(doc);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.util.DocumentStyle.Provider#documentStyleAssigned(de.uka.ipd.idaho.gamta.util.DocumentStyle, de.uka.ipd.idaho.gamta.Attributed)
	 */
	public void documentStyleAssigned(DocumentStyle docStyle, Attributed doc) {
		if (docStyle.hasAttribute(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE))
			doc.setAttribute(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE, docStyle.getAttribute(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE));
	}
	
	private DocumentStyle findStyleFor(Attributed doc) {
		
		//	use ID for cache lookup if given
		String docStyleId = ((String) doc.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE));
		if ((docStyleId != null) && this.docStyleTraysById.containsKey(docStyleId))
			return ((DocumentStyleTray) this.docStyleTraysById.get(docStyleId)).docStyle;
		
		//	use name for cache lookup if given
		String docStyleName = ((String) doc.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE));
		if ((docStyleName != null) && this.docStyleTraysByName.containsKey(docStyleName))
			return ((DocumentStyleTray) this.docStyleTraysByName.get(docStyleName)).docStyle;
		
		//	use anchors to find (best) matching style
		ArrayList docStyleMatches = new ArrayList();
		for (Iterator dsnit = this.docStyleTraysByName.keySet().iterator(); dsnit.hasNext();) {
			docStyleName = ((String) dsnit.next());
			DocumentStyleTray docStyleTray = ((DocumentStyleTray) this.docStyleTraysByName.get(docStyleName));
			System.out.println("Testing " + docStyleName + " with " + docStyleTray.anchors.length + " anchors");
			DocumentStyleMatch docStyleMatch = docStyleTray.matchAgainst(doc);
			if (docStyleMatch != null)
				docStyleMatches.add(docStyleMatch);
		}
		if (docStyleMatches.isEmpty())
			return null; // nothing to work with ...
		
		//	return best matching style
		Collections.sort(docStyleMatches);
		return ((DocumentStyleMatch) docStyleMatches.get(0)).docStyle;
	}
	
	private static class TrayData implements Data {
		Data data;
		DocumentStyleTray docStyleTray;
		TrayData(Data data) {
			this.data = data;
		}
		void setData(Data data) {
			if (data instanceof TrayData)
				this.setData(((TrayData) data).data);
			else {
				this.data = data;
				if (this.docStyleTray != null)
					this.docStyleTray.updateAnchors();
			}
		}
		public void setAttribute(String name) {
			this.data.setAttribute(name);
		}
		public Object setAttribute(String name, Object value) {
			return this.data.setAttribute(name, value);
		}
		public void copyAttributes(Attributed source) {
			this.data.copyAttributes(source);
		}
		public Object getAttribute(String name) {
			return this.data.getAttribute(name);
		}
		public Object getAttribute(String name, Object def) {
			return this.data.getAttribute(name, def);
		}
		public boolean hasAttribute(String name) {
			return this.data.hasAttribute(name);
		}
		public String[] getAttributeNames() {
			return this.data.getAttributeNames();
		}
		public Object removeAttribute(String name) {
			return this.data.removeAttribute(name);
		}
		public void clearAttributes() {
			this.data.clearAttributes();
		}
		public String getPropertyData(String key) {
			return this.data.getPropertyData(key);
		}
		public String[] getPropertyNames() {
			return this.data.getPropertyNames();
		}
		public Provider getProvider() {
			return this.data.getProvider();
		}
		public void setProvider(Provider provider) {
			this.data.setProvider(provider);
		}
	}
	
	private static class DocumentStyleTray {
		Anchor[] anchors;
		String docStyleName;
		DocumentStyle docStyle;
		TrayData docStyleData;
		DocumentStyleTray(String docStyleName, TrayData docStyleData, DocumentStyle docStyle) {
			this.docStyleName = docStyleName;
			this.docStyle = docStyle;
			this.docStyleData = docStyleData;
			this.docStyleData.docStyleTray = this;
			this.updateAnchors();
		}
		void updateAnchors() {
			this.anchors = Anchor.getAnchors(this.docStyle);
		}
		DocumentStyleMatch matchAgainst(Attributed doc) {
			int anchorsMatched = 0;
			for (int a = 0; a < this.anchors.length; a++) {
				if (this.anchors[a].matches(doc))
					anchorsMatched++;
				else if (this.anchors[a].isRequired)
					return null;
			}
			return ((anchorsMatched == 0) ? null : new DocumentStyleMatch(this.docStyle, this.anchors.length, anchorsMatched));
		}
	}
	
	private static class DocumentStyleMatch implements Comparable {
		DocumentStyle docStyle;
		int anchorsTested;
		int anchorsMatched;
		DocumentStyleMatch(DocumentStyle docStyle, int anchorsTested, int anchorsMatched) {
			this.docStyle = docStyle;
			this.anchorsTested = anchorsTested;
			this.anchorsMatched = anchorsMatched;
		}
		public int compareTo(Object obj) {
			if (obj == this)
				return 0;
			DocumentStyleMatch dsm = ((DocumentStyleMatch) obj);
//			int c = ((dsm.anchorsMatched / dsm.anchorsTested) - (this.anchorsMatched / this.anchorsTested)); // descending order by match percentage
			int c = ((dsm.anchorsMatched * this.anchorsTested) - (this.anchorsMatched * dsm.anchorsTested)); // descending order by match percentage
			if (c != 0)
				return c;
			c = (dsm.anchorsTested - this.anchorsTested); // descending order by number of anchors
			if (c != 0)
				return c;
			return 0;
		}
	}
}
