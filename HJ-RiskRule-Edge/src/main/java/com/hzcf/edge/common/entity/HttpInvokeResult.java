package com.hzcf.edge.common.entity;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.nio.charset.Charset;

public class HttpInvokeResult {

	public static final int SC_OK = 200;
	public static final int SC_NOT_MODIFIED = 304;
	public static final int SC_NOT_FOUND = 404;
	public static final int SC_SERVICE_UNAVAILABLE = 503;

	private HttpResponse response;
	private String url = null;
	private int statusCode = -1;
	private byte[] content = null;
	private Charset charset = null;
	private String reason = null;
	private Throwable e = null;

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public void setStatusCode(final int statusCode) {
		this.statusCode = statusCode;
	}

	public void setReason(final String reasonPhrase) {
		this.reason = reasonPhrase;
	}

	public HttpInvokeResult setContent(final byte[] content) {
		this.content = content;
		return this;
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	public byte[] getContent() {
		return this.content;
	}

	public String getContentString() {
		if (this.content == null) {
			return null;
		}
		return new String(this.content, this.charset);
	}

	public String getContentStringAndReset() {
		final String result = this.getContentString();
		this.content = null;
		return result;
	}

	public String getReason() {
		return this.reason;
	}

	public void setException(final Throwable e) {
		this.e = e;
	}

	public Throwable getException() {
		return this.e;
	}

	public boolean isOK() {
		return this.statusCode == SC_OK;
	}

	String FORMAT = "URL:[%s],StatusCode:[%s],Reason[%s]";

	@Override
	public String toString() {
		return String.format(this.FORMAT, this.url, this.statusCode, this.reason);
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(final String url) {
		this.url = url;

	}

	public HttpResponse getResponse() {
		return response;
	}

	public void setResponse(final HttpResponse response) {
		this.response = response;
	}

	public String getHeader(final String name) {
		if(this.response.getLastHeader(name) == null)
			return null;
		return this.response != null ? this.response.getLastHeader(name).getValue() : null;
	}
	
	public Header[] getAllHeader() {
		return this.response != null ? this.response.getAllHeaders() : null;
	}
	
	public Header[] getRespCookies() {
		if (this.response != null && this.response.getFirstHeader("Set-Cookie") != null) {
			return this.response.getHeaders("Set-Cookie");
		} else {
			return null;
		}
	}

}
