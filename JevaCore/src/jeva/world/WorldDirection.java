/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.world;

import jeva.math.Vector2D;
import jeva.math.Vector2F;

/**
 * The Enum WorldDirection.
 * 
 * @author Scott
 */
public enum WorldDirection
{

	/** The X plus. */
	XPlus(new Vector2D(1, 0), false), /** The Y plus. */
	YPlus(new Vector2D(0, 1), false), /** The X minus. */
	XMinus(new Vector2D(-1, 0), false), /** The Y minus. */
	YMinus(new Vector2D(0, -1), false), /** The XY plus. */
	XYPlus(new Vector2D(1, 1), true), /** The XY minus. */
	XYMinus(new Vector2D(-1, -1), true), /** The XY plus minus. */
	XYPlusMinus(new Vector2D(1, -1), true), /** The XY minus plus. */
	XYMinusPlus(new Vector2D(-1, 1), true), /** The Zero. */
	Zero(new Vector2D(0, 0), false);

	/** The Constant HV_DIRECTIONS. */
	public static final WorldDirection[] HV_DIRECTIONS =
	{ XPlus, YPlus, XMinus, YMinus };

	/** The Constant DIAGONAL_DIRECTIONS. */
	public static final WorldDirection[] DIAGONAL_DIRECTIONS =
	{ XYPlus, XYMinus, XYPlusMinus, XYMinusPlus };

	/** The Constant ALL_MOVEMENT. */
	public static final WorldDirection[] ALL_MOVEMENT =
	{ XPlus, YPlus, XMinus, YMinus, XYPlus, XYMinus, XYPlusMinus, XYMinusPlus };

	/** The m_movement vector. */
	private Vector2D m_movementVector;

	/** The m_is diagonal. */
	private boolean m_isDiagonal;

	/**
	 * Instantiates a new world direction.
	 * 
	 * @param movementVector
	 *            the movement vector
	 * @param isDiagonal
	 *            the is diagonal
	 */
	WorldDirection(Vector2D movementVector, boolean isDiagonal)
	{
		m_movementVector = movementVector;
		m_isDiagonal = isDiagonal;
	}

	/**
	 * Checks if is diagonal.
	 * 
	 * @return true, if is diagonal
	 */
	public boolean isDiagonal()
	{
		return m_isDiagonal;
	}

	/**
	 * From vector.
	 * 
	 * @param vec
	 *            the vec
	 * @return the world direction
	 */
	public static WorldDirection fromVector(Vector2F vec)
	{
		if (vec.isZero())
			return WorldDirection.Zero;

		Vector2F dir = vec.normalize();
		float fAngle = (float) Math.atan(Math.abs(dir.y) / Math.abs(dir.x));

		if (fAngle < Math.PI / 4 - Math.PI / 5.5 || fAngle > Math.PI / 4 + Math.PI / 5.5)
		{
			if (dir.x > Vector2F.TOLERANCE && dir.y > Vector2F.TOLERANCE)
				return dir.x > dir.y ? XPlus : YPlus;
			else if (dir.x < -Vector2F.TOLERANCE && dir.y < -Vector2F.TOLERANCE)
				return (-dir.x > -dir.y ? XMinus : YMinus);
			else if (dir.y > Vector2F.TOLERANCE && dir.x < -Vector2F.TOLERANCE)
				return (dir.y > -dir.x ? YPlus : XMinus);
			else if (dir.y < -Vector2F.TOLERANCE && dir.x > Vector2F.TOLERANCE)
				return (-dir.y > dir.x ? YMinus : XPlus);
			else if (Math.abs(dir.x) >= Vector2F.TOLERANCE && Math.abs(dir.y) <= Vector2F.TOLERANCE)
				return (dir.x < 0 ? XMinus : XPlus);
			else if (Math.abs(dir.x) <= Vector2F.TOLERANCE && Math.abs(dir.y) >= Vector2F.TOLERANCE)
				return (dir.y > 0 ? YPlus : YMinus);
		} else
		{
			if (vec.x > Vector2F.TOLERANCE && vec.y > Vector2F.TOLERANCE)
				return XYPlus;
			else if (vec.x > Vector2F.TOLERANCE && vec.y < -Vector2F.TOLERANCE)
				return XYPlusMinus;
			else if (vec.x < -Vector2F.TOLERANCE && vec.y > Vector2F.TOLERANCE)
				return XYMinusPlus;
			else if (vec.x < -Vector2F.TOLERANCE && vec.y < -Vector2F.TOLERANCE)
				return XYMinus;
		}

		return WorldDirection.Zero;
	}

	/**
	 * Gets the direction vector.
	 * 
	 * @return the direction vector
	 */
	public Vector2D getDirectionVector()
	{
		return m_movementVector;
	}

	/**
	 * Gets the angle.
	 * 
	 * @return the angle
	 */
	public float getAngle()
	{
		switch (this)
		{
		case XMinus:
			return (float) Math.PI;
		case XPlus:
			return 0;
		case XYMinus:
			return 3 * (float) Math.PI / 4.0F;
		case XYMinusPlus:
			return 5 * (float) Math.PI / 4.0F;
		case XYPlus:
			return 7 * (float) Math.PI / 4.0F;
		case XYPlusMinus:
			return (float) Math.PI / 4.0F;
		case YMinus:
			return (float) Math.PI / 2.0F;
		case YPlus:
			return 3 * (float) Math.PI / 2.0F;
		case Zero:
			return 0;
		default:
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	public String toString()
	{
		switch (this)
		{
		case XMinus:
			return "w";
		case XPlus:
			return "e";
		case XYMinus:
			return "nw";
		case XYMinusPlus:
			return "sw";
		case XYPlus:
			return "se";
		case XYPlusMinus:
			return "ne";
		case YMinus:
			return "n";
		case YPlus:
			return "s";
		case Zero:
			return "z";
		default:
			throw new RuntimeException("Unknown world direction");
		}
	}
}