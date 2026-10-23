package com.example.wordpressclient.feature.posts.detail

import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wordpressclient.core.common.UiState
import com.example.wordpressclient.core.ui.BookmarkButton
import com.example.wordpressclient.core.ui.ErrorRetryMessage
import com.example.wordpressclient.core.ui.sharePost
import com.example.wordpressclient.domain.model.Post

/**
 * PostDetailScreen = màn hình đọc một bài viết (bản "có trạng thái").
 *
 * Lấy ViewModel từ Hilt, đọc state rồi giao cho PostDetailContent vẽ.
 *
 * @param onBackClick Gọi khi người dùng bấm nút quay lại. AppNavHost quyết định việc quay lại.
 */
@Composable
fun PostDetailScreen(
    onBackClick: () -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val savedPostIds by viewModel.savedPostIds.collectAsStateWithLifecycle()

    PostDetailContent(
        state = state,
        isSaved = (state as? UiState.Success)?.data?.id?.let { it in savedPostIds } == true,
        onBackClick = onBackClick,
        onRetry = viewModel::loadPost,
        onBookmarkClick = viewModel::toggleSaved
    )
}

/** Phần giao diện thuần: chỉ nhận state và vẽ ra, không phụ thuộc ViewModel hay Hilt. */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PostDetailContent(
    state: PostDetailUiState,
    isSaved: Boolean,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    val context = LocalContext.current
    val postToShare = (state as? UiState.Success)?.data

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    postToShare?.let { post ->
                        BookmarkButton(isSaved = isSaved, onClick = onBookmarkClick)
                        IconButton(
                            onClick = {
                                if (!context.sharePost(post.title, post.link)) {
                                    Toast.makeText(
                                        context,
                                        "Không thể chia sẻ bài viết.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Chia sẻ bài viết"
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state) {
                // Đang tải: vòng xoay giữa màn hình.
                is UiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                // Lỗi: thông báo kèm nút thử lại.
                is UiState.Error -> {
                    ErrorRetryMessage(
                        message = state.message,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Có dữ liệu: hiện nội dung bài viết.
                is UiState.Success -> PostBody(post = state.data)
            }
        }
    }
}

/** Nội dung bài viết: tiêu đề, ngày đăng và phần thân, cuộn được. */
@Composable
private fun PostBody(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = post.title,
            style = MaterialTheme.typography.headlineMedium
        )

        // Ngày đăng: tạm cắt 10 ký tự đầu của chuỗi ISO, ví dụ "2024-05-01".
        Text(
            text = post.date.take(10),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        HtmlText(html = post.contentHtml)
    }
}

/**
 * Hiển thị chuỗi HTML.
 *
 * Compose chưa có thành phần hiển thị HTML trực tiếp, nên ta dùng TextView của Android
 * (qua AndroidView) và để HtmlCompat chuyển HTML thành văn bản có định dạng.
 */
@Composable
private fun HtmlText(html: String, modifier: Modifier = Modifier) {
    // Lấy màu từ theme để chữ đúng ở cả giao diện sáng và tối.
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val linkColor = MaterialTheme.colorScheme.primary.toArgb()

    AndroidView(
        modifier = modifier,
        // factory chạy một lần: tạo TextView và cấu hình cố định.
        factory = { context ->
            TextView(context).apply {
                // Cho phép bấm vào đường link trong bài.
                movementMethod = LinkMovementMethod.getInstance()
                textSize = 17f
                // Giãn dòng 1.3 lần cho dễ đọc đoạn dài.
                setLineSpacing(0f, 1.3f)
            }
        },
        // update chạy khi nội dung hoặc màu đổi.
        update = { textView ->
            textView.text = HtmlCompat.fromHtml(
                html,
                HtmlCompat.FROM_HTML_MODE_LEGACY,
                CoilImageGetter(textView),
                null
            )
            textView.setTextColor(textColor)
            textView.setLinkTextColor(linkColor)
        }
    )
}