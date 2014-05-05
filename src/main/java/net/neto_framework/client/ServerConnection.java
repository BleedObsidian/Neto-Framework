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

import java.io.IOException;

import net.neto_framework.Connection;
import net.neto_framework.Packet;

/**
 * A connection thread to handle the server connection.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerConnection implements Runnable {
    private final Client client;
    private final Connection connection;

    /**
     * New ServerConnection.
     * 
     * @param client
     *            Client.
     * @param connection
     *            Connection.
     */
    public ServerConnection(Client client, Connection connection) {
        this.client = client;
        this.connection = connection;
    }

    public void run() {
        while (this.client.isConnected()) {
            try {
                int packetID = this.connection.receiveInteger();

                if (this.client.getPacketManager().hasPacket(packetID)) {
                    try {
                        this.client.getPacketManager().receive(packetID,
                                this.connection);
                    } catch (InstantiationException e) {

                    } catch (IllegalAccessException e) {

                    }
                } else {
                }
            } catch (IOException e) {

            }
        }
    }

    /**
     * Send server packet.
     * 
     * @param packet
     *            - Packet.
     * @throws IOException
     *             If fails to send packet.
     */
    public void sendPacket(Packet packet) throws IOException {
        this.connection.sendInteger(packet.getID());
        packet.send(this.connection);
    }
}
