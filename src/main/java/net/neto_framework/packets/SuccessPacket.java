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
 * The success packet is sent from server to client and is the final packet involved in the
 * handshake process. This packet contains the UUID of the client given by the server and the random
 * short from the client. This packet is fully encrypted at this point via the connection handler,
 * if the UUID of a client is obtained by a malicious user, it could be used to send UDP packets
 * on behalf of a client.
 *
 * @author BleedObsidian (Jesse Prescott)
 */
public class SuccessPacket implements Packet {
    
    /**
     * The UUID of the client.
     */
    private String uuid;
    
    /**
     * The random short that was received from the client in the encryption response.
     */
    private short random;

    @Override
    public void send(Connection connection) throws IOException {
        connection.sendString(this.uuid);
        connection.sendShort(this.random);
    }

    @Override
    public void receive(Connection connection) throws IOException {
        this.uuid = connection.receiveString();
        this.random = connection.receiveShort();
    }
    
    /**
     * 
     * @return The UUID of the client.
     */
    public String getUUID() {
        return this.uuid;
    }
    
    /**
     * @param uuid The UUID of the client.
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }
    
    /**
     * @return The random short that was received from the client in the encryption response.
     */
    public short getRandom() {
        return this.random;
    }
    
    /**
     * @param random The random short that was received from the client in the encryption response.
     */
    public void setRandom(short random) {
        this.random = random;
    }

    @Override
    public int getId() {
        return -4;
    }

}
