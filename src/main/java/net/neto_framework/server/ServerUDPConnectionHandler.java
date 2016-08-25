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

package net.neto_framework.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import net.neto_framework.Connection;
import net.neto_framework.Protocol;
import net.neto_framework.exceptions.PacketException;
import net.neto_framework.server.event.events.PacketExceptionEvent;

/**
 * A connection handler that accepts UDP connections on a separate thread.
 *
 * @author BleedObsidian (Jesse Prescott)
 */
public class ServerUDPConnectionHandler extends Thread {

    /**
     * Running instance of Server.
     */
    private final Server server;

    /**
     * @param server Running instance of {@link net.neto_framework.server.Server Server}.
     */
    public ServerUDPConnectionHandler(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        // Name this thread.
        Thread.currentThread().setName("Neto-Framework Server UDP Handler");
        
        // Continuously read UDP packets until the server is no longer running.
        mainLoop:
        while (this.server.isRunning()) {
            
            // Create a data buffer and packet.
            byte[] data = new byte[65508];
            DatagramPacket dataPacket = new DatagramPacket(data, data.length);

            // Attempt to read UDP data and place it into the data buffer.
            try {
                this.server.getUdpSocket().receive(dataPacket);
            } catch (IOException e) {
                if(!this.server.getUdpSocket().isClosed()) {
                    PacketException exception = new PacketException("Failed to read an incoming"
                            + " UDP packet.", e);
                    PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                    this.server.getEventHandler().callEvent(event);
                }
            }
            
            // Trim the data if it does not fit exactly into 65508 bytes.
            //    Store the length of the data.
            int i = data.length - 1;
            
            //    Iterate through data array and reduce the size of the data length for every null
            //    null byte found.
            while(i >= 0 && data[i] == 0) {
                --i;
            }
            
            //   Copy the array but truncate any bytes above found length.
            data = Arrays.copyOf(data, i + 1);
            
            // Decode the data with Base64.
            data = Base64.getDecoder().decode(data);
            
            // Check if the data received is actually a hashed random.
            //    Loop through each pending (in handshake process) client.
            for(ClientConnection client :
                    this.server.getConnectionManager().getPendingClientConnections()) {
                
                //    Check if the data is equal to their hash.
                if(Arrays.equals(data, client.getHashedRandom())) {
                    
                    //    Validate the connection completing the handshake process server side.
                    this.server.getConnectionManager().onConnectionValidated(client.getUUID());
                    
                    //    Record the port that this packet came from so the correct cipher can be
                    //    used in the future when reading UDP packets.
                    client.setClientUdpPort(dataPacket.getPort());
                    
                    //    Place the client in the completed handshake state.
                    client.setHandshakeCompleted(true);
                    
                    //    Continue main loop ready to read another UDP packet.
                    continue mainLoop;
                }
            }
            
            // Attempt to retreive the ClientConnection that sent this UDP packet.
            ClientConnection client = this.server.getConnectionManager().getClientConnection(
                    dataPacket.getAddress(), dataPacket.getPort());
            
            // Check if we managed to find a ClientConnection.
            if(client == null) {
                PacketException exception = new PacketException("UDP Packet received from unkown"
                        + " source.");
                PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                this.server.getEventHandler().callEvent(event);
                continue;
            }
            
            // Load the UDP connection interface for the client.
            Connection connection = client.getUDPConnection();
            
            // Place the data into a stream so the connection interface can read it.
            connection.setUdpDataInputStream(new ByteArrayInputStream(data));
            
            // Define metadata variables.
            int packetId = 0;
            UUID uuid = null;
            long timestamp = 0;
            
            // Attempt to read metadata.
            try {
                packetId = connection.receiveInteger();
                uuid = UUID.fromString(connection.receiveString());
                timestamp = connection.receiveLong();
            } catch (IOException e) {
                PacketException exception = new PacketException("Failed to read metadata for"
                        + " incoming UDP packet.", e);
                PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                this.server.getEventHandler().callEvent(event);
            }
            
            // Check to see if the packet arrived within the replay window.
            if((System.currentTimeMillis() - timestamp) > Connection.REPLAY_WINDOW) {
                continue;
            }
            
            // Check to see if the received UUID is valid.
            if(!client.getUUID().equals(uuid)) {
                PacketException exception = new PacketException("UDP Packet was received with an"
                        + " invalid UUID.");
                PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                this.server.getEventHandler().callEvent(event);
            }
            
            // Check to see if the server knows the given packet.
            if(!this.server.getPacketManager().hasPacket(packetId)) {
                PacketException exception = new PacketException("Unkown UDP packet received.");
                PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                this.server.getEventHandler().callEvent(event);
            }
            
            // Attempt to read packet data.
            try {
                this.server.getPacketManager().receive(this.server, packetId, client, Protocol.UDP);
            } catch (IOException e) {
                PacketException exception = new PacketException("Failed to read UDP packet.", e);
                PacketExceptionEvent event = new PacketExceptionEvent(this.server, exception);
                this.server.getEventHandler().callEvent(event);
            }
        }
    }
}
