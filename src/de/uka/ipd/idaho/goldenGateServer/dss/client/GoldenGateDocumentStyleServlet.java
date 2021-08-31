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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.util.DocumentStyle;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentListBuffer;
import de.uka.ipd.idaho.goldenGateServer.client.GgServerClientServlet;
import de.uka.ipd.idaho.goldenGateServer.client.ServerConnection.Connection;
import de.uka.ipd.idaho.goldenGateServer.dss.GoldenGateDssConstants;
import de.uka.ipd.idaho.goldenGateServer.dss.data.DssDocumentStyleList;
import de.uka.ipd.idaho.stringUtils.csvHandler.StringTupel;

/**
 * This servlet is intended to serve document style parameter lists from a
 * backing GoldenGATE DSS.
 * 
 * @author sautter
 */
public class GoldenGateDocumentStyleServlet extends GgServerClientServlet implements GoldenGateDssConstants {
	private File docStyleDataRoot;
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGateServer.client.GgServerClientServlet#doInit()
	 */
	protected void doInit() throws ServletException {
		super.doInit();
		
		//	get configuration data location
		String configDataLocation = this.getSetting("docStyleDataLocation");
		if (configDataLocation == null)
//			this.docStyleDataRoot = new File(new File(this.webInfFolder, "caches"), "DocumentStyles");
			this.docStyleDataRoot = new File(this.cacheRootFolder, "DocumentStyles");
		else this.docStyleDataRoot = new File(configDataLocation);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.easyIO.web.HtmlServlet#reInit()
	 */
	protected void reInit() throws ServletException {
		super.reInit();
		this.docStyleList = null;
		this.docStyleDataCache.clear();
		this.docStyleCache.clear();
	}
	
	private long lastModifiedChecked = -1;
	private synchronized void checkLastModified() throws IOException {
		long time = System.currentTimeMillis();
		if (time < (this.lastModifiedChecked + (1000 * 60 * 10) /* 10 minutes */))
			return; // we've already check in past 10 minutes
		
		//	get last modification timestamp from server
		long lastModified = this.getLastModified();
		this.lastModifiedChecked = time;
		
		//	clear outdated document list to force reload (styles proper are checked against timestamps from list)
		if (this.docStylesLastModified < lastModified) {
			this.docStyleList = null;
			this.docStyleDataCache.clear();
		}
	}
	
	//	need to have our own implementation of server interaction to work without DSS client (which requires AuthenticatedClient)
	private long getLastModified() throws IOException {
		Connection con = null;
		try {
			con = this.serverConnection.getConnection();
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
	
	private DocumentListBuffer docStyleList = null;
	private Map docStyleDataCache = Collections.synchronizedMap(new TreeMap(String.CASE_INSENSITIVE_ORDER));
	private long docStylesLastModified = -1;
	private synchronized DocumentListBuffer getDocStyleList(boolean refresh) throws IOException {
		if (refresh || (this.docStyleList == null)) {
			DssDocumentStyleList dsl = this.loadDocumentStyleList();
			DocumentListBuffer dslBuf = new DocumentListBuffer(dsl);
			this.docStyleDataCache.clear();
			for (int s = 0; s < dslBuf.size(); s++) {
				StringTupel docStyleData = dslBuf.get(s);
				String docStyleId = docStyleData.getValue(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE);
				if (docStyleId == null)
					continue;
				this.docStyleDataCache.put(docStyleId, docStyleData);
				String docStyleName = docStyleData.getValue(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE);
				if (docStyleName != null)
					this.docStyleDataCache.put(docStyleName, docStyleData);
				try {
					long docStyleLastMod = Long.parseLong(docStyleData.getValue(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, "-1"));
					if (docStyleLastMod != -1)
						this.docStylesLastModified = Math.max(this.docStylesLastModified, docStyleLastMod);
				} catch (NumberFormatException nfe) { /* can happen if list is empty */ }
			}
			this.docStyleList = dslBuf;
		}
		return this.docStyleList;
	}
	
	//	need to have our own implementation of server interaction to work without DSS client (which requires AuthenticatedClient)
	private DssDocumentStyleList loadDocumentStyleList() throws IOException {
		final Connection con = this.serverConnection.getConnection();
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
			});
		else {
			con.close();
			throw new IOException(error);
		}
	}
	
	private Map docStyleCache = Collections.synchronizedMap(new HashMap());
	private synchronized DocumentStyle getDocumentStyle(String idOrName, boolean refresh) throws IOException {
		if (this.docStyleList == null)
			this.getDocStyleList(false);
		StringTupel docStyleData = ((StringTupel) this.docStyleDataCache.get(idOrName));
		if (docStyleData == null) {
			this.getDocStyleList(true);
			docStyleData = ((StringTupel) this.docStyleDataCache.get(idOrName));
			if (docStyleData == null)
				return null;
		}
		String docStyleId = docStyleData.getValue(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE);
		if (docStyleId == null)
			return null;
		
		File docStyleCacheFile = new File(this.docStyleDataRoot, (docStyleId + ".docStyle"));
		if (refresh)
			return this.getDocumentStyle(docStyleId, docStyleCacheFile);
		
		long docStyleDataLastMod = Long.parseLong(docStyleData.getValue(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, "-1"));
		if (this.docStyleCache.containsKey(docStyleId)) {
			DocumentStyle docStyle = ((DocumentStyle) this.docStyleCache.get(docStyleId));
			long docStyleLastMod = Long.parseLong((String) docStyle.getAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, "-1"));
			if ((docStyleDataLastMod == -1) || (docStyleDataLastMod <= docStyleLastMod))
				return docStyle;
		}
		
		if (docStyleCacheFile.exists() && (docStyleDataLastMod != -1) && (docStyleDataLastMod <= (docStyleCacheFile.lastModified() + 999 /* cut some slack for file systems not storing milliseconds */))) {
			BufferedReader dsCacheBr = new BufferedReader(new InputStreamReader(new FileInputStream(docStyleCacheFile), "UTF-8"));
			DocumentStyle docStyle = DocumentStyle.readDocumentStyle(dsCacheBr);
			dsCacheBr.close();
			this.docStyleCache.put(docStyleId, docStyle);
			return docStyle;
		}
		
		return this.getDocumentStyle(docStyleId, docStyleCacheFile);
	}
	
