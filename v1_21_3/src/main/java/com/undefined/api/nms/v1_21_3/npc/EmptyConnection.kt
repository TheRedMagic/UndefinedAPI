package com.undefined.api.nms.v1_21_3.npc

import net.minecraft.network.Connection
import net.minecraft.network.PacketSendListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketFlow

import java.net.SocketAddress

/**
 * This class represents an empty connection that extends the Connection class.
 * An empty connection does not perform any actual network operations and is always considered to be connected.
 *
 * @param flag the packet flow flag for the connection
 */
class EmptyConnection(flag: PacketFlow? = null): Connection(flag) {

    init {
        channel = EmptyChannel(null)
        address = object : SocketAddress() {
            private val serialVersionUID = 8207338859896320185L
        }
    }

    override fun flushChannel() {
    }

    override fun isConnected(): Boolean = true

    override fun send(packet: Packet<*>) {
    }

    override fun send(packet: Packet<*>, genericfuturelistener: PacketSendListener?) {
    }

    override fun send(packet: Packet<*>, genericfuturelistener: PacketSendListener?, flag: Boolean) {
    }
}