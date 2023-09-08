package es.skepz.magik.tuodlib.tools

import es.skepz.magik.tuodlib.colorize
import es.skepz.magik.tuodlib.decolorize
import es.skepz.magik.tuodlib.spawnEntity
import es.skepz.magik.tuodlib.wrappers.CFGFile
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.collections.ArrayList

/***
 * Get a hologram from the file by name
 * @param plugin: the plugin that is displaying the hologram
 * @param identifier: the name/ID of the hologram
 ***/
fun getHologramByID(plugin: JavaPlugin, identifier: String): Hologram? {
    val config = CFGFile(plugin, "holograms", "ds")
    if (config.cfg.get("holograms.$identifier") == null) return null
    val world = config.cfg.getString("holograms.$identifier.loc.world")
    val x = config.cfg.getDouble("holograms.$identifier.loc.x")
    val y = config.cfg.getDouble("holograms.$identifier.loc.y")
    val z = config.cfg.getDouble("holograms.$identifier.loc.z")
    val loc = Location(Bukkit.getWorld(world!!), x, y, z)
    val stands = ArrayList<ArmorStand>()
    val namesList = ArrayList<String>()
    val exists = config.cfg.getBoolean("holograms.$identifier.exists")
    if (exists) {
        for (standID: String in config.cfg.getConfigurationSection("holograms.$identifier.lines")!!.getKeys(false)) {
            stands.add(loc.world.getEntity(UUID.fromString(standID)) as ArmorStand)
            namesList.add(config.cfg.getString("holograms.$identifier.lines.$standID")!!)
        }
    }
    val names = namesList.toTypedArray()
    return Hologram(plugin, identifier, loc, stands, names, exists)
}

/***
 * A hologram to display in game
 * @param plugin: the plugin that is displaying the hologram
 * @param identifier: the name/ID of the hologram
 * @param loc: The location of the hologram
 * @param lines: the lines to be displayed
 ***/
class Hologram(plugin: JavaPlugin, private val identifier: String, private val loc: Location, vararg lines: String) {
    private val distance = 0.23 // distance between lines (for entity spawning)
    private var exists = false // is it displayed
    private val names = ArrayList<String>() // the names of the armorstands
    private val armorStands = ArrayList<ArmorStand>() // the armorstands
    val config = CFGFile(plugin, "holograms", "ds")

    /***
     * An already existing hologram
     * @param plugin: the plugin that is displaying the hologram
     * @param identifier: the name/ID of the hologram
     * @param loc: The location of the hologram
     * @param stands: the preexisting armorstands
     * @param names: the names of the stands
     * @param ex: if the hologram is displayed or not
     ***/
    constructor(plugin: JavaPlugin, identifier: String, loc: Location, stands: ArrayList<ArmorStand>, names: Array<String>, ex: Boolean) : this(plugin, identifier, loc, *names) {
        armorStands.clear()
        armorStands.addAll(stands)
        exists = ex
    }

    init {
        for (s in lines) names.add(colorize(s))
    }

    fun setLines(vararg newLines: String) {
        val created = exists
        if (created) destroy()
        names.clear()
        for (s in newLines) names.add(colorize(s))
        if (created) create()
    }
    fun setLine(index: Int, line: String) {
        val created = exists
        if (created) destroy()
        names[index] = line
        if (created) create()
    }

    fun create() {
        if (exists) {
            refresh()
            return
        }
        val lineLoc = loc.clone().add(0.0, names.size / 2 * distance, 0.0)
        for (s: String in names) {
            armorStands.add(makeLine(lineLoc.clone(), s))
            lineLoc.subtract(0.0, distance, 0.0)
        }
        exists = true
        saveToConfig()
    }

    fun destroy() {
        if (!exists) return

        for (stand in armorStands) {
            stand.health = 0.0
            stand.remove()
        }

        exists = false
    }

    fun refresh() {
        if (exists) {
            destroy()
            create()
        } else create()
    }

    fun makeLine(loc: Location, line: String): ArmorStand {
        val display = spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand
        display.customName = line // set name to the line
        display.isVisible = false // set the armor stand invisible
        display.isCollidable = false // set it to not collide with players
        display.isInvulnerable = true // set it invulnerable
        display.isCustomNameVisible = true // set the name visible
        display.setBasePlate(false) // remove the base plate (not really needed)
        display.setGravity(false) // remove gravity (no falling)
        display.canPickupItems = false // cant pick up items
        // remove intractability with the slots of the armorstand
        display.disabledSlots.add(EquipmentSlot.HEAD)
        display.disabledSlots.add(EquipmentSlot.CHEST)
        display.disabledSlots.add(EquipmentSlot.LEGS)
        display.disabledSlots.add(EquipmentSlot.FEET)
        display.disabledSlots.add(EquipmentSlot.HAND)
        display.disabledSlots.add(EquipmentSlot.OFF_HAND)

        saveToConfig() // ensure saving on each step as to ensure a crash does not corrupt the hologram

        return display
    }

    fun saveToConfig() {
        config["holograms.$identifier.exists"] = exists
        config["holograms.$identifier.loc.world"] = loc.world.name
        config["holograms.$identifier.loc.x"] = loc.x
        config["holograms.$identifier.loc.y"] = loc.y
        config["holograms.$identifier.loc.z"] = loc.z
        for (stand in armorStands) config["holograms.$identifier.lines.${stand.uniqueId}"] = decolorize(stand.customName!!)
    }
}