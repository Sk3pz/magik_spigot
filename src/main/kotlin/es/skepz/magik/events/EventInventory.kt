package es.skepz.magik.events

import es.skepz.magik.Magik
import es.skepz.magik.races.createInventory
import es.skepz.magik.races.getRace
import es.skepz.magik.races.raceFromName
import es.skepz.magik.races.setRace
import es.skepz.magik.tuodlib.playSound
import es.skepz.magik.tuodlib.sendMessage
import es.skepz.magik.tuodlib.serverBroadcast
import es.skepz.magik.tuodlib.wrappers.CoreEvent
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataType

class EventInventory(val magik: Magik) : CoreEvent(magik) {

    private fun checkInventory(inv: Inventory): Boolean {
        return magik.inventories.contains(inv)
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {

        val p = event.whoClicked as Player
        val inv = event.clickedInventory?.takeIf { checkInventory(it) } ?: return
        val clicked = event.currentItem ?: return

        event.isCancelled = true // cancel all inventory clicks in the custom gui

        if (!clicked.hasItemMeta()) return
        val container = clicked.itemMeta.persistentDataContainer

        magik.guiKeys.forEach {
            if (container.has(it, PersistentDataType.STRING)) {
                val raceName = container.get(it, PersistentDataType.STRING) ?: return
                val race = raceFromName(magik, raceName) ?: return
                setRace(magik, p, race)
                serverBroadcast("&b${p.name} &7Has chosen to become a &b${raceName}&7!")
                inv.close()
            }
        }
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        val inv = event.inventory

        if (checkInventory(inv)) {
            val player = event.player as Player
            magik.inventories.remove(inv)

            if (getRace(magik, player) == null) {
                magik.server.scheduler.scheduleSyncDelayedTask(magik, {
                    createInventory(magik, player)
                }, 1L)
                sendMessage(player, "&cPlease pick a race. Pick human for no race!")
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            }
        }
    }

}