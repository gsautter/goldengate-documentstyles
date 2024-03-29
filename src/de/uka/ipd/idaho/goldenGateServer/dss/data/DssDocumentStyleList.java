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
 *     * Neither the name of the Universitaet Karlsruhe (TH) nor the
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
package de.uka.ipd.idaho.goldenGateServer.dss.data;


import java.io.IOException;
import java.io.Reader;

import de.uka.ipd.idaho.gamta.util.DocumentStyle;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentList;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentListElement;
import de.uka.ipd.idaho.goldenGateServer.dss.GoldenGateDssConstants;

/**
 * List of document styles in a GoldenGATE DSS, implemented iterator-style for
 * efficiency.
 * 
 * @author sautter
 */
public abstract class DssDocumentStyleList extends DocumentList implements GoldenGateDssConstants {
	
	/**
	 * Constructor for general use
	 * @param listFieldNames the field names for the document style list, in
	 *            the order they should be displayed
	 */
	public DssDocumentStyleList(String[] listFieldNames) {
		super(listFieldNames);
	}
	
	/**
	 * Constructor for creating wrappers
	 * @param model the document style list to wrap
	 */
	public DssDocumentStyleList(DocumentList model) {
		super(model);
	}
	
	public boolean hasNoSummary(String listFieldName) {
		return true;
	}
	
	public boolean isNumeric(String listFieldName) {
		return (CHECKIN_TIME_ATTRIBUTE.equals(listFieldName) || UPDATE_TIME_ATTRIBUTE.equals(listFieldName) || DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE.equals(listFieldName) || DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE.equals(listFieldName));
	}
	
	public boolean isFilterable(String listFieldName) {
		return false;
	}
	
	/**
	 * Wrap a document style list around a reader, which provides the list's
	 * data in form of a character stream. Do not close the specified reader
	 * after this method returns. The reader is closed by the returned list
	 * after the last document list element is read.
	 * @param in the Reader to read from
	 * @return a document style list that makes the data from the specified
	 *         reader available as document list elements
	 * @throws IOException
	 */
	public static DssDocumentStyleList readDocumentList(Reader in) throws IOException {
		return readDocumentList(in, ProgressMonitor.silent);
	}
	
	/**
	 * Wrap a document style list around a reader, which provides the list's
	 * data in form of a character stream. Do not close the specified reader
	 * after this method returns. The reader is closed by the returned list
	 * after the last document list element is read.
	 * @param in the Reader to read from
	 * @param pm a progress monitor observing the reading process
	 * @return a document style list that makes the data from the specified
	 *         reader available as document list elements
	 * @throws IOException
	 */
	public static DssDocumentStyleList readDocumentList(Reader in, ProgressMonitor pm) throws IOException {
		
		//	wrap DSS specific behavior around generic list
		final DocumentList dl = DocumentList.readDocumentList(in, pm);
		return new DssDocumentStyleList(dl) {
			public boolean hasNextDocument() {
				return dl.hasNextDocument();
			}
			public DocumentListElement getNextDocument() {
				return dl.getNextDocument();
			}
			public int getDocumentCount() {
				return dl.getDocumentCount();
			}
			public int getRetrievedDocumentCount() {
				return dl.getRetrievedDocumentCount();
			}
			public int getRemainingDocumentCount() {
				return dl.getRemainingDocumentCount();
			}
			public boolean hasNoSummary(String listFieldName) {
				return (super.hasNoSummary(listFieldName) || dl.hasNoSummary(listFieldName));
			}
			public boolean isNumeric(String listFieldName) {
				return (super.isNumeric(listFieldName) || dl.isNumeric(listFieldName));
			}
			public boolean isFilterable(String listFieldName) {
				return (super.isFilterable(listFieldName) || dl.isFilterable(listFieldName));
			}
		};
	}
}