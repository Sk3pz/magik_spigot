package es.skepz.magik.races

import es.skepz.magik.Magik
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import es.skepz.magik.tuodlib.colorize

class Orc(magik: Magik) : Race(magik) {

    override fun update(player: Player) {

    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.NETHERITE_AXE, 1)
        val meta = item.itemMeta
        meta.isUnbreakable = true
        meta.displayName(Component.text(colorize("&4&lOrc")))
        meta.lore(listOf(
            Component.text(colorize("&7- &aBattle Axe - more damage, slower speed")),
            Component.text(colorize("&7- &aJump boost")),
            Component.text(colorize("&7- &aStrength")),
            Component.text(colorize("&7- &a+10 max hearts")),
            Component.text(colorize("&7- &cSlowness 2"))))
        item.setItemMeta(meta)

        return item
    }

    override fun name(): String {
        return "Orc"
    }

    override fun set(player: Player) {
        // TODO
    }

    override fun remove(player: Player) {
        // TODO
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {

    }
}