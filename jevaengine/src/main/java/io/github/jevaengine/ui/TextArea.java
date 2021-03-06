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
import java.util.ArrayList;
import java.util.Arrays;

import io.github.jevaengine.graphics.Font;
import io.github.jevaengine.joystick.InputManager.InputKeyEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.EventType;
import io.github.jevaengine.math.Rect2D;

public class TextArea extends Panel
{

	private static final int TYPE_LENGTH = 25;

	public enum DisplayEffect
	{

		Typewriter,

		None
	}

	private String m_text;

	private String m_renderText;

	private Font m_font;

	private float m_fScroll;

	private Color m_color;

	private int m_elapsedTime;

	private DisplayEffect m_displayEffect;

	private boolean m_allowEdit;

	public TextArea(String text, Color color, int width, int height)
	{
		super(width, height);
		m_allowEdit = false;
		m_color = color;
		m_text = text;
		m_renderText = m_text;
		m_displayEffect = DisplayEffect.None;
	}

	public TextArea(Color color, int width, int height)
	{
		this("", color, width, height);
	}

	public String getText()
	{
		return m_text;
	}

	public void setEffect(DisplayEffect effect)
	{
		m_displayEffect = effect;

		if (effect == DisplayEffect.None)
			m_renderText = m_text;
		else if (effect == DisplayEffect.Typewriter)
			m_renderText = "";
	}

	public void setText(String text)
	{
		m_text = text;
		m_fScroll = 0;
		setEffect(m_displayEffect);
	}

	public void appendText(String text)
	{
		m_text = getText() + text;
		setEffect(m_displayEffect);
	}

	public void setEditable(boolean isEditable)
	{
		m_allowEdit = isEditable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.graphics.ui.Panel#render(java.awt.Graphics2D, int, int, float)
	 */
	@Override
	public void render(Graphics2D g, int x, int y, float fScale)
	{
		super.render(g, x, y, fScale);

		String[] text = m_renderText.split("(?<=[ \n])");

		ArrayList<ArrayList<Rect2D>> lines = new ArrayList<ArrayList<Rect2D>>();
		lines.add(new ArrayList<Rect2D>());

		int offsetX = 0;

		for (String s : text)
		{
			Rect2D[] strMap = m_font.getString(s);

			int wordWidth = 0;

			for (Rect2D r : strMap)
				wordWidth += r.width;

			if (offsetX + wordWidth >= this.getBounds().width && offsetX != 0)
			{
				lines.add(new ArrayList<Rect2D>());
				offsetX = 0;
			}

			lines.get(lines.size() - 1).addAll(Arrays.asList(strMap));
			offsetX += wordWidth;

			if (s.endsWith("\n"))
			{
				lines.add(new ArrayList<Rect2D>());
				offsetX = 0;
			}
		}

		int offsetY = 0;

		int minScroll = Math.max(0, lines.size() - 1 - getBounds().height / (m_font.getHeight() + 5));

		m_fScroll = Math.min(minScroll, Math.max(m_fScroll, 0));

		for (ArrayList<Rect2D> line : lines.subList((int) m_fScroll, lines.size()))
		{
			offsetX = 0;
			
			for (Rect2D lineChar : line)
			{
				if (offsetY < this.getBounds().height - m_font.getHeight())
					m_font.getSource().render(g, x + offsetX, y + offsetY, lineChar.width, lineChar.height, lineChar.x, lineChar.y, lineChar.width, lineChar.height);

				offsetX += lineChar.width;
			}

			offsetY += m_font.getHeight() + 5;
		}
	}

	public void scrollToEnd()
	{
		m_fScroll = Float.MAX_VALUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.graphics.ui.Panel#onStyleChanged()
	 */
	@Override
	public void onStyleChanged()
	{
		super.onStyleChanged();

		if (getStyle() != null)
			m_font = getStyle().getFont(m_color);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.graphics.ui.Panel#onMouseEvent(jeva.joystick.InputManager.
	 * InputMouseEvent)
	 */
	@Override
	public void onMouseEvent(InputMouseEvent mouseEvent)
	{
		if (mouseEvent.type == EventType.MouseWheelMoved)
		{
			m_fScroll = Math.max(0.0F, m_fScroll + Math.signum(mouseEvent.deltaMouseWheel));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.github.jeremywildsmith.jevaengine.graphics.ui.Panel#onKeyEvent(jeva.joystick.InputManager.InputKeyEvent
	 * )
	 */
	@Override
	public void onKeyEvent(InputKeyEvent keyEvent)
	{
		if (!m_allowEdit || keyEvent.isConsumed)
			return;

		if (keyEvent.type == InputKeyEvent.EventType.KeyTyped)
		{
			if (keyEvent.keyChar == '\b')
			{
				keyEvent.isConsumed = true;
				setText(m_text.substring(0, Math.max(0, m_text.length() - 1)));
			} else if (keyEvent.keyChar == '\n')
			{
				keyEvent.isConsumed = true;
				setText(m_text + keyEvent.keyChar);
			} else if (m_font.mappingExists(keyEvent.keyChar))
			{
				keyEvent.isConsumed = true;
				setText(m_text + keyEvent.keyChar);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.graphics.ui.Panel#update(int)
	 */
	@Override
	public void update(int deltaTime)
	{
		if (m_displayEffect == DisplayEffect.Typewriter)
		{
			m_elapsedTime += deltaTime;
			for (; m_elapsedTime > TYPE_LENGTH && m_renderText.length() < m_text.length(); m_elapsedTime -= TYPE_LENGTH)
			{
				m_renderText += m_text.charAt(m_renderText.length());
			}
		}
	}
}
