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
package io.github.jevaengine.rpgbase.demo;

import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.graphics.Sprite.SpriteDeclaration;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.RpgGame;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.UIStyle.UIStyleDeclaration;
import io.github.jevaengine.util.Nullable;

public class DemoGame extends RpgGame implements IStateContext
{
	@Nullable private RpgCharacter m_player;

	private Sprite m_cursor;
	
	private IState m_state;
	
	@Override
	protected void startup()
	{
		super.startup();
		
		ResourceLibrary library = Core.getService(ResourceLibrary.class);
		
		UIStyle style = UIStyle.create(library.openConfiguration("ui/game.juis").getValue(UIStyleDeclaration.class));
		
		m_cursor = Sprite.create(library.openConfiguration("ui/tech/cursor/cursor.jsf").getValue(SpriteDeclaration.class));
		m_cursor.setAnimation("idle", AnimationState.Play);
		
		m_state = new MainMenu(style);
		m_state.enter(this);
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);
		m_state.update(deltaTime);
	}

	@Override
	public void setPlayer(@Nullable RpgCharacter player)
	{
		m_player = player;
	}

	@Override
	public RpgCharacter getPlayer()
	{
		return m_player;
	}

	@Override
	protected Sprite getCursor()
	{
		return m_cursor;
	}
	
	public void setState(IState state)
	{
		m_state.leave();
		m_state = state;
		state.enter(this);
	}
}
