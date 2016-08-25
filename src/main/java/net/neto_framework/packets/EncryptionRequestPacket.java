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
 * The encryption request packet is sent from server to client. It contains the secret key and IV
 * parameter that will be used to encrypt UDP communication. This packet must be sent over an
 * encrypted TCP connection such as SSLSocket.
 *
 * @author BleedObsidian (Jesse Prescott)
 */
public class EncryptionRequestPacket implements Packet {
    
    /**
     * The magic string that is sent to ensure the same framework is being used.
     */
    private String magicStringValue;
    
    /**
     * The secret key.
     */
    private byte[] secretKey;
    
    /**
     * The IV parameter used for ciphering.
     */
    private byte[] iv;
    
    /**
     * A random sequence of bytes that must be returned in encrypted form over UDP by the client.
     */
    private byte[] random;

    @Override
    public void send(Connection connection) throws IOException {
        connection.sendString(Connection.MAGIC_STRING);
        connection.sendByteArray(this.secretKey);
        connection.sendByteArray(this.iv);
        connection.sendByteArray(this.random);
    }

    @Override
    public void receive(Connection connection) throws IOException {
        this.magicStringValue = connection.receiveString();
        this.secretKey = connection.receiveByteArray();
        this.iv = connection.receiveByteArray();
        this.random = connection.receiveByteArray();
    }
    
    /**
     * @return The magic string that is sent to ensure the same framework is being used.
     */
    public String getMagicStringValue() {
        return this.magicStringValue;
    }
    
    /**
     * @return The secret key.
     */
    public byte[] getSecretKey() {
        return this.secretKey;
    }
    
    /**
     * @param secretKey The secret key.
     */
    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }
    
    /**
     * @return The IV parameter used for ciphering.
     */
    public byte[] getIv() {
        return this.iv;
    }
    
    /**
     * @param iv The IV parameter used for ciphering.
     */
    public void setIv(byte[] iv) {
        this.iv = iv;
    }
    
    /**
     * @return A random sequence of bytes that must be returned in encrypted form over UDP by the
     *         client.
     */
    public byte[] getRandom() {
        return this.random;
    }
    
    /**
     * @param random A random sequence of bytes that must be returned in encrypted form over UDP by
     *               the client.
     */
    public void setRandom(byte[] random) {
        this.random = random;
    }

    @Override
    public int getId() {
        return -2;
    }
}
