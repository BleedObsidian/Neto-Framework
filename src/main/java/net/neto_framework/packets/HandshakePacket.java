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
 * The handshake packet is the first packet sent from client to server. It contains a magic string
 * that is used to check that both parties are using the Neto-Framework. It also contains version
 * information allowing the server to ensure that both parties are using versions of Neto-Framework
 * that work together.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class HandshakePacket implements Packet {
    
    /**
     * The magic string that is sent to ensure the same framework is being used.
     */
    private String magicStringValue;
    
    /**
     * The version of Neto-Framework the client is running.
     */
    private String clientVersion;
    
    /**
     * The UDP port the client is listening on.
     */
    private int listeningUdpPort;

    @Override
    public void send(Connection connection) throws IOException {
        connection.sendString(Connection.MAGIC_STRING);
        connection.sendString(this.clientVersion);
        connection.sendInteger(this.listeningUdpPort);
    }
    
    @Override
    public void receive(Connection connection) throws IOException {
        this.magicStringValue = connection.receiveString();
        this.clientVersion = connection.receiveString();
        this.listeningUdpPort = connection.receiveInteger();
    }
    
    /**
     * @return The magic string that is sent to ensure the same framework is being used.
     */
    public String getMagicStringValue() {
        return this.magicStringValue;
    }
    
    /**
     * @return The version of Neto-Framework the client is running.
     */
    public String getClientVersion() {
        return this.clientVersion;
    }
    
    /**
     * @param version The version of Neto-Framework the client is running.
     */
    public void setClientVersion(String version) {
        this.clientVersion = version;
    }
    
    /**
     * @return The UDP port the client is listening on.
     */
    public int getListeningUdpPort() {
        return this.listeningUdpPort;
    }
    
    /**
     * @param port The UDP port the client is listening on.
     */
    public void setListeningUdpPort(int port) {
        this.listeningUdpPort = port;
    }

    @Override
    public int getId() {
        return -1;
    }
}
