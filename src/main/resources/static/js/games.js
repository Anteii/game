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
        createMessageElem(JSON.parse(ans.body));
    });
    stompSubs.historyLoad = stompClient.subscribe('/topic/history-load',
        ans => {
            stompSubs.historyLoad.unsubscribe('/topic/history-load');
            JSON.parse(ans.body).map(ms => {
                createMessageElem(ms);
            });
        });
    stompSubs.activeGames = stompClient.subscribe("/topic/active-games",
        ans =>{
            createGameElem(JSON.parse(ans.body));
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
        if (isEmptyString(message)){
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
$(".friend").bind("contextmenu", function(event) {
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
$("#create-game-btn").click(()=>{
    let gameName = $("#game-name").val();
    if (isEmptyString(gameName)){
        alert("Введите название лобби");
        return;
    }
    $.ajax({
        type: "POST",
        url: "/games/create-game",
        dataType : "json",
        data : {"gameName" : gameName},
        success: function(data, textStatus) {
            window.location.href = "/game/" + data.gameId;
        },
        error: (x, y, _) => {
            //console.log(x);
            //console.log(y);
            //console.log(_);
            if (x.status == 400){
                alert("Лобби с таким именем уже создано");
            }
            else{
                alert("Что-то пошло не так.. Обновите страницу");
                $("#game-name").val("");
            }
        },
        complete: (x, y) => {
            //console.log(x);
            //console.log(y);
        }
    });
});
$("#find-game-btn").click(()=>{
    let gameName = $("#game-name").val();
    let lobbyList = $(".lobby");
    lobbyList.attr("hidden", true);
    if (!isEmptyString(gameName)){
        lobbyList = lobbyList.filter(function( index ) {
            return $(this).text() === gameName;
        });
    }
    lobbyList.attr("hidden", false);
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
    updateUserStatus("MENU");
});
const loadActiveGames = () => {
    $.ajax({
        url: '/games/load-active-games',
        type: 'GET',
        success: (data, status, _) => {
            console.log("Loaded lobby list");
            data.map(
                lobbyData => {
                    createGameElem(lobbyData);
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
            console.log("successfully added to friend list");
        },
        error: (x, y, _) => {
            console.log("error when trying add to friend list");
        },
        complete: (x, y) => {
            //console.log("complete");
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
            console.log("successfully deleted from friends");
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
    bindChatContextMenu(d);
};
const bindChatContextMenu = (elem) =>{
    elem.bind("contextmenu", (event)=>{
        // Avoid the real one
        if ($("#your-username").val() === elem.attr("data-username"))
            return;
        event.preventDefault();
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
const isEmptyString = (s) =>{
    return s.length === 0 || !s.trim()
};
const createGameElem = (data) => {
    $("<div>").addClass("lobby")
        .html(data.gameName)
        .attr("data-game-id", data.gameId)
        .attr("tabindex", 0)
        .on("click", (e)=>{
            $(this).focus();
        })
        .on("dblclick", (e)=> {
            window.location.href = "/game/" + data.gameId
        }).appendTo($("#games-list"));
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
