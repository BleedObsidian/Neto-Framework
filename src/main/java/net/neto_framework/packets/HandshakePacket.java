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
import net.neto_framework.Packet;

/**
 * The handshake packet is the first packet send from client to server.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class HandshakePacket implements Packet {
    
    /**
     * The magic string that is sent to ensure the same framework is being used.
     */
    public static final String MAGIC_STRING = "sjd7dHS92jS92L02";
    
    /**
     * The actual value that was received.
     */
    private String value;
    
    /**
     * The UDP port the client is listening on.
     */
    private int udpPort;

    @Override
    public void send(Connection connection) throws IOException {
        connection.sendString(HandshakePacket.MAGIC_STRING);
        connection.sendInteger(this.udpPort);
    }
    
    @Override
    public void receive(Connection connection) throws IOException {
        this.value = connection.receiveString();
        this.udpPort = connection.receiveInteger();
    }
    
    /**
     * @return The actual magic string value that was received.
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * @return The UDP port the client is listening on..
     */
    public int getUdpPort() {
        return this.udpPort;
    }
    
    /**
     * @param port The UDP port the client is listening on.
     */
    public void setUdpPort(int port) {
        this.udpPort = port;
    }

    @Override
    public int getId() {
        return -1;
    }
}
