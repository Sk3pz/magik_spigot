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

class Orc(magik: Magik) : Race(magik) {

    private val maxHealth = 40.0
    private val axeKey = NamespacedKey(magik, "orc_axe")

    override fun update(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 2, 1, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 2, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 2, 1, false, false))

        if (checkAxe(player.inventory.itemInMainHand)) {
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING, 2, 0, false, false))
        }
    }

    override fun guiDisplayItem(): ItemStack {

        val item = ItemStack(Material.NETHERITE_AXE, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&4&lOrc")))
            it.lore(listOf(
                Component.text(colorize("&7Great for players who like to hit hard")),
                Component.text(colorize("&7- &aBattle Axe - more damage, slower speed")),
                Component.text(colorize("  &7- &cBad for chopping trees")),
                Component.text(colorize("&7- &aJumps really high")),
                Component.text(colorize("&7- &aStronger than others")),
                Component.text(colorize("&7- &aDouble health (20 hearts)")),
                Component.text(colorize("&7- &cSlower than most"))))
        }

        return item
    }

    private fun generateAxe(): ItemStack {
        val item = ItemStack(Material.IRON_AXE)
        item.itemMeta = item.itemMeta.also {
            it.displayName(Component.text(colorize("&4Battle Axe")))
            it.lore(listOf(Component.text(colorize("&cME ORC. ME DESTROY YOU."))))
            it.isUnbreakable = true
            it.persistentDataContainer.set(axeKey, PersistentDataType.DOUBLE, Math.PI)
            it.addEnchant(Enchantment.DAMAGE_ALL, 7, true)
            it.addEnchant(Enchantment.KNOCKBACK, 2, true)
        }
        return item
    }

    private fun checkAxe(item: ItemStack): Boolean {
        if (!item.hasItemMeta()) return false
        return item.itemMeta.persistentDataContainer.has(axeKey, PersistentDataType.DOUBLE)
    }

    private fun Player.isOrc(): Boolean {
        return getRace(magik, this) is Orc
    }

    override fun name(): String {
        return "Orc"
    }

    override fun set(player: Player) {
        player.maxHealth = maxHealth
        player.inventory.addItem(generateAxe())
    }

    override fun remove(player: Player) {
        player.resetMaxHealth()
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (checkAxe(item)) {
                inv.remove(item)
            }
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isOrc() }
            ?: return

        if (checkAxe(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = (event.whoClicked as? Player)?.takeIf { it.isOrc() }
            ?: return

        val item = event.currentItem
            ?: return

        if (checkAxe(item) && (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.inventory.type == InventoryType.ANVIL)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isOrc()) return
        val drops = event.drops
        drops.forEach { item ->
            if (item == null) return@forEach
            if (checkAxe(item)) {
                drops.remove(item)
            }
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isOrc() }
            ?: return

        setRace(magik, player, this, false)
    }
}