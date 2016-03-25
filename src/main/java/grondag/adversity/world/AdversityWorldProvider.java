package com.grondag.adversity.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.DimensionManager;

import com.grondag.adversity.client.AdversityCloudRenderer;
import com.grondag.adversity.client.AdversitySkyRenderer;
import com.grondag.adversity.client.AdversityWeatherRenderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AdversityWorldProvider extends WorldProvider {

	private final AdversitySkyRenderer		skyRenderer		= new AdversitySkyRenderer();
	private final AdversityWeatherRenderer	weatherRenderer	= new AdversityWeatherRenderer();
	private final AdversityCloudRenderer	cloudRenderer	= new AdversityCloudRenderer();

	@Override
	/** tells Minecraft to use our new Terrain Generator */
	public IChunkProvider createChunkGenerator() {
		return new AdversityChunkProvider(this.worldObj, this.worldObj.getSeed(), true);
	}

	@Override
	/** tells Minecraft to use our new WorldChunkManager **/
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new AdversityChunkManager(this.worldObj.getSeed(), this.terrainType);
		this.dimensionId = 0;
	}

	/** Get Provider for Dimension **/
	public static WorldProvider getProviderForDimension(int id) {
		return DimensionManager.createProviderFor(0);
	}

	@Override
	/**
	 * @return the name of the dimension
	 */
	public String getDimensionName() {
		return "Drylands";
	}

	@Override
	/** sets/creates the save folder */
	public String getSaveFolder() {
		return "DIM" + 0;
	}

	@SideOnly(Side.CLIENT)
	/** should stars be rendered? */
	public boolean renderStars() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	/** @return the player speed */
	public double getMovementFactor() {
		return 0.1;
	}

	@SideOnly(Side.CLIENT)
	/** @return the light value of the stars*/
	public float getStarBrightness(World world, float f) {
		return 1.0F;
	}

	@SideOnly(Side.CLIENT)
	/** should clouds be rendered? */
	public boolean renderClouds() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public boolean renderVoidFog() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	/** should the end sky be rendered or the overworld sky? */
	public boolean renderEndSky() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	/** @return the size of the sun */
	public float setSunSize() {
		return 2.00F;
	}

	@SideOnly(Side.CLIENT)
	/** @return the size of the moon */
	public float setMoonSize() {
		return 4.0F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	/**
	 * @return the sky color
	 */
	public Vec3 getSkyColor(Entity cameraEntity, float partialTicks) {
		return this.worldObj.getSkyColorBody(cameraEntity, partialTicks);
	}

	@Override
	@SideOnly(Side.CLIENT)
	/** should a color for the sky be rendered? */
	public boolean isSkyColored() {
		return true;
	}

	@Override
	/** can the player respawn in this dimension? */
	public boolean canRespawnHere() {
		return true;
	}

	@Override
	/** is this a surface world or an underworld */
	public boolean isSurfaceWorld() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	/** @return the high of the clouds */
	public float getCloudHeight() {
		return this.terrainType.getCloudHeight();
	}

	@Override
	public ChunkCoordinates getEntrancePortalLocation() {
		return new ChunkCoordinates(50, 5, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	/** @return the dimension join message */
	public String getWelcomeMessage() {
		return "Welcome to Adversity.  You're doomed.";
	}

	@Override
	@SideOnly(Side.CLIENT)
	/** @return the dimension leave message */
	public String getDepartMessage() {
		return "Bye. You're still doomed.";
	}

	@Override
	public IRenderHandler getSkyRenderer() {
		return this.skyRenderer;
	}

	@Override
	public IRenderHandler getCloudRenderer() {
		return this.cloudRenderer;
	}

	@Override
	public IRenderHandler getWeatherRenderer() {
		return this.weatherRenderer;
	}

	@Override
	public Vec3 drawClouds(float partialTicks) {
		// TODO Auto-generated method stub
		return super.drawClouds(partialTicks);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getFogColor(float par1, float par2) {
		float f2 = MathHelper.cos(par1 * (float) Math.PI * 2.0F) * 2.0F + 0.5F;

		if (f2 < 0.0F) {
			f2 = 0.0F;
		}

		if (f2 > 1.0F) {
			f2 = 1.0F;
		}

		float f3 = 0.7529412F;
		float f4 = 0.84705883F;
		float f5 = 1.0F;
		f3 *= f2 * 0.94F + 0.06F;
		f4 *= f2 * 0.94F + 0.06F;
		f5 *= f2 * 0.91F + 0.09F;
		return Vec3.createVectorHelper(f3, f4, f5);
	}

}