/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.rpgbase.demo.demos;


import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.game.ControlledCamera;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.MouseButton;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.demo.IState;
import io.github.jevaengine.rpgbase.demo.IStateContext;
import io.github.jevaengine.rpgbase.demo.MainMenu;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WorldView;
import io.github.jevaengine.ui.WorldView.IWorldViewListener;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.World.WorldConfiguration;

/**
 *
 * @author Jeremy
 */
public class Demo1 implements IState
{
	private static final String DEMO_MAP = "world/demo1.jmp";
	private static final String PLAYER = "artifact/entity/skeletonWarrior/player.jec";
	
	private IStateContext m_context;
	
	private World m_world;
	private Window m_window;
	private WorldView m_worldViewport;
	
	private RpgCharacter m_player;
	
	public Demo1(final UIStyle style)
	{
		ResourceLibrary resourceLibrary = Core.getService(ResourceLibrary.class);
		
		m_world = World.create(resourceLibrary.openConfiguration(DEMO_MAP).getValue(WorldConfiguration.class));

		m_player = new RpgCharacter(resourceLibrary.openConfiguration(PLAYER));
		m_player.setLocation(new Vector2F(4,4));
		m_world.addEntity(m_player);
		
		m_window = new Window(style, 420, 500);
		m_window.setLocation(new Vector2D(100, 100));
		
		ControlledCamera camera = new ControlledCamera(new Vector2F(2.5F, 2.5F));
		camera.attach(m_world);
		
		m_worldViewport = new WorldView(400, 400);
		m_worldViewport.setRenderBackground(false);
		m_worldViewport.setCamera(camera);
		m_worldViewport.addListener(new WorldViewListener());
		
		
		m_window.addControl(m_worldViewport, new Vector2D(10,70));
		
		m_window.addControl(new Button("Go Back")
		{
			@Override
			public void onButtonPress()
			{
				m_context.setState(new MainMenu(style));
			}
		}, new Vector2D(10,10));
	}
	
	public void enter(IStateContext context)
	{
		m_context = context;
		context.setPlayer(m_player);
		context.getWindowManager().addWindow(m_window);
	}

	public void leave()
	{
		m_context.setPlayer(null);
		m_context.getWindowManager().removeWindow(m_window);
	}

	public void update(int iDelta)
	{
		m_world.update(iDelta);
	}
	
	private class WorldViewListener implements IWorldViewListener
	{
		public void worldSelection(Vector2D location, Vector2F worldLocation, MouseButton button)
		{
			if(button != MouseButton.Left)
				return;
			
			m_player.moveTo(worldLocation);
		}

		public void worldMove(Vector2D location, Vector2F worldLocation)
		{
		}
	}
	
}
