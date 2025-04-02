package es.skepz.magik.races

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import es.skepz.magik.CustomItem
import es.skepz.magik.Magik
import es.skepz.magik.skepzlib.colorize
import es.skepz.magik.skepzlib.playSound
import es.skepz.magik.skepzlib.sendMessage
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType.*
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class Avian(magik: Magik) : Race(magik) {

    private val maxHealth = 10.0

    private val elytra = CustomItem(magik, Material.ELYTRA, 1, "&fAvian Wings",
        listOf("&6Allows avians to fly"),
        "avian_elytra", true)

    private val firework = CustomItem(magik, Material.FIREWORK_ROCKET, 1, "&cInfinite Firework",
        listOf("&6Allows avians to fly"),
        "avian_firework", false)

    override fun update(player: Player) {
        if (!player.isSneaking) {
            player.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 2, 0, false, false))
        }

        if ((player as LivingEntity).isOnGround || player.isSleeping || player.isSwimming) {
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 2, 1, false, false))
        }
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.ELYTRA, 1)
        item.itemMeta = item.itemMeta.also { meta ->
            meta.isUnbreakable = true
            meta.displayName(colorize("&6&lAvian"))
            meta.lore(
                listOf(
                    "&7Great for players who love to explore",
                    "&7- &aPermanent elytra",
                    "&7- &aInfinite firework",
                    "&7- &aJumps higher",
                    "&7- &aImmune to fall damage", // todo maybe reduced fall damage?
                    "&7- &cSlower on the ground",
                    "&7- &cCan only wear leather and chainmail armor",
                    "&7- &c5 max hearts",
                    "&7- &71 block tall",
                    "&7- &cAllergic to unloaded chunks"
                ).map { colorize(it) }
            )
        }

        return item
    }

    override fun name(): String {
        return "Avian"
    }

    override fun set(player: Player) {
        val inventory = player.inventory

        inventory.addItem(firework.generate())

        // handle armor
        val helm = inventory.helmet
        val ches = inventory.chestplate
        val legg = inventory.leggings
        val boot = inventory.boots

        if (helm != null && isHeavyArmor(helm)) {
            inventory.addItem(helm)
            inventory.helmet = ItemStack(Material.AIR)
        }

        if (ches != null && !elytra.check(ches)) {
            inventory.addItem(ches)
        }

        inventory.chestplate = elytra.generate()
        if (legg != null && isHeavyArmor(legg)) {
            inventory.addItem(legg)
            inventory.leggings = ItemStack(Material.AIR)
        }
        if (boot != null && isHeavyArmor(boot)) {
            inventory.addItem(boot)
            inventory.boots = ItemStack(Material.AIR)
        }

        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = maxHealth
        player.getAttribute(Attribute.GENERIC_SCALE)?.baseValue = 0.5
    }

    override fun remove(player: Player) {
        val inventory = player.inventory

        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
        player.getAttribute(Attribute.GENERIC_SCALE)?.baseValue = 1.0
        inventory.chestplate = ItemStack(Material.AIR)
        inventory.contents.forEach { item ->
            if (item == null) return@forEach
            if (firework.check(item)) {
                inventory.remove(item)
            }
        }
    }

    private fun isAvian(p: Player): Boolean {
        return getRace(magik, p) is Avian
    }

    private fun isHeavyArmor(item: ItemStack): Boolean {
        return item.type == Material.IRON_BOOTS || item.type == Material.DIAMOND_BOOTS ||
                item.type == Material.GOLDEN_BOOTS || item.type == Material.NETHERITE_BOOTS ||
                item.type == Material.IRON_LEGGINGS || item.type == Material.DIAMOND_LEGGINGS ||
                item.type == Material.GOLDEN_LEGGINGS || item.type == Material.NETHERITE_LEGGINGS ||
                item.type == Material.IRON_CHESTPLATE || item.type == Material.DIAMOND_CHESTPLATE ||
                item.type == Material.GOLDEN_CHESTPLATE || item.type == Material.NETHERITE_CHESTPLATE ||
                item.type == Material.IRON_HELMET || item.type == Material.DIAMOND_HELMET ||
                item.type == Material.GOLDEN_HELMET || item.type == Material.NETHERITE_HELMET
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val item = event.item
            ?: return

        val player = event.player
        if (!isAvian(player)) {
            return
        }

        // check if the item is armor
        if (isHeavyArmor(item) && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            event.isCancelled = true
            sendMessage(player, "&cAvians can only wear leather or chainmail armor!")
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            return
        }

        if ((item.type == Material.LEATHER_CHESTPLATE || item.type == Material.CHAINMAIL_CHESTPLATE || item.type == Material.ELYTRA)
            && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            return
        }

        // check if item is the special firework
        if (firework.check(item) && (event.action != Action.RIGHT_CLICK_AIR)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteractAtEntity(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        if (!isAvian(player)) {
            return
        }

        // check if the player's hand is empty
        if (player.inventory.itemInMainHand.type != Material.AIR) {
            return
        }

        // check if the entity is a player
        if (event.rightClicked !is Player) {
            return
        }

        // make the avian sit on the player
        val target = event.rightClicked as Player
        target.addPassenger(player)
        target.allowFlight = true;
    }

    // when a player dismounts another player
    @EventHandler
    fun onPlayerDismount(event: EntityDismountEvent) {
        if (event.entity !is Player || event.dismounted !is Player) {
            return
        }

        val player = event.entity as Player
        val target = event.dismounted as Player

        if (!isAvian(player)) {
            return
        }

        target.allowFlight = false
    }

    @EventHandler
    fun onPlayerKinetic(event: EntityDamageEvent) {
        if (event.entity !is Player) {
            return
        }
        if (event.cause != EntityDamageEvent.DamageCause.FLY_INTO_WALL) {
            return
        }
        if (isAvian(event.entity as Player)) {
            event.isCancelled = true
            return
        }

        event.isCancelled = true
    }

    @EventHandler
    fun onBoost(event: PlayerElytraBoostEvent) {
        val player = event.player
        if (!isAvian(player)) {
            return
        }

        if (player.gameMode == GameMode.CREATIVE) {
            return
        }

        if (firework.check(event.itemStack)) {
            // get the slot the firework was in
            val slot = player.inventory.contents.indexOf(event.itemStack)

            player.inventory.remove(event.firework.item)

            Bukkit.getScheduler().scheduleSyncDelayedTask(magik, {
                player.inventory.setItem(slot, firework.generate())
            }, 5L)
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        if (!isAvian(player)) {
            return
        }

        val current = event.currentItem

        if (current != null) {
            if (event.inventory.type != CRAFTING && firework.check(current)) {
                event.isCancelled = true
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
                return
            }

            if (event.click.isShiftClick && event.inventory.type == CRAFTING && isHeavyArmor(current)) {
                event.isCancelled = true
                sendMessage(player, "&cAvians can only wear leather or chainmail armor!")
                playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
                return
            }
        }

        if (event.slotType != SlotType.ARMOR) {
            return
        }

        if (current?.type == Material.ELYTRA) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            return
        }

        val cursor = event.cursor
        if (isHeavyArmor(cursor)) {
            event.isCancelled = true
            sendMessage(player, "&cAvians can only wear leather or chainmail armor!")
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            return
        }
    }

    @EventHandler
    fun onPlayerFall(event: EntityDamageEvent) {
        if (event.entity !is Player || event.cause != EntityDamageEvent.DamageCause.FALL) {
            return
        }

        if (isAvian(event.entity as Player)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val player = event.player
        if (!isAvian(player)) {
            return
        }

        if (firework.check(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!isAvian(event.player)) {
            return
        }
        remove(event.player)
        val drops = event.drops
        val flagged = mutableListOf<ItemStack>()
        drops.forEach { item ->
            if (item == null) return@forEach
            if (elytra.check(item) || (item.type == Material.ELYTRA && (item.hasItemMeta()))) {
                flagged.add(item)
            }
            if (firework.check(item)) {
                flagged.add(item)
            }
        }
        flagged.forEach {
            drops.remove(it)
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        if (!isAvian(player)) {
            return
        }

        setRace(magik, player, this, false)
    }
}