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
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import net.neto_framework.ClientPacketHandler;
import net.neto_framework.Protocol;
import net.neto_framework.client.Client;
import net.neto_framework.client.exceptions.ClientConnectException;
import net.neto_framework.packets.EncryptionRequestPacket;
import net.neto_framework.packets.EncryptionResponsePacket;
import net.neto_framework.packets.HandshakePacket;

/**
 * A client-side packet handler for EncryptionRequestPacket.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class EncryptionRequestPacketHandler implements 
        ClientPacketHandler<EncryptionRequestPacket> {

    @Override
    public void onReceivePacket(Client client, EncryptionRequestPacket packet) {
        if(packet.getValue().equals(HandshakePacket.MAGIC_STRING)) {
            try {
                PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(
                        new X509EncodedKeySpec(packet.getPublicKey()));
                client.setPublicKey(publicKey);

                KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede");
                SecretKey secretKey = keyGenerator.generateKey();
                client.setSecretKey(secretKey);
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                ClientConnectException exception = new ClientConnectException("Received invalid"
                        + " public key from server.", e);
                client.setHandshakeException(exception);
                client.disconnect(false);
                return;
            }

            EncryptionResponsePacket encryptionResponsePacket = new EncryptionResponsePacket();
            byte[] encryptedSecretKey;

            try {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, client.getPublicKey());
                encryptedSecretKey = cipher.doFinal(client.getSecretKey().getEncoded());
            } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                    InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                ClientConnectException exception = new ClientConnectException("Failed to use "
                        + "public key form server.", e);
                client.setHandshakeException(exception);
                client.disconnect(false);
                
                return;
            }

            encryptionResponsePacket.setSecretKey(encryptedSecretKey);

            SecureRandom random = new SecureRandom();
            byte iv[] = new byte[8];
            random.nextBytes(iv);
            IvParameterSpec ivParameter = new IvParameterSpec(iv);
            client.setIvParameterSpec(ivParameter);

            byte[] encryptedIv;

            try {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, client.getPublicKey());
                encryptedIv = cipher.doFinal(iv);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                    InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                ClientConnectException exception = new ClientConnectException("Failed to use"
                        + " public key form server.", e);
                client.setHandshakeException(exception);
                client.disconnect(false);
                return;
            }

            encryptionResponsePacket.setIv(encryptedIv);
            encryptionResponsePacket.setRandom(client.getRandom());

            try {
                client.getServerConnection().sendPacket(encryptionResponsePacket,
                        Protocol.TCP);
                client.getServerConnection().enableEncryption();
            } catch (IOException e) {
                ClientConnectException exception = new ClientConnectException("Failed to send"
                        + " encryption response.", e);
                client.setHandshakeException(exception);
                client.disconnect(false);
            }
        } else {
            ClientConnectException exception = new ClientConnectException("Incorrect magic "
                    + "string received.");
            client.setHandshakeException(exception);
            client.disconnect(false);
        }
    }
    
}
