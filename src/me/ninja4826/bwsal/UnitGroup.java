package me.ninja4826.bwsal;

import java.util.HashSet;

import bwapi.*;

public class UnitGroup extends HashSet<Unit> {
	
	public enum FilterFlag {
		exists,
	    isAccelerating,
	    isAttacking,
	    isBeingConstructed,
	    isBeingGathered,
	    isBeingHealed,
	    isBlind,
	    isBraking,
	    isBurrowed,
	    isCarryingGas,
	    isCarryingMinerals,
	    isCloaked,
	    isCompleted,
	    isConstructing,
	    isDefenseMatrixed,
	    isDetected,
	    isEnsnared,
	    isFollowing,
	    isGatheringGas,
	    isGatheringMinerals,
	    isHallucination,
	    isHoldingPosition,
	    isIdle,
	    isInterruptible,
	    isIrradiated,
	    isLifted,
	    isLoaded,
	    isLockedDown,
	    isMaelstrommed,
	    isMorphing,
	    isMoving,
	    isParasited,
	    isPatrolling,
	    isPlagued,
	    isRepairing,
	    isResearching,
	    isSelected,
	    isSieged,
	    isStartingAttack,
	    isStasised,
	    isStimmed,
	    isStuck,
	    isTraining,
	    isUnderStorm,
	    isUnpowered,
	    isUpgrading,
	    isVisible,

	    canProduce,
	    canAttack,
	    canMove,
	    isFlyer,
	    regeneratesHP,
	    isSpellcaster,
	    hasPermanentCloak,
	    isInvincible,
	    isOrganic,
	    isMechanical,
	    isRobotic,
	    isDetector,
	    isResourceContainer,
	    isResourceDepot,
	    isRefinery,
	    isWorker,
	    requiresPsi,
	    requiresCreep,
	    isTwoUnitsInOneEgg,
	    isBurrowable,
	    isCloakable,
	    isBuilding,
	    isAddon,
	    isFlyingBuilding,
	    isNeutral,
	    isHero,
	    isPowerup,
	    isBeacon,
	    isFlagBeacon,
	    isSpecialBuilding,
	    isSpell,

	    Firebat,
	    Ghost,
	    Goliath,
	    Marine,
	    Medic,
	    SCV,
	    Siege_Tank,
	    Vulture,
	    Vulture_Spider_Mine,
	    Battlecruiser,
	    Dropship,
	    Nuclear_Missile,
	    Science_Vessel,
	    Valkyrie,
	    Wraith,
	    Alan_Schezar,
	    Alexei_Stukov,
	    Arcturus_Mengsk,
	    Edmund_Duke,
	    Gerard_DuGalle,
	    Gui_Montag,
	    Hyperion,
	    Jim_Raynor_Marine,
	    Jim_Raynor_Vulture,
	    Magellan,
	    Norad_II,
	    Samir_Duran,
	    Sarah_Kerrigan,
	    Tom_Kazansky,
	    Civilian,
	    Academy,
	    Armory,
	    Barracks,
	    Bunker,
	    Command_Center,
	    Engineering_Bay,
	    Factory,
	    Missile_Turret,
	    Refinery,
	    Science_Facility,
	    Starport,
	    Supply_Depot,
	    Comsat_Station,
	    Control_Tower,
	    Covert_Ops,
	    Machine_Shop,
	    Nuclear_Silo,
	    Physics_Lab,
	    Crashed_Norad_II,
	    Ion_Cannon,
	    Power_Generator,
	    Psi_Disrupter,

	    Archon,
	    Dark_Archon,
	    Dark_Templar,
	    Dragoon,
	    High_Templar,
	    Probe,
	    Reaver,
	    Scarab,
	    Zealot,
	    Arbiter,
	    Carrier,
	    Corsair,
	    Interceptor,
	    Observer,
	    Scout,
	    Shuttle,
	    Aldaris,
	    Artanis,
	    Danimoth,
	    Hero_Dark_Templar,
	    Fenix_Dragoon,
	    Fenix_Zealot,
	    Gantrithor,
	    Mojo,
	    Raszagal,
	    Tassadar,
	    Tassadar_Zeratul_Archon,
	    Warbringer,
	    Zeratul,
	    Arbiter_Tribunal,
	    Assimilator,
	    Citadel_of_Adun,
	    Cybernetics_Core,
	    Fleet_Beacon,
	    Forge,
	    Gateway,
	    Nexus,
	    Observatory,
	    Photon_Cannon,
	    Pylon,
	    Robotics_Facility,
	    Robotics_Support_Bay,
	    Shield_Battery,
	    Stargate,
	    Templar_Archives,
	    Khaydarin_Crystal_Form,
	    Protoss_Temple,
	    Stasis_Cell_Prison,
	    Warp_Gate,
	    XelNaga_Temple,

