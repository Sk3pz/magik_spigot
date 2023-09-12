package es.skepz.magik

import es.skepz.magik.commands.MagikCommand
import es.skepz.magik.events.EventInventory
import es.skepz.magik.events.EventPlayerJoin
import es.skepz.magik.events.EventPlayerLeave
import es.skepz.magik.files.UserFile
import es.skepz.magik.races.*
import es.skepz.magik.tuodlib.wrappers.CFGFile
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class Magik : JavaPlugin() {

    val userFiles = mutableMapOf<UUID, UserFile>()
    var config = CFGFile(this, "config", "")

    val races = mutableListOf<Race>()
    val comingSoonRaces = mutableListOf<Race>()
    val players = mutableMapOf<UUID, Race>()
    val inventories = mutableListOf<Inventory>()

    val guiKeys = mutableListOf<NamespacedKey>()

    override fun onEnable() {
        // commands
        MagikCommand(this).register()

        // events
        EventPlayerJoin(this).register()
        EventPlayerLeave(this).register()
        EventInventory(this).register()

        // races
        Aquarian(this).register()
        Avian(this).register()
        Druid(this).register()
        Dwarf(this).register()
        Elf(this).register()
        Orc(this).register()

        // REMEMBER TO CHANGE THE GUI ITEMS FOR NEW RACES!
        Enderian(this).register()
        Goblin(this).register()
        Blazeborn(this).register()

        Human(this).register()

        // start the subsystems for handling the races
        start(this)

        server.onlinePlayers.forEach { player ->

            val file = UserFile(this, player)
            userFiles[player.uniqueId] = file

            val race = file.getRace()
            if (race == null) {
                createInventory(this, player)
                return@forEach
            }

            players[player.uniqueId] = race
            setRace(this, player, race, false)
        }

    }

    override fun onDisable() {
        server.onlinePlayers.forEach {
            players[it.uniqueId]?.remove(it)
        }
    }

}