/*
 * Copyright (c) 2006-2008, IPD Boehm, Universitaet Karlsruhe (TH)
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
 * THIS SOFTWARE IS PROVIDED BY UNIVERSITAET KARLSRUHE (TH) AND CONTRIBUTORS 
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

package de.uka.ipd.idaho.goldenGateServer.dss.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;

import de.uka.ipd.idaho.gamta.util.DocumentStyle;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentListElement;
import de.uka.ipd.idaho.goldenGateServer.client.ServerConnection;
import de.uka.ipd.idaho.goldenGateServer.client.ServerConnection.Connection;
import de.uka.ipd.idaho.goldenGateServer.dss.GoldenGateDssConstants;
import de.uka.ipd.idaho.goldenGateServer.dss.data.DssDocumentStyleList;
import de.uka.ipd.idaho.goldenGateServer.uaa.client.AuthenticatedClient;
import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * A client for remotely accessing the document style parameter lists provided
 * by a GoldenGATE DSS.
 * 
 * @author sautter
 */
public class GoldenGateDssClient implements GoldenGateDssConstants {
	private ServerConnection serverCon;
	private AuthenticatedClient authClient;
	
	/** Constructor with authentication
	 * @param authClient the authenticated client to use for authentication and connection 
	 */
	public GoldenGateDssClient(AuthenticatedClient authClient) {
		this.authClient = authClient;
	}
	
	/** Constructor without authentication (read access only)
	 * @param serverCon the server connection to use for communication with the backing DSS
	 */
	public GoldenGateDssClient(ServerConnection serverCon) {
		this.serverCon = serverCon;
	}
	
	private Connection getConnection() throws IOException {
		return ((this.authClient == null) ? this.serverCon.getConnection() : this.authClient.getConnection());
	}
	
	/**
	 * Obtain the timestamp of the last modification to the document style
	 * templates hosted in the backing DSS.
	 * @return the timestamp of the last modification
	 */
	public long getLastModified() throws IOException {
		Connection con = null;
		try {
			con = this.getConnection();
			BufferedWriter bw = con.getWriter();
			
			bw.write(GET_LAST_MODIFIED);
			bw.newLine();
			bw.flush();
			
			BufferedReader br = con.getReader();
			String error = br.readLine();
			if (GET_LAST_MODIFIED.equals(error))
				return Long.parseLong(br.readLine());
			else throw new IOException(error);
		}
		finally {
			if (con != null)
				con.close();
		}
	}
	
	/**
	 * Obtain the list of document styles available from the DSS.
	 * @return the list of document styles available from the backing DSS
	 */
	public DssDocumentStyleList getDocumentStyleList() throws IOException {
		return this.getDocumentStyleList(ProgressMonitor.silent);
	}
	
	/**
	 * Obtain the list of document styles available from the DSS.
	 * @param pm a progress monitor to observe the loading process
	 * @return the list of document styles available from the backing DSS
	 */
	public DssDocumentStyleList getDocumentStyleList(ProgressMonitor pm) throws IOException {
		final Connection con = this.getConnection();
		BufferedWriter bw = con.getWriter();
		
		bw.write(LIST_STYLES);
		bw.newLine();
		bw.flush();
		
		final BufferedReader br = con.getReader();
		String error = br.readLine();
		if (LIST_STYLES.equals(error))
			return DssDocumentStyleList.readDocumentList(new Reader() {
				public void close() throws IOException {
					br.close();
					con.close();
				}
				public int read(char[] cbuf, int off, int len) throws IOException {
					return br.read(cbuf, off, len);
				}
			}, pm);
		else {
			con.close();
			throw new IOException(error);
		}
	}
	
	/**
	 * Obtain a document style from the DSS. The valid document style IDs can
	 * be read from the document style list returned by getDocumentStyleList().
	 * @param docStyleId the ID of the document style to load
	 * @return the document style with the specified ID
	 */
	public DocumentStyle getDocumentStyle(String docStyleId) throws IOException {
		return this.getDocumentStyle(docStyleId, 0);
	}
	
	/**
	 * Obtain a document style from the DSS. The valid document style IDs can
	 * be read from the document style list returned by getDocumentStyleList().
	 * @param docStyleId the ID of the document style to load
	 * @param version the number of the document style version to load
	 * @return the specified version of the document style with the specified ID
	 */
	public DocumentStyle getDocumentStyle(String docStyleId, int version) throws IOException {
		Connection con = null;
		try {
			con = this.getConnection();
			BufferedWriter bw = con.getWriter();
			
			bw.write(GET_STYLE);
			bw.newLine();
			bw.write(docStyleId + ((version == 0) ? "" : ("." + version)));
			bw.newLine();
			bw.flush();
			
			BufferedReader br = con.getReader();
			String error = br.readLine();
			if (GET_STYLE.equals(error))
				return DocumentStyle.readDocumentStyle(br);
			else throw new IOException(error);
		}
		finally {
			if (con != null)
				con.close();
		}
	}
	
	/**
	 * Store/update a document style in the backing DSS. Client code must set
	 * the <code>docStyleId</code> attribute of a document style before handing
	 * it to this method, and is mandated to keep this ID persistent an any
	 * later calls to this method. Failure to do the latter will result in
	 * duplication.
	 * @param docStyle the document style to store
	 * @param docStyleName the name of the document style (may be null if the
	 *            document style was loaded from the backing DSS, for then it
	 *            has the name as an attribute)
	 * @return a document list element holding the updated metadata of the
	 *            document style.
	 */
	public DocumentListElement updateDocumentStyle(DocumentStyle docStyle, String docStyleName) throws IOException {
		return this.updateDocumentStyleFromData(docStyle.getData(), docStyleName);
	}
	
