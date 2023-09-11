package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.colorize
import es.skepz.magik.tuodlib.playSound
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
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

class Aquarian(magik: Magik) : Race(magik) {

    private val tridentKey = NamespacedKey(magik, "aquarian_trident")

    override fun update(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.DOLPHINS_GRACE, 2, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.WATER_BREATHING, 2, 0, false, false))

        if (player.location.block.type != Material.WATER) {
            player.maxHealth = 18.0
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 2, 1, false, false))
            if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION)
            }
        } else {
            player.resetMaxHealth()
            player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 2, 1, false, false))
            player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, 360, 0, false, false))
        }
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.TRIDENT, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&3&lAquarian")))
            it.lore(listOf(
                Component.text(colorize("&7Great for Dihydrogen Monoxide enjoyers")),
                Component.text(colorize("&7(&cWIP, SUBJECT TO CHANGE&7)")),
                Component.text(colorize("&7- &aTrident: The power of the seas at your fingertips")),
                Component.text(colorize("  &7- Please note the trident may change in the next update")),
                Component.text(colorize("&7- &aSee better underwater")),
                Component.text(colorize("&7- &aCan breathe underwater")),
                Component.text(colorize("&7- &aFaster digging underwater")),
                Component.text(colorize("&7- &aFast swimmer")),
                Component.text(colorize("&7- &cSlow on land")),
                Component.text(colorize("&7- &cDecreased health out of water")),
            ))
        }

        return item
    }

    // consider: modes to switch between loyalty and riptide?

    private fun generateTrident(): ItemStack {
        val item = ItemStack(Material.TRIDENT)
        item.itemMeta = item.itemMeta.also {
            it.displayName(Component.text(colorize("&3Posideon's Trident")))
            it.lore(listOf(Component.text(colorize("&6The power of the sea in your fingertips"))))
            it.isUnbreakable = true
            it.persistentDataContainer.set(tridentKey, PersistentDataType.DOUBLE, Math.PI)
            it.addEnchant(Enchantment.LOYALTY, 3, true)
            it.addEnchant(Enchantment.IMPALING, 7, true)
        }
        return item
    }

    private fun checkTrident(item: ItemStack): Boolean {
        if (!item.hasItemMeta()) return false
        return item.itemMeta.persistentDataContainer.has(tridentKey, PersistentDataType.DOUBLE)
    }

    private fun Player.isAquarian(): Boolean {
        return getRace(magik, this) is Aquarian
    }

    override fun name(): String {
        return "Aquarian"
    }

    override fun set(player: Player) {
        val inv = player.inventory
        inv.addItem(generateTrident())
        if (!inv.contains(Material.ARROW)) {
            inv.addItem(ItemStack(Material.ARROW, 1))
        }
    }

    override fun remove(player: Player) {
        player.resetMaxHealth()
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (checkTrident(item)) {
                inv.remove(item)
            }
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isAquarian() }
            ?: return

        if (checkTrident(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = (event.whoClicked as? Player)?.takeIf { it.isAquarian() }
            ?: return

        val item = event.currentItem
            ?: return

        if (checkTrident(item) && (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.inventory.type == InventoryType.ANVIL)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isAquarian()) return
        val drops = event.drops
        drops.forEach { item ->
            if (item == null) return@forEach
            if (checkTrident(item)) {
                drops.remove(item)
            }
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isAquarian() }
            ?: return

        setRace(magik, player, this, false)
    }
}