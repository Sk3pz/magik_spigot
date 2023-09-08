package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.colorize
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class Elf(magik: Magik) : Race(magik) {

    override fun update(player: Player) {

    }

    override fun guiDisplayItem(): ItemStack {

        val item = ItemStack(Material.BOW, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&2&lElf")))
            it.lore(listOf(
                Component.text(colorize("&7- &aLongbow: Shoots more powerful arrows")),
                Component.text(colorize("&7- &aIncreased speed")),
                Component.text(colorize("&7- &aJump boost")),
                Component.text(colorize("&7- &cPermanent weakness")),
                Component.text(colorize("&7- &cBlindness underground"))))
        }

        return item
    }

    override fun name(): String {
        return "Elf"
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