package com.armadialogcreator.control.impl;

import com.armadialogcreator.canvas.CanvasContext;
import com.armadialogcreator.canvas.Region;
import com.armadialogcreator.control.ArmaControl;
import com.armadialogcreator.control.ArmaControlRenderer;
import com.armadialogcreator.control.ArmaResolution;
import com.armadialogcreator.control.impl.utility.*;
import com.armadialogcreator.core.ConfigClass;
import com.armadialogcreator.core.ConfigProperty;
import com.armadialogcreator.core.ConfigPropertyLookup;
import com.armadialogcreator.core.sv.SVColor;
import com.armadialogcreator.core.sv.SVColorArray;
import com.armadialogcreator.core.sv.SVFont;
import com.armadialogcreator.core.sv.SVNumericValue;
import com.armadialogcreator.expression.Env;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 A renderer for {@link ComboControl}

 @author Kayler
 @since 07/23/2017 */
public class ListboxRenderer extends ArmaControlRenderer implements BasicTextRenderer.UpdateCallback {

	private BlinkControlHandler blinkControlHandler;
	private BasicTextRenderer textRenderer;
	private TooltipRenderer tooltipRenderer;

	private double rowHeight = 0;
	private double period = 0;
	private Color colorSelect = Color.RED;
	private Color colorSelect2 = null;
	private Color colorDisabled = Color.BLACK;
	private Color colorSelectBackground = null;
	private Color colorSelectBackground2 = null;
	private final ScrollbarRenderer scrollbarRenderer;
	private final AlternatorHelper periodAlternator = new AlternatorHelper(0);
	private int selectedRow = 0;

	private final Function<GraphicsContext, Void> tooltipRenderFunc = gc -> {
		tooltipRenderer.paint(gc, this.mouseOverX, this.mouseOverY);
		return null;
	};


	public ListboxRenderer(ArmaControl control, ArmaResolution resolution, Env env) {
		super(control, resolution, env);
		textRenderer = new BasicTextRenderer(control, this,
				null, ConfigPropertyLookup.COLOR_TEXT,
				ConfigPropertyLookup.STYLE, ConfigPropertyLookup.SIZE_EX,
				ConfigPropertyLookup.SHADOW, true, this
		);
		textRenderer.setText("Placeholder");

		ConfigProperty colorBackground = myControl.findProperty(ConfigPropertyLookup.COLOR_BACKGROUND);
		{
			addValueListener(colorBackground.getName(), (observer, oldValue, newValue) -> {
				if (newValue instanceof SVColor) {
					getBackgroundColorObserver().updateValue((SVColor) newValue);
				}
			});
			colorBackground.setValue(new SVColorArray(getBackgroundColor()));

		}

		myControl.findProperty(ConfigPropertyLookup.COLOR_TEXT).setValue(new SVColorArray(getTextColor()));
		myControl.findProperty(ConfigPropertyLookup.FONT).setValue(SVFont.DEFAULT);

		blinkControlHandler = new BlinkControlHandler(this, ConfigPropertyLookup.BLINKING_PERIOD);

		tooltipRenderer = new TooltipRenderer(
				this.myControl, this,
				ConfigPropertyLookup.TOOLTIP_COLOR_SHADE,
				ConfigPropertyLookup.TOOLTIP_COLOR_TEXT,
				ConfigPropertyLookup.TOOLTIP_COLOR_BOX,
				ConfigPropertyLookup.TOOLTIP
		);

		addValueListener(ConfigPropertyLookup.COLOR_SELECT, (observer, oldValue, newValue) -> {
			if (newValue instanceof SVColor) {
				colorSelect = ((SVColor) newValue).toJavaFXColor();
			}
			requestRender();
		});
		addValueListener(ConfigPropertyLookup.COLOR_SELECT2, (observer, oldValue, newValue) -> {
			if (newValue instanceof SVColor) {
				colorSelect2 = ((SVColor) newValue).toJavaFXColor();
			} else if (newValue == null) {
				colorSelect2 = null;
			}
		});


		addValueListener(ConfigPropertyLookup.COLOR_SELECT_BACKGROUND, (observer, oldValue, newValue) -> {
			if (newValue instanceof SVColor) {
				colorSelectBackground = ((SVColor) newValue).toJavaFXColor();
			} else if (newValue == null) {
				colorSelectBackground = null;
			}
			requestRender();
		});
		addValueListener(ConfigPropertyLookup.COLOR_SELECT_BACKGROUND2, (observer, oldValue, newValue) -> {
			if (newValue instanceof SVColor) {
				colorSelectBackground2 = ((SVColor) newValue).toJavaFXColor();
			} else if (newValue == null) {
				colorSelectBackground2 = null;
			}
		});


		addValueListener(ConfigPropertyLookup.PERIOD, (observer, oldValue, newValue) -> {
			if (newValue instanceof SVNumericValue) {
				period = ((SVNumericValue) newValue).toDouble();
				periodAlternator.setAlternateMillis((long) (period * 1000));
			}
		});

		addValueListener(ConfigPropertyLookup.ROW_HEIGHT, (observer, oldValue, newValue) -> {
			if (newValue instanceof SVNumericValue) {
				rowHeight = ((SVNumericValue) newValue).toDouble();
			}
		});


		{
			ConfigClass scrollBar = myControl.findNestedClass(ListboxControl.NestedClassName_ListScrollBar);

			scrollbarRenderer = new ScrollbarRenderer(scrollBar, this,
					ConfigPropertyLookup.THUMB, ConfigPropertyLookup.ARROW_FULL,
					ConfigPropertyLookup.ARROW_EMPTY, ConfigPropertyLookup.BORDER,
					ConfigPropertyLookup.COLOR
			);
		}
	}

