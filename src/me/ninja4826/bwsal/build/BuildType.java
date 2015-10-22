package me.ninja4826.bwsal.build;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import bwapi.Race;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import me.ninja4826.bwsal.util.Pair;

public class BuildType {
	
	public class BuildTypeInternal {
		public TechType techType;
		public UnitType unitType;
		public UpgradeType upgradeType;
		public int upgradeLevel;
		
		public String name;
		public Race race;
		public Pair<BuildType, Integer> whatBuilds;
		public ConcurrentSkipListMap<BuildType, Integer> requiredBuildTypes;
		
		public boolean requiresPsi;
		public boolean requiresLarva;
		public BuildType requiredAddon;
		public int mineralPrice;
		public int gasPrice;
		public int executionTime;
		public int builderTime;
		public int buildUnitTime;
		public int prepTime;
		public boolean createsUnit;
		public boolean morphsBuilder;
		public boolean needsBuildLocation;
		
		public int supplyRequired;
		public int supplyProvided;
		
		public long mask;
		public long requiredMask;
		
		public boolean valid;
		
		public BuildTypeInternal() {
			techType = TechType.None;
			unitType = UnitType.None;
			upgradeType = UpgradeType.None;
			upgradeLevel = 0;
			buildUnitTime = 0;
			prepTime = 0;
			createsUnit = false;
			morphsBuilder = false;
			needsBuildLocation = false;
			supplyRequired = 0;
			supplyProvided = 0;
			requiresPsi = false;
			requiresLarva = false;
			requiredAddon = BuildTypes.None;
			valid = false;
			mask = 0;
			requiredMask = 0;
		}
		
		public void set(TechType type) {
			if (initializingBuildType) {
				this.techType = type;
				this.name = type.toString();
				this.race = type.getRace();
				this.mineralPrice = type.mineralPrice();
				this.gasPrice = type.gasPrice();
				this.builderTime = type.researchTime();
				this.buildUnitTime = type.researchTime();
				this.valid = true;
			}
		}
		
		public void set(UnitType type) {
			if (initializingBuildType) {
				this.unitType = type;
				this.name = type.toString();
				this.race = type.getRace();
				this.mineralPrice = type.mineralPrice();
				this.gasPrice = type.gasPrice();
				this.builderTime = type.buildTime() + 24;
				this.buildUnitTime = type.buildTime() + 24;
				if (this.race == Race.Protoss && type.isBuilding()) {
					this.builderTime = 4*24;
					this.buildUnitTime += 4*24;
				}
				this.requiresPsi = type.requiresPsi();
				this.needsBuildLocation = ( type.isBuilding() && !type.whatBuilds().first.isBuilding() );
				this.prepTime = 0;
				if (this.needsBuildLocation) {
					this.prepTime = 4*24;
				}
				
				if (this.race == Race.Zerg) {
					this.morphsBuilder = true;
					this.createsUnit = type.isTwoUnitsInOneEgg();
					if (type == UnitType.Zerg_Infested_Terran) {
						this.morphsBuilder = false;
						this.createsUnit = true;
					}
				} else {
					this.morphsBuilder = false;
					this.createsUnit = true;
					if (type == UnitType.Protoss_Archon || type == UnitType.Protoss_Dark_Archon) {
						this.morphsBuilder = true;
						this.createsUnit = false;
					}
				}
				
				this.supplyRequired = type.supplyRequired();
				this.supplyProvided = type.supplyProvided();
				this.valid = true;
			}
		}
		
		public void set(UpgradeType type) {
			this.set(type, 1);
		}
		
		public void set(UpgradeType type, int level) {
			if (initializingBuildType) {
				this.upgradeType = type;
				this.upgradeLevel = level;
				if (type.maxRepeats() == 1) {
					this.name = type.toString();
				} else {
					if (level == 1) {
						this.name = type.toString() + " 1";
					} else if (level == 2) {
						this.name = type.toString() + " 2";
					} else {
						this.name = type.toString() + " 3";
					}
				}
				this.race = type.getRace();
				this.mineralPrice = type.mineralPrice(level);
				this.gasPrice = type.gasPrice();
				this.builderTime = this.buildUnitTime = type.upgradeTime(level);
				this.valid = true;
			}
		}
		
