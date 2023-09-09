package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.colorize
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

fun start(magik: Magik) {
    Bukkit.getScheduler().runTaskTimer(magik, Runnable {
        for ((u, r) in magik.players) {
            val player = Bukkit.getPlayer(u)
            if (player == null) {
                // not online anymore, needs to be removed
                magik.players.remove(u)
                continue
            }
            r.update(player)
        }
    }, 0L, 1L) // run every second
}

fun raceFromName(magik: Magik, name: String): Race? {
    return magik.races.find { it.name() == name }
}

fun getRaceNames(magik: Magik): List<String> {
    return magik.races.map { it.name() }
}

fun setRace(magik: Magik, player: Player, race: Race) {
    // remove old race if needed
    magik.players[player.uniqueId]?.remove(player)
    // set new race
    magik.players[player.uniqueId] = race
    magik.userFiles[player.uniqueId]?.setRace(race)
    race.set(player)
}

fun getRace(magik: Magik, player: Player): Race? {
    return magik.players[player.uniqueId]
}

fun createInventory(magik: Magik, p: Player) {
    val inv = Bukkit.createInventory(p, 27, Component.text(colorize("&6Pick your race")))
    magik.inventories.add(inv)
    magik.races.forEachIndexed { x, race ->
        val item = race.guiDisplayItem()
        item.itemMeta = item.itemMeta.also {
            it.persistentDataContainer.set(NamespacedKey(magik, race.name()), PersistentDataType.DOUBLE, Math.PI)
        }
        inv.addItem(item)
    }
    p.openInventory(inv)
}

abstract class Race(val magik: Magik) : Listener {

    // registers the race
    fun register() {

        magik.config.default("enabled_races.${name()}", true)

        if (magik.config.cfg.getBoolean("enabled_races.${name()}")) {
            magik.server.pluginManager.registerEvents(this, magik)
            magik.races.add(this)
        }
    }

    // run every second in the scheduler
    abstract fun update(player: Player)

    // get the name of the race as a string
    abstract fun name(): String

    // returns the item in the race selection gui
    abstract fun guiDisplayItem(): ItemStack

    // run when the user first selects the race
    abstract fun set(player: Player)

    // run when the user changes to a different race
    abstract fun remove(player: Player)

}