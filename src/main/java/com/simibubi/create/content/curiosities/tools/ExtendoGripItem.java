package com.simibubi.create.content.curiosities.tools;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.curiosities.armor.BackTankUtil;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.lib.item.CustomDurabilityBarItem;
import com.simibubi.create.lib.utility.ExtraDataUtil;
import com.simibubi.create.foundation.config.AllConfigs;

public class ExtendoGripItem extends Item implements IBackTankRechargeable, CustomDurabilityBarItem {
	private static DamageSource lastActiveDamageSource;

	public static final int MAX_DAMAGE = 200;

	public static final AttributeModifier singleRangeAttributeModifier =
		new AttributeModifier(UUID.fromString("7f7dbdb2-0d0d-458a-aa40-ac7633691f66"), "Range modifier", 3,
			AttributeModifier.Operation.ADDITION);
	public static final AttributeModifier doubleRangeAttributeModifier =
		new AttributeModifier(UUID.fromString("8f7dbdb2-0d0d-458a-aa40-ac7633691f66"), "Range modifier", 5,
			AttributeModifier.Operation.ADDITION);

	static LazyLoadedValue<Multimap<Attribute, AttributeModifier>> rangeModifier = new LazyLoadedValue<>(() ->
	// Holding an ExtendoGrip
	ImmutableMultimap.of(ReachEntityAttributes.REACH, singleRangeAttributeModifier));

	static LazyLoadedValue<Multimap<Attribute, AttributeModifier>> doubleRangeModifier = new LazyLoadedValue<>(() ->
	// Holding two ExtendoGrips o.O
	ImmutableMultimap.of(ReachEntityAttributes.REACH, doubleRangeAttributeModifier));

	public ExtendoGripItem(Properties properties) {
		super(properties.stacksTo(1)
			.rarity(Rarity.UNCOMMON));
	}

	public static final String EXTENDO_MARKER = "createExtendo";
	public static final String DUAL_EXTENDO_MARKER = "createDualExtendo";

