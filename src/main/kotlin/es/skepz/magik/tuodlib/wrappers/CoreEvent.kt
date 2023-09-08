package es.skepz.magik.tuodlib.wrappers

import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

open class CoreEvent(val plugin: JavaPlugin) : Listener {

    fun register() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

}