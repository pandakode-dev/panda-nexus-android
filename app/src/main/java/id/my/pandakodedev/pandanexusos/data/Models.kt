package com.pandakode.nexus.data

data class QueueItem(
    val id: Int,
    val branch_id: Int,
    val queue_code: String,
    val customer_name: String,
    val customer_ref: String,
    val current_stage: Int,
    val estimated_price: Double,
    val items_summary: String
)

data class TvFeedResponse(
    val status: String,
    val active_count: Int,
    val latest_called: QueueItem?,
    val queues: List<QueueItem>
)