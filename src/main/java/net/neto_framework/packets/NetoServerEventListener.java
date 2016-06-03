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

import java.io.IOException;
import net.neto_framework.Connection;
import net.neto_framework.Protocol;
import net.neto_framework.exceptions.PacketException;
import net.neto_framework.server.event.ServerEventListener;
import net.neto_framework.server.event.events.ClientDisconnectEvent;
import net.neto_framework.server.event.events.PacketExceptionEvent;
import net.neto_framework.server.event.events.ReceivePacketEvent;

/**
 * A server event listener used to listen to built-in packets.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class NetoServerEventListener extends ServerEventListener {
    
    @Override
    public void onReceivePacket(ReceivePacketEvent event) {
        if(event.getPacket() instanceof HandshakePacket) {
            HandshakePacket packet = (HandshakePacket) event.getPacket();
            
            if(packet.getValue().equals(HandshakePacket.MAGIC_STRING)) {
                HandshakeResponsePacket response = new HandshakeResponsePacket();
                response.setUUID(event.getClientConnection().getUUID().toString());

                try {
                    event.getClientConnection().sendPacket(response, Protocol.TCP);
                } catch (IOException e) {
                    PacketException exception = new PacketException("Failed to send handshake "
                            + "reponse.", e);
                    PacketExceptionEvent packetEvent = new PacketExceptionEvent(event.getServer(),
                            exception);
                    event.getServer().getEventHandler().callEvent(packetEvent);

                    event.getClientConnection().disconnect();
                    ClientDisconnectEvent disconnectEvent = new ClientDisconnectEvent(
                            event.getServer(), 
                            ClientDisconnectEvent.ClientDisconnectReason.EXCEPTION,
                            event.getClientConnection().getUUID(), exception);
                    event.getServer().getEventHandler().callEvent(disconnectEvent);
                    return;
                }

                System.out.println("5");
                Connection udpConnection = new Connection(event.getServer().getUdpSocket(),
                        event.getClientConnection().getTCPConnection().getTCPSocket().
                                getInetAddress(), packet.getUdpPort());
                event.getClientConnection().addUdpConnection(udpConnection);

                event.getServer().getConnectionManager().onConnectionValidated(
                        event.getClientConnection().getUUID());
            } else {
                PacketException exception = new PacketException("Incorrect magic string received.");
                PacketExceptionEvent packetEvent = new PacketExceptionEvent(event.getServer(),
                        exception);
                event.getServer().getEventHandler().callEvent(packetEvent);

                event.getClientConnection().disconnect();
                ClientDisconnectEvent disconnectEvent = new ClientDisconnectEvent(event.getServer(),
                        ClientDisconnectEvent.ClientDisconnectReason.EXCEPTION,
                        event.getClientConnection().getUUID(), exception);
                event.getServer().getEventHandler().callEvent(disconnectEvent);
            }
        } else if(event.getPacket() instanceof DisconnectPacket) {
            event.getClientConnection().disconnect(false);
            ClientDisconnectEvent disconnectEvent = new ClientDisconnectEvent(event.getServer(),
                    ClientDisconnectEvent.ClientDisconnectReason.DISCONNECT_PACKET,
                    event.getClientConnection().getUUID());
            event.getServer().getEventHandler().callEvent(disconnectEvent);
        }
    }
}
