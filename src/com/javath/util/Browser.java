package com.javath.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.javath.Configuration;
import com.javath.Object;
import com.javath.ObjectException;

public class Browser extends Object {
	
	public static final int ERROR_NOT_SUPPORT = 1; // ObjectException
	
	private static boolean proxyEnable;
	private static HttpHost defaultProxy;
	private static Scheme http;
	private static Scheme https;
	
	private URI uri;
	private HttpClient httpClient;
	private HttpContext httpContext;
	private HttpHost proxy;
	
	private static int buffer_size = 2048;
	
	private String pathContent;
	private String fileContent;
	
	private Header[] headers;
	private String protocolVersion;
	private int statusCode = 0;
	private String reasonPhrase;
	private String contentType = "";
	private String contentCharset = Charset.defaultCharset().toString();

	static {
		try {

			Properties properties = new Properties();
			String configuration = path.etc + file.separator
					+ "browser.properties";
			FileInputStream propsFile = new FileInputStream(
					Configuration.getProperty("configuration.util.browser",
							configuration));
			properties.load(propsFile);
			propsFile.close();
			String enable = properties.getProperty("proxy.enable", "false").toLowerCase();
			if (enable.equals("true") || enable.equals("1"))
				proxyEnable = true;
			else
				proxyEnable = false;
			String hostname = properties.getProperty("proxy.hostname", 
					"localhost");
			int port = Integer.parseInt(properties.getProperty("proxy.port",
					"8080"));
			if (hostname != null)
				defaultProxy = new HttpHost(hostname, port);
			setScheme();
		} catch (FileNotFoundException e) {
			//logger.severe(message(e));
			throw new ObjectException(e);
		} catch (IOException e) {
			//logger.severe(message(e));
			throw new ObjectException(e);
		}
	}

	
	public Browser(HttpHost proxy, HttpContext context) {
		setContext(context);
		proxyEnable(proxy);
		setPathContent(path.tmp);
	}
	
	public Browser(HttpHost proxy) {
		getContext();
		proxyEnable(proxy);
		setPathContent(path.tmp);
	}
	
	public Browser(HttpContext context) {
		setContext(context);
		if (proxyEnable)
			proxyEnable(defaultProxy);
		else
			proxyDisble();
		setPathContent(path.tmp);
	}

	public Browser() {
		getContext();
		if (proxyEnable)
			proxyEnable(defaultProxy);
		else
			proxyDisble();
		setPathContent(path.tmp);
	}
	
	private static void setScheme() {
		try {
			http = new Scheme("http", 80,
					PlainSocketFactory.getSocketFactory());
	
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
	
			    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
			    }
	
			    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
			    }
	
