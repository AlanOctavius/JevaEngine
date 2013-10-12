package jeva.graphics.ui;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import proguard.annotation.KeepClassMemberNames;
import proguard.annotation.KeepName;
import jeva.Core;
import jeva.CoreScriptException;
import jeva.IResourceLibrary;
import jeva.Script;
import jeva.joystick.InputManager.InputKeyEvent;
import jeva.joystick.InputManager.InputKeyEvent.EventType;
import jeva.math.Vector2D;

/**
 * The Class CommandMenu.
 */
public final class CommandMenu extends Window
{

	/** The m_command out area. */
	private TextArea m_commandOutArea;

	/** The m_command in. */
	private TextArea m_commandIn;

	/** The m_script. */
	private Script m_script = new Script();

	/** The m_is ctrl down. */
	private boolean m_isCtrlDown = false;

	/**
	 * Instantiates a new command menu.
	 * 
	 * @param style
	 *            the style
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public CommandMenu(UIStyle style, int width, int height)
	{
		super(style, Math.max(100, width), Math.max(300, height));
		this.setVisible(false);

		Rectangle bounds = this.getBounds();

		m_commandOutArea = new TextArea(Color.green, (int) (bounds.width - 20), (int) (bounds.height - 210));
		m_commandOutArea.setLocation(new Vector2D(10, 10));
		m_commandOutArea.setText("Core Command Interface.\n");
		m_commandOutArea.setRenderBackground(false);
		this.addControl(m_commandOutArea);

		m_commandIn = new TextArea(Color.white, (int) (bounds.width - 40), 180);
		m_commandIn.setEditable(true);
		m_commandIn.setLocation(new Vector2D(10, bounds.height - 200));
		m_commandIn.setRenderBackground(false);
		this.addControl(m_commandIn);

		this.addControl(new Label(">", Color.red), new Vector2D(3, bounds.height - 200));

		try
		{
			m_script.setScript(new CommandMenuScriptContext());
		} catch (CoreScriptException e)
		{
			m_commandOutArea.appendText("Error Initializing scripting interface : " + e.toString() + "\n");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.graphics.ui.Panel#onKeyEvent(jeva.joystick.InputManager.InputKeyEvent
	 * )
	 */
	@Override
	public void onKeyEvent(InputKeyEvent e)
	{
		if (e.keyCode == KeyEvent.VK_CONTROL && e.type != EventType.KeyTyped)
		{
			m_isCtrlDown = true;
		} else if (m_isCtrlDown && e.type == EventType.KeyTyped)
		{
			m_isCtrlDown = false;
			if (Character.toLowerCase(e.keyChar) == 'l')
			{
				try
				{
					m_script.setScript(m_commandIn.getText(), new CommandMenuScriptContext());
					m_commandIn.setText("");
				} catch (CoreScriptException ex)
				{
					m_commandOutArea.appendText("Error occured while loading script: " + ex.toString() + "\n");
					m_commandOutArea.scrollToEnd();
				}
			} else if (Character.toLowerCase(e.keyChar) == 'e')
			{
				try
				{
					Object out = m_script.evaluate(m_commandIn.getText());

					if (out != null)
						m_commandOutArea.appendText("Evaluation: " + out.toString() + "\n");

					m_commandIn.setText("");
				} catch (Exception ex)
				{
					m_commandOutArea.appendText("Error occured while executing script: " + ex.toString() + "\n");
					m_commandOutArea.scrollToEnd();
				}
			}
		} else
			super.onKeyEvent(e);
	}

	/**
	 * The Class CommandMenuScriptContext.
	 */
	@KeepName
	@KeepClassMemberNames
	public class CommandMenuScriptContext
	{

		/**
		 * Echo.
		 * 
		 * @param s
		 *            the s
		 */
		public void echo(String s)
		{
			m_commandOutArea.appendText(s + "\n");
		}

		/**
		 * Execute.
		 * 
		 * @param path
		 *            the path
		 */
		public void execute(String path)
		{
			m_script.setScript(Core.getService(IResourceLibrary.class).openResourceContents(path), new CommandMenuScriptContext());
			m_commandIn.setText("");
		}

		/**
		 * Clear.
		 */
		public void clear()
		{
			m_commandOutArea.setText("");
		}
	}
}