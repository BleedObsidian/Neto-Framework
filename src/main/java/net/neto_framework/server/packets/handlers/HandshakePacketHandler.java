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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
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
        
        // Check to make sure that the client sent a valid magic string.
        if(!packet.getMagicStringValue().equals(Connection.MAGIC_STRING)) {
            ConnectionException exception = new ConnectionException("A new client attempted to"
                    + " connect but did not follow protocol, client most-likely not using the"
                    + " Neto-Framework.");
            ClientFailedToConnectEvent failedToConnectEvent = new ClientFailedToConnectEvent(server,
                    exception);
            server.getEventHandler().callEvent(failedToConnectEvent);
            client.disconnect(false);
            return;
        }
        
        // Create new secure random.
        SecureRandom secureRandom = new SecureRandom();
        
        // Generate a new DESede secret key and IV Parameter.
        try {
            KeyGenerator generator;
            generator = KeyGenerator.getInstance("DESede");
            generator.init(Server.DEFAULT_KEYSIZE);
            SecretKey secretKey = generator.generateKey();
            client.setSecretKey(secretKey);

            byte[] iv = new byte[8];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            client.setIvParameterSpec(ivSpec);
        } catch (NoSuchAlgorithmException e) {
            client.disconnect(false);
            throw new RuntimeException("Failed to generate secret key/iv.", e);
        }
        
        // Generate a new 64 byte random.
        byte[] random = new byte[64];
        secureRandom.nextBytes(random);
        
        // Hash the random with SHA-512 and store it.
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            byte[] hashedRandom = messageDigest.digest(random);
            client.setHashedRandom(hashedRandom);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash random.", e);
        }
        
        // Create client's UDP connection using UDP port information received.
        Connection udpConnection = new Connection(
                server.getUdpSocket(),
                client.getTCPConnection().getTCPSocket().getInetAddress(),
                packet.getListeningUdpPort()
            );
        client.addUdpConnection(udpConnection);
        
        // Craft an EncryptionRequestPacket.
        EncryptionRequestPacket encryptionRequestPacket = new EncryptionRequestPacket();
        encryptionRequestPacket.setSecretKey(client.getSecretKey().getEncoded());
        encryptionRequestPacket.setIv(client.getIvParameterSpec().getIV());
        encryptionRequestPacket.setRandom(random);
        
        // Attempt to send EncryptionRequestPacket.
        try {
            client.sendPacket(encryptionRequestPacket, Protocol.TCP);
        } catch(IOException e) {
            ConnectionException exception = new ConnectionException("Failed to send encryption"
                    + " request to client.", e);
            ClientFailedToConnectEvent failedToConnectEvent = new ClientFailedToConnectEvent(server,
                exception);
            server.getEventHandler().callEvent(failedToConnectEvent);
            client.disconnect(false);
        }
    }
}
