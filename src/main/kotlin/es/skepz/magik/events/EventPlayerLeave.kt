package es.skepz.magik.events

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.wrappers.CoreEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent

class EventPlayerLeave(val magik: Magik) : CoreEvent(magik) {

    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {

        val player = event.player
        val uuid = player.uniqueId

        magik.userFiles.remove(uuid)
        magik.players[uuid]?.remove(player)
    }
}