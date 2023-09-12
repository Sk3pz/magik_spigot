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

class Goblin(magik: Magik) : Race(magik) {

    private val swordKey = NamespacedKey(magik, "goblin_knife")

    override fun update(player: Player) {

    }

    override fun comingSoon(): Boolean {
        return true
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.IRON_SWORD, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&c&lGoblin")))
            it.lore(listOf(
                // TODO: CHANGE THIS FOR ITS RELEASE!!!
                Component.text(colorize("&cCOMING SOON")),
                Component.text(colorize("&7Great for players who love to move quick and make fast attacks")),
                Component.text(colorize("&7- &aDagger: attack fast and efficiently")),
                Component.text(colorize("&7- &aAmong the quickest around")),
                Component.text(colorize("&7- &a&kImmune to poison")),
                Component.text(colorize("&7- &c&kCan only wear leather armor")),
                Component.text(colorize("&7- &c6 max hearts"))))
        }

        return item
    }

    private fun generateSword(): ItemStack {
        val item = ItemStack(Material.IRON_SWORD)
        item.itemMeta = item.itemMeta.also {
            it.displayName(Component.text(colorize("&eDagger")))
            it.lore(listOf(
                Component.text(colorize("&aQuick and efficient strikes")),
                Component.text(colorize("&8[&6Right Click&8] &7to dash."))
            ))
            it.isUnbreakable = true
            it.persistentDataContainer.set(swordKey, PersistentDataType.DOUBLE, Math.PI)
//            it.addEnchant(Enchantment.DAMAGE_ALL, 5, true)
//            it.addEnchant(Enchantment.KNOCKBACK, 2, true)
        }
        return item
    }

    private fun checkSword(item: ItemStack): Boolean {
        if (!item.hasItemMeta()) return false
        return item.itemMeta.persistentDataContainer.has(swordKey, PersistentDataType.DOUBLE)
    }

    private fun Player.isGoblin(): Boolean {
        return getRace(magik, this) is Goblin
    }

    override fun name(): String {
        return "Goblin"
    }

    override fun set(player: Player) {
        player.maxHealth = 12.0
        val inv = player.inventory
        inv.addItem(generateSword())
    }

    override fun remove(player: Player) {
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (checkSword(item)) {
                inv.remove(item)
            }
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isGoblin() }
            ?: return

        if (checkSword(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = (event.whoClicked as? Player)?.takeIf { it.isGoblin() }
            ?: return

        val item = event.currentItem
            ?: return

        if (checkSword(item) && (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.inventory.type == InventoryType.ANVIL)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isGoblin()) return
        val drops = event.drops
        drops.forEach { item ->
            if (item == null) return@forEach
            if (checkSword(item)) {
                drops.remove(item)
            }
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isGoblin() }
            ?: return

        setRace(magik, player, this, false)
    }
}