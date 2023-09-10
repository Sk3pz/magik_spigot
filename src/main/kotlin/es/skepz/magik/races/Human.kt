package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.colorize
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class Human(magik: Magik) : Race(magik) {

    override fun update(player: Player) {

    }

    override fun guiDisplayItem(): ItemStack {

        val item = ItemStack(Material.PLAYER_HEAD, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&7&lHuman")))
            it.lore(listOf(
                Component.text(colorize("&7For players who want the vanilla experience")),
                Component.text(colorize("&7- No buffs or debuffs"))
            ))
        }

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