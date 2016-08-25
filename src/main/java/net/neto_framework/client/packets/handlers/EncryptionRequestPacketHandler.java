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

package net.neto_framework.client.packets.handlers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.neto_framework.ClientPacketHandler;
import net.neto_framework.Connection;
import net.neto_framework.client.Client;
import net.neto_framework.client.exceptions.ClientConnectException;
import net.neto_framework.packets.EncryptionRequestPacket;

/**
 * A client-side packet handler for EncryptionRequestPacket.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class EncryptionRequestPacketHandler implements 
        ClientPacketHandler<EncryptionRequestPacket> {

    @Override
    public void onReceivePacket(Client client, EncryptionRequestPacket packet) {
        
        // Check to make sure the server sent a valid magic string.
        if(!packet.getMagicStringValue().equals(Connection.MAGIC_STRING)) {
            ClientConnectException exception = new ClientConnectException("Server did not follow"
                    + " protocol, server is most likely not using Neto-Framework.");
            client.setHandshakeException(exception);
            client.disconnect(false);
            return;
        }
        
        // Store secret key.
        SecretKeySpec secretKeySpec = new SecretKeySpec(packet.getSecretKey(), 0,
                packet.getSecretKey().length, "DESede");
        client.setSecretKey(secretKeySpec);
        
        // Store IV Parameter.
        IvParameterSpec ivParameterSpec = new IvParameterSpec(packet.getIv());
        client.setIvParameterSpec(ivParameterSpec);
        
        // Define byte array to store hash.
        byte[] hash = null;
        
        // Create hash from given random.
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            hash = messageDigest.digest(packet.getRandom());
            hash = Base64.getEncoder().withoutPadding().encode(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to create hash from given random.", e);
        }
        
        // Craft raw packet containing only the hash.
        DatagramPacket hashPacket = new DatagramPacket(
                hash,
                hash.length,
                client.getAddress().getInetAddress(),
                client.getAddress().getPort());
        
        // Attempt to send raw hash packet.
        try {
            client.getUdpSocket().send(hashPacket);
        } catch (IOException e) {
            ClientConnectException exception = new ClientConnectException("Failed to send raw hash"
                    + " to server over UDP.", e);
            client.setHandshakeException(exception);
            client.disconnect(false);
        }
    }
}