	public void paint(@NotNull GraphicsContext gc, CanvasContext canvasContext) {
		boolean preview = paintPreview(canvasContext);

		if (!isEnabled()) {
			Color oldTextColor = textRenderer.getTextColor();
			super.paint(gc, canvasContext);
			textRenderer.setTextColor(colorDisabled);
			textRenderer.paint(gc);
			textRenderer.setTextColor(oldTextColor);
		} else {
			if (preview) {
				blinkControlHandler.paint(gc);
				if (this.mouseOver) {
					canvasContext.paintLast(tooltipRenderFunc);
				}
			}
			super.paint(gc, canvasContext);
			int controlHeight = getHeight();
			int allTextHeight = 0;
			final int textPadding = (int) (getWidth() * 0.025);
			int leftTextX = x1 + textPadding;
			int textHeight = textRenderer.getTextLineHeight();
			double ratio = periodAlternator.updateAndGetRatio();
			final int rowHeight = getRowPixelHeight();

			scrollbarRenderer.paint(gc, preview, x2 - ScrollbarRenderer.SCROLLBAR_WIDTH, y1, controlHeight);

			gc.beginPath();
			gc.rect(x1, y1, getWidth() - ScrollbarRenderer.SCROLLBAR_WIDTH, getHeight());
			gc.closePath();
			gc.clip();
			int row = 0;
			int selectedRow = preview ? this.selectedRow : 0;
			while (allTextHeight <= controlHeight && textHeight > 0) { //<= to make sure text goes out of bounds of menu to force scrollbar
				int textY2 = y1 + allTextHeight;
				if (row == selectedRow) {
					if (colorSelectBackground != null) {
						Color bgColor = colorSelectBackground;
						if (focused && colorSelectBackground2 != null) {
							bgColor = colorSelectBackground.interpolate(colorSelectBackground2, ratio);
						}
						gc.setStroke(bgColor);
						Region.fillRectangle(gc, x1, y1 + allTextHeight, x2, textY2 + rowHeight);
					}
					Color tColor = colorSelect;
					if (focused && colorSelect2 != null) {
						tColor = colorSelect2.interpolate(colorSelect, ratio);
					}
					Color oldTextColor = textRenderer.getTextColor();
					textRenderer.setTextColor(tColor);
					textRenderer.paint(gc, leftTextX, textY2);
					textRenderer.setTextColor(oldTextColor);

				} else {
					textRenderer.paint(gc, leftTextX, textY2);
				}
				allTextHeight += rowHeight;
				row++;
			}
		}
	}

	@NotNull
	public Color getTextColor() {
		return textRenderer.getTextColor();
	}

	@Override
	public boolean canHaveFocus() {
		return true;
	}

	@Override
	public void mousePress(@NotNull MouseButton mb) {
		super.mousePress(mb);
		if (mouseButtonDown != MouseButton.PRIMARY) {
			return;
		}
		this.selectedRow = Math.abs(this.mouseOverY - this.y1) / getRowPixelHeight();
	}

	private int getRowPixelHeight() {
		return textRenderer.getTextLineHeight() + (int) Math.max(0, this.rowHeight * resolution.getViewportHeight());
	}
}
