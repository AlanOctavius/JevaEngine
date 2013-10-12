package jeva.graphics;

import java.awt.Graphics2D;

import jeva.Core;
import jeva.IResourceLibrary;
import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.math.Vector2D;
import jeva.math.Vector2F;

/**
 * The Class ParticleEmitter.
 */
public final class ParticleEmitter implements IRenderable
{

	/**
	 * The Enum ParticleOffset.
	 */
	private enum ParticleOffset
	{
		/** The Location x. */
		LocationX(0),
		/** The Location y. */
		LocationY(1),
		/** The Velocity x. */
		VelocityX(2),
		/** The Velocity y. */
		VelocityY(3),
		/** The Acceleration x. */
		AccelerationX(4),
		/** The Acceleration y. */
		AccelerationY(5),
		/** The Life. */
		Life(6),
		/** The Size. */
		Size(7);

		/** The offset. */
		public int offset;

		/**
		 * Instantiates a new particle offset.
		 * 
		 * @param _offset
		 *            the _offset
		 */
		ParticleOffset(int _offset)
		{
			offset = _offset;
		}
	}

	/** The Constant MAX_SPRITES. */
	private static final int MAX_SPRITES = 25;

	/** The m_sprite maps. */
	private Sprite[] m_spriteMaps;

	/** The m_particle sprites. */
	private Sprite[] m_particleSprites;

	/** The m_acceleration. */
	private Vector2F m_acceleration;

	/** The m_particle count. */
	private int m_particleCount;

	/** The m_particle life. */
	private int m_particleLife;

	/** The m_f variation. */
	private float m_fVariation;

	/** The m_velocity. */
	private Vector2F m_velocity;

	/** The m_particle buffer. */
	private float[] m_particleBuffer;

	/** The m_is emitting. */
	private boolean m_isEmitting;

	/** The m_anchor. */
	private Vector2D m_anchor;

	/**
	 * Instantiates a new particle emitter.
	 * 
	 * @param anchor
	 *            the anchor
	 * @param spriteMaps
	 *            the sprite maps
	 * @param acceleration
	 *            the acceleration
	 * @param velocity
	 *            the velocity
	 * @param particleCount
	 *            the particle count
	 * @param particleLife
	 *            the particle life
	 * @param fVariation
	 *            the f variation
	 */
	public ParticleEmitter(Vector2D anchor, Sprite[] spriteMaps, Vector2F acceleration, Vector2F velocity, int particleCount, int particleLife, float fVariation)
	{
		m_anchor = anchor;

		m_spriteMaps = spriteMaps;
		m_acceleration = acceleration;
		m_velocity = velocity;
		m_particleCount = particleCount;
		m_particleLife = particleLife;
		m_fVariation = fVariation;
		m_particleBuffer = new float[m_particleCount * ParticleOffset.Size.ordinal()];
		m_particleSprites = new Sprite[MAX_SPRITES];

		m_isEmitting = false;
	}

