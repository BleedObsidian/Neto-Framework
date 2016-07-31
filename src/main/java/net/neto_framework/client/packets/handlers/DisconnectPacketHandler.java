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

package net.neto_framework.client.packets.handlers;

import net.neto_framework.ClientPacketHandler;
import net.neto_framework.client.Client;
import net.neto_framework.client.event.events.DisconnectEvent;
import net.neto_framework.client.event.events.DisconnectEvent.DisconnectReason;
import net.neto_framework.packets.DisconnectPacket;

/**
 * A server-side packet handler for DisconnectPacket.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class DisconnectPacketHandler implements ClientPacketHandler<DisconnectPacket> {

    @Override
    public void onReceivePacket(Client client, DisconnectPacket packet) {
        client.disconnect(false);
        DisconnectEvent disconnectEvent = new DisconnectEvent(client,
                DisconnectReason.DISCONNECT_PACKET);
        client.getEventHandler().callEvent(disconnectEvent);
    }

}
