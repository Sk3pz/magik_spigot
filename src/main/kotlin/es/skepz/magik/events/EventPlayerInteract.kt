package es.skepz.magik.events

import es.skepz.magik.Backpack
import es.skepz.magik.Magik
import es.skepz.magik.races.createInventory
import es.skepz.magik.tuodlib.wrappers.CoreEvent
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class EventPlayerInteract(val magik: Magik) : CoreEvent(magik) {

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val item = event.item ?: return
        val action = event.action
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) return
        val player = event.player

        if (magik.specialItems.changeItem.check(item)) {
            event.isCancelled = true
            createInventory(magik, player)
            if (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE) {
                player.inventory.remove(item)
            }
            return
        }

        if (Backpack.isBackpack(magik, item)) {
            val backpack = Backpack.fromItemStack(magik, item) ?: return
            event.isCancelled = true
            backpack.createInventory(player)
            return
        }
    }

}