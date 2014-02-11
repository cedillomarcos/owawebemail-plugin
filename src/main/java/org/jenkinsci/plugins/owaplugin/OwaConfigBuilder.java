package org.jenkinsci.plugins.owaplugin;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link OwaConfigBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class OwaConfigBuilder extends Notifier {

	private static final Logger LOGGER = Logger.getLogger(OwaConfigBuilder.class.getName());
	
	private String nameId;
	private String to;
	private String cc;
	private String bcc;
	private String subject;
	private String owaEmailTemplate;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public OwaConfigBuilder(String nameId, String to, String cc, String bcc, String subject, String owaEmailTemplate) {
    	this.nameId = nameId;
		this.to = to;
		this.cc = cc;
		this.bcc = bcc;
		this.subject = subject;
		this.owaEmailTemplate = owaEmailTemplate;
    }
    

    public String getNameId() {
		return nameId;
	}

	public void setNameId(String nameId) {
		this.nameId = nameId;
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

	
	@Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
    		 													throws IOException, InterruptedException {
		
		String[] ccsplit = null;
		StringBuffer buffercontext = null;
		String tokenSubject = null;
		try {
			String ccText = TokenMacro.expand( build, listener, cc );
			ccsplit = ccText.split(",");
			
			Map<String, String> env = build.getEnvironment(listener);
			
			tokenSubject = TokenMacro.expand( build, listener, subject );
			buffercontext = new StringBuffer(TokenMacro.expand( build, listener, owaEmailTemplate));
			
				//	String text = TokenMacro.expand( build, listener, "My revision at #${BUILD_NUMBER}" );
			
		} catch (MacroEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		OwaWebConnector owaweb = new OwaWebConnector(descriptor().owaUrl,
													 descriptor().owaUsername,
													 descriptor().owaPassword,
													 descriptor().owaBoxAccount);
		owaweb.initLoginOWA();		
		owaweb.sendEmail(to, ccsplit, new String[]{}, tokenSubject, buffercontext);
		
        // This is where you 'build' the project.
        // Since this is a dummy, we just say 'hello world' and call that a build.

        // This also shows how you can consult the global configuration of the builder
        /*if (getDescriptor().getUseFrench())
            listener.getLogger().println("Bonjour, "+name+"!");
        else
            listener.getLogger().println("Hello, "+name+"!");
        */   
            
        return true;
        
    }

    // Overridden for better type safety.
    public static DescriptorImpl descriptor()
    {
      return (DescriptorImpl)Jenkins.getInstance().getDescriptorByType(DescriptorImpl.class);
    }
    
    
    public BuildStepMonitor getRequiredMonitorService()
    {
      return BuildStepMonitor.NONE;
    }

    /**
     * Descriptor for {@link OwaConfigBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        
        private String owaUrl;
        private String owaUsername;
        private String owaPassword;
        private String owaBoxAccount;
        
        
        private volatile EmailTemplate[] emailTemplate = new EmailTemplate[0];
	

		/**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
        	super(OwaConfigBuilder.class); 
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Enviar Owa Email";
        }
        
        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) {
            return req.bindJSON(OwaConfigBuilder.class, formData);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            //useFrench = formData.getBoolean("useFrench");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
        	
        	
        	owaUrl = formData.getString("owaUrl");
        	owaUsername = formData.getString("owaUsername");
        	owaPassword = formData.getString("owaPassword");
        	owaBoxAccount = formData.getString("owaBoxAccount");

        	
        	List<EmailTemplate> list = req.bindJSONToList(EmailTemplate.class,formData.get("template"));
        	this.emailTemplate =  list.toArray( new EmailTemplate[list.size()]);
        	
        	
            save();
            return super.configure(req,formData);
        }
        
        
     
        
        /**
         * Comprueba la conexion
         * @param accessId
         * @param secretKey
         * @return
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doTestConnection(@QueryParameter("owaUrl") final String owaUrl,
        									   @QueryParameter("owaUsername") final String owaUsername,
        								       @QueryParameter("owaPassword") final String owaPassword) 
        								    		   					throws IOException, ServletException {
            try {
                
            	//ProxyConfiguration.load()
            	
            	OwaWebConnector owaConnect = new  OwaWebConnector(owaUrl, owaUsername, owaPassword,"");
            	owaConnect.initLoginOWA();
            	
            	
                return FormValidation.ok("Success");
            } catch (Exception e) {
                return FormValidation.error("Client error : "+e.getMessage());
            }
        }

        /**
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
        public String getOwaBoxAccount() {
			return owaBoxAccount;
		}

		public void setOwaBoxAccount(String owaBoxAccount) {
			this.owaBoxAccount = owaBoxAccount;
		}

		public String getOwaUrl() {
			return owaUrl;
		}

		public String getOwaUsername() {
			return owaUsername;
		}

		public String getOwaPassword() {
			return owaPassword;
		} 
		
		public EmailTemplate[] getEmailTemplate() {
             return this.emailTemplate;
		}

        public void setEmailTemplate(EmailTemplate... emailTemplate) {
			this.emailTemplate = emailTemplate;
			save();
		}
        
        
        @JavaScriptMethod
        public JSONObject getParameters(String scriptlerScriptId) {
        	return JSONObject.fromObject(getEmailTemplate()[0]);
        }
     
    }
}

