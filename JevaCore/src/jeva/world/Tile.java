/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.world;

import jeva.graphics.AnimationState;
import jeva.graphics.IRenderable;
import jeva.graphics.Sprite;
import jeva.math.Vector2F;
import jeva.world.EffectMap;
import jeva.world.EffectMap.TileEffects;

/**
 * The Class Tile.
 * 
 * @author Scott
 */
public class Tile extends Actor
{

	/** The m_sprite. */
	protected Sprite m_sprite;

	/** The m_is traversable. */
	private boolean m_isTraversable;

	/** The m_f visiblity obstruction. */
	private float m_fVisiblityObstruction;

	/** The m_enable splitting. */
	private boolean m_enableSplitting;

	/**
	 * Instantiates a new tile.
	 * 
	 * @param name
	 *            the name
	 * @param sprite
	 *            the sprite
	 * @param direction
	 *            the direction
	 * @param defaultAnimation
	 *            the default animation
	 * @param isTraversable
	 *            the is traversable
	 * @param enableSplitting
	 *            the enable splitting
	 * @param fVisibilityObstruction
	 *            the f visibility obstruction
	 */
	public Tile(String name, Sprite sprite, WorldDirection direction, String defaultAnimation, boolean isTraversable, boolean enableSplitting, float fVisibilityObstruction)
	{
		super(name, direction);

		m_sprite = sprite;
		m_isTraversable = isTraversable;
		m_enableSplitting = enableSplitting;

		setAnimation(defaultAnimation);

		m_fVisiblityObstruction = fVisibilityObstruction;
	}

	/**
	 * Instantiates a new tile.
	 * 
	 * @param sprite
	 *            the sprite
	 * @param direction
	 *            the direction
	 * @param defaultAnimation
	 *            the default animation
	 * @param isTraversable
	 *            the is traversable
	 * @param enableSplitting
	 *            the enable splitting
	 * @param fVisibilityObstruction
	 *            the f visibility obstruction
	 */
	public Tile(Sprite sprite, WorldDirection direction, String defaultAnimation, boolean isTraversable, boolean enableSplitting, float fVisibilityObstruction)
	{
		super(null, direction);
		m_sprite = sprite;
		m_isTraversable = isTraversable;
		m_enableSplitting = enableSplitting;

		setAnimation(defaultAnimation);

		m_fVisiblityObstruction = fVisibilityObstruction;
	}

	/**
	 * Sets the visibility obstruction.
	 * 
	 * @param fObstruction
	 *            the new visibility obstruction
	 */
	protected void setVisibilityObstruction(float fObstruction)
	{
		m_fVisiblityObstruction = fObstruction;
	}

	/**
	 * Gets the visibility obstruction.
	 * 
	 * @return the visibility obstruction
	 */
	protected float getVisibilityObstruction()
	{
		return m_fVisiblityObstruction;
	}

	/**
	 * Sets the animation.
	 * 
	 * @param animation
	 *            the new animation
	 */
	protected void setAnimation(String animation)
	{
		m_sprite.setAnimation(getDirection().toString() + animation, AnimationState.Play);
	}

	/**
	 * Sets the sprite.
	 * 
	 * @param sprite
	 *            the new sprite
	 */
	protected void setSprite(Sprite sprite)
	{
		m_sprite = sprite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getGraphics()
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
	 * @see jeva.world.Actor#getTileWidth()
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
	 * @see jeva.world.Actor#getTileHeight()
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
	 * @see jeva.world.Entity#blendEffectMap(jeva.world.EffectMap)
	 */
	@Override
	public void blendEffectMap(EffectMap globalEffectMap)
	{
		globalEffectMap.applyOverlayEffects(this.getLocation().round(), new TileEffects(m_isTraversable).overlay(new TileEffects(m_fVisiblityObstruction)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Entity#doLogic(int)
	 */
	@Override
	public void doLogic(int deltaTime)
	{
		m_sprite.update(deltaTime);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getVisibilityFactor()
	 */
	@Override
	public float getVisibilityFactor()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getViewDistance()
	 */
	@Override
	public float getViewDistance()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getFieldOfView()
	 */
	@Override
	public float getFieldOfView()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getVisualAcuity()
	 */
	@Override
	public float getVisualAcuity()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getSpeed()
	 */
	@Override
	public float getSpeed()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getAllowedMovements()
	 */
	@Override
	public WorldDirection[] getAllowedMovements()
	{
		return new WorldDirection[]
		{ WorldDirection.Zero };
	}
}