package com.theupnextapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.theupnextapp.R

@Composable
fun PosterAttributionItem() {
    Text(
        text = stringResource(id = R.string.tv_maze_creative_commons_attribution_text_single),
        modifier = Modifier.padding(
            start = 4.dp,
            top = 8.dp,
            bottom = 8.dp,
            end = 4.dp
        ),
        maxLines = 5,
        style = MaterialTheme.typography.caption
    )
}

@Composable
fun PosterTitleTextItem(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.caption,
        maxLines = 1,
        modifier = Modifier.padding(
            start = 4.dp,
            top = 8.dp,
            bottom = 8.dp,
            end = 4.dp
        )
    )
}

@Composable
fun SectionHeadingText(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        text = text,
        modifier = modifier
            .padding(
                start = 16.dp,
                top = 4.dp,
                end = 16.dp,
                bottom = 4.dp
            ),
        style = MaterialTheme.typography.h5,
        fontWeight = FontWeight.Bold
    )
}

@Preview
@Composable
fun SectionHeadingTextPreview() {
    SectionHeadingText(text = "Test Heading")
}