	    Broodling,
	    Defiler,
	    Drone,
	    Egg,
	    Hydralisk,
	    Infested_Terran,
	    Larva,
	    Lurker,
	    Lurker_Egg,
	    Ultralisk,
	    Zergling,
	    Cocoon,
	    Devourer,
	    Guardian,
	    Mutalisk,
	    Overlord,
	    Queen,
	    Scourge,
	    Devouring_One,
	    Hunter_Killer,
	    Infested_Duran,
	    Infested_Kerrigan,
	    Kukulza_Guardian,
	    Kukulza_Mutalisk,
	    Matriarch,
	    Torrasque,
	    Unclean_One,
	    Yggdrasill,
	    Creep_Colony,
	    Defiler_Mound,
	    Evolution_Chamber,
	    Extractor,
	    Greater_Spire,
	    Hatchery,
	    Hive,
	    Hydralisk_Den,
	    Infested_Command_Center,
	    Lair,
	    Nydus_Canal,
	    Queens_Nest,
	    Spawning_Pool,
	    Spire,
	    Spore_Colony,
	    Sunken_Colony,
	    Ultralisk_Cavern,
	    Cerebrate,
	    Cerebrate_Daggoth,
	    Mature_Chrysalis,
	    Overmind,
	    Overmind_Cocoon,
	    Overmind_With_Shell,

	    Bengalaas,
	    Kakaru,
	    Ragnasaur,
	    Rhynadon,
	    Scantid,
	    Ursadon,

	    Mineral_Field,
	    Vespene_Geyser,

	    Dark_Swarm,
	    Disruption_Web,
	    Scanner_Sweep,

	    Protoss_Beacon,
	    Protoss_Flag_Beacon,
	    Terran_Beacon,
	    Terran_Flag_Beacon,
	    Zerg_Beacon,
	    Zerg_Flag_Beacon,

	    Powerup_Data_Disk,
	    Powerup_Flag,
	    Powerup_Khalis_Crystal,
	    Powerup_Khaydarin_Crystal,
	    Powerup_Psi_Emitter,
	    Powerup_Uraj_Crystal,
	    Powerup_Young_Chrysalis,

	    None,
	    Unknown_Unit
	}

	public enum FilterAttributeScalar {
		HitPoints,
	    InitialHitPoints,
	    Shields,
	    Energy,
	    Resources,
	    InitialResources,
	    KillCount,
	    GroundWeaponCooldown,
	    AirWeaponCooldown,
	    SpellCooldown,
	    DefenseMatrixPoints,
	    DefenseMatrixTimer,
	    EnsnareTimer,
	    IrradiateTimer,
	    LockdownTimer,
	    MaelstromTimer,
	    PlagueTimer,
	    RemoveTimer,
	    StasisTimer,
	    StimTimer,
	    PositionX,
	    PositionY,
	    InitialPositionX,
	    InitialPositionY,
	    TilePositionX,
	    TilePositionY,
	    InitialTilePositionX,
	    InitialTilePositionY,
	    Angle,
	    VelocityX,
	    VelocityY,
	    TargetPositionX,
	    TargetPositionY,
	    OrderTimer,
	    RemainingBuildTime,
	    RemainingTrainTime,
	    TrainingQueueCount,
	    LoadedUnitsCount,
	    InterceptorCount,
	    ScarabCount,
	    SpiderMineCount,
	    RemainingResearchTime,
	    RemainingUpgradeTime,
	    RallyPositionX,
	    RallyPositionY
	}
	
	public enum FilterAttributeUnit {
	    GetTarget,
	    GetOrderTarget,
	    GetBuildUnit,
	    GetTransport,
	    GetRallyUnit,
	    GetAddon
	}
	
	public enum FilterAttributeType {
		GetType,
		GetInitialType,
		GetBuildType,
		GetTech,
		GetUpgrade
	}
	
	public enum FilterAttributePosition {
		GetPosition,
		GetInitialPosition,
		GetTargetPosition,
		GetRallyPosition
	}
	
	public enum FilterAttributeTilePosiiton {
		GetTilePosiiton,
		GetInitialTilePosition
	}
	
	public enum FilterAttributeOrder {
		GetOrder,
		GetSecondaryOrder
	}
	
	public static UnitGroup getUnitGroup(HashSet<Unit> units) {
		return (UnitGroup) units;
	}
}
