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

import java.io.IOException;
import net.neto_framework.Connection;
import net.neto_framework.Protocol;
import net.neto_framework.ServerPacketHandler;
import net.neto_framework.packets.EncryptionRequestPacket;
import net.neto_framework.packets.HandshakePacket;
import net.neto_framework.server.ClientConnection;
import net.neto_framework.server.Server;
import net.neto_framework.server.event.events.ClientFailedToConnectEvent;
import net.neto_framework.server.exceptions.ConnectionException;

/**
 * A server-side packet handler for HandshakePacket.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class HandshakePacketHandler implements ServerPacketHandler<HandshakePacket> {
    
    @Override
    public void onReceivePacket(Server server, ClientConnection client, HandshakePacket packet) {
        if(packet.getValue().equals(HandshakePacket.MAGIC_STRING)) {
            EncryptionRequestPacket response = new EncryptionRequestPacket();
            response.setPublicKey(server.getPublicKey().getEncoded());

            try {
                client.sendPacket(response, Protocol.TCP);
            } catch (IOException e) {
                ConnectionException exception = new ConnectionException(
                        "Failed to send encryption request.", e);
                ClientFailedToConnectEvent failedEvent = new ClientFailedToConnectEvent(server,
                        exception);
                server.getEventHandler().callEvent(failedEvent);

                client.disconnect(false);
                return;
            }

            Connection udpConnection = new Connection(server.getUdpSocket(),
                    client.getTCPConnection().getTCPSocket().getInetAddress(),
                    packet.getUdpPort());
            client.addUdpConnection(udpConnection);
        } else {
            ConnectionException exception = new ConnectionException(
                    "Incorrect magic string received.");
            ClientFailedToConnectEvent failedEvent = new ClientFailedToConnectEvent(
                    server, exception);
            server.getEventHandler().callEvent(failedEvent);

            client.disconnect(false);
        }
    }
    
}
