package es.skepz.magik.races

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import es.skepz.magik.Magik
import es.skepz.magik.tuodlib.*
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
import org.bukkit.event.inventory.InventoryType.*
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
        val emeta = elytra.itemMeta
        emeta.isUnbreakable = true
        emeta.displayName(Component.text(colorize(wingsName)))
        emeta.lore(listOf(Component.text(colorize("&6Allows avians to fly"))))
        elytra.setItemMeta(emeta)

        val fmeta = firework.itemMeta as FireworkMeta
        fmeta.displayName(Component.text(colorize(fireworkName)))
        fmeta.lore(listOf(Component.text(colorize("&6Allows avians to fly"))))
        fmeta.power = 1
        firework.setItemMeta(fmeta)
    }

    override fun update(player: Player) {
        if (!player.isSneaking) {
            player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 2, 1, false, false))
        }
        if (player.isOnGround || player.isSleeping || player.isSwimming) {
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 2, 1, false, false))
        }
    }

    override fun guiDisplayItem(): ItemStack {
        val item = ItemStack(Material.ELYTRA, 1)
        val meta = item.itemMeta
        meta.isUnbreakable = true
        meta.displayName(Component.text(colorize("&6&lAvian")))
        meta.lore(listOf(Component.text(colorize("&7- &aPermanent elytra")),
                         Component.text(colorize("&7- &aInfinite firework")),
                         Component.text(colorize("&7- &aJump boost")),
                         Component.text(colorize("&7- &aImmune to fall damage")), // todo maybe reduced fall damage?
                         Component.text(colorize("&7- &cSlowness when on ground")),
                         Component.text(colorize("&7- &cCan only wear leather and chainmail armor")),
                         Component.text(colorize("&7- &c-4 max hearts"))))
        item.setItemMeta(meta)

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
        if (!player.inventory.contains(firework)) {
            player.inventory.addItem(firework)
        }

        // handle armor
        val helm = player.inventory.helmet
        val ches = player.inventory.chestplate
        val legg = player.inventory.leggings
        val boot = player.inventory.boots

        if (helm != null && isHeavyArmor(helm)) {
            player.inventory.addItem(helm)
            player.inventory.helmet = ItemStack(Material.AIR)
        }
        if (ches != null && !checkElytra(ches)) {
            player.inventory.addItem(ches)
        }
        player.inventory.chestplate = elytra
        if (legg != null && isHeavyArmor(legg)) {
            player.inventory.addItem(legg)
            player.inventory.leggings = ItemStack(Material.AIR)
        }
        if (boot != null && isHeavyArmor(boot)) {
            player.inventory.addItem(boot)
            player.inventory.boots = ItemStack(Material.AIR)
        }

        player.maxHealth = maxHealth
    }

    override fun remove(player: Player) {
        player.maxHealth = 20.0
        player.inventory.chestplate = ItemStack(Material.AIR)
        player.inventory.remove(firework)
    }

    private fun isAvian(p: Player): Boolean {
        val race = getRace(magik, p)
        return race != null && race is Avian
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
        val p = event.player
        if (!isAvian(p)) {
            return
        }
        val item = event.item ?: return
        // check if the item is armor
        if (isHeavyArmor(item) && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            event.isCancelled = true
            sendMessage(p, "&cAvians can only wear leather or chainmail armor!")
            playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            return
        }

        if ((item.type == Material.LEATHER_CHESTPLATE || item.type == Material.CHAINMAIL_CHESTPLATE || item.type == Material.ELYTRA)
            && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            event.isCancelled = true
            playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            return
        }

        // check if item is the special firework
        if (checkFirework(item) && (event.action != Action.RIGHT_CLICK_AIR)) {
            if (p.gameMode == GameMode.CREATIVE) {
                return
            }
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBoost(event: PlayerElytraBoostEvent) {
        val p = event.player
        if (!isAvian(p)) {
            return
        }
        if (p.gameMode == GameMode.CREATIVE) {
            return
        }

        if (checkFirework(event.firework.item)) {
            p.inventory.remove(firework)
            p.inventory.addItem(firework)
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val p = event.whoClicked as Player
        if (!isAvian(p)) {
            return
        }

        val current = event.currentItem

        if (event.click.isShiftClick && (event.inventory.type == CRAFTING)) {
            if (current != null && isHeavyArmor(current)) {
                event.isCancelled = true
                sendMessage(p, "&cAvians can only wear leather or chainmail armor!")
                playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
                return
            }
        }

        if (event.slotType != SlotType.ARMOR) {
            return
        }
        if (current?.type == Material.ELYTRA) {
            event.isCancelled = true
            playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            return
        }
        val cursor = event.cursor
        if (cursor != null && isHeavyArmor(cursor)) {
            event.isCancelled = true
            sendMessage(p, "&cAvians can only wear leather or chainmail armor!")
            playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
            return
        }
    }

    @EventHandler
    fun onPlayerFall(event: EntityDamageEvent) {
        if (event.entity !is Player || event.cause != EntityDamageEvent.DamageCause.FALL) {
            return
        }
        val p = event.entity as Player
        if (isAvian(p)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun itemDrop(event: PlayerDropItemEvent) {
        val p = event.player
        if (!isAvian(p)) {
            return
        }
        val item = event.itemDrop.itemStack
        if (checkFirework(item)) {
            event.isCancelled = true
            playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.1f)
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val p = event.player
        if (!isAvian(p)) {
            return
        }

        event.drops.remove(elytra)
        event.drops.remove(firework)
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val p = event.player
        if (!isAvian(p)) {
            return
        }

        setRace(magik, p, this)
    }
}