package es.skepz.magik

import es.skepz.magik.commands.MagikCommand
import es.skepz.magik.events.EventInventory
import es.skepz.magik.events.EventPlayerJoin
import es.skepz.magik.events.EventPlayerLeave
import es.skepz.magik.files.UserFile
import es.skepz.magik.races.*
import es.skepz.magik.tuodlib.wrappers.CFGFile
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class Magik : JavaPlugin() {

    val userFiles = mutableMapOf<UUID, UserFile>()
    var config = CFGFile(this, "config", "")

    val races = mutableListOf<Race>()
    val players = mutableMapOf<UUID, Race>()
    val inventories = mutableListOf<Inventory>()
    val raceItems = mutableListOf<ItemStack>()

    override fun onEnable() {

        // commands
        MagikCommand(this).register()

        // events
        EventPlayerJoin(this).register()
        EventPlayerLeave(this).register()
        EventInventory(this).register()

        // config defaults
        config.default("misc.dwarf_max_vein_mine", 5)

        // races
        Avian(this).register()
        Druid(this).register()
        Dwarf(this).register()
        Elf(this).register()
        Orc(this).register()
        Human(this).register()

        races.forEach {
            raceItems.add(it.guiDisplayItem())
        }

        // start the subsystems for handling the races
        start(this)

        server.onlinePlayers.forEach { player ->

            val file = UserFile(this, player)
            userFiles[player.uniqueId] = file

            val race = file.getRace()
            if (race == null) {
                // TODO: Race selection here
                return@forEach
            }

            setRace(this, player, race)
        }

    }

    override fun onDisable() {

        server.onlinePlayers.forEach {
            players[it.uniqueId]?.remove(it)
        }

        // todo: possible edge case if player has selection inventory open on reload run
    }

}