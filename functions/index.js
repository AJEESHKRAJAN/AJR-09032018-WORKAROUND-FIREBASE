const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });


let admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref('/chatrooms/{chatroomId}/chatroom_messages/{chatMessageId}')
.onWrite(event =>{
	
	//get the message that was written
	let message = event.data.child('message').val();
	let messageUserId = event.data.child('user_id').val();
	let chatroomId = event.params.chatroomId;
	console.log("message : ", message);
	console.log("user_id : ", messageUserId);
	console.log("chatroom_id : ", chatroomId);
	
	return event.data.ref.parent.parent.once('value').then(snap => {
		let data = snap.child('users').val();
		console.log('data : ', data);
		
		//get the number of users in chatroom
		let length=0;
		for(value in data){
			length++;
		}
		console.log('data length', length);
		
		
		let tokens =[];
		let i=0;
		for(var user_id in data){
			console.log('user_id : ',user_id);
			
			//get the token and add it to the array 
			//loop through each user currently in the chatroom
			let reference = admin.database().ref("/users/" + user_id);
			console.log("reference : " ,reference);
			
			reference.once('value').then(snap =>{
				let token = snap.child('messaging_token').val();
				console.log('messaging token : ', token);
				tokens.push(token);
				i++;
				
				//also check to see if the user_id we're viewing is the user who posted the message
				//if it is, then save that name so we can pre-pend it to the message
				let messageUserName = "";
				if(snap.child('user_id').val() === messageUserId){
					messageUserName = snap.child('name').val();
					console.log('message user name : ' , messageUserName);
					
					message = messageUserName + ' : ' + message;
				}
				
				
				//Once the last user in the list has been added we can continue
				if(i === length){
					console.log("Constructing the notification message. ");
					
					const payload = {
						data:{
							data_type : "data_type_chat_message",
							title : "AjSys",
							message : message,
							chatroom_id: chatroomId
						}
					};
					
					return admin.messaging().sendToDevice(tokens,payload)
					.then(function(response){
						// See the MessagingDevicesResponse reference documentation for
					    // the contents of response.
						console.log("Successfully sent the messages. " , response);
						return getPromise();
					})
					.catch(function(error){
						console.log("Error sending messages. " , error);
						throw new Error(error)
					});					
				}
				return getPromise();
			})
			.catch(function(error){
				console.log("Error sending messages. " , error);
				throw new Error(error)
			});
			
		}
		return getPromise();
	})
	.catch(function(error){
		console.log("Error sending messages. " , error);
		throw new Error(error)
	});
});