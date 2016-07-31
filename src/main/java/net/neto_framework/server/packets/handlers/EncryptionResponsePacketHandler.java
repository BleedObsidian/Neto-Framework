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
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.neto_framework.Protocol;
import net.neto_framework.ServerPacketHandler;
import net.neto_framework.packets.EncryptionResponsePacket;
import net.neto_framework.packets.SuccessPacket;
import net.neto_framework.server.ClientConnection;
import net.neto_framework.server.Server;
import net.neto_framework.server.event.events.ClientFailedToConnectEvent;
import net.neto_framework.server.exceptions.ConnectionException;

/**
 * A server-side packet handler for EncryptionResponsePacket.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class EncryptionResponsePacketHandler implements
        ServerPacketHandler<EncryptionResponsePacket> {

    @Override
    public void onReceivePacket(Server server, ClientConnection client, 
            EncryptionResponsePacket packet) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, server.getPrivateKey());

            byte[] secretKey = cipher.doFinal(packet.getSecretKey());
            client.setSecretKey(new SecretKeySpec(secretKey, 0, secretKey.length, "DESede"));

            byte[] iv = cipher.doFinal(packet.getIv());
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            client.setIvParameterSpec(ivParameterSpec);

            client.enableEncryption();

            SuccessPacket successPacket = new SuccessPacket();
            successPacket.setUUID(client.getUUID().toString());
            successPacket.setRandom(packet.getRandom());

            // Store hashed version of packet.
            String successPacketData = client.getUUID().toString() + packet.getRandom();
            MessageDigest md = null;

            try {
                md = MessageDigest.getInstance("SHA-512");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Unkown hashing algorithm.", e);
            }

            byte[] hashedSuccessPacketData = md.digest(successPacketData.getBytes());
            hashedSuccessPacketData = Base64.getEncoder().withoutPadding()
                    .encode(hashedSuccessPacketData);

            client.setHashedSuccessPacket(hashedSuccessPacketData);

            try {
                client.sendPacket(successPacket, Protocol.TCP);
            } catch (IOException e) {
                ConnectionException exception = new ConnectionException(
                        "Failed to send success packet.", e);
                ClientFailedToConnectEvent failedEvent = new ClientFailedToConnectEvent(server,
                        exception);
                server.getEventHandler().callEvent(failedEvent);

                client.disconnect(false);
            }

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException e) {
            ConnectionException exception = new ConnectionException("Invalid encryption response.",
                    e);
            ClientFailedToConnectEvent failedEvent = new ClientFailedToConnectEvent(server,
                    exception);
            server.getEventHandler().callEvent(failedEvent);

            client.disconnect(false);
        }
    }

}
