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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.jevaengine.ui;

import java.awt.Color;
import java.awt.Graphics2D;

import io.github.jevaengine.joystick.InputManager;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.MouseButton;

public abstract class Button extends Label
{

	private static final Color CURSOR_OVER_COLOR = new Color(255, 100, 100);

	private static final Color CURSOR_OFF_COLOR = new Color(150, 150, 150);

	public Button(String text)
	{
		super(text, CURSOR_OFF_COLOR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.graphics.ui.Control#onEnter()
	 */
	@Override
	protected void onEnter()
	{
		setColor(CURSOR_OVER_COLOR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.graphics.ui.Control#onLeave()
	 */
	@Override
	protected void onLeave()
	{
		setColor(CURSOR_OFF_COLOR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.graphics.ui.Label#onMouseEvent(jeva.joystick.InputManager.
	 * InputMouseEvent)
	 */
	@Override
	public void onMouseEvent(InputMouseEvent mouseEvent)
	{
		if (!mouseEvent.mouseButtonState &&
			mouseEvent.mouseButton == MouseButton.Left &&
			mouseEvent.type == InputMouseEvent.EventType.MouseClicked)
		{
			getStyle().getPressButtonAudio().play();
			onButtonPress();
		}

		super.onMouseEvent(mouseEvent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.github.jeremywildsmith.jevaengine.graphics.ui.Label#onKeyEvent(jeva.joystick.InputManager.InputKeyEvent
	 * )
	 */
	@Override
	public void onKeyEvent(InputManager.InputKeyEvent keyEvent)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Label#render(java.awt.Graphics2D, int, int, float)
	 */
	@Override
	public void render(Graphics2D g, int x, int y, float fScale)
	{
		super.render(g, x, y, fScale);
	}

	public abstract void onButtonPress();
}
