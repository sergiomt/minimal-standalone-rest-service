package com.knowgate.rest.test;

import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import java.net.URL;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpGetRequest extends Thread {

	private String targetUrl;
	private URL refererUrl;
	private Object retval;
	private String pageSrc, pageEncoding;
	private int responseCode;

	public HttpGetRequest(String targetUrl) {
		this.targetUrl = targetUrl;
		refererUrl = null;
		responseCode = 0;
		retval = null;
		pageSrc = null;
		pageEncoding = null;
	}

	public HttpGetRequest(String targetUrl, URL refererUrl) {
		this.targetUrl = targetUrl;
		this.refererUrl = refererUrl;
		responseCode = 0;
		retval = null;
		pageSrc = null;
		pageEncoding = null;
	}

	public void run() {
		try {
			get();
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
	} // run

	// ------------------------------------------------------------------------

	public String url() {
		return targetUrl;
	}

	public Object get()
	    throws IOException, URISyntaxException, MalformedURLException {

		retval = null;
		pageSrc = null;
		pageEncoding = null;

		URL urlObj = null;
		
		try {
			urlObj = new URL(targetUrl);
		} catch (MalformedURLException badurl) {
			return retval;
		}

		HttpURLConnection httpConn = (HttpURLConnection) urlObj.openConnection();

		httpConn.setUseCaches(false);
		httpConn.setInstanceFollowRedirects(false);
		httpConn.setRequestMethod("GET");
		httpConn.setRequestProperty("User-Agent",
		    "Mozilla/5.0 (Windows; U; Windows NT 6.1; rv:2.2) Gecko/20110201");

		try {
			responseCode = httpConn.getResponseCode();
		} catch (UnknownHostException ukn) {
			responseCode = HttpURLConnection.HTTP_BAD_REQUEST;
		} catch (ConnectException ukn) {
			responseCode = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
		}
		
		if ((responseCode == HttpURLConnection.HTTP_MOVED_PERM
		    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP)
		    && !targetUrl.equals(httpConn.getHeaderField("Location"))) {
			HttpGetRequest moved = new HttpGetRequest(
			    httpConn.getHeaderField("Location"), urlObj);
			retval = moved.get();
			targetUrl = moved.url();
			httpConn.disconnect();
		} else if (responseCode == HttpURLConnection.HTTP_OK) {
			InputStream instrm = null;
			try {
				instrm = httpConn.getInputStream();
			} catch (FileNotFoundException fnf) {
				responseCode = HttpURLConnection.HTTP_NOT_FOUND;
			}
			if (instrm != null) {
				pageEncoding = httpConn.getContentEncoding();
				if (pageEncoding == null) {
					ByteArrayOutputStream byout = new ByteArrayOutputStream();
					new StreamPipe().between(instrm, byout);
					retval = byout.toByteArray();
				} else {
					int c;
					StringBuffer stringbuff = new StringBuffer();
					Reader oRdr = new InputStreamReader(instrm, pageEncoding);
					while ((c = oRdr.read()) != -1) {
						stringbuff.append((char) c);
					} // wend
					oRdr.close();
					retval = stringbuff.toString();
				}
				instrm.close();
			} // fi (oStm!=null)
			if (responseCode != HttpURLConnection.HTTP_OK
			    && responseCode != HttpURLConnection.HTTP_ACCEPTED) {
				httpConn.disconnect();
				throw new IOException(String.valueOf(responseCode));
			} else {
				httpConn.disconnect();
			} // fi (responseCode)
		} // fi

		return retval;
	} // get

	// ------------------------------------------------------------------------

	/**
	 * Perform HTTP HEAD request
	 * 
	 * @return int HTTP response code
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public int head()
	    throws IOException, URISyntaxException, MalformedURLException {

		retval = null;
		pageSrc = null;
		pageEncoding = null;

		URL oUrl;

		if (null == refererUrl)
			oUrl = new URL(targetUrl);
		else
			oUrl = new URL(refererUrl, targetUrl);

		HttpURLConnection oCon = (HttpURLConnection) oUrl.openConnection();

		oCon.setUseCaches(false);
		oCon.setInstanceFollowRedirects(false);
		oCon.setRequestMethod("HEAD");

		responseCode = oCon.getResponseCode();

		if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
		    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
			HttpGetRequest oMoved = new HttpGetRequest(
			    oCon.getHeaderField("Location"), oUrl);
			oMoved.head();
			targetUrl = oMoved.url();
		}

		oCon.disconnect();

		return responseCode;
	} // head

	// ------------------------------------------------------------------------

	/**
	 * Get response code from last GET, POST or HEAD request
	 * 
	 * @return int
	 */
	public int responseCode() {
		return responseCode;
	}

	// ------------------------------------------------------------------------

	/**
	 * Get response as String
	 * 
	 * @return String
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * @throws URISyntaxException
	 */
	public String src()
	    throws IOException, UnsupportedEncodingException, URISyntaxException {

		if (pageSrc == null) {
			if (retval == null)
				get();

			pageSrc = null;

			if (retval != null) {
				String sRcl = retval.getClass().getName();
				if (sRcl.equals("[B")) {
					pageSrc = new String((byte[]) retval,
					pageEncoding == null ? "ASCII" : pageEncoding);
				} else if (sRcl.equals("java.lang.String")) {
					pageSrc = (String) retval;
				}
				Pattern content = Pattern.compile("content=[\"']text/\\w+;\\s*charset=((_|-|\\d|\\w)+)[\"']", Pattern.CASE_INSENSITIVE);
				Pattern xml = Pattern.compile( "<\\?xml version=\"1\\.0\" encoding=\"((_|-|\\d|\\w)+)\"\\?>", Pattern.CASE_INSENSITIVE);
				Matcher mtchr = content.matcher(pageSrc);
				if (mtchr.find()) {
					pageEncoding = mtchr.group(1);
					pageSrc = new String((byte[]) retval, pageEncoding);
				} else {
					mtchr = xml.matcher(pageSrc);
					if (mtchr.find()) {
						pageEncoding = mtchr.group(1);
						pageSrc = new String((byte[]) retval, pageEncoding);
					} else {
						pageEncoding = "ASCII";
					}
				}
			} // fi
		} // fi

		return pageSrc;
	} // src

	/**
	 * Get response encoding
	 * 
	 * @return String
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * @throws URISyntaxException
	 */
	public String encoding()
	    throws IOException, UnsupportedEncodingException, URISyntaxException {
		src();
		return pageEncoding;
	} // encoding

	/**
	 * Get response HTML document title
	 * 
	 * @return String
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
  public String getTitle() throws UnsupportedEncodingException, IOException, URISyntaxException {	 
	  String title = null;

  	src();
	 
  	if (pageSrc!=null) {
  	  Pattern titleTag = Pattern.compile("<title>(.*)</title>", Pattern.CASE_INSENSITIVE);
  	  Matcher mtchr = titleTag.matcher(pageSrc);
  	  if (mtchr.find())
  		  title = mtchr.group(1);
  	  else	 
  	  	title = "null";
  	}

	  return title;
  } // getTitle

}