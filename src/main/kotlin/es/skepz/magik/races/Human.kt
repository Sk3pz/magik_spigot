package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.skepzlib.colorize
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Human(magik: Magik) : Race(magik) {

    override fun update(player: Player) {

    }

    override fun guiDisplayItem(): ItemStack {

        val item = ItemStack(Material.PLAYER_HEAD, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(colorize("&7&lHuman"))
            it.lore(listOf(
               colorize("&7Great for players who want the vanilla experience"),
                colorize("&7- No buffs or debuffs")
            ))
        }

        return item
    }

    override fun name(): String {
        return "Human"
    }

    override fun set(player: Player) {

    }

    override fun remove(player: Player) {

    }
}