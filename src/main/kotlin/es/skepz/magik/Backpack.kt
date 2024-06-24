package es.skepz.magik

import es.skepz.magik.skepzlib.colorize
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class Backpack(val magik: Magik, private val uuid: UUID, private val size: Int = 9) : ItemStack(Material.CHEST) {

    constructor(magik: Magik, size: Int = 9) : this(magik, UUID.randomUUID(), size)

    init {
        itemMeta = itemMeta.also {
            it.displayName(colorize("&eBackpack"))
            it.lore(mutableListOf(
                colorize("&7Size: &c$size")
            ))
            it.persistentDataContainer.set(magik.backpackKey, PersistentDataType.STRING, "$uuid,$size")
        }
    }

    companion object {
        fun fromItemStack(magik: Magik, item: ItemStack): Backpack? {
            val persistent = fromPersistent(magik, item) ?: return null
            val uuid = persistent.first
            val size = persistent.second

            return Backpack(magik, uuid, size)
        }

        fun isBackpackInv(magik: Magik, inv: Inventory): Boolean {
            return magik.backpacks.contains(inv)
        }

        fun isBackpack(magik: Magik, checked: ItemStack): Boolean {
            if (!checked.hasItemMeta()) return false
            return checked.itemMeta.persistentDataContainer.has(magik.backpackKey, PersistentDataType.STRING)
        }

        fun find(magik: Magik, inventory: Inventory, uuid: UUID): Backpack? {
            inventory.contents.forEach {
                if (it == null) return@forEach
                if (!isBackpack(magik, it)) return@forEach
                val bp = fromItemStack(magik, it) ?: return@forEach
                if (bp.uuid == uuid) return bp
            }
            return null
        }

        private fun fromPersistent(magik: Magik, item: ItemStack): Pair<UUID, Int>? {
            if (!isBackpack(magik, item)) return null
            val data = item.itemMeta.persistentDataContainer.get(magik.backpackKey, PersistentDataType.STRING) ?: return null
            val split = data.split(",")
            if (split.size != 2) return null
            val uuid = UUID.fromString(split.first())
            val size = split.last().toInt()
            return Pair(uuid, size)
        }
    }

    fun createInventory(player: Player) {
        val inv = Bukkit.createInventory(player, size, colorize("&eBackpack"))

        val items = this.loadData()

        items.forEach { (slot, item) ->
            inv.setItem(slot, item)
        }

        magik.backpacks[inv] = uuid

        player.openInventory(inv)
    }

    fun loadData(): Map<Int, ItemStack> {
        val map = mutableMapOf<Int, ItemStack>()
        val config = magik.backpackCFG.cfg
        for (x in 0..<size) {
            map[x] = config.getItemStack("$uuid.$x") ?: ItemStack(Material.AIR)
        }
        return map
    }

    fun saveData(items: Map<Int, ItemStack>) {
        items.forEach { (slot, item) ->
            magik.backpackCFG["$uuid.$slot"] = item
        }
    }

}