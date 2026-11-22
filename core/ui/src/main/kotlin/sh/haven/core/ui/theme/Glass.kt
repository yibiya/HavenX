package sh.haven.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/**
 * Static translucent chrome: a single tinted highlight fill and a hairline rim.
 * The backdrop remains visible through the fill; this does not blur or refract it.
 */
@Composable
fun Modifier.glassSurface(selected: Boolean = false): Modifier {
    val colors = MaterialTheme.colorScheme
    val shape = remember { RoundedCornerShape(24.dp) }
    val fill = remember(colors, selected) {
        val light = colors.surface.luminance() > 0.5f
        val base = colors.primary.copy(alpha = if (selected) 0.20f else 0.10f)
            .compositeOver(colors.surface.copy(alpha = if (light) 0.48f else 0.42f))
        fun highlight(alpha: Float) = Color.White.copy(alpha = alpha).compositeOver(base)
        Brush.linearGradient(
            0.00f to highlight(if (light) 0.52f else 0.24f),
            0.06f to highlight(if (light) 0.30f else 0.12f),
            0.20f to base,
            0.62f to colors.secondary.copy(alpha = 0.05f).compositeOver(base),
            0.90f to base,
            1.00f to highlight(if (light) 0.28f else 0.10f),
        )
    }
    val rim = remember(colors, selected) {
        val light = colors.surface.luminance() > 0.5f
        Brush.linearGradient(
            0.00f to Color.White.copy(alpha = if (light) 0.95f else 0.62f),
            0.22f to colors.primary.copy(alpha = if (selected) 0.55f else 0.32f),
            0.55f to colors.outlineVariant.copy(alpha = 0.16f),
            0.82f to colors.secondary.copy(alpha = 0.28f),
            1.00f to Color.White.copy(alpha = if (light) 0.70f else 0.32f),
        )
    }
    return clip(shape)
        .background(fill)
        .border(1.dp, rim, shape)
}

/**
 * Shared, opaque palette backdrop for the navigation root. Apply once behind the
 * destination content, not to each glass panel. Its broad color bands show through
 * translucent chrome without offscreen rendering, blur, or animation.
 */
@Composable
fun Modifier.glassBackdrop(): Modifier {
    val colors = MaterialTheme.colorScheme
    val backdrop = remember(colors) {
        val light = colors.surface.luminance() > 0.5f
        val base = colors.background.copy(alpha = 1f)
        fun tint(color: Color, alpha: Float) = color.copy(alpha = alpha).compositeOver(base)
        Brush.linearGradient(
            0.00f to tint(colors.primary, if (light) 0.30f else 0.34f),
            0.28f to tint(colors.tertiary, if (light) 0.20f else 0.22f),
            0.52f to base,
            0.76f to tint(colors.secondary, if (light) 0.24f else 0.28f),
            1.00f to tint(colors.primary, if (light) 0.18f else 0.22f),
        )
    }
    return background(backdrop)
}

/** Navigation-root wrapper; destination backgrounds must remain transparent. */
@Composable
fun GlassBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.glassBackdrop(), content = content)
}

@Composable
fun GlassPanel(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.glassSurface(), content = content)
}
