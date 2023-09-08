package es.skepz.magik.races

import es.skepz.magik.Magik
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import es.skepz.magik.tuodlib.colorize

class Human(magik: Magik) : Race(magik) {

    override fun update(player: Player) {

    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.ELYTRA, 1)
        val meta = item.itemMeta
        meta.isUnbreakable = true
        meta.displayName(Component.text(colorize("&7&lHuman")))
        meta.lore(listOf(
            Component.text(colorize("&7- No buffs or debuffs"))))
        item.setItemMeta(meta)

        return item
    }

    override fun name(): String {
        return "Human"
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