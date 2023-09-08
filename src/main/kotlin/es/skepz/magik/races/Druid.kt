package es.skepz.magik.races

import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.*
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.type.Cocoa
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.ceil
import kotlin.math.min

class Druid(magik: Magik) : Race(magik) {

    private val maxHealth = 18.0

    private val stick = ItemStack(Material.STICK, 1)
    private val stickName = "&aStick of Life"

    init {
        val smeta = stick.itemMeta
        smeta.displayName(Component.text(colorize(stickName)))
        smeta.lore(listOf(Component.text(colorize("&6Grow plants with magik"))))
        stick.setItemMeta(smeta)
    }

    override fun update(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 2, 1, false, false))
        val loc = player.location
        val below = Location(loc.world, loc.x, loc.y - 1, loc.z)
        if (below.block.type == Material.GRASS_BLOCK) {
            player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 2, 2, false, false))
        }
        if (loc.world.environment == World.Environment.NETHER || loc.world.environment == World.Environment.THE_END) {
            player.maxHealth = maxHealth - 2.0
        } else {
            player.maxHealth = maxHealth
        }
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.OAK_SAPLING, 1)
        val meta = item.itemMeta
        meta.isUnbreakable = true
        meta.displayName(Component.text(colorize("&a&lDruid")))
        // TODO: auto replant crops (if not sneaking)
        meta.lore(listOf(
            Component.text(colorize("&7- &aStick Of Life: grows crops")),
            Component.text(colorize("&7- &aRegen when on grass")),
            Component.text(colorize("&7- &aPoison thorns")),
            Component.text(colorize("  &8- &aPoisons your attackers")),
            Component.text(colorize("  &8- &cActs like regen to undead mobs")),
            Component.text(colorize("&7- &aSpeed 1")),
            Component.text(colorize("&7- &aAutomatically replants crops")),
            Component.text(colorize("&7- &a2x crop yield")),
            Component.text(colorize("&7- &c-1 max hearts")),
            Component.text(colorize("&7- &cDecreased max health in Nether and End"))))
        item.setItemMeta(meta)

        return item
    }

    private fun isDruid(p: Player): Boolean {
        val race = getRace(magik, p)
        return race != null && race is Druid
    }

    private fun checkStick(item: ItemStack): Boolean {
        return item.isSimilar(stick)
    }

    override fun name(): String {
        return "Druid"
    }

    override fun set(player: Player) {
        if (!player.inventory.contains(stick)) {
            player.inventory.addItem(stick)
        }
    }

    override fun remove(player: Player) {
        player.inventory.remove(stick)
    }

    private fun handleDrops(block: Block, harvestItem: ItemStack?) {
        val enchlvl = if (harvestItem != null) {
            val i = harvestItem.clone()
            if (i.hasItemMeta()) {
                val meta = i.itemMeta
                var lvl = 0
                if (meta.hasEnchant(Enchantment.LOOT_BONUS_BLOCKS)) {
                    lvl += i.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)
                }
                lvl
            } else {
                0
            }
        } else {
            0
        }

        val multiplier = 2 + ceil(enchlvl * 1.2).toInt()
        val loc = block.location

        when (block.type) {
            Material.WHEAT -> {
                val drops = listOf(ItemStack(Material.WHEAT, 1 * random(1, multiplier)),
                    ItemStack(Material.WHEAT_SEEDS, random(1, 4) * multiplier))
                for (d in drops) {
                    dropItem(loc, d)
                }
            }
            Material.POTATOES -> {
                dropItem(loc, ItemStack(Material.POTATO, random(1, 3) * random(1, multiplier)))
            }
            Material.CARROTS -> {
                dropItem(loc, ItemStack(Material.CARROT, random(1, 3) * random(1, multiplier)))
            }
            Material.BEETROOTS -> {
                val drops = listOf(ItemStack(Material.BEETROOT, 1 * random(1, multiplier)),
                    ItemStack(Material.BEETROOT_SEEDS, random(1, 4) * multiplier))
                for (d in drops) {
                    dropItem(loc, d)
                }
            }
            Material.COCOA -> {
                dropItem(loc, ItemStack(Material.POTATO, random(3, 5) * enchlvl))
            }
            else -> return
        }
    }

    private fun harvest(block: Block, harvestItem: ItemStack?): Boolean {
        when (block.type) {
            Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS, Material.COCOA -> {
                val data = block.blockData as Ageable
                data.age = 0
                block.blockData = data
                handleDrops(block, harvestItem)
                return true
            }
            else -> {}
        }
        return false
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val p = event.player
        if (!isDruid(p)) return
        val item = event.item ?: return
        if (!checkStick(item)) return
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        if (block.blockData !is Ageable) return
        val ageable = block.blockData as Ageable
        displayParticles(block.location, Particle.VILLAGER_HAPPY, 10, 1.0, 1.0, 1.0)
        if (ageable.age >= ageable.maximumAge) {
            if (p.isSneaking) return
            harvest(block, null)
            return
        }
        ageable.age = min(ageable.maximumAge, ageable.age + 2)
        block.blockData = ageable
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        val p = event.player
        if (!isDruid(p)) return
        val block = event.block
        if (p.isSneaking) return
        event.isCancelled = harvest(block, p.activeItem)
    }

    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) {
            return
        }
        val p = event.entity as Player
        if (!isDruid(p)) {
            return
        }
        if (event.damager !is LivingEntity) return
        val attacker = event.damager as LivingEntity
        attacker.addPotionEffect(PotionEffect(PotionEffectType.POISON, 5 * 20, 1))
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val p = event.player
        if (!isDruid(p)) return
        val item = event.itemDrop.itemStack
        if (checkStick(item)) {
            event.isCancelled = true
            playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val p = event.whoClicked as Player
        if (!isDruid(p)) return
        val item = event.currentItem ?: return
        if (checkStick(item) && event.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.isCancelled = true
            playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val p = event.player
        if (!isDruid(p)) return
        event.drops.remove(stick)
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val p = event.player
        if (!isDruid(p)) return
        setRace(magik, p, this)
    }
}