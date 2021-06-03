const socket = new SockJS('/gs-guide-websocket');
let stompClient = Stomp.over(socket);
let stompSubs = {
    newMessageSource : null,
    historyLoad : null,
    activeGames : null
};

stompClient.connect({}, (frame) => {
    console.log('Connected: ' + frame);
    stompSubs.newMessageSource = stompClient.subscribe('/topic/greetings',
        ans => {
        let ansObj = JSON.parse(ans.body);
        createMessageElem(ansObj);
    });
    stompSubs.historyLoad = stompClient.subscribe('/topic/history-load',
        ans => {
            let ansObj = JSON.parse(ans.body);
            ansObj.map(ms => {
                createMessageElem(ms);
            });
            stompSubs.historyLoad.unsubscribe('/topic/history-load');
        });
    stompSubs.activeGames = stompClient.subscribe("/topic/active-games",
        ans =>{
            let ansObj = JSON.parse(ans.body);
            let lobby = $("<div>").addClass("lobby")
                .html(ansObj.gameName)
                .attr("data-game-id", ansObj.gameId)
                .on("dblclick", (e)=> {
                    alert(1);
                });
            lobby.appendTo($("#games-list"));
        });
    stompClient.send(
        "/app/history-load-request",
        {},
        "payload"
    );
});
const myModal = new HystModal({
    linkAttributeName: 'data-hystmodal',
    catchFocus: true,
    waitTransitions: true,
    closeOnEsc: true,
    beforeOpen: function(modal){
        console.log('Message before opening the modal');
        console.log(modal); //modal window object
    },
    afterClose: function(modal){
        console.log('Message after modal has closed');
        $('input[name="ban-option"]:checked').prop('checked', false);
        $('#ban-text').val("");
        console.log(modal); //modal window object
    },
});
$('#chat-input').on('keypress', (e) => {
    if(e.which === 13){

        //Disable textbox to prevent multiple submit
        $(this).attr("disabled", "disabled");

        //Do Stuff, submit, etc..
        $("#chat-btn").click();

        //Enable the textbox again if needed.
        $(this).removeAttr("disabled");
        $("#chat-input").focus();
    }
});
$("#chat-btn").click(()=>{
        let message = $("#chat-input").val();
        if (message.length === 0 || !message.trim()){
            message.val("");
            return;
        }
        $("#chat-input").val("");
        stompClient.send(
            "/app/hello",
            {},
            JSON.stringify({'message': message})
        );
    });
