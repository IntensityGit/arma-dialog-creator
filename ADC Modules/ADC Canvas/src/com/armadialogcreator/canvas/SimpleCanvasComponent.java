package com.armadialogcreator.canvas;


import com.armadialogcreator.util.ColorUtil;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 Default implementation of CanvasComponent

 @author Kayler
 @since 05/12/2016. */
public class SimpleCanvasComponent implements CanvasComponent {

	private static final Random rand = new Random();

	protected int x1, y1, x2, y2;
	protected int backgroundColorARGB = ColorUtil.darken(ColorUtil.opaqueARGB(rand.nextInt()));
	private Color backgroundColorAsColorObj = ColorUtil.toColor(backgroundColorARGB);
	private boolean updateBackgroundColorObj = false;

	protected Border border;
	private boolean isEnabled = true;
	private boolean isVisible = true;

	private int renderPriority = 0;

	public SimpleCanvasComponent(int x, int y, int width, int height) {
		this.x1 = x;
		this.y1 = y;
		this.x2 = x + width;
		this.y2 = y + height;
	}

	@Override
	public boolean isEnabled() {
		return this.isEnabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.isEnabled = enabled;
	}

	/**
	 Returns true if the component is invisible and is disabled, false otherwise
	 */
	@Override
	public boolean isGhost() {
		return !isVisible && !isEnabled;
	}


	/**
	 Sets the visibility and enable values. A ghost is not visible and is not enabled.
	 */
	@Override
	public void setGhost(boolean ghost) {
		this.isVisible = !ghost;
		setEnabled(!ghost);
	}

	/** Invokes {@link #paintBorder(Graphics)} and then paints the square with {@link #backgroundColorARGB} */
	@Override
	public void paint(@NotNull Graphics graphics) {
		paintBorder(graphics);
		graphics.setFill(backgroundColorARGB);
		System.out.println("SimpleCanvasComponent.paint ");
		graphics.fillRectangle(x1, y1, getWidth(), getHeight());
	}

	/** Will paint the {@link #border} if it isn't null. If the border is null, nothing will happen. */
	protected void paintBorder(@NotNull Graphics graphics) {
		if (border != null) {
			graphics.save();
			graphics.setStroke(ColorUtil.toARGB(border.getColor()));
			graphics.setLineWidth(border.getThickness());
			graphics.strokeRectangle(this);
			graphics.restore();
		}
	}

	@Override
	public void setBackgroundColor(@NotNull Color color) {
		this.backgroundColorARGB = ColorUtil.toARGB(color);
		this.backgroundColorAsColorObj = color;
		updateBackgroundColorObj = false;
	}

	@Override
	public void setBackgroundColor(int argb) {
		this.backgroundColorARGB = argb;
		updateBackgroundColorObj = true;
	}

	@Override
	@NotNull
	public Color getBackgroundColor() {
		if (updateBackgroundColorObj) {
			backgroundColorAsColorObj = ColorUtil.toColor(backgroundColorARGB);
			updateBackgroundColorObj = false;
		}
		return backgroundColorAsColorObj;
	}

	@Override
	public int getBackgroundColorARGB() {
		return backgroundColorARGB;
	}

	@Nullable
	public Border getBorder() {
		return border;
	}

	public void setBorder(@Nullable Border border) {
		this.border = border;
	}


	@Override
	public int getRenderPriority() {
		return renderPriority;
	}

	@Override
	public void setRenderPriority(int priority) {
		this.renderPriority = priority;
	}


	@Override
	public int getLeftX() {
		return Math.min(x1, x2);
	}

	@Override
	public int getRightX() {
		return Math.max(x1, x2);
	}

	@Override
	public int getTopY() {
		return Math.min(y1, y2);
	}

	@Override
	public int getBottomY() {
		return Math.max(y1, y2);
	}

	@Override
	public int getArea() {
		return getWidth() * getHeight();
	}

	@Override
	public int getX1() {
		return x1;
	}

	@Override
	public int getY1() {
		return y1;
	}

	@Override
	public int getX2() {
		return x2;
	}

	@Override
	public int getY2() {
		return y2;
	}

	@Override
	public int getWidth() {
		return Math.abs(x2 - x1);
	}

	@Override
	public int getHeight() {
		return Math.abs(y2 - y1);
	}

	/** Set the position equal to the given region */
	@Override
	public void setPosition(@NotNull Region r) {
		setPosition(r.getX1(), r.getY1(), r.getX2(), r.getY2());
	}

	/** Sets the position based on min x,y values and max x,y values */
	@Override
	public void setPosition(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	/**
	 Sets the position of the region based on the given top left corner with width and height values

	 @param x1 top left x coord
	 @param y1 top left y coord
	 @param width new width
	 @param height new height
	 */
	@Override
	public void setPositionWH(int x1, int y1, int width, int height) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x1 + width;
		this.y2 = y1 + height;
	}

	@Override
	public void setX1(int x1) {
		this.x1 = x1;
	}

	@Override
	public void setY1(int y1) {
		this.y1 = y1;
	}

	@Override
	public void setX2(int x2) {
		this.x2 = x2;
	}

	@Override
	public void setY2(int y2) {
		this.y2 = y2;
	}

	@Override
	public int getCenterX() {
		int left = getLeftX();
		return left + (getRightX() - left) / 2;
	}

	@Override
	public int getCenterY() {
		int top = getTopY();
		return top + (getBottomY() - top) / 2;
	}


	/**
	 Translate the region's x and y coordinates relative to the given point. Even if this region isn't allowed to move, this method will work.

	 @param dx change in x
	 @param dy change in y
	 */
	@Override
	public void translate(int dx, int dy) {
		this.x1 += dx;
		this.y1 += dy;
		this.x2 += dx;
		this.y2 += dy;
	}

	@Override
	public void scale(int dxl, int dxr, int dyt, int dyb) {
		this.x1 = getLeftX() + dxl;
		this.x2 = getRightX() + dxr;
		this.y1 = getTopY() + dyt;
		this.y2 = getBottomY() + dyb;
	}
}
