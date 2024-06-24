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
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Orc(magik: Magik) : Race(magik) {

    private val maxHealth = 40.0
    private val axe = CustomItem(magik, Material.IRON_AXE, 1, "&4Battle Axe",
        listOf("&cME ORC. ME DESTROY YOU."),
        "orc_axe", true,
        mapOf(Pair(Enchantment.SHARPNESS, 5), Pair(Enchantment.KNOCKBACK, 2)))

    override fun update(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 2, 1, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.STRENGTH, 2, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 2, 1, false, false))

        if (axe.check(player.inventory.itemInMainHand)) {
            player.addPotionEffect(PotionEffect(PotionEffectType.MINING_FATIGUE, 2, 0, false, false))
        }
    }

    override fun guiDisplayItem(): ItemStack {

        val item = ItemStack(Material.NETHERITE_AXE, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(colorize("&4&lOrc"))
            it.lore(listOf(
                colorize("&7Great for players who like to hit hard"),
                colorize("&7- &aBattle Axe - more damage, slower speed"),
                colorize("  &7- &cBad for chopping trees"),
                colorize("&7- &aJumps really high"),
                colorize("&7- &aStronger than others"),
                colorize("&7- &aDouble health (20 hearts)"),
                colorize("&7- &cSlower than most"),
                colorize("&7- &73 blocks tall")
            ))
        }

        return item
    }

    private fun Player.isOrc(): Boolean {
        return getRace(magik, this) is Orc
    }

    override fun name(): String {
        return "Orc"
    }

    override fun set(player: Player) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = maxHealth
        player.getAttribute(Attribute.GENERIC_SCALE)?.baseValue = 1.5
        player.inventory.addItem(axe.generate())
    }

    override fun remove(player: Player) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
        player.getAttribute(Attribute.GENERIC_SCALE)?.baseValue = 1.0
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (axe.check(item)) {
                inv.remove(item)
            }
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isOrc() }
            ?: return

        if (axe.check(event.itemDrop.itemStack)) {
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

        if (axe.check(item) && event.inventory.type != InventoryType.CRAFTING) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isOrc()) return
        val drops = event.drops
        val remove = mutableListOf<ItemStack>()
        drops.forEach { item ->
            if (item == null) return@forEach
            if (axe.check(item)) {
                remove.add(item)
            }
        }
        remove.forEach { item ->
            drops.remove(item)
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isOrc() }
            ?: return

        setRace(magik, player, this, false)
    }
}