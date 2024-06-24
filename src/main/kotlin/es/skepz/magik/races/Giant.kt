package es.skepz.magik.races

import es.skepz.magik.CustomItem
import es.skepz.magik.Magik
import es.skepz.magik.skepzlib.colorize
import es.skepz.magik.skepzlib.playSound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack

class Giant(magik: Magik) : Race(magik) {

    private val mace = CustomItem(magik, Material.BOW, 1, "&cSledge Hammer",
        listOf("&aBuilt to deal damage"),
        "giant_mace", true,
        mutableMapOf(Pair(Enchantment.SHARPNESS, 5),
            Pair(Enchantment.DENSITY, 5),
            Pair(Enchantment.BREACH, 4)))

    override fun update(player: Player) {

    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.BOW, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(colorize("&a&lGiant"))
            it.lore(listOf(
                colorize("&7Hard to get in normal sized spaces!"),
                colorize("&7- &aSledge Hammer: Beat your enemies to a pulp"),
                colorize("&7- &aIncreased size (massive!)"),
                colorize("&7- &aStep and jump over several blocks high!"),
                colorize("&7- &aImproved reach"),
                colorize("&7- &a3x the normal health"),
                colorize("&7- &cCan't fit in small spaces"),
                colorize("&7- &cVery large target"),
                colorize("&7- &cSlightly slower"),
            ))
        }

        return item
    }

    private fun Player.isGiant(): Boolean {
        return getRace(magik, this) is Giant
    }

    override fun name(): String {
        return "Giant"
    }

    override fun set(player: Player) {
        player.getAttribute(Attribute.GENERIC_SCALE)?.baseValue = 10.0
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 60.0
        player.getAttribute(Attribute.GENERIC_STEP_HEIGHT)?.baseValue = 2.5
        player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE)?.baseValue = 14.0
        player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)?.baseValue = 12.0
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = 0.08
        player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)?.baseValue = 0.8

        val inv = player.inventory
        inv.addItem(mace.generate())
    }

    override fun remove(player: Player) {
        player.getAttribute(Attribute.GENERIC_SCALE)?.baseValue = 1.0
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
        player.getAttribute(Attribute.GENERIC_STEP_HEIGHT)?.baseValue = 0.6
        player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE)?.baseValue = 4.5
        player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)?.baseValue = 3.0
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = 0.1
        player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)?.baseValue = 0.8

        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (mace.check(item)) {
                inv.remove(item)
            }
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isGiant() }
            ?: return

        if (mace.check(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = (event.whoClicked as? Player)?.takeIf { it.isGiant() }
            ?: return

        val item = event.currentItem
            ?: return

        if (mace.check(item) && event.inventory.type != InventoryType.CRAFTING) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isGiant()) return
        val drops = event.drops
        val remove = mutableListOf<ItemStack>()
        drops.forEach { item ->
            if (item == null) return@forEach
            if (mace.check(item)) {
                remove.add(item)
            }
        }
        remove.forEach { item ->
            drops.remove(item)
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isGiant() }
            ?: return

        setRace(magik, player, this, false)
    }
}