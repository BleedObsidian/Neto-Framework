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

package net.neto_framework.server.event;

import net.neto_framework.server.Server;

/**
 * A base class for all sever events.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerEvent {
    private final Server server;
    private final ServerEvents event;

    /**
     * New ServerEvent.
     * 
     * @param server
     *            Server.
     * @param event
     *            ServerEvents.
     */
    public ServerEvent(Server server, ServerEvents event) {
        this.server = server;
        this.event = event;
    }

    /**
     * @return Server.
     */
    public Server getServer() {
        return this.server;
    }

    /**
     * @return ServerEvents.
     */
    public ServerEvents getEvent() {
        return this.event;
    }
}
