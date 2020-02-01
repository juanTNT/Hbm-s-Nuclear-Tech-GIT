package com.hbm.main;

import java.lang.reflect.Field;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.forgefluid.SpecialContainerFillLists.EnumCanister;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.BulletConfiguration;
import com.hbm.handler.GunConfiguration;
import com.hbm.handler.HbmShaderManager;
import com.hbm.interfaces.IConstantRenderer;
import com.hbm.interfaces.IHasCustomModel;
import com.hbm.interfaces.IHoldableWeapon;
import com.hbm.items.ModItems;
import com.hbm.items.gear.RedstoneSword;
import com.hbm.items.special.weapon.GunB92;
import com.hbm.items.tool.ItemAssemblyTemplate;
import com.hbm.items.tool.ItemChemistryTemplate;
import com.hbm.items.tool.ItemChemistryTemplate.EnumChemistryTemplate;
import com.hbm.items.tool.ItemFluidCanister;
import com.hbm.items.tool.ItemFluidTank;
import com.hbm.items.tool.ItemForgeFluidIdentifier;
import com.hbm.items.weapon.ItemGunBase;
import com.hbm.lib.RefStrings;
import com.hbm.packet.GunButtonPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.particle.ParticleDSmokeFX;
import com.hbm.portals.Mirror;
import com.hbm.portals.Portal;
import com.hbm.portals.PortalManager;
import com.hbm.render.entity.DSmokeRenderer;
import com.hbm.render.item.AssemblyTemplateBakedModel;
import com.hbm.render.item.AssemblyTemplateRender;
import com.hbm.render.item.B92BakedModel;
import com.hbm.render.item.BakedModelNoGui;
import com.hbm.render.item.ChemTemplateBakedModel;
import com.hbm.render.item.ChemTemplateRender;
import com.hbm.render.item.FFIdentifierModel;
import com.hbm.render.item.FFIdentifierRender;
import com.hbm.render.item.FluidBarrelBakedModel;
import com.hbm.render.item.FluidBarrelRender;
import com.hbm.render.item.FluidCanisterBakedModel;
import com.hbm.render.item.FluidCanisterRender;
import com.hbm.render.item.FluidTankBakedModel;
import com.hbm.render.item.FluidTankRender;
import com.hbm.render.item.GunRevolverBakedModel;
import com.hbm.render.item.GunRevolverRender;
import com.hbm.render.item.ItemRedstoneSwordRender;
import com.hbm.render.item.ItemRenderGunAnim;
import com.hbm.render.item.ItemRenderRedstoneSword;
import com.hbm.render.item.TEISRBase;
import com.hbm.render.misc.RenderAccessoryUtility;
import com.hbm.render.misc.RenderScreenOverlay;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;

public class ModEventHandlerClient {

	private Field debugViewDirection = null;
	private Field debugView = null;
	
	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {

		int i = 0;
		ResourceLocation[] list = new ResourceLocation[EnumCanister.values().length];
		for (EnumCanister e : EnumCanister.values()) {
			list[i] = e.getResourceLocation();
			i++;
		}
		ModelLoader.registerItemVariants(ModItems.canister_generic, list);

		for (Item item : ModItems.ALL_ITEMS) {
			registerModel(item, 0);
		}
		for (Block block : ModBlocks.ALL_BLOCKS) {
			registerBlockModel(block, 0);
		}
	}

	private void registerBlockModel(Block block, int meta) {
		registerModel(Item.getItemFromBlock(block), meta);
	}

