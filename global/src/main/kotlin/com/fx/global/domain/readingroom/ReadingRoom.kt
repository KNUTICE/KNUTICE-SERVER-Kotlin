package com.fx.global.domain.readingroom

enum class ReadingRoom(val roomId: Int) {
    ROOM1(1),
    ROOM2(2),
    ROOM3(3);

    companion object {
        fun from(roomId: Int): ReadingRoom =
            entries.find { it.roomId == roomId }
                ?: throw IllegalArgumentException("Unknown roomId: $roomId")
    }
    
}