	public static void holdingExtendoGripIncreasesRange(LivingEntity entity) {
		if (!(entity instanceof Player))
			return;

		Player player = (Player) entity;

		CompoundTag persistentData = ExtraDataUtil.getExtraData(player);
		boolean inOff = AllItems.EXTENDO_GRIP.isIn(player.getOffhandItem());
		boolean inMain = AllItems.EXTENDO_GRIP.isIn(player.getMainHandItem());
		boolean holdingDualExtendo = inOff && inMain;
		boolean holdingExtendo = inOff ^ inMain;
		holdingExtendo &= !holdingDualExtendo;
		boolean wasHoldingExtendo = persistentData.contains(EXTENDO_MARKER);
		boolean wasHoldingDualExtendo = persistentData.contains(DUAL_EXTENDO_MARKER);

		if (holdingExtendo != wasHoldingExtendo) {
			if (!holdingExtendo) {
				player.getAttributes()
					.removeAttributeModifiers(rangeModifier.get());
				persistentData.remove(EXTENDO_MARKER);
			} else {
				if (player instanceof ServerPlayer)
					AllTriggers.EXTENDO.trigger((ServerPlayer) player);
				player.getAttributes()
					.addTransientAttributeModifiers(rangeModifier.get());
				persistentData.putBoolean(EXTENDO_MARKER, true);
			}
		}

		if (holdingDualExtendo != wasHoldingDualExtendo) {
			if (!holdingDualExtendo) {
				player.getAttributes()
					.removeAttributeModifiers(doubleRangeModifier.get());
				persistentData.remove(DUAL_EXTENDO_MARKER);
			} else {
				if (player instanceof ServerPlayer)
					AllTriggers.GIGA_EXTENDO.trigger((ServerPlayer) player);
				player.getAttributes()
					.addTransientAttributeModifiers(doubleRangeModifier.get());
				persistentData.putBoolean(DUAL_EXTENDO_MARKER, true);
			}
		}

	}

//	@SubscribeEvent
//	public static void addReachToJoiningPlayersHoldingExtendo(PlayerEvent.PlayerLoggedInEvent event) {
//		PlayerEntity player = event.getPlayer();
//		CompoundNBT persistentData = player.getPersistentData();
//
//		if (persistentData.contains(DUAL_EXTENDO_MARKER))
//			player.getAttributes()
//				.addTemporaryModifiers(doubleRangeModifier.getValue());
//		else if (persistentData.contains(EXTENDO_MARKER))
//			player.getAttributes()
//				.addTemporaryModifiers(rangeModifier.getValue());
//	}
//
//		if (persistentData.contains(DUAL_EXTENDO_MARKER))
//			player.getAttributes()
//				.addTransientAttributeModifiers(doubleRangeModifier.get());
//		else if (persistentData.contains(EXTENDO_MARKER))
//			player.getAttributes()
//				.addTransientAttributeModifiers(rangeModifier.get());
//	}

//	@SubscribeEvent
//	@OnlyIn(Dist.CLIENT)
//	public static void dontMissEntitiesWhenYouHaveHighReachDistance(ClickInputEvent event) {
//		Minecraft mc = Minecraft.getInstance();
//		ClientPlayerEntity player = mc.player;
//		if (mc.level == null || player == null)
//			return;
//		if (!isHoldingExtendoGrip(player))
//			return;
//		if (mc.hitResult instanceof BlockRayTraceResult && mc.hitResult.getType() != Type.MISS)
//			return;
//
//		// Modified version of GameRenderer#getMouseOver
//		double d0 = player.getAttribute(ForgeMod.REACH_DISTANCE.get())
//			.getValue();
//		if (!player.isCreative())
//			d0 -= 0.5f;
//		Vector3d Vector3d = player.getEyePosition(AnimationTickHolder.getPartialTicks());
//		Vector3d Vector3d1 = player.getViewVector(1.0F);
//		Vector3d Vector3d2 = Vector3d.add(Vector3d1.x * d0, Vector3d1.y * d0, Vector3d1.z * d0);
//		AxisAlignedBB axisalignedbb = player.getBoundingBox()
//			.expandTowards(Vector3d1.scale(d0))
//			.inflate(1.0D, 1.0D, 1.0D);
//		EntityRayTraceResult entityraytraceresult =
//			ProjectileHelper.getEntityHitResult(player, Vector3d, Vector3d2, axisalignedbb, (e) -> {
//				return !e.isSpectator() && e.isPickable();
//			}, d0 * d0);
//		if (entityraytraceresult != null) {
//			Entity entity1 = entityraytraceresult.getEntity();
//			Vector3d Vector3d3 = entityraytraceresult.getLocation();
//			double d2 = Vector3d.distanceToSqr(Vector3d3);
//			if (d2 < d0 * d0 || mc.hitResult == null || mc.hitResult.getType() == Type.MISS) {
//				mc.hitResult = entityraytraceresult;
//				if (entity1 instanceof LivingEntity || entity1 instanceof ItemFrameEntity)
//					mc.crosshairPickEntity = entity1;
//			}
//		}
//	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void consumeDurabilityOnBlockBreak(BreakEvent event) {
		findAndDamageExtendoGrip(event.getPlayer());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void consumeDurabilityOnPlace(EntityPlaceEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player)
			findAndDamageExtendoGrip((Player) entity);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void consumeDurabilityOnPlace(PlayerInteractEvent event) {
//		findAndDamageExtendoGrip(event.getPlayer());
	}

