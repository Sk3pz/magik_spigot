package es.skepz.magik.races

import es.skepz.magik.CustomItem
import es.skepz.magik.Magik
import es.skepz.magik.skepzlib.colorize
import es.skepz.magik.skepzlib.displayParticles
import es.skepz.magik.skepzlib.playSound
import es.skepz.magik.skepzlib.sendMessage
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Blazeborn(magik: Magik) : Race(magik) {

    private val rodName = "&6Rod of Fire"

    private val rod = CustomItem(magik, Material.BLAZE_ROD, 1, rodName,
        listOf("&aA flaming rod only you can wield", "&8[&6Right Click&8] &7to fire boost while in the air."),
        "blazeborn_rod", false,
        mutableMapOf(Pair(Enchantment.SHARPNESS, 10), Pair(Enchantment.FIRE_ASPECT, 2)))

    private val defaultCooldown = 5

    override fun cooldownUpdate(player: Player, seconds: Int) {
        val rod = rod.find(player.inventory) ?: return
        updateData(player, rod)
    }

    override fun update(player: Player) {
        if (player.location.block.type == Material.LAVA) {
            player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 2, 0, false, false))
        }
        if (player.location.block.type == Material.WATER) {
            player.addPotionEffect(PotionEffect(PotionEffectType.POISON, 2, 5 * 20, false, false))
        }
        if (player.location.world.environment == World.Environment.NORMAL) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 18.0
        } else {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
        }
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.BLAZE_ROD, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(colorize("&6&lBlazeborn"))
            it.lore(listOf(
                colorize("&7Feel the lava pulsing in your veins"), // change this
                colorize("&7- &aRod of Fire: Boost yourself with fire"),
                colorize("&7- &aImmune to fire damage"),
                colorize("&7- &aRegenerate health in lava"),
                colorize("&7- &cAllergic to water"),
                colorize("&7- &cLess health in the overworld")))
        }

        return item
    }

    private fun updateData(player: Player, sword: ItemStack) {
        sword.itemMeta = sword.itemMeta.also {
            if (magik.cooldowns.containsKey(player)) {
                it.displayName(colorize("$rodName &8[&c${magik.cooldowns[player] ?: 0}&8]"))
            } else {
                it.displayName(colorize(rodName))
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
        inv.addItem(rod.generate())
    }

    override fun remove(player: Player) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (rod.check(item)) {
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

        if (rod.check(item) && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            val cooldown = magik.cooldowns[player]
            if (cooldown != null) {
                sendMessage(player, "&cYou can't use this item for another $cooldown seconds!")
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
                return
            }

            if ((player as LivingEntity).isOnGround) return

            displayParticles(player.location, Particle.FLAME, 50, 0.2, 0.2, 0.2)
            val direction = player.location.direction
            direction.y = 0.8
            direction.x *= 0.8
            direction.z *= 0.8

            player.velocity = direction
            magik.cooldowns[player] = defaultCooldown
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

        if (rod.check(event.itemDrop.itemStack)) {
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

        if (rod.check(item) && event.inventory.type != InventoryType.CRAFTING) {
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
            if (rod.check(item)) {
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