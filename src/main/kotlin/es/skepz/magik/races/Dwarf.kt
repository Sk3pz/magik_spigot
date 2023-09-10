package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.colorize
import es.skepz.magik.tuodlib.dropItem
import es.skepz.magik.tuodlib.playSound
import es.skepz.magik.tuodlib.sendMessage
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.lang.Math.pow
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.pow

class Dwarf(magik: Magik) : Race(magik) {

    enum class PickaxeMode { Mine, Fast, Vein, Silk, Smelt }
    private val pxKey = NamespacedKey(magik, "dwarven_pickaxe")

    private val playerMode = mutableMapOf<UUID, PickaxeMode>()
    //private val cavePlayers = mutableSetOf<Player>()

    private val maxHealth = 24.0

    private val veinMineMax = magik.config.cfg.getInt("misc.dwarf_max_vein_mine").toDouble().pow(3.0).toInt()

    override fun update(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 2, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 2, 1, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.HUNGER, 2, 0, false, false))

//        if (player.location.block.lightFromSky > 10) {
//            player.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 40, 0, false, false))
//        }
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.DIAMOND_PICKAXE, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&7&lDwarf")))
            it.lore(listOf(
                Component.text(colorize("&7Great for players who love to mine")),
                Component.text(colorize("&7- &aDwarven Pickaxe: different modes to help mine")),
                Component.text(colorize("&7- &aHaste 2")),
                Component.text(colorize("&7- &a+2 max hearts")),
                Component.text(colorize("&7- &cSlowness")),
                Component.text(colorize("&7- &cHungrier than normal")),
                //Component.text(colorize("&7- &cTrouble seeing above ground")),
            ))
        }

        return item
    }

    private fun getMode(player: Player): PickaxeMode {
        val mode = playerMode[player.uniqueId]
            if (mode == null) {
                sendMessage(player, "&cFailed to change pickaxe mode, if this persists please leave and rejoin to fix the issue.")
                return PickaxeMode.Mine
            }
        return mode
    }

    private fun Player.isDwarf(): Boolean {
         return getRace(magik, this) is Dwarf
    }

    override fun name(): String {
        return "Dwarf"
    }

    override fun set(player: Player) {
        player.maxHealth = maxHealth
        playerMode[player.uniqueId] = PickaxeMode.Mine

        player.inventory.addItem(generatePickaxe())
    }

    override fun remove(player: Player) {
        player.resetMaxHealth()
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (checkPickaxe(item)) {
                inv.remove(item)
            }
        }
    }

    private fun generatePickaxe(): ItemStack {
        val pick = ItemStack(Material.NETHERITE_PICKAXE)
        pick.itemMeta = pick.itemMeta.also {
            it.displayName(Component.text(colorize("&cDwarven Pickaxe ${modeToName(PickaxeMode.Mine)}")))
            it.lore(listOf(
                Component.text(colorize("&4Forged in the fires of the nether")),
                Component.text(colorize("&4Right click with this in your hand to change modes."))
            ))
            it.isUnbreakable = true
            it.persistentDataContainer.set(pxKey, PersistentDataType.DOUBLE, Math.PI)
            it.addEnchant(Enchantment.DAMAGE_ALL, 7, true)
            it.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 4, true)
        }
        return pick
    }

    private fun checkPickaxe(item: ItemStack): Boolean {
        if (!item.hasItemMeta()) return false
        return item.itemMeta.persistentDataContainer.has(pxKey, PersistentDataType.DOUBLE)
    }

    private fun modeToName(mode: PickaxeMode): String {
        return when (mode) {
            PickaxeMode.Mine  -> "&8(&6mine&8)"
            PickaxeMode.Fast  -> "&8(&6fast&8)"
            PickaxeMode.Vein  -> "&8(&6vein&8)"
            PickaxeMode.Silk  -> "&8(&6silk&8)"
            PickaxeMode.Smelt -> "&8(&6smelt&8)"
        }
    }

    private fun setMode(player: Player, pick: ItemStack, mode: PickaxeMode) {
        playerMode[player.uniqueId] = mode
        pick.itemMeta = pick.itemMeta.also { meta ->

            meta.displayName(Component.text(colorize("&cDwarven Pickaxe ${modeToName(mode)}")))

            meta.enchants.keys.forEach {
                meta.removeEnchant(it)
            }

            meta.addEnchant(Enchantment.DAMAGE_ALL, 7, true)

            when (mode) {
                PickaxeMode.Mine  -> {
                    meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 4, true)
                }
                PickaxeMode.Silk  -> {
                    meta.addEnchant(Enchantment.SILK_TOUCH, 1, true)
                }
                PickaxeMode.Fast -> {
                    meta.addEnchant(Enchantment.DIG_SPEED, 5, true)
                }
                else -> {}
            }
        }
    }

    private fun changeMode(player: Player, pick: ItemStack) {
        val mode = getMode(player)

        when (mode) {
            PickaxeMode.Mine  -> setMode(player, pick, PickaxeMode.Fast)
            PickaxeMode.Fast  -> setMode(player, pick, PickaxeMode.Vein)
            PickaxeMode.Vein  -> setMode(player, pick, PickaxeMode.Silk)
            PickaxeMode.Silk  -> setMode(player, pick, PickaxeMode.Smelt)
            PickaxeMode.Smelt -> setMode(player, pick, PickaxeMode.Mine)
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player.takeIf { it.isDwarf() }
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
            changeMode(player, item)
        }
    }

    private fun isVeinMiningBlock(type: Material): Boolean {
        return type == Material.COAL_ORE ||
                type == Material.DEEPSLATE_COAL_ORE ||
                type == Material.COPPER_ORE ||
                type == Material.DEEPSLATE_COPPER_ORE ||
                type == Material.IRON_ORE ||
                type == Material.DEEPSLATE_IRON_ORE ||
                type == Material.GOLD_ORE ||
                type == Material.DEEPSLATE_GOLD_ORE ||
                type == Material.REDSTONE_ORE ||
                type == Material.DEEPSLATE_REDSTONE_ORE ||
                type == Material.LAPIS_ORE ||
                type == Material.DEEPSLATE_LAPIS_ORE ||
                type == Material.DIAMOND_ORE ||
                type == Material.DEEPSLATE_DIAMOND_ORE ||
                type == Material.EMERALD_ORE ||
                type == Material.DEEPSLATE_EMERALD_ORE ||
                type == Material.NETHER_QUARTZ_ORE ||
                type == Material.NETHER_GOLD_ORE ||
                type == Material.DRIPSTONE_BLOCK ||
                type == Material.COBBLESTONE
    }

    private fun vineBlockBreak(startBlock: Block, type: Material, dropLocation: Location) {
        val stack = LinkedList<Block>()
        var count = 0

        stack.add(startBlock)

        while (count < veinMineMax) {

            val block = stack.poll()
                ?: break

            count++

            if (block.type != type) {
                continue
            }

            // break the block and handle drops
            block.drops.forEach {
                dropItem(dropLocation, it)
            }

            block.type = Material.AIR

            // handle surrounding drops
            for (x in -1..1) {
                for (y in -1..1) {
                    for (z in -1..1) {
                        val relativeBlock = block.getRelative(x, y, z)

                        if (relativeBlock.type == type) {
                            stack.add(relativeBlock)
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        val player = event.player.takeIf { it.isDwarf() }
            ?: return

        if (player.gameMode != GameMode.SURVIVAL) return
        if (!checkPickaxe(player.inventory.itemInMainHand)) return

        val block = event.block

        when (getMode(player)) {
            PickaxeMode.Vein -> {
                if (isVeinMiningBlock(block.type)) {
                    event.isDropItems = false
                    vineBlockBreak(block, block.type, block.location.add(0.5, 0.5, 0.5))
                } else {
                    sendMessage(player, "&cYou can't vein mine this block!")
                    playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
                }
            }
            PickaxeMode.Smelt -> {
                // iron, gold, copper
                val dropLoc = block.location.add(0.5, 0.5, 0.5)
                when (block.type) {
                    Material.IRON_ORE -> {
                        event.isDropItems = false
                        dropItem(dropLoc, ItemStack(Material.IRON_INGOT))
                    }
                    Material.GOLD_ORE -> {
                        event.isDropItems = false
                        dropItem(dropLoc, ItemStack(Material.GOLD_INGOT))
                    }
                    Material.COPPER_ORE -> {
                        event.isDropItems = false
                        dropItem(dropLoc, ItemStack(Material.COPPER_INGOT))
                    }
                    Material.STONE -> {
                        event.isDropItems = false
                        dropItem(dropLoc, ItemStack(Material.STONE))
                    }
                    Material.DEEPSLATE -> {
                        event.isDropItems = false
                        dropItem(dropLoc, ItemStack(Material.DEEPSLATE))
                    }
                    Material.NETHERRACK -> {
                        event.isDropItems = false
                        dropItem(dropLoc, ItemStack(Material.NETHER_BRICK))
                    }
                    else -> {}
                }
            }
            else -> {}
        }
    }

//    @EventHandler
//    fun onMove(event: PlayerMoveBlockEvent) {
//        val player = event.player.takeIf { it.isDwarf() }
//            ?: return
//        if (isInCave(player)) {
//            cavePlayers.add(player)
//        } else {
//            cavePlayers.remove(player)
//        }
//    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isDwarf() }
            ?: return

        if (checkPickaxe(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = (event.whoClicked as? Player)?.takeIf { it.isDwarf() }
            ?: return

        val item = event.currentItem
            ?: return

        if (checkPickaxe(item) && event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isDwarf()) return
        val drops = event.drops
        drops.forEach { item ->
            if (item == null) return@forEach
            if (checkPickaxe(item)) {
                drops.remove(item)
            }
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isDwarf() }
            ?: return

        setRace(magik, player, this)
    }
}