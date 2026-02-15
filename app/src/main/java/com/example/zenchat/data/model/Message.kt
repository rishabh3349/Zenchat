package com.example.zenchat.data.model

//class Message {
//    var message:String?=null
//    var time:String?=null
//    var senderId :String?=null
//
//    constructor(){}
//    constructor(message: String?,time:String?,senderId:String?){
//        this.message=message
//        this.time=time
//        this.senderId=senderId
//    }
//}
data class Message(
    var message:String?=null,
    var time:String?=null,
    var senderId :String?=null
)