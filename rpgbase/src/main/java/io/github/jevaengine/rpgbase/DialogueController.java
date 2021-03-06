/**
 * *****************************************************************************
 * Copyright (c) 2013 Jeremy. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the GNU Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
* If you'd like to obtain a another license to this code, you may contact
 * Jeremy to discuss alternative redistribution options.
 * 
* Contributors: Jeremy - initial API and implementation
*****************************************************************************
 */
package io.github.jevaengine.rpgbase;

import io.github.jevaengine.Script;
import io.github.jevaengine.rpgbase.DialoguePath.Answer;
import io.github.jevaengine.rpgbase.DialoguePath.Query;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.Entity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public final class DialogueController
{
	private Queue<DialoguePathEntry> m_dialogueQueue = new LinkedList<DialoguePathEntry>();
	
	private DialoguePathEntry m_currentDialogue;
	private Answer[] m_currentAnswers = new Answer[0];
	
	private boolean m_isBusy = false;
	
	private Observers m_observers = new Observers();
	
	protected DialogueController()
	{
		
	}
	
	private Script createConditionTestEnvironment(DialoguePathEntry entry)
	{
		Script workingScript = new Script();
		
		workingScript.putConst("speaker", entry.speaker == null ? null : entry.speaker.getScriptBridge());
		workingScript.putConst("listener", entry.listener == null ? null : entry.listener.getScriptBridge());
		
		return workingScript;
	}
	
	private int findInitialQuery(DialoguePathEntry entry)
	{
		Script workingScript = createConditionTestEnvironment(entry);
		
		workingScript.putConst("speaker", entry.speaker == null ? null : entry.speaker.getScriptBridge());
		workingScript.putConst("listener", entry.listener == null ? null : entry.listener.getScriptBridge());
		
		Query queries[] = entry.path.queries;
		
		for(int i = 0; i < queries.length; i++)
		{
			if(queries[i].entryCondition != null)
			{
				Object o = workingScript.evaluate(queries[i].entryCondition);
				
				if(o instanceof Boolean && ((Boolean)o).booleanValue())
					return i;
			}
		}
		
		throw new NoSuchElementException();
	}

	private void beginDialogue(DialoguePathEntry entry)
	{
		m_isBusy = true;
		m_currentDialogue = entry;
		inquire(entry.path.queries[findInitialQuery(entry)]);
		m_observers.beginDialogue();
	}
	
	private void endDialogue()
	{
		m_isBusy = false;
		m_currentDialogue = null;
		m_currentAnswers = new Answer[0];
		m_observers.endDialogue();
	}
	
	private void inquire(Query query)
	{
		Script workingScript = createConditionTestEnvironment(m_currentDialogue);
		ArrayList<Answer> availibleAnswers = new ArrayList<Answer>();
		
		for(Answer a : query.answers)
		{
			if(a.condition != null)
			{
				Object o = workingScript.evaluate(a.condition);

				if(o instanceof Boolean && !((Boolean)o).booleanValue())
					continue;
			}
			
			availibleAnswers.add(a);
		}
		
		m_currentAnswers = availibleAnswers.toArray(new Answer[availibleAnswers.size()]);
		
		m_observers.speakerInquired(query.query);
	}
	
	public void addObserver(IDialogueControlObserver observer)
	{
		m_observers.add(observer);
	}
	
	public void removeObserver(IDialogueControlObserver observer)
	{
		m_observers.remove(observer);
	}
	
	public void enqueueDialogue(@Nullable Entity speaker, @Nullable Entity listener, DialoguePath path)
	{
		m_dialogueQueue.add(new DialoguePathEntry(speaker, listener, path));
	}
	
	public boolean say(String message)
	{
		m_observers.listenerSaid(message);
		
		if(!m_isBusy)
			return false;
	
		Answer answer = null;
			
		for(Answer a : m_currentAnswers)
		{
			if(a.answer.equals(message))
				answer = a;
		}
		
		if(answer == null)
			return false;
		
		int next = answer.next;
					
		if(answer.event >= 0)
			m_observers.dialogueEvent(answer.event);
		
		if(next >= 0)
			inquire(m_currentDialogue.path.queries[next]);
		else
			endDialogue();
		
		return true;
	}
	
	void update(int deltaTime)
	{
		if(!m_isBusy && !m_dialogueQueue.isEmpty())
			beginDialogue(m_dialogueQueue.remove());
	}
	
	public boolean isBusy()
	{
		return m_isBusy;
	}

	@Nullable
	public Entity getSpeaker()
	{
		return m_currentDialogue == null ? null : m_currentDialogue.speaker;
	}
	
	@Nullable
	public Entity getListener()
	{
		return m_currentDialogue == null ? null : m_currentDialogue.listener;
	}
	
	public String[] getAnswers()
	{
		if(!m_isBusy)
			return new String[] {};
		
		String[] strAnswers = new String[m_currentAnswers.length];
		
		for(int i = 0; i < m_currentAnswers.length; i++)
			strAnswers[i] = m_currentAnswers[i].answer;
		
		return strAnswers;
	}

	public void cancelCurrent()
	{
		if(m_isBusy)
			endDialogue();
	}
	
	public void clear()
	{
		cancelCurrent();
		m_dialogueQueue.clear();
	}
	
	public interface IDialogueControlObserver
	{
		void beginDialogue();
		void endDialogue();
		void dialogueEvent(int event);
		void speakerSaid(String message);
		void listenerSaid(String message);
	}
	
	private class Observers extends StaticSet<IDialogueControlObserver>
	{
		
		void beginDialogue()
		{
			for(IDialogueControlObserver o : this)
				o.beginDialogue();
		}
		
		void endDialogue()
		{
			for(IDialogueControlObserver o : this)
				o.endDialogue();
		}
				
		void dialogueEvent(int event)
		{
			for(IDialogueControlObserver o : this)
				o.dialogueEvent(event);
		}
		
		void speakerInquired(String message)
		{
			for(IDialogueControlObserver o : this)
				o.speakerSaid(message);
		}
		
		void listenerSaid(String message)
		{
			for(IDialogueControlObserver o : this)
				o.listenerSaid(message);
		}
	}
	
	private class DialoguePathEntry
	{
		public @Nullable Entity speaker;
		public @Nullable Entity listener;
		public DialoguePath path;
		
		public DialoguePathEntry(Entity _speaker, Entity _listener, DialoguePath _path)
		{
			speaker = _speaker;
			listener = _listener;
			path = _path;
		}
	}
}
