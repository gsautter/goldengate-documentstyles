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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import de.uka.ipd.idaho.easyIO.EasyIO;
import de.uka.ipd.idaho.easyIO.IoProvider;
import de.uka.ipd.idaho.easyIO.SqlQueryResult;
import de.uka.ipd.idaho.easyIO.sql.TableDefinition;
import de.uka.ipd.idaho.gamta.util.DocumentStyle;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentListElement;
import de.uka.ipd.idaho.goldenGateServer.AbstractGoldenGateServerComponent;
import de.uka.ipd.idaho.goldenGateServer.GoldenGateServerComponentRegistry;
import de.uka.ipd.idaho.goldenGateServer.dss.data.DssDocumentStyleList;
import de.uka.ipd.idaho.goldenGateServer.uaa.UserAccessAuthority;
import de.uka.ipd.idaho.goldenGateServer.util.IdentifierKeyedDataObjectStore;
import de.uka.ipd.idaho.goldenGateServer.util.IdentifierKeyedDataObjectStore.DataObjectNotFoundException;
import de.uka.ipd.idaho.goldenGateServer.util.IdentifierKeyedDataObjectStore.DataObjectOutputStream;
import de.uka.ipd.idaho.stringUtils.StringVector;


/**
 * Server component for storing and retrieving document style parameter lists
 * by ID over the network.
 * 
 * @author sautter
 */
public class GoldenGateDSS extends AbstractGoldenGateServerComponent implements GoldenGateDssConstants {
	private static final String DOCUMENT_STYLE_TABLE_NAME = "GgDssDocumentStyles";
	
	private static final int DOCUMENT_NAME_COLUMN_LENGTH = 255;
	
	private IoProvider io;
	private IdentifierKeyedDataObjectStore iks;
	private long lastModified = -1;
	
	private UserAccessAuthority uaa = null;
	
