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

package net.neto_framework.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import net.neto_framework.Connection;
import net.neto_framework.PacketManager;
import net.neto_framework.Protocol;
import net.neto_framework.address.SocketAddress;
import net.neto_framework.client.event.events.DisconnectEvent;
import net.neto_framework.client.event.events.DisconnectEvent.DisconnectReason;
import net.neto_framework.client.event.events.PacketExceptionEvent;
import net.neto_framework.client.exceptions.ClientConnectException;
import net.neto_framework.client.packets.handlers.DisconnectPacketHandler;
import net.neto_framework.client.packets.handlers.SuccessPacketHandler;
import net.neto_framework.event.EventHandler;
import net.neto_framework.exceptions.PacketException;
import net.neto_framework.packets.DisconnectPacket;
import net.neto_framework.packets.HandshakePacket;
import net.neto_framework.packets.SuccessPacket;
import net.neto_framework.server.Server;
import net.neto_framework.utils.NetoFramework;

/**
 * A client handler that can connect to a TCP or UDP server.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class Client {

    /**
     * The version of Neto-Framework the client is using loaded at runtime.
     */
    private final String version;
    
    /**
     * The {@link net.neto_framework.PacketManager PacketManager}.
     */
    private final PacketManager packetManager;
            
    /**
     * The {@link net.neto_framework.event.EventHandler EventHandler}.
     */
    private final EventHandler eventHandler;

    /**
     * The {@link net.neto_framework.address.SocketAddress} to connect to.
     */
    private final SocketAddress address;
    
    /**
     * Timer used to stop handshake process from hanging.
     */
    private final Timer timer;
    
    /**
     * The KeyStore containing the servers certificate and private key (May be null).
     */
    private final KeyStore keyStore;

    /**
     * TCP Socket.
     */
    private SSLSocket tcpSocket;
    
    /**
     * UDP Socket.
     */
    private DatagramSocket udpSocket;

    /**
     * {@link net.neto_framework.client.ServerConnection ServerConnection}.
     */
    private ServerConnection serverConnection;

    /**
     * If client is connected to a Server.
     */
    private volatile boolean isConnected;
    
     /**
     * If client has completed the handshake process with the server.
     */
    private volatile boolean isHandshakeComplete;
    
    /**
     * Used to store the ClientConnectException if one occurs during the handshake process.
     */
    private volatile ClientConnectException handshakeException;
    
    /**
     * The secret key used to encrypt/decrypt UDP packets.
     */
    private SecretKey secretKey;
    
    /**
     * The IV parameter spec used for ciphering.
     */
    private IvParameterSpec ivParameterSpec;
    
    /**
     * UUID given by server.
     */
    private UUID uuid;

    /**
     * New client using server authentication.
     * 
     * @param address The {@link net.neto_framework.address.SocketAddress SocketAddress} to connect
     *                to.
     * @param keyStore The KeyStore containing the servers certificate and NOT a private key.
     */
    public Client(SocketAddress address, KeyStore keyStore) {
        
        try {
            Properties properties = new Properties();
            properties.load(ClassLoader.getSystemResourceAsStream(
                    NetoFramework.PROPERTIES_LOCATION));
            this.version = properties.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException("Failed to detect version of neto-framework.", e);
        }
        
        this.packetManager = new PacketManager();
        this.packetManager.registerPacket(HandshakePacket.class);
        this.packetManager.registerPacket(SuccessPacket.class, new SuccessPacketHandler());
        this.packetManager.registerPacket(DisconnectPacket.class, new DisconnectPacketHandler());
        
        this.eventHandler = new EventHandler();
        this.address = address;
        this.timer = new Timer();
        
        this.keyStore = keyStore;
    }
    
    /**
     * New client not using server authentication (Vulnerable to man in the middle attacks).
     * 
     * @param address The {@link net.neto_framework.address.SocketAddress SocketAddress} to connect
     *                to.
     */
    public Client(SocketAddress address) {
        this(address, null);
    }

    /**
     * Attempt to connect to server.
     * 
     * @throws net.neto_framework.client.exceptions.ClientConnectException If failed.
     */
    public void connect() throws ClientConnectException {
        if (!this.isConnected) {
            TrustManagerFactory trustManagerFactory = null;
            
            if(this.keyStore != null) {
                try {
                    trustManagerFactory = TrustManagerFactory.getInstance(
                            TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init(this.keyStore);
                } catch (NoSuchAlgorithmException | KeyStoreException e) {
                    throw new ClientConnectException("Failed to connect to server with given"
                            + " KeyStore and password.", e);
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
                    context.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
                } else {
                    context.init(null, null, new SecureRandom());
                }
            } catch (KeyManagementException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {
                SSLSocketFactory factory = context.getSocketFactory();
                this.tcpSocket = (SSLSocket) factory.createSocket(this.address.getInetAddress(),
                        this.address.getPort());
                
                if(this.keyStore == null) {
                    this.tcpSocket.setEnabledCipherSuites(new String[] {
                        "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA"});
                }
            } catch (IOException e) {
                throw new ClientConnectException("Failed to start server on given address.", e);
            }
            
            this.tcpSocket.setUseClientMode(true);
            
            try {
                this.tcpSocket.startHandshake();
            } catch (IOException e) {
                throw new ClientConnectException("Failed to connect to given SocketAddress. ", e);
            }
            
            this.isConnected = true;

            try {
                this.udpSocket = new DatagramSocket();
            } catch (SocketException e) {
                this.disconnect(false);
                throw new ClientConnectException("Failed to create UDP socket.", e);
            }

            Connection tcpConnection = new Connection(this.tcpSocket);
            Connection udpConnection = new Connection(this.udpSocket,
                    this.address.getInetAddress(), this.address.getPort());

            this.serverConnection = new ServerConnection(this, tcpConnection, udpConnection);
            (new Thread(this.serverConnection)).start();
            
            try {
                HandshakePacket packet = new HandshakePacket();
                packet.setClientVersion(this.version);
                packet.setListeningUdpPort(this.udpSocket.getLocalPort());
                this.serverConnection.sendPacket(packet, Protocol.TCP);
            } catch (IOException e) {
                this.disconnect(false);
                throw new ClientConnectException("Failed to send handshake packet.", e);
            }
            
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ClientConnectException exception = new ClientConnectException("Server took too"
                            + " long to complete handshake process.");
                    Client.this.setHandshakeException(exception);
                    Client.this.disconnect(false);
                }
            }, Connection.HANDSHAKE_TIMEOUT);
            
            (new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("Neto-Framework Client UDP Handler");
                    
                    while(Client.this.isConnected) {
                        byte[] data = new byte[65508];
                        DatagramPacket dataPacket = new DatagramPacket(data, data.length);

                        try {
                            Client.this.udpSocket.receive(dataPacket);

                             // Trim data
                            int i = data.length - 1;
                            while (i >= 0 && data[i] == 0) {
                                --i;
                            }

                            data = Arrays.copyOf(data, i + 1);
                            data = Base64.getDecoder().decode(data);
                            
                            Connection connection = new Connection(udpSocket,
                                    dataPacket.getAddress(), dataPacket.getPort());
                            connection.enableEncryption(Client.this.secretKey,
                                    Client.this.ivParameterSpec);
                            
                            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                            connection.setUdpDataInputStream(inputStream);
                            int packetId = connection.receiveInteger();
                            UUID uuid = UUID.fromString(connection.receiveString());
                            long timestamp = connection.receiveLong();

                            if(!Client.this.packetManager.hasPacket(packetId)) {
                                PacketException exception = new PacketException("Unkown UDP packet"
                                        + " received.");
                                PacketExceptionEvent event = new PacketExceptionEvent(Client.this,
                                        exception);
                                Client.this.eventHandler.callEvent(event);
                                
                                if(Client.this.isHandshakeComplete) {
                                    Client.this.disconnect();
                                    DisconnectEvent disconnectEvent =
                                            new DisconnectEvent(Client.this,
                                                    DisconnectReason.EXCEPTION, exception);
                                    Client.this.getEventHandler().callEvent(disconnectEvent);
                                } else {
                                    Client.this.disconnect(false);
                                }
                            }

                            if(Client.this.uuid != null | Client.this.uuid.equals(uuid)) {
                                Client.this.serverConnection.getUDPConnection().
                                        setUdpDataInputStream(inputStream);
                                
                                if((System.currentTimeMillis() - timestamp) > 
                                        Connection.REPLAY_WINDOW) {
                                    Client.this.packetManager.receive(Client.this, packetId,
                                            Client.this.serverConnection, Protocol.UDP, true);
                                } else {
                                   Client.this.packetManager.receive(Client.this, packetId,
                                            Client.this.serverConnection, Protocol.UDP, false); 
                                }
                            } else {
                                PacketException exception = new PacketException("UUID does not "
                                        + "match on received UDP packet.");
                                PacketExceptionEvent event = new PacketExceptionEvent(Client.this,
                                        exception);
                                Client.this.eventHandler.callEvent(event);
                                
                                if(Client.this.isHandshakeComplete) {
                                    Client.this.disconnect();
                                    DisconnectEvent disconnectEvent =
                                            new DisconnectEvent(Client.this,
                                                    DisconnectReason.EXCEPTION, exception);
                                    Client.this.getEventHandler().callEvent(disconnectEvent);
                                } else {
                                    Client.this.disconnect(false);
                                }
                            }
                        } catch (IOException e) {
                            if(!Client.this.udpSocket.isClosed()) {
                                PacketException exception = new PacketException("Failed to read UDP"
                                        + " packet.", e);
                                PacketExceptionEvent event = new PacketExceptionEvent(Client.this,
                                        exception);
                                Client.this.eventHandler.callEvent(event);
                            }
                        }
                    }
                }
            })).start();
            
            while(!this.isHandshakeComplete) {
                // Block until handshake is completed.
                
                // Check if handshake process failed.
                if(this.handshakeException != null) {
                    throw new ClientConnectException("Failed to complete handshake process.",
                            this.handshakeException);
                }
            }
        }
    }
    
    /**
     * Disconnect from the server.
     */
    public void dissconnect() {
        this.disconnect(true);
    }

    /**
     * Disconnect from the server.
     * 
     * @param sendDisconnectPacket If true, sends a disconnect packet to the server before closing.
     */
    public void disconnect(boolean sendDisconnectPacket) {
        if (this.isConnected) {
            if(sendDisconnectPacket) {
                try {
                    this.serverConnection.sendPacket(new DisconnectPacket(), Protocol.TCP);
                } catch (IOException e) {} //TODO: Log
            }
            
            this.isConnected = false;
            
            try {
                this.tcpSocket.close();
            } catch (IOException e) {} //TODO: Log

            if(this.udpSocket != null) {
                this.udpSocket.close();
            }
        }
    }
    
    /**
     * Disconnect from the server.
     */
    public void disconnect() {
        this.disconnect(true);
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
     * @return {@link net.neto_framework.address.SocketAddress} the client is/will connect to.
     */
    public SocketAddress getAddress() {
        return this.address;
    }
    
    /**
     * @return Timer used to stop handshake process from hanging.
     */
    public Timer getTimer() {
        return this.timer;
    }

    /**
     * @return TCP Socket. (Null if not using TCP as protocol or if the client
     *         is not connected.)
     */
    public Socket getTcpSocket() {
        return this.tcpSocket;
    }

    /**
     * @return UDP Socket. (Null if not using UDP as protocol or if the client
     *         is not connected.)
     */
    public DatagramSocket getUdpSocket() {
        return this.udpSocket;
    }

    /**
     * @return {@link net.neto_framework.client.ServerConnection ServerConnection}.
     */
    public ServerConnection getServerConnection() {
        return this.serverConnection;
    }

    /**
     * @return If client is currently connected to a server.
     */
    public boolean isConnected() {
        return this.isConnected;
    }
    
    /**
     * @return If handshake completed.
     */
    public boolean isHandshakeCompleted() {
        return this.isHandshakeComplete;
    }
    
    /**
     * @param value If handshake completed.
     */
    public void setHandshakeCompleted(boolean value) {
        this.isHandshakeComplete = value;
    }
    
    /**
     * @param exception The ClientConnectException that occurred during the handshake process.
     */
    public void setHandshakeException(ClientConnectException exception) {
        this.handshakeException = exception;
    }
    
    /**
     * @return The secret key used to encrypt/decrypt packets after encryption response.
     */
    public SecretKey getSecretKey() {
        return this.secretKey;
    }
    
    /**
     * @param secretKey The secret key used to encrypt/decrypt packets after encryption response.
     */
    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }
    
    /**
     * @return The IV parameter spec used for ciphering.
     */
    public IvParameterSpec getIvParameterSpec() {
        return this.ivParameterSpec;
    }
    
    /**
     * @param ivParameterSpec The IV parameter spec used for ciphering.
     */
    public void setIvParameterSpec(IvParameterSpec ivParameterSpec) {
        this.ivParameterSpec = ivParameterSpec;
    }
    
    /**
     * @return UUID given by server.
     */
    public UUID getUUID() {
        return this.uuid;
    }
    
    /**
     * @param uuid UUID.
     */
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }
}
