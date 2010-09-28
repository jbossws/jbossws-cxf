package org.jboss.test.ws.saaj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

public class InputStreamDataSource implements DataSource 
{

	private InputStream is;
	private String contentType;
	private String name;
	
	public InputStreamDataSource(InputStream is, String contentType, String name) 
	{
		this.is = is;
		this.contentType = contentType;
		this.name = name;
	}
	@Override
	public String getContentType() 
	{
	   return contentType;
	}

	@Override
	public InputStream getInputStream() throws IOException 
	{
	   return is;
	}

	@Override
	public String getName() {
	   return name;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
	   throw new UnsupportedOperationException();
	}

}
