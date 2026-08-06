package com.example.game.model

/**
 * Playable Factions / Pantheons in Age of Mythology.
 */
enum class Faction(
    val displayName: String,
    val iconSymbol: String,
    val primaryColorHex: String,
    val favorGatheringDesc: String
) {
    GREEK("Greek Gods", "⚡", "#4A90E2", "Pray at the Temple to gather Favor"),
    EGYPTIAN("Egyptian Pharaohs", "𓀀", "#F5A623", "Build Monuments to generate Favor"),
    NORSE("Norse Vikings", "⚡", "#7ED321", "Fight enemies and hunt beasts to gain Favor"),
    ATLANTEAN("Atlantean Titans", "🌊", "#BD10E0", "Control Oracle nodes to extract Favor")
}

/**
 * Major Gods for selection.
 */
enum class MajorGod(
    val faction: Faction,
    val godName: String,
    val title: String,
    val passiveBonus: String,
    val startingGodPower: GodPower
) {
    ZEUS(Faction.GREEK, "Zeus", "King of the Olympians", "Hopilites move 15% faster & bonus Hopilite HP", GodPower.LIGHTNING_BOLT),
    POSEIDON(Faction.GREEK, "Poseidon", "God of the Seas & Horses", "Kills spawn Militia & extra Gold", GodPower.CEASEFIRE),
    HADES(Faction.GREEK, "Hades", "Ruler of the Underworld", "Dead soldiers turn into Shades & Archers +2 Range", GodPower.RESTORATION),

    RA(Faction.EGYPTIAN, "Ra", "Sun God of Heliopolis", "Pharaoh empowers buildings faster & +20% Farm yield", GodPower.RAIN),
    ISIS(Faction.EGYPTIAN, "Isis", "Goddess of Magic & Protection", "Monuments block enemy God Powers & -10% Tech cost", GodPower.PROSPERITY),
    SET(Faction.EGYPTIAN, "Set", "God of Chaos & Animals", "Pharaoh can convert wild beasts to fight for you", GodPower.VISION),

    ODIN(Faction.NORSE, "Odin", "Allfather of Asgard", "Human units regenerate HP slowly & Ravens scout map", GodPower.GREAT_HUNT),
    THOR(Faction.NORSE, "Thor", "God of Thunder & Dwarves", "Dwarven Armory available early & cheaper upgrades", GodPower.DWARVEN_MINE),
    LOKI(Faction.NORSE, "Loki", "Trickster God", "Hersir summon myth units in battle & faster infantry", GodPower.SPY),

    KRONOS(Faction.ATLANTEAN, "Kronos", "Titan of Time", "Can teleport buildings & myth units move +10% fast", GodPower.SHOCKWAVE),
    GAIA(Faction.ATLANTEAN, "Gaia", "Mother Earth", "Gaia lush heals nearby friendly units & blocks enemy buildings", GodPower.GAIA_FOREST)
}

/**
 * Major Ages of Mythology.
 */
enum class Age(val ageName: String, val level: Int, val foodCost: Int, val goldCost: Int) {
    ARCHAIC("Archaic Age", 1, 0, 0),
    CLASSICAL("Classical Age", 2, 400, 0),
    HEROIC("Heroic Age", 3, 800, 500),
    MYTHIC("Mythic Age", 4, 1000, 1000)
}

/**
 * Minor Gods selected when aging up.
 */
enum class MinorGod(
    val godName: String,
    val age: Age,
    val faction: Faction,
    val mythUnitName: String,
    val grantedGodPower: GodPower
) {
    // Greek
    ARES("Ares", Age.CLASSICAL, Faction.GREEK, "Cyclops", GodPower.PESTILENCE),
    HERMES("Hermes", Age.CLASSICAL, Faction.GREEK, "Centaur", GodPower.CEASEFIRE),
    APOLLO("Apollo", Age.HEROIC, Faction.GREEK, "Manticore", GodPower.UNDERWORLD_PASSAGE),
    DIONYSUS("Dionysus", Age.HEROIC, Faction.GREEK, "Hydra", GodPower.BRONZE),
    HEPHAESTUS("Hephaestus", Age.MYTHIC, Faction.GREEK, "Colossus", GodPower.PLENTY_VAULT),

    // Egyptian
    ANUBIS("Anubis", Age.CLASSICAL, Faction.EGYPTIAN, "Anubite", GodPower.PLAGUE_OF_SERPENTS),
    BASTET("Bastet", Age.CLASSICAL, Faction.EGYPTIAN, "Sphinx", GodPower.ECLIPSE),
    SEKHMET("Sekhmet", Age.HEROIC, Faction.EGYPTIAN, "Scorpion Man", GodPower.CITADEL),
    HORUS("Horus", Age.MYTHIC, Faction.EGYPTIAN, "Avenger", GodPower.TORNADO),

    // Norse
    FREYJA("Freyja", Age.CLASSICAL, Faction.NORSE, "Valkyrie", GodPower.FROST),
    HEIMDALL("Heimdall", Age.CLASSICAL, Faction.NORSE, "Einherjar", GodPower.UNDERWORLD_PASSAGE),
    SKADI("Skadi", Age.HEROIC, Faction.NORSE, "Frost Giant", GodPower.WINTER_BLAST),
    BALDR("Baldr", Age.MYTHIC, Faction.NORSE, "Fire Giant", GodPower.RAGNAROK)
}

