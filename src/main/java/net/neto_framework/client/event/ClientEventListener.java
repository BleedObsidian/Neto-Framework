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

package net.neto_framework.client.event;

import net.neto_framework.client.event.events.DisconnectEvent;
import net.neto_framework.client.event.events.PacketExceptionEvent;

/**
 * An abstract class that allows actions to be taken when specified events are called on the client.
 * 
 * @author BleedObsidian (Jesse Prescott)
 */
public abstract class ClientEventListener {

    /**
     * Fired when the client is disconnected from the server. Reasons include IOExceptions,
     * receiving disconnect packet and not receiving keep alive from server.
     * 
     * @param event {@link net.neto_framework.client.event.events.DisconnectEvent DisconnectEvent}.
     */
    public void onDisconnect(DisconnectEvent event) {
    }

    /**
     * Fired when the client has a problem receiving or sending a packet.
     * 
     * @param event {@link net.neto_framework.client.event.events.PacketExceptionEvent
     *              PacketExceptionEvent}.
     */
    public void onPacketException(PacketExceptionEvent event) {
    }
}
