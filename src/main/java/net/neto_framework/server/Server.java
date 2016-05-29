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
import net.neto_framework.PacketManager;
import net.neto_framework.Protocol;
import net.neto_framework.address.SocketAddress;
import net.neto_framework.event.EventHandler;
import net.neto_framework.server.exceptions.ServerException;

/**
 * A server handler that receives and accepts connections using a given
 * protocol.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class Server {
    /**
     * Default backlog value for TCP servers.
     */
    public static final int DEFAULT_BACKLOG = 50;

    /**
     * Default protocol used when a protocol is not given.
     */
    public static final Protocol DEFAULT_PROTOCOL = Protocol.TCP;

    /**
     * The {@link net.neto_framework.PacketManager PacketManager}.
     */
    private final PacketManager packetManager;

    /**
     * The {@link net.neto_framework.server.ServerConnectionHandler 
     * ServerConnectionHandler}.
     */
    private final ServerConnectionHandler connectionHandler;
    
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
     * The {@link net.neto_framework.Protocol Protocol} the server is using.
     */
    private final Protocol protocol;
    
    /**
     * The TCP backlog value.
     */
    private final int backlog;

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
    private boolean isRunning;

    /**
     * @param packetManager The {@link net.neto_framework.PacketManager
     *                      PacketManager}.
     * @param address {@link net.neto_framework.address.SocketAddress
     *                SocketAddress} for server to bind to.
     * @param protocol {@link net.neto_framework.Protocol Protocol} for server
     *                 to use.
     * @param backlog Maximum amount of connections to queue for TCP.
     */
    public Server(PacketManager packetManager, SocketAddress address,
            Protocol protocol, int backlog) {
        this.packetManager = new PacketManager();
        this.connectionHandler = new ServerConnectionHandler(this);
        this.connectionManager = new ServerConnectionManager();

        this.eventHandler = new EventHandler();

        this.address = address;
        this.protocol = protocol;
        this.backlog = backlog;
        //TODO: Only choose protocol when sending a packet. Only do handhshake process in TCP.
    }

    /**
     * @param packetManager The {@link net.neto_framework.PacketManager
     *                      PacketManager}.
     * @param address {@link net.neto_framework.address.SocketAddress
     *                SocketAddress} for server to bind to.
     * @param protocol {@link net.neto_framework.Protocol Protocol} for server
     *                 to use.
     */
    public Server(PacketManager packetManager, SocketAddress address,
            Protocol protocol) {
        this.packetManager = packetManager;
        this.connectionHandler = new ServerConnectionHandler(this);
        this.connectionManager = new ServerConnectionManager();

        this.eventHandler = new EventHandler();

        this.address = address;
        this.protocol = protocol;
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
            if (this.protocol == Protocol.TCP) {
                try {
                    this.tcpSocket = new ServerSocket(this.address.getPort(),
                            this.backlog, this.address.getInetAddress());

                    (new Thread(this.connectionHandler)).start();

                    this.isRunning = true;
                } catch (IOException e) {
                    throw new ServerException(
                            "Failed to start server on given address.", e);
                }
            } else if (this.protocol == Protocol.UDP) {
                try {
                    this.udpSocket = new DatagramSocket(this.address.getPort(),
                            this.address.getInetAddress());

                    (new Thread(this.connectionHandler)).start();

                    this.isRunning = true;
                } catch (SocketException e) {
                    throw new ServerException(
                            "Failed to start server on given address.", e);
                }
            }
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
            if (this.protocol == Protocol.TCP) {
                try {
                    this.tcpSocket.close();

                    this.isRunning = false;
                } catch (IOException e) {
                    throw new ServerException("Failed to close server socket.",
                            e);
                }
            } else if (this.protocol == Protocol.UDP) {
                this.udpSocket.close();

                this.isRunning = false;
            }
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
     * @return {@link net.neto_framework.Protocol Protocol} the server is using.
     */
    public synchronized Protocol getProtocol() {
        return this.protocol;
    }

    /**
     * @return TCP Backlog value.
     */
    public int getBacklog() {
        return this.backlog;
    }

    /**
     * @return TCP Server Socket. (Null if not using TCP as protocol or if the
     *         server has not been started.)
     */
    public synchronized ServerSocket getTcpSocket() {
        return this.tcpSocket;
    }

    /**
     * @return UDP Socket. (Null if not using UDP as protocol or if the server
     *         has not been started.)
     */
    public synchronized DatagramSocket getUdpSocket() {
        return this.udpSocket;
    }

    /**
     * @return If server is currently running.
     */
    public synchronized boolean isRunning() {
        return this.isRunning;
    }

    /**
     * @return {@link net.neto_framework.server.ServerConnectionManager
     * ServerConnectionManager} of this server.
     */
    public synchronized ServerConnectionManager getConnectionManager() {
        return this.connectionManager;
    }
}
