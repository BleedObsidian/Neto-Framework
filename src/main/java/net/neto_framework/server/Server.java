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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import net.neto_framework.PacketManager;
import net.neto_framework.address.SocketAddress;
import net.neto_framework.event.EventHandler;
import net.neto_framework.packets.DisconnectPacket;
import net.neto_framework.packets.EncryptionRequestPacket;
import net.neto_framework.packets.EncryptionResponsePacket;
import net.neto_framework.packets.HandshakePacket;
import net.neto_framework.packets.NetoServerEventListener;
import net.neto_framework.packets.SuccessPacket;
import net.neto_framework.server.exceptions.ServerException;

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
     * Default key size for RSA key pair.
     */
    public static final int DEFAULT_KEYSIZE = 2048;

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
     * The public key used for encrypting shared secret.
     */
    private PublicKey publicKey;
    
    /**
     * The private key pair used for decrypting shared secret.
     */
    private PrivateKey privateKey;
    
    /**
     * The TCP backlog value.
     */
    private int backlog;

    /**
     * The TCP Socket. (If using TCP)
     */
    private ServerSocket tcpSocket;
    
    /**
     * The UDP Socket. (If using UDP)
     */
    private DatagramSocket udpSocket;

    /**
     * If the server is currently running.
     */
    private volatile boolean isRunning;

    /**
     * @param address {@link net.neto_framework.address.SocketAddress
     *                SocketAddress} for server to bind to.
     */
    public Server(SocketAddress address) {
        this.packetManager = new PacketManager();
        this.packetManager.registerPacket(HandshakePacket.class);
        this.packetManager.registerPacket(EncryptionRequestPacket.class);
        this.packetManager.registerPacket(EncryptionResponsePacket.class);
        this.packetManager.registerPacket(SuccessPacket.class);
        this.packetManager.registerPacket(DisconnectPacket.class);
        
        this.tcpConnectionHandler = new ServerTCPConnectionHandler(this);
        this.udpConnectionHandler = new ServerUDPConnectionHandler(this);
        
        this.connectionManager = new ServerConnectionManager(this);
        this.eventHandler = new EventHandler();
        this.eventHandler.registerServerEventListener(new NetoServerEventListener());

        this.address = address;
        this.backlog = Server.DEFAULT_BACKLOG;
    }

    /**
     * Start accepting and listening for incoming connections.
     * 
     * @throws net.neto_framework.server.exceptions.ServerException if fails to
     *         start.
     */
    public void start() throws ServerException {
        if (!this.isRunning) {
            try {
                this.tcpSocket = new ServerSocket(this.address.getPort(),
                        this.backlog, this.address.getInetAddress());
            } catch (IOException e) {
                throw new ServerException("Failed to start server on given address.", e);
            }

            try {
                this.udpSocket = new DatagramSocket(this.address.getPort(),
                        this.address.getInetAddress());
            } catch (SocketException e) {
                throw new ServerException("Failed to start server on given address.", e);
            }
            
            try {
                KeyPairGenerator keyPairGenerator= KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(Server.DEFAULT_KEYSIZE,
                        SecureRandom.getInstanceStrong());
                KeyPair keyPair = keyPairGenerator.generateKeyPair();
                this.publicKey = keyPair.getPublic();
                this.privateKey = keyPair.getPrivate();
            } catch (NoSuchAlgorithmException e) {
                throw new ServerException("Failed to start server due to unknown encryption"
                        + " algorithm.", e);
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
     * @return Elliptic Curve public key.
     */
    public PublicKey getPublicKey() {
        return this.publicKey;
    }
    
    /**
     * @return Elliptic Curve private key.
     */
    public PrivateKey getPrivateKey() {
        return this.privateKey;
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
