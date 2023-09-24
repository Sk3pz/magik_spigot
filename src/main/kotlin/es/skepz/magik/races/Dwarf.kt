package es.skepz.magik.races

import es.skepz.magik.CustomItem
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
import org.bukkit.event.inventory.InventoryType
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
    private val pickaxe = CustomItem(magik, Material.NETHERITE_PICKAXE, 1, "&cDwarven Pickaxe &8(&6mine&8)",
        listOf("&4Forged in the fires of the nether", "&8[&6Right Click&8] &7while holding to change modes."),
        "dwarven_pickaxe", true,
        mutableMapOf(Pair(Enchantment.DAMAGE_ALL, 5), Pair(Enchantment.LOOT_BONUS_BLOCKS, 3)))

    private val playerMode = mutableMapOf<UUID, PickaxeMode>()
    private val smeltingPower = mutableMapOf<UUID, Int>()
    private val veinMineCooldown = 30

    private val maxHealth = 24.0

    private val veinMineMax = 256

    private val coalPower = 3
    private val charcoalPower = 3
    private val coalBlockPower = 30
    private val lavaPower = 50

    override fun cooldownUpdate(player: Player, seconds: Int) {
        val itm = pickaxe.find(player.inventory) ?: return
        updateName(player, itm, getMode(player))
    }

    override fun update(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 2, 1, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.HUNGER, 2, 0, false, false))

        if (player.location.block.lightFromSky <= 10) {
            player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 2, 1, false, false))
        }
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.DIAMOND_PICKAXE, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize("&8&lDwarf")))
            it.lore(listOf(
                Component.text(colorize("&7Great for players who love to mine")),
                Component.text(colorize("&7- &aDwarven Pickaxe: different modes to help mine")),
                Component.text(colorize("&7- &aQuicker at digging")),
                Component.text(colorize("&7- &a2 more hearts")),
                Component.text(colorize("&7- &cSlower than most")),
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

        player.inventory.addItem(pickaxe.generate())
    }

    override fun remove(player: Player) {
        player.resetMaxHealth()
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (pickaxe.check(item)) {
                inv.remove(item)
            }
        }
        playerMode.remove(player.uniqueId)
        magik.cooldowns.remove(player)
    }

    private fun modeToName(player: Player, mode: PickaxeMode): String {
        return when (mode) {
            PickaxeMode.Mine  -> "&8(&6mine&8)"
            PickaxeMode.Fast  -> "&8(&6fast&8)"
            PickaxeMode.Vein  -> {
                val cooldown = magik.cooldowns[player]
                if (cooldown != null) {
                    "&8(&6vein &8[&c$cooldown&8])"
                } else {
                    "&8(&6vein&8)"
                }
            }
            PickaxeMode.Silk -> "&8(&6silk&8)"
            PickaxeMode.Smelt -> {
                val fuel = smeltingPower[player.uniqueId]
                if (fuel != null) {
                    "&8(&6smelt &8[&c$fuel&8]&8)"
                } else {
                    "&8(&6smelt &8[&c0&8]&8)"
                }
            }
        }
    }

    private fun updateName(player: Player, pick: ItemStack, mode: PickaxeMode) {
        pick.itemMeta = pick.itemMeta.also { meta ->
            meta.displayName(Component.text(colorize("&cDwarven Pickaxe ${modeToName(player, mode)}")))
        }
    }

    private fun setMode(player: Player, pick: ItemStack, mode: PickaxeMode) {
        playerMode[player.uniqueId] = mode
        updateName(player, pick, mode)
        pick.itemMeta = pick.itemMeta.also { meta ->

            meta.enchants.keys.forEach {
                meta.removeEnchant(it)
            }

            meta.addEnchant(Enchantment.DAMAGE_ALL, 5, true)

            when (mode) {
                PickaxeMode.Mine  -> {
                    meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 3, true)
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

        if (!pickaxe.check(item)) {
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
                type == Material.COBBLESTONE ||
                type == Material.ANCIENT_DEBRIS
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
        val pick = player.inventory.itemInMainHand
        if (!pickaxe.check(pick)) return

        val mode = playerMode[player.uniqueId] ?: PickaxeMode.Mine

        val block = event.block

        when (getMode(player)) {
            PickaxeMode.Vein -> {
                val cooldown = magik.cooldowns[player]
                if (cooldown != null) {
                    sendMessage(player, "&cYou can't use that mode for another $cooldown seconds!")
                    playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
                    event.isCancelled = true
                    return
                }
                if (isVeinMiningBlock(block.type)) {
                    event.isDropItems = false
                    vineBlockBreak(block, block.type, block.location.add(0.5, 0.5, 0.5))
                    magik.cooldowns[player] = veinMineCooldown
                    updateName(player, pick, mode)
                } else {
                    sendMessage(player, "&cYou can't vein mine this block!")
                    playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
                }
            }
            PickaxeMode.Smelt -> {
                val inv = player.inventory

                var drop: ItemStack? = null

                // iron, gold, copper
                val dropLoc = block.location.add(0.5, 0.5, 0.5)
                when (block.type) {
                    Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE -> {
                        drop = ItemStack(Material.IRON_INGOT)
                    }
                    Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE -> {
                        drop = ItemStack(Material.GOLD_INGOT)
                    }
                    Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE -> {
                        drop = ItemStack(Material.COPPER_INGOT)
                    }
                    Material.STONE -> {
                        drop = ItemStack(Material.SMOOTH_STONE)
                    }
                    Material.DEEPSLATE -> {
                        drop = ItemStack(Material.DEEPSLATE)
                    }
                    Material.NETHERRACK -> {
                        drop = ItemStack(Material.NETHER_BRICK)
                    }
                    Material.ANCIENT_DEBRIS -> {
                        drop = ItemStack(Material.NETHERITE_SCRAP)
                    }
                    else -> {}
                }

                if (drop != null) {
                    var power = smeltingPower[player.uniqueId] ?: 0
                    if (power == 0) {
                        if (inv.contains(Material.COAL)) {
                            for (it in inv.contents) {
                                if (it == null) continue
                                if (it.type == Material.COAL) {
                                    if (it.amount == 1) {
                                        inv.remove(it)
                                    } else {
                                        it.amount -= 1
                                    }
                                    break
                                }
                            }
                            power = coalPower
                        } else if (inv.contains(Material.CHARCOAL)) {
                            for (it in inv.contents) {
                                if (it == null) continue
                                if (it.type == Material.CHARCOAL) {
                                    if (it.amount == 1) {
                                        inv.remove(it)
                                    } else {
                                        it.amount -= 1
                                    }
                                    break
                                }
                            }
                            power = charcoalPower
                        } else if (inv.contains(Material.COAL_BLOCK)) {
                            for (it in inv.contents) {
                                if (it == null) continue
                                if (it.type == Material.COAL_BLOCK) {
                                    if (it.amount == 1) {
                                        inv.remove(it)
                                    } else {
                                        it.amount -= 1
                                    }
                                    break
                                }
                            }
                            power = coalBlockPower
                        } else if (inv.contains(Material.LAVA_BUCKET)) {
                            inv.remove(Material.LAVA_BUCKET)
                            inv.addItem(ItemStack(Material.BUCKET))
                            power = lavaPower
                        } else {
                            event.isCancelled = true
                            sendMessage(player, "&cYou must have coal, charcoal, a coal block or a lava bucket in your inventory to use this!")
                            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
                            return
                        }
                    }
                    event.isDropItems = false
                    dropItem(dropLoc, drop)
                    smeltingPower[player.uniqueId] = power - 1
                    updateName(player, pick, mode)
                }
            }
            else -> {}
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isDwarf() }
            ?: return

        if (pickaxe.check(event.itemDrop.itemStack)) {
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

        if (pickaxe.check(item) && event.inventory.type != InventoryType.CRAFTING) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isDwarf()) return
        val drops = event.drops
        val remove = mutableListOf<ItemStack>()
        drops.forEach { item ->
            if (item == null) return@forEach
            if (pickaxe.check(item)) {
                remove.add(item)
            }
        }
        remove.forEach { item ->
            drops.remove(item)
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player.takeIf { it.isDwarf() }
            ?: return

        setRace(magik, player, this, false)
    }
}