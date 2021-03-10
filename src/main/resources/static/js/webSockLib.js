export let connect = (stompClient) => {
    const socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/greetings', function (greeting) {
            showGreeting(JSON.parse(greeting.body));
        });
    });
};

export let disconnect = (stompClient) => {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
};

export let sendName = (stompClient) => {
    stompClient.send("/app/hello", {}, JSON.stringify({'name': $("#name").val()}));
};

export let showGreeting = (message) => {
    $("#greetings").append("<tr><td>" + message.from + "</td></tr>");
};
