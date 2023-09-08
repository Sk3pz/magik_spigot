package es.skepz.magik.tuodlib.wrappers

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * @param plugin: the plugin
 * @param fileName the name of the file (automatically adds .yml)
 * @param folder the folder which the config should be held in (empty if in data folder)
 */
open class CFGFile(plugin: JavaPlugin, private val fileName: String, folder: String) {
    var cfg: FileConfiguration
        private set
    private val file: File
    private val dataFolder: File

    /**
     * assert that a value exists in the file (for making default values)
     * @return true if set value
     */
    fun default(key: String?, obj: Any?): Boolean {
        if (!cfg.contains(key!!)) {
            set(key, obj)
            return true
        }
        return false
    }

    /**
     * @return returns true if saved successfully
     */
    fun save(): Boolean {
        try {
            cfg.save(file)
        } catch (ex: Exception) {
            Bukkit.getServer().logger.severe("Could not save $fileName.yml!")
            return false
        }
        return true
    }

    fun reload() {
        cfg = YamlConfiguration.loadConfiguration(file)
    }

    /**
     * Set a value and save (save 1 line of code lmao)
     */
    operator fun set(value: String, obj: Any?) {
        cfg[value] = obj
        save()
    }

    init {
        if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdir()
        dataFolder = File(plugin.dataFolder.toString() + File.separator + folder)
        if (!dataFolder.exists()) dataFolder.mkdir()
        file = File(dataFolder, "$fileName.yml")
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (ex: Exception) {
                Bukkit.getServer().logger.severe("could not create $fileName.yml")
            }
        }
        cfg = YamlConfiguration.loadConfiguration(file)
    }
}