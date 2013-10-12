package jevarpg.net;

import com.sun.istack.internal.Nullable;

import jeva.communication.Communicator;
import jeva.communication.InvalidMessageException;
import jeva.communication.SharedEntity;
import jeva.config.Variable;
import jeva.world.DialogicalEntity;
import jeva.world.Entity;
import jeva.world.World;

public abstract class NetWorld extends SharedEntity
{
	protected static class InitializationArguments
	{
		private String m_varStore;

		@SuppressWarnings("unused")
		// Used by Kryo
		private InitializationArguments()
		{
		}

		public InitializationArguments(String varStore)
		{
			m_varStore = varStore;
		}

		public String getStore()
		{
			return m_varStore;
		}
	}

	protected static enum PrimitiveQuery
	{
		Initialize,
	}

	protected interface IWorldVisitor
	{
		void visit(Communicator sender, World world) throws InvalidMessageException;

		boolean isServerOnly();
	}

	protected static class DialogEvent implements IWorldVisitor
	{
		private String m_entityName;
		private String m_subjectName;
		private int m_eventCode;

		@SuppressWarnings("unused")
		// Used by Kryo
		private DialogEvent()
		{
		}

		public DialogEvent(String entity, int eventId, @Nullable String subjectName)
		{
			m_entityName = entity;
			m_subjectName = subjectName;
			m_eventCode = eventId;
		}

		public DialogEvent(String entity, int eventId)
		{
			this(entity, eventId, null);
		}

		@Override
		public void visit(Communicator sender, World world) throws InvalidMessageException
		{
			if (!world.variableExists(m_entityName))
				throw new InvalidMessageException(sender, this, "Entity name is invalid");

			Variable dialogEntity = world.getVariable(m_entityName);

			if (!(dialogEntity instanceof DialogicalEntity))
				throw new InvalidMessageException(sender, this, "Entity cannot posses dialog.");

			if (m_subjectName != null && (!world.variableExists(m_subjectName) || !(world.getVariable(m_subjectName) instanceof DialogicalEntity)))
				throw new InvalidMessageException(sender, this, "Invalid subject name or type.");

			((DialogicalEntity) dialogEntity).invokeDialogEvent(m_subjectName == null ? null : (Entity) world.getVariable(m_subjectName), m_eventCode);
		}

		@Override
		public boolean isServerOnly()
		{
			return false;
		}
	}
}