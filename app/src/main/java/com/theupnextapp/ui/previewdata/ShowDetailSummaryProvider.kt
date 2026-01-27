/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.ui.previewdata

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.theupnextapp.domain.ShowDetailSummary

class ShowDetailSummaryProvider : PreviewParameterProvider<ShowDetailSummary> {
    override val values: Sequence<ShowDetailSummary> =
        sequenceOf(
            SampleShowDetailSummary.summaryMinimal,
            SampleShowDetailSummary.summaryWithMissingData,
            SampleShowDetailSummary.summaryLongText,
        )
}

object SampleShowDetailSummary {
    val summaryMinimal =
        ShowDetailSummary(
            airDays = "Mondays",
            averageRating = "8.5",
            id = 1,
            imdbID = "tt1234567",
            genres = "Drama, Sci-Fi",
            language = "English",
            mediumImageUrl = "http://example.com/medium.jpg",
            name = "Awesome Show Title",
            originalImageUrl = "http://example.com/original.jpg",
            summary = "This is a captivating summary of an awesome TV show that will keep you on the edge of your seat. It involves drama, science fiction, and maybe even a cat.",
            time = "20:00",
            status = "Running",
            previousEpisodeHref = "/episodes/122",
            nextEpisodeHref = "/episodes/124",
            nextEpisodeLinkedId = 124,
            previousEpisodeLinkedId = 122,
        )

    val summaryWithMissingData =
        ShowDetailSummary(
            airDays = null,
            averageRating = "7.0",
            id = 2,
            imdbID = "tt7654321",
            genres = "Comedy",
            language = "English",
            mediumImageUrl = null,
            name = "Funny Show",
            originalImageUrl = null,
            summary = "A hilarious comedy series.",
            time = "21:00",
            status = "Ended",
            previousEpisodeHref = null,
            nextEpisodeHref = null,
            nextEpisodeLinkedId = null,
            previousEpisodeLinkedId = null,
        )

    val summaryLongText =
        ShowDetailSummary(
            airDays = "Daily at Noon and Midnight",
            averageRating = "9.9",
            id = 3,
            imdbID = "tt9876543",
            genres = "Action, Adventure, Fantasy, Thriller, Mystery, Romance, Animation for Adults",
            language = "Klingon",
            mediumImageUrl = "http://example.com/medium_long.jpg",
            name = "The Most Epic Saga Ever Told On Television With A Very Long Title",
            originalImageUrl = "http://example.com/original_long.jpg",
            summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt.",
            time = "Varies",
            status = "In Production",
            previousEpisodeHref = "/episodes/1022",
            nextEpisodeHref = "/episodes/1024",
            nextEpisodeLinkedId = 1024,
            previousEpisodeLinkedId = 1022,
        )
}
