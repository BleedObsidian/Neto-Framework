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

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

import net.neto_framework.Connection;

/**
 * A manager to take care of all connections via tcp/udp.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerConnectionManager {
    
    /**
     * All ClientConnections.
     */
    private volatile HashMap<UUID, ClientConnection> connections = new HashMap<UUID, ClientConnection>();

    /**
     * Add given TCP connection into pool.
     * 
     * @param server
     *            Server.
     * @param socket
     *            Socket of new TCP connection.
     * @return Unique ID.
     */
    public UUID addConnection(Server server, Socket socket) {
        UUID uuid = UUID.randomUUID();
        ClientConnection connection = new ClientConnection(server, uuid,
                new Connection(socket));
        this.connections.put(uuid, connection);
        (new Thread(connection)).start();

        return uuid;
    }

    /**
     * Add given UDP connection into pool.
     * 
     * @param server
     *            Server.
     * @param address
     *            InetAddress of new UDP connection.
     * @param port
     *            Port number of new UDP connection.
     * @return Unique ID.
     */
    public UUID addConnection(Server server, InetAddress address, int port) {
        UUID uuid = UUID.randomUUID();
        ClientConnection connection = new ClientConnection(server, uuid,
                new Connection(server.getUdpSocket(), address, port));
        this.connections.put(uuid, connection);
        (new Thread(connection)).start();

        return uuid;
    }

    /**
     * Remove connection.
     * 
     * @param uuid
     *            Unique ID of connection.
     */
    public void removeConnection(UUID uuid) {
        this.connections.remove(uuid);
    }

    /**
     * If connection pool contains a connection with the given ID.
     * 
     * @param uuid
     *            Unique ID of connection.
     * @return If connection exists.
     */
    public boolean hasConnection(UUID uuid) {
        return this.connections.containsKey(uuid);
    }

    /**
     * Get Connection handle for connection with given ID.
     * 
     * @param uuid
     *            Unique ID of connection.
     * @return Connection.
     */
    public ClientConnection getConnection(UUID uuid) {
        return this.connections.get(uuid);
    }
}
