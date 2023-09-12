package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.colorize
import es.skepz.magik.tuodlib.playSound
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Blazeborn(magik: Magik) : Race(magik) {

    private val rodKey = NamespacedKey(magik, "blazeborn_rod")

    override fun update(player: Player) {
        if (player.location.world.environment == World.Environment.NETHER) {
            player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 2, 0, false, false))
        }
        if (player.location.world.environment == World.Environment.NORMAL) {
            player.maxHealth = 18.0
        } else {
            player.resetMaxHealth()
        }
    }

    override fun comingSoon(): Boolean {
        return true
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.BLAZE_ROD, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&6&lBlazeborn")))
            it.lore(listOf(
                // TODO: CHANGE THIS FOR ITS RELEASE!!!
                Component.text(colorize("&cCOMING SOON")),
                Component.text(colorize("&7Feel the lava pulsing in your veins")), // change this
                Component.text(colorize("&7- &aRod of Fire: Boost yourself with fire")),
                Component.text(colorize("&7- &a&kImmune to fire damage")),
                Component.text(colorize("&7- &a&kRegenerate health in the nether")),
                Component.text(colorize("&7- &cAllergic to water")),
                Component.text(colorize("&7- &c&kLess health in the overworld"))))
        }

        return item
    }

    private fun generateRod(): ItemStack {
        val item = ItemStack(Material.BLAZE_ROD)
        item.itemMeta = item.itemMeta.also {
            it.displayName(Component.text(colorize("&6Rod of Fire")))
            it.lore(listOf(
                Component.text(colorize("&aA flaming rod only you can wield")),
                Component.text(colorize("&8[&6Right Click&8] &7to fire boost."))
            ))
            it.isUnbreakable = true
            it.persistentDataContainer.set(rodKey, PersistentDataType.DOUBLE, Math.PI)
            it.addEnchant(Enchantment.DAMAGE_ALL, 5, true)
            it.addEnchant(Enchantment.KNOCKBACK, 2, true)
        }
        return item
    }

    private fun checkRod(item: ItemStack): Boolean {
        if (!item.hasItemMeta()) return false
        return item.itemMeta.persistentDataContainer.has(rodKey, PersistentDataType.DOUBLE)
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
        drops.forEach { item ->
            if (item == null) return@forEach
            if (checkRod(item)) {
                drops.remove(item)
            }
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isBlazeborn() }
            ?: return

        setRace(magik, player, this, false)
    }
}