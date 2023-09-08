package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.colorize
import es.skepz.magik.tuodlib.sendMessage
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class Dwarf(magik: Magik) : Race(magik) {

    enum class PickaxeMode { Mine, Vein, Silk, Smelt }

    private val playerMode = HashMap<UUID, PickaxeMode>()

    private val maxHealth = 22.0

    override fun update(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 2, 1, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 2, 1, false, false))
    }

    override fun guiDisplayItem(): ItemStack {

        val item = ItemStack(Material.DIAMOND_PICKAXE, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&7&lDwarf")))
            it.lore(listOf(
                Component.text(colorize("&7- &aDwarven Pickaxe: different modes to help mine")),
                //Component.text(colorize("&7- &aOre Sense (5 blocks distance)")),
                Component.text(colorize("&7- &aPermanent haste 2")),
                Component.text(colorize("&7- &a+2 max hearts")),
                Component.text(colorize("&7- &cSlowness")),
                //Component.text(colorize("&7- &cBlindness when not underground")) // todo: tbd
            ))
        }

        return item
    }

    private fun isDwarf(p: Player): Boolean {
         return getRace(magik, p) is Dwarf
    }

    override fun name(): String {
        return "Dwarf"
    }

    override fun set(player: Player) {
        player.maxHealth = maxHealth
    }

    override fun remove(player: Player) {
        // TODO
    }

    private fun checkPickaxe(item: ItemStack): Boolean {
        TODO()
    }

    fun modeToName(mode: PickaxeMode): String {
        return when (mode) {
            PickaxeMode.Mine  -> "&8(&6mine&8)"
            PickaxeMode.Vein  -> "&8(&6vein&8)"
            PickaxeMode.Silk  -> "&8(&6silk&8)"
            PickaxeMode.Smelt -> "&8(&6smlt&8)"
        }
    }

    private fun setMode(player: Player, mode: PickaxeMode) {
        playerMode[player.uniqueId] = mode
        // todo: change the pickaxe in the player's inventory
    }

    private fun changeMode(player: Player) {

        val mode = playerMode[player.uniqueId]
            ?: return sendMessage(player, "&cFailed to change pickaxe mode, if this persists please leave and rejoin to fix the issue.")

        when (mode) {
            PickaxeMode.Mine  -> setMode(player, PickaxeMode.Vein)
            PickaxeMode.Vein  -> setMode(player, PickaxeMode.Silk)
            PickaxeMode.Silk  -> setMode(player, PickaxeMode.Smelt)
            PickaxeMode.Smelt -> setMode(player, PickaxeMode.Mine)
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {

        val player = event.player.takeIf { isDwarf(it) }
            ?: return

        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) {
            return
        }

        val item = event.item
            ?: return

        if (!checkPickaxe(item)) {
            return
        }

        if (!player.isSneaking) {
            changeMode(player)
        }
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {

        val player = event.player.takeIf { isDwarf(it) }
            ?: return

        val block = event.block
    }
}