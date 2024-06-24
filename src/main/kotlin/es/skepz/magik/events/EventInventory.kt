package es.skepz.magik.events

import es.skepz.magik.Backpack
import es.skepz.magik.Magik
import es.skepz.magik.races.createInventory
import es.skepz.magik.races.getRace
import es.skepz.magik.races.raceFromName
import es.skepz.magik.races.setRace
import es.skepz.magik.skepzlib.playSound
import es.skepz.magik.skepzlib.sendMessage
import es.skepz.magik.skepzlib.wrappers.CoreEvent
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class EventInventory(val magik: Magik) : CoreEvent(magik) {

    private fun checkInventory(inv: Inventory): Boolean {
        return magik.inventories.contains(inv)
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val p = event.whoClicked as Player

        if (Backpack.isBackpackInv(magik, event.inventory)) {
            val clicked = event.currentItem ?: return
            if (Backpack.isBackpack(magik, clicked)) {
                event.isCancelled = true
                playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
                return
            }
        }

        val inv = event.clickedInventory?.takeIf { checkInventory(it) } ?: return
        val clicked = event.currentItem ?: return

        event.isCancelled = true // cancel all inventory clicks in the custom gui

        if (!clicked.hasItemMeta()) return
        val container = clicked.itemMeta.persistentDataContainer

        magik.guiKeys.forEach {
            if (container.has(it, PersistentDataType.STRING)) {
                val raceName = container.get(it, PersistentDataType.STRING) ?: return
                val race = raceFromName(magik, raceName) ?: return
                setRace(magik, p, race, true)
                inv.close()
                return
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
            return
        }

        if (Backpack.isBackpackInv(magik, inv)) {
            val id = magik.backpacks[inv] ?: return
            magik.backpacks.remove(inv)
            val backpack = Backpack.find(magik, event.player.inventory, id) ?: return
            val items = mutableMapOf<Int, ItemStack>()
            inv.contents.forEachIndexed { slot, item ->
                items[slot] = item ?: ItemStack(Material.AIR)
            }
            backpack.saveData(items)
        }

    }

}