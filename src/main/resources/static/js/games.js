const socket = new SockJS('/gs-guide-websocket');
let stompClient = Stomp.over(socket);
let stompSubs = {
    "newMessageSource" : null,
    "historyLoad" : null,
};
stompClient.connect({}, (frame) => {
    console.log('Connected: ' + frame);
    stompSubs.newMessageSource = stompClient.subscribe('/topic/greetings',
        ans => {
        let ansObj = JSON.parse(ans.body);
        let messageView = ansObj.from + ": " + ansObj.text;
        $("<div>").addClass("chat-message").html(messageView).appendTo($("#chat"));
    });
    stompSubs.historyLoad = stompClient.subscribe('/topic/history-load',
        ans => {
            let ansObj = JSON.parse(ans.body);
            ansObj.map(ms => {
                let messageView = ms.from + ": " + ms.text;
                $("<div>").addClass("chat-message")
                    .html(messageView)
                    .attr("data-username", ms.from)
                    .appendTo($("#chat"));
            });
            stompSubs.historyLoad.unsubscribe('/topic/history-load');
        });
    stompClient.send(
        "/app/history-load-request",
        {},
        "payload"
    );
});
$('#chat-input').on('keypress', function (e) {
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
        $("#chat-input").val("");
        stompClient.send(
            "/app/hello",
            {},
            JSON.stringify({'message': message})
        );
    });

$(".friend").bind("contextmenu", function (event) {

    // Avoid the real one
    event.preventDefault();
    let contextMenu = $(".custom-menu");

    $("<li>").addClass("context-action")
        .text("Profile")
        .attr("data-action", "profile")
        .attr("data-username", $(this).attr("username"))
        .appendTo(contextMenu);
    $("<li>").addClass("context-action")
        .text("Remove")
        .attr("data-action", "delete")
        .attr("data-username", $(this).attr("username"))
        .appendTo(contextMenu);
    // Show contextmenu
    console.log()
    doBindsFriendsContextMenu();
    contextMenu.finish().toggle(100).
    // In the right position (the mouse)
    css({
        position: "absolute",
        top: event.pageY + "px",
        left: event.pageX + "px"
    });
});

$(".chat-message").bind("contextmenu", ()=>{
    // Avoid the real one
    event.preventDefault();
    let contextMenu = $(".custom-menu");

    $("<li>").addClass("context-action")
        .text("Profile")
        .attr("data-action", "profile")
        .attr("data-username", $(this).attr("username"))
        .appendTo(contextMenu);
    $("<li>").addClass("context-action")
        .text("Пожаловаться")
        .attr("data-action", "ticket")
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
$(document).bind("mousedown", function (e) {

    // If the clicked element is not the menu
    if (!$(e.target).parents(".custom-menu").length > 0) {

        // Hide it
        $(".custom-menu").empty().hide(100);
    }
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
const doBindsChatContextMenu  = () =>{
    // If the menu element is clicked
    $(".custom-menu li").click(function(){

        // This is the triggered action name
        if ($(this).attr("data-action") === "ticket") {
            ($(this).attr("data-username"))
        }
        else if ($(this).attr("data-action") === "profile"){
            window.location.href = "/profile/" + $(this).attr("data-username");
        }

        // Hide it AFTER the action was triggered
        $(".custom-menu").empty().hide(100);
    });
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




