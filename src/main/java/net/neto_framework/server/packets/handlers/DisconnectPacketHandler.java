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

package net.neto_framework.server.packets.handlers;

import net.neto_framework.ServerPacketHandler;
import net.neto_framework.packets.DisconnectPacket;
import net.neto_framework.server.ClientConnection;
import net.neto_framework.server.Server;
import net.neto_framework.server.event.events.ClientDisconnectEvent;
import net.neto_framework.server.event.events.ClientDisconnectEvent.ClientDisconnectReason;

/**
 * A server-side packet handler for DisconnectPacket.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class DisconnectPacketHandler implements ServerPacketHandler<DisconnectPacket> {

    @Override
    public void onReceivePacket(Server server, ClientConnection client, DisconnectPacket packet) {
        client.disconnect(false);
        ClientDisconnectEvent disconnectEvent = new ClientDisconnectEvent(server, 
                ClientDisconnectReason.DISCONNECT_PACKET, client);
        server.getEventHandler().callEvent(disconnectEvent);
    }

}
