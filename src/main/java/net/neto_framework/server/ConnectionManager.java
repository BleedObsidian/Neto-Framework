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

import net.neto_framework.Connection;

/**
 * A manager to take care of all connections via tcp/udp.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ConnectionManager {
    private int idPool = 0;

    private volatile HashMap<Integer, ClientConnection> connections = new HashMap<Integer, ClientConnection>();

    /**
     * Add given TCP connection into pool.
     * 
     * @param server
     *            Server.
     * @param socket
     *            Socket of new TCP connection.
     * @return Unique connection ID.
     */
    public int addConnection(Server server, Socket socket) {
        int id = this.idPool++;
        ClientConnection connection = new ClientConnection(server, id,
                new Connection(socket));
        this.connections.put(id, connection);
        (new Thread(connection)).start();

        return this.idPool;
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
     * @return Unique connection ID.
     */
    public int addConnection(Server server, InetAddress address, int port) {
        int id = this.idPool++;
        ClientConnection connection = new ClientConnection(server, id,
                new Connection(server.getUdpSocket(), address, port));
        this.connections.put(id, connection);
        (new Thread(connection)).start();

        return this.idPool;
    }

    /**
     * Remove connection.
     * 
     * @param id
     *            Unique ID of connection.
     */
    public void removeConnection(int id) {
        this.connections.remove(id);
    }

    /**
     * If connection pool contains a connection with the given ID.
     * 
     * @param id
     *            Connection ID.
     * @return If connection exists.
     */
    public boolean hasConnection(int id) {
        return this.connections.containsKey(id) ? true : false;
    }

    /**
     * Get Connection handle for connection with given ID.
     * 
     * @param id
     *            Connection ID.
     * @return Connection.
     */
    public ClientConnection getConnection(int id) {
        return this.connections.get(id);
    }
}
