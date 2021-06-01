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
package de.uka.ipd.idaho.goldenGateServer.dss;

import de.uka.ipd.idaho.goldenGateServer.GoldenGateServerConstants;
import de.uka.ipd.idaho.goldenGateServer.util.DataObjectUpdateConstants;

/**
 * Constant bearer interface for GoldenGATE Document Style Server (DSS)
 * 
 * @author sautter
 */
public interface GoldenGateDssConstants extends GoldenGateServerConstants, DataObjectUpdateConstants {
	
	/** the command for obtaining the timestamp of the last update from the backing server */
	public static final String GET_LAST_MODIFIED = "DSS_GET_LAST_MODIFIED";
	
	/** the command for obtaining the list of the available document styles from the backing server */
	public static final String LIST_STYLES = "DSS_LIST_STYLES";
	
	/** the command for obtaining a document style from the backing server */
	public static final String GET_STYLE = "DSS_GET_STYLE";
	
	/** the command for obtaining a document style from the backing server as a stream */
	public static final String GET_STYLE_AS_STREAM = "DSS_GET_STYLE_AS_STREAM";
	
	/** the command for updating a specific document style on the backing server */
	public static final String UPDATE_STYLE = "DSS_UPDATE_STYLE";
	
	/** the command for deleting a specific document style on the backing server */
	public static final String DELETE_STYLE = "DSS_DELETE_STYLE";
	
	/** the permission for uploading new or updating existing document styles in the DSS */
	public static final String UPDATE_DOCUMENT_STYLE_PERMISSION = "DSS.UpdateDocumentStyle";
	
	/** the permission for deleting document styles from the DSS */
	public static final String DELETE_DOCUMENT_STYLE_PERMISSION = "DSS.DeleteDocumentStyle";
}
