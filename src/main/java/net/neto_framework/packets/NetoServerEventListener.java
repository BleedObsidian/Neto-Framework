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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.neto_framework.Connection;
import net.neto_framework.Protocol;
import net.neto_framework.server.event.ServerEventListener;
import net.neto_framework.server.event.events.ClientDisconnectEvent;
import net.neto_framework.server.event.events.ClientFailedToConnectEvent;
import net.neto_framework.server.event.events.ReceivePacketEvent;
import net.neto_framework.server.exceptions.ConnectionException;

/**
 * A server event listener used to listen to built-in packets.
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class NetoServerEventListener extends ServerEventListener {
    
    @Override
    public void onReceivePacket(ReceivePacketEvent event) {
        if(event.getPacket() instanceof HandshakePacket) {
            HandshakePacket packet = (HandshakePacket) event.getPacket();
            
            if(packet.getValue().equals(HandshakePacket.MAGIC_STRING)) {
                EncryptionRequestPacket response = new EncryptionRequestPacket();
                response.setPublicKey(event.getServer().getPublicKey().getEncoded());

                try {
                    event.getClientConnection().sendPacket(response, Protocol.TCP);
                } catch (IOException e) {
                    ConnectionException exception = new ConnectionException(
                            "Failed to send encryption request.", e);
                    ClientFailedToConnectEvent failedEvent = new ClientFailedToConnectEvent(
                            event.getServer(), exception);
                    event.getServer().getEventHandler().callEvent(failedEvent);

                    event.getClientConnection().disconnect(false);
                    return;
                }

                Connection udpConnection = new Connection(event.getServer().getUdpSocket(),
                        event.getClientConnection().getTCPConnection().getTCPSocket().
                                getInetAddress(), packet.getUdpPort());
                event.getClientConnection().addUdpConnection(udpConnection);
            } else {
                ConnectionException exception = new ConnectionException(
                        "Incorrect magic string received.");
                ClientFailedToConnectEvent failedEvent = new ClientFailedToConnectEvent(
                        event.getServer(), exception);
                event.getServer().getEventHandler().callEvent(failedEvent);

                event.getClientConnection().disconnect(false);
                return;
            }
        } else if(event.getPacket() instanceof EncryptionResponsePacket) {
            EncryptionResponsePacket packet = (EncryptionResponsePacket) event.getPacket();

            try {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, event.getServer().getPrivateKey());
                
                byte[] secretKey = cipher.doFinal(packet.getSecretKey());
                event.getClientConnection().setSecretKey(new SecretKeySpec(secretKey, 0,
                        secretKey.length, "DESede"));
                
                byte[] iv = cipher.doFinal(packet.getIv());
                IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
                event.getClientConnection().setIvParameterSpec(ivParameterSpec);
                
                event.getClientConnection().enableEncryption();
                
                SuccessPacket successPacket = new SuccessPacket();
                successPacket.setUUID(event.getClientConnection().getUUID().toString());
                successPacket.setRandom(packet.getRandom());
                
                // Store hashed version of packet.
                String successPacketData = event.getClientConnection().getUUID().toString() +
                        packet.getRandom();
                MessageDigest md = null;
            
                try {
                    md = MessageDigest.getInstance("SHA-512");
                    //md.update("TEST".getBytes());
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("Unkown hashing algorithm.", e);
                }
                
                byte[] hashedSuccessPacketData = md.digest(successPacketData.getBytes());
                hashedSuccessPacketData = Base64.getEncoder().withoutPadding()
                        .encode(hashedSuccessPacketData);
                
                event.getClientConnection().setHashedSuccessPacket(hashedSuccessPacketData);
                
                try {
                    event.getClientConnection().sendPacket(successPacket, Protocol.TCP);
                } catch (IOException e) {
                    ConnectionException exception = new ConnectionException(
                            "Failed to send success packet.", e);
                    ClientFailedToConnectEvent failedEvent = new ClientFailedToConnectEvent(
                            event.getServer(), exception);
                    event.getServer().getEventHandler().callEvent(failedEvent);

                    event.getClientConnection().disconnect(false);
                    return;
                }
                
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                    IllegalBlockSizeException | BadPaddingException e) {
                ConnectionException exception = new ConnectionException(
                            "Invalid encryption response.", e);
                ClientFailedToConnectEvent failedEvent = new ClientFailedToConnectEvent(
                        event.getServer(), exception);
                event.getServer().getEventHandler().callEvent(failedEvent);

                event.getClientConnection().disconnect(false);
                return;
            }
        } else if(event.getPacket() instanceof DisconnectPacket) {
            event.getClientConnection().disconnect(false);
            ClientDisconnectEvent disconnectEvent = new ClientDisconnectEvent(event.getServer(),
                    ClientDisconnectEvent.ClientDisconnectReason.DISCONNECT_PACKET,
                    event.getClientConnection());
            event.getServer().getEventHandler().callEvent(disconnectEvent);
        }
    }
}
