package jeva.graphics.ui;

import java.awt.Color;

import jeva.math.Vector2D;

/**
 * The Class DialogMenu.
 */
public class DialogMenu extends Window
{

	/**
	 * Instantiates a new dialog menu.
	 * 
	 * @param style
	 *            the style
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public DialogMenu(UIStyle style, int width, int height)
	{
		super(style, width, height);
		this.setVisible(false);
	}

	/**
	 * Issue query.
	 * 
	 * @param query
	 *            the query
	 * @param options
	 *            the options
	 * @param responder
	 *            the responder
	 */
	public void issueQuery(String query, String[] options, final IDialogResponder responder)
	{
		this.clearControls();

		TextArea textArea = new TextArea(query, Color.orange, this.getBounds().width - 20, this.getBounds().height - 60);
		textArea.setRenderBackground(false);
		textArea.setEffect(TextArea.DisplayEffect.Typewriter);

		textArea.setLocation(new Vector2D(10, 10));
		this.addControl(textArea);

		int xOffset = 10;
		for (final String option : options)
		{
			Button btn = new Button(option)
			{
				@Override
				public void onButtonPress()
				{
					DialogMenu.this.setVisible(false);
					responder.onAnswer(option);
				}
			};

			btn.setLocation(new Vector2D(xOffset, this.getBounds().height - 40));
			this.addControl(btn);

			xOffset += btn.getBounds().width + 15;
		}

		this.setVisible(true);
	}
}