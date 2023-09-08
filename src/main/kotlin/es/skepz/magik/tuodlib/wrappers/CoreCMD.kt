package es.skepz.magik.tuodlib.wrappers

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import es.skepz.magik.tuodlib.*
import java.util.*

abstract class CoreCMD
protected constructor(private val plugin: JavaPlugin, private val cmd: String,
                      private val usage: String, private val argc: Int,
                      var permission: String, var onlyPlayer: Boolean,
                      var tabCom: Boolean) : CommandExecutor, TabCompleter {

    lateinit var sender: CommandSender
    lateinit var args: ArrayList<String>
    var helpMessage: String = "&7Usage: &c$usage"

    abstract fun init()
    abstract fun run()

    abstract fun registerTabComplete(sender: CommandSender, args: ArrayList<String>): List<String>
    override fun onTabComplete(s: CommandSender, cmd: Command, alias: String, args: Array<String>): List<String> {
        sender = s
        return registerTabComplete(sender, ArrayList(listOf(*args)))
    }

    fun register() {
        plugin.getCommand(cmd)!!.setExecutor(this)
        if (tabCom) plugin.getCommand(cmd)!!.tabCompleter = this
    }

    fun invalidUse() {
        invalidCmdUsage(sender, usage)
    }

    val isPlayer: Boolean
        get() = sender is Player

    fun requirePlayer(): Boolean {
        if (!isPlayer) notPlayer(sender)
        return isPlayer
    }

    fun getPlayer(): Player? {
        return if (isPlayer) sender as Player else null
    }

    // Command will run init(), then will check permissions and for player
    // then execute run()
    override fun onCommand(s: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {
        this.args = ArrayList(listOf(*args)) // put args into arraylist
        sender = s // set the sender
        init() // run init function
        if (args.size == 1 && args[0] == "?") { // if user is running the help function
            sendMessage(sender, helpMessage)
            return true
        }

        if (onlyPlayer && !isPlayer) { // if command requires sender to be a player, run the check
            requirePlayer(sender)
            return true
        }

        if (permission != "none" && !checkPermission(sender, permission)) { // check the sender for the required permission
            noPerms(sender)
            return true
        }

        if (args.size < argc) { // check if there is a correct amount of arguments
            invalidUse()
            return true
        }

        run() // run the main code

        return true
    }

}