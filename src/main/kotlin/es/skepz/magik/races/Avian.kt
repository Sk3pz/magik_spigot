package es.skepz.magik.races

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.colorize
import es.skepz.magik.tuodlib.playSound
import es.skepz.magik.tuodlib.sendMessage
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType.CRAFTING
import org.bukkit.event.inventory.InventoryType.SlotType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Avian(magik: Magik) : Race(magik) {

    private val maxHealth = 12.0
    private val wingsName = "&fAvian Wings"
    private val fireworkName = "&cInfinite Firework"
    private val elytra = ItemStack(Material.ELYTRA, 1)
    private val firework = ItemStack(Material.FIREWORK_ROCKET, 1)

    init {
        elytra.itemMeta = elytra.itemMeta.also {
            it.isUnbreakable = true
            it.displayName(Component.text(colorize(wingsName)))
            it.lore(listOf(Component.text(colorize("&6Allows avians to fly"))))
        }

        firework.itemMeta = (firework.itemMeta as FireworkMeta).also {
            it.displayName(Component.text(colorize(fireworkName)))
            it.lore(listOf(Component.text(colorize("&6Allows avians to fly"))))
            it.power = 1
        }
    }

    override fun update(player: Player) {
        if (!player.isSneaking) {
            player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 2, 0, false, false))
        }

        if (player.isOnGround || player.isSleeping || player.isSwimming) {
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 2, 1, false, false))
        }
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.ELYTRA, 1)
        item.itemMeta = item.itemMeta.also { meta ->
            meta.isUnbreakable = true
            meta.displayName(Component.text(colorize("&6&lAvian")))
            meta.lore(
                listOf(
                    "&7Great for players who love to explore",
                    "&7- &aPermanent elytra",
                    "&7- &aInfinite firework",
                    "&7- &aJump boost",
                    "&7- &aImmune to fall damage", // todo maybe reduced fall damage?
                    "&7- &cSlowness when on ground",
                    "&7- &cCan only wear leather and chainmail armor",
                    "&7- &c-4 max hearts"
                ).map { Component.text(colorize(it)) }
            )
        }

        return item
    }

    override fun name(): String {
        return "Avian"
    }

    private fun checkElytra(item: ItemStack): Boolean {
        return item.isSimilar(elytra)
    }

    private fun checkFirework(item: ItemStack): Boolean {
        return item.isSimilar(firework)
    }

    override fun set(player: Player) {
        val inventory = player.inventory

        if (!inventory.contains(firework)) {
            inventory.addItem(firework)
        }

        // handle armor
        val helm = inventory.helmet
        val ches = inventory.chestplate
        val legg = inventory.leggings
        val boot = inventory.boots

        if (helm != null && isHeavyArmor(helm)) {
            inventory.addItem(helm)
            inventory.helmet = ItemStack(Material.AIR)
        }

        if (ches != null && !checkElytra(ches)) {
            inventory.addItem(ches)
        }

        inventory.chestplate = elytra
        if (legg != null && isHeavyArmor(legg)) {
            inventory.addItem(legg)
            inventory.leggings = ItemStack(Material.AIR)
        }
        if (boot != null && isHeavyArmor(boot)) {
            inventory.addItem(boot)
            inventory.boots = ItemStack(Material.AIR)
        }

        player.maxHealth = maxHealth
    }

    override fun remove(player: Player) {
        val inventory = player.inventory

        player.resetMaxHealth()
        inventory.chestplate = ItemStack(Material.AIR)
        inventory.remove(firework)
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
        if (checkFirework(item) && (event.action != Action.RIGHT_CLICK_AIR)) {
            if (player.gameMode != GameMode.CREATIVE) {
                event.isCancelled = true
            }
        }
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

        if (checkFirework(event.firework.item)) {
            player.inventory.remove(firework)
            player.inventory.addItem(firework)
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        if (!isAvian(player)) {
            return
        }

        val current = event.currentItem

        if (event.click.isShiftClick && (event.inventory.type == CRAFTING)) {
            if (current != null && isHeavyArmor(current)) {
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
        if (cursor != null && isHeavyArmor(cursor)) {
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

        if (checkFirework(event.itemDrop.itemStack)) {
            event.isCancelled = true
            playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (!isAvian(event.player)) {
            return
        }

        event.drops.remove(elytra)
        event.drops.remove(firework)
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        if (!isAvian(player)) {
            return
        }

        setRace(magik, player, this)
    }
}