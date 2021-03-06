/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
package io.github.jevaengine.rpgbase;

import io.github.jevaengine.Core;
import io.github.jevaengine.CoreScriptException;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.Script;
import io.github.jevaengine.audio.Audio;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.graphics.Sprite.SpriteDeclaration;
import io.github.jevaengine.util.Nullable;

import javax.script.ScriptException;

import org.mozilla.javascript.NativeArray;

public class Item
{
	private ItemIdentifer m_identifer;

	private String m_name;
	private String m_description;

	private Sprite m_graphic;

	private ItemType m_type;

	private Script m_script;

	public enum ItemType
	{
		General(),
		Weapon(true, "item/weaponGear.jsf"),
		HeadArmor(true, "item/headGear.jsf"),
		BodyArmor(true, "item/bodyGear.jsf"),
		Accessory(true, "item/accessoryGear.jsf");

		private Sprite m_icon;
		private boolean m_isWieldable;

		ItemType()
		{
			m_icon = null;
			m_isWieldable = false;
		}

		ItemType(boolean isWieldable, String backgroundSpritePath)
		{
			m_isWieldable = isWieldable;
			m_icon = Sprite.create(Core.getService(ResourceLibrary.class).openConfiguration(backgroundSpritePath).getValue(SpriteDeclaration.class));
			m_icon.setAnimation("idle", AnimationState.Play);
		}

		public boolean hasIcon()
		{
			return m_icon != null;
		}
		
		public boolean isWieldable()
		{
			return m_isWieldable;
		}

		@Nullable
		public Sprite getIcon()
		{
			return m_icon;
		}
	}

	public Item(ItemIdentifer identifer, String name, Sprite graphic, ItemType itemType, Script script, String description)
	{
		m_identifer = identifer;
		m_name = name;
		m_graphic = graphic;
		m_type = itemType;
		m_script = script;
		m_description = description;
	}

	public static Item create(ItemIdentifer identifier)
	{
		ItemDeclaration itemDecl = Core.getService(ResourceLibrary.class)
									.openConfiguration(identifier.m_descriptor)
									.getValue(ItemDeclaration.class);

		Sprite graphic = Sprite.create(
							Core.getService(ResourceLibrary.class)
								.openConfiguration(itemDecl.sprite)
								.getValue(SpriteDeclaration.class));
		
		Script script;

		if (itemDecl.script != null)
			script = Core.getService(ResourceLibrary.class).openScript(itemDecl.script, new ItemBridge());
		else
			script = new Script();
		
		graphic.setAnimation("idle", AnimationState.Play);

		return new Item(identifier, itemDecl.name, graphic, itemDecl.type, script, itemDecl.description);

	}

	public static Item create(String descriptor)
	{
		return create(new ItemIdentifer(descriptor));
	}

	public String[] getCommands()
	{
		try
		{
			NativeArray jsStringArray = (NativeArray)m_script.invokeScriptFunction("getCommands");

			if (jsStringArray == null)
				return new String[0];

			String[] commands = new String[(int) jsStringArray.getLength()];

			for (int i = 0; i < commands.length; i++)
			{
				Object element = jsStringArray.get(i, null);

				if (!(element instanceof String))
					throw new CoreScriptException("Unexpected data returned on invoking getCommands for Actor Interactable entity.");

				commands[i] = (String) element;
			}
			
			return commands;
		} catch (NoSuchMethodException ex)
		{
			return new String[0];
		} catch (ScriptException ex)
		{
			throw new CoreScriptException(ex);
		}
	}

	public void doCommand(RpgCharacter user, ItemSlot slot, String command)
	{
		try
		{
			m_script.invokeScriptFunction("doCommand", user, slot.getScriptBridge(), command);
			
		} catch (NoSuchMethodException ex) { 
		} catch (ScriptException ex)
		{
			throw new CoreScriptException(ex);
		}
	}
	
	public ItemType getType()
	{
		return m_type;
	}

	public ItemIdentifer getDescriptor()
	{
		return m_identifer;
	}

	public String getName()
	{
		return m_name;
	}

	public IRenderable getGraphic()
	{
		return m_graphic;
	}

	public String getDescription()
	{
		return m_description;
	}

	public boolean use(RpgCharacter user, RpgCharacter target)
	{
		try {
			Object o = m_script.invokeScriptFunction("use", user, target);
			
			if(!(o instanceof Boolean))
				throw new CoreScriptException("Use does not return the expected return value.");
			else
				return ((Boolean)o).booleanValue();
			
		} catch (NoSuchMethodException e) {
			return false;
		} catch (ScriptException e) {
			throw new CoreScriptException(e);
		}
	}

	public static class ItemIdentifer implements Comparable<ItemIdentifer>
	{
		private String m_descriptor;

		public ItemIdentifer(String descriptor)
		{
			m_descriptor = descriptor.trim().toLowerCase().replace('\\', '/');
			m_descriptor = (m_descriptor.startsWith("/") ? m_descriptor.substring(1) : m_descriptor);
		}

		@Override
		public int compareTo(ItemIdentifer item)
		{
			return m_descriptor.compareTo(item.m_descriptor);
		}

		@Override
		public boolean equals(Object o)
		{
			if (o instanceof ItemIdentifer)
				return ((ItemIdentifer) o).m_descriptor.compareTo(m_descriptor) == 0;
			else
				return new ItemIdentifer(o.toString()).m_descriptor.compareTo(m_descriptor) == 0;
		}

		@Override
		public String toString()
		{
			return m_descriptor;
		}
	}

	public static class ItemBridge
	{
		public void playAudio(String audioName)
		{
			new Audio(audioName).play();
		}
	}
	
	public static class ItemDeclaration implements ISerializable
	{
		public String name;
		public ItemType type;
		public String sprite;
		
		public String description;
		
		@Nullable
		public String script;

		@Override
		public void serialize(IVariable target)
		{
			target.addChild("name").setValue(name);
			target.addChild("type").setValue(type.ordinal());
			target.addChild("sprite").setValue(sprite);
			
			if(description != null && description.length() > 0)
				target.addChild("description").setValue(description);
			
			if(script != null && script.length() > 0)
				target.addChild("script").setValue(script);
		}

		@Override
		public void deserialize(IImmutableVariable source)
		{
			name = source.getChild("name").getValue(String.class);
			type = ItemType.values()[source.getChild("type").getValue(Integer.class)];
			sprite = source.getChild("sprite").getValue(String.class);
			
			if(source.childExists("description"))
				description = source.getChild("description").getValue(String.class);
			
			if(source.childExists("script"))
				script = source.getChild("script").getValue(String.class);
		}
	}
}
