package es.skepz.magik.tuodlib.wrappers

import es.skepz.magik.tuodlib.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

abstract class CoreCMD protected constructor(
    private val plugin: JavaPlugin,
    private val cmd: String,
    private val usage: String,
    private val argc: Int,
    var permission: String,
    var onlyPlayer: Boolean,
    var tabCom: Boolean
) : CommandExecutor, TabCompleter {

    class Context(val sender: CommandSender, val args: Array<String>)

    var helpMessage: String = "&7Usage: &c$usage"

    abstract fun Context.run()
    abstract fun registerTabComplete(sender: CommandSender, args: Array<String>): List<String>

    override fun onTabComplete(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): List<String> {
        return registerTabComplete(sender, args)
    }

    fun register() {

        val command = plugin.getCommand(cmd)
            ?: return

        command.setExecutor(this)

        if (tabCom) {
            command.tabCompleter = this
        }
    }

    fun Context.invalidUse() {
        invalidCmdUsage(sender, usage)
    }

    fun Context.requirePlayer(): Boolean {
        if (sender !is Player) {
            notPlayer(sender)
        }

        return true
    }

    // Will check permissions and for player
    // then execute run()
    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {

        if (args.size == 1 && args[0] == "?") { // if user is running the help function
            sendMessage(sender, helpMessage)
            return true
        }

        if (onlyPlayer && sender !is Player) { // if command requires sender to be a player, run the check
            requirePlayer(sender)
            return true
        }

        if (permission != "none" && !checkPermission(sender, permission)) { // check the sender for the required permission
            noPerms(sender)
            return true
        }

        if (args.size < argc) { // check if there is a correct amount of arguments
            invalidCmdUsage(sender, usage)
            return true
        }


        Context(sender, args).run() // run the main code

        return true
    }

}