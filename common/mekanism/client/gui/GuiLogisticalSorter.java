package mekanism.client.gui;

import java.util.ArrayList;

import mekanism.api.Object3D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketLogisticalSorterGui;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.transporter.ItemStackFilter;
import mekanism.common.transporter.OreDictFilter;
import mekanism.common.transporter.TransporterFilter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLogisticalSorter extends GuiMekanism
{
	public TileEntityLogisticalSorter tileEntity;
	
	public boolean isDragging = false;
	
	public int dragOffset = 0;
	
	public float scroll;
	
	public GuiLogisticalSorter(EntityPlayer player, TileEntityLogisticalSorter tentity)
	{
		super(new ContainerNull(player, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiLogisticalSorter.png")));
	}
	
	public int getScroll()
	{
		return Math.max(Math.min((int)(scroll*125), 125), 0);
	}
	
	public int getFilterIndex()
	{
		return 0;
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);
		
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		if(xAxis >= 154 && xAxis <= 166 && yAxis >= getScroll()+18 && yAxis <= getScroll()+18+15)
		{
			dragOffset = yAxis - (getScroll()+18);
			isDragging = true;
		}
		
		for(int i = 0; i < 4; i++)
		{
			if(tileEntity.filters.get(getFilterIndex()+i) != null)
			{
				int yStart = i*29 + 18;
				
				if(xAxis >= 56 && xAxis <= 152 && yAxis >= yStart && yAxis <= yStart+29)
				{
					TransporterFilter filter = tileEntity.filters.get(getFilterIndex()+i);
					
					if(filter instanceof ItemStackFilter)
					{
						PacketHandler.sendPacket(Transmission.SERVER, new PacketLogisticalSorterGui().setParams(SorterGuiPacket.SERVER_INDEX, Object3D.get(tileEntity), 1, getFilterIndex()+i));
					}
					else if(filter instanceof OreDictFilter)
					{
						PacketHandler.sendPacket(Transmission.SERVER, new PacketLogisticalSorterGui().setParams(SorterGuiPacket.SERVER_INDEX, Object3D.get(tileEntity), 2, getFilterIndex()+i));
					}
				}
			}
		}
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int button, long ticks)
	{
		super.mouseClickMove(mouseX, mouseY, button, ticks);
		
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		if(isDragging)
		{
			scroll = (float)(yAxis-18-dragOffset)/123F;
		}
	}
	
	@Override
	protected void mouseMovedOrUp(int x, int y, int type)
	{
		super.mouseMovedOrUp(x, y, type);
		
		if(type == 0 && isDragging)
		{
			dragOffset = 0;
			isDragging = false;
		}
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
		
		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 56, guiHeight + 136, 54, 20, "ItemStack"));
		buttonList.add(new GuiButton(1, guiWidth + 110, guiHeight + 136, 43, 20, "OreDict"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);
		
		if(guibutton.id == 0)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketLogisticalSorterGui().setParams(SorterGuiPacket.SERVER, Object3D.get(tileEntity), 1));
		}
		else if(guibutton.id == 1)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketLogisticalSorterGui().setParams(SorterGuiPacket.SERVER, Object3D.get(tileEntity), 2));
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {	
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		fontRenderer.drawString("Logistical Sorter", 43, 6, 0x404040);
		fontRenderer.drawString("Filters:", 11, 19, 0x00CD00);
		fontRenderer.drawString("T: " + tileEntity.filters.size(), 11, 28, 0x00CD00);
		fontRenderer.drawString("IS: " + getItemStackFilters().size(), 11, 37, 0x00CD00);
		fontRenderer.drawString("OD: " + getOreDictFilters().size(), 11, 46, 0x00CD00);
		
		for(int i = 0; i < 4; i++)
		{
			if(tileEntity.filters.get(getFilterIndex()+i) != null)
			{
				TransporterFilter filter = tileEntity.filters.get(getFilterIndex()+i);
				int yStart = i*29 + 18;
				
				if(filter instanceof ItemStackFilter)
				{
					fontRenderer.drawString("ItemStack Filter", 58, yStart + 2, 0x404040);
					fontRenderer.drawString("Color: " + filter.color.getName(), 58, yStart + 11, 0x404040);
				}
				else if(filter instanceof OreDictFilter)
				{
					fontRenderer.drawString("OreDict Filter", 58, yStart + 2, 0x404040);
					fontRenderer.drawString("Color: " + filter.color.getName(), 58, yStart + 11, 0x404040);
				}
			}
		}
		
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

	@Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
    {
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
		
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiLogisticalSorter.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
		drawTexturedModalRect(guiWidth + 154, guiHeight + 18 + getScroll(), 232, 0, 12, 15);
		
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		for(int i = 0; i < 4; i++)
		{
			if(tileEntity.filters.get(getFilterIndex()+i) != null)
			{
				TransporterFilter filter = tileEntity.filters.get(getFilterIndex()+i);
				int yStart = i*29 + 18;
				
				boolean mouseOver = xAxis >= 56 && xAxis <= 152 && yAxis >= yStart && yAxis <= yStart+29;
				
				if(filter instanceof ItemStackFilter)
				{
					drawTexturedModalRect(guiWidth + 56, guiHeight + yStart, mouseOver ? 0 : 96, 166, 96, 29);
				}
				else if(filter instanceof OreDictFilter)
				{
					drawTexturedModalRect(guiWidth + 56, guiHeight + yStart, mouseOver ? 0 : 96, 195, 96, 29);
				}
			}
		}
    }
	
	public ArrayList getItemStackFilters()
	{
		ArrayList list = new ArrayList();
		
		for(TransporterFilter filter : tileEntity.filters)
		{
			if(filter instanceof ItemStackFilter)
			{
				list.add(filter);
			}
		}
		
		return list;
	}
	
	public ArrayList getOreDictFilters()
	{
		ArrayList list = new ArrayList();
		
		for(TransporterFilter filter : tileEntity.filters)
		{
			if(filter instanceof OreDictFilter)
			{
				list.add(filter);
			}
		}
		
		return list;
	}
}