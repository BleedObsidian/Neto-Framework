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

package net.neto_framework;

import java.io.IOException;
import net.neto_framework.client.Client;
import net.neto_framework.client.ServerConnection;
import net.neto_framework.server.ClientConnection;
import net.neto_framework.server.Server;

/**
 * A packet interface that can be sent and received to and from connections.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public interface Packet {
    /**
     * Send packet to connection.
     * 
     * @param connection {@link net.neto_framework.Connection Connection}.
     * @throws java.io.IOException
     */
    public void send(Connection connection) throws IOException;
    
    /**
     * Receive packet from connection.
     * 
     * @param connection {@link net.neto_framework.Connection Connection}.
     * @throws java.io.IOException
     */
    public void receive(Connection connection) throws IOException;

    /**
     * Fired when this packet is received server-side.
     * 
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     * @param clientConnection The {@link net.neto_framework.server.ClientConnection
     *                         ClientConnection}.
     * @param packet The {@link net.neto_framework.Packet Packet} instance.
     */
    public void onServerReceive(Server server, ClientConnection clientConnection, Packet packet);
    
    /**
     * Fired when this packet is received client-side.
     * 
     * @param client Running instance of {@link net.neto_framework.client.Client Client}.
     * @param serverConnection The {@link net.neto_framework.client.ServerConnection
     *                         ServerConnection}.
     * @param packet The {@link net.neto_framework.Packet Packet} instance.
     */
    public void onClientReceive(Client client, ServerConnection serverConnection, Packet packet);

    /**
     * @return Unique packet ID.
     */
    public int getId();
}
