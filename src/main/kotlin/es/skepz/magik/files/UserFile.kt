package es.skepz.magik.files

import es.skepz.magik.Magik
import org.bukkit.entity.Player
import es.skepz.magik.races.Race
import es.skepz.magik.races.raceFromName
import es.skepz.magik.tuodlib.wrappers.CFGFile

class UserFile(private val plugin: Magik, player: Player) : CFGFile(plugin, player.uniqueId.toString(), "users") {

    init {
        set("name", player.name)
    }

    fun getRace(): Race? {
        return raceFromName(plugin, cfg.getString("race").orEmpty())
    }

    fun setRace(r: Race) {
        set("race", r.name())
    }

}