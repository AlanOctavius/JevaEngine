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

package io.github.jevaengine.rpgbase.server.ui;

import io.github.jevaengine.game.ControlledCamera;
import io.github.jevaengine.joystick.InputManager.InputKeyEvent;
import io.github.jevaengine.joystick.InputManager.InputKeyEvent.EventType;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.MouseButton;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.Label;
import io.github.jevaengine.ui.MenuStrip;
import io.github.jevaengine.ui.MenuStrip.IMenuStripListener;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WorldView;
import io.github.jevaengine.ui.WorldView.IWorldViewListener;
import io.github.jevaengine.world.Actor;
import io.github.jevaengine.world.IInteractable;
import io.github.jevaengine.world.World;

import java.awt.Color;
import java.awt.event.KeyEvent;

public class WorldViewWindow extends Window
{
	private final ControlledCamera m_camera = new ControlledCamera();

	private final MenuStrip m_contextStrip = new MenuStrip();
	
	private final Label m_cursorActionLabel = new Label("None", Color.yellow);
	
	private WorldView m_worldView;
	
	private Vector2F m_cameraMovement = new Vector2F();
	
	public WorldViewWindow(UIStyle style, World world)
	{
		super(style, 400, 400);

		m_camera.setZoom(.5F);
		m_camera.attach(world);

		m_worldView = new WorldView(360, 370);
		m_worldView.setRenderBackground(false);
		m_worldView.setCamera(m_camera);
		m_worldView.addListener(new WorldViewListener());
		m_worldView.addControl(m_cursorActionLabel);

		this.addControl(m_worldView, new Vector2D(15, 15));

		this.addControl(new Button("Close") {

			@Override
			public void onButtonPress()
			{
				WorldViewWindow.this.remove();
			}
		}, new Vector2D(2, 2));

		m_cursorActionLabel.setVisible(false);
		
		this.addControl(m_contextStrip);
	}

	@Override
	public void update(int delta)
	{
		super.update(delta);

		if (!m_cameraMovement.isZero())
			m_camera.move(m_cameraMovement.normalize().multiply(0.3F));
	}

	@Override
	public void onMouseEvent(InputMouseEvent e)
	{
		if(e.deltaMouseWheel != 0)
		{
			e.isConsumed = true;
			if(e.deltaMouseWheel < 0)
				m_camera.setZoom(Math.min(1.3F, m_camera.getZoom() + 0.05F));
			else if(e.deltaMouseWheel > 0)
				m_camera.setZoom(Math.max(0.2F, m_camera.getZoom() - 0.05F));
		}
		super.onMouseEvent(e);
	}
	
	@Override
	public void onKeyEvent(InputKeyEvent e)
	{
		if (e.isConsumed)
			return;

		if (e.type == EventType.KeyUp)
			m_cameraMovement = new Vector2F();
		else
		{
			switch (e.keyCode)
			{
				case KeyEvent.VK_UP:
					e.isConsumed = true;
					m_cameraMovement.y = -1;
					break;
				case KeyEvent.VK_RIGHT:
					e.isConsumed = true;
					m_cameraMovement.x = 1;
					break;
				case KeyEvent.VK_DOWN:
					e.isConsumed = true;
					m_cameraMovement.y = 1;
					break;
				case KeyEvent.VK_LEFT:
					e.isConsumed = true;
					m_cameraMovement.x = -1;
					break;
			}
		}
	}

	private class WorldViewListener implements IWorldViewListener
	{
		private IInteractable m_lastTarget = null;
		
		@Override
		public void worldSelection(Vector2D location, Vector2F worldLocation, MouseButton button)
		{
			final IInteractable[] interactables = m_camera.getWorld().getTileEffects(worldLocation.round()).interactables.toArray(new IInteractable[0]);

			if (button == MouseButton.Right)
			{
				if (interactables.length > 0 && interactables[0].getCommands().length > 0)
				{
					m_contextStrip.setContext(interactables[0].getCommands(), new IMenuStripListener()
					{
						@Override
						public void onCommand(String command)
						{
							interactables[0].doCommand(command);
						}
					});

					m_contextStrip.setLocation(location);
				}
			}else if(button == MouseButton.Left)
			{
				if(m_lastTarget != null)
				{
					m_lastTarget.doCommand(m_lastTarget.getDefaultCommand());
					m_lastTarget = null;
					m_cursorActionLabel.setVisible(false);
				}
			}
		}

		@Override
		public void worldMove(Vector2D location, Vector2F worldLocation)
		{
			final Actor interactable = m_worldView.pick(Actor.class, location);

			if(interactable != null)
			{
				String defaultCommand = interactable.getDefaultCommand();
				
				if(defaultCommand != null)
				{
					m_cursorActionLabel.setText(defaultCommand);
					m_cursorActionLabel.setVisible(true);
					
					Vector2D offset = new Vector2D(10, 15);
					
					m_cursorActionLabel.setLocation(location.add(offset));
				
					m_lastTarget = interactable;
				}
			}else
			{
				m_lastTarget = null;
				m_cursorActionLabel.setVisible(false);
			}
		}

	}
}
