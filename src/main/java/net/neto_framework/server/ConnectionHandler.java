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

import net.neto_framework.Protocol;

/**
 * A connection handler that accepts TCP/UDP connections on a separate thread.
 * (Used internally)
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ConnectionHandler extends Thread {
    /**
     * A magic string that should be within a handshake packet.
     */
    public final static String MAGIC_STRING = "1293";

    private final Server server;

    /**
     * New ConnectionHandler.
     * 
     * @param server
     *            Server that is using this ConnectionHandler.
     */
    public ConnectionHandler(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (this.server.isRunning()) {
            if (this.server.getProtocol() == Protocol.TCP) {
                try {
                    Socket socket = this.server.getTcpSocket().accept();
                    this.server.getConnectionManager().addTcpConnection(socket);
                } catch (IOException e) {
                    /*
                     * (TODO: Pass to an event) throw new ConnectionException(
                     * "I/O Error when trying to accept a TCP socket connection."
                     * , e);
                     */
                }
            } else if (this.server.getProtocol() == Protocol.UDP) {
                byte[] buffer = new byte[ConnectionHandler.MAGIC_STRING
                        .getBytes().length];
                DatagramPacket packet = new DatagramPacket(buffer,
                        buffer.length);

                try {
                    this.server.getUdpSocket().receive(packet);

                    if (new String(packet.getData())
                            .equals(ConnectionHandler.MAGIC_STRING)) {
                        String id = String.valueOf(this.server
                                .getConnectionManager().addUdpConnection(
                                        packet.getAddress()));
                        byte[] idBuffer = id.getBytes();
                        DatagramPacket idPacket = new DatagramPacket(idBuffer,
                                idBuffer.length, packet.getAddress(),
                                packet.getPort());
                        this.server.getUdpSocket().send(idPacket);
                    } else {
                        /*
                         * (TODO: Pass to an event) UDP packet received but
                         * contains invalid Magic Number.
                         */
                    }
                } catch (IOException e) {
                    /*
                     * (TODO: Pass to an event) throw new ConnectionException(
                     * "I/O Error when trying to accept a UDP handshake packet."
                     * , e);
                     */
                }
            }
        }
    }
}
