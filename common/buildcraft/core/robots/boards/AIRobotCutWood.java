/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.ForgeHooks;

import buildcraft.core.BlockIndex;
import buildcraft.core.utils.BlockUtil;
import buildcraft.robots.AIRobot;
import buildcraft.robots.EntityRobotBase;

public class AIRobotCutWood extends AIRobot {

	public BlockIndex woodToChop;
	private float blockDamage = 0;

	private Block block;
	private int meta;
	private float hardness;
	private float speed;

	public AIRobotCutWood(EntityRobotBase iRobot, BlockIndex iWoodToChop) {
		super(iRobot, 2);

		woodToChop = iWoodToChop;
	}

	@Override
	public void start() {
		robot.aimItemAt(woodToChop.x, woodToChop.y, woodToChop.z);

		robot.setItemActive(true);
		block = robot.worldObj.getBlock(woodToChop.x, woodToChop.y, woodToChop.z);
		meta = robot.worldObj.getBlockMetadata(woodToChop.x, woodToChop.y, woodToChop.z);
		hardness = block.getBlockHardness(robot.worldObj, woodToChop.x, woodToChop.y, woodToChop.z);
		speed = getBreakSpeed(robot, robot.getItemInUse(), block, meta);
	}

	@Override
	public void update() {
		blockDamage += speed / hardness / 30F;

		if (blockDamage > 1.0F) {
			robot.worldObj.destroyBlockInWorldPartially(robot.getEntityId(), woodToChop.x,
					woodToChop.y, woodToChop.z, -1);
			blockDamage = 0;
			BlockUtil.breakBlock((WorldServer) robot.worldObj, woodToChop.x, woodToChop.y, woodToChop.z, 6000);
			robot.getItemInUse().getItem().onBlockDestroyed(robot.getItemInUse(), robot.worldObj, block, woodToChop.x,
					woodToChop.y, woodToChop.z, robot);

			if (robot.getItemInUse().getItemDamage() >= robot.getItemInUse().getMaxDamage()) {
				robot.setItemInUse(null);
			}

			terminate();
		} else {
			robot.worldObj.destroyBlockInWorldPartially(robot.getEntityId(), woodToChop.x,
					woodToChop.y, woodToChop.z, (int) (blockDamage * 10.0F) - 1);
		}
	}

	@Override
	public void end() {
		robot.setItemActive(false);
		robot.worldObj.destroyBlockInWorldPartially(robot.getEntityId(), woodToChop.x,
				woodToChop.y, woodToChop.z, -1);
	}

	private float getBreakSpeed(EntityRobotBase robot, ItemStack usingItem, Block block, int meta) {
		ItemStack stack = usingItem;
		float f = stack == null ? 1.0F : stack.getItem().getDigSpeed(stack, block, meta);

		if (f > 1.0F) {
			int i = EnchantmentHelper.getEfficiencyModifier(robot);
			ItemStack itemstack = usingItem;

			if (i > 0 && itemstack != null) {
				float f1 = i * i + 1;

				boolean canHarvest = ForgeHooks.canToolHarvestBlock(block, meta, itemstack);

				if (!canHarvest && f <= 1.0F) {
					f += f1 * 0.08F;
				} else {
					f += f1;
				}
			}
		}

		return f;
	}
}
