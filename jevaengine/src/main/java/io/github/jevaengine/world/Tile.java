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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.jevaengine.world;

import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.EffectMap.TileEffects;

public class Tile extends Actor
{

	protected Sprite m_sprite;

	private boolean m_isTraversable;

	private float m_fVisiblityObstruction;

	private boolean m_enableSplitting;

	public Tile(String name, Sprite sprite, WorldDirection direction, String defaultAnimation, boolean isTraversable, boolean enableSplitting, float fVisibilityObstruction)
	{
		super(name, direction);

		m_sprite = sprite;
		m_isTraversable = isTraversable;
		m_enableSplitting = enableSplitting;

		setAnimation(defaultAnimation);

		m_fVisiblityObstruction = fVisibilityObstruction;
	}

	public Tile(Sprite sprite, WorldDirection direction, String defaultAnimation, boolean isTraversable, boolean enableSplitting, float fVisibilityObstruction)
	{
		super(null, direction);

		m_sprite = sprite;
		m_isTraversable = isTraversable;
		m_enableSplitting = enableSplitting;

		setAnimation(defaultAnimation);

		m_fVisiblityObstruction = fVisibilityObstruction;
	}

	protected void setVisibilityObstruction(float fObstruction)
	{
		m_fVisiblityObstruction = fObstruction;
	}

	protected float getVisibilityObstruction()
	{
		return m_fVisiblityObstruction;
	}

	protected void setAnimation(String animation)
	{
		m_sprite.setAnimation(getDirection().toString() + animation, AnimationState.Play);
	}

	protected void setSprite(Sprite sprite)
	{
		m_sprite = sprite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.Actor#getGraphics()
	 */
	@Override
	public IRenderable[] getGraphics()
	{
		return new Sprite[]
		{ m_sprite };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.Actor#getTileWidth()
	 */
	@Override
	public int getTileWidth()
	{
		if (!m_enableSplitting)
			return 1;

		// get tile width
		Vector2F v = getWorld().getPerspectiveMatrix(1.0F).dot(new Vector2F(1, -1));
		return (int) (Math.ceil((m_sprite.getBounds().width) / v.x));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.Actor#getTileHeight()
	 */
	@Override
	public int getTileHeight()
	{
		if (!m_enableSplitting)
			return 1;

		// get tile height
		Vector2F v = getWorld().getPerspectiveMatrix(1.0F).dot(new Vector2F(0, 2));
		return (int) (Math.ceil((m_sprite.getBounds().height) / v.y));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.Entity#blendEffectMap(jeva.world.EffectMap)
	 */
	@Override
	public void blendEffectMap(EffectMap globalEffectMap)
	{
		globalEffectMap.applyOverlayEffects(this.getLocation().round(), new TileEffects(m_isTraversable).overlay(new TileEffects(m_fVisiblityObstruction)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.Entity#doLogic(int)
	 */
	@Override
	public void doLogic(int deltaTime)
	{
		m_sprite.update(deltaTime);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.Actor#getVisibilityFactor()
	 */
	@Override
	public float getVisibilityFactor()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.Actor#getViewDistance()
	 */
	@Override
	public float getViewDistance()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.Actor#getFieldOfView()
	 */
	@Override
	public float getFieldOfView()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.Actor#getVisualAcuity()
	 */
	@Override
	public float getVisualAcuity()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.Actor#getSpeed()
	 */
	@Override
	public float getSpeed()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.Actor#getAllowedMovements()
	 */
	@Override
	public WorldDirection[] getAllowedMovements()
	{
		return new WorldDirection[]
		{ WorldDirection.Zero };
	}
}