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
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;

import net.neto_framework.Protocol;
import net.neto_framework.server.event.events.ServerFailedToAcceptConnection;
import net.neto_framework.server.event.events.ServerReceiveInvalidHandshake;
import net.neto_framework.server.exceptions.ConnectionException;

/**
 * A connection thread to handle all connections individually.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class Connection implements Runnable {
    private final Server server;

    private final int id;
    private final Protocol protocol;

    private final Socket socket;
    private final InetAddress address;
    private final int port;

    /**
     * New TCP Connection.
     * 
     * @param server
     *            Server.
     * @param id
     *            Connection ID.
     * @param socket
     *            TCP Socket.
     */
    public Connection(Server server, int id, Socket socket) {
        this.server = server;

        this.id = id;
        this.protocol = Protocol.TCP;

        this.socket = socket;
        this.address = null;
        this.port = 0;
    }

    /**
     * New UDP Connection.
     * 
     * @param server
     *            Server.
     * @param id
     *            Connection ID.
     * @param address
     *            UDP InetAddress.
     * @param port
     *            Port number.
     */
    public Connection(Server server, int id, InetAddress address, int port) {
        this.server = server;

        this.id = id;
        this.protocol = Protocol.UDP;

        this.socket = null;
        this.address = address;
        this.port = port;
    }

    public void run() {
        byte[] magicStringBuffer = ConnectionHandler.MAGIC_STRING.getBytes();

        if (this.protocol == Protocol.TCP) {
            try {
                byte[] buffer = new byte[ConnectionHandler.MAGIC_STRING
                        .getBytes().length];
                this.socket.getInputStream().read(buffer);

                if (!new String(buffer).equals(ConnectionHandler.MAGIC_STRING)) {
                    ServerReceiveInvalidHandshake event = new ServerReceiveInvalidHandshake(
                            this.server, this.address, buffer);
                    this.server.getEventHandler().callEvent(event);
                    return;
                }
            } catch (IOException e) {
                ConnectionException exception = new ConnectionException(
                        "I/O Error when trying to read a TCP handshake packet.",
                        e);
                ServerFailedToAcceptConnection event = new ServerFailedToAcceptConnection(
                        this.server, this.server.getProtocol(), exception);
                this.server.getEventHandler().callEvent(event);
                return;
            }

            try {
                this.socket.getOutputStream().write(magicStringBuffer);
            } catch (IOException e) {
                ConnectionException exception = new ConnectionException(
                        "I/O Error when trying to send a TCP handshake packet.",
                        e);
                ServerFailedToAcceptConnection event = new ServerFailedToAcceptConnection(
                        this.server, this.server.getProtocol(), exception);
                this.server.getEventHandler().callEvent(event);
                return;
            }
        } else if (this.protocol == Protocol.UDP) {
            DatagramPacket idPacket = new DatagramPacket(magicStringBuffer,
                    magicStringBuffer.length, this.address, this.port);
            try {
                this.server.getUdpSocket().send(idPacket);
            } catch (IOException e) {
                ConnectionException exception = new ConnectionException(
                        "I/O Error when trying to send a UDP handshake packet.",
                        e);
                ServerFailedToAcceptConnection event = new ServerFailedToAcceptConnection(
                        this.server, this.server.getProtocol(), exception);
                this.server.getEventHandler().callEvent(event);
                return;
            }
        }

        while (this.server.isRunning()) {
            if (this.protocol == Protocol.TCP) {

            } else if (this.protocol == Protocol.UDP) {

            }
        }
    }

    /**
     * @return Connection ID.
     */
    public synchronized int getID() {
        return this.id;
    }

    /**
     * @return Protocol.
     */
    public synchronized Protocol getProtocol() {
        return this.protocol;
    }

    /**
     * @return Socket. (Null if protocol is UDP)
     */
    public synchronized Socket getSocket() {
        return this.socket;
    }

    /**
     * @return InetAddress. (Null if protocol is TCP)
     */
    public synchronized InetAddress getAddress() {
        return this.address;
    }

    /**
     * @return Port. (Null if protocol is TCP)
     */
    public synchronized int getPort() {
        return this.port;
    }
}
