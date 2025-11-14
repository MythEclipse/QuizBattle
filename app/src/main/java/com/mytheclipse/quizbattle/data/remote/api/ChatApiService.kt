package com.mytheclipse.quizbattle.data.remote.api

import com.mytheclipse.quizbattle.data.remote.model.UserResponse
import retrofit2.http.*

interface ChatApiService {
    
    @GET("api/chat/rooms")
    suspend fun getChatRooms(): ChatRoomsResponse
    
    @POST("api/chat/rooms")
    suspend fun createChatRoom(@Body request: CreateRoomRequest): CreateRoomResponse
    
    @GET("api/chat/rooms/{roomId}/messages")
    suspend fun getRoomMessages(
        @Path("roomId") roomId: String,
        @Query("limit") limit: Int? = 50,
        @Query("before") before: String? = null
    ): RoomMessagesResponse
    
    @POST("api/chat/rooms/{roomId}/messages")
    suspend fun sendMessage(
        @Path("roomId") roomId: String,
        @Body request: SendMessageRequest
    ): SendMessageResponse
    
    @POST("api/chat/rooms/{roomId}/join")
    suspend fun joinRoom(@Path("roomId") roomId: String): JoinRoomResponse
    
    @POST("api/chat/rooms/{roomId}/leave")
    suspend fun leaveRoom(@Path("roomId") roomId: String): LeaveRoomResponse
    
    @DELETE("api/chat/messages/{messageId}")
    suspend fun deleteMessage(@Path("messageId") messageId: String): DeleteMessageResponse
}

data class ChatRoomResponse(
    val id: String,
    val name: String,
    val description: String?,
    val isPrivate: Int,
    val createdAt: String,
    val updatedAt: String,
    val members: List<RoomMemberResponse>,
    val messages: List<ChatMessageResponse>
)

data class RoomMemberResponse(
    val id: String,
    val roomId: String,
    val userId: String,
    val role: String,
    val user: UserResponse
)

data class ChatMessageResponse(
    val id: String,
    val roomId: String,
    val userId: String,
    val content: String,
    val createdAt: String,
    val user: UserResponse
)

data class ChatRoomsResponse(
    val success: Boolean,
    val rooms: List<ChatRoomResponse>
)

data class CreateRoomRequest(
    val name: String,
    val description: String? = null,
    val isPrivate: Boolean = false
)

data class CreateRoomResponse(
    val success: Boolean,
    val room: ChatRoomResponse
)

data class RoomMessagesResponse(
    val success: Boolean,
    val messages: List<ChatMessageResponse>
)

data class SendMessageRequest(
    val content: String
)

data class SendMessageResponse(
    val success: Boolean,
    val message: ChatMessageResponse
)

data class JoinRoomResponse(
    val success: Boolean,
    val message: String,
    val member: RoomMemberResponse
)

data class LeaveRoomResponse(
    val success: Boolean,
    val message: String
)

data class DeleteMessageResponse(
    val success: Boolean,
    val message: String
)
