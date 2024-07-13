package com.undefined.api.nms.v1_20_4.entity

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import com.undefined.api.nms.EntityInteract
import com.undefined.api.nms.interfaces.NMSEntity
import com.undefined.api.nms.v1_20_4.NMSManager
import com.undefined.api.nms.v1_20_4.SpigotNMSMappings
import com.undefined.api.nms.v1_20_4.extensions.sendPacket
import net.minecraft.ChatFormatting
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntityType
import org.bukkit.craftbukkit.v1_20_R3.util.CraftChatMessage
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.UUID


open class NMSEntity(override val entityType: EntityType): com.undefined.api.nms.interfaces.NMSEntity {
    override val viewers: MutableList<Player> = mutableListOf()
    override var location: Location? = null
    var entity: Entity? = null


    private val scoreboard = Scoreboard()
    private val team = scoreboard.addPlayerTeam("glow_${UUID.randomUUID()}")
    private val DATA_NO_GRAVITY: EntityDataAccessor<Boolean>
    override var gravity: Boolean = false
        set(value) {

            entity?.let {
                entity!!.entityData.set(DATA_NO_GRAVITY, !value)
                field = value
                sendMetaPackets()
            }

        }

    override var onFire: Boolean = false
        set(value){

            val entity = entity ?: return
            field = value
            if (value){

                entity.remainingFireTicks = 2000000

                val dataList: MutableList<SynchedEntityData.DataValue<*>> = mutableListOf(
                    SynchedEntityData.DataValue.create(EntityDataAccessor(0, EntityDataSerializers.BYTE), 0x01)
                )

                val dataPacket = ClientboundSetEntityDataPacket(entity.id, dataList)

                viewers.sendPacket(dataPacket)
            }else{
                entity.remainingFireTicks = 0

                val dataList: MutableList<SynchedEntityData.DataValue<*>> = mutableListOf(
                    SynchedEntityData.DataValue.create(EntityDataAccessor(0, EntityDataSerializers.BYTE), 0)
                )

                val dataPacket = ClientboundSetEntityDataPacket(entity.id, dataList)

                viewers.sendPacket(dataPacket)
            }

        }

