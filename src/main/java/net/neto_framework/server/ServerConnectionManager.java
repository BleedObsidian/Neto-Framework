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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import net.neto_framework.Connection;

/**
 * A manager to take care of all client connections.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerConnectionManager {
    
    /**
     * A hashmap of all connected {@link 
     * net.neto_framework.server.ClientConnection ClientConnections} and their
     * UUID as keys.
     */
    private volatile HashMap<UUID, ClientConnection> connections = 
            new HashMap<>();

    /**
     * Add given clientConnection into pool.
     * 
     * @param server Running instance of {@link net.neto_framework.server.Server
     *               Server}.
     * @param connection The {@link net.neto_framework.Connection Connection}.
     * @return {@link net.neto_framework.server.ClientConnection ClientConnection}.
     */
    public ClientConnection addClientConnection(Server server, Connection connection) {
        UUID uuid = UUID.randomUUID();
        ClientConnection clientConnection = new ClientConnection(server, uuid, connection);
        this.connections.put(uuid, clientConnection);
        (new Thread(clientConnection)).start();

        return clientConnection;
    }
    
    /**
     * Add given TCP clientConnection into pool.
     * 
     * @param server Running instance of {@link net.neto_framework.server.Server
     *               Server}.
     * @param socket TCP socket used to communicate with client.
     * @return {@link net.neto_framework.server.ClientConnection ClientConnection}.
     */
    public ClientConnection addClientConnection(Server server, Socket socket) {
        UUID uuid = UUID.randomUUID();
        ClientConnection clientConnection = new ClientConnection(server, uuid, new Connection(
                socket));
        this.connections.put(uuid, clientConnection);
        (new Thread(clientConnection)).start();

        return clientConnection;
    }

    /**
     * Add given UDP clientConnection into pool.
     * 
     * @param server Running instance of {@link net.neto_framework.server.Server
     *               Server}.
     * @param address InetAddress of new UDP clientConnection.
     * @param port Port number of new UDP clientConnection.
     * @return {@link net.neto_framework.server.ClientConnection ClientConnection}.
     */
    public ClientConnection addClientConnection(Server server, InetAddress address, int port) {
        UUID uuid = UUID.randomUUID();
        ClientConnection clientConnection = new ClientConnection(server, uuid, new Connection(
                server.getUdpSocket(), address, port));
        this.connections.put(uuid, clientConnection);
        (new Thread(clientConnection)).start();

        return clientConnection;
    }

    /**
     * Remove ClientConnection.
     * 
     * @param uuid UUID of ClientConnection.
     */
    public void removeClientConnection(UUID uuid) {
        this.connections.remove(uuid);
    }

    /**
     * If clientConnection pool contains a ClientConnection with the given UUID.
     * 
     * @param uuid UUID of client.
     * @return If ClientConnection exists.
     */
    public boolean hasClientConnection(UUID uuid) {
        return this.connections.containsKey(uuid);
    }

    /**
     * Get ClientConnection from given UUID.
     * 
     * @param uuid UUID.
     * @return Connection.
     */
    public ClientConnection getClientConnection(UUID uuid) {
        return this.connections.get(uuid);
    }
    
    /**
     * @return ArrayList<ClientConnection>.
     */
    public ArrayList<ClientConnection> getClientConnections() {
        ArrayList<ClientConnection> values = new ArrayList<>();
        
        this.connections.values().stream().forEach((connection) -> {
            values.add(connection);
        });
        
        return values;
    }
}
