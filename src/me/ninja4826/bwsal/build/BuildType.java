package me.ninja4826.bwsal.build;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import bwapi.*;
import me.ninja4826.bwsal.GameHandler;
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
		
		void set(BuildType buildType, TechType type) {
			buildTypeSet.add(buildType);
			buildTypeData[buildType.getID()].set(type);
			techTypeToBuildTypeMap.put(type, buildType);
		}
		
		void set(BuildType buildType, UnitType type) {
			buildTypeSet.add(buildType);
			buildTypeData[buildType.getID()].set(type);
			unitTypeToBuildTypeMap.put(type, buildType);
		}
		
		void set(BuildType buildType, UpgradeType type) {
			set(buildType, type, 1);
		}
		
		void set(BuildType buildType, UpgradeType type, int level) {
			buildTypeSet.add(buildType);
			buildTypeData[buildType.getID()].set(type, level);
			if (level == 1) upgradeTypeToBuildTypeMap.put(type, buildType);
		}
		
		void init() {
			set(Terran_Marine, UnitType.Terran_Marine);
			set(Terran_Ghost, UnitType.Terran_Ghost);
			set(Terran_Vulture, UnitType.Terran_Vulture);
			set(Terran_Goliath, UnitType.Terran_Goliath);
			set(Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Siege_Tank_Tank_Mode);
			set(Terran_SCV, UnitType.Terran_SCV);
			set(Terran_Wraith, UnitType.Terran_Wraith);
			set(Terran_Science_Vessel, UnitType.Terran_Science_Vessel);
			set(Terran_Dropship, UnitType.Terran_Dropship);
			set(Terran_Battlecruiser, UnitType.Terran_Battlecruiser);
			set(Terran_Nuclear_Missile, UnitType.Terran_Nuclear_Missile);
			set(Terran_Siege_Tank_Siege_Mode, UnitType.Terran_Siege_Tank_Siege_Mode);
			set(Terran_Firebat, UnitType.Terran_Firebat);
			set(Terran_Medic, UnitType.Terran_Medic);
			set(Zerg_Larva, UnitType.Zerg_Larva);
			set(Zerg_Zergling, UnitType.Zerg_Zergling);
			set(Zerg_Hydralisk, UnitType.Zerg_Hydralisk);
			set(Zerg_Ultralisk, UnitType.Zerg_Ultralisk);
			set(Zerg_Drone, UnitType.Zerg_Drone);
			set(Zerg_Overlord, UnitType.Zerg_Overlord);
			set(Zerg_Mutalisk, UnitType.Zerg_Mutalisk);
			set(Zerg_Guardian, UnitType.Zerg_Guardian);
			set(Zerg_Queen, UnitType.Zerg_Queen);
			set(Zerg_Defiler, UnitType.Zerg_Defiler);
			set(Zerg_Scourge, UnitType.Zerg_Scourge);
			set(Zerg_Infested_Terran, UnitType.Zerg_Infested_Terran);
			set(Terran_Valkyrie, UnitType.Terran_Valkyrie);
			set(Zerg_Cocoon, UnitType.Zerg_Cocoon);
			set(Protoss_Corsair, UnitType.Protoss_Corsair);
			set(Protoss_Dark_Templar, UnitType.Protoss_Dark_Templar);
			set(Zerg_Devourer, UnitType.Zerg_Devourer);
			set(Protoss_Dark_Archon, UnitType.Protoss_Dark_Archon);
			set(Protoss_Probe, UnitType.Protoss_Probe);
			set(Protoss_Zealot, UnitType.Protoss_Zealot);
			set(Protoss_Dragoon, UnitType.Protoss_Dragoon);
			set(Protoss_High_Templar, UnitType.Protoss_High_Templar);
			set(Protoss_Archon, UnitType.Protoss_Archon);
			set(Protoss_Shuttle, UnitType.Protoss_Shuttle);
			set(Protoss_Scout, UnitType.Protoss_Scout);
			set(Protoss_Arbiter, UnitType.Protoss_Arbiter);
			set(Protoss_Carrier, UnitType.Protoss_Carrier);
			set(Protoss_Interceptor, UnitType.Protoss_Interceptor);
			set(Protoss_Reaver, UnitType.Protoss_Reaver);
			set(Protoss_Observer, UnitType.Protoss_Observer);
			set(Protoss_Scarab, UnitType.Protoss_Scarab);
			set(Zerg_Lurker, UnitType.Zerg_Lurker);
			set(Terran_Command_Center, UnitType.Terran_Command_Center);
			set(Terran_Comsat_Station, UnitType.Terran_Comsat_Station);
			set(Terran_Nuclear_Silo, UnitType.Terran_Nuclear_Silo);
			set(Terran_Supply_Depot, UnitType.Terran_Supply_Depot);
			set(Terran_Refinery, UnitType.Terran_Refinery);
			set(Terran_Barracks, UnitType.Terran_Barracks);
			set(Terran_Academy, UnitType.Terran_Academy);
			set(Terran_Factory, UnitType.Terran_Factory);
			set(Terran_Starport, UnitType.Terran_Starport);
			set(Terran_Control_Tower, UnitType.Terran_Control_Tower);
			set(Terran_Science_Facility, UnitType.Terran_Science_Facility);
			set(Terran_Covert_Ops, UnitType.Terran_Covert_Ops);
			set(Terran_Physics_Lab, UnitType.Terran_Physics_Lab);
			set(Terran_Machine_Shop, UnitType.Terran_Machine_Shop);
			set(Terran_Engineering_Bay, UnitType.Terran_Engineering_Bay);
			set(Terran_Armory, UnitType.Terran_Armory);
			set(Terran_Missile_Turret, UnitType.Terran_Missile_Turret);
			set(Terran_Bunker, UnitType.Terran_Bunker);
			set(Zerg_Hatchery, UnitType.Zerg_Hatchery);
			set(Zerg_Lair, UnitType.Zerg_Lair);
			set(Zerg_Hive, UnitType.Zerg_Hive);
			set(Zerg_Nydus_Canal, UnitType.Zerg_Nydus_Canal);
			set(Zerg_Hydralisk_Den, UnitType.Zerg_Hydralisk_Den);
			set(Zerg_Defiler_Mound, UnitType.Zerg_Defiler_Mound);
			set(Zerg_Greater_Spire, UnitType.Zerg_Greater_Spire);
			set(Zerg_Queens_Nest, UnitType.Zerg_Queens_Nest);
			set(Zerg_Evolution_Chamber, UnitType.Zerg_Evolution_Chamber);
			set(Zerg_Ultralisk_Cavern, UnitType.Zerg_Ultralisk_Cavern);
			set(Zerg_Spire, UnitType.Zerg_Spire);
			set(Zerg_Spawning_Pool, UnitType.Zerg_Spawning_Pool);
			set(Zerg_Creep_Colony, UnitType.Zerg_Creep_Colony);
			set(Zerg_Spore_Colony, UnitType.Zerg_Spore_Colony);
			set(Zerg_Sunken_Colony, UnitType.Zerg_Sunken_Colony);
			set(Zerg_Extractor, UnitType.Zerg_Extractor);
			set(Protoss_Nexus, UnitType.Protoss_Nexus);
			set(Protoss_Robotics_Facility, UnitType.Protoss_Robotics_Facility);
			set(Protoss_Pylon, UnitType.Protoss_Pylon);
			set(Protoss_Assimilator, UnitType.Protoss_Assimilator);
			set(Protoss_Observatory, UnitType.Protoss_Observatory);
			set(Protoss_Gateway, UnitType.Protoss_Gateway);
			set(Protoss_Photon_Cannon, UnitType.Protoss_Photon_Cannon);
			set(Protoss_Citadel_of_Adun, UnitType.Protoss_Citadel_of_Adun);
			set(Protoss_Cybernetics_Core, UnitType.Protoss_Cybernetics_Core);
			set(Protoss_Templar_Archives, UnitType.Protoss_Templar_Archives);
			set(Protoss_Forge, UnitType.Protoss_Forge);
			set(Protoss_Stargate, UnitType.Protoss_Stargate);
			set(Protoss_Fleet_Beacon, UnitType.Protoss_Fleet_Beacon);
			set(Protoss_Arbiter_Tribunal, UnitType.Protoss_Arbiter_Tribunal);
			set(Protoss_Robotics_Support_Bay, UnitType.Protoss_Robotics_Support_Bay);
			set(Protoss_Shield_Battery, UnitType.Protoss_Shield_Battery);
			set(Stim_Packs, TechType.Stim_Packs);
			set(Lockdown, TechType.Lockdown);
			set(EMP_Shockwave, TechType.EMP_Shockwave);
			set(Spider_Mines, TechType.Spider_Mines);
			set(Tank_Siege_Mode, TechType.Tank_Siege_Mode);
			set(Irradiate, TechType.Irradiate);
			set(Yamato_Gun, TechType.Yamato_Gun);
			set(Cloaking_Field, TechType.Cloaking_Field);
			set(Personnel_Cloaking, TechType.Personnel_Cloaking);
			set(Burrowing, TechType.Burrowing);
			set(Spawn_Broodlings, TechType.Spawn_Broodlings);
			set(Plague, TechType.Plague);
			set(Consume, TechType.Consume);
			set(Ensnare, TechType.Ensnare);
			set(Psionic_Storm, TechType.Psionic_Storm);
			set(Hallucination, TechType.Hallucination);
			set(Recall, TechType.Recall);
			set(Stasis_Field, TechType.Stasis_Field);
			set(Restoration, TechType.Restoration);
			set(Disruption_Web, TechType.Disruption_Web);
			set(Mind_Control, TechType.Mind_Control);
			set(Optical_Flare, TechType.Optical_Flare);
			set(Maelstrom, TechType.Maelstrom);
			set(Lurker_Aspect, TechType.Lurker_Aspect);
			set(Terran_Infantry_Armor_1, UpgradeType.Terran_Infantry_Armor, 1);
			set(Terran_Infantry_Armor_2, UpgradeType.Terran_Infantry_Armor, 2);
			set(Terran_Infantry_Armor_3, UpgradeType.Terran_Infantry_Armor, 3);
			set(Terran_Vehicle_Plating_1, UpgradeType.Terran_Vehicle_Plating, 1);
			set(Terran_Vehicle_Plating_2, UpgradeType.Terran_Vehicle_Plating, 2);
			set(Terran_Vehicle_Plating_3, UpgradeType.Terran_Vehicle_Plating, 3);
			set(Terran_Ship_Plating_1, UpgradeType.Terran_Ship_Plating, 1);
			set(Terran_Ship_Plating_2, UpgradeType.Terran_Ship_Plating, 2);
			set(Terran_Ship_Plating_3, UpgradeType.Terran_Ship_Plating, 3);
			set(Zerg_Carapace_1, UpgradeType.Zerg_Carapace, 1);
			set(Zerg_Carapace_2, UpgradeType.Zerg_Carapace, 2);
			set(Zerg_Carapace_3, UpgradeType.Zerg_Carapace, 3);
			set(Zerg_Flyer_Carapace_1, UpgradeType.Zerg_Flyer_Carapace, 1);
			set(Zerg_Flyer_Carapace_2, UpgradeType.Zerg_Flyer_Carapace, 2);
			set(Zerg_Flyer_Carapace_3, UpgradeType.Zerg_Flyer_Carapace, 3);
			set(Protoss_Ground_Armor_1, UpgradeType.Protoss_Ground_Armor, 1);
			set(Protoss_Ground_Armor_2, UpgradeType.Protoss_Ground_Armor, 2);
			set(Protoss_Ground_Armor_3, UpgradeType.Protoss_Ground_Armor, 3);
			set(Protoss_Air_Armor_1, UpgradeType.Protoss_Air_Armor, 1);
			set(Protoss_Air_Armor_2, UpgradeType.Protoss_Air_Armor, 2);
			set(Protoss_Air_Armor_3, UpgradeType.Protoss_Air_Armor, 3);
			set(Terran_Infantry_Weapons_1, UpgradeType.Terran_Infantry_Weapons, 1);
			set(Terran_Infantry_Weapons_2, UpgradeType.Terran_Infantry_Weapons, 2);
			set(Terran_Infantry_Weapons_3, UpgradeType.Terran_Infantry_Weapons, 3);
			set(Terran_Vehicle_Weapons_1, UpgradeType.Terran_Vehicle_Weapons, 1);
			set(Terran_Vehicle_Weapons_2, UpgradeType.Terran_Vehicle_Weapons, 2);
			set(Terran_Vehicle_Weapons_3, UpgradeType.Terran_Vehicle_Weapons, 3);
			set(Terran_Ship_Weapons_1, UpgradeType.Terran_Ship_Weapons, 1);
			set(Terran_Ship_Weapons_2, UpgradeType.Terran_Ship_Weapons, 2);
			set(Terran_Ship_Weapons_3, UpgradeType.Terran_Ship_Weapons, 3);
			set(Zerg_Melee_Attacks_1, UpgradeType.Zerg_Melee_Attacks, 1);
			set(Zerg_Melee_Attacks_2, UpgradeType.Zerg_Melee_Attacks, 2);
			set(Zerg_Melee_Attacks_3, UpgradeType.Zerg_Melee_Attacks, 3);
			set(Zerg_Missile_Attacks_1, UpgradeType.Zerg_Missile_Attacks, 1);
			set(Zerg_Missile_Attacks_2, UpgradeType.Zerg_Missile_Attacks, 2);
			set(Zerg_Missile_Attacks_3, UpgradeType.Zerg_Missile_Attacks, 3);
			set(Zerg_Flyer_Attacks_1, UpgradeType.Zerg_Flyer_Attacks, 1);
			set(Zerg_Flyer_Attacks_2, UpgradeType.Zerg_Flyer_Attacks, 2);
			set(Zerg_Flyer_Attacks_3, UpgradeType.Zerg_Flyer_Attacks, 3);
			set(Protoss_Ground_Weapons_1, UpgradeType.Protoss_Ground_Weapons, 1);
			set(Protoss_Ground_Weapons_2, UpgradeType.Protoss_Ground_Weapons, 2);
			set(Protoss_Ground_Weapons_3, UpgradeType.Protoss_Ground_Weapons, 3);
			set(Protoss_Air_Weapons_1, UpgradeType.Protoss_Air_Weapons, 1);
			set(Protoss_Air_Weapons_2, UpgradeType.Protoss_Air_Weapons, 2);
			set(Protoss_Air_Weapons_3, UpgradeType.Protoss_Air_Weapons, 3);
			set(Protoss_Plasma_Shields_1, UpgradeType.Protoss_Plasma_Shields, 1);
			set(Protoss_Plasma_Shields_2, UpgradeType.Protoss_Plasma_Shields, 2);
			set(Protoss_Plasma_Shields_3, UpgradeType.Protoss_Plasma_Shields, 3);
			set(U_238_Shells, UpgradeType.U_238_Shells);
			set(Ion_Thrusters, UpgradeType.Ion_Thrusters);
			set(Titan_Reactor, UpgradeType.Titan_Reactor);
			set(Ocular_Implants, UpgradeType.Ocular_Implants);
			set(Moebius_Reactor, UpgradeType.Moebius_Reactor);
			set(Apollo_Reactor, UpgradeType.Apollo_Reactor);
			set(Colossus_Reactor, UpgradeType.Colossus_Reactor);
			set(Ventral_Sacs, UpgradeType.Ventral_Sacs);
			set(Antennae, UpgradeType.Antennae);
			set(Pneumatized_Carapace, UpgradeType.Pneumatized_Carapace);
			set(Metabolic_Boost, UpgradeType.Metabolic_Boost);
			set(Adrenal_Glands, UpgradeType.Adrenal_Glands);
			set(Muscular_Augments, UpgradeType.Muscular_Augments);
			set(Grooved_Spines, UpgradeType.Grooved_Spines);
			set(Gamete_Meiosis, UpgradeType.Gamete_Meiosis);
			set(Metasynaptic_Node, UpgradeType.Metasynaptic_Node);
			set(Singularity_Charge, UpgradeType.Singularity_Charge);
			set(Leg_Enhancements, UpgradeType.Leg_Enhancements);
			set(Scarab_Damage, UpgradeType.Scarab_Damage);
			set(Reaver_Capacity, UpgradeType.Reaver_Capacity);
			set(Gravitic_Drive, UpgradeType.Gravitic_Drive);
			set(Sensor_Array, UpgradeType.Sensor_Array);
			set(Gravitic_Boosters, UpgradeType.Gravitic_Boosters);
			set(Khaydarin_Amulet, UpgradeType.Khaydarin_Amulet);
			set(Apial_Sensors, UpgradeType.Apial_Sensors);
			set(Gravitic_Thrusters, UpgradeType.Gravitic_Thrusters);
			set(Carrier_Capacity, UpgradeType.Carrier_Capacity);
			set(Khaydarin_Core, UpgradeType.Khaydarin_Core);
			set(Argus_Jewel, UpgradeType.Argus_Jewel);
			set(Argus_Talisman, UpgradeType.Argus_Talisman);
			set(Caduceus_Reactor, UpgradeType.Caduceus_Reactor);
			set(Chitinous_Plating, UpgradeType.Chitinous_Plating);
			set(Anabolic_Synthesis, UpgradeType.Anabolic_Synthesis);
			set(Charon_Boosters, UpgradeType.Charon_Boosters);
			set(None, UnitType.None);
			
			for (BuildType i : buildTypeSet) {
				if (i != BuildTypes.None) buildTypeSetByRace.get(i.getRace()).add(i);
				buildTypeData[i.getID()].setDependencies();
				String name = i.getName();
				buildTypeMap.put(name, i);
			}
			
			for (BuildType i : buildTypeSet) {
				if (i != BuildTypes.None) {
					for (Entry<BuildType, Integer> j : i.requiredBuildTypes().entrySet()) requiredBuildTypeSet.add(j.getKey());
				}
			}
			HashSet<Race> races = new HashSet<Race>();
			races.add(Race.Terran);
			races.add(Race.Protoss);
			races.add(Race.Zerg);
			
			for (Race r : races) {
				requiredBuildTypeSet.add(new BuildType(r.getWorker()));
				requiredBuildTypeSet.add(new BuildType(r.getRefinery()));
				requiredBuildTypeSet.add(new BuildType(r.getSupplyProvider()));
				requiredBuildTypeSet.add(new BuildType(r.getCenter()));
				requiredBuildTypeSetByRace.get(r).add(new BuildType(r.getWorker()));
				requiredBuildTypeSetByRace.get(r).add(new BuildType(r.getRefinery()));
				requiredBuildTypeSetByRace.get(r).add(new BuildType(r.getSupplyProvider()));
				requiredBuildTypeSetByRace.get(r).add(new BuildType(r.getCenter()));
				buildTypeData[new BuildType(r.getWorker()).getID()].mask = WorkerMask;
				buildTypeData[new BuildType(r.getWorker()).getID()].mask = RefineryMask;
				buildTypeData[new BuildType(r.getSupplyProvider()).getID()].mask = SupplyMask;
				buildTypeData[new BuildType(r.getSupplyProvider()).getID()].mask = CenterMask;
			}
			
			for (BuildType i : requiredBuildTypeSet) {
				if (buildTypeData[i.getID()].mask > 0) continue;
				buildTypeData[i.getID()].mask = 1 << requiredBuildTypeSetByRace.get(i.getRace()).size();
				requiredBuildTypeSetByRace.get(i.getRace()).add(i);
			}
			
			for (BuildType i : buildTypeSet) {
				if (i != BuildTypes.None) {
					for (Entry<BuildType, Integer> j : i.requiredBuildTypes().entrySet()) buildTypeData[i.getID()].requiredMask |= j.getKey().getMask();
				}
			}
			
			initializingBuildType = false;
		}
		
		public HashSet<BuildType> allBuildTypes() { return buildTypeSet; }
		public HashSet<BuildType> allBuildTypes(Race r) { return buildTypeSetByRace.get(r); }
		public HashSet<BuildType> allRequiredBuildTypes() { return requiredBuildTypeSet; }
		public HashSet<BuildType> allRequiredBuildTypes(Race r) { return requiredBuildTypeSetByRace.get(r); }
	}
	
	public enum Compare {
		EQUAL,
		GREATER,
		GREATER_EQUAL,
		LESS,
		LESS_EQUAL,
		NOT
	}
	
	static HashSet<BuildType> buildTypeSet;
	static ConcurrentSkipListMap<Race, HashSet<BuildType>> buildTypeSetByRace;
	static HashSet<BuildType> requiredBuildTypeSet;
	static ConcurrentSkipListMap<Race, HashSet<BuildType>> requiredBuildTypeSetByRace;
	
	static ConcurrentSkipListMap<String, BuildType> buildTypeMap;
	static ConcurrentSkipListMap<TechType, BuildType> techTypeToBuildTypeMap;
	static ConcurrentSkipListMap<UnitType, BuildType> unitTypeToBuildTypeMap;
	static ConcurrentSkipListMap<UpgradeType, BuildType> upgradeTypeToBuildTypeMap;
	static boolean initializingBuildType = true;
	
	final static BuildTypeInternal[] buildTypeData = new BuildTypeInternal[203];
	
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
	
	public boolean compare(BuildType other, Compare comp) {
		switch (comp) {
		case EQUAL:
			return this.id == other.id;
		case GREATER:
			return this.id > other.id;
		case GREATER_EQUAL:
			return this.id >= other.id;
		case LESS:
			return this.id < other.id;
		case LESS_EQUAL:
			return this.id < other.id;
		case NOT:
			return this.id != other.id;
		}
		return false;
	}
	
	public boolean equals(BuildType other) { return this.id == other.id; }
	
	public boolean equals(TechType other) { return this.id == new BuildType(other).id; }
	
	public boolean equals(UnitType other) { return this.id == new BuildType(other).id; }
	
	public boolean equals(UpgradeType other) { return this.id == new BuildType(other).id; }
	
	public int toInt() { return this.id; }
	
	public int getID() { return this.id; }
	
	public String getName() { return buildTypeData[this.id].name; }
	
	public Race getRace() { return buildTypeData[this.id].race; }
	
	public boolean isTechType() { return buildTypeData[this.id].techType != TechType.None; }
	
	public boolean isUnitType() { return buildTypeData[this.id].unitType != UnitType.None; }
	
	public boolean isUpgradeType() { return buildTypeData[this.id].upgradeType != UpgradeType.None; }
	
	public TechType getTechType() { return buildTypeData[this.id].techType; }
	
	public UnitType getUnitType() { return buildTypeData[this.id].unitType; }
	
	public UpgradeType getUpgradeType() { return buildTypeData[this.id].upgradeType; }
	
	public int getUpgradeLevel() { return buildTypeData[this.id].upgradeLevel; }
	
	public long getMask() { return buildTypeData[this.id].mask; }
	
	public long getRequiredMask() { return buildTypeData[this.id].requiredMask; }
	
	public Pair<BuildType, Integer> whatBuilds() { return buildTypeData[this.id].whatBuilds; }
	
	public ConcurrentSkipListMap<BuildType, Integer> requiredBuildTypes() { return buildTypeData[this.id].requiredBuildTypes; }
	
	public boolean requiresPsi() { return buildTypeData[this.id].requiresPsi; }
	
	public boolean requiresLarva() { return buildTypeData[this.id].requiresLarva; }
	
	public BuildType requiredAddon() { return buildTypeData[this.id].requiredAddon; }
	
	public int mineralPrice() { return buildTypeData[this.id].mineralPrice; }
	
	public int gasPrice() { return buildTypeData[this.id].gasPrice; }
	
	public int builderTime() { return buildTypeData[this.id].builderTime; }
	
	public int buildUnitTime() { return buildTypeData[this.id].buildUnitTime; }
	
	public int prepTime() { return buildTypeData[this.id].prepTime; }
	
	public boolean createsUnit() { return buildTypeData[this.id].createsUnit; }
	
	public boolean morphsBuilder() { return buildTypeData[this.id].morphsBuilder; }
	
	public boolean needsBuildLocation() { return buildTypeData[this.id].needsBuildLocation; }
	
	public int supplyRequired() { return buildTypeData[this.id].supplyRequired; }
	
	public int supplyProvided() { return buildTypeData[this.id].supplyProvided; }
	
	public boolean build(Unit builder, Unit secondBuilder, TilePosition buildLocation) {
		if (builder == null) return false;
		BuildTypeInternal buildType = buildTypeData[this.id];
		if (buildType.techType != TechType.None) return builder.research(buildType.techType);
		if (buildType.upgradeType != UpgradeType.None) return builder.upgrade(buildType.upgradeType);
		
		if (buildType.unitType != UnitType.None) {
			if (buildType.unitType == UnitType.Protoss_Archon) return builder.useTech(TechType.Archon_Warp, secondBuilder);
			if (buildType.unitType == UnitType.Protoss_Dark_Archon) return builder.useTech(TechType.Dark_Archon_Meld, secondBuilder);
			if (buildType.unitType.isAddon()) return builder.buildAddon(buildType.unitType);
			if (buildType.unitType.isBuilding() == buildType.unitType.whatBuilds().first.isBuilding()) return builder.morph(buildType.unitType);
			if (buildType.unitType.isBuilding()) return builder.build(buildType.unitType, buildLocation);
			return builder.train(buildType.unitType);
		}
		return false;
	}
	
	public boolean isPreparing(Unit builder, Unit secondBuilder) {
		if (builder == null) return false;
		
		BuildTypeInternal buildType = buildTypeData[this.id];
		
		if (buildType.techType != TechType.None) return builder.isResearching() && builder.getTech() == buildType.techType;
		if (buildType.upgradeType != UpgradeType.None) return builder.isUpgrading() && builder.getUpgrade() == buildType.upgradeType;
		if (buildType.unitType != UnitType.None) {
			return builder.isConstructing() ||
					builder.isBeingConstructed() ||
					builder.isMorphing() ||
					builder.isTraining() ||
					builder.getOrder() == Order.ArchonWarp ||
					builder.getOrder() == Order.DarkArchonMeld;
		}
		return false;
	}
	
	public boolean isBuilding(Unit builder, Unit secondBuilder, Unit createdUnit) {
		if (builder == null) return false;
		BuildTypeInternal buildType = buildTypeData[this.id];
		
		if (buildType.techType != TechType.None) return builder.isResearching() && builder.getTech() == buildType.techType;
		if (buildType.upgradeType != UpgradeType.None) return builder.isUpgrading() && builder.getUpgrade() == buildType.upgradeType;
		if (buildType.unitType != UnitType.None) {
			if (buildType.unitType == UnitType.Protoss_Archon) return builder.getBuildType() == UnitType.Protoss_Archon || secondBuilder.getBuildType() == UnitType.Protoss_Archon;
			if (buildType.unitType == UnitType.Protoss_Dark_Archon) return builder.getBuildType() == UnitType.Protoss_Dark_Archon || secondBuilder.getBuildType() == UnitType.Protoss_Dark_Archon;
			if (buildType.unitType.isAddon()) {
				return createdUnit != null &&
						createdUnit.exists() &&
						createdUnit.isConstructing() &&
						createdUnit.getType() == buildType.unitType;
			}
			if (buildType.morphsBuilder) return builder.isConstructing() || builder.isMorphing();
			if (buildType.unitType.isBuilding()) {
				return createdUnit != null &&
						createdUnit.exists() &&
						createdUnit.isConstructing() &&
						createdUnit.getType() == buildType.unitType;
			}
			
			return builder.isTraining();
		}
		return false;
	}
	
	public boolean isCompleted(Unit builder, Unit secondBuilder, Unit createdUnit, Unit secondCreatedUnit) {
		Mirror mirror = GameHandler.getMirror();
		Player self = mirror.getGame().self();
		BuildTypeInternal buildType = buildTypeData[this.id];
		
		if (buildType.techType != TechType.None) return self.hasResearched(buildType.techType);
		if (buildType.upgradeType != UpgradeType.None) return self.getUpgradeLevel(buildType.upgradeType) >= buildType.upgradeLevel;
		if (buildType.unitType != UnitType.None) {
			if ((buildType.createsUnit || buildType.requiresLarva) &&
					!(createdUnit != null &&
					  createdUnit.exists() &&
					  createdUnit.isCompleted() &&
					  createdUnit.getType() == buildType.unitType)) return false;
			if ((buildType.createsUnit && buildType.requiresLarva) &&
					!(secondCreatedUnit != null &&
					  secondCreatedUnit.exists() &&
					  secondCreatedUnit.isCompleted() &&
					  secondCreatedUnit.getType() == buildType.unitType)) return false;
			if (buildType.morphsBuilder) {
				boolean builderMorphed = (builder != null && builder.exists() && builder.isCompleted() && builder.getType() == buildType.unitType);
				boolean secondBuilderMorphed = (secondBuilder != null && secondBuilder.exists() && secondBuilder.isCompleted() && secondBuilder.getType() == buildType.unitType);
				if (builderMorphed == false && secondBuilderMorphed == false) return false;
			}
			if (buildType.unitType.isAddon() && builder.getAddon() != createdUnit) return false;
			return true;
		}
		return false;
	}
	
	public int remainingTime(Unit builder, Unit secondBuilder, Unit createdUnit) {
		BuildTypeInternal buildType = buildTypeData[this.id];
		
		if (builder == null) return buildType.builderTime;
		if (buildType.techType != TechType.None) return builder.getRemainingResearchTime();
		if (buildType.upgradeType != UpgradeType.None) return builder.getRemainingUpgradeTime();
		if (buildType.unitType != UnitType.None) {
			int t = 0;
			if (buildType.createsUnit) {
				if (createdUnit != null && createdUnit.exists()) {
					return createdUnit.getRemainingBuildTime();
				} else {
					return buildType.buildUnitTime;
				}
			} else {
				t = 0;
				if (builder != null && builder.exists()) t = Math.max(builder.getRemainingBuildTime(), builder.getRemainingTrainTime());
				if (secondBuilder != null && secondBuilder.exists()) t = Math.max(t, Math.max(secondBuilder.getRemainingBuildTime(), secondBuilder.getRemainingTrainTime()));
			}
			return t;
		}
		return 0;
	}
	
	public BuildType getBuildType(String name) {
		if (buildTypeMap.containsKey(name)) return buildTypeMap.get(name);
		return BuildTypes.None;
	}
}




















































