/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.neto_framework.packets;

import java.io.IOException;
import net.neto_framework.Connection;
import net.neto_framework.Packet;
import net.neto_framework.client.Client;
import net.neto_framework.client.ServerConnection;
import net.neto_framework.client.event.events.DisconnectEvent;
import net.neto_framework.client.event.events.DisconnectEvent.DisconnectReason;
import net.neto_framework.server.ClientConnection;
import net.neto_framework.server.Server;
import net.neto_framework.server.event.events.ClientDisconnectEvent;
import net.neto_framework.server.event.events.ClientDisconnectEvent.ClientDisconnectReason;

/**
 * A disconnect packet can be sent to/from either server or client. This packet is sent before the
 * client/server closes connection, allowing the client/server to end the connection cleanly.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class DisconnectPacket implements Packet {
    
    @Override
    public void send(Connection connection) throws IOException {
    }

    @Override
    public void receive(Connection connection) throws IOException {
    }

    @Override
    public void onServerReceive(Server server, ClientConnection clientConnection, Packet packet) {
        clientConnection.disconnect(false);
        ClientDisconnectEvent event = new ClientDisconnectEvent(server,
                ClientDisconnectReason.DISCONNECT_PACKET, clientConnection.getUUID());
        server.getEventHandler().callEvent(event);
    }

    @Override
    public void onClientReceive(Client client, ServerConnection serverConnection, Packet packet) {
        client.disconnect(false);
        DisconnectEvent event = new DisconnectEvent(client, DisconnectReason.DISCONNECT_PACKET);
        client.getEventHandler().callEvent(event);
    }

    @Override
    public int getId() {
        return -3;
    }
}
