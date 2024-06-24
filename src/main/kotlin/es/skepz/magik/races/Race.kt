package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.skepzlib.colorize
import es.skepz.magik.skepzlib.serverBroadcast
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

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
    }, 0L, 1L) // run every tick
    Bukkit.getScheduler().runTaskTimer(magik, Runnable {
        magik.cooldowns.forEach { (player, seconds) ->
            val race = getRace(magik, player) ?: return@forEach
            if (seconds <= 1) {
                magik.cooldowns.remove(player)
                race.cooldownUpdate(player, seconds)
                return@forEach
            }
            magik.cooldowns[player] = seconds - 1
            race.cooldownUpdate(player, seconds)
        }
    }, 0L, 20L) // run every second
}

fun raceFromName(magik: Magik, name: String): Race? {
    return magik.races.find { it.name() == name }
}

fun getRaceNames(magik: Magik): List<String> {
    return magik.races.map { it.name() }
}

fun setRace(magik: Magik, player: Player, race: Race, new: Boolean) {
    // remove old race if needed
    magik.players[player.uniqueId]?.remove(player)
    // set new race
    magik.players[player.uniqueId] = race
    magik.userFiles[player.uniqueId]?.setRace(race)
    if (new) {
        serverBroadcast("&b${player.name} &7Has chosen to become a &b${race.name()}&7!")
    }
    race.set(player)
    player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 60, 90, false, false))
}

fun getRace(magik: Magik, player: Player): Race? {
    return magik.players[player.uniqueId]
}

fun createInventory(magik: Magik, p: Player) {
    val inv = Bukkit.createInventory(p, 27, colorize("&6Pick your race"))

    if (magik.races.isEmpty()) return

    magik.inventories.add(inv)

    var comingSoonIndex = 0
    magik.races.forEach { race ->
        if (race.comingSoon()) {
            inv.setItem(26-comingSoonIndex, race.guiDisplayItem())
            comingSoonIndex += 1
            return@forEach
        }
        val item = race.guiDisplayItem()

        item.itemMeta = item.itemMeta.also {
            val key = NamespacedKey(magik, race.name())

            it.persistentDataContainer.set(key, PersistentDataType.STRING, race.name())

            magik.guiKeys.add(key)
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

    open fun cooldownUpdate(player: Player, seconds: Int) {}
    open fun comingSoon(): Boolean {
        return false
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