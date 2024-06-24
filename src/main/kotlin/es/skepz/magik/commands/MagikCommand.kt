package es.skepz.magik.commands

import es.skepz.magik.Backpack
import es.skepz.magik.Magik
import es.skepz.magik.races.*
import es.skepz.magik.skepzlib.sendMessage
import es.skepz.magik.skepzlib.wrappers.CoreCMD
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MagikCommand(val magik: Magik) : CoreCMD(magik, "magik", "&c/magik <&7set&c|&7check&c|&7inv&c|&7give&c>",
    1, "magik.set_race", true, true) {

    override fun run() {
        val player = sender as Player

        when (args[0].lowercase()) {

            "set" -> {
                if (args.size < 3) return invalidUse()
                val race = raceFromName(magik, args[2]) ?: return sendMessage(sender, "&cInvalid race!")

                val t = Bukkit.getPlayer(args[1])
                    ?: return sendMessage(sender, "&cThat player either isn't online or doesn't exist!")

                setRace(magik, t, race, false)
                sendMessage(sender, "&7Set &b${t.name}&7's race to &b${race.name()}")
            }

            "inv" -> {
                if (args.size < 2) {
                    return createInventory(magik, player)
                }

                val target = Bukkit.getPlayer(args[1])
                    ?: return sendMessage(sender, "&cThat player either isn't online or doesn't exist!")

                createInventory(magik, target)
                sendMessage(sender, "&7Opened the race selection menu for &b${target.name}&7.")
            }

            "check" -> {
                if (args.size < 2) {
                    return invalidUse()
                }

                val target = Bukkit.getPlayer(args[1])
                    ?: return sendMessage(sender, "&cThat player either isn't online or doesn't exist!")

                val race = getRace(magik, target)
                    ?: return sendMessage(sender, "&cThat player either isn't online or doesn't exist!")

                sendMessage(player, "&b${target.name}&7's race is &b${race.name()}&7.")
            }

            "give" -> {
                if (args.size < 3) {
                    return invalidUse()
                }

                val target = Bukkit.getPlayer(args[1])
                    ?: return sendMessage(sender, "&cThat player either isn't online or doesn't exist!")

                val item = args[2].lowercase()

                when (item) {
                    "strange_rock" -> {
                        target.inventory.addItem(magik.specialItems.changeItem.generate())
                    }
                    "backpack" -> {
                        target.inventory.addItem(Backpack(magik))
                    }
                }
            }

            else -> invalidUse()
        }
    }

    override fun registerTabComplete(sender: CommandSender, args: Array<String>): List<String> {
        if (
            args.size == 3 && args[0].equals("check", true)
        ) {
            return emptyList()
        }

        return when(args.size) {
            1 -> listOf("set", "check", "inv", "give")
            2 -> magik.server.onlinePlayers.map { it.name }
            3 -> if (args[0].equals("give", true))
                listOf("strange_rock", "backpack")
                else getRaceNames(magik)
            else -> emptyList()
        }.filter { it.startsWith(args.last(), true) }
    }

}