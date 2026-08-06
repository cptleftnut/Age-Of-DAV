package com.example.game.assets

import androidx.annotation.DrawableRes
import com.example.R
import com.example.game.model.*

/**
 * Central asset registry mapping all game entities to their vector drawable resources.
 * Replaces emoji placeholders with proper scalable vector icons.
 */
object AssetManager {

    @DrawableRes
    fun getUnitDrawable(type: UnitType): Int = when (type) {
        UnitType.VILLAGER -> R.drawable.ic_unit_villager
        UnitType.DWARVEN_MINER -> R.drawable.ic_unit_dwarven_miner
        UnitType.HOPLITE -> R.drawable.ic_unit_hoplite
        UnitType.SPEARMAN -> R.drawable.ic_unit_spearman
        UnitType.TOXOTES_ARCHER -> R.drawable.ic_unit_toxotes
        UnitType.PRODROMOS_CAVALRY -> R.drawable.ic_unit_cavalry
        UnitType.THROWING_AXEMAN -> R.drawable.ic_unit_axeman
        UnitType.HERCULES -> R.drawable.ic_unit_hercules
        UnitType.PHARAOH -> R.drawable.ic_unit_pharaoh
        UnitType.HERSIR -> R.drawable.ic_unit_hersir
        UnitType.MINOTAUR -> R.drawable.ic_unit_minotaur
        UnitType.CYCLOPS -> R.drawable.ic_unit_cyclops
        UnitType.VALKYRIE -> R.drawable.ic_unit_valkyrie
        UnitType.SPHINX -> R.drawable.ic_unit_sphinx
        UnitType.TITAN -> R.drawable.ic_unit_titan
    }

    @DrawableRes
    fun getBuildingDrawable(type: BuildingType): Int = when (type) {
        BuildingType.TOWN_CENTER -> R.drawable.ic_building_town_center
        BuildingType.HOUSE -> R.drawable.ic_building_house
        BuildingType.FARM -> R.drawable.ic_building_farm
        BuildingType.GRANARY -> R.drawable.ic_building_granary
        BuildingType.STOREHOUSE -> R.drawable.ic_building_storehouse
        BuildingType.BARRACKS -> R.drawable.ic_building_barracks
        BuildingType.TEMPLE -> R.drawable.ic_building_temple
        BuildingType.ARMORY -> R.drawable.ic_building_armory
        BuildingType.WONDER -> R.drawable.ic_building_wonder
    }

    @DrawableRes
    fun getResourceDrawable(type: ResourceNodeType): Int = when (type) {
        ResourceNodeType.GOLD_MINE -> R.drawable.ic_resource_gold
        ResourceNodeType.FOREST_TREE -> R.drawable.ic_resource_wood
        ResourceNodeType.BERRY_BUSH -> R.drawable.ic_resource_food
        ResourceNodeType.HUNT_ANIMAL -> R.drawable.ic_resource_food
        ResourceNodeType.RELIC -> R.drawable.ic_resource_favor
    }

    @DrawableRes
    fun getFactionDrawable(faction: Faction): Int = when (faction) {
        Faction.GREEK -> R.drawable.ic_faction_greek
        Faction.EGYPTIAN -> R.drawable.ic_faction_egyptian
        Faction.NORSE -> R.drawable.ic_faction_norse
        Faction.ATLANTEAN -> R.drawable.ic_faction_atlantean
    }

    @DrawableRes
    fun getGodPowerDrawable(power: GodPower): Int = when (power) {
        GodPower.LIGHTNING_BOLT -> R.drawable.ic_gp_lightning
        GodPower.METEOR -> R.drawable.ic_gp_meteor
        GodPower.HEALING_SPRING -> R.drawable.ic_gp_healing
        GodPower.RESTORATION -> R.drawable.ic_gp_restoration
        GodPower.CEASEFIRE -> R.drawable.ic_gp_ceasefire
        GodPower.GREAT_HUNT -> R.drawable.ic_gp_great_hunt
        GodPower.FROST -> R.drawable.ic_gp_frost
        GodPower.SHOCKWAVE -> R.drawable.ic_gp_shockwave
        GodPower.TORNADO -> R.drawable.ic_gp_tornado
        GodPower.RAIN -> R.drawable.ic_gp_rain
        GodPower.PROSPERITY -> R.drawable.ic_gp_prosperity
        GodPower.UNDERWORLD_PASSAGE -> R.drawable.ic_gp_passage
        GodPower.BRONZE -> R.drawable.ic_gp_bronze
        GodPower.PLENTY_VAULT -> R.drawable.ic_gp_plenty
        GodPower.RAGNAROK -> R.drawable.ic_gp_ragnarok
        GodPower.VISION -> R.drawable.ic_gp_vision
        GodPower.DWARVEN_MINE -> R.drawable.ic_gp_dwarven_mine
        GodPower.SPY -> R.drawable.ic_gp_spy
        GodPower.GAIA_FOREST -> R.drawable.ic_gp_gaia_forest
        GodPower.PESTILENCE -> R.drawable.ic_gp_pestilence
        GodPower.PLAGUE_OF_SERPENTS -> R.drawable.ic_gp_serpents
        GodPower.ECLIPSE -> R.drawable.ic_gp_eclipse
        GodPower.CITADEL -> R.drawable.ic_gp_citadel
        GodPower.WINTER_BLAST -> R.drawable.ic_gp_winter
    }

    @DrawableRes
    fun getTerrainDrawable(type: TerrainType): Int = when (type) {
        TerrainType.GRASS -> R.drawable.ic_terrain_grass
        TerrainType.DESERT_SAND -> R.drawable.ic_terrain_desert
        TerrainType.SNOW -> R.drawable.ic_terrain_snow
        TerrainType.WATER -> R.drawable.ic_terrain_water
        TerrainType.ROCK -> R.drawable.ic_terrain_rock
    }

    @DrawableRes
    fun getGatherResourceDrawable(type: GatherResourceType): Int = when (type) {
        GatherResourceType.FOOD -> R.drawable.ic_resource_food
        GatherResourceType.WOOD -> R.drawable.ic_resource_wood
        GatherResourceType.GOLD -> R.drawable.ic_resource_gold
        GatherResourceType.FAVOR -> R.drawable.ic_resource_favor
    }
}

/* ── Extension Properties for easy access ── */

val UnitType.iconRes: Int get() = AssetManager.getUnitDrawable(this)
val BuildingType.iconRes: Int get() = AssetManager.getBuildingDrawable(this)
val ResourceNodeType.iconRes: Int get() = AssetManager.getResourceDrawable(this)
val Faction.iconRes: Int get() = AssetManager.getFactionDrawable(this)
val GodPower.iconRes: Int get() = AssetManager.getGodPowerDrawable(this)
val TerrainType.iconRes: Int get() = AssetManager.getTerrainDrawable(this)
val GatherResourceType.iconRes: Int get() = AssetManager.getGatherResourceDrawable(this)
