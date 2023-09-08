package es.skepz.magik.events

import es.skepz.magik.Magik
import es.skepz.magik.files.UserFile
import es.skepz.magik.races.setRace
import es.skepz.magik.tuodlib.wrappers.CoreEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent

class EventPlayerJoin(val magik: Magik) : CoreEvent(magik) {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        val file = UserFile(magik, player)
        magik.userFiles[player.uniqueId] = file
        val race = file.getRace()
        if (race == null) {
            // TODO: Race selection here
            return
        }
        setRace(magik, player, race)
    }
}