		public void setDependencies() {
			if (initializingBuildType) {
				if (this.techType != TechType.None) {
					this.whatBuilds.first = new BuildType(this.techType.whatResearches());
					this.whatBuilds.second = 1;
					if (this.techType == TechType.Lurker_Aspect) {
						this.requiredBuildTypes.put(BuildTypes.Zerg_Lair, 1);
					}
				} else if (this.unitType != UnitType.None) {
					if (this.unitType.whatBuilds().first == UnitType.Zerg_Larva) {
						this.whatBuilds.first = BuildTypes.Zerg_Hatchery;
						this.whatBuilds.second = 1;
					} else {
						this.whatBuilds.first = new BuildType(this.unitType.whatBuilds().first);
						this.whatBuilds.second = this.unitType.whatBuilds().second;
					}
					this.unitType.requiredUnits();
					for (Entry<UnitType, Integer> r : this.unitType.requiredUnits().entrySet()) {
						if (r.getKey() == UnitType.Zerg_Larva) {
							this.requiresLarva = true;
							this.requiredBuildTypes.put(BuildTypes.Zerg_Hatchery, r.getValue());
						} else {
							if (r.getKey().isAddon() && r.getKey().whatBuilds().first == this.unitType.whatBuilds().first) this.requiredAddon = new BuildType(r.getKey());
							this.requiredBuildTypes.put(new BuildType(r.getKey()), r.getValue());
						}
					}
					
					if (this.unitType.requiredTech() != TechType.None) this.requiredBuildTypes.put(new BuildType(this.unitType.requiredTech()), 1);
					if (this.unitType.requiresPsi()) this.requiredBuildTypes.put(BuildTypes.Protoss_Pylon, 1);
				} else if (this.upgradeType != UpgradeType.None) {
					this.whatBuilds.first = new BuildType(this.upgradeType.whatUpgrades());
					this.whatBuilds.second = 1;
					this.requiredBuildTypes.put(this.whatBuilds.first, this.whatBuilds.second);
					this.requiredBuildTypes.put(new BuildType(this.upgradeType.whatsRequired()), 1);
					if (this.upgradeLevel > 1) this.requiredBuildTypes.put(new BuildType(this.upgradeType, this.upgradeLevel - 1), 1);
				}
			}
		}
	}
	
	public static class BuildTypes {
		public long WorkerMask 		= 1 << 0;
		public long RefineryMask 	= 1 << 1;
		public long SupplyMask 		= 1 << 2;
		public long CenterMask 		= 1 << 3;
		
