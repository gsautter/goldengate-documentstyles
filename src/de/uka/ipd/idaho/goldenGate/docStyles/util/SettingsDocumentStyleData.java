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
package de.uka.ipd.idaho.goldenGate.docStyles.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.AbstractData;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.Data;


/**
 * Settings based implementation of document style template data.
 * 
 * @author sautter
 */
public class SettingsDocumentStyleData extends AbstractData {
	private Settings data;
	
	/** Constructor
	 * @param data the setting to wrap
	 */
	public SettingsDocumentStyleData(Settings data) {
		this.data = data;
	}
	
	/**
	 * Retrieve the wrapped settings data.
	 * @return the wrapped settings data
	 */
	public Settings getData() {
		return this.data;
	}
	
	/**
	 * Replace the wrapped settings data. If a document style might already be
	 * assigned to a document, this method should be used with extreme care.
	 * @param data the new settings data to wrap
	 */
	public void setData(Settings data) {
		this.data = data;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.util.DocumentStyle.Data#getPropertyData(java.lang.String)
	 */
	public String getPropertyData(String key) {
		return this.data.getSetting(key);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.util.DocumentStyle.Data#getPropertyNames()
	 */
	public String[] getPropertyNames() {
		return this.data.getKeys();
	}
	
	/**
	 * Output the data contained in the document style parameter list to some
	 * <code>Writer</code> as text formatted <code>Settings</code>. Attributes
	 * are stored in a subset prefixed with '@'. If the argument
	 * <code>Writer</code> is not a <code>BufferedWriter</code>, it will be
	 * wrapped in one, and flushed after all data is written.
	 * @param out the writer to write to
	 * @throws IOException
	 */
	public void writeData(Writer out) throws IOException {
		writeData(this, out);
	}
	
	/**
	 * Output the data contained in a document style parameter data object to
	 * some <code>Writer</code> as text formatted <code>Settings</code>. The
	 * attributes of the argument document style data object are stored in a
	 * subset prefixed with '@'. If the argument <code>Writer</code> is not a
	 * <code>BufferedWriter</code>, it will be wrapped in one, and flushed
	 * after all data is written.
	 * @param data the document style data object to write
	 * @param out the writer to write to
	 * @throws IOException
	 */
	public static void writeData(Data data, Writer out) throws IOException {
		BufferedWriter bw = ((out instanceof BufferedWriter) ? ((BufferedWriter) out) : new BufferedWriter(out));
		
		//	create and populate settings
		Settings dsSet = new Settings();
		if (data instanceof SettingsDocumentStyle)
			dsSet.setSettings(((SettingsDocumentStyleData) data).data);
		else {
			String[] dsPropertyNames = data.getPropertyNames();
			for (int p = 0; p < dsPropertyNames.length; p++) {
				String propertyValue = data.getPropertyData(dsPropertyNames[p]);
				if (propertyValue != null)
					dsSet.setSetting(dsPropertyNames[p], propertyValue);
			}
		}
		
		//	add attributes
		Settings dsAttributeSet = dsSet.getSubset("@");
		String[] dsAttributeNames = data.getAttributeNames();
		for (int n = 0; n < dsAttributeNames.length; n++) {
			Object attributeValue = data.getAttribute(dsAttributeNames[n]);
			if (attributeValue != null)
				dsAttributeSet.setSetting(dsAttributeNames[n], attributeValue.toString());
		}
		
		//	write data
		dsSet.storeAsText(bw);
		
		//	finally ...
		if (bw != out)
			bw.flush();
	}
	
	/**
	 * Deserialize the data for a document style parameter list from a
	 * <code>Reader</code>. This method reads the character stream from the
	 * argument <code>Reader</code> until the end. Settings in the subset with
	 * prefix '@' are interpreted as attributes in the returned document style
	 * data object.
	 * @param in the reader to read from
	 * @return the deserialized document style data object
	 * @throws IOException
	 */
	public static SettingsDocumentStyleData readDocumentStyleData(Reader in) throws IOException {
		BufferedReader br = ((in instanceof BufferedReader) ? ((BufferedReader) in) : new BufferedReader(in));
		
		//	read data
		Settings dsSet = Settings.loadSettings(br);
		if ((dsSet == null) || (dsSet.isEmpty()))
			return null;
		
		//	create document style data
		SettingsDocumentStyleData dsData = new SettingsDocumentStyleData(dsSet);
		
		//	handle attributes
		Settings dsAttributeSet = dsSet.getSubset("@");
		String[] dsAttributeNames = dsAttributeSet.getKeys();
		for (int n = 0; n < dsAttributeNames.length; n++)
			dsData.setAttribute(dsAttributeNames[n], dsAttributeSet.getSetting(dsAttributeNames[n]));
		dsSet.removeSubset(dsAttributeSet);
		
		//	finally ...
		return dsData;
	}
}
