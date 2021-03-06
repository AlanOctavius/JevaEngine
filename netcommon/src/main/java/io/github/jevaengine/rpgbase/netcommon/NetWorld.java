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
package io.github.jevaengine.rpgbase.netcommon;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.world.World;

public abstract class NetWorld extends SharedEntity
{
	protected static final class InitializationArguments
	{
		private String m_worldName;

		@SuppressWarnings("unused")
		// Used by Kryo
		private InitializationArguments() { }

		public InitializationArguments(String worldName)
		{
			m_worldName = worldName;
		}

		public String getWorldName()
		{
			return m_worldName;
		}
	}

	protected static final class InitializeRequest
	{
		public InitializeRequest() {}
	}

	protected interface IWorldVisitor
	{
		void visit(Communicator sender, World world) throws InvalidMessageException;

		boolean isServerDispatchOnly();
	}
}