		public static BuildType Terran_Marine = new BuildType(0);
		public static BuildType Terran_Ghost = new BuildType(1);
		public static BuildType Terran_Vulture = new BuildType(2);
		public static BuildType Terran_Goliath = new BuildType(3);
		public static BuildType Terran_Siege_Tank_Tank_Mode = new BuildType(4);
		public static BuildType Terran_SCV = new BuildType(5);
		public static BuildType Terran_Wraith = new BuildType(6);
		public static BuildType Terran_Science_Vessel = new BuildType(7);
		public static BuildType Terran_Dropship = new BuildType(8);
		public static BuildType Terran_Battlecruiser = new BuildType(9);
		public static BuildType Terran_Nuclear_Missile = new BuildType(10);
		public static BuildType Terran_Siege_Tank_Siege_Mode = new BuildType(11);
		public static BuildType Terran_Firebat = new BuildType(12);
		public static BuildType Terran_Medic = new BuildType(13);
		public static BuildType Zerg_Larva = new BuildType(14);
		public static BuildType Zerg_Zergling = new BuildType(15);
		public static BuildType Zerg_Hydralisk = new BuildType(16);
		public static BuildType Zerg_Ultralisk = new BuildType(17);
		public static BuildType Zerg_Drone = new BuildType(18);
		public static BuildType Zerg_Overlord = new BuildType(19);
		public static BuildType Zerg_Mutalisk = new BuildType(20);
		public static BuildType Zerg_Guardian = new BuildType(21);
		public static BuildType Zerg_Queen = new BuildType(22);
		public static BuildType Zerg_Defiler = new BuildType(23);
		public static BuildType Zerg_Scourge = new BuildType(24);
		public static BuildType Zerg_Infested_Terran = new BuildType(25);
		public static BuildType Terran_Valkyrie = new BuildType(26);
		public static BuildType Zerg_Cocoon = new BuildType(27);
		public static BuildType Protoss_Corsair = new BuildType(28);
		public static BuildType Protoss_Dark_Templar = new BuildType(29);
		public static BuildType Zerg_Devourer = new BuildType(30);
		public static BuildType Protoss_Dark_Archon = new BuildType(31);
		public static BuildType Protoss_Probe = new BuildType(32);
		public static BuildType Protoss_Zealot = new BuildType(33);
		public static BuildType Protoss_Dragoon = new BuildType(34);
		public static BuildType Protoss_High_Templar = new BuildType(35);
		public static BuildType Protoss_Archon = new BuildType(36);
		public static BuildType Protoss_Shuttle = new BuildType(37);
		public static BuildType Protoss_Scout = new BuildType(38);
		public static BuildType Protoss_Arbiter = new BuildType(39);
		public static BuildType Protoss_Carrier = new BuildType(40);
		public static BuildType Protoss_Interceptor = new BuildType(41);
		public static BuildType Protoss_Reaver = new BuildType(42);
		public static BuildType Protoss_Observer = new BuildType(43);
		public static BuildType Protoss_Scarab = new BuildType(44);
		public static BuildType Zerg_Lurker = new BuildType(45);
		public static BuildType Terran_Command_Center = new BuildType(46);
		public static BuildType Terran_Comsat_Station = new BuildType(47);
		public static BuildType Terran_Nuclear_Silo = new BuildType(48);
		public static BuildType Terran_Supply_Depot = new BuildType(49);
		public static BuildType Terran_Refinery = new BuildType(50);
		public static BuildType Terran_Barracks = new BuildType(51);
		public static BuildType Terran_Academy = new BuildType(52);
		public static BuildType Terran_Factory = new BuildType(53);
		public static BuildType Terran_Starport = new BuildType(54);
		public static BuildType Terran_Control_Tower = new BuildType(55);
		public static BuildType Terran_Science_Facility = new BuildType(56);
		public static BuildType Terran_Covert_Ops = new BuildType(57);
		public static BuildType Terran_Physics_Lab = new BuildType(58);
		public static BuildType Terran_Machine_Shop = new BuildType(59);
		public static BuildType Terran_Engineering_Bay = new BuildType(60);
		public static BuildType Terran_Armory = new BuildType(61);
		public static BuildType Terran_Missile_Turret = new BuildType(62);
		public static BuildType Terran_Bunker = new BuildType(63);
		public static BuildType Zerg_Hatchery = new BuildType(64);
		public static BuildType Zerg_Lair = new BuildType(65);
		public static BuildType Zerg_Hive = new BuildType(66);
		public static BuildType Zerg_Nydus_Canal = new BuildType(67);
		public static BuildType Zerg_Hydralisk_Den = new BuildType(68);
		public static BuildType Zerg_Defiler_Mound = new BuildType(69);
		public static BuildType Zerg_Greater_Spire = new BuildType(70);
		public static BuildType Zerg_Queens_Nest = new BuildType(71);
		public static BuildType Zerg_Evolution_Chamber = new BuildType(72);
		public static BuildType Zerg_Ultralisk_Cavern = new BuildType(73);
		public static BuildType Zerg_Spire = new BuildType(74);
		public static BuildType Zerg_Spawning_Pool = new BuildType(75);
		public static BuildType Zerg_Creep_Colony = new BuildType(76);
		public static BuildType Zerg_Spore_Colony = new BuildType(77);
		public static BuildType Zerg_Sunken_Colony = new BuildType(78);
		public static BuildType Zerg_Extractor = new BuildType(79);
		public static BuildType Protoss_Nexus = new BuildType(80);
		public static BuildType Protoss_Robotics_Facility = new BuildType(81);
		public static BuildType Protoss_Pylon = new BuildType(82);
		public static BuildType Protoss_Assimilator = new BuildType(83);
		public static BuildType Protoss_Observatory = new BuildType(84);
		public static BuildType Protoss_Gateway = new BuildType(85);
		public static BuildType Protoss_Photon_Cannon = new BuildType(86);
		public static BuildType Protoss_Citadel_of_Adun = new BuildType(87);
		public static BuildType Protoss_Cybernetics_Core = new BuildType(88);
		public static BuildType Protoss_Templar_Archives = new BuildType(89);
		public static BuildType Protoss_Forge = new BuildType(90);
		public static BuildType Protoss_Stargate = new BuildType(91);
		public static BuildType Protoss_Fleet_Beacon = new BuildType(92);
		public static BuildType Protoss_Arbiter_Tribunal = new BuildType(93);
		public static BuildType Protoss_Robotics_Support_Bay = new BuildType(94);
		public static BuildType Protoss_Shield_Battery = new BuildType(95);
		public static BuildType Stim_Packs = new BuildType(96);
		public static BuildType Lockdown = new BuildType(97);
		public static BuildType EMP_Shockwave = new BuildType(98);
		public static BuildType Spider_Mines = new BuildType(99);
		public static BuildType Tank_Siege_Mode = new BuildType(100);
		public static BuildType Irradiate = new BuildType(101);
		public static BuildType Yamato_Gun = new BuildType(102);
		public static BuildType Cloaking_Field = new BuildType(103);
		public static BuildType Personnel_Cloaking = new BuildType(104);
		public static BuildType Burrowing = new BuildType(105);
		public static BuildType Spawn_Broodlings = new BuildType(106);
		public static BuildType Plague = new BuildType(107);
		public static BuildType Consume = new BuildType(108);
		public static BuildType Ensnare = new BuildType(109);
		public static BuildType Psionic_Storm = new BuildType(110);
		public static BuildType Hallucination = new BuildType(111);
		public static BuildType Recall = new BuildType(112);
		public static BuildType Stasis_Field = new BuildType(113);
		public static BuildType Restoration = new BuildType(114);
		public static BuildType Disruption_Web = new BuildType(115);
		public static BuildType Mind_Control = new BuildType(116);
		public static BuildType Optical_Flare = new BuildType(117);
		public static BuildType Maelstrom = new BuildType(118);
		public static BuildType Lurker_Aspect = new BuildType(119);
		public static BuildType Terran_Infantry_Armor_1 = new BuildType(120);
		public static BuildType Terran_Infantry_Armor_2 = new BuildType(121);
		public static BuildType Terran_Infantry_Armor_3 = new BuildType(122);
		public static BuildType Terran_Vehicle_Plating_1 = new BuildType(123);
		public static BuildType Terran_Vehicle_Plating_2 = new BuildType(124);
		public static BuildType Terran_Vehicle_Plating_3 = new BuildType(125);
		public static BuildType Terran_Ship_Plating_1 = new BuildType(126);
		public static BuildType Terran_Ship_Plating_2 = new BuildType(127);
		public static BuildType Terran_Ship_Plating_3 = new BuildType(128);
		public static BuildType Zerg_Carapace_1 = new BuildType(129);
		public static BuildType Zerg_Carapace_2 = new BuildType(130);
		public static BuildType Zerg_Carapace_3 = new BuildType(131);
		public static BuildType Zerg_Flyer_Carapace_1 = new BuildType(132);
		public static BuildType Zerg_Flyer_Carapace_2 = new BuildType(133);
		public static BuildType Zerg_Flyer_Carapace_3 = new BuildType(134);
		public static BuildType Protoss_Ground_Armor_1 = new BuildType(135);
		public static BuildType Protoss_Ground_Armor_2 = new BuildType(136);
		public static BuildType Protoss_Ground_Armor_3 = new BuildType(137);
		public static BuildType Protoss_Air_Armor_1 = new BuildType(138);
		public static BuildType Protoss_Air_Armor_2 = new BuildType(139);
		public static BuildType Protoss_Air_Armor_3 = new BuildType(140);
		public static BuildType Terran_Infantry_Weapons_1 = new BuildType(141);
		public static BuildType Terran_Infantry_Weapons_2 = new BuildType(142);
		public static BuildType Terran_Infantry_Weapons_3 = new BuildType(143);
		public static BuildType Terran_Vehicle_Weapons_1 = new BuildType(144);
		public static BuildType Terran_Vehicle_Weapons_2 = new BuildType(145);
		public static BuildType Terran_Vehicle_Weapons_3 = new BuildType(146);
		public static BuildType Terran_Ship_Weapons_1 = new BuildType(147);
		public static BuildType Terran_Ship_Weapons_2 = new BuildType(148);
		public static BuildType Terran_Ship_Weapons_3 = new BuildType(149);
		public static BuildType Zerg_Melee_Attacks_1 = new BuildType(150);
		public static BuildType Zerg_Melee_Attacks_2 = new BuildType(151);
		public static BuildType Zerg_Melee_Attacks_3 = new BuildType(152);
		public static BuildType Zerg_Missile_Attacks_1 = new BuildType(153);
		public static BuildType Zerg_Missile_Attacks_2 = new BuildType(154);
		public static BuildType Zerg_Missile_Attacks_3 = new BuildType(155);
		public static BuildType Zerg_Flyer_Attacks_1 = new BuildType(156);
		public static BuildType Zerg_Flyer_Attacks_2 = new BuildType(157);
		public static BuildType Zerg_Flyer_Attacks_3 = new BuildType(158);
		public static BuildType Protoss_Ground_Weapons_1 = new BuildType(159);
		public static BuildType Protoss_Ground_Weapons_2 = new BuildType(160);
		public static BuildType Protoss_Ground_Weapons_3 = new BuildType(161);
		public static BuildType Protoss_Air_Weapons_1 = new BuildType(162);
		public static BuildType Protoss_Air_Weapons_2 = new BuildType(163);
		public static BuildType Protoss_Air_Weapons_3 = new BuildType(164);
		public static BuildType Protoss_Plasma_Shields_1 = new BuildType(165);
		public static BuildType Protoss_Plasma_Shields_2 = new BuildType(166);
		public static BuildType Protoss_Plasma_Shields_3 = new BuildType(167);
		public static BuildType U_238_Shells = new BuildType(168);
		public static BuildType Ion_Thrusters = new BuildType(169);
		public static BuildType Titan_Reactor = new BuildType(170);
		public static BuildType Ocular_Implants = new BuildType(171);
		public static BuildType Moebius_Reactor = new BuildType(172);
		public static BuildType Apollo_Reactor = new BuildType(173);
		public static BuildType Colossus_Reactor = new BuildType(174);
		public static BuildType Ventral_Sacs = new BuildType(175);
		public static BuildType Antennae = new BuildType(176);
		public static BuildType Pneumatized_Carapace = new BuildType(177);
		public static BuildType Metabolic_Boost = new BuildType(178);
		public static BuildType Adrenal_Glands = new BuildType(179);
		public static BuildType Muscular_Augments = new BuildType(180);
		public static BuildType Grooved_Spines = new BuildType(181);
		public static BuildType Gamete_Meiosis = new BuildType(182);
		public static BuildType Metasynaptic_Node = new BuildType(183);
		public static BuildType Singularity_Charge = new BuildType(184);
		public static BuildType Leg_Enhancements = new BuildType(185);
		public static BuildType Scarab_Damage = new BuildType(186);
		public static BuildType Reaver_Capacity = new BuildType(187);
		public static BuildType Gravitic_Drive = new BuildType(188);
		public static BuildType Sensor_Array = new BuildType(189);
		public static BuildType Gravitic_Boosters = new BuildType(190);
		public static BuildType Khaydarin_Amulet = new BuildType(191);
		public static BuildType Apial_Sensors = new BuildType(192);
		public static BuildType Gravitic_Thrusters = new BuildType(193);
		public static BuildType Carrier_Capacity = new BuildType(194);
		public static BuildType Khaydarin_Core = new BuildType(195);
		public static BuildType Argus_Jewel = new BuildType(196);
		public static BuildType Argus_Talisman = new BuildType(197);
		public static BuildType Caduceus_Reactor = new BuildType(198);
		public static BuildType Chitinous_Plating = new BuildType(199);
		public static BuildType Anabolic_Synthesis = new BuildType(200);
		public static BuildType Charon_Boosters = new BuildType(201);
		public static BuildType None = new BuildType(202);
	}
	