	/** public zero-argument constructor for class loading */
	public GoldenGateDSS() {
		super("DSS");
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGateServer.AbstractGoldenGateServerComponent#initComponent()
	 */
	protected void initComponent() {
		
		//	get document storage folder
		String docStyleFolderName = this.configuration.getSetting("styleFolderName", "DocumentStyles");
		while (docStyleFolderName.startsWith("./"))
			docStyleFolderName = docStyleFolderName.substring("./".length());
		File docStyleFolder = (((docStyleFolderName.indexOf(":\\") == -1) && (docStyleFolderName.indexOf(":/") == -1) && !docStyleFolderName.startsWith("/")) ? new File(this.dataPath, docStyleFolderName) : new File(docStyleFolderName));
		
		// initialize document store
		this.iks = new IdentifierKeyedDataObjectStore("DssDocumentStyles", docStyleFolder, ".docStyle", this);
		
		// get and check database connection
		this.io = this.host.getIoProvider();
		if (!this.io.isJdbcAvailable())
			throw new RuntimeException("GoldenGateDSS: Cannot work without database access.");
		
		//	create document style management table
		TableDefinition td = new TableDefinition(DOCUMENT_STYLE_TABLE_NAME);
		td.addColumn(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE, TableDefinition.VARCHAR_DATATYPE, 32);
		td.addColumn(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, TableDefinition.VARCHAR_DATATYPE, DOCUMENT_NAME_COLUMN_LENGTH);
		td.addColumn(CHECKIN_USER_ATTRIBUTE, TableDefinition.VARCHAR_DATATYPE, UserAccessAuthority.USER_NAME_MAX_LENGTH);
		td.addColumn(CHECKIN_TIME_ATTRIBUTE, TableDefinition.BIGINT_DATATYPE, 0);
		td.addColumn(UPDATE_USER_ATTRIBUTE, TableDefinition.VARCHAR_DATATYPE, UserAccessAuthority.USER_NAME_MAX_LENGTH);
		td.addColumn(UPDATE_TIME_ATTRIBUTE, TableDefinition.BIGINT_DATATYPE, 0);
		td.addColumn(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE, TableDefinition.INT_DATATYPE, 0);
		td.addColumn(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, TableDefinition.BIGINT_DATATYPE, 0);
		if (!this.io.ensureTable(td, true))
			throw new RuntimeException("GoldenGateDSS: Cannot work without database access.");
		
		//	index table
		this.io.indexColumn(DOCUMENT_STYLE_TABLE_NAME, DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE);
		this.io.indexColumn(DOCUMENT_STYLE_TABLE_NAME, CHECKIN_USER_ATTRIBUTE);
		this.io.indexColumn(DOCUMENT_STYLE_TABLE_NAME, UPDATE_USER_ATTRIBUTE);
		
		/*
Register DSS as document style provider in server proper:
- helps getting document specific data in exporters in generic way
- could be useful for custom licensing, etc.
- only match on ID and name, not on anchors
- might help specify decoding options (fonts !!!) to IMI PDF importer (and from there to slave)
  ==> maybe do use attribute anchors
    ==> also allow handing document attributes to PdfExtractor.decode() methods to facilitate using such early-on anchors ...
    ==> OR BETTER allow specifying document style proper
      ==> facilitates hitting IMI PDF decoder with document style selected by attributes of import
    ==> OR YET BETTER add style right in produceDocument() method
      ==> will require looping through attributes to latter method
    ==> BEST TODO hand over attribute doped ImDocument instance that would be created anyway in context at hand ...
    ==> ... and set document style before handing back
		 */
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGateServer.AbstractGoldenGateServerComponent#link()
	 */
	public void link() {

		// get access authority
		this.uaa = ((UserAccessAuthority) GoldenGateServerComponentRegistry.getServerComponent(UserAccessAuthority.class.getName()));

		// check success
		if (this.uaa == null) throw new RuntimeException(UserAccessAuthority.class.getName());
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGateServer.AbstractGoldenGateServerComponent#linkInit()
	 */
	public void linkInit() {
		
		//	register permissions
		this.uaa.registerPermission(UPDATE_DOCUMENT_STYLE_PERMISSION);
		this.uaa.registerPermission(DELETE_DOCUMENT_STYLE_PERMISSION);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGateServer.AbstractGoldenGateServerComponent#exitComponent()
	 */
	protected void exitComponent() {
		
		//	disconnect from database
		this.io.close();
		
		//	shut down document style store
		this.iks.shutdown();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGateServer.AbstractGoldenGateServerComponent#getActions()
	 */
	public ComponentAction[] getActions() {
		ArrayList cal = new ArrayList();
		ComponentAction ca;
		
		// get last modification
		ca = new ComponentActionNetwork() {
			public String getActionCommand() {
				return GET_LAST_MODIFIED;
			}
			public void performActionNetwork(BufferedReader input, BufferedWriter output) throws IOException {
				output.write(GET_LAST_MODIFIED);
				output.newLine();
				output.write("" + getLastModified());
				output.newLine();
			}
		};
		cal.add(ca);
		
		// list document styles
		ca = new ComponentActionNetwork() {
			public String getActionCommand() {
				return LIST_STYLES;
			}
			public void performActionNetwork(BufferedReader input, BufferedWriter output) throws IOException {
				DssDocumentStyleList docStyleList = getDocumentStyleList();
				output.write(LIST_STYLES);
				output.newLine();
				docStyleList.writeData(output);
				output.newLine();
			}
		};
		cal.add(ca);
		
		// get document style
		ca = new ComponentActionNetwork() {
			public String getActionCommand() {
				return GET_STYLE;
			}
			public void performActionNetwork(BufferedReader input, BufferedWriter output) throws IOException {
				String docStyleId = input.readLine();
				int version = 0;
				int idVersionSplit = docStyleId.indexOf('.');
				if (idVersionSplit != -1) {
					try {
						version = Integer.parseInt(docStyleId.substring(idVersionSplit + 1));
					} catch (NumberFormatException nfe) {}
					docStyleId = docStyleId.substring(0, idVersionSplit);
				}
				DocumentStyle docList = getDocumentStyle(docStyleId, version);
				output.write(GET_STYLE);
				output.newLine();
				docList.writeData(output);
				output.newLine();
			}
		};
		cal.add(ca);
		
		// update a document style, or store a new one
		ca = new ComponentActionNetwork() {
			public String getActionCommand() {
				return UPDATE_STYLE;
			}
			public void performActionNetwork(final BufferedReader input, BufferedWriter output) throws IOException {

				// check authentication
				String sessionId = input.readLine();
				if (!uaa.isValidSession(sessionId)) {
					output.write("Invalid session (" + sessionId + ")");
					output.newLine();
					logError("Request for invalid session - " + sessionId);
					return;
				}
				
				//	check permission
				if (!uaa.hasSessionPermission(sessionId, UPDATE_DOCUMENT_STYLE_PERMISSION, true)) {
					output.write("Insufficient permissions for updating a document style");
					output.newLine();
					return;
				}
				
				//	store or update document style
				try {
					String docStyleName = input.readLine();
					logInfo(" - name is " + docStyleName);
					
					DocumentStyle docStyle = DocumentStyle.readDocumentStyle(input, true);
					
					String docStyleId = ((String) docStyle.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE));
					if (docStyleId == null) {
						output.write("Invalid document style ID");
						output.newLine();
						return;
					}
					
					try {
						DocumentStyle localDocStyle = getDocumentStyle(docStyleId);
						docStyle.setAttribute(CHECKIN_USER_ATTRIBUTE, localDocStyle.getAttribute(CHECKIN_USER_ATTRIBUTE));
						docStyle.setAttribute(CHECKIN_TIME_ATTRIBUTE, localDocStyle.getAttribute(CHECKIN_TIME_ATTRIBUTE));
					} catch (DataObjectNotFoundException donfe) {}
					
					String user = uaa.getUserNameForSession(sessionId);
					logInfo(" - user is " + user);
					
					docStyle.setAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, docStyleName);
					
					int version = updateDocumentStyle(user, docStyleId, docStyle);
					
					output.write(UPDATE_STYLE);
					output.newLine();
					
					output.write(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE + "\t" + docStyleId);
					output.newLine();
					output.write(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE + "\t" + docStyleName);
					output.newLine();
					output.write(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE + "\t" + version);
					output.newLine();
					output.write(CHECKIN_USER_ATTRIBUTE + "\t" + docStyle.getAttribute(CHECKIN_USER_ATTRIBUTE));
					output.newLine();
					output.write(CHECKIN_TIME_ATTRIBUTE + "\t" + docStyle.getAttribute(CHECKIN_TIME_ATTRIBUTE));
					output.newLine();
					output.write(UPDATE_USER_ATTRIBUTE + "\t" + docStyle.getAttribute(UPDATE_USER_ATTRIBUTE));
					output.newLine();
					output.write(UPDATE_TIME_ATTRIBUTE + "\t" + docStyle.getAttribute(UPDATE_TIME_ATTRIBUTE));
					output.newLine();
					
					output.newLine();
				}
				catch (IOException ioe) {
					output.write(ioe.getMessage());
					output.newLine();
				}
			}
		};
		cal.add(ca);
		
		// delete a document style
		ca = new ComponentActionNetwork() {
			public String getActionCommand() {
				return DELETE_STYLE;
			}
			public void performActionNetwork(BufferedReader input, BufferedWriter output) throws IOException {

				// check authentication
				String sessionId = input.readLine();
				if (!uaa.isValidSession(sessionId)) {
					output.write("Invalid session (" + sessionId + ")");
					output.newLine();
					logError("Request for invalid session - " + sessionId);
					return;
				}
				
				//	check permission
				if (!uaa.hasSessionPermission(sessionId, DELETE_DOCUMENT_STYLE_PERMISSION, true)) {
					output.write("Insufficient permissions for deleting a document style");
					output.newLine();
					return;
				}
				
				try {
					String docStyleId = input.readLine();
					deleteDocumentStyle(docStyleId);
					output.write(DELETE_STYLE);
					output.newLine();
				}
				catch (IOException ioe) {
					output.write(ioe.getMessage());
					output.newLine();
				}
			}
		};
		cal.add(ca);
		
		// get actions from document style store
		ComponentAction[] iksActions = this.iks.getActions();
		for (int a = 0; a < iksActions.length; a++)
			cal.add(iksActions[a]);
		
		//	finally ...
		return ((ComponentAction[]) cal.toArray(new ComponentAction[cal.size()]));
	}
	
	/**
	 * Obtain the timestamp of the last modification to the document style
	 * templates hosted in DSS.
	 * @return the timestamp of the last modification
	 */
	public long getLastModified() {
		if (this.lastModified != -1)
			return lastModified;
		
		//	load update time from database
		String query = "SELECT max(" + UPDATE_TIME_ATTRIBUTE + ") AS " + UPDATE_TIME_ATTRIBUTE +
				" FROM " + DOCUMENT_STYLE_TABLE_NAME +
				";";
		SqlQueryResult sqr = null;
		try {
			sqr = this.io.executeSelectQuery(query.toString());
			if (sqr.next())
				this.lastModified = Math.max(this.lastModified, sqr.getLong(0));
			else this.lastModified = Math.max(this.lastModified, 0);
		}
		catch (SQLException sqle) {
			this.logError("GoldenGateDSS: " + sqle.getClass().getName() + " (" + sqle.getMessage() + ") while getting timestamp of last modification.");
			this.logError("  query was " + query);
		}
		finally {
			if (sqr != null)
				sqr.close();
		}
		
		//	finally ...
		return this.lastModified;
	}
	
	private static final String[] documentListFields = {
		DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE,
		DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE,
		CHECKIN_USER_ATTRIBUTE,
		CHECKIN_TIME_ATTRIBUTE,
		UPDATE_USER_ATTRIBUTE,
		UPDATE_TIME_ATTRIBUTE,
		DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE,
		DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE,
	};
	
	/**
	 * Retrieve a list of meta data for the document styles available through this
	 * DSS. The list includes the document style ID, name, checkin user, checkin
	 * time, last update user, last update time, and most recent version.
	 * @return a meta data list of the document styles available through this DSS
	 */
	public DssDocumentStyleList getDocumentStyleList() {
		
		// collect field names
		StringVector fieldNames = new StringVector();
		fieldNames.addContent(documentListFields);
		
		// assemble query
		String query = "SELECT " + fieldNames.concatStrings(", ") + 
				" FROM " + DOCUMENT_STYLE_TABLE_NAME +
				" ORDER BY " + DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE +
				";";
		
		SqlQueryResult sqr = null;
		try {
			sqr = this.io.executeSelectQuery(query.toString());
			
			// return SQL backed list
			final SqlQueryResult finalSqr = sqr;
			return new DssDocumentStyleList(fieldNames.toStringArray()) {
				SqlQueryResult sqr = finalSqr;
				DocumentListElement next = null;
				public boolean hasNextDocument() {
					if (this.next != null) return true;
					else if (this.sqr == null) return false;
					else if (this.sqr.next()) {
						this.next = new DocumentListElement();
						for (int f = 0; f < this.listFieldNames.length; f++)
							this.next.setAttribute(this.listFieldNames[f], this.sqr.getString(f));
						this.addListFieldValues(this.next);
						return true;
					}
					else {
						this.sqr.close();
						this.sqr = null;
						return false;
					}
				}
				public DocumentListElement getNextDocument() {
					if (!this.hasNextDocument()) return null;
					DocumentListElement next = this.next;
					this.next = null;
					return next;
				}
//				public int getDocumentCount() {
//					return docIdSet.size();
//				}
			};
		}
		catch (SQLException sqle) {
			this.logError("GoldenGateDSS: " + sqle.getClass().getName() + " (" + sqle.getMessage() + ") while listing document styles.");
			this.logError("  query was " + query);
			
			// return dummy list
			return new DssDocumentStyleList(fieldNames.toStringArray()) {
				public boolean hasNextDocument() {
					return false;
				}
				public DocumentListElement getNextDocument() {
					return null;
				}
				public int getDocumentCount() {
					return 0;
				}
			};
		}
	}
	
	/**
	 * Load a document style from storage (the most recent version).
	 * @param docStyleId the ID of the document style to load
	 * @return the document style with the specified ID
	 * @throws IOException
	 */
	public DocumentStyle getDocumentStyle(String docStyleId) throws IOException {
		return this.getDocumentStyle(docStyleId, 0);
	}
	
	/**
	 * Load a specific version of a document style from storage. A positive
	 * version number indicates an actual version specifically, while a
	 * negative version number indicates a version backward relative to the
	 * most recent version. Version number 0 always returns the most recent
	 * version.
	 * @param docStyleId the ID of the document style to load
	 * @param version the version to load
	 * @return the document style with the specified ID
	 * @throws IOException
	 */
	public DocumentStyle getDocumentStyle(String docStyleId, int version) throws IOException {
		BufferedReader dsbr = null;
		try {
			dsbr = new BufferedReader(new InputStreamReader(this.iks.getInputStream(docStyleId, version), "UTF-8"));
			return DocumentStyle.readDocumentStyle(dsbr);
		} 
		finally {
			if (dsbr != null)
				dsbr.close();
		}
	}
	
	/**
	 * Update an existing document style, or store a new one, using its
	 * 'docStyleId' attribute as the storage ID (if the docStyleId attribute is
	 * not set, a new one is minted).
	 * @param userName the name of the user doing the update
	 * @param docStyleId the ID of the document style
	 * @param docStyle the document style to store
	 * @return the new version number of the document style just updated
	 * @throws IOException
	 */
	public synchronized int updateDocumentStyle(String userName, String docStyleId, DocumentStyle docStyle) throws IOException {
		
		/* TODO Issue update and deletion events from DSS
		 * ==> facilitates RES based replication
		 * ==> implement DSR to pick up replication
		 */
		
		// get timestamp
		long time = System.currentTimeMillis();
		String timeString = ("" + time);
		
		// update meta data
		docStyle.setAttribute(UPDATE_USER_ATTRIBUTE, userName);
		if (!docStyle.hasAttribute(CHECKIN_USER_ATTRIBUTE))
			docStyle.setAttribute(CHECKIN_USER_ATTRIBUTE, userName);

		docStyle.setAttribute(UPDATE_TIME_ATTRIBUTE, timeString);
		if (!docStyle.hasAttribute(CHECKIN_TIME_ATTRIBUTE))
			docStyle.setAttribute(CHECKIN_TIME_ATTRIBUTE, timeString);
		
		// store document in IKS
		DataObjectOutputStream dsOut = this.iks.getOutputStream(docStyleId);
		BufferedWriter dsbw = new BufferedWriter(new OutputStreamWriter(dsOut, "UTF-8"));
		docStyle.writeData(dsbw);
		dsbw.flush();
		dsbw.close();
		final int version = dsOut.getVersion();
		
		StringVector assignments = new StringVector();
		
		// check and (if necessary) truncate name
		String name = ((String) docStyle.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE, docStyleId));
		if (name.length() > DOCUMENT_NAME_COLUMN_LENGTH)
			name = name.substring(0, DOCUMENT_NAME_COLUMN_LENGTH);
		assignments.addElement(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE + " = '" + EasyIO.sqlEscape(name) + "'");
		
		// get update user
		String updateUser = ((String) docStyle.getAttribute(UPDATE_USER_ATTRIBUTE, userName));
		if (updateUser.length() > UserAccessAuthority.USER_NAME_MAX_LENGTH)
			updateUser = updateUser.substring(0, UserAccessAuthority.USER_NAME_MAX_LENGTH);
		assignments.addElement(UPDATE_USER_ATTRIBUTE + " = '" + EasyIO.sqlEscape(updateUser) + "'");
		
		// set update time
		assignments.addElement(UPDATE_TIME_ATTRIBUTE + " = " + time);
		
		// update version number
		assignments.addElement(DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE + " = " + version);
		
		// update last modification timestamp
		long lastMod = Long.parseLong((String) docStyle.getAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, "-1"));
		assignments.addElement(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE + " = " + lastMod);
		
		// write new values
		String updateQuery = ("UPDATE " + DOCUMENT_STYLE_TABLE_NAME + 
				" SET " + assignments.concatStrings(", ") + 
				" WHERE " + DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE + " = '" + EasyIO.sqlEscape(docStyleId) + "'" +
				";");

		try {
			
			//	update did not affect any rows ==> new document style
			if (this.io.executeUpdateQuery(updateQuery) == 0) {
				
				//	gather complete data for creating master table record
				StringBuffer fields = new StringBuffer(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE);
				StringBuffer fieldValues = new StringBuffer("'" + EasyIO.sqlEscape(docStyleId) + "'");
				
				//	set name
				fields.append(", " + DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE);
				fieldValues.append(", '" + EasyIO.sqlEscape(name) + "'");
				
				//	set checkin user
				fields.append(", " + CHECKIN_USER_ATTRIBUTE);
				fieldValues.append(", '" + EasyIO.sqlEscape(updateUser) + "'");
				
				//	set checkin time
				fields.append(", " + CHECKIN_TIME_ATTRIBUTE);
				fieldValues.append(", " + time);
				
				//	set update user
				fields.append(", " + UPDATE_USER_ATTRIBUTE);
				fieldValues.append(", '" + EasyIO.sqlEscape(updateUser) + "'");
				
				//	set update time
				fields.append(", " + UPDATE_TIME_ATTRIBUTE);
				fieldValues.append(", " + time);
				
				//	set version number
				fields.append(", " + DocumentStyle.DOCUMENT_STYLE_VERSION_ATTRIBUTE);
				fieldValues.append(", " + version);
				
				//	set last modification time
				fields.append(", " + DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE);
				fieldValues.append(", " + lastMod);
				
				// store data in collection main table
				String insertQuery = "INSERT INTO " + DOCUMENT_STYLE_TABLE_NAME + 
						" (" + fields.toString() + ")" +
						" VALUES" +
						" (" + fieldValues.toString() + ")" +
						";";
				try {
					this.io.executeUpdateQuery(insertQuery);
				}
				catch (SQLException sqle) {
					this.logError("GoldenGateDSS: " + sqle.getClass().getName() + " (" + sqle.getMessage() + ") while storing new document style.");
					this.logError("  query was " + insertQuery);
					throw new IOException(sqle.getMessage());
				}
			}
		}
		catch (SQLException sqle) {
			this.logError("GoldenGateDSS: " + sqle.getClass().getName() + " (" + sqle.getMessage() + ") while updating existing document style.");
			this.logError("  query was " + updateQuery);
			throw new IOException(sqle.getMessage());
		}
		
		//	update modification timestamp
		this.lastModified = Math.max(this.lastModified, time);
		
		// report new version
		return version;
	}
	
	/**
	 * Delete a document style from storage.
	 * @param docStyleId the ID of the document style to delete
	 * @throws IOException
	 */
	public void deleteDocumentStyle(String docStyleId) throws IOException {
		
		/* TODO Issue update and deletion events from DSS
		 * ==> facilitates RES based replication
		 * ==> implement DSR to pick up replication
		 */
		
		// delete document from IKS
		this.iks.deleteDataObject(docStyleId);

		// delete meta data
		String deleteQuery = "DELETE FROM " + DOCUMENT_STYLE_TABLE_NAME + 
				" WHERE " + DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE + " LIKE '" + EasyIO.sqlEscape(docStyleId) + "'" +
				";";
		try {
			this.io.executeUpdateQuery(deleteQuery);
		}
		catch (SQLException sqle) {
			this.logError("GoldenGateDSS: " + sqle.getClass().getName() + " (" + sqle.getMessage() + ") while deleting document style.");
			this.logError("  query was " + deleteQuery);
			throw new IOException(sqle.getMessage());
		}
		
		//	update modification timestamp
		this.lastModified = Math.max(this.lastModified, System.currentTimeMillis());
	}
}
