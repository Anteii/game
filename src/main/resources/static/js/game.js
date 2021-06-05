
class Message{
    constructor(type, message){
        this.type = type;
        this.message = message;
    }
}
const socket = new SockJS('/gs-guide-websocket');
const myModal = new HystModal({
    linkAttributeName: 'data-hystmodal',
    catchFocus: true,
    waitTransitions: true,
    closeOnEsc: false,
    closeOnOverlay: false,
    closeObButton: false,
    beforeOpen: function(modal){
        console.log('Message before opening the modal');
        console.log(modal); //modal window object
    },
    afterClose: function(modal){
        console.log('Message after modal has closed');
        console.log(modal); //modal window object
        $(".question-window-content").empty();
    },
});
let timer = null;
let lobby = null;

let stompClient = Stomp.over(socket);
let stompSubs = {
    newMessageSource: null,
    gameEventSource: null,
    userDataUpdatesSource: null,
    questionDataSource: null,
    stateControlSource: null,
    scoreUpdatesScource: null
};
const getGameId = () =>{
    let url = window.location.href;
    let urlComponents = url.split("/");
    return urlComponents[urlComponents.length-1];
};

const udpatePlayers = (data) => {
    $(".player").remove();
    for(let playerData of data){
        addPlayer(playerData);
    }
};

stompClient.connect({}, (frame) => {
    console.log('Connected: ' + frame);
    stompSubs.userDataUpdatesSource = stompClient.subscribe(
        "/topic/game/" + getGameId() + "/update-game-users-data",
        ans =>{
            let data = JSON.parse(ans.body);
            lobby = data;
            console.log(data);
            udpatePlayers(data["players"]);
            setHost(data["host"]);
            setCaptain(data["captain"]);
            setSettings();
            if (stompSubs.questionDataSource == null){
                subscribeToQuestions();
            }
        });
    stompSubs.stateControlSource = stompClient.subscribe(
        "/topic/game/" + getGameId() + "/change-state",
        ans =>{
            let data = ans.body;
            changeState(data);
        });
    stompSubs.scoreUpdatesScource = stompClient.subscribe(
        "/topic/game/" + getGameId() + "/get-score",
        ans =>{
            let data = ans.body;
            setScore(data);
        });
    $.ajax({
        url: "/game/"+getGameId()+"/request-update-game-users-data",
        type: 'GET',
        success: (data, y, _) => {
            lobby = data;
            console.log(data);
            setHost(data["host"]);
            udpatePlayers(data["players"]);
            setCaptain(data["captain"]);
            setSettings();
            if (stompSubs.questionDataSource == null){
                subscribeToQuestions();
            }
        },
        dataType : "json",
        data : {}
    });
});
$(document).ready(() => {
    console.log("Sup 2ch");
    updateUserStatus("GAME");
});

