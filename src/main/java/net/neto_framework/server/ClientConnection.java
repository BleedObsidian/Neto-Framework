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
import java.util.UUID;

import net.neto_framework.Connection;
import net.neto_framework.Packet;
import net.neto_framework.PacketReceiver;
import net.neto_framework.Protocol;
import net.neto_framework.exceptions.PacketException;
import net.neto_framework.server.event.events.ServerClientConnect;
import net.neto_framework.server.event.events.ServerFailedToAcceptConnection;
import net.neto_framework.server.event.events.ServerInvalidPacket;
import net.neto_framework.server.event.events.ServerPacketException;
import net.neto_framework.server.event.events.ServerReceiveInvalidHandshake;
import net.neto_framework.server.exceptions.ConnectionException;

/**
 * A connection thread to handle all client connections individually.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ClientConnection implements Runnable {
    
    /**
     * Unique ID of ClientConnection.
     */
    private final UUID uuid;

    /**
     * Server.
     */
    private final Server server;
    
    /**
     * Connection of ClientConnection.
     */
    private final Connection connection;

    /**
     * New ClientConnection.
     * 
     * @param server
     *            Server.
     * @param uuid
     *            Unique ID of client.
     * @param connection
     *            Connection.
     */
    public ClientConnection(Server server, UUID uuid, Connection connection) {
        this.server = server;
        this.uuid = uuid;
        this.connection = connection;
    }

    /**
     * Thread run.
     */
    public void run() {
        byte[] magicStringBuffer = Connection.MAGIC_STRING.getBytes();

        if (this.server.getProtocol() == Protocol.TCP) {
            try {
                byte[] buffer = new byte[Connection.MAGIC_STRING
                        .getBytes().length];
                this.connection.getTCPSocket().getInputStream().read(buffer);

                if (!new String(buffer).equals(Connection.MAGIC_STRING)) {
                    ServerReceiveInvalidHandshake event = new ServerReceiveInvalidHandshake(
                            this.server, this.connection.getAddress(), buffer);
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
                this.connection.getTCPSocket().getOutputStream()
                        .write(magicStringBuffer);
            } catch (IOException e) {
                ConnectionException exception = new ConnectionException(
                        "I/O Error when trying to send a TCP handshake packet.",
                        e);
                ServerFailedToAcceptConnection event = new ServerFailedToAcceptConnection(
                        this.server, this.server.getProtocol(), exception);
                this.server.getEventHandler().callEvent(event);
                return;
            }
        } else if (this.server.getProtocol() == Protocol.UDP) {
            DatagramPacket idPacket = new DatagramPacket(magicStringBuffer,
                    magicStringBuffer.length, this.connection.getAddress(),
                    this.connection.getPort());
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

        this.server.getEventHandler().callEvent(
                new ServerClientConnect(this.server, this));

        while (this.server.isRunning()) {
            try {
                int packetID = this.connection.receiveInteger();

                if (this.server.getPacketManager().hasPacket(packetID)) {
                    try {
                        this.server.getPacketManager().receive(packetID,
                                this.connection, PacketReceiver.SERVER);
                    } catch (InstantiationException e) {
                        PacketException exception = new PacketException(
                                "Failed to create instance of packet.", e);
                        this.server.getEventHandler().callEvent(
                                new ServerPacketException(this.server,
                                        exception));
                    } catch (IllegalAccessException e) {
                        PacketException exception = new PacketException(
                                "Failed to create instance of packet.", e);
                        this.server.getEventHandler().callEvent(
                                new ServerPacketException(this.server,
                                        exception));
                    }
                } else {
                    PacketException exception = new PacketException(
                            "Invalid packet received.");
                    this.server.getEventHandler().callEvent(
                            new ServerInvalidPacket(this.server, packetID,
                                    exception));
                }
            } catch (IOException e) {
                PacketException exception = new PacketException(
                        "Failed to receive packet ID.", e);
                this.server.getEventHandler().callEvent(
                        new ServerPacketException(this.server, exception));
            }
        }
    }

    /**
     * Send client packet.
     * 
     * @param packet
     *            Packet.
     * @throws IOException
     *             If fails to send packet.
     */
    public void sendPacket(Packet packet) throws IOException {
        this.connection.sendInteger(packet.getID());
        packet.send(this.connection);
    }

    /**
     * @return Client ID.
     */
    public UUID getClientID() {
        return this.uuid;
    }

    /**
     * @return Connection.
     */
    public Connection getConnection() {
        return this.connection;
    }
}
