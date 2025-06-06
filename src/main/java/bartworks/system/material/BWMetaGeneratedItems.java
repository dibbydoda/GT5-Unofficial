/*
 * Copyright (c) 2018-2020 bartimaeusnek Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package bartworks.system.material;

import static gregtech.api.util.GTRecipeBuilder.INGOTS;
import static net.minecraft.util.StatCollector.translateToLocal;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import bartworks.API.IRadMaterial;
import bartworks.API.SideReference;
import bartworks.client.textures.PrefixTextureLinker;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.hazards.HazardProtection;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.items.MetaGeneratedItem;
import gregtech.api.util.GTLanguageManager;
import gregtech.api.util.GTOreDictUnificator;
import ic2.core.IC2Potion;

public class BWMetaGeneratedItems extends MetaGeneratedItem implements IRadMaterial {

    public static final CreativeTabs metaTab = new CreativeTabs("bartworksMetaMaterials") {

        @Override
        public Item getTabIconItem() {
            return new ItemStack(Blocks.iron_ore).getItem();
        }
    };

    protected final OrePrefixes orePrefixes;
    protected final String itemTypeLocalizedName;

    public BWMetaGeneratedItems(OrePrefixes orePrefixes, Object unused) {
        super("bwMetaGeneratedGTEnhancement" + orePrefixes.name(), (short) 32766, (short) 0);
        this.orePrefixes = orePrefixes;
        this.itemTypeLocalizedName = GTLanguageManager.addStringLocalization(
            "bw.itemtype." + orePrefixes,
            orePrefixes.mLocalizedMaterialPre + "%material" + orePrefixes.mLocalizedMaterialPost);
    }

    public BWMetaGeneratedItems(OrePrefixes orePrefixes) {
        super("bwMetaGenerated" + orePrefixes.name(), (short) 32766, (short) 0);
        this.orePrefixes = orePrefixes;
        this.itemTypeLocalizedName = GTLanguageManager.addStringLocalization(
            "bw.itemtype." + orePrefixes,
            orePrefixes.mLocalizedMaterialPre + "%material" + orePrefixes.mLocalizedMaterialPost);
        this.setCreativeTab(BWMetaGeneratedItems.metaTab);
        for (Werkstoff w : Werkstoff.werkstoffHashSet) {
            ItemStack tStack = new ItemStack(this, 1, w.getmID());
            if (!w.hasItemType(this.orePrefixes)) continue;
            GTOreDictUnificator.registerOre(this.orePrefixes.name() + w.getVarName(), tStack);
        }
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem aItemEntity) {
        if (this.orePrefixes == OrePrefixes.dustImpure || this.orePrefixes == OrePrefixes.dustPure
            || this.orePrefixes == OrePrefixes.crushed) {
            int aDamage = aItemEntity.getEntityItem()
                .getItemDamage();
            if (aDamage >= 0 && !aItemEntity.worldObj.isRemote) {
                Werkstoff aMaterial = Werkstoff.werkstoffHashMap.get((short) aDamage);
                if (aMaterial != null && aMaterial != Werkstoff.default_null_Werkstoff) {
                    int tX = MathHelper.floor_double(aItemEntity.posX);
                    int tY = MathHelper.floor_double(aItemEntity.posY);
                    int tZ = MathHelper.floor_double(aItemEntity.posZ);
                    Block tBlock = aItemEntity.worldObj.getBlock(tX, tY, tZ);
                    int tMetaData = aItemEntity.worldObj.getBlockMetadata(tX, tY, tZ);
                    if (tBlock == Blocks.cauldron && tMetaData > 0) {
                        if (this.orePrefixes == OrePrefixes.dustImpure || this.orePrefixes == OrePrefixes.dustPure) {
                            aItemEntity.setEntityItemStack(
                                WerkstoffLoader.getCorrespondingItemStack(
                                    OrePrefixes.dust,
                                    aMaterial,
                                    aItemEntity.getEntityItem().stackSize));
                        } else {
                            aItemEntity.setEntityItemStack(
                                WerkstoffLoader.getCorrespondingItemStack(
                                    OrePrefixes.crushedPurified,
                                    aMaterial,
                                    aItemEntity.getEntityItem().stackSize));
                        }
                        aItemEntity.worldObj.setBlockMetadataWithNotify(tX, tY, tZ, tMetaData - 1, 3);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void addAdditionalToolTips(List<String> aList, ItemStack aStack, EntityPlayer aPlayer) {
        if (this.orePrefixes == OrePrefixes.dustImpure || this.orePrefixes == OrePrefixes.dustPure) {
            aList.add(translateToLocal("GT5U.tooltip.purify.1"));
        }
        if (this.orePrefixes == OrePrefixes.crushed) aList.add(translateToLocal("GT5U.tooltip.purify.2"));

        if (aStack != null && aStack.getItem() instanceof BWMetaGeneratedItems
            && aStack.getItemDamage() == WerkstoffLoader.Tiberium.getmID())
            aList.add(translateToLocal("GT5U.tooltip.nqgen"));

        Werkstoff werkstoff = Werkstoff.werkstoffHashMap.get((short) this.getDamage(aStack));
        if (werkstoff != null) {
            String tooltip = werkstoff.getLocalizedToolTip();
            if (!tooltip.isEmpty()) {
                aList.add(tooltip);
            }
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack aStack) {
        int aMetaData = aStack.getItemDamage();
        Werkstoff werkstoff = Werkstoff.werkstoffHashMap.get((short) aMetaData);
        if (werkstoff == null) werkstoff = Werkstoff.default_null_Werkstoff;
        return this.itemTypeLocalizedName.replace("%material", werkstoff.getLocalizedName());
    }

    @Override
    public IIconContainer getIconContainer(int aMetaData) {
        if (Werkstoff.werkstoffHashMap.get((short) aMetaData) == null) return null;
        if (this.orePrefixes.mTextureIndex == -1) return this.getIconContainerBartWorks(aMetaData);
        return Werkstoff.werkstoffHashMap.get((short) aMetaData)
            .getTexSet().mTextures[this.orePrefixes.mTextureIndex];
    }

    protected IIconContainer getIconContainerBartWorks(int aMetaData) {
        if (SideReference.Side.Client) return PrefixTextureLinker.texMap.get(this.orePrefixes)
            .get(
                Werkstoff.werkstoffHashMap.get((short) aMetaData)
                    .getTexSet());
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item var1, CreativeTabs aCreativeTab, List<ItemStack> aList) {
        for (Werkstoff werkstoff : Werkstoff.werkstoffHashSet) {
            if (werkstoff != null && werkstoff.hasItemType(this.orePrefixes)) {
                ItemStack tStack = new ItemStack(this, 1, werkstoff.getmID());
                aList.add(tStack);
            }
        }
        // super.getSubItems(var1, aCreativeTab, aList);
    }

    @Override
    public short[] getRGBa(ItemStack aStack) {
        Werkstoff werkstoff = Werkstoff.werkstoffHashMap.get((short) this.getDamage(aStack));
        return werkstoff == null ? Materials._NULL.mRGBa : werkstoff.getRGBA();
    }

    @Override
    public void onUpdate(ItemStack aStack, World aWorld, Entity aPlayer, int aTimer, boolean aIsInHand) {
        super.onUpdate(aStack, aWorld, aPlayer, aTimer, aIsInHand);
        if (aStack == null || aStack.getItem() == null || !(aPlayer instanceof EntityLivingBase)) return;

        EntityLivingBase bPlayer = (EntityPlayer) aPlayer;
        Werkstoff w = Werkstoff.werkstoffHashMap.get((short) aStack.getItemDamage());
        if (w == null || w.getStats() == null) return;

        if (w.getStats()
            .isToxic() && !HazardProtection.isWearingFullBioHazmat(bPlayer)) {
            bPlayer.addPotionEffect(new PotionEffect(Potion.poison.getId(), 80, 4));
        }

        if (w.getStats()
            .isRadioactive() && !HazardProtection.isWearingFullRadioHazmat(bPlayer)) {
            bPlayer.addPotionEffect(new PotionEffect(IC2Potion.radiation.id, 80, 4));
        }
    }

    @Override
    public IIcon getIconFromDamage(int aMetaData) {
        if (aMetaData < 0) return null;
        Werkstoff tMaterial = Werkstoff.werkstoffHashMap.get((short) aMetaData);
        if (tMaterial == null) return null;
        IIconContainer tIcon = this.getIconContainer(aMetaData);
        if (tIcon != null) return tIcon.getIcon();
        return null;
    }

    @Override
    public int getItemStackLimit(ItemStack aStack) {
        return this.orePrefixes.mDefaultStackSize;
    }

    @Override
    public int getRadiationLevel(ItemStack aStack) {
        Werkstoff w = Werkstoff.werkstoffHashMap.get((short) aStack.getItemDamage());
        return w.getStats()
            .isRadioactive()
                ? (int) w.getStats()
                    .getProtons()
                : 0;
    }

    @Override
    public byte getAmountOfMaterial(ItemStack aStack) {
        return (byte) (this.orePrefixes == OrePrefixes.stick ? 1 : this.orePrefixes == OrePrefixes.stickLong ? 2 : 0);
    }

    @Override
    public short[] getColorForGUI(ItemStack aStack) {
        Werkstoff w = Werkstoff.werkstoffHashMap.get((short) aStack.getItemDamage());
        return w.getRGBA();
    }

    @Override
    public String getNameForGUI(ItemStack aStack) {
        Werkstoff w = Werkstoff.werkstoffHashMap.get((short) aStack.getItemDamage());
        return w.getDefaultName();
    }

    @Override
    public int getCapacity(ItemStack aStack) {
        return this.orePrefixes == OrePrefixes.capsule || this.orePrefixes == OrePrefixes.cell
            || this.orePrefixes == OrePrefixes.cellPlasma
                ? 1_000
                : this.orePrefixes == OrePrefixes.cellMolten || this.orePrefixes == OrePrefixes.capsuleMolten
                    ? 1 * INGOTS
                    : 0;
    }

    @Override
    public ItemStack getContainerItem(ItemStack aStack) {
        if (this.orePrefixes == OrePrefixes.cell || this.orePrefixes == OrePrefixes.cellPlasma
            || this.orePrefixes == OrePrefixes.cellMolten) {
            return Materials.Empty.getCells(1);
        }

        if (this.orePrefixes == OrePrefixes.capsule) return ItemList.FR_WaxCapsule.get(1);
        if (this.orePrefixes == OrePrefixes.capsuleMolten) return ItemList.FR_RefractoryCapsule.get(1);
        return null;
    }
}
