/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.world;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jeva.math.Vector2D;
import jeva.math.Vector2F;

/**
 * The Class EffectMap.
 * 
 * @author Scott
 */
public class EffectMap
{

	/** The m_tile effects. */
	private HashMap<Vector2D, TileEffects> m_tileEffects;

	/** The m_bounds. */
	private Rectangle m_bounds;

	/**
	 * Instantiates a new effect map.
	 */
	public EffectMap()
	{
		m_bounds = null;
		m_tileEffects = new HashMap<Vector2D, TileEffects>();
	}

	/**
	 * Instantiates a new effect map.
	 * 
	 * @param bounds
	 *            the bounds
	 */
	public EffectMap(Rectangle bounds)
	{
		m_bounds = bounds;
		m_tileEffects = new HashMap<Vector2D, TileEffects>();
	}

	/**
	 * Instantiates a new effect map.
	 * 
	 * @param map
	 *            the map
	 */
	public EffectMap(EffectMap map)
	{
		m_bounds = map.m_bounds;
		m_tileEffects = new HashMap<Vector2D, TileEffects>();

		for (Map.Entry<Vector2D, TileEffects> effects : map.m_tileEffects.entrySet())
			applyOverlayEffects(effects.getKey(), effects.getValue());
	}

	/**
	 * Clear.
	 */
	public void clear()
	{
		m_tileEffects.clear();
	}

	/**
	 * Gets the tile effects.
	 * 
	 * @param location
	 *            the location
	 * @return the tile effects
	 */
	public final TileEffects getTileEffects(Vector2D location)
	{
		if (m_bounds != null && !m_bounds.contains(new Point(location.x, location.y)))
		{
			TileEffects effects = new TileEffects(location);
			effects.isTraversable = false;

			return effects;
		}

		if (!m_tileEffects.containsKey(location))
			return new TileEffects();

		return m_tileEffects.get(location);
	}

	/**
	 * Gets the tile effects.
	 * 
	 * @param filter
	 *            the filter
	 * @return the tile effects
	 */
	public final TileEffects[] getTileEffects(ISearchFilter<TileEffects> filter)
	{
		ArrayList<TileEffects> tileEffects = new ArrayList<TileEffects>();

		Rectangle searchBounds = filter.getSearchBounds();

		for (int x = searchBounds.x; x <= searchBounds.x + searchBounds.width; x++)
		{
			for (int y = searchBounds.y; y <= searchBounds.y + searchBounds.height; y++)
			{
				TileEffects effects = getTileEffects(new Vector2D(x, y));

				if (effects != null && filter.shouldInclude(new Vector2F(x, y)) && (effects = filter.filter(effects)) != null)
				{
					tileEffects.add(effects);
				}
			}
		}

		return tileEffects.toArray(new TileEffects[tileEffects.size()]);
	}

	/**
	 * Apply overlay effects.
	 * 
	 * @param filter
	 *            the filter
	 * @param overlay
	 *            the overlay
	 */
	public final void applyOverlayEffects(ISearchFilter<TileEffects> filter, TileEffects overlay)
	{
		Rectangle searchBounds = filter.getSearchBounds();

		for (int x = searchBounds.x; x <= searchBounds.width; x++)
		{
			for (int y = searchBounds.y; y <= searchBounds.height; y++)
			{
				TileEffects effects = getTileEffects(new Vector2D(x, y));

				if (filter.shouldInclude(new Vector2F(x, y)) && effects == null)
				{
					m_tileEffects.put(new Vector2D(x, y), overlay);
					effects = overlay;
				}

				if (effects != null && filter.shouldInclude(new Vector2F(x, y)) && (effects = filter.filter(effects)) != null)
				{
					effects.overlay(overlay);
				}
			}
		}
	}