$("#send-ticket-btn").bind("click", ()=>{
    let usernameToBan = $("#banWindow").attr("data-username");
    let theme = $('input[name="ban-option"]:checked').val();
    let text = $('#ban-text').val();
    if (theme === undefined){
        alert("Выберете тему!");
        return;
    }
    if (text.length  < 4){
        alert("Опишите ваше недовольство подробнее!");
        return;
    }
    $.ajax({
        url: '/social/send-ticket',
        type: 'POST',
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
            "username" : usernameToBan,
            "theme" : theme,
            "text": text
        }
    });
    myModal.close();
});
$(".friend").bind("contextmenu", (event) => {
    // Avoid the real one
    event.preventDefault();
    let contextMenu = $(".custom-menu");

    $("<li>").addClass("context-action")
        .text("Профиль")
        .attr("data-action", "profile")
        .attr("data-username", $(this).attr("username"))
        .appendTo(contextMenu);
    $("<li>").addClass("context-action")
        .text("Удалить")
        .attr("data-action", "delete")
        .attr("data-username", $(this).attr("username"))
        .appendTo(contextMenu);
    // Show contextmenu
    doBindsFriendsContextMenu();
    contextMenu.finish().toggle(100).
    // In the right position (the mouse)
    css({
        position: "absolute",
        top: event.pageY + "px",
        left: event.pageX + "px"
    });
});
// If the document is clicked somewhere
$(document).bind("mousedown", (e) => {

    // If the clicked element is not the menu
    if (!$(e.target).parents(".custom-menu").length > 0) {

        // Hide it
        $(".custom-menu").empty().hide(100);
    }
});
$(document).ready((e) =>{
    loadActiveGames();
    console.log(11111);
});
function loadActiveGames(){
    $.ajax({
        url: '/games/load-active-games',
        type: 'GET',
        success: (data, status, _) => {
            console.log("Loaded lobby list");
            data.map(
                lobbyData => {
                    let lobby = $("<div>").addClass("lobby")
                        .html(lobbyData.gameName)
                        .attr("data-game-id", lobbyData.gameId)
                        .on("dblclick", (e)=> {
                            alert(1);
                        });
                    lobby.appendTo($("#games-list"));
                }
            );
        },
        error: (x, y, _) => {
            console.log("Error: Loaded lobby list");
        },
        complete: (x, y) => {
            //console.log("complete");
        }
    });
}
$("#create-game-btn").click(()=>{
    let gameName = $("#game-name").val();
    $.ajax({
        type: "POST",
        url: "/games/create-game",
        dataType : "json",
        data : {"gameName" : gameName},
        success: function(data, textStatus) {
            window.location.href = "/game/" + data.gameId;
        },
        complete: (x, y) => {
            console.log(x);
            console.log(y);
        }
    });
});
const doBindsFriendsContextMenu  = () =>{
    // If the menu element is clicked
    $(".custom-menu li").click(function(){

        // This is the triggered action name
        if ($(this).attr("data-action") === "delete") {
            deleteFriend($(this).attr("data-username"))
        }
        else if ($(this).attr("data-action") === "profile"){
            window.location.href = "/profile/" + $(this).attr("data-username");
        }

        // Hide it AFTER the action was triggered
        $(".custom-menu").empty().hide(100);
    });
};
const addFriend = (username) =>{
    $.ajax({
        url: '/social/add-friend',
        type: 'PATCH',
        success: (x, y, _) => {
            console.log("success");
        },
        error: (x, y, _) => {
            console.log("error");
        },
        complete: (x, y) => {
            console.log("complete");
        },
        dataType : "json",
        data : {"username" : username}
    });
    //let d = $("<div>").
};
const deleteFriend = (username) => {
    $.ajax({
        url: '/social/delete-friend',
        type: 'DELETE',
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
        data : {"username" : username}
    });
    $(".friend[username="+ username +"]").remove();
};
const createMessageElem = (data) =>{
    let messageView = data.from + ": " + data.text;
    let d = $("<div>").addClass("chat-message")
        .html(messageView)
        .attr("data-username", data.username)
        .appendTo($("#chat"));
    console.log(d.attr("data-username"));
    bindChatContextMenu(d);
};
const bindChatContextMenu = (elem) =>{
    elem.bind("contextmenu", (event)=>{
        // Avoid the real one
        if ($("#your-username").val() === elem.attr("data-username"))
            return;
        event.preventDefault();
        //console.log(elem.attr("data-username"));
        let contextMenu = $(".custom-menu");
        $("<li>").addClass("context-action")
            .text("Профиль")
            .attr("data-action", "profile")
            .attr("data-username", elem.attr("data-username"))
            .appendTo(contextMenu);
        $("<li>").addClass("context-action")
            .text("Добавить в друзья")
            .attr("data-action", "addFriend")
            .attr("data-username", elem.attr("data-username"))
            .appendTo(contextMenu);
        $("<li>").addClass("context-action")
            .text("Пожаловаться")
            .attr("data-action", "ticket")
            .attr("data-username", elem.attr("data-username"))
            .appendTo(contextMenu);
        // Show contextmenu
        doBindsChatContextMenu();
        contextMenu.finish().toggle(100).
        // In the right position (the mouse)
        css({
            position: "absolute",
            top: event.pageY + "px",
            left: event.pageX + "px"
        });
    });
};
const doBindsChatContextMenu = () =>{
    // If the menu element is clicked
    $(".custom-menu li").click(function(){

        // This is the triggered action name
        if ($(this).attr("data-action") === "ticket") {
            $("#banWindow").attr("data-username", $(this).attr("data-username"));
            myModal.open("#banWindow")
        }
        else if ($(this).attr("data-action") === "profile"){
            window.location.href = "/profile/" + $(this).attr("data-username");
        }
        else if ($(this).attr("data-action") === "addFriend"){
            addFriend($(this).attr("data-username"));
        }

        // Hide it AFTER the action was triggered
        $(".custom-menu").empty().hide(100);
    });
};