	/**
	 * Store/update a document style represented by its underlying data object
	 * in the backing DSS. Client code must set the <code>docStyleId</code>
	 * attribute of a document style before handing it to this method, and is
	 * mandated to keep this ID persistent an any later calls to this method.
	 * Failure to do the latter will result in duplication.
	 * @param docStyleData the document style data object to store
	 * @param docStyleName the name of the document style (may be null if the
	 *            document style was loaded from the backing DSS, for then it
	 *            has the name as an attribute)
	 * @return a document list element holding the updated metadata of the
	 *            document style.
	 */
	public DocumentListElement updateDocumentStyleFromData(DocumentStyle.Data docStyleData, String docStyleName) throws IOException {
		if (this.authClient == null)
			throw new IOException("No authenticated connection.");
		if (!this.authClient.isLoggedIn())
			throw new IOException("Not logged in.");
		
		//	make sure we have a document style ID and name
		String docStyleId = ((String) docStyleData.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE));
		if (docStyleId == null)
			throw new IllegalArgumentException("Document style ID missing.");
		if (!docStyleId.matches("[0-9A-F]{32}"))
			throw new IllegalArgumentException("Invalid document style ID '" + docStyleId + "'.");
		docStyleName = ((docStyleName == null) ? docStyleData.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, "Unknown Document Style").toString() : docStyleName);
		
		//	do update
		Connection con = null;
		try {
			con = this.authClient.getConnection();
			BufferedWriter bw = con.getWriter();
			
			bw.write(UPDATE_STYLE);
			bw.newLine();
			bw.write(this.authClient.getSessionID());
			bw.newLine();
			bw.write(docStyleName);
			bw.newLine();
			DocumentStyle.writeData(docStyleData, bw);
			bw.newLine();
			bw.flush();
			
			BufferedReader br = con.getReader();
			String error = br.readLine();
			if (UPDATE_STYLE.equals(error)) {
				DocumentListElement dle = new DocumentListElement();
				for (String dleLine; (dleLine = br.readLine()) != null;) {
					dleLine = dleLine.trim();
					if (dleLine.length() == 0)
						break;
					if (dleLine.indexOf("\t") == -1)
						dle.setAttribute(dleLine);
					else {
						String dleAttributeName = dleLine.substring(0, dleLine.indexOf("\t")).trim();
						String dleAttributeValue = dleLine.substring(dleLine.indexOf("\t") + "\t".length()).trim();
						dle.setAttribute(dleAttributeName, dleAttributeValue);
					}
				}
				return dle;
//				StringVector log = new StringVector();
//				for (String logEntry; (logEntry = br.readLine()) != null;)
//					log.addElement(logEntry);
//				return log.toStringArray();
			}
			else throw new IOException(error);
		}
		finally {
			if (con != null)
				con.close();
		}
	}
	
	/**
	 * Delete a documents style from the DSS.
	 * @param docStyleId the ID of the document style to delete
	 * @return an array holding the logging messages collected during the
	 *         deletion process.
	 */
	public String[] deleteDocumentStyle(String docStyleId) throws IOException {
		if (this.authClient == null)
			throw new IOException("No authenticated connection.");
		if (!this.authClient.isLoggedIn())
			throw new IOException("Not logged in.");
		
		Connection con = null;
		try {
			con = this.authClient.getConnection();
			BufferedWriter bw = con.getWriter();
			
			bw.write(DELETE_STYLE);
			bw.newLine();
			bw.write(this.authClient.getSessionID());
			bw.newLine();
			bw.write(docStyleId);
			bw.newLine();
			bw.flush();
			
			BufferedReader br = con.getReader();
			String error = br.readLine();
			if (DELETE_STYLE.equals(error)) {
				StringVector log = new StringVector();
				for (String logEntry; (logEntry = br.readLine()) != null;)
					log.addElement(logEntry);
				return log.toStringArray();
			}
			else throw new IOException(error);
		}
		catch (Exception e) {
			throw new IOException(e.getMessage());
		}
		finally {
			if (con != null)
				con.close();
		}
	}
//	
//	public static void main(String[] args) throws Exception {
//		ApplicationHttpsEnabler.enableHttps();
//		ServerConnection sCon = ServerConnection.getServerConnection("https://tb.plazi.org/GgServer/proxy");
//		AuthenticatedClient auth = AuthenticatedClient.getAuthenticatedClient(sCon);
//		auth.login("admin", ""); // TODO enter password for testing
//		GoldenGateDssClient dssc = new GoldenGateDssClient(auth);
//		File dsFolder = new File("E:/GoldenGATEv3/Plugins/DocumentStyleProviderData");
//		String dsName = "ijsem.0000.journal_article";
//		File dsFile = new File(dsFolder, (dsName + ".docStyle"));
//		BufferedReader dsBr = new BufferedReader(new InputStreamReader(new FileInputStream(dsFile), "UTF-8"));
//		Settings dsSet = Settings.loadSettings(dsBr);
//		dsBr.close();
//		Settings dsAttributes = dsSet.getSubset("@");
//		DocumentStyle ds = new SettingsDocumentStyle(dsSet);
//		ds.setAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE, dsAttributes.getSetting(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE));
//		ds.setAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, dsAttributes.getSetting(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE));
//		if (dsAttributes.containsKey(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE))
//			ds.setAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, dsAttributes.getSetting(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE));
//		else ds.setAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, ("" + System.currentTimeMillis()));
//		dsSet.removeSubset(dsAttributes);
//		DocumentListElement dle = dssc.updateDocumentStyle(ds, dsName);
//		auth.logout();
//		String[] dleAns = dle.getAttributeNames();
//		System.out.println(dle.toTabString(dleAns));
//	}
}