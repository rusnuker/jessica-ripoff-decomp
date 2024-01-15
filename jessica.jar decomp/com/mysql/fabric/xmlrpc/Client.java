/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.fabric.xmlrpc;

import com.mysql.fabric.xmlrpc.base.MethodCall;
import com.mysql.fabric.xmlrpc.base.MethodResponse;
import com.mysql.fabric.xmlrpc.base.ResponseParser;
import com.mysql.fabric.xmlrpc.exceptions.MySQLFabricException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Client {
    private URL url;
    private Map<String, String> headers = new HashMap<String, String>();

    public Client(String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    public void setHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public void clearHeader(String name) {
        this.headers.remove(name);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public MethodResponse execute(MethodCall methodCall) throws IOException, ParserConfigurationException, SAXException, MySQLFabricException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)this.url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "MySQL XML-RPC");
            connection.setRequestProperty("Content-Type", "text/xml");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            for (Map.Entry<String, String> entry : this.headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            String out = methodCall.toString();
            OutputStream os = connection.getOutputStream();
            os.write(out.getBytes());
            os.flush();
            os.close();
            InputStream is = connection.getInputStream();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            ResponseParser saxp = new ResponseParser();
            parser.parse(is, (DefaultHandler)saxp);
            is.close();
            MethodResponse resp = saxp.getMethodResponse();
            if (resp.getFault() != null) {
                throw new MySQLFabricException(resp.getFault());
            }
            MethodResponse methodResponse = resp;
            Object var12_11 = null;
            if (connection != null) {
                connection.disconnect();
            }
            return methodResponse;
        }
        catch (Throwable throwable) {
            block5: {
                Object var12_12 = null;
                if (connection == null) break block5;
                connection.disconnect();
            }
            throw throwable;
        }
    }
}

