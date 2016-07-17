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
 * The encryption request packet is sent from server -> client after the server has received a
 * handshake. This packet contains the servers public key that should be used to encrypt the shared
 * secret when sending the encryption response.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class EncryptionRequestPacket implements Packet {
    
    /**
     * The actual magic string value that was received.
     */
    private String value;
    
    /**
     * The public key to be used by client to encrypt shared secret.
     */
    private byte[] publicKey;

    @Override
    public void send(Connection connection) throws IOException {
        connection.sendString(HandshakePacket.MAGIC_STRING);
        connection.sendInteger(this.publicKey.length);
        connection.send(this.publicKey);
    }
    
    @Override
    public void receive(Connection connection) throws IOException {
        this.value = connection.receiveString();
        int keyLength = connection.receiveInteger();
        this.publicKey = connection.receive(new byte[keyLength]);
    }
    
    /**
     * @return The actual magic string value that was received.
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * @return The public key to be used by client to encrypt shared secret.
     */
    public byte[] getPublicKey() {
        return this.publicKey;
    }
    
    /**
     * @param publicKey The public key to be used by client to encrypt shared secret.
     */
    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public int getId() {
        return -2;
    }
}
