/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {
    public static Socket client = null;
    public static Socket server = null;
    public static ServerSocket ss;

    public static void proxyServer(String host, int remoteport) throws IOException {
        try {
            int localport = 61245;
            ProxyServer.runServer(host, remoteport, localport);
        }
        catch (Exception e) {
            try {
                client.close();
                server.close();
                ss.close();
                client = null;
                server = null;
                ss = null;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public static void runServer(String host, int remoteport, int localport) throws IOException {
        block23: {
            ss = new ServerSocket(localport);
            final byte[] request = new byte[1024];
            byte[] reply = new byte[4096];
            try {
                client = ss.accept();
                final InputStream streamFromClient = client.getInputStream();
                OutputStream streamToClient = client.getOutputStream();
                try {
                    server = new Socket(host, remoteport);
                }
                catch (IOException e) {
                    client.close();
                    client = null;
                    server = null;
                    try {
                        if (server != null) {
                            server.close();
                        }
                        if (client != null) {
                            client.close();
                        }
                        break block23;
                    }
                    catch (IOException iOException) {}
                    break block23;
                }
                try {
                    InputStream streamFromServer = server.getInputStream();
                    final OutputStream streamToServer = server.getOutputStream();
                    Thread t = new Thread(){

                        @Override
                        public void run() {
                            try {
                                int bytesRead;
                                while ((bytesRead = streamFromClient.read(request)) != -1) {
                                    streamToServer.write(request, 0, bytesRead);
                                    streamToServer.flush();
                                }
                            }
                            catch (IOException iOException) {
                                // empty catch block
                            }
                            try {
                                streamToServer.close();
                            }
                            catch (IOException iOException) {
                                // empty catch block
                            }
                        }
                    };
                    t.start();
                    try {
                        int bytesRead;
                        while ((bytesRead = streamFromServer.read(reply)) != -1) {
                            streamToClient.write(reply, 0, bytesRead);
                            streamToClient.flush();
                        }
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                    streamToClient.close();
                }
                catch (IOException e) {
                    System.err.println(e);
                }
            }
            finally {
                try {
                    if (server != null) {
                        server.close();
                    }
                    if (client != null) {
                        client.close();
                    }
                }
                catch (IOException iOException) {}
            }
        }
    }
}

