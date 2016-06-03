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

package net.neto_framework.packets;

import java.util.UUID;
import net.neto_framework.client.event.ClientEventListener;
import net.neto_framework.client.event.events.DisconnectEvent;
import net.neto_framework.client.event.events.PacketExceptionEvent;
import net.neto_framework.client.event.events.ReceivePacketEvent;
import net.neto_framework.exceptions.PacketException;

/**
 * A client event listener used to listen to built-in packets.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class NetoClientEventListener extends ClientEventListener {
    
    @Override
    public void onReceivePacket(ReceivePacketEvent event) {
        if(event.getPacket() instanceof HandshakeResponsePacket) {
            HandshakeResponsePacket packet = (HandshakeResponsePacket) event.getPacket();
            
            if(packet.getValue().equals(HandshakePacket.MAGIC_STRING)) {
                event.getClient().setUUID(UUID.fromString(packet.getUUID()));
            } else {
                PacketException exception = new PacketException("Incorrect magic string received.");
                PacketExceptionEvent packetEvent = new PacketExceptionEvent(event.getClient(),
                        exception);
                event.getClient().getEventHandler().callEvent(packetEvent);

                event.getClient().disconnect();
                DisconnectEvent disconnectEvent = new DisconnectEvent(event.getClient(),
                        DisconnectEvent.DisconnectReason.EXCEPTION, 
                        exception);
                event.getClient().getEventHandler().callEvent(disconnectEvent);
            }
        } else if(event.getPacket() instanceof DisconnectPacket) {
            event.getClient().disconnect(false);
            DisconnectEvent disconnectEvent = new DisconnectEvent(event.getClient(),
                    DisconnectEvent.DisconnectReason.DISCONNECT_PACKET);
            event.getClient().getEventHandler().callEvent(disconnectEvent);
        }
    }
}
