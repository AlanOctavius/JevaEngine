/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.rpgbase.demo;

import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.TextArea;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.Window;

import java.awt.Color;

/**
 *
 * @author Jeremy
 */
public class Credits implements IState
{
	private IStateContext m_context;
	private Window m_credits;
	
	public Credits(final UIStyle style)
	{
		m_credits = new Window(style, 800, 500);
		m_credits.setRenderBackground(false);
		
		m_credits.setLocation(new Vector2D(100,100));
		m_credits.setMovable(false);
		
		m_credits.addControl(new Button("Main Menu")
		{
			@Override
			public void onButtonPress()
			{
				m_context.setState(new MainMenu(style));
			}
		}, new Vector2D(0, 00));
		
		TextArea textArea = new TextArea(Color.white, 800, 400);
		textArea.setRenderBackground(false);
		textArea.setEffect(TextArea.DisplayEffect.Typewriter);
		textArea.setText("Programming: Jeremy. Allen. Wildsmith\n\n" + 
						 "Graphics (OpenGameArt.org):\n" +
						 "Skeleton Warrior - Clint Bellanger\n" +
						 "Castle Tile Set - Seth Galbraith\n"+
						 "Nature Tile Set - Yar");
		
		m_credits.addControl(textArea, new Vector2D(0,100));
	}
	
	public void enter(IStateContext context)
	{
		m_context = context;
		context.getWindowManager().addWindow(m_credits);
	}

	public void leave()
	{
		m_context.getWindowManager().removeWindow(m_credits);
	}

	public void update(int iDelta)
	{
		
	}
	
}
