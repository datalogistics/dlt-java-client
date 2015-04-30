/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.ui.utils.img;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.Timer;

public class RotatingIcon implements Icon
{
	private final Icon icon_original;
	private double angleInDegrees = 0;
	private final Timer rotatingTimer;

	public RotatingIcon(Icon icon, final JComponent component)
	{
		icon_original = icon;
		rotatingTimer = new Timer(100, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				angleInDegrees = angleInDegrees + 10;
				if (angleInDegrees == 360) {
					angleInDegrees = 0;
				}
				component.repaint();
			}
		});
		rotatingTimer.setRepeats(false);
		rotatingTimer.start();
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		rotatingTimer.stop();
		Graphics2D graphics = (Graphics2D) g.create();
		int width = icon_original.getIconWidth() / 2;
		int height = icon_original.getIconHeight() / 2;
		Rectangle r = new Rectangle(x, y, icon_original.getIconWidth(), icon_original.getIconHeight());
		graphics.setClip(r);
		AffineTransform original = graphics.getTransform();
		AffineTransform at = new AffineTransform();
		at.concatenate(original);
		at.rotate(Math.toRadians(angleInDegrees), x + width, y + height);
		graphics.setTransform(at);
		icon_original.paintIcon(c, graphics, x, y);
		graphics.setTransform(original);
		rotatingTimer.start();
	}

	@Override
	public int getIconWidth()
	{
		return icon_original.getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return icon_original.getIconHeight();
	}
}
