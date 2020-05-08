package com.actigence.aal;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class HttpResponseCachingWrapper extends HttpServletResponseWrapper {
    private ByteArrayPrintWriter byteArrayPrintWriter = new ByteArrayPrintWriter();

    private byte[] bytes;

    private final HttpServletResponse httpServletResponse;

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @param response The response to be wrapped
     * @throws IllegalArgumentException if the response is null
     */
    public HttpResponseCachingWrapper(HttpServletResponse response) {
        super(response);
        httpServletResponse = response;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return byteArrayPrintWriter.getWriter();
    }

    public ServletOutputStream getOutputStream() {
        return byteArrayPrintWriter.stream;
    }

    public byte[] getBytes() {
        if (bytes == null) {
            bytes = byteArrayPrintWriter.toByteArray();
        }
        return bytes;
    }

    public int getStatusCode() {
        return httpServletResponse.getStatus();
    }

    private static class ByteArrayPrintWriter {
        private ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private PrintWriter writer = new PrintWriter(baos);
        ServletOutputStream stream = new ByteArrayServletStream(baos);

        byte[] toByteArray() {
            return baos.toByteArray();
        }

        public PrintWriter getWriter() {
            return writer;
        }
    }

    private static class ByteArrayServletStream extends ServletOutputStream {
        private final ByteArrayOutputStream byteArrayOutputStream;

        private ByteArrayServletStream(ByteArrayOutputStream baos) {
            this.byteArrayOutputStream = baos;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }

        @Override
        public void write(int b) throws IOException {
            byteArrayOutputStream.write(b);
        }
    }
}
