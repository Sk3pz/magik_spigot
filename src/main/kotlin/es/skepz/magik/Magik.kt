package es.skepz.magik

import es.skepz.magik.commands.MagikCommand
import es.skepz.magik.events.EventInventory
import es.skepz.magik.events.EventPlayerJoin
import es.skepz.magik.events.EventPlayerLeave
import es.skepz.magik.files.UserFile
import es.skepz.magik.races.*
import es.skepz.magik.tuodlib.wrappers.CFGFile
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Magik : JavaPlugin() {

    val userFiles = HashMap<UUID, UserFile>()
    var config = CFGFile(this, "config", "")

    val races = ArrayList<Race>()
    val players = HashMap<UUID, Race>()
    val inventories = ArrayList<Inventory>()
    val raceItems = ArrayList<ItemStack>()

    override fun onEnable() {
        // commands
        MagikCommand(this).register()

        // events
        EventPlayerJoin(this).register()
        EventPlayerLeave(this).register()
        EventInventory(this).register()

        // races
        Avian(this).register()
        Druid(this).register()
        Dwarf(this).register()
        Elf(this).register()
        Human(this).register()
        Orc(this).register()

        for (r in races) {
            raceItems.add(r.guiDisplayItem())
        }

        // start the subsystems for handling the races
        start(this)

        for (p in Bukkit.getOnlinePlayers()) {
            val file = UserFile(this, p)
            userFiles[p.uniqueId] = file
            val race = file.getRace()
            if (race == null) {
                // TODO: Race selection here
                continue
            }

            setRace(this, p, race)
        }
    }

    override fun onDisable() {
        for (p in Bukkit.getOnlinePlayers()) {
            val race = players[p.uniqueId] ?: continue
            race.remove(p)
        }
        // todo: possible edge case if player has selection inventory open on reload run
    }

}