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

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.util.DocumentStyle;

/**
 * Settings based document style parameter list, easier to handle for resource
 * managers.
 * 
 * @author sautter
 */
public class SettingsDocumentStyle extends DocumentStyle {
	private SettingsDocumentStyleData setData;
	
	/** Constructor
	 * @param data the data to wrap
	 */
	public SettingsDocumentStyle(Settings data) {
		this(new SettingsDocumentStyleData(data));
	}
	
	/** Constructor
	 * @param data the data to wrap
	 */
	public SettingsDocumentStyle(SettingsDocumentStyleData data) {
		super(data);
		this.setData = data;
	}
	
	/**
	 * Retrieve the wrapped settings data.
	 * @return the wrapped settings data
	 */
	public SettingsDocumentStyleData getSettingsData() {
		return this.setData;
	}
	
	/**
	 * Replace the wrapped settings data. If a document style might already be
	 * assigned to a document, this method should be used with extreme care.
	 * @param data the new settings data to wrap
	 */
	public void setSettingsData(SettingsDocumentStyleData data) {
		this.data = data;
		this.setData = data;
	}
	
	/**
	 * Retrieve the plain wrapped settings data.
	 * @return the wrapped settings data
	 */
	public Settings getSettings() {
		return this.setData.getData();
	}
	
	/**
	 * Replace the wrapped settings data. If a document style might already be
	 * assigned to a document, this method should be used with extreme care.
	 * @param data the new settings data to wrap
	 */
	public void setSettings(Settings data) {
		this.setData.setData(data);
	}
}
