package es.skepz.magik.commands

import es.skepz.magik.Magik
import es.skepz.magik.races.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import es.skepz.magik.tuodlib.sendMessage
import es.skepz.magik.tuodlib.wrappers.CoreCMD
import java.util.ArrayList

class MagikCommand(val magik: Magik) : CoreCMD(magik, "magik", "&c/magik <&7set&c|&7check&c|&7inv&c>",
    1, "magik.set_race", true, true) {

    override fun init() {
        // no init needed
    }

    override fun run() {
        val player = getPlayer()!!

        when (args[0].lowercase()) {
            "set" -> {
                val race = raceFromName(magik, args[2])
                if (race == null || args.size != 3) {
                    invalidUse()
                    return
                }
                val t = Bukkit.getPlayer(args[1])
                if (t == null) {
                    sendMessage(sender, "&cThat player either isn't online or doesn't exist!")
                    return
                }

                setRace(magik, t, race)
                sendMessage(sender, "&7Set &b${t.name}&7's race to &b${race.name()}")
            }
            "inv" -> {
                var target = player
                if (args.size == 2) {
                    val t = Bukkit.getPlayer(args[1])
                    if (t == null) {
                        sendMessage(sender, "&cThat player either isn't online or doesn't exist!")
                        return
                    }
                    target = t
                }
                createInventory(magik, target)
            }
            "check" -> {
                if (args.size != 2) {
                    invalidUse()
                    return
                }
                val t = Bukkit.getPlayer(args[1])
                if (t == null) {
                    sendMessage(sender, "&cThat player either isn't online or doesn't exist!")
                    return
                }
                val race = getRace(magik, t)
                if (race == null) {
                    sendMessage(sender, "&cThat player either isn't online or doesn't exist!")
                    return
                }
                sendMessage(player, "&b${t.name}&7's race is &b${race.name()}&7.")
            }
        }
    }

    override fun registerTabComplete(sender: CommandSender, args: ArrayList<String>): List<String> {
        val completions = ArrayList<String>()
        if (args.size == 1) {
            StringUtil.copyPartialMatches(args[0], listOf("set", "check", "inv"), completions)
        }
        if (args.size == 2 && args[0].lowercase() != "inv") {
            val players = Bukkit.getServer().onlinePlayers
            val names = ArrayList<String>()
            for (p in players) {
                names.add(p.name)
            }
            StringUtil.copyPartialMatches(args[1], names, completions)
        }
        if (args.size == 3 && args[0].lowercase() == "set") {
            val races = getRaceNames(magik)
            StringUtil.copyPartialMatches(args[2], races, completions)
        }
        return completions
    }

}