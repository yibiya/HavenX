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
import androidx.compose.ui.unit.dp

/** Palette-driven glass chrome without blur passes or continuously running effects. */
@Composable
fun Modifier.glassSurface(selected: Boolean = false): Modifier {
    val colors = MaterialTheme.colorScheme
    val shape = remember { RoundedCornerShape(24.dp) }
    val background = remember(colors) {
        Brush.verticalGradient(listOf(
            colors.surfaceContainerHigh.copy(alpha = 0.94f),
            colors.surfaceContainer.copy(alpha = 0.86f),
        ))
    }
    return clip(shape)
        .background(background)
        .then(if (selected) Modifier.background(colors.primary.copy(alpha = 0.14f)) else Modifier)
        .border(1.dp, colors.outlineVariant.copy(alpha = 0.55f), shape)
}

@Composable
fun GlassPanel(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.glassSurface(), content = content)
}