	/**
	 * Creates the.
	 * 
	 * @param root
	 *            the root
	 * @param anchor
	 *            the anchor
	 * @return the particle emitter
	 */
	public static ParticleEmitter create(Variable root, Vector2D anchor)
	{
		int particleCount = Math.max(10, root.getVariable("particleCount").getValue().getInt());
		int particleLife = Math.max(10, root.getVariable("particleLife").getValue().getInt());

		Vector2F velocity = new Vector2F(root.getVariable("velocity/x").getValue().getFloat(), root.getVariable("velocity/y").getValue().getFloat());

		Vector2F acceleration = new Vector2F(root.getVariable("accleration/x").getValue().getFloat(), root.getVariable("accleration/y").getValue().getFloat());

		float fVariation = root.getVariable("variation").getValue().getFloat();

		Variable[] maps = root.getVariable("sprites").getVariableArray();
		Sprite[] spriteMaps = new Sprite[maps.length];

		for (int i = 0; i < maps.length; i++)
		{
			spriteMaps[i] = Sprite.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(maps[i].getValue().getString())));
		}

		return new ParticleEmitter(anchor, spriteMaps, acceleration, velocity, particleCount, particleLife, fVariation);
	}

	/**
	 * Update.
	 * 
	 * @param deltaTime
	 *            the delta time
	 */
	public void update(int deltaTime)
	{
		for (int i = 0; i < m_particleCount; i++)
		{
			int particle = (i % m_particleCount);
			int base = particle * ParticleOffset.Size.offset;

			if (m_particleBuffer[base + ParticleOffset.Life.offset] <= 0 && m_isEmitting)
			{
				m_particleBuffer[base + ParticleOffset.Life.offset] = m_particleLife * (float) Math.random();

				m_particleBuffer[base + ParticleOffset.LocationX.offset] = 0;
				m_particleBuffer[base + ParticleOffset.LocationY.offset] = 0;

				Vector2F velocity = m_velocity.multiply(1.0F + m_fVariation * (Math.random() > 0.5 ? -1 : 1)).rotate(m_fVariation * (float) Math.random() * (float) Math.PI * 2);

				m_particleBuffer[base + ParticleOffset.VelocityX.offset] = velocity.x;
				m_particleBuffer[base + ParticleOffset.VelocityY.offset] = velocity.y;

				Vector2F acceleration = m_acceleration.multiply(1.0F + m_fVariation * (Math.random() > 0.5 ? -1 : 1)).rotate(m_fVariation * (float) Math.random() * (float) Math.PI * 2);

				m_particleBuffer[base + ParticleOffset.AccelerationX.offset] = acceleration.x;
				m_particleBuffer[base + ParticleOffset.AccelerationY.offset] = acceleration.y;

				if (m_particleSprites[particle % m_particleSprites.length] == null)
				{
					m_particleSprites[particle % m_particleSprites.length] = new Sprite(m_spriteMaps[((int) (Math.random() * 100) % m_spriteMaps.length)]);
					m_particleSprites[particle % m_particleSprites.length].setAnimation("idle", AnimationState.PlayToEnd);
				}
			}

			m_particleBuffer[base + ParticleOffset.LocationX.offset] += m_particleBuffer[base + ParticleOffset.VelocityX.offset] * (float) deltaTime / 1000.0F;
			m_particleBuffer[base + ParticleOffset.LocationY.offset] += m_particleBuffer[base + ParticleOffset.VelocityY.offset] * (float) deltaTime / 1000.0F;

			m_particleBuffer[base + ParticleOffset.VelocityX.offset] += m_particleBuffer[base + ParticleOffset.AccelerationX.offset] * (float) deltaTime / 1000.0F;
			m_particleBuffer[base + ParticleOffset.VelocityY.offset] += m_particleBuffer[base + ParticleOffset.AccelerationY.offset] * (float) deltaTime / 1000.0F;

			m_particleBuffer[base + ParticleOffset.Life.offset] -= deltaTime;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.IRenderable#render(java.awt.Graphics2D, int, int,
	 * float)
	 */
	@Override
	public void render(Graphics2D g, int x, int y, float fScale)
	{
		for (int i = 0; i < m_particleCount; i++)
		{
			int base = i * ParticleOffset.Size.offset;

			if (m_particleSprites[i % m_particleSprites.length] != null && m_particleBuffer[base + ParticleOffset.Life.offset] > 0)
				m_particleSprites[i % m_particleSprites.length].render(g, x - m_anchor.x + Math.round(m_particleBuffer[base + ParticleOffset.LocationX.offset]), y - m_anchor.y + Math.round(m_particleBuffer[base + ParticleOffset.LocationY.offset]), fScale * (float) m_particleBuffer[base + ParticleOffset.Life.offset] / (float) m_particleLife);
		}
	}

	/**
	 * Sets the emit.
	 * 
	 * @param emit
	 *            the new emit
	 */
	public void setEmit(boolean emit)
	{
		m_isEmitting = emit;
	}
}