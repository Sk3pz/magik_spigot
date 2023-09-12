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

class Enderian(magik: Magik) : Race(magik) {

    private val swordKey = NamespacedKey(magik, "enderian_sword")

    override fun update(player: Player) {
        if (player.location.world.environment == World.Environment.THE_END) {
            player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 2, 0, false, false))
        }
    }

    override fun comingSoon(): Boolean {
        return true
    }

    override fun guiDisplayItem(): ItemStack {
        //val item = ItemStack(Material.ENDER_EYE, 1) TODO
        val item = ItemStack(Material.BARRIER, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&5&l&kEnderian")))
            it.lore(listOf(
                // TODO: CHANGE THIS FOR ITS RELEASE!!!
                Component.text(colorize("&cCOMING SOON")),
                Component.text(colorize("&7&kThe power of the end in your fingertips")),
                Component.text(colorize("&7- &a&kSword of the End: Teleport where you look")),
                Component.text(colorize("&7- &aSneak for silk touch")),
                Component.text(colorize("&7- &a&kRegenerate health in the end")),
                Component.text(colorize("&7- &c&kAllergic to water (&knot rain&r&c)")),
                Component.text(colorize("&7- &cCan't use elytra"))))
        }

        return item
    }

    private fun generateSword(): ItemStack {
        val item = ItemStack(Material.DIAMOND_SWORD)
        item.itemMeta = item.itemMeta.also {
            it.displayName(Component.text(colorize("&5Sword of the End")))
            it.lore(listOf(
                Component.text(colorize("&aThe power of the end in your fingertips")),
                Component.text(colorize("&8[&6Right Click&8] &7to teleport."))
            ))
            it.isUnbreakable = true
            it.persistentDataContainer.set(swordKey, PersistentDataType.DOUBLE, Math.PI)
            it.addEnchant(Enchantment.DAMAGE_ALL, 5, true)
            it.addEnchant(Enchantment.KNOCKBACK, 2, true)
        }
        return item
    }

    private fun checkSword(item: ItemStack): Boolean {
        if (!item.hasItemMeta()) return false
        return item.itemMeta.persistentDataContainer.has(swordKey, PersistentDataType.DOUBLE)
    }

    private fun Player.isEnderian(): Boolean {
        return getRace(magik, this) is Enderian
    }

    override fun name(): String {
        return "Enderian"
    }

    override fun set(player: Player) {
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
        val player = event.player.takeIf { it.isEnderian() }
            ?: return

        if (checkSword(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = (event.whoClicked as? Player)?.takeIf { it.isEnderian() }
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
        if (!event.player.isEnderian()) return
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
        val player = event.player.takeIf { it.isEnderian() }
            ?: return

        setRace(magik, player, this, false)
    }
}