function loadPlayers(){
    let gameId = getGameId();
    $.ajax({
        type: "GET",
        url: "/game/" + gameId + "/get-players",
        success: function(data, textStatus) {
            data.map(
                playerData => {
                    addPlayer(playerData);
                }
            );
        },
        complete: (x, y) => {
            console.log(x);
            console.log(y);
        }
    });
}
$("#main-button").on("click", ()=>{
    if (isHost(getYourUsername())){
        if (roulette.isActive){
            roulette.isActive = false;
            nextQuestion();
        }
    }
});
$("#start-game").on("click", ()=>{
    if (isHost(getYourUsername())){
        $("#start-game").css("display", "none");
        startGame();
    }
});
const subscribeToQuestions = () => {
    let role = (isHost(getYourUsername()) ? "host" : "player");
    stompSubs.questionDataSource = stompClient.subscribe(
        "/topic/game/" + getGameId() + "/next-question-for-"+role,
        ans =>{
            let data = JSON.parse(ans.body);
            movePointerToPoint(new Point(
                roulette.radius + roulette.radius * Math.cos(data["angle"]),
                roulette.radius + roulette.radius * Math.sin(data["angle"])
            ));
            setTimeout(()=>{
                roulette.isActive = true;
                excludeSector(data["sector"]);
                if (isHost(getYourUsername())){
                    displayQuestion(data["text"], data["answer"]);
                }
                else{
                    displayQuestion(data["text"]);
                }
            }, 11000);
            console.log(data);
        });
};
const updateUserStatus = (status) =>{
    $.ajax({
        url: '/social/update-user-status',
        type: 'PATCH',
        success: (x, y, _) => {
            console.log("success");
        },
        error: (x, y, _) => {
            //console.log("error");
        },
        complete: (x, y) => {
            //console.log("complete");
        },
        dataType : "json",
        data : {
            "status" : status
        }
    });
};
const setHost = (data) =>{
    $(".host").remove();
    if (data["username"] == null){
        return;
    }

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
};
const setCaptain = (data) => {
    addPlayer(data, true);
};
const addPlayer = (data, captain=false) =>{
    if (data["username"] == null){
        return;
    }

    let playerDiv = $("<div>").addClass("player");
    if (captain){
        playerDiv.addClass("captain");
    }
    let playerInfoDiv = $("<div>").addClass("player-info");
    let playerNicknameDiv = $("<div>").addClass("player-nickname");
    let playerStatsDiv = $("<div>").addClass("player-stats");
    let playerImageDiv =  $("<img>").addClass("player-image");

    playerImageDiv.attr("src", "/images/profiles_pictures/" + data.pictureName);
    playerDiv.attr("data-username", data.username);
    playerNicknameDiv.text(data.nickname);

    playerStatsDiv.appendTo(playerInfoDiv);
    playerNicknameDiv.appendTo(playerInfoDiv);
    playerImageDiv.appendTo(playerDiv);
    playerInfoDiv.appendTo(playerDiv);
    playerDiv.appendTo($(".players-list"));
};
const setPlayerSettings = () => {
    $("#start-game").css("display", "none");
    $("#main-button").css("display", "none");
};
const setHostSettings = () => {
    $("#main-button").val("Следующий вопрос");
};
const setCaptainSettings = () => {
    $("#start-game").css("display", "none");
    $("#main-button").css("display", "none");
};
const setSettings = () => {
    let username = getYourUsername();
    if (isHost(username)){
        setHostSettings();
    }
    else if (isCaptain(username)){
        setCaptainSettings();
    }
    else{
        setPlayerSettings();
    }
};
const removePlayer = (username) =>{
    $(".player").filter( (index)=> {
        return $(this, "data-username") === username;
    }).remove();
};
const isHost = (username) =>{
    return username === lobby["host"].username;
};
const isCaptain = (username) =>{
    return username === lobby["captain"].username;
};
const getYourUsername = () => {
    return $("#your-username").val();
};
const nextQuestion = () => {
    $.ajax({
        url: "/game/"+getGameId()+"/request-next-question",
        type: 'GET',
        success: (data, y, _) => {
            console.log(data);
        },
        dataType : "json",
        data : {}
    });
};
const startGame = () => {
    $.ajax({
        url: "/game/"+getGameId()+"/start-game",
        type: 'PATCH',
        success: (data, y, _) => {
            console.log(data);
        },
        dataType : "json",
        data : {}
    });
};
const displayQuestion = (text, answer=null) => {
    $("<div>").addClass("question-timer").html("60s").appendTo($(".question-window-content"));
    $("<div>").addClass("question-text").html("Вопрос:"+"<br>"+text).appendTo($(".question-window-content"));
    if (answer != null){
        $("<div>").addClass("question-answer").html("Ответ:"+"<br>"+answer).appendTo($(".question-window-content"));
        $("<button>").addClass("right-answer")
            .text("Ответ правильный")
            .click((e)=>{
                sendAnswer(true);
            })
            .appendTo($(".question-window-content"));
        $("<button>").addClass("wrong-answer")
            .text("Ответ неправильный")
            .click((e)=>{
                sendAnswer(false);
            })
            .appendTo($(".question-window-content"));
    }
    else if (isCaptain(getYourUsername())){
        $("<button>").addClass("submit-answer")
            .click((e)=>{
                submitAnswer();
            })
            .text("Ответить")
            .appendTo($(".question-window-content"));
    }
    myModal.open("#questionWindow");
    setTimer();
};
const sendAnswer = (isCorrect) =>{
    $.ajax({
        url: "/game/"+getGameId()+"/answer-question",
        type: 'PATCH',
        success: (data, y, _) => {
            console.log(data);
            changeState("IDLE");
        },
        dataType : "json",
        data : {
            "isCorrect" : isCorrect
        }
    });
};
const submitAnswer = () =>{
    answer();
};
const changeState = (state) => {
    if (state === "IDLE"){
        myModal.close();
        clearInterval(timer);
    }
    else if (state === "ANSWER"){
        $(".question-timer").html("ОТВЕЧАЕТ КОМАНДА ЗНАТОКОВ");
        $(".submit-answer").css("display", "none");
        clearInterval(timer);
    }
    else if (state === "END"){
        displayEndGamePanel();
    }
};
const displayEndGamePanel = () => {
    console.log("ENGAME");
    $("<div>").addClass("question-timer").html("60s").appendTo($(".question-window-content"));
    $("<div>").addClass("final-message").html("Поздравляем, ваша игра подошла к концу!").appendTo($(".question-window-content"));
    $("<button>").addClass("final-btn").text("Выйти")
        .click((e)=>{
            window.location.href = "/games";
        })
        .appendTo($(".question-window-content"));
    myModal.open("#questionWindow");
};
const answer = () => {
    $.ajax({
        url: "/game/"+getGameId()+"/change-state",
        type: 'PATCH',
        success: (data, y, _) => {
            console.log(data);
            changeState("IDLE");
        },
        dataType : "json",
        data : {
            "state" : "ANSWER"
        }
    });
};
const setTimer = () => {
    let timeToThink = 60 * 1000;
    let countDownTime = Date.now() + timeToThink;
    timer = setInterval(function() {
        let distance = countDownTime - Date.now();
        let seconds = Math.floor(distance / 1000);
        $(".question-timer").html(seconds+"s");
        // If the count down is finished, write some text
        if (distance <= 0) {
            $(".question-timer").html("ВРЕМЯ КОНЧИЛОСЬ");
            if (!isHost(getYourUsername())){
                submitAnswer();
            }
            clearInterval(timer);
        }
    }, 500);
};
const setScore = (score) => {
    $(".scoreboard").html(score);
};