			    public X509Certificate[] getAcceptedIssuers() {
			        return null;
			    }
			};
			sslcontext.init(null, new TrustManager[]{(TrustManager) tm}, null);
			SSLSocketFactory sf = new SSLSocketFactory(sslcontext,
					SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			//		SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
			//		SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
			https = new Scheme("https", 443, sf);
		} catch (NoSuchAlgorithmException e) {
			//logger.severe(message(e));
			throw new ObjectException(e);
		} catch (KeyManagementException e) {
			//logger.severe(message(e));
			throw new ObjectException(e);
		}
	}
	
	public HttpHost getProxy() {
		return proxy;
	}
	
	public void setPathContent(String path) {
		pathContent = path;
	}
	
	private void setContext(HttpContext httpContext) {
		 this.httpContext = httpContext;
	}
	
	public HttpContext getContext() {
		if (this.httpContext == null) {
			CookieStore cookieStore = new BasicCookieStore();
			HttpContext httpContext = new BasicHttpContext();
			httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
			this.httpContext = httpContext;
		}
		return this.httpContext;
	}

	public Browser setBufferSize(int size) {
		buffer_size = size;
		return this;
	}
	
	private HttpClient getHttpClient() {
		HttpClient httpClient = new DefaultHttpClient();
		
		// HttpParams httpParams = new BasicHttpParams();
		httpClient.getConnectionManager().getSchemeRegistry()
				.register(http);
		httpClient.getConnectionManager().getSchemeRegistry()
				.register(https);
		//
		// User-Agent: Mozilla/5.0 (Compatible)
		httpClient.getParams().setParameter(
				CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (Compatible)");
		return httpClient;
	}
	
	public Browser proxyEnable() {
		return proxyEnable(getProxy());
	}
	
	public Browser proxyEnable(HttpHost proxy) {
		httpClient = getHttpClient();
		if (proxy != null)
			httpClient.getParams().setParameter(
					ConnRouteParams.DEFAULT_PROXY, proxy);
		return this;
	}
	
	public Browser proxyDisble() {
		httpClient = getHttpClient();
		return this;
	}
	
	private void setURI(String request) {
		try {
			URI uri = null;
			if (request.charAt(0) == '/')
				uri = new URI(this.uri.getScheme() + "://" + this.uri.getHost() + request);
			else
				uri = new URI(request);
			if (uri.getScheme() == null)
				uri = new URI("http://" + request);
			this.uri = uri;
		} catch (URISyntaxException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
		}
	}
	
	public InputStream get(String request) {
		// TODO HTTP request method get()
		InputStream inputStream = null;
		HttpGet httpGet = null;

		try {
			setURI(request);
			httpGet = new HttpGet(uri);
			HttpResponse httpResponse = httpClient.execute(httpGet,
					httpContext);
			headers = httpResponse.getAllHeaders();
			this.setResponse(httpResponse);
			HttpEntity httpEntity = httpResponse.getEntity();
			if (httpEntity != null) {
				this.setContentType(httpEntity);
				inputStream = httpEntity.getContent();
				inputStream = saveContent(uri, inputStream);
				//return saveContent(uri, inputStream);
			}
		} catch (ClientProtocolException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
		} catch (IOException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
		} finally {
			httpGet.abort();
		}
		return inputStream;
	}

	public InputStream head(String request) {
		// TODO HTTP request method head()
		throw newObjectException(ERROR_NOT_SUPPORT);
	}

	public InputStream post(String request, List<NameValuePair> form) {
		// TODO HTTP request method post()
		InputStream inputStream = null;
		HttpPost httpPost = null;

		try {
			setURI(request);
			httpPost = new HttpPost(uri);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form);
			httpPost.setEntity(entity);

			HttpResponse httpResponse = httpClient.execute(httpPost,
					httpContext);
			headers = httpResponse.getAllHeaders();
			this.setResponse(httpResponse);
			HttpEntity httpEntity = httpResponse.getEntity();
			if (httpEntity != null) {
				this.setContentType(httpEntity);
				inputStream = httpEntity.getContent();
				inputStream = saveContent(uri, inputStream);
				//return saveContent(uri, inputStream);
			}
		} catch (UnsupportedEncodingException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
		} catch (ClientProtocolException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
		} catch (IOException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
		} finally {
			httpPost.abort();
		}
		return inputStream;
	}

	public InputStream post(String request, Form form) {
		return this.post(request, form.build());
	}

	public InputStream put(String request) {
		// TODO HTTP request method put()
		throw newObjectException(ERROR_NOT_SUPPORT);
	}

	public InputStream delete(String request) {
		// TODO HTTP request method delete()
		throw newObjectException(ERROR_NOT_SUPPORT);
	}

	public InputStream trace(String request) {
		// TODO HTTP request method trace()
		throw newObjectException(ERROR_NOT_SUPPORT);
	}

	public InputStream option(String request) {
		// TODO HTTP request method option()
		throw newObjectException(ERROR_NOT_SUPPORT);
	}
	
	public InputStream getInputStream() {
		try {
			return new FileInputStream(fileContent);
		} catch (FileNotFoundException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
			//return null;
		}
	}
	
	public String getScheme() {
		return uri.getScheme();
	}
	
	public String getHost() {
		return uri.getHost();
	}
	
	public Header[] getHeaders() {
		return headers;
	}
	
	private void setResponse(HttpResponse httpResponse) {
		protocolVersion = httpResponse.getProtocolVersion().toString();
		statusCode = httpResponse.getStatusLine().getStatusCode();
		reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
	}
	
	public String getProtocolVersion() {
		return protocolVersion;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getReasonPhrase() {
		return reasonPhrase;
	}

	private void setContentType(HttpEntity httpEntity) {
		String Type = " ";
		String charset = null;
		try {
			Header header = httpEntity.getContentType();
			HeaderElement[] element = header.getElements();
			for (int index = 0; index < element.length; index++) {
				Type = Type + element[index].getName() + " ";
				NameValuePair valuepair = element[index]
						.getParameterByName("charset");
				if (valuepair != null)
					charset = valuepair.getValue();
			}
		} catch (NullPointerException e) {
			logger.warning(message(e));
		}
		contentType = Type.trim();
		if (charset != null)
			contentCharset = charset;
	}

	public String getContentType() {
		return contentType;
	}

	public String getContentCharset() {
		return contentCharset;
	}
	
	private String queryValue(String query) {
		return query.substring(query.indexOf('=') + 1);
	}

	private InputStream saveContent(URI uri, InputStream inputStream)
			throws IOException {
		String fileOutput = uri.getPath();
		if (fileOutput.equals(""))
			fileOutput = "index.html";
		else
			fileOutput = fileOutput.substring(fileOutput.lastIndexOf('/') + 1);
		// Replace value of query in fileOutput
		String query = uri.getQuery();
		String queryOutput = "";
		if (query != null) {
			int index = 0;
			while(query.indexOf('&', index) > -1) {
				int start = index;
				index = query.indexOf('&', start);
				String value = queryValue(query.substring(start, index));
				if (!value.equals(""))
					queryOutput += ("." +  value);
				index += 1;
			}
			String value = queryValue(query.substring(index));
			if (!value.equals(""))
				queryOutput += ("." +  value);
		}
		if (!queryOutput.equals(""))
			fileOutput = fileOutput.replace(".", queryOutput + "." );
		//
		fileContent = file.unique(pathContent + file.separator + fileOutput);
		OutputStream outputStream = file.overwrite(fileContent);

		int read = 0;
		byte[] buffer = new byte[buffer_size];
		try {
			while ((read = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, read);
			}
		} catch (IOException e) {
			logger.severe(message(e));
			throw new ObjectException(e);
		} finally {
			inputStream.close();
			outputStream.flush();
			outputStream.close();
		}
		return file.read(fileContent);
	}
	
	
	public void printHeaders() {
		printStart("Headers");
		for (int index = 0; index < headers.length; index++) {
			System.out.printf("%s = '%s'%n", headers[index].getName(), headers[index].getValue()); 
		}
		printEnd("Headers");
	}
	
	public String getCookie(String name) {
		CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
		List<Cookie> cookies = cookieStore.getCookies();
		for (int index = 0; index < cookies.size(); index++)
			if (cookies.get(index).getName().equals(name))
				return cookies.get(index).getValue();
		return null;
	}
	
	public void printCookie() {
		CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
		List<Cookie> cookies = cookieStore.getCookies();
		printStart("Context Cookie");
		for (int index = 0; index < cookies.size(); index++) {
			System.out.printf("Name='%s', Value='%s', Path='%s', Domain='%s', Expires='%s'%n", 
					cookies.get(index).getName(), cookies.get(index).getValue(),
					cookies.get(index).getPath(), cookies.get(index).getDomain(), cookies.get(index).getExpiryDate()); 
		}
		printEnd("Context Cookie");
	}
	
	public void printContent() {
		try {
			InputStream inputStream = file.read(fileContent);
			int read = 0;
			byte[] buffer = new byte[buffer_size];
			printStart("Content");
			while ((read = inputStream.read(buffer)) != -1) {
				System.out.print(new String(buffer, 0, read, this
						.getContentCharset()));
			}
			inputStream.close();
			printEnd("Content");
		} catch (IOException e) {
			logger.severe(message(e));
			//throw new ObjectException(e);
		}
	}
	
	private void printStart(String message) {
		System.out.printf("%n/********************%n");
		System.out.printf(" * Start %s%n", message);
		System.out.printf(" ********************/%n");
	}
	
	private void printEnd(String message) {
		System.out.printf("%n/********************%n");
		System.out.printf(" * End %s%n", message);
		System.out.printf(" ********************/%n");
	}
	
	public String getFileContent() {
		return fileContent;
	}

}
