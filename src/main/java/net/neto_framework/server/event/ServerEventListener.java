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

import net.neto_framework.server.event.events.ServerFailedToAcceptConnection;
import net.neto_framework.server.event.events.ServerReceiveInvalidHandshake;

/**
 * An abstract class that allows actions to be taken when specified events are
 * called on the server.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public abstract class ServerEventListener {
    /**
     * Fired when the server failes to accept a TCP or UDP connection.
     * 
     * @param event
     *            ServerFaildToAcceptConnection event.
     */
    public void onServerFailedToAcceptConnection(
            ServerFailedToAcceptConnection event) {
        return;
    }

    /**
     * Fired when the server received an invalid data in the handshake packet.
     * 
     * @param event
     *            ServerReceiveInvalidHandshake event.
     */
    public void onServerReceiveInvalidHandshake(
            ServerReceiveInvalidHandshake event) {

    }
}
