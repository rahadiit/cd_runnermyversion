/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.view;

import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * A progress bar with a red/green indication for success or failure.
 */
// TODO: Fix description
// TODO: Refactor for C/C++ unit tests
public class ProgressBar extends Canvas {
	private static final int DEFAULT_WIDTH = 160;
	private static final int DEFAULT_HEIGHT = 18;

	private ITestingSession testingSession;
	private int colorBarWidth;
	private Color okColor;
	private Color failureColor;
	private Color stoppedColor;

	
	public ProgressBar(Composite parent, ITestingSession testingSession) {
		super(parent, SWT.NONE);

		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				updateInfoFromSession();
			}
		});
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				paint(e);
			}
		});

		// Manage progress bar colors
		Display display= parent.getDisplay();
		failureColor= new Color(display, 159, 63, 63);
		okColor= new Color(display, 95, 191, 95);
		stoppedColor= new Color(display, 120, 120, 120);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				failureColor.dispose();
				okColor.dispose();
				stoppedColor.dispose();
			}
		});
		setTestingSession(testingSession);
	}
	
	public void setTestingSession(ITestingSession testingSession) {
		this.testingSession = testingSession;
		updateInfoFromSession();
	}
	
	public void updateInfoFromSession() {
		recalculateColorBarWidth();
		redraw();
	}

	private void setStatusColor(GC gc) {
		if (testingSession.wasStopped())
			gc.setBackground(stoppedColor);
		else if (testingSession.hasErrors())
			gc.setBackground(failureColor);
		else
			gc.setBackground(okColor);
	}

	private void recalculateColorBarWidth() {
		Rectangle r = getClientArea();
		int newColorBarWidth;
		if (testingSession.getTotalCounter() > 0) {
			newColorBarWidth = testingSession.getCurrentCounter()*(r.width-2)/testingSession.getTotalCounter();
		} else {
			newColorBarWidth = testingSession.getCurrentCounter() == 0 ? 0 : (r.width-2)/2;
		}
		colorBarWidth = Math.max(0, newColorBarWidth);
	}

	private void drawBevelRect(GC gc, int x, int y, int w, int h, Color topleft, Color bottomright) {
		gc.setForeground(topleft);
		gc.drawLine(x, y, x+w-1, y);
		gc.drawLine(x, y, x, y+h-1);

		gc.setForeground(bottomright);
		gc.drawLine(x+w, y, x+w, y+h);
		gc.drawLine(x, y+h, x+w, y+h);
	}

	private void paint(PaintEvent event) {
		GC gc = event.gc;
		Display disp= getDisplay();

		Rectangle rect = getClientArea();
		gc.fillRectangle(rect);
		drawBevelRect(gc, rect.x, rect.y, rect.width-1, rect.height-1,
			disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW),
			disp.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));

		setStatusColor(gc);
		colorBarWidth = Math.min(rect.width-2, colorBarWidth);
		gc.fillRectangle(1, 1, colorBarWidth, rect.height-2);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		Point size= new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		if (wHint != SWT.DEFAULT) {
			size.x = wHint;
		}
		if (hHint != SWT.DEFAULT) {
			size.y = hHint;
		}
		return size;
	}
}
