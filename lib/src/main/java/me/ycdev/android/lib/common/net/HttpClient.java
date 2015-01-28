package me.ycdev.android.lib.common.net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import me.ycdev.android.lib.common.utils.LibConfigs;
import me.ycdev.android.lib.common.utils.IoUtils;
import me.ycdev.android.lib.common.utils.LibLogger;

import org.apache.http.protocol.HTTP;

import android.content.Context;

public class HttpClient {
    private static final String TAG = "HttpClient";
    private static final boolean DEBUG = LibConfigs.DEBUG_LOG;

    private String mCharset = HTTP.UTF_8;
    private int mConnectTimeout;  // ms
    private int mReadTimeout;  // ms

    public HttpClient() {
        setTimeout(10000, 10000); // default to 10 seconds
    }

    public void setTimeout(int connectTimeout, int readTimeout) {
        mConnectTimeout = connectTimeout;
        mReadTimeout = readTimeout;
    }

    public String get(Context cxt, String url,  HashMap<String, String> requestHeaders)
            throws IOException {
        HttpURLConnection httpConn = null;
        httpConn = getHttpConnection(cxt, url, false, requestHeaders);
        try {
            httpConn.connect();
        } catch (Exception e) {
            throw new IOException(e.toString());
        }
        try {
            return getResponse(httpConn);
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
    }

    public String post(Context cxt, String url, String body) throws IOException {
        HttpURLConnection httpConn = null;
        DataOutputStream os = null;

        httpConn = getHttpConnection(cxt, url, true, null);

        // Send the "POST" request
        try {
            os = new DataOutputStream(httpConn.getOutputStream());
            os.write(body.getBytes(mCharset));
            os.flush();
            return getResponse(httpConn);
        } catch (Exception e) {
            // should not be here, but.....
            throw new IOException(e.toString());
        } finally {
            // Must be called before calling HttpURLConnection.disconnect()
            IoUtils.closeQuietly(os);

            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
    }

    public String post(Context cxt, String url, byte[] body) throws IOException {
        HttpURLConnection httpConn = null;
        DataOutputStream os = null;

        httpConn = getHttpConnection(cxt, url, true, null);

        // Send the "POST" request
        try {
            os = new DataOutputStream(httpConn.getOutputStream());
            os.write(body);
            os.flush();
            return getResponse(httpConn);
        } catch (Exception e) {
            // prepare for any unexpected exceptions
            throw new IOException(e.toString());
        } finally {
            // Must be called before calling HttpURLConnection.disconnect()
            IoUtils.closeQuietly(os);

            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
    }

    private HttpURLConnection getHttpConnection(Context cxt, String url,
            boolean post, HashMap<String, String> requestHeaders) throws IOException {
        HttpURLConnection httpConn = openHttpURLConnection(cxt, url);
        httpConn.setConnectTimeout(mConnectTimeout);
        httpConn.setReadTimeout(mReadTimeout);
        httpConn.setDoInput(true);
        httpConn.setUseCaches(false);
        httpConn.setRequestProperty("Accept-Encoding", "gzip,deflate");
        httpConn.setRequestProperty("Charset", mCharset);
        if (requestHeaders != null) {
            addRequestHeaders(httpConn, requestHeaders);
        }
        if (post) {
            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("POST");
        } else {
            httpConn.setRequestMethod("GET");  // by default
        }
        return httpConn;
    }

    private static HttpURLConnection openHttpURLConnection(Context ctx, String url) throws IOException {
        // TODO support proxy
        return (HttpURLConnection) new URL(url).openConnection();
    }

    private void addRequestHeaders(HttpURLConnection httpConn, HashMap<String, String> requestHeaders) {
        Set<Map.Entry<String, String>> allHeaders = requestHeaders.entrySet();
        for (Map.Entry<String, String> header : allHeaders) {
            httpConn.addRequestProperty(header.getKey(), header.getValue());
        }
    }

    private String getResponse(HttpURLConnection httpConn) throws IOException {
        String contentEncoding = httpConn.getContentEncoding();
        if (DEBUG) {
            LibLogger.d(TAG, "response code: " + httpConn.getResponseCode()
                    + ", encoding: " + contentEncoding + ", method: " + httpConn.getRequestMethod());
        }

        InputStream httpInputStream = null;
        try {
            httpInputStream = httpConn.getInputStream();
        } catch (IllegalStateException e) {
            // ignore
        }
        if (httpInputStream == null) {
            throw new IOException("HttpURLConnection.getInputStream() returned null");
        }

        InputStream is = null;
        if (contentEncoding != null && contentEncoding.contains("gzip")) {
            is = new GZIPInputStream(httpInputStream);
        } else if (contentEncoding != null && contentEncoding.contains("deflate")) {
            is = new InflaterInputStream(httpInputStream);
        } else {
            is = httpInputStream;
        }

        // Read the response content
        try {
            byte[] responseContent = IoUtils.readAllBytes(is);
            return new String(responseContent, mCharset);
        } finally {
            // Must be called before calling HttpURLConnection.disconnect()
            IoUtils.closeQuietly(is);
        }
    }

}
