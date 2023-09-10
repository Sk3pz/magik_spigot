package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.colorize
import es.skepz.magik.tuodlib.playSound
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Elf(magik: Magik) : Race(magik) {

    private val bowKey = NamespacedKey(magik, "elven_bow")

    override fun update(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 2, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 2, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 2, 0, false, false))

        if (player.location.block.lightFromSky < 10 && player.location.block.lightFromBlocks < 7) {
            player.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 40, 1, false, false))
        }
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.BOW, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&2&lElf")))
            it.lore(listOf(
                Component.text(colorize("&7Great for players who prefer ranged PVP")),
                Component.text(colorize("&7- &aLongbow: Shoots more powerful arrows")),
                Component.text(colorize("&7- &aIncreased speed")),
                Component.text(colorize("&7- &aJump boost")),
                Component.text(colorize("&7- &cPermanent weakness")),
                Component.text(colorize("&7- &cTrouble seeing without proper light"))))
        }

        return item
    }

    private fun generateBow(): ItemStack {
        val pick = ItemStack(Material.BOW)
        pick.itemMeta = pick.itemMeta.also {
            it.displayName(Component.text(colorize("&2Longbow")))
            it.lore(listOf(Component.text(colorize("&aBuilt with precision to do maximum damage"))))
            it.isUnbreakable = true
            it.persistentDataContainer.set(bowKey, PersistentDataType.DOUBLE, Math.PI)
            it.addEnchant(Enchantment.ARROW_DAMAGE, 5, true)
            it.addEnchant(Enchantment.ARROW_INFINITE, 1, true)
            it.addEnchant(Enchantment.ARROW_KNOCKBACK, 4, true)
        }
        return pick
    }

    private fun checkBow(item: ItemStack): Boolean {
        if (!item.hasItemMeta()) return false
        return item.itemMeta.persistentDataContainer.has(bowKey, PersistentDataType.DOUBLE)
    }

    private fun Player.isElf(): Boolean {
        return getRace(magik, this) is Elf
    }

    override fun name(): String {
        return "Elf"
    }

    override fun set(player: Player) {
        val inv = player.inventory
        inv.addItem(generateBow())
        if (!inv.contains(Material.ARROW)) {
            inv.addItem(ItemStack(Material.ARROW, 1))
        }
    }

    override fun remove(player: Player) {
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (checkBow(item)) {
                inv.remove(item)
            }
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isElf() }
            ?: return

        if (checkBow(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = (event.whoClicked as? Player)?.takeIf { it.isElf() }
            ?: return

        val item = event.currentItem
            ?: return

        if (checkBow(item) && event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isElf()) return
        val drops = event.drops
        drops.forEach { item ->
            if (item == null) return@forEach
            if (checkBow(item)) {
                drops.remove(item)
            }
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isElf() }
            ?: return

        setRace(magik, player, this)
    }
}