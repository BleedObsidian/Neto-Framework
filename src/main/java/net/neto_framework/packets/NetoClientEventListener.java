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
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import net.neto_framework.Protocol;
import net.neto_framework.client.event.ClientEventListener;
import net.neto_framework.client.event.events.DisconnectEvent;
import net.neto_framework.client.event.events.PacketExceptionEvent;
import net.neto_framework.client.event.events.ReceivePacketEvent;
import net.neto_framework.exceptions.PacketException;

/**
 * A client event listener used to listen to built-in packets.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class NetoClientEventListener extends ClientEventListener {
    
    @Override
    public void onReceivePacket(ReceivePacketEvent event) {
        if(event.getPacket() instanceof EncryptionRequestPacket) {
            EncryptionRequestPacket packet = (EncryptionRequestPacket) event.getPacket();
            
            if(packet.getValue().equals(HandshakePacket.MAGIC_STRING)) {
                try {
                    PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(
                            new X509EncodedKeySpec(packet.getPublicKey()));
                    event.getClient().setPublicKey(publicKey);
                    
                    KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede");
                    SecretKey secretKey = keyGenerator.generateKey();
                    event.getClient().setSecretKey(secretKey);
                } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                    PacketException exception = new PacketException("Received invalid public key"
                            + " from server.", e);
                    PacketExceptionEvent packetEvent = new PacketExceptionEvent(event.getClient(),
                            exception);
                    event.getClient().getEventHandler().callEvent(packetEvent);

                    event.getClient().disconnect();
                    DisconnectEvent disconnectEvent = new DisconnectEvent(event.getClient(),
                            DisconnectEvent.DisconnectReason.EXCEPTION, 
                            exception);
                    event.getClient().getEventHandler().callEvent(disconnectEvent);
                    return;
                }
                
                EncryptionResponsePacket encryptionResponsePacket = new EncryptionResponsePacket();
                byte[] encryptedSecretKey;
                
                try {
                    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, event.getClient().getPublicKey());
                    encryptedSecretKey = cipher.doFinal(event.getClient().getSecretKey().
                            getEncoded());
                } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                        InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                    PacketException exception = new PacketException("Failed to use public key form"
                            + " server.", e);
                    PacketExceptionEvent packetEvent = new PacketExceptionEvent(event.getClient(),
                            exception);
                    event.getClient().getEventHandler().callEvent(packetEvent);

                    event.getClient().disconnect();
                    DisconnectEvent disconnectEvent = new DisconnectEvent(event.getClient(),
                            DisconnectEvent.DisconnectReason.EXCEPTION, 
                            exception);
                    event.getClient().getEventHandler().callEvent(disconnectEvent);
                    return;
                }
                
                encryptionResponsePacket.setSecretKey(encryptedSecretKey);
                
                SecureRandom random = new SecureRandom();
                byte iv[] = new byte[8];
                random.nextBytes(iv);
                IvParameterSpec ivParameter = new IvParameterSpec(iv);
                event.getClient().setIvParameterSpec(ivParameter);
                
                byte[] encryptedIv;
                
                try {
                    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, event.getClient().getPublicKey());
                    encryptedIv = cipher.doFinal(iv);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                        InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                    PacketException exception = new PacketException("Failed to use public key form"
                            + " server.", e);
                    PacketExceptionEvent packetEvent = new PacketExceptionEvent(event.getClient(),
                            exception);
                    event.getClient().getEventHandler().callEvent(packetEvent);

                    event.getClient().disconnect();
                    DisconnectEvent disconnectEvent = new DisconnectEvent(event.getClient(),
                            DisconnectEvent.DisconnectReason.EXCEPTION, 
                            exception);
                    event.getClient().getEventHandler().callEvent(disconnectEvent);
                    return;
                }
                
                encryptionResponsePacket.setIv(encryptedIv);
                encryptionResponsePacket.setRandom(event.getClient().getRandom());
                
                try {
                    event.getClient().getServerConnection().sendPacket(encryptionResponsePacket,
                            Protocol.TCP);
                    event.getClient().getServerConnection().enableEncryption();
                } catch (IOException e) {
                    PacketException exception = new PacketException("Failed to send encryption"
                            + " response.", e);
                    PacketExceptionEvent packetEvent = new PacketExceptionEvent(event.getClient(),
                            exception);
                    event.getClient().getEventHandler().callEvent(packetEvent);

                    event.getClient().disconnect();
                    DisconnectEvent disconnectEvent = new DisconnectEvent(event.getClient(),
                            DisconnectEvent.DisconnectReason.EXCEPTION, 
                            exception);
                    event.getClient().getEventHandler().callEvent(disconnectEvent);
                }
            } else {
                PacketException exception = new PacketException("Incorrect magic string received.");
                PacketExceptionEvent packetEvent = new PacketExceptionEvent(event.getClient(),
                        exception);
                event.getClient().getEventHandler().callEvent(packetEvent);

                event.getClient().disconnect();
                DisconnectEvent disconnectEvent = new DisconnectEvent(event.getClient(),
                        DisconnectEvent.DisconnectReason.EXCEPTION, 
                        exception);
                event.getClient().getEventHandler().callEvent(disconnectEvent);
            }
        } else if (event.getPacket() instanceof SuccessPacket) {
            SuccessPacket packet = (SuccessPacket) event.getPacket();
            
            if(packet.getRandom() == event.getClient().getRandom()) {
                event.getClient().setUUID(UUID.fromString(packet.getUUID()));
                event.getClient().setHandshakeCompleted(true);
            } else {
                PacketException exception = new PacketException("Received incorrect random from"
                        + " server.");
                PacketExceptionEvent packetEvent = new PacketExceptionEvent(event.getClient(),
                        exception);
                event.getClient().getEventHandler().callEvent(packetEvent);

                event.getClient().disconnect();
                DisconnectEvent disconnectEvent = new DisconnectEvent(event.getClient(),
                        DisconnectEvent.DisconnectReason.EXCEPTION, 
                        exception);
                event.getClient().getEventHandler().callEvent(disconnectEvent);
            }
        } else if(event.getPacket() instanceof DisconnectPacket) {
            event.getClient().disconnect(false);
            DisconnectEvent disconnectEvent = new DisconnectEvent(event.getClient(),
                    DisconnectEvent.DisconnectReason.DISCONNECT_PACKET);
            event.getClient().getEventHandler().callEvent(disconnectEvent);
        }
    }
}