	private void registerModel(Item item, int meta) {
		if (item == ModItems.chemistry_icon) {
			for (int i = 0; i < EnumChemistryTemplate.values().length; i++) {
				ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(RefStrings.MODID + ":chem_icon_" + EnumChemistryTemplate.getEnum(i).getName().toLowerCase(), "inventory"));
			}
		} else if (item == ModItems.chemistry_template) {
			for (int i = 0; i < EnumChemistryTemplate.values().length; i++) {
				ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(item.getRegistryName(), "inventory"));
			}
		} else if (item instanceof IHasCustomModel) {
			ModelLoader.setCustomModelResourceLocation(item, 0, ((IHasCustomModel) item).getResourceLocation());
		} else {
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
		}
	}

	@SubscribeEvent
	public void modelBaking(ModelBakeEvent evt) {

		for (EnumCanister e : EnumCanister.values()) {
			Object o = evt.getModelRegistry().getObject(e.getResourceLocation());
			if (o instanceof IBakedModel)
				e.putRenderModel((IBakedModel) o);
		}

		// TODO Sigh... find a better custom model loading system.
		ResourceManager.init();
		Object obj = evt.getModelRegistry().getObject(RedstoneSword.rsModel);
		if (obj instanceof IBakedModel) {
			IBakedModel model = (IBakedModel) obj;
			ItemRedstoneSwordRender.INSTANCE.itemModel = model;
			evt.getModelRegistry().putObject(RedstoneSword.rsModel, new ItemRenderRedstoneSword());
		}
		Object object = evt.getModelRegistry().getObject(ItemAssemblyTemplate.location);
		if (object instanceof IBakedModel) {
			IBakedModel model = (IBakedModel) object;
			AssemblyTemplateRender.INSTANCE.itemModel = model;
			evt.getModelRegistry().putObject(ItemAssemblyTemplate.location, new AssemblyTemplateBakedModel());
		}

		Object object3 = evt.getModelRegistry().getObject(GunB92.b92Model);
		if (object instanceof IBakedModel) {
			IBakedModel model = (IBakedModel) object3;
			ItemRenderGunAnim.INSTANCE.b92ItemModel = model;
			evt.getModelRegistry().putObject(GunB92.b92Model, new B92BakedModel());
		}
		Object object4 = evt.getModelRegistry().getObject(ItemFluidTank.fluidTankModel);
		if (object4 instanceof IBakedModel) {
			IBakedModel model = (IBakedModel) object4;
			FluidTankRender.INSTANCE.itemModel = model;
			evt.getModelRegistry().putObject(ItemFluidTank.fluidTankModel, new FluidTankBakedModel());
		}
		Object object5 = evt.getModelRegistry().getObject(ItemFluidTank.fluidBarrelModel);
		if (object5 instanceof IBakedModel) {
			IBakedModel model = (IBakedModel) object5;
			FluidBarrelRender.INSTANCE.itemModel = model;
			evt.getModelRegistry().putObject(ItemFluidTank.fluidBarrelModel, new FluidBarrelBakedModel());
		}
		Object object6 = evt.getModelRegistry().getObject(ItemFluidCanister.fluidCanisterModel);
		if (object6 instanceof IBakedModel) {
			IBakedModel model = (IBakedModel) object6;
			FluidCanisterRender.INSTANCE.itemModel = model;
			evt.getModelRegistry().putObject(ItemFluidCanister.fluidCanisterModel, new FluidCanisterBakedModel());
		}
		Object object7 = evt.getModelRegistry().getObject(ItemChemistryTemplate.chemModel);
		if (object7 instanceof IBakedModel) {
			IBakedModel model = (IBakedModel) object7;
			ChemTemplateRender.INSTANCE.itemModel = model;
			evt.getModelRegistry().putObject(ItemChemistryTemplate.chemModel, new ChemTemplateBakedModel());
		}
		Object object8 = evt.getModelRegistry().getObject(ItemForgeFluidIdentifier.identifierModel);
		if (object8 instanceof IBakedModel) {
			IBakedModel model = (IBakedModel) object8;
			FFIdentifierRender.INSTANCE.itemModel = model;
			evt.getModelRegistry().putObject(ItemForgeFluidIdentifier.identifierModel, new FFIdentifierModel());
		}
		Object object9 = evt.getModelRegistry().getObject(new ModelResourceLocation(ModItems.gun_revolver.getRegistryName(), "inventory"));
		if (object9 instanceof IBakedModel) {
			IBakedModel model = (IBakedModel) object9;
			GunRevolverRender.INSTANCE.revolverModel = model;
			evt.getModelRegistry().putObject(new ModelResourceLocation(ModItems.gun_revolver.getRegistryName(), "inventory"), new GunRevolverBakedModel());
		}
		IRegistry<ModelResourceLocation, IBakedModel> reg = evt.getModelRegistry();
		swapModelsNoGui(ModItems.gun_revolver_nightmare, reg);
		swapModelsNoGui(ModItems.gun_revolver_nightmare2, reg);
		swapModelsNoGui(ModItems.gun_revolver_iron, reg);
		swapModelsNoGui(ModItems.gun_revolver_gold, reg);
		swapModelsNoGui(ModItems.gun_revolver_lead, reg);
		swapModelsNoGui(ModItems.gun_revolver_schrabidium, reg);
		swapModelsNoGui(ModItems.gun_revolver_cursed, reg);
		swapModelsNoGui(ModItems.gun_revolver_pip, reg);
		swapModelsNoGui(ModItems.gun_revolver_nopip, reg);
		swapModelsNoGui(ModItems.gun_revolver_blackjack, reg);
		swapModelsNoGui(ModItems.gun_revolver_red, reg);
		swapModelsNoGui(ModItems.gun_lever_action, reg);
		swapModelsNoGui(ModItems.gun_spark, reg);
		swapModelsNoGui(ModItems.gun_b93, reg);
	}
	
	public void swapModelsNoGui(Item item, IRegistry<ModelResourceLocation, IBakedModel> reg){
		ModelResourceLocation loc = new ModelResourceLocation(item.getRegistryName(), "inventory");
		IBakedModel model = reg.getObject(loc);
		TileEntityItemStackRenderer render = item.getTileEntityItemStackRenderer();
		if(render instanceof TEISRBase){
			((TEISRBase)render).itemModel = model;
			reg.putObject(loc, new BakedModelNoGui((TEISRBase)render));
		}
		
	}

	@SubscribeEvent
	public void textureStitch(TextureStitchEvent.Pre evt) {
		DSmokeRenderer.sprites[0] = evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "particle/d_smoke1"));
		DSmokeRenderer.sprites[1] = evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "particle/d_smoke2"));
		DSmokeRenderer.sprites[2] = evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "particle/d_smoke3"));
		DSmokeRenderer.sprites[3] = evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "particle/d_smoke4"));
		DSmokeRenderer.sprites[4] = evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "particle/d_smoke5"));
		DSmokeRenderer.sprites[5] = evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "particle/d_smoke6"));
		DSmokeRenderer.sprites[6] = evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "particle/d_smoke7"));
		DSmokeRenderer.sprites[7] = evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "particle/d_smoke8"));
		ParticleDSmokeFX.sprites = DSmokeRenderer.sprites;

		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/steam_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/steam_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hotsteam_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hotsteam_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/superhotsteam_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/superhotsteam_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/coolant_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/coolant_flowing"));

		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/deuterium_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/deuterium_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/tritium_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/tritium_flowing"));

		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/oil_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/oil_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hotoil_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hotoil_flowing"));

		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/heavyoil_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/heavyoil_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/bitumen_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/bitumen_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/smear_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/smear_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/heatingoil_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/heatingoil_flowing"));

		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/reclaimed_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/reclaimed_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/petroil_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/petroil_flowing"));

		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/lubricant_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/lubricant_flowing"));

		// Yes yes I know, I spelled 'naphtha' wrong.
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/napatha_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/napatha_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/diesel_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/diesel_flowing"));

		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/lightoil_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/lightoil_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/kerosene_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/kerosene_flowing"));

		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/gas_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/gas_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/petroleum_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/petroleum_flowing"));

		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/biogas_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/biogas_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/biofuel_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/biofuel_flowing"));

		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/nitan_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/nitan_flowing"));

		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/uf6_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/uf6_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/puf6_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/puf6_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/sas3_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/sas3_flowing"));

		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/amat_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/amat_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/aschrab_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/aschrab_flowing"));

		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/acid_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/acid_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/watz_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/watz_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/cryogel_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/cryogel_flowing"));

		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hydrogen_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/hydrogen_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/oxygen_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/oxygen_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/xenon_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/xenon_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/balefire_still"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/balefire_flowing"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "models/boxcar"));
		evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID, "models/boxcarflipv"));

		contrail = evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID + ":particle/contrail"));

		// evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID,
		// "blocks/forgefluid/toxic_still"));
		// evt.getMap().registerSprite(new ResourceLocation(RefStrings.MODID,
		// "blocks/forgefluid/toxic_flowing"));
	}

	public static TextureAtlasSprite contrail;
	int renderCount = 0;
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void renderWorld(RenderWorldLastEvent evt) {
		List<Entity> list = Minecraft.getMinecraft().world.loadedEntityList;
		for (Entity e : list) {
			if (e instanceof IConstantRenderer) {

				float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
				double d0 = e.lastTickPosX + (e.posX - e.lastTickPosX) * (double) partialTicks;
				double d1 = e.lastTickPosY + (e.posY - e.lastTickPosY) * (double) partialTicks;
				double d2 = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * (double) partialTicks;
				float f = e.prevRotationYaw + (e.rotationYaw - e.prevRotationYaw) * partialTicks;
				Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
				double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
				double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
				double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;

				Render<Entity> r = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(e);
				r.doRender(e, d0 - d3, d1 - d4, d2 - d5, f, partialTicks);
			}
		}
	//	renderCount ++;
	//	if(renderCount <= 1)
			//PortalManager.ALL_RENDER_PORTALS.forEach(Portal::render);
	//	else
		//	renderCount = 0;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void renderLast(TickEvent.RenderTickEvent evt){
		if(evt.phase == Phase.END){
			//if(OpenGlHelper.shadersSupported && MainRegistry.useShaders)
			//	if(Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND).getItem() == ModItems.gun_spark)
					//HbmShaderManager.renderGauss();
			//PortalManager.ALL_RENDER_PORTALS.forEach((Portal p) -> {System.out.println(p);});
			
		}
		if(evt.phase == Phase.START){
			//HbmShaderManager.stealDepthBuffer();
		}
	}

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public void onOverlayRender(RenderGameOverlayEvent.Pre event) {
		
		EntityPlayer player = Minecraft.getMinecraft().player;
		if(this.debugViewDirection == null){
			//Drillgon200: Oof, OfbReflect didn't work.
			this.debugViewDirection = ReflectionHelper.findField(EntityRenderer.class, "debugViewDirection", "field_175079_V");
			debugViewDirection.setAccessible(true);
		}
		if(this.debugView == null){
			//Drillgon200: Oof, OfbReflect didn't work.
			this.debugView = ReflectionHelper.findField(EntityRenderer.class, "debugView", "field_175078_W");
			debugView.setAccessible(true);
		}
		if(player.getUniqueID().toString().equals("c874fd4e-5841-42e4-8f77-70efd5881bc1"))
			if(player.ticksExisted > 5 * 60 * 20){
				try {
					debugViewDirection.setInt(Minecraft.getMinecraft().entityRenderer, 4);
					debugView.setBoolean(Minecraft.getMinecraft().entityRenderer, true);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		
		if(event.getType() == ElementType.HOTBAR && player.getHeldItem(EnumHand.MAIN_HAND) != null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemGunBase) {
			
			ItemGunBase gun = ((ItemGunBase)player.getHeldItem(EnumHand.MAIN_HAND).getItem());
			GunConfiguration gcfg = gun.mainConfig;
			BulletConfiguration bcfg = BulletConfigSyncingUtil.pullConfig(gun.mainConfig.config.get(ItemGunBase.getMagType(player.getHeldItem(EnumHand.MAIN_HAND))));
			
			Item ammo = bcfg.ammo;
			int count = ItemGunBase.getMag(player.getHeldItem(EnumHand.MAIN_HAND));
			int max = gcfg.ammoCap;
			
			if(gcfg.reloadType == GunConfiguration.RELOAD_NONE) {
				ammo = ItemGunBase.getBeltType(player, player.getHeldItem(EnumHand.MAIN_HAND));
				count = ItemGunBase.getBeltSize(player, ammo);
				max = -1;
			}
			
			int dura = ItemGunBase.getItemWear(player.getHeldItem(EnumHand.MAIN_HAND)) * 50 / gcfg.durability;
			
			RenderScreenOverlay.renderAmmo(event.getResolution(), Minecraft.getMinecraft().ingameGUI, ammo, count, max, dura, EnumHand.MAIN_HAND);
			//RenderScreenOverlay.renderRadCounter(event.resolution, 0, Minecraft.getMinecraft().ingameGUI);
		}
		
		if(event.getType() == ElementType.HOTBAR && player.getHeldItem(EnumHand.OFF_HAND) != null && player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ItemGunBase) {
			
			ItemGunBase gun = ((ItemGunBase)player.getHeldItem(EnumHand.OFF_HAND).getItem());
			GunConfiguration gcfg = gun.mainConfig;
			BulletConfiguration bcfg = BulletConfigSyncingUtil.pullConfig(gun.mainConfig.config.get(ItemGunBase.getMagType(player.getHeldItem(EnumHand.OFF_HAND))));
			
			Item ammo = bcfg.ammo;
			int count = ItemGunBase.getMag(player.getHeldItem(EnumHand.OFF_HAND));
			int max = gcfg.ammoCap;
			
			if(gcfg.reloadType == GunConfiguration.RELOAD_NONE) {
				ammo = ItemGunBase.getBeltType(player, player.getHeldItem(EnumHand.OFF_HAND));
				count = ItemGunBase.getBeltSize(player, ammo);
				max = -1;
			}
			
			int dura = ItemGunBase.getItemWear(player.getHeldItem(EnumHand.OFF_HAND)) * 50 / gcfg.durability;
			
			RenderScreenOverlay.renderAmmo(event.getResolution(), Minecraft.getMinecraft().ingameGUI, ammo, count, max, dura, EnumHand.OFF_HAND);
			//RenderScreenOverlay.renderRadCounter(event.resolution, 0, Minecraft.getMinecraft().ingameGUI);
		}
		
		if(event.getType() == ElementType.HOTBAR) {
			//TODO geiger counter
			/*if(player.inventory.hasItem(ModItems.geiger_counter)) {

				float rads = 0;
				float abs = 0;

				RadEntitySavedData data = RadEntitySavedData.getData(player.worldObj);
				rads = data.getRadFromEntity(player);
				
				RenderScreenOverlay.renderRadCounter(event.getResolution(), rads, Minecraft.getMinecraft().ingameGUI);
			}*/
		}
		
		if(event.getType() == ElementType.CROSSHAIRS && (player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof IHoldableWeapon || player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof IHoldableWeapon)) {
			event.setCanceled(true);
			
			if(player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof IHoldableWeapon && !(player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemGunBase && ((ItemGunBase)player.getHeldItem(EnumHand.MAIN_HAND).getItem()).mainConfig.hasSights && player.isSneaking()))
				RenderScreenOverlay.renderCustomCrosshairs(event.getResolution(), Minecraft.getMinecraft().ingameGUI, ((IHoldableWeapon)player.getHeldItem(EnumHand.MAIN_HAND).getItem()).getCrosshair());
			if(!(player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof IHoldableWeapon) && player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof IHoldableWeapon){
				RenderScreenOverlay.renderCustomCrosshairs(event.getResolution(), Minecraft.getMinecraft().ingameGUI, ((IHoldableWeapon)player.getHeldItem(EnumHand.OFF_HAND).getItem()).getCrosshair());
			}
		}
	}
	
	@SubscribeEvent
	public void preRenderPlayer(RenderPlayerEvent.Pre evt) {
		// event.setCanceled(true);
		AbstractClientPlayer player = (AbstractClientPlayer) evt.getEntityPlayer();

		ResourceLocation cloak = RenderAccessoryUtility.getCloakFromPlayer(player);
		// GL11.glRotated(180, 1, 0, 0);
		NetworkPlayerInfo info = Minecraft.getMinecraft().getConnection().getPlayerInfo(player.getUniqueID());
		if (cloak != null)
			RenderAccessoryUtility.loadCape(info, cloak);
	}
	
	@SubscribeEvent
	public void preRenderLiving(RenderLivingEvent.Pre<AbstractClientPlayer> event){
		if(event.getEntity() instanceof AbstractClientPlayer){
			RenderPlayer renderer = (RenderPlayer) event.getRenderer();
			AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();
			

			if (player.getHeldItem(EnumHand.MAIN_HAND) != null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof IHoldableWeapon) {
				renderer.getMainModel().rightArmPose = ArmPose.BOW_AND_ARROW;
				renderer.getMainModel().bipedRightArm.rotateAngleY = 90;
			}
			if (player.getHeldItem(EnumHand.OFF_HAND) != null && player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof IHoldableWeapon) {
				renderer.getMainModel().leftArmPose = ArmPose.BOW_AND_ARROW;
			}
		}
	}

	@SubscribeEvent
	public void clickHandler(MouseEvent event){	
		EntityPlayer player = Minecraft.getMinecraft().player;
		
		if(player.getHeldItem(EnumHand.MAIN_HAND) != null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemGunBase) {
			
			if(event.getButton() == 0)
				event.setCanceled(true);
			
			ItemGunBase item = (ItemGunBase)player.getHeldItem(EnumHand.MAIN_HAND).getItem();
			
			if(event.getButton() == 0 && !item.m1r && !item.m2r) {
				item.m1r = true;
				PacketDispatcher.wrapper.sendToServer(new GunButtonPacket(true, (byte) 0, EnumHand.MAIN_HAND));
				//System.out.println("M1");
			}
			else if(event.getButton() == 1 && !item.m2r && !item.m1r) {
				item.m2r = true;
				PacketDispatcher.wrapper.sendToServer(new GunButtonPacket(true, (byte) 1, EnumHand.MAIN_HAND));
				//System.out.println("M2");
			}
		}
		if(player.getHeldItem(EnumHand.OFF_HAND) != null && player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ItemGunBase) {
			
			if(event.getButton() == 0)
				event.setCanceled(true);
			
			ItemGunBase item = (ItemGunBase)player.getHeldItem(EnumHand.OFF_HAND).getItem();
			if(event.getButton() == 0 && !item.m1l && !item.m2l) {
				item.m1l = true;
				PacketDispatcher.wrapper.sendToServer(new GunButtonPacket(true, (byte) 0, EnumHand.OFF_HAND));
				
				//System.out.println("M1");
			}
			else if(event.getButton() == 1 && !item.m2l && !item.m1l) {
				item.m2l = true;
				PacketDispatcher.wrapper.sendToServer(new GunButtonPacket(true, (byte) 1, EnumHand.OFF_HAND));
				//System.out.println("M2");
			}
		}
	}
	
	@SubscribeEvent
	public void clientDisconnectFromServer(ClientDisconnectionFromServerEvent e) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && ItemAssemblyTemplate.recipesBackup != null) {
			ItemAssemblyTemplate.recipes = ItemAssemblyTemplate.recipesBackup;
			ItemAssemblyTemplate.recipesBackup = null;
		}
	}

}
