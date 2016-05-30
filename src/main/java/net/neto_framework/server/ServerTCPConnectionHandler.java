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
import java.net.Socket;
import net.neto_framework.server.event.events.ClientFailedToConnectEvent;
import net.neto_framework.server.exceptions.ConnectionException;

/**
 * A connection handler that accepts TCP connections on a separate thread.
 *
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerTCPConnectionHandler extends Thread {

    /**
     * Running instance of Server.
     */
    private final Server server;

    /**
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     */
    public ServerTCPConnectionHandler(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (this.server.isRunning()) {
            try {
                Socket socket = this.server.getTcpSocket().accept();
                this.server.getConnectionManager().addClientConnection(this.server, socket);
            } catch (IOException e) {
                if(!this.server.getTcpSocket().isClosed()) {
                    ClientFailedToConnectEvent event = new ClientFailedToConnectEvent(this.server,
                                    new ConnectionException("I/O Error when accepting a TCP" + 
                                            " connection.", e));
                    this.server.getEventHandler().callEvent(event);
                }
            }
        }
    }
}
