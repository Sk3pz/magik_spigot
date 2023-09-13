package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.colorize
import es.skepz.magik.tuodlib.playSound
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.World
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

class Lycan(magik: Magik) : Race(magik) {

    private val clawKey = NamespacedKey(magik, "lycan_claw")
    private val clawName = colorize("&cLycan Claw")

    override fun update(player: Player) {
        val world = player.location.world
        if (world.environment != World.Environment.NORMAL) {

            return
        }
        if (!player.location.world.isDayTime) {
            // day

        } else {
            // night

        }
    }

    override fun comingSoon(): Boolean {
        return true
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.BONE, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&4&l&kLycan")))
            it.lore(listOf(
                Component.text(colorize("&7&kAll the benefits of a werewolf, without the wolf part")),
                Component.text(colorize("&7- &a&kWolf Claw: Attacks stronger at night")),
                Component.text(colorize("&7- &a&kExtremely strong at night")),
                Component.text(colorize("&7- &a&kTame wolves with no bones")),
                Component.text(colorize("&7- &c&kWeak during the day")),
                Component.text(colorize("&7- &c&kEven weakerer in other dimensions (no moon)")),
                //Component.text(colorize("&7- &cTrouble seeing in the dark"))
            ))
        }

        return item
    }

    private fun generateClaw(): ItemStack {
        val item = ItemStack(Material.PRISMARINE_SHARD)
        item.itemMeta = item.itemMeta.also {
            it.displayName(Component.text(clawName))
        }
        return item
    }

    private fun checkClaw(item: ItemStack): Boolean {
        if (!item.hasItemMeta()) return false
        return item.itemMeta.persistentDataContainer.has(clawKey, PersistentDataType.DOUBLE)
    }

    private fun Player.isLycan(): Boolean {
        return getRace(magik, this) is Lycan
    }

    override fun name(): String {
        return "Lycan"
    }

    override fun set(player: Player) {
        val inv = player.inventory
        inv.addItem(generateClaw())
        if (!inv.contains(Material.ARROW)) {
            inv.addItem(ItemStack(Material.ARROW, 1))
        }
    }

    override fun remove(player: Player) {
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (checkClaw(item)) {
                inv.remove(item)
            }
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isLycan() }
            ?: return

        if (checkClaw(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = (event.whoClicked as? Player)?.takeIf { it.isLycan() }
            ?: return

        val item = event.currentItem
            ?: return

        if (checkClaw(item) && (event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.inventory.type == InventoryType.ANVIL)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isLycan()) return
        val drops = event.drops
        val remove = mutableListOf<ItemStack>()
        drops.forEach { item ->
            if (item == null) return@forEach
            if (checkClaw(item)) {
                remove.add(item)
            }
        }
        remove.forEach { item ->
            drops.remove(item)
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isLycan() }
            ?: return

        setRace(magik, player, this, false)
    }
}