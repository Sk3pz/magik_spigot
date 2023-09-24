package es.skepz.magik

import es.skepz.magik.tuodlib.colorize
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType

class SpecialItems(val magik: Magik) {

    val changeItem = CustomItem(magik, Material.CONDUIT, 1, "&eStrange Rock",
        listOf("&6Change to a different race"),
        "change_item", false)

    val changeRecipeKey = NamespacedKey(magik, "change_item")
    val chainhelmKey = NamespacedKey(magik, "chelm")
    val chainhelm2Key = NamespacedKey(magik, "chelm2")
    val chainchesKey = NamespacedKey(magik, "cches")
    val chainlegKey = NamespacedKey(magik, "cleg")
    val chainbootKey = NamespacedKey(magik, "cboot")
    val chainboot2Key = NamespacedKey(magik, "cboot2")

    fun registerRecipes() {
        val changeRecipe = ShapedRecipe(changeRecipeKey, changeItem.generate())
        changeRecipe.shape("ede", "dnd", "ede")
        changeRecipe.setIngredient('e', Material.EMERALD)
        changeRecipe.setIngredient('d', Material.DIAMOND)
        changeRecipe.setIngredient('n', Material.NETHER_STAR)
        magik.server.addRecipe(changeRecipe)

        val chelmRecipe = ShapedRecipe(chainhelmKey, ItemStack(Material.CHAINMAIL_HELMET))
        chelmRecipe.shape("ccc", "cac", "aaa")
        chelmRecipe.setIngredient('c', Material.CHAIN)
        chelmRecipe.setIngredient('a', Material.AIR)
        magik.server.addRecipe(chelmRecipe)

        val chelmRecipe2 = ShapedRecipe(chainhelm2Key, ItemStack(Material.CHAINMAIL_HELMET))
        chelmRecipe2.shape("aaa", "ccc", "cac")
        chelmRecipe2.setIngredient('c', Material.CHAIN)
        chelmRecipe2.setIngredient('a', Material.AIR)
        magik.server.addRecipe(chelmRecipe2)

        val cchestRecipe = ShapedRecipe(chainchesKey, ItemStack(Material.CHAINMAIL_CHESTPLATE))
        cchestRecipe.shape("cac", "ccc", "ccc")
        cchestRecipe.setIngredient('c', Material.CHAIN)
        cchestRecipe.setIngredient('a', Material.AIR)
        magik.server.addRecipe(cchestRecipe)

        val clegRecipe = ShapedRecipe(chainlegKey, ItemStack(Material.CHAINMAIL_LEGGINGS))
        clegRecipe.shape("ccc", "cac", "cac")
        clegRecipe.setIngredient('c', Material.CHAIN)
        clegRecipe.setIngredient('a', Material.AIR)
        magik.server.addRecipe(clegRecipe)

        val cbootRecipe = ShapedRecipe(chainbootKey, ItemStack(Material.CHAINMAIL_BOOTS))
        cbootRecipe.shape("cac", "cac", "aaa")
        cbootRecipe.setIngredient('c', Material.CHAIN)
        cbootRecipe.setIngredient('a', Material.AIR)
        magik.server.addRecipe(cbootRecipe)

        val cbootRecipe2 = ShapedRecipe(chainboot2Key, ItemStack(Material.CHAINMAIL_BOOTS))
        cbootRecipe2.shape("aaa", "cac", "cac")
        cbootRecipe2.setIngredient('c', Material.CHAIN)
        cbootRecipe2.setIngredient('a', Material.AIR)
        magik.server.addRecipe(cbootRecipe2)
    }

}