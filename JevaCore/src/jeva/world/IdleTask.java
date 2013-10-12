package jeva.world;

/**
 * The Class IdleTask.
 */
public class IdleTask implements ITask
{

	/** The m_idle length. */
	private int m_idleLength;

	/** The m_idle time. */
	private int m_idleTime;

	/**
	 * Instantiates a new idle task.
	 * 
	 * @param idleTime
	 *            the idle time
	 */
	public IdleTask(int idleTime)
	{
		m_idleLength = idleTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#begin(jeva.world.Entity)
	 */
	@Override
	public void begin(Entity entity)
	{
		m_idleTime = m_idleLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#end()
	 */
	@Override
	public void end()
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#doCycle(int)
	 */
	@Override
	public boolean doCycle(int deltaTime)
	{
		m_idleTime -= deltaTime;

		return (m_idleTime <= 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#cancel()
	 */
	@Override
	public void cancel()
	{
		m_idleTime = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#isParallel()
	 */
	@Override
	public final boolean isParallel()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#ignoresPause()
	 */
	@Override
	public boolean ignoresPause()
	{
		return false;
	}

}