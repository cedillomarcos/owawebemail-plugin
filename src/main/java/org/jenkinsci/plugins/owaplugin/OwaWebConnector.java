package org.jenkinsci.plugins.owaplugin;


import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

/** ------------------------------------------------------------
 * OWA - Microsoft Office Outlook utility to send
 * email via web form POST
 * 
 * Simulate a navigator that send email. 
 * 
 * @author Marcos Martinez Cedillo
 *  ------------------------------------------------------------
 */
public class OwaWebConnector {
	
	/* HTTCLIENT --> call get or method */
	private  HttpClient httpclient = null;
	private String urlOWA = "";
	private String boxAccount = "";
	private String userName = "";
	private String passWord = "";
	
	
	/**
	 * Contructor
	 * 
	 */
	public OwaWebConnector(String url, String username, String password, String boxAccount){
		
		this.urlOWA = url;
		this.userName = username;
		this.passWord = password;
		this.boxAccount = boxAccount;
		
		
		if(url.contains("https://")){
		  initSSLContext();
		}
		
		initHTTClient();
		
		//initProxy();
	}
	
	
	/**
	 * Initzialite a ssl context 
	 */
	private void initSSLContext(){
	    SSLContext ctx;
		try {
			ctx = SSLContext.getInstance("TLS");
		    ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
	        SSLContext.setDefault(ctx);
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		
	}
	
	private void initProxy(){
		 //httpclient.getHostConfiguration().setProxy("127.0.0.1",8383);
	}
	
	
	/**
	 * Initial of httclient simulator 
	 */
	private void initHTTClient(){
	
	     
        // Get initial state object
        HttpState initialState = new HttpState();
        
        httpclient = new HttpClient();
        httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
        httpclient.setState(initialState);
        
        
	}
	
	/**
	 *  Make a login 
	 */
	public boolean initLoginOWA(){
		boolean login = false; 
		
		HttpMethod method  = new GetMethod(urlOWA + "/auth/logon.aspx?url=" + urlOWA + "&reason=0");
        method.addRequestHeader("user-agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
        
        try {
        	//Getting cookie
			httpclient.executeMethod(method);
			method.releaseConnection();
			method.getStatusCode();
			//Make a login post
			PostMethod httpPost = new PostMethod(urlOWA + "auth/owaauth.dll");
			addHeadersParams(httpPost);
			
			NameValuePair[] data = {
	                new NameValuePair("destination", urlOWA),
	                new NameValuePair("flags", "0"),
	                new NameValuePair("forcedownlevel", "0"),
	                new NameValuePair("trusted", "0"),
	                new NameValuePair("username", userName ),
	                new NameValuePair("password", passWord ),
	                new NameValuePair("isUtf8", "1")
	              };
	        httpPost.setRequestBody(data);
	        httpclient.executeMethod(httpPost);
	        httpPost.releaseConnection(); 

	        int statuscode = httpPost.getStatusCode();
	        if ((statuscode == HttpStatus.SC_MOVED_TEMPORARILY)
                    || (statuscode == HttpStatus.SC_MOVED_PERMANENTLY)
                    || (statuscode == HttpStatus.SC_SEE_OTHER)
                    || (statuscode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
               Header header = httpPost.getResponseHeader("location");
               httpPost.getStatusLine();
               
	        }  
	        //Header locationHeader = method.getResponseHeader("location");
	        // Get all the cookies
    	    Cookie[] cookies = httpclient.getState().getCookies();
   	        // Display the cookies
    	    
   	        for (int i = 0; i < cookies.length; i++) {
   	        	if("sessionid".equals(cookies[i].getName())){
   	        		login = true;	
   	        	}
   	            //System.out.println(" - " + cookies[i].toExternalForm());
   	        }
    	    
	        
	        String response = httpPost.getResponseBodyAsString();
	          
	        System.out.println(method.getResponseBodyAsString());
	        
	        // Release current connection to the connection pool once you are done
	        httpPost.releaseConnection();
			
			
			
			
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        method.releaseConnection();
        return login;
	}
	
	
	/**
	 * Send email with OWA
	 * 
	 * 
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public void sendEmail(String to, String [] cc, String bcc[], String subject, StringBuffer textEmail) throws HttpException, IOException{
		
		GetMethod method = new GetMethod(urlOWA + boxAccount + "/?ae=PreFormAction&t=IPM.Note&a=Send");
        httpclient.executeMethod(method);
        
        
        System.out.println("Response status code: " + method.getStatusLine());
        String bodyHtmlForm = method.getResponseBodyAsString();
        System.out.println(bodyHtmlForm);
        method.releaseConnection();
    	        
        //Buscamos el campo hidden hidcanary que contiene un id que hay que reenviar
    	Pattern p = Pattern.compile("name=\"hidcanary\" value=\"(.*)\"");
		Matcher m = p.matcher(bodyHtmlForm);

		String hidcanary="";
		while (m.find()) {
			System.out.println("Found a " + m.group() + ".");
			hidcanary = m.group(1);
		}
		
		
		PostMethod httpSendEmail = new PostMethod(urlOWA + boxAccount + "/?ae=PreFormAction&t=IPM.Note&a=Send");
        addHeadersParams(httpSendEmail);
        
        List<NameValuePair> dataEmail = new ArrayList<NameValuePair>();
        
        dataEmail.add(new NameValuePair("hidpnst", ""));
        dataEmail.add(new NameValuePair("txtto", to));
        if(cc!=null && cc.length>0){
        	dataEmail.add(new NameValuePair("txtcc", Arrays.toString(cc).replace("[", "").replace("]", "")));
        }
        dataEmail.add(new NameValuePair("txtbcc", ""));
        //Subject
        dataEmail.add(new NameValuePair("txtsbj", subject));
        
        //Body
        dataEmail.add(new NameValuePair("txtbdy", textEmail.toString()));
        dataEmail.add(new NameValuePair("hidid", ""));
        dataEmail.add(new NameValuePair("hidchk", ""));
        dataEmail.add(new NameValuePair("hidunrslrcp", "0"));
        dataEmail.add(new NameValuePair("hidcmdpst", "snd"));
        dataEmail.add(new NameValuePair("hidrmrcp", ""));
        dataEmail.add(new NameValuePair("hidaddrcptype", ""));
        dataEmail.add(new NameValuePair("hidaddrcp", ""));
        dataEmail.add(new NameValuePair("hidss", ""));
        dataEmail.add(new NameValuePair("hidmsgimp", "1"));
        dataEmail.add(new NameValuePair("hidrcptid", ""));
        dataEmail.add(new NameValuePair("hidrw", ""));
        dataEmail.add(new NameValuePair("hidpid", "EditMessage"));
        dataEmail.add(new NameValuePair("hidcanary", hidcanary));
        
        
        httpSendEmail.setRequestBody(dataEmail.toArray(new NameValuePair[dataEmail.size()]));
        httpclient.executeMethod(httpSendEmail);

	}
	


	public void test(){
		   // configure the SSLContext with a TrustManager
   

//        URL url = new URL("https://correoweb.mineco.es/owa/");
//        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//        conn.setHostnameVerifier(new HostnameVerifier() {
//            @Override
//            public boolean verify(String arg0, SSLSession arg1) {
//                return true;
//            }
//        });
        
   
        
        //httpclient.getHostConfiguration().setProxy("127.0.0.1",8383);
        
        
        
//        Host: correoweb.mineco.es
//        User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0
//        Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//        Accept-Language: es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3
//        Accept-Encoding: gzip, deflate
//        Connection: keep-alive
//        Cache-Control: max-age=0
        
        
        
//        httpget.getParams().setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        
        //client.getParams().setParameter("http.protocol.single-cookie-header", true);
//        client.getParams().setCookiePolicy(DefaultHttpParams.);
        
        // RFC 2101 cookie management spec is used per default
        // to parse, validate, format & match cookies
        //httpclient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
 //       httpclient.getParams().setParameter(HttpMethodParams.USER_AGENT, "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.7) Gecko/20070914 Firefox/2.0.0.7"); 
        //httpclient.getParams().setCookiePolicy(CookiePolicy.DEFAULT);
        
      //  httpclient.getParams().setParameter("http.protocol.single-cookie-header", true);
	    //client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
     //   httpclient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        
		

        
     
        	        
  //      System.out.println(method.getResponseBodyAsString());
        	        
        	        // Release current connection to the connection pool once you are done

        	        
        	        
        	        
        	        

        
        	        
        	        /*
        	        System.out.println("POST:=>Response status code: " + httpPost.getStatusLine());
                    // Get all the cookies
            	        Cookie[] cookies = httpclient.getState().getCookies();
            	        // Display the cookies
            	        System.out.println("Present cookies: ");
            	        for (int i = 0; i < cookies.length; i++) {
            	            System.out.println(" - " + cookies[i].toExternalForm());
            	        }
            	        
            
            
            
            
            
            method  = new GetMethod(urlOWA + boxAccount + "/?ae=PreFormAction&t=IPM.Note&a=Send");
            httpclient.executeMethod(method);
            
            
            System.out.println("Response status code: " + method.getStatusLine());
            String bodyHtmlForm = method.getResponseBodyAsString();
            System.out.println(bodyHtmlForm);
            method.releaseConnection();
        	        
            //Buscamos el campo hidden hidcanary que contiene un id que hay que reenviar
            
            
        	Pattern p = Pattern.compile("name=\"hidcanary\" value=\"(.*)\"");
    		Matcher m = p.matcher(bodyHtmlForm);

    		String hidcanary="";
    		while (m.find()) {
    			System.out.println("Found a " + m.group() + ".");
    			hidcanary = m.group(1);
    		}
            
            
        	        

            
        	///---------------------------------------------------
            
            
            //hay que buscar el codigo hidden obtenidos del form 
            
            //--------------------
            
	        PostMethod httpSendEmail = new PostMethod("https://correoweb.mineco.es/owa/soporte.uca@redinterna.age/?ae=PreFormAction&t=IPM.Note&a=Send");
	        addHeadersParams(httpSendEmail);
	        
	        NameValuePair[] dataEmail = {
	                new NameValuePair("hidpnst", ""),
	                new NameValuePair("txtto", "marcos.martinez@bilbomatica.es"),
	                new NameValuePair("txtcc", "drimada@bilbomatica.es"),
	                new NameValuePair("txtbcc", ""),
	                //Subject
	                new NameValuePair("txtsbj", "Prueba envio correo desde la uca"),
	                
	                //Body
	                new NameValuePair("txtbdy", "Prueba env�o"),
	                
	                //Otros datos
	                new NameValuePair("hidid", ""),
	                new NameValuePair("hidchk", ""),
	                new NameValuePair("hidunrslrcp", "0"),
	                new NameValuePair("hidcmdpst", "snd"),
	                new NameValuePair("hidrmrcp", ""),
	                new NameValuePair("hidaddrcptype", ""),
	                new NameValuePair("hidaddrcp", ""),
	                new NameValuePair("hidss", ""),
	                new NameValuePair("hidmsgimp", "1"),
	                new NameValuePair("hidrcptid", ""),
	                new NameValuePair("hidrw", ""),
	                new NameValuePair("hidpid", "EditMessage"),
	                new NameValuePair("hidcanary", hidcanary)
	              };
	        httpSendEmail.setRequestBody(dataEmail);
	        httpclient.executeMethod(httpSendEmail);
	        
	        System.out.println("Response status code: " + httpSendEmail.getStatusLine());
	        
	        //System.out.println(conn.getResponseCode());
//        conn.disconnect();
//        
		*/
	}
	
	/**
	 * 
	 * @param method
	 */
	private void addHeadersParams(HttpMethod method){
		method.addRequestHeader("user-agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
	        //	        httpPost.addRequestHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux " +
	        //	  						   "i686; en-US; rv:1.8.1.6) Gecko/20061201 Firefox/2.0.0.6 (Ubuntu-feisty)");
	        //	        httpPost.addRequestHeader("Accept", "text/html,application/xml," +
	        //	  							"application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
	        	        //httpPost.addRequestHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
		method.addRequestHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		method.addRequestHeader("Connection","keep-alive");
	    method.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		
	}
	
	/**
	 * Implements default thrust manager to accept all ssl connections
	 * 
	 */
	private static class DefaultTrustManager implements X509TrustManager {

	        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
	        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
	        public X509Certificate[] getAcceptedIssuers() {
	            return null;
	        }
	    }
}
