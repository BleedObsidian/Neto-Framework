/*
    Neto-Framework, a lightweight, event driven network application framework.
    Copyright (C) 2014  BleedObsidian (Jesse Prescott)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.neto_framework.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import net.neto_framework.PacketManager;
import net.neto_framework.address.SocketAddress;
import net.neto_framework.event.EventHandler;
import net.neto_framework.packets.DisconnectPacket;
import net.neto_framework.packets.HandshakePacket;
import net.neto_framework.packets.SuccessPacket;
import net.neto_framework.server.exceptions.ServerException;
import net.neto_framework.server.packets.handlers.DisconnectPacketHandler;
import net.neto_framework.server.packets.handlers.HandshakePacketHandler;
import net.neto_framework.utils.NetoFramework;

/**
 * A server handler that receives and accepts connections using a given
 * protocol.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class Server {
    
    /**
     * Default backlog value for TCP server socket.
     */
    public static final int DEFAULT_BACKLOG = 50;
    
    /**
     * Default key size for DESede used for encrypting UDP.
     */
    public static final int DEFAULT_KEYSIZE = 2048;
    
    /**
     * The version of Neto-Framework the server is using loaded at runtime.
     */
    private final String version;

    /**
     * The {@link net.neto_framework.PacketManager PacketManager}.
     */
    private final PacketManager packetManager;

    /**
     * The {@link net.neto_framework.server.ServerTCPConnectionHandler 
     * ServerTCPConnectionHandler}.
     */
    private final ServerTCPConnectionHandler tcpConnectionHandler;
    
    /**
     * The {@link net.neto_framework.server.ServerUDPConnectionHandler 
     * ServerUDPConnectionHandler}.
     */
    private final ServerUDPConnectionHandler udpConnectionHandler;
    
    /**
     * The {@link net.neto_framework.server.ServerConnectionManager
     * ServerConnectionManager}.
     */
    private final ServerConnectionManager connectionManager;

    /**
     * The {@link net.neto_framework.event.EventHandler EventHandler}.
     */
    private final EventHandler eventHandler;

    /**
     * The {@link net.neto_framework.address.SocketAddress SocketAddress} the
     * server is running or to be run on.
     */
    private final SocketAddress address;
    
    /**
     * The KeyStore containing the servers certificate and private key (May be null).
     */
    private final KeyStore keyStore;
    
    /**
     * The password for the provided key store (May be null).
     */
    private final String keyStorePassword;
    
    /**
     * The TCP backlog value.
     */
    private int backlog;

    /**
     * The TCP Socket. (If using TCP)
     */
    private SSLServerSocket tcpSocket;
    
    /**
     * The UDP Socket. (If using UDP)
     */
    private DatagramSocket udpSocket;

    /**
     * If the server is currently running.
     */
    private volatile boolean isRunning;

    /**
     * New Server using given KeyStore.
     * 
     * @param address {@link net.neto_framework.address.SocketAddress
     *                SocketAddress} for server to bind to.
     * @param keyStore The KeyStore containing the servers certificate and private key.
     * @param keyStorePassword The password for the given KeyStore.
     */
    public Server(SocketAddress address, KeyStore keyStore, String keyStorePassword) {
        
        try {
            Properties properties = new Properties();
            properties.load(ClassLoader.getSystemResourceAsStream(
                    NetoFramework.PROPERTIES_LOCATION));
            this.version = properties.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException("Failed to detect version of neto-framework.", e);
        }
        
        this.packetManager = new PacketManager();
        this.packetManager.registerPacket(HandshakePacket.class, new HandshakePacketHandler());
        this.packetManager.registerPacket(SuccessPacket.class);
        this.packetManager.registerPacket(DisconnectPacket.class, new DisconnectPacketHandler());
        
        this.tcpConnectionHandler = new ServerTCPConnectionHandler(this);
        this.udpConnectionHandler = new ServerUDPConnectionHandler(this);
        
        this.connectionManager = new ServerConnectionManager(this);
        this.eventHandler = new EventHandler();

        this.address = address;
        this.backlog = Server.DEFAULT_BACKLOG;
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
    }
    
    /**
     * New Server without a certificate (Vulnerable to man in the middle attacks).
     * 
     * @param address {@link net.neto_framework.address.SocketAddress
     *                SocketAddress} for server to bind to.
     */
    public Server(SocketAddress address) {
        this(address, null, null);
    }

    /**
     * Start accepting and listening for incoming connections.
     * 
     * @throws net.neto_framework.server.exceptions.ServerException if fails to
     *         start.
     */
    public void start() throws ServerException {
        //TODO: Document.
        
        if (!this.isRunning) {
            KeyManagerFactory keyManagerFactory = null;
            
            if(this.keyStore != null) {
                try {
                    keyManagerFactory = KeyManagerFactory.getInstance(
                            KeyManagerFactory.getDefaultAlgorithm());
                    keyManagerFactory.init(this.keyStore,
                            this.keyStorePassword.toCharArray());
                } catch (NoSuchAlgorithmException | KeyStoreException |
                        UnrecoverableKeyException e) {
                    throw new ServerException("Failed to start server with given KeyStore and"
                            + " password.", e);
                }
            }
            
            SSLContext context = null;
            try {
                context = SSLContext.getInstance("TLSv1.2");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Unkown SSL Context algorithm.", e);
            }
            
            try {
                if(this.keyStore != null) {
                    context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
                } else {
                    context.init(null, null, new SecureRandom());
                }
            } catch (KeyManagementException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {
                SSLServerSocketFactory factory = context.getServerSocketFactory();
                this.tcpSocket = (SSLServerSocket) factory.createServerSocket(
                        this.address.getPort(), this.backlog, this.address.getInetAddress());
                
                if(this.keyStore == null) {
                    this.tcpSocket.setEnabledCipherSuites(new String[] {
                        "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA"});
                }
            } catch (IOException e) {
                throw new ServerException("Failed to start server on given address.", e);
            }
            
            this.tcpSocket.setNeedClientAuth(false);

            try {
                this.udpSocket = new DatagramSocket(this.address.getPort(),
                        this.address.getInetAddress());
            } catch (SocketException e) {
                throw new ServerException("Failed to start server on given address.", e);
            }
            
            (new Thread(this.tcpConnectionHandler)).start();
            (new Thread(this.udpConnectionHandler)).start();
            this.isRunning = true;
        }
    }

    /**
     * Stop accepting and listening for incoming connections.
     * 
     * @throws net.neto_framework.server.exceptions.ServerException if fails to
     *         stop.
     */
    public void stop() throws ServerException {
        if (this.isRunning) {
            for(ClientConnection client : this.connectionManager.getClientConnections()) {
                client.disconnect();
            }
            
            this.isRunning = false;
            
            try {
                this.tcpSocket.close();
            } catch (IOException e) {
                throw new ServerException("Failed to close server socket.", e);
            }

            this.udpSocket.close();
        }
    }
    
    /**
     * @return he version of Neto-Framework the server is using loaded at runtime.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * @return {@link net.neto_framework.PacketManager PacketManager}.
     */
    public PacketManager getPacketManager() {
        return this.packetManager;
    }

    /**
     * @return {@link net.neto_framework.event.EventHandler EventHandler}.
     */
    public EventHandler getEventHandler() {
        return this.eventHandler;
    }

    /**
     * @return {@link net.neto_framework.address.SocketAddress} the server is
     *         /will bind to.
     */
    public SocketAddress getAddress() {
        return this.address;
    }

    /**
     * @return TCP Backlog value.
     */
    public int getBacklog() {
        return this.backlog;
    }
    
    /**
     * @param backlog TCP backlog value. (Must be set before server is started).
     */
    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    /**
     * @return TCP Server Socket. (Null if not using TCP as protocol or if the
     *         server has not been started.)
     */
    public ServerSocket getTcpSocket() {
        return this.tcpSocket;
    }

    /**
     * @return UDP Socket. (Null if not using UDP as protocol or if the server
     *         has not been started.)
     */
    public DatagramSocket getUdpSocket() {
        return this.udpSocket;
    }

    /**
     * @return If server is currently running.
     */
    public boolean isRunning() {
        return this.isRunning;
    }

    /**
     * @return {@link net.neto_framework.server.ServerConnectionManager
     * ServerConnectionManager} of this server.
     */
    public ServerConnectionManager getConnectionManager() {
        return this.connectionManager;
    }
}
