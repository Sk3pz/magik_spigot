package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.colorize
import es.skepz.magik.tuodlib.playSound
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
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

class Shulker(magik: Magik) : Race(magik) {

    private val bowKey = NamespacedKey(magik, "elven_bow")

    override fun update(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 2, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 2, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 2, 0, false, false))

//        if (player.location.block.lightFromSky < 10 && player.location.block.lightFromBlocks < 7) {
//            player.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 40, 1, false, false))
//        }
    }

    override fun comingSoon(): Boolean {
        return true
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.BARRIER, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&d&l&kShulker")))
            it.lore(listOf(
                Component.text(colorize("&7&k??????????????")),
                Component.text(colorize("&7- &a&kinsert item here")),
                Component.text(colorize("&7- &a&kExtremely strong at night")),
                Component.text(colorize("&7- &a&kTame wolves with no bones")),
                Component.text(colorize("&7- &c&kWeak during the day")),
                Component.text(colorize("&7- &c&kEven weaker in other dimensions")),
                //Component.text(colorize("&7- &cTrouble seeing in the dark"))
            ))
        }

        return item
    }

    private fun generateBow(): ItemStack {
        val item = ItemStack(Material.BOW)

        return item
    }

    private fun checkBow(item: ItemStack): Boolean {
        if (!item.hasItemMeta()) return false
        return item.itemMeta.persistentDataContainer.has(bowKey, PersistentDataType.DOUBLE)
    }

    private fun Player.isElf(): Boolean {
        return getRace(magik, this) is Elf
    }

    override fun name(): String {
        return "Shulker"
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

        if (checkBow(item) && (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.inventory.type == InventoryType.ANVIL)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isElf()) return
        val drops = event.drops
        val remove = mutableListOf<ItemStack>()
        drops.forEach { item ->
            if (item == null) return@forEach
            if (checkBow(item)) {
                remove.add(item)
            }
        }
        remove.forEach { item ->
            drops.remove(item)
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isElf() }
            ?: return

        setRace(magik, player, this, false)
    }
}