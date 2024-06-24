package es.skepz.magik

import es.skepz.magik.skepzlib.colorize
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class CustomItem(magik: Magik, type: Material, amount: Int = 1,
                 val name: String, lore: List<String>,
                 keyName: String,
                 unbreakable: Boolean, enchantments: Map<Enchantment, Int> = mutableMapOf())
    : ItemStack(type, amount) {

    private val key = NamespacedKey(magik, keyName)

    init {
        itemMeta = itemMeta.also {
            it.displayName(colorize(name))
            val loreList = lore.map { loreItem ->
                colorize(loreItem)
            }
            it.lore(loreList)
            it.isUnbreakable = unbreakable
            enchantments.forEach { (ench, lvl) ->
                it.addEnchant(ench, lvl, true)
            }
            it.persistentDataContainer.set(key, PersistentDataType.DOUBLE, 1.0)
        }
    }

    fun generate(): ItemStack {
        return this.clone()
    }

    fun check(checked: ItemStack): Boolean {
        if (!checked.hasItemMeta()) return false
        return checked.itemMeta.persistentDataContainer.has(key, PersistentDataType.DOUBLE)
    }

    fun find(inv: Inventory): ItemStack? {
        inv.contents.forEach {
            if (it == null) return@forEach
            if (check(it)) {
                return it
            }
        }
        return null
    }

}