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
package io.github.jevaengine.rpgbase.server;

import io.github.jevaengine.Core;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.PolicyViolationException;
import io.github.jevaengine.communication.ShareEntityException;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.config.VariableStore;
import io.github.jevaengine.config.VariableValue;
import io.github.jevaengine.rpgbase.library.RpgEntityLibrary;
import io.github.jevaengine.rpgbase.netcommon.NetWorld;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SharedClass(name = "World", policy = SharePolicy.ClientR)
public class ServerWorld extends NetWorld implements IDisposable, IServerShared
{
	private static final int SYNC_INTERVAL = 200;

	private ArrayList<IServerEntity> m_serverEntities = new ArrayList<IServerEntity>();

	private String m_worldName;

	private World m_world;

	private int m_tickCount = 0;

	private ServerGame m_server;

	public ServerWorld(ServerGame server, String worldName)
	{
		m_server = server;

		m_worldName = worldName;

		m_world = World.create(new ServerEntityLibrary(), VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(worldName)));
	}

	@Override
	public void dispose()
	{
		m_world.dispose();
	}

	private void resyncEntity(IServerEntity entity)
	{
		try
		{
			entity.getSharedEntity().synchronize();
		} catch (InvalidMessageException e)
		{
			ServerCommunicator sender = (ServerCommunicator) e.getSender();

			if (sender.isConnected())
				sender.disconnect("Invalid message: " + e.toString());

			// Try again...
			resyncEntity(entity);
		}
	}

	@Override
	public void update(int deltaTime)
	{
		m_tickCount += deltaTime;

		m_world.update(deltaTime);

		for (IServerEntity entity : m_serverEntities)
		{
			resyncEntity(entity);
			entity.update(deltaTime);
		}

		if (m_tickCount >= SYNC_INTERVAL)
		{
			m_tickCount = 0;
			snapshot();
		}
	}

	@Override
	public SharedEntity getSharedEntity()
	{
		return this;
	}

	public String getName()
	{
		return m_worldName;
	}

	protected World getWorld()
	{
		return m_world;
	}

	protected synchronized void entityEnter(IServerEntity entity)
	{
		m_serverEntities.add(entity);

		try
		{
			share(entity.getSharedEntity());
		} catch (IOException | ShareEntityException | PolicyViolationException e)
		{
			// TODO: Log error, disconnect from server
			e.printStackTrace();
		}
	}

	protected synchronized void entityLeave(IServerEntity entity)
	{
		m_serverEntities.remove(entity);

		try
		{
			unshare(entity.getSharedEntity());
		} catch (IOException e)
		{
			// TODO: Log error, disconnect from server
			e.printStackTrace();
		}
	}

	@Override
	protected boolean onMessageRecieved(Communicator sender, Object recv)
	{
		try
		{
			if (recv instanceof PrimitiveQuery)
			{
				switch ((PrimitiveQuery) recv)
				{
					case Initialize:
						send(sender, new InitializationArguments(m_worldName));
						break;
					default:
						throw new InvalidMessageException(sender, recv, "Unrecognize message recieved from server");
				}
			} else if (recv instanceof IWorldVisitor)
			{
				IWorldVisitor visitor = (IWorldVisitor) recv;

				if (visitor.isServerOnly())
					throw new InvalidMessageException(sender, recv, "This visitor is server only.");

				visitor.visit(sender, m_world);
			} else
				throw new InvalidMessageException(sender, recv, "Unrecognize message recieved from server");

		} catch (InvalidMessageException e)
		{
			((ServerCommunicator) sender).disconnect(e.toString());
		}

		return true;
	}

	private class ServerEntityLibrary extends RpgEntityLibrary
	{
		@Override
		public Entity createEntity(String entityName, @Nullable String instanceName, List<VariableValue> arguments)
		{
			// Override RpgCharacter implementation with networked RPG Character
			if (entityName.compareTo("rpgCharacter") == 0)
				return new ServerRpgCharacter(instanceName, m_server, arguments).getControlledEntity();
			else
				return super.createEntity(entityName, instanceName, arguments);
		}
	}
}
