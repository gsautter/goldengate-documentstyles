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
package de.uka.ipd.idaho.goldenGate.docStyles.plugins.editor;

import de.uka.ipd.idaho.gamta.util.DocumentStyle;
import de.uka.ipd.idaho.gamta.util.DocumentStyle.Data;
import de.uka.ipd.idaho.goldenGate.docStyles.plugins.AbstractDocumentStyleProvider;


/**
 * This plug-in provides document style parameter lists for XML documents to
 * others.
 * 
 * @author sautter
 */
public class DocumentStyleProvider extends AbstractDocumentStyleProvider {
	
	/** zero-argument constructor for class loading */
	public DocumentStyleProvider() {
		super("Document Style Provider");
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.docStyles.plugins.AbstractDocumentStyleProvider#init()
	 */
	public void init() {
		DocumentStyle.getStyleFor(null); // make damn sure class is loaded and initialized before superclass uses it
		super.init();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.docStyles.plugins.AbstractDocumentStyleProvider#wrapDocumentStyle(de.uka.ipd.idaho.gamta.util.DocumentStyle.Data)
	 */
	protected DocumentStyle wrapDocumentStyle(Data docStyleData) {
		return new DocStyle(docStyleData);
	}
	
	static class DocStyle extends DocumentStyle {
		private Data pData = null;
		DocStyle(Data data) {
			super(data);
		}
		void setPreferredData(Data pData) {
			this.pData = pData;
		}
		public String getPropertyData(String key) {
			if (this.pData != null) {
				String data = this.pData.getPropertyData(key);
				if (data != null)
					return data;
			}
			return super.getPropertyData(key);
		}
	}
}
