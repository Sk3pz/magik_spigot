package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.colorize
import es.skepz.magik.tuodlib.displayParticles
import es.skepz.magik.tuodlib.playSound
import es.skepz.magik.tuodlib.sendMessage
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class Blazeborn(magik: Magik) : Race(magik) {

    private val rodKey = NamespacedKey(magik, "blazeborn_rod")
    private val rodName = colorize("&6Rod of Fire")

    private val cooldownMap = mutableMapOf<Player, Int>()
    private val defaultCooldown = 5

    override fun cooldownUpdate() {
        cooldownMap.forEach { (plr, seconds) ->
            if (seconds == 0) {
                cooldownMap.remove(plr)
                val rod = findRod(plr) ?: return@forEach
                updateData(plr, rod)
                return@forEach
            }
            cooldownMap[plr] = seconds - 1
            val rod = findRod(plr) ?: return@forEach
            updateData(plr, rod)
        }
    }

    override fun update(player: Player) {
        if (player.location.block.type == Material.LAVA) {
            player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 2, 0, false, false))
        }
        if (player.location.block.type == Material.WATER) {
            player.addPotionEffect(PotionEffect(PotionEffectType.POISON, 2, 5 * 20, false, false))
        }
        if (player.location.world.environment == World.Environment.NORMAL) {
            player.maxHealth = 18.0
        } else {
            player.resetMaxHealth()
        }
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.BLAZE_ROD, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&6&lBlazeborn")))
            it.lore(listOf(
                Component.text(colorize("&7Feel the lava pulsing in your veins")), // change this
                Component.text(colorize("&7- &aRod of Fire: Boost yourself with fire")),
                Component.text(colorize("&7- &aImmune to fire damage")),
                Component.text(colorize("&7- &aRegenerate health in lava")),
                Component.text(colorize("&7- &cAllergic to water")),
                Component.text(colorize("&7- &cLess health in the overworld"))))
        }

        return item
    }

    private fun generateRod(): ItemStack {
        val item = ItemStack(Material.BLAZE_ROD)
        item.itemMeta = item.itemMeta.also {
            it.displayName(Component.text(rodName))
            it.lore(listOf(
                Component.text(colorize("&aA flaming rod only you can wield")),
                Component.text(colorize("&8[&6Right Click&8] &7to fire boost while in the air."))
            ))
            it.isUnbreakable = true
            it.persistentDataContainer.set(rodKey, PersistentDataType.DOUBLE, Math.PI)
            it.addEnchant(Enchantment.DAMAGE_ALL, 10, true)
            it.addEnchant(Enchantment.FIRE_ASPECT, 2, true)
        }
        return item
    }

    private fun findRod(player: Player) : ItemStack? {
        player.inventory.contents.forEach {
            if (it == null) return@forEach
            if (checkRod(it)) {
                return it
            }
        }
        return null
    }

    private fun checkRod(item: ItemStack): Boolean {
        if (!item.hasItemMeta()) return false
        return item.itemMeta.persistentDataContainer.has(rodKey, PersistentDataType.DOUBLE)
    }

    private fun updateData(player: Player, sword: ItemStack) {
        sword.itemMeta = sword.itemMeta.also {
            if (cooldownMap.containsKey(player)) {
                it.displayName(Component.text(colorize("$rodName &8[&c${cooldownMap[player] ?: 0}&8]")))
            } else {
                it.displayName(Component.text(colorize(rodName)))
            }
        }
    }

    private fun Player.isBlazeborn(): Boolean {
        return getRace(magik, this) is Blazeborn
    }

    override fun name(): String {
        return "Blazeborn"
    }

    override fun set(player: Player) {
        val inv = player.inventory
        inv.addItem(generateRod())
    }

    override fun remove(player: Player) {
        player.resetMaxHealth()
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (checkRod(item)) {
                inv.remove(item)
            }
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val item = event.item
            ?: return

        val player = event.player
        if (!player.isBlazeborn()) {
            return
        }

        if (checkRod(item) && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            val cooldown = cooldownMap[player]
            if (cooldown != null) {
                sendMessage(player, "&cYou can't use this item for another $cooldown seconds!")
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
                return
            }

            if (player.isOnGround) return

            displayParticles(player.location, Particle.FLAME, 50, 0.2, 0.2, 0.2)
            val direction = player.location.direction
            direction.y = 0.8
            direction.x *= 0.8
            direction.z *= 0.8

            player.velocity = direction
            cooldownMap[player] = defaultCooldown
            updateData(player, item)
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        (event.entity as? Player)?.takeIf { it.isBlazeborn() }
            ?: return

        if (event.cause == EntityDamageEvent.DamageCause.FIRE || event.cause == EntityDamageEvent.DamageCause.LAVA ||
            event.cause == EntityDamageEvent.DamageCause.FIRE_TICK) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isBlazeborn() }
            ?: return

        if (checkRod(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = (event.whoClicked as? Player)?.takeIf { it.isBlazeborn() }
            ?: return

        val item = event.currentItem
            ?: return

        if (checkRod(item) && (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.inventory.type == InventoryType.ANVIL)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isBlazeborn()) return
        val drops = event.drops
        val remove = mutableListOf<ItemStack>()
        drops.forEach { item ->
            if (item == null) return@forEach
            if (checkRod(item)) {
                remove.add(item)
            }
        }
        remove.forEach { item ->
            drops.remove(item)
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isBlazeborn() }
            ?: return

        setRace(magik, player, this, false)
    }
}