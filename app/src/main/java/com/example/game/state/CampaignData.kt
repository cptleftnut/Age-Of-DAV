package com.example.game.state

import com.example.game.model.Faction
import com.example.game.model.MajorGod

data class CampaignMission(
    val id: Int,
    val title: String,
    val subtitle: String,
    val playerFaction: Faction,
    val playerGod: MajorGod,
    val enemyFaction: Faction,
    val objectiveDesc: String,
    val introDialogue: String,
    val initialPlayerUnitsCount: Int = 8,
    val initialEnemyUnitsCount: Int = 10,
    val mapSize: Int = 50
)

object CampaignData {
    val missions = listOf(
        CampaignMission(
            id = 1,
            title = "Act I: Gates of Atlantis",
            subtitle = "Fall of the Trident",
            playerFaction = Faction.GREEK,
            playerGod = MajorGod.POSEIDON,
            enemyFaction = Faction.EGYPTIAN,
            objectiveDesc = "Build a Town Center, train 6 Hoplites, and defeat the raiding Egyptian army.",
            introDialogue = "Arkantos! Black sails have been spotted on the horizon. Pirates and mythical beasts storm the harbor of Atlantis. To victory or to the deep!"
        ),
        CampaignMission(
            id = 2,
            title = "Act II: Siege of Troy",
            subtitle = "The Trojan Horse",
            playerFaction = Faction.GREEK,
            playerGod = MajorGod.ZEUS,
            enemyFaction = Faction.GREEK,
            objectiveDesc = "Advance to the Classical Age, summon Minotaurs, and breach the walls of Troy.",
            introDialogue = "High King Agamemnon calls upon Atlantis! Troy's walls stand unbreakable, but Zeus grants us thunder and mythical beasts. Lead the vanguard!"
        ),
        CampaignMission(
            id = 3,
            title = "Act III: Escaping Hades",
            subtitle = "The Underworld Gates",
            playerFaction = Faction.NORSE,
            playerGod = MajorGod.ODIN,
            enemyFaction = Faction.NORSE,
            objectiveDesc = "Survive the Viking Frost Giant waves, collect 3 Sacred Relics, and cast Fimbulwinter.",
            introDialogue = "Loki's trickery has frozen the northern realm. The Valkyries descend to aid Arkantos and King Folstag against the Frost Giants!"
        ),
        CampaignMission(
            id = 4,
            title = "Act IV: Titanomachy",
            subtitle = "Awakening of the Colossus",
            playerFaction = Faction.ATLANTEAN,
            playerGod = MajorGod.KRONOS,
            enemyFaction = Faction.EGYPTIAN,
            objectiveDesc = "Build a Wonder, awaken the Colossal Titan, and annihilate the Enemy Base.",
            introDialogue = "The ultimate clash of gods and myth! Unleash the power of the Titans to restore peace across Greece, Egypt, and Asgard!"
        )
    )
}