/**
 * God Powers castable by player and AI.
 */
enum class GodPower(
    val displayName: String,
    val iconSymbol: String,
    val cooldownSeconds: Int,
    val description: String
) {
    LIGHTNING_BOLT("Lightning Bolt", "⚡", 45, "Smite an enemy myth unit or structure with divine thunder"),
    METEOR("Meteor Storm", "☄️", 90, "Rain burning meteors from the sky on enemy forces"),
    HEALING_SPRING("Healing Spring", "⛲", 60, "Summon a holy spring that heals allied units in range"),
    RESTORATION("Restoration", "✨", 60, "Instantly cure and fully heal all friendly armies"),
    CEASEFIRE("Ceasefire", "🕊️", 120, "Halt all combat across the battlefield for 20 seconds"),
    GREAT_HUNT("Great Hunt", "🦌", 30, "Multiply nearby wild animals into food sources"),
    FROST("Frost", "❄️", 75, "Freeze enemy army in block of ice, rendering them helpless"),
    SHOCKWAVE("Shockwave", "💥", 40, "Knock back and stun nearby enemy soldiers"),
    TORNADO("Tornado", "🌪️", 100, "Summon a devastating twister that rips through enemy base"),
    RAIN("Rain", "🌧️", 50, "Boost farm food gathering rate by +100% for 30 seconds"),
    PROSPERITY("Prosperity", "💎", 60, "Boost gold mining rate by +100% for 30 seconds"),
    UNDERWORLD_PASSAGE("Underworld Passage", "🕳️", 90, "Create instant teleport portals between two map points"),
    BRONZE("Bronze Skin", "🛡️", 80, "Grant golden invulnerability armor to infantry"),
    PLENTY_VAULT("Vault of Plenty", "🏛️", 150, "Summon an ancient vault generating infinite resources"),
    RAGNAROK("Ragnarok", "🔥", 180, "Transform all Villagers into powerful Heroes of Ragnarok!"),
    VISION("Divine Vision", "👁️", 45, "Reveal enemy base movements through the clouds"),
    DWARVEN_MINE("Dwarven Gold Mine", "⛏️", 60, "Summon a rich dwarven gold vein anywhere on ground"),
    SPY("Loki Spy", "👁️‍🗨️", 30, "Attach an invisible eye to an enemy hero"),
    GAIA_FOREST("Gaia Lush Forest", "🌲", 50, "Summon a lush healing forest that blocks enemies"),
    PESTILENCE("Pestilence", "🦟", 60, "Disable enemy military building training for 30 seconds"),
    PLAGUE_OF_SERPENTS("Plague of Serpents", "🐍", 65, "Summon a horde of sacred cobras to attack foes"),
    ECLIPSE("Solar Eclipse", "🌑", 70, "Block sunlight and boost all Myth Unit damage by +50%"),
    CITADEL("Citadel Fortress", "🏰", 110, "Upgrade Town Center into a fortified Citadel"),
    WINTER_BLAST("Winter Blast", "🌨️", 80, "Freeze enemy troops with arctic snow")
}

/**
 * Core RTS Resources.
 */
data class Resources(
    val food: Int = 200,
    val wood: Int = 200,
    val gold: Int = 100,
    val favor: Int = 25
) {
    fun hasEnough(cost: Resources): Boolean =
        food >= cost.food && wood >= cost.wood && gold >= cost.gold && favor >= cost.favor

    fun minus(cost: Resources) = Resources(
        food = (food - cost.food).coerceAtLeast(0),
        wood = (wood - cost.wood).coerceAtLeast(0),
        gold = (gold - cost.gold).coerceAtLeast(0),
        favor = (favor - cost.favor).coerceAtLeast(0)
    )

    fun plus(gain: Resources) = Resources(
        food = food + gain.food,
        wood = wood + gain.wood,
        gold = gold + gain.gold,
        favor = favor + gain.favor
    )
}

/**
 * Player Faction Controller (0 = Player, 1 = AI Enemy, 2 = Neutral).
 */
enum class Owner(val id: Int, val colorHex: String, val label: String) {
    PLAYER(0, "#2196F3", "Player"),
    AI_ENEMY(1, "#F44336", "Enemy AI"),
    NEUTRAL(2, "#9E9E9E", "Neutral")
}
