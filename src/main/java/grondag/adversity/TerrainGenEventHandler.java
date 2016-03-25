package com.grondag.adversity.event;

import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class TerrainGenEventHandler {

	@SubscribeEvent
	public void onDecorateBiome(DecorateBiomeEvent.Decorate event) {
		if (event.world.provider.dimensionId == 0) {
			// System.out.println("onDecorateBiome");
			if (event.type == DecorateBiomeEvent.Decorate.EventType.BIG_SHROOM
					|| event.type == DecorateBiomeEvent.Decorate.EventType.LILYPAD
					|| event.type == DecorateBiomeEvent.Decorate.EventType.FLOWERS
					|| event.type == DecorateBiomeEvent.Decorate.EventType.GRASS
					|| event.type == DecorateBiomeEvent.Decorate.EventType.LAKE
					|| event.type == DecorateBiomeEvent.Decorate.EventType.PUMPKIN
					|| event.type == DecorateBiomeEvent.Decorate.EventType.REED
					|| event.type == DecorateBiomeEvent.Decorate.EventType.SAND
					|| event.type == DecorateBiomeEvent.Decorate.EventType.SAND_PASS2
					|| event.type == DecorateBiomeEvent.Decorate.EventType.SHROOM
					|| event.type == DecorateBiomeEvent.Decorate.EventType.TREE) {

				event.setResult(Result.DENY);
			}
		}
	}

	@SubscribeEvent
	public void onPopulateChunkEvent(PopulateChunkEvent.Populate event) {
		if (event.world.provider.dimensionId == 0) {
			// System.out.println("onPopulateChunkEvent");
			// using a switch statement here gives array out of bounds?
			// guessing compiler optimizes enum comparison as a small array
			if (event.type == PopulateChunkEvent.Populate.EventType.ANIMALS
					|| event.type == PopulateChunkEvent.Populate.EventType.ICE
					|| event.type == PopulateChunkEvent.Populate.EventType.LAKE) {
				event.setResult(Result.DENY);
			}
		}
	}
}
