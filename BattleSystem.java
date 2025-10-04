public class BattleSystem {
	public final WorldObject[] getTargetList(Creature activeChar, boolean onlyFirst, Creature target) {
		switch (_targetType) {
			case TARGET_ONE:
				boolean canTargetSelf = false;

				switch (_skillType) {
					case BUFF:
					case HEAL:
					case HOT:
					case HEAL_PERCENT:
					case MANARECHARGE:
					case MANAHEAL:
					case NEGATE:
					case CANCEL_DEBUFF:
					case REFLECT:
					case COMBATPOINTHEAL:
					case SEED:
					case BALANCE_LIFE:
						canTargetSelf = true;
						
					default:
						break;
				}
				
				if (target == null || target.isDead() || (target == activeChar && !canTargetSelf)) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				return new Creature[] { target };
			case TARGET_SELF:
			case TARGET_GROUND:
				return new Creature[] { activeChar };
			case TARGET_HOLY:
				if (!(target instanceof HolyThing)) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				return new Creature[] { target };
			case TARGET_PET:
				target = activeChar.getPet();
				
				if (target != null && !target.isDead())
					return new Creature[] { target };
				
				return _emptyTargetList;
			case TARGET_SUMMON:
				target = activeChar.getPet();
				
				if (target != null && !target.isDead() && target instanceof Servitor)
					return new Creature[] { target };
				
				return _emptyTargetList;
			case TARGET_OWNER_PET:
				if (activeChar instanceof Summon) {
					target = activeChar.getActingPlayer();
					
					if (target != null && !target.isDead())
						return new Creature[] { target };
				}
				
				return _emptyTargetList;
			case TARGET_CORPSE_PET:
				if (activeChar instanceof Player) {
					target = activeChar.getPet();

					if (target != null && target.isDead())
						return new Creature[] { target };
				}
				
				return _emptyTargetList;
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA: {
				List<Creature> targetList = new ArrayList<>();

				// Go through the Creature knownList
				if (_skillType == L2SkillType.DUMMY) {
					if (onlyFirst)
						return new Creature[] { activeChar };

					final Player sourcePlayer = activeChar.getActingPlayer();
					targetList.add(activeChar);

					for (Creature obj : activeChar.getKnownTypeInRadius(Creature.class, _skillRadius)) {
						if (!(obj == activeChar || obj == sourcePlayer || obj instanceof Npc || obj instanceof Attackable))
							continue;

						targetList.add(obj);
					}
				} else {
					final boolean srcInArena = activeChar.isInArena();
					
					for (Creature obj : activeChar.getKnownTypeInRadius(Creature.class, _skillRadius)) {
						if (obj instanceof Attackable || obj instanceof Playable) {
							switch (_targetType) {
								case TARGET_FRONT_AURA:
									if (!obj.isInFrontOf(activeChar))
										continue;

									break;
								case TARGET_BEHIND_AURA:
									if (!obj.isBehind(activeChar))
										continue;

								default:
									break;
							}
							
							if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
								continue;

							if (onlyFirst)
								return new Creature[] { obj };

							targetList.add(obj);
						}
					}
				}
				
				return targetList.toArray(new Creature[targetList.size()]);
			}

			case TARGET_AREA_SUMMON: {
				target = activeChar.getPet();

				if (target == null || !(target instanceof Servitor) || target.isDead())
					return _emptyTargetList;

				if (onlyFirst)
					return new Creature[] { target };

				final boolean srcInArena = activeChar.isInArena();
				List<Creature> targetList = new ArrayList<>();
				
				for (Creature obj : target.getKnownType(Creature.class)) {
					if (obj == null || obj == target || obj == activeChar)
						continue;

					if (!MathUtil.checkIfInRange(_skillRadius, target, obj, true))
						continue;

					if (!(obj instanceof Attackable || obj instanceof Playable))
						continue;

					if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
						continue;

					targetList.add(obj);
				}

				if (targetList.isEmpty())
					return _emptyTargetList;

				return targetList.toArray(new Creature[targetList.size()]);
			}
			
			case TARGET_AREA:
			case TARGET_FRONT_AREA:
			case TARGET_BEHIND_AREA: {
				if (((target == null || target == activeChar || target.isAlikeDead()) && _castRange >= 0) || (!(target instanceof Attackable || target instanceof Playable))) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				final Creature origin;
				final boolean srcInArena = activeChar.isInArena();
				List<Creature> targetList = new ArrayList<>();

				if (_castRange >= 0) {
					if (!checkForAreaOffensiveSkills(activeChar, target, this, srcInArena))
						return _emptyTargetList;
					
					if (onlyFirst)
						return new Creature[] { target };
					
					origin = target;
					targetList.add(origin); // Add target to target list
				} else {
					origin = activeChar;
				}
				
				for (Creature obj : activeChar.getKnownType(Creature.class)) {
					if (!(obj instanceof Attackable || obj instanceof Playable))
						continue;

					if (obj == origin)
						continue;

					if (MathUtil.checkIfInRange(_skillRadius, origin, obj, true)) {
						switch (_targetType) {
							case TARGET_FRONT_AREA:
								if (!obj.isInFrontOf(activeChar))
									continue;

								break;
							case TARGET_BEHIND_AREA:
								if (!obj.isBehind(activeChar))
									continue;

							default:
								break;
						}
						
						if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
							continue;

						targetList.add(obj);
					}
				}
				
				if (targetList.isEmpty())
					return _emptyTargetList;

				return targetList.toArray(new Creature[targetList.size()]);
			}
			
			case TARGET_PARTY: {
				if (onlyFirst)
					return new Creature[] { activeChar };

				List<Creature> targetList = new ArrayList<>();
				targetList.add(activeChar);
				final int radius = _skillRadius;
				final Player player = activeChar.getActingPlayer();

				if (activeChar instanceof Summon) {
					if (addCharacter(activeChar, player, radius, false))
						targetList.add(player);
				} else if (activeChar instanceof Player) {
					if (addSummon(activeChar, player, radius, false))
						targetList.add(player.getPet());
				}

				final Party party = activeChar.getParty();

				if (party != null) {
					// Get a list of Party Members
					for (Player partyMember : party.getMembers()) {
						if (partyMember == player)
							continue;

						if (addCharacter(activeChar, partyMember, radius, false))
							targetList.add(partyMember);

						if (addSummon(activeChar, partyMember, radius, false))
							targetList.add(partyMember.getPet());
					}
				}
				
				return targetList.toArray(new Creature[targetList.size()]);
			}

			case TARGET_PARTY_MEMBER:
				if (target != null && (target == activeChar || (activeChar.isInParty() && target.isInParty() && activeChar.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId()) || (activeChar instanceof Player && target instanceof Summon && activeChar.getPet() == target) || (activeChar instanceof Summon && target instanceof Player && activeChar == target.getPet()))) {
					if (!target.isDead())
						return new Creature[] { target }; // If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT

					return _emptyTargetList;
				}

				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return _emptyTargetList;
			case TARGET_PARTY_OTHER:
				if (target != null && target != activeChar && activeChar.isInParty() && target.isInParty() && activeChar.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId()) {
					if (!target.isDead()) {
						if (target instanceof Player) {
							switch (getId()) {
								// FORCE BUFFS may cancel here but there should be a proper condition
								case 426:
									if (!((Player) target).isMageClass())
										return new Creature[] { target };

									return _emptyTargetList;
								case 427:
									if (((Player) target).isMageClass())
										return new Creature[] { target };

									return _emptyTargetList;
							}
						}
						
						return new Creature[] { target };
					}
					
					return _emptyTargetList;
				}
				
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return _emptyTargetList;
			case TARGET_ALLY: {
				final Player player = activeChar.getActingPlayer();

				if (player == null)
					return _emptyTargetList;

				if (onlyFirst || player.isInOlympiadMode())
					return new Creature[] { activeChar };

				List<Creature> targetList = new ArrayList<>();
				targetList.add(player);
				final int radius = _skillRadius;

				if (addSummon(activeChar, player, radius, false))
					targetList.add(player.getPet());

				if (player.getClan() != null) {
					for (Player obj : activeChar.getKnownTypeInRadius(Player.class, radius)) {
						if ((obj.getAllyId() == 0 || obj.getAllyId() != player.getAllyId()) && (obj.getClan() == null || obj.getClanId() != player.getClanId()))
							continue;

						if (player.isInDuel()) {
							if (player.getDuelId() != obj.getDuelId())
								continue;

							if (player.isInParty() && obj.isInParty() && player.getParty().getLeaderObjectId() != obj.getParty().getLeaderObjectId())
								continue;
						}

						if (!player.checkPvpSkill(obj, this))
							continue;

						if (blockEventSkill(player, obj))
							continue;

						final Summon summon = obj.getPet();

						if (summon != null && !summon.isDead())
							targetList.add(summon);

						if (!obj.isDead())
							targetList.add(obj);
					}
				}
				
				return targetList.toArray(new Creature[targetList.size()]);
			}

			case TARGET_CORPSE_ALLY: {
				final Player player = activeChar.getActingPlayer();

				if (player == null)
					return _emptyTargetList;

				if (onlyFirst || player.isInOlympiadMode())
					return new Creature[] { activeChar };
				
				final int radius = _skillRadius;
				List<Creature> targetList = new ArrayList<>();
				targetList.add(activeChar);
				
				if (player.getClan() != null) {
					final boolean isInBossZone = player.isInsideZone(ZoneId.BOSS);
					
					for (Player obj : activeChar.getKnownTypeInRadius(Player.class, radius)) {
						if (!obj.isDead())
							continue;

						if ((obj.getAllyId() == 0 || obj.getAllyId() != player.getAllyId()) && (obj.getClan() == null || obj.getClanId() != player.getClanId()))
							continue;

						if (player.isInDuel()) {
							if (player.getDuelId() != obj.getDuelId())
								continue;

							if (player.isInParty() && obj.isInParty() && player.getParty().getLeaderObjectId() != obj.getParty().getLeaderObjectId())
								continue;
						}
						
						// Siege battlefield resurrect has been made possible for participants
						if (obj.isInsideZone(ZoneId.SIEGE) && !obj.isInSiege())
							continue;
						
						// Check if both caster and target are in a boss zone.
						if (isInBossZone != obj.isInsideZone(ZoneId.BOSS))
							continue;

						targetList.add(obj);
					}
				}
				
				return targetList.toArray(new Creature[targetList.size()]);
			}

			case TARGET_CLAN: {
				List<Creature> targetList = new ArrayList<>();
				
				if (activeChar instanceof Playable) {
					final Player player = activeChar.getActingPlayer();
					
					if (player == null)
						return _emptyTargetList;

					if (onlyFirst || player.isInOlympiadMode())
						return new Creature[] { activeChar };

					targetList.add(player);
					final int radius = _skillRadius;

					if (addSummon(activeChar, player, radius, false))
						targetList.add(player.getPet());

					final Clan clan = player.getClan();
					
					if (clan != null) {
						for (ClanMember member : clan.getMembers()) {
							final Player obj = member.getPlayerInstance();

							if (obj == null || obj == player)
								continue;

							if (player.isInDuel()) {
								if (player.getDuelId() != obj.getDuelId())
									continue;

								if (player.isInParty() && obj.isInParty() && player.getParty().getLeaderObjectId() != obj.getParty().getLeaderObjectId())
									continue;
							}

							if (!player.checkPvpSkill(obj, this))
								continue;

							if (blockEventSkill(player, obj))
								continue;

							if (addSummon(activeChar, obj, radius, false))
								targetList.add(obj.getPet());

							if (!addCharacter(activeChar, obj, radius, false))
								continue;

							targetList.add(obj);
						}
					}
				} else if (activeChar instanceof Npc) {
					targetList.add(activeChar);
					
					for (Npc newTarget : activeChar.getKnownTypeInRadius(Npc.class, _castRange)) {
						if (newTarget.isDead() || !ArraysUtil.contains(((Npc) activeChar).getTemplate().getClans(), newTarget.getTemplate().getClans()))
							continue;

						targetList.add(newTarget);
					}
				}
				
				return targetList.toArray(new Creature[targetList.size()]);
			}

			case TARGET_CORPSE_PLAYER:
				if (!(activeChar instanceof Player))
					return _emptyTargetList;

				if (target != null && target.isDead()) {
					final Player targetPlayer;

					if (target instanceof Player)
						targetPlayer = (Player) target;
					else
						targetPlayer = null;

					final Pet targetPet;

					if (target instanceof Pet)
						targetPet = (Pet) target;
					else
						targetPet = null;

					if (targetPlayer != null || targetPet != null) {
						boolean condGood = true;

						if (_skillType == L2SkillType.RESURRECT) {
							final Player player = (Player) activeChar;

							if (targetPlayer != null) {
								// Check target is not in a active siege zone
								if (targetPlayer.isInsideZone(ZoneId.SIEGE) && !targetPlayer.isInSiege()) {
									condGood = false;
									activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE));
								}

								if (targetPlayer.isReviveRequested()) {
									if (targetPlayer.isRevivingPet())
										player.sendPacket(SystemMessageId.MASTER_CANNOT_RES); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
									else
										player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
									
									condGood = false;
								}
							} else if (targetPet != null) {
								if (targetPet.getActingPlayer() != player) {
									if (targetPet.getActingPlayer().isReviveRequested()) {
										if (targetPet.getActingPlayer().isRevivingPet())
											player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
										else
											player.sendPacket(SystemMessageId.CANNOT_RES_PET2); // A pet cannot be resurrected while it's owner is in the process of resurrecting.

										condGood = false;
									}
								}
							}
						}
						
						if (condGood)
							return new Creature[] { target };
					}
				}
				
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return _emptyTargetList;
			case TARGET_CORPSE_MOB:
				if (!(target instanceof Attackable) || !target.isDead()) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				// Corpse mob only available for half time
				if (_skillType == L2SkillType.DRAIN && !DecayTaskManager.getInstance().isCorpseActionAllowed((Attackable) target)) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CORPSE_TOO_OLD_SKILL_NOT_USED));
					return _emptyTargetList;
				}
				
				return new Creature[] { target };
			case TARGET_AREA_CORPSE_MOB: {
				if ((!(target instanceof Attackable)) || !target.isDead()) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				if (onlyFirst)
					return new Creature[] { target };

				List<Creature> targetList = new ArrayList<>();
				targetList.add(target);
				final boolean srcInArena = activeChar.isInArena();
				
				for (Creature obj : activeChar.getKnownTypeInRadius(Creature.class, _skillRadius)) {
					if (!(obj instanceof Attackable || obj instanceof Playable))
						continue;

					if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
						continue;
					
					targetList.add(obj);
				}
				
				if (targetList.isEmpty())
					return _emptyTargetList;

				return targetList.toArray(new Creature[targetList.size()]);
			}

			case TARGET_UNLOCKABLE:
				if (!(target instanceof Door) && !(target instanceof Chest))
					return _emptyTargetList;

				return new Creature[] { target };
			case TARGET_UNDEAD:
				if (target instanceof Npc || target instanceof Servitor) {
					if (!target.isUndead() || target.isDead()) {
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
						return _emptyTargetList;
					}
					
					return new Creature[] { target };
				}
				
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return _emptyTargetList;
			case TARGET_AURA_UNDEAD:
				List<Creature> targetList = new ArrayList<>();
				
				for (Creature obj : activeChar.getKnownTypeInRadius(Creature.class, _skillRadius)) {
					if (!(obj instanceof Npc || obj instanceof Servitor))
						continue;

					target = obj;
					
					if (target.isAlikeDead() || !target.isUndead())
						continue;

					if (!GeoEngine.getInstance().canSeeTarget(activeChar, target))
						continue;

					if (onlyFirst)
						return new Creature[] { obj };

					targetList.add(obj);
				}
				
				if (targetList.isEmpty())
					return _emptyTargetList;

				return targetList.toArray(new Creature[targetList.size()]);
			case TARGET_ENEMY_SUMMON:
				if (target instanceof Summon) {
					final Summon targetSummon = (Summon) target;
					final Player summonOwner = targetSummon.getActingPlayer();
					
					if (activeChar instanceof Player && activeChar.getPet() != targetSummon && !targetSummon.isDead() &&
					(summonOwner.isFlagged() || summonOwner.hasKarma()) || 
					(summonOwner.isInsideZone(ZoneId.PVP) && activeChar.isInsideZone(ZoneId.PVP)) ||
					(summonOwner.isInDuel() && ((Player) activeChar).isInDuel() && summonOwner.getDuelId() == ((Player) activeChar).getDuelId()))
						return new Creature[] { targetSummon };
				}
				
				return _emptyTargetList;
			default:
				activeChar.sendMessage("Target type of skill is not currently handled");
				return _emptyTargetList;
		}
	}
}
