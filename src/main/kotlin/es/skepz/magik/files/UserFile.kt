package es.skepz.magik.files

import es.skepz.magik.Magik
import es.skepz.magik.races.Race
import es.skepz.magik.races.raceFromName
import es.skepz.magik.skepzlib.wrappers.CFGFile
import org.bukkit.entity.Player

class UserFile(plugin: Magik, player: Player) : CFGFile(plugin, player.uniqueId.toString(), "users") {

    init {
        set("name", player.name)
    }

    fun getRace(): Race? {
        return raceFromName(plugin as Magik, cfg.getString("race").orEmpty())
    }

    fun setRace(r: Race) {
        set("race", r.name())
    }

}