package es.skepz.magik.races

import es.skepz.magik.CustomItem
import es.skepz.magik.Magik
import es.skepz.magik.skepzlib.colorize
import es.skepz.magik.skepzlib.playSound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
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

class Leprechaun(magik: Magik) : Race(magik) {

    private val brush = CustomItem(magik, Material.BRUSH, 1, "&eLeatherworking Tool",
        listOf("&aCraft unique items", "&8[&6Right Click&8] &7to open the leatherworking menu."),
        "leprechaun_brush", false)

    override fun update(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, 2, 2, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 2, 1, false, false))
    }

    override fun comingSoon(): Boolean {
        return true
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.GOLD_NUGGET, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(colorize("&a&l&kLeprechaun"))
            it.lore(listOf(
                colorize("&7&kThe lucky leather worker"),
                colorize("&7- &a&kLeather working stick: Craft unique items like backpacks"),
                colorize("&7- &a&kVery lucky"),
                colorize("&7- &c&kNot good at combat"),
                colorize("&7- &c&kFaster movement speed"),
                colorize("&7- &c&k1 block tall"),
            ))
        }

        return item
    }

    private fun Player.isLeprechaun(): Boolean {
        return getRace(magik, this) is Leprechaun
    }

    override fun name(): String {
        return "Leprechaun"
    }

    override fun set(player: Player) {
        player.getAttribute(Attribute.GENERIC_SCALE)?.baseValue = 0.5
        val inv = player.inventory
        inv.addItem(brush.generate())
        if (!inv.contains(Material.ARROW)) {
            inv.addItem(ItemStack(Material.ARROW, 1))
        }
    }

    override fun remove(player: Player) {
        player.getAttribute(Attribute.GENERIC_SCALE)?.baseValue = 1.0
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (brush.check(item)) {
                inv.remove(item)
            }
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isLeprechaun() }
            ?: return

        if (brush.check(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = (event.whoClicked as? Player)?.takeIf { it.isLeprechaun() }
            ?: return

        val item = event.currentItem
            ?: return

        if (brush.check(item) && event.inventory.type != InventoryType.CRAFTING) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isLeprechaun()) return
        val drops = event.drops
        val remove = mutableListOf<ItemStack>()
        drops.forEach { item ->
            if (item == null) return@forEach
            if (brush.check(item)) {
                remove.add(item)
            }
        }
        remove.forEach { item ->
            drops.remove(item)
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isLeprechaun() }
            ?: return

        setRace(magik, player, this, false)
    }
}