package com.fx.readingroom.domain

enum class ReadingRoom(val roomId: Int, val roomName: String) {
    ROOM1(1, "제1집중 학습 ZONE"),
    ROOM2(2, "제2집중 학습 ZONE"),
    ROOM3(3, "제3협업 학습 ZONE"),;

    companion object {
        fun from(roomId: Int): ReadingRoom =
            entries.find { it.roomId == roomId }
                ?: throw IllegalArgumentException("Unknown roomId: $roomId")
    }
    
}