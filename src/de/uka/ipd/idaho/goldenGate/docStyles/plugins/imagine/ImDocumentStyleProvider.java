/*
< * Copyright (c) 2006-, IPD Boehm, Universitaet Karlsruhe (TH) / KIT, by Guido Sautter
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.util.DocumentStyle;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.Data;
import de.uka.ipd.idaho.goldenGate.docStyles.plugins.AbstractDocumentStyleProvider;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePlugin;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImaginePlugin;
import de.uka.ipd.idaho.im.util.ImDocumentStyle;

/**
 * This plug-in provides document style parameter lists for Image Markup
 * documents to others.
 * 
 * @author sautter
 */
public class ImDocumentStyleProvider extends AbstractDocumentStyleProvider implements GoldenGateImaginePlugin {
	private Map docStylesById = Collections.synchronizedMap(new HashMap());
	private Map liveDocStyleDataById = Collections.synchronizedMap(new HashMap());
	
	/** zero-argument constructor for class loading */
	public ImDocumentStyleProvider() {
		super("IM Document Style Provider");
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImaginePlugin#setImagineParent(de.uka.ipd.idaho.im.imagine.GoldenGateImagine)
	 */
	public void setImagineParent(GoldenGateImagine ggImagine) { /* we don't really need GG Imagine, at least for now */ }
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.docStyles.plugins.AbstractDocumentStyleProvider#init()
	 */
	public void init() {
		ImDocumentStyle.getStyleFor(null); // make damn sure class is loaded and initialized before superclass uses it
		super.init();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImaginePlugin#initImagine()
	 */
	public void initImagine() {
		
		//	remove any other plug-in based providers but ourselves
		GoldenGatePlugin[] dsps = this.parent.getImplementingPlugins(AbstractDocumentStyleProvider.class);
		for (int p = 0; p < dsps.length; p++) {
			if (dsps[p] != this)
				DocumentStyle.removeProvider((AbstractDocumentStyleProvider) dsps[p]);
		}
	}
	
	interface LiveData extends Data {
		
		/**
		 * Receive notification that a property value was retrieved by consumer
		 * code.
		 * @param key the name of the property
		 * @param value the returned property value
		 * @param fromLiveData was the value retrieved from this data object?
		 */
		public abstract void propertyDataRetrieved(String key, String value, boolean fromLiveData);
	}
	
	void setLiveData(String docStyleId, LiveData lDocStyleData) {
		ImDocStyle docStyle = ((ImDocStyle) this.docStylesById.get(docStyleId));
		if (docStyle != null)
			docStyle.setLiveData(lDocStyleData);
		if (lDocStyleData == null)
			this.liveDocStyleDataById.remove(docStyleId);
		else this.liveDocStyleDataById.put(docStyleId, lDocStyleData);
	}
	
	DocumentStyle getDocStyleById(String docStyleId) {
		Data docStyleData = this.getDocStyleDataById(docStyleId); // calls wrap() before caching tray
		return ((docStyleData == null) ? null : this.wrapDocumentStyle(docStyleData)); // finds document style by ID and replaces data with itself
	}
	
	boolean publishDocumentStyle(String docStyleName, Data docStyleData) {
		return this.storeDocStyleData(docStyleName, docStyleData);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.docStyles.plugins.AbstractDocumentStyleProvider#wrapDocumentStyle(de.uka.ipd.idaho.gamta.util.DocumentStyle.Data)
	 */
	protected DocumentStyle wrapDocumentStyle(Data docStyleData) {
		String docStyleId = ((String) docStyleData.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE));
		ImDocStyle docStyle = ((ImDocStyle) this.docStylesById.get(docStyleId));
		if (docStyle == null) {
			docStyle = new ImDocStyle(docStyleData);
			this.docStylesById.put(docStyleId, docStyle);
		}
		else docStyle.setData(docStyleData); // will happen on publishing a newly created style
		docStyle.setLiveData((LiveData) this.liveDocStyleDataById.get(docStyleId));
		return docStyle;
	}
	
	static class ImDocStyle extends ImDocumentStyle {
		private LiveData liveData = null;
		ImDocStyle(Data data) {
			super(data);
			if (data instanceof LiveData) // will happen when creating new style, before publishing
				this.liveData = ((LiveData) data);
		}
		void setData(Data data) {
			this.data = data; // will happen when directly getting style on assignment
			if (data instanceof LiveData) // should not happen, but let's try and track this !!!
				this.liveData = ((LiveData) data);
		}
		void setLiveData(LiveData liveData) {
			this.liveData = liveData;
		}
		public String[] getPropertyNames() {
			if (this.liveData != null)
				return this.liveData.getPropertyNames();
			else return super.getPropertyNames();
		}
		public String getPropertyData(String key) {
			if (this.liveData != null) {
				String data = this.liveData.getPropertyData(key);
				if (data != null) {
					this.liveData.propertyDataRetrieved(key, data, true);
					return data;
				}
			}
			String data = super.getPropertyData(key);
			if (data != null) {
				if (this.liveData != null)
					this.liveData.propertyDataRetrieved(key, data, false);
				return data;
			}
			//	emulate anchor specific settings for maximum page ID using Image Markup specific fallbacks
			if ((Anchor.ANCHOR_PREFIX + "." + PageFeatureAnchor.MAXIMUM_PAGES_AFTER_FIRST_PROPERTY).equals(key)) {
				data = this.getPropertyData("layout.coverPageCount");
				if (data == null)
					data = "0";
			}
			else if (key.startsWith(Anchor.ANCHOR_PREFIX + ".") && key.endsWith("." + PageFeatureAnchor.MAXIMUM_PAGES_AFTER_FIRST_PROPERTY)) {
				data = this.getPropertyData(Anchor.ANCHOR_PREFIX + "." + PageFeatureAnchor.MAXIMUM_PAGES_AFTER_FIRST_PROPERTY);
				if (data == null)
					data = "0";
			}
			return data;
		}
	}
	
	/**
	 * @deprecated remove after POA data migration
	 */
	String[] getNamedDocStyleNames() {
		String[] dataNames = this.dataProvider.getDataNames();
		TreeSet docStyleDataNames = new TreeSet(String.CASE_INSENSITIVE_ORDER);
		for (int s = 0; s < dataNames.length; s++) {
			if (dataNames[s].startsWith("cache/"))
				continue; // this is the new mode
			if (dataNames[s].endsWith(".docStyle"))
				docStyleDataNames.add(dataNames[s].substring(0, (dataNames[s].length() - ".docStyle".length())));
		}
		return ((String[]) docStyleDataNames.toArray(new String[docStyleDataNames.size()]));
	}
	
	/**
	 * @deprecated remove after POA data migration
	 */
	Settings getNamedDocStyleData(String docStyleName) throws IOException {
		String docStyleDataName = (docStyleName + ".docStyle");
		BufferedReader docStyleBr = new BufferedReader(new InputStreamReader(this.dataProvider.getInputStream(docStyleDataName), "UTF-8"));
		Settings docStyleSet = Settings.loadSettings(docStyleBr);
		docStyleBr.close();
		return docStyleSet;
	}
	
	/**
	 * @deprecated remove after POA data migration
	 */
	boolean deleteNamedDocStyleData(String docStyleName) {
		String docStyleDataName = (docStyleName + ".docStyle");
		if (this.dataProvider.isDataAvailable(docStyleDataName))
			return this.dataProvider.deleteData(docStyleDataName);
		else return false;
	}
}