	private static void findAndDamageExtendoGrip(Player player) {
		if (player == null)
			return;
		if (player.level.isClientSide)
			return;
		Hand hand = Hand.MAIN_HAND;
		ItemStack extendo = player.getMainHandItem();
		if (!AllItems.EXTENDO_GRIP.isIn(extendo)) {
			extendo = player.getOffhandItem();
			hand = Hand.OFF_HAND;
		}
		if (!AllItems.EXTENDO_GRIP.isIn(extendo))
			return;
		final Hand h = hand;
		if (!BackTankUtil.canAbsorbDamage(player, maxUses()))
			extendo.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(h));
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return BackTankUtil.getRGBDurabilityForDisplay(stack, maxUses());
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return BackTankUtil.getDurabilityForDisplay(stack, maxUses());
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return BackTankUtil.showDurabilityBar(stack, maxUses());
	}

	private static int maxUses() {
		return AllConfigs.SERVER.curiosities.maxExtendoGripActions.get();
	}

	@Override
	public boolean canBeDepleted() {
		return true;
	}

//	@Override
//	public int getMaxDamage(ItemStack stack) {
//		return MAX_DAMAGE; // handled in constructor for item
//	}

	@SubscribeEvent
	public static void bufferLivingAttackEvent(LivingAttackEvent event) {
		// Workaround for removed patch to get the attacking entity.
		lastActiveDamageSource = event.getSource();

		DamageSource source = event.getSource();
		if (source == null)
			return;
		Entity trueSource = source.getEntity();
		if (trueSource instanceof Player)
			findAndDamageExtendoGrip((Player) trueSource);
	}

	public static float attacksByExtendoGripHaveMoreKnockback(float strength, Player player) {
//		if (lastActiveDamageSource == null)
//			return strength;
		Entity entity = lastActiveDamageSource.getDirectEntity();
		if (!(entity instanceof Player))
			return strength;
		if (!isHoldingExtendoGrip(player))
			return strength;
		return strength + 2;
	}

//	private static boolean isUncaughtClientInteraction(Entity entity, Entity target) {
//		// Server ignores entity interaction further than 6m
//		if (entity.distanceToSqr(target) < 36)
//			return false;
//		if (!entity.level.isClientSide)
//			return false;
//		if (!(entity instanceof PlayerEntity))
//			return false;
//		return true;
//	}

//	@Environment(EnvType.CLIENT)
//	public static InteractionResult notifyServerOfLongRangeAttacks(PlayerEntity player, World world, Hand hand, Entity target, @Nullable EntityRayTraceResult traceResult) {
//		if (!isUncaughtClientInteraction(player, target))
//			return InteractionResult.PASS;
//		if (isHoldingExtendoGrip(player)) {
//			AllPackets.channel.sendToServer(new ExtendoGripInteractionPacket(target));
//			return InteractionResult.SUCCESS;
//		}
//
//		return InteractionResult.PASS;
//	}

//	@Environment(EnvType.CLIENT)
//	public static InteractionResult notifyServerOfLongRangeInteractions(PlayerEntity player, World world, Hand hand, Entity target, @Nullable EntityRayTraceResult traceResult) {
//		if (!isUncaughtClientInteraction(player, target))
//			return InteractionResult.PASS;
//		if (isHoldingExtendoGrip(player)) {
//			AllPackets.channel.sendToServer(new ExtendoGripInteractionPacket(target, hand));
//			return InteractionResult.SUCCESS;
//		}
//
//		return InteractionResult.PASS;
//	}

//	@Environment(EnvType.CLIENT)
//	public static void notifyServerOfLongRangeSpecificInteractions(PlayerInteractEvent.EntityInteractSpecific event) {
//		Entity entity = event.getEntity();
//		Entity target = event.getTarget();
//		if (!isUncaughtClientInteraction(entity, target))
//			return;
//		PlayerEntity player = (PlayerEntity) entity;
//		if (isHoldingExtendoGrip(player))
//			AllPackets.channel
//				.sendToServer(new ExtendoGripInteractionPacket(target, event.getHand(), event.getLocalPos()));
//	}

	public static boolean isHoldingExtendoGrip(PlayerEntity player) {
		boolean inOff = AllItems.EXTENDO_GRIP.isIn(player.getOffhandItem());
		boolean inMain = AllItems.EXTENDO_GRIP.isIn(player.getMainHandItem());
		boolean holdingGrip = inOff || inMain;
		return holdingGrip;
	}
}
