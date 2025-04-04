package es.skepz.magik.races

import es.skepz.magik.CustomItem
import es.skepz.magik.Magik
import es.skepz.magik.skepzlib.*
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.ceil
import kotlin.math.min

class Druid(magik: Magik) : Race(magik) {

    private val maxHealth = 16.0

    private val thornsChance = 75

    private val stickName = "&aStick of Life"
    private val stick = CustomItem(magik, Material.STICK, 1, stickName,
        listOf("&6Right click on crops to grow and harvest them", "&7Has a 5 second cooldown every 5 uses."),
        "druid_stick", false)

    private val usageMap = mutableMapOf<Player, Int>()

    private val defaultUses = 5
    private val defaultCooldown = 5

    override fun cooldownUpdate(player: Player, seconds: Int) {
        val itm = stick.find(player.inventory) ?: return
        if (!magik.cooldowns.containsKey(player)) {
            usageMap[player] = defaultUses
        }
        updateData(player, itm)
    }

    override fun update(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 2, 0, false, false))

        val loc = player.location
        val below = Location(loc.world, loc.x, loc.y - 1, loc.z)

        if (below.block.type == Material.GRASS_BLOCK) {
            player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 2, 1, false, false))
        }

        if (loc.world.environment == World.Environment.NETHER || loc.world.environment == World.Environment.THE_END) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = maxHealth - 2.0
        }
        else {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = maxHealth
        }
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.OAK_SAPLING, 1)

        item.itemMeta = item.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(colorize("&a&lDruid"))
            it.lore(listOf(
                colorize("&7Great for farmers"),
                colorize("&7- &aStick Of Life: grows crops"),
                colorize("&7- &aRegen when on grass"),
                colorize("&7- &a$thornsChance% Chance for poison thorns"),
                colorize("  &8- &aPoisons your attackers"),
                colorize("  &8- &cActs like regen to undead mobs"),
                colorize("&7- &aQuicker than most"),
                colorize("&7- &aAutomatically replants crops"),
                colorize("&7- &a2x crop yield"),
                colorize("&7- &c8 max health"),
                colorize("&7- &cDecreased max health in Nether and End")))
        }

        return item
    }

    private fun Player.isDruid(): Boolean {
        return getRace(magik, this) is Druid
    }

    override fun name(): String {
        return "Druid"
    }

    override fun set(player: Player) {
        player.inventory.addItem(stick.generate())
        usageMap[player] = defaultUses
    }

    override fun remove(player: Player) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
        val inv = player.inventory
        inv.contents.forEach { item ->
            if (item == null) return@forEach
            if (stick.check(item)) {
                inv.remove(item)
            }
        }

        usageMap.remove(player)
        magik.cooldowns.remove(player)
    }

    private fun updateData(player: Player, stick: ItemStack) {
        stick.itemMeta = stick.itemMeta.also {
            if (magik.cooldowns.containsKey(player)) {
                it.displayName(colorize("$stickName &8[&c${(magik.cooldowns[player] ?: 1)}&8]"))
            } else if (usageMap.containsKey(player)) {
                it.displayName(colorize("$stickName &8[&f${(usageMap[player] ?: 1)}&8]"))
            }
        }
    }

    private fun handleDrops(block: Block, harvestItem: ItemStack?) {
        var enchLevel = 0

        if (harvestItem != null && harvestItem.hasItemMeta()) {

            val meta = harvestItem.itemMeta

            if (meta.hasEnchant(Enchantment.FORTUNE)) {
	            enchLevel += harvestItem.getEnchantmentLevel(Enchantment.FORTUNE)
            }
        }

        val blockLocation = block.location
	    val multiplier = 2 + ceil(enchLevel * 1.2).toInt()

        when (block.type) {

            Material.WHEAT -> {
                dropItem(blockLocation, ItemStack(Material.WHEAT, 1 * random(1, multiplier)))
                dropItem(blockLocation, ItemStack(Material.WHEAT_SEEDS, random(1, 4) * multiplier))
            }

            Material.POTATOES -> {
                dropItem(blockLocation, ItemStack(Material.POTATO, random(1, 3) * random(1, multiplier)))
            }

            Material.CARROTS -> {
                dropItem(blockLocation, ItemStack(Material.CARROT, random(1, 3) * random(1, multiplier)))
            }

            Material.BEETROOTS -> {
                dropItem(blockLocation, ItemStack(Material.BEETROOT, 1 * random(1, multiplier)))
                dropItem(blockLocation, ItemStack(Material.BEETROOT_SEEDS, random(1, 4) * multiplier))
            }

            Material.COCOA -> {
                dropItem(blockLocation, ItemStack(Material.POTATO, random(3, 5) * enchLevel))
            }

            else -> return
        }
    }

    private fun harvest(block: Block, harvestItem: ItemStack?): Boolean {
        when (block.type) {

            Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS, Material.COCOA -> {

	            block.blockData = (block.blockData as Ageable).also {
					it.age = 0
	            }

                handleDrops(block, harvestItem)
                return true
            }

            else -> {}
        }

        return false
    }

    private fun useStick(player: Player) {
        val uses = usageMap[player] ?: 1
        usageMap[player] = uses - 1
        if (uses <= 1) {
            magik.cooldowns[player] = defaultCooldown
            usageMap.remove(player)
        }
        val stick = stick.find(player.inventory) ?: return
        updateData(player, stick)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player.takeIf { it.isDruid() }
            ?: return

        if (!stick.check(event.item ?: return)) {
            return
        }

        if (event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }

        val block = event.clickedBlock
            ?: return

        val ageable = block.blockData as? Ageable
            ?: return

        val cooldown = magik.cooldowns[player]

        if (cooldown != null) {
            sendMessage(player, "&cYou can't use this item for another $cooldown seconds!")
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            return
        }

        if (ageable.age >= ageable.maximumAge) {
            if (!player.isSneaking) {
                displayParticles(block.location, Particle.HAPPY_VILLAGER, 10, 1.0, 1.0, 1.0)
                harvest(block, null)
                useStick(player)
            }
            return
        }

        ageable.age = min(ageable.maximumAge, ageable.age + 2)
        block.blockData = ageable
        useStick(player)
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        val player = event.player.takeIf { it.isDruid() }
            ?: return

        if (player.isSneaking) {
            return
        }

        val ageable = event.block.blockData as? Ageable
            ?: return

        if (ageable.age < ageable.maximumAge) {
            return
        }

        if (harvest(event.block, player.activeItem)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        val player = (event.entity as? Player)?.takeIf { it.isDruid() }
            ?: return

        val attacker = event.damager as? LivingEntity
            ?: return

        // 25% chance to inflict poison thorns
        if (random(1, 100) <= thornsChance) {
            attacker.addPotionEffect(PotionEffect(PotionEffectType.POISON, 5 * 20, 1))
            displayParticles(player.location, Particle.HAPPY_VILLAGER, 20, 1.0, 2.0, 1.0)
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player.takeIf { it.isDruid() }
            ?: return

        if (stick.check(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
	    val player = (event.whoClicked as? Player)?.takeIf { it.isDruid() }
		    ?: return
	    
        val item = event.currentItem 
	        ?: return
	    
        if (stick.check(item) && event.inventory.type != InventoryType.CRAFTING) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!event.player.isDruid()) return
        val drops = event.drops
        val remove = mutableListOf<ItemStack>()
        drops.forEach { item ->
            if (item == null) return@forEach
            if (stick.check(item)) {
                remove.add(item)
            }
        }
        remove.forEach { item ->
            drops.remove(item)
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {

        val player = event.player.takeIf { it.isDruid() }
            ?: return

        setRace(magik, player, this, false)
    }
}