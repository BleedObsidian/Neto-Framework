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
import java.net.Socket;
import net.neto_framework.Connection;

import net.neto_framework.Protocol;
import net.neto_framework.server.event.events.ServerFailedToAcceptConnection;
import net.neto_framework.server.event.events.ServerReceiveInvalidHandshake;
import net.neto_framework.server.exceptions.ConnectionException;

/**
 * A connection handler that accepts TCP/UDP connections on a separate thread.
 * (Used internally)
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerConnectionHandler extends Thread {
    
    /**
     * Server.
     */
    private final Server server;

    /**
     * New ServerConnectionHandler.
     * 
     * @param server
     *            Server that is using this ServerConnectionHandler.
     */
    public ServerConnectionHandler(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (this.server.isRunning()) {
            if (this.server.getProtocol() == Protocol.TCP) {
                try {
                    Socket socket = this.server.getTcpSocket().accept();
                    this.server.getConnectionManager().addConnection(
                            this.server, socket);
                } catch (IOException e) {
                    ConnectionException exception = new ConnectionException(
                            "I/O Error when trying to accept a TCP socket connection.",
                            e);
                    ServerFailedToAcceptConnection event = new ServerFailedToAcceptConnection(
                            this.server, this.server.getProtocol(), exception);
                    this.server.getEventHandler().callEvent(event);
                }
            } else if (this.server.getProtocol() == Protocol.UDP) {
                byte[] buffer = new byte[Connection.MAGIC_STRING
                        .getBytes().length];
                DatagramPacket packet = new DatagramPacket(buffer,
                        buffer.length);

                try {
                    this.server.getUdpSocket().receive(packet);

                    if (new String(packet.getData())
                            .equals(Connection.MAGIC_STRING)) {
                        this.server.getConnectionManager().addConnection(
                                this.server, packet.getAddress(),
                                packet.getPort());
                    } else {
                        ServerReceiveInvalidHandshake event = new ServerReceiveInvalidHandshake(
                                this.server, packet.getAddress(),
                                packet.getData());
                        this.server.getEventHandler().callEvent(event);
                    }
                } catch (IOException e) {
                    ConnectionException exception = new ConnectionException(
                            "I/O Error when trying to read a UDP handshake packet.",
                            e);
                    ServerFailedToAcceptConnection event = new ServerFailedToAcceptConnection(
                            this.server, this.server.getProtocol(), exception);
                    this.server.getEventHandler().callEvent(event);
                }
            }
        }
    }
}