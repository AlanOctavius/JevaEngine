/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.world;

import com.sun.istack.internal.Nullable;

import jeva.math.Vector2D;
import jeva.math.Vector2F;

/**
 * The Class TraverseRouteTask.
 * 
 * @author Scott
 */
public class TraverseRouteTask extends MovementTask
{

	/** The m_travel route. */
	private Route m_travelRoute;

	/** The m_route destination. */
	private Vector2D m_routeDestination;

	/** The m_f radius. */
	private float m_fRadius;

	/**
	 * Instantiates a new traverse route task.
	 * 
	 * @param traveler
	 *            the traveler
	 * @param destination
	 *            the destination
	 * @param fRadius
	 *            the f radius
	 */
	public TraverseRouteTask(IRouteTraveler traveler, @Nullable Vector2D destination, float fRadius)
	{
		super(traveler);

		m_travelRoute = new Route();

		m_routeDestination = destination;
		m_fRadius = fRadius;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.MovementTask#begin(jeva.world.Entity)
	 */
	@Override
	public final void begin(Entity entity)
	{
		super.begin(entity);

		try
		{
			World world = entity.getWorld();

			if (m_routeDestination == null)
			{
				m_travelRoute = Route.createRandom(new WorldNavigationRoutingRules((IRouteTraveler) getTraveler()), world, getTraveler().getSpeed(), getTraveler().getLocation().round(), (int) m_fRadius, true);
			} else
			{
				m_travelRoute = Route.create(new WorldNavigationRoutingRules((IRouteTraveler) getTraveler()), world, getTraveler().getSpeed(), getTraveler().getLocation().round(), m_routeDestination, m_fRadius, true);
			}
		} catch (IncompleteRouteException r)
		{
			m_travelRoute = new Route(getTraveler().getSpeed(), r.getIncompleteRoute());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.MovementTask#getDestination()
	 */
	@Override
	protected final Vector2F getDestination()
	{
		if (m_travelRoute.getCurrentTarget() == null)
			return getTraveler().getLocation();

		return new Vector2F(m_travelRoute.getCurrentTarget().getLocation());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.MovementTask#atDestination()
	 */
	@Override
	protected final boolean atDestination()
	{
		if (m_travelRoute.getCurrentTarget() != null)
			m_travelRoute.getCurrentTarget().unschedule(m_travelRoute);

		return !m_travelRoute.nextTarget();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.MovementTask#blocking()
	 */
	@Override
	protected final void blocking()
	{
		this.cancel();
	}

	/**
	 * Truncate.
	 * 
	 * @param maxSteps
	 *            the max steps
	 */
	public final void truncate(int maxSteps)
	{
		m_travelRoute.truncate(maxSteps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.MovementTask#cancel()
	 */
	@Override
	public void cancel()
	{
		m_travelRoute.unschedule();

		super.cancel();
	}

	/**
	 * The Interface IRouteTraveler.
	 */
	public interface IRouteTraveler extends ITraveler
	{

		/**
		 * Gets the allowed movements.
		 * 
		 * @return the allowed movements
		 */
		public WorldDirection[] getAllowedMovements();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.MovementTask#hasNext()
	 */
	@Override
	protected boolean hasNext()
	{
		return m_travelRoute.hasNext();
	}
}