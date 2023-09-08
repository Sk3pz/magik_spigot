package es.skepz.magik.events

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.wrappers.CoreEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent

class EventPlayerLeave(val magik: Magik) : CoreEvent(magik) {

    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {
        val player = event.player

        magik.userFiles.remove(player.uniqueId)
        val race = magik.players[player.uniqueId] ?: return
        race.remove(player)
    }
}