	ConcurrentSkipListMap<String, BuildType> buildTypeMap;
	Set<BuildType> buildTypeSet;
	ConcurrentSkipListMap<Race, Set<BuildType>> buildTypeSetByRace;
	Set<BuildType> requiredBuildTypeSet;
	ConcurrentSkipListMap<Race, Set<BuildType>> requiredBuildTypeSetByRace;
	ConcurrentSkipListMap<TechType, BuildType> techTypeToBuildTypeMap;
	ConcurrentSkipListMap<UnitType, BuildType> unitTypeToBuildTypeMap;
	ConcurrentSkipListMap<UpgradeType, BuildType> upgradeTypeToBuildTypeMap;
	boolean initializingBuildType = true;
	
	BuildTypeInternal[] buildTypeData = new BuildTypeInternal[203];
	
	private int id;
	
	public BuildType() {
		this.id = BuildTypes.None.id;
	}
	
	public BuildType(int id) {
		this.id = id;
		if (!initializingBuildType && (id < 0 || id >= 203 || !buildTypeData[id].valid)) this.id = BuildTypes.None.id;
	}
	
	public BuildType(BuildType other) {
		this.id = other.id;
	}
	
	public BuildType(TechType other) {
		if (techTypeToBuildTypeMap.containsKey(other)) {
			this.id = techTypeToBuildTypeMap.get(other).id;
		} else {
			this.id = BuildTypes.None.id;
		}
	}
	
