package ee.merelaager.gossip.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: String,
    val title: String,
    val content: String?,
    val imageId: String?,
    val createdAt: String,
    val published: Boolean,
    val likeCount: Int,
    val isLiked: Boolean
)