	/**
	 * Apply overlay effects.
	 * 
	 * @param location
	 *            the location
	 * @param value
	 *            the value
	 */
	public final void applyOverlayEffects(Vector2D location, TileEffects value)
	{
		if (m_bounds != null && !m_bounds.contains(new Point(location.x, location.y)))
			return;

		TileEffects effect = new TileEffects(value);

		if (!m_tileEffects.containsKey(location))
			m_tileEffects.put(location, effect);
		else
			m_tileEffects.get(location).overlay(effect);

		effect.location = location;
	}

	/**
	 * Overlay.
	 * 
	 * @param overlay
	 *            the overlay
	 * @param offset
	 *            the offset
	 */
	public final void overlay(EffectMap overlay, Vector2D offset)
	{
		for (Map.Entry<Vector2D, TileEffects> effects : overlay.m_tileEffects.entrySet())
			applyOverlayEffects(effects.getKey().add(offset), effects.getValue());
	}

	/**
	 * Overlay.
	 * 
	 * @param overlay
	 *            the overlay
	 */
	public final void overlay(EffectMap overlay)
	{
		overlay(overlay, new Vector2D());
	}

	/**
	 * The Class TileEffects.
	 */
	public static class TileEffects
	{

		/** The interactables. */
		public ArrayList<IInteractable> interactables;

		/** The is traversable. */
		public boolean isTraversable;

		/** The sight effect. */
		public float sightEffect;

		/** The location. */
		public Vector2D location;

		/**
		 * Instantiates a new tile effects.
		 */
		public TileEffects()
		{
			location = new Vector2D();
			isTraversable = true;
			sightEffect = 1.0F;
			interactables = new ArrayList<IInteractable>();
		}

		/**
		 * Instantiates a new tile effects.
		 * 
		 * @param _location
		 *            the _location
		 */
		public TileEffects(Vector2D _location)
		{
			location = _location;
			isTraversable = true;
			sightEffect = 1.0F;
			interactables = new ArrayList<IInteractable>();
		}

		/**
		 * Instantiates a new tile effects.
		 * 
		 * @param effects
		 *            the effects
		 */
		public TileEffects(TileEffects effects)
		{
			location = effects.location;
			isTraversable = effects.isTraversable;
			sightEffect = effects.sightEffect;

			interactables = new ArrayList<IInteractable>(effects.interactables);
		}

		/**
		 * Instantiates a new tile effects.
		 * 
		 * @param _isTraversable
		 *            the _is traversable
		 */
		public TileEffects(boolean _isTraversable)
		{
			location = new Vector2D();
			isTraversable = _isTraversable;
			sightEffect = 1.0F;
			interactables = new ArrayList<IInteractable>();
		}

		/**
		 * Instantiates a new tile effects.
		 * 
		 * @param _sightEffect
		 *            the _sight effect
		 */
		public TileEffects(float _sightEffect)
		{
			location = new Vector2D();
			isTraversable = true;
			sightEffect = _sightEffect;
			interactables = new ArrayList<IInteractable>();
		}

		/**
		 * Instantiates a new tile effects.
		 * 
		 * @param _interactables
		 *            the _interactables
		 */
		public TileEffects(IInteractable... _interactables)
		{
			location = new Vector2D();
			isTraversable = true;
			interactables = new ArrayList<IInteractable>(Arrays.asList(_interactables));
		}

		/**
		 * Merge.
		 * 
		 * @param tiles
		 *            the tiles
		 * @return the tile effects
		 */
		public static TileEffects merge(TileEffects[] tiles)
		{
			TileEffects effect = new TileEffects();

			for (TileEffects tile : tiles)
				effect.overlay(tile);

			return effect;
		}

		/**
		 * Overlay.
		 * 
		 * @param overlay
		 *            the overlay
		 * @return the tile effects
		 */
		public TileEffects overlay(TileEffects overlay)
		{
			isTraversable &= overlay.isTraversable;
			sightEffect = Math.min(sightEffect, overlay.sightEffect);

			for (IInteractable i : overlay.interactables)
			{
				if (!interactables.contains(i))
					interactables.add(i);
			}

			return this;
		}
	}
}