	public BuildType(UnitType other) {
		if (unitTypeToBuildTypeMap.containsKey(other)) {
			this.id = unitTypeToBuildTypeMap.get(other).id;
		} else {
			this.id = BuildTypes.None.id;
		}
	}
	
	public BuildType(UpgradeType other) {
		this(other, 1);
	}
	
	public BuildType(UpgradeType other, int level) {
		if (upgradeTypeToBuildTypeMap.containsKey(other)) {
			this.id = (upgradeTypeToBuildTypeMap.get(other).id) + level - 1;
		} else {
			this.id = BuildTypes.None.id;
		}
	}
	
	public BuildType assign(BuildType other) {
		this.id = other.id;
		return this;
	}
	
	public BuildType assign(TechType other) {
		if (techTypeToBuildTypeMap.containsKey(other)) {
			this.id = techTypeToBuildTypeMap.get(other).id;
		} else {
			this.id = BuildTypes.None.id;
		}
		return this;
	}
	
	public BuildType assign(UnitType other) {
		if (unitTypeToBuildTypeMap.containsKey(other)) {
			this.id = unitTypeToBuildTypeMap.get(other).id;
		} else {
			this.id = BuildTypes.None.id;
		}
		return this;
	}
	
	public BuildType assign(UpgradeType other) {
		if (upgradeTypeToBuildTypeMap.containsKey(other)) {
			this.id = upgradeTypeToBuildTypeMap.get(other).id;
		} else {
			this.id = BuildTypes.None.id;
		}
		return this;
	}
	
	public boolean equals(BuildType other) { return this.id == other.id; }
	
	public boolean equals(TechType other) { return this.id == new BuildType(other).id; }
	
	public boolean equals(UnitType other) { return this.id == new BuildType(other).id; }
	
	public boolean equals(UpgradeType other) { return this.id == new BuildType(other).id; }
	
	
}
