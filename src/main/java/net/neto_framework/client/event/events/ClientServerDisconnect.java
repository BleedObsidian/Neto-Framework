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

package net.neto_framework.client.event.events;

import net.neto_framework.client.Client;
import net.neto_framework.client.ServerConnection;
import net.neto_framework.client.event.ClientEvent;
import net.neto_framework.client.event.ClientEvents;

/**
 * An event that is fired when the client disconnects from a server.
 * 
 * @author BleedObsidian (Jesse Precott)
 */
public class ClientServerDisconnect extends ClientEvent {
    
    /**
     * ServerConnection that was disconnected.
     */
    private final ServerConnection connection;

    /**
     * New ClientServerDisconnect event.
     * 
     * @param client Client.
     * @param connection Old Connection.
     */
    public ClientServerDisconnect(Client client, ServerConnection connection) {
        super(client, ClientEvents.CLIENT_SERVER_DISCONNECT);

        this.connection = connection;
    }

    /**
     * @return Old Connection.
     */
    public ServerConnection getServerConnection() {
        return this.connection;
    }
}