    override var glowingColor: ChatColor = ChatColor.WHITE
        set(value) {

            if (entity == null) return

            field = value

            val format = ChatFormatting.valueOf(value.name)
            team.color = format

            viewers.sendPacket(
                ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true),
                ClientboundSetPlayerTeamPacket.createPlayerPacket(team, entity!!.uuid.toString(), ClientboundSetPlayerTeamPacket.Action.ADD)
            )

        }

    override var glowing: Boolean = false
        set(value) {

            if (entity == null) return

            field = value

            entity!!.setGlowingTag(value)

            sendMetaPackets()
        }


    override var collibable: Boolean = false
        set(value) {

            if (entity == null) return

            field = value

            team.collisionRule = if (value) Team.CollisionRule.ALWAYS else Team.CollisionRule.NEVER

            viewers.sendPacket(
                ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true),
            )
        }
    override var isVisible: Boolean = true
        set(value) {

            if (entity == null) return

            entity!!.isInvisible = !value

            sendMetaPackets()

            field = value
        }

    override var customName: String? = null
        get() = if (field == null) "" else field
        set(value) {

            if (entity == null) return

            if (value == null){
                //Hide Name

                entity!!.isCustomNameVisible = false

                sendMetaPackets()

            }else{
                //Show / Set name

                var name = value

                if (name.length > 256) {
                    name = name.substring(0, 256)
                }

                entity!!.customName = CraftChatMessage.fromStringOrNull(name)

                entity!!.isCustomNameVisible = true

                sendMetaPackets()
            }

            field = value
        }

    init {
        val field = Entity::class.java.getDeclaredField(SpigotNMSMappings.EntityNoGrafity)
        field.isAccessible = true
        DATA_NO_GRAVITY = field.get(null) as EntityDataAccessor<Boolean>
    }

    override fun addViewer(player: Player) {
        viewers.add(player)
    }

    override fun removeViewer(player: Player) {
        viewers.remove(player)
    }


    override fun spawn(newLocation: Location) {
        if (viewers.isEmpty()) {
            return
        }

        val nmsEntityType = CraftEntityType.bukkitToMinecraft(entityType)

        val craftWorld = newLocation.world as CraftWorld

        val entity = getUndefinedEntityClass(nmsEntityType, craftWorld.handle)

        entity.setPos(newLocation.x, newLocation.y, newLocation.z)

        val m = Entity::class.java.getDeclaredMethod(SpigotNMSMappings.EntitySetRotMethod, Float::class.java, Float::class.java)
        m.isAccessible = true
        m.invoke(entity, newLocation.yaw, newLocation.pitch)


        scoreboard.addPlayerToTeam(entity.uuid.toString(), team)

        val packet = entity.addEntityPacket

        team.collisionRule = Team.CollisionRule.NEVER
        team.setSeeFriendlyInvisibles(false)

        viewers.sendPacket(
            packet,
            ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true),
            ClientboundSetPlayerTeamPacket.createPlayerPacket(team, entity.uuid.toString(), ClientboundSetPlayerTeamPacket.Action.ADD)
        )
        this.entity = entity
        this.location = newLocation
    }

    override fun teleport(newLocation: Location) {
        entity?.let {
            entity!!.setPos(newLocation.x, newLocation.y, newLocation.z)
            val m = Entity::class.java.getDeclaredMethod(SpigotNMSMappings.EntitySetRotMethod, Float::class.java, Float::class.java)
            m.isAccessible = true
            m.invoke(entity, newLocation.yaw, newLocation.pitch)

            val packet = ClientboundTeleportEntityPacket(entity!!)

            viewers.sendPacket(packet)
            location = newLocation
        }
    }

    override fun kill() {
        if (entity == null) return

        val entityRemovePacket = ClientboundRemoveEntitiesPacket(entity!!.id)

        viewers.sendPacket(entityRemovePacket)
    }

    override fun getEntityID(): Int = if (entity == null) 0 else entity!!.id

    override fun interact(interact: EntityInteract.() -> Unit) {
        NMSManager.entityInteraction[this] = interact
    }

    override fun addPassenger(nmsEntity: NMSEntity) {
        entity?.let {
            val method = com.undefined.api.nms.v1_20_4.entity.NMSEntity::class.java.getDeclaredMethod("getEntityM")
            method.isAccessible = true
            val rider = method.invoke(nmsEntity) as Entity?

            rider?.let { rider ->

                if (it.passengers.isEmpty()) {
                    it.passengers = ImmutableList.of(rider)
                } else {
                    val list: MutableList<Entity> = Lists.newArrayList(it.passengers)

                    if (!it.level().isClientSide && rider is Player && it.getPassengers() !is Player) {
                        list.add(0, rider)
                    } else {
                        list.add(rider)
                    }

                    it.passengers = ImmutableList.copyOf(list)

                }

                viewers.sendPacket(ClientboundSetPassengersPacket(it))
            }
        }
    }

    override fun removePassenger(nmsEntity: NMSEntity) {
        entity?.let {
            val method = com.undefined.api.nms.v1_20_4.entity.NMSEntity::class.java.getDeclaredMethod("getEntityM")
            method.isAccessible = true
            val rider = method.invoke(nmsEntity) as Entity?

            rider?.let { rider ->

                if (it.passengers.contains(rider)) {
                    val list: MutableList<Entity> = Lists.newArrayList(it.passengers)
                    list.remove(rider)
                    it.passengers = ImmutableList.copyOf(list)
                    viewers.sendPacket(ClientboundSetPassengersPacket(it))
                }
            }
        }
    }

    open fun getUndefinedEntityClass(entityType: net.minecraft.world.entity.EntityType<*>, level: Level): Entity = UndefinedEntity(entityType, level)

    fun sendMetaPackets() {
        entity?.let { entity ->
            entity.entityData.nonDefaultValues?.let {

                ClientboundSetEntityDataPacket(
                    entity.id,
                    it
                )
            }?.let { viewers.sendPacket(it) }
        }
    }

    fun getEntityM(): Entity? = entity
}