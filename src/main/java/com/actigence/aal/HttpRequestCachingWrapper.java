package com.actigence.aal;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
public class HttpRequestCachingWrapper extends HttpServletRequestWrapper
{
    private final String body;

    public HttpRequestCachingWrapper(HttpServletRequest httpServletRequest) throws IOException
    {
        super(httpServletRequest);
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try
        {
            InputStream inputStream = httpServletRequest.getInputStream();
            if (inputStream != null)
            {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = bufferedReader.read(charBuffer);
                while (bytesRead > 0)
                {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                    bytesRead = bufferedReader.read(charBuffer);
                }
            }
        }
        finally
        {
            if (bufferedReader != null)
            {
                try
                {
                    bufferedReader.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }

        body = stringBuilder.toString();
    }

    @Override
    public BufferedReader getReader()
    {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    public ServletInputStream getInputStream()
    {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());

        return new ServletInputStream()
        {
            @Override
            public boolean isFinished()
            {
                return false;
            }

            @Override
            public boolean isReady()
            {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener)
            {

            }

            @Override
            public int read()
            {
                return byteArrayInputStream.read();
            }
        };
    }

    public String getBody()
    {
        return body;
    }
}
