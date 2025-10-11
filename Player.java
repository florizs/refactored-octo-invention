public final class Player extends Playable {
	private static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?";
	private static final String ADD_OR_UPDATE_SKILL = "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,class_index) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE skill_level=VALUES(skill_level)";
	private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?";
	private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?";
	private static final String ADD_SKILL_SAVE = "INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)";
	private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime, restore_type FROM character_skills_save WHERE char_obj_id=? AND class_index=? ORDER BY buff_index ASC";
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?";
	private static final String INSERT_CHARACTER = "INSERT INTO characters (account_name,obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,karma,pvpkills,pkkills,clanid,race,classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,nobless,power_grade) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String UPDATE_CHARACTER = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,punish_level=?,punish_timer=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=?,factionId=?,rec_have=?,rec_left=?,kills=?,deaths=? WHERE obj_id=?";
	private static final String RESTORE_CHARACTER = "SELECT * FROM characters WHERE obj_id=?";
	private static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC";
	private static final String ADD_CHAR_SUBCLASS = "INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
	private static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index =?";
	private static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?";
	private static final String RESTORE_CHAR_HENNAS = "SELECT slot,symbol_id FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	private static final String ADD_CHAR_HENNA = "INSERT INTO character_hennas (char_obj_id,symbol_id,slot,class_index) VALUES (?,?,?,?)";
	private static final String DELETE_CHAR_HENNA = "DELETE FROM character_hennas WHERE char_obj_id=? AND slot=? AND class_index=?";
	private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";
	private static final String RESTORE_CHAR_RECOMS = "SELECT char_id,target_id FROM character_recommends WHERE char_id=?";
	private static final String ADD_CHAR_RECOM = "INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)";
	private static final String UPDATE_TARGET_RECOM_HAVE = "UPDATE characters SET rec_have=? WHERE obj_Id=?";
	private static final String UPDATE_CHAR_RECOM_LEFT = "UPDATE characters SET rec_left=? WHERE obj_Id=?";
	private static final String UPDATE_NOBLESS = "UPDATE characters SET nobless=? WHERE obj_Id=?";
	
	private static final Comparator<GeneralSkillNode> COMPARE_SKILLS_BY_MIN_LVL = Comparator.comparing(GeneralSkillNode::getMinLvl);
	private static final Comparator<GeneralSkillNode> COMPARE_SKILLS_BY_LVL = Comparator.comparing(GeneralSkillNode::getValue);
	
	private static final Map<Integer, Integer> KILLED_PLAYERS = new HashMap<>();
	private static final Map<Integer, Long> TIME_TO_NEXT_KILL = new HashMap<>();

	private static final AbnormalEffect ABNORMAL_EFFECT_TELEPORT = AbnormalEffect.FIREROOT_STUN;

	private static final String DELETE_COMPLETED_QUESTS_BY_NAME = "DELETE FROM character_quests WHERE char_obj_id=? AND name IN ";
	private static final Set<String> QUESTS_TO_KEEP_ON_REMOVE = new HashSet<>(Arrays.asList("Q3000_BrokenSeal", "Q4000_TheEndlessHunt",
		"Q1000_ThePathOfGlory", "Q1001_TestOfCourage", "Q1002_TestOfCourage", "Q1003_TrainingOfSkills", "Q1004_TrainingOfSkills", "Q1005_ExamOfStrength", "Q1006_ExamOfStrength", "Q1007_ExamOfEndurance", "Q1008_ExamOfEndurance", "Q1009_TrialOfHonor", "Q1010_TrialOfFortitude", "Q1011_TrialOfTenacity", "Q1012_FinalOfDestiny", "Q1013_FinalOfValor", "Q1014_FinalOfFearlessness", 
		"Q2001_TempleOfOblivion50Green", "Q2002_TempleOfOblivion50Blue", "Q2003_TempleOfOblivion50Red", "Q2004_TempleOfOblivion60Green", "Q2005_TempleOfOblivion60Blue", "Q2006_TempleOfOblivion60Red", "Q2007_TempleOfOblivion70Green", "Q2008_TempleOfOblivion70Blue", "Q2009_TempleOfOblivion70Red", "Q2010_TempleOfOblivion80Green", "Q2011_TempleOfOblivion80Blue", "Q2012_TempleOfOblivion80Red", "Q2013_TempleOfOblivion90Green", "Q2014_TempleOfOblivion90Blue", "Q2015_TempleOfOblivion90Red", 
		"Q2101_TowerOfAbyss50", "Q2102_TowerOfAbyss60", "Q2103_TowerOfAbyss70", "Q2104_TowerOfAbyss80", "Q2105_TowerOfAbyss90", 
		"Q2201_ThroneOfDestructionEternal", "Q2202_ThroneOfDestructionPrime", "Q2203_ThroneOfDestructionAbsolute", "Q2204_ThroneOfDestructionInfinite", "Q2205_ThroneOfDestructionSupreme", "Q2206_ThroneOfDestructionFinal"
	));
	
	private static final String UPDATE_PUNISH = "UPDATE characters SET punish_level=?, punish_timer=? WHERE char_name=?";

	private static final List<String> RANK_LIST = new CopyOnWriteArrayList<>();
	private static final String CHARACTERS_QUERY = "SELECT char_name,base_class FROM characters WHERE rec_left >= 1000 AND level >= 60 ORDER BY rec_left DESC LIMIT 2";
	private static final int REFRESH_INTERVAL_MS = 60000;
	private static long lastRefreshTime = 0;

	private static final int[][] ANTI_DROP_VALUES = {
		{ 1, 8, 7, 6, 5 },
		{ 1, 7, 6, 5, 4 },
		{ 1, 6, 5, 4, 3 },
		{ 1, 5, 4, 3, 2 },
		{ 1, 4, 3, 2, 1 }
	};
	
	private static final int[] VIP_SKILLS = {
		7098, 7099, 485, 486, 487, 488, 489
	};
	
	private static final int[][] START_END_BOSS_IDS_GODS = {
		{ 5000, 5018, 5400 }, 
		{ 5100, 5107, 5500 }, 
		{ 5200, 5205, 5600 }
	};
	
	private static final int[][] START_END_BOSS_IDS_GIANTS = {
		{ 6000, 6018, 5400 }, 
		{ 6100, 6107, 5500 }, 
		{ 6200, 6205, 5600 },
	};
	
	private static final int[][] START_END_BOSS_IDS_TITANS = {
		{ 7000, 7018, 5400 }, 
		{ 7100, 7107, 5500 }, 
		{ 7200, 7205, 5600 }
	};

	/**
	 * Constructor of Player (use Creature constructor).
	 * Call the Creature constructor to create an empty _skills slot and copy basic Calculator set to this Player
	 * Set the name of the Player
	 * This method SET the level of the Player to 1
	 * @param objectId Identifier of the object to initialized
	 * @param template The PlayerTemplate to apply to the Player
	 * @param accountName The name of the account including this Player
	 * @param app The PcAppearance of the Player
	 */
	private Player(int objectId, PlayerTemplate template, String accountName, PcAppearance app) {
		super(objectId, template);
		initCharStatusUpdateValues();
		_accountName = accountName;
		_appearance = app;
		_ai = new PlayerAI(this); // Create an AI
		_radar = new L2Radar(this); // Create a L2Radar object
		getInventory().restore(); // Retrieve from the database all items of this Player and add them to _inventory
		getWarehouse();
		getFreight();
	}
	
	private Player(int objectId) {
		super(objectId, null);
		initCharStatusUpdateValues();
	}
	
	/** !!! STATIC METHODS !!! */
	
	/**
	 * Create a new Player and add it in the characters table of the database.
	 * Create a new Player with an account name
	 * Set the name, the Hair Style, the Hair Color and the Face type of the Player
	 * Add the player in the characters table of the database
	 * @param objectId Identifier of the object to initialized
	 * @param template The PlayerTemplate to apply to the Player
	 * @param accountName The name of the Player
	 * @param name The name of the Player
	 * @param hairStyle The hair style Identifier of the Player
	 * @param hairColor The hair color Identifier of the Player
	 * @param face The face type Identifier of the Player
	 * @param sex The sex type Identifier of the Player
	 * @return The Player added to the database or null
	 */
	public static Player create(int objectId, PlayerTemplate template, String accountName, String name, byte hairStyle, byte hairColor, byte face, Sex sex) {
		PcAppearance playerAppearance = new PcAppearance(face, hairColor, hairStyle, sex);
		Player player = new Player(objectId, template, accountName, playerAppearance);
		player.setName(name);
		player.setAccessLevel(Config.DEFAULT_ACCESS_LEVEL);
		
		final int playerAccessLevel = player.getAccessLevel().getLevel();
		PlayerInfoTable.getInstance().addPlayer(objectId, accountName, name, playerAccessLevel); // Cache few informations into CharNameTable.
		
		final ClassId playerClassId = player.getClassId();
		player.setBaseClass(playerClassId); // Set the base class ID to that of the actual class ID.
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); // Add the player in the characters table of the database
			PreparedStatement ps = con.prepareStatement(INSERT_CHARACTER)) {
			ps.setString(1, accountName);
			ps.setInt(2, player.getObjectId());
			ps.setString(3, player.getName());
			ps.setInt(4, player.getLevel());
			ps.setInt(5, player.getMaxHp());
			ps.setDouble(6, player.getCurrentHp());
			ps.setInt(7, player.getMaxCp());
			ps.setDouble(8, player.getCurrentCp());
			ps.setInt(9, player.getMaxMp());
			ps.setDouble(10, player.getCurrentMp());
			ps.setInt(11, playerAppearance.getFace());
			ps.setInt(12, playerAppearance.getHairStyle());
			ps.setInt(13, playerAppearance.getHairColor());
			ps.setInt(14, playerAppearance.getSex().ordinal());
			ps.setLong(15, player.getExp());
			ps.setInt(16, player.getSp());
			ps.setInt(17, player.getKarma());
			ps.setInt(18, player.getPvpKills());
			ps.setInt(19, player.getPkKills());
			ps.setInt(20, player.getClanId());
			ps.setInt(21, player.getRace().ordinal());
			ps.setInt(22, playerClassId.getId());
			ps.setLong(23, player.getDeleteTimer());
			ps.setInt(24, CharInfo.getStatus(player.hasDwarvenCraft()));
			ps.setString(25, player.getTitle());
			ps.setInt(26, playerAccessLevel);
			ps.setInt(27, player.isOnlineInt());
			ps.setInt(28, CharInfo.getStatus(player.isIn7sDungeon()));
			ps.setInt(29, player.getClanPrivileges());
			ps.setInt(30, CharInfo.getStatus(player.wantsPeace()));
			ps.setInt(31, player.getBaseClass());
			ps.setInt(32, CharInfo.getStatus(player.isNoble()));
			ps.setLong(33, 0);
			ps.executeUpdate();
		} catch (Exception e) {
			LOGGER.error("Couldn't create player {} for {} account.", e, name, accountName);
			return null;
		}
		
		return player;
	}
	
	/** TODO: refactor?
	 * Retrieve a Player from the characters table of the database.
	 * Retrieve the Player from the characters table of the database
	 * Set the x,y,z position of the Player and make it invisible
	 * Update the overloaded status of the Player
	 * @param objectId Identifier of the object to initialized
	 * @return The Player loaded from the database
	 */
	public static Player restore(int objectId) {
		Player player = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection()) {
			PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER);
			statement.setInt(1, objectId);
			ResultSet rset = statement.executeQuery();
			
			while (rset.next()) {
				final int activeClassId = rset.getInt("classid");
				final PlayerTemplate template = PlayerData.getInstance().getTemplate(activeClassId);
				final PcAppearance app = new PcAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), Sex.values()[rset.getInt("sex")]);

				player = new Player(objectId, template, rset.getString("account_name"), app);
				player.setName(rset.getString("char_name"));
				player._lastAccess = rset.getLong("lastAccess");
				player.setAccessLevel(rset.getInt("accesslevel"));
				player.setFactionId(rset.getInt("factionId"));
				player.getStat().setExp(rset.getLong("exp"));
				player.getStat().setLevel(rset.getByte("level"));
				player.getStat().setSp(rset.getInt("sp"));
				player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
				player.setWantsPeace(rset.getInt("wantspeace") == 1);
				player.setHeading(rset.getInt("heading"));
				player.setKarma(rset.getInt("karma"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setKills(rset.getInt("kills"));
				player.setDeaths(rset.getInt("deaths"));
				player.setOnlineTime(rset.getLong("onlinetime"));
				player.setNoble(rset.getInt("nobless") == 1, false);
				player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
				
				if (player.getClanJoinExpiryTime() < System.currentTimeMillis())
					player.setClanJoinExpiryTime(0);

				player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));

				if (player.getClanCreateExpiryTime() < System.currentTimeMillis())
					player.setClanCreateExpiryTime(0);

				player.setPowerGrade(rset.getInt("power_grade"));
				player.setPledgeType(rset.getInt("subpledge"));

				int clanId = rset.getInt("clanid");

				if (clanId > 0)
					player.setClan(ClanTable.getInstance().getClan(clanId));

				if (player.hasClan()) {
					if (player.getClan().getLeaderId() != player.getObjectId()) {
						if (player.getPowerGrade() == 0)
							player.setPowerGrade(5);

						player.setClanPrivileges(player.getClan().getPriviledgesByRank(player.getPowerGrade()));
					} else {
						player.setClanPrivileges(Clan.CP_ALL);
						player.setPowerGrade(1);
					}
				} else {
					player.setClanPrivileges(Clan.CP_NOTHING);
				}
				
				player.setDeleteTimer(rset.getLong("deletetime"));
				player.setTitle(rset.getString("title"));
				player.setUptime(System.currentTimeMillis());
				player.setRecomHave(rset.getInt("rec_have"));
				player.setRecomLeft(rset.getInt("rec_left"));
				player._classIndex = 0;
				
				try {
					player.setBaseClass(rset.getInt("base_class"));
				} catch (Exception e) {
					player.setBaseClass(activeClassId);
				}
				
				int baseClass = player.getBaseClass();
				
				// Restore Subclass Data (cannot be done earlier in function)
				if (restoreSubClassData(player) && activeClassId != baseClass)
					for (SubClass subClass : player.getSubClasses().values())
						if (subClass.getClassId() == activeClassId)
							player._classIndex = subClass.getClassIndex();

				// Subclass in use but doesn't exist in DB - a possible subclass cheat has been attempted. Switching to base class.
				if (player.getClassIndex() == 0 && activeClassId != baseClass)
					player.setClassId(baseClass);
				else
					player._activeClass = activeClassId;
				
				player.setApprentice(rset.getInt("apprentice"));
				player.setSponsor(rset.getInt("sponsor"));
				player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
				player.setIsIn7sDungeon(rset.getInt("isin7sdungeon") == 1);
				player.setPunishLevel(rset.getInt("punish_level"));
				player.setPunishTimer((player.getPunishLevel() == PunishLevel.NONE) ? 0 : rset.getLong("punish_timer"));
				CursedWeaponManager.getInstance().checkPlayer(player);
				player.setAllianceWithVarkaKetra(rset.getInt("varka_ketra_ally"));
				player.setDeathPenaltyBuffLevel(rset.getInt("death_penalty_level"));
				player.getPosition().set(rset.getInt("x"), rset.getInt("y"), rset.getInt("z")); // Set the x,y,z position of the Player and make it invisible

				if (Hero.getInstance().isActiveHero(objectId))
					player.setHero(true); // Set Hero status if it applies

				player.setPledgeClass(ClanMember.calculatePledgeClass(player)); // Set pledge class rank.

				// Retrieve from the database all secondary data of this Player and reward expertise/lucky skills if necessary. Note that Clan, Noblesse and Hero skills are given separately and not here.
				player.restoreCharData();
				player.giveSkills();

				if (Config.STORE_SKILL_COOLTIME)
					player.restoreEffects(); // buff and status icons

				final double currentHp = rset.getDouble("curHp"); // Restore current CP, HP and MP values
				player.setCurrentCp(rset.getDouble("curCp"));
				player.setCurrentHp(currentHp);
				player.setCurrentMp(rset.getDouble("curMp"));
				
				if (currentHp < 0.5) {
					player.setIsDead(true);
					player.stopHpMpRegeneration();
				}

				final Pet pet = World.getInstance().getPet(player.getObjectId()); // Restore pet if it exists in the world.

				if (pet != null) {
					player.setPet(pet);
					pet.setActingPlayer(player);
				}

				player.refreshOverloaded();
				player.refreshExpertisePenalty();
				player.restoreFriendList();

				PreparedStatement stmt = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id<>?"); // Retrieve the name and ID of the other characters assigned to this account.
				stmt.setString(1, player._accountName);
				stmt.setInt(2, objectId);
				ResultSet chars = stmt.executeQuery();
				
				while (chars.next())
					player.getAccountChars().put(chars.getInt("obj_Id"), chars.getString("char_name"));

				chars.close();
				stmt.close();
				break;
			}
			
			rset.close();
			statement.close();
		} catch (Exception e) {
			LOGGER.error("Couldn't restore player data.", e);
		}
		
		return player;
	}
	
	/**
	 * Restores sub-class data for the Player, used to check the current class index for the character.
	 * @param player The player to make checks on.
	 * @return true if successful.
	 */
	private static boolean restoreSubClassData(Player player) {
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(RESTORE_CHAR_SUBCLASSES)) {
			ps.setInt(1, player.getObjectId());

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					final SubClass subClass = new SubClass(rs.getInt("class_id"), rs.getInt("class_index"), rs.getLong("exp"), rs.getInt("sp"), rs.getByte("level"));
					player.getSubClasses().put(subClass.getClassIndex(), subClass); // Enforce the correct indexing of _subClasses against their class indexes.
				}
			}
		} catch (Exception e) {
			LOGGER.error("Couldn't restore subclasses for {}.", e, player.getName());
			return false;
		}
		
		return true;
	}

	public static boolean isValidFaction(int factionId) {
		return factionId >= Constant.GOD && factionId <= Constant.TITAN;
	}
	
	public static void handleProgression(Creature killer, AchievementType type, QuestIdType idType) {
		if (killer == null)
			return;

		Player player = killer.getActingPlayer();

		if (player == null)
			return;

		player.progressAchievement(type);
		player.progressQuests(idType);
	}
	
	private static void taskLoader(Player player) {
		player.getQuestManagerDefault().loadQuests();
		player.getQuestManagerGlobal().loadQuests();
		player.getQuestManagerBattle().loadQuests();
		player.getCounterManager().loadCounters();
		player.getSpecialStatusManager().load();
	}
	
	public static void setIsParalyzedDelayedMsg(Player player, boolean start, int skId, int delay, String msg) {
		player.broadcastPacket(new MagicSkillUse(player, player, skId, 1, delay, 0));
		setIsParalyzedStart(player, delay);
		ThreadPool.schedule(() -> player.setTransform(start, msg), delay);
	}
	
	public static void setIsParalyzedStart(Player player, int delay) {
		player.sendPacket(new SetupGauge(GaugeColor.CYAN, delay));
		player.setIsParalyzed(true);
	}
	/** !!! STATIC METHODS !!! */
	
	/** !!! ALL DATA !!! */
	private int _lastCompassZone; // the last compass zone update send to the client
	private final Map<Integer, L2Skill> _skills = new ConcurrentSkipListMap<>();

	private final Request _request = new Request(this);
	
	public Request getRequest() {
		return _request;
	}
	
	private long _requestExpireTime;
	
	/** @return True if a request is in progress. */
	public boolean isProcessingRequest() {
		return getActiveRequester() != null || _requestExpireTime > System.currentTimeMillis();
	}
	
	/** @return True if a transaction <B>(trade OR request)</B> is in progress. */
	public boolean isProcessingTransaction() {
		return getActiveRequester() != null || _activeTradeList != null || _requestExpireTime > System.currentTimeMillis();
	}
	
	/** Set the _requestExpireTime of that Player, and set his partner as the active requester. @param partner The partner to make checks on. */
	public void onTransactionRequest(Player partner) {
		_requestExpireTime = System.currentTimeMillis() + Request.REQUEST_TIMEOUT;
		partner.setActiveRequester(this);
	}

	public boolean isRequestExpired() {
		return _requestExpireTime <= System.currentTimeMillis();
	}
	
	/** Select the Warehouse to be used in next activity. */
	public void onTransactionResponse() {
		_requestExpireTime = 0;
	}
	
	private Player _activeRequester;
	
	/** Set the Player requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...). */
	public void setActiveRequester(Player requester) {
		_activeRequester = requester;
	}
	
	/** @return the Player requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...). */
	public Player getActiveRequester() {
		if (_activeRequester != null && _activeRequester.isRequestExpired() && _activeTradeList == null)
			_activeRequester = null;
		
		return _activeRequester;
	}
	
	private LootRule _lootRule;
	
	public LootRule getLootRule() {
		return _lootRule;
	}
	
	public void setLootRule(LootRule lootRule) {
		_lootRule = lootRule;
	}
	
	/**
	 * @param objectId : The looter object to make checks on.
	 * @return true if the active player is the looter or in the same party or command channel than looter objectId.
	 */
	public boolean isLooterOrInLooterParty(int objectId) {
		if (objectId == getObjectId())
			return true;

		final Player looter = World.getInstance().getPlayer(objectId);

		if (looter == null || !isInParty())
			return false;

		return (_party.getCommandChannel() != null) ? _party.getCommandChannel().containsPlayer(looter) : _party.containsPlayer(looter);
	}
	
	private Party _party;
	
	public void setParty(Party party) {
		_party = party;
	}
	
	private int _partyroom;
	
	public int getPartyRoom() {
		return _partyroom;
	}
	
	public boolean isInPartyMatchRoom() {
		return _partyroom > 0;
	}
	
	public void setPartyRoom(int id) {
		_partyroom = id;
	}
	
	/** Remove the player from both waiting list and any potential room. */
	public void removeMeFromPartyMatch() {
		PartyMatchWaitingList.getInstance().removePlayer(this);
		
		if (_partyroom != 0) {
			PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_partyroom);
			
			if (room != null)
				room.deleteMember(this);
		}
	}
	
	private Location _currentSkillWorldPosition;
	
	public Location getCurrentSkillWorldPosition() {
		return _currentSkillWorldPosition;
	}
	
	public void setCurrentSkillWorldPosition(Location worldPosition) {
		_currentSkillWorldPosition = worldPosition;
	}
	
	private AccessLevel _accessLevel;
	
	public AccessLevel getAccessLevel() {
		return _accessLevel;
	}

	/**
	 * Set the {@link AccessLevel} of this {@link Player}.
	 * If invalid, set the default user access level 0.
	 * If superior to 0, it means it's a special access.
	 * @param level : The level to set.
	 */
	public void setAccessLevel(int level) {
		AccessLevel accessLevel = AdminData.getInstance().getAccessLevel(level); // Retrieve the AccessLevel. Even if not existing, it returns user level.

		if (accessLevel == null) {
			LOGGER.warn("An invalid access level {} has been granted for {}, therefore it has been reset.", level, toString());
			accessLevel = AdminData.getInstance().getAccessLevel(0);
		}

		_accessLevel = accessLevel;

		if (level > 0) {
			String accessName = accessLevel.getName();

			if (!accessName.equals("")) // For level lower or equal to user, we don't apply AccessLevel name as title.
				setTitle(accessName);

			if (level == AdminData.getInstance().getMasterAccessLevel()) // We log master access.
				LOGGER.info("{} has logged in with Master access level.", getName());
		}

		if (accessLevel.isGm() && !AdminData.getInstance().isRegisteredAsGM(this))
			AdminData.getInstance().addGm(this, false); // A little hack to avoid Enterworld config to be replaced.
		else
			AdminData.getInstance().deleteGm(this);

		PlayerInfoTable.getInstance().updatePlayerData(this, true);
	}
	
	public void setAccountAccesslevel(int level) {
		LoginServerThread.getInstance().sendAccessLevel(getAccountName(), level);
	}
	
	private ScheduledFuture<?> _protectTask;
	
	public boolean isSpawnProtected() {
		return _protectTask != null;
	}
	
	/** Launch a task corresponding to Config time. @param protect boolean Drop timer or activate it. */
	public void setSpawnProtection(boolean protect) {
		if (protect) {
			if (_protectTask == null) {
				_protectTask = ThreadPool.schedule(() -> {
					sendMessage("The spawn protection has ended.");
					setSpawnProtection(false);
					stopAbnormalEffect(ABNORMAL_EFFECT_TELEPORT);
				}, MathUtil.convertToMs(Config.PLAYER_SPAWN_PROTECTION));
			}
		} else {
			if (_protectTask != null) {
				_protectTask.cancel(true);
				_protectTask = null;
			}
		}
		
		broadcastUserInfo();
	}
	
	private long _recentFakeDeathEndTime;
	
	/** Set protection from agro mobs when getting up from fake death, according settings. */
	public void setRecentFakeDeath() {
		_recentFakeDeathEndTime = System.currentTimeMillis() + MathUtil.convertToMs(Config.PLAYER_FAKEDEATH_UP_PROTECTION);
	}
	
	public void clearRecentFakeDeath() {
		_recentFakeDeathEndTime = 0;
	}
	
	public boolean isRecentFakeDeath() {
		return _recentFakeDeathEndTime > System.currentTimeMillis();
	}
	
	private boolean _isFakeDeath;
	
	public final boolean isFakeDeath() {
		return _isFakeDeath;
	}
	
	public final void setIsFakeDeath(boolean value) {
		_isFakeDeath = value;
	}
	
	private int _expertiseArmorPenalty;
	
	public int getExpertiseArmorPenalty() {
		return _expertiseArmorPenalty;
	}
	
	private boolean _expertiseWeaponPenalty;
	
	public boolean getExpertiseWeaponPenalty() {
		return _expertiseWeaponPenalty;
	}
	
	/** Refresh expertise level ; weapon got one rank, when armor got 4 ranks.<br> */
	public void refreshExpertisePenalty() {
		final int expertiseLevel = getSkillLevel(L2Skill.SKILL_EXPERTISE);
		int armorPenalty = 0;
		boolean weaponPenalty = false;
		
		for (ItemInstance item : getInventory().getPaperdollItems()) {
			if (item.getItemType() != EtcItemType.ARROW && item.getItem().getCrystalType().getId() > expertiseLevel) {
				if (item.isInstWeapon())
					weaponPenalty = true;
				else
					armorPenalty += (item.getItem().getBodyPart() == Item.SLOT_FULL_ARMOR) ? 2 : 1;
			}
		}
		
		armorPenalty = Math.min(armorPenalty, 4);
		
		if (_expertiseWeaponPenalty != weaponPenalty || _expertiseArmorPenalty != armorPenalty) { // Found a different state than previous ; update it.
			_expertiseWeaponPenalty = weaponPenalty;
			_expertiseArmorPenalty = armorPenalty;

			if (_expertiseWeaponPenalty || _expertiseArmorPenalty > 0) // Passive skill "Grade Penalty" is either granted or dropped.
				addTemporarySkill(Constant.GRADE_PENALTY_SKILL, 1);
			else
				removeSkill(Constant.GRADE_PENALTY_SKILL, false);

			sendSkillList();
			sendPacket(new EtcStatusUpdate(this));
			final ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);

			if (weapon != null) {
				if (_expertiseWeaponPenalty)
					ItemPassiveSkillsListener.getInstance().onUnequip(0, weapon, this);
				else
					ItemPassiveSkillsListener.getInstance().onEquip(0, weapon, this);
			}
		}
	}
	
	private ItemInstance _activeEnchantItem;
	
	public void setActiveEnchantItem(ItemInstance scroll) {
		_activeEnchantItem = scroll;
	}
	
	public ItemInstance getActiveEnchantItem() {
		return _activeEnchantItem;
	}
	
	private Map<Integer, Cubic> _cubics = new ConcurrentSkipListMap<>();
	
	public Map<Integer, Cubic> getCubics() {
		return _cubics;
	}
	
	/** Add a L2CubicInstance to the Player _cubics. */
	public void addCubic(int id, int level, double matk, int activationtime, int activationchance, int totalLifetime, boolean givenByOther) {
		_cubics.put(id, new Cubic(this, id, level, (int) matk, activationtime, activationchance, totalLifetime, givenByOther));
	}
	
	/** Remove a L2CubicInstance from the Player _cubics. */
	public void delCubic(int id) {
		_cubics.remove(id);
	}
	
	/** @return the L2CubicInstance corresponding to the Identifier of the Player _cubics. */
	public Cubic getCubic(int id) {
		return _cubics.get(id);
	}
	
	public void unSummonCubics() {
		if (!_cubics.isEmpty()) { // Unsummon Cubics
			for (Cubic cubic : _cubics.values()) {
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			
			_cubics.clear();
		}
	}
	
	private Set<Integer> _activeSoulShots = ConcurrentHashMap.newKeySet(1);
	
	public void addAutoSoulShot(int itemId) {
		_activeSoulShots.add(itemId);
	}
	
	public boolean removeAutoSoulShot(int itemId) {
		return _activeSoulShots.remove(itemId);
	}
	
	public Set<Integer> getAutoSoulShot() {
		return _activeSoulShots;
	}
	
	/**
	 * Cancel autoshot use for shot itemId
	 * @param itemId int id to disable
	 * @return true if canceled.
	 */
	public boolean disableAutoShot(int itemId) {
		if (_activeSoulShots.contains(itemId)) {
			removeAutoSoulShot(itemId);
			sendPacket(new ExAutoSoulShot(itemId, 0));
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId));
			return true;
		}
		
		return false;
	}
	
	/** Cancel all autoshots for player */
	public void disableAutoShotsAll() {
		for (int itemId : _activeSoulShots) {
			sendPacket(new ExAutoSoulShot(itemId, 0));
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId));
		}
		
		_activeSoulShots.clear();
	}
	
	private final int _loto[] = new int[5];
	
	public int getLoto(int i) {
		return _loto[i];
	}
	
	public void setLoto(int i, int val) {
		_loto[i] = val;
	}
	
	private final int _race[] = new int[2];
	
	public int getRace(int i) {
		return _race[i];
	}
	
	public void setRace(int i, int val) {
		_race[i] = val;
	}
	
	private final BlockList _blockList = new BlockList(this);
	
	public BlockList getBlockList() {
		return _blockList;
	}
	
	private FishingStance _fishingStance = new FishingStance(this);
	
	public boolean isFishing() {
		return _fishingStance.isUnderFishCombat() || _fishingStance.isLookingForFish();
	}
	
	public FishingStance getFishingStance() {
		return _fishingStance;
	}
	
	private final List<String> _validBypass = new ArrayList<>();
	
	public synchronized void addBypass(String bypass) {
		if (bypass == null)
			return;
		
		_validBypass.add(bypass);
	}
	
	private final List<String> _validBypass2 = new ArrayList<>();
	
	public synchronized void addBypass2(String bypass) {
		if (bypass == null)
			return;
		
		_validBypass2.add(bypass);
	}
	
	public synchronized boolean validateBypass(String cmd) {
		for (String bp : _validBypass) {
			if (bp == null)
				continue;
			
			if (bp.equals(cmd))
				return true;
		}
		
		for (String bp : _validBypass2) {
			if (bp == null)
				continue;
			
			if (cmd.startsWith(bp))
				return true;
		}
		
		return false;
	}
	
	public synchronized void clearBypass() {
		_validBypass.clear();
		_validBypass2.clear();
	}
	
	private Forum _forumMemo;
	
	public Forum getMemo() {
		if (_forumMemo == null) {
			final Forum forum = ForumsBBSManager.getInstance().getForumByName("MemoRoot");

			if (forum != null) {
				_forumMemo = forum.getChildByName(_accountName);
				
				if (_forumMemo == null)
					_forumMemo = ForumsBBSManager.getInstance().createNewForum(_accountName, forum, Forum.MEMO, Forum.OWNERONLY, getObjectId());
			}
		}

		return _forumMemo;
	}
	
	private int _mailPosition;
	
	public int getMailPosition() {
		return _mailPosition;
	}

	public void setMailPosition(int mailPosition) {
		_mailPosition = mailPosition;
	}
	
	private final SkillUseHolder _currentSkill = new SkillUseHolder();
	
	/** @return the current player skill in use. */
	public SkillUseHolder getCurrentSkill() {
		return _currentSkill;
	}
	
	/**
	 * Update the _currentSkill holder.
	 * @param skill : The skill to update for (or null)
	 * @param ctrlPressed : The boolean information regarding ctrl key.
	 * @param shiftPressed : The boolean information regarding shift key.
	 */
	public void setCurrentSkill(L2Skill skill, boolean ctrlPressed, boolean shiftPressed) {
		_currentSkill.setSkill(skill);
		_currentSkill.setCtrlPressed(ctrlPressed);
		_currentSkill.setShiftPressed(shiftPressed);
	}
	
	private final SkillUseHolder _currentPetSkill = new SkillUseHolder();
	
	/** @return the current pet skill in use. */
	public SkillUseHolder getCurrentPetSkill() {
		return _currentPetSkill;
	}
	
	/**
	 * Update the _currentPetSkill holder.
	 * @param skill : The skill to update for (or null)
	 * @param ctrlPressed : The boolean information regarding ctrl key.
	 * @param shiftPressed : The boolean information regarding shift key.
	 */
	public void setCurrentPetSkill(L2Skill skill, boolean ctrlPressed, boolean shiftPressed) {
		_currentPetSkill.setSkill(skill);
		_currentPetSkill.setCtrlPressed(ctrlPressed);
		_currentPetSkill.setShiftPressed(shiftPressed);
	}
	
	private final SkillUseHolder _queuedSkill = new SkillUseHolder();
	
	/** @return the current queued skill in use. */
	public SkillUseHolder getQueuedSkill() {
		return _queuedSkill;
	}
	
	/**
	 * Update the _queuedSkill holder.
	 * @param skill : The skill to update for (or null)
	 * @param ctrlPressed : The boolean information regarding ctrl key.
	 * @param shiftPressed : The boolean information regarding shift key.
	 */
	public void setQueuedSkill(L2Skill skill, boolean ctrlPressed, boolean shiftPressed) {
		_queuedSkill.setSkill(skill);
		_queuedSkill.setCtrlPressed(ctrlPressed);
		_queuedSkill.setShiftPressed(shiftPressed);
	}
	
	private final Map<Integer, Timestamp> _reuseTimeStamps = new ConcurrentHashMap<>(); //
	
	public Collection<Timestamp> getReuseTimeStamps() {
		return _reuseTimeStamps.values();
	}
	
	public Map<Integer, Timestamp> getReuseTimeStamp() {
		return _reuseTimeStamps;
	}

	/** Index according to skill this TimeStamp instance for restoration purposes only. */
	public void addTimeStamp(L2Skill skill, long reuse, long systime) {
		_reuseTimeStamps.put(skill.getReuseHashCode(), new Timestamp(skill, reuse, systime));
	}
	
	private int _cursedWeaponEquippedId;
	
	public void setCursedWeaponEquippedId(int value) {
		_cursedWeaponEquippedId = value;
	}
	
	public int getCursedWeaponEquippedId() {
		return _cursedWeaponEquippedId;
	}
	
	private volatile long _fallingTimestamp;
	
	/**
	 * @param z
	 * @return True if Character Falling Now On the Start of Fall return False for Correct Coord Sync!
	 */
	public final boolean isFalling(int z) {
		if (isDead() || isFlying() || isInsideZone(ZoneId.WATER))
			return false;
		
		if (System.currentTimeMillis() < _fallingTimestamp)
			return true;
		
		final int deltaZ = getZ() - z;
		
		if (deltaZ <= getBaseTemplate().getFallHeight())
			return false;
		
		final int damage = (int) Formulas.calcFallDam(this, deltaZ);
		
		if (damage > 0) {
			reduceCurrentHp(Math.min(damage, getCurrentHp() - 1), null, false, true, null);
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FALL_DAMAGE_S1).addNumber(damage));
		}
		
		setFalling();
		return false;
	}
	
	public final void setFalling() {
		_fallingTimestamp = System.currentTimeMillis() + Constant.NUMBER_10000;
	}
	
	private int _reviveRequested;
	
	public void reviveRequest(Player reviver, L2Skill skill, boolean pet) {
		if (_reviveRequested == 1) {
			if (_revivePet == pet) // Resurrection has already been proposed.
				reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
			else // A pet cannot be resurrected while it's owner is in the process of resurrecting. Or // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
				reviver.sendPacket(pet ? SystemMessageId.CANNOT_RES_PET2 : SystemMessageId.MASTER_CANNOT_RES);

			return;
		}
		
		if ((pet && getPet() != null && getPet().isDead()) || (!pet && isDead())) {
			_reviveRequested = 1;
			_revivePower = isPhoenixBlessed() ? 100 : isAffected(L2EffectFlag.CHARM_OF_COURAGE) ? 0 : Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), reviver);
			_revivePet = pet;

			ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_S1.getId());
			confirm.addTime(60000);
			confirm.addRequesterId(reviver.getObjectId());
			confirm.addCharName(reviver);
			sendPacket(confirm);

			if (isAffected(L2EffectFlag.CHARM_OF_COURAGE)) {
				sendPacket(new ConfirmDlg(SystemMessageId.DO_YOU_WANT_TO_BE_RESTORED).addTime(60000));
				return;
			}
		}
	}
	
	public void reviveAnswer(int answer) {
		Summon pet = getPet();
		
		if (_reviveRequested != 1 || (!isDead() && !_revivePet) || (_revivePet && pet != null && !pet.isDead()))
			return;

		if (answer == 0 && isPhoenixBlessed()) {
			stopPhoenixBlessing(null);
		} else if (answer == 1) {
			if (!_revivePet) {
				if (_revivePower != 0)
					doRevive(_revivePower);
				else
					doRevive();
			} else if (pet != null) {
				if (_revivePower != 0)
					pet.doRevive(_revivePower);
				else
					pet.doRevive();
			}
		}
		
		_reviveRequested = 0;
		_revivePower = 0;
	}
	
	public boolean isReviveRequested() {
		return _reviveRequested == 1;
	}
	
	private double _revivePower = .0;
	
	public void removeReviving() {
		_reviveRequested = 0;
		_revivePower = 0;
	}
	
	private boolean _revivePet;
	
	public boolean isRevivingPet() {
		return _revivePet;
	}
	
	private double _cpUpdateIncCheck = .0;
	private double _cpUpdateDecCheck = .0;
	private double _cpUpdateInterval = .0;
	
	/** @return true if cp update should be done, false if not */
	private boolean needCpUpdate(int barPixels) {
		double currentCp = getCurrentCp();

		if (currentCp <= 1.0 || getMaxCp() < barPixels)
			return true;

		if (currentCp <= _cpUpdateDecCheck || currentCp >= _cpUpdateIncCheck) {
			if (currentCp == getMaxCp()) {
				_cpUpdateIncCheck = currentCp + 1;
				_cpUpdateDecCheck = currentCp - _cpUpdateInterval;
			} else {
				double doubleMulti = currentCp / _cpUpdateInterval;
				int intMulti = (int) doubleMulti;
				_cpUpdateDecCheck = _cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_cpUpdateIncCheck = _cpUpdateDecCheck + _cpUpdateInterval;
			}

			return true;
		}

		return false;
	}
	
	private double _mpUpdateIncCheck = .0;
	private double _mpUpdateDecCheck = .0;
	private double _mpUpdateInterval = .0;
	
	/** @return true if mp update should be done, false if not */
	private boolean needMpUpdate(int barPixels) {
		double currentMp = getCurrentMp();

		if (currentMp <= 1.0 || getMaxMp() < barPixels)
			return true;

		if (currentMp <= _mpUpdateDecCheck || currentMp >= _mpUpdateIncCheck) {
			if (currentMp == getMaxMp()) {
				_mpUpdateIncCheck = currentMp + 1;
				_mpUpdateDecCheck = currentMp - _mpUpdateInterval;
			} else {
				double doubleMulti = currentMp / _mpUpdateInterval;
				int intMulti = (int) doubleMulti;
				_mpUpdateDecCheck = _mpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_mpUpdateIncCheck = _mpUpdateDecCheck + _mpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	private int _clientX;
	
	public final int getClientX() {
		return _clientX;
	}

	public final void setClientX(int val) {
		_clientX = val;
	}

	private int _clientY;

	public final int getClientY() {
		return _clientY;
	}

	public final void setClientY(int val) {
		_clientY = val;
	}

	private int _clientZ;

	public final int getClientZ() {
		return _clientZ;
	}

	public final void setClientZ(int val) {
		_clientZ = val;
	}
	
	private int _clientHeading;
	
	public final int getClientHeading() {
		return _clientHeading;
	}
	
	public final void setClientHeading(int val) {
		_clientHeading = val;
	}
	
	private ScheduledFuture<?> _shortBuffTask;
	
	public void shortBuffStatusUpdate(int magicId, int level, int time) {
		if (_shortBuffTask != null) {
			_shortBuffTask.cancel(false);
			_shortBuffTask = null;
		}
		
		_shortBuffTask = ThreadPool.schedule(() -> {
			sendPacket(new ShortBuffStatusUpdate(0, 0, 0));
			setShortBuffTaskSkillId(0);
		}, MathUtil.convertToMs(time));
		
		setShortBuffTaskSkillId(magicId);
		sendPacket(new ShortBuffStatusUpdate(magicId, level, time));
	}
	
	private int _shortBuffTaskSkillId;
	
	public int getShortBuffTaskSkillId() {
		return _shortBuffTaskSkillId;
	}
	
	public void setShortBuffTaskSkillId(int id) {
		_shortBuffTaskSkillId = id;
	}
	
	private int _coupleId;
	
	public int getCoupleId() {
		return _coupleId;
	}
	
	public void setCoupleId(int coupleId) {
		_coupleId = coupleId;
	}
	
	private boolean _isUnderMarryRequest;
	
	public boolean isUnderMarryRequest() {
		return _isUnderMarryRequest;
	}
	
	public void setUnderMarryRequest(boolean state) {
		_isUnderMarryRequest = state;
	}
	
	private int _requesterId;
	
	public void setRequesterId(int requesterId) {
		_requesterId = requesterId;
	}
	
	public void engageAnswer(int answer) {
		if (!_isUnderMarryRequest || _requesterId == 0)
			return;

		final Player requester = World.getInstance().getPlayer(_requesterId);

		if (requester != null) {
			if (answer == 1) {
				CoupleManager.getInstance().addCouple(requester, this); // Create the couple
				WeddingManagerNpc.justMarried(requester, this); // Then register wedding
			} else {
				setUnderMarryRequest(false);
				sendMessage("You declined your partner's marriage request.");
				requester.setUnderMarryRequest(false);
				requester.sendMessage("Your partner declined your marriage request.");
			}
		}
	}
	
	private final List<Integer> _friendList = new ArrayList<>(); // Related to CB.
	
	public List<Integer> getFriendList() {
		return _friendList;
	}
	
	private void restoreFriendList() {
		_friendList.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id = ? AND relation = 0")) {
			ps.setInt(1, getObjectId());
			
			try (ResultSet rset = ps.executeQuery()) {
				while (rset.next()) {
					final int friendId = rset.getInt("friend_id");

					if (friendId == getObjectId())
						continue;

					_friendList.add(friendId);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Couldn't restore {}'s friendlist.", e, getName());
		}
	}
	
	private void notifyFriends(boolean login) {
		for (int id : _friendList) {
			Player friend = World.getInstance().getPlayer(id);
			
			if (friend != null) {
				friend.sendPacket(new FriendList(friend));
				
				if (login)
					friend.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN).addCharName(this));
			}
		}
	}
	
	private final List<Integer> _selectedFriendList = new ArrayList<>(); // Related to CB.

	public void selectFriend(Integer friendId) {
		if (!_selectedFriendList.contains(friendId))
			_selectedFriendList.add(friendId);
	}
	
	public void deselectFriend(Integer friendId) {
		if (_selectedFriendList.contains(friendId))
			_selectedFriendList.remove(friendId);
	}
	
	public List<Integer> getSelectedFriendList() {
		return _selectedFriendList;
	}

	private final List<Integer> _selectedBlocksList = new ArrayList<>(); // Related to CB.
	
	public void selectBlock(Integer friendId) {
		if (!_selectedBlocksList.contains(friendId))
			_selectedBlocksList.add(friendId);
	}
	
	public void deselectBlock(Integer friendId) {
		if (_selectedBlocksList.contains(friendId))
			_selectedBlocksList.remove(friendId);
	}
	
	public List<Integer> getSelectedBlocksList() {
		return _selectedBlocksList;
	}
	
	private L2Skill _summonSkillRequest;
	private Player _summonTargetRequest;
	
	public boolean teleportRequest(Player requester, L2Skill skill) {
		if (_summonTargetRequest != null && requester != null) {
			requester.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_SUMMONED).addCharName(this));
			return false;
		}
		
		_summonTargetRequest = requester;
		_summonSkillRequest = skill;
		return true;
	}
	
	public void teleportAnswer(int answer, int requesterId) {
		if (_summonTargetRequest == null)
			return;
		
		if (answer == 1 && _summonTargetRequest.getObjectId() == requesterId)
			teleportToFriend(_summonTargetRequest, _summonSkillRequest);

		_summonTargetRequest = null;
		_summonSkillRequest = null;
	}
	
	private Door _requestedGate;

	public void activateGate(int answer, int type) {
		if (_requestedGate == null)
			return;

		if (answer == 1 && getTarget() == _requestedGate) {
			if (type == 1)
				_requestedGate.openMe();
			else if (type == 0)
				_requestedGate.closeMe();
		}
		
		_requestedGate = null;
	}
	
	public void setRequestedGate(Door door) {
		_requestedGate = door;
	}

	private Clan _clan;
	
	public int getClanCrestId() {
		return hasClan() ? _clan.getCrestId() : 0;
	}

	public int getClanCrestLargeId() {
		return hasClan() ? _clan.getCrestLargeId() : 0;
	}
	
	/**
	 * @param castleId The castle to check.
	 * @return True if this Player is a clan leader in ownership of the passed castle.
	 */
	public boolean isCastleLord(int castleId) {
		if (!hasClan() || getClan().getLeader().getPlayer() != this)
			return false;
		
		Castle castle = CastleManager.getInstance().getCastleByOwner(getClan());
		return castle != null && castle.getCastleId() == castleId;
	}
	
	/** @return the Alliance Identifier of the Player. */
	public int getAllyId() {
		return (_clan == null) ? 0 : _clan.getAllyId();
	}
	
	public void setClan(Clan clan) {
		_clan = clan;
		setTitle("");

		if (clan == null) {
			_clanId = 0;
			_clanPrivileges = 0;
			_pledgeType = 0;
			_powerGrade = 0;
			_lvlJoinedAcademy = 0;
			_apprentice = 0;
			_sponsor = 0;
			return;
		}

		if (!clan.isMember(getObjectId())) { // char has been kicked from clan
			setClan(null);
			return;
		}

		_clanId = clan.getClanId();
	}

	public Clan getClan() {
		return _clan;
	}
	
	public boolean hasClan() {
		return _clan != null;
	}

	public boolean isClanLeader() {
		return hasClan() && getObjectId() == _clan.getLeaderId();
	}
	
	private int _clanId;
	
	public int getClanId() {
		return showMaskedName ? 0 : _clanId;
	}
	
	public int getAllyCrestId() {
		return (getClanId() == 0 || getClan().getAllyId() == 0) ? 0 : getClan().getAllyCrestId();
	}
	
	private int _apprentice;
	
	public int getApprentice() {
		return _apprentice;
	}
	
	public void setApprentice(int id) {
		_apprentice = id;
	}
	
	private int _sponsor;
	
	public int getSponsor() {
		return _sponsor;
	}
	
	public void setSponsor(int id) {
		_sponsor = id;
	}
	
	private long _clanJoinExpiryTime;
	
	public long getClanJoinExpiryTime() {
		return _clanJoinExpiryTime;
	}
	
	public void setClanJoinExpiryTime(long time) {
		_clanJoinExpiryTime = time;
	}
	
	private long _clanCreateExpiryTime;
	
	public long getClanCreateExpiryTime() {
		return _clanCreateExpiryTime;
	}
	
	public void setClanCreateExpiryTime(long time) {
		_clanCreateExpiryTime = time;
	}
	
	private int _powerGrade;
	
	public int getPowerGrade() {
		return _powerGrade;
	}
	
	public void setPowerGrade(int power) {
		_powerGrade = power;
	}
	
	private int _clanPrivileges;
	
	public int getClanPrivileges() {
		return _clanPrivileges;
	}
	
	public void setClanPrivileges(int n) {
		_clanPrivileges = n;
	}
	
	private int _pledgeClass;
	
	public int getPledgeClass() {
		return _pledgeClass;
	}
	
	public void setPledgeClass(int classId) {
		_pledgeClass = classId;
	}
	
	private int _pledgeType;
	
	public int getPledgeType() {
		return _pledgeType;
	}
	
	public boolean pledgeTypeNotAcademy() {
		return _pledgeType != Clan.SUBUNIT_ACADEMY;
	}
	
	public void setPledgeType(int typeId) {
		_pledgeType = typeId;
	}
	
	private int _lvlJoinedAcademy;
	
	public void setLvlJoinedAcademy(int lvl) {
		_lvlJoinedAcademy = lvl;
	}
	
	public int getLvlJoinedAcademy() {
		return _lvlJoinedAcademy;
	}
	
	public boolean isAcademyMember() {
		return _lvlJoinedAcademy > 0;
	}
	
	private int _team;
	
	public void setTeam(int team) {
		_team = team;
	}
	
	public int getTeam() {
		return _team;
	}
	
	private int _alliedVarkaKetra; // lvl of alliance with ketra orcs or varka silenos, used in quests and aggro checks [-5,-1] varka, 0 neutral, [1,5] ketra
	
	public void setAllianceWithVarkaKetra(int sideAndLvlOfAlliance) {
		_alliedVarkaKetra = sideAndLvlOfAlliance;
	}
	
	/** [-5,-1] varka, 0 neutral, [1,5] ketra @return the side faction. */
	public int getAllianceWithVarkaKetra() {
		return _alliedVarkaKetra;
	}
	
	public boolean isAlliedWithVarka() {
		return _alliedVarkaKetra < 0;
	}
	
	public boolean isAlliedWithKetra() {
		return _alliedVarkaKetra > 0;
	}
	
	private int _deathPenaltyBuffLevel;
	
	public int getDeathPenaltyBuffLevel() {
		return _deathPenaltyBuffLevel;
	}
	
	public void setDeathPenaltyBuffLevel(int level) {
		_deathPenaltyBuffLevel = level;
	}
	
	private boolean hasDeathPenaltyBuff() {
		return _deathPenaltyBuffLevel > 0;
	}
	
	private void updateDeathPenalty(Creature killer, boolean killerPlayerExist, boolean isInRestrictedZone) {
		if (_deathPenaltyBuffLevel >= 15 || killerPlayerExist || isInRestrictedZone || killer.isRaidRelated())
			return;

		if (hasKarma() || Rnd.isLessThanRandom(Config.DEATH_PENALTY_CHANCE)) {
			_deathPenaltyBuffLevel++;
			addTemporarySkill(FrequentSkill.DEATH_PENALTY.getId(), _deathPenaltyBuffLevel);
			sendPacket(new EtcStatusUpdate(this));
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(_deathPenaltyBuffLevel));
		}
	}

	public void reduceDeathPenaltyBuffLevel() {
		if (!hasDeathPenaltyBuff())
			return;

		removeSkill(FrequentSkill.DEATH_PENALTY.getId(), false);
		_deathPenaltyBuffLevel--;

		if (hasDeathPenaltyBuff()) {
			addTemporarySkill(FrequentSkill.DEATH_PENALTY.getId(), _deathPenaltyBuffLevel);
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(_deathPenaltyBuffLevel));
		} else {
			sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
			stopAbnormalEffect(AbnormalEffect.BIG_HEAD);
		}

		sendPacket(new EtcStatusUpdate(this));
	}
	
	public void restoreDeathPenaltyBuffLevel() {
		if (hasDeathPenaltyBuff())
			addTemporarySkill(FrequentSkill.DEATH_PENALTY.getId(), _deathPenaltyBuffLevel);
	}
	
	private boolean _isNoble;
	
	public boolean isNoble() {
		return _isNoble;
	}
	
	/**
	 * Set Noblesse Status, and reward with nobles' skills.
	 * @param add Add skills if setted to true, else remove skills.
	 * @param store Store the status directly in the db if setted to true.
	 */
	public void setNoble(boolean add, boolean store) {
		addOrRemoveSkills(SkillTable.getNobleSkills(), add);
		_isNoble = add;
		sendSkillList();
		
		if (store) {
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(UPDATE_NOBLESS)) {
				ps.setBoolean(1, add);
				ps.setInt(2, getObjectId());
				ps.executeUpdate();
			} catch (Exception e) {
				LOGGER.error("Couldn't update nobless status for {}.", e, getName());
			}
		}
	}
	
	private boolean _isHero;
	
	public boolean isHero() {
		return _isHero;
	}
	
	public void setHero(boolean hero) {
		_isHero = hero;
		addOrRemoveSkills(SkillTable.getHeroSkills(), hero && _baseClass == _activeClass);
		sendSkillList();
	}
	
	private boolean _wantsPeace;
	
	public void setWantsPeace(boolean wantsPeace) {
		_wantsPeace = wantsPeace;
	}
	
	public boolean wantsPeace() {
		return _wantsPeace;
	}
	
	private final AtomicInteger _charges = new AtomicInteger();
	
	public int getCharges() {
		return _charges.get();
	}
	
	public void increaseCharges(int count, int max) {
		if (_charges.get() >= max) {
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
			return;
		}

		restartChargeTask();

		if (_charges.addAndGet(count) >= max) {
			_charges.set(max);
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
		} else {
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1).addNumber(_charges.get()));
		}

		sendPacket(new EtcStatusUpdate(this));
	}
	
	public boolean decreaseCharges(int count) {
		if (_charges.get() < count)
			return false;

		if (_charges.addAndGet(-count) == 0)
			stopChargeTask();
		else
			restartChargeTask();

		sendPacket(new EtcStatusUpdate(this));
		return true;
	}
	
	public void clearCharges() {
		_charges.set(0);
		sendPacket(new EtcStatusUpdate(this));
	}
	
	private ScheduledFuture<?> _chargeTask;
	
	/** Starts/Restarts the ChargeTask to Clear Charges after 10 Mins. */
	private void restartChargeTask() {
		if (_chargeTask != null) {
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
		
		_chargeTask = ThreadPool.schedule(() -> clearCharges(), 600000);
	}

	public void stopChargeTask() {
		if (_chargeTask != null) {
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
	}

	private final List<Integer> _recomChars = new ArrayList<>();
	
	public List<Integer> getRecomChars() {
		return _recomChars;
	}
	
	public boolean canRecom(Player target) {
		return !_recomChars.contains(target.getObjectId());
	}
	
	public void giveRecom(Player target) {
		target.addRecomHave(1);
		decRecomLeft();
		_recomChars.add(target.getObjectId());
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection()) {
			PreparedStatement ps = con.prepareStatement(ADD_CHAR_RECOM);
			ps.setInt(1, getObjectId());
			ps.setInt(2, target.getObjectId());
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement(UPDATE_TARGET_RECOM_HAVE);
			ps.setInt(1, target.getRecomHave());
			ps.setInt(2, target.getObjectId());
			ps.execute();
			ps.close();
			
			ps = con.prepareStatement(UPDATE_CHAR_RECOM_LEFT);
			ps.setInt(1, getRecomLeft());
			ps.setInt(2, getObjectId());
			ps.execute();
			ps.close();
		} catch (Exception e) {
			LOGGER.error("Couldn't update player recommendations.", e);
		}
	}
	
	/** Retrieve from the database all Recommendation data of this Player, add to _recomChars and calculate stats of the Player. */
	private void restoreRecom() {
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(RESTORE_CHAR_RECOMS)) {
			ps.setInt(1, getObjectId());
			
			try (ResultSet rset = ps.executeQuery()) {
				while (rset.next()) {
					_recomChars.add(rset.getInt("target_id"));
				}
			}
		} catch (Exception e) {
			LOGGER.error("Couldn't restore recommendations.", e);
		}
	}

	private boolean _inventoryDisable;
	
	/** Disable the Inventory and create a new task to enable it after 1.5s. */
	public void tempInventoryDisable() {
		_inventoryDisable = true;
		ThreadPool.schedule(() -> _inventoryDisable = false, 1500);
	}

	public boolean isInventoryDisabled() {
		return _inventoryDisable;
	}
	
	private final PcInventory _inventory = new PcInventory(this);
	
	/** @return The current weight of the Player. */
	public int getCurrentLoad() {
		return _inventory.getTotalWeight();
	}
	
	public int getAdena() {
		return _inventory.getAdena();
	}

	public int getAncientAdena() {
		return _inventory.getAncientAdena();
	}
	
	/**
	 * Add adena to Inventory of the Player and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param count int Quantity of adena to be added
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 */
	public void addAdena(String process, int count, WorldObject reference, boolean sendMessage) {
		if (sendMessage)
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA).addNumber(count));

		if (count > 0) {
			_inventory.addAdena(process, count, this, reference);
			inventoryUpdate(_inventory.getAdenaInstance());
		}
	}
	
	/**
	 * Reduce adena in Inventory of the Player and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param count int Quantity of adena to be reduced
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean reduceAdena(String process, int count, WorldObject reference, boolean sendMessage) {
		if (count > getAdena()) {
			if (sendMessage)
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);

			return false;
		}
		
		if (count > 0) {
			ItemInstance adenaItem = _inventory.getAdenaInstance();

			if (!_inventory.reduceAdena(process, count, this, reference))
				return false;

			inventoryUpdate(adenaItem); // Send update packet

			if (sendMessage)
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA).addNumber(count));
		}

		return true;
	}
	
	/**
	 * Add ancient adena to Inventory of the Player and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param count int Quantity of ancient adena to be added
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 */
	public void addAncientAdena(String process, int count, WorldObject reference, boolean sendMessage) {
		if (sendMessage)
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(PcInventory.ANCIENT_ADENA_ID).addNumber(count));

		if (count > 0) {
			_inventory.addAncientAdena(process, count, this, reference);
			inventoryUpdate(_inventory.getAncientAdenaInstance());
		}
	}
	
	/**
	 * Reduce ancient adena in Inventory of the Player and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param count int Quantity of ancient adena to be reduced
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean reduceAncientAdena(String process, int count, WorldObject reference, boolean sendMessage) {
		if (count > getAncientAdena()) {
			if (sendMessage)
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);

			return false;
		}
		
		if (count > 0) {
			ItemInstance ancientAdenaItem = _inventory.getAncientAdenaInstance();

			if (!_inventory.reduceAncientAdena(process, count, this, reference))
				return false;

			inventoryUpdate(ancientAdenaItem);
			
			if (sendMessage) {
				if (count > 1)
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(PcInventory.ANCIENT_ADENA_ID).addItemNumber(count));
				else
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(PcInventory.ANCIENT_ADENA_ID));
			}
		}
		
		return true;
	}
	
	/**
	 * Adds item to inventory and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param item ItemInstance to be added
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 */
	public void addItem(String process, ItemInstance item, WorldObject reference, boolean sendMessage) {
		if (item.getCount() > 0) {
			if (sendMessage) { // Sends message to client if requested
				if (item.getCount() > 1)
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1).addItemName(item).addNumber(item.getCount()));
				else if (item.getEnchantLevel() > 0)
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2).addNumber(item.getEnchantLevel()).addItemName(item));
				else
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(item));
			}

			ItemInstance newitem = _inventory.addItem(process, item, this, reference); // Add the item to inventory
			inventoryUpdate(newitem); // Send inventory update packet
			statusLoadUpdate(); // Update current load as well
			checkArrowOrCursedUpdRuneShadow(newitem, item.getItem().getItemType());
		}
	}
	
	/**
	 * Adds item to Inventory and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param itemId int Item Identifier of the item to be added
	 * @param count int Quantity of items to be added
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return The created ItemInstance.
	 */
	public ItemInstance addItem(String process, int itemId, int count, WorldObject reference, boolean sendMessage) {
		if (count > 0) {
			final Item item = ItemTable.getInstance().getTemplate(itemId); // Retrieve the template of the item.
			
			if (item == null)
				return null;
			
			ItemType itemType = item.getItemType();
			boolean isHerb = itemType == EtcItemType.HERB;
			
			if (sendMessage && ((!isCastingNow() && isHerb) || !isHerb)) { // Sends message to client if requested.
				boolean isSweepOrQuest = process.equalsIgnoreCase("Sweep") || process.equalsIgnoreCase("Quest");

				if (count > 1)
					sendPacket(SystemMessage.getSystemMessage(isSweepOrQuest ? SystemMessageId.EARNED_S2_S1_S : SystemMessageId.YOU_PICKED_UP_S2_S1).addItemName(itemId).addItemNumber(count));
				else
					sendPacket(SystemMessage.getSystemMessage(isSweepOrQuest ? SystemMessageId.EARNED_ITEM_S1 : SystemMessageId.YOU_PICKED_UP_S1).addItemName(itemId));
			}
			
			if (isHerb) { // If the item is herb type, dont add it to inventory.
				final ItemInstance herb = new ItemInstance(0, itemId);
				final IItemHandler handler = ItemHandler.getInstance().getHandler(herb.getEtcItem());
				
				if (handler != null)
					handler.useItem(this, herb, false);
			} else {
				final ItemInstance createdItem = _inventory.addItem(process, itemId, count, this, reference); // Add the item to inventory
				checkArrowOrCursedUpdRuneShadow(createdItem, itemType);
				return createdItem;
			}
		}
		
		return null;
	}
	
	public void checkArrowOrCursedUpdRuneShadow(ItemInstance newitem, ItemType type) {
		_inventory.updRuneAndShadow(newitem);

		if (CursedWeaponManager.getInstance().isCursed(newitem.getItemId())) // Cursed Weapon
			CursedWeaponManager.getInstance().activate(this, newitem);
	}
	
	public void addItems(int... itemIds) {
		for (int item : itemIds)
			addItem(item, 1);
	}
	
	/**
	 * Destroy item from inventory and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param item ItemInstance to be destroyed
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItem(String process, ItemInstance item, WorldObject reference, boolean sendMessage) {
		return destroyItem(process, item, item.getCount(), reference, sendMessage);
	}
	
	/**
	 * Destroy item from inventory and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param item ItemInstance to be destroyed
	 * @param count int Quantity of ancient adena to be reduced
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItem(String process, ItemInstance item, int count, WorldObject reference, boolean sendMessage) {
		item = _inventory.destroyItem(process, item, count, this, reference);
		
		if (item == null) {
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

			return false;
		}

		final InventoryUpdate iu = new InventoryUpdate(); // Send inventory update packet

		if (item.getCount() == 0)
			iu.addRemovedItem(item);
		else
			iu.addModifiedItem(item);

		sendPacket(iu);
		statusLoadUpdate(); // Update current load as well
		_inventory.updateRuneBonus(item);

		if (sendMessage) { // Sends message to client if requested
			if (count > 1)
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item).addItemNumber(count));
			else
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item));
		}
		
		return true;
	}

	/**
	 * Destroys shots from inventory without logging and only occasional saving to database. Sends InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param objectId int Item Instance identifier of the item to be destroyed
	 * @param count int Quantity of items to be destroyed
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItemWithoutTrace(String process, int objectId, int count, WorldObject reference, boolean sendMessage) {
		ItemInstance item = _inventory.getItemByObjectId(objectId);
		
		if (item == null || item.getCount() < count) {
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

			return false;
		}
		
		return destroyItem(null, item, count, reference, sendMessage);
	}
	
	/**
	 * Transfers item to another ItemContainer and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param objectId int Item Identifier of the item to be transfered
	 * @param count int Quantity of items to be transfered
	 * @param target Inventory the Inventory target.
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public ItemInstance transferItem(String process, int objectId, int count, Inventory target, WorldObject reference) {
		final ItemInstance oldItem = checkItemManipulation(objectId, count);
		
		if (oldItem == null)
			return null;
		
		final ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
		
		if (newItem == null)
			return null;
		
		InventoryUpdate playerIU = new InventoryUpdate(); // Send inventory update packet
		
		if (oldItem.getCount() > 0 && oldItem != newItem)
			playerIU.addModifiedItem(oldItem);
		else
			playerIU.addRemovedItem(oldItem);
		
		sendPacket(playerIU);
		statusLoadUpdate(); // Update current load as well
		
		if (target instanceof PcInventory) { // Send target update packet
			final Player targetPlayer = ((PcInventory) target).getOwner();
			InventoryUpdate playerIU2 = new InventoryUpdate();
			
			if (newItem.getCount() > count)
				playerIU2.addModifiedItem(newItem);
			else
				playerIU2.addNewItem(newItem);
			
			targetPlayer.sendPacket(playerIU2);
			targetPlayer.statusLoadUpdate(); // Update current load as well
		} else if (target instanceof PetInventory) {
			PetInventoryUpdate piu = new PetInventoryUpdate();
			
			if (newItem.getCount() > count)
				piu.addModifiedItem(newItem);
			else
				piu.addNewItem(newItem);
			
			((PetInventory) target).getOwner().getActingPlayer().sendPacket(piu);
		}

		return newItem;
	}
	
	/**
	 * Drop item from inventory and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param item ItemInstance to be dropped
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean dropItem(String process, ItemInstance item, WorldObject reference, boolean sendMessage) {
		item = _inventory.dropItem(process, item, this, reference);
		
		if (item == null) {
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			
			return false;
		}
		
		item.dropMe(this, getX() + Rnd.get(-25, 25), getY() + Rnd.get(-25, 25), getZ() + 20);
		inventoryUpdate(item); // Send inventory update packet
		statusLoadUpdate(); // Update current load as well

		if (sendMessage) // Sends message to client if requested
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1).addItemName(item));
		
		return true;
	}
	
	/**
	 * Drop item from inventory by using its <B>objectID</B> and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param objectId int Item Instance identifier of the item to be dropped
	 * @param count int Quantity of items to be dropped
	 * @param x int coordinate for drop X
	 * @param y int coordinate for drop Y
	 * @param z int coordinate for drop Z
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public ItemInstance dropItem(String process, int objectId, int count, int x, int y, int z, WorldObject reference, boolean sendMessage) {
		ItemInstance invItem = _inventory.getItemByObjectId(objectId);
		ItemInstance item = _inventory.dropItem(process, objectId, count, this, reference);
		
		if (item == null) {
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			
			return null;
		}
		
		item.setDimension(getDimension(), true); // True because Drop me will spawn it
		item.dropMe(this, x, y, z);
		inventoryUpdate(invItem); // Send inventory update packet
		statusLoadUpdate(); // Update current load as well

		if (sendMessage) // Sends message to client if requested
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1).addItemName(item));
		
		return item;
	}
	
	/** Check Item Manipulation */
	public ItemInstance checkItemManipulation(int objectId, int count) {
		if (World.getInstance().getObject(objectId) == null)
			return null;

		final ItemInstance item = getInventory().getItemByObjectId(objectId);

		if (item == null || item.getOwnerId() != getObjectId())
			return null;

		if (count < 1 || (count > 1 && !item.isStackable()))
			return null;

		if (count > item.getCount())
			return null;

		if (getPet() != null && getPet().getControlItemId() == objectId || _mountObjectId == objectId) // Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
			return null;

		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
			return null;

		if (item.isAugmented() && (isCastingNow() || isCastingSimultaneouslyNow())) // We cannot put a Weapon with Augmention in WH while casting (Possible Exploit)
			return null;

		return item;
	}
	
	/**
	 * Test cases (player drop, trade item) where the item shouldn't be able to manipulate.
	 * @param objectId : The item objectId.
	 * @return true if it the item can be manipulated, false ovtherwise.
	 */
	public ItemInstance validateItemManipulation(int objectId) {
		final ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		// You don't own the item, or item is null.
		if (item == null || item.getOwnerId() != getObjectId())
			return null;
		
		// Pet whom item you try to manipulate is summoned/mounted.
		if (getPet() != null && getPet().getControlItemId() == objectId || _mountObjectId == objectId)
			return null;
		
		// Item is under enchant process.
		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
			return null;
		
		// Can't trade a cursed weapon.
		if (CursedWeaponManager.getInstance().isCursed(item.getItemId()))
			return null;
		
		return item;
	}
	
	public void handleItemRestriction() {
		for (ItemInstance equipped : getInventory().getPaperdollItems()) {
			if (equipped.getItem().checkCondition(this, this, false))
				continue;

			useEquippableItem(equipped, equipped.isInstWeapon());
		}
	}
	
	/**
	 * Equip or unequip the item.
	 * If item is equipped, shots are applied if automation is on.
	 * If item is unequipped, shots are discharged.
	 * @param item The item to charge/discharge.
	 * @param abortAttack If true, the current attack will be aborted in order to equip the item.
	 */
	public void useEquippableItem(ItemInstance item, boolean abortAttack) {
		ItemInstance[] items = null;
		final boolean isEquipped = item.isEquipped();
		final int oldInvLimit = getInventoryLimit();

		if (item.getItem() instanceof Weapon)
			item.unChargeAllShots();

		if (isEquipped) {
			if (item.getEnchantLevel() > 0)
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(item.getEnchantLevel()).addItemName(item));
			else
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(item));

			items = getInventory().unEquipItemInBodySlotAndRecord(item);
		} else {
			items = getInventory().equipItemAndRecord(item);
			
			if (item.isEquipped()) {
				if (item.getEnchantLevel() > 0)
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED).addNumber(item.getEnchantLevel()).addItemName(item));
				else
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED).addItemName(item));

				if ((item.getItem().getBodyPart() & Item.SLOT_ALLWEAPON) != 0)
					rechargeShots(true, true);
			} else {
				sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
			}
		}
		
		refreshExpertisePenalty();
		broadcastUserInfo();
		InventoryUpdate iu = new InventoryUpdate();
		iu.addItems(Arrays.asList(items));
		sendPacket(iu);

		if (abortAttack)
			abortAttack();

		if (getInventoryLimit() != oldInvLimit)
			sendPacket(new ExStorageMaxCount(this));
	}
	
	/** Disarm the player's weapon and shield. @return true if successful, false otherwise. */
	public boolean disarmWeapons() {
		if (isCursedWeaponEquipped()) // Don't allow disarming a cursed weapon
			return false;

		disarmWeapons(getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND)); // Unequip the Weapon
		disarmWeapons(getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND)); // Unequip the Shield
		return true;
	}
	
	private void disarmWeapons(ItemInstance item) {
		if (item != null) {
			ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(item);
			InventoryUpdate iu = new InventoryUpdate();
			
			for (ItemInstance itm : unequipped)
				iu.addModifiedItem(itm);
			
			sendPacket(iu);
			abortAttack();
			broadcastUserInfo();
			
			if (unequipped.length > 0) { // This Can be 0 if the User Pressed the Right Mousebutton Twice Very Fast
				SystemMessage sm;
				
				if (unequipped[0].getEnchantLevel() > 0)
					sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(unequipped[0].getEnchantLevel()).addItemName(unequipped[0]);
				else
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(unequipped[0]);
				
				sendPacket(sm);
			}
		}
	}
	
	public int getInventoryLimit() {
		return (isDwarfRace() ? Config.INVENTORY_MAXIMUM_DWARF : Config.INVENTORY_MAXIMUM_NO_DWARF) + calcStat(Stats.INV_LIM);
	}
	
	public int getWareHouseLimit() {
		return (isDwarfRace() ? Config.WAREHOUSE_SLOTS_DWARF : Config.WAREHOUSE_SLOTS_NO_DWARF) + calcStat(Stats.WH_LIM);
	}
	
	public int getPrivateSellStoreLimit() {
		return (isDwarfRace() ? Config.MAX_PVTSTORE_SLOTS_DWARF : Config.MAX_PVTSTORE_SLOTS_OTHER) + calcStat(Stats.P_SELL_LIM);
	}
	
	public int getPrivateBuyStoreLimit() {
		return (isDwarfRace() ? Config.MAX_PVTSTORE_SLOTS_DWARF : Config.MAX_PVTSTORE_SLOTS_OTHER) + calcStat(Stats.P_BUY_LIM);
	}
	
	public int getFreightLimit() {
		return Config.FREIGHT_SLOTS + calcStat(Stats.FREIGHT_LIM);
	}
	
	public int getDwarfRecipeLimit() {
		return Config.DWARF_RECIPE_LIMIT + calcStat(Stats.REC_D_LIM);
	}
	
	public int getCommonRecipeLimit() {
		return Config.COMMON_RECIPE_LIMIT + calcStat(Stats.REC_C_LIM);
	}
	
	private final List<PcFreight> _depositedFreight = new ArrayList<>();
	
	/**
	 * @param objectId The id of the owner.
	 * @return deposited PcFreight object for the objectId or create new if not existing.
	 */
	public PcFreight getDepositedFreight(int objectId) {
		for (PcFreight freight : _depositedFreight)
			if (freight != null && freight.getOwnerId() == objectId)
				return freight;

		PcFreight freight = new PcFreight(null);
		freight.doQuickRestore(objectId);
		_depositedFreight.add(freight);
		return freight;
	}
	
	/** Clear memory used by deposited freight */
	public void clearDepositedFreight() {
		for (PcFreight freight : _depositedFreight)
			if (freight != null)
				freight.deleteMe();

		_depositedFreight.clear();
	}
	
	private StoreType _storeType = StoreType.NONE;
	
	public boolean isInStoreMode() {
		return _storeType != StoreType.NONE;
	}
	
	/** Set the Store type of the Player. @param type : 0 = none, 1 = sell, 2 = sellmanage, 3 = buy, 4 = buymanage, 5 = manufacture. */
	public void setStoreType(StoreType type) {
		_storeType = type;

		if (Config.OFFLINE_DISCONNECT_FINISHED && type == StoreType.NONE && (getClient() == null || getClient().isDetached()))
			deleteMe();
	}

	public StoreType getStoreType() {
		return _storeType;
	}
	
	/**
	 * Manage Interact Task with another Player.<BR>
	 * Turn the character in front of the target.<BR>
	 * In case of private stores, send the related packet.
	 * @param target The Creature targeted
	 */
	public void doInteract(Creature target) {
		if (target.isInstanceOfPlayer()) {
			Player temp = (Player) target;
			sendPacket(new MoveToPawn(this, temp, Npc.INTERACTION_DISTANCE));
			
			switch (temp.getStoreType()) {
				case SELL:
				case PACKAGE_SELL:
					sendPacket(new PrivateStoreListSell(this, temp));
					return;
				case BUY:
					sendPacket(new PrivateStoreListBuy(this, temp));
					return;
				case MANUFACTURE:
					sendPacket(new RecipeShopSellList(this, temp));
				default:
					return;
			}
		}

		if (target != null) // _interactTarget = null; should never happen
			target.onAction(this);
	}
	
	private PcWarehouse _warehouse;
	
	/** @return The PcWarehouse object of the Player. */
	public PcWarehouse getWarehouse() {
		if (_warehouse == null) {
			_warehouse = new PcWarehouse(this);
			_warehouse.restore();
		}
		
		return _warehouse;
	}
	
	/** Free memory used by Warehouse */
	public void clearWarehouse() {
		if (_warehouse != null)
			_warehouse.deleteMe();

		_warehouse = null;
	}
	
	private PcFreight _freight;
	
	/** @return The PcFreight object of the Player. */
	public PcFreight getFreight() {
		if (_freight == null) {
			_freight = new PcFreight(this);
			_freight.restore();
		}

		return _freight;
	}
	
	/** Free memory used by Freight */
	public void clearFreight() {
		if (_freight != null)
			_freight.deleteMe();

		_freight = null;
	}
	
	private TradeList _activeTradeList;
	
	public void setActiveTradeList(TradeList tradeList) {
		_activeTradeList = tradeList;
	}

	public TradeList getActiveTradeList() {
		return _activeTradeList;
	}
	
	public void onTradeStart(Player partner) {
		_activeTradeList = new TradeList(this);
		_activeTradeList.setPartner(partner);
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BEGIN_TRADE_WITH_S1).addString(partner.getName()));
		sendPacket(new TradeStart(this));
	}
	
	public void startTrade(Player partner) {
		onTradeStart(partner);
		partner.onTradeStart(this);
	}
	
	public void onTradeCancel(Player partner) {
		if (_activeTradeList == null)
			return;
		
		_activeTradeList.lock();
		_activeTradeList = null;
		sendPacket(new SendTradeDone(0));
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANCELED_TRADE).addString(partner.getName()));
	}
	
	public void onTradeFinish(boolean successfull) {
		_activeTradeList = null;
		sendPacket(new SendTradeDone(1));

		if (successfull)
			sendPacket(SystemMessageId.TRADE_SUCCESSFUL);
	}
	
	public void cancelActiveTrade() {
		if (_activeTradeList == null)
			return;

		Player partner = _activeTradeList.getPartner();
		
		if (partner != null)
			partner.onTradeCancel(this);

		onTradeCancel(this);
	}
	
	private ItemContainer _activeWarehouse;
	
	public void setActiveWarehouse(ItemContainer warehouse) {
		_activeWarehouse = warehouse;
	}

	public ItemContainer getActiveWarehouse() {
		return _activeWarehouse;
	}
	
	private ManufactureList _createList;
	
	public ManufactureList getCreateList() {
		return _createList;
	}

	public void setCreateList(ManufactureList list) {
		_createList = list;
	}
	
	private TradeList _sellList;
	
	public TradeList getSellList() {
		if (_sellList == null)
			_sellList = new TradeList(this);

		return _sellList;
	}
	
	private TradeList _buyList;
	
	public TradeList getBuyList() {
		if (_buyList == null)
			_buyList = new TradeList(this);

		return _buyList;
	}
	
	private PreparedListContainer _currentMultiSell;
	
	public final PreparedListContainer getMultiSell() {
		return _currentMultiSell;
	}
	
	public final void setMultiSell(PreparedListContainer list) {
		_currentMultiSell = list;
	}
	
	private Folk _currentFolk;
	
	/** Remember the current {@link Folk} of the {@link Player}, used notably for integrity check. @param folk : The Folk to remember. */
	public void setCurrentFolk(Folk folk) {
		_currentFolk = folk;
	}
	
	/** @return the current {@link Folk} of the {@link Player}. */
	public Folk getCurrentFolk() {
		return _currentFolk;
	}
	
	private int _questNpcObject;
	
	/** @return the Id for the last talked quest NPC. */
	public int getLastQuestNpcObject() {
		return _questNpcObject;
	}
	
	public void setLastQuestNpcObject(int npcId) {
		_questNpcObject = npcId;
	}
	
	private final List<QuestState> _quests = new CopyOnWriteArrayList<>();

	/**
	 * @param name The name of the quest.
	 * @return The QuestState object corresponding to the quest name.
	 */
	public QuestState getQuestState(String name) {
		for (QuestState qs : _quests)
			if (name.equals(qs.getQuest().getName()))
				return qs;

		return null;
	}
	
	/** Add a QuestState to the table _quest containing all quests began by the Player. @param qs The QuestState to add to _quest. */
	public void setQuestState(QuestState qs) {
		_quests.add(qs);
	}
	
	public List<QuestState> getQuests() {
		return _quests;
	}
	
	/** Remove a QuestState from the table _quest containing all quests began by the Player. @param qs : The QuestState to be removed from _quest. */
	public void delQuestState(QuestState qs) {
		_quests.remove(qs);
	}
	
	/**
	 * @param completed : If true, include completed quests to the list.
	 * @return list of started and eventually completed quests of the player.
	 */
	public List<Quest> getAllQuests(boolean completed) {
		List<Quest> quests = new ArrayList<>();
		
		for (QuestState qs : _quests) {
			if (qs == null || completed && qs.isCreated() || !completed && !qs.isStarted())
				continue;
			
			Quest quest = qs.getQuest();
			
			if (quest == null || !quest.isRealQuest())
				continue;
			
			quests.add(quest);
		}
		
		return quests;
	}
	
	public void processQuestEvent(String questName, String event) {
		Quest quest = ScriptData.getInstance().getQuest(questName);
		
		if (quest == null)
			return;
		
		QuestState qs = getQuestState(questName);
		
		if (qs == null)
			return;
		
		WorldObject object = World.getInstance().getObject(getLastQuestNpcObject());
		
		if (!(object instanceof Npc) || !isInsideRadius(object, Npc.INTERACTION_DISTANCE, false, false))
			return;
		
		final Npc npc = (Npc) object;
		final List<Quest> scripts = npc.getTemplate().getEventQuests(EventType.ON_TALK);
		
		if (scripts != null) {
			for (Quest script : scripts) {
				if (script == null || !script.equals(quest))
					continue;
				
				quest.notifyEvent(event, npc, this);
				break;
			}
		}
	}
	
	public void removeCompletedQuests() {
		final List<QuestState> questsToRemove = new ArrayList<>();
		final StringBuilder placeholders = new StringBuilder();
		
		for (QuestState qs : _quests) {
			if (qs.isCompleted() && !QUESTS_TO_KEEP_ON_REMOVE.contains(qs.getQuest().getName())) {
				if (placeholders.length() > 0)
					placeholders.append(',');

				placeholders.append('?');
				questsToRemove.add(qs);
			}
		}

		if (questsToRemove.isEmpty())
			return;

		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_COMPLETED_QUESTS_BY_NAME + "(" + placeholders.toString() + ")")) {
			ps.setInt(1, getObjectId());
			int paramIndex = 2;

			for (QuestState qs : questsToRemove)
				ps.setString(paramIndex++, qs.getQuest().getName());

			ps.executeUpdate();
		} catch (Exception e) {
			LOGGER.error("Could not delete completed quests for player {}.", e, getName());
			return;
		}

		_quests.removeAll(questsToRemove); //sendPacket(new QuestList(this));
	}
	
	private final List<QuestState> _notifyQuestOfDeathList = new ArrayList<>();
	
	/** Add QuestState instance that is to be notified of Player's death. @param qs The QuestState that subscribe to this event */
	public void addNotifyQuestOfDeath(QuestState qs) {
		if (qs == null)
			return;
		
		if (!_notifyQuestOfDeathList.contains(qs))
			_notifyQuestOfDeathList.add(qs);
	}
	
	/** Remove QuestState instance that is to be notified of Player's death. @param qs The QuestState that subscribe to this event */
	public void removeNotifyQuestOfDeath(QuestState qs) {
		if (qs == null)
			return;

		_notifyQuestOfDeathList.remove(qs);
	}
	
	/** @return A list of QuestStates which registered for notify of death of this Player. */
	public final List<QuestState> getNotifyQuestOfDeath() {
		return _notifyQuestOfDeathList;
	}
	
	private final PlayerMemo _vars = new PlayerMemo(getObjectId());
	
	/** @return player memos. */
	public PlayerMemo getMemos() {
		return _vars;
	}
	
	private final ShortCuts _shortCuts = new ShortCuts(this);
	
	/** @return A table containing all L2ShortCut of the Player. */
	public L2ShortCut[] getAllShortCuts() {
		return _shortCuts.getAllShortCuts();
	}
	
	/**
	 * @param slot The slot in wich the shortCuts is equipped
	 * @param page The page of shortCuts containing the slot
	 * @return The L2ShortCut of the Player corresponding to the position (page-slot).
	 */
	public L2ShortCut getShortCut(int slot, int page) {
		return _shortCuts.getShortCut(slot, page);
	}
	
	/** Add a L2shortCut to the Player _shortCuts @param shortcut The shortcut to add. */
	public void registerShortCut(L2ShortCut shortcut) {
		_shortCuts.registerShortCut(shortcut);
	}
	
	/** Delete the L2ShortCut corresponding to the position (page-slot) from the Player _shortCuts. */
	public void deleteShortCut(int slot, int page) {
		_shortCuts.deleteShortCut(slot, page);
	}
	
	/** Delete a ShortCut of the Player _shortCuts. @param objectId The shortcut id. */
	public void removeItemFromShortCut(int objectId) {
		_shortCuts.deleteShortCutByObjectId(objectId);
	}
	
	private final MacroList _macroses = new MacroList(this);
	
	/** Add a L2Macro to the Player _macroses. @param macro The Macro object to add. */
	public void registerMacro(L2Macro macro) {
		_macroses.registerMacro(macro);
	}
	
	/** Delete the L2Macro corresponding to the Identifier from the Player _macroses. */
	public void deleteMacro(int id) {
		_macroses.deleteMacro(id);
	}
	
	/** @return all L2Macro of the Player. */
	public MacroList getMacroses() {
		return _macroses;
	}
	
	private final Henna[] _henna = new Henna[3];
	
	/** Retrieve from the database all Henna of this Player, add them to _henna and calculate stats of the Player. */
	private void restoreHenna() {
		for (int i = 0; i < 3; i++) // Initialize the array.
			_henna[i] = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(RESTORE_CHAR_HENNAS)) {
			ps.setInt(1, getObjectId());
			ps.setInt(2, getClassIndex());
			
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					final int slot = rs.getInt("slot");

					if (slot < 1 || slot > 3)
						continue;

					final int symbolId = rs.getInt("symbol_id");
					
					if (symbolId != 0) {
						final Henna henna = HennaData.getInstance().getHenna(symbolId);
						
						if (henna != null)
							_henna[slot - 1] = henna;
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Couldn't restore henna.", e);
		}

		recalcHennaStats(); // Calculate Henna modifiers of this Player
	}
	
	/** @return the number of {@link Henna} empty slots of this {@link Player}. */
	public int getHennaEmptySlots() {
		int totalSlots = (getClassLevel() == 1) ? 3 : 1;

		for (int i = 0; i < 3; i++)
			if (_henna[i] != null)
				totalSlots--;

		return (totalSlots <= 0) ? 0 : totalSlots;
	}
	
	/**
	 * Remove an {@link Henna} of this {@link Player}, save it to the database and send packets to refresh client.
	 * @param slot : The slot number to make checks on.
	 * @return true if successful.
	 */
	public boolean removeHenna(int slot) {
		if (slot < 1 || slot > 3)
			return false;
		
		slot--;
		
		if (_henna[slot] == null)
			return false;
		
		Henna henna = _henna[slot];
		_henna[slot] = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement ps = con.prepareStatement(DELETE_CHAR_HENNA)) {
			ps.setInt(1, getObjectId());
			ps.setInt(2, slot + 1);
			ps.setInt(3, getClassIndex());
			ps.execute();
		} catch (Exception e) {
			LOGGER.error("Couldn't remove henna.", e);
		}
		
		recalcHennaStats(); // Calculate Henna modifiers of this Player
		refreshHennaList(); // Send HennaInfo packet to this Player
		sendUserInfo(); // Send UserInfo packet to this Player
		reduceAdena("Henna", Constant.NUMBER_1000, this, false); // henna.getPrice() / 5
		addItem(henna.getDyeId(), Constant.NUMBER_1);// Add the recovered dyes to the player's inventory and notify them. // Henna.getRequiredDyeAmount() / 2
		sendPacket(SystemMessageId.SYMBOL_DELETED);
		return true;
	}
	
	/** Add a {@link Henna} to this {@link Player}, save it to the database and send packets to refresh client. @param henna : The Henna template to add. */
	public void addHenna(Henna henna) {
		for (int i = 0; i < 3; i++) {
			if (_henna[i] == null) {
				_henna[i] = henna;
				recalcHennaStats(); // Calculate Henna modifiers of this Player
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(ADD_CHAR_HENNA)) {
					ps.setInt(1, getObjectId());
					ps.setInt(2, henna.getSymbolId());
					ps.setInt(3, i + 1);
					ps.setInt(4, getClassIndex());
					ps.execute();
				} catch (Exception e) {
					LOGGER.error("Couldn't save henna.", e);
				}
				
				refreshHennaList();
				sendUserInfo();
				sendPacket(SystemMessageId.SYMBOL_ADDED);
				return;
			}
		}
	}
	
	/** Recalculate {@link Henna} modifiers of this {@link Player}. */
	private void recalcHennaStats() {
		_hennaSTR = 0;
		_hennaDEX = 0;
		_hennaCON = 0;
		_hennaINT = 0;
		_hennaWIT = 0;
		_hennaMEN = 0;
		
		for (int i = 0; i < 3; i++) {
			if (_henna[i] == null)
				continue;
			
			_hennaSTR += _henna[i].getSTR();
			_hennaDEX += _henna[i].getDEX();
			_hennaCON += _henna[i].getCON();
			_hennaINT += _henna[i].getINT();
			_hennaWIT += _henna[i].getWIT();
			_hennaMEN += _henna[i].getMEN();
		}

		_hennaSTR = setMaxHennaStat(_hennaSTR);
		_hennaDEX = setMaxHennaStat(_hennaDEX);
		_hennaCON = setMaxHennaStat(_hennaCON);
		_hennaINT = setMaxHennaStat(_hennaINT);
		_hennaWIT = setMaxHennaStat(_hennaWIT);
		_hennaMEN = setMaxHennaStat(_hennaMEN);
	}
	
	private int setMaxHennaStat(int current) {
		return current > 15 ? 15 : current;
	}
	
	/**
	 * @param slot A slot to check.
	 * @return the {@link Henna} of this {@link Player} corresponding to the selected slot.
	 */
	public Henna getHenna(int slot) {
		if (slot < 1 || slot > 3)
			return null;
		
		return _henna[slot - 1];
	}
	
	private int _hennaSTR;
	
	public int getHennaStatSTR() {
		return _hennaSTR;
	}
	
	private int _hennaINT;
	
	public int getHennaStatINT() {
		return _hennaINT;
	}
	
	private int _hennaDEX;
	
	public int getHennaStatDEX() {
		return _hennaDEX;
	}
	
	private int _hennaMEN;
	
	public int getHennaStatMEN() {
		return _hennaMEN;
	}
	
	private int _hennaWIT;
	
	public int getHennaStatWIT() {
		return _hennaWIT;
	}
	
	private int _hennaCON;
	
	public int getHennaStatCON() {
		return _hennaCON;
	}
	
	private Summon _summon;

	public boolean hasPet() {
		return _summon instanceof Pet;
	}

	public boolean hasServitor() {
		return _summon instanceof Servitor;
	}

	public void setPet(Summon summon) {
		_summon = summon;
	}
	
	/** Unsummon all types of summons : pets, cubics, normal summons and trained beasts. */
	public void dropAllSummons() {
		if (getPet() != null)
			getPet().unSummon(this); // Delete summons and pets

		if (getTrainedBeast() != null)
			getTrainedBeast().deleteMe(); // Delete trained beasts

		stopCubics(); // Delete any form of cubics
	}
	
	private TamedBeast _tamedBeast;
	
	public TamedBeast getTrainedBeast() {
		return _tamedBeast;
	}

	public void setTrainedBeast(TamedBeast tamedBeast) {
		_tamedBeast = tamedBeast;
	}
	
	private L2Radar _radar; // This needs to be better integrated and saved/loaded
	
	public L2Radar getRadar() {
		return _radar;
	}

	private SystemMessageId _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
	
	/** This returns a SystemMessage stating why the player is not available for duelling. @return S1_CANNOT_DUEL... message */
	public SystemMessage getNoDuelReason() {
		final SystemMessage sm = SystemMessage.getSystemMessage(_noDuelReason).addCharName(this); // Prepare the message with the good reason.
		_noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL; // Reinitialize the reason.
		return sm; // Send stored reason.
	}
	
	/** Checks if this player might join / start a duel. To get the reason use getNoDuelReason() after calling this function. @return true if the player might join/start a duel. */
	public boolean canDuel() {
		if (isInCombat() || getPunishLevel() == PunishLevel.JAIL)
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
		else if (isDead() || isAlikeDead() || (getCurrentHp() < getMaxHp() / 2 || getCurrentMp() < getMaxMp() / 2))
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_HP_OR_MP_IS_BELOW_50_PERCENT;
		else if (isInDuel())
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL;
		else if (isInOlympiadMode())
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
		else if (isCursedWeaponEquipped() || hasKarma())
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE;
		else if (isInStoreMode())
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
		else if (isMounted() || isInBoat())
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER;
		else if (isFishing())
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING;
		else if (isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.PEACE) || isInsideZone(ZoneId.SIEGE))
			_noDuelReason = SystemMessageId.S1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
		else
			return true;
		
		return false;
	}
	
	private Boat _boat;
	
	public boolean isInBoat() {
		return _boat != null;
	}
	
	public Boat getBoat() {
		return _boat;
	}
	
	public void setBoat(Boat v) {
		if (v == null && _boat != null)
			_boat.removePassenger(this);
		
		_boat = v;
	}
	
	private SpawnLocation _boatPosition = new SpawnLocation(0, 0, 0, 0);
	
	public SpawnLocation getBoatPosition() {
		return _boatPosition;
	}
	
	private int _curFeed;
	
	public int getCurrentFeed() {
		return _curFeed;
	}
	
	public void setCurrentFeed(int num) {
		_curFeed = Math.min(num, _petData.getMaxMeal());
		sendPacket(new SetupGauge(GaugeColor.GREEN, getCurrentFeed() * 10000 / getFeedConsume(), _petData.getMaxMeal() * 10000 / getFeedConsume()));
	}
	
	private boolean _canFeed;
	
	protected synchronized void startFeed(int npcId) {
		_canFeed = npcId > 0;

		if (!isMounted())
			return;

		if (getPet() != null) {
			setCurrentFeed(((Pet) getPet()).getCurrentFed());
			_controlItemId = getPet().getControlItemId();
			stopFeeding();
		} else if (_canFeed) {
			setCurrentFeed(_petData.getMaxMeal());
			stopFeeding();
		}
	}
	
	/**
	 * @param state : The state to check (can be autofeed, hungry or unsummon).
	 * @return true if the limit is reached, false otherwise or if there is no need to feed.
	 */
	public boolean checkFoodState(double state) {
		return (_canFeed) ? getCurrentFeed() < (_petData.getMaxMeal() * state) : false;
	}
	
	private Future<?> _mountFeedTask;
	
	protected synchronized void stopFeeding() {
		sendPacket(new SetupGauge(GaugeColor.GREEN, getCurrentFeed() * Constant.NUMBER_10000 / getFeedConsume(), _petData.getMaxMeal() * Constant.NUMBER_10000 / getFeedConsume()));

		if (!isDead())
			_mountFeedTask = ThreadPool.scheduleAtFixedRate(new FeedTask(), Constant.NUMBER_10000, Constant.NUMBER_10000);
	}
	
	protected synchronized void stopFeed() {
		if (_mountFeedTask != null) {
			_mountFeedTask.cancel(false);
			_mountFeedTask = null;
		}
	}
	
	private PetTemplate _petTemplate;
	
	public PetTemplate getPetTemplate() {
		return _petTemplate;
	}
	
	private PetDataEntry _petData;
	
	protected int getFeedConsume() {
		return (isAttackingNow()) ? _petData.getMountMealInBattle() : _petData.getMountMealInNormal();
	}
	
	public PetDataEntry getPetDataEntry() {
		return _petData;
	}
	
	private int _controlItemId;
	
	public void storePetFood(int petId) {
		if (_controlItemId != 0 && petId != 0) {
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement("UPDATE pets SET fed=? WHERE item_obj_id = ?")) {
				ps.setInt(1, getCurrentFeed());
				ps.setInt(2, _controlItemId);
				ps.executeUpdate();
				_controlItemId = 0;
			} catch (Exception e) {
				LOGGER.error("Couldn't store pet food data for {}.", e, _controlItemId);
			}
		}
	}
	
	private ScheduledFuture<?> _dismountTask;
	
	/** A method used to test player entrance on no landing zone. If a player is mounted on a Wyvern, it launches a dismount task after 5 seconds, and a warning message. */
	public void enterOnNoLandingZone() {
		if (getMountType() == 2) {
			if (_dismountTask == null)
				_dismountTask = ThreadPool.schedule(() -> dismount(), 5000);

			sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
		}
	}
	
	/** A method used to test player leave on no landing zone. If a player is mounted on a Wyvern, it cancels the dismount task, if existing. */
	public void exitOnNoLandingZone() {
		if (getMountType() == 2 && _dismountTask != null) {
			_dismountTask.cancel(true);
			_dismountTask = null;
		}
	}
	
	private int _mountType;
	
	public boolean isMounted() {
		return _mountType > 0;
	}

	/** @return the type of Pet mounted (0 : none, 1 : Strider, 2 : Wyvern). */
	public int getMountType() {
		return _mountType;
	}
	
	private int _mountNpcId;
	
	public int getMountNpcId() {
		return _mountNpcId;
	}
	
	public boolean mount(Summon pet) {
		if (!disarmWeapons())
			return false;
		
		setRunning();
		stopAllToggles();
		Ride mount = new Ride(getObjectId(), Ride.ACTION_MOUNT, pet.getTemplate().getNpcId());
		setMount(pet.getNpcId(), pet.getLevel(), mount.getMountType());
		_petTemplate = (PetTemplate) pet.getTemplate();
		_petData = _petTemplate.getPetDataEntry(pet.getLevel());
		_mountObjectId = pet.getControlItemId();
		startFeed(pet.getNpcId());
		broadcastPacket(mount);
		broadcastUserInfo(); // Notify self and others about speed change
		pet.unSummon(this);
		return true;
	}
	
	public boolean mount(int npcId, int controlItemId) {
		if (!disarmWeapons())
			return false;

		setRunning();
		stopAllToggles();
		Ride mount = new Ride(getObjectId(), Ride.ACTION_MOUNT, npcId);
		
		if (setMount(npcId, getLevel(), mount.getMountType())) {
			_petTemplate = (PetTemplate) NpcData.getInstance().getTemplate(npcId);
			_petData = _petTemplate.getPetDataEntry(getLevel());
			_mountObjectId = controlItemId;
			broadcastPacket(mount);
			broadcastUserInfo(); // Notify self and others about speed change
			startFeed(npcId);
			return true;
		}
		
		return false;
	}
	
	/**
	 * This method allows to :
	 * change isRiding/isFlying flags
	 * gift player with Wyvern Breath skill if mount is a wyvern
	 * send the skillList (faded icons update)
	 * @param npcId the npcId of the mount
	 * @param npcLevel The level of the mount
	 * @param mountType 0, 1 or 2 (dismount, strider or wyvern).
	 * @return always true.
	 */
	public boolean setMount(int npcId, int npcLevel, int mountType) {
		switch (mountType) {
			case 0: // Dismounted
				if (isFlying())
					removeSkill(FrequentSkill.WYVERN_BREATH.getSkill().getId(), false);
				break;

			case 2: // Flying Wyvern
				addSkill(FrequentSkill.WYVERN_BREATH.getSkill(), false);
		}
		
		_mountNpcId = npcId;
		_mountType = mountType;
		_mountLevel = npcLevel;
		sendSkillList(); // Update faded icons && eventual added skills.
		return true;
	}
	
	public boolean mountPlayer(Summon summon) {
		if (summon instanceof Pet && summon.isMountable() && !isMounted() && !isBetrayed()) {
			if (isDead()) {
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD); // A strider cannot be ridden when dead.
				return false;
			}
			
			if (summon.isDead()) {
				sendPacket(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN); // A dead strider cannot be ridden.
				return false;
			}
			
			if (summon.isInCombat() || summon.isRooted()) {
				sendPacket(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN); // A strider in battle cannot be ridden.
				return false;
			}
			
			if (isInCombat()) {
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE); // A strider cannot be ridden while in battle.
				return false;
			}
			
			if (isSitting()) {
				sendPacket(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING); // A strider can be ridden only when standing.
				return false;
			}
			
			if (isFishing()) {
				sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2); // You can't mount, dismount, break and drop items while fishing.
				return false;
			}
			
			if (isCursedWeaponEquipped()) {
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE); // You can't mount, dismount, break and drop items while weilding a cursed weapon.
				return false;
			}
			
			if (!MathUtil.checkIfInRange(200, this, summon, true)) {
				sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_STRIDER_TO_MOUNT);
				return false;
			}
			
			if (((Pet) summon).checkHungryState()) {
				sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
				return false;
			}
			
			if (!summon.isDead() && !isMounted())
				mount(summon);
		} else if (isMounted()) {
			if (getMountType() == 2 && isInsideZone(ZoneId.NO_LANDING)) {
				sendPacket(SystemMessageId.NO_DISMOUNT_HERE);
				return false;
			}
			
			if (checkFoodState(_petTemplate.getHungryLimit())) {
				sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
				return false;
			}
			
			dismount();
		}
		
		return true;
	}
	
	public boolean dismount() {
		sendPacket(new SetupGauge(GaugeColor.GREEN, 0));
		int petId = _mountNpcId;

		if (setMount(0, 0, 0)) {
			stopFeed();
			broadcastPacket(new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0));
			_petTemplate = null;
			_petData = null;
			_mountObjectId = 0;
			storePetFood(petId);
			broadcastUserInfo(); // Notify self and others about speed change
			return true;
		}

		return false;
	}
	
	private int _mountLevel;
	
	public int getMountLevel() {
		return _mountLevel;
	}
	
	private int _mountObjectId;
	
	public void setMountObjectId(int id) {
		_mountObjectId = id;
	}
	
	public int getMountObjectId() {
		return _mountObjectId;
	}
	
	private int _teleMode;
	
	public int getTeleMode() {
		return _teleMode;
	}
	
	public void setTeleMode(int mode) {
		_teleMode = mode;
	}
	
	private boolean _isCrystallizing;
	
	public void setCrystallizing(boolean mode) {
		_isCrystallizing = mode;
	}
	
	public boolean isCrystallizing() {
		return _isCrystallizing;
	}
	
	private boolean _isCrafting;
	
	public boolean isCrafting() {
		return _isCrafting;
	}
	
	public void setCrafting(boolean state) {
		_isCrafting = state;
	}
	
	private int _throneId;
	private boolean _isSitting;
	
	public boolean isSitting() {
		return _isSitting;
	}

	public void setSitting(boolean state) {
		_isSitting = state;
	}
	
	/** Sit down the Player, set the AI Intention to REST and send ChangeWaitType packet (broadcast) */
	public void sitDown() {
		sitDown(true);
	}
	
	public void sitDown(boolean checkCast) {
		if (checkCast && isCastingNow())
			return;

		if (!_isSitting && !isAttackingDisabled() && !isOutOfControl() && !isImmobilized()) {
			breakAttack();
			setSitting(true);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
			getAI().setIntention(CtrlIntention.REST);
			ThreadPool.schedule(() -> setIsParalyzed(false), 2500); // Schedule a sit down task to wait for the animation to finish
			setIsParalyzed(true);
		}
	}
	
	/** Stand up the Player, set the AI Intention to IDLE and send ChangeWaitType packet (broadcast) */
	public void standUp() {
		if (_isSitting && !isInStoreMode() && !isAlikeDead() && !isParalyzed()) {
			if (_effects.isAffected(L2EffectFlag.RELAXING))
				stopEffects(L2EffectType.RELAXING);

			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			setIsParalyzed(true);
			ThreadPool.schedule(() -> { // Schedule a stand up task to wait for the animation to finish
				setSitting(false);
				setIsParalyzed(false);
				getAI().setIntention(CtrlIntention.IDLE);
			}, 2500);
		}
	}
	
	/** Stands up and close any opened shop window, if any. */
	public void forceStandUp() {
		if (isInStoreMode()) { // Cancels any shop types.
			setStoreType(StoreType.NONE);
			broadcastUserInfo();
		}
		
		standUp(); // Stand up.
	}
	
	/**
	 * Used to sit or stand. If not possible, queue the action.
	 * @param target The target, used for thrones types.
	 * @param sittingState The sitting state, inheritated from packet or player status.
	 */
	public void tryToSitOrStand(final WorldObject target, final boolean sittingState) {
		if (isFakeDeath()) {
			stopFakeDeath(true);
			return;
		}

		final boolean isThrone = target instanceof StaticObject && ((StaticObject) target).getType() == 1;

		if (isThrone && !sittingState && !isInsideRadius(target, Npc.INTERACTION_DISTANCE, false, false)) { // Player wants to sit on a throne but is out of radius, move to the throne delaying the sit action.
			getAI().setIntention(CtrlIntention.MOVE_TO, new Location(target.getX(), target.getY(), target.getZ()));
			
			NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.MOVE_TO, () -> {
				if (getMountType() != 0)
					return;
				
				sitDown();
				
				if (!((StaticObject) target).isBusy()) {
					_throneId = target.getObjectId();
					((StaticObject) target).setBusy(true);
					broadcastPacket(new ChairSit(getObjectId(), ((StaticObject) target).getStaticObjectId()));
				}
			});
			
			getAI().setNextAction(nextAction); // Binding next action to AI.
			return;
		}
		
		if (!isMoving()) { // Player isn't moving, sit directly.
			if (getMountType() != 0)
				return;
			
			if (sittingState) {
				if (_throneId != 0) {
					final WorldObject object = World.getInstance().getObject(_throneId);

					if (object instanceof StaticObject)
						((StaticObject) object).setBusy(false);

					_throneId = 0;
				}
				
				standUp();
			} else {
				sitDown();
				
				if (isThrone && !((StaticObject) target).isBusy() && isInsideRadius(target, Npc.INTERACTION_DISTANCE, false, false)) {
					_throneId = target.getObjectId();
					((StaticObject) target).setBusy(true);
					broadcastPacket(new ChairSit(getObjectId(), ((StaticObject) target).getStaticObjectId()));
				}
			}
			
			nextActionSit(target, sittingState, isThrone);
		} else {
			NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.MOVE_TO, () -> nextActionSit(target, sittingState, isThrone)); // Player is moving, wait the current action is done, then sit.
			getAI().setNextAction(nextAction); // Binding next action to AI.
		}
	}
	
	private void nextActionSit(WorldObject target, boolean sittingState, boolean isThrone) {
		if (getMountType() != 0)
			return;

		if (sittingState) {
			if (_throneId != 0) {
				final WorldObject object = World.getInstance().getObject(_throneId);

				if (object instanceof StaticObject)
					((StaticObject) object).setBusy(false);

				_throneId = 0;
			}

			standUp();
		} else {
			sitDown();

			if (isThrone && !((StaticObject) target).isBusy() && isInsideRadius(target, Npc.INTERACTION_DISTANCE, false, false)) {
				_throneId = target.getObjectId();
				((StaticObject) target).setBusy(true);
				broadcastPacket(new ChairSit(getObjectId(), ((StaticObject) target).getStaticObjectId()));
			}
		}
	}
	
	private final Map<Integer, Recipe> _dwarvenRecipeBook = new HashMap<>();
	
	public Collection<Recipe> getDwarvenRecipeBook() {
		return _dwarvenRecipeBook.values();
	}
	
	/** Add a new L2RecipList to the table _recipebook containing all RecipeList of the Player. @param recipe The RecipeList to add to the _recipebook */
	public void registerDwarvenRecipeList(Recipe recipe) {
		_dwarvenRecipeBook.put(recipe.getId(), recipe);
	}
	
	/**
	 * @param recipeId The Identifier of the RecipeList to check in the player's recipe books
	 * @return <b>TRUE</b> if player has the recipe on Common or Dwarven Recipe book else returns <b>FALSE</b>
	 */
	public boolean hasRecipeList(int recipeId) {
		return _dwarvenRecipeBook.containsKey(recipeId) || _commonRecipeBook.containsKey(recipeId);
	}
	
	/** Tries to remove a {@link Recipe} from this {@link Player}. Delete the associated {@link L2ShortCut}, if existing. @param recipeId : The id of the Recipe to remove. */
	public void unregisterRecipeList(int recipeId) {
		if (_dwarvenRecipeBook.containsKey(recipeId))
			_dwarvenRecipeBook.remove(recipeId);
		else if (_commonRecipeBook.containsKey(recipeId))
			_commonRecipeBook.remove(recipeId);

		for (L2ShortCut sc : getAllShortCuts())
			if (sc.getId() == recipeId && sc.getType() == L2ShortCut.TYPE_RECIPE)
				deleteShortCut(sc.getSlot(), sc.getPage());
	}
	
	private final Map<Integer, Recipe> _commonRecipeBook = new HashMap<>();
	
	public Collection<Recipe> getCommonRecipeBook() {
		return _commonRecipeBook.values();
	}
	
	/** Add a new L2RecipList to the table _commonrecipebook containing all RecipeList of the Player. @param recipe The RecipeList to add to the _recipebook */
	public void registerCommonRecipeList(Recipe recipe) {
		_commonRecipeBook.put(recipe.getId(), recipe);
	}
	
	/** Store {@link Recipe} book data for this {@link Player}, if he isn't on an active subclass. */
	private void storeRecipeBook() {
		if (isSubClassActive()) // If the player is on a sub-class don't even attempt to store a recipe book.
			return;

		int playerObjectId = getObjectId();

		try (Connection con = L2DatabaseFactory.getInstance().getConnection()) {
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?");
			ps.setInt(1, playerObjectId);
			ps.execute();
			ps.close();
			ps = con.prepareStatement("INSERT INTO character_recipebook (charId, recipeId) values(?,?)");
			insertRecipes(ps, getCommonRecipeBook(), playerObjectId);
			insertRecipes(ps, getDwarvenRecipeBook(), playerObjectId);
			ps.executeBatch();
			ps.close();
		} catch (Exception e) {
			LOGGER.error("Couldn't store recipe book data.", e);
		}
	}
	
	private void insertRecipes(PreparedStatement ps, Collection<Recipe> recipeBook, int playerObjectId) {
		for (Recipe recipe : recipeBook) {
			try {
				ps.setInt(1, playerObjectId);
				ps.setInt(2, recipe.getId());
				ps.addBatch();
			} catch (Exception e) {
				LOGGER.error("Couldn't insert recipe: " + recipe, e);
			}
		}
	}
	
	/** Restore {@link Recipe} book data for this {@link Player}. */
	private void restoreRecipeBook() {
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT recipeId FROM character_recipebook WHERE charId=?")) {
			ps.setInt(1, getObjectId());
			
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					final Recipe recipe = RecipeData.getInstance().getRecipeList(rs.getInt("recipeId"));
					
					if (recipe.isDwarven())
						registerDwarvenRecipeList(recipe);
					else
						registerCommonRecipeList(recipe);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Couldn't restore recipe book data.", e);
		}
	}
	
	private final Location _savedLocation = new Location(0, 0, 0);
	
	private void teleToLocationAndClean() {
		teleToLocation(_savedLocation, 0);
		_savedLocation.clean(); // Clear the location.
	}
	
	public Location getSavedLocation() {
		return _savedLocation;
	}
	
	private int _recomHave;
	
	public int getRecomHave() {
		return _recomHave;
	}

	public void setRecomHave(int value) {
		_recomHave = value;
	}

	public void addRecomHave(int value) {
		_recomHave += value;
	}
	
	private int _recomLeft;
	
	public int getRecomLeft() {
		return _recomLeft;
	}

	public void setRecomLeft(int value) {
		_recomLeft = value;
	}
	
	public void addRecomLeft(int value) {
		_recomLeft += value;
	}
	
	public void decRecomLeft() {
		if (_recomLeft > 0)
			_recomLeft--;
	}

	private boolean _isInOlympiadMode;
	
	public boolean isInObserverMode() {
		return !_isInOlympiadMode && !_savedLocation.equals(Location.DUMMY_LOC);
	}
	
	public boolean isInOlympiadMode() {
		return _isInOlympiadMode;
	}
	
	public void setOlympiadMode(boolean b) {
		_isInOlympiadMode = b;
	}
	
	private boolean _isInOlympiadStart;
	
	public boolean isOlympiadStart() {
		return _isInOlympiadStart;
	}
	
	public void setOlympiadStart(boolean b) {
		_isInOlympiadStart = b;
	}
	
	private int _olympiadGameId = -1;
	
	public int getOlympiadGameId() {
		return _olympiadGameId;
	}
	
	public void setOlympiadGameId(int id) {
		_olympiadGameId = id;
	}
	
	public void enterObserverMode(int x, int y, int z) {
		dropAllSummons();
		
		if (isInParty())
			getParty().removePartyMember(this, MessageType.EXPELLED);
		
		standUp();
		_savedLocation.set(getPosition());
		setTarget(null);
		setIsInvul(true);
		getAppearance().setInvisible();
		setIsParalyzed(true);
		startParalyze();
		teleToLocation(x, y, z, 0);
		sendPacket(new ObservationMode(x, y, z));
	}
	
	public void leaveObserverMode() {
		hasAIaction();
		setIsParalyzed(false);
		stopParalyze(false);
		sendPacket(new ObservationReturn(_savedLocation));
		teleToLocationAndClean();
	}
	
	public void enterOlympiadObserverMode(int id) {
		final OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(id);

		if (task == null)
			return;

		dropAllSummons();

		if (isInParty())
			getParty().removePartyMember(this, MessageType.EXPELLED);

		_olympiadGameId = id;
		standUp();

		if (!isInObserverMode()) // Don't override saved location if we jump from stadium to stadium.
			_savedLocation.set(getPosition());

		setTarget(null);
		setIsInvul(true);
		getAppearance().setInvisible();
		teleToLocation(task.getZone().getLocs().get(2), 0);
		sendPacket(new ExOlympiadMode(3));
	}
	
	public void leaveOlympiadObserverMode() {
		if (_olympiadGameId == -1)
			return;

		_olympiadGameId = -1;
		hasAIaction();
		sendPacket(new ExOlympiadMode(0));
		teleToLocationAndClean();
	}
	
	private int _olympiadSide = -1;
	
	public int getOlympiadSide() {
		return _olympiadSide;
	}
	
	public void setOlympiadSide(int side) {
		_olympiadSide = side;
	}
	
	private DuelState _duelState = DuelState.NO_DUEL;
	
	public void setDuelState(DuelState state) {
		_duelState = state;
	}
	
	public DuelState getDuelState() {
		return _duelState;
	}
	
	/** Sets up the duel state using a non 0 duelId. @param duelId 0 = not in a duel */
	public void setInDuel(int duelId) {
		if (duelId > 0) {
			_duelState = DuelState.ON_COUNTDOWN;
			_duelId = duelId;
		} else {
			if (_duelState == DuelState.DEAD) {
				enableAllSkills();
				getStatus().startHpMpRegeneration();
			}

			_duelState = DuelState.NO_DUEL;
			_duelId = 0;
		}
	}
	
	private int _duelId;
	
	public boolean isInDuel() {
		return _duelId > 0;
	}
	
	public int getDuelId() {
		return _duelId;
	}

	private int _curWeightPenalty;
	
	public int getWeightPenalty() {
		return _curWeightPenalty;
	}
	
	/** @return The max weight that the Player can load. */
	public int getMaxLoad() {
		return (int) calcStat(Stats.MAX_LOAD, (Math.pow(1.024, getLevel()) * 20000.0) * Config.ALT_WEIGHT_LIMIT, this, null);
	}

	public void refreshOverloaded() {
		int maxLoad = getMaxLoad();
		
		if (maxLoad > 0) {
			int weightProc = getCurrentLoad() * 1000 / maxLoad;
			int newWeightPenalty = (weightProc < 550) ? 0 : (weightProc < 700) ? 1 : (weightProc < 850) ? 2 : (weightProc < 1000) ? 3 : 4;

			if (_curWeightPenalty != newWeightPenalty) {
				_curWeightPenalty = newWeightPenalty;
				
				if (newWeightPenalty > 0) {
					addTemporarySkill(4270, newWeightPenalty);
					setIsOverloaded(getCurrentLoad() > maxLoad);
				} else {
					removeSkill(4270, false);
					setIsOverloaded(false);
				}
				
				sendUserInfo();
				sendPacket(new EtcStatusUpdate(this));
				broadcastCharInfo();
			}
		}
	}
	
	private long _punishTimer;
	
	public long getPunishTimer() {
		return _punishTimer;
	}
	
	public void setPunishTimer(long time) {
		_punishTimer = time;
	}
	
	private ScheduledFuture<?> _punishTask;
	
	private void updatePunishState() {
		if (getPunishLevel() != PunishLevel.NONE) {
			if (_punishTimer > 0) { // If Punish Timer Exists, Restart Punishtask.
				_punishTask = ThreadPool.schedule(() -> setPunishLevel(PunishLevel.NONE, 0), _punishTimer);
				sendMessage("You are still in " + getPunishLevel().getPunishName() + " state for " + Math.round(_punishTimer / 60000f) + " minutes.");
			}
			
			if (getPunishLevel() == PunishLevel.JAIL && !isInsideZone(ZoneId.JAIL))  // If Player Escaped, Put Him Back in Jail.
				teleToLocation(Location.JAIL);
		}
	}
	
	public void stopPunishTask(boolean save) {
		if (_punishTask != null) {
			if (save) {
				long delay = _punishTask.getDelay(TimeUnit.MILLISECONDS);

				if (delay < 0)
					delay = 0;

				setPunishTimer(delay);
			}

			_punishTask.cancel(false);
			_punishTask = null;
		}
	}
	
	private PunishLevel _punishLevel = PunishLevel.NONE;
	
	public PunishLevel getPunishLevel() {
		return _punishLevel;
	}

	public boolean isInJail() {
		return _punishLevel == PunishLevel.JAIL;
	}

	public boolean isChatBanned() {
		return _punishLevel == PunishLevel.CHAT;
	}
	
	public boolean isDeadPenalty() {
		return _punishLevel == PunishLevel.DEAD;
	}
	
	public void setPunishLevel(PunishLevel level) {
		_punishLevel = level;
	}
	
	public void setPunishLevel(int state) {
		setPunishLevel(getPunishLevelState(state));
	}
	
	public boolean isPunishLevelJail() {
		return _punishLevel == PunishLevel.JAIL;
	}
	
	public PunishLevel getPunishLevelState(int state) {
		switch (state) {
			case 1: return PunishLevel.CHAT;
			case 2: return PunishLevel.JAIL;
			case 3: return PunishLevel.CHAR;
			case 4: return PunishLevel.ACC;
			case 5: return PunishLevel.DEAD;
			default: return PunishLevel.NONE;
		}
	}
	
	/** Sets punish level for player based on delay */
	public void setPunishLevel(PunishLevel state, int delayInMinutes) {
		switch (state) {
			case NONE: // Remove Punishments
				handleRemovePunishment();
				break;

			case CHAT: // Chat ban
				applyPunishment(state, delayInMinutes, "Chat penalty has been activated", "Chat penalty has been lifted.");
				break;

			case JAIL: // Jail Player
				applyJailPunishment(delayInMinutes);
				break;

			case CHAR: // Ban Character
				applyCharPunishment();
				break;

			case ACC: // Ban Account
				applyAccPunishment();
				break;

			case DEAD:
				applyPunishment(state, delayInMinutes, "Dead penalty has been activated", "Dead penalty has been lifted.");
				break;

			default:
				setPunishLevel(state);
				break;
		}

		storeCharBase();
	}

	private void applyJailPunishment(int delayInMinutes) {
		setPunishLevel(PunishLevel.JAIL);
		schedulePunishmentTask(delayInMinutes, "You are jailed");

		if (OlympiadManager.getInstance().isRegisteredInComp(this))
			OlympiadManager.getInstance().removeDisconnectedCompetitor(this);

		if (EventBase.isPlayerParticipant(getObjectId()))
			EventBase.getEventInstance().removeParticipant(getObjectId());

		sendHtmlMessage("data/html/jail_in.htm");
		setIsIn7sDungeon(false);
		teleToLocation(Location.JAIL);
	}
	
	private void applyPunishment(PunishLevel level, int delayInMinutes, String activatedMessage, String liftedMessage) {
		if (isPunishLevelJail())
			return;

		setPunishLevel(level);
		sendPacket(new EtcStatusUpdate(this));
		
		if (!schedulePunishmentTask(delayInMinutes, activatedMessage))
			sendMessage(liftedMessage);
	}
	
	private boolean schedulePunishmentTask(int delayInMinutes, String baseMessage) {
		int delayInMillis = MathUtil.minsToMs(delayInMinutes);
		boolean hasDelay = hasDelay(delayInMillis);
		setPunishTimer(0);
		stopPunishTask(false);

		if (hasDelay) {
			setPunishTimer(delayInMillis);
			_punishTask = ThreadPool.schedule(() -> setPunishLevel(PunishLevel.NONE, 0), _punishTimer);
			sendMessage(baseMessage + " for " + delayInMinutes + " minute(s).");
		}
		
		sendPacket(new PlaySound("systemmsg_e.346"));
		return hasDelay;
	}
	
	private boolean hasDelay(int delayInMillis) {
		return delayInMillis > 0;
	}
	
	private void handleRemovePunishment() {
		setPunishLevel(PunishLevel.NONE);
		stopPunishTask(true);

		switch (_punishLevel) {
			case CHAT:
				completePunishmentRemoval("Chatting is now available.");
				break;
			case JAIL:
				removeJailPunishment();
				break;
			case DEAD:
				removeDeadPunishment();
			default:
				break;
		}
	}
	
	private void completePunishmentRemoval(String message) {
		sendPacket(new EtcStatusUpdate(this));
		sendPacket(new PlaySound("systemmsg_e.345"));
		sendMessage(message);
	}
	
	private void removeJailPunishment() {
		sendHtmlMessage("data/html/jail_out.htm");
		teleToLocation(Location.FLORAN);
	}

	private void removeDeadPunishment() {
		completePunishmentRemoval("Dead penalty has been lifted.");

		if (isDead())
			Constant.teleportBasedOnFaction(this, 50);
	}

	private void applyCharPunishment() {
		setAccessLevel(-1);
		logout(false);
	}

	private void applyAccPunishment() {
		setAccountAccesslevel(-100);
		logout(false);
	}
	
	private void sendHtmlMessage(String htmlFilePath) {
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(htmlFilePath);
		sendPacket(html);
	}
	
	private long _onlineBeginTime;
	private long _onlineTime;

	public void setOnlineTime(long time) {
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}

	public long getOnlineTime() {
		return _onlineTime;
	}

	private long _lastAccess;
	
	public long getLastAccess() {
		return _lastAccess;
	}
	
	private long _uptime;
	
	public void setUptime(long time) {
		_uptime = time;
	}
	
	public long getUptime() {
		return System.currentTimeMillis() - _uptime;
	}
	
	private long _expBeforeDeath;
	
	public void setExpBeforeDeath(long exp) {
		_expBeforeDeath = exp;
	}
	
	public long getExpBeforeDeath() {
		return _expBeforeDeath;
	}
	
	private boolean _isOnline;
	
	/**
	 * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).
	 * @param isOnline
	 * @param updateInDb
	 */
	public void setOnlineStatus(boolean isOnline, boolean updateInDb) {
		if (_isOnline != isOnline)
			_isOnline = isOnline;

		if (updateInDb)
			updateOnlineStatus(); // Update the characters table of the database with online status and lastAccess (called when login and logout)
	}
	
	
	public boolean isOnline() {
		return _isOnline;
	}
	
	/** @return an int interpretation of online status. */
	public int isOnlineInt() {
		if (_isOnline && getClient() != null)
			return getClient().isDetached() ? 2 : 1;

		return 0;
	}
	
	/** Update the characters table of the database with online status and lastAccess of this Player (called when login and logout). */
	public void updateOnlineStatus() {
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?")) {
			ps.setInt(1, isOnlineInt());
			ps.setLong(2, System.currentTimeMillis());
			ps.setInt(3, getObjectId());
			ps.execute();
		} catch (Exception e) {
			LOGGER.error("Couldn't set player online status.", e);
		}
	}
	
	private boolean _isInSiege;
	
	public void setIsInSiege(boolean b) {
		_isInSiege = b;
	}
	
	public boolean isInSiege() {
		return _isInSiege;
	}
	
	/*public boolean isInActiveSiege() {
		return isInSiege() && getSiegeState() > 0;
	}*/
	
	private boolean _isIn7sDungeon; // not in use
	
	public void setIsIn7sDungeon(boolean isIn7sDungeon) {
		_isIn7sDungeon = isIn7sDungeon;
	}
	
	public boolean isIn7sDungeon() {
		return _isIn7sDungeon;
	}
	
	private int _baseClass;
	
	public void setBaseClass(int baseClass) {
		_baseClass = baseClass;
	}
	
	public void setBaseClass(ClassId classId) {
		_baseClass = classId.ordinal();
	}
	
	public int getBaseClass() {
		return _baseClass;
	}
	
	private int _activeClass;
	
	public int getActiveClass() {
		return _activeClass;
	}
	
	private void setClassTemplate(int classId) {
		_activeClass = classId;
		setTemplate(PlayerData.getInstance().getTemplate(classId)); // Set the template of the Player
	}
	
	/**
	 * Changes the character's class based on the given class index. <BR>
	 * An index of zero specifies the character's original (base) class, while indexes 1-3 specifies the character's sub-classes respectively.
	 * @return true if successful.
	 */
	public boolean setActiveClass(int classIndex) {
		if (!_subclassLock.tryLock())
			return false;
		
		try {
			for (ItemInstance item : getInventory().getAugmentedItems()) // Remove active item skills before saving char to database because next time when choosing this class, worn items can be different
				if (item != null && item.isEquipped())
					item.getAugmentation().removeBonus(this);

			abortCast(); // Abort any kind of cast

			for (Creature character : getKnownType(Creature.class)) // Stop casting for any player that may be casting a force buff on this player.
				if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
					character.abortCast();
				
			store();
			_reuseTimeStamps.clear();
			_charges.set(0); // Clear charges
			stopChargeTask();
			
			if (classIndex == 0) {
				setClassTemplate(getBaseClass());
			} else {
				try {
					setClassTemplate(_subClasses.get(classIndex).getClassId());
				} catch (Exception e) {
					LOGGER.error("Could not switch {}'s subclass to class index {}.", e, getName(), classIndex);
					return false;
				}
			}
			
			_classIndex = classIndex;
			
			if (isInParty())
				_party.recalculateLevel();
			
			if (getPet() instanceof Servitor)
				getPet().unSummon(this);

			removeAllSkills(false);
			stopAllEffectsExceptThoseThatLastThroughDeath();
			stopCubics();
			
			if (isSubClassActive()) {
				_dwarvenRecipeBook.clear();
				_commonRecipeBook.clear();
			} else {
				restoreRecipeBook();
			}
			
			restoreSkills();
			giveSkills();
			regiveTemporarySkills();
			addSkills();
			_inventory.updateRuneBonus();
			getDisabledSkills().clear(); // Prevents some issues when changing between subclases that shares skills
			restoreEffects();
			updateEffectIcons();
			sendPacket(new EtcStatusUpdate(this));
			QuestState st = getQuestState("Q422_RepentYourSins"); // If player has quest "Repent Your Sins", remove it
			
			if (st != null)
				st.exitQuest(true);
			
			for (int i = 0; i < 3; i++)
				_henna[i] = null;
			
			restoreHenna();
			refreshHennaList();
			
			if (getCurrentHp() > getMaxHp())
				setCurrentHp(getMaxHp());

			if (getCurrentMp() > getMaxMp())
				setCurrentMp(getMaxMp());
			
			if (getCurrentCp() > getMaxCp())
				setCurrentCp(getMaxCp());
			
			refreshOverloaded();
			refreshExpertisePenalty();
			broadcastUserInfo();
			setExpBeforeDeath(0); // Clear resurrect xp calculation
			disableAutoShotsAll(); // Remove shot automation
			ItemInstance item = getActiveWeaponInstance(); // Discharge any active shots
			
			if (item != null)
				item.unChargeAllShots();
			
			_shortCuts.restore();
			sendPacket(new ShortCutInit(this));
			broadcastPacket(new SocialAction(this, 15));
			sendPacket(new SkillCoolTime(this));
			return true;
		} finally {
			_subclassLock.unlock();
		}
	}
	
	/** Set the template of the Player. @param Id The Identifier of the PlayerTemplate to set to the Player */
	public void setClassId(int Id) {
		if (!_subclassLock.tryLock())
			return;

		try {
			int academyLevel = getLvlJoinedAcademy();

			if (academyLevel != 0 && hasClan() && ClassId.VALUES[Id].level() >= 1) {
				_clan.addReputationScore(10000 / academyLevel);
				setLvlJoinedAcademy(0);
				_clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()), SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED).addString(getName()));
				_clan.removeClanMember(getObjectId(), 0); // Oust pledge member from the academy, because he has finished his 2nd class transfer.
				sendPacket(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED);
				addItem(8181, 1); // receive graduation gift : academy circlet
			}

			if (isSubClassActive())
				_subClasses.get(_classIndex).setClassId(Id);

			setClassTemplate(Id);

			if (getClassLevel() == 3)
				sendPacket(SystemMessageId.THIRD_CLASS_TRANSFER);
			else
				sendPacket(SystemMessageId.CLASS_TRANSFER);

			if (isInParty()) // Update class icon in party and clan
				_party.broadcastPacket(new PartySmallWindowUpdate(this));

			if (hasClan())
				_clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));

			if (Config.AUTO_LEARN_SKILLS)
				rewardSkills();
			
			broadcastPacket(new MagicSkillUse(this, this, Constant.GET_CLASS_SKILL, 1, 1000, 0));
		} finally {
			_subclassLock.unlock();
		}
	}
	
	/** @return the base PlayerTemplate link to the Player. */
	public final PlayerTemplate getBaseTemplate() {
		return PlayerData.getInstance().getTemplate(_baseClass);
	}

	public void setTemplate(ClassId newclass) {
		super.setTemplate(PlayerData.getInstance().getTemplate(newclass));
	}
	
	public ClassRace getRace() {
		return isSubClassActive() ? getBaseTemplate().getRace() : getTemplate().getRace();
	}
	
	public boolean isDwarfRace() {
		return getRace() == ClassRace.DWARF;
	}
	
	public String getRaceName() {
		return getRace().getName();
	}
	
	/** @return The ClassId object of the Player contained in PlayerTemplate. */
	public ClassId getClassId() {
		return getTemplate().getClassId();
	}
	
	/** @return The String of the Player class name from PlayerTemplate. */
	public String getClassName() {
		return getTemplate().getClassName();
	}
	
	/** Returns the level of the {@link ClassId}. @return int : The level (-1=dummy, 0=base, 1=1st class, 2=2nd class, 3=3rd class) */
	public int getClassLevel() {
		return getClassId().level();
	}
	
	/**
	 * A newbie is a player reaching level 1. He isn't considered newbie at lvl 10+. Since IL newbie isn't anymore the first character of an account reaching that state, but any.
	 * @return True if newbie.
	 */
	public boolean isNewbie() {
		return isClassLevelLow() && isPlayerLevelInRange(1, 10);
	}
	
	public boolean isClassLevelLow() {
		return getClassLevel() < 1;
	}
	
	/** @return True if the Player is a Mage (based on class templates). */
	public boolean isMageClass() {
		return getClassId().getType() != ClassType.FIGHTER;
	}
	
	public int getClassIdId() {
		return getClassId().getId();
	}
	
	public String getArchetypeName() {
		return getClassId().getArchetype().getName();
	}
	
	public int getArchetypeSkill() {
		return getClassId().getArchetype().getSkillId();
	}
	
	private int _classIndex;
	
	public boolean isSubClassActive() {
		return _classIndex > 0;
	}
	
	public int getClassIndex() {
		return _classIndex;
	}
	
	private final Map<Integer, SubClass> _subClasses = new ConcurrentSkipListMap<>();
	
	public Map<Integer, SubClass> getSubClasses() {
		return _subClasses;
	}
	
	private final ReentrantLock _subclassLock = new ReentrantLock();
	
	public boolean isLocked() {
		return _subclassLock.isLocked();
	}
	
	/**
	 * 1. Add the specified class ID as a subclass (up to the maximum number of <b>three</b>) for this character.<BR>
	 * 2. This method no longer changes the active _classIndex of the player. This is only done by the calling of setActiveClass() method as that should be the only way to do so.
	 */
	public boolean addSubClass(int classId, int classIndex) {
		if (!_subclassLock.tryLock())
			return false;
		
		try {
			if (_subClasses.size() == 3 || classIndex == 0 || _subClasses.containsKey(classIndex))
				return false;
			
			final SubClass subclass = new SubClass(classId, classIndex);
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(ADD_CHAR_SUBCLASS)) {
				ps.setInt(1, getObjectId());
				ps.setInt(2, subclass.getClassId());
				ps.setLong(3, subclass.getExp());
				ps.setInt(4, subclass.getSp());
				ps.setInt(5, subclass.getLevel());
				ps.setInt(6, subclass.getClassIndex());
				ps.execute();
			} catch (Exception e) {
				LOGGER.error("Couldn't add subclass for {}.", e, getName());
				return false;
			}
			
			_subClasses.put(subclass.getClassIndex(), subclass);
			PlayerData.getInstance().getTemplate(classId).getSkills().stream()
				.filter(s -> s.getMinLvl() <= ClassMaster.MIN_LVL)
				.collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL)))
				.forEach((i, s) -> storeSkill(s.get().getSkill(), classIndex));
			return true;
		} finally {
			_subclassLock.unlock();
		}
	}
	
	/**
	 * 1. Completely erase all existance of the subClass linked to the classIndex.<BR>
	 * 2. Send over the newClassId to addSubClass()to create a new instance on this classIndex.<BR>
	 * 3. Upon Exception, revert the player to their BaseClass to avoid further problems.<BR>
	 */
	public boolean modifySubClass(int classIndex, int newClassId) {
		if (!_subclassLock.tryLock())
			return false;

		try {
			try (Connection con = L2DatabaseFactory.getInstance().getConnection()) {
				PreparedStatement ps = con.prepareStatement(DELETE_CHAR_HENNAS); // Remove all henna info stored for this sub-class.
				ps.setInt(1, getObjectId());
				ps.setInt(2, classIndex);
				ps.execute();
				ps.close();
				
				ps = con.prepareStatement(DELETE_CHAR_SHORTCUTS); // Remove all shortcuts info stored for this sub-class.
				ps.setInt(1, getObjectId());
				ps.setInt(2, classIndex);
				ps.execute();
				ps.close();
				
				ps = con.prepareStatement(DELETE_SKILL_SAVE); // Remove all effects info stored for this sub-class.
				ps.setInt(1, getObjectId());
				ps.setInt(2, classIndex);
				ps.execute();
				ps.close();

				ps = con.prepareStatement(DELETE_CHAR_SKILLS); // Remove all skill info stored for this sub-class.
				ps.setInt(1, getObjectId());
				ps.setInt(2, classIndex);
				ps.execute();
				ps.close();

				ps = con.prepareStatement(DELETE_CHAR_SUBCLASS); // Remove all basic info stored about this sub-class.
				ps.setInt(1, getObjectId());
				ps.setInt(2, classIndex);
				ps.execute();
				ps.close();
			} catch (Exception e) {
				LOGGER.error("Couldn't modify subclass for {} to class index {}.", e, getName(), classIndex);
				_subClasses.remove(classIndex); // This must be done in order to maintain data consistency.
				return false;
			}
			
			_subClasses.remove(classIndex);
		} finally {
			_subclassLock.unlock();
		}
		
		return addSubClass(newClassId, classIndex);
	}
	
	private PcAppearance _appearance;
	
	public final PcAppearance getAppearance() {
		return _appearance;
	}
	
	public String getGenderName() {
		return getAppearance().getSex().getName();
	}
	
	private byte _pvpFlag;
	
	public boolean isFlagged() {
		return _pvpFlag > 0;
	}
	
	public void setPvpFlag(int pvpFlag) {
		_pvpFlag = (byte) pvpFlag;
	}

	public void updatePvPFlag(int value) {
		if (_pvpFlag == value)
			return;

		setPvpFlag(value);
		sendUserInfo();
		
		if (getPet() != null)
			sendPacket(new RelationChanged(getPet(), getRelation(this), false));
		
		broadcastRelationsChanges();
	}
	
	private byte _siegeState;
	
	/** Set the siege state of the Player. @param siegeState 1 = attacker, 2 = defender, 0 = not involved */
	public void setSiegeState(byte siegeState) {
		_siegeState = siegeState;
	}

	public byte getSiegeState() {
		return _siegeState;
	}
	
	private int _karma;
	
	/** Set the Karma of the Player and send StatusUpdate (broadcast). */
	public void setKarma(int karma) {
		if (karma < 0)
			karma = 0;
		
		if (_karma > 0 && karma == 0) {
			sendUserInfo();
			broadcastRelationsChanges();
		}
		
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1).addNumber(karma)); // send message with new karma value
		_karma = karma;
		broadcastKarma();
	}
	
	public void updateKarmaLoss(long exp) {
		if (!isCursedWeaponEquipped() && hasKarma()) {
			int karmaLost = Formulas.calculateKarmaLost(getLevel(), exp);

			if (karmaLost > 0)
				setKarma(_karma - karmaLost);

			if (!hasKarma() && karmaLost > 0)
				karmaLost = 0;

			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.KARMA_DECREASE_S1).addNumber(karmaLost));
		}
	}
	
	/** Send StatusUpdate packet with Karma to the Player and all Player to inform (broadcast). */
	public void broadcastKarma() {
		statusUpdate(this, StatusUpdate.KARMA, _karma);

		if (getPet() != null)
			sendPacket(new RelationChanged(getPet(), getRelation(this), false));

		broadcastRelationsChanges();
	}
	
	private int _pvpKills;
	
	public int getPvpKills() {
		return _pvpKills;
	}
	
	/** Set PvP Kills of the Player (number of player killed during a PvP). */
	public void setPvpKills(int pvpKills) {
		_pvpKills = pvpKills;
	}

	public void increasePvpKills() {
		_pvpKills++;
	}
	
	private int _pkKills;
	
	public int getPkKills() {
		return _pkKills;
	}

	public void setPkKills(int pkKills) {
		_pkKills = pkKills;
	}
	
	public void increasePkKills() {
		_pkKills++;
	}
	
	private L2GameClient _client;

	/** @return The client owner of this char. */
	public L2GameClient getClient() {
		return _client;
	}

	public void setClient(L2GameClient client) {
		_client = client;
	}
	
	public String getAccountName() {
		return getClient() == null ? getAccountNamePlayer() : _client.getAccountName();
	}
	
	public String getIP() {
		return getClient().getConnection() == null ? "N/A IP" : getClient().getConnection().getInetAddress().getHostAddress();
	}
	
	/**
	 * Close the active connection with the {@link L2GameClient} linked to this {@link Player}.
	 * @param closeClient : If true, the client is entirely closed. Otherwise, the client is sent back to login.
	 */
	public void logout(boolean closeClient) {
		final L2GameClient client = _client;

		if (client == null)
			return;
		
		if (client.isDetached())
			client.cleanMe(true);
		else if (!client.getConnection().isClosed())
			client.close((closeClient) ? LeaveWorld.STATIC_PACKET : ServerClose.STATIC_PACKET);
	}
	
	private final Map<Integer, String> _chars = new HashMap<>();
	
	public Map<Integer, String> getAccountChars() {
		return _chars;
	}
	
	private String _accountName;
	
	public String getAccountNamePlayer() {
		return _accountName;
	}

	private long _deleteTimer;
	
	public long getDeleteTimer() {
		return _deleteTimer;
	}
	
	public void setDeleteTimer(long ms) {
		_deleteTimer = ms;
	}
	
	/** Skills */
	public boolean hasDwarvenCraft() {
		return hasSkill(L2Skill.SKILL_CREATE_DWARVEN);
	}

	public boolean hasCommonCraft() {
		return hasSkill(L2Skill.SKILL_CREATE_COMMON);
	}
	
	/** Method used by regular leveling system. Reward the {@link Player} with autoGet skills only, or if Config.AUTO_LEARN_SKILLS is activated, with all available skills. */
	public void giveSkills() {
		if (Config.AUTO_LEARN_SKILLS) {
			rewardSkills();
		} else {
			for (GeneralSkillNode skill : getAvailableAutoGetSkills()) // We reward all autoGet skills to this player, but don't store any on database.
				addSkill(skill.getSkill(), false);

			removeLuckySkill();
			removeInvalidSkillsAndSendSkillList();
		}
	}
	
	/** Method used by admin commands, Config.AUTO_LEARN_SKILLS or class master. Reward the {@link Player} with all available skills, being autoGet or general skills. */
	public void rewardSkills() {
		for (GeneralSkillNode skill : getAllAvailableSkills()) // We reward all skills to the players, but don't store autoGet skills on the database.
			addSkill(skill.getSkill(), skill.getCost() != 0);

		removeLuckySkill();
		removeInvalidSkillsAndSendSkillList();
	}
	
	private void removeLuckySkill() {
		if (getLevel() >= 10 && hasSkill(L2Skill.SKILL_LUCKY))
			removeSkill(L2Skill.SKILL_LUCKY, false);
	}
	
	private void removeInvalidSkillsAndSendSkillList() {
		removeInvalidSkills();
		sendSkillList();
	}
	
	/**
	 * Delete all invalid {@link L2Skill}s for this {@link Player}.<br>
	 * A skill is considered invalid when the level of obtention of the skill is superior to 9 compared to player level (expertise skill obtention level is compared to player level without any penalty).<br>
	 * It is then either deleted, or level is refreshed.
	 */
	private void removeInvalidSkills() {
		if (getSkills().isEmpty())
			return;

		final Set<Integer> classSkillIds = getTemplate().getSkills().stream()
			.map(GeneralSkillNode::getId)
			.collect(Collectors.toSet());

		int playerLevel = getLevel();
		final Map<Integer, Optional<GeneralSkillNode>> availableSkills = getTemplate().getSkills().stream() // Retrieve the player template skills, based on actual level (+9 for regular skills, unchanged for expertise).
			.filter(s -> s.getMinLvl() <= playerLevel + ((s.getId() == L2Skill.SKILL_EXPERTISE) ? 0 : 9))
			.collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL)));
		
		for (L2Skill skill : getSkills().values()) {
			int skillId = skill.getId();

			if (!classSkillIds.contains(skillId)) // Bother only with skills existing on template (spare temporary skills, items skills, etc).
				continue;

			final Optional<GeneralSkillNode> tempSkill = availableSkills.get(skillId); // The known skill doesn't exist on available skills, we drop existing skill.
			
			if (tempSkill == null) {
				removeSkill(skillId, true);
				continue;
			}

			final GeneralSkillNode availableSkill = tempSkill.get(); // Retrieve the skill.
			final int maxLevel = SkillTable.getInstance().getMaxLevel(skillId); // Retrieve the max level for enchant scenario.

			if (skill.getLevel() > maxLevel) { // Case of enchanted skills.
				if ((playerLevel < 76 || availableSkill.getValue() < maxLevel) && skill.getLevel() > availableSkill.getValue()) // Player level is inferior to 76, or available skill is a good candidate.
					addSkill(availableSkill.getSkill(), true);
			} else if (skill.getLevel() > availableSkill.getValue()) {
				addSkill(availableSkill.getSkill(), true);
			}
		}
	}
	
	public void removeSkills(int... ids) {
		removeSkills(false, ids);
	}
	
	public void removeSkills(boolean save, int... ids) {
		for (int id : ids)
			removeSkill(id, save);
	}
	
	/** Regive all skills which aren't saved to database, like Noble, Hero, Clan Skills. !!! Do not call this on enterworld or char load. !!! */
	public void regiveTemporarySkills() {
		if (isNoble())
			setNoble(true, false); // Add Noble Skills if Noble.

		if (isHero())
			setHero(true); // Add Hero Skills if Hero.

		if (hasClan()) {
			getClan().addSkillEffects(this); // Add Clan Skills.
			
			if (getClan().getLevel() >= Config.MINIMUM_CLAN_LEVEL && isClanLeader())
				addSiegeSkills();
		}
		
		getInventory().reloadEquippedItems(); // Reload passive skills from armors / jewels / weapons
		restoreDeathPenaltyBuffLevel(); // Add Death Penalty Buff Level
	}
	
	public void addSiegeSkills() {
		for (L2Skill sk : SkillTable.getInstance().getSiegeSkills(isNoble()))
			addSkill(sk, false);
	}
	
	public void removeSiegeSkills() {
		for (L2Skill sk : SkillTable.getInstance().getSiegeSkills(isNoble()))
			removeSkill(sk.getId(), false);
	}
	
	/** @return a {@link List} of all available autoGet {@link GeneralSkillNode}s <b>of maximal level</b> for this {@link Player}. */
	public List<GeneralSkillNode> getAvailableAutoGetSkills() {
		final List<GeneralSkillNode> result = new ArrayList<>();

		getTemplate().getSkills().stream()
			.filter(s -> s.getMinLvl() <= getLevel() && s.getCost() == 0)
			.collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL)))
			.forEach((i, s) -> {
				if (getSkillLevel(i) < s.get().getValue())
					result.add(s.get());
		});

		return result;
	}
	
	/** @return a {@link List} of available {@link GeneralSkillNode}s (only general) for this {@link Player}. */
	public List<GeneralSkillNode> getAvailableSkills() {
		final List<GeneralSkillNode> result = new ArrayList<>();

		getTemplate().getSkills().stream()
			.filter(s -> s.getMinLvl() <= getLevel() && s.getCost() != 0)
			.forEach(s -> {
				if (getSkillLevel(s.getId()) == s.getValue() - 1)
					result.add(s);
		});

		return result;
	}

	public void rewardSkillsNew() {
		List<GeneralSkillNode> skills = getAvailableToBuySkills();

		if (skills.isEmpty()) {
			int minLevel = getRequiredLevelForNextSkill();
			sendPacket(minLevel > 0 ? SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1).addNumber(minLevel) : SystemMessage.getSystemMessage(SystemMessageId.NO_MORE_SKILLS_TO_LEARN));
			return;
		}

		int priceSp = calculateGoldForAllAvailableSkills(skills);
		int priceGold = priceSp;

		if (!canAfford(priceGold, priceSp)) {
			sendMessage("To continue you need: " + MathUtil.formatNumber(priceGold) + " Gold and " + MathUtil.formatNumber(priceSp) + " SP.");
			return;
		}

		skills.forEach(skill -> addSkill(skill.getSkill(), skill.getCost() != 0));
		useResources(priceGold, priceSp);
		sendMessage("You have acquired all of the new skills.");
	}

	public List<GeneralSkillNode> getAvailableToBuySkills() {
		Map<Integer, GeneralSkillNode> skillMap = new HashMap<>();
		int playerLevel = getLevel();
		boolean higherThanMinLevel = verifyPlayerLevel(ClassMaster.MIN_LVL, false);

		for (GeneralSkillNode s : getTemplate().getSkills()) {
			int skillId = s.getId();
			int skillValue = s.getValue();
			int skillLevel = getSkillLevel(skillId);

			if (s.getMinLvl() <= playerLevel && s.getCost() != 0 && skillLevel < skillValue && (!higherThanMinLevel || (skillLevel >= 1 && higherThanMinLevel))) {
				GeneralSkillNode existingSkill = skillMap.get(skillId);
				
				if (existingSkill == null || existingSkill.getValue() < skillValue)
					skillMap.put(skillId, s);
			}
		}

		return new ArrayList<>(skillMap.values());
	}

	public int calculateGoldForSkills(List<GeneralSkillNode> skills, boolean available) {
		int totalGoldCost = 0;
		
		for (GeneralSkillNode availableSkill : skills) {
			int skillId = availableSkill.getId();
			int skillLevel = getSkillLevel(skillId);

			for (int level = 1 + (available ? skillLevel : 0); level <= (available ? availableSkill.getValue() : skillLevel); level++) {
				GeneralSkillNode skillNode = getTemplate().findSkill(skillId, level);
						
				if (skillNode != null)
					totalGoldCost += skillNode.getCost();
			}
		}

		return totalGoldCost;
	}

	public int calculateGoldForAllAvailableSkills(List<GeneralSkillNode> skills) {
		return calculateGoldForSkills(skills, true);
	}
	
	public int calculateGoldSpentOnLearnedSkills(List<GeneralSkillNode> skills) {
		return calculateGoldForSkills(skills, false);
	}

	/** @return a {@link List} of all available {@link GeneralSkillNode}s (being general or autoGet) <b>of maximal level</b> for this {@link Player}. */
	public List<GeneralSkillNode> getAllAvailableSkills() {
		final List<GeneralSkillNode> result = new ArrayList<>();

		getTemplate().getSkills().stream()
			.filter(s -> s.getMinLvl() <= getLevel())
			.collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL)))
			.forEach((i, s) -> {
				if (getSkillLevel(i) < s.get().getValue())
					result.add(s.get());
		});

		return result;
	}
	
	/**
	 * Retrieve next lowest level skill to learn, based on current player level and skill sp cost.
	 * @return the required level for next {@link GeneralSkillNode} to learn for this {@link Player}.
	 */
	public int getRequiredLevelForNextSkill() {
		return getTemplate().getSkills().stream()
			.filter(s -> s.getMinLvl() > getLevel() && s.getCost() != 0)
			.min(COMPARE_SKILLS_BY_MIN_LVL)
			.map(s -> s.getMinLvl())
			.orElse(0);
	}
	
	public L2Skill getSkillInfo(int id, int level) {
		return SkillTable.getInstance().getInfo(id, level);
	}
	
	public L2Skill getSkillInfo(int id) {
		return getSkillInfo(id, 1);
	}
	
	public void addTemporarySkill(int skillId, int skillLevel) {
		addSkill(getSkillInfo(skillId, skillLevel), false);
	}
	
	public void addSkillFirstLevel(int skillId) {
		addTemporarySkill(skillId, 1);
	}
	
	public void addSkillSendMsg(L2Skill skill) {
		addSkill(skill, true);
		sendMessage("Your skill " + skill.getName() + " is now level " + skill.getLevel() + ".");
	}
	
	public void addSkillNoStore(L2Skill skill) {
		addSkill(skill, false);
	}
	
	/**
	 * Add a {@link L2Skill} and its Func objects to the calculator set of the {@link Player}.<BR>
	 * Replace or add oldSkill by newSkill (only if oldSkill is different than newSkill)
	 * If an old skill has been replaced, remove all its Func objects of Creature calculator set
	 * Add Func objects of newSkill to the calculator set of the Creature
	 * @param newSkill : The skill to add.
	 * @param store : If true, we save the skill on database.
	 * @return true if the skill has been successfully added.
	 */
	public boolean addSkill(L2Skill newSkill, boolean store) {
		if (newSkill == null) // New skill is null, abort.
			return false;

		final L2Skill oldSkill = getSkills().get(newSkill.getId()); // Search the old skill. We test if it's different than the new one. If yes, we abort the operation.
		
		if (oldSkill != null && oldSkill.equals(newSkill))
			return false;

		getSkills().put(newSkill.getId(), newSkill); // The 2 skills were different (or old wasn't existing). We can refresh the map.
		
		if (oldSkill != null) { // If an old skill has been replaced, remove all its Func objects
			if (oldSkill.triggerAnotherSkill())
				removeSkill(oldSkill.getTriggeredId(), false); // if skill came with another one, we should delete the other one too.

			removeStatsByOwner(oldSkill);
		}
		
		addStatFuncs(newSkill.getStatFuncs(this)); // Add Func objects of newSkill to the calculator set of the Creature
		
		if (oldSkill != null && getChanceSkills() != null)
			removeChanceSkill(oldSkill.getId()); // Test and delete chance skill if found.

		if (newSkill.isChance())
			addChanceTrigger(newSkill); // If new skill got a chance, trigger it.

		if (store)
			storeSkill(newSkill, -1); // Add or update the skill in the database.

		return true;
	}
	
	/**
	 * Remove a {@link L2Skill} from this {@link Player}. If parameter store is true, we also remove it from database and update shortcuts.
	 * @param skillId : The skill identifier to remove.
	 * @param store : If true, we delete the skill from database.
	 * @param removeEffect : If true, we remove the associated effect if existing.
	 * @return the L2Skill removed or null if it couldn't be removed.
	 */
	public L2Skill removeSkill(int skillId, boolean store, boolean removeEffect) {
		final L2Skill oldSkill = getSkills().remove(skillId); // Remove the skill from the Creature _skills

		if (oldSkill == null)
			return null;

		if (oldSkill.triggerAnotherSkill() && oldSkill.getTriggeredId() > 0) // this is just a fail-safe againts buggers and gm dummies...
			removeSkill(oldSkill.getTriggeredId(), false);

		if (getLastSkillCast() != null && isCastingNow() && skillId == getLastSkillCast().getId()) // Stop casting if this skill is used right now
			abortCast();

		L2Skill lastSimultaneousSkillCast = getLastSimultaneousSkillCast();

		if (lastSimultaneousSkillCast != null && isCastingSimultaneouslyNow() && skillId == lastSimultaneousSkillCast.getId())
			abortCast();

		if (removeEffect) { // Remove all its Func objects from the Creature calculator set
			removeStatsByOwner(oldSkill);
			stopSkillEffects(skillId);
		}
		
		if (oldSkill.isChance() && getChanceSkills() != null)
			removeChanceSkill(skillId);

		if (store) {
			try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement(DELETE_SKILL_FROM_CHAR)) {
				ps.setInt(1, skillId);
				ps.setInt(2, getObjectId());
				ps.setInt(3, getClassIndex());
				ps.execute();
			} catch (Exception e) {
				LOGGER.error("Couldn't delete player skill.", e);
			}
			
			if (!oldSkill.isPassive()) { // Don't busy with shortcuts if skill was a passive skill.
				for (L2ShortCut sc : getAllShortCuts())
					if (sc != null && sc.getId() == skillId && sc.getType() == L2ShortCut.TYPE_SKILL)
						deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
		
		return oldSkill;
	}
	
	/**
	 * Remove a {@link L2Skill} from this {@link Player}. If parameter store is true, we also remove it from database and update shortcuts.
	 * @param skillId : The skill identifier to remove.
	 * @param store : If true, we delete the skill from database.
	 * @return the L2Skill removed or null if it couldn't be removed.
	 */
	public L2Skill removeSkill(int skillId, boolean store) {
		L2Skill skill = removeSkill(skillId, store, true);

		if (skill != null)
			sendMessage("Your skill " + skill.getName() + " has been removed.");

		return skill;
	}
	
	public void removeSkillNoStore(int skillId) {
		removeSkill(skillId, false);
	}
	
	public void removeSkillNoStore(L2Skill skill) {
		if (skill != null)
			removeSkillNoStore(skill.getId());
	}
	
	/**
	 * Insert or update a {@link Player} skill in the database.<br>
	 * If newClassIndex > -1, the skill will be stored with that class index, not the current one.
	 * @param skill : The skill to add or update (if updated, only the level is refreshed).
	 * @param classIndex : The current class index to set, or current if none is found.
	 */
	private void storeSkill(L2Skill skill, int classIndex) {
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement ps = con.prepareStatement(ADD_OR_UPDATE_SKILL)) {
			ps.setInt(1, getObjectId());
			ps.setInt(2, skill.getId());
			ps.setInt(3, skill.getLevel());
			ps.setInt(4, (classIndex > -1) ? classIndex : _classIndex);
			ps.executeUpdate();
		} catch (Exception e) {
			LOGGER.error("Couldn't store player skill.", e);
		}
	}
	
	/** Restore all skills from database for this {@link Player} and feed getSkills(). */
	public void restoreSkills() {
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR)) {
			ps.setInt(1, getObjectId());
			ps.setInt(2, getClassIndex());

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					addTemporarySkill(rs.getInt("skill_id"), rs.getInt("skill_level"));
				}
			}
		} catch (Exception e) {
			LOGGER.error("Couldn't restore player skills.", e);
		}
	}
	
	/** Retrieve from the database all skill effects of this Player and add them to the player. */
	public void restoreEffects() {
		try (Connection con = L2DatabaseFactory.getInstance().getConnection()) {
			PreparedStatement statement = con.prepareStatement(RESTORE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			ResultSet rset = statement.executeQuery();
			
			while (rset.next()) {
				int effectCount = rset.getInt("effect_count");
				int effectCurTime = rset.getInt("effect_cur_time");
				long reuseDelay = rset.getLong("reuse_delay");
				long systime = rset.getLong("systime");
				int restoreType = rset.getInt("restore_type");
				final L2Skill skill = getSkillInfo(rset.getInt("skill_id"), rset.getInt("skill_level"));
				
				if (skill == null)
					continue;
				
				final long remainingTime = systime - System.currentTimeMillis();
				
				if (remainingTime > 10) {
					disableSkill(skill, remainingTime);
					addTimeStamp(skill, reuseDelay, systime);
				}
				
				// Restore Type 1 The remaning skills lost effect upon logout but were still under a high reuse delay.
				if (restoreType > 0)
					continue;
				
				// Restore Type 0 These skills were still in effect on the character upon logout. Some of which were self casted and might still have a long reuse delay which also is restored.
				if (skill.hasEffects()) {
					final Env env = new Env();
					env.setCharacter(this);
					env.setTarget(this);
					env.setSkill(skill);
					
					for (EffectTemplate et : skill.getEffectTemplates()) {
						final L2Effect ef = et.getEffect(env);
						
						if (ef != null) {
							ef.setCount(effectCount);
							ef.setFirstTime(effectCurTime);
							ef.scheduleEffect();
						}
					}
				}
			}
			
			rset.close();
			statement.close();
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			LOGGER.error("Couldn't restore effects.", e);
		}
	}
	
	private void storeEffect(boolean storeEffects) {
		if (!Config.STORE_SKILL_COOLTIME)
			return;

		try (Connection con = L2DatabaseFactory.getInstance().getConnection()) {
			try (PreparedStatement ps = con.prepareStatement(DELETE_SKILL_SAVE)) { // Delete all current stored effects for char to avoid dupe
				ps.setInt(1, getObjectId());
				ps.setInt(2, getClassIndex());
				ps.executeUpdate();
			}

			int buff_index = 0;
			final List<Integer> storedSkills = new ArrayList<>();

			try (PreparedStatement ps = con.prepareStatement(ADD_SKILL_SAVE)) {
				if (storeEffects) { // Store all effect data along with calculated remaining reuse delays for matching skills. 'restore_type'= 0.
					for (L2Effect effect : getAllEffects()) {
						if (effect == null)
							continue;

						switch (effect.getEffectType()) {
							case HEAL_OVER_TIME:
							case COMBAT_POINT_HEAL_OVER_TIME:
								continue;
								
							default:
								break;
						}
						
						final L2Skill skill = effect.getSkill();

						if (storedSkills.contains(skill.getReuseHashCode()))
							continue;

						storedSkills.add(skill.getReuseHashCode());
						
						if (!effect.isHerbEffect() && effect.getInUse() && !skill.isToggle()) {
							ps.setInt(1, getObjectId());
							ps.setInt(2, skill.getId());
							ps.setInt(3, skill.getLevel());
							ps.setInt(4, effect.getCount());
							ps.setInt(5, effect.getTime());
							
							final Timestamp t = _reuseTimeStamps.get(skill.getReuseHashCode());
							
							if (t != null && t.hasNotPassed()) {
								ps.setLong(6, t.getReuse());
								ps.setDouble(7, t.getStamp());
							} else {
								ps.setLong(6, 0);
								ps.setDouble(7, 0);
							}
							
							ps.setInt(8, 0);
							ps.setInt(9, getClassIndex());
							ps.setInt(10, ++buff_index);
							ps.addBatch(); // Add SQL
						}
					}
				}
				
				// Store the reuse delays of remaining skills which lost effect but still under reuse delay. 'restore_type' 1.
				for (Map.Entry<Integer, Timestamp> entry : _reuseTimeStamps.entrySet()) {
					final int hash = entry.getKey();

					if (storedSkills.contains(hash))
						continue;

					final Timestamp t = entry.getValue();
					
					if (t != null && t.hasNotPassed()) {
						storedSkills.add(hash);
						
						ps.setInt(1, getObjectId());
						ps.setInt(2, t.getId());
						ps.setInt(3, t.getValue());
						ps.setInt(4, -1);
						ps.setInt(5, -1);
						ps.setLong(6, t.getReuse());
						ps.setDouble(7, t.getStamp());
						ps.setInt(8, 1);
						ps.setInt(9, getClassIndex());
						ps.setInt(10, ++buff_index);
						ps.addBatch(); // Add SQL
					}
				}
				
				ps.executeBatch(); // Execute SQLs
			}
		} catch (Exception e) {
			LOGGER.error("Couldn't store player effects.", e);
		}
	}

	private boolean checkUseMagicConditions(L2Skill skill, boolean forceUse, boolean dontMove) {
		// *** Check Player State. ***
		L2SkillType sklType = skill.getSkillType();

		if (!isPlayerStateValidForCasting(skill, sklType))
			return false;
		
		// *** Check Target. ***
		SkillTargetType sklTargetType = skill.getTargetType();
		Location worldPosition = getCurrentSkillWorldPosition();
		WorldObject target = getSkillTarget(sklTargetType, worldPosition);
		
		if (target == null) {
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if ((target instanceof Door && (!((Door) target).isAutoAttackable(this) || (((Door) target).isUnlockable() && sklType != L2SkillType.UNLOCK))) ||  // Siege Doors Only Hittable During Siege or Unlockable Doors.
			(isInDuel() && target instanceof Playable && target.getActingPlayer().getDuelId() != getDuelId()) ||  // Are the Target and the Player in the Same Duel.
			(target.isInstanceOfPlayer() && ((Player) target).isAlikeDead()) ||
			(isSameFaction(((Creature) target).getFactionId()))) { // Cannot Attack Same Faction NPC.
			sendTargetError();
			return false;
		}
		
		// *** Check Skill Availability. ***
		if (skill.isSiegeSummonSkill()) {
			final Siege siege = CastleManager.getInstance().getActiveSiege(this);

			if (siege == null || !siege.checkSide(getClan(), SiegeSide.ATTACKER) || (isInSiege() && isInsideZone(ZoneId.CASTLE))) {
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_CALL_PET_FROM_THIS_LOCATION));
				return false;
			}
		}
		
		// *** Check Casting Conditions. ***
		if (!skill.checkCondition(this, target, false)) {
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// *** Check Skill Type. ***
		if (skill.isOffensive()) {
			if (!canCastOffensiveSkill(target))
				return false;
			
			if (!target.isAutoAttackable(this) && !forceUse && !canForceAttackTarget(sklTargetType)) // Check if a Forced ATTACK is in Progress on Non-Attackable Target.
				return false;

			if (dontMove) { // Check if the Target is in the Skill Cast Range.
				int radius = (int) (skill.getCastRange() + getCollisionRadius());

				if ((sklTargetType == SkillTargetType.TARGET_GROUND && !isInsideRadius(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), radius, false, false)) || skill.getCastRange() > 0 && !isInsideRadius(target, radius, false, false)) {
						sendPacket(SystemMessageId.TARGET_TOO_FAR);
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
				}
			}
		} else if (!forceUse && (target instanceof Monster || target instanceof SiegeNpcBase)) { // Check if the Skill is Defensive.
			if (!isValidDefensiveTarget(sklType, sklTargetType)) // Check if the Target is a Monster and if Force Attack is Set. If Not Then We Don't Want to Cast.
				return false;
		}
		
		if ((sklType == L2SkillType.SPOIL || sklType == L2SkillType.DRAIN_SOUL) && !(target instanceof Monster)) {
			sendTargetError();
			return false;
		}
		
		if (!canTargetInPvp(skill, target, sklTargetType))
			return false;
		
		if ((sklTargetType == SkillTargetType.TARGET_HOLY && !canCastSealOfRule(CastleManager.getInstance().getCastle(this), false, skill, target)) || isSkillRestricted(target, sklType, skill)) {
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return false;
		}
		
		if (skill.getCastRange() > 0 && ((sklTargetType == SkillTargetType.TARGET_GROUND && !GeoEngine.getInstance().canSeeTarget(this, worldPosition)) || !GeoEngine.getInstance().canSeeTarget(this, target))) {
			sendPacket(SystemMessageId.CANT_SEE_TARGET);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		return true;
	}

	private boolean isSkillRestricted(WorldObject target, L2SkillType sklType, L2Skill skill) {
		switch(sklType) {
			case SIEGEFLAG: return !L2SkillSiegeFlag.checkIfOkToPlaceFlag(this, false);
			case STRSIEGEASSAULT: return !checkIfOkToUseStriderSiegeAssault(skill);
			case SUMMON_FRIEND: return !canSummon() || !isTargetSummonable(target);
			default: return false;
		}
	}

	private boolean isPlayerStateValidForCasting(L2Skill skill, L2SkillType sklType) {
		if (isDead() || isOutOfControl() || isFakeDeath()) {
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (isFishing() && (sklType != L2SkillType.PUMPING && sklType != L2SkillType.REELING && sklType != L2SkillType.FISHING)) {
			sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_NOW);
			return false;
		}
		
		if (isInObserverMode()) {
			sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return false;
		}
		
		if (isSitting()) {
			sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (skill.isToggle()) {
			L2Effect effect = getFirstEffect(skill.getId());
			
			if (effect != null) {
				if (skill.getId() != 60)
					effect.exit();

				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		return true;
	}

	private WorldObject getSkillTarget(SkillTargetType sklTargetType, Location worldPosition) {
		if (sklTargetType == SkillTargetType.TARGET_GROUND && worldPosition == null)
			return null;
		
		switch (sklTargetType) { // Target the Player if Skill Type is AURA, PARTY, CLAN or SELF.
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_AURA_UNDEAD:
			case TARGET_PARTY:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_GROUND:
			case TARGET_SELF:
			case TARGET_CORPSE_ALLY:
			case TARGET_AREA_SUMMON:
				return this;
				
			case TARGET_PET:
			case TARGET_SUMMON:
				return getPet();
				
			default:
				return getTarget();
		}
	}

	private boolean canCastOffensiveSkill(WorldObject target) {
		if (isInsidePeaceZone(this, target)) {
			sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if ((isInOlympiadMode() && !isOlympiadStart()) || (!target.isAttackable() && !getAccessLevel().allowPeaceAttack())) {
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		return true;
	}

	private boolean canForceAttackTarget(SkillTargetType sklTargetType) {
		switch (sklTargetType) {
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_AURA_UNDEAD:
			case TARGET_CLAN:
			case TARGET_ALLY:
			case TARGET_PARTY:
			case TARGET_SELF:
			case TARGET_GROUND:
			case TARGET_CORPSE_ALLY:
			case TARGET_AREA_SUMMON:
				return true;

			default:
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
		}
	}

	private boolean isValidDefensiveTarget(L2SkillType sklType, SkillTargetType sklTargetType) {
		switch (sklTargetType) {
			case TARGET_PET:
			case TARGET_SUMMON:
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_AURA_UNDEAD:
			case TARGET_CLAN:
			case TARGET_SELF:
			case TARGET_CORPSE_ALLY:
			case TARGET_PARTY:
			case TARGET_ALLY:
			case TARGET_CORPSE_MOB:
			case TARGET_AREA_CORPSE_MOB:
			case TARGET_GROUND:
				return true;

			default:
				return isUtilitySkill(sklType);
		}
	}

	private boolean isUtilitySkill(L2SkillType sklType) {
		switch (sklType) {
			case BEAST_FEED:
			case DELUXE_KEY_UNLOCK:
			case UNLOCK:
				return true;

			default:
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
		}
	}

	private boolean canTargetInPvp(L2Skill skill, WorldObject target, SkillTargetType sklTargetType) {
		switch (sklTargetType) { // Check if This is a PvP Skill and Target Isn't a Non-Flagged or Non-Karma Player.
			case TARGET_PARTY:
			case TARGET_ALLY: // For Such Skills, checkPvpSkill() is Called from L2Skill.getTargetList().
			case TARGET_CLAN: // For Such Skills, checkPvpSkill() is Called from L2Skill.getTargetList().
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_AURA_UNDEAD:
			case TARGET_GROUND:
			case TARGET_SELF:
			case TARGET_CORPSE_ALLY:
			case TARGET_AREA_SUMMON:
				break;

			default:
				if (!checkPvpSkill(target, skill) && !getAccessLevel().allowPeaceAttack()) {
					sendTargetError();
					return false;
				}
		}

		return true;
	}

	private void sendTargetError() { // sendErrorPackets
		sendPacket(SystemMessageId.INCORRECT_TARGET); // Send a System Message to the Player
		sendPacket(ActionFailed.STATIC_PACKET); // Send ActionFailed to the Player
	}
	
	public boolean checkIfOkToUseStriderSiegeAssault(L2Skill skill) {
		SystemMessage sm = getStriderSiegeAssaultError(skill);

		if (sm == null)
			return true;

		sendPacket(sm);
		return false;
	}
	
	private SystemMessage getStriderSiegeAssaultError(L2Skill skill) {
		if (!isRiding())
			return SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);

		if (!(getTarget() instanceof Door))
			return SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);

		final Siege siege = CastleManager.getInstance().getActiveSiege(this);
		
		if (siege == null || !siege.checkSide(getClan(), SiegeSide.ATTACKER))
			return SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);

		return null;
	}
	
	public boolean canCastSealOfRule(Castle castle, boolean isCheckOnly, L2Skill skill, WorldObject target) {
		SystemMessage sm = validateSealOfRule(castle, skill, target);

		if (sm != null) {
			sendPacket(sm);
			return false;
		}

		if (!isCheckOnly)
			castle.getSiege().announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.OPPONENT_STARTED_ENGRAVING), false);

		return true;
	}

	private SystemMessage validateSealOfRule(Castle castle, L2Skill skill, WorldObject target) {
		if (castle == null || castle.getCastleId() <= 0)
			return SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);

		if (!castle.isGoodArtifact(target))
			return SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);

		if (!castle.getSiege().isInProgress())
			return SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);

		if (!MathUtil.checkIfInRange(200, this, target, true))
			return SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);

		if (!isInsideZone(ZoneId.CAST_ON_ARTIFACT))
			return SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);

		if (!castle.getSiege().checkSide(getClan(), SiegeSide.ATTACKER))
			return SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill);

		return null;
	}
	
	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
	 * @param target WorldObject instance containing the target
	 * @param skill L2Skill instance with the skill being casted
	 * @return {@code false} if the skill is a pvpSkill and target is not a valid pvp target, {@code true} otherwise.
	 */
	public boolean checkPvpSkill(WorldObject target, L2Skill skill) {
		if (skill == null || target == null)
			return false;

		if (!(target instanceof Playable))
			return true;

		if (!skill.isDebuff() && !skill.isOffensive())
			return true;

		final Player targetPlayer = target.getActingPlayer();
		
		if (targetPlayer == null || this == target)
			return false;

		if (target.isInsideZone(ZoneId.PEACE))
			return false;

		if (!isSameFaction(targetPlayer))
			return true;

		if (isInDuel() && targetPlayer.isInDuel() && getDuelId() == targetPlayer.getDuelId())
			return true;

		if (isInOlympiadMode() && targetPlayer.isInOlympiadMode() && getOlympiadGameId() == targetPlayer.getOlympiadGameId())
			return true;

		final boolean isCtrlPressed = (getCurrentSkill() != null && getCurrentSkill().isCtrlPressed()) || (getCurrentPetSkill() != null && getCurrentPetSkill().isCtrlPressed());
		boolean isSkillConditionMet = skill.getEffectRange() > 0 && isCtrlPressed && getTarget() == target;
		boolean isDamageSkill = isSkillConditionMet && skill.isDamage(); // Check if skill can do dmg

		if (isInParty() && targetPlayer.isInParty() && (getParty().getLeader() == targetPlayer.getParty().getLeader() || (getParty().getCommandChannel() != null && getParty().getCommandChannel().containsPlayer(targetPlayer))))
			return isDamageSkill;

		if (bothInsideZone(targetPlayer, ZoneId.PVP)) // You can debuff anyone except party members while in an arena...
			return true;

		if (isCtrlPressed && isSameFaction(target.isInstanceOfPlayer() ? targetPlayer : target.getActingPlayer()) && (target.isInsideSiegeZone() || isInsideSiegeZone()))
			return false;

		if (hasClan() && targetPlayer.hasClan()) {
			if (getClan().isAtWarWith(targetPlayer.getClan().getClanId()) && targetPlayer.getClan().isAtWarWith(getClan().getClanId()))
				return isSkillConditionMet && skill.isAOE() || isCtrlPressed; // Check if skill can do dmg
			else if (getClanId() == targetPlayer.getClanId() || (getAllyId() > 0 && getAllyId() == targetPlayer.getAllyId()))
				return isDamageSkill;
		}

		if (!targetPlayer.isFlagged() && !targetPlayer.hasKarma()) // On retail, it is impossible to debuff a "peaceful" player.
			return isDamageSkill;

		return targetPlayer.isFlagged() || targetPlayer.hasKarma();
	}
	
	/** Stop all toggle-type effects */
	public final void stopAllToggles() {
		_effects.stopAllToggles();
	}
	
	public final void stopCubics() {
		if (getCubics() != null) {
			for (Cubic cubic : getCubics().values()) {
				cubic.stopAction();
				delCubic(cubic.getId());
			}

			broadcastUserInfo();
		}
	}
	
	public final void stopCubicsByOthers() {
		if (getCubics() != null) {
			boolean removed = false;
			
			for (Cubic cubic : getCubics().values()) {
				if (cubic.givenByOther()) {
					cubic.stopAction();
					delCubic(cubic.getId());
					removed = true;
				}
			}
			
			if (removed)
				broadcastUserInfo();
		}
	}
	
	public void addOrRemoveSkills(L2Skill[] skills, boolean add) {
		for (L2Skill skill : skills) {
			if (add)
				addSkill(skill, false);
			else
				removeSkill(skill.getId(), false);
		}
	}
	
	public void sendSkillList() {
		final ItemInstance formal = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		final boolean isWearingFormalWear = formal != null && formal.getItem().getBodyPart() == Item.SLOT_ALLDRESS;
		final SkillList sl = new SkillList();
		
		for (L2Skill s : getSkills().values()) {
			if (isWearingFormalWear) {
				sl.addSkill(s.getId(), s.getLevel(), s.isPassive(), true);
			} else {
				boolean isDisabled = false;

				if (hasClan())
					isDisabled = s.isClanSkill() && getClan().getReputationScore() < 0;

				if (isCursedWeaponEquipped()) { // Only Demonic skills are available
					isDisabled = !s.isDemonicSkill();
				} else if (isMounted()) {
					if (getMountType() == 1) // Only Strider skills are available
						isDisabled = !s.isStriderSkill();
					else if (getMountType() == 2) // Only Wyvern skills are available
						isDisabled = !s.isFlyingSkill();
				}

				sl.addSkill(s.getId(), s.getLevel(), s.isPassive(), isDisabled);
			}
		}
		
		sendPacket(sl);
	}
	
	public void removeAllSkills(boolean store) {
		for (L2Skill skill : getSkills().values())
			removeSkill(skill.getId(), store);
	}
	
	public void addGenderSkill() {
		addTemporarySkill(getGenderSkill(), 1);
	}
	
	public int getGenderSkill() {
		return getAppearance().getSex() == Sex.MALE ? 5300 : 5301;
	}
	
	public void addRaceSkill() {
		addTemporarySkill(getRaceSkill(), 1);
	}
	
	public int getRaceSkill() {
		return 5307 + getRace().ordinal();
	}
		
	public void assignSiegeSkills(int factionId) {
		SiegeManager[] managers = getManagers(factionId);
		assignSiegeHalfAllSkills(this, managers);
		
		for (int[] group : getSiegeNpcIds(factionId))
			addSkillsForGroup(this, group[0], group[1], group[2], managers);
	}
	
	private SiegeManager[] getManagers(int factionId) {
		switch (factionId) {
			case 1: return GuardGod.MANAGERS_GODS;
			case 2: return GuardGiant.MANAGERS_GIANTS;
			default: return GuardTitan.MANAGERS_TITANS;
		}
	}
	
	private void addSkillsForGroup(Player player, int startBossId, int endBossId, int startSkillId, SiegeManager[] managers) {
		for (int boss = startBossId; boss <= endBossId; boss++) {
			if (getBossStatus(boss, managers) == Constant.ALIVE) {
				player.addTemporarySkill(startSkillId + (boss - startBossId), 1);
				
				if (Config.OUTPOST_STAT)
					player.sendMessage("New skill #" + (boss - startBossId + 1) + " added!");
			}
		}
	}
	
	private int getBossStatus(int boss, SiegeManager[] managers) {
		for (SiegeManager manager : managers)
			if (manager != null && boss >= manager.getStartId() && boss <= manager.getEndId())
				return manager.getBossStatus(boss);

		return Constant.DEAD;
	}
	
	private int[][] getSiegeNpcIds(int factionId) {
		switch (factionId) {
			case 1: return START_END_BOSS_IDS_GODS;
			case 2: return START_END_BOSS_IDS_GIANTS;
			default: return START_END_BOSS_IDS_TITANS;
		}
	}

	private void assignSiegeHalfAllSkills(Player player, SiegeManager[] managers) {
		addSiegeSkill(player, 10, 5419, "OUTPOST HALF", managers[0]);
		addSiegeSkill(player, 19, 5420, "OUTPOST ALL", managers[0]);
		addSiegeSkill(player, 4, 5508, "ARTIFACT HALF", managers[1]);
		addSiegeSkill(player, 8, 5509, "ARTIFACT ALL", managers[1]);
		addSiegeSkill(player, 3, 5606, "TOWN HALF", managers[2]);
		addSiegeSkill(player, 6, 5607, "TOWN ALL", managers[2]);
	}
	
	private void addSiegeSkill(Player player, int status, int skillId, String skillMessage, SiegeManager manager) {
		if (manager != null && manager.getStatus() >= status) {
			player.addTemporarySkill(skillId, 1);
			
			if (Config.OUTPOST_STAT)
				player.sendMessage("New skill < " + skillMessage + " > added!");
		}
	}
	
	public void addSkills() {
		if (!isMinLevel() || !isValidFaction(getFactionId()))
			return;

		updateRankList();
		addGenderSkill();
		addFactionSkill();
		addRaceSkill();
		assignSiegeSkills(getFactionId());
	}
	/** Skills */
	
	/** LEVEL */
	public void removeExpAndSp(long removeExp, int removeSp) {
		getStat().removeExpAndSp(removeExp, removeSp);
	}

	public void addExpAndSp(long addToExp, int addToSp, Map<Creature, RewardInfo> rewards) {
		double multiplier = getXPBonus();
		getStat().addExpAndSp((long) (addToExp * multiplier), (int) (addToSp * multiplier), rewards);
	}

	public void addExpAndSpNoBonus(long addToExp, int addToSp) {
		getStat().addExpAndSp(addToExp, addToSp);
	}
	
	public double getVipXPMultiplier() {
		return getVIPDataStats(VipStats::getXPMultiplier);
	}

	public double getVipSPMultiplier() {
		return getVIPDataStats(VipStats::getSPMultiplier);
	}
	
	public double getVipDropMultiplier() {
		return getVIPDataStats(VipStats::getDropMultiplier);
	}
	
	public double getVipSpoilMultiplier() {
		return getVIPDataStats(VipStats::getSpoilMultiplier);
	}
	
	private double getVIPDataStats(Function<VipStats, Double> vipStatExtractor) {
		return isVip() ? vipStatExtractor.apply(getVipData()) : 1;
	}
	
	public VipStats getVipData() {
		return VipSystem.getVIPData(_vipLevel);
	}
	
	private double getBonus(double rate, Stats stat, Function<VipStats, Double> vipStatExtractor) {
		double baseRate = rate - 1;
		double statRate = calcStat(stat, 1, null, null);
		double vipBonus = (isVip() ? vipStatExtractor.apply(getVipData()) - 1 : 0);
		return baseRate + statRate + vipBonus;
	}

	/** Stats.XP_RATE + VIP bonus for XP */
	public double getXPBonus() {
		return getBonus(Config.RATE_XP, Stats.XP_RATE, VipStats::getXPMultiplier);
	}

	/** Stats.SP_RATE + VIP bonus for SP */
	public double getSPBonus() {
		return getBonus(Config.RATE_SP, Stats.SP_RATE, VipStats::getSPMultiplier);
	}

	/** Stats.XP_RATE_Q + VIP bonus for XP */
	public double getXPBonusQuest() {
		return getBonus(Config.RATE_QUEST_REWARD_XP, Stats.XP_RATE_Q, VipStats::getXPMultiplier);
	}

	/** Stats.SP_RATE_Q + VIP bonus for SP */
	public double getSPBonusQuest() {
		return getBonus(Config.RATE_QUEST_REWARD_SP, Stats.SP_RATE_Q, VipStats::getSPMultiplier);
	}

	/** Stats.DROP_RATE + VIP bonus for Drop */
	public double getDropBonus() {
		return getBonus(Config.RATE_DROP_ITEMS, Stats.DROP_RATE, VipStats::getDropMultiplier);
	}

	/** Stats.SPOIL_RATE + VIP bonus for Spoil */
	public double getSpoilBonus() {
		return getBonus(Config.RATE_DROP_SPOIL, Stats.SPOIL_RATE, VipStats::getSpoilMultiplier);
	}
	
	public long getExp() {
		return getStat().getExp();
	}

	public int getSp() {
		return getStat().getSp();
	}
	
	public boolean verifyVipAndPlayerLevel(int vipLvl, int playerLevel) {
		return verifyPlayerVipLevel(vipLvl, false) && verifyPlayerLevel(playerLevel, false);
	}
	
	public boolean verifyPlayerVipLevel(int requiredLevel, boolean sendMessage) {
		return isLevelSufficient(getVipLevel(), requiredLevel, "VIP", sendMessage);
	}

	public boolean verifyPlayerLevel(int requiredLevel, boolean sendMessage) {
		return isLevelSufficient(getLevel(), requiredLevel, "character", sendMessage);
	}
	
	private boolean isLevelSufficient(int currentLevel, int requiredLevel, String levelType, boolean sendMessage) {
		boolean isLevelNotSufficient = currentLevel < requiredLevel;

		if (isLevelNotSufficient && sendMessage)
			sendMessage(String.format("Your %s level must be %d or higher.", levelType, requiredLevel));

		return !isLevelNotSufficient;
	}
	
	public boolean isPlayerLevelInRange(int min, int max) {
		return getLevel() >= min && getLevel() <= max;
	}
	
	public boolean isMinLvl(boolean sendMsg) {
		return verifyPlayerLevel(ClassMaster.MIN_LVL, sendMsg);
	}
	
	public boolean isMinLevel() {
		return isMinLvl(false);
	}
	
	public boolean isMinLevelMsg() {
		return isMinLvl(true);
	}
	
	public boolean isMaxLevel() {
		return verifyPlayerLevel(ClassMaster.MAX_LVL, false);
	}

	public boolean levelLessThan(int lvl) {
		return getLevel() < lvl;
	}
	
	/** Restore the experience this Player has lost and sends StatusUpdate packet. @param restorePercent The specified % of restored experience. */
	public void restoreExp(double restorePercent) {
		if (getExpBeforeDeath() > 0) {
			getStat().addExp((int) calcExpLose(getExpBeforeDeath(), getExp(), restorePercent));
			setExpBeforeDeath(0);
		}
	}

	/**
	 * Applies the experience (and, if necessary, level) loss that occurs when this player dies. 
	 * Nothing happens if the death took place inside a siege or PvP zone, or if the server is configured to disable de-levelling.
	 */
	public void applyDeathExperienceLoss() {
		if (!Config.ALT_GAME_DELEVEL || isInsideSiegeOrPvPZone() || isInSiege())
			return;

		int level = getLevel();

		if (level % 5 == 0 || level == 99)
			return;

		double percentLost = calculatePercentageOrMultiplier(level, 1);

		if (hasKarma())
			percentLost = Config.RATE_KARMA_EXP_LOST * calculatePercentageOrMultiplier(level, 2);

		if (hasSkill(L2Skill.ANTI_DROP))
			percentLost /= getSkillLevel(L2Skill.ANTI_DROP) + 1;

		long lostExp = (level < Experience.MAX_LEVEL) ? calcExpLose(level + 1, level, percentLost) : calcExpLose(Experience.MAX_LEVEL, Experience.MAX_LEVEL - 1, percentLost);
		setExpBeforeDeath(getExp());
		updateKarmaLoss(lostExp);
		getStat().addExp(-lostExp);
	}

	public long calcExpLose(int level1, int level2, double percent) {
		return calcExpLose(getExpForLevel(level1), getExpForLevel(level2), percent);
	}
	
	public long calcExpLose(long exp1, long exp2, double percent) {
		return Math.round((exp1 - exp2) * percent / 100);
	}

	public long getExpForLevel(int level) {
		return getStat().getExpForLevel(level);
	}

	private double calculatePercentageOrMultiplier(int level, double multiplier) {
		return multiplier * Math.max(1, 10 - (level / 10));
	}
	/** LEVEL */
	
	/** Death Penalty */
	public void penaltyDeadPlayer(PunishLevel state, int delayInMinutes) {
		setPunishLevel(state, delayInMinutes);

		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_PUNISH)) {
			ps.setInt(1, state.getPunishValue()); // Level
			ps.setLong(2, MathUtil.minsToMs(delayInMinutes)); // Value
			ps.setString(3, getName());
			ps.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** Death Penalty */
	/** !!! ALL DATA !!! */

	/** !!! MY DATA !!! */
	/** Block PvP System */
	boolean pvpActivityBlock = true;
	
	private void setPvPActivityBlock(boolean activate) {
		pvpActivityBlock = activate;
	}
	
	/*private boolean isSpreeBeginTime() {
		return killingSpreeTimer;
	}*/

	/*private void getProtectedMsg() {
		//CreatureSay cs = new CreatureSay(this.getObjectId(), Say2.ALL, this.getName(), "PvP Protection is OFF! Pvp receiving unblocked.");
		//	this.sendPacket(cs);
		//Broadcast.announceToOnlinePlayers(cs); Broadcast.toAllOnlinePlayers(cs);
		this.sendMessage("PvP Protection is OFF! PvP reward unblocked.");
	}*/
	
	/*private void spreeCounterMsg() {
		//CreatureSay cs = new CreatureSay(this.getObjectId(), Say2.ALL, this.getName(), "PvP Protection is OFF! Pvp receiving unblocked.");
		//	this.sendPacket(cs);
		//Broadcast.announceToOnlinePlayers(cs); Broadcast.toAllOnlinePlayers(cs);
		this.sendMessage("Killing Spree is over. Kills back to: " + currentSpreeKills);
	}*/
	
	private void blockPvPActivity() {
		setPvPActivityBlock(false);
		//sendMessage("PvP Protection is ON! PvP reward blocked for " + Config.PVP_BLOCK_REWARD_TIME + " sec.");
		ThreadPool.schedule(() -> setPvPActivityBlock(true), MathUtil.convertToMs(Config.PVP_BLOCK_REWARD_TIME)); //10000 (5000) // getProtectedMsg();
	}
	
	/*private long _lastPvPTime = 0;

	private long getLastPvpTime() {
		return _lastPvPTime;
	}

	private void setLastPvpTime() {
		_lastPvPTime = System.currentTimeMillis();
	}

	private void blockedPvPactiveSecondVar() {
		if (System.currentTimeMillis() - getLastPvpTime() < MathUtil.convertToMs(Config.PVP_BLOCK_REWARD_TIME)) {
			sendMessage("PvP Protection is ON! " + Config.PVP_BLOCK_REWARD_TIME + " sec.");
		} else {
			setLastPvpTime();
		}
	}*/
	/** Block PvP System */
	
	/** Spree System */
	private boolean killingSpreeTimer = true;
	
	private void setKillingSpreeTimer(boolean activate) {
		killingSpreeTimer = activate;
	}
	
	private int currentSpreeKills;
	
	private void spreeCounter() {
		if (currentSpreeKills < 6) {
			currentSpreeKills++;
			
			/*CreatureSay cs = new CreatureSay(getObjectId(), Say2.ALL, getName(), "Kills " + killingSpreeKills1);
				sendPacket(cs);
				
			if (killingSpreeKills1 == 2)
				sendMessage("You have " + Config.KILLING_SPREE_TIME + " sec to finish Killing Spree. Kills now: " + killingSpreeKills1);*/
		}
		
		if (killingSpreeTimer) {
			setKillingSpreeTimer(false);

			ThreadPool.schedule(() -> {
				setKillingSpreeTimer(true);
				currentSpreeKills = 0;
//				spreeCounterMsg();
			}, MathUtil.convertToMs(Config.KILLING_SPREE_TIME));
		}
	}
	
	private void spreeReward() {
		for (SpreeKills spreeKills : SpreeKillsData.getInstance().getSpreeKills()) {
			if (currentSpreeKills == spreeKills.getKills()) {
				sendPacket(new CreatureSay(getObjectId(), Say2.ALL, getName(), "performed " + spreeKills.getMsg()));

				if (!spreeKills.getSound().isEmpty())
					sendPacket(new PlaySound(2, spreeKills.getSound()));

				if (Rnd.isLessThanRandom(spreeKills.getItemChance()))
					addItem(spreeKills.getItemId(), 1);

				progressAchievement(AchievementType.STREAK);
				
				if (currentSpreeKills == 5)
					Broadcast.announceToOnlinePlayers("Player <" + getName() + "> performed Penta Kill!", true);
			}
		}
	}
	/** Spree System */
	
	/** Color System */
	public void updateColor(boolean isEvent) {
		if (!isValidFaction(getFactionId()))
			updateColor(isGM() ? getAccessLevel().getNameColor() : Constant.INT_BLACK);
		else if (isEvent)
			PvPColorSystem.updateColorForEvent();
		else
			PvPColorSystem.updateNameAndTitleColor(this, getFactionId());
	}
	
	public void updateColor(int color) {
		PcAppearance appearance = getAppearance();
		appearance.setNameColor(color);
		appearance.setTitleColor(color);
		broadcastUserInfo();
	}
	/** Color System */
	
	/** Faction System */
	public void addFactionSkill() {
		addTemporarySkill(getFactionSkill(), 1);
	}

	public String getFactionName() {
		return FactionInfo.getNameById(getFactionId());
	}
	
	public int getFactionSkill() {
		return FactionInfo.values()[getFactionId()].getSkillId();
	}
	
	public boolean isSameFaction(int factionId) {
		return getFactionId() == factionId;
	}
	
	public boolean isSameFaction(Player target) {
		return getFactionId() == target.getFactionId();
	}
	
	public boolean hasFaction() {
		return getFactionId() > 0;
	}
	/** Faction System */
	
	/** Reward System */
	public void giveReward(RewardName rewardName) {
		giveReward(rewardName.getName());
	}

	public void giveReward(String rewardType) {
		int pvpKills = getPvpKills();
		int pkKills = getPkKills();
		int level = getLevel();
		
		for (PvpReward pvpReward : PvpRewardTable.getInstance().getRewards()) {
			if (pvpReward.meetsAllConditions(rewardType, pvpKills, pkKills)) {
				pvpReward.addExpAndSp(this, level);
				pvpReward.addItem(this);
				sendMessage("You've been rewarded " + rewardType + "!");
			}
		}
	}

	public void addItem(int id, int count) {
		addItem("Item", id, count, this, true);
	}
	/** Reward System */
	
	/** Offline Trade */
	private long _offlineShopStart;

	public long getOfflineStartTime() {
		return _offlineShopStart;
	}
	
	public void setOfflineStartTime(long time) {
		_offlineShopStart = time;
	}
	/** Offline Trade */
	
	/** Red Aura System */
	public void updateRankList() {
		if (getLevel() < ClassMaster.PLAYER_CLASS_SKILLS_LEVEL || getRecomLeft() < Constant.NUMBER_1000)
			return;

		populateRankList();
		String playerName = getName();

		if (RANK_LIST.contains(playerName)) {
			if (getTeam() == 0)
				setTeam(2);

			addTemporarySkill(170, (RANK_LIST.indexOf(playerName) + 1) / 2);
		}
	}

	private void populateRankList() {
		long currentTimeMillis = System.currentTimeMillis();

		if (currentTimeMillis - lastRefreshTime < REFRESH_INTERVAL_MS)
			return;

		lastRefreshTime = currentTimeMillis;
		List<String> tempRankList = new ArrayList<>(Constant.NUMBER_20);

		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stm = con.prepareStatement(CHARACTERS_QUERY);
			ResultSet rSet = stm.executeQuery()) {

			while (rSet.next() && tempRankList.size() < Constant.NUMBER_2) {
				String className = PlayerData.getInstance().getClassNameById(rSet.getInt("base_class"));

				if (!tempRankList.contains(className)) {
					tempRankList.add(className);
					tempRankList.add(rSet.getString("char_name"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!RANK_LIST.equals(tempRankList)) {
			RANK_LIST.clear();
			RANK_LIST.addAll(tempRankList);
		}
	}
	/** Red Aura System */

	/** Menu Options */
	private boolean _isMessageDisabled;
	
	public boolean isInRefusalMode() {
		return _isMessageDisabled;
	}
	
	public void setInRefusalMode(boolean mode) {
		_isMessageDisabled = mode;
		sendPacket(new EtcStatusUpdate(this));
	}
	
	private boolean _isTradeDisabled;
	
	public void setTradeRefusal(boolean mode) {
		_isTradeDisabled = mode;
	}
	
	public boolean isTradeRefusal() {
		return _isTradeDisabled;
	}
	
	private boolean _isEXPDisabled;
	
	public void setExpGain(boolean value) {
		_isEXPDisabled = value;
	}
	
	public boolean isExpGainDisabled() {
		return _isEXPDisabled;
	}
	
	private boolean _isPartyDisabled;
	
	public void setIsInPartyRefusal(boolean value) {
		_isPartyDisabled = value;
	}
	
	public boolean isInPartyRefusal() {
		return _isPartyDisabled;
	}

	private boolean _isBuffDisabled;

	public void setIsInBuffRefusal(boolean value) {
		_isBuffDisabled = value;
	}
	
	public boolean isInBuffRefusal() {
		return _isBuffDisabled;
	}
	
	private boolean _isSSDisabled;
	
	public void setIsSSDisabled(boolean value) {
		_isSSDisabled = value;
	}
	
	public boolean isSSDisabled() {
		return _isSSDisabled;
	}
	/** Menu Options */
	
	/** Dungeon */
	private Dungeon _dungeon;
	
	public void setDungeon(Dungeon val) {
		_dungeon = val;
	}
	
	public Dungeon getDungeon() {
		return _dungeon;
	}
	
	public boolean isInDungeon() {
		return _dungeon != null;
	}
	/** Dungeon */

	/** Siege Damage System */
	private int[] _siegeDamage = { 0, 0, 0 }; // 0 - Outpost, 1 - Artifact, 2 - Town

	public int getSiegeDamage(int type) {
		return _siegeDamage[type];
	}

	public void setSiegeDamage(int type, int damage) {
		_siegeDamage[type] = damage;
	}
	
	public void addSiegeDamage(int type, double damage) {
		_siegeDamage[type] += damage;
	}
	/** Siege Damage System */

	/** Achievement Stages */
	private AchievementProgress _achievementProgress = new AchievementProgress(this);
	
	public final AchievementProgress getAchievementProgress() {
		return _achievementProgress;
	}
	
	public void progressAchievement(AchievementType atype) {
		getAchievementProgress().increase(atype);
	}
	/** Achievement Stages */

	/** VIP */
	private int _vipLevel;
	
	public void setVipLevel(int level) {
		_vipLevel = vipLevelClamp(level);

		for (int skill : VIP_SKILLS) {
			if (_vipLevel > 0 && (_vipLevel == 3 || skill > 489))
				addTemporarySkill(skill, _vipLevel);
			else
				removeSkill(skill, false);
		}
	}
	
	public int vipLevelClamp(int level) {
		return Math.max(0, Math.min(level, 3));
	}
	
	public int getVipLevel() {
		return _vipLevel;
	}
	
	public boolean isVip() {
		return _vipLevel > 0;
	}
	
	public boolean isVipOrHigher(int level) {
		return _vipLevel >= level;
	}
	
	public boolean hasExactVipLevel(int level) {
		return _vipLevel == level;
	}
	
	public void giveStatus(SpecialStatusType type, int val) {
		statusAction(type, val, "You got " + type + " status for the next 7 days.", 0);
	}
	
	public void extendStatus(SpecialStatusType type, int val) {
		statusAction(type, val, "You extended " + type + " status for the next 7 days.", _specialStatusManager.getExpireTime(type));
	}
	
	private void statusAction(SpecialStatusType type, int val, String msg, long time) {
		sendMessage(msg);
		_specialStatusManager.store(type, String.valueOf(val), time + ClassMaster.daysToMillis(ClassMaster.DAYS));
	}
	/** VIP */

	/** Damage System */
	private int hits; //
	
	public int getHits() {
		return hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}
	
	public void addHits() {
		hits++;
	}

	private int totalDamage; //
	
	public int getTotalDamage() {
		return totalDamage;
	}

	public void setTotalDamage(int totalDamage) {
		this.totalDamage = totalDamage;
	}
	
	public void addTotalDamage(double damage) {
		totalDamage += damage;
	}

	private int maxDamage; //
	
	public int getMaxDamage() {
		return maxDamage;
	}
	
	public void setMaxDamage(int maxDamage) {
		this.maxDamage = maxDamage;
	}
	
	private boolean isInProgress; //

	public boolean isInProgress() {
		return isInProgress;
	}

	public void setInProgress(boolean isInProgress) {
		this.isInProgress = isInProgress;
	}
	/** Damage System */

	/** PvP Manager */
	private int _kills;

	public int getKills() {
		return _kills;
	}
	
	public void setKills(int kills) {
		_kills = kills;
	}
	
	public void addKills(int kills) {
		_kills += kills;
	}

	private int _deaths; // _deaths not in use, use them for something else
	
	public int getDeaths() {
		return _deaths;
	}
	
	public void setDeaths(int deaths) {
		_deaths = deaths;
	}
	
	public void increaseDeaths() {
		_deaths++;
	}
	/** PvP Manager */
	
	/** Events */
	private String originalTitle;
	
	public String getOriginalTitle() {
		return originalTitle;
	}
	
	public void setOriginalTitle(String originalTitle) {
		this.originalTitle = originalTitle;
	}
	
	private boolean showMaskedName;
	private String maskedName = "";
	private String maskedTitle = "";
	
	public void setMaskName(boolean show, String name, String title) {
		showMaskedName = show;
		maskedName = name;
		maskedTitle = title;
	}

	public void setEventChannel() {
		if (EventBase.isStartedAndParticipant(getObjectId()) && Config.TM_HIDE_NAMES) // isAffected(L2EffectFlag.SILENT_MOVE)
			setMaskName(true, Config.TM_ALT_NAME, Config.TM_ALT_NAME); // setMaskName(true, " ", " ");
		else
			setMaskName(false, "", ""); // setMaskName(false, "", "");
	}
	/** Events */
	
	/** Skins System */
	private Skinable[] _skins = new Skinable[2]; // 0 - Armor | 1 - Weapon
	
	public Skinable getSkin(int id) {
		return (id >= 0 && id < _skins.length) ? _skins[id] : _skins[0];
	}

	public void setSkin(Skinable skin, int id) {
		if (id >= 0 && id < _skins.length)
			_skins[id] = skin;
	}
	/** Skins System */
	
	/** Daily Weekly Tasks */
	private QuestManagerDefault _questManagerDefault = new QuestManagerDefault(this);
	
	public QuestManagerDefault getQuestManagerDefault() {
		return _questManagerDefault;
	}
	
	private QuestManagerGlobal _questManagerGlobal = new QuestManagerGlobal(this);
	
	public QuestManagerGlobal getQuestManagerGlobal() {
		return _questManagerGlobal;
	}
	
	private QuestManagerBattle _questManagerBattle = new QuestManagerBattle(this);;
	
	public QuestManagerBattle getQuestManagerBattle() {
		return _questManagerBattle;
	}
	
	private CounterManager _counterManager = new CounterManager(this);
	
	public CounterManager getCounterManager() {
		if (Time.isBeforeNoon())
			_counterManager.updateCountersTime();

		return _counterManager;
	}
	
	public void progressQuests(QuestIdType type) {
		getQuestManagerDefault().updateQuestsByIdType(type);
		getQuestManagerGlobal().updateQuestsByIdType(type);
		getQuestManagerBattle().updateQuestsByIdType(type);
	}
	/** Daily Weekly Tasks */
	
	/** Other */
	private QuestManagerAbstract _questManager;
	
	public QuestManagerAbstract getPlayerQuestManager() {
		return _questManager;
	}
	
	public void setPlayerQuestManager(QuestManagerAbstract questManagerAbstract) {
		_questManager = questManagerAbstract;
	}
	
	private SpecialStatusManager _specialStatusManager = new SpecialStatusManager(this);
	
	public SpecialStatusManager getSpecialStatusManager() {
		return _specialStatusManager;
	}
	/** Other */
	
	/** Auction Item Type System */
	private String grade = "B";
	
	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	private String quality = "LOW";

	public String getQuality() {
		return quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	private String type = "SHIELD";

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private String currency = "Gold";

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	/** Auction Item Type System */

	/** Polymorph System */
	public boolean isPolyTypeNPC() {
		return getPolyType() == PolyType.NPC;
	}

	public L2GameServerPacket sendMorphInfo() {
		return new AbstractNpcInfo.PcMorphInfo(this, getPolyTemplate());
	}

	public final void refreshInfos() {
		for (WorldObject object : getKnownType(WorldObject.class)) {
			if (object.isInstanceOfPlayer() && ((Player) object).isInObserverMode())
				continue;

			sendInfoFrom(object);
		}
	}
	
	private final void sendInfoFrom(WorldObject object) {
		if (object.getPolyType() == PolyType.ITEM) {
			sendPacket(new SpawnItem(object));
		} else {
			object.sendInfo(this); // send object info to player

			if (object instanceof Creature) {
				Creature obj = (Creature) object; // Update the state of the Creature object client side by sending Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the Player

				if (obj.hasAI())
					obj.getAI().describeStateToPlayer(this);
			}
		}
	}
	
	public void unpolymorphFull() {
		stopAbnormalEffect(Transform.TRANSFORM_ABNORMAL);
		unpolymorphStopEffect();
	}
	
	public void unpolymorphStopEffect() {
		unpolymorph();
		stopTransformEffect();
	}
	
	public void stopTransformEffect() {
		stopSkillEffects(Transform.TRANSFORM[4]); // By Skill Id | player.stopSkillEffects(skillType); By Skill Type | player.stopEffects(effectType); By Effect Type
	}
	
	public void sendUserInfo() {
		sendPacket(new UserInfo(this));
	}
	
	public void setUnParalyzedMsg(String msg) {
		setIsParalyzed(false);
		sendMessage(msg);
	}
	
	public void polymorphTransform() {
		setIsParalyzedDelayedMsg(this, true, Transform.TRANSFORM[5], Transform.TRANSFORM[7], Transform.TRANSFORM_MSG[0]);
	}
	
	public void unpolymorphTransform() {
		setIsParalyzedDelayedMsg(this, false, Transform.TRANSFORM[6], Transform.TRANSFORM[8], Transform.TRANSFORM_MSG[1]);
	}
	
	public void unTransform(ItemInstance item) {
		if (item.isTransform() && !isPolyTypeDefault())
			unpolymorphTransform();
	}
	
	public void setTransform(boolean start, String msg) {
		if (start) {
			polymorph(PolyType.NPC, Transform.TRANSFORM[getFactionId()]);
			getSkillInfo(Transform.TRANSFORM[4]).getEffects(this, this);
		} else {
			unpolymorphStopEffect();
		}

		setAbnormalEffect(start, Transform.TRANSFORM_ABNORMAL);
		setUnParalyzedMsg(msg);
	}
	/** Polymorph System */
	/** !!! MY DATA !!! */

	/** CHECKS */
	/**
	 * Signets check used to valid who is affected when he entered in the aoe effect.
	 * @param cha The target to make checks on.
	 * @return true if player can attack the target.
	 */
	public boolean canAttackCharacter(Creature cha) {
		if (!(cha instanceof Playable) || cha.isInArena())
			return true;

		final Player target = cha.getActingPlayer();

		/*if (getFactionId() != target.getFactionId())
			return true;*/

		if (isInDuel() && target.isInDuel() && target.getDuelId() == getDuelId())
			return true;

		if (isInParty() && target.isInParty()) {
			if (getParty() == target.getParty())
				return false;
			
			if ((getParty().getCommandChannel() != null || target.getParty().getCommandChannel() != null) && (getParty().getCommandChannel() == target.getParty().getCommandChannel()))
				return false;
		}
		
		if (hasClan() && target.hasClan()) {
			if (getClanId() == target.getClanId())
				return false;

			if ((getAllyId() > 0 || target.getAllyId() > 0) && getAllyId() == target.getAllyId())
				return false;

			if (getClan().isAtWarWith(target.getClanId()))
				return true;
		}
		
		return target.isFlagged() || target.hasKarma();
	}
	
	/**
	 * Test if the current {@link Player} can summon. Send back messages if he can't.
	 * @return True if the player can summon, False otherwise.
	 */
	public boolean canSummon() {
		if (isInOlympiadMode() || isInObserverMode() || isInsideZone(ZoneId.NO_SUMMON_FRIEND) || !EventBase.canUseEscape(getObjectId()) || isInDungeon()) {
			sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
			return false;
		}

		return !isMounted();
	}
	
	/**
	 * Test if the {@link WorldObject} can be summoned. Send back messages if he can't.
	 * @param target : The target to test.
	 * @return True if the given target can be summoned, False otherwise.
	 */
	public boolean isTargetSummonable(WorldObject target) {
		if (!(target.isInstanceOfPlayer()))
			return false;

		final Player player = (Player) target;

		if (!EventBase.canUseEscape(player.getObjectId()) || player.isInDungeon()) {
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		
		if (player.isAlikeDead()) {
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED).addCharName(player));
			return false;
		}

		if (player.isInStoreMode()) {
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED).addCharName(player));
			return false;
		}
		
		if (player.isRooted() || player.isInCombat()) {
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED).addCharName(player));
			return false;
		}
		
		if (player.isInOlympiadMode()) {
			sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD);
			return false;
		}
		
		if (player.isMounted()) {
			sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		
		if (player.isInObserverMode() || player.isInsideZone(ZoneId.NO_SUMMON_FRIEND)) {
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IN_SUMMON_BLOCKING_AREA).addCharName(player));
			return false;
		}
		
		return true;
	}
	
	public boolean isAllowedToEnchantSkills() {
		if (isLocked())
			return false;

		if (AttackStanceTaskManager.getInstance().isInAttackStance(this))
			return false;

		if (isCastingNow() || isCastingSimultaneouslyNow())
			return false;

		return !isInBoat();
	}
	
	/**
	 * @param type : The ArmorType to check. It supports NONE, SHIELD, MAGIC, LIGHT and HEAVY.
	 * @return true if the given ArmorType is used by the chest of the player, false otherwise.
	 */
	public boolean isWearingArmorType(ArmorType type) {
		final ItemInstance armor = getInventory().getPaperdollItem((type == ArmorType.SHIELD) ? Inventory.PAPERDOLL_LHAND : Inventory.PAPERDOLL_CHEST); // Retrieve either the shield or the chest, following ArmorType to check.
		return (armor == null) ? type == ArmorType.NONE : armor.getItemType() instanceof ArmorType && armor.getItemType() == type; // Return true if not equipped and the check was based on NONE ArmorType. OR Test if the equipped item is an armor, then finally compare both ArmorType.
	}
	
	private boolean canAfford(int priceGold, int priceSp) {
		ItemInstance goldItem = getInventory().getItemByItemId(ClassMaster.GOLD_ITEM_ID);
		return goldItem != null && goldItem.getCount() >= priceGold && getSp() >= priceSp;
	}
	/** CHECKS */

	/** Helper */
	public void inventoryUpdate(ItemInstance item) {
		InventoryUpdate iu = new InventoryUpdate();
		iu.addItem(item);
		sendPacket(iu);
	}
	
	public void statusLoadUpdate() {
		statusUpdate(this, StatusUpdate.CUR_LOAD, getCurrentLoad());
	}
	
	public void statusUpdate(Player player, int id, int level) {
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(id, level);
		sendPacket(su);
	}

	/**
	 * Broadcast informations from a user to himself and his knownlist.<BR>
	 * If player is morphed, it sends informations from the template the player is using.
	 * Send a UserInfo packet (public and private data) to this Player.
	 * Send a CharInfo packet (public data only) to Player's knownlist.
	 */
	public final void broadcastUserInfo() {
		sendUserInfo();

		if (isPolyTypeNPC())
			Broadcast.toKnownPlayers(this, sendMorphInfo());
		else
			broadcastCharInfo();
	}
	
	public final void broadcastCharInfo() {
		for (Player player : getKnownType(Player.class)) {
			player.sendPacket(new CharInfo(this));
			final int relation = getRelation(player);
			final boolean isAutoAttackable = isAutoAttackable(player);
			player.sendPacket(new RelationChanged(this, relation, isAutoAttackable));

			if (getPet() != null)
				player.sendPacket(new RelationChanged(getPet(), relation, isAutoAttackable));
		}
	}
	
	/** Broadcast player title information. */
	public final void broadcastTitleInfo() {
		sendUserInfo();
		broadcastPacket(new TitleUpdate(this));
	}
	
	/** Update Stats of the Player client side by sending UserInfo/StatusUpdate to this Player and CharInfo/StatusUpdate to all Player in its _KnownPlayers (broadcast). */
	public void updateAndBroadcastStatus(int broadcastType) {
		refreshOverloaded();
		refreshExpertisePenalty();

		if (broadcastType == 1)
			sendUserInfo();
		else if (broadcastType == 2)
			broadcastUserInfo();
	}
	
	/**
	 * Manage AutoLoot Task.
	 * Send a System Message to the Player : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2
	 * Add the Item to the Player inventory
	 * Send InventoryUpdate to this Player with NewItem (use a new slot) or ModifiedItem (increase amount)
	 * Send StatusUpdate to this Player with current weight
	 * If a Party is in progress, distribute Items between party members
	 * @param target The reference Object.
	 * @param item The dropped ItemHolder.
	 */
	public void doAutoLoot(Attackable target, IntIntHolder item) {
		if (isInParty())
			getParty().distributeItem(this, item, false, target);
		else
			addItem("Loot", item.getId(), item.getValue(), target, true);
	}
	
	public void onTradeConfirm(Player partner) {
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CONFIRMED_TRADE).addString(partner.getName()));
		partner.sendPacket(TradePressOwnOk.STATIC_PACKET);
		sendPacket(TradePressOtherOk.STATIC_PACKET);
	}
	
	private void useResources(int priceGold, int priceSp) {
		destroyItemByItemId("Consume", ClassMaster.GOLD_ITEM_ID, priceGold, this, true);
		removeExpAndSp(0, priceSp);
	}
	
	public int getRelation(Player target) {
		int result = 0;

		if (isFlagged()) // Karma and PvP may not be required
			result |= RelationChanged.RELATION_PVP_FLAG;

		if (hasKarma())
			result |= RelationChanged.RELATION_HAS_KARMA;

		if (isClanLeader())
			result |= RelationChanged.RELATION_LEADER;

		int state = getSiegeState();

		if (state != 0) {
			result |= RelationChanged.RELATION_INSIEGE;

			if (state != target.getSiegeState())
				result |= RelationChanged.RELATION_ENEMY;
			else
				result |= RelationChanged.RELATION_ALLY;

			if (state == 1)
				result |= RelationChanged.RELATION_ATTACKER;
		}
		
		if (hasClan() && target.hasClan()) {
			if (target.pledgeTypeNotAcademy() && pledgeTypeNotAcademy() && target.getClan().isAtWarWith(getClan().getClanId())) {
				result |= RelationChanged.RELATION_1SIDED_WAR;

				if (getClan().isAtWarWith(target.getClan().getClanId()))
					result |= RelationChanged.RELATION_MUTUAL_WAR;
			}
		}
		
		return result;
	}
	
	/** Restores secondary data for the Player, based on the current class index. */
	private void restoreCharData() {
		restoreSkills(); // Retrieve from the database all skills of this Player and add them to _skills.
		_macroses.restore(); // Retrieve from the database all macroses of this Player and add them to _macroses.
		_shortCuts.restore(); // Retrieve from the database all shortCuts of this Player and add them to _shortCuts.
		restoreHenna(); // Retrieve from the database all henna of this Player and add them to _henna.
		restoreRecom(); // Retrieve from the database all recom data of this Player and add to _recomChars.

		if (!isSubClassActive())
			restoreRecipeBook(); // Retrieve from the database the recipe book of this Player.
	}
	
	/** Update Player stats in the characters table of the database. */
	public synchronized void store(boolean storeActiveEffects) {
		if (isInsideRadius(getClientX(), getClientY(), 1000, true)) // update client coords, if these look like true
			setXYZ(getClientX(), getClientY(), getClientZ());
		
		storeCharBase();
		storeCharSub();
		storeEffect(storeActiveEffects);
		storeRecipeBook();
		_vars.storeMe();
	}
	
	public void store() {
		store(true);
	}
	
	private void storeCharBase() {
		final int currentClassIndex = getClassIndex(); // Get the exp, level, and sp of base class to store in base table
		_classIndex = 0;
		final long exp = getStat().getExp();
		final int level = getStat().getLevel();
		final int sp = getStat().getSp();
		_classIndex = currentClassIndex;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE_CHARACTER)) {
			ps.setInt(1, level);
			ps.setInt(2, getMaxHp());
			ps.setDouble(3, getCurrentHp());
			ps.setInt(4, getMaxCp());
			ps.setDouble(5, getCurrentCp());
			ps.setInt(6, getMaxMp());
			ps.setDouble(7, getCurrentMp());
			ps.setInt(8, getAppearance().getFace());
			ps.setInt(9, getAppearance().getHairStyle());
			ps.setInt(10, getAppearance().getHairColor());
			ps.setInt(11, getAppearance().getSex().ordinal());
			ps.setInt(12, getHeading());
			
			if (!isInObserverMode()) {
				ps.setInt(13, getX());
				ps.setInt(14, getY());
				ps.setInt(15, getZ());
			} else {
				ps.setInt(13, _savedLocation.getX());
				ps.setInt(14, _savedLocation.getY());
				ps.setInt(15, _savedLocation.getZ());
			}
			
			ps.setLong(16, exp);
			ps.setLong(17, getExpBeforeDeath());
			ps.setInt(18, sp);
			ps.setInt(19, getKarma());
			ps.setInt(20, getPvpKills());
			ps.setInt(21, getPkKills());
			ps.setInt(22, getClanId());
			ps.setInt(23, getRace().ordinal());
			ps.setInt(24, getClassId().getId());
			ps.setLong(25, getDeleteTimer());
			ps.setString(26, getTitle());
			ps.setInt(27, getAccessLevel().getLevel());
			ps.setInt(28, isOnlineInt());
			ps.setInt(29, isIn7sDungeon() ? 1 : 0);
			ps.setInt(30, getClanPrivileges());
			ps.setInt(31, wantsPeace() ? 1 : 0);
			ps.setInt(32, getBaseClass());
			ps.setLong(33, (_onlineBeginTime > 0 ? _onlineTime + (System.currentTimeMillis() - _onlineBeginTime) / 1000 : 0));
			ps.setInt(34, getPunishLevel().getPunishValue());
			ps.setLong(35, getPunishTimer());
			ps.setInt(36, isNoble() ? 1 : 0);
			ps.setLong(37, getPowerGrade());
			ps.setInt(38, getPledgeType());
			ps.setInt(39, getLvlJoinedAcademy());
			ps.setLong(40, getApprentice());
			ps.setLong(41, getSponsor());
			ps.setInt(42, getAllianceWithVarkaKetra());
			ps.setLong(43, getClanJoinExpiryTime());
			ps.setLong(44, getClanCreateExpiryTime());
			ps.setString(45, getName());
			ps.setLong(46, getDeathPenaltyBuffLevel());
			ps.setInt(47, getFactionId());
			ps.setInt(48, getRecomHave());
			ps.setInt(49, getRecomLeft());
			ps.setInt(50, getKills());
			ps.setInt(51, getDeaths());
			ps.setInt(52, getObjectId());
			ps.execute();
		} catch (Exception e) {
			LOGGER.error("Couldn't store player base data.", e);
		}

		_questManagerDefault.storeQuests();
		_questManagerGlobal.storeQuests();
		_questManagerBattle.storeQuests();
		_counterManager.storeCounters();
		getAchievementProgress().storeAllAchievements();
	}
	
	private void storeCharSub() {
		if (_subClasses.isEmpty())
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE_CHAR_SUBCLASS)) {
			for (SubClass subClass : _subClasses.values()) {
				ps.setLong(1, subClass.getExp());
				ps.setInt(2, subClass.getSp());
				ps.setInt(3, subClass.getLevel());
				ps.setInt(4, subClass.getClassId());
				ps.setInt(5, getObjectId());
				ps.setInt(6, subClass.getClassIndex());
				ps.addBatch();
			}
			
			ps.executeBatch();
		} catch (Exception e) {
			LOGGER.error("Couldn't store subclass data.", e);
		}
	}
	
	private void rechargeShots(ItemInstance item, boolean condition, ActionType actionType) {
		if (condition && item.getItem().getDefaultAction() == actionType) {
			IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
			
			if (handler != null)
				handler.useItem(this, item, false);
		}
	}
	
	private void hasAIaction() {
		if (hasAI())
			getAI().setIntention(CtrlIntention.IDLE);

		setTarget(null);
		getAppearance().setVisible();
		setIsInvul(false);
	}
	
	public void refreshHennaList() {
		sendPacket(new HennaInfo(this));
	}
	
	public void onPlayerEnter() {
		if (isCursedWeaponEquipped())
			CursedWeaponManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()).cursedOnLogin();

		GameTimeTaskManager.getInstance().add(this); // Add to the GameTimeTask to keep inform about activity time.
		updatePunishState(); // Jail task
		
		if (isGM()) {
			if (isInvul())
				sendMessage("Entering world in Invulnerable mode.");
			
			if (getAppearance().getInvisible())
				sendMessage("Entering world in Invisible mode.");

			if (isInRefusalMode())
				sendMessage("Entering world in Message Refusal mode.");
		}
		
		revalidateZone(true);
		notifyFriends(true);
		getAchievementProgress().load();
		taskLoader(this);
		stopTransformEffect();
		addSkills();
		sendMessage("Overall time spent in-game: " + (getOnlineTime() / Constant.ONE_HOUR_SEC) + " hrs.");
	}
	
	public void onActionRequest() {
		if (isSpawnProtected()) {
			sendMessage("As you acted, you are no longer under spawn protection.");
			setSpawnProtection(false);
			stopAbnormalEffect(ABNORMAL_EFFECT_TELEPORT);
		}
	}

	private synchronized void cleanup() {
		try {
			setOnlineStatus(false, true); // Put the online status to false.
			abortAttack(); // abort cast & attack and remove the target. Cancels movement aswell.
			abortCast();
			stopMove(null);
			setTarget(null);
			removeMeFromPartyMatch();

			if (isFlying())
				removeSkill(FrequentSkill.WYVERN_BREATH.getSkill().getId(), false);

			if (isMounted())
				dismount(); // Dismount the player.
			else if (getPet() != null)
				getPet().unSummon(this); // If the Player has a summon, unsummon it.

			// Stop all scheduled tasks.
			stopHpMpRegeneration();
			stopPunishTask(true);
			stopChargeTask();
			// Stop all timers associated to that Player.
			WaterTaskManager.getInstance().remove(this);
			AttackStanceTaskManager.getInstance().remove(this);
			PvpFlagTaskManager.getInstance().remove(this);
			GameTimeTaskManager.getInstance().remove(this);
			ShadowItemTaskManager.getInstance().remove(this);
			
			// Cancel the cast of eventual fusion skill users on this target.
			for (Creature character : getKnownType(Creature.class))
				if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
					character.abortCast();

			// Stop signets & toggles effects.
			for (L2Effect effect : getAllEffects()) {
				if (effect.getSkill().isToggle()) {
					effect.exit();
					continue;
				}
				
				switch (effect.getEffectType()) {
					case SIGNET_GROUND:
					case SIGNET_EFFECT:
						effect.exit();
						break;
						
					default:
						break;
				}
			}
			
			// Remove the Player from the world
			decayMe();
			
			// If a party is in progress, leave it
			if (isInParty())
				_party.removePartyMember(this, MessageType.DISCONNECTED);

			// Handle removal from olympiad game
			if (OlympiadManager.getInstance().isRegistered(this) || getOlympiadGameId() != -1)
				OlympiadManager.getInstance().removeDisconnectedCompetitor(this);

			int playerObjectId = getObjectId();
			
			// set the status for pledge member list to OFFLINE
			if (hasClan()) {
				ClanMember clanMember = getClan().getClanMember(playerObjectId);
				
				if (clanMember != null)
					clanMember.setPlayerInstance(null);
			}
			
			// deals with sudden exit in the middle of transaction
			if (getActiveRequester() != null) {
				setActiveRequester(null);
				cancelActiveTrade();
			}

			if (EventBase.isStartedAndParticipant(playerObjectId))
				EventBase.getEventInstance(eventInstance -> eventInstance.onLogout(this));

			// If the Player is a GM, remove it from the GM List
			if (isGM())
				AdminData.getInstance().deleteGm(this);

			// Check if the Player is in observer mode to set its position to its position before entering in observer mode
			if (isInObserverMode())
				setXYZInvisible(_savedLocation);

			// Oust player from boat
			if (getBoat() != null)
				getBoat().oustPlayer(this, true, Location.DUMMY_LOC);

			getInventory().deleteMe(); // Update inventory and remove them from the world
			clearWarehouse(); // Update warehouse and remove them from the world

			// Update freight and remove them from the world
			clearFreight();
			clearDepositedFreight();
			
			if (isCursedWeaponEquipped())
				CursedWeaponManager.getInstance().getCursedWeapon(_cursedWeaponEquippedId).setPlayer(null);

			if (getClanId() > 0)
				getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);

			if (isSeated()) {
				final WorldObject object = World.getInstance().getObject(_throneId);

				if (object instanceof StaticObject)
					((StaticObject) object).setBusy(false);
			}

			World.getInstance().removePlayer(this); // force remove in case of crash during teleport

			// friends & blocklist update
			notifyFriends(false);
			getBlockList().playerLogout();
		} catch (Exception e) {
			LOGGER.error("Couldn't disconnect correctly the player.", e);
		}
	}
	
	/**
	 * Teleport the current {@link Player} to the destination of another player.<br>
	 * Check if summoning is allowed, and consume items if {@link L2Skill} got such constraints.
	 * @param player : The player to teleport on.
	 * @param skill : The skill used to find item consumption informations.
	 */
	public void teleportToFriend(Player player, L2Skill skill) {
		if (player == null || skill == null)
			return;

		if (!player.canSummon() || !player.isTargetSummonable(this))
			return;

		final int itemConsumeId = skill.getTargetConsumeId();
		final int itemConsumeCount = skill.getTargetConsume();
		
		if (itemConsumeId != 0 && itemConsumeCount != 0) {
			if (getInventory().getInventoryItemCount(itemConsumeId, -1) < itemConsumeCount) {
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING).addItemName(skill.getTargetConsumeId()));
				return;
			}
			
			destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, this, true);
		}
		
		teleToLocation(player.getX(), player.getY(), player.getZ(), 20);
	}
	
	/** @return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127). */
	public int getEnchantEffect() {
		return (getActiveWeaponInstance() == null) ? 0 : Math.min(127, getActiveWeaponInstance().getEnchantLevel());
	}
	/** Helper */

	/** Store */
	public boolean canOpenPrivateStore() {
		if (getActiveTradeList() != null)
			cancelActiveTrade();

		return !isAlikeDead() && !isInOlympiadMode() && !isMounted() && !isInsideZone(ZoneId.NO_STORE) && !isCastingNow();
	}
	
	public void tryOpenPrivateBuyStore() {
		if (canOpenPrivateStore()) {
			if (getStoreType() == StoreType.BUY || getStoreType() == StoreType.BUY_MANAGE)
				setStoreType(StoreType.NONE);

			if (getStoreType() == StoreType.NONE) {
				standUp();
				setStoreType(StoreType.BUY_MANAGE);
				sendPacket(new PrivateStoreManageListBuy(this));
			}
		} else {
			if (isInsideZone(ZoneId.NO_STORE))
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);

			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public void tryOpenPrivateSellStore(boolean isPackageSale) {
		if (canOpenPrivateStore()) {
			if (getStoreType() == StoreType.SELL || getStoreType() == StoreType.SELL_MANAGE || getStoreType() == StoreType.PACKAGE_SELL)
				setStoreType(StoreType.NONE);
			
			if (getStoreType() == StoreType.NONE) {
				standUp();
				setStoreType(StoreType.SELL_MANAGE);
				sendPacket(new PrivateStoreManageListSell(this, isPackageSale));
			}
		} else {
			if (isInsideZone(ZoneId.NO_STORE))
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);

			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public void tryOpenWorkshop(boolean isDwarven) {
		if (canOpenPrivateStore()) {
			if (isInStoreMode())
				setStoreType(StoreType.NONE);

			if (getStoreType() == StoreType.NONE) {
				standUp();

				if (getCreateList() == null)
					setCreateList(new ManufactureList());

				sendPacket(new RecipeShopManageList(this, isDwarven));
			}
		} else {
			if (isInsideZone(ZoneId.NO_STORE))
				sendPacket(SystemMessageId.NO_PRIVATE_WORKSHOP_HERE);

			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	/** Store */

	/** Zones */
	public boolean isInsideSiegeOrPvPZone() {
		return isInsideSiegeZone() || isInsideZone(ZoneId.PVP);
	}
	
	public boolean isInsideRestrictedZone() {
		return isInsideSiegeZone() || isInsideAnyZone(ZoneId.PVP, ZoneId.PEACE);
	}
	
	/** Remove player from BossZones (used on char logout/exit) */
	public void removeFromBossZone() {
		for (BossZone zone : ZoneManager.getInstance().getAllZones(BossZone.class))
			zone.removePlayer(this);
	}
	/** Zones */
	
	/** PvP Actions */
	private void onDieDropItem(Player playerKiller, boolean killerPlayerExist, boolean isInRestrictedZone) {
		if ((killerPlayerExist && playerKiller.hasClan() && hasClan() && playerKiller.getClan().isAtWarWith(getClanId())) || (getPkKills() < Config.KARMA_PK_LIMIT) || isInRestrictedZone || hasExactVipLevel(3))
			return;

		if (hasKarma()) {
			handleDropItems(playerKiller, Config.KARMA_DROP_LIMIT, Config.KARMA_RATE_DROP, Config.KARMA_RATE_DROP_ITEM, Config.KARMA_RATE_DROP_EQUIP, Config.KARMA_RATE_DROP_EQUIP_WEAPON);
		} else if (hasSkill(L2Skill.ANTI_DROP)) {
			int skillLevel = getSkillLevel(L2Skill.ANTI_DROP) - 1;
			handleDropItems(playerKiller, ANTI_DROP_VALUES[skillLevel][0], ANTI_DROP_VALUES[skillLevel][1], ANTI_DROP_VALUES[skillLevel][2], ANTI_DROP_VALUES[skillLevel][3], ANTI_DROP_VALUES[skillLevel][4]);
		} else {
			handleDropItems(playerKiller, Config.PLAYER_DROP_LIMIT, Config.PLAYER_RATE_DROP, Config.PLAYER_RATE_DROP_ITEM, Config.PLAYER_RATE_DROP_EQUIP, Config.PLAYER_RATE_DROP_EQUIP_WEAPON);
		}
	}
	
	private void handleDropItems(Player playerKiller, int dropLimit, int dropPercent, int dropItem, int dropEquip, int dropEquipWeapon) {
		if (dropPercent > 0 && Rnd.isLessThanRandom(dropPercent)) {
			int dropCount = 0;
			int itemDropPercent = 0;

			for (ItemInstance itemDrop : getInventory().getItems()) {
				int itemId = itemDrop.getItemId();
				Item item = itemDrop.getItem();
				Summon pet = getPet();
				
				if (!itemDrop.isDropable() || itemId == ClassMaster.GOLD_ITEM_ID || item.getType2() == Item.TYPE2_QUEST || pet != null && pet.getControlItemId() == itemId || 
					ArraysUtil.containsItem(Config.KARMA_LIST_NONDROPPABLE_ITEMS, itemId) || ArraysUtil.containsItem(Config.KARMA_LIST_NONDROPPABLE_PET_ITEMS, itemId))
					continue;
				
				itemDropPercent = itemDrop.isEquipped() ? (item.getType2() == Item.TYPE2_WEAPON ? dropEquipWeapon : dropEquip) : dropItem;

				if (Rnd.isLessThanRandom(itemDropPercent)) {
					dropItem("DieDrop", itemDrop, playerKiller, true);
					
					if (++dropCount >= dropLimit)
						break;
				}
			}
		}
	}
	
	public void handlePlayerKiller(Creature killer, Player playerKiller, boolean isInRestrictedZone) {
		boolean killerPlayerExist = playerKiller != null;
		handleCursedWeapon(playerKiller, killer, killerPlayerExist, isInRestrictedZone);
		updateDeathPenalty(killer, killerPlayerExist, isInRestrictedZone);
		onDieDropItem(playerKiller, killerPlayerExist, isInRestrictedZone);
		handlePvPBlock(playerKiller, killerPlayerExist, isInRestrictedZone);
		EventBase.getEventInstance(eventInstance -> eventInstance.onKill(playerKiller, this, killerPlayerExist));
	}
	
	public void handleCursedWeapon(Player playerKiller, Creature killer, boolean killerPlayerExist, boolean isInRestrictedZone) {
		if (isCursedWeaponEquipped()) {
			CursedWeaponManager.getInstance().drop(_cursedWeaponEquippedId, killer);
			return;
		}
		
		if (killerPlayerExist && !playerKiller.isCursedWeaponEquipped() && !isInRestrictedZone && playerKiller.hasClan() && hasClan() && !isAcademyMember() && !playerKiller.isAcademyMember()) {
			if (_clan.isAtWarWith(playerKiller.getClanId()) && playerKiller.getClan().isAtWarWith(_clan.getClanId())) {
				if (getClan().getReputationScore() > 0)
					playerKiller.getClan().addReputationScore(1);

				if (playerKiller.getClan().getReputationScore() > 0)
					_clan.takeReputationScore(1);
			}
		}
		
		applyDeathExperienceLoss();
	}
	
	public void handlePvPBlock(Player playerKiller, boolean killerPlayerExist, boolean isInRestrictedZone) {
		if (isMinLevel() && pvpActivityBlock && killerPlayerExist && !isInRestrictedZone) {
			KILLED_PLAYERS.put(playerKiller.getObjectId(), getObjectId());
			TIME_TO_NEXT_KILL.put(playerKiller.getObjectId(), System.currentTimeMillis());
			blockPvPActivity();
	
			if (Config.ENABLE_PVP_REWARD)
				currentSpreeKills = 0;

			if (Config.ENABLE_PK_REWARD)
				giveReward(RewardName.PK);

			if (!isPvPCounterReady(playerKiller, false))
				bonusForDeath(playerKiller, false, (hasKarma() || isFlagged()) ? Rnd.get(Math.min(playerKiller.getLevel(), getLevel()), Math.max(playerKiller.getLevel(), getLevel())) : getRecomHave() / 10);
			
			addKills(-1);
		}

		increaseAndUpdate(false);
	}
	
	private void increaseAndUpdate(boolean onKill) {
		if (onKill) {
			increasePvpKills();
			handleProgression(this, AchievementType.PVP, QuestIdType.PVP);
		} else {
			increasePkKills();
			handleProgression(this, AchievementType.DEATHS, QuestIdType.DEATH);
		}

		updateColor(false);
	}
	
	/**
	 * This method is used to update PvP counter, or PK counter / add Karma if necessary.<br>
	 * It also updates clan kills/deaths counters on siege.
	 * @param target The Playable victim.
	 */
	public void onKillUpdatePvPKarma(Player targetPlayer) {
		if (Config.SAME_KILLINARAW_REWARD_BLOCK) {
			if ((TIME_TO_NEXT_KILL.getOrDefault(getObjectId(), -1L) + Constant.NUMBER_60000) > System.currentTimeMillis()) {
				sendMessage("It is impossible to receive a reward for the player if he was killed less than 1 minute ago.");
				return;
			}
			
			if (targetPlayer.getObjectId() == KILLED_PLAYERS.getOrDefault(getObjectId(), -1)) {
				sendMessage("It is impossible to receive a reward for the same player twice in a row.");
				return;
			}
		}
		
		if (Config.SAME_IP_REWARD_BLOCK) {
			if (getClient().getConnection().getInetAddress().getHostAddress().equals(targetPlayer.getClient().getConnection().getInetAddress().getHostAddress())) {
				sendMessage("Impossible to get a reward because you have same IP as a target player.");
				return;
			}
		}

		if (Config.SAME_CLANALLY_REWARD_BLOCK) {
			if ((getClanId() > 0 && targetPlayer.getClanId() > 0 && getClanId() == targetPlayer.getClanId()) || (getAllyId() > 0 && targetPlayer.getAllyId() > 0 && getAllyId() == targetPlayer.getAllyId())) {
				sendMessage("Impossible to get a reward because you and a target player from same Guild or Alliance.");
				return;
			}
		}

		if (Config.SAME_PARTY_REWARD_BLOCK) {
			if (isInParty() && targetPlayer.isInParty() && getParty().getLeaderObjectId() == targetPlayer.getParty().getLeaderObjectId()) {
				sendMessage("Impossible to get a reward because you and a target player from same Party.");
				return;
			}
		}

		if (isCursedWeaponEquipped()) {
			CursedWeaponManager.getInstance().increaseKills(_cursedWeaponEquippedId);
			return;
		}

		if (isInDuel() && targetPlayer.isInDuel())
			return;
		
		if (isInsideZone(ZoneId.PVP) && targetPlayer.isInsideZone(ZoneId.PVP)) {
			byte playerState = getSiegeState();
			byte targetState = targetPlayer.getSiegeState();
			
			if (playerState > 0 && targetState > 0 && playerState != targetState) {
				if (hasClan())
					getClan().setSiegeKills(getClan().getSiegeKills() + 1);

				if (targetPlayer.hasClan())
					targetPlayer.getClan().setSiegeDeaths(targetPlayer.getClan().getSiegeDeaths() + 1);
			}
			
			return;
		}

		if (isMinLevel() && pvpActivityBlock) {
			blockPvPActivity();
			addKills(10);

			if (!isPvPCounterReady(targetPlayer, true)) {
				if (isSameFaction(targetPlayer) && !targetPlayer.hasKarma() && !targetPlayer.isFlagged()) {
					if (hasDeathPenaltyBuff())
						startAbnormalEffect(AbnormalEffect.BIG_HEAD);

					setKarma(getKarma() + Formulas.calculateKarmaGain(getPkKills(), false, getLevel()));
					updateDeathPenalty(this, false, isInsideRestrictedZone());
					handleProgression(this, AchievementType.PK, QuestIdType.PK);
				}

				if (checkLevelsRange(getLevel(), targetPlayer.getLevel()))
					bonusForKill(targetPlayer, false, targetPlayer.getRecomHave() / 10);
			}
			
			if (Config.ENABLE_PVP_REWARD) {
				giveReward(RewardName.PVP);
				spreeCounter();
				spreeReward();
			}
		}

		increaseAndUpdate(true);
	}
	
	private boolean isPvPCounterReady(Player targetOrKiller, boolean onKill) {
		boolean canIncrement = targetOrKiller.getCounterManager().increment(CounterType.PVP);

		if (canIncrement) {
			if (onKill)
				bonusForKill(targetOrKiller, true, Constant.NUMBER_100);
			else
				bonusForDeath(targetOrKiller, true, Constant.NUMBER_50);
		}

		return canIncrement;
	}
	
	private void bonusForKill(Player target, boolean recLeft, int bonus) {
		if (Formulas.isInsideSameZone(this, target) && !recLeft)
			bonus *= 2;

		if (target.isHero())
			bonus += 30;

		if (target.isClanLeader())
			bonus += 20;

		if (target.isNoble())
			bonus += 10;

		if (recLeft)
			addRecomLeft(bonus);
		else
			addRecomHave(bonus);

		sendPacket(SystemMessage.getSystemMessage(recLeft ? SystemMessageId.CURRENTLY_GLORYP_S1 : SystemMessageId.CURRENTLY_HONOR_S1).addNumber(bonus));
	}
	
	private void bonusForDeath(Creature killer, boolean recLeft, int bonus) {
		if (Formulas.isInsideSameZone(killer, this) && !recLeft)
			bonus /= 2;

		if (recLeft)
			addRecomLeft(bonus);
		else
			addRecomHave(-bonus);

		sendPacket(SystemMessage.getSystemMessage(recLeft ? SystemMessageId.CURRENTLY_GLORYP_S1 : SystemMessageId.CURRENTLY_HONORM_S1).addNumber(recLeft ? bonus : -bonus));
	}
	
	public boolean checkLevelsRange(int playerLevel, int targetPlayerLevel) {
		return (targetPlayerLevel / 10 == playerLevel / 10) && VillageMaster.isWithinRange(targetPlayerLevel, ClassMaster.MIN_LVL, ClassMaster.MAX_LVL);
	}
	
	public void updatePvPStatus() {
		if (isInsideZone(ZoneId.PVP))
			return;

		PvpFlagTaskManager.getInstance().add(this, Config.PVP_NORMAL_TIME);

		if (!isFlagged())
			updatePvPFlag(1);
	}
	
	public void updatePvPStatus(Creature targetPlayer) {
		final Player target = targetPlayer.getActingPlayer();

		if (target == null)
			return;

		if (isInDuel() && target.getDuelId() == getDuelId())
			return;
		
		if (!isSameFaction(target))
			return;

		if ((!isInsideZone(ZoneId.PVP) || !target.isInsideZone(ZoneId.PVP)) && !target.hasKarma()) {
			PvpFlagTaskManager.getInstance().add(this, checkIfPvP(target) ? Config.PVP_PVP_TIME : Config.PVP_NORMAL_TIME);

			if (!isFlagged())
				updatePvPFlag(1);
		}
	}
	/** PvP Actions */
	
	/**
	 * Kill the Creature, Apply Death Penalty, Manage gain/loss Karma and Item Drop.
	 * Reduce the Experience of the Player in function of the calculated Death Penalty
	 * If necessary, unsummon the Pet of the killed Player
	 * Manage Karma gain for attacker and Karma loss for the killed Player
	 * If the killed Player has Karma, manage Drop Item
	 * Kill the Player
	 * @param killer The Creature who attacks
	 */
	@Override
	public boolean doDie(Creature killer) {
		if (!super.doDie(killer))
			return false;

		if (isMounted())
			stopFeed();

		synchronized (this) {
			if (isFakeDeath())
				stopFakeDeath(true);
		}

		boolean isInRestrictedZone = isInsideRestrictedZone();

		if (killer != null) {
			handlePlayerKiller(killer, killer.getActingPlayer(), isInRestrictedZone);
			setExpBeforeDeath(0);
		}

		if (isInDungeon())
			getDungeon().onPlayerDeath(this);

		if (isMinLevel() && !isInOlympiadMode() && !isInRestrictedZone)
			penaltyDeadPlayer(PunishLevel.DEAD, 5);

		unpolymorphFull();
		unSummonCubics();

		if (_fusionSkill != null)
			abortCast();

		for (Creature character : getKnownType(Creature.class))
			if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
				character.abortCast();

		WaterTaskManager.getInstance().remove(this);

		if (isPhoenixBlessed() || (isAffected(L2EffectFlag.CHARM_OF_COURAGE) && isInSiege()))
			reviveRequest(this, null, false);

		updateEffectIcons();
		return true;
	}
	
	@Override
	public void addFuncsToNewCharacter() {
		super.addFuncsToNewCharacter(); // Add Creature functionalities.
		addStatFunc(FuncHennaSTR.getInstance());
		addStatFunc(FuncHennaINT.getInstance());
		addStatFunc(FuncHennaDEX.getInstance());
		addStatFunc(FuncHennaWIT.getInstance());
		addStatFunc(FuncHennaCON.getInstance());
		addStatFunc(FuncHennaMEN.getInstance());
	}

	@Override
	protected void initCharStatusUpdateValues() {
		super.initCharStatusUpdateValues();
		_cpUpdateInterval = getMaxCp() / 352.0;
		_cpUpdateIncCheck = getMaxCp();
		_cpUpdateDecCheck = getMaxCp() - _cpUpdateInterval;
		_mpUpdateInterval = getMaxMp() / 352.0;
		_mpUpdateIncCheck = getMaxMp();
		_mpUpdateDecCheck = getMaxMp() - _mpUpdateInterval;
	}

	@Override
	public void initCharStat() {
		setStat(new PlayerStat(this));
	}

	@Override
	public final PlayerStat getStat() {
		return (PlayerStat) super.getStat();
	}

	@Override
	public void initCharStatus() {
		setStatus(new PlayerStatus(this));
	}

	@Override
	public final PlayerStatus getStatus() {
		return (PlayerStatus) super.getStatus();
	}

	@Override
	public final PlayerTemplate getTemplate() {
		return (PlayerTemplate) super.getTemplate();
	}

	@Override
	public CreatureAI getAI() {
		CreatureAI ai = _ai;

		if (ai == null) {
			synchronized (this) {
				if (_ai == null)
					_ai = new PlayerAI(this);

				return _ai;
			}
		}

		return ai;
	}

	@Override
	public final int getLevel() {
		return getStat().getLevel();
	}

	@Override
	public byte getPvpFlag() {
		return _pvpFlag;
	}

	@Override
	public boolean hasKarma() {
		return _karma > 0;
	}
	
	@Override
	public int getKarma() {
		return _karma;
	}

	@Override
	public void revalidateZone(boolean force) {
		super.revalidateZone(force);

		if (Config.ALLOW_WATER) {
			if (isInsideZone(ZoneId.WATER))
				WaterTaskManager.getInstance().add(this);
			else
				WaterTaskManager.getInstance().remove(this);
		}
		
		if (isInsideZone(ZoneId.SIEGE)) {
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
				return;

			_lastCompassZone = ExSetCompassZoneCode.SIEGEWARZONE2;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.SIEGEWARZONE2));
		} else if (isInsideZone(ZoneId.PVP)) {
			if (_lastCompassZone == ExSetCompassZoneCode.PVPZONE)
				return;

			_lastCompassZone = ExSetCompassZoneCode.PVPZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.PVPZONE));
		} else if (isInsideZone(ZoneId.PEACE)) {
			if (_lastCompassZone == ExSetCompassZoneCode.PEACEZONE)
				return;

			_lastCompassZone = ExSetCompassZoneCode.PEACEZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.PEACEZONE));
		} else {
			if (_lastCompassZone == ExSetCompassZoneCode.GENERALZONE)
				return;

			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
				updatePvPStatus();

			_lastCompassZone = ExSetCompassZoneCode.GENERALZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE));
		}
	}

	@Override
	public PcInventory getInventory() {
		return _inventory;
	}

	/**
	 * Destroys item from inventory and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param objectId int Item Instance identifier of the item to be destroyed
	 * @param count int Quantity of items to be destroyed
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItem(String process, int objectId, int count, WorldObject reference, boolean sendMessage) {
		final ItemInstance item = _inventory.getItemByObjectId(objectId);
		
		if (item == null) {
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

			return false;
		}

		return destroyItem(process, item, count, reference, sendMessage);
	}

	/**
	 * Destroy item from inventory by using its <B>itemId</B> and send InventoryUpdate packet to the Player.
	 * @param process String Identifier of process triggering this action
	 * @param itemId int Item identifier of the item to be destroyed
	 * @param count int Quantity of items to be destroyed
	 * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItemByItemId(String process, int itemId, int count, WorldObject reference, boolean sendMessage) {
		if (itemId == ClassMaster.GOLD_ITEM_ID)
			return reduceAdena(process, count, reference, sendMessage);

		ItemInstance item = _inventory.getItemByItemId(itemId);

		if (item == null || item.getCount() < count || _inventory.destroyItemByItemId(process, itemId, count, this, reference) == null) {
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

			return false;
		}
		
		inventoryUpdate(item); // Send inventory update packet
		statusLoadUpdate(); // Update current load as well
		_inventory.updateRuneBonus(item); // Update runes

		if (sendMessage) { // Sends message to client if requested
			if (count > 1)
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(itemId).addItemNumber(count));
			else
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(itemId));
		}
		
		return true;
	}

	@Override
	public final boolean isAlikeDead() {
		return super.isAlikeDead() || isFakeDeath();
	}

	@Override
	public void enableSkill(L2Skill skill) {
		super.enableSkill(skill);
		_reuseTimeStamps.remove(skill.getReuseHashCode());
	}

	@Override
	protected boolean checkDoCastConditions(L2Skill skill) {
		if (!super.checkDoCastConditions(skill))
			return false;

		L2SkillType skillType = skill.getSkillType();

		if (skillType == L2SkillType.SUMMON) { // Can't summon multiple servitors.
			if (!((L2SkillSummon) skill).isCubic() && (getPet() != null || isMounted())) {
				sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
				return false;
			}
		} else if (skillType == L2SkillType.RESURRECT) { // Can't use ressurect skills on siege if you are defender and control towers is not alive, if you are attacker and flag isn't spawned or if you aren't part of that siege.
			final Siege siege = CastleManager.getInstance().getActiveSiege(this);
			
			if (siege != null) {
				if (!hasClan()) {
					sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
					return false;
				}
				
				final SiegeSide side = siege.getSide(getClan());
				
				if (side == SiegeSide.DEFENDER || side == SiegeSide.OWNER) {
					if (siege.getControlTowerCount() == 0) {
						sendPacket(SystemMessageId.TOWER_DESTROYED_NO_RESURRECTION);
						return false;
					}
				} else if (side == SiegeSide.ATTACKER) {
					if (getClan().getFlag() == null) {
						sendPacket(SystemMessageId.NO_RESURRECTION_WITHOUT_BASE_CAMP);
						return false;
					}
				} else {
					sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
					return false;
				}
			}
		} else if (skillType == L2SkillType.SIGNET || skillType == L2SkillType.SIGNET_CASTTIME) { // Can't casting signets on peace zone.
			final WorldRegion region = getRegion();

			if (region == null)
				return false;

			if (!region.checkEffectRangeInsidePeaceZone(skill, (skill.getTargetType() == SkillTargetType.TARGET_GROUND) ? getCurrentSkillWorldPosition() : getPosition())) {
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
				return false;
			}
		}
		
		if (isInOlympiadMode() && (skill.isHeroSkill() || skillType == L2SkillType.RESURRECT)) { // Can't use Hero and resurrect skills during Olympiad
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return false;
		}
		
		if (skill.getMaxCharges() == 0 && getCharges() < skill.getNumCharges()) { // Check if the spell uses charges
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
			return false;
		}
		
		return true;
	}

	@Override
	public void onAction(Player player) {
		int playerObjectId = getObjectId();

		if (!EventBase.getEventInstance().onAction(player, playerObjectId))
			return;

		if (player.getTarget() != this) { // Set the target of the player
			player.setTarget(this);
		} else {
			if (isInStoreMode()) { // Check if this Player has a Private Store
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
				return;
			}
			
			if (isAutoAttackable(player)) { // Check if this Player is autoAttackable
				if ((isCursedWeaponEquipped() && player.levelLessThan(21)) || (player.isCursedWeaponEquipped() && levelLessThan(21))) { // Player with lvl < 21 can't attack a cursed weapon holder and a cursed weapon holder can't attack players with lvl < 21
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if (GeoEngine.getInstance().canSeeTarget(player, this)) {
					player.getAI().setIntention(CtrlIntention.ATTACK, this);
					player.onActionRequest();
				}
			} else {
				player.sendPacket(ActionFailed.STATIC_PACKET); // avoids to stuck when clicking two or more times
				
				if (player != this && GeoEngine.getInstance().canSeeTarget(player, this))
					player.getAI().setIntention(CtrlIntention.FOLLOW, this);
			}
		}
	}

	@Override
	public void onActionShift(Player player) {
		if (player.isGM())
			AdminEditChar.showCharacterInfo(player, this);

		super.onActionShift(player);
	}

	/**
	 * Send packet StatusUpdate with current HP,MP and CP to the Player and only current HP, MP and Level to all other Player of the Party.
	 * Send StatusUpdate with current HP, MP and CP to this Player
	 * Send PartySmallWindowUpdate with current HP, MP and Level to all other Player of the Party
	 * This method DOESN'T SEND current HP and MP to all Player of the _statusListener
	 */
	@Override
	public void broadcastStatusUpdate() {
		StatusUpdate su = new StatusUpdate(this); // Send StatusUpdate with current HP, MP and CP to this Player
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		sendPacket(su);
		final boolean needCpUpdate = needCpUpdate(352);
		final boolean needHpUpdate = needHpUpdate(352);
		
		if (isInParty() && (needCpUpdate || needHpUpdate || needMpUpdate(352))) // Check if a party is in progress and party window update is needed.
			_party.broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));

		if (isInOlympiadMode() && isOlympiadStart() && (needCpUpdate || needHpUpdate)) {
			final OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(getOlympiadGameId());

			if (game != null && game.isBattleStarted())
				game.getZone().broadcastStatusUpdate(this);
		}
		
		if (isInDuel() && (needCpUpdate || needHpUpdate)) { // In duel, MP updated only with CP or HP
			ExDuelUpdateUserInfo update = new ExDuelUpdateUserInfo(this);
			DuelManager.getInstance().broadcastToOppositeTeam(this, update);
		}
	}

	/** Send a packet to the Player. */
	@Override
	public void sendPacket(L2GameServerPacket packet) {
		if (_client != null)
			_client.sendPacket(packet);
	}

	/** Send SystemMessage packet. @param id SystemMessageId */
	@Override
	public void sendPacket(SystemMessageId id) {
		sendPacket(SystemMessage.getSystemMessage(id));
	}

	/**
	 * Manage Pickup Task.
	 * Send StopMove to this Player
	 * Remove the ItemInstance from the world and send GetItem packets
	 * Send a System Message to the Player : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2
	 * Add the Item to the Player inventory
	 * Send InventoryUpdate to this Player with NewItem (use a new slot) or ModifiedItem (increase amount)
	 * Send StatusUpdate to this Player with current weight
	 * If a Party is in progress, distribute Items between party members
	 * @param object The ItemInstance to pick up
	 */
	@Override
	public void doPickupItem(WorldObject object) {
		if (isAlikeDead() || isFakeDeath())
			return;
		
		getAI().setIntention(CtrlIntention.IDLE); // Set the AI Intention to IDLE

		if (!(object instanceof ItemInstance)) // Check if the WorldObject to pick up is a ItemInstance
			return;
		
		ItemInstance item = (ItemInstance) object;
		sendPacket(ActionFailed.STATIC_PACKET);
		sendPacket(new StopMove(this));
		
		synchronized (item) {
			if (!item.isVisible())
				return;
			
			if (isInStoreMode())
				return;
			
			if (!_inventory.validateWeight(item.getCount() * item.getItem().getWeight())) {
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
				return;
			}
			
			if (((isInParty() && getParty().getLootRule() == LootRule.ITEM_LOOTER) || !isInParty()) && !_inventory.validateCapacity(item)) {
				sendPacket(SystemMessageId.SLOTS_FULL);
				return;
			}
			
			if (getActiveTradeList() != null) {
				sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
				return;
			}
			
			if (item.getOwnerId() != 0 && !isLooterOrInLooterParty(item.getOwnerId())) {
				if (item.getItemId() == ClassMaster.GOLD_ITEM_ID)
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA).addNumber(item.getCount()));
				else if (item.getCount() > 1)
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S).addItemName(item).addNumber(item.getCount()));
				else
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(item));

				return;
			}
			
			if (item.hasDropProtection())
				item.removeDropProtection();

			item.pickupMe(this); // Remove the ItemInstance from the world and send GetItem packets
			ItemsOnGroundTaskManager.getInstance().remove(item); // item must be removed from ItemsOnGroundManager if is active
		}
		
		if (item.isHerb()) { // Auto use herbs - pick up
			IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());

			if (handler != null)
				handler.useItem(this, item, false);

			item.destroyMe("Consume", this, null);
		} else if (CursedWeaponManager.getInstance().isCursed(item.getItemId())) { // Cursed Weapons are not distributed
			addItem("Pickup", item, null, true);
		} else {
			if (item.getItemType() instanceof ArmorType || item.getItemType() instanceof WeaponType) { // if item is instance of L2ArmorType or WeaponType broadcast an "Attention" system message
				SystemMessage msg;

				if (item.getEnchantLevel() > 0)
					msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2_S3).addString(getName()).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
				else
					msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2).addString(getName()).addItemName(item.getItemId());

				broadcastPacket(msg, 1400);
			}
			
			if (isInParty()) { // Check if a Party is in progress
				getParty().distributeItem(this, item);
			} else if (item.getItemId() == ClassMaster.GOLD_ITEM_ID && getInventory().getAdenaInstance() != null) { // Target is adena
				addAdena("Pickup", item.getCount(), null, true);
				item.destroyMe("Pickup", this, null);
			} else { // Target is regular item
				addItem("Pickup", item, null, true);
			}
		}
		
		ThreadPool.schedule(() -> setIsParalyzed(false), (int) (700 / getStat().getMovementSpeedMultiplier())); // Schedule a paralyzed task to wait for the animation to finish
		setIsParalyzed(true);
	}

	@Override
	public void doAttack(Creature target) {
		super.doAttack(target);
		clearRecentFakeDeath();
	}

	@Override
	public void doCast(L2Skill skill) {
		super.doCast(skill);
		clearRecentFakeDeath();
	}

	@Override
	public void setTarget(WorldObject newTarget) {
		boolean newTargetIsPlayer = newTarget.isInstanceOfPlayer();

		if (newTargetIsPlayer) { /// TODO : TEST Get the stack trace
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			StackTraceElement callingMethod = stackTrace[2]; // The calling method will be at index 2 in the stack trace
			System.out.println("Called from class: " + callingMethod.getClassName());
			System.out.println("Called from method: " + callingMethod.getMethodName());
		} /// TODO : TEST Get the stack trace

		/*if (getName().equals("Mage") && getTarget() == null || getName().equals("Fighter") && getTarget() == null) {
			Thread.dumpStack();
			System.out.println("Player");
		}*/

		//STOP SKILL IF TARGET IS PLAYER. IN THIS EXAMPLE STOP IF NOT MONSTER
		/*if (!(newTarget instanceof Monster) && newTarget != null && newTarget != this && getFirstEffect(7029) != null) { //newTarget.isInstanceOfPlayer()
			stopSkillEffects(7029);
			//if (&& hasSkill(7029))
		}*/

		if (newTargetIsPlayer && getTarget() == null && !hasKarma() && !isFlagged() && !isSameFaction(((Player) newTarget)) && !((Player) newTarget).isInsideZone(ZoneId.PEACE)) // disable auto target
			newTarget = this;

		if (newTarget != null && !newTarget.isVisible() && !(newTargetIsPlayer && isInParty() && _party.containsPlayer(newTarget))) // Check if the new target is visible.
			newTarget = null;

		WorldObject oldTarget = getTarget(); // Get the current target

		if (oldTarget != null) {
			if (oldTarget.equals(newTarget)) // no target change
				return;

			if (oldTarget instanceof Creature) // Remove the Player from the _statusListener of the old target if it was a Creature
				((Creature) oldTarget).removeStatusListener(this);
		}
		
		if (newTarget instanceof StaticObject) { // Verify if it's a static object.
			sendPacket(new MyTargetSelected(newTarget.getObjectId(), 0));
			sendPacket(new StaticObjectInfo((StaticObject) newTarget));
		} else if (newTarget instanceof Creature) { // Add the Player to the _statusListener of the new target if it's a Creature
			final Creature target = (Creature) newTarget;

			if (newTarget.getObjectId() != getObjectId()) // Validate location of the new target.
				sendPacket(new ValidateLocation(target));

			sendPacket(new MyTargetSelected(target.getObjectId(), (target.isAutoAttackable(this) || target instanceof Summon) ? getLevel() - target.getLevel() : 0)); // Show the client his new target.
			target.addStatusListener(this);
			final StatusUpdate su = new StatusUpdate(target); // Send max/current hp.
			su.addAttribute(StatusUpdate.MAX_HP, target.getMaxHp());
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			sendPacket(su);
			Broadcast.toKnownPlayers(this, new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ()));
		}
		
		if (newTarget instanceof Folk) {
			setCurrentFolk((Folk) newTarget);
		} else if (newTarget == null) {
			sendPacket(ActionFailed.STATIC_PACKET);
			
			if (getTarget() != null) {
				broadcastPacket(new TargetUnselected(this));
				setCurrentFolk(null);
			}
		}
		
		super.setTarget(newTarget); // Target the new WorldObject
	}

	/** Return the active weapon instance (always equipped in the right hand). */
	@Override
	public ItemInstance getActiveWeaponInstance() {
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}

	/** Return the active weapon item (always equipped in the right hand). */
	@Override
	public Weapon getActiveWeaponItem() {
		return (getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND) == null) ? getTemplate().getFists() : (Weapon) getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND).getItem();
	}

	/** @return the type of attack, depending of the worn weapon. */
	@Override
	public WeaponType getAttackType() {
		return getActiveWeaponItem().getItemType();
	}

	/** Return the secondary weapon instance (always equipped in the left hand). */
	@Override
	public ItemInstance getSecondaryWeaponInstance() {
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}

	/** Return the secondary L2Item item (always equiped in the left hand). */
	@Override
	public Item getSecondaryWeaponItem() {
		ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		return (item != null) ? item.getItem() : null;
	}

	@Override
	public Summon getPet() {
		return _summon;
	}

	@Override
	public boolean isInvul() {
		return super.isInvul() || isSpawnProtected();
	}

	@Override
	public boolean isInParty() {
		return _party != null;
	}

	@Override
	public Party getParty() {
		return _party;
	}

	@Override
	public boolean isGM() {
		return getAccessLevel().isGm();
	}

	/**
	 * Return True if the Player is autoAttackable.
	 * Check if the attacker isn't the Player Pet
	 * Check if the attacker is L2MonsterInstance
	 * If the attacker is a Player, check if it is not in the same party
	 * Check if the Player has Karma
	 * If the attacker is a Player, check if it is not in the same siege clan (Attacker, Defender)
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		if (attacker == this || attacker == getPet())
			return false;

		if (attacker instanceof Attackable)
			return true;

		if (isInParty() && _party.containsPlayer(attacker))
			return false;

		final Siege siege = CastleManager.getInstance().getActiveSiege(this);
		
		if (attacker instanceof Playable) {
			if (isInsideZone(ZoneId.PEACE))
				return false;

			final Player attackerPlayer = attacker.getActingPlayer();

			if (!isSameFaction(attackerPlayer))
				return true;

			if (getDuelState() == DuelState.DUELLING && getDuelId() == attackerPlayer.getDuelId()) // is AutoAttackable if both players are in the same duel and the duel is still going on
				return true;

			if (attackerPlayer.isInOlympiadMode()) // Check if the attacker is in olympiad and olympiad start
				return isInOlympiadMode() && isOlympiadStart() && attackerPlayer.getOlympiadGameId() == getOlympiadGameId();

			if (!EventBase.isPlayerParticipant(getObjectId()) && (isInArena() && attacker.isInArena()))
				return true;

			if (EventBase.isStartedAndParticipant(getObjectId()) && !TMEvent.isInSameTeam(getObjectId(), attackerPlayer.getObjectId()))
				return true;

			if (hasClan()) {
				Clan clan = getClan();

				if (siege != null) {
					if (siege.checkSides(attackerPlayer.getClan(), SiegeSide.DEFENDER, SiegeSide.OWNER) && siege.checkSides(clan, SiegeSide.DEFENDER, SiegeSide.OWNER)) // Check if a siege is in progress and if attacker and the Player aren't in the Defender clan.
						return false;

					if (siege.checkSide(attackerPlayer.getClan(), SiegeSide.ATTACKER) && siege.checkSide(clan, SiegeSide.ATTACKER)) // Check if a siege is in progress and if attacker and the Player aren't in the Attacker clan.
						return false;
				}

				if (clan.isAtWarWith(attackerPlayer.getClanId()) && !wantsPeace() && !attackerPlayer.wantsPeace() && !isAcademyMember()) // Check if clan is at war.
					return true;

				if (clan.isMember(attackerPlayer.getObjectId())) // Check if the attacker is not in the same clan.
					return false;

				if (getAllyId() != 0 && getAllyId() == attackerPlayer.getAllyId()) // Check if the attacker is not in the same ally.
					return false;
			}
		}

		if (attacker instanceof SiegeGuard)
			return hasClan() && siege != null && siege.checkSide(getClan(), SiegeSide.ATTACKER);

		return hasKarma() || isFlagged();
	}

	/**
	 * Check if the active L2Skill can be casted.
	 * Check if the skill isn't toggle and is offensive
	 * Check if the target is in the skill cast range
	 * Check if the skill is Spoil type and if the target isn't already spoiled
	 * Check if the caster owns enought consummed Item, enough HP and MP to cast the skill
	 * Check if the caster isn't sitting
	 * Check if all skills are enabled and this skill is enabled
	 * Check if the caster own the weapon needed
	 * Check if the skill is active
	 * Check if all casting conditions are completed
	 * Notify the AI with CAST and target
	 * @param skill The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 */
	@Override
	public boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove) {
		if (skill.isPassive()) { // Check if the skill is active
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (isSkillDisabled(skill)) // Check if this skill is enabled (ex : reuse time)
			return false;
		
		// Cancels the use of skills when player uses a cursed weapon or is flying.
		if ((isCursedWeaponEquipped() && !skill.isDemonicSkill()) || // If CW, allow ONLY demonic skills.
			(getMountType() == 1 && !skill.isStriderSkill()) || // If mounted, allow ONLY Strider skills.
			(getMountType() == 2 && !skill.isFlyingSkill())) { // If flying, allow ONLY Wyvern skills.
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		final ItemInstance formal = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST); // Players wearing Formal Wear cannot use skills.
		
		if (formal != null && formal.getItem().getBodyPart() == Item.SLOT_ALLDRESS) {
			sendPacket(SystemMessageId.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// *** Check Casting in Progress *** //
		if (isCastingNow()) { // If a skill is currently being used, queue this one if this is not the same
			if (_currentSkill.getSkill() != null && skill.getId() != _currentSkill.getSkillId()) // Check if new skill different from current skill in progress ; queue it in the player _queuedSkill
				setQueuedSkill(skill, forceUse, dontMove);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		setIsCastingNow(true);
		setCurrentSkill(skill, forceUse, dontMove); // Set the player _currentSkill.

		if (_queuedSkill.getSkill() != null) // Wipe queued skill.
			setQueuedSkill(null, false, false);
		
		if (!checkUseMagicConditions(skill, forceUse, dontMove)) {
			setIsCastingNow(false);
			return false;
		}

		WorldObject target = null; // Check if the target is correct and Notify the AI with CAST and target

		switch (skill.getTargetType()) {
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_GROUND:
			case TARGET_SELF:
			case TARGET_CORPSE_ALLY:
			case TARGET_AURA_UNDEAD:
				target = this;
				break;
			
			default: // Get the first target of the list
				target = skill.getFirstOfTargetList(this);
		}
		
		// Notify the AI with CAST and target
		getAI().setIntention(CtrlIntention.CAST, skill, target);
		return true;
	}

	@Override
	public boolean isSeated() {
		return _throneId > 0;
	}

	@Override
	public boolean isRiding() {
		return _mountType == 1;
	}

	@Override
	public boolean isFlying() {
		return _mountType == 2;
	}

	@Override
	public final void stopAllEffects() {
		super.stopAllEffects();
		updateAndBroadcastStatus(2);
	}

	@Override
	public final void stopAllEffectsExceptThoseThatLastThroughDeath() {
		super.stopAllEffectsExceptThoseThatLastThroughDeath();
		updateAndBroadcastStatus(2);
	}

	/**
	 * Send UserInfo to this Player and CharInfo to all Player in its _KnownPlayers.<BR>
	 * Send UserInfo to this Player (Public and Private Data)
	 * Send CharInfo to all Player in _KnownPlayers of the Player (Public data only)
	 * DON'T SEND UserInfo packet to other players instead of CharInfo packet. Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...<BR>
	 */
	@Override
	public void updateAbnormalEffect() {
		broadcastUserInfo();
	}

	@Override
	public String toString() {
		return getName() + " (" + getObjectId() + ")";
	}

	@Override
	public boolean isChargedShot(ShotType type) {
		return getActiveWeaponInstance() != null && getActiveWeaponInstance().isChargedShot(type);
	}

	@Override
	public void setChargedShot(ShotType type, boolean charged) {
		if (getActiveWeaponInstance() != null)
			getActiveWeaponInstance().setChargedShot(type, charged);
	}

	@Override
	public void rechargeShots(boolean physical, boolean magic) {
		if (_activeSoulShots.isEmpty())
			return;

		for (int itemId : _activeSoulShots) {
			ItemInstance item = getInventory().getItemByItemId(itemId);
			
			if (item != null) {
				rechargeShots(item, magic, ActionType.spiritshot);
				rechargeShots(item, physical, ActionType.soulshot);
			} else {
				removeAutoSoulShot(itemId);
			}
		}
	}

	@Override
	public void sendMessage(String message) {
		sendPacket(SystemMessage.sendString(message));
	}

	@Override
	public void doRevive() {
		super.doRevive();
		stopEffects(L2EffectType.CHARMOFCOURAGE);
		sendPacket(new EtcStatusUpdate(this));
		broadcastUserInfo();
		_reviveRequested = 0;
		_revivePower = 0;

		if (isMounted())
			startFeed(_mountNpcId);

		if (getPunishLevel() == PunishLevel.DEAD && getPunishTimer() > 0) {
			penaltyDeadPlayer(PunishLevel.NONE, 0);
			stopPunishTask(true);
		}

		ThreadPool.schedule(() -> setIsParalyzed(false), (int) (2000 / getStat().getMovementSpeedMultiplier())); // Schedule a paralyzed task to wait for the animation to finish
		setIsParalyzed(true);
	}

	@Override
	public void doRevive(double revivePower) {
		restoreExp(revivePower); // Restore the player's lost experience, depending on the % return of the skill used (based on its power).
		doRevive();
	}

	@Override
	public final void onTeleported() {
		super.onTeleported();
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0) {
			setSpawnProtection(true);
			startAbnormalEffect(ABNORMAL_EFFECT_TELEPORT);
		}

		if (!isGM())
			stopAllToggles();

		if (getTrainedBeast() != null) {
			getTrainedBeast().getAI().stopFollow();
			getTrainedBeast().teleToLocation(getPosition(), 0);
			getTrainedBeast().getAI().startFollow(this);
		}
		
		Summon pet = getPet();
		
		if (pet != null) {
			pet.setFollowStatus(false);
			pet.teleToLocation(getPosition(), 0);
			((SummonAI) pet.getAI()).setStartFollowController(true);
			pet.setFollowStatus(true);
		}

		EventBase.onTeleported(this);
	}

	@Override
	public void addExpAndSp(long addToExp, int addToSp) {
		addExpAndSp(addToExp, addToSp, null);
	}

	@Override
	public void reduceCurrentHp(double value, Creature attacker, boolean awake, boolean isDOT, L2Skill skill) {
		if (skill != null)
			getStatus().reduceHp(value, attacker, awake, isDOT, skill.isToggle(), skill.getDmgDirectlyToHP());
		else
			getStatus().reduceHp(value, attacker, awake, isDOT, false, false);

		if (getTrainedBeast() != null) // notify the tamed beast of attacks
			getTrainedBeast().onOwnerGotAttacked(attacker);
	}

	/**
	 * Manage the delete task of a Player (Leave Party, Unsummon pet, Save its inventory in the database, Remove it from the world...).
	 * If the Player is in observer mode, set its position to its position before entering in observer mode
	 * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess
	 * Stop the HP/MP/CP Regeneration task
	 * Cancel Crafting, Attak or Cast
	 * Remove the Player from the world
	 * Stop Party and Unsummon Pet
	 * Update database with items in its inventory and remove them from the world
	 * Remove the object from region
	 * Close the connection with the client
	 */
	@Override
	public void deleteMe() {
		cleanup();
		store();
		super.deleteMe();
	}

	@Override
	public Map<Integer, L2Skill> getSkills() {
		return _skills;
	}

	@Override
	public boolean isCursedWeaponEquipped() {
		return _cursedWeaponEquippedId != 0;
	}

	@Override
	public void addTimeStamp(L2Skill skill, long reuse) {
		_reuseTimeStamps.put(skill.getReuseHashCode(), new Timestamp(skill, reuse));
	}

	@Override
	public Player getActingPlayer() {
		return this;
	}

	@Override
	public final void sendDamageMessage(Creature target, int damage, boolean mcrit, boolean pcrit, boolean miss) {
		if (miss) {
			sendPacket(SystemMessageId.MISSED_TARGET);
			return;
		}

		if (pcrit)
			sendPacket(SystemMessageId.CRITICAL_HIT);
		else if (mcrit)
			sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);

		if (target.isInvul())
			sendPacket(target.isParalyzed() ? SystemMessageId.OPPONENT_PETRIFIED : SystemMessageId.ATTACK_WAS_BLOCKED);
		else
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_S2_DID_S1_DMG).addNumber(damage).addCharName(target));

		if (isInOlympiadMode() && target.isInstanceOfPlayer() && ((Player) target).isInOlympiadMode() && ((Player) target).getOlympiadGameId() == getOlympiadGameId())
			OlympiadGameManager.getInstance().notifyCompetitorDamage(this, damage);
	}

	@Override
	public void broadcastRelationsChanges() {
		for (Player player : getKnownType(Player.class)) {
			final int relation = getRelation(player);
			final boolean isAutoAttackable = isAutoAttackable(player);
			player.sendPacket(new RelationChanged(this, relation, isAutoAttackable));
			
			if (getPet() != null)
				player.sendPacket(new RelationChanged(getPet(), relation, isAutoAttackable));
		}
	}

	@Override
	public void sendInfo(Player activeChar) {
		if (isInBoat())
			getPosition().set(getBoat().getPosition());

		if (isPolyTypeNPC()) {
			activeChar.sendPacket(sendMorphInfo());
		} else {
			activeChar.sendPacket(new CharInfo(this));
			
			if (isSeated()) {
				final WorldObject object = World.getInstance().getObject(_throneId);
				
				if (object instanceof StaticObject)
					activeChar.sendPacket(new ChairSit(getObjectId(), ((StaticObject) object).getStaticObjectId()));
			}
		}
		
		int relation = getRelation(activeChar);
		boolean isAutoAttackable = isAutoAttackable(activeChar);
		activeChar.sendPacket(new RelationChanged(this, relation, isAutoAttackable));
		
		if (getPet() != null)
			activeChar.sendPacket(new RelationChanged(getPet(), relation, isAutoAttackable));

		relation = activeChar.getRelation(this);
		isAutoAttackable = activeChar.isAutoAttackable(this);
		sendPacket(new RelationChanged(activeChar, relation, isAutoAttackable));
		
		if (activeChar.getPet() != null)
			sendPacket(new RelationChanged(activeChar.getPet(), relation, isAutoAttackable));

		if (isInBoat())
			activeChar.sendPacket(new GetOnVehicle(getObjectId(), getBoat().getObjectId(), getBoatPosition()));

		switch (getStoreType()) {
			case SELL:
			case PACKAGE_SELL:
				activeChar.sendPacket(new PrivateStoreMsgSell(this));
				return;
			case BUY:
				activeChar.sendPacket(new PrivateStoreMsgBuy(this));
				return;
			case MANUFACTURE:
				activeChar.sendPacket(new RecipeShopMsg(this));
			default:
				return;
		}
	}

	@Override
	public double getCollisionRadius() {
		return getBaseTemplate().getCollisionRadiusBySex(getAppearance().getSex());
	}

	@Override
	public double getCollisionHeight() {
		return getBaseTemplate().getCollisionHeightBySex(getAppearance().getSex());
	}

	@Override
	public void addKnownObject(WorldObject object) {
		sendInfoFrom(object);
	}

	@Override
	public void removeKnownObject(WorldObject object) {
		super.removeKnownObject(object);
		sendPacket(new DeleteObject(object, (object.isInstanceOfPlayer()) && ((Player) object).isSeated())); // send Server-Client Packet DeleteObject to the Player
	}

	@Override
	public void setFactionId(int id) {
		super.setFactionId(id);
		updateColor(false);
	}

	@Override
	public String getName() {
		return showMaskedName ? maskedName : super.getName();
	}

	@Override
	public String getTitle() {
		return showMaskedName ? maskedTitle : super.getTitle();
	}

	@Override
	public boolean polymorph(PolyType type, int npcId) {
		if (super.polymorph(type, npcId)) {
			sendUserInfo();
			return true;
		}
		
		return false;
	}

	@Override
	public void unpolymorph() {
		super.unpolymorph();
		sendUserInfo();
	}
	
	protected class FeedTask implements Runnable {
		@Override
		public void run() {
			if (!isMounted()) {
				stopFeed();
				return;
			}
			
			if (getCurrentFeed() > getFeedConsume()) { // Eat or return to pet control item.
				setCurrentFeed(getCurrentFeed() - getFeedConsume());
			} else {
				setCurrentFeed(0);
				stopFeed();
				dismount();
				sendPacket(SystemMessageId.OUT_OF_FEED_MOUNT_CANCELED);
				return;
			}

			ItemInstance food = getInventory().getItemByItemId(_petTemplate.getFood1());

			if (food == null)
				food = getInventory().getItemByItemId(_petTemplate.getFood2());

			if (food != null && checkFoodState(_petTemplate.getAutoFeedLimit())) {
				IItemHandler handler = ItemHandler.getInstance().getHandler(food.getEtcItem());

				if (handler != null) {
					handler.useItem(Player.this, food, false);
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY).addItemName(food));
				}
			}
		}
	}
}
