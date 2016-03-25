package com.grondag.adversity.feature.volcano;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;

public class VolcanoHazeMaker {

	private int				x;
	private int				z;
	private int				decisionOver2;
	private int				radius;
	// private int height=127;

	private final String	NBT_NAME	= "hazeMaker";

	private void setup(int r) {
		this.radius = r;
		this.x = this.radius;
		this.decisionOver2 = 1 - this.radius;
		this.z = 0;
	}

	// public void setHeight(int y){
	// if(y != height){
	// height = Math.min(255, y);
	// radius = 0;
	// }
	// }

	private void pollute(World worldObj, int x, int z) {
		// worldObj.getChunkFromBlockCoords(x, z).heightMap[(z & 15) << 4 | (x & 15)]=255;
		// worldObj.setBlock(x, 254, z, Adversity.blockHaze, 0, 3);

		// int h = worldObj.getHeightValue(x, z)+6;
		// boolean keepGoing = true;
		// while( keepGoing && h > 0){
		// if(worldObj.getBlock(x, h, z) == Blocks.air) {
		// worldObj.setBlock(x, h, z, Adversity.blockHaze, 0, 3);
		// --h;
		// } else {
		// keepGoing = false;
		// }
		//
		// }
		if (worldObj.getBiomeGenForCoords(x, z) != Volcano.volcano) {
			ReikaWorldHelper.setBiomeForXZ(worldObj, x, z, Volcano.volcano);
		}
	}

	public void update(World worldObj, int x0, int z0) {

		if (this.radius == 0) {

			this.pollute(worldObj, x0, z0);
			this.setup(1);

		} else if (this.z <= this.x) {

			this.pollute(worldObj, this.x + x0, this.z + z0); // Octant 1
			this.pollute(worldObj, this.z + x0, this.x + z0); // Octant 2
			this.pollute(worldObj, -this.x + x0, this.z + z0); // Octant 3
			this.pollute(worldObj, -this.z + x0, this.x + z0); // Octant 4
			this.pollute(worldObj, -this.x + x0, -this.z + z0); // Octant 5
			this.pollute(worldObj, -this.z + x0, -this.x + z0); // Octant 6
			this.pollute(worldObj, this.x + x0, -this.z + z0); // Octant 7
			this.pollute(worldObj, this.z + x0, -this.x + z0); // Octant 8
			this.z++;

			if (this.decisionOver2 <= 0) {
				this.decisionOver2 += 2 * this.z + 1; // Change in decision criterion for y -> y+1
			} else {
				this.pollute(worldObj, this.x + x0, this.z + z0); // Octant 1
				this.pollute(worldObj, this.z + x0, this.x + z0); // Octant 2
				this.pollute(worldObj, -this.x + x0, this.z + z0); // Octant 3
				this.pollute(worldObj, -this.z + x0, this.x + z0); // Octant 4
				this.pollute(worldObj, -this.x + x0, -this.z + z0); // Octant 5
				this.pollute(worldObj, -this.z + x0, -this.x + z0); // Octant 6
				this.pollute(worldObj, this.x + x0, -this.z + z0); // Octant 7
				this.pollute(worldObj, this.z + x0, -this.x + z0); // Octant 8
				this.x--;
				this.decisionOver2 += 2 * (this.z - this.x) + 1; // Change for y -> y+1, x -> x-1

			}
		} else if (this.radius < 32) {
			this.setup(this.radius + 1);

		} else {
			this.radius = 0;

		}
		// }
	}

	public void writeToNBT(NBTTagCompound NBT) {

		final NBTTagCompound myData = new NBTTagCompound();

		myData.setInteger("x", this.x);
		myData.setInteger("z", this.z);
		myData.setInteger("decisionOver2", this.decisionOver2);
		myData.setInteger("radius", this.radius);

		NBT.setTag(this.NBT_NAME, myData);

	}

	public void readFromNBT(NBTTagCompound NBT) {
		try {
			if (NBT.hasKey(this.NBT_NAME)) {
				final NBTTagCompound myData = NBT.getCompoundTag(this.NBT_NAME);
				this.x = myData.getInteger("x");
				this.z = myData.getInteger("z");
				this.decisionOver2 = myData.getInteger("decisionOver2");
				this.radius = myData.getInteger("radius");
			}
		} catch (final IllegalArgumentException e) {
			DragonAPICore.logError("Unable to load hazeMaker NBT data.");
			e.printStackTrace();
		}
	}
}
