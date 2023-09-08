package es.skepz.magik.events

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.wrappers.CoreEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class EventInventory(val magik: Magik) : CoreEvent(magik) {

    private fun checkInventory(inv: Inventory): Boolean {
        return magik.inventories.contains(inv)
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val p = event.whoClicked as Player
        val inv = event.clickedInventory ?: return
        if (!checkInventory(inv)) return
        event.isCancelled = true // cancel all inventory clicks in the custom gui

        // todo: add selection
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        val inv = event.inventory
        if (checkInventory(inv)) {
            magik.inventories.remove(inv)
        }
    }

}