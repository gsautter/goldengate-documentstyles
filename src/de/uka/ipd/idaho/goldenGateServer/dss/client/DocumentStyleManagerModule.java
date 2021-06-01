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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.easyIO.web.WebAppHost;
import de.uka.ipd.idaho.gamta.util.DocumentStyle;
import de.uka.ipd.idaho.gamta.util.transfer.DocumentListElement;
import de.uka.ipd.idaho.goldenGateServer.dss.GoldenGateDssConstants;
import de.uka.ipd.idaho.goldenGateServer.dss.data.DssDocumentStyleList;
import de.uka.ipd.idaho.goldenGateServer.uaa.client.AuthenticatedClient;
import de.uka.ipd.idaho.goldenGateServer.uaa.webClient.AuthenticatedWebClientModul;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder;
import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * Module for managing document styles.
 * 
 * @author sautter
 */
public class DocumentStyleManagerModule extends AuthenticatedWebClientModul implements GoldenGateDssConstants {
	private WebAppHost webAppHost;
	private String dssServletName;
	
	private Map dssClientCache = Collections.synchronizedMap(new HashMap());
	private GoldenGateDssClient getDssClient(AuthenticatedClient authClient) {
		GoldenGateDssClient dssc = ((GoldenGateDssClient) this.dssClientCache.get(authClient.getSessionID()));
		if (dssc == null) {
			dssc = new GoldenGateDssClient(authClient);
			this.dssClientCache.put(authClient.getSessionID(), dssc);
		}
		return dssc;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGateServer.uaa.webClient.AuthenticatedWebClientModul#init()
	 */
	protected void init() {
		Settings config = Settings.loadSettings(new File(this.dataPath, "config.cnfg"));
		this.dssServletName = config.getSetting("dssServletName");
		if (this.dssServletName != null)
			this.webAppHost = WebAppHost.getInstance(this.parent.getServletContext());
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGateScf.uaa.webClient.AuthenticatedWebClientModul#getModulLabel()
	 */
	public String getModulLabel() {
		return "Document Styles";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGateServer.uaa.webClient.AuthenticatedWebClientModul#displayFor(de.uka.ipd.idaho.goldenGateServer.uaa.client.AuthenticatedClient)
	 */
	public boolean displayFor(AuthenticatedClient authClient) {
		return authClient.isAdmin(); // managing document styles is admin business
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGateScf.uaa.webClient.AuthenticatedWebClientModul#handleRequest(de.uka.ipd.idaho.goldenGateScf.uaa.client.AuthenticatedClient, javax.servlet.http.HttpServletRequest)
	 */
	public String[] handleRequest(AuthenticatedClient authClient, HttpServletRequest request) throws IOException {
		GoldenGateDssClient dssc = this.getDssClient(authClient);
		StringVector messageCollector = new StringVector();
		
		String command = request.getParameter(COMMAND_PARAMETER);
		
		//	delete document style
		if (DELETE_STYLE.equals(command)) {
			
			//	get parameters
			String styleId = request.getParameter(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE);
			String styleName = request.getParameter(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE);
			
			//	delete group
			dssc.deleteDocumentStyle(styleId);
			messageCollector.addElement("Document style '" + styleName + "' deleted successfully.");
			
			//	refresh any present DSS servlet
			if (this.webAppHost != null) try {
				this.webAppHost.reInitialize(this.dssServletName);
				messageCollector.addElement("Document style servlet cache cleared.");
			}
			catch (ServletException se) {
				messageCollector.addElement("Failed to clear document style servlet cache: " + se.getMessage());
			}
		}
		
		return messageCollector.toStringArray();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGateServer.uaa.webClient.AuthenticatedWebClientModul#writePageContent(de.uka.ipd.idaho.goldenGateServer.uaa.client.AuthenticatedClient, de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder)
	 */
	public void writePageContent(AuthenticatedClient authClient, HtmlPageBuilder pageBuilder) throws IOException {
		GoldenGateDssClient dssc = this.getDssClient(authClient);
		
		DssDocumentStyleList dsl = dssc.getDocumentStyleList();
		
		//	build label row
		pageBuilder.writeLine("<table class=\"mainTable\">");
		pageBuilder.writeLine("<tr>");
		pageBuilder.writeLine("<td width=\"100%\" class=\"mainTableHeader\">");
		pageBuilder.writeLine("Manage document style parameter lists");
		pageBuilder.writeLine("</td>");
		pageBuilder.writeLine("</tr>");
		
		//	open document style table
		pageBuilder.writeLine("<tr>");
		pageBuilder.writeLine("<td width=\"100%\" class=\"mainTableBody\">");
		pageBuilder.writeLine("<table width=\"100%\" class=\"dataTable\">");
		
		//	add actual document style data
		while (dsl.hasNextDocument()) {
			DocumentListElement dle  = dsl.getNextDocument();
			String docStyleId = ((String) dle.getAttribute(DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE));
			String docStyleName = ((String) dle.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE));
			
			//	open table row
			pageBuilder.writeLine("<tr>");
			
			pageBuilder.writeLine("<td class=\"dataTableBody\">");
			pageBuilder.writeLine("<form method=\"POST\" action=\"" + pageBuilder.request.getContextPath() + pageBuilder.request.getServletPath() + "/" + this.getClass().getName() + "\">");
			pageBuilder.writeLine("<input type=\"hidden\" name=\"" + COMMAND_PARAMETER + "\" value=\"" + DELETE_STYLE + "\">");
			pageBuilder.writeLine("<input type=\"hidden\" name=\"" + DocumentStyle.DOCUMENT_STYLE_ID_ATTRIBUTE + "\" value=\"" + docStyleId + "\">");
			pageBuilder.writeLine("<input type=\"hidden\" name=\"" + DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE + "\" value=\"" + docStyleName + "\">");
			pageBuilder.writeLine(docStyleName);
			pageBuilder.writeLine("&nbsp;");
			pageBuilder.writeLine("<input type=\"submit\" value=\"Delete Document Style\" class=\"submitButton\">");
			pageBuilder.writeLine("</form>");
			pageBuilder.writeLine("</td>");
			
			pageBuilder.writeLine("</tr>");
		}
		
		//	close document style table
		pageBuilder.writeLine("</table>");
		pageBuilder.writeLine("</td>");
		pageBuilder.writeLine("</tr>");
		
		//	close master table
		pageBuilder.writeLine("</table>");
	}
}