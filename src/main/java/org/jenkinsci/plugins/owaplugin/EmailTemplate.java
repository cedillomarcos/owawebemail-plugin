package org.jenkinsci.plugins.owaplugin;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * 
 * 
 *
 */
public class EmailTemplate implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	private String name;
	
	private String to;
	private String cc;
	private String bcc;
	private String subject;
	private String owaEmailTemplate;
	
	
	@DataBoundConstructor
	public EmailTemplate(String name, String to, String cc, String bcc, String subject, String owaEmailTemplate) {
		super();
		this.name = name;
		this.to = to;
		this.cc = cc;
		this.bcc = bcc;
		this.subject = subject;
		this.owaEmailTemplate = owaEmailTemplate;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getCc() {
		return cc;
	}
	public void setCc(String cc) {
		this.cc = cc;
	}
	public String getBcc() {
		return bcc;
	}
	public void setBcc(String bcc) {
		this.bcc = bcc;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getOwaEmailTemplate() {
		return owaEmailTemplate;
	}
	public void setOwaEmailTemplate(String owaEmailTemplate) {
		this.owaEmailTemplate = owaEmailTemplate;
	}

	
}
