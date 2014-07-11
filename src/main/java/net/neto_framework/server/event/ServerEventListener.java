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

import net.neto_framework.server.event.events.ServerClientConnect;
import net.neto_framework.server.event.events.ServerFailedToAcceptConnection;
import net.neto_framework.server.event.events.ServerInvalidPacket;
import net.neto_framework.server.event.events.ServerPacketException;
import net.neto_framework.server.event.events.ServerReceiveInvalidHandshake;
import net.neto_framework.server.event.events.ServerStart;
import net.neto_framework.server.event.events.ServerStop;

/**
 * An abstract class that allows actions to be taken when specified events are
 * called on the server.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public abstract class ServerEventListener {
    /**
     * Fired when the server fails to accept a TCP or UDP connection.
     * 
     * @param event
     *            ServerFaildToAcceptConnection event.
     */
    public void onServerFailedToAcceptConnection(
            ServerFailedToAcceptConnection event) {
        
    }

    /**
     * Fired when the server received invalid data in the handshake packet.
     * 
     * @param event
     *            ServerReceiveInvalidHandshake event.
     */
    public void onServerReceiveInvalidHandshake(
            ServerReceiveInvalidHandshake event) {

    }

    /**
     * Fired when a new client successfully connects to the server.
     * 
     * @param event
     *            ServerClientConnect.
     */
    public void onServerClientConnect(ServerClientConnect event) {

    }

    /**
     * Fired when the server starts.
     * 
     * @param event
     *            ServerStart event.
     */
    public void onServerStart(ServerStart event) {

    }

    /**
     * Fired when the server stops.
     * 
     * @param event
     *            ServerStop event.
     */
    public void onServerStop(ServerStop event) {

    }

    /**
     * Fired when the server receives an invalid packet id or packet.
     * 
     * @param event
     *            ServerInvalidPacket event.
     */
    public void onServerInvalidPacket(ServerInvalidPacket event) {

    }

    /**
     * Fired when the server has a problem receiving or sending a packet.
     * 
     * @param event
     *            ServerPacketException.
     */
    public void onServerPacketException(ServerPacketException event) {

    }
}