	private DocumentStyle getDocumentStyle(String docStyleId, File docStyleCacheFile) throws IOException {
		DocumentStyle docStyle = this.loadDocumentStyle(docStyleId);
		this.docStyleCache.put(docStyleId, docStyle);
		BufferedWriter dsCacheBw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docStyleCacheFile), "UTF-8"));
		docStyle.writeData(dsCacheBw);
		dsCacheBw.flush();
		dsCacheBw.close();
		long docStyleLastMod = Long.parseLong((String) docStyle.getAttribute(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, "-1"));
		if (docStyleLastMod != -1)
			docStyleCacheFile.setLastModified(docStyleLastMod);
		return docStyle;
	}
	
	//	need to have our own implementation of server interaction to work without DSS client (which requires AuthenticatedClient)
	private DocumentStyle loadDocumentStyle(String docStyleId) throws IOException {
		Connection con = null;
		try {
			con = this.serverConnection.getConnection();
			BufferedWriter bw = con.getWriter();
			
			bw.write(GET_STYLE);
			bw.newLine();
			bw.write(docStyleId);
			bw.newLine();
			bw.flush();
			
			BufferedReader br = con.getReader();
			String error = br.readLine();
			if (GET_STYLE.equals(error))
				return DocumentStyle.readDocumentStyle(br, true);
			else throw new IOException(error);
		}
		finally {
			if (con != null)
				con.close();
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//	get invocation path and data name
		String servletPath = request.getServletPath();
		while (servletPath.startsWith("/"))
			servletPath = servletPath.substring("/".length());
		String dataName = request.getPathInfo();
		
		//	HTTP 404 forward
		if ("404.html".equals(servletPath)) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		//	check for updates on backing server
		this.checkLastModified();
		
		//	request for file list
		if ((dataName == null) || "/list".equals(dataName) || dataName.startsWith("/list.")) {
			DocumentListBuffer dsl = this.getDocStyleList("force".equals(request.getParameter("cacheControl")));
			response.setHeader("Cache-Control", "no-cache");
			if ("/list.txt".equals(dataName)) // TSV format
				this.sendFileListTsv(response, dsl);
			else if ("/list.xml".equals(dataName)) // XML format
				this.sendFileListXml(response, dsl);
			else this.sendFileListHtml(request, response, dsl); // send HTML list in all other cases
			return;
		}
		
		//	trim data name
		while (dataName.startsWith("/"))
			dataName = dataName.substring("/".length());
		
		//	try and find document style
		DocumentStyle ds = this.getDocumentStyle(dataName, "force".equals(request.getParameter("cacheControl")));
		if (ds == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		//	send document style
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("text/plain; charset=utf-8");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
		ds.writeData(bw);
		bw.flush();
	}
	
	/* TODO Offer HTML diff view
	 * ==> ... using "diff/<ID>/<inVersion>/<sinceVersion>" data name
	 *   - filter provenance related attributes from view
	 *   - order property names lexicographically ...
	 *   - ... and perform merge join ...
	 *   - ... highlighting differences
	 */
	
	private void sendFileListTsv(HttpServletResponse response, DocumentListBuffer dsl) throws IOException {
		response.setContentType("text/plain; charset=utf-8");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
		for (int f = 0; f < dsl.listFieldNames.length; f++) {
			if (f != 0)
				bw.write("\t");
			bw.write(dsl.listFieldNames[f]);
		}
		bw.newLine();
		for (int s = 0; s < dsl.size(); s++) {
			StringTupel st = dsl.get(s);
			if ((st.getValue(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE) == null) || "".equals(st.getValue(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE)))
				continue;
			for (int f = 0; f < dsl.listFieldNames.length; f++) {
				if (f != 0)
					bw.write("\t");
				bw.write(st.getValue(dsl.listFieldNames[f], ""));
			}
			bw.newLine();
		}
		bw.flush();
	}
	
	private void sendFileListXml(HttpServletResponse response, DocumentListBuffer dsl) throws IOException {
		response.setContentType("text/xml; charset=utf-8");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
		bw.write("<docStyles time=\"" + System.currentTimeMillis() + "\">"); bw.newLine();
		for (int s = 0; s < dsl.size(); s++) {
			StringTupel st = dsl.get(s);
			if ((st.getValue(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE) == null) || "".equals(st.getValue(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE)))
				continue;
			bw.write("\t<docStyle");
			for (int f = 0; f < dsl.listFieldNames.length; f++) {
				String lfv = st.getValue(dsl.listFieldNames[f]);
				if (lfv == null)
					continue;
				bw.write(" " + dsl.listFieldNames[f] + "=");
				bw.write("\"" +  AnnotationUtils.escapeForXml(lfv) + "\"");
			}
			bw.write("/>");
			bw.newLine();
		}
		bw.write("</docStyles>"); bw.newLine();
		bw.flush();
	}
	
	private void sendFileListHtml(HttpServletRequest request, HttpServletResponse response, DocumentListBuffer dsl) throws IOException {
		response.setContentType("text/html; charset=utf-8");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
		bw.write("<html><head>"); bw.newLine();
		bw.write("<title>Document Styles</title>"); bw.newLine();
		bw.write("</head><body>"); bw.newLine();
		bw.write("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"5\" align=\"center\">"); bw.newLine();
		
		bw.write("<tr>"); bw.newLine();
		bw.write("<td align=\"left\"><font size=\"+1\"><strong>Name</strong></font></td>"); bw.newLine();
		bw.write("<td align=\"center\"><font size=\"+1\"><strong>UUID</strong></font></td>"); bw.newLine();
		bw.write("<td align=\"right\"><font size=\"+1\"><strong>Last Modified</strong></font></td>"); bw.newLine();
		bw.write("</tr>"); bw.newLine();
		
		for (int s = 0; s < dsl.size(); s++) {
			StringTupel st = dsl.get(s);
			String dsId = st.getValue(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE);
			if ((dsId == null) || "".equals(dsId))
				continue;
			String dsName = st.getValue(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE);
			if ((dsName == null) || "".equals(dsName))
				continue;
			long dsLastMod = Long.parseLong(st.getValue(DocumentStyle.DOCUMENT_STYLE_LAST_MODIFIED_ATTRIBUTE, "-1"));
			bw.write("<tr" + (((s % 2) == 1) ? " bgcolor=\"#eeeeee\"" : "") + ">"); bw.newLine();
			bw.write("<td align=\"left\">&nbsp;&nbsp;"); bw.newLine();
			bw.write("<a href=\"" + request.getContextPath() + request.getServletPath() + "/" + dsName + "\"><tt>" + dsName + "</tt></a></td>"); bw.newLine();
			bw.write("<td align=\"center\">"); bw.newLine();
			bw.write("<a href=\"" + request.getContextPath() + request.getServletPath() + "/" + dsId + "\"><tt>" + dsId + "</tt></a></td>"); bw.newLine();
			bw.write("<td align=\"right\"><tt>" + ((dsLastMod == -1) ? "unknown" : lastModifiedFormat.format(new Date(dsLastMod))) + "</tt></td>"); bw.newLine();
			bw.write("</tr>"); bw.newLine();
		}
		
		bw.write("</table>"); bw.newLine();
		bw.write("</body></html>"); bw.newLine();
		bw.flush();
	}
	
	private static final SimpleDateFormat lastModifiedFormat = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss 'GMT'Z", Locale.US);
}