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

package net.neto_framework.server.event.events;

import net.neto_framework.server.ClientConnection;
import net.neto_framework.server.Server;
import net.neto_framework.server.event.ServerEvent;

/**
 * An event that is fired when a new client successfully connects to the server.
 * <p>
 * This event contains the new {@link net.neto_framework.server.ClientConnection
 * ClientConnection} that can be used to send packets.
 * 
 * @author BleedObsidian (Jesse Precott)
 */
public class ClientConnectEvent extends ServerEvent {
    
    /**
     * The new ClientConnection.
     */
    private final ClientConnection connection;

    /**
     * @param server Running instance of {@link net.neto_framework.server.Server
     *               Server}.
     * @param connection The new {@link
     *                   net.neto_framework.server.ClientConnection
     *                   ClientConnection}.
     */
    public ClientConnectEvent(Server server, ClientConnection connection) {
        super(server, Event.CLIENT_CONNECT);

        this.connection = connection;
    }

    /**
     * @return The new {@link net.neto_framework.server.ClientConnection
     *         ClientConnection}.
     */
    public ClientConnection getClientConnection() {
        return this.connection;
    }
}
