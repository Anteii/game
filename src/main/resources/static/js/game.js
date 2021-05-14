
class Message{
    constructor(type, message){
        this.type = type;
        this.message = message;
    }
}
const socket = new SockJS('/gs-guide-websocket');

let stompClient = Stomp.over(socket);
let stompSubs = {
    newMessageSource: null,
    gameEventSource: null
};
$(document).ready(() => {
    console.log("Sup 2ch");

    loadHost();
    loadPlayers();
    loadChat();
    loadGame();
});

function loadPlayers(){
    let gameId = getGameID();
    $.ajax({
        type: "GET",
        url: "/game/" + gameId + "/get-players",
        success: function(data, textStatus) {
            data.map(
                playerData => {
                    let playerDiv = $("<div>").addClass("player");
                    let playerInfoDiv = $("<div>").addClass("player-info");
                    let playerNicknameDiv = $("<div>").addClass("player-nickname");
                    let playerStatsDiv = $("<div>").addClass("player-stats");
                    let playerImageDiv =  $("<img>").addClass("player-image");

                    playerImageDiv.attr("src", "/images/profiles_pictures/" + playerData.pictureName);
                    playerDiv.attr("data-username", playerData.username);
                    playerNicknameDiv.text(playerData.nickname);

                    playerStatsDiv.appendTo(playerInfoDiv);
                    playerNicknameDiv.appendTo(playerInfoDiv);
                    playerImageDiv.appendTo(playerDiv);
                    playerInfoDiv.appendTo(playerDiv);
                    playerDiv.appendTo($(".players-list"));
                }
            );
        },
        complete: (x, y) => {
            console.log(x);
            console.log(y);
        }
    });
}
function loadHost(){
    let gameId = getGameID();
    $.ajax({
        type: "GET",
        url: "/game/" + gameId + "/get-host",
        success: function(data, textStatus) {
            let hostDiv = $("<div>").addClass("host");
            let hostNicknameDiv = $("<div>").addClass("host-nickname");
            let hostImageDiv =  $("<img>").addClass("host-image");

            hostImageDiv
                .attr("src", "/images/profiles_pictures/" + data.pictureName)
                .attr("data-username", data.username);
            hostNicknameDiv.text(data.nickname);

            hostImageDiv.appendTo(hostDiv);
            hostNicknameDiv.appendTo(hostDiv);
            hostDiv.prependTo($(".host-area"));
        },
        complete: (x, y) => {
            console.log(x);
            console.log(y);
        }
    });
}
function loadChat(){

}

function loadGame(){

}

function getGameID(){
    let temp = window.location.href.split("/");
    return temp[temp.length-1];
}