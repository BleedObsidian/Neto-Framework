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
import net.neto_framework.server.event.events.ServerStart;
import net.neto_framework.server.event.events.ServerStop;
import net.neto_framework.server.exceptions.ServerException;

/**
 * A server handler that receives and accepts connections using a given
 * protocol.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class Server {
    /**
     * The default backlog value for TCP servers.
     */
    public static final int DEFAULT_BACKLOG = 50;

    /**
     * Default protocol used when a protocol is not given.
     */
    public static final Protocol DEFAULT_PROTOCOL = Protocol.TCP;

    /**
     * Server PacketManager.
     */
    private final PacketManager packetManager;

    /**
     * ServerConnectionHandler.
     */
    private final ServerConnectionHandler connectionHandler;
    
    /**
     * ServerConnectionManager.
     */
    private final ServerConnectionManager connectionManager;

    /**
     * Server EventHandler.
     */
    private final EventHandler eventHandler;

    /**
     * Server SocketAddress to run on.
     */
    private final SocketAddress address;
    
    /**
     * Server Protocol to use.
     */
    private final Protocol protocol;
    
    /**
     * Server backlog.
     */
    private final int backlog;

    /**
     * TCP Socket.
     */
    private ServerSocket tcpSocket;
    
    /**
     * UDP Socket.
     */
    private DatagramSocket udpSocket;

    /**
     * If server is running.
     */
    private boolean isRunning;

    /**
     * New server that binds to given address and uses a given protocol.
     * 
     * @param packetManager PacketManager.
     * @param address SocketAddress for server to bind to.
     * @param protocol Protocol for server to use.
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
    }

    /**
     * New server that binds to given address and uses a given protocol.
     * 
     * @param packetManager PacketManager.
     * @param address SocketAddress for server to bind to.
     * @param protocol Protocol for server to use.
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
     * New server that binds to given address and uses TCP.
     * 
     * @param packetManager PacketManager.
     * @param address SocketAddress for server to bind to.
     */
    public Server(PacketManager packetManager, SocketAddress address) {
        this.packetManager = packetManager;
        this.connectionHandler = new ServerConnectionHandler(this);
        this.connectionManager = new ServerConnectionManager();

        this.eventHandler = new EventHandler();

        this.address = address;
        this.protocol = Server.DEFAULT_PROTOCOL;
        this.backlog = Server.DEFAULT_BACKLOG;
    }

    /**
     * New server that is not bound to anything.
     * 
     * @param packetManager PacketManager.
     */
    public Server(PacketManager packetManager) {
        this.packetManager = packetManager;
        this.connectionHandler = new ServerConnectionHandler(this);
        this.connectionManager = new ServerConnectionManager();

        this.eventHandler = new EventHandler();

        this.address = null;
        this.protocol = Server.DEFAULT_PROTOCOL;
        this.backlog = Server.DEFAULT_BACKLOG;
    }

    /**
     * Start accepting and listening for incoming connections.
     * 
     * @throws ServerException If fails to start server.
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

            this.eventHandler.callEvent(new ServerStart(this));
        }
    }

    /**
     * Stop accepting and listening for incoming connections.
     * 
     * @throws ServerException If fails to stop server.
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

            this.eventHandler.callEvent(new ServerStop(this));
        }
    }

    /**
     * @return PacketManager.
     */
    public synchronized PacketManager getPacketManager() {
        return this.packetManager;
    }

    /**
     * @return EventHandler.
     */
    public synchronized EventHandler getEventHandler() {
        return this.eventHandler;
    }

    /**
     * @return Address that server is binding to.
     */
    public SocketAddress getAddress() {
        return this.address;
    }

    /**
     * @return Protocol the server is using.
     */
    public synchronized Protocol getProtocol() {
        return this.protocol;
    }

    /**
     * @return TCP Backlog.
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
     * @return ServerConnectionManager of this server.
     */
    public synchronized ServerConnectionManager getConnectionManager() {
        return this.connectionManager;
    }
}
