/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.neto_framework.client.packets.handlers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import net.neto_framework.ClientPacketHandler;
import net.neto_framework.client.Client;
import net.neto_framework.client.exceptions.ClientConnectException;
import net.neto_framework.packets.SuccessPacket;

/**
 * 
 *
 * @author Jesse Prescott (BleedObsidian)
 */
public class SuccessPacketHandler implements ClientPacketHandler<SuccessPacket> {

    @Override
    public void onReceivePacket(Client client, SuccessPacket packet) {
        if(packet.getRandom() == client.getRandom()) {
            client.setUUID(UUID.fromString(packet.getUUID()));
        } else {
            ClientConnectException exception = new ClientConnectException("Received incorrect"
                    + " random from server.");
            client.setHandshakeException(exception);
            client.disconnect(false);
            
            return;
        }

        // Hash success packet and sent it back to server over UDP.
        String successPacket = client.getUUID().toString() +
                client.getRandom();
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-512");
            //md.update("TEST".getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unkown hashing algorithm.", e);
        }

        byte[] data = md.digest(successPacket.getBytes());
        data = Base64.getEncoder().withoutPadding().encode(data);
        DatagramPacket hashedSuccessPacket = new DatagramPacket(data, data.length,
                client.getAddress().getInetAddress(),
                client.getAddress().getPort());

        try {
            client.getUdpSocket().send(hashedSuccessPacket);
        } catch (IOException e) {
            ClientConnectException exception = new ClientConnectException(
                    "Failed to send hashed success packet", e);
            client.setHandshakeException(exception);
            client.disconnect(false);
            
            return;
        }

        client.setHandshakeCompleted(true);
        client.getTimer().cancel();
    }

}
