package com.undefined.api.nms.v1_21.entity.entityClass.display

import com.undefined.api.nms.interfaces.display.NMSTextDisplay
import com.undefined.api.nms.v1_21.SpigotNMSMappings
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Display
import org.bukkit.Location
import org.bukkit.entity.EntityType
import java.lang.reflect.Method


class NMSTextDisplayEntity(text: String) : NMSDisplayEntity(EntityType.TEXT_DISPLAY), NMSTextDisplay {

    private val method: Method = Display.TextDisplay::class.java.getDeclaredMethod(SpigotNMSMappings.TextDisplaySetBackGroundColor)

    init {
        method.isAccessible = true
    }

    override var text: String = text
        set(value) {
            entity?.let {
                val textEntity = it as Display.TextDisplay
                textEntity.text = Component.literal(value)
                sendMetaPackets()
                field = value
            }
        }
    override var width: Float = 200F
        set(value) {
            entity?.let {
                val textEntity = it as Display.TextDisplay
                textEntity.width = value
                sendMetaPackets()
                field = value
            }
        }
    override var backGroundColor: Int = 1073741824
        set(value) {
            entity?.let {
                val textEntity = it as Display.TextDisplay
                method.invoke(textEntity, value)
                sendMetaPackets()
                field = value
            }
        }
    override var textOpacity: Byte = -1
        set(value) {
            entity?.let {
                val textEntity = it as Display.TextDisplay
                textEntity.textOpacity = value
                sendMetaPackets()
                field = value
            }
        }


    override fun spawn(newLocation: Location) {
        super.spawn(newLocation)
        text = text
    }
}