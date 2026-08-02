package com.curio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateTopPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.curio.app.ui.theme.paperBorder
import com.curio.app.ui.theme.paperRule
import com.curio.app.ui.theme.paperSurface

/**
 * A note-paper card — the quotes entry's surface instead of the category
 * tint. Warm cream paper in light mode, warm off-black "toned paper" in dark,
 * with faint horizontal ruled lines (notebook texture) and a soft hairline
 * edge. [rotation] keeps the hand-placed notecard feel in the saved view.
 *
 * [contentPadding] defaults to a COMPACT inset (12dp) so quote cards stay
 * tight; pass a larger value for the journal page.
 */
@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    ruled: Boolean = true,
    rotation: Float = 0f,
    corner: Dp = 14.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(corner),
        color = paperSurface(),
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, paperBorder()),
        modifier = modifier.rotate(rotation)
    ) {
        Box {
            // Faint ruled lines behind the content — the notebook texture.
            // (paperRule() is @Composable, so resolve it here in the
            // composable scope — the Canvas draw lambda is not composable.)
            val ruleColor = if (ruled) paperRule() else Color.Unspecified
            if (ruled) {
                // Notebook cadence: rules spaced at the body line height,
                // starting one cadence below the top content padding so the
                // first line of text sits ON the first rule (real paper).
                val density = LocalDensity.current
                // Guard against an Unspecified lineHeight (custom typography
                // that omits it) — fall back to the classic 24dp cadence.
                val bodyLineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                val ruleSpacing = with(density) {
                    if (bodyLineHeight.isUnspecified) 24.dp.toPx() else bodyLineHeight.toPx()
                }
                val ruleStart = with(density) {
                    contentPadding.calculateTopPadding().toPx()
                } + ruleSpacing
                Canvas(modifier = Modifier.matchParentSize()) {
                    var y = ruleStart
                    while (y < size.height) {
                        drawLine(
                            color = ruleColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += ruleSpacing
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                content = content
            )
        }
    }
}
