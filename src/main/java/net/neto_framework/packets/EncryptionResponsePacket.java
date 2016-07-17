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
 * The encryption response packet is sent client -> server and contains the shared secret encrypted
 * with the servers public key. This packet also contains a random byte that should be returned
 * by the server exactly.
 *
 * @author BleedObsidian (Jesse Prescott)
 */
public class EncryptionResponsePacket implements Packet {
    
    /**
     * The secret key encrypted with the servers public key.
     */
    private byte[] secretKey;
    
    /**
     * The IV parameter used for ciphering. (Encrypted)
     */
    private byte[] iv;
    
    /**
     * A random short that must be returned by the server exactly.
     */
    private short random;

    @Override
    public void send(Connection connection) throws IOException {
        connection.sendByteArray(this.secretKey);
        connection.sendByteArray(this.iv);
        connection.sendShort(this.random);
    }

    @Override
    public void receive(Connection connection) throws IOException {
        this.secretKey = connection.receiveByteArray();
        this.iv = connection.receiveByteArray();
        this.random = connection.receiveShort();
    }
    
    /**
     * @return The secret key encrypted with the servers public key.
     */
    public byte[] getSecretKey() {
        return this.secretKey;
    }
    
    /**
     * @param secretKey The secret key encrypted with the servers public key.
     */
    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }
    
    /**
     * @return The IV parameter used for ciphering. (Encrypted)
     */
    public byte[] getIv() {
        return this.iv;
    }
    
    /**
     * @param iv The IV parameter used for ciphering.(Encrypted)
     */
    public void setIv(byte[] iv) {
        this.iv = iv;
    }
    
    /**
     * @return A random short that must be returned by the server exactly.
     */
    public short getRandom() {
        return this.random;
    }
    
    /**
     * @param random A random short that must be returned by the server exactly.
     */
    public void setRandom(short random) {
        this.random = random;
    }

    @Override
    public int getId() {
        return -3;
    }

}
