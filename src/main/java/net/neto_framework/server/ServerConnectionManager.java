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
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import net.neto_framework.Connection;
import net.neto_framework.server.event.events.ClientConnectEvent;
import net.neto_framework.server.event.events.ClientFailedToConnectEvent;
import net.neto_framework.server.exceptions.ConnectionException;

/**
 * A manager to take care of all client connections.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerConnectionManager {
    
    /**
     * The amount of time a client is allowed to complete the handshake process before getting
     * kicked.
     */
    public static int HANDSHAKE_TIMEOUT = 10000;
    
    /**
     * Running instance of {@link net.neto_framework.server.Server Server}.
     */
    private final Server server;
    
    /**
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     */
    public ServerConnectionManager(Server server) {
        this.server = server;
    }
    
    /**
     * A hashmap of all connected {@link net.neto_framework.server.ClientConnection
     * ClientConnections} and their UUID as keys.
     */
    private volatile HashMap<UUID, ClientConnection> connections = new HashMap<>();
    
    /**
     * A hashmap of all connected {@link net.neto_framework.server.ClientConnection
     * ClientConnections} in the handshake process.
     */
    private volatile HashMap<UUID, ClientConnection> pendingConnections = new HashMap<>();
    
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
        ClientConnection clientConnection = new ClientConnection(server, uuid,
                new Connection(socket));
        
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                clientConnection.disconnect(false);
                
                ConnectionException exception = new ConnectionException("Client took too long to "
                        + "complete handshake process.");
                ClientFailedToConnectEvent event = new ClientFailedToConnectEvent(
                        ServerConnectionManager.this.server, exception);
                ServerConnectionManager.this.server.getEventHandler().callEvent(event);
            }
        }, ServerConnectionManager.HANDSHAKE_TIMEOUT);
        clientConnection.setTimer(timer);
        
        this.pendingConnections.put(uuid, clientConnection);
        (new Thread(clientConnection)).start();

        return clientConnection;
    }
    
    /**
     * Called by a {@link net.neto_framework.packets.HandshakePacket HandshakePacket} when a client
     * has completed the handshake process.
     * 
     * @param uuid UUID of client.
     */
    public void onConnectionValidated(UUID uuid) {
        ClientConnection client = this.pendingConnections.get(uuid);
        this.connections.put(uuid, client);
        this.pendingConnections.remove(uuid);
        client.getTimer().cancel();
        
        ClientConnectEvent event = new ClientConnectEvent(this.server, client);
        this.server.getEventHandler().callEvent(event);
    }

    /**
     * Remove ClientConnection.
     * 
     * @param uuid UUID of ClientConnection.
     */
    public void removeClientConnection(UUID uuid) {
        this.connections.remove(uuid);
        this.pendingConnections.remove(uuid);
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
     * @return Connection. (May be null)
     */
    public ClientConnection getClientConnection(UUID uuid) {
        return this.connections.get(uuid);
    }
    
    /**
     * Get ClientConnection from given IP address.
     * 
     * @param address InetAddress
     * @return Connection. (May be null)
     */
    public ClientConnection getClientConnection(InetAddress address) {
        for(ClientConnection connection : this.connections.values()) {
            if(connection.getTCPConnection().getTCPSocket().getInetAddress().equals(address)) {
                return connection;
            }
        }
        
        return null;
    }
    
    /**
     * @return ArrayList.
     */
    public ArrayList<ClientConnection> getClientConnections() {
        ArrayList<ClientConnection> values = new ArrayList<>();
        
        this.connections.values().stream().forEach((connection) -> {
            values.add(connection);
        });
        
        return